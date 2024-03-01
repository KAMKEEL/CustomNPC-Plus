package noppes.npcs.controllers.data;

import net.minecraft.nbt.NBTTagCompound;

public class HitboxData {

    private float width = 1f;
    private float height = 1.8f;
    private boolean hitboxEnabled = false;

    public NBTTagCompound writeToNBT(NBTTagCompound nbttagcompound) {
        nbttagcompound.setBoolean("HitboxEnabled", hitboxEnabled);
        if(hitboxEnabled) {
            nbttagcompound.setFloat("HitboxWidth", width);
            nbttagcompound.setFloat("HitboxHeight", height);
        }
        return nbttagcompound;
    }

    public void readFromNBT(NBTTagCompound nbttagcompound) {
        hitboxEnabled = nbttagcompound.getBoolean("CustomHitbox");
        if(hitboxEnabled) {
            width = nbttagcompound.getFloat("HitboxWidth");
            height = nbttagcompound.getFloat("HitboxHeight");
        }
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public boolean isHitboxEnabled() {
        return hitboxEnabled;
    }

    public void setHitboxEnabled(boolean hitboxEnabled) {
        this.hitboxEnabled = hitboxEnabled;
    }
}
