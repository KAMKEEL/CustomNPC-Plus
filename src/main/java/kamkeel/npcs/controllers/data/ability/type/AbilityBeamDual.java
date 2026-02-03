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
import kamkeel.npcs.entity.EntityAbilityBeam;
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

public class AbilityBeamDual extends Ability {
    private float beamWidth = 0.4f;
    private float headSize = 0.6f;
    private boolean dualFire= true;
    private int dualFireDelay = 0;

    // Data classes
    private EnergyColorData[] colorData = new EnergyColorData[]{
        new EnergyColorData(0xFFFFFF, 0x00AAFF, true, 0.4f, 0.5f, 6.0f),
        new EnergyColorData(0xFFFFFF, 0xFF0000, true, 0.4f, 0.5f, 6.0f),
    };
    private EnergyCombatData combatData = new EnergyCombatData(10.0f, 1.5f, 0.2f, false, 4.0f, 0.5f);
    private EnergyHomingData homingData = new EnergyHomingData(0.4f, true, 0.1f, 15.0f);
    private EnergyLightningData[] lightningData = new EnergyLightningData[]{
        new EnergyLightningData(), new EnergyLightningData()
    };
    private EnergyLifespanData lifespanData = new EnergyLifespanData(25.0f, 200);
    private EnergyAnchorData[] anchorData = new EnergyAnchorData[]{
        new EnergyAnchorData(AnchorPoint.RIGHT_HAND), new EnergyAnchorData(AnchorPoint.LEFT_HAND)
    };

    // Transient state for beam entity (used during windup charging)
    private transient EntityAbilityBeam beamEntity = null;
    private transient EntityAbilityBeam beamEntity2 = null;

    public AbilityBeamDual() {
        this.typeId = "ability.cnpc.beam_dual";
        this.name = "Beam Dual";
        this.targetingMode = TargetingMode.AGGRO_TARGET;
        this.maxRange = 20.0f;
        this.minRange = 5.0f;
        this.cooldownTicks = 0;
        this.windUpTicks = 80;
        this.lockMovement = LockMovementType.WINDUP_AND_ACTIVE;
        this.telegraphType = TelegraphType.CIRCLE;
        this.showTelegraph = true;
        // Default built-in animations
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

        // Start firing the beam that was spawned during windup
        if (beamEntity != null && !beamEntity.isDead) {
            beamEntity.startFiring(target);
        }

        if (dualFire) {
            if (beamEntity2 != null && !beamEntity2.isDead && dualFireDelay <= 0) {
                beamEntity2.startFiring(target);
            }
        } else {
            beamEntity2.setDead();
        }

        // Ability stays active until entity dies (prevents firing another while projectile is alive)
        // Movement locking is handled separately by the base class
    }

    @Override
    public void onWindUpTick(EntityLivingBase caster, EntityLivingBase target, World world, int tick) {
        if (world.isRemote) return;

        // Spawn beam in charging mode on first tick of windup
        if (tick == 1) {
            float offsetDist = 1.0f;

            // Create beam in charging mode - follows caster based on anchor point during windup
            Vec3 spawnPos1 = AnchorPointHelper.calculateAnchorPosition(caster, anchorData[0], offsetDist);
            beamEntity = new EntityAbilityBeam(
                world, caster, target,
                spawnPos1.xCoord, spawnPos1.yCoord, spawnPos1.zCoord,
                beamWidth, headSize,
                colorData[0], combatData, homingData, lightningData[0], lifespanData,
                lockMovement.locksActive());

            Vec3 spawnPos2 = AnchorPointHelper.calculateAnchorPosition(caster, anchorData[1], offsetDist);
            beamEntity2 = new EntityAbilityBeam(
                world, caster, target,
                spawnPos2.xCoord, spawnPos2.yCoord, spawnPos2.zCoord,
                beamWidth, headSize,
                colorData[1], combatData, homingData, lightningData[1], lifespanData,
                lockMovement.locksActive());

            beamEntity.setSiblingUUID(beamEntity2.getPersistentID());
            beamEntity2.setSiblingUUID(beamEntity.getPersistentID());

            beamEntity.setupCharging(anchorData[0], windUpTicks, offsetDist);
            beamEntity2.setupCharging(anchorData[1], windUpTicks, offsetDist);

            beamEntity.setEffects(this.effects);
            if (dualFire) beamEntity2.setEffects(this.effects);

            world.spawnEntityInWorld(beamEntity);
            world.spawnEntityInWorld(beamEntity2);
        }
    }

