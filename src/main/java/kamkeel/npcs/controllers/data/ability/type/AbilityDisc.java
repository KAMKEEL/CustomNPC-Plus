package kamkeel.npcs.controllers.data.ability.type;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.AnchorPoint;
import kamkeel.npcs.controllers.data.ability.LockMovementType;
import kamkeel.npcs.controllers.data.ability.TargetingMode;
import kamkeel.npcs.controllers.data.ability.data.*;
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
import noppes.npcs.client.gui.advanced.SubGuiAbilityConfig;
import noppes.npcs.client.gui.advanced.ability.SubGuiAbilityDisc;
import noppes.npcs.client.gui.util.IAbilityConfigCallback;
import noppes.npcs.api.ability.type.IAbilityDisc;
import noppes.npcs.entity.EntityNPCInterface;

/**
 * Disc ability: Spawns a flat spinning disc projectile.
 * Has optional boomerang behavior to return to owner.
 */
public class AbilityDisc extends Ability implements IAbilityDisc {

    // Disc geometry
    private float discRadius = 1.0f;
    private float discThickness = 0.2f;

    // Boomerang properties
    private boolean boomerang = false;
    private int boomerangDelay = 40;

    // Energy data classes
    private EnergyColorData colorData = new EnergyColorData(0xFFFFFF, 0xFF8800, true, 0.4f, 0.5f, 5.0f);
    private EnergyCombatData combatData = new EnergyCombatData(8.0f, 1.2f, 0.15f, false, 3.0f, 0.5f);
    private EnergyHomingData homingData = new EnergyHomingData(0.6f, true, 0.12f, 18.0f);
    private EnergyLightningData lightningData = new EnergyLightningData();
    private EnergyLifespanData lifespanData = new EnergyLifespanData(35.0f, 200);
    private EnergyAnchorData anchorData = new EnergyAnchorData(AnchorPoint.RIGHT_HAND);

    // Transient state for disc entity (used during windup charging)
    private transient EntityAbilityDisc discEntity = null;

    public AbilityDisc() {
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
        // Default built-in animations
        this.windUpAnimationName = "Ability_Disc_Windup";
        this.activeAnimationName = "Ability_Disc_Active";
    }

    @Override
    public boolean hasTypeSettings() {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public SubGuiAbilityConfig createConfigGui(IAbilityConfigCallback callback) {
        return new SubGuiAbilityDisc(this, callback);
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

        // Start moving the disc that was spawned during windup
        if (discEntity != null && !discEntity.isDead) {
            discEntity.startMoving(target);
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
            Vec3 spawnPos = AnchorPointHelper.calculateAnchorPosition(caster, anchorData);
            discEntity = new EntityAbilityDisc(
                world, caster, target,
                spawnPos.xCoord, spawnPos.yCoord, spawnPos.zCoord,
                discRadius, discThickness,
                colorData, combatData, homingData, lightningData, lifespanData,
                boomerang, boomerangDelay);
            discEntity.setupCharging(anchorData, windUpTicks);

            discEntity.setEffects(this.effects);
            world.spawnEntityInWorld(discEntity);
        }
    }

    @Override
    public void onActiveTick(EntityLivingBase caster, EntityLivingBase target, World world, int tick) {
        // Signal completion when entity dies
        if (discEntity == null || discEntity.isDead) {
            discEntity = null;
            signalCompletion();
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
        discEntity = null;
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
        anchorData.writeNBT(nbt);
        colorData.writeNBT(nbt);
        combatData.writeNBT(nbt);
        homingData.writeNBT(nbt);
        lightningData.writeNBT(nbt);
        lifespanData.writeNBT(nbt);
    }

    @Override
    public void readTypeNBT(NBTTagCompound nbt) {
        this.discRadius = nbt.hasKey("discRadius") ? nbt.getFloat("discRadius") : 1.0f;
        this.discThickness = nbt.hasKey("discThickness") ? nbt.getFloat("discThickness") : 0.2f;
        this.boomerang = nbt.hasKey("boomerang") && nbt.getBoolean("boomerang");
        this.boomerangDelay = nbt.hasKey("boomerangDelay") ? nbt.getInteger("boomerangDelay") : 40;
        anchorData.readNBT(nbt);
        colorData.readNBT(nbt);
        combatData.readNBT(nbt);
        homingData.readNBT(nbt);
        lightningData.readNBT(nbt);
        lifespanData.readNBT(nbt);
    }

    // Getters & Setters
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
    public int getInnerColor() { return colorData.innerColor; }
    public void setInnerColor(int innerColor) { colorData.innerColor = innerColor; }
    public int getOuterColor() { return colorData.outerColor; }
    public void setOuterColor(int outerColor) { colorData.outerColor = outerColor; }
    public float getOuterColorWidth() { return colorData.outerColorWidth; }
    public void setOuterColorWidth(float outerColorWidth) { colorData.outerColorWidth = outerColorWidth; }
    public float getOuterColorAlpha() { return colorData.outerColorAlpha; }
    public void setOuterColorAlpha(float outerColorAlpha) { colorData.outerColorAlpha = outerColorAlpha; }
    public boolean isOuterColorEnabled() { return colorData.outerColorEnabled; }
    public void setOuterColorEnabled(boolean outerColorEnabled) { colorData.outerColorEnabled = outerColorEnabled; }
    public float getRotationSpeed() { return colorData.rotationSpeed; }
    public void setRotationSpeed(float rotationSpeed) { colorData.rotationSpeed = rotationSpeed; }
    public boolean hasLightningEffect() { return lightningData.lightningEffect; }
    public void setLightningEffect(boolean lightningEffect) { lightningData.lightningEffect = lightningEffect; }
    public float getLightningDensity() { return lightningData.lightningDensity; }
    public void setLightningDensity(float lightningDensity) { lightningData.lightningDensity = lightningDensity; }
    public float getLightningRadius() { return lightningData.lightningRadius; }
    public void setLightningRadius(float lightningRadius) { lightningData.lightningRadius = lightningRadius; }
    public AnchorPoint getAnchorPointEnum() { return anchorData.anchorPoint; }
    public float getAnchorOffsetX() { return anchorData.anchorOffsetX; }
    public float getAnchorOffsetY() { return anchorData.anchorOffsetY; }
    public float getAnchorOffsetZ() { return anchorData.anchorOffsetZ; }
    public void setAnchorPointEnum(AnchorPoint anchorPoint) { this.anchorData.anchorPoint = anchorPoint; }
    public void setAnchorOffsetX(float x) { this.anchorData.anchorOffsetX = x; }
    public void setAnchorOffsetY(float y) { this.anchorData.anchorOffsetY = y; }
    public void setAnchorOffsetZ(float z) { this.anchorData.anchorOffsetZ = z; }

    @Override
    public int getAnchorPoint() { return anchorData.anchorPoint.ordinal(); }

    @Override
    public void setAnchorPoint(int point) { this.anchorData.anchorPoint = AnchorPoint.fromOrdinal(point); }

    @Override
    @SideOnly(Side.CLIENT)
    public Entity createPreviewEntity(EntityNPCInterface npc) {
        if (npc == null || npc.worldObj == null) return null;

        EntityAbilityDisc disc = new EntityAbilityDisc(npc.worldObj);
        disc.setupPreview(npc, discRadius, discThickness, colorData, lightningData, anchorData, windUpTicks);
        return disc;
    }

    @Override
    public int getPreviewActiveDuration() {
        return lifespanData.maxLifetime > 0 ? Math.min(lifespanData.maxLifetime, 100) : 100;
    }
}
