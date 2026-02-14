package kamkeel.npcs.controllers.data.ability.data;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.api.ability.data.IEnergyCombatData;

/**
 * Groups combat properties shared by energy projectile abilities.
 * Used as a parameter object for entity constructors and ability configuration.
 */
public class EnergyCombatData implements IEnergyCombatData {
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

    @Override
    public float getDamage() {
        return damage;
    }

    @Override
    public void setDamage(float damage) {
        this.damage = damage;
    }

    @Override
    public float getKnockback() {
        return knockback;
    }

    @Override
    public void setKnockback(float knockback) {
        this.knockback = knockback;
    }

    @Override
    public float getKnockbackUp() {
        return knockbackUp;
    }

    @Override
    public void setKnockbackUp(float knockbackUp) {
        this.knockbackUp = knockbackUp;
    }

    @Override
    public boolean isExplosive() {
        return explosive;
    }

    @Override
    public void setExplosive(boolean explosive) {
        this.explosive = explosive;
    }

    @Override
    public float getExplosionRadius() {
        return explosionRadius;
    }

    @Override
    public void setExplosionRadius(float explosionRadius) {
        this.explosionRadius = explosionRadius;
    }

    @Override
    public float getExplosionDamageFalloff() {
        return explosionDamageFalloff;
    }

    @Override
    public void setExplosionDamageFalloff(float explosionDamageFalloff) {
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
        damage = nbt.getFloat("damage");
        knockback = nbt.getFloat("knockback");
        knockbackUp = nbt.getFloat("knockbackUp");
        explosive = nbt.getBoolean("explosive");
        explosionRadius = nbt.getFloat("explosionRadius");
        explosionDamageFalloff = nbt.getFloat("explosionDamageFalloff");
    }

    public EnergyCombatData copy() {
        return new EnergyCombatData(damage, knockback, knockbackUp,
            explosive, explosionRadius, explosionDamageFalloff);
    }
}
