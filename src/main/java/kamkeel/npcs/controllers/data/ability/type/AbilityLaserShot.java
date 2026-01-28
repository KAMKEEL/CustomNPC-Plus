package kamkeel.npcs.controllers.data.ability.type;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.TargetingMode;
import kamkeel.npcs.controllers.data.telegraph.TelegraphType;
import kamkeel.npcs.entity.EntityAbilityLaser;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import noppes.npcs.client.gui.advanced.SubGuiAbilityConfig;
import noppes.npcs.client.gui.advanced.ability.SubGuiAbilityLaserShot;
import noppes.npcs.client.gui.util.IAbilityConfigCallback;
import noppes.npcs.api.ability.type.IAbilityLaserShot;
import noppes.npcs.entity.EntityNPCInterface;

/**
 * Laser Shot ability: Fast expanding thin line that pierces through targets.
 * Travels in a straight line from origin, damaging all entities it passes through.
 */
public class AbilityLaserShot extends Ability implements IAbilityLaserShot {

    // Laser properties
    private float laserWidth = 0.3f;
    private float expansionSpeed = 3.0f;
    private int lingerTicks = 8;
    private float maxDistance = 40.0f;
    private int maxLifetime = 100;

    // Combat properties
    private float damage = 6.0f;
    private float knockback = 0.5f;
    private float knockbackUp = 0.05f;

    // Explosion properties
    private boolean explosive = false;
    private float explosionRadius = 2.0f;
    private float explosionDamageFalloff = 0.5f;

    // Effect properties
    private int stunDuration = 0;
    private int slowDuration = 0;
    private int slowLevel = 0;

    // Visual properties
    private int innerColor = 0xFFFFFF;
    private int outerColor = 0xFF0000;
    private float outerColorWidth = 0.4f; // Additive offset from inner width
    private boolean outerColorEnabled = true;

    // Lightning effect properties
    private boolean lightningEffect = false;
    private float lightningDensity = 0.15f;
    private float lightningRadius = 0.5f;

    public AbilityLaserShot() {
        this.typeId = "ability.cnpc.laser_shot";
        this.name = "Laser Shot";
        this.targetingMode = TargetingMode.AGGRO_TARGET;
        this.maxRange = 35.0f;
        this.minRange = 3.0f;
        this.cooldownTicks = 0;
        this.windUpTicks = 15;
        this.telegraphType = TelegraphType.LINE;
        this.showTelegraph = true;
    }

