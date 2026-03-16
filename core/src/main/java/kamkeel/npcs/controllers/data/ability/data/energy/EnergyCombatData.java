package kamkeel.npcs.controllers.data.ability.data.energy;


import kamkeel.npcs.controllers.data.ability.enums.HitType;
import noppes.npcs.api.ability.data.IEnergyCombatData;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IEntityLiving;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.IDamageSource;
import noppes.npcs.api.INbt;
import noppes.npcs.api.INbtList;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.IWorld;

/**
 * Groups combat properties shared by energy projectile abilities.
 * Used as a parameter object for IEntity constructors and ability IConfiguration.
 */
public class EnergyCombatData implements IEnergyCombatData {
    public static final float MAX_IExplosion_RADIUS = 15.0f;
    public static final int DEFAULT_MAX_HITS = 5;
    public static final int MAX_HITS = 200;

    public float damage = 7.0f;
    public float knockback = 1.0f;
    public float knockbackUp = 0.1f;
    public boolean explosive = false;
    public float IExplosionRadius = 3.0f;
    public float IExplosionDamageFalloff = 0.5f;
    public HitType hitType = HitType.SINGLE;
    public int multiHitDelayTicks = 5;
    public int maxHits = DEFAULT_MAX_HITS;

    public EnergyCombatData() {
    }

    public EnergyCombatData(float damage, float knockback, float knockbackUp,
                            boolean explosive, float IExplosionRadius, float IExplosionDamageFalloff,
                            HitType hitType, int multiHitDelay) {
        this(damage, knockback, knockbackUp, explosive, IExplosionRadius, IExplosionDamageFalloff, hitType, multiHitDelay, DEFAULT_MAX_HITS);
    }

    public EnergyCombatData(float damage, float knockback, float knockbackUp,
                            boolean explosive, float IExplosionRadius, float IExplosionDamageFalloff,
                            HitType hitType, int multiHitDelay, int maxHits) {
        this.damage = damage;
        this.knockback = knockback;
        this.knockbackUp = knockbackUp;
        this.explosive = explosive;
        this.IExplosionRadius = clampIExplosionRadius(IExplosionRadius);
        this.IExplosionDamageFalloff = IExplosionDamageFalloff;
        this.hitType = hitType;
        this.multiHitDelayTicks = multiHitDelay;
        this.maxHits = clampMaxHits(maxHits);
    }

    public EnergyCombatData(float damage, float knockback, float knockbackUp,
                            boolean explosive, float IExplosionRadius, float IExplosionDamageFalloff) {
        this.damage = damage;
        this.knockback = knockback;
        this.knockbackUp = knockbackUp;
        this.explosive = explosive;
        this.IExplosionRadius = clampIExplosionRadius(IExplosionRadius);
        this.IExplosionDamageFalloff = IExplosionDamageFalloff;
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
    public float getIExplosionRadius() {
        return IExplosionRadius;
    }

    @Override
    public void setIExplosionRadius(float IExplosionRadius) {
        this.IExplosionRadius = clampIExplosionRadius(IExplosionRadius);
    }

    @Override
    public float getIExplosionDamageFalloff() {
        return IExplosionDamageFalloff;
    }

    @Override
    public void setIExplosionDamageFalloff(float IExplosionDamageFalloff) {
        this.IExplosionDamageFalloff = IExplosionDamageFalloff;
    }

    public int getMaxHits() {
        return maxHits;
    }

    public void setMaxHits(int maxHits) {
        this.maxHits = clampMaxHits(maxHits);
    }

    public void writeNBT(INbt nbt) {
        nbt.setFloat("damage", damage);
        nbt.setFloat("knockback", knockback);
        nbt.setFloat("knockbackUp", knockbackUp);
        nbt.setBoolean("explosive", explosive);
        nbt.setFloat("IExplosionRadius", IExplosionRadius);
        nbt.setFloat("IExplosionDamageFalloff", IExplosionDamageFalloff);
        nbt.setInteger("hitType", hitType.ordinal());
        nbt.setInteger("multiHitDelayTicks", multiHitDelayTicks);
        nbt.setInteger("maxHits", maxHits);
    }

    public void readNBT(INbt nbt) {
        damage = nbt.hasKey("damage") ? nbt.getFloat("damage") : 7.0f;
        knockback = nbt.hasKey("knockback") ? nbt.getFloat("knockback") : 1.0f;
        knockbackUp = nbt.hasKey("knockbackUp") ? nbt.getFloat("knockbackUp") : 0.1f;
        explosive = nbt.hasKey("explosive") && nbt.getBoolean("explosive");
        IExplosionRadius = nbt.hasKey("IExplosionRadius") ? nbt.getFloat("IExplosionRadius") : 3.0f;
        IExplosionDamageFalloff = nbt.hasKey("IExplosionDamageFalloff") ? nbt.getFloat("IExplosionDamageFalloff") : 0.5f;
        hitType = HitType.fromOrdinal(nbt.hasKey("hitType") ? nbt.getInteger("hitType") : 0);
        multiHitDelayTicks = nbt.hasKey("multiHitDelayTicks") ? nbt.getInteger("multiHitDelayTicks") : 5;
        maxHits = nbt.hasKey("maxHits") ? nbt.getInteger("maxHits") : DEFAULT_MAX_HITS;

        // Sanitize
        if (Float.isNaN(damage) || Float.isInfinite(damage)) damage = 7.0f;
        if (Float.isNaN(knockback) || Float.isInfinite(knockback) || knockback < 0) knockback = 1.0f;
        if (Float.isNaN(IExplosionRadius) || Float.isInfinite(IExplosionRadius) || IExplosionRadius < 0) IExplosionRadius = 3.0f;
        IExplosionRadius = clampIExplosionRadius(IExplosionRadius);
        if (multiHitDelayTicks < 1) multiHitDelayTicks = 1;
        maxHits = clampMaxHits(maxHits);
    }

    private static float clampIExplosionRadius(float IExplosionRadius) {
        if (Float.isNaN(IExplosionRadius) || Float.isInfinite(IExplosionRadius)) return 0.0f;
        return Math.max(0.0f, Math.min(MAX_IExplosion_RADIUS, IExplosionRadius));
    }

    private static int clampMaxHits(int maxHits) {
        return Math.max(1, Math.min(MAX_HITS, maxHits));
    }

    public EnergyCombatData copy() {
        EnergyCombatData copy = new EnergyCombatData(damage, knockback, knockbackUp,
            explosive, IExplosionRadius, IExplosionDamageFalloff);
        copy.hitType = hitType;
        copy.multiHitDelayTicks = multiHitDelayTicks;
        copy.maxHits = maxHits;
        return copy;
    }
}
