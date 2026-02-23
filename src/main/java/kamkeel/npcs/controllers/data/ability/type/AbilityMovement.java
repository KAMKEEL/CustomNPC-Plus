package kamkeel.npcs.controllers.data.ability.type;

import kamkeel.npcs.controllers.data.ability.Ability;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.util.Vec3;

/**
 * Abstract base class for abilities that move the caster along the ground
 * in a specific direction (Charge, Dash, etc.).
 * <p>
 * Provides shared movement logic: direction locking, stall detection with
 * player-aware grace period, velocity application, distance tracking,
 * rotation enforcement, and wall collision checking.
 * <p>
 * NOT for ballistic/arc movement (Slam uses a different pattern).
 */
public abstract class AbilityMovement extends Ability {

    /**
     * Grace period (in active ticks) before stall detection activates.
     * Players need more time because:
     * - Server sets motionX/Z and sends S12 velocity packet to client
     * - Client receives S12 and starts moving
     * - Client sends C03 position packet with updated position back to server
     * Until the round trip completes, the server sees the old (stationary) position,
     * which would falsely trigger stall detection.
     */
    private static final int PLAYER_STALL_GRACE_TICKS = 8;
    private static final int NPC_STALL_GRACE_TICKS = 2;

    // ═══════════════════════════════════════════════════════════════════
    // COMMON MOVEMENT STATE (transient, not saved to NBT)
    // ═══════════════════════════════════════════════════════════════════

    protected transient double startX, startY, startZ;
    protected transient double prevTickX, prevTickZ;
    protected transient Vec3 movementDirection;
    protected transient float lockedYaw;
    protected transient int maxActiveTicks;

    // ═══════════════════════════════════════════════════════════════════
    // OVERRIDES
    // ═══════════════════════════════════════════════════════════════════

    @Override
    public boolean hasAbilityMovement() {
        return true;
    }

    @Override
    public void cleanup() {
        movementDirection = null;
        maxActiveTicks = 0;
    }

    @Override
    public void resetForBurst() {
        movementDirection = null;
        maxActiveTicks = 0;
    }

    // ═══════════════════════════════════════════════════════════════════
    // DIRECTION HELPERS
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Lock direction based on caster type.
     * NPC: toward target. Player: look direction.
     */
    protected void lockDirection(EntityLivingBase caster, EntityLivingBase target) {
        if (!isPlayerCaster(caster) && target != null) {
            lockDirectionToTarget(caster, target);
        } else {
            lockDirectionFromLook(caster);
        }
    }

    /**
     * Lock direction to the caster's look direction (horizontal only).
     */
    protected void lockDirectionFromLook(EntityLivingBase caster) {
        float yaw = (float) Math.toRadians(caster.rotationYaw);
        movementDirection = Vec3.createVectorHelper(-Math.sin(yaw), 0, Math.cos(yaw));
        lockedYaw = computeYawFromDirection();
    }

    /**
     * Lock direction toward a target entity.
     * Falls back to look direction if target is at the same position.
     */
    protected void lockDirectionToTarget(EntityLivingBase caster, EntityLivingBase target) {
        double dx = target.posX - caster.posX;
        double dz = target.posZ - caster.posZ;
        double len = Math.sqrt(dx * dx + dz * dz);
        if (len > 0) {
            movementDirection = Vec3.createVectorHelper(dx / len, 0, dz / len);
            lockedYaw = computeYawFromDirection();
        } else {
            lockDirectionFromLook(caster);
        }
    }

    /**
     * Get the base yaw for direction calculation.
     * NPC: yaw toward target. Player: current look yaw.
     */
    protected float getBaseYaw(EntityLivingBase caster, EntityLivingBase target) {
        if (!isPlayerCaster(caster) && target != null) {
            double dx = target.posX - caster.posX;
            double dz = target.posZ - caster.posZ;
            return (float) Math.toDegrees(Math.atan2(-dx, dz));
        }
        return caster.rotationYaw;
    }

    /**
     * Set movement direction from a yaw angle (degrees).
     */
    protected void setDirectionFromYaw(float yawDegrees) {
        float yawRad = (float) Math.toRadians(yawDegrees);
        movementDirection = Vec3.createVectorHelper(-Math.sin(yawRad), 0, Math.cos(yawRad));
        lockedYaw = yawDegrees;
    }

    // ═══════════════════════════════════════════════════════════════════
    // MOVEMENT INITIALIZATION
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Initialize movement tracking. Call from onExecute().
     *
     * @param caster   The entity performing the movement
     * @param distance Maximum travel distance (for timeout computation)
     * @param speed    Movement speed in blocks/tick (for timeout computation)
     */
    protected void initMovement(EntityLivingBase caster, float distance, float speed) {
        startX = caster.posX;
        startY = caster.posY;
        startZ = caster.posZ;
        prevTickX = caster.posX;
        prevTickZ = caster.posZ;
        maxActiveTicks = speed > 0 ? (int) (distance / speed) + 10 : 10;
    }

    // ═══════════════════════════════════════════════════════════════════
    // ACTIVE TICK CHECKS
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Check for timeout or missing movement direction.
     *
     * @return true if the ability should stop
     */
    protected boolean checkTimeout(int tick) {
        return !isPreview() && (movementDirection == null || tick > maxActiveTicks);
    }

