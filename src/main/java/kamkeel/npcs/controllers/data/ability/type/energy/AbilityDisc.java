package kamkeel.npcs.controllers.data.ability.type.energy;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.controllers.data.ability.AbilityVariant;
import kamkeel.npcs.controllers.data.ability.LockMovementType;
import kamkeel.npcs.controllers.data.ability.TargetingMode;
import kamkeel.npcs.controllers.data.ability.data.*;
import kamkeel.npcs.controllers.data.ability.gui.AbilityFieldDefs;
import kamkeel.npcs.controllers.data.telegraph.TelegraphType;
import kamkeel.npcs.entity.EntityAbilityDisc;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Vec3;
import noppes.npcs.api.ability.type.IAbilityDisc;
import noppes.npcs.client.gui.builder.FieldDef;

import java.util.Arrays;
import java.util.List;

/**
 * Disc ability: Spawns a flat spinning disc projectile.
 * Supports 1-8 projectiles with shared visuals and per-projectile anchor points.
 * Has optional boomerang behavior to return to owner.
 */
public class AbilityDisc extends AbilityEnergyProjectile<EntityAbilityDisc> implements IAbilityDisc {

    private float discRadius = 1.0f;
    private float discThickness = 0.2f;
    private boolean vertical = false;
    private boolean boomerang = false;
    private int boomerangDelay = 40;

    public AbilityDisc() {
        super(
            new EnergyDisplayData(0xFFFFFF, 0xFF8800, true, 0.4f, 0.5f, 5.0f),
            new EnergyCombatData(8.0f, 1.2f, 0.15f, false, 3.0f, 0.5f),
            new EnergyHomingData(0.6f, true, 0.12f, 18.0f),
            new EnergyLifespanData(35.0f, 200)
        );
        this.typeId = "ability.cnpc.disc";
        this.name = "Disc";
        this.targetingMode = TargetingMode.AGGRO_TARGET;
        this.maxRange = 30.0f;
        this.minRange = 5.0f;
        this.cooldownTicks = 0;
        this.windUpTicks = 60;
        this.lockMovement = LockMovementType.WINDUP;
        this.telegraphType = TelegraphType.CIRCLE;
        this.showTelegraph = true;
        this.windUpAnimationName = "Ability_Disc_Windup";
        this.activeAnimationName = "Ability_Disc_Active";
    }

    // ==================== ABSTRACT IMPLEMENTATIONS ====================

    @Override
    protected EntityAbilityDisc createEntity(EntityLivingBase caster, EntityLivingBase target,
                                              Vec3 spawnPos, EnergyDisplayData resolved, int index) {
        return new EntityAbilityDisc(
            caster.worldObj, caster, target,
            spawnPos.xCoord, spawnPos.yCoord, spawnPos.zCoord,
            discRadius, discThickness,
            resolved, combatData, homingData, lightningData, lifespanData, trajectoryData,
            boomerang, boomerangDelay);
    }

    @Override
    protected void fireEntity(EntityAbilityDisc disc, EntityLivingBase target) {
        if (isPreview()) {
            disc.startPreviewFiring();
        } else {
            disc.startMoving(target);
        }
    }

    @Override
    protected void setupEntityCharging(EntityAbilityDisc disc, ProjectileData projData, int index) {
        disc.setupCharging(projData.anchor, windUpTicks, vertical);
    }

    @Override
    protected void setupEntityPreview(EntityAbilityDisc disc, EntityLivingBase caster,
                                       EnergyDisplayData resolved, ProjectileData projData, int index) {
        disc.setupPreview(caster, discRadius, discThickness, resolved, lightningData, projData.anchor, windUpTicks, vertical);
    }

    @Override
    protected EntityAbilityDisc[] createEntityArray(int size) {
        return new EntityAbilityDisc[size];
    }

    @Override
    protected float getProjectileTelegraphRadius() {
        return discRadius * 1.5f;
    }

    // ==================== VARIANTS ====================

