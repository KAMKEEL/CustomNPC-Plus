package kamkeel.npcs.controllers.data.ability.type;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.AbilityAnimation;
import kamkeel.npcs.controllers.data.ability.AnchorPoint;
import kamkeel.npcs.controllers.data.ability.TargetingMode;
import kamkeel.npcs.controllers.data.telegraph.Telegraph;
import kamkeel.npcs.controllers.data.telegraph.TelegraphInstance;
import kamkeel.npcs.controllers.data.telegraph.TelegraphType;
import kamkeel.npcs.entity.EntityAbilityOrb;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import noppes.npcs.client.gui.advanced.SubGuiAbilityConfig;
import noppes.npcs.client.gui.advanced.ability.SubGuiAbilityOrb;
import noppes.npcs.client.gui.util.IAbilityConfigCallback;
import noppes.npcs.controllers.data.Animation;
import noppes.npcs.api.ability.type.IAbilityOrb;
import noppes.npcs.entity.EntityNPCInterface;

/**
 * Orb ability: Spawns a homing projectile sphere that tracks target.
 * The EntityAbilityOrb handles all movement, collision, and damage logic.
 * This ability just configures and spawns the orb entity.
 */
public class AbilityOrb extends Ability implements IAbilityOrb {

    // Movement properties
    private float orbSpeed = 0.5f;
    private float orbSize = 1.0f;
    private float maxDistance = 30.0f; // Max distance before orb dies
    private int maxLifetime = 200; // Ticks the orb lives

    // Combat properties
    private float damage = 7.0f;
    private float knockback = 1.0f;
    private float knockbackUp = 0.1f;

    // Homing properties
    private boolean homing = true;
    private float homingStrength = 0.15f;
    private float homingRange = 20.0f;

    // Explosion properties
    private boolean explosive = false;
    private float explosionRadius = 3.0f;
    private float explosionDamageFalloff = 0.5f;

    // Effect properties
    private int stunDuration = 0;
    private int slowDuration = 0;
    private int slowLevel = 0;

    // Visual properties
    private int innerColor = 0xFFFFFF;  // White core
    private int outerColor = 0x8888FF;  // Light blue glow
    private float outerColorWidth = 0.4f; // Additive offset from inner size
    private boolean outerColorEnabled = true; // Whether to render outer glow
    private float rotationSpeed = 4.0f; // Degrees per tick

    // Lightning effect properties
    private boolean lightningEffect = false;
    private float lightningDensity = 0.15f;  // Bolts spawned per tick (0.15 = ~15% chance per frame)
    private float lightningRadius = 0.5f;    // Max distance lightning arcs - scales with orbSize
    private int lightningFadeTime = 6;       // Ticks before lightning fades out

    // Anchor point for charging position
    private AnchorPoint anchorPoint = AnchorPoint.FRONT;

    // Transient state for orb entity (used during windup charging)
    private transient EntityAbilityOrb orbEntity = null;

    private final AbilityAnimation animation = AbilityAnimation.ABILITYORB;

    public AbilityOrb() {
        this.typeId = "ability.cnpc.orb";
        this.name = "Orb";
        this.targetingMode = TargetingMode.AGGRO_TARGET;
        this.maxRange = 25.0f;
        this.minRange = 5.0f;
        this.cooldownTicks = 0;
        this.windUpTicks = 30;
        this.telegraphType = TelegraphType.CIRCLE;
        this.showTelegraph = true;
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
        orbEntity = null;

        // Orb entity manages itself - ability is done
        signalCompletion();
    }

    @Override
    public void onWindUpTick(EntityNPCInterface npc, EntityLivingBase target, World world, int tick) {
        if (world.isRemote) return;

        // Spawn orb in charging mode on first tick of windup
        if (tick == 1) {
            // Create orb in charging mode - follows NPC based on anchor point during windup
            orbEntity = EntityAbilityOrb.createCharging(
                world, npc, target,
                orbSize, innerColor, outerColor, outerColorEnabled, outerColorWidth, rotationSpeed,
                damage, knockback, knockbackUp,
                orbSpeed, homing, homingStrength, homingRange,
                explosive, explosionRadius, explosionDamageFalloff,
                stunDuration, slowDuration, slowLevel,
                maxDistance, maxLifetime,
                lightningEffect, lightningDensity, lightningRadius, lightningFadeTime,
                anchorPoint,  // Anchor point for charging position
                windUpTicks   // Charge duration = windup duration
            );

            world.spawnEntityInWorld(orbEntity);
        }
    }

