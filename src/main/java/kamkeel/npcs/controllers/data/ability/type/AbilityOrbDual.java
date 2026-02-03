package kamkeel.npcs.controllers.data.ability.type;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.AnchorPoint;
import kamkeel.npcs.controllers.data.ability.LockMovementType;
import kamkeel.npcs.controllers.data.ability.TargetingMode;
import kamkeel.npcs.controllers.data.ability.data.*;
import noppes.npcs.client.gui.builder.ColumnHint;
import noppes.npcs.client.gui.builder.FieldDef;
import kamkeel.npcs.controllers.data.ability.gui.AbilityFieldDefs;
import kamkeel.npcs.controllers.data.telegraph.Telegraph;
import kamkeel.npcs.controllers.data.telegraph.TelegraphInstance;
import kamkeel.npcs.controllers.data.telegraph.TelegraphType;
import kamkeel.npcs.entity.EntityAbilityOrb;
import kamkeel.npcs.util.AnchorPointHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.ValueUtil;

import java.util.Arrays;
import java.util.List;

public class AbilityOrbDual extends Ability {
    // Ability-specific properties
    private float orbSize = 1.0f;
    private boolean dualFire= true;
    private int dualFireDelay = 20;

    // Data classes for energy properties
    public final EnergyColorData[] colorData = new EnergyColorData[]{
        new EnergyColorData(), new EnergyColorData()
    };
    public final EnergyCombatData combatData = new EnergyCombatData();
    public final EnergyHomingData homingData = new EnergyHomingData();
    public final EnergyLightningData[] lightningData = new EnergyLightningData[]{
        new EnergyLightningData(), new EnergyLightningData()
    };
    public final EnergyLifespanData lifespanData = new EnergyLifespanData();
    private EnergyAnchorData[] anchorData = new EnergyAnchorData[]{
        new EnergyAnchorData(AnchorPoint.RIGHT_HAND), new EnergyAnchorData(AnchorPoint.LEFT_HAND)
    };

    // Transient state for orb entity (used during windup charging)
    private transient EntityAbilityOrb orbEntity = null;
    private transient EntityAbilityOrb orbEntity2 = null;

    public AbilityOrbDual() {
        this.typeId = "ability.cnpc.orb_dual";
        this.name = "Orb Dual";
        this.targetingMode = TargetingMode.AGGRO_TARGET;
        this.maxRange = 25.0f;
        this.minRange = 5.0f;
        this.cooldownTicks = 0;
        this.windUpTicks = 30;
        this.lockMovement = LockMovementType.WINDUP;
        this.telegraphType = TelegraphType.CIRCLE;
        this.showTelegraph = true;
        // Default built-in animation
        this.windUpAnimationName = "Ability_BeamDual_Windup";
        this.activeAnimationName = "Ability_BeamDual_Active";
    }

    @Override
    public boolean isTargetingModeLocked() {
        return true;
    }

    @Override
    public TargetingMode[] getAllowedTargetingModes() {
        return new TargetingMode[]{TargetingMode.AGGRO_TARGET};
    }

    @Override
    public void onExecute(EntityLivingBase caster, EntityLivingBase target, World world) {
        if (world.isRemote) {
            signalCompletion();
            return;
        }

        // Start moving the orb that was spawned during windup
        if (orbEntity != null && !orbEntity.isDead) {
            orbEntity.startMoving(target);
        }

        if (dualFire) {
            if (orbEntity2 != null && !orbEntity2.isDead && dualFireDelay <= 0) {
                orbEntity2.startMoving(target);
            }
        } else {
            orbEntity2.setDead();
        }

        // Ability stays active until entity dies (prevents firing another while projectile is alive)
        // Movement locking is handled separately by the base class
    }

