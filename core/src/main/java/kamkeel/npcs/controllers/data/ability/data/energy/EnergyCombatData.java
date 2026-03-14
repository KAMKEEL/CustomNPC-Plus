package kamkeel.npcs.controllers.data.ability.data.energy;

import kamkeel.npcs.controllers.data.ability.enums.HitType;
import noppes.npcs.api.ability.data.IEnergyCombatData;
import noppes.npcs.platform.nbt.INBTCompound;

/**
 * Groups combat properties shared by energy projectile abilities.
 * Used as a parameter object for entity constructors and ability configuration.
 */
public class EnergyCombatData implements IEnergyCombatData {
    public static final float MAX_EXPLOSION_RADIUS = 15.0f;
    public static final int DEFAULT_MAX_HITS = 5;
    public static final int MAX_HITS = 200;

    public float damage = 7.0f;
    public float knockback = 1.0f;
    public float knockbackUp = 0.1f;
    public boolean explosive = false;
    public float explosionRadius = 3.0f;
    public float explosionDamageFalloff = 0.5f;
    public HitType hitType = HitType.SINGLE;
    public int multiHitDelayTicks = 5;
    public int maxHits = DEFAULT_MAX_HITS;

    public EnergyCombatData() {
    }

    public EnergyCombatData(float damage, float knockback, float knockbackUp,
                            boolean explosive, float explosionRadius, float explosionDamageFalloff,
                            HitType hitType, int multiHitDelay) {
        this(damage, knockback, knockbackUp, explosive, explosionRadius, explosionDamageFalloff, hitType, multiHitDelay, DEFAULT_MAX_HITS);
    }

    public EnergyCombatData(float damage, float knockback, float knockbackUp,
                            boolean explosive, float explosionRadius, float explosionDamageFalloff,
                            HitType hitType, int multiHitDelay, int maxHits) {
        this.damage = damage;
        this.knockback = knockback;
        this.knockbackUp = knockbackUp;
        this.explosive = explosive;
        this.explosionRadius = clampExplosionRadius(explosionRadius);
        this.explosionDamageFalloff = explosionDamageFalloff;
        this.hitType = hitType;
        this.multiHitDelayTicks = multiHitDelay;
        this.maxHits = clampMaxHits(maxHits);
    }

    public EnergyCombatData(float damage, float knockback, float knockbackUp,
                            boolean explosive, float explosionRadius, float explosionDamageFalloff) {
        this.damage = damage;
        this.knockback = knockback;
        this.knockbackUp = knockbackUp;
        this.explosive = explosive;
        this.explosionRadius = clampExplosionRadius(explosionRadius);
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
        this.explosionRadius = clampExplosionRadius(explosionRadius);
    }

    @Override
    public float getExplosionDamageFalloff() {
        return explosionDamageFalloff;
    }

    @Override
    public void setExplosionDamageFalloff(float explosionDamageFalloff) {
        this.explosionDamageFalloff = explosionDamageFalloff;
    }

    public int getMaxHits() {
        return maxHits;
    }

    public void setMaxHits(int maxHits) {
        this.maxHits = clampMaxHits(maxHits);
    }

    public void writeNBT(INBTCompound nbt) {
        nbt.setFloat("damage", damage);
        nbt.setFloat("knockback", knockback);
        nbt.setFloat("knockbackUp", knockbackUp);
        nbt.setBoolean("explosive", explosive);
        nbt.setFloat("explosionRadius", explosionRadius);
        nbt.setFloat("explosionDamageFalloff", explosionDamageFalloff);
        nbt.setInteger("hitType", hitType.ordinal());
        nbt.setInteger("multiHitDelayTicks", multiHitDelayTicks);
        nbt.setInteger("maxHits", maxHits);
    }

    public void readNBT(INBTCompound nbt) {
        damage = nbt.hasKey("damage") ? nbt.getFloat("damage") : 7.0f;
        knockback = nbt.hasKey("knockback") ? nbt.getFloat("knockback") : 1.0f;
        knockbackUp = nbt.hasKey("knockbackUp") ? nbt.getFloat("knockbackUp") : 0.1f;
        explosive = nbt.hasKey("explosive") && nbt.getBoolean("explosive");
        explosionRadius = nbt.hasKey("explosionRadius") ? nbt.getFloat("explosionRadius") : 3.0f;
        explosionDamageFalloff = nbt.hasKey("explosionDamageFalloff") ? nbt.getFloat("explosionDamageFalloff") : 0.5f;
        hitType = HitType.fromOrdinal(nbt.hasKey("hitType") ? nbt.getInteger("hitType") : 0);
        multiHitDelayTicks = nbt.hasKey("multiHitDelayTicks") ? nbt.getInteger("multiHitDelayTicks") : 5;
        maxHits = nbt.hasKey("maxHits") ? nbt.getInteger("maxHits") : DEFAULT_MAX_HITS;

        // Sanitize
        if (Float.isNaN(damage) || Float.isInfinite(damage)) damage = 7.0f;
        if (Float.isNaN(knockback) || Float.isInfinite(knockback) || knockback < 0) knockback = 1.0f;
        if (Float.isNaN(explosionRadius) || Float.isInfinite(explosionRadius) || explosionRadius < 0) explosionRadius = 3.0f;
        explosionRadius = clampExplosionRadius(explosionRadius);
        if (multiHitDelayTicks < 1) multiHitDelayTicks = 1;
        maxHits = clampMaxHits(maxHits);
    }

    private static float clampExplosionRadius(float explosionRadius) {
        if (Float.isNaN(explosionRadius) || Float.isInfinite(explosionRadius)) return 0.0f;
        return Math.max(0.0f, Math.min(MAX_EXPLOSION_RADIUS, explosionRadius));
    }

    private static int clampMaxHits(int maxHits) {
        return Math.max(1, Math.min(MAX_HITS, maxHits));
    }

    public EnergyCombatData copy() {
        EnergyCombatData copy = new EnergyCombatData(damage, knockback, knockbackUp,
            explosive, explosionRadius, explosionDamageFalloff);
        copy.hitType = hitType;
        copy.multiHitDelayTicks = multiHitDelayTicks;
        copy.maxHits = maxHits;
        return copy;
    }
}
