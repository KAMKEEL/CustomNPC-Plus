package noppes.npcs.scripted.entity;

import kamkeel.npcs.entity.EntityEnergyBarrier;
import kamkeel.npcs.entity.EntityEnergyDome;
import noppes.npcs.api.entity.IEnergyBarrier;
import noppes.npcs.scripted.constants.EntityType;

/**
 * Base script wrapper for energy barrier entities (Dome, Panel).
 * All shared methods delegate directly to EntityEnergyBarrier base.
 */
public class ScriptEnergyBarrier<T extends EntityEnergyBarrier> extends ScriptEnergyAbility<T> implements IEnergyBarrier {

    public ScriptEnergyBarrier(T entity) {
        super(entity);
    }

    @Override
    public int getType() {
        return EntityType.ENERGY_BARRIER;
    }

    @Override
    public boolean typeOf(int type) {
        return type == EntityType.ENERGY_BARRIER || super.typeOf(type);
    }

    // ==================== HEALTH ====================

    public float getCurrentHealth() {
        return entity.getCurrentHealth();
    }

    public void setCurrentHealth(float health) {
        entity.setCurrentHealth(health);
    }

    public float getHealthPercent() {
        return entity.getHealthPercent();
    }

    public float getMaxHealth() {
        return entity.getBarrierData().maxHealth;
    }

    public void setMaxHealth(float maxHealth) {
        entity.getBarrierData().setMaxHealth(maxHealth);
    }

    public boolean isUseHealth() {
        return entity.getBarrierData().useHealth;
    }

    public void setUseHealth(boolean useHealth) {
        entity.getBarrierData().useHealth = useHealth;
    }

    // ==================== DURATION ====================

    public int getDuration() {
        return entity.getBarrierData().durationTicks;
    }

    public void setDuration(int ticks) {
        entity.getBarrierData().setDurationTicks(ticks);
    }

    public boolean isUseDuration() {
        return entity.getBarrierData().useDuration;
    }

    public void setUseDuration(boolean useDuration) {
        entity.getBarrierData().useDuration = useDuration;
    }

    // ==================== STATE (BARRIER-SPECIFIC) ====================

    public int getTicksAlive() {
        return entity.getTicksAlive();
    }

    // ==================== BARRIER ====================

    public float getDefaultMultiplier() {
        return entity.getBarrierData().defaultMultiplier;
    }

    public void setDefaultMultiplier(float multiplier) {
        entity.getBarrierData().defaultMultiplier = multiplier;
    }

    public boolean isSolid() {
        return entity.getBarrierData().solid;
    }

    public void setSolid(boolean solid) {
        entity.getBarrierData().solid = solid;
    }

    public boolean isKnockbackEnabled() {
        return entity.getBarrierData().knockbackEnabled;
    }

    public void setKnockbackEnabled(boolean enabled) {
        entity.getBarrierData().knockbackEnabled = enabled;
    }

    public float getKnockbackStrength() {
        return entity.getBarrierData().knockbackStrength;
    }

    public void setKnockbackStrength(float strength) {
        entity.getBarrierData().knockbackStrength = strength;
    }

    public boolean isAbsorbing() {
        return entity.getBarrierData().absorbing;
    }

    public void setAbsorbing(boolean absorbing) {
        entity.getBarrierData().absorbing = absorbing;
    }

    public boolean isReflect() {
        return entity.getBarrierData().reflect;
    }

    public void setReflect(boolean reflect) {
        entity.getBarrierData().reflect = reflect;
    }

    public float getReflectStrengthPct() {
        return entity.getBarrierData().reflectStrengthPct;
    }

    public void setReflectStrengthPct(float strengthPct) {
        entity.getBarrierData().setReflectStrengthPct(strengthPct);
    }

    public int getBarrierType() {
        return entity instanceof EntityEnergyDome ? 0 : 1;
    }
}
