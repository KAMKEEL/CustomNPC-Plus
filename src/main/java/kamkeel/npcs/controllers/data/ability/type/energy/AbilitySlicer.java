package kamkeel.npcs.controllers.data.ability.type.energy;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.controllers.data.ability.AbilityVariant;
import kamkeel.npcs.controllers.data.ability.LockMovementType;
import kamkeel.npcs.controllers.data.ability.TargetingMode;
import kamkeel.npcs.controllers.data.ability.data.*;
import kamkeel.npcs.controllers.data.ability.gui.AbilityFieldDefs;
import kamkeel.npcs.controllers.data.telegraph.TelegraphType;
import kamkeel.npcs.entity.EntityEnergySlicer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Vec3;
import noppes.npcs.client.gui.builder.FieldDef;

import java.util.Arrays;
import java.util.List;

/**
 * Energy Slicer ability: Fires a thin, wide blade projectile in a straight line.
 * JJK Cleave/Dismantle inspired. Piercing by default.
 * Extends AbilityEnergyProjectile to leverage the existing projectile infrastructure.
 */
public class AbilitySlicer extends AbilityEnergyProjectile<EntityEnergySlicer> {

    private float sliceWidth = 3.0f;
    private float sliceThickness = 0.15f;
    private boolean piercing = true;

    public AbilitySlicer() {
        super(
            new EnergyDisplayData(0xFF4444, 0xFF0000, true, 0.2f, 0.6f, 0.0f),
            new EnergyCombatData(12.0f, 0.5f, 0.0f, false, 3.0f, 0.5f),
            new EnergyHomingData(0.8f, false, 0.0f, 0.0f),
            new EnergyLifespanData(40.0f, 100)
        );
        this.typeId = "ability.cnpc.slicer";
        this.name = "Slicer";
        this.targetingMode = TargetingMode.AGGRO_TARGET;
        this.maxRange = 30.0f;
        this.minRange = 3.0f;
        this.cooldownTicks = 40;
        this.windUpTicks = 15;
        this.lockMovement = LockMovementType.WINDUP;
        this.telegraphType = TelegraphType.LINE;
        this.showTelegraph = true;
        this.windUpAnimationName = "";
        this.activeAnimationName = "";
    }

    // ==================== ABSTRACT IMPLEMENTATIONS ====================

    @Override
    protected EntityEnergySlicer createEntity(EntityLivingBase caster, EntityLivingBase target,
                                               Vec3 spawnPos, EnergyDisplayData resolved, int index) {
        return new EntityEnergySlicer(
            caster.worldObj, caster, target,
            spawnPos.xCoord, spawnPos.yCoord, spawnPos.zCoord,
            sliceWidth, sliceThickness,
            resolved, combatData, homingData, lightningData, lifespanData, trajectoryData,
            piercing
        );
    }

    @Override
    protected void fireEntity(EntityEnergySlicer slicer, EntityLivingBase target) {
        if (isPreview()) {
            slicer.startPreviewFiring();
        } else {
            slicer.startMoving(target);
        }
    }

    @Override
    protected void setupEntityCharging(EntityEnergySlicer slicer, ProjectileData projData, int index) {
        slicer.setupSlicerCharging(projData.anchor, windUpTicks, sliceWidth, sliceThickness);
    }

    @Override
    protected void setupEntityPreview(EntityEnergySlicer slicer, EntityLivingBase caster,
                                       EnergyDisplayData resolved, ProjectileData projData, int index) {
        slicer.setupPreview(caster, sliceWidth, sliceThickness, resolved, lightningData, projData.anchor, windUpTicks);
    }

    @Override
    protected EntityEnergySlicer[] createEntityArray(int size) {
        return new EntityEnergySlicer[size];
    }

    @Override
    protected float getProjectileTelegraphRadius() {
        return sliceWidth * 0.75f;
    }

    // ==================== VARIANTS ====================