    @Override
    public void onWindUpTick(EntityLivingBase caster, EntityLivingBase target, World world, int tick) {
        if (world.isRemote) return;

        // Spawn orb in charging mode on first tick of windup
        if (tick == 1) {
            // Create orb in charging mode - follows caster based on anchor point during windup
            Vec3 spawnPos1 = AnchorPointHelper.calculateAnchorPosition(caster, anchorData[0]);
            orbEntity = new EntityAbilityOrb(
                world, caster, target,
                spawnPos1.xCoord, spawnPos1.yCoord, spawnPos1.zCoord, orbSize,
                colorData[0], combatData, homingData, lightningData[0], lifespanData);

            Vec3 spawnPos2 = AnchorPointHelper.calculateAnchorPosition(caster, anchorData[1]);
            orbEntity2 = new EntityAbilityOrb(
                world, caster, target,
                spawnPos2.xCoord, spawnPos2.yCoord, spawnPos2.zCoord, orbSize,
                colorData[1], combatData, homingData, lightningData[1], lifespanData);

            orbEntity.setSiblingUUID(orbEntity2.getPersistentID());
            orbEntity2.setSiblingUUID(orbEntity.getPersistentID());

            orbEntity.setupCharging(anchorData[0], windUpTicks);
            orbEntity2.setupCharging(anchorData[1], windUpTicks);

            orbEntity.setEffects(this.effects);

            if (dualFire) {
                orbEntity2.setEffects(this.effects);
            }

            world.spawnEntityInWorld(orbEntity);
            world.spawnEntityInWorld(orbEntity2);
        }
    }

    @Override
    public void onActiveTick(EntityLivingBase caster, EntityLivingBase target, World world, int tick) {
        // Signal completion when entity dies
        if (dualFire) {
            if ((orbEntity == null || orbEntity.isDead) && (orbEntity2 == null || orbEntity2.isDead)) {
                orbEntity = null;
                orbEntity2 = null;
                signalCompletion();
            }

            if (tick == dualFireDelay && orbEntity2 != null && !orbEntity2.isDead) {
                orbEntity2.startMoving(target);
            }
        } else {
            if (orbEntity == null || orbEntity.isDead) {
                orbEntity = null;
                signalCompletion();
            }
        }
    }

    @Override
    public void onComplete(EntityLivingBase caster, EntityLivingBase target) {
        // Nothing to clean up - entity manages itself
    }

    @Override
    public void cleanup() {
        // Despawn orb entity if still alive
        if (orbEntity != null && !orbEntity.isDead) {
            orbEntity.setDead();
        }

        if (orbEntity2 != null && !orbEntity2.isDead) {
            orbEntity2.setDead();
        }

        orbEntity = null;
        orbEntity2 = null;
    }

    @Override
    public TelegraphInstance createTelegraph(EntityLivingBase caster, EntityLivingBase target) {
        if (!showTelegraph || telegraphType == TelegraphType.NONE || target == null) {
            return null;
        }

        // Create small circle telegraph at target position
        Telegraph telegraph = Telegraph.circle(orbSize * 1.5f);
        telegraph.setDurationTicks(windUpTicks);
        telegraph.setColor(windUpColor);
        telegraph.setWarningColor(activeColor);
        telegraph.setWarningStartTick(Math.max(5, windUpTicks / 4));
        telegraph.setHeightOffset(telegraphHeightOffset);

        // Position at target and follow target during windup
        TelegraphInstance instance = new TelegraphInstance(telegraph, target.posX, target.posY, target.posZ, caster.rotationYaw);
        instance.setCasterEntityId(caster.getEntityId());
        instance.setEntityIdToFollow(target.getEntityId());

        return instance;
    }

    @Override
    public float getTelegraphRadius() {
        return orbSize * 1.5f;
    }

    @Override
    public void writeTypeNBT(NBTTagCompound nbt) {
        nbt.setFloat("orbSize", orbSize);
        nbt.setBoolean("dualFire", dualFire);
        nbt.setInteger("dualFireDelay", dualFireDelay);
        combatData.writeNBT(nbt);
        homingData.writeNBT(nbt);
        lifespanData.writeNBT(nbt);
        // Backward compat: old key was "orbSpeed"
        nbt.setFloat("orbSpeed", homingData.speed);

        NBTTagCompound orb1 = new NBTTagCompound();
        NBTTagCompound orb2 = new NBTTagCompound();

        anchorData[0].writeNBT(orb1);
        anchorData[1].writeNBT(orb2);
        colorData[0].writeNBT(orb1);
        colorData[1].writeNBT(orb2);
        lightningData[0].writeNBT(orb1);
        lightningData[1].writeNBT(orb2);

        nbt.setTag("Orb_1", orb1);
        nbt.setTag("Orb_2", orb2);
    }

