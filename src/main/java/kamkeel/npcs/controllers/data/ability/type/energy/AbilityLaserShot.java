package kamkeel.npcs.controllers.data.ability.type.energy;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.controllers.data.ability.AnchorPoint;
import kamkeel.npcs.controllers.data.ability.LockMovementType;
import kamkeel.npcs.controllers.data.ability.RotationMode;
import kamkeel.npcs.controllers.data.ability.TargetingMode;
import kamkeel.npcs.controllers.data.ability.data.EnergyCombatData;
import kamkeel.npcs.controllers.data.ability.data.EnergyDisplayData;
import kamkeel.npcs.controllers.data.ability.data.EnergyHomingData;
import kamkeel.npcs.controllers.data.ability.data.EnergyLifespanData;
import kamkeel.npcs.controllers.data.ability.data.ProjectileData;
import kamkeel.npcs.controllers.data.ability.gui.AbilityFieldDefs;
import kamkeel.npcs.controllers.data.telegraph.Telegraph;
import kamkeel.npcs.controllers.data.telegraph.TelegraphInstance;
import kamkeel.npcs.controllers.data.telegraph.TelegraphType;
import kamkeel.npcs.entity.EntityAbilityLaser;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Vec3;
import noppes.npcs.api.ability.type.IAbilityLaserShot;
import noppes.npcs.client.gui.builder.FieldDef;

import java.util.List;

/**
 * Laser Shot ability: Fast expanding thin line that pierces through targets.
 * Travels in a straight line from origin, damaging all entities it passes through.
 * Single-projectile only (always projectileCount=1).
 */
public class AbilityLaserShot extends AbilityEnergyProjectile<EntityAbilityLaser> implements IAbilityLaserShot {

    private float laserWidth = 0.3f;
    private float expansionSpeed = 3.0f;
    private int lingerTicks = 8;
    private boolean dieOnImpact = false;

    public AbilityLaserShot() {
        super(
            new EnergyDisplayData(0xFFFFFF, 0xFF0000, true, 0.4f, 0.5f, 0.0f),
            new EnergyCombatData(6.0f, 0.5f, 0.05f, false, 2.0f, 0.5f),
            new EnergyHomingData(),
            new EnergyLifespanData(150.0f, 100)
        );
        this.typeId = "ability.cnpc.laser_shot";
        this.name = "Laser Shot";
        this.targetingMode = TargetingMode.AGGRO_TARGET;
        this.maxRange = 35.0f;
        this.minRange = 3.0f;
        this.cooldownTicks = 0;
        this.windUpTicks = 15;
        this.lockMovement = LockMovementType.WINDUP_AND_ACTIVE;
        this.rotationMode = RotationMode.TRACK;
        this.rotationPhase = LockMovementType.WINDUP;
        this.telegraphType = TelegraphType.LINE;
        this.showTelegraph = true;
        this.windUpAnimationName = "Ability_Laser_Windup";
        this.activeAnimationName = "Ability_Laser_Active";
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
        return lifespanData.maxLifetime > 0 ? Math.min(lifespanData.maxLifetime, 60) : 60;
    }

    /**
     * LINE telegraph: positioned at caster, aimed toward target.
     * Overrides the circle telegraph from AbilityEnergyProjectile.
     */
    @Override
    public TelegraphInstance createTelegraph(EntityLivingBase caster, EntityLivingBase target) {
        if (!showTelegraph || telegraphType == TelegraphType.NONE || isPlayerCaster(caster)) {
            return null;
        }

        Telegraph telegraph = Telegraph.line(getTelegraphLength(), getTelegraphWidth());
        telegraph.setDurationTicks(windUpTicks);
        telegraph.setColor(windUpColor);
        telegraph.setWarningColor(activeColor);
        telegraph.setWarningStartTick(Math.max(5, windUpTicks / 4));
        telegraph.setHeightOffset(telegraphHeightOffset);

        TelegraphInstance instance = new TelegraphInstance(telegraph, caster.posX, caster.posY, caster.posZ, caster.rotationYaw);
        instance.setCasterEntityId(caster.getEntityId());
        instance.setEntityIdToFollow(caster.getEntityId());
        if (target != null) {
            instance.setTargetEntityId(target.getEntityId());
        }
        return instance;
    }

