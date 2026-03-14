package kamkeel.npcs.controllers.data.ability.data.energy;

import noppes.npcs.core.NBT;
import noppes.npcs.platform.nbt.INBTCompound;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Groups barrier properties shared by energy barrier abilities (Dome, Wall, Shield).
 * Handles health/durability, duration, and per-projectile-type damage multipliers.
 *
 * Properties:
 * - Solid: Hard wall - entities cannot pass through the barrier boundary (either direction)
 * - Knockback: Repulsion field - entities near the surface get pushed away
 * - Absorbing: Caster's incoming damage is redirected to the barrier
 * - Allows Melee: Barrier can be hit by melee attacks (punching)
 */
public class EnergyBarrierData {

    public float maxHealth = 100.0f;
    public boolean useHealth = true;
    public int durationTicks = 200;
    public boolean useDuration = true;
    public float defaultMultiplier = 1.0f;

    // Solid: entities cannot pass through the barrier boundary
    public boolean solid = false;

    // Knockback: repulsion field pushing entities away from surface
    public boolean knockbackEnabled = false;
    public float knockbackStrength = 1.0f;

    // Absorbing: caster's damage is redirected to the barrier
    public boolean absorbing = false;
    // Absorb radius: max distance from barrier's current position for absorption to work
    // -1 = no radius limit (absorb regardless of distance)
    //  0 = use barrier's own extent as the radius (dome radius, panel half-size, etc.)
    // positive = owner must be within this many blocks of the barrier entity
    public float absorbRadius = 0.0f;

    // Melee: barrier can be hit by melee attacks
    public boolean meleeEnabled = false;
    public float meleeDamageMultiplier = 1.0f;

    // Reflection: rebound projectiles that fail to break the barrier
    public boolean reflect = false;
    public float reflectStrengthPct = 25.0f;
    public boolean targetOwner = false;

    /**
     * Per-projectile-type damage multipliers.
     * Key is the ability typeId (e.g., "ability.cnpc.orb", "ability.cnpc.beam").
     * Value is the multiplier applied to damage dealt to this barrier.
     */
    private Map<String, Float> damageMultipliers = new HashMap<>();

    public EnergyBarrierData() {
    }

    public EnergyBarrierData(float maxHealth, boolean useHealth, int durationTicks, boolean useDuration) {
        this.maxHealth = maxHealth;
        this.useHealth = useHealth;
        this.durationTicks = durationTicks;
        this.useDuration = useDuration;
    }

    // ==================== MULTIPLIER METHODS ====================

    public float getMultiplier(String abilityTypeId) {
        Float mult = damageMultipliers.get(abilityTypeId);
        return mult != null ? mult : defaultMultiplier;
    }

    public void setMultiplier(String abilityTypeId, float multiplier) {
        damageMultipliers.put(abilityTypeId, multiplier);
    }

    public void removeMultiplier(String abilityTypeId) {
        damageMultipliers.remove(abilityTypeId);
    }

    public Map<String, Float> getDamageMultipliers() {
        return damageMultipliers;
    }

    public void setDamageMultipliers(Map<String, Float> multipliers) {
        this.damageMultipliers = multipliers != null ? multipliers : new HashMap<>();
    }

    // ==================== GETTERS & SETTERS ====================

    public float getMaxHealth() {
        return maxHealth;
    }

    public void setMaxHealth(float maxHealth) {
        this.maxHealth = Math.max(1.0f, maxHealth);
    }

    public boolean isUseHealth() {
        return useHealth;
    }

    public void setUseHealth(boolean useHealth) {
        this.useHealth = useHealth;
    }

    public int getDurationTicks() {
        return durationTicks;
    }

    public void setDurationTicks(int durationTicks) {
        this.durationTicks = Math.max(1, durationTicks);
    }

    public boolean isUseDuration() {
        return useDuration;
    }

    public void setUseDuration(boolean useDuration) {
        this.useDuration = useDuration;
    }

    public float getDefaultMultiplier() {
        return defaultMultiplier;
    }

    public void setDefaultMultiplier(float defaultMultiplier) {
        this.defaultMultiplier = defaultMultiplier;
    }

    public boolean isReflect() {
        return reflect;
    }

    public void setReflect(boolean reflect) {
        this.reflect = reflect;
    }

    public float getReflectStrengthPct() {
        return reflectStrengthPct;
    }

    public void setReflectStrengthPct(float reflectStrengthPct) {
        this.reflectStrengthPct = Math.max(0.0f, Math.min(100.0f, reflectStrengthPct));
    }

    public boolean isTargetOwner() {
        return targetOwner;
    }

    public void setTargetOwner(boolean targetOwner) {
        this.targetOwner = targetOwner;
    }