    @Override
    public void readTypeNBT(NBTTagCompound nbt) {
        NBTTagCompound orb1 = nbt.hasKey("Orb_1") ? nbt.getCompoundTag("Orb_1") : new NBTTagCompound();
        NBTTagCompound orb2 = nbt.hasKey("Orb_2") ? nbt.getCompoundTag("Orb_2") : new NBTTagCompound();

        this.orbSize = nbt.hasKey("orbSize") ? nbt.getFloat("orbSize") : 1.0f;
        this.dualFire = !nbt.hasKey("dualFire") || nbt.getBoolean("dualFire");
        this.dualFireDelay = nbt.hasKey("dualFireDelay") ? nbt.getInteger("dualFireDelay") : 0;
        combatData.readNBT(nbt);
        homingData.readNBT(nbt);
        lifespanData.readNBT(nbt);
        // Backward compat: old key was "orbSpeed"
        if (nbt.hasKey("orbSpeed")) {
            homingData.speed = nbt.getFloat("orbSpeed");
        }

        anchorData[0].readNBT(orb1);
        anchorData[1].readNBT(orb2);
        colorData[0].readNBT(orb1);
        colorData[1].readNBT(orb2);
        lightningData[0].readNBT(orb1);
        lightningData[1].readNBT(orb2);
    }

    // Getters & Setters
    private EnergyColorData getColorData(int orb) {
        orb = ValueUtil.clamp(orb, 0, 1);
        return colorData[orb];
    }
    private EnergyLightningData getLightningData(int orb) {
        orb = ValueUtil.clamp(orb, 0, 1);
        return lightningData[orb];
    }
    private EnergyAnchorData getAnchorData(int orb) {
        orb = ValueUtil.clamp(orb, 0, 1);
        return anchorData[orb];
    }

    public float getOrbSpeed() {
        return homingData.speed;
    }

    public void setOrbSpeed(float orbSpeed) {
        homingData.speed = orbSpeed;
    }

    public float getOrbSize() {
        return orbSize;
    }

    public void setOrbSize(float orbSize) {
        this.orbSize = orbSize;
    }

    public boolean isDualFire() {
        return dualFire;
    }

    public void setDualFire(boolean dualFire) {
        this.dualFire = dualFire;
    }

    public int getDualFireDelay() {
        return dualFireDelay;
    }

    public void setDualFireDelay(int dualFireDelay) {
        this.dualFireDelay = dualFireDelay;
    }

    public float getMaxDistance() {
        return lifespanData.maxDistance;
    }

    public void setMaxDistance(float maxDistance) {
        lifespanData.maxDistance = maxDistance;
    }

    public int getMaxLifetime() {
        return lifespanData.maxLifetime;
    }

    public void setMaxLifetime(int maxLifetime) {
        lifespanData.maxLifetime = maxLifetime;
    }

    public float getDamage() {
        return combatData.damage;
    }

    public void setDamage(float damage) {
        combatData.damage = damage;
    }

    public float getKnockback() {
        return combatData.knockback;
    }

    public void setKnockback(float knockback) {
        combatData.knockback = knockback;
    }

    public float getKnockbackUp() {
        return combatData.knockbackUp;
    }

    public void setKnockbackUp(float knockbackUp) {
        combatData.knockbackUp = knockbackUp;
    }

    public boolean isHoming() {
        return homingData.homing;
    }

    public void setHoming(boolean homing) {
        homingData.homing = homing;
    }

    public float getHomingStrength() {
        return homingData.homingStrength;
    }

    public void setHomingStrength(float homingStrength) {
        homingData.homingStrength = homingStrength;
    }

    public float getHomingRange() {
        return homingData.homingRange;
    }

    public void setHomingRange(float homingRange) {
        homingData.homingRange = homingRange;
    }

    public boolean isExplosive() {
        return combatData.explosive;
    }

    public void setExplosive(boolean explosive) {
        combatData.explosive = explosive;
    }

    public float getExplosionRadius() {
        return combatData.explosionRadius;
    }

    public void setExplosionRadius(float explosionRadius) {
        combatData.explosionRadius = explosionRadius;
    }

    public float getExplosionDamageFalloff() {
        return combatData.explosionDamageFalloff;
    }

