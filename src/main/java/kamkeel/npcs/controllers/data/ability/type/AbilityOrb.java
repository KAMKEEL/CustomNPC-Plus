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
import kamkeel.npcs.entity.EntityAbilityOrb;
import kamkeel.npcs.util.AnchorPointHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import noppes.npcs.client.gui.advanced.SubGuiAbilityConfig;
import noppes.npcs.client.gui.advanced.ability.SubGuiAbilityOrb;
import noppes.npcs.client.gui.util.IAbilityConfigCallback;
import noppes.npcs.api.ability.type.IAbilityOrb;
import noppes.npcs.entity.EntityNPCInterface;

/**
 * Orb ability: Spawns a homing projectile sphere that tracks target.
 * The EntityAbilityOrb handles all movement, collision, and damage logic.
 * This ability just configures and spawns the orb entity.
 */
public class AbilityOrb extends Ability implements IAbilityOrb {

    // Ability-specific properties
    private float orbSize = 1.0f;

    // Data classes for energy properties
    public final EnergyColorData colorData = new EnergyColorData();
    public final EnergyCombatData combatData = new EnergyCombatData();
    public final EnergyHomingData homingData = new EnergyHomingData();
    public final EnergyLightningData lightningData = new EnergyLightningData();
    public final EnergyLifespanData lifespanData = new EnergyLifespanData();
    private EnergyAnchorData anchorData = new EnergyAnchorData(AnchorPoint.RIGHT_HAND);

    // Transient state for orb entity (used during windup charging)
    private transient EntityAbilityOrb orbEntity = null;

    public AbilityOrb() {
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
        // Default built-in animation
        this.windUpAnimationName = "Ability_Orb_Windup";
        this.activeAnimationName = "Ability_Orb_Active";
    }

    @Override
    public boolean hasTypeSettings() {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public SubGuiAbilityConfig createConfigGui(IAbilityConfigCallback callback) {
        return new SubGuiAbilityOrb(this, callback);
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
    public void onExecute(EntityNPCInterface npc, EntityLivingBase target, World world) {
        if (world.isRemote) {
            signalCompletion();
            return;
        }

        // Start moving the orb that was spawned during windup
        if (orbEntity != null && !orbEntity.isDead) {
            orbEntity.startMoving(target);
        }

        // Ability stays active until entity dies (prevents firing another while projectile is alive)
        // Movement locking is handled separately by the base class
    }

    @Override
    public void onWindUpTick(EntityNPCInterface npc, EntityLivingBase target, World world, int tick) {
        if (world.isRemote) return;

        // Spawn orb in charging mode on first tick of windup
        if (tick == 1) {
            // Create orb in charging mode - follows NPC based on anchor point during windup
            Vec3 spawnPos = AnchorPointHelper.calculateAnchorPosition(npc, anchorData);
            orbEntity = new EntityAbilityOrb(
                world, npc, target,
                spawnPos.xCoord, spawnPos.yCoord, spawnPos.zCoord, orbSize,
                colorData, combatData, homingData, lightningData, lifespanData);
            orbEntity.setupCharging(anchorData, windUpTicks);

            orbEntity.setEffects(this.effects);
            world.spawnEntityInWorld(orbEntity);
        }
    }

    @Override
    public void onActiveTick(EntityNPCInterface npc, EntityLivingBase target, World world, int tick) {
        // Signal completion when entity dies
        if (orbEntity == null || orbEntity.isDead) {
            orbEntity = null;
            signalCompletion();
        }
    }

    @Override
    public void onComplete(EntityNPCInterface npc, EntityLivingBase target) {
        // Nothing to clean up - entity manages itself
    }

    @Override
    public void cleanup() {
        // Despawn orb entity if still alive
        if (orbEntity != null && !orbEntity.isDead) {
            orbEntity.setDead();
        }
        orbEntity = null;
    }

    @Override
    public TelegraphInstance createTelegraph(EntityNPCInterface npc, EntityLivingBase target) {
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
        TelegraphInstance instance = new TelegraphInstance(telegraph, target.posX, target.posY, target.posZ, npc.rotationYaw);
        instance.setCasterEntityId(npc.getEntityId());
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
        anchorData.writeNBT(nbt);
        colorData.writeNBT(nbt);
        combatData.writeNBT(nbt);
        homingData.writeNBT(nbt);
        lightningData.writeNBT(nbt);
        lifespanData.writeNBT(nbt);
        // Backward compat: old key was "orbSpeed"
        nbt.setFloat("orbSpeed", homingData.speed);
    }

    @Override
    public void readTypeNBT(NBTTagCompound nbt) {
        this.orbSize = nbt.hasKey("orbSize") ? nbt.getFloat("orbSize") : 1.0f;
        anchorData.readNBT(nbt);
        colorData.readNBT(nbt);
        combatData.readNBT(nbt);
        homingData.readNBT(nbt);
        lightningData.readNBT(nbt);
        lifespanData.readNBT(nbt);
        // Backward compat: old key was "orbSpeed"
        if (nbt.hasKey("orbSpeed")) {
            homingData.speed = nbt.getFloat("orbSpeed");
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

    public AnchorPoint getAnchorPointEnum() { return anchorData.anchorPoint; }

    public float getAnchorOffsetX() { return anchorData.anchorOffsetX; }

    public float getAnchorOffsetY() { return anchorData.anchorOffsetY; }

    public float getAnchorOffsetZ() { return anchorData.anchorOffsetZ; }

    public void setAnchorPointEnum(AnchorPoint anchorPoint) { this.anchorData.anchorPoint = anchorPoint; }

    public void setAnchorOffsetX(float x) { this.anchorData.anchorOffsetX = x; }

    public void setAnchorOffsetY(float y) { this.anchorData.anchorOffsetY = y; }

    public void setAnchorOffsetZ(float z) { this.anchorData.anchorOffsetZ = z; }

    public int getAnchorPoint() {
        return anchorData.anchorPoint.getId();
    }

    @Override
    public void setAnchorPoint(int point) {
        this.anchorData.anchorPoint = AnchorPoint.fromId(point);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Entity createPreviewEntity(EntityNPCInterface npc) {
        if (npc == null || npc.worldObj == null) return null;

        EntityAbilityOrb orb = new EntityAbilityOrb(npc.worldObj);
        orb.setupPreview(npc, orbSize, colorData, lightningData, anchorData, windUpTicks);
        return orb;
    }

    @Override
    public int getPreviewActiveDuration() {
        return lifespanData.maxLifetime > 0 ? Math.min(lifespanData.maxLifetime, 100) : 100;
    }
}
