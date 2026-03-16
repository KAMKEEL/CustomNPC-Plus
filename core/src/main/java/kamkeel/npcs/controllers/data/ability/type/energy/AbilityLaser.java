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
import kamkeel.npcs.controllers.data.ability.enums.*;
import kamkeel.npcs.controllers.data.ability.data.energy.EnergyCombatData;
import kamkeel.npcs.controllers.data.ability.data.energy.EnergyDisplayData;
import kamkeel.npcs.controllers.data.ability.data.energy.EnergyHomingData;
import kamkeel.npcs.controllers.data.ability.data.energy.EnergyLifespanData;
import kamkeel.npcs.controllers.data.ability.data.ProjectileData;
import kamkeel.npcs.controllers.data.telegraph.Telegraph;
import kamkeel.npcs.controllers.data.telegraph.TelegraphInstance;
import kamkeel.npcs.controllers.data.telegraph.TelegraphType;
import noppes.npcs.api.ability.type.IAbilityLaser;
import java.util.List;

/**
 * Laser ability: Sweeping beam that follows the caster's look vector.
 * Expands from origin to maxLength, then stays active until maxLifetime expires.
 * Single-projectile only (always projectileCount=1).
 */
public class AbilityLaser extends AbilityEnergyProjectile<EntityAbilityLaser> implements IAbilityLaser {

    private float laserWidth = 0.3f;
    private float expansionSpeed = 3.0f;
    private float maxLength = 32.0f;

    public AbilityLaser() {
        super(
            new EnergyDisplayData(0xFFFFFF, 0xFF0000, true, 0.4f, 0.5f, 4.0f),
            new EnergyCombatData(4.0f, 0.5f, 0.05f, false, 2.0f, 0.5f, HitType.MULTI, 5),
            new EnergyHomingData(),
            new EnergyLifespanData(150.0f, 100)
        );
        this.typeId = "ability.cnpc.laser_shot";
        this.name = "Laser";
        this.targetingMode = TargetingMode.AGGRO_TARGET;
        this.maxRange = 30.0f;
        this.minRange = 3.0f;
        this.cooldownTicks = 0;
        this.windUpTicks = 15;
        this.lockMovement = LockMode.WINDUP_AND_ACTIVE;
        this.rotationMode = RotationMode.TRACK;
        this.rotationPhase = LockMode.WINDUP_AND_ACTIVE;
        this.trackSpeed = 4.0f;
        this.telegraphType = TelegraphType.LINE;
        this.showTelegraph = true;
        this.windUpAnimationName = "Ability_Laser_Windup";
        this.activeAnimationName = "Ability_Laser_Active";

        this.defaultIconLayers = new DefaultIconLayer[]{
            new DefaultIconLayer("customnpcs:textures/gui/ability/laser_shot.png"),
            new DefaultIconLayer("customnpcs:textures/gui/ability/laser_shot_overlay.png",
                () -> isOuterColorEnabled() ? getOuterColor() : getInnerColor())
        };
    }

    // ==================== OVERRIDES ====================

    @Override
    protected AnchorPoint getDefaultAnchor(int index) {
        return AnchorPoint.FRONT;
    }

    @Override
    public boolean isTargetingModeLocked() {
        return false;
    }

    @Override
    public int getMaxPreviewDuration() {
        int lifetime = lifespanData.maxLifetime;
        return lifetime > 0 ? Math.min(lifetime, 60) : 60;
    }

    /**
     * LINE telegraph: positioned at caster, aimed toward target.
     * Overrides the circle telegraph from AbilityEnergyProjectile.
     */
    @Override
    public TelegraphInstance createTelegraph(IEntityLivingBase caster, IEntityLivingBase target) {
        if (!showTelegraph || telegraphType == TelegraphType.NONE || isPlayerCaster(caster)) {
            return null;
        }

        Telegraph telegraph = Telegraph.line(getTelegraphLength(), getTelegraphWidth());
        telegraph.setDurationTicks(windUpTicks);
        telegraph.setColor(windUpColor);
        telegraph.setWarningColor(activeColor);
        telegraph.setWarningStartTick(Math.max(5, windUpTicks / 4));
        telegraph.setHeightOffset(telegraphHeightOffset);

        double groundY = findGroundLevel(caster.worldObj, caster.posX, caster.posY, caster.posZ);
        TelegraphInstance instance = new TelegraphInstance(telegraph, caster.posX, groundY, caster.posZ, caster.rotationYaw);
        instance.setCasterEntityId(caster.getEntityId());
        instance.setEntityIdToFollow(caster.getEntityId());
        if (target != null) {
            instance.setTargetEntityId(target.getEntityId());
        }
        return instance;
    }