    public void setExplosionDamageFalloff(float explosionDamageFalloff) {
        combatData.explosionDamageFalloff = explosionDamageFalloff;
    }


    public int getInnerColor(int orb) { return getColorData(orb).innerColor; }
    public void setInnerColor(int orb, int innerColor) { getColorData(orb).innerColor = innerColor; }
    public int getOuterColor(int orb) { return getColorData(orb).outerColor; }
    public void setOuterColor(int orb, int outerColor) { getColorData(orb).outerColor = outerColor; }
    public boolean isOuterColorEnabled(int orb) { return getColorData(orb).outerColorEnabled; }
    public void setOuterColorEnabled(int orb, boolean enabled) { getColorData(orb).outerColorEnabled = enabled; }
    public float getOuterColorWidth(int orb) { return getColorData(orb).outerColorWidth; }
    public void setOuterColorWidth(int orb, float width) { getColorData(orb).outerColorWidth = width; }
    public float getOuterColorAlpha(int orb) { return getColorData(orb).outerColorAlpha; }
    public void setOuterColorAlpha(int orb, float alpha) { getColorData(orb).outerColorAlpha = alpha; }
    public float getRotationSpeed(int orb) { return getColorData(orb).rotationSpeed; }
    public void setRotationSpeed(int orb, float speed) { getColorData(orb).rotationSpeed = speed; }

    public int getInnerColor() { return getInnerColor(0); }
    public void setInnerColor(int innerColor) { setInnerColor(0, innerColor); }
    public int getOuterColor() { return getOuterColor(0); }
    public void setOuterColor(int outerColor) { setOuterColor(0, outerColor); }
    public boolean isOuterColorEnabled() { return isOuterColorEnabled(0); }
    public void setOuterColorEnabled(boolean enabled) { setOuterColorEnabled(0, enabled); }
    public float getOuterColorWidth() { return getOuterColorWidth(0); }
    public void setOuterColorWidth(float width) { setOuterColorWidth(0, width); }
    public float getOuterColorAlpha() { return getOuterColorAlpha(0); }
    public void setOuterColorAlpha(float alpha) { setOuterColorAlpha(0, alpha); }
    public float getRotationSpeed() { return getRotationSpeed(0); }
    public void setRotationSpeed(float speed) { setRotationSpeed(0, speed); }


    public boolean hasLightningEffect(int orb) { return getLightningData(orb).lightningEffect; }
    public void setLightningEffect(int orb, boolean lightningEffect) { getLightningData(orb).lightningEffect = lightningEffect; }
    public float getLightningDensity(int orb) { return getLightningData(orb).lightningDensity; }
    public void setLightningDensity(int orb, float lightningDensity) { getLightningData(orb).lightningDensity = lightningDensity; }
    public float getLightningRadius(int orb) { return getLightningData(orb).lightningRadius; }
    public void setLightningRadius(int orb, float lightningRadius) { getLightningData(orb).lightningRadius = lightningRadius; }

    public boolean hasLightningEffect() { return hasLightningEffect(0); }
    public void setLightningEffect(boolean lightningEffect) { setLightningEffect(0, lightningEffect); }
    public float getLightningDensity() { return getLightningDensity(0); }
    public void setLightningDensity(float lightningDensity) { setLightningDensity(0, lightningDensity); }
    public float getLightningRadius() { return getLightningRadius(0); }
    public void setLightningRadius(float lightningRadius) { setLightningRadius(0, lightningRadius); }


    public AnchorPoint getAnchorPointEnum(int orb) { return getAnchorData(orb).anchorPoint; }
    public void setAnchorPointEnum(int orb, AnchorPoint anchorPoint) { getAnchorData(orb).anchorPoint = anchorPoint; }
    public float getAnchorOffsetX(int orb) { return getAnchorData(orb).anchorOffsetX; }
    public void setAnchorOffsetX(int orb, float x) { getAnchorData(orb).anchorOffsetX = x; }
    public float getAnchorOffsetY(int orb) { return getAnchorData(orb).anchorOffsetY; }
    public void setAnchorOffsetY(int orb, float y) { getAnchorData(orb).anchorOffsetY = y; }
    public float getAnchorOffsetZ(int orb) { return getAnchorData(orb).anchorOffsetZ; }
    public void setAnchorOffsetZ(int orb, float z) { getAnchorData(orb).anchorOffsetZ = z; }

