package kamkeel.npcs.controllers.data.ability.type;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.TargetingMode;
import kamkeel.npcs.controllers.data.ability.telegraph.TelegraphType;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import noppes.npcs.api.ability.IAbilityHolder;
import noppes.npcs.client.gui.util.IAbilityConfigCallback;
import noppes.npcs.client.gui.advanced.SubGuiAbilityConfig;
import noppes.npcs.client.gui.advanced.ability.SubGuiAbilityCutter;
import noppes.npcs.entity.EntityNPCInterface;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Cutter ability: Sweeping fan attack in an arc.
 * Deals damage to all entities in a cone/arc in front of the caster.
 * Can sweep outward over time or hit instantly.
 */
public class AbilityCutter extends Ability {

    public enum SweepMode {
        INSTANT,
        EXPANDING,
        ROTATING
    }

    private float arcAngle = 90.0f;
    private float range = 6.0f;
    private float damage = 12.0f;
    private float knockback = 1.5f;
    private float knockbackUp = 0.2f;

    private SweepMode sweepMode = SweepMode.INSTANT;
    private int sweepWaves = 1;
    private int waveInterval = 5;
    private float rotationSpeed = 5.0f;
    private float startAngleOffset = 0.0f;

    private int stunDuration = 0;
    private int bleedDuration = 0;
    private int bleedLevel = 0;
    private boolean piercing = true;
    private float innerRadius = 0.0f;

    // Runtime state
    private transient Set<Integer> hitEntities = new HashSet<>();
    private transient int currentWave = 0;
    private transient float currentRotation = 0.0f;

    public AbilityCutter() {
        this.typeId = "ability.cnpc.cutter";
        this.name = "Cutter";
        this.targetingMode = TargetingMode.AOE_SELF;
        this.maxRange = 8.0f;
        this.lockMovement = true;
        this.cooldownTicks = 80;
        this.windUpTicks = 20;
        this.activeTicks = 15;
        this.recoveryTicks = 15;
        this.telegraphType = TelegraphType.CONE;
    }

    @Override
    public boolean hasTypeSettings() { return true; }

    @Override
    @SideOnly(Side.CLIENT)
    public SubGuiAbilityConfig createConfigGui(IAbilityConfigCallback callback) {
        return new SubGuiAbilityCutter(this, callback);
    }

    @Override
    public boolean isTargetingModeLocked() { return true; }

    @Override
    public TargetingMode[] getAllowedTargetingModes() {
        return new TargetingMode[] { TargetingMode.AOE_SELF };
    }

    @Override
    public float getTelegraphRadius() { return range; }

    @Override
    public float getTelegraphLength() { return range; }

    @Override
    public float getTelegraphAngle() { return arcAngle; }

    @Override
    public void onExecute(IAbilityHolder holder, EntityLivingBase target, World world) {
        hitEntities.clear();
        currentWave = 0;
        currentRotation = startAngleOffset;

        if (sweepMode == SweepMode.INSTANT && !world.isRemote) {
            performSweepDamage(holder, world, 0.0f, range);
        }
    }

    @Override
    public void onActiveTick(IAbilityHolder holder, EntityLivingBase target, World world, int tick) {
        if (world.isRemote) return;

        switch (sweepMode) {
            case EXPANDING:
                if (tick % waveInterval == 0 && currentWave < sweepWaves) {
                    float waveProgress = (float)(currentWave + 1) / sweepWaves;
                    float waveInner = innerRadius + (range - innerRadius) * ((float)currentWave / sweepWaves);
                    float waveOuter = innerRadius + (range - innerRadius) * waveProgress;
                    performSweepDamage(holder, world, waveInner, waveOuter);
                    currentWave++;
                }
                break;

            case ROTATING:
                currentRotation += rotationSpeed;
                hitEntities.clear();
                performSweepDamage(holder, world, innerRadius, range);
                break;

            case INSTANT:
                break;
        }
    }

