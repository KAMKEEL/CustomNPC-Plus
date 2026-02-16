package kamkeel.npcs.controllers.data.ability.data;

import net.minecraft.nbt.NBTTagCompound;

import java.util.HashMap;
import java.util.Map;

/**
 * Groups barrier properties shared by energy barrier abilities (Dome, Wall, Shield).
 * Handles health/durability, duration, and per-projectile-type damage multipliers.
 */
public class EnergyBarrierData {
    public float maxHealth = 100.0f;
    public boolean useHealth = true;
    public int durationTicks = 200;
    public boolean useDuration = true;
    public float defaultMultiplier = 1.0f;

    /**
     * Per-projectile-type damage multipliers.
     * Key is the ability typeId (e.g., "ability.cnpc.orb", "ability.cnpc.beam").
     * Value is the multiplier applied to damage dealt to this barrier.
     */
    private Map<String, Float> damageMultipliers = new HashMap<>();

    public EnergyBarrierData() {}

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

    public float getMaxHealth() { return maxHealth; }
    public void setMaxHealth(float maxHealth) { this.maxHealth = Math.max(0, maxHealth); }

    public boolean isUseHealth() { return useHealth; }
    public void setUseHealth(boolean useHealth) { this.useHealth = useHealth; }

    public int getDurationTicks() { return durationTicks; }
    public void setDurationTicks(int durationTicks) { this.durationTicks = Math.max(1, durationTicks); }

    public boolean isUseDuration() { return useDuration; }
    public void setUseDuration(boolean useDuration) { this.useDuration = useDuration; }

    public float getDefaultMultiplier() { return defaultMultiplier; }
    public void setDefaultMultiplier(float defaultMultiplier) { this.defaultMultiplier = defaultMultiplier; }

    // ==================== NBT ====================

    public void writeNBT(NBTTagCompound nbt) {
        nbt.setFloat("barrierMaxHealth", maxHealth);
        nbt.setBoolean("barrierUseHealth", useHealth);
        nbt.setInteger("barrierDuration", durationTicks);
        nbt.setBoolean("barrierUseDuration", useDuration);
        nbt.setFloat("barrierDefaultMult", defaultMultiplier);

        NBTTagCompound multNbt = new NBTTagCompound();
        for (Map.Entry<String, Float> entry : damageMultipliers.entrySet()) {
            multNbt.setFloat(entry.getKey(), entry.getValue());
        }
        nbt.setTag("barrierMultipliers", multNbt);
    }

    public void readNBT(NBTTagCompound nbt) {
        maxHealth = nbt.getFloat("barrierMaxHealth");
        useHealth = nbt.getBoolean("barrierUseHealth");
        durationTicks = nbt.getInteger("barrierDuration");
        useDuration = nbt.getBoolean("barrierUseDuration");
        defaultMultiplier = nbt.hasKey("barrierDefaultMult") ? nbt.getFloat("barrierDefaultMult") : 1.0f;

        damageMultipliers.clear();
        if (nbt.hasKey("barrierMultipliers")) {
            NBTTagCompound multNbt = nbt.getCompoundTag("barrierMultipliers");
            @SuppressWarnings("unchecked")
            java.util.Set<String> keys = multNbt.func_150296_c();
            for (String key : keys) {
                damageMultipliers.put(key, multNbt.getFloat(key));
            }
        }
    }

    public EnergyBarrierData copy() {
        EnergyBarrierData copy = new EnergyBarrierData(maxHealth, useHealth, durationTicks, useDuration);
        copy.defaultMultiplier = defaultMultiplier;
        copy.damageMultipliers = new HashMap<>(damageMultipliers);
        return copy;
    }
}