    @Override
    public List<AbilityVariant> getVariants() {
        return Arrays.asList(
            new AbilityVariant("ability.variant.single", a -> {
                a.setName("Disc");
            }),
            new AbilityVariant("ability.variant.dual", a -> {
                AbilityDisc disc = (AbilityDisc) a;
                a.setName("Dual Disc");
                disc.setProjectileCount(2);
                disc.setFireDelay(5);
                disc.projectiles[1].colorOverride = true;
                disc.projectiles[1].innerColor = 0xFFFFFF;
                disc.projectiles[1].outerColor = 0x8800FF;
                a.setWindUpAnimationName("Ability_DiscDual_Windup");
                a.setActiveAnimationName("Ability_DiscDual_Active");
            })
        );
    }

    // ==================== TYPE-SPECIFIC NBT ====================

    @Override
    protected void writeTypeSpecificNBT(NBTTagCompound nbt) {
        nbt.setFloat("discRadius", discRadius);
        nbt.setFloat("discThickness", discThickness);
        nbt.setBoolean("vertical", vertical);
        nbt.setBoolean("boomerang", boomerang);
        nbt.setInteger("boomerangDelay", boomerangDelay);
    }

    @Override
    protected void readTypeSpecificNBT(NBTTagCompound nbt) {
        this.discRadius = nbt.getFloat("discRadius");
        this.discThickness = nbt.getFloat("discThickness");
        this.vertical = nbt.getBoolean("vertical");
        this.boomerang = nbt.getBoolean("boomerang");
        this.boomerangDelay = nbt.getInteger("boomerangDelay");
    }

    // ==================== TYPE-SPECIFIC GETTERS ====================

    public float getSpeed() { return homingData.speed; }
    public void setSpeed(float speed) { homingData.speed = speed; }

    public float getDiscRadius() { return discRadius; }
    public void setDiscRadius(float discRadius) { this.discRadius = discRadius; }

    public float getDiscThickness() { return discThickness; }
    public void setDiscThickness(float discThickness) { this.discThickness = discThickness; }

    public boolean isVertical() { return vertical; }
    public void setVertical(boolean vertical) { this.vertical = vertical; }

    public boolean isBoomerang() { return boomerang; }
    public void setBoomerang(boolean boomerang) { this.boomerang = boomerang; }

    public int getBoomerangDelay() { return boomerangDelay; }
    public void setBoomerangDelay(int boomerangDelay) { this.boomerangDelay = boomerangDelay; }

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
            FieldDef.floatField("stats.speed", this::getSpeed, this::setSpeed)
        ));
        defs.add(FieldDef.floatField("ability.knockback", this::getKnockback, this::setKnockback));
        defs.add(FieldDef.section("ability.section.disc"));
        defs.add(FieldDef.row(
            FieldDef.floatField("gui.radius", this::getDiscRadius, this::setDiscRadius),
            FieldDef.floatField("gui.thickness", this::getDiscThickness, this::setDiscThickness)
        ));
        defs.add(FieldDef.boolField("ability.vertical", this::isVertical, this::setVertical)
            .hover("ability.hover.vertical"));
        defs.add(FieldDef.row(
            FieldDef.floatField("ability.maxDistance", this::getMaxDistance, this::setMaxDistance),
            FieldDef.intField("ability.lifetime", this::getMaxLifetime, this::setMaxLifetime)
        ));
        defs.add(FieldDef.section("ability.section.homing"));
        defs.add(FieldDef.boolField("gui.enabled", this::isHoming, this::setHoming).hover("ability.hover.homing"));
        defs.add(FieldDef.floatField("gui.strength", this::getHomingStrength, this::setHomingStrength).visibleWhen(this::isHoming));
        defs.add(FieldDef.section("ability.section.boomerang"));
        defs.add(FieldDef.boolField("gui.enabled", this::isBoomerang, this::setBoomerang).hover("ability.hover.boomerang"));
        defs.add(FieldDef.intField("gui.delay", this::getBoomerangDelay, this::setBoomerangDelay).visibleWhen(this::isBoomerang));
        defs.add(FieldDef.section("ability.section.explosive"));
        defs.add(FieldDef.boolField("gui.enabled", this::isExplosive, this::setExplosive).hover("ability.hover.explosive"));
        defs.add(FieldDef.floatField("gui.radius", this::getExplosionRadius, this::setExplosionRadius).visibleWhen(this::isExplosive));
        defs.add(AbilityFieldDefs.effectsListField("ability.effects", this::getEffects, this::setEffects));
    }
}