    @Override
    public void onActiveTick(EntityLivingBase caster, EntityLivingBase target, World world, int tick) {
        // Signal completion when entity dies
        if (dualFire) {
            if ((beamEntity == null || beamEntity.isDead) && (beamEntity2 == null || beamEntity2.isDead)) {
                beamEntity = null;
                beamEntity2 = null;
                signalCompletion();
            }

            if (tick == dualFireDelay && beamEntity2 != null && !beamEntity2.isDead) {
                beamEntity2.startFiring(target);
            }
        } else {
            if (beamEntity == null || beamEntity.isDead) {
                beamEntity = null;
                signalCompletion();
            }
        }
    }

    @Override
    public void onComplete(EntityLivingBase caster, EntityLivingBase target) {
    }

    @Override
    public void cleanup() {
        // Despawn beam entity if still alive
        if (beamEntity != null && !beamEntity.isDead) {
            beamEntity.setDead();
        }

        if (beamEntity2 != null && !beamEntity2.isDead) {
            beamEntity2.setDead();
        }

        beamEntity = null;
        beamEntity2 = null;
    }

    @Override
    public TelegraphInstance createTelegraph(EntityLivingBase caster, EntityLivingBase target) {
        if (!showTelegraph || telegraphType == TelegraphType.NONE || target == null) {
            return null;
        }

        // Create circle telegraph at target position
        Telegraph telegraph = Telegraph.circle(headSize * 2.0f);
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
        return headSize * 2.0f;
    }

    @Override
    public void writeTypeNBT(NBTTagCompound nbt) {
        nbt.setFloat("beamWidth", beamWidth);
        nbt.setFloat("headSize", headSize);
        nbt.setBoolean("dualFire", dualFire);
        nbt.setInteger("dualFireDelay", dualFireDelay);
        combatData.writeNBT(nbt);
        homingData.writeNBT(nbt);
        lifespanData.writeNBT(nbt);

        NBTTagCompound beam1 = new NBTTagCompound();
        NBTTagCompound beam2 = new NBTTagCompound();

        anchorData[0].writeNBT(beam1);
        anchorData[1].writeNBT(beam2);
        colorData[0].writeNBT(beam1);
        colorData[1].writeNBT(beam2);
        lightningData[0].writeNBT(beam1);
        lightningData[1].writeNBT(beam2);

        nbt.setTag("Beam_1", beam1);
        nbt.setTag("Beam_2", beam2);
    }

    @Override
    public void readTypeNBT(NBTTagCompound nbt) {
        NBTTagCompound beam1 = nbt.hasKey("Beam_1") ? nbt.getCompoundTag("Beam_1") : new NBTTagCompound();
        NBTTagCompound beam2 = nbt.hasKey("Beam_2") ? nbt.getCompoundTag("Beam_2") : new NBTTagCompound();

        this.beamWidth = nbt.hasKey("beamWidth") ? nbt.getFloat("beamWidth") : 0.4f;
        this.headSize = nbt.hasKey("headSize") ? nbt.getFloat("headSize") : 0.6f;
        this.dualFire = !nbt.hasKey("dualFire") || nbt.getBoolean("dualFire");
        this.dualFireDelay = nbt.hasKey("dualFireDelay") ? nbt.getInteger("dualFireDelay") : 0;
        combatData.readNBT(nbt);
        homingData.readNBT(nbt);
        lifespanData.readNBT(nbt);

        anchorData[0].readNBT(beam1);
        anchorData[1].readNBT(beam2);
        colorData[0].readNBT(beam1);
        colorData[1].readNBT(beam2);
        lightningData[0].readNBT(beam1);
        lightningData[1].readNBT(beam2);
    }

    // Getters & Setters - Standalone fields
    public float getBeamWidth() { return beamWidth; }
    public void setBeamWidth(float beamWidth) { this.beamWidth = beamWidth; }
    public float getHeadSize() { return headSize; }
    public void setHeadSize(float headSize) { this.headSize = headSize; }
    public boolean isDualFire() { return dualFire; }
    public void setDualFire(boolean dualFire) { this.dualFire = dualFire; }
    public int getDualFireDelay() { return dualFireDelay; }
    public void setDualFireDelay(int dualFireDelay) { this.dualFireDelay = dualFireDelay; }

    private EnergyColorData getColorData(int beam) {
        beam = ValueUtil.clamp(beam, 0, 1);
        return colorData[beam];
    }
    private EnergyLightningData getLightningData(int beam) {
        beam = ValueUtil.clamp(beam, 0, 1);
        return lightningData[beam];
    }
    private EnergyAnchorData getAnchorData(int beam) {
        beam = ValueUtil.clamp(beam, 0, 1);
        return anchorData[beam];
    }

