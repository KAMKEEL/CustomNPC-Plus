package kamkeel.npcs.entity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.controllers.data.ability.data.energy.EnergyDisplayData;
import kamkeel.npcs.util.CNPCDebug;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import noppes.npcs.NpcDamageSource;
import noppes.npcs.controllers.PartyController;
import noppes.npcs.controllers.data.Party;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.entity.EntityNPCInterface;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Sweeper ability entity - a rotating beam attached to the NPC.
 * Unlike other projectiles, this handles its own damage logic since it's
 * attached to and rotates around the caster.
 * Extends EntityEnergyAbility for shared visual/owner/charging state.
 * <p>
 * Design: Similar to Beam trail visuals but rotating around origin.
 */
public class EntityEnergySweeper extends EntityEnergyAbility {

    // Beam properties
    private float beamLength = 10.0f;
    private float beamWidth = 0.3f;  // Thin like beam trail
    private float beamHeight = 1.0f;

    // Combat properties
    private float damage = 5.0f;
    private int damageInterval = 5;
    private boolean piercing = true;

    // Rotation
    private float currentAngle = 0;
    private float prevAngle = 0;
    private float sweepSpeed = 3.0f;
    private int numberOfRotations = 2;
    private int completedRotations = 0;
    private float baseYaw = 0;  // Starting yaw direction
    private boolean lockOnTarget = false;

    // Target tracking
    private int targetEntityId = -1;

    // Lifetime
    private int maxTicks = 400;
    private long deathWorldTime = -1;

    // Damage state
    private transient int ticksSinceDamage = 0;
    private transient Set<Integer> hitThisTick = new HashSet<>();

    public EntityEnergySweeper(World world) {
        super(world);
        this.setSize(0.1f, 0.1f);
        this.noClip = true;
    }

    public EntityEnergySweeper(World world, EntityLivingBase owner, EntityLivingBase target,
                               float beamLength, float beamWidth, float beamHeight,
                               EnergyDisplayData displayData,
                               float sweepSpeed, int numberOfRotations,
                               float damage, int damageInterval, boolean piercing,
                               boolean lockOnTarget) {
        this(world);

        this.ownerEntityId = owner.getEntityId();
        this.targetEntityId = target != null ? target.getEntityId() : -1;
        this.beamLength = beamLength;
        this.beamWidth = beamWidth;
        this.beamHeight = beamHeight;
        this.displayData = displayData != null ? displayData.copy() : new EnergyDisplayData();
        this.sweepSpeed = sweepSpeed;
        this.numberOfRotations = numberOfRotations;
        this.damage = damage;
        this.damageInterval = damageInterval;
        this.piercing = piercing;
        this.lockOnTarget = lockOnTarget;

        // Calculate max ticks based on rotations
        this.maxTicks = (int) ((360.0f * numberOfRotations) / sweepSpeed) + 10;

        // Calculate base yaw
        if (lockOnTarget && target != null) {
            double dx = target.posX - owner.posX;
            double dz = target.posZ - owner.posZ;
            this.baseYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        } else {
            this.baseYaw = owner.rotationYaw;
        }

        // Position at beam center (bottom at feet, top at feet + beamHeight)
        this.setPosition(owner.posX, owner.posY + beamHeight / 2.0, owner.posZ);

        // Ready to deal damage immediately
        this.ticksSinceDamage = damageInterval;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
    }

    @Override
    public void onUpdate() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        this.prevAngle = this.currentAngle;

        super.onUpdate();

        // Set death time on first tick
        if (deathWorldTime < 0 && worldObj != null) {
            deathWorldTime = worldObj.getTotalWorldTime() + maxTicks;
        }

        // Check lifetime
        if (deathWorldTime > 0 && worldObj.getTotalWorldTime() >= deathWorldTime) {
            this.setDead();
            return;
        }

        // Absolute hard lifetime cap (safety net)
        if (ticksExisted > HARD_LIFETIME_CAP) {
            this.setDead();
            return;
        }

