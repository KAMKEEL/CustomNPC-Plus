package noppes.npcs.controllers.data;


import noppes.npcs.api.entity.data.IHitboxData;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IEntityLiving;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.IDamageSource;
import noppes.npcs.api.INbt;
import noppes.npcs.api.INbtList;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.IWorld;
import noppes.npcs.core.CoreConfig;

public class HitboxData implements IHitboxData {

    private float widthScale = 1f;
    private float heightScale = 1f;
    private boolean hitboxEnabled = false;

    public INbt writeToNBT(INbt INbt) {
        INbt.setBoolean("HitboxEnabled", hitboxEnabled);
        if (hitboxEnabled) {
            if (widthScale > CoreConfig.HitBoxScaleMax)
                widthScale = CoreConfig.HitBoxScaleMax;
            INbt.setFloat("HitboxWidthScale", widthScale);

            if (heightScale > CoreConfig.HitBoxScaleMax)
                heightScale = CoreConfig.HitBoxScaleMax;
            INbt.setFloat("HitboxHeightScale", heightScale);
        }
        return INbt;
    }

    public void readFromNBT(INbt INbt) {
        hitboxEnabled = INbt.getBoolean("HitboxEnabled");
        if (hitboxEnabled) {
            widthScale = INbt.getFloat("HitboxWidthScale");
            if (widthScale > CoreConfig.HitBoxScaleMax)
                widthScale = CoreConfig.HitBoxScaleMax;

            heightScale = INbt.getFloat("HitboxHeightScale");
            if (heightScale > CoreConfig.HitBoxScaleMax)
                heightScale = CoreConfig.HitBoxScaleMax;
        }
    }

    public float getWidthScale() {
        return widthScale;
    }

    public void setWidthScale(float widthScale) {
        this.widthScale = widthScale;
    }

    public float getHeightScale() {
        return heightScale;
    }

    public void setHeightScale(float heightScale) {
        this.heightScale = heightScale;
    }

    public boolean isHitboxEnabled() {
        return hitboxEnabled;
    }

    public void setHitboxEnabled(boolean hitboxEnabled) {
        this.hitboxEnabled = hitboxEnabled;
    }
}
