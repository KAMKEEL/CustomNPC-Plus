package kamkeel.npcs.controllers.data.ability.type;

import noppes.npcs.constants.ClientOnly;
import kamkeel.npcs.util.IVector3;
import kamkeel.npcs.controllers.data.ability.preview.PreviewEntityHandler;
import kamkeel.npcs.controllers.data.ability.gui.SubGuiAbilityConfig;
import kamkeel.npcs.controllers.data.ability.gui.IAbilityConfigCallback;
import kamkeel.npcs.controllers.data.ability.gui.FieldDef;
import kamkeel.npcs.controllers.data.ability.gui.IChainedAbilityFieldProvider;
import kamkeel.npcs.controllers.data.ability.gui.IAbilityFieldProvider;
import noppes.npcs.entity.EntityNPCInterface;
import kamkeel.npcs.entity.EntityEnergyDome;
import kamkeel.npcs.entity.EntityEnergyBarrier;
import kamkeel.npcs.entity.EntityEnergyPanel;
import kamkeel.npcs.entity.EntityAbilityOrb;
import kamkeel.npcs.entity.EntityAbilityLaser;
import kamkeel.npcs.entity.EntityAbilityDisc;
import kamkeel.npcs.entity.EntityAbilityBeam;
import kamkeel.npcs.entity.EntityEnergyProjectile;
import noppes.npcs.api.entity.IEntityLiving;
import noppes.npcs.api.IDamageSource;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.INbtList;
import noppes.npcs.api.INbt;
import kamkeel.npcs.controllers.data.ability.Ability;
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
    protected transient IVector3 movementDirection;
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
    protected void lockDirection(IEntityLivingBase caster, IEntityLivingBase target) {
        if (!isPlayerCaster(caster) && target != null) {
            lockDirectionToTarget(caster, target);
        } else {
            lockDirectionFromLook(caster);
        }
    }

    /**
     * Lock direction to the caster's look direction (horizontal only).
     */
    protected void lockDirectionFromLook(IEntityLivingBase caster) {
        float yaw = (float) Math.toRadians(caster.rotationYaw);
        movementDirection = IVector3.createVectorHelper(-Math.sin(yaw), 0, Math.cos(yaw));
        lockedYaw = computeYawFromDirection();
    }

    /**
     * Lock direction toward a target IEntity.
     * Falls back to look direction if target is at the same position.
     */
    protected void lockDirectionToTarget(IEntityLivingBase caster, IEntityLivingBase target) {
        double dx = target.posX - caster.posX;
        double dz = target.posZ - caster.posZ;
        double len = Math.sqrt(dx * dx + dz * dz);
        if (len > 0) {
            movementDirection = IVector3.createVectorHelper(dx / len, 0, dz / len);
            lockedYaw = computeYawFromDirection();
        } else {
            lockDirectionFromLook(caster);
        }
    }

    /**
     * Get the base yaw for direction calculation.
     * NPC: yaw toward target. Player: current look yaw.
     */
    protected float getBaseYaw(IEntityLivingBase caster, IEntityLivingBase target) {
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
        movementDirection = IVector3.createVectorHelper(-Math.sin(yawRad), 0, Math.cos(yawRad));
        lockedYaw = yawDegrees;
    }

    // ═══════════════════════════════════════════════════════════════════
    // MOVEMENT INITIALIZATION
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Initialize movement tracking. Call from onExecute().
     *
     * @param caster   The IEntity performing the movement
     * @param distance Maximum travel distance (for timeout computation)
     * @param speed    Movement speed in blocks/tick (for timeout computation)
     */
    protected void initMovement(IEntityLivingBase caster, float distance, float speed) {
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
     * @return true if the IEntity is stalled (not moving)
     */
    protected boolean checkStall(IEntityLivingBase caster, int tick) {
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
    protected void updatePrevPosition(IEntityLivingBase caster) {
        prevTickX = caster.posX;
        prevTickZ = caster.posZ;
    }

    /**
     * Get total linear distance traveled from start position.
     */
    protected double getDistanceTraveled(IEntityLivingBase caster) {
        double dx = caster.posX - startX;
        double dz = caster.posZ - startZ;
        return Math.sqrt(dx * dx + dz * dz);
    }

    /**
     * Get total distance traveled squared (more efficient for comparison).
     */
    protected double getDistanceTraveledSq(IEntityLivingBase caster) {
        double dx = caster.posX - startX;
        double dz = caster.posZ - startZ;
        return dx * dx + dz * dz;
    }

    /**
     * Check if movement direction is blocked by walls or solid barriers.
     *
     * @return true if blocked
     */
    protected boolean checkBlocked(IEntityLivingBase caster, float speed) {
        if (isPreview() || movementDirection == null) return false;
        return isMovementBlocked(caster, movementDirection.xCoord, movementDirection.zCoord, speed)
            || isMovementBlockedByBarrier(caster, movementDirection.xCoord, movementDirection.zCoord, speed);
    }

    // ═══════════════════════════════════════════════════════════════════
    // VELOCITY APPLICATION
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Send an authoritative velocity packet to a player.
     * <p>
     * The ability tick runs BEFORE moveEntityWithHeading (at PlayerTickEvent.START).
     * If we use velocityChanged, the IEntity tracker sends S12 AFTER friction has
     * reduced the motion (~55% of set value). The client then moves at reduced speed,
     * reports a short position via C04, and the server resets to the client's position.
     * <p>
     * By sending S12 manually here — before friction — the client receives the full
     * velocity and moves at the correct speed.
     */
    protected void sendPlayerVelocity(IEntityLivingBase caster) {
        if (caster instanceof IPlayer) {
            ((IPlayer) caster).playerNetServerHandler.sendPacket(
                new S12PacketEntityVelocity(caster));
        }
    }

    /**
     * Apply horizontal momentum and sync to client.
     */
    protected void applyHorizontalMomentum(IEntityLivingBase caster, double motionX, double motionZ) {
        caster.motionX = motionX;
        caster.motionZ = motionZ;
        if (!isPreview()) {
            if (caster instanceof IPlayer) {
                sendPlayerVelocity(caster);
            } else {
                caster.velocityChanged = true;
            }
        }
    }

    /**
     * Apply movement velocity to the caster (horizontal only, Y unchanged).
     */
    protected void applyVelocity(IEntityLivingBase caster, float speed) {
        applyHorizontalMomentum(caster, movementDirection.xCoord * speed, movementDirection.zCoord * speed);
    }

    /**
     * Apply movement velocity with zero vertical motion.
     * Used for ground-locked movement like Charge.
     * motionY is set BEFORE sending velocity packet to prevent client-server desync
     * (which causes "moved too fast" kicks when used in mid-air).
     */
    protected void applyVelocityFlat(IEntityLivingBase caster, float speed) {
        caster.motionY = 0;
        applyHorizontalMomentum(caster, movementDirection.xCoord * speed, movementDirection.zCoord * speed);
    }

    /**
     * Stop all horizontal momentum.
     */
    protected void stopMomentum(IEntityLivingBase caster) {
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
    protected void enforceLockedRotation(IEntityLivingBase caster) {
        caster.rotationYaw = lockedYaw;
        caster.rotationYawHead = lockedYaw;
        caster.prevRotationYaw = lockedYaw;
        caster.prevRotationYawHead = lockedYaw;
        caster.renderYawOffset = lockedYaw;
        caster.prevRenderYawOffset = lockedYaw;
    }
}
