package kamkeel.npcs.controllers.data.ability.type;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.AnchorPoint;
import kamkeel.npcs.controllers.data.ability.LockMovementType;
import kamkeel.npcs.controllers.data.ability.TargetingMode;
import kamkeel.npcs.controllers.data.ability.data.*;
import noppes.npcs.client.gui.builder.FieldDef;
import kamkeel.npcs.controllers.data.ability.gui.AbilityFieldDefs;
import kamkeel.npcs.controllers.data.telegraph.Telegraph;
import kamkeel.npcs.controllers.data.telegraph.TelegraphInstance;
import kamkeel.npcs.controllers.data.telegraph.TelegraphType;
import kamkeel.npcs.entity.EntityAbilityDisc;
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

public class AbilityDiscDual extends Ability {

    // Disc geometry
    private float discRadius = 1.0f;
    private float discThickness = 0.2f;

    // Boomerang properties
    private boolean boomerang = false;
    private int boomerangDelay = 40;

    // Dual properties
    private boolean dualFire = true;
    private int dualFireDelay = 10;

    // Energy data classes
    private EnergyColorData[] colorData = new EnergyColorData[]{
        new EnergyColorData(0xFFFFFF, 0xFF8800, true, 0.4f, 0.5f, 5.0f),
        new EnergyColorData(0xFFFFFF, 0x8800FF, true, 0.4f, 0.5f, 5.0f)
    };
    private EnergyCombatData combatData = new EnergyCombatData(8.0f, 1.2f, 0.15f, false, 3.0f, 0.5f);
    private EnergyHomingData homingData = new EnergyHomingData(0.6f, true, 0.12f, 18.0f);
    public final EnergyLightningData[] lightningData = new EnergyLightningData[]{
        new EnergyLightningData(), new EnergyLightningData()
    };
    private EnergyLifespanData lifespanData = new EnergyLifespanData(35.0f, 200);
    private EnergyAnchorData[] anchorData = new EnergyAnchorData[]{
        new EnergyAnchorData(AnchorPoint.LEFT_HAND), new EnergyAnchorData(AnchorPoint.RIGHT_HAND)
    };

    // Transient state for disc entity (used during windup charging)
    private transient EntityAbilityDisc discEntity = null;
    private transient EntityAbilityDisc discEntity2 = null;

    public AbilityDiscDual() {
        this.typeId = "ability.cnpc.disc_dual";
        this.name = "Disc Dual";
        this.targetingMode = TargetingMode.AGGRO_TARGET;
        this.maxRange = 30.0f;
        this.minRange = 5.0f;
        this.cooldownTicks = 0;
        this.windUpTicks = 60;
        this.lockMovement = LockMovementType.WINDUP;
        this.telegraphType = TelegraphType.CIRCLE;
        this.showTelegraph = true;
        // Default built-in animations
        this.windUpAnimationName = "Ability_DiscDual_Windup";
        this.activeAnimationName = "Ability_DiscDual_Active";
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
        if (discEntity != null && !discEntity.isDead) {
            discEntity.startMoving(target);
        }

        if (dualFire) {
            if (discEntity2 != null && !discEntity2.isDead && dualFireDelay <= 0) {
                discEntity2.startMoving(target);
            }
        } else {
            discEntity2.setDead();
        }

        // Ability stays active until entity dies (prevents firing another while projectile is alive)
        // Movement locking is handled separately by the base class
    }

    @Override
    public void onWindUpTick(EntityLivingBase caster, EntityLivingBase target, World world, int tick) {
        if (world.isRemote) return;

        // Spawn disc in charging mode on first tick of windup
        if (tick == 1) {
            // Create disc in charging mode - follows caster based on anchor point during windup
            Vec3 spawnPos1 = AnchorPointHelper.calculateAnchorPosition(caster, anchorData[0]);
            discEntity = new EntityAbilityDisc(
                world, caster, target,
                spawnPos1.xCoord, spawnPos1.yCoord, spawnPos1.zCoord,
                discRadius, discThickness,
                colorData[0], combatData, homingData, lightningData[0], lifespanData,
                boomerang, boomerangDelay);

            Vec3 spawnPos2 = AnchorPointHelper.calculateAnchorPosition(caster, anchorData[1]);
            discEntity2 = new EntityAbilityDisc(
                world, caster, target,
                spawnPos2.xCoord, spawnPos2.yCoord, spawnPos2.zCoord,
                discRadius, discThickness,
                colorData[1], combatData, homingData, lightningData[1], lifespanData,
                boomerang, boomerangDelay);

            discEntity.setSiblingUUID(discEntity2.getPersistentID());
            discEntity2.setSiblingUUID(discEntity.getPersistentID());

            discEntity.setupCharging(anchorData[0], windUpTicks);
            discEntity2.setupCharging(anchorData[1], windUpTicks);

            discEntity.setEffects(this.effects);

            if (dualFire) {
                discEntity2.setEffects(this.effects);
            }

            world.spawnEntityInWorld(discEntity);
            world.spawnEntityInWorld(discEntity2);
        }
    }

