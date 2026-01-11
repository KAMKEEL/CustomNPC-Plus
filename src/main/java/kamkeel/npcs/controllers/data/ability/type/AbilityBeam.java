package kamkeel.npcs.controllers.data.ability.type;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import kamkeel.npcs.controllers.data.ability.Ability;
import noppes.npcs.api.ability.IAbilityHolder;
import noppes.npcs.client.gui.util.IAbilityConfigCallback;
import noppes.npcs.client.gui.advanced.SubGuiAbilityConfig;
import noppes.npcs.client.gui.advanced.ability.SubGuiAbilityBeam;
import kamkeel.npcs.controllers.data.ability.TargetingMode;
import kamkeel.npcs.controllers.data.ability.telegraph.TelegraphType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import noppes.npcs.entity.EntityNPCInterface;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Beam ability: Continuous line attack that damages all targets in a line.
 * Can sweep across an arc or remain fixed.
 */
public class AbilityBeam extends Ability {

    // Type-specific parameters
    private float beamLength = 10.0f;
    private float beamWidth = 1.0f;
    private float damage = 5.0f;
    private int damageInterval = 5;
    private boolean piercing = true;
    private float sweepAngle = 0.0f;
    private float sweepSpeed = 2.0f;
    private boolean sweepBackAndForth = true;
    private boolean lockOnTarget = false;

    // Runtime state (transient)
    private transient float currentSweepAngle = 0;
    private transient boolean sweepingRight = true;
    private transient int ticksSinceDamage = 0;
    private transient Set<Integer> hitThisTick = new HashSet<>();

    public AbilityBeam() {
        this.typeId = "ability.cnpc.beam";
        this.name = "Beam";
        this.targetingMode = TargetingMode.AGGRO_TARGET;
        this.maxRange = 15.0f;
        this.minRange = 0.0f;
        this.lockMovement = true;
        this.cooldownTicks = 200;
        this.windUpTicks = 60;
        this.activeTicks = 80;
        this.recoveryTicks = 20;
        // No telegraph for beam - it's a continuous line attack
        this.telegraphType = TelegraphType.NONE;
        this.showTelegraph = false;
    }

    @Override
    public boolean hasTypeSettings() { return true; }

    @Override
    @SideOnly(Side.CLIENT)
    public SubGuiAbilityConfig createConfigGui(
            IAbilityConfigCallback callback) {
        return new SubGuiAbilityBeam(this, callback);
    }

    @Override
    public boolean isTargetingModeLocked() { return true; }

    @Override
    public TargetingMode[] getAllowedTargetingModes() {
        return new TargetingMode[] { TargetingMode.AGGRO_TARGET };
    }

    @Override
    public void onExecute(IAbilityHolder holder, EntityLivingBase target, World world) {
        // Initialize beam
        currentSweepAngle = -sweepAngle / 2;
        sweepingRight = true;
        ticksSinceDamage = damageInterval; // Deal damage immediately
        hitThisTick.clear();
    }

    @Override
    public void onActiveTick(IAbilityHolder holder, EntityLivingBase target, World world, int tick) {
        if (world.isRemote) return;

        EntityLivingBase entity = (EntityLivingBase) holder;
        hitThisTick.clear();
        ticksSinceDamage++;

        // Update sweep angle
        if (sweepAngle > 0) {
            if (sweepBackAndForth) {
                if (sweepingRight) {
                    currentSweepAngle += sweepSpeed;
                    if (currentSweepAngle >= sweepAngle / 2) {
                        sweepingRight = false;
                    }
                } else {
                    currentSweepAngle -= sweepSpeed;
                    if (currentSweepAngle <= -sweepAngle / 2) {
                        sweepingRight = true;
                    }
                }
            } else {
                currentSweepAngle += sweepSpeed;
                if (currentSweepAngle >= sweepAngle) {
                    currentSweepAngle = 0;
                }
            }
        }

        // Calculate beam direction
        float baseYaw = entity.rotationYaw;

        // If lock on target, point at target
        if (lockOnTarget && target != null) {
            double dx = target.posX - entity.posX;
            double dz = target.posZ - entity.posZ;
            baseYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        }

        float beamYaw = baseYaw + currentSweepAngle;
        float yawRad = (float) Math.toRadians(beamYaw);

        // Calculate beam start position
        double startX = entity.posX;
        double startY = entity.posY + entity.getEyeHeight() * 0.8;
        double startZ = entity.posZ;

        double dirX = -Math.sin(yawRad);
        double dirZ = Math.cos(yawRad);

        // Check for entities along the beam
        if (ticksSinceDamage >= damageInterval) {
            ticksSinceDamage = 0;

            // Sample points along the beam
            int samples = (int) (beamLength / 0.5);
            for (int i = 1; i <= samples; i++) {
                double progress = (double) i / samples;
                double checkX = startX + dirX * beamLength * progress;
                double checkY = startY;
                double checkZ = startZ + dirZ * beamLength * progress;

                // Find entities at this point
                AxisAlignedBB checkBox = AxisAlignedBB.getBoundingBox(
                    checkX - beamWidth, checkY - 1, checkZ - beamWidth,
                    checkX + beamWidth, checkY + 2, checkZ + beamWidth
                );

                @SuppressWarnings("unchecked")
                List<Entity> entities = world.getEntitiesWithinAABB(EntityLivingBase.class, checkBox);

                for (Entity e : entities) {
                    if (!(e instanceof EntityLivingBase)) continue;
                    if (e == entity) continue;
                    if (hitThisTick.contains(e.getEntityId())) continue;

                    EntityLivingBase livingEntity = (EntityLivingBase) e;

                    // Check if actually in beam path
                    if (isInBeamPath(livingEntity, startX, startY, startZ, dirX, dirZ)) {
                        hitThisTick.add(e.getEntityId());
                        // Apply damage with scripted event support (no knockback for beam)
                        applyAbilityDamage(holder, livingEntity, damage, 0, 0);

                        if (!piercing) {
                            return; // Stop at first hit
                        }
                    }
                }
            }
        }

        // Spawn beam particles
        spawnBeamParticles(world, startX, startY, startZ, dirX, dirZ);
    }

