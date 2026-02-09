package kamkeel.npcs.controllers.data.ability.type;

import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.LockMovementType;
import kamkeel.npcs.controllers.data.ability.TargetingMode;
import kamkeel.npcs.controllers.data.telegraph.TelegraphType;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import noppes.npcs.entity.EntityNPCInterface;

import noppes.npcs.api.ability.type.IAbilityCutter;

import noppes.npcs.client.gui.builder.FieldDef;
import kamkeel.npcs.controllers.data.ability.gui.AbilityFieldDefs;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Cutter ability: Sweeping fan attack in an arc.
 * Deals damage to all entities in a cone/arc in front of the caster.
 * Can sweep outward over time or hit instantly.
 */
public class AbilityCutter extends Ability implements IAbilityCutter {

    public enum SweepMode {
        SWIPE,
        SPIN;

        @Override
        public String toString() {
            switch (this) {
                case SWIPE: return "ability.sweep.swipe";
                case SPIN: return "ability.sweep.spin";
                default: return name();
            }
        }
    }

    private float arcAngle = 90.0f;
    private float range = 6.0f;
    private float damage = 7.0f;
    private float knockback = 1.5f;

    private SweepMode sweepMode = SweepMode.SWIPE;
    private float sweepSpeed = 6.0f;
    private int spinDurationTicks = 60; // Duration for SPIN mode

    private boolean piercing = true;
    private float innerRadius = 0.0f;

    // Runtime state
    private transient Set<Integer> hitEntities = new HashSet<>();
    private transient float currentRotation = 0.0f;

    public AbilityCutter() {
        this.typeId = "ability.cnpc.cutter";
        this.name = "Cutter";
        this.targetingMode = TargetingMode.AOE_SELF;
        this.maxRange = 8.0f;
        this.lockMovement = LockMovementType.NO;
        this.cooldownTicks = 0;
        this.windUpTicks = 20;
        this.telegraphType = TelegraphType.CONE;
        this.windUpSound = "random.bow";
        this.activeSound = "random.break";
        this.windUpAnimationName = "Ability_Cutter_Windup";
        this.activeAnimationName = "Ability_Cutter_Active";
    }

    @Override
    public boolean isTargetingModeLocked() {
        return true;
    }

    @Override
    public TargetingMode[] getAllowedTargetingModes() {
        return new TargetingMode[]{TargetingMode.AOE_SELF};
    }

    @Override
    public float getTelegraphRadius() {
        return range;
    }

    @Override
    public float getTelegraphLength() {
        return range;
    }

    @Override
    public float getTelegraphAngle() {
        return arcAngle;
    }

    @Override
    public void onExecute(EntityLivingBase caster, EntityLivingBase target, World world) {
        hitEntities.clear();
        currentRotation = -arcAngle / 2.0f;
    }

    @Override
    public void onActiveTick(EntityLivingBase caster, EntityLivingBase target, World world, int tick) {
        if (world.isRemote && !isPreview()) return;

        switch (sweepMode) {
            case SWIPE:
                float prevRotation = currentRotation;
                currentRotation += sweepSpeed;
                if (currentRotation > arcAngle / 2.0f) {
                    if (!isPreview()) {
                        performSweepDamageRange(caster, world, innerRadius, range, prevRotation, arcAngle / 2.0f);
                    }
                    signalCompletion();
                    return;
                }
                if (!isPreview()) {
                    performSweepDamageRange(caster, world, innerRadius, range, prevRotation, currentRotation);
                }
                break;

            case SPIN:
                if (tick >= spinDurationTicks) {
                    signalCompletion();
                    return;
                }
                float prevSpin = currentRotation;
                currentRotation = (currentRotation + sweepSpeed) % 360.0f;
                if (!isPreview()) {
                    hitEntities.clear();
                    performSweepDamageRange(caster, world, innerRadius, range, prevSpin, currentRotation);
                }
                break;
        }
    }

    /**
     * Check for entities between two sweep angles, ensuring no gaps between ticks.
     * For SWIPE mode, this covers the continuous arc from startAngle to endAngle.
     */
    private void performSweepDamageRange(EntityLivingBase caster, World world, float minDist, float maxDist,
                                          float startAngle, float endAngle) {
        // Calculate the angular range covered this tick
        float minAngle = Math.min(startAngle, endAngle);
        float maxAngle = Math.max(startAngle, endAngle);

        AxisAlignedBB searchBox = AxisAlignedBB.getBoundingBox(
            caster.posX - maxDist, caster.posY - 1, caster.posZ - maxDist,
            caster.posX + maxDist, caster.posY + 3, caster.posZ + maxDist
        );

        @SuppressWarnings("unchecked")
        List<EntityLivingBase> entities = world.getEntitiesWithinAABB(EntityLivingBase.class, searchBox);

        for (EntityLivingBase entity : entities) {
            if (entity == caster) continue;
            if (hitEntities.contains(entity.getEntityId())) continue;
            // If not piercing, stop after hitting one entity this sweep
            if (!piercing && !hitEntities.isEmpty()) break;

            double dx = entity.posX - caster.posX;
            double dz = entity.posZ - caster.posZ;
            double dist = Math.sqrt(dx * dx + dz * dz);

            if (dist < minDist || dist > maxDist) continue;
            if (!isInSweepRange(dx, dz, caster.rotationYaw, minAngle, maxAngle)) continue;

            hitEntities.add(entity.getEntityId());

            float distFactor = 1.0f - ((float) dist / maxDist) * 0.3f;
            float actualDamage = damage * distFactor;

            // Apply damage with scripted event support
            boolean wasHit = applyAbilityDamage(caster, entity, actualDamage, knockback);

            // Apply effects if hit wasn't cancelled
            if (wasHit) {
                applyEffects(entity);
            }
        }
    }

