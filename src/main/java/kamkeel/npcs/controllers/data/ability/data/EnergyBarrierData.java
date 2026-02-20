package kamkeel.npcs.controllers.data.ability.data;

import net.minecraft.nbt.NBTTagCompound;

import java.util.HashMap;
import java.util.Map;

/**
 * Groups barrier properties shared by energy barrier abilities (Dome, Wall, Shield).
 * Handles health/durability, duration, and per-projectile-type damage multipliers.
 */
public class EnergyBarrierData {
    // Knockback target display keys
    private static final String[] KNOCKBACK_TARGET_KEYS = {"gui.both", "gui.players", "gui.npcs"};

    public float maxHealth = 100.0f;
    public boolean useHealth = true;
    public int durationTicks = 200;
    public boolean useDuration = true;
    public float defaultMultiplier = 1.0f;

    // Knockback settings
    public boolean knockbackEnabled = false;
    public float knockbackStrength = 1.0f;
    /**
     * 0=Both, 1=Player only, 2=NPC only
     */
    public int knockbackTarget = 0;

    // Melee settings
    public boolean meleeEnabled = false;
    public float meleeDamageMultiplier = 1.0f;

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

    // Knockback target key helpers
    public static String[] getKnockbackTargetKeys() {
        return KNOCKBACK_TARGET_KEYS;
    }

    public String getKnockbackTargetKey() {
        return KNOCKBACK_TARGET_KEYS[Math.max(0, Math.min(knockbackTarget, 2))];
    }

    public void setKnockbackTargetFromKey(String key) {
        for (int i = 0; i < KNOCKBACK_TARGET_KEYS.length; i++) {
            if (KNOCKBACK_TARGET_KEYS[i].equals(key)) {
                knockbackTarget = i;
                return;
            }
        }
    }

    // ==================== NBT ====================

    public void writeNBT(NBTTagCompound nbt) {
        nbt.setFloat("barrierMaxHealth", maxHealth);
        nbt.setBoolean("barrierUseHealth", useHealth);
        nbt.setInteger("barrierDuration", durationTicks);
        nbt.setBoolean("barrierUseDuration", useDuration);
        nbt.setFloat("barrierDefaultMult", defaultMultiplier);
        nbt.setBoolean("barrierKnockback", knockbackEnabled);
        nbt.setFloat("barrierKnockbackStr", knockbackStrength);
        nbt.setInteger("barrierKnockbackTarget", knockbackTarget);
        nbt.setBoolean("barrierMelee", meleeEnabled);
        nbt.setFloat("barrierMeleeMult", meleeDamageMultiplier);

        NBTTagCompound multNbt = new NBTTagCompound();
        for (Map.Entry<String, Float> entry : damageMultipliers.entrySet()) {
            multNbt.setFloat(entry.getKey(), entry.getValue());
        }
        nbt.setTag("barrierMultipliers", multNbt);
    }

    public void readNBT(NBTTagCompound nbt) {
        maxHealth = nbt.hasKey("barrierMaxHealth") ? nbt.getFloat("barrierMaxHealth") : 100.0f;
        useHealth = !nbt.hasKey("barrierUseHealth") || nbt.getBoolean("barrierUseHealth");
        durationTicks = nbt.hasKey("barrierDuration") ? nbt.getInteger("barrierDuration") : 200;
        useDuration = !nbt.hasKey("barrierUseDuration") || nbt.getBoolean("barrierUseDuration");
        defaultMultiplier = nbt.hasKey("barrierDefaultMult") ? nbt.getFloat("barrierDefaultMult") : 1.0f;
        knockbackEnabled = nbt.hasKey("barrierKnockback") && nbt.getBoolean("barrierKnockback");
        knockbackStrength = nbt.hasKey("barrierKnockbackStr") ? nbt.getFloat("barrierKnockbackStr") : 1.0f;
        knockbackTarget = nbt.hasKey("barrierKnockbackTarget") ? nbt.getInteger("barrierKnockbackTarget") : 0;
        meleeEnabled = nbt.hasKey("barrierMelee") && nbt.getBoolean("barrierMelee");
        meleeDamageMultiplier = nbt.hasKey("barrierMeleeMult") ? nbt.getFloat("barrierMeleeMult") : 1.0f;

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
        copy.knockbackEnabled = knockbackEnabled;
        copy.knockbackStrength = knockbackStrength;
        copy.knockbackTarget = knockbackTarget;
        copy.meleeEnabled = meleeEnabled;
        copy.meleeDamageMultiplier = meleeDamageMultiplier;
        copy.damageMultipliers = new HashMap<>(damageMultipliers);
        return copy;
    }
}