    @Override
    public void onActiveTick(EntityNPCInterface npc, EntityLivingBase target, World world, int tick) {
        // Nothing to do - entity manages itself
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
        nbt.setFloat("orbSpeed", orbSpeed);
        nbt.setFloat("orbSize", orbSize);
        nbt.setFloat("maxDistance", maxDistance);
        nbt.setInteger("maxLifetime", maxLifetime);
        nbt.setFloat("damage", damage);
        nbt.setFloat("knockback", knockback);
        nbt.setFloat("knockbackUp", knockbackUp);
        nbt.setBoolean("homing", homing);
        nbt.setFloat("homingStrength", homingStrength);
        nbt.setFloat("homingRange", homingRange);
        nbt.setBoolean("explosive", explosive);
        nbt.setFloat("explosionRadius", explosionRadius);
        nbt.setFloat("explosionDamageFalloff", explosionDamageFalloff);
        nbt.setInteger("stunDuration", stunDuration);
        nbt.setInteger("slowDuration", slowDuration);
        nbt.setInteger("slowLevel", slowLevel);
        nbt.setInteger("innerColor", innerColor);
        nbt.setInteger("outerColor", outerColor);
        nbt.setFloat("outerColorWidth", outerColorWidth);
        nbt.setBoolean("outerColorEnabled", outerColorEnabled);
        nbt.setFloat("rotationSpeed", rotationSpeed);
        nbt.setBoolean("lightningEffect", lightningEffect);
        nbt.setFloat("lightningDensity", lightningDensity);
        nbt.setFloat("lightningRadius", lightningRadius);
        nbt.setInteger("lightningFadeTime", lightningFadeTime);
        nbt.setInteger("anchorPoint", anchorPoint.getId());
    }

    @Override
    public void readTypeNBT(NBTTagCompound nbt) {
        this.orbSpeed = nbt.hasKey("orbSpeed") ? nbt.getFloat("orbSpeed") : 0.5f;
        this.orbSize = nbt.hasKey("orbSize") ? nbt.getFloat("orbSize") : 1.0f;
        this.maxDistance = nbt.hasKey("maxDistance") ? nbt.getFloat("maxDistance") : 30.0f;
        this.maxLifetime = nbt.hasKey("maxLifetime") ? nbt.getInteger("maxLifetime") : 200;
        this.damage = nbt.hasKey("damage") ? nbt.getFloat("damage") : 7.0f;
        this.knockback = nbt.hasKey("knockback") ? nbt.getFloat("knockback") : 1.0f;
        this.knockbackUp = nbt.hasKey("knockbackUp") ? nbt.getFloat("knockbackUp") : 0.1f;
        this.homing = !nbt.hasKey("homing") || nbt.getBoolean("homing");
        this.homingStrength = nbt.hasKey("homingStrength") ? nbt.getFloat("homingStrength") : 0.15f;
        this.homingRange = nbt.hasKey("homingRange") ? nbt.getFloat("homingRange") : 20.0f;
        this.explosive = nbt.hasKey("explosive") && nbt.getBoolean("explosive");
        this.explosionRadius = nbt.hasKey("explosionRadius") ? nbt.getFloat("explosionRadius") : 3.0f;
        this.explosionDamageFalloff = nbt.hasKey("explosionDamageFalloff") ? nbt.getFloat("explosionDamageFalloff") : 0.5f;
        this.stunDuration = nbt.hasKey("stunDuration") ? nbt.getInteger("stunDuration") : 0;
        this.slowDuration = nbt.hasKey("slowDuration") ? nbt.getInteger("slowDuration") : 0;
        this.slowLevel = nbt.hasKey("slowLevel") ? nbt.getInteger("slowLevel") : 0;
        this.innerColor = nbt.hasKey("innerColor") ? nbt.getInteger("innerColor") : 0xFFFFFF;
        this.outerColor = nbt.hasKey("outerColor") ? nbt.getInteger("outerColor") : 0x8888FF;
        this.outerColorWidth = nbt.hasKey("outerColorWidth") ? nbt.getFloat("outerColorWidth") : 0.4f;
        this.outerColorEnabled = !nbt.hasKey("outerColorEnabled") || nbt.getBoolean("outerColorEnabled");
        this.rotationSpeed = nbt.hasKey("rotationSpeed") ? nbt.getFloat("rotationSpeed") : 4.0f;
        this.lightningEffect = nbt.hasKey("lightningEffect") && nbt.getBoolean("lightningEffect");
        this.lightningDensity = nbt.hasKey("lightningDensity") ? nbt.getFloat("lightningDensity") : 0.15f;
        this.lightningRadius = nbt.hasKey("lightningRadius") ? nbt.getFloat("lightningRadius") : 0.5f;
        this.lightningFadeTime = nbt.hasKey("lightningFadeTime") ? nbt.getInteger("lightningFadeTime") : 6;
        this.anchorPoint = nbt.hasKey("anchorPoint") ? AnchorPoint.fromId(nbt.getInteger("anchorPoint")) : AnchorPoint.FRONT;
    }