    @Override
    public boolean hasTypeSettings() {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public SubGuiAbilityConfig createConfigGui(IAbilityConfigCallback callback) {
        return new SubGuiAbilityLaserShot(this, callback);
    }

    @Override
    public boolean isTargetingModeLocked() {
        return false;
    }

    @Override
    public TargetingMode[] getAllowedTargetingModes() {
        return new TargetingMode[]{TargetingMode.AGGRO_TARGET};
    }

    @Override
    public float getTelegraphLength() {
        return maxDistance;
    }

    @Override
    public float getTelegraphWidth() {
        return laserWidth * 2.0f; // Make telegraph slightly wider for visibility
    }

    @Override
    public void onExecute(EntityNPCInterface npc, EntityLivingBase target, World world) {
        if (world.isRemote) {
            signalCompletion();
            return;
        }

        double spawnX = npc.posX;
        double spawnY = npc.posY + npc.getEyeHeight() * 0.8;
        double spawnZ = npc.posZ;

        EntityAbilityLaser laser = new EntityAbilityLaser(
            world, npc, target,
            spawnX, spawnY, spawnZ,
            laserWidth, innerColor, outerColor, outerColorEnabled, outerColorWidth,
            damage, knockback, knockbackUp,
            expansionSpeed, lingerTicks,
            explosive, explosionRadius, explosionDamageFalloff,
            stunDuration, slowDuration, slowLevel,
            maxDistance, maxLifetime,
            lightningEffect, lightningDensity, lightningRadius
        );

        world.spawnEntityInWorld(laser);

        // Laser entity manages itself - ability is done
        signalCompletion();
    }

    @Override
    public void onActiveTick(EntityNPCInterface npc, EntityLivingBase target, World world, int tick) {
        // Laser entity manages itself
    }

    @Override
    public void onComplete(EntityNPCInterface npc, EntityLivingBase target) {
    }

    @Override
    public void onInterrupt(EntityNPCInterface npc, DamageSource source, float damage) {
    }

    @Override
    public void writeTypeNBT(NBTTagCompound nbt) {
        nbt.setFloat("laserWidth", laserWidth);
        nbt.setFloat("expansionSpeed", expansionSpeed);
        nbt.setInteger("lingerTicks", lingerTicks);
        nbt.setFloat("maxDistance", maxDistance);
        nbt.setInteger("maxLifetime", maxLifetime);
        nbt.setFloat("damage", damage);
        nbt.setFloat("knockback", knockback);
        nbt.setFloat("knockbackUp", knockbackUp);
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
        nbt.setBoolean("lightningEffect", lightningEffect);
        nbt.setFloat("lightningDensity", lightningDensity);
        nbt.setFloat("lightningRadius", lightningRadius);
    }

    @Override
    public void readTypeNBT(NBTTagCompound nbt) {
        this.laserWidth = nbt.hasKey("laserWidth") ? nbt.getFloat("laserWidth") : 0.3f;
        this.expansionSpeed = nbt.hasKey("expansionSpeed") ? nbt.getFloat("expansionSpeed") : 3.0f;
        this.lingerTicks = nbt.hasKey("lingerTicks") ? nbt.getInteger("lingerTicks") : 8;
        this.maxDistance = nbt.hasKey("maxDistance") ? nbt.getFloat("maxDistance") : 40.0f;
        this.maxLifetime = nbt.hasKey("maxLifetime") ? nbt.getInteger("maxLifetime") : 100;
        this.damage = nbt.hasKey("damage") ? nbt.getFloat("damage") : 6.0f;
        this.knockback = nbt.hasKey("knockback") ? nbt.getFloat("knockback") : 0.5f;
        this.knockbackUp = nbt.hasKey("knockbackUp") ? nbt.getFloat("knockbackUp") : 0.05f;
        this.explosive = nbt.hasKey("explosive") && nbt.getBoolean("explosive");
        this.explosionRadius = nbt.hasKey("explosionRadius") ? nbt.getFloat("explosionRadius") : 2.0f;
        this.explosionDamageFalloff = nbt.hasKey("explosionDamageFalloff") ? nbt.getFloat("explosionDamageFalloff") : 0.5f;
        this.stunDuration = nbt.hasKey("stunDuration") ? nbt.getInteger("stunDuration") : 0;
        this.slowDuration = nbt.hasKey("slowDuration") ? nbt.getInteger("slowDuration") : 0;
        this.slowLevel = nbt.hasKey("slowLevel") ? nbt.getInteger("slowLevel") : 0;
        this.innerColor = nbt.hasKey("innerColor") ? nbt.getInteger("innerColor") : 0xFFFFFF;
        this.outerColor = nbt.hasKey("outerColor") ? nbt.getInteger("outerColor") : 0xFF0000;
        this.outerColorWidth = nbt.hasKey("outerColorWidth") ? nbt.getFloat("outerColorWidth") : 0.4f;
        this.outerColorEnabled = !nbt.hasKey("outerColorEnabled") || nbt.getBoolean("outerColorEnabled");
        this.lightningEffect = nbt.hasKey("lightningEffect") && nbt.getBoolean("lightningEffect");
        this.lightningDensity = nbt.hasKey("lightningDensity") ? nbt.getFloat("lightningDensity") : 0.15f;
        this.lightningRadius = nbt.hasKey("lightningRadius") ? nbt.getFloat("lightningRadius") : 0.5f;
    }

    // Getters & Setters
    public float getLaserWidth() { return laserWidth; }
    public void setLaserWidth(float laserWidth) { this.laserWidth = laserWidth; }
    public float getExpansionSpeed() { return expansionSpeed; }
    public void setExpansionSpeed(float expansionSpeed) { this.expansionSpeed = expansionSpeed; }
    public int getLingerTicks() { return lingerTicks; }
    public void setLingerTicks(int lingerTicks) { this.lingerTicks = lingerTicks; }
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
    public boolean isOuterColorEnabled() { return outerColorEnabled; }
    public void setOuterColorEnabled(boolean outerColorEnabled) { this.outerColorEnabled = outerColorEnabled; }
    public boolean hasLightningEffect() { return lightningEffect; }
    public void setLightningEffect(boolean lightningEffect) { this.lightningEffect = lightningEffect; }
    public float getLightningDensity() { return lightningDensity; }
    public void setLightningDensity(float lightningDensity) { this.lightningDensity = lightningDensity; }
    public float getLightningRadius() { return lightningRadius; }
    public void setLightningRadius(float lightningRadius) { this.lightningRadius = lightningRadius; }
}