    @Override
    public List<AbilityVariant> getVariants() {
        return Arrays.asList(
            new AbilityVariant("ability.variant.single", a -> {
                a.setName("Slicer");
            }),
            new AbilityVariant("ability.variant.dual", a -> {
                AbilitySlicer slicer = (AbilitySlicer) a;
                a.setName("Double Slicer");
                slicer.setProjectileCount(2);
                slicer.setFireDelay(5);
                a.setWindUpAnimationName("");
                a.setActiveAnimationName("");
            })
        );
    }

    // ==================== TYPE-SPECIFIC NBT ====================

    @Override
    protected void writeTypeSpecificNBT(NBTTagCompound nbt) {
        nbt.setFloat("sliceWidth", sliceWidth);
        nbt.setFloat("sliceThickness", sliceThickness);
        nbt.setBoolean("piercing", piercing);
    }

    @Override
    protected void readTypeSpecificNBT(NBTTagCompound nbt) {
        this.sliceWidth = nbt.hasKey("sliceWidth") ? nbt.getFloat("sliceWidth") : 3.0f;
        this.sliceThickness = nbt.hasKey("sliceThickness") ? nbt.getFloat("sliceThickness") : 0.15f;
        this.piercing = !nbt.hasKey("piercing") || nbt.getBoolean("piercing");
    }

    // ==================== TYPE-SPECIFIC GETTERS ====================

    public float getSlicerSpeed() { return homingData.speed; }
    public void setSlicerSpeed(float speed) { homingData.speed = speed; }

    public float getSliceWidth() { return sliceWidth; }
    public void setSliceWidth(float width) { this.sliceWidth = Math.max(0.5f, width); }

    public float getSliceThickness() { return sliceThickness; }
    public void setSliceThickness(float thickness) { this.sliceThickness = Math.max(0.05f, thickness); }

    public boolean isPiercing() { return piercing; }
    public void setPiercing(boolean piercing) { this.piercing = piercing; }

    // ==================== TYPE-SPECIFIC GUI ====================

    @SideOnly(Side.CLIENT)
    @Override
    protected void addTypeDefinitions(List<FieldDef> defs) {
        defs.add(FieldDef.row(
            FieldDef.intField("ability.projectileCount", this::getProjectileCount, this::setProjectileCount).range(1, MAX_PROJECTILES),
            FieldDef.intField("ability.fireDelay", this::getFireDelay, this::setFireDelay)
                .range(0, 200).visibleWhen(() -> projectileCount > 1)
        ));
        defs.add(FieldDef.row(
            FieldDef.floatField("enchantment.damage", this::getDamage, this::setDamage),
            FieldDef.floatField("stats.speed", this::getSlicerSpeed, this::setSlicerSpeed)
        ));
        defs.add(FieldDef.floatField("ability.knockback", this::getKnockback, this::setKnockback));
        defs.add(FieldDef.section("ability.section.slicer"));
        defs.add(FieldDef.row(
            FieldDef.floatField("gui.width", this::getSliceWidth, this::setSliceWidth).range(0.5f, 20.0f),
            FieldDef.floatField("gui.thickness", this::getSliceThickness, this::setSliceThickness).range(0.05f, 2.0f)
        ));
        defs.add(FieldDef.boolField("ability.piercing", this::isPiercing, this::setPiercing)
            .hover("ability.hover.piercing"));
        defs.add(FieldDef.row(
            FieldDef.floatField("ability.maxDistance", this::getMaxDistance, this::setMaxDistance),
            FieldDef.intField("ability.lifetime", this::getMaxLifetime, this::setMaxLifetime)
        ));
        defs.add(FieldDef.section("ability.section.explosive"));
        defs.add(FieldDef.boolField("gui.enabled", this::isExplosive, this::setExplosive).hover("ability.hover.explosive"));
        defs.add(FieldDef.floatField("gui.radius", this::getExplosionRadius, this::setExplosionRadius).visibleWhen(this::isExplosive));
        defs.add(AbilityFieldDefs.effectsListField("ability.effects", this::getEffects, this::setEffects));
    }
}