    @Override
    public float getTelegraphLength() {
        return maxLength;
    }

    @Override
    public float getTelegraphWidth() {
        return laserWidth * 2.0f;
    }

    // ==================== ABSTRACT IMPLEMENTATIONS ====================

    @Override
    protected EntityAbilityLaser createEntity(IEntityLivingBase caster, IEntityLivingBase target,
                                              IVector3 spawnPos, EnergyDisplayData resolved, int index) {
        EntityAbilityLaser laser = new EntityAbilityLaser(
            caster.worldObj, caster, target,
            spawnPos.xCoord, spawnPos.yCoord, spawnPos.zCoord,
            laserWidth,
            resolved, combatData, lightningData, lifespanData,
            expansionSpeed, maxLength);
        return laser;
    }

    @Override
    protected void fireEntity(EntityAbilityLaser laser, IEntityLivingBase target) {
        laser.startMoving(target);
    }

    @Override
    protected void setupEntityCharging(EntityAbilityLaser laser, ProjectileData projData, int index) {
        laser.setupCharging(projData.anchor, windUpTicks);
    }

    @Override
    protected void setupEntityPreview(EntityAbilityLaser laser, IEntityLivingBase caster,
                                      EnergyDisplayData resolved, ProjectileData projData, int index) {
        laser.setupPreview(caster, laserWidth, resolved, lightningData, projData.anchor, windUpTicks, expansionSpeed, maxLength);
    }

    @Override
    protected EntityAbilityLaser[] createEntityArray(int size) {
        return new EntityAbilityLaser[size];
    }

    @Override
    protected float getProjectileTelegraphRadius() {
        return laserWidth * 2.0f;
    }

    // ==================== TYPE-SPECIFIC NBT ====================

    @Override
    protected void writeTypeSpecificNBT(INbt nbt) {
        nbt.setFloat("laserWidth", laserWidth);
        nbt.setFloat("expansionSpeed", expansionSpeed);
        nbt.setFloat("maxLength", maxLength);
    }

    @Override
    protected void readTypeSpecificNBT(INbt nbt) {
        this.laserWidth = nbt.getFloat("laserWidth");
        this.expansionSpeed = nbt.getFloat("expansionSpeed");
        this.maxLength = nbt.hasKey("maxLength") ? nbt.getFloat("maxLength") : 32.0f;
    }

    // ==================== TYPE-SPECIFIC GETTERS ====================

    public float getLaserWidth() {
        return laserWidth;
    }

    public void setLaserWidth(float laserWidth) {
        this.laserWidth = laserWidth;
    }

    public float getExpansionSpeed() {
        return expansionSpeed;
    }

    public void setExpansionSpeed(float expansionSpeed) {
        this.expansionSpeed = expansionSpeed;
    }

    public float getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(float maxLength) {
        this.maxLength = maxLength;
    }

    // ==================== TYPE-SPECIFIC GUI ====================

    @ClientOnly
    @Override
    protected void addTypeDefinitions(List<FieldDef> defs) {
        defs.add(FieldDef.floatField("enchantment.damage", this::getDamage, this::setDamage));
        defs.add(FieldDef.row(
            FieldDef.floatField("ability.knockback", this::getKnockback, this::setKnockback),
            FieldDef.floatField("ability.knockbackUp", this::getKnockbackUp, this::setKnockbackUp)
        ));
        defs.add(FieldDef.section("ability.section.beam"));
        defs.add(FieldDef.row(
            FieldDef.floatField("ability.laserWidth", this::getLaserWidth, this::setLaserWidth).range(0.1f, 100.0f),
            FieldDef.floatField("ability.expansionSpeed", this::getExpansionSpeed, this::setExpansionSpeed).range(0.1f, 50.0f)
        ));
        defs.add(FieldDef.row(
            FieldDef.floatField("ability.maxLength", this::getMaxLength, this::setMaxLength).range(1.0f, 500.0f),
            FieldDef.intField("ability.lifetime", this::getMaxLifetime, this::setMaxLifetime).range(1, 1200)
        ));
        defs.add(FieldDef.section("ability.section.explosive"));
        defs.add(FieldDef.boolField("gui.enabled", this::isExplosive, this::setExplosive).hover("ability.hover.explosive"));
        defs.add(FieldDef.floatField("gui.radius", this::getExplosionRadius, this::setExplosionRadius)
            .range(0.0f, EnergyCombatData.MAX_EXPLOSION_RADIUS)
            .visibleWhen(this::isExplosive));
        defs.add(AbilityFieldDefs.effectsListField("ability.effects", this::getEffects, this::setEffects));
    }
}
