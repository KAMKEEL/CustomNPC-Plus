package kamkeel.npcs.controllers.data.ability.data;

import net.minecraft.nbt.NBTTagCompound;

/**
 * Groups combat properties shared by energy projectile abilities.
 * Used as a parameter object for entity constructors and ability configuration.
 */
public class EnergyCombatData {
    public float damage = 7.0f;
    public float knockback = 1.0f;
    public float knockbackUp = 0.1f;
    public boolean explosive = false;
    public float explosionRadius = 3.0f;
    public float explosionDamageFalloff = 0.5f;

    public EnergyCombatData() {}

    public EnergyCombatData(float damage, float knockback, float knockbackUp,
                            boolean explosive, float explosionRadius, float explosionDamageFalloff) {
        this.damage = damage;
        this.knockback = knockback;
        this.knockbackUp = knockbackUp;
        this.explosive = explosive;
        this.explosionRadius = explosionRadius;
        this.explosionDamageFalloff = explosionDamageFalloff;
    }

    public void writeNBT(NBTTagCompound nbt) {
        nbt.setFloat("damage", damage);
        nbt.setFloat("knockback", knockback);
        nbt.setFloat("knockbackUp", knockbackUp);
        nbt.setBoolean("explosive", explosive);
        nbt.setFloat("explosionRadius", explosionRadius);
        nbt.setFloat("explosionDamageFalloff", explosionDamageFalloff);
    }

    public void readNBT(NBTTagCompound nbt) {
        damage = nbt.hasKey("damage") ? nbt.getFloat("damage") : 7.0f;
        knockback = nbt.hasKey("knockback") ? nbt.getFloat("knockback") : 1.0f;
        knockbackUp = nbt.hasKey("knockbackUp") ? nbt.getFloat("knockbackUp") : 0.1f;
        explosive = nbt.hasKey("explosive") && nbt.getBoolean("explosive");
        explosionRadius = nbt.hasKey("explosionRadius") ? nbt.getFloat("explosionRadius") : 3.0f;
        explosionDamageFalloff = nbt.hasKey("explosionDamageFalloff") ? nbt.getFloat("explosionDamageFalloff") : 0.5f;
    }

    public EnergyCombatData copy() {
        return new EnergyCombatData(damage, knockback, knockbackUp,
            explosive, explosionRadius, explosionDamageFalloff);
    }
}