    @Override
    public void onActiveTick(EntityLivingBase caster, EntityLivingBase target, World world, int tick) {
        // Signal completion when entity dies
        if (dualFire) {
            if ((discEntity == null || discEntity.isDead) && (discEntity2 == null || discEntity2.isDead)) {
                discEntity = null;
                discEntity2 = null;
                signalCompletion();
            }

            if (tick == dualFireDelay && discEntity2 != null && !discEntity2.isDead) {
                discEntity2.startMoving(target);
            }
        } else {
            if (discEntity == null || discEntity.isDead) {
                discEntity = null;
                signalCompletion();
            }
        }
    }

    @Override
    public void onComplete(EntityLivingBase caster, EntityLivingBase target) {
    }

    @Override
    public void cleanup() {
        // Despawn disc entity if still alive
        if (discEntity != null && !discEntity.isDead) {
            discEntity.setDead();
        }

        if (discEntity2 != null && !discEntity2.isDead) {
            discEntity2.setDead();
        }

        discEntity = null;
        discEntity2 = null;
    }

    @Override
    public TelegraphInstance createTelegraph(EntityLivingBase caster, EntityLivingBase target) {
        if (!showTelegraph || telegraphType == TelegraphType.NONE || target == null) {
            return null;
        }

        // Create circle telegraph at target position
        Telegraph telegraph = Telegraph.circle(discRadius * 1.5f);
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
        return discRadius * 1.5f;
    }

    @Override
    public void writeTypeNBT(NBTTagCompound nbt) {
        nbt.setFloat("discRadius", discRadius);
        nbt.setFloat("discThickness", discThickness);
        nbt.setBoolean("boomerang", boomerang);
        nbt.setInteger("boomerangDelay", boomerangDelay);
        nbt.setBoolean("dualFire", dualFire);
        nbt.setInteger("dualFireDelay", dualFireDelay);
        combatData.writeNBT(nbt);
        homingData.writeNBT(nbt);
        lifespanData.writeNBT(nbt);

        NBTTagCompound disc1 = new NBTTagCompound();
        NBTTagCompound disc2 = new NBTTagCompound();

        anchorData[0].writeNBT(disc1);
        anchorData[1].writeNBT(disc2);
        colorData[0].writeNBT(disc1);
        colorData[1].writeNBT(disc2);
        lightningData[0].writeNBT(disc1);
        lightningData[1].writeNBT(disc2);

        nbt.setTag("Disc_1", disc1);
        nbt.setTag("Disc_2", disc2);
    }

