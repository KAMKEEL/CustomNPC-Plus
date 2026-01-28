package kamkeel.npcs.controllers.data.ability.type;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.AnchorPoint;
import kamkeel.npcs.controllers.data.ability.LockMovementType;
import kamkeel.npcs.controllers.data.ability.TargetingMode;
import kamkeel.npcs.controllers.data.telegraph.Telegraph;
import kamkeel.npcs.controllers.data.telegraph.TelegraphInstance;
import kamkeel.npcs.controllers.data.telegraph.TelegraphType;
import kamkeel.npcs.entity.EntityAbilityDisc;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
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

    // Movement properties
    private float speed = 0.6f;
    private float discRadius = 1.0f;
    private float discThickness = 0.2f;
    private float maxDistance = 35.0f;
    private int maxLifetime = 200;

    // Combat properties
    private float damage = 8.0f;
    private float knockback = 1.2f;
    private float knockbackUp = 0.15f;

    // Homing properties
    private boolean homing = true;
    private float homingStrength = 0.12f;
    private float homingRange = 18.0f;

    // Boomerang properties
    private boolean boomerang = false;
    private int boomerangDelay = 40;

    // Explosion properties
    private boolean explosive = false;
    private float explosionRadius = 3.0f;
    private float explosionDamageFalloff = 0.5f;

    // Effect properties
    private int stunDuration = 0;
    private int slowDuration = 0;
    private int slowLevel = 0;

    // Visual properties
    private int innerColor = 0xFFFFFF;
    private int outerColor = 0xFF8800;
    private float outerColorWidth = 0.4f; // Additive offset from inner size
    private float outerColorAlpha = 0.5f; // Additive offset from inner size
    private boolean outerColorEnabled = true;
    private float rotationSpeed = 5.0f;

    // Lightning effect properties
    private boolean lightningEffect = false;
    private float lightningDensity = 0.15f;
    private float lightningRadius = 0.5f;

    // Anchor point for charging position
    private AnchorPoint anchorPoint = AnchorPoint.FRONT;

    // Transient state for disc entity (used during windup charging)
    private transient EntityAbilityDisc discEntity = null;

    public AbilityDisc() {
        this.typeId = "ability.cnpc.disc";
        this.name = "Disc";
        this.targetingMode = TargetingMode.AGGRO_TARGET;
        this.maxRange = 30.0f;
        this.minRange = 5.0f;
        this.cooldownTicks = 0;
        this.windUpTicks = 20;
        this.lockMovement = LockMovementType.WINDUP;
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
    public void onExecute(EntityNPCInterface npc, EntityLivingBase target, World world) {
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
    public void onWindUpTick(EntityNPCInterface npc, EntityLivingBase target, World world, int tick) {
        if (world.isRemote) return;

        // Spawn disc in charging mode on first tick of windup
        if (tick == 1) {
            // Create disc in charging mode - follows NPC based on anchor point during windup
            discEntity = EntityAbilityDisc.createCharging(
                world, npc, target,
                discRadius, discThickness, innerColor, outerColor, outerColorEnabled, outerColorWidth, outerColorAlpha, rotationSpeed,
                damage, knockback, knockbackUp,
                speed, homing, homingStrength, homingRange,
                boomerang, boomerangDelay,
                explosive, explosionRadius, explosionDamageFalloff,
                stunDuration, slowDuration, slowLevel,
                maxDistance, maxLifetime,
                lightningEffect, lightningDensity, lightningRadius,
                anchorPoint,  // Anchor point for charging position
                windUpTicks   // Charge duration = windup duration
            );

            world.spawnEntityInWorld(discEntity);
        }
    }

    @Override
    public void onActiveTick(EntityNPCInterface npc, EntityLivingBase target, World world, int tick) {
        // Signal completion when entity dies
        if (discEntity == null || discEntity.isDead) {
            discEntity = null;
            signalCompletion();
        }
    }

    @Override
    public void onComplete(EntityNPCInterface npc, EntityLivingBase target) {
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
    public TelegraphInstance createTelegraph(EntityNPCInterface npc, EntityLivingBase target) {
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
        TelegraphInstance instance = new TelegraphInstance(telegraph, target.posX, target.posY, target.posZ, npc.rotationYaw);
        instance.setCasterEntityId(npc.getEntityId());
        instance.setEntityIdToFollow(target.getEntityId());

        return instance;
    }

    @Override
    public float getTelegraphRadius() {
        return discRadius * 1.5f;
    }

    @Override
    public void writeTypeNBT(NBTTagCompound nbt) {
        nbt.setFloat("speed", speed);
        nbt.setFloat("discRadius", discRadius);
        nbt.setFloat("discThickness", discThickness);
        nbt.setFloat("maxDistance", maxDistance);
        nbt.setInteger("maxLifetime", maxLifetime);
        nbt.setFloat("damage", damage);
        nbt.setFloat("knockback", knockback);
        nbt.setFloat("knockbackUp", knockbackUp);
        nbt.setBoolean("homing", homing);
        nbt.setFloat("homingStrength", homingStrength);
        nbt.setFloat("homingRange", homingRange);
        nbt.setBoolean("boomerang", boomerang);
        nbt.setInteger("boomerangDelay", boomerangDelay);
        nbt.setBoolean("explosive", explosive);
        nbt.setFloat("explosionRadius", explosionRadius);
        nbt.setFloat("explosionDamageFalloff", explosionDamageFalloff);
        nbt.setInteger("stunDuration", stunDuration);
        nbt.setInteger("slowDuration", slowDuration);
        nbt.setInteger("slowLevel", slowLevel);
        nbt.setInteger("innerColor", innerColor);
        nbt.setInteger("outerColor", outerColor);
        nbt.setFloat("outerColorWidth", outerColorWidth);
        nbt.setFloat("outerColorAlpha", outerColorAlpha);
        nbt.setBoolean("outerColorEnabled", outerColorEnabled);
        nbt.setFloat("rotationSpeed", rotationSpeed);
        nbt.setBoolean("lightningEffect", lightningEffect);
        nbt.setFloat("lightningDensity", lightningDensity);
        nbt.setFloat("lightningRadius", lightningRadius);
        nbt.setInteger("anchorPoint", anchorPoint.getId());
    }

    @Override
    public void readTypeNBT(NBTTagCompound nbt) {
        this.speed = nbt.hasKey("speed") ? nbt.getFloat("speed") : 0.6f;
        this.discRadius = nbt.hasKey("discRadius") ? nbt.getFloat("discRadius") : 1.0f;
        this.discThickness = nbt.hasKey("discThickness") ? nbt.getFloat("discThickness") : 0.2f;
        this.maxDistance = nbt.hasKey("maxDistance") ? nbt.getFloat("maxDistance") : 35.0f;
        this.maxLifetime = nbt.hasKey("maxLifetime") ? nbt.getInteger("maxLifetime") : 200;
        this.damage = nbt.hasKey("damage") ? nbt.getFloat("damage") : 8.0f;
        this.knockback = nbt.hasKey("knockback") ? nbt.getFloat("knockback") : 1.2f;
        this.knockbackUp = nbt.hasKey("knockbackUp") ? nbt.getFloat("knockbackUp") : 0.15f;
        this.homing = !nbt.hasKey("homing") || nbt.getBoolean("homing");
        this.homingStrength = nbt.hasKey("homingStrength") ? nbt.getFloat("homingStrength") : 0.12f;
        this.homingRange = nbt.hasKey("homingRange") ? nbt.getFloat("homingRange") : 18.0f;
        this.boomerang = nbt.hasKey("boomerang") && nbt.getBoolean("boomerang");
        this.boomerangDelay = nbt.hasKey("boomerangDelay") ? nbt.getInteger("boomerangDelay") : 40;
        this.explosive = nbt.hasKey("explosive") && nbt.getBoolean("explosive");
        this.explosionRadius = nbt.hasKey("explosionRadius") ? nbt.getFloat("explosionRadius") : 3.0f;
        this.explosionDamageFalloff = nbt.hasKey("explosionDamageFalloff") ? nbt.getFloat("explosionDamageFalloff") : 0.5f;
        this.stunDuration = nbt.hasKey("stunDuration") ? nbt.getInteger("stunDuration") : 0;
        this.slowDuration = nbt.hasKey("slowDuration") ? nbt.getInteger("slowDuration") : 0;
        this.slowLevel = nbt.hasKey("slowLevel") ? nbt.getInteger("slowLevel") : 0;
        this.innerColor = nbt.hasKey("innerColor") ? nbt.getInteger("innerColor") : 0xFFFFFF;
        this.outerColor = nbt.hasKey("outerColor") ? nbt.getInteger("outerColor") : 0xFF8800;
        this.outerColorWidth = nbt.hasKey("outerColorWidth") ? nbt.getFloat("outerColorWidth") : 0.4f;
        this.outerColorAlpha = nbt.hasKey("outerColorAlpha") ? nbt.getFloat("outerColorAlpha") : 0.5f;
        this.outerColorEnabled = !nbt.hasKey("outerColorEnabled") || nbt.getBoolean("outerColorEnabled");
        this.rotationSpeed = nbt.hasKey("rotationSpeed") ? nbt.getFloat("rotationSpeed") : 5.0f;
        this.lightningEffect = nbt.hasKey("lightningEffect") && nbt.getBoolean("lightningEffect");
        this.lightningDensity = nbt.hasKey("lightningDensity") ? nbt.getFloat("lightningDensity") : 0.15f;
        this.lightningRadius = nbt.hasKey("lightningRadius") ? nbt.getFloat("lightningRadius") : 0.5f;
        this.anchorPoint = nbt.hasKey("anchorPoint") ? AnchorPoint.fromId(nbt.getInteger("anchorPoint")) : AnchorPoint.FRONT;
    }

    // Getters & Setters
    public float getSpeed() { return speed; }
    public void setSpeed(float speed) { this.speed = speed; }
    public float getDiscRadius() { return discRadius; }
    public void setDiscRadius(float discRadius) { this.discRadius = discRadius; }
    public float getDiscThickness() { return discThickness; }
    public void setDiscThickness(float discThickness) { this.discThickness = discThickness; }
    public float getMaxDistance() { return maxDistance; }
    public void setMaxDistance(float maxDistance) { this.maxDistance = maxDistance; }
    public int getMaxLifetime() { return maxLifetime; }
    public void setMaxLifetime(int maxLifetime) { this.maxLifetime = maxLifetime; }
    public float getDamage() { return damage; }
    public void setDamage(float damage) { this.damage = damage; }
    public float getKnockback() { return knockback; }
    public void setKnockback(float knockback) { this.knockback = knockback; }
    public float getKnockbackUp() { return knockbackUp; }
    public void setKnockbackUp(float knockbackUp) { this.knockbackUp = knockbackUp; }
    public boolean isHoming() { return homing; }
    public void setHoming(boolean homing) { this.homing = homing; }
    public float getHomingStrength() { return homingStrength; }
    public void setHomingStrength(float homingStrength) { this.homingStrength = homingStrength; }
    public float getHomingRange() { return homingRange; }
    public void setHomingRange(float homingRange) { this.homingRange = homingRange; }
    public boolean isBoomerang() { return boomerang; }
    public void setBoomerang(boolean boomerang) { this.boomerang = boomerang; }
    public int getBoomerangDelay() { return boomerangDelay; }
    public void setBoomerangDelay(int boomerangDelay) { this.boomerangDelay = boomerangDelay; }
    public boolean isExplosive() { return explosive; }
    public void setExplosive(boolean explosive) { this.explosive = explosive; }
    public float getExplosionRadius() { return explosionRadius; }
    public void setExplosionRadius(float explosionRadius) { this.explosionRadius = explosionRadius; }
    public float getExplosionDamageFalloff() { return explosionDamageFalloff; }
    public void setExplosionDamageFalloff(float explosionDamageFalloff) { this.explosionDamageFalloff = explosionDamageFalloff; }
    public int getStunDuration() { return stunDuration; }
    public void setStunDuration(int stunDuration) { this.stunDuration = stunDuration; }
    public int getSlowDuration() { return slowDuration; }
    public void setSlowDuration(int slowDuration) { this.slowDuration = slowDuration; }
    public int getSlowLevel() { return slowLevel; }
    public void setSlowLevel(int slowLevel) { this.slowLevel = slowLevel; }
    public int getInnerColor() { return innerColor; }
    public void setInnerColor(int innerColor) { this.innerColor = innerColor; }
    public int getOuterColor() { return outerColor; }
    public void setOuterColor(int outerColor) { this.outerColor = outerColor; }
    public float getOuterColorWidth() { return outerColorWidth; }
    public void setOuterColorWidth(float outerColorWidth) { this.outerColorWidth = outerColorWidth; }
    public float getOuterColorAlpha() { return outerColorAlpha; }
    public void setOuterColorAlpha(float outerColorAlpha) { this.outerColorAlpha = outerColorAlpha; }
    public boolean isOuterColorEnabled() { return outerColorEnabled; }
    public void setOuterColorEnabled(boolean outerColorEnabled) { this.outerColorEnabled = outerColorEnabled; }
    public float getRotationSpeed() { return rotationSpeed; }
    public void setRotationSpeed(float rotationSpeed) { this.rotationSpeed = rotationSpeed; }
    public boolean hasLightningEffect() { return lightningEffect; }
    public void setLightningEffect(boolean lightningEffect) { this.lightningEffect = lightningEffect; }
    public float getLightningDensity() { return lightningDensity; }
    public void setLightningDensity(float lightningDensity) { this.lightningDensity = lightningDensity; }
    public float getLightningRadius() { return lightningRadius; }
    public void setLightningRadius(float lightningRadius) { this.lightningRadius = lightningRadius; }
    public AnchorPoint getAnchorPointEnum() { return anchorPoint; }
    public void setAnchorPointEnum(AnchorPoint anchorPoint) { this.anchorPoint = anchorPoint; }

    @Override
    public int getAnchorPoint() { return anchorPoint.getId(); }

    @Override
    public void setAnchorPoint(int point) { this.anchorPoint = AnchorPoint.fromId(point); }
}
