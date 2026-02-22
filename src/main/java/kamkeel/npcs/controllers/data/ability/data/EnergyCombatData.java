package kamkeel.npcs.controllers.data.ability.data;

import kamkeel.npcs.controllers.data.ability.HitType;
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
    public HitType hitType = HitType.SINGLE;
    public int multiHitDelayTicks = 5;

    public EnergyCombatData() {
    }

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
        this.explosionRadius = Math.max(0, explosionRadius);
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
        nbt.setInteger("hitType", hitType.ordinal());
        nbt.setInteger("multiHitDelayTicks", multiHitDelayTicks);
    }

    public void readNBT(NBTTagCompound nbt) {
        damage = nbt.hasKey("damage") ? nbt.getFloat("damage") : 7.0f;
        knockback = nbt.hasKey("knockback") ? nbt.getFloat("knockback") : 1.0f;
        knockbackUp = nbt.hasKey("knockbackUp") ? nbt.getFloat("knockbackUp") : 0.1f;
        explosive = nbt.hasKey("explosive") && nbt.getBoolean("explosive");
        explosionRadius = nbt.hasKey("explosionRadius") ? nbt.getFloat("explosionRadius") : 3.0f;
        explosionDamageFalloff = nbt.hasKey("explosionDamageFalloff") ? nbt.getFloat("explosionDamageFalloff") : 0.5f;
        hitType = HitType.fromOrdinal(nbt.hasKey("hitType") ? nbt.getInteger("hitType") : 0);
        multiHitDelayTicks = nbt.hasKey("multiHitDelayTicks") ? nbt.getInteger("multiHitDelayTicks") : 5;

        // Sanitize
        if (Float.isNaN(damage) || Float.isInfinite(damage)) damage = 7.0f;
        if (Float.isNaN(knockback) || Float.isInfinite(knockback) || knockback < 0) knockback = 1.0f;
        if (Float.isNaN(explosionRadius) || Float.isInfinite(explosionRadius) || explosionRadius < 0) explosionRadius = 3.0f;
        if (multiHitDelayTicks < 1) multiHitDelayTicks = 1;
    }

    public EnergyCombatData copy() {
        EnergyCombatData copy = new EnergyCombatData(damage, knockback, knockbackUp,
            explosive, explosionRadius, explosionDamageFalloff);
        copy.hitType = hitType;
        copy.multiHitDelayTicks = multiHitDelayTicks;
        return copy;
    }
}
