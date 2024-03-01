package noppes.npcs.controllers.data;

import net.minecraft.nbt.NBTTagCompound;

public class HitboxData {

    private float widthScale = 1f;
    private float heightScale = 1f;
    private boolean hitboxEnabled = false;

    public NBTTagCompound writeToNBT(NBTTagCompound nbttagcompound) {
        nbttagcompound.setBoolean("HitboxEnabled", hitboxEnabled);
        if(hitboxEnabled) {
            nbttagcompound.setFloat("HitboxWidthScale", widthScale);
            nbttagcompound.setFloat("HitboxHeightScale", heightScale);
        }
        return nbttagcompound;
    }

    public void readFromNBT(NBTTagCompound nbttagcompound) {
        hitboxEnabled = nbttagcompound.getBoolean("HitboxEnabled");
        if(hitboxEnabled) {
            widthScale = nbttagcompound.getFloat("HitboxWidthScale");
            heightScale = nbttagcompound.getFloat("HitboxHeightScale");
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
