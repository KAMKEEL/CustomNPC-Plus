package noppes.npcs.scripted.entity;

import kamkeel.npcs.entity.EntityAbilityZone;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.api.INbt;
import noppes.npcs.api.entity.IEnergyZone;
import noppes.npcs.scripted.NpcAPI;

/**
 * Script wrapper for EntityAbilityZone (Hazard and Trap zones).
 */
public class ScriptEnergyZone<T extends EntityAbilityZone> extends ScriptEntity<T> implements IEnergyZone {

    public ScriptEnergyZone(T entity) {
        super(entity);
    }

    // ==================== ZONE TYPE ====================

    @Override
    public int getZoneType() {
        return entity.getZoneType().ordinal();
    }

    // ==================== COMMON ZONE PROPERTIES ====================

    @Override
    public int getZoneShape() {
        return entity.getShape().ordinal();
    }

    @Override
    public void setZoneShape(int shape) {
        EntityAbilityZone.ZoneShape[] values = EntityAbilityZone.ZoneShape.values();
        if (shape >= 0 && shape < values.length) {
            entity.setShape(values[shape]);
        }
    }

    @Override
    public float getRadius() {
        return entity.getRadius();
    }

    @Override
    public void setRadius(float radius) {
        entity.setRadius(radius);
    }

    @Override
    public float getZoneHeight() {
        return entity.getZoneHeight();
    }

    @Override
    public void setZoneHeight(float height) {
        entity.setZoneHeight(height);
    }

    @Override
    public int getDuration() {
        return entity.getDurationTicks();
    }

    @Override
    public void setDuration(int ticks) {
        entity.setDurationTicks(ticks);
    }

    // ==================== COLORS ====================

    @Override
    public int getInnerColor() {
        return entity.getInnerColor();
    }

    @Override
    public void setInnerColor(int color) {
        entity.setInnerColor(color);
    }

    @Override
    public int getOuterColor() {
        return entity.getOuterColor();
    }

    @Override
    public void setOuterColor(int color) {
        entity.setOuterColor(color);
    }

    @Override
    public boolean isOuterColorEnabled() {
        return entity.isOuterColorEnabled();
    }

    @Override
    public void setOuterColorEnabled(boolean enabled) {
        entity.setOuterColorEnabled(enabled);
    }

    // ==================== VISUALS ====================

    @Override
    public float getParticleDensity() {
        return entity.getParticleDensity();
    }

    @Override
    public void setParticleDensity(float density) {
        entity.setParticleDensity(density);
    }

    @Override
    public float getParticleScale() {
        return entity.getParticleScale();
    }

    @Override
    public void setParticleScale(float scale) {
        entity.setParticleScale(scale);
    }

    @Override
    public float getAnimSpeed() {
        return entity.getAnimSpeed();
    }

    @Override
    public void setAnimSpeed(float speed) {
        entity.setAnimSpeed(speed);
    }

    @Override
    public boolean isIgnoreIFrames() {
        return entity.isIgnoreIFrames();
    }

    @Override
    public void setIgnoreIFrames(boolean ignore) {
        entity.setIgnoreIFrames(ignore);
    }

    // ==================== HAZARD-SPECIFIC ====================

    @Override
    public float getDamagePerSecond() {
        return entity.getDamagePerSecond();
    }

    @Override
    public void setDamagePerSecond(float dps) {
        entity.setDamagePerSecond(dps);
    }

    @Override
    public int getDamageInterval() {
        return entity.getDamageInterval();
    }

    @Override
    public void setDamageInterval(int ticks) {
        entity.setDamageInterval(ticks);
    }

    @Override
    public boolean isAffectsCaster() {
        return entity.isAffectsCaster();
    }

    @Override
    public void setAffectsCaster(boolean affects) {
        entity.setAffectsCaster(affects);
    }

    // ==================== TRAP-SPECIFIC ====================

    @Override
    public float getTriggerRadius() {
        return entity.getTriggerRadius();
    }

    @Override
    public void setTriggerRadius(float radius) {
        entity.setTriggerRadius(radius);
    }

    @Override
    public int getArmTime() {
        return entity.getArmTime();
    }

    @Override
    public void setArmTime(int ticks) {
        entity.setArmTime(ticks);
    }

    @Override
    public int getMaxTriggers() {
        return entity.getMaxTriggers();
    }

    @Override
    public void setMaxTriggers(int max) {
        entity.setMaxTriggers(max);
    }

    @Override
    public int getTriggerCooldown() {
        return entity.getTriggerCooldown();
    }

    @Override
    public void setTriggerCooldown(int ticks) {
        entity.setTriggerCooldown(ticks);
    }

    @Override
    public float getDamage() {
        return entity.getDamage();
    }

    @Override
    public void setDamage(float damage) {
        entity.setDamage(damage);
    }

    @Override
    public float getKnockback() {
        return entity.getKnockback();
    }

    @Override
    public void setKnockback(float knockback) {
        entity.setKnockback(knockback);
    }

    @Override
    public boolean isVisible() {
        return entity.isVisible();
    }

    @Override
    public void setVisible(boolean visible) {
        entity.setVisible(visible);
    }

    // ==================== CUSTOM DAMAGE DATA ====================

    @Override
    public INbt getDamageData() {
        NBTTagCompound data = entity.getCustomDamageData();
        if (data == null) {
            data = new NBTTagCompound();
            entity.setCustomDamageData(data);
        }
        return NpcAPI.Instance().getINbt(data);
    }

    @Override
    public void setDamageData(INbt data) {
        if (data == null) {
            entity.setCustomDamageData(null);
        } else {
            entity.setCustomDamageData((NBTTagCompound) data.getMCNBT());
        }
    }

    // ==================== SPAWNING ====================

    @Override
    public void spawn() {
        if (!entity.addedToChunk && entity.worldObj != null) {
            entity.worldObj.spawnEntityInWorld(entity);
        }
    }
}
