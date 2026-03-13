package noppes.npcs.controllers.data;

import noppes.npcs.api.entity.data.IHitboxData;
import noppes.npcs.platform.nbt.INBTCompound;
import noppes.npcs.core.CoreConfig;

public class HitboxData implements IHitboxData {

    private float widthScale = 1f;
    private float heightScale = 1f;
    private boolean hitboxEnabled = false;

    public INBTCompound writeToNBT(INBTCompound nbttagcompound) {
        nbttagcompound.setBoolean("HitboxEnabled", hitboxEnabled);
        if (hitboxEnabled) {
            if (widthScale > CoreConfig.HitBoxScaleMax)
                widthScale = CoreConfig.HitBoxScaleMax;
            nbttagcompound.setFloat("HitboxWidthScale", widthScale);

            if (heightScale > CoreConfig.HitBoxScaleMax)
                heightScale = CoreConfig.HitBoxScaleMax;
            nbttagcompound.setFloat("HitboxHeightScale", heightScale);
        }
        return nbttagcompound;
    }

    public void readFromNBT(INBTCompound nbttagcompound) {
        hitboxEnabled = nbttagcompound.getBoolean("HitboxEnabled");
        if (hitboxEnabled) {
            widthScale = nbttagcompound.getFloat("HitboxWidthScale");
            if (widthScale > CoreConfig.HitBoxScaleMax)
                widthScale = CoreConfig.HitBoxScaleMax;

            heightScale = nbttagcompound.getFloat("HitboxHeightScale");
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