    // Getters & Setters - Color data
    public int getInnerColor(int beam) { return getColorData(beam).innerColor; }
    public void setInnerColor(int beam, int innerColor) { getColorData(beam).innerColor = innerColor; }
    public int getOuterColor(int beam) { return getColorData(beam).outerColor; }
    public void setOuterColor(int beam, int outerColor) { getColorData(beam).outerColor = outerColor; }
    public boolean isOuterColorEnabled(int beam) { return getColorData(beam).outerColorEnabled; }
    public void setOuterColorEnabled(int beam, boolean enabled) { getColorData(beam).outerColorEnabled = enabled; }
    public float getOuterColorWidth(int beam) { return getColorData(beam).outerColorWidth; }
    public void setOuterColorWidth(int beam, float width) { getColorData(beam).outerColorWidth = width; }
    public float getOuterColorAlpha(int beam) { return getColorData(beam).outerColorAlpha; }
    public void setOuterColorAlpha(int beam, float alpha) { getColorData(beam).outerColorAlpha = alpha; }
    public float getRotationSpeed(int beam) { return getColorData(beam).rotationSpeed; }
    public void setRotationSpeed(int beam, float speed) { getColorData(beam).rotationSpeed = speed; }


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

    // Getters & Setters - Combat data
    public float getDamage() { return combatData.damage; }
    public void setDamage(float damage) { this.combatData.damage = damage; }
    public float getKnockback() { return combatData.knockback; }
    public void setKnockback(float knockback) { this.combatData.knockback = knockback; }
    public float getKnockbackUp() { return combatData.knockbackUp; }
    public void setKnockbackUp(float knockbackUp) { this.combatData.knockbackUp = knockbackUp; }
    public boolean isExplosive() { return combatData.explosive; }
    public void setExplosive(boolean explosive) { this.combatData.explosive = explosive; }
    public float getExplosionRadius() { return combatData.explosionRadius; }
    public void setExplosionRadius(float explosionRadius) { this.combatData.explosionRadius = explosionRadius; }
    public float getExplosionDamageFalloff() { return combatData.explosionDamageFalloff; }
    public void setExplosionDamageFalloff(float explosionDamageFalloff) { this.combatData.explosionDamageFalloff = explosionDamageFalloff; }

    // Getters & Setters - Homing data
    public float getSpeed() { return homingData.speed; }
    public void setSpeed(float speed) { this.homingData.speed = speed; }
    public boolean isHoming() { return homingData.homing; }
    public void setHoming(boolean homing) { this.homingData.homing = homing; }
    public float getHomingStrength() { return homingData.homingStrength; }
    public void setHomingStrength(float homingStrength) { this.homingData.homingStrength = homingStrength; }
    public float getHomingRange() { return homingData.homingRange; }
    public void setHomingRange(float homingRange) { this.homingData.homingRange = homingRange; }

    // Getters & Setters - Lightning data
    public boolean hasLightningEffect(int beam) { return getLightningData(beam).lightningEffect; }
    public void setLightningEffect(int beam, boolean lightningEffect) { getLightningData(beam).lightningEffect = lightningEffect; }
    public float getLightningDensity(int beam) { return getLightningData(beam).lightningDensity; }
    public void setLightningDensity(int beam, float lightningDensity) { getLightningData(beam).lightningDensity = lightningDensity; }
    public float getLightningRadius(int beam) { return getLightningData(beam).lightningRadius; }
    public void setLightningRadius(int beam, float lightningRadius) { getLightningData(beam).lightningRadius = lightningRadius; }

    public boolean hasLightningEffect() { return hasLightningEffect(0); }
    public void setLightningEffect(boolean lightningEffect) { setLightningEffect(0, lightningEffect); }
    public float getLightningDensity() { return getLightningDensity(0); }
    public void setLightningDensity(float lightningDensity) { setLightningDensity(0, lightningDensity); }
    public float getLightningRadius() { return getLightningRadius(0); }
    public void setLightningRadius(float lightningRadius) { setLightningRadius(0, lightningRadius); }


    // Getters & Setters - Lifespan data
    public float getMaxDistance() { return lifespanData.maxDistance; }
    public void setMaxDistance(float maxDistance) { this.lifespanData.maxDistance = maxDistance; }
    public int getMaxLifetime() { return lifespanData.maxLifetime; }
    public void setMaxLifetime(int maxLifetime) { this.lifespanData.maxLifetime = maxLifetime; }

    // Getters & Setters - Anchor point
    public AnchorPoint getAnchorPointEnum(int beam) { return getAnchorData(beam).anchorPoint; }
    public void setAnchorPointEnum(int beam, AnchorPoint anchorPoint) { getAnchorData(beam).anchorPoint = anchorPoint; }
    public float getAnchorOffsetX(int beam) { return getAnchorData(beam).anchorOffsetX; }
    public void setAnchorOffsetX(int beam, float x) { getAnchorData(beam).anchorOffsetX = x; }
    public float getAnchorOffsetY(int beam) { return getAnchorData(beam).anchorOffsetY; }
    public void setAnchorOffsetY(int beam, float y) { getAnchorData(beam).anchorOffsetY = y; }
    public float getAnchorOffsetZ(int beam) { return getAnchorData(beam).anchorOffsetZ; }
    public void setAnchorOffsetZ(int beam, float z) { getAnchorData(beam).anchorOffsetZ = z; }

