package kamkeel.npcs.controllers.data.ability.type.energy;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.controllers.data.ability.AbilityVariant;
import kamkeel.npcs.controllers.data.ability.LockMovementType;
import kamkeel.npcs.controllers.data.ability.TargetingMode;
import kamkeel.npcs.controllers.data.ability.data.EnergyCombatData;
import kamkeel.npcs.controllers.data.ability.data.EnergyDisplayData;
import kamkeel.npcs.controllers.data.ability.data.EnergyHomingData;
import kamkeel.npcs.controllers.data.ability.data.EnergyLifespanData;
import kamkeel.npcs.controllers.data.ability.data.ProjectileData;
import kamkeel.npcs.controllers.data.ability.gui.AbilityFieldDefs;
import kamkeel.npcs.controllers.data.telegraph.TelegraphType;
import kamkeel.npcs.entity.EntityAbilityOrb;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Vec3;
import noppes.npcs.api.ability.type.IAbilityOrb;
import noppes.npcs.client.gui.builder.FieldDef;

import java.util.Arrays;
import java.util.List;

/**
 * Orb ability: Spawns homing projectile sphere(s) that track target.
 * Supports 1-8 projectiles with shared visuals and per-projectile anchor points.
 */
public class AbilityOrb extends AbilityEnergyProjectile<EntityAbilityOrb> implements IAbilityOrb {

    private float orbSize = 1.0f;

    public AbilityOrb() {
        super(
            new EnergyDisplayData(0xFFFFFF, 0xFF0000, true, 0.4f, 0.5f, 0.0f),
            new EnergyCombatData(),
            new EnergyHomingData(),
            new EnergyLifespanData()
        );
        this.typeId = "ability.cnpc.orb";
        this.name = "Orb";
        this.targetingMode = TargetingMode.AGGRO_TARGET;
        this.maxRange = 25.0f;
        this.minRange = 5.0f;
        this.cooldownTicks = 0;
        this.windUpTicks = 30;
        this.lockMovement = LockMovementType.WINDUP;
        this.telegraphType = TelegraphType.CIRCLE;
        this.showTelegraph = true;
        this.windUpAnimationName = "Ability_Orb_Windup";
        this.activeAnimationName = "Ability_Orb_Active";
    }

    // ==================== ABSTRACT IMPLEMENTATIONS ====================

    @Override
    protected EntityAbilityOrb createEntity(EntityLivingBase caster, EntityLivingBase target,
                                            Vec3 spawnPos, EnergyDisplayData resolved, int index) {
        return new EntityAbilityOrb(
            caster.worldObj, caster, target,
            spawnPos.xCoord, spawnPos.yCoord, spawnPos.zCoord, orbSize,
            resolved, combatData, homingData, lightningData, lifespanData, trajectoryData);
    }

    @Override
    protected void fireEntity(EntityAbilityOrb orb, EntityLivingBase target) {
        if (isPreview()) {
            orb.startPreviewFiring();
        } else {
            orb.startMoving(target);
        }
    }

    @Override
    protected void setupEntityCharging(EntityAbilityOrb orb, ProjectileData projData, int index) {
        orb.setupCharging(projData.anchor, windUpTicks);
    }

    @Override
    protected void setupEntityPreview(EntityAbilityOrb orb, EntityLivingBase caster,
                                      EnergyDisplayData resolved, ProjectileData projData, int index) {
        orb.setupPreview(caster, orbSize, resolved, lightningData, projData.anchor, windUpTicks);
    }

    @Override
    protected EntityAbilityOrb[] createEntityArray(int size) {
        return new EntityAbilityOrb[size];
    }

    @Override
    protected float getProjectileTelegraphRadius() {
        return orbSize * 1.5f;
    }

    // ==================== VARIANTS ====================

    @Override
    public List<AbilityVariant> getVariants() {
        return Arrays.asList(
            new AbilityVariant("ability.variant.single", a -> {
                a.setName("Orb");
            }),
            new AbilityVariant("ability.variant.dual", a -> {
                AbilityOrb orb = (AbilityOrb) a;
                a.setName("Dual Orb");
                orb.setProjectileCount(2);
                orb.setFireDelay(5);
                a.setWindUpAnimationName("Ability_OrbDual_Windup");
                a.setActiveAnimationName("Ability_OrbDual_Active");
            }),
            new AbilityVariant("ability.variant.barrage", a -> {
                AbilityOrb orb = (AbilityOrb) a;
                a.setName("Orb Barrage");
                orb.setOrbSize(0.5f);
                orb.setDamage(4.0f);
                orb.setKnockback(0.0f);
                orb.setKnockbackUp(0.0f);
                orb.setProjectileCount(2);
                orb.setFireDelay(2);
                a.setLockMovement(LockMovementType.WINDUP_AND_ACTIVE);
                a.setMaxRange(75.0f);
                a.setBurstEnabled(true);
                a.setBurstAmount(15);
                a.setBurstDelay(5);
                a.setBurstReplayAnimations(false);
                a.setBurstOverlap(true);
                a.setWindUpAnimationName("Ability_OrbBarrage_Windup");
                a.setActiveAnimationName("Ability_OrbBarrage_Active");
            })
        );
    }

    // ==================== TYPE-SPECIFIC NBT ====================

    @Override
    protected void writeTypeSpecificNBT(NBTTagCompound nbt) {
        nbt.setFloat("orbSize", orbSize);
    }

    @Override
    protected void readTypeSpecificNBT(NBTTagCompound nbt) {
        this.orbSize = nbt.getFloat("orbSize");
    }

    // ==================== TYPE-SPECIFIC GETTERS ====================

    public float getOrbSpeed() {
        return homingData.speed;
    }

    public void setOrbSpeed(float speed) {
        homingData.speed = speed;
    }

    public float getOrbSize() {
        return orbSize;
    }

    public void setOrbSize(float size) {
        this.orbSize = size;
    }

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
            FieldDef.floatField("stats.speed", this::getOrbSpeed, this::setOrbSpeed)
        ));
        defs.add(FieldDef.row(
            FieldDef.floatField("stats.size", this::getOrbSize, this::setOrbSize).range(0.1f, 100.0f),
            FieldDef.floatField("ability.knockback", this::getKnockback, this::setKnockback)
        ));
        defs.add(FieldDef.row(
            FieldDef.floatField("ability.maxDistance", this::getMaxDistance, this::setMaxDistance).range(1.0f, 500.0f),
            FieldDef.intField("ability.lifetime", this::getMaxLifetime, this::setMaxLifetime).range(1, 1200)
        ));
        defs.add(FieldDef.section("ability.section.homing"));
        defs.add(FieldDef.boolField("gui.enabled", this::isHoming, this::setHoming).hover("ability.hover.homing"));
        defs.add(FieldDef.floatField("gui.strength", this::getHomingStrength, this::setHomingStrength).visibleWhen(this::isHoming));
        defs.add(FieldDef.section("ability.section.explosive"));
        defs.add(FieldDef.boolField("gui.enabled", this::isExplosive, this::setExplosive).hover("ability.hover.explosive"));
        defs.add(FieldDef.floatField("gui.radius", this::getExplosionRadius, this::setExplosionRadius).visibleWhen(this::isExplosive));
        defs.add(AbilityFieldDefs.effectsListField("ability.effects", this::getEffects, this::setEffects));
    }
}