    /**
     * Check if an entity falls within the angular range between two sweep positions.
     * This ensures entities at long range can't slip between tick-gaps.
     */
    private boolean isInSweepRange(double dx, double dz, float casterYaw, float minAngle, float maxAngle) {
        double angleToEntity = Math.toDegrees(Math.atan2(-dx, dz));
        double entityRelative = normalizeAngle(angleToEntity - casterYaw);
        return entityRelative >= minAngle && entityRelative <= maxAngle;
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
    public void onComplete(EntityLivingBase caster, EntityLivingBase target) {
        hitEntities.clear();
        currentRotation = 0.0f;
    }

    @Override
    public void onInterrupt(EntityLivingBase caster, DamageSource source, float damage) {
        hitEntities.clear();
        currentRotation = 0.0f;
    }

    @Override
    public void writeTypeNBT(NBTTagCompound nbt) {
        nbt.setFloat("arcAngle", arcAngle);
        nbt.setFloat("range", range);
        nbt.setFloat("damage", damage);
        nbt.setFloat("knockback", knockback);
        nbt.setString("sweepMode", sweepMode.name());
        nbt.setFloat("sweepSpeed", sweepSpeed);
        nbt.setInteger("spinDurationTicks", spinDurationTicks);
        nbt.setBoolean("piercing", piercing);
        nbt.setFloat("innerRadius", innerRadius);
    }

    @Override
    public void readTypeNBT(NBTTagCompound nbt) {
        this.arcAngle = nbt.hasKey("arcAngle") ? nbt.getFloat("arcAngle") : 90.0f;
        this.range = nbt.hasKey("range") ? nbt.getFloat("range") : 6.0f;
        this.damage = nbt.hasKey("damage") ? nbt.getFloat("damage") : 7.0f;
        this.knockback = nbt.hasKey("knockback") ? nbt.getFloat("knockback") : 1.5f;
        try {
            this.sweepMode = SweepMode.valueOf(nbt.getString("sweepMode"));
        } catch (Exception e) {
            this.sweepMode = SweepMode.SWIPE;
        }
        this.sweepSpeed = nbt.hasKey("sweepSpeed") ? nbt.getFloat("sweepSpeed") : 6.0f;
        this.spinDurationTicks = nbt.hasKey("spinDurationTicks") ? nbt.getInteger("spinDurationTicks") : 60;
        this.piercing = !nbt.hasKey("piercing") || nbt.getBoolean("piercing");
        this.innerRadius = nbt.hasKey("innerRadius") ? nbt.getFloat("innerRadius") : 0.0f;
    }

    // Getters & Setters
    public float getArcAngle() {
        return arcAngle;
    }

    public void setArcAngle(float arcAngle) {
        this.arcAngle = arcAngle;
    }

    public float getRange() {
        return range;
    }

    public void setRange(float range) {
        this.range = range;
    }

    public float getDamage() {
        return damage;
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    public float getKnockback() {
        return knockback;
    }

    public void setKnockback(float knockback) {
        this.knockback = knockback;
    }

    public SweepMode getSweepModeEnum() {
        return sweepMode;
    }

    public void setSweepModeEnum(SweepMode sweepMode) {
        this.sweepMode = sweepMode;
    }

    @Override
    public int getSweepMode() {
        return sweepMode.ordinal();
    }

    @Override
    public void setSweepMode(int mode) {
        SweepMode[] values = SweepMode.values();
        this.sweepMode = mode >= 0 && mode < values.length ? values[mode] : SweepMode.SWIPE;
    }

    public float getSweepSpeed() {
        return sweepSpeed;
    }

    public void setSweepSpeed(float sweepSpeed) {
        this.sweepSpeed = sweepSpeed;
    }

    public int getSpinDurationTicks() {
        return spinDurationTicks;
    }

    public void setSpinDurationTicks(int spinDurationTicks) {
        this.spinDurationTicks = Math.max(1, spinDurationTicks);
    }

    public boolean isPiercing() {
        return piercing;
    }

    public void setPiercing(boolean piercing) {
        this.piercing = piercing;
    }

    public float getInnerRadius() {
        return innerRadius;
    }

    public void setInnerRadius(float innerRadius) {
        this.innerRadius = innerRadius;
    }

    public float getCurrentRotation() {
        return currentRotation;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void getAbilityDefinitions(List<FieldDef> defs) {
        defs.addAll(Arrays.asList(
            FieldDef.row(
                FieldDef.floatField("enchantment.damage", this::getDamage, this::setDamage),
                FieldDef.floatField("gui.range", this::getRange, this::setRange)
            ),
            FieldDef.floatField("ability.knockback", this::getKnockback, this::setKnockback),
            FieldDef.section("ability.section.sweep"),
            FieldDef.enumField("ability.sweepMode", SweepMode.class, this::getSweepModeEnum, this::setSweepModeEnum)
                .hover("ability.hover.sweepMode"),
            FieldDef.row(
                FieldDef.floatField("ability.arcAngle", this::getArcAngle, this::setArcAngle),
                FieldDef.floatField("ability.innerRadius", this::getInnerRadius, this::setInnerRadius)
            ),
            FieldDef.floatField("ability.sweepSpeed", this::getSweepSpeed, this::setSweepSpeed),
            FieldDef.intField("ability.spinDuration", this::getSpinDurationTicks, this::setSpinDurationTicks)
                .range(1, 1000)
                .visibleWhen(() -> this.getSweepModeEnum() == SweepMode.SPIN),
            FieldDef.boolField("ability.piercing", this::isPiercing, this::setPiercing)
                .hover("ability.hover.piercing"),
            AbilityFieldDefs.effectsListField("ability.effects", this::getEffects, this::setEffects)
        ));
    }
}