    public AnchorPoint getAnchorPointEnum() { return getAnchorPointEnum(0); }
    public void setAnchorPointEnum(AnchorPoint anchorPoint) { setAnchorPointEnum(0, anchorPoint); }
    public float getAnchorOffsetX() { return getAnchorOffsetX(0); }
    public void setAnchorOffsetX(float x) { setAnchorOffsetX(0, x); }
    public float getAnchorOffsetY() { return getAnchorOffsetY(0); }
    public void setAnchorOffsetY(float y) { setAnchorOffsetY(0, y); }
    public float getAnchorOffsetZ() { return getAnchorOffsetZ(0); }
    public void setAnchorOffsetZ(float z) { setAnchorOffsetZ(0, z); }

    //@Override
    public int getAnchorPoint(int beam) { return getAnchorData(beam).anchorPoint.ordinal(); }
    public int getAnchorPoint() { return getAnchorData(0).anchorPoint.ordinal(); }

    //@Override
    public void setAnchorPoint(int beam, int point) { getAnchorData(beam).anchorPoint = AnchorPoint.fromOrdinal(point); }
    public void setAnchorPoint(int point) { getAnchorData(0).anchorPoint = AnchorPoint.fromOrdinal(point); }

    @SideOnly(Side.CLIENT)
    @Override
    public List<FieldDef> getFieldDefinitions() {
        return Arrays.asList(
            // Type tab
            FieldDef.floatField("enchantment.damage", this::getDamage, this::setDamage).column(ColumnHint.LEFT),
            FieldDef.floatField("stats.speed", this::getSpeed, this::setSpeed).column(ColumnHint.RIGHT),
            FieldDef.floatField("ability.knockback", this::getKnockback, this::setKnockback),
            FieldDef.section("ability.section.beam"),
            FieldDef.floatField("ability.beamWidth", this::getBeamWidth, this::setBeamWidth).column(ColumnHint.LEFT),
            FieldDef.floatField("ability.headSize", this::getHeadSize, this::setHeadSize).column(ColumnHint.RIGHT),
            FieldDef.floatField("ability.maxDistance", this::getMaxDistance, this::setMaxDistance).column(ColumnHint.LEFT),
            FieldDef.intField("ability.lifetime", this::getMaxLifetime, this::setMaxLifetime).column(ColumnHint.RIGHT),
            FieldDef.section("ability.section.dual"),
            FieldDef.boolField("ability.dualFire", this::isDualFire, this::setDualFire),
            FieldDef.intField("ability.dualFireDelay", this::getDualFireDelay, this::setDualFireDelay)
                .visibleWhen(this::isDualFire),
            FieldDef.section("ability.section.homing"),
            FieldDef.boolField("gui.enabled", this::isHoming, this::setHoming)
                .hover("ability.hover.homing"),
            FieldDef.floatField("gui.strength", this::getHomingStrength, this::setHomingStrength)
                .visibleWhen(this::isHoming).column(ColumnHint.LEFT),
            FieldDef.floatField("gui.range", this::getHomingRange, this::setHomingRange)
                .visibleWhen(this::isHoming).column(ColumnHint.RIGHT),
            FieldDef.section("ability.section.explosive"),
            FieldDef.boolField("gui.enabled", this::isExplosive, this::setExplosive)
                .hover("ability.hover.explosive"),
            FieldDef.floatField("gui.radius", this::getExplosionRadius, this::setExplosionRadius)
                .visibleWhen(this::isExplosive),
            AbilityFieldDefs.effectsListField("ability.effects", this::getEffects, this::setEffects),
            // Visual tab - Beam 1
            FieldDef.enumField("ability.anchorPoint", AnchorPoint.class,
                () -> getAnchorPointEnum(0), v -> setAnchorPointEnum(0, v))
                .tab("ability.tab.visual"),
            FieldDef.section("ability.section.beam1").tab("ability.tab.visual"),
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
            // Visual tab - Beam 2
            FieldDef.enumField("ability.anchorPoint", AnchorPoint.class,
                () -> getAnchorPointEnum(1), v -> setAnchorPointEnum(1, v))
                .tab("ability.tab.visual"),
            FieldDef.section("ability.section.beam2").tab("ability.tab.visual"),
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

        EntityAbilityBeam beam = new EntityAbilityBeam(npc.worldObj);
        beam.setupPreview(npc, beamWidth, headSize, colorData[0], lightningData[0], anchorData[0], windUpTicks, 1.0f);
        return beam;
    }

    @Override
    public int getPreviewActiveDuration() {
        return lifespanData.maxLifetime > 0 ? Math.min(lifespanData.maxLifetime, 100) : 100;
    }
}