    // Getters & Setters
    public float getOrbSpeed() {
        return orbSpeed;
    }

    public void setOrbSpeed(float orbSpeed) {
        this.orbSpeed = orbSpeed;
    }

    public float getOrbSize() {
        return orbSize;
    }

    public void setOrbSize(float orbSize) {
        this.orbSize = orbSize;
    }

    public float getMaxDistance() {
        return maxDistance;
    }

    public void setMaxDistance(float maxDistance) {
        this.maxDistance = maxDistance;
    }

    public int getMaxLifetime() {
        return maxLifetime;
    }

    public void setMaxLifetime(int maxLifetime) {
        this.maxLifetime = maxLifetime;
    }

    public float getDamage() {
        return damage;
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    public float getKnockback() {
        return knockback;
    }

    public void setKnockback(float knockback) {
        this.knockback = knockback;
    }

    public float getKnockbackUp() {
        return knockbackUp;
    }

    public void setKnockbackUp(float knockbackUp) {
        this.knockbackUp = knockbackUp;
    }

    public boolean isHoming() {
        return homing;
    }

    public void setHoming(boolean homing) {
        this.homing = homing;
    }

    public float getHomingStrength() {
        return homingStrength;
    }

    public void setHomingStrength(float homingStrength) {
        this.homingStrength = homingStrength;
    }

    public float getHomingRange() {
        return homingRange;
    }

    public void setHomingRange(float homingRange) {
        this.homingRange = homingRange;
    }

    public boolean isExplosive() {
        return explosive;
    }

    public void setExplosive(boolean explosive) {
        this.explosive = explosive;
    }

    public float getExplosionRadius() {
        return explosionRadius;
    }

    public void setExplosionRadius(float explosionRadius) {
        this.explosionRadius = explosionRadius;
    }

    public float getExplosionDamageFalloff() {
        return explosionDamageFalloff;
    }

    public void setExplosionDamageFalloff(float explosionDamageFalloff) {
        this.explosionDamageFalloff = explosionDamageFalloff;
    }

    public int getStunDuration() {
        return stunDuration;
    }

    public void setStunDuration(int stunDuration) {
        this.stunDuration = stunDuration;
    }

    public int getSlowDuration() {
        return slowDuration;
    }

    public void setSlowDuration(int slowDuration) {
        this.slowDuration = slowDuration;
    }

    public int getSlowLevel() {
        return slowLevel;
    }

    public void setSlowLevel(int slowLevel) {
        this.slowLevel = slowLevel;
    }

    public int getInnerColor() {
        return innerColor;
    }

    public void setInnerColor(int innerColor) {
        this.innerColor = innerColor;
    }

    public int getOuterColor() {
        return outerColor;
    }

    public void setOuterColor(int outerColor) {
        this.outerColor = outerColor;
    }

    public float getOuterColorWidth() {
        return outerColorWidth;
    }

    public void setOuterColorWidth(float outerColorWidth) {
        this.outerColorWidth = outerColorWidth;
    }

    public boolean isOuterColorEnabled() {
        return outerColorEnabled;
    }

    public void setOuterColorEnabled(boolean outerColorEnabled) {
        this.outerColorEnabled = outerColorEnabled;
    }

    public float getRotationSpeed() {
        return rotationSpeed;
    }

    public void setRotationSpeed(float rotationSpeed) {
        this.rotationSpeed = rotationSpeed;
    }

    public boolean hasLightningEffect() {
        return lightningEffect;
    }

    public void setLightningEffect(boolean lightningEffect) {
        this.lightningEffect = lightningEffect;
    }

    public float getLightningDensity() {
        return lightningDensity;
    }

    public void setLightningDensity(float lightningDensity) {
        this.lightningDensity = lightningDensity;
    }

    public float getLightningRadius() {
        return lightningRadius;
    }

    public void setLightningRadius(float lightningRadius) {
        this.lightningRadius = lightningRadius;
    }

    public int getLightningFadeTime() {
        return lightningFadeTime;
    }

    public void setLightningFadeTime(int lightningFadeTime) {
        this.lightningFadeTime = lightningFadeTime;
    }

    public AnchorPoint getAnchorPointEnum() {
        return anchorPoint;
    }

    public void setAnchorPointEnum(AnchorPoint anchorPoint) {
        this.anchorPoint = anchorPoint;
    }

    @Override
    public Animation getWindUpAnimation() {
        if (windUpAnimationId != -1)
            return super.getWindUpAnimation();

        return animation.windUp();
    }

    public int getAnchorPoint() {
        return anchorPoint.getId();
    }

    @Override
    public void setAnchorPoint(int point) {
        this.anchorPoint = AnchorPoint.fromId(point);
    }
}
