package noppes.npcs.scripted.entity;

import kamkeel.npcs.entity.EntityEnergyAbility;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.api.INbt;
import noppes.npcs.api.entity.IEnergyAbility;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.handler.data.IMagicData;
import noppes.npcs.controllers.data.MagicData;
import noppes.npcs.scripted.NpcAPI;

/**
 * Base script wrapper for all energy ability entities.
 * Implements shared display, lightning, owner, and charging methods.
 */
public abstract class ScriptEnergyAbility<T extends EntityEnergyAbility> extends ScriptEntity<T> implements IEnergyAbility {

    public ScriptEnergyAbility(T entity) {
        super(entity);
    }

    // ==================== OWNER ====================

    public int getOwnerEntityId() {
        return entity.getOwnerEntityId();
    }

    public IEntity getOwner() {
        Entity owner = entity.getOwnerEntity();
        return owner != null ? NpcAPI.Instance().getIEntity(owner) : null;
    }

    // ==================== DISPLAY ====================

    public int getInnerColor() {
        return entity.getInnerColor();
    }

    public void setInnerColor(int color) {
        entity.setInnerColor(color);
    }

    public float getInnerAlpha() {
        return entity.getInnerAlpha();
    }

    public void setInnerAlpha(float alpha) {
        entity.setInnerAlpha(alpha);
    }

    public int getOuterColor() {
        return entity.getOuterColor();
    }

    public void setOuterColor(int color) {
        entity.setOuterColor(color);
    }

    public boolean isOuterColorEnabled() {
        return entity.isOuterColorEnabled();
    }

    public void setOuterColorEnabled(boolean enabled) {
        entity.setOuterColorEnabled(enabled);
    }

    public float getOuterColorWidth() {
        return entity.getOuterColorWidth();
    }

    public void setOuterColorWidth(float width) {
        entity.setOuterColorWidth(width);
    }

    public float getOuterColorAlpha() {
        return entity.getOuterColorAlpha();
    }

    public void setOuterColorAlpha(float alpha) {
        entity.setOuterColorAlpha(alpha);
    }

    // ==================== LIGHTNING ====================

    public boolean hasLightningEffect() {
        return entity.hasLightningEffect();
    }

    public void setLightningEffect(boolean enabled) {
        entity.setLightningEffect(enabled);
    }

    public float getLightningDensity() {
        return entity.getLightningDensity();
    }

    public void setLightningDensity(float density) {
        entity.setLightningDensity(density);
    }

    public float getLightningRadius() {
        return entity.getLightningRadius();
    }

    public void setLightningRadius(float radius) {
        entity.setLightningRadius(radius);
    }

    public int getLightningFadeTime() {
        return entity.getLightningFadeTime();
    }

    public void setLightningFadeTime(int ticks) {
        entity.setLightningFadeTime(ticks);
    }

    // ==================== STATE ====================

    public boolean isCharging() {
        return entity.isCharging();
    }

    public float getChargeProgress() {
        return entity.getChargeProgress();
    }

    public boolean isIgnoreIFrames() {
        return entity.isIgnoreIFrames();
    }

    public void setIgnoreIFrames(boolean ignore) {
        entity.setIgnoreIFrames(ignore);
    }

    // ==================== CUSTOM DAMAGE DATA ====================

    public INbt getDamageData() {
        NBTTagCompound data = entity.getCustomDamageData();
        if (data == null) {
            data = new NBTTagCompound();
            entity.setCustomDamageData(data);
        }
        return NpcAPI.Instance().getINbt(data);
    }

    public void setDamageData(INbt data) {
        if (data == null) {
            entity.setCustomDamageData(null);
        } else {
            entity.setCustomDamageData((NBTTagCompound) data.getMCNBT());
        }
    }

    // ==================== MAGIC DATA ====================

    public IMagicData getMagicData() {
        return entity.getMagicData();
    }

    public void setMagicData(IMagicData data) {
        if (data instanceof MagicData) {
            entity.setMagicData((MagicData) data);
        }
    }
}