    public AnchorPoint getAnchorPointEnum() { return getAnchorPointEnum(0); }
    public void setAnchorPointEnum(AnchorPoint anchorPoint) { setAnchorPointEnum(0, anchorPoint); }
    public float getAnchorOffsetX() { return getAnchorOffsetX(0); }
    public void setAnchorOffsetX(float x) { setAnchorOffsetX(0, x); }
    public float getAnchorOffsetY() { return getAnchorOffsetY(0); }
    public void setAnchorOffsetY(float y) { setAnchorOffsetY(0, y); }
    public float getAnchorOffsetZ() { return getAnchorOffsetZ(0); }
    public void setAnchorOffsetZ(float z) { setAnchorOffsetZ(0, z); }

    //@Override
    public int getAnchorPoint(int orb) { return getAnchorData(orb).anchorPoint.ordinal(); }
    public int getAnchorPoint() { return getAnchorData(0).anchorPoint.ordinal(); }

    //@Override
    public void setAnchorPoint(int orb, int point) { getAnchorData(orb).anchorPoint = AnchorPoint.fromOrdinal(point); }
    public void setAnchorPoint(int point) { getAnchorData(0).anchorPoint = AnchorPoint.fromOrdinal(point); }

    @SideOnly(Side.CLIENT)
    @Override
    public List<FieldDef> getFieldDefinitions() {
        return Arrays.asList(
            // Type tab
            FieldDef.floatField("enchantment.damage", this::getDamage, this::setDamage).column(ColumnHint.LEFT),
            FieldDef.floatField("stats.speed", this::getOrbSpeed, this::setOrbSpeed).column(ColumnHint.RIGHT),
            FieldDef.floatField("stats.size", this::getOrbSize, this::setOrbSize).column(ColumnHint.LEFT),
            FieldDef.floatField("ability.knockback", this::getKnockback, this::setKnockback).column(ColumnHint.RIGHT),
            FieldDef.floatField("ability.maxDistance", this::getMaxDistance, this::setMaxDistance).column(ColumnHint.LEFT),
            FieldDef.intField("ability.lifetime", this::getMaxLifetime, this::setMaxLifetime).column(ColumnHint.RIGHT),
            FieldDef.section("ability.section.dual"),
            FieldDef.boolField("ability.dualFire", this::isDualFire, this::setDualFire),
            FieldDef.intField("ability.dualFireDelay", this::getDualFireDelay, this::setDualFireDelay)
                .visibleWhen(this::isDualFire),
            FieldDef.section("ability.section.homing"),
            FieldDef.boolField("gui.enabled", this::isHoming, this::setHoming).hover("ability.hover.homing"),
            FieldDef.floatField("gui.strength", this::getHomingStrength, this::setHomingStrength).visibleWhen(this::isHoming),
            FieldDef.section("ability.section.explosive"),
            FieldDef.boolField("gui.enabled", this::isExplosive, this::setExplosive).hover("ability.hover.explosive"),
            FieldDef.floatField("gui.radius", this::getExplosionRadius, this::setExplosionRadius).visibleWhen(this::isExplosive),
            AbilityFieldDefs.effectsListField("ability.effects", this::getEffects, this::setEffects),
            // Visual tab - Orb 1
            FieldDef.enumField("ability.anchorPoint", AnchorPoint.class,
                () -> getAnchorPointEnum(0), v -> setAnchorPointEnum(0, v))
                .tab("ability.tab.visual"),
            FieldDef.section("ability.section.orb1").tab("ability.tab.visual"),
            FieldDef.colorSubGui("ability.innerColor",
                () -> getInnerColor(0), v -> setInnerColor(0, v))
                .tab("ability.tab.visual"),
            FieldDef.boolField("ability.outerEnabled",
                () -> isOuterColorEnabled(0), v -> setOuterColorEnabled(0, v))
                .tab("ability.tab.visual"),
            FieldDef.colorSubGui("ability.outerColor",
                () -> getOuterColor(0), v -> setOuterColor(0, v))
                .tab("ability.tab.visual").visibleWhen(() -> isOuterColorEnabled(0)),
            FieldDef.floatField("ability.outerWidth",
                () -> getOuterColorWidth(0), v -> setOuterColorWidth(0, v))
                .tab("ability.tab.visual").visibleWhen(() -> isOuterColorEnabled(0)).column(ColumnHint.LEFT),
            FieldDef.floatField("ability.outerAlpha",
                () -> getOuterColorAlpha(0), v -> setOuterColorAlpha(0, v))
                .tab("ability.tab.visual").range(0, 1).visibleWhen(() -> isOuterColorEnabled(0)).column(ColumnHint.RIGHT),
            FieldDef.floatField("ability.rotationSpeed",
                () -> getRotationSpeed(0), v -> setRotationSpeed(0, v))
                .tab("ability.tab.visual"),
            FieldDef.boolField("ability.lightning",
                () -> hasLightningEffect(0), v -> setLightningEffect(0, v))
                .tab("ability.tab.visual"),
            FieldDef.floatField("gui.density",
                () -> getLightningDensity(0), v -> setLightningDensity(0, v))
                .tab("ability.tab.visual").range(0.01f, 5.0f).visibleWhen(() -> hasLightningEffect(0)).column(ColumnHint.LEFT),
            FieldDef.floatField("gui.radius",
                () -> getLightningRadius(0), v -> setLightningRadius(0, v))
                .tab("ability.tab.visual").range(0.1f, 10.0f).visibleWhen(() -> hasLightningEffect(0)).column(ColumnHint.RIGHT),
            // Visual tab - Orb 2
            FieldDef.enumField("ability.anchorPoint", AnchorPoint.class,
                () -> getAnchorPointEnum(1), v -> setAnchorPointEnum(1, v))
                .tab("ability.tab.visual"),
            FieldDef.section("ability.section.orb2").tab("ability.tab.visual"),
            FieldDef.colorSubGui("ability.innerColor",
                () -> getInnerColor(1), v -> setInnerColor(1, v))
                .tab("ability.tab.visual"),
            FieldDef.boolField("ability.outerEnabled",
                () -> isOuterColorEnabled(1), v -> setOuterColorEnabled(1, v))
                .tab("ability.tab.visual"),
            FieldDef.colorSubGui("ability.outerColor",
                () -> getOuterColor(1), v -> setOuterColor(1, v))
                .tab("ability.tab.visual").visibleWhen(() -> isOuterColorEnabled(1)),
            FieldDef.floatField("ability.outerWidth",
                () -> getOuterColorWidth(1), v -> setOuterColorWidth(1, v))
                .tab("ability.tab.visual").visibleWhen(() -> isOuterColorEnabled(1)).column(ColumnHint.LEFT),
            FieldDef.floatField("ability.outerAlpha",
                () -> getOuterColorAlpha(1), v -> setOuterColorAlpha(1, v))
                .tab("ability.tab.visual").range(0, 1).visibleWhen(() -> isOuterColorEnabled(1)).column(ColumnHint.RIGHT),
            FieldDef.floatField("ability.rotationSpeed",
                () -> getRotationSpeed(1), v -> setRotationSpeed(1, v))
                .tab("ability.tab.visual"),
            FieldDef.boolField("ability.lightning",
                () -> hasLightningEffect(1), v -> setLightningEffect(1, v))
                .tab("ability.tab.visual"),
            FieldDef.floatField("gui.density",
                () -> getLightningDensity(1), v -> setLightningDensity(1, v))
                .tab("ability.tab.visual").range(0.01f, 5.0f).visibleWhen(() -> hasLightningEffect(1)).column(ColumnHint.LEFT),
            FieldDef.floatField("gui.radius",
                () -> getLightningRadius(1), v -> setLightningRadius(1, v))
                .tab("ability.tab.visual").range(0.1f, 10.0f).visibleWhen(() -> hasLightningEffect(1)).column(ColumnHint.RIGHT)
        );
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Entity createPreviewEntity(EntityNPCInterface npc) {
        if (npc == null || npc.worldObj == null) return null;

        EntityAbilityOrb orb = new EntityAbilityOrb(npc.worldObj);
        orb.setupPreview(npc, orbSize, colorData[0], lightningData[0], anchorData[0], windUpTicks);
        return orb;
    }

    @Override
    public int getPreviewActiveDuration() {
        return lifespanData.maxLifetime > 0 ? Math.min(lifespanData.maxLifetime, 100) : 100;
    }
}