    @Override
    public void readTypeNBT(NBTTagCompound nbt) {
        NBTTagCompound disc1 = nbt.hasKey("Disc_1") ? nbt.getCompoundTag("Disc_1") : new NBTTagCompound();
        NBTTagCompound disc2 = nbt.hasKey("Disc_2") ? nbt.getCompoundTag("Disc_2") : new NBTTagCompound();

        this.discRadius = nbt.hasKey("discRadius") ? nbt.getFloat("discRadius") : 1.0f;
        this.discThickness = nbt.hasKey("discThickness") ? nbt.getFloat("discThickness") : 0.2f;
        this.boomerang = nbt.hasKey("boomerang") && nbt.getBoolean("boomerang");
        this.boomerangDelay = nbt.hasKey("boomerangDelay") ? nbt.getInteger("boomerangDelay") : 40;
        this.dualFire = !nbt.hasKey("dualFire") || nbt.getBoolean("dualFire");
        this.dualFireDelay = nbt.hasKey("dualFireDelay") ? nbt.getInteger("dualFireDelay") : 0;
        combatData.readNBT(nbt);
        homingData.readNBT(nbt);
        lifespanData.readNBT(nbt);

        anchorData[0].readNBT(disc1);
        anchorData[1].readNBT(disc2);
        colorData[0].readNBT(disc1);
        colorData[1].readNBT(disc2);
        lightningData[0].readNBT(disc1);
        lightningData[1].readNBT(disc2);
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

    public boolean isDualFire() { return dualFire; }
    public void setDualFire(boolean dualFire) { this.dualFire = dualFire; }
    public int getDualFireDelay() { return dualFireDelay; }
    public void setDualFireDelay(int dualFireDelay) { this.dualFireDelay = dualFireDelay; }
    public float getSpeed() { return homingData.speed; }
    public void setSpeed(float speed) { homingData.speed = speed; }
    public float getDiscRadius() { return discRadius; }
    public void setDiscRadius(float discRadius) { this.discRadius = discRadius; }
    public float getDiscThickness() { return discThickness; }
    public void setDiscThickness(float discThickness) { this.discThickness = discThickness; }
    public float getMaxDistance() { return lifespanData.maxDistance; }
    public void setMaxDistance(float maxDistance) { lifespanData.maxDistance = maxDistance; }
    public int getMaxLifetime() { return lifespanData.maxLifetime; }
    public void setMaxLifetime(int maxLifetime) { lifespanData.maxLifetime = maxLifetime; }
    public float getDamage() { return combatData.damage; }
    public void setDamage(float damage) { combatData.damage = damage; }
    public float getKnockback() { return combatData.knockback; }
    public void setKnockback(float knockback) { combatData.knockback = knockback; }
    public float getKnockbackUp() { return combatData.knockbackUp; }
    public void setKnockbackUp(float knockbackUp) { combatData.knockbackUp = knockbackUp; }
    public boolean isHoming() { return homingData.homing; }
    public void setHoming(boolean homing) { homingData.homing = homing; }
    public float getHomingStrength() { return homingData.homingStrength; }
    public void setHomingStrength(float homingStrength) { homingData.homingStrength = homingStrength; }
    public float getHomingRange() { return homingData.homingRange; }
    public void setHomingRange(float homingRange) { homingData.homingRange = homingRange; }
    public boolean isBoomerang() { return boomerang; }
    public void setBoomerang(boolean boomerang) { this.boomerang = boomerang; }
    public int getBoomerangDelay() { return boomerangDelay; }
    public void setBoomerangDelay(int boomerangDelay) { this.boomerangDelay = boomerangDelay; }
    public boolean isExplosive() { return combatData.explosive; }
    public void setExplosive(boolean explosive) { combatData.explosive = explosive; }
    public float getExplosionRadius() { return combatData.explosionRadius; }
    public void setExplosionRadius(float explosionRadius) { combatData.explosionRadius = explosionRadius; }
    public float getExplosionDamageFalloff() { return combatData.explosionDamageFalloff; }
    public void setExplosionDamageFalloff(float explosionDamageFalloff) { combatData.explosionDamageFalloff = explosionDamageFalloff; }


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
    public void getAbilityDefinitions(List<FieldDef> defs) {
        defs.addAll(Arrays.asList(
            // Type tab
            FieldDef.row(
                FieldDef.floatField("enchantment.damage", this::getDamage, this::setDamage),
                FieldDef.floatField("stats.speed", this::getSpeed, this::setSpeed)
            ),
            FieldDef.floatField("ability.knockback", this::getKnockback, this::setKnockback),
            FieldDef.section("ability.section.disc"),
            FieldDef.row(
                FieldDef.floatField("gui.radius", this::getDiscRadius, this::setDiscRadius),
                FieldDef.floatField("gui.thickness", this::getDiscThickness, this::setDiscThickness)
            ),
            FieldDef.row(
                FieldDef.floatField("ability.maxDistance", this::getMaxDistance, this::setMaxDistance),
                FieldDef.intField("ability.lifetime", this::getMaxLifetime, this::setMaxLifetime)
            ),
            FieldDef.section("ability.section.dual"),
            FieldDef.boolField("ability.dualFire", this::isDualFire, this::setDualFire),
            FieldDef.intField("ability.dualFireDelay", this::getDualFireDelay, this::setDualFireDelay)
                .visibleWhen(this::isDualFire),
            FieldDef.section("ability.section.homing"),
            FieldDef.boolField("gui.enabled", this::isHoming, this::setHoming)
                .hover("ability.hover.homing"),
            FieldDef.floatField("gui.strength", this::getHomingStrength, this::setHomingStrength)
                .visibleWhen(this::isHoming),
            FieldDef.section("ability.section.boomerang"),
            FieldDef.boolField("gui.enabled", this::isBoomerang, this::setBoomerang)
                .hover("ability.hover.boomerang"),
            FieldDef.intField("gui.delay", this::getBoomerangDelay, this::setBoomerangDelay)
                .visibleWhen(this::isBoomerang),
            FieldDef.section("ability.section.explosive"),
            FieldDef.boolField("gui.enabled", this::isExplosive, this::setExplosive)
                .hover("ability.hover.explosive"),
            FieldDef.floatField("gui.radius", this::getExplosionRadius, this::setExplosionRadius)
                .visibleWhen(this::isExplosive),
            AbilityFieldDefs.effectsListField("ability.effects", this::getEffects, this::setEffects),
            // Visual tab - Disc 1
            FieldDef.enumField("ability.anchorPoint", AnchorPoint.class,
                () -> getAnchorPointEnum(0), v -> setAnchorPointEnum(0, v))
                .tab("ability.tab.visual"),
            FieldDef.section("ability.section.disc1").tab("ability.tab.visual"),
            FieldDef.colorSubGui("ability.innerColor",
                () -> getInnerColor(0), v -> setInnerColor(0, v))
                .tab("ability.tab.visual"),
            FieldDef.boolField("ability.outerEnabled",
                () -> isOuterColorEnabled(0), v -> setOuterColorEnabled(0, v))
                .tab("ability.tab.visual"),
            FieldDef.colorSubGui("ability.outerColor",
                () -> getOuterColor(0), v -> setOuterColor(0, v))
                .tab("ability.tab.visual").visibleWhen(() -> isOuterColorEnabled(0)),
            FieldDef.row(
                FieldDef.floatField("ability.outerWidth",
                    () -> getOuterColorWidth(0), v -> setOuterColorWidth(0, v))
                    .visibleWhen(() -> isOuterColorEnabled(0)),
                FieldDef.floatField("ability.outerAlpha",
                    () -> getOuterColorAlpha(0), v -> setOuterColorAlpha(0, v))
                    .range(0, 1).visibleWhen(() -> isOuterColorEnabled(0))
            ).tab("ability.tab.visual"),
            FieldDef.floatField("ability.rotationSpeed",
                () -> getRotationSpeed(0), v -> setRotationSpeed(0, v))
                .tab("ability.tab.visual"),
            FieldDef.boolField("ability.lightning",
                () -> hasLightningEffect(0), v -> setLightningEffect(0, v))
                .tab("ability.tab.visual"),
            FieldDef.row(
                FieldDef.floatField("gui.density",
                    () -> getLightningDensity(0), v -> setLightningDensity(0, v))
                    .range(0.01f, 5.0f).visibleWhen(() -> hasLightningEffect(0)),
                FieldDef.floatField("gui.radius",
                    () -> getLightningRadius(0), v -> setLightningRadius(0, v))
                    .range(0.1f, 10.0f).visibleWhen(() -> hasLightningEffect(0))
            ).tab("ability.tab.visual"),
            // Visual tab - Disc 2
            FieldDef.enumField("ability.anchorPoint", AnchorPoint.class,
                () -> getAnchorPointEnum(1), v -> setAnchorPointEnum(1, v))
                .tab("ability.tab.visual"),
            FieldDef.section("ability.section.disc2").tab("ability.tab.visual"),
            FieldDef.colorSubGui("ability.innerColor",
                () -> getInnerColor(1), v -> setInnerColor(1, v))
                .tab("ability.tab.visual"),
            FieldDef.boolField("ability.outerEnabled",
                () -> isOuterColorEnabled(1), v -> setOuterColorEnabled(1, v))
                .tab("ability.tab.visual"),
            FieldDef.colorSubGui("ability.outerColor",
                () -> getOuterColor(1), v -> setOuterColor(1, v))
                .tab("ability.tab.visual").visibleWhen(() -> isOuterColorEnabled(1)),
            FieldDef.row(
                FieldDef.floatField("ability.outerWidth",
                    () -> getOuterColorWidth(1), v -> setOuterColorWidth(1, v))
                    .visibleWhen(() -> isOuterColorEnabled(1)),
                FieldDef.floatField("ability.outerAlpha",
                    () -> getOuterColorAlpha(1), v -> setOuterColorAlpha(1, v))
                    .range(0, 1).visibleWhen(() -> isOuterColorEnabled(1))
            ).tab("ability.tab.visual"),
            FieldDef.floatField("ability.rotationSpeed",
                () -> getRotationSpeed(1), v -> setRotationSpeed(1, v))
                .tab("ability.tab.visual"),
            FieldDef.boolField("ability.lightning",
                () -> hasLightningEffect(1), v -> setLightningEffect(1, v))
                .tab("ability.tab.visual"),
            FieldDef.row(
                FieldDef.floatField("gui.density",
                    () -> getLightningDensity(1), v -> setLightningDensity(1, v))
                    .range(0.01f, 5.0f).visibleWhen(() -> hasLightningEffect(1)),
                FieldDef.floatField("gui.radius",
                    () -> getLightningRadius(1), v -> setLightningRadius(1, v))
                    .range(0.1f, 10.0f).visibleWhen(() -> hasLightningEffect(1))
            ).tab("ability.tab.visual")
        ));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Entity createPreviewEntity(EntityNPCInterface npc) {
        if (npc == null || npc.worldObj == null) return null;

        EntityAbilityDisc disc = new EntityAbilityDisc(npc.worldObj);
        disc.setupPreview(npc, discRadius, discThickness, colorData[0], lightningData[0], anchorData[0], windUpTicks);
        return disc;
    }

    @Override
    public int getPreviewActiveDuration() {
        return lifespanData.maxLifetime > 0 ? Math.min(lifespanData.maxLifetime, 100) : 100;
    }
}