    private void performSweepDamage(IAbilityHolder holder, World world, float minDist, float maxDist) {
        EntityLivingBase entity = (EntityLivingBase) holder;
        float casterYaw = entity.rotationYaw + currentRotation;

        AxisAlignedBB searchBox = AxisAlignedBB.getBoundingBox(
            entity.posX - maxDist, entity.posY - 1, entity.posZ - maxDist,
            entity.posX + maxDist, entity.posY + 3, entity.posZ + maxDist
        );

        @SuppressWarnings("unchecked")
        List<EntityLivingBase> entities = world.getEntitiesWithinAABB(EntityLivingBase.class, searchBox);

        for (EntityLivingBase currentEntity : entities) {
            if (currentEntity == entity) continue;
            if (hitEntities.contains(currentEntity.getEntityId())) continue;
            // If not piercing, stop after hitting one e this sweep
            if (!piercing && !hitEntities.isEmpty()) break;

            double dx = currentEntity.posX - entity.posX;
            double dz = currentEntity.posZ - entity.posZ;
            double dist = Math.sqrt(dx * dx + dz * dz);

            if (dist < minDist || dist > maxDist) continue;
            if (!isInArc(dx, dz, casterYaw, arcAngle)) continue;

            hitEntities.add(currentEntity.getEntityId());

            float distFactor = 1.0f - ((float)dist / maxDist) * 0.3f;
            float actualDamage = damage * distFactor;

            // Apply damage with scripted event support
            boolean wasHit = applyAbilityDamage(holder, currentEntity, actualDamage, knockback, knockbackUp);

            // Only apply effects if hit wasn't cancelled
            if (wasHit) {
                if (stunDuration > 0) {
                    currentEntity.addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, stunDuration, 10));
                    currentEntity.addPotionEffect(new PotionEffect(Potion.weakness.id, stunDuration, 2));
                }
                if (bleedDuration > 0) {
                    currentEntity.addPotionEffect(new PotionEffect(Potion.wither.id, bleedDuration, bleedLevel));
                }
            }
        }
    }

    private boolean isInArc(double dx, double dz, float casterYaw, float arcWidth) {
        double angleToEntity = Math.toDegrees(Math.atan2(-dx, dz));
        double angleDiff = normalizeAngle(angleToEntity - casterYaw);
        return Math.abs(angleDiff) <= arcWidth / 2.0;
    }

    private double normalizeAngle(double angle) {
        while (angle > 180) angle -= 360;
        while (angle < -180) angle += 360;
        return angle;
    }

    @Override
    public void onComplete(IAbilityHolder holder, EntityLivingBase target) {
        hitEntities.clear();
        currentWave = 0;
        currentRotation = startAngleOffset;
    }

    @Override
    public void onInterrupt(IAbilityHolder holder, DamageSource source, float damage) {
        hitEntities.clear();
        currentWave = 0;
        currentRotation = startAngleOffset;
    }

    @Override
    public void writeTypeNBT(NBTTagCompound nbt) {
        nbt.setFloat("arcAngle", arcAngle);
        nbt.setFloat("range", range);
        nbt.setFloat("damage", damage);
        nbt.setFloat("knockback", knockback);
        nbt.setFloat("knockbackUp", knockbackUp);
        nbt.setString("sweepMode", sweepMode.name());
        nbt.setInteger("sweepWaves", sweepWaves);
        nbt.setInteger("waveInterval", waveInterval);
        nbt.setFloat("rotationSpeed", rotationSpeed);
        nbt.setFloat("startAngleOffset", startAngleOffset);
        nbt.setInteger("stunDuration", stunDuration);
        nbt.setInteger("bleedDuration", bleedDuration);
        nbt.setInteger("bleedLevel", bleedLevel);
        nbt.setBoolean("piercing", piercing);
        nbt.setFloat("innerRadius", innerRadius);
    }

    @Override
    public void readTypeNBT(NBTTagCompound nbt) {
        this.arcAngle = nbt.hasKey("arcAngle") ? nbt.getFloat("arcAngle") : 90.0f;
        this.range = nbt.hasKey("range") ? nbt.getFloat("range") : 6.0f;
        this.damage = nbt.hasKey("damage") ? nbt.getFloat("damage") : 12.0f;
        this.knockback = nbt.hasKey("knockback") ? nbt.getFloat("knockback") : 1.5f;
        this.knockbackUp = nbt.hasKey("knockbackUp") ? nbt.getFloat("knockbackUp") : 0.2f;
        try {
            this.sweepMode = SweepMode.valueOf(nbt.getString("sweepMode"));
        } catch (Exception e) {
            this.sweepMode = SweepMode.INSTANT;
        }
        this.sweepWaves = nbt.hasKey("sweepWaves") ? nbt.getInteger("sweepWaves") : 1;
        this.waveInterval = nbt.hasKey("waveInterval") ? nbt.getInteger("waveInterval") : 5;
        this.rotationSpeed = nbt.hasKey("rotationSpeed") ? nbt.getFloat("rotationSpeed") : 5.0f;
        this.startAngleOffset = nbt.hasKey("startAngleOffset") ? nbt.getFloat("startAngleOffset") : 0.0f;
        this.stunDuration = nbt.hasKey("stunDuration") ? nbt.getInteger("stunDuration") : 0;
        this.bleedDuration = nbt.hasKey("bleedDuration") ? nbt.getInteger("bleedDuration") : 0;
        this.bleedLevel = nbt.hasKey("bleedLevel") ? nbt.getInteger("bleedLevel") : 0;
        this.piercing = !nbt.hasKey("piercing") || nbt.getBoolean("piercing");
        this.innerRadius = nbt.hasKey("innerRadius") ? nbt.getFloat("innerRadius") : 0.0f;
    }

    // Getters & Setters
    public float getArcAngle() { return arcAngle; }
    public void setArcAngle(float arcAngle) { this.arcAngle = arcAngle; }

    public float getRange() { return range; }
    public void setRange(float range) { this.range = range; }

    public float getDamage() { return damage; }
    public void setDamage(float damage) { this.damage = damage; }

    public float getKnockback() { return knockback; }
    public void setKnockback(float knockback) { this.knockback = knockback; }

    public float getKnockbackUp() { return knockbackUp; }
    public void setKnockbackUp(float knockbackUp) { this.knockbackUp = knockbackUp; }

    public SweepMode getSweepMode() { return sweepMode; }
    public void setSweepMode(SweepMode sweepMode) { this.sweepMode = sweepMode; }

    public int getSweepWaves() { return sweepWaves; }
    public void setSweepWaves(int sweepWaves) { this.sweepWaves = sweepWaves; }

    public int getWaveInterval() { return waveInterval; }
    public void setWaveInterval(int waveInterval) { this.waveInterval = waveInterval; }

    public float getRotationSpeed() { return rotationSpeed; }
    public void setRotationSpeed(float rotationSpeed) { this.rotationSpeed = rotationSpeed; }

    public float getStartAngleOffset() { return startAngleOffset; }
    public void setStartAngleOffset(float startAngleOffset) { this.startAngleOffset = startAngleOffset; }

    public int getStunDuration() { return stunDuration; }
    public void setStunDuration(int stunDuration) { this.stunDuration = stunDuration; }

    public int getBleedDuration() { return bleedDuration; }
    public void setBleedDuration(int bleedDuration) { this.bleedDuration = bleedDuration; }

    public int getBleedLevel() { return bleedLevel; }
    public void setBleedLevel(int bleedLevel) { this.bleedLevel = bleedLevel; }

    public boolean isPiercing() { return piercing; }
    public void setPiercing(boolean piercing) { this.piercing = piercing; }

    public float getInnerRadius() { return innerRadius; }
    public void setInnerRadius(float innerRadius) { this.innerRadius = innerRadius; }

    public float getCurrentRotation() { return currentRotation; }
}
