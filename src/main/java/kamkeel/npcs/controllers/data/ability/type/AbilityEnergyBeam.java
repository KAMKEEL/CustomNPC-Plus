package kamkeel.npcs.controllers.data.ability.type;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.TargetingMode;
import kamkeel.npcs.controllers.data.telegraph.TelegraphType;
import kamkeel.npcs.entity.EntityAbilityBeam;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import noppes.npcs.client.gui.advanced.SubGuiAbilityConfig;
import noppes.npcs.client.gui.advanced.ability.SubGuiAbilityEnergyBeam;
import noppes.npcs.client.gui.util.IAbilityConfigCallback;
import noppes.npcs.entity.EntityNPCInterface;

/**
 * Energy Beam ability: A homing head with a trailing path.
 * The beam stays attached to the origin and curves as it homes in on target.
 * Cannot cross over itself (self-intersection prevention).
 */
public class AbilityEnergyBeam extends Ability {

    // Movement properties
    private float speed = 0.4f;
    private float maxDistance = 25.0f;
    private int maxLifetime = 200;

    // Shape properties
    private float beamWidth = 0.4f;
    private float headSize = 0.6f;

    // Combat properties
    private float damage = 10.0f;
    private float knockback = 1.5f;
    private float knockbackUp = 0.2f;

    // Homing properties
    private boolean homing = true;
    private float homingStrength = 0.1f;
    private float homingRange = 15.0f;

    // Explosion properties
    private boolean explosive = false;
    private float explosionRadius = 4.0f;
    private float explosionDamageFalloff = 0.5f;

    // Effect properties
    private int stunDuration = 0;
    private int slowDuration = 0;
    private int slowLevel = 0;

    // Visual properties
    private int innerColor = 0xFFFFFF;
    private int outerColor = 0x00AAFF;
    private float outerColorWidth = 1.8f;
    private boolean outerColorEnabled = true;
    private float rotationSpeed = 6.0f;

    public AbilityEnergyBeam() {
        this.typeId = "ability.cnpc.beam";
        this.name = "Beam";
        this.targetingMode = TargetingMode.AGGRO_TARGET;
        this.maxRange = 20.0f;
        this.minRange = 5.0f;
        this.cooldownTicks = 150;
        this.windUpTicks = 40;
        this.activeTicks = 200;  // Match maxLifetime to keep NPC locked while beam is active
        this.recoveryTicks = 5;
        this.telegraphType = TelegraphType.POINT;
        this.showTelegraph = false;
    }

    @Override
    public boolean hasTypeSettings() {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public SubGuiAbilityConfig createConfigGui(IAbilityConfigCallback callback) {
        return new SubGuiAbilityEnergyBeam(this, callback);
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
        if (world.isRemote) return;

        double spawnX = npc.posX;
        double spawnY = npc.posY + npc.getEyeHeight() * 0.7;
        double spawnZ = npc.posZ;

        EntityAbilityBeam beam = new EntityAbilityBeam(
            world, npc, target,
            spawnX, spawnY, spawnZ,
            beamWidth, headSize, innerColor, outerColor, rotationSpeed,
            damage, knockback, knockbackUp,
            speed, homing, homingStrength, homingRange,
            explosive, explosionRadius, explosionDamageFalloff,
            stunDuration, slowDuration, slowLevel,
            maxDistance, maxLifetime
        );

        world.spawnEntityInWorld(beam);
    }

    @Override
    public void onActiveTick(EntityNPCInterface npc, EntityLivingBase target, World world, int tick) {
    }

    @Override
    public void onComplete(EntityNPCInterface npc, EntityLivingBase target) {
    }

    @Override
    public void onInterrupt(EntityNPCInterface npc, DamageSource source, float damage) {
    }

    @Override
    public void writeTypeNBT(NBTTagCompound nbt) {
        nbt.setFloat("speed", speed);
        nbt.setFloat("maxDistance", maxDistance);
        nbt.setInteger("maxLifetime", maxLifetime);
        nbt.setFloat("beamWidth", beamWidth);
        nbt.setFloat("headSize", headSize);
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
    }

    @Override
    public void readTypeNBT(NBTTagCompound nbt) {
        this.speed = nbt.hasKey("speed") ? nbt.getFloat("speed") : 0.4f;
        this.maxDistance = nbt.hasKey("maxDistance") ? nbt.getFloat("maxDistance") : 25.0f;
        this.maxLifetime = nbt.hasKey("maxLifetime") ? nbt.getInteger("maxLifetime") : 200;
        this.beamWidth = nbt.hasKey("beamWidth") ? nbt.getFloat("beamWidth") : 0.4f;
        this.headSize = nbt.hasKey("headSize") ? nbt.getFloat("headSize") : 0.6f;
        this.damage = nbt.hasKey("damage") ? nbt.getFloat("damage") : 10.0f;
        this.knockback = nbt.hasKey("knockback") ? nbt.getFloat("knockback") : 1.5f;
        this.knockbackUp = nbt.hasKey("knockbackUp") ? nbt.getFloat("knockbackUp") : 0.2f;
        this.homing = !nbt.hasKey("homing") || nbt.getBoolean("homing");
        this.homingStrength = nbt.hasKey("homingStrength") ? nbt.getFloat("homingStrength") : 0.1f;
        this.homingRange = nbt.hasKey("homingRange") ? nbt.getFloat("homingRange") : 15.0f;
        this.explosive = nbt.hasKey("explosive") && nbt.getBoolean("explosive");
        this.explosionRadius = nbt.hasKey("explosionRadius") ? nbt.getFloat("explosionRadius") : 4.0f;
        this.explosionDamageFalloff = nbt.hasKey("explosionDamageFalloff") ? nbt.getFloat("explosionDamageFalloff") : 0.5f;
        this.stunDuration = nbt.hasKey("stunDuration") ? nbt.getInteger("stunDuration") : 0;
        this.slowDuration = nbt.hasKey("slowDuration") ? nbt.getInteger("slowDuration") : 0;
        this.slowLevel = nbt.hasKey("slowLevel") ? nbt.getInteger("slowLevel") : 0;
        this.innerColor = nbt.hasKey("innerColor") ? nbt.getInteger("innerColor") : 0xFFFFFF;
        this.outerColor = nbt.hasKey("outerColor") ? nbt.getInteger("outerColor") : 0x00AAFF;
        this.outerColorWidth = nbt.hasKey("outerColorWidth") ? nbt.getFloat("outerColorWidth") : 1.8f;
        this.outerColorEnabled = !nbt.hasKey("outerColorEnabled") || nbt.getBoolean("outerColorEnabled");
        this.rotationSpeed = nbt.hasKey("rotationSpeed") ? nbt.getFloat("rotationSpeed") : 6.0f;
    }

    // Getters & Setters
    public float getSpeed() { return speed; }
    public void setSpeed(float speed) { this.speed = speed; }
    public float getMaxDistance() { return maxDistance; }
    public void setMaxDistance(float maxDistance) { this.maxDistance = maxDistance; }
    public int getMaxLifetime() { return maxLifetime; }
    public void setMaxLifetime(int maxLifetime) { this.maxLifetime = maxLifetime; }
    public float getBeamWidth() { return beamWidth; }
    public void setBeamWidth(float beamWidth) { this.beamWidth = beamWidth; }
    public float getHeadSize() { return headSize; }
    public void setHeadSize(float headSize) { this.headSize = headSize; }
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
    public float getRotationSpeed() { return rotationSpeed; }
    public void setRotationSpeed(float rotationSpeed) { this.rotationSpeed = rotationSpeed; }
}
