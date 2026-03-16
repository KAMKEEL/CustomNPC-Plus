package kamkeel.npcs.controllers.data.ability.type.energy;

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
import kamkeel.npcs.util.ByteBufUtils;
import noppes.npcs.api.entity.IEntityLiving;
import noppes.npcs.api.IDamageSource;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.INbtList;
import noppes.npcs.api.INbt;
import kamkeel.npcs.controllers.data.ability.AbilityVariant;
import kamkeel.npcs.controllers.data.ability.enums.LockMode;
import kamkeel.npcs.controllers.data.ability.enums.TargetingMode;
import kamkeel.npcs.controllers.data.ability.data.energy.EnergyCombatData;
import kamkeel.npcs.controllers.data.ability.data.energy.EnergyDisplayData;
import kamkeel.npcs.controllers.data.ability.data.energy.EnergyHomingData;
import kamkeel.npcs.controllers.data.ability.data.energy.EnergyLifespanData;
import kamkeel.npcs.controllers.data.ability.data.ProjectileData;
import kamkeel.npcs.controllers.data.telegraph.TelegraphType;
import noppes.npcs.api.ability.type.IAbilityOrb;
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
            new EnergyDisplayData(0xFFFFFF, 0xFF0000, true, 0.4f, 0.5f, 10.0f),
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
        this.lockMovement = LockMode.WINDUP;
        this.telegraphType = TelegraphType.CIRCLE;
        this.showTelegraph = true;
        this.windUpAnimationName = "Ability_Orb_Windup";
        this.activeAnimationName = "Ability_Orb_Active";

        this.defaultIconLayers = new DefaultIconLayer[]{
            new DefaultIconLayer("customnpcs:textures/gui/ability/orb.png",
                () -> isOuterColorEnabled() ? getOuterColor() : getInnerColor())
        };
    }

    // ==================== ABSTRACT IMPLEMENTATIONS ====================

    @Override
    protected EntityAbilityOrb createEntity(IEntityLivingBase caster, IEntityLivingBase target,
                                            IVector3 spawnPos, EnergyDisplayData resolved, int index) {
        return new EntityAbilityOrb(
            caster.worldObj, caster, target,
            spawnPos.xCoord, spawnPos.yCoord, spawnPos.zCoord, orbSize,
            resolved, combatData, homingData, lightningData, lifespanData);
    }

    @Override
    protected void fireEntity(EntityAbilityOrb orb, IEntityLivingBase target) {
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
    protected void setupEntityPreview(EntityAbilityOrb orb, IEntityLivingBase caster,
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
                a.setLockMovement(LockMode.WINDUP_AND_ACTIVE);
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
    protected void writeTypeSpecificNBT(INbt nbt) {
        nbt.setFloat("orbSize", orbSize);
    }

    @Override
    protected void readTypeSpecificNBT(INbt nbt) {
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

    @ClientOnly
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
        defs.add(FieldDef.floatField("gui.radius", this::getExplosionRadius, this::setExplosionRadius)
            .range(0.0f, EnergyCombatData.MAX_EXPLOSION_RADIUS)
            .visibleWhen(this::isExplosive));
        defs.add(AbilityFieldDefs.effectsListField("ability.effects", this::getEffects, this::setEffects));
    }
}