    private boolean isInBeamPath(EntityLivingBase entity, double startX, double startY, double startZ,
                                  double dirX, double dirZ) {
        // Project entity position onto beam line
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

        // Check if within beam width
        return perpDist <= beamWidth + entity.width / 2;
    }

    private void spawnBeamParticles(World world, double startX, double startY, double startZ,
                                     double dirX, double dirZ) {
        // Spawn particles along beam line
        for (int i = 0; i < beamLength; i++) {
            double px = startX + dirX * i;
            double pz = startZ + dirZ * i;
            world.spawnParticle("flame", px, startY, pz, 0, 0, 0);
        }
    }

    @Override
    public void reset() {
        super.reset();
        currentSweepAngle = 0;
        sweepingRight = true;
        ticksSinceDamage = 0;
        hitThisTick.clear();
    }

    @Override
    public float getTelegraphRadius() {
        return beamLength;
    }

    @Override
    public void writeTypeNBT(NBTTagCompound nbt) {
        nbt.setFloat("beamLength", beamLength);
        nbt.setFloat("beamWidth", beamWidth);
        nbt.setFloat("damage", damage);
        nbt.setInteger("damageInterval", damageInterval);
        nbt.setBoolean("piercing", piercing);
        nbt.setFloat("sweepAngle", sweepAngle);
        nbt.setFloat("sweepSpeed", sweepSpeed);
        nbt.setBoolean("sweepBackAndForth", sweepBackAndForth);
        nbt.setBoolean("lockOnTarget", lockOnTarget);
    }

    @Override
    public void readTypeNBT(NBTTagCompound nbt) {
        this.beamLength = nbt.hasKey("beamLength") ? nbt.getFloat("beamLength") : 10.0f;
        this.beamWidth = nbt.hasKey("beamWidth") ? nbt.getFloat("beamWidth") : 1.0f;
        this.damage = nbt.hasKey("damage") ? nbt.getFloat("damage") : 5.0f;
        this.damageInterval = nbt.hasKey("damageInterval") ? nbt.getInteger("damageInterval") : 5;
        this.piercing = !nbt.hasKey("piercing") || nbt.getBoolean("piercing");
        this.sweepAngle = nbt.hasKey("sweepAngle") ? nbt.getFloat("sweepAngle") : 0.0f;
        this.sweepSpeed = nbt.hasKey("sweepSpeed") ? nbt.getFloat("sweepSpeed") : 2.0f;
        this.sweepBackAndForth = !nbt.hasKey("sweepBackAndForth") || nbt.getBoolean("sweepBackAndForth");
        this.lockOnTarget = nbt.hasKey("lockOnTarget") && nbt.getBoolean("lockOnTarget");
    }

    // Getters & Setters
    public float getBeamLength() { return beamLength; }
    public void setBeamLength(float beamLength) { this.beamLength = beamLength; }

    public float getBeamWidth() { return beamWidth; }
    public void setBeamWidth(float beamWidth) { this.beamWidth = beamWidth; }

    public float getDamage() { return damage; }
    public void setDamage(float damage) { this.damage = damage; }

    public int getDamageInterval() { return damageInterval; }
    public void setDamageInterval(int damageInterval) { this.damageInterval = damageInterval; }

    public boolean isPiercing() { return piercing; }
    public void setPiercing(boolean piercing) { this.piercing = piercing; }

    public float getSweepAngle() { return sweepAngle; }
    public void setSweepAngle(float sweepAngle) { this.sweepAngle = sweepAngle; }

    public float getSweepSpeed() { return sweepSpeed; }
    public void setSweepSpeed(float sweepSpeed) { this.sweepSpeed = sweepSpeed; }

    public boolean isSweepBackAndForth() { return sweepBackAndForth; }
    public void setSweepBackAndForth(boolean sweepBackAndForth) { this.sweepBackAndForth = sweepBackAndForth; }

    public boolean isLockOnTarget() { return lockOnTarget; }
    public void setLockOnTarget(boolean lockOnTarget) { this.lockOnTarget = lockOnTarget; }
}