    /**
     * Check stall detection with appropriate grace period for player vs NPC.
     * Players need a longer grace period because velocity sync via S12 packet
     * takes time to reach the client, and C03 position packets lag behind.
     *
     * @return true if the entity is stalled (not moving)
     */
    protected boolean checkStall(EntityLivingBase caster, int tick) {
        if (isPreview()) return false;
        int graceTicks = isPlayerCaster(caster) ? PLAYER_STALL_GRACE_TICKS : NPC_STALL_GRACE_TICKS;
        if (tick <= graceTicks) return false;

        double dx = caster.posX - prevTickX;
        double dz = caster.posZ - prevTickZ;
        return dx * dx + dz * dz < 0.0001;
    }

    /**
     * Update previous tick position for stall detection.
     * Call after stall check, before movement application.
     */
    protected void updatePrevPosition(EntityLivingBase caster) {
        prevTickX = caster.posX;
        prevTickZ = caster.posZ;
    }

    /**
     * Get total linear distance traveled from start position.
     */
    protected double getDistanceTraveled(EntityLivingBase caster) {
        double dx = caster.posX - startX;
        double dz = caster.posZ - startZ;
        return Math.sqrt(dx * dx + dz * dz);
    }

    /**
     * Get total distance traveled squared (more efficient for comparison).
     */
    protected double getDistanceTraveledSq(EntityLivingBase caster) {
        double dx = caster.posX - startX;
        double dz = caster.posZ - startZ;
        return dx * dx + dz * dz;
    }

    /**
     * Check if movement direction is blocked by walls.
     *
     * @return true if blocked
     */
    protected boolean checkBlocked(EntityLivingBase caster, float speed) {
        if (isPreview() || movementDirection == null) return false;
        return isMovementBlocked(caster, movementDirection.xCoord, movementDirection.zCoord, speed);
    }

    // ═══════════════════════════════════════════════════════════════════
    // VELOCITY APPLICATION
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Send an authoritative velocity packet to a player.
     * <p>
     * The ability tick runs BEFORE moveEntityWithHeading (at PlayerTickEvent.START).
     * If we use velocityChanged, the entity tracker sends S12 AFTER friction has
     * reduced the motion (~55% of set value). The client then moves at reduced speed,
     * reports a short position via C04, and the server resets to the client's position.
     * <p>
     * By sending S12 manually here — before friction — the client receives the full
     * velocity and moves at the correct speed.
     */
    protected void sendPlayerVelocity(EntityLivingBase caster) {
        if (caster instanceof EntityPlayerMP) {
            ((EntityPlayerMP) caster).playerNetServerHandler.sendPacket(
                new S12PacketEntityVelocity(caster));
        }
    }

    /**
     * Apply horizontal momentum and sync to client.
     */
    protected void applyHorizontalMomentum(EntityLivingBase caster, double motionX, double motionZ) {
        caster.motionX = motionX;
        caster.motionZ = motionZ;
        if (!isPreview()) {
            if (caster instanceof EntityPlayerMP) {
                sendPlayerVelocity(caster);
            } else {
                caster.velocityChanged = true;
            }
        }
    }

    /**
     * Apply movement velocity to the caster (horizontal only, Y unchanged).
     */
    protected void applyVelocity(EntityLivingBase caster, float speed) {
        applyHorizontalMomentum(caster, movementDirection.xCoord * speed, movementDirection.zCoord * speed);
    }

    /**
     * Apply movement velocity with zero vertical motion.
     * Used for ground-locked movement like Charge.
     * motionY is set BEFORE sending velocity packet to prevent client-server desync
     * (which causes "moved too fast" kicks when used in mid-air).
     */
    protected void applyVelocityFlat(EntityLivingBase caster, float speed) {
        caster.motionY = 0;
        applyHorizontalMomentum(caster, movementDirection.xCoord * speed, movementDirection.zCoord * speed);
    }

    /**
     * Stop all horizontal momentum.
     */
    protected void stopMomentum(EntityLivingBase caster) {
        applyHorizontalMomentum(caster, 0, 0);
    }

    // ═══════════════════════════════════════════════════════════════════
    // ROTATION
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Compute yaw angle (degrees) from current movement direction.
     */
    protected float computeYawFromDirection() {
        if (movementDirection == null) return 0;
        return (float) Math.toDegrees(Math.atan2(-movementDirection.xCoord, movementDirection.zCoord));
    }

    /**
     * Enforce locked rotation on all yaw fields.
     * Sets rotationYaw, rotationYawHead, prevRotationYaw, prevRotationYawHead,
     * renderYawOffset, and prevRenderYawOffset to lockedYaw.
     */
    protected void enforceLockedRotation(EntityLivingBase caster) {
        caster.rotationYaw = lockedYaw;
        caster.rotationYawHead = lockedYaw;
        caster.prevRotationYaw = lockedYaw;
        caster.prevRotationYawHead = lockedYaw;
        caster.renderYawOffset = lockedYaw;
        caster.prevRenderYawOffset = lockedYaw;
    }
}
