package noppes.npcs.controllers.data;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.config.ConfigMain;

public class HitboxData {

    private float widthScale = 1f;
    private float heightScale = 1f;
    private boolean hitboxEnabled = false;

    public NBTTagCompound writeToNBT(NBTTagCompound nbttagcompound) {
        nbttagcompound.setBoolean("HitboxEnabled", hitboxEnabled);
        if(hitboxEnabled) {
            if(widthScale > ConfigMain.HitBoxScaleMax)
                widthScale = ConfigMain.HitBoxScaleMax;
            nbttagcompound.setFloat("HitboxWidthScale", widthScale);

            if(heightScale > ConfigMain.HitBoxScaleMax)
                heightScale = ConfigMain.HitBoxScaleMax;
            nbttagcompound.setFloat("HitboxHeightScale", heightScale);
        }
        return nbttagcompound;
    }

    public void readFromNBT(NBTTagCompound nbttagcompound) {
        hitboxEnabled = nbttagcompound.getBoolean("HitboxEnabled");
        if(hitboxEnabled) {
            widthScale = nbttagcompound.getFloat("HitboxWidthScale");
            if(widthScale > ConfigMain.HitBoxScaleMax)
                widthScale = ConfigMain.HitBoxScaleMax;

            heightScale = nbttagcompound.getFloat("HitboxHeightScale");
            if(heightScale > ConfigMain.HitBoxScaleMax)
                heightScale = ConfigMain.HitBoxScaleMax;
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
