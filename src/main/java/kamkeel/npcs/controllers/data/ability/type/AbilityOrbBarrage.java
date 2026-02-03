package kamkeel.npcs.controllers.data.ability.type;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.AnchorPoint;
import kamkeel.npcs.controllers.data.ability.LockMovementType;
import kamkeel.npcs.controllers.data.ability.TargetingMode;
import kamkeel.npcs.controllers.data.ability.data.*;
import kamkeel.npcs.controllers.data.ability.gui.AbilityFieldDefs;
import kamkeel.npcs.controllers.data.telegraph.Telegraph;
import kamkeel.npcs.controllers.data.telegraph.TelegraphInstance;
import kamkeel.npcs.controllers.data.telegraph.TelegraphType;
import kamkeel.npcs.entity.EntityAbilityOrb;
import kamkeel.npcs.util.AnchorPointHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import noppes.npcs.client.gui.builder.FieldDef;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.ValueUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AbilityOrbBarrage extends Ability {

    // Ability-specific properties
    private float orbSize = 0.5f;
    private int orbAmount = 16;
    private int firingSpeed = 2;
    private boolean dualCharging = true;

    // Data classes for energy properties
    private final EnergyDisplayData colorData = new EnergyDisplayData();
    private final EnergyCombatData combatData = new EnergyCombatData(4.0f, 0.0f, 0.0f, false, 1.5f, 0.5f);
    private final EnergyHomingData homingData = new EnergyHomingData();
    private final EnergyLightningData lightningData = new EnergyLightningData();
    private final EnergyLifespanData lifespanData = new EnergyLifespanData();
    private final EnergyAnchorData[] anchorData = new EnergyAnchorData[]{
        new EnergyAnchorData(AnchorPoint.RIGHT_HAND), new EnergyAnchorData(AnchorPoint.LEFT_HAND)
    };

    // Array to store entities
    private transient List<EntityAbilityOrb> orbEntities = new ArrayList<>();
    private transient EntityAbilityOrb[] chargingEntities = null;

    // Field to keep track of the currently firing orb
    private int currentOrb = 0;

    public AbilityOrbBarrage() {
        this.typeId = "ability.cnpc.orb_barrage";
        this.name = "Orb Barrage";
        this.targetingMode = TargetingMode.AGGRO_TARGET;
        this.maxRange = 75.0f;
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

//    @Override
//    @SideOnly(Side.CLIENT)
//    public SubGuiAbilityConfig createConfigGui(IAbilityConfigCallback callback) {
//        return new SubGuiAbilityOrbBarrage(this, callback);
//    }

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

        for (int i = 0; i < chargingEntities.length; i++) {
            EntityAbilityOrb orbEntity = chargingEntities[i];

            if (orbEntity != null && !orbEntity.isDead) {
                orbEntity.setDead();
            }

            chargingEntities[i] = null;
        }
    }

    @Override
    public void onWindUpTick(EntityLivingBase caster, EntityLivingBase target, World world, int tick) {
        if (world.isRemote) return;

        // Spawn orb in charging mode on first tick of windup
        if (tick == 1) {
            // Create orb in charging mode - follows caster based on anchor point during windup

            if (dualCharging) {
                chargingEntities = new EntityAbilityOrb[2];
            } else {
                chargingEntities = new EntityAbilityOrb[1];
            }

            for (int i = 0; i < chargingEntities.length; i++) {
                Vec3 spawnPos = AnchorPointHelper.calculateAnchorPosition(caster, anchorData[i]);

                EntityAbilityOrb orbEntity = new EntityAbilityOrb(
                    world, caster, target,
                    spawnPos.xCoord, spawnPos.yCoord, spawnPos.zCoord, orbSize,
                    colorData, combatData, homingData, lightningData, lifespanData
                );

                orbEntity.setupCharging(anchorData[i], windUpTicks);

                chargingEntities[i] = orbEntity;
                world.spawnEntityInWorld(orbEntity);
            }
        }
    }

    @Override
    public void onActiveTick(EntityLivingBase caster, EntityLivingBase target, World world, int tick) {
        if (!orbEntities.isEmpty()) {
            boolean allDead = true;

            for (EntityAbilityOrb orbEntity : orbEntities) {
                if (orbEntity != null && !orbEntity.isDead) {
                    allDead = false;
                    break;
                }
            }

            if (allDead && currentOrb >= orbAmount) {
                signalCompletion();
                return;
            }
        }

        if (currentOrb >= orbAmount) {
            return;
        }

        int ticksPerOrb = Math.max(1, 20 / firingSpeed);

        if (tick % ticksPerOrb != 0) {
            return;
        }

        EnergyAnchorData anchor;

        if (dualCharging) {
            anchor = anchorData[currentOrb % 2];
        } else {
            anchor = anchorData[0];
        }

        Vec3 spawnPos = AnchorPointHelper.calculateAnchorPosition(caster, anchor);

        EntityAbilityOrb orb = new EntityAbilityOrb(
            world, caster, target, spawnPos.xCoord, spawnPos.yCoord, spawnPos.zCoord,
            orbSize, colorData, combatData, homingData, lightningData, lifespanData
        );

        world.spawnEntityInWorld(orb);

        orbEntities.add(orb);
        currentOrb++;
    }

    @Override
    public boolean canInterrupt(DamageSource source) {
        // Can be interrupted if the caster is hit when the beam is moving
        if (!interruptible) {
            return false;
        }

        // Only direct physical hits can interrupt, not magic, fire, or other indirect damage
        if (source == null) {
            return false;
        }

        // Reject indirect damage types
        if (source.isMagicDamage() || source.isFireDamage() || source.isExplosion()) {
            return false;
        }

        // Reject damage without a direct attacker entity
        if (source.getEntity() == null) {
            return false;
        }

        // Direct hit from an entity - can interrupt
        return true;
    }

    @Override
    public void cleanup() {
        currentOrb = 0;

        if (chargingEntities != null) {
            for (int i = 0; i < chargingEntities.length; i++) {
                if (chargingEntities[i] != null && !chargingEntities[i].isDead) {
                    chargingEntities[i].setDead();
                }
                chargingEntities[i] = null;
            }
            chargingEntities = null;
        }

        if (orbEntities != null && !orbEntities.isEmpty()) {
            for (EntityAbilityOrb orbEntity : orbEntities) {
                if (orbEntity != null && !orbEntity.isDead) {
                    orbEntity.setDead();
                }
            }
            orbEntities.clear();
        }
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
        nbt.setInteger("orbAmount", orbAmount);
        nbt.setInteger("firingSpeed", firingSpeed);
        nbt.setBoolean("dualCharging", dualCharging);
        colorData.writeNBT(nbt);
        combatData.writeNBT(nbt);
        homingData.writeNBT(nbt);
        lightningData.writeNBT(nbt);
        lifespanData.writeNBT(nbt);

        for (int i = 0; i < anchorData.length; i++) {
            NBTTagCompound comp = new NBTTagCompound();
            anchorData[i].writeNBT(comp);
            nbt.setTag("Anchor_" + i, comp);
        }
    }

    @Override
    public void readTypeNBT(NBTTagCompound nbt) {
        this.orbSize = nbt.hasKey("orbSize") ? nbt.getFloat("orbSize") : 1.0f;
        this.orbAmount = nbt.hasKey("orbAmount") ? nbt.getInteger("orbAmount") : 16;
        this.firingSpeed = nbt.hasKey("firingSpeed") ? nbt.getInteger("firingSpeed") : 2;
        this.dualCharging = !nbt.hasKey("dualCharging") || nbt.getBoolean("dualCharging");
        colorData.readNBT(nbt);
        combatData.readNBT(nbt);
        homingData.readNBT(nbt);
        lightningData.readNBT(nbt);
        lifespanData.readNBT(nbt);

        for (int i = 0; i < anchorData.length; i++) {
            if (nbt.hasKey("Anchor_" + i, Constants.NBT.TAG_COMPOUND)) {
                anchorData[i].readNBT(nbt.getCompoundTag("Anchor_" + i));
            } else {
                anchorData[i] = new EnergyAnchorData(i == 0 ? AnchorPoint.RIGHT_HAND : AnchorPoint.LEFT_HAND);
            }
        }
    }

    // Getters & Setters
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

    public int getOrbAmount() { return orbAmount; }

    public void setOrbAmount(int orbAmount) { this.orbAmount = orbAmount; }

    public int getFiringSpeed() { return firingSpeed; }

    public void setFiringSpeed(int firingSpeed) { this.firingSpeed = firingSpeed; }

    public boolean isDualCharging() { return dualCharging; }

    public void setDualCharging(boolean dualCharging) { this.dualCharging = dualCharging; }

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

    public int getInnerColor() {
        return colorData.innerColor;
    }

    public void setInnerColor(int innerColor) {
        colorData.innerColor = innerColor;
    }

    public int getOuterColor() {
        return colorData.outerColor;
    }

    public void setOuterColor(int outerColor) {
        colorData.outerColor = outerColor;
    }

    public float getOuterColorWidth() {
        return colorData.outerColorWidth;
    }

    public void setOuterColorWidth(float outerColorWidth) {
        colorData.outerColorWidth = outerColorWidth;
    }

    public float getOuterColorAlpha() {
        return colorData.outerColorAlpha;
    }

    public void setOuterColorAlpha(float outerColorAlpha) {
        colorData.outerColorAlpha = outerColorAlpha;
    }

    public boolean isOuterColorEnabled() {
        return colorData.outerColorEnabled;
    }

    public void setOuterColorEnabled(boolean outerColorEnabled) {
        colorData.outerColorEnabled = outerColorEnabled;
    }

    public float getRotationSpeed() {
        return colorData.rotationSpeed;
    }

    public void setRotationSpeed(float rotationSpeed) {
        colorData.rotationSpeed = rotationSpeed;
    }

    public boolean hasLightningEffect() {
        return lightningData.lightningEffect;
    }

    public void setLightningEffect(boolean lightningEffect) {
        lightningData.lightningEffect = lightningEffect;
    }

    public float getLightningDensity() {
        return lightningData.lightningDensity;
    }

    public void setLightningDensity(float lightningDensity) {
        lightningData.lightningDensity = lightningDensity;
    }

    public float getLightningRadius() {
        return lightningData.lightningRadius;
    }

    public void setLightningRadius(float lightningRadius) {
        lightningData.lightningRadius = lightningRadius;
    }

    public int getLightningFadeTime() {
        return lightningData.lightningFadeTime;
    }

    public void setLightningFadeTime(int lightningFadeTime) {
        lightningData.lightningFadeTime = lightningFadeTime;
    }

    private EnergyAnchorData getAnchorData(int orb) {
        orb = ValueUtil.clamp(orb, 0, 1);
        return anchorData[orb];
    }

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

    @Override
    @SideOnly(Side.CLIENT)
    public Entity createPreviewEntity(EntityNPCInterface npc) {
        if (npc == null || npc.worldObj == null) return null;

        EntityAbilityOrb orb = new EntityAbilityOrb(npc.worldObj);
        orb.setupPreview(npc, orbSize, colorData, lightningData, anchorData[0], windUpTicks);
        return orb;
    }

    @Override
    public int getPreviewActiveDuration() {
        return lifespanData.maxLifetime > 0 ? Math.min(lifespanData.maxLifetime, 100) : 100;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void getAbilityDefinitions(List<FieldDef> defs) {
        defs.addAll(Arrays.asList(
            // Type tab
            FieldDef.row(
                FieldDef.floatField("enchantment.damage", this::getDamage, this::setDamage),
                FieldDef.floatField("stats.speed", this::getOrbSpeed, this::setOrbSpeed)
            ),
            FieldDef.row(
                FieldDef.floatField("stats.size", this::getOrbSize, this::setOrbSize),
                FieldDef.floatField("ability.knockback", this::getKnockback, this::setKnockback)
            ),
            FieldDef.row(
                FieldDef.floatField("ability.maxDistance", this::getMaxDistance, this::setMaxDistance),
                FieldDef.intField("ability.lifetime", this::getMaxLifetime, this::setMaxLifetime)
            ),
            FieldDef.section("ability.section.barrage"),
            FieldDef.row(
                FieldDef.intField("ability.amount", this::getOrbAmount, this::setOrbAmount),
                FieldDef.intField("ability.firingSpeed", this::getFiringSpeed, this::setFiringSpeed)
            ),
            FieldDef.boolField("ability.dualCharge", this::isDualCharging, this::setDualCharging),
            FieldDef.section("ability.section.homing"),
            FieldDef.boolField("gui.enabled", this::isHoming, this::setHoming).hover("ability.hover.homing"),
            FieldDef.floatField("gui.strength", this::getHomingStrength, this::setHomingStrength).visibleWhen(this::isHoming),
            FieldDef.section("ability.section.explosive"),
            FieldDef.boolField("gui.enabled", this::isExplosive, this::setExplosive).hover("ability.hover.explosive"),
            FieldDef.floatField("gui.radius", this::getExplosionRadius, this::setExplosionRadius).visibleWhen(this::isExplosive),
            AbilityFieldDefs.effectsListField("ability.effects", this::getEffects, this::setEffects),


            // Visual tab
            FieldDef.enumField("ability.anchorPoint", AnchorPoint.class, this::getAnchorPointEnum, this::setAnchorPointEnum)
                .tab("ability.tab.visual"),
            FieldDef.section("ability.section.colors").tab("ability.tab.visual"),
            FieldDef.colorSubGui("ability.innerColor", this::getInnerColor, this::setInnerColor).tab("ability.tab.visual"),
            FieldDef.boolField("ability.outerEnabled", this::isOuterColorEnabled, this::setOuterColorEnabled).tab("ability.tab.visual"),
            FieldDef.colorSubGui("ability.outerColor", this::getOuterColor, this::setOuterColor)
                .tab("ability.tab.visual").visibleWhen(this::isOuterColorEnabled),
            FieldDef.row(
                FieldDef.floatField("ability.outerWidth", this::getOuterColorWidth, this::setOuterColorWidth)
                    .visibleWhen(this::isOuterColorEnabled),
                FieldDef.floatField("ability.outerAlpha", this::getOuterColorAlpha, this::setOuterColorAlpha)
                    .range(0, 1).visibleWhen(this::isOuterColorEnabled)
            ).tab("ability.tab.visual"),
            FieldDef.section("ability.section.effects").tab("ability.tab.visual"),
            FieldDef.floatField("ability.rotationSpeed", this::getRotationSpeed, this::setRotationSpeed).tab("ability.tab.visual"),
            FieldDef.boolField("ability.lightning", this::hasLightningEffect, this::setLightningEffect).tab("ability.tab.visual"),
            FieldDef.row(
                FieldDef.floatField("gui.density", this::getLightningDensity, this::setLightningDensity)
                    .range(0.01f, 100f).visibleWhen(this::hasLightningEffect),
                FieldDef.floatField("gui.radius", this::getLightningRadius, this::setLightningRadius)
                    .range(0.1f, 100f).visibleWhen(this::hasLightningEffect)
            ).tab("ability.tab.visual")
        ));
    }
}