        Entity owner = getOwnerEntity();
        if (owner != null && owner.isDead) {
            this.setDead();
            return;
        }
        if (owner instanceof EntityNPCInterface && ((EntityNPCInterface) owner).isKilled()) {
            this.setDead();
            return;
        }

        if (owner != null) {
            this.setPosition(owner.posX, owner.posY + beamHeight / 2.0, owner.posZ);
        }

        // Check if sweep is done
        if (completedRotations >= numberOfRotations) {
            this.setDead();
            return;
        }

        // Update rotation
        currentAngle += sweepSpeed;
        while (currentAngle >= 360) {
            currentAngle -= 360;
            completedRotations++;
        }

        // Server handles damage
        if (!worldObj.isRemote) {
            handleDamage(owner);
        }

        // Debug logging
        {
            boolean isClient = worldObj.isRemote;
            if (isClient ? CNPCDebug.isClientEnabled("energy") : CNPCDebug.isServerEnabled("energy")) {
                CNPCDebug.log("energy", isClient, String.format(
                    "[Sweeper id=%d tick=%d] pos=(%.2f,%.2f,%.2f) angle=%.1f prevAngle=%.1f " +
                    "speed=%.1f rotations=%d/%d length=%.2f width=%.2f",
                    getEntityId(), ticksExisted, posX, posY, posZ,
                    currentAngle, prevAngle, sweepSpeed,
                    completedRotations, numberOfRotations, beamLength, beamWidth));
            }
        }
    }

    /**
     * Handle damage detection and application.
     * This runs on server only.
     */
    private void handleDamage(Entity owner) {
        hitThisTick.clear();
        ticksSinceDamage++;

        if (ticksSinceDamage < damageInterval) {
            return;
        }
        ticksSinceDamage = 0;

        // Calculate beam direction using the ACTUAL render angle
        float beamYaw = baseYaw + currentAngle;
        float yawRad = (float) Math.toRadians(beamYaw);

        double startX = posX;
        double startY = posY;
        double startZ = posZ;

        double dirX = -Math.sin(yawRad);
        double dirZ = Math.cos(yawRad);

        // Sample points along the beam for collision
        int samples = (int) (beamLength / 0.5);
        for (int i = 1; i <= samples; i++) {
            double progress = (double) i / samples;
            double checkX = startX + dirX * beamLength * progress;
            double checkY = startY;
            double checkZ = startZ + dirZ * beamLength * progress;

            // Hitbox: beam center ± half-height vertically, generous width for thin beams
            AxisAlignedBB checkBox = AxisAlignedBB.getBoundingBox(
                checkX - beamWidth * 2, checkY - beamHeight / 2.0, checkZ - beamWidth * 2,
                checkX + beamWidth * 2, checkY + beamHeight / 2.0, checkZ + beamWidth * 2
            );

            @SuppressWarnings("unchecked")
            List<Entity> entities = worldObj.getEntitiesWithinAABB(EntityLivingBase.class, checkBox);

            for (Entity entity : entities) {
                if (!(entity instanceof EntityLivingBase)) continue;
                if (entity == owner) continue;
                if (hitThisTick.contains(entity.getEntityId())) continue;
                if (shouldIgnoreEntity((EntityLivingBase) entity, owner)) continue;

                EntityLivingBase livingEntity = (EntityLivingBase) entity;

                // Check if actually in beam path
                if (isInBeamPath(livingEntity, startX, startY, startZ, dirX, dirZ)) {
                    hitThisTick.add(entity.getEntityId());
                    applyDamage(livingEntity, owner);

                    if (!piercing) {
                        return;
                    }
                }
            }
        }
    }

    private boolean isInBeamPath(EntityLivingBase entity, double startX, double startY, double startZ,
                                 double dirX, double dirZ) {
        double entityX = entity.posX - startX;
        double entityZ = entity.posZ - startZ;

        // Dot product to find projection distance
        double projectionDist = entityX * dirX + entityZ * dirZ;

        // Check if within beam length
        if (projectionDist < 0 || projectionDist > beamLength) {
            return false;
        }

        // Calculate perpendicular distance
        double projX = dirX * projectionDist;
        double projZ = dirZ * projectionDist;
        double perpX = entityX - projX;
        double perpZ = entityZ - projZ;
        double perpDist = Math.sqrt(perpX * perpX + perpZ * perpZ);

        // Check if within beam width (use wider hitbox than visual)
        if (perpDist > beamWidth * 3 + entity.width / 2) {
            return false;
        }

        // Height check - entity must overlap beam vertically
        double beamTopY = startY + beamHeight / 2.0;
        double beamBottomY = startY - beamHeight / 2.0;
        double entityFeetY = entity.posY;
        double entityTopY = entity.posY + entity.height;

        // Entity feet must be below beam top AND entity top must be above beam bottom
        return entityFeetY < beamTopY && entityTopY > beamBottomY;
    }

    /**
     * Check if an entity should be ignored for sweep damage.
     * Same logic as EntityEnergyProjectile.shouldIgnoreEntity but adapted for sweeper.
     */
    private boolean shouldIgnoreEntity(EntityLivingBase entity, Entity owner) {
        if (entity instanceof EntityNPCInterface) {
            EntityNPCInterface targetNpc = (EntityNPCInterface) entity;
            if (targetNpc.faction.isPassive) return true;
            if (owner instanceof EntityNPCInterface) {
                if (((EntityNPCInterface) owner).faction.id == targetNpc.faction.id) return true;
            }
            if (owner instanceof EntityPlayer) {
                if (targetNpc.faction.isFriendlyToPlayer((EntityPlayer) owner)) return true;
            }
        }
        if (owner instanceof EntityPlayer && entity instanceof EntityPlayer) {
            PlayerData ownerData = PlayerData.get((EntityPlayer) owner);
            PlayerData targetData = PlayerData.get((EntityPlayer) entity);
            if (ownerData.partyUUID != null && ownerData.partyUUID.equals(targetData.partyUUID)) {
                Party party = PartyController.Instance().getParty(ownerData.partyUUID);
                if (party != null && !party.friendlyFire()) return true;
            }
        }
        return false;
    }

    private void applyDamage(EntityLivingBase target, Entity owner) {
        if (owner instanceof EntityNPCInterface) {
            target.attackEntityFrom(new NpcDamageSource("npc_ability", (EntityNPCInterface) owner), damage);
        } else if (owner instanceof EntityPlayer) {
            target.attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer) owner), damage);
        } else if (owner instanceof EntityLivingBase) {
            target.attackEntityFrom(DamageSource.causeMobDamage((EntityLivingBase) owner), damage);
        } else {
            target.attackEntityFrom(new NpcDamageSource("npc_ability", null), damage);
        }

        // Small upward knockback to help escape
        target.addVelocity(0, 0.2, 0);
        target.velocityChanged = true;
    }

    /**
     * Set up preview mode. Owner is stored directly since world entity lookup won't work.
     */
    public void setupPreview(EntityLivingBase owner) {
        this.previewMode = true;
        this.previewOwner = owner;
    }

    // ==================== RENDERING ====================

    @Override
    @SideOnly(Side.CLIENT)
    public boolean isInRangeToRenderDist(double distance) {
        return distance < 16384.0D; // 128 blocks squared
    }

    // ==================== GETTERS FOR RENDERER ====================

    public float getBeamLength() {
        return beamLength;
    }

    public float getBeamWidth() {
        return beamWidth;
    }

    public float getBeamHeight() {
        return beamHeight;
    }

    /**
     * Get the full interpolated angle INCLUDING base yaw for rendering.
     */
    public float getInterpolatedAngle(float partialTicks) {
        // Handle wrap-around for the rotating part
        float diff = currentAngle - prevAngle;
        if (diff > 180) diff -= 360;
        if (diff < -180) diff += 360;
        float interpAngle = prevAngle + diff * partialTicks;
        // Add base yaw for correct world orientation
        return baseYaw + interpAngle;
    }

    public float getCurrentAngle() {
        return currentAngle;
    }

    public float getBaseYaw() {
        return baseYaw;
    }

    // ==================== NBT ====================

    @Override
    protected void readEntityFromNBT(NBTTagCompound nbt) {
        // Intentionally empty — ability entities are transient (not saved to world)
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound nbt) {
        // Intentionally empty — ability entities are transient (not saved to world)
    }

    @Override
    protected void writeSpawnNBT(NBTTagCompound nbt) {
        writeEnergyBaseNBT(nbt);
        nbt.setFloat("BeamLength", beamLength);
        nbt.setFloat("BeamWidth", beamWidth);
        nbt.setFloat("BeamHeight", beamHeight);
        nbt.setFloat("SweepSpeed", sweepSpeed);
        nbt.setInteger("NumRotations", numberOfRotations);
        nbt.setInteger("CompletedRotations", completedRotations);
        nbt.setInteger("MaxTicks", maxTicks);
        nbt.setInteger("TargetId", targetEntityId);
        nbt.setFloat("CurrentAngle", currentAngle);
        nbt.setFloat("BaseYaw", baseYaw);
        nbt.setLong("DeathWorldTime", deathWorldTime);
        nbt.setFloat("Damage", damage);
        nbt.setInteger("DamageInterval", damageInterval);
        nbt.setBoolean("Piercing", piercing);
        nbt.setBoolean("LockOnTarget", lockOnTarget);
    }

    @Override
    protected void readSpawnNBT(NBTTagCompound nbt) {
        readEnergyBaseNBT(nbt);
        this.beamLength = sanitize(nbt.getFloat("BeamLength"), 10.0f, MAX_ENTITY_SIZE);
        this.beamWidth = sanitize(nbt.getFloat("BeamWidth"), 0.3f, MAX_ENTITY_SIZE);
        this.beamHeight = sanitize(nbt.getFloat("BeamHeight"), 1.0f, MAX_ENTITY_SIZE);
        this.sweepSpeed = nbt.hasKey("SweepSpeed") ? nbt.getFloat("SweepSpeed") : 3.0f;
        if (Float.isNaN(sweepSpeed) || Float.isInfinite(sweepSpeed) || sweepSpeed <= 0) sweepSpeed = 3.0f;
        this.numberOfRotations = nbt.hasKey("NumRotations") ? nbt.getInteger("NumRotations") : 2;
        if (numberOfRotations <= 0) numberOfRotations = 1;
        this.completedRotations = nbt.getInteger("CompletedRotations");
        this.maxTicks = nbt.hasKey("MaxTicks") ? nbt.getInteger("MaxTicks") : 400;
        if (maxTicks <= 0) maxTicks = 400;
        this.targetEntityId = nbt.getInteger("TargetId");
        this.currentAngle = nbt.getFloat("CurrentAngle");
        this.prevAngle = currentAngle;
        this.baseYaw = nbt.getFloat("BaseYaw");
        this.deathWorldTime = nbt.getLong("DeathWorldTime");
        this.damage = nbt.hasKey("Damage") ? nbt.getFloat("Damage") : 5.0f;
        if (Float.isNaN(damage) || Float.isInfinite(damage)) damage = 5.0f;
        this.damageInterval = nbt.hasKey("DamageInterval") ? nbt.getInteger("DamageInterval") : 5;
        if (damageInterval <= 0) damageInterval = 1;
        this.piercing = !nbt.hasKey("Piercing") || nbt.getBoolean("Piercing");
        this.lockOnTarget = nbt.getBoolean("LockOnTarget");
    }
}