    @Override
    public float getTelegraphLength() {
        return lifespanData.maxDistance;
    }

    @Override
    public float getTelegraphWidth() {
        return laserWidth * 2.0f;
    }

    // ==================== ABSTRACT IMPLEMENTATIONS ====================

    @Override
    protected EntityAbilityLaser createEntity(EntityLivingBase caster, EntityLivingBase target,
                                              Vec3 spawnPos, EnergyDisplayData resolved, int index) {
        EntityAbilityLaser laser = new EntityAbilityLaser(
            caster.worldObj, caster, target,
            spawnPos.xCoord, spawnPos.yCoord, spawnPos.zCoord,
            laserWidth,
            resolved, combatData, lightningData, lifespanData, trajectoryData,
            expansionSpeed, lingerTicks);
        laser.setDieOnImpact(dieOnImpact);
        return laser;
    }

    @Override
    protected void fireEntity(EntityAbilityLaser laser, EntityLivingBase target) {
        laser.setLockVerticalDirection(true);
        laser.startMoving(target);
    }

    @Override
    protected void setupEntityCharging(EntityAbilityLaser laser, ProjectileData projData, int index) {
        laser.setupCharging(projData.anchor, windUpTicks);
    }

    @Override
    protected void setupEntityPreview(EntityAbilityLaser laser, EntityLivingBase caster,
                                      EnergyDisplayData resolved, ProjectileData projData, int index) {
        laser.setupPreview(caster, laserWidth, resolved, lightningData, expansionSpeed, lifespanData.maxDistance);
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
    protected void writeTypeSpecificNBT(NBTTagCompound nbt) {
        nbt.setFloat("laserWidth", laserWidth);
        nbt.setFloat("expansionSpeed", expansionSpeed);
        nbt.setInteger("lingerTicks", lingerTicks);
        nbt.setBoolean("dieOnImpact", dieOnImpact);
    }

    @Override
    protected void readTypeSpecificNBT(NBTTagCompound nbt) {
        this.laserWidth = nbt.getFloat("laserWidth");
        this.expansionSpeed = nbt.getFloat("expansionSpeed");
        this.lingerTicks = nbt.getInteger("lingerTicks");
        this.dieOnImpact = nbt.getBoolean("dieOnImpact");
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

    public int getLingerTicks() {
        return lingerTicks;
    }

    public void setLingerTicks(int lingerTicks) {
        this.lingerTicks = lingerTicks;
    }

    public boolean isDieOnImpact() {
        return dieOnImpact;
    }

    public void setDieOnImpact(boolean dieOnImpact) {
        this.dieOnImpact = dieOnImpact;
    }

    // ==================== TYPE-SPECIFIC GUI ====================

    @SideOnly(Side.CLIENT)
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
            FieldDef.intField("ability.lingerTicks", this::getLingerTicks, this::setLingerTicks).range(1, 200),
            FieldDef.intField("ability.lifetime", this::getMaxLifetime, this::setMaxLifetime).range(1, 1200)
        ));
        defs.add(FieldDef.floatField("ability.maxDistance", this::getMaxDistance, this::setMaxDistance).range(1.0f, 500.0f));
        defs.add(FieldDef.boolField("ability.dieOnImpact", this::isDieOnImpact, this::setDieOnImpact));
        defs.add(FieldDef.section("ability.section.explosive"));
        defs.add(FieldDef.boolField("gui.enabled", this::isExplosive, this::setExplosive).hover("ability.hover.explosive"));
        defs.add(FieldDef.floatField("gui.radius", this::getExplosionRadius, this::setExplosionRadius).visibleWhen(this::isExplosive));
        defs.add(AbilityFieldDefs.effectsListField("ability.effects", this::getEffects, this::setEffects));
    }
}