    public float getAbsorbRadius() {
        return absorbRadius;
    }

    public void setAbsorbRadius(float absorbRadius) {
        this.absorbRadius = absorbRadius;
    }

    // ==================== NBT ====================

    public void writeNBT(INBTCompound nbt) {
        nbt.setFloat("barrierMaxHealth", maxHealth);
        nbt.setBoolean("barrierUseHealth", useHealth);
        nbt.setInteger("barrierDuration", durationTicks);
        nbt.setBoolean("barrierUseDuration", useDuration);
        nbt.setFloat("barrierDefaultMult", defaultMultiplier);
        nbt.setBoolean("barrierSolid", solid);
        nbt.setBoolean("barrierKnockback", knockbackEnabled);
        nbt.setFloat("barrierKnockbackStr", knockbackStrength);
        nbt.setBoolean("barrierAbsorbing", absorbing);
        nbt.setFloat("barrierAbsorbRadius", absorbRadius);
        nbt.setBoolean("barrierMelee", meleeEnabled);
        nbt.setFloat("barrierMeleeMult", meleeDamageMultiplier);
        nbt.setBoolean("barrierReflect", reflect);
        nbt.setFloat("barrierReflectStrPct", reflectStrengthPct);
        nbt.setBoolean("barrierTargetOwner", targetOwner);

        INBTCompound multNbt = NBT.compound();
        for (Map.Entry<String, Float> entry : damageMultipliers.entrySet()) {
            multNbt.setFloat(entry.getKey(), entry.getValue());
        }
        nbt.setCompound("barrierMultipliers", multNbt);
    }

    public void readNBT(INBTCompound nbt) {
        maxHealth = nbt.hasKey("barrierMaxHealth") ? nbt.getFloat("barrierMaxHealth") : 100.0f;
        useHealth = !nbt.hasKey("barrierUseHealth") || nbt.getBoolean("barrierUseHealth");
        durationTicks = nbt.hasKey("barrierDuration") ? nbt.getInteger("barrierDuration") : 200;
        useDuration = !nbt.hasKey("barrierUseDuration") || nbt.getBoolean("barrierUseDuration");
        defaultMultiplier = nbt.hasKey("barrierDefaultMult") ? nbt.getFloat("barrierDefaultMult") : 1.0f;
        solid = nbt.hasKey("barrierSolid") && nbt.getBoolean("barrierSolid");
        knockbackEnabled = nbt.hasKey("barrierKnockback") && nbt.getBoolean("barrierKnockback");
        knockbackStrength = nbt.hasKey("barrierKnockbackStr") ? nbt.getFloat("barrierKnockbackStr") : 1.0f;
        absorbing = nbt.hasKey("barrierAbsorbing") && nbt.getBoolean("barrierAbsorbing");
        absorbRadius = nbt.hasKey("barrierAbsorbRadius") ? nbt.getFloat("barrierAbsorbRadius") : 0.0f;
        meleeEnabled = nbt.hasKey("barrierMelee") && nbt.getBoolean("barrierMelee");
        meleeDamageMultiplier = nbt.hasKey("barrierMeleeMult") ? nbt.getFloat("barrierMeleeMult") : 1.0f;
        reflect = nbt.hasKey("barrierReflect") && nbt.getBoolean("barrierReflect");
        setReflectStrengthPct(nbt.hasKey("barrierReflectStrPct") ? nbt.getFloat("barrierReflectStrPct") : 25.0f);
        targetOwner = nbt.hasKey("barrierTargetOwner") && nbt.getBoolean("barrierTargetOwner");

        damageMultipliers.clear();
        if (nbt.hasKey("barrierMultipliers")) {
            INBTCompound multNbt = nbt.getCompound("barrierMultipliers");
            Set<String> keys = multNbt.getKeySet();
            for (String key : keys) {
                damageMultipliers.put(key, multNbt.getFloat(key));
            }
        }
    }

    public EnergyBarrierData copy() {
        EnergyBarrierData copy = new EnergyBarrierData(maxHealth, useHealth, durationTicks, useDuration);
        copy.defaultMultiplier = defaultMultiplier;
        copy.solid = solid;
        copy.knockbackEnabled = knockbackEnabled;
        copy.knockbackStrength = knockbackStrength;
        copy.absorbing = absorbing;
        copy.absorbRadius = absorbRadius;
        copy.meleeEnabled = meleeEnabled;
        copy.meleeDamageMultiplier = meleeDamageMultiplier;
        copy.reflect = reflect;
        copy.reflectStrengthPct = reflectStrengthPct;
        copy.targetOwner = targetOwner;
        copy.damageMultipliers = new HashMap<>(damageMultipliers);
        return copy;
    }
}
