package noppes.npcs.controllers.data;

import net.minecraft.nbt.NBTTagCompound;

public class CustomHitboxData {
    private float width = 1f;
    private float height = 1.8f;

    private boolean customHitbox = false;

    public NBTTagCompound writeToNBT(NBTTagCompound nbttagcompound) {
        nbttagcompound.setBoolean("CustomHitbox", customHitbox);
        if(customHitbox) {
            nbttagcompound.setFloat("HitboxWidth", width);
            nbttagcompound.setFloat("HitboxHeight", height);
        }
        return nbttagcompound;
    }

    public void readFromNBT(NBTTagCompound nbttagcompound) {
        customHitbox = nbttagcompound.getBoolean("CustomHitbox");
        if(customHitbox) {
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

    public boolean isCustomHitbox() {
        return customHitbox;
    }

    public void setCustomHitbox(boolean customHitbox) {
        this.customHitbox = customHitbox;
    }
}
