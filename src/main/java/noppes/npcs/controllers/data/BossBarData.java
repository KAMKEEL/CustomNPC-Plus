package noppes.npcs.controllers.data;

import net.minecraft.nbt.NBTTagCompound;

/**
 * @author MayIHaveK
 * @date 2025/07/11 16:23
 **/
public class BossBarData {

    private boolean bossBarEnabled = false;
    private String bossBarTexture = "customnpcs:textures/gui/bossbar/boss_bar.png";
    private String bossBarBackgroundTexture = "customnpcs:textures/gui/bossbar/boss_bar_background.png";
    private int bossBarColor = 0xFFFFFF;
    private int bossBarBackgroundColor = 0xFFFFFF;
    private float bossBarScale = 1.0f;
    private int bossBarOffsetX = 0;
    private int bossBarOffsetY = 0;

    public boolean isBossBarEnabled() {
        return bossBarEnabled;
    }

    public void setBossBarEnabled(boolean bossBarEnabled) {
        this.bossBarEnabled = bossBarEnabled;
    }

    public String getBossBarTexture() {
        return bossBarTexture;
    }

    public void setBossBarTexture(String bossBarTexture) {
        this.bossBarTexture = bossBarTexture;
    }

    public String getBossBarBackgroundTexture() {
        return bossBarBackgroundTexture;
    }

    public void setBossBarBackgroundTexture(String bossBarBackgroundTexture) {
        this.bossBarBackgroundTexture = bossBarBackgroundTexture;
    }

    public int getBossBarColor() {
        return bossBarColor;
    }

    public void setBossBarColor(int bossBarColor) {
        this.bossBarColor = bossBarColor;
    }

    public int getBossBarBackgroundColor() {
        return bossBarBackgroundColor;
    }

    public void setBossBarBackgroundColor(int bossBarBackgroundColor) {
        this.bossBarBackgroundColor = bossBarBackgroundColor;
    }

    public float getBossBarScale() {
        return bossBarScale;
    }

    public void setBossBarScale(float bossBarScale) {
        this.bossBarScale = bossBarScale;
    }

    public int getBossBarOffsetX() {
        return bossBarOffsetX;
    }

    public void setBossBarOffsetX(int bossBarOffsetX) {
        this.bossBarOffsetX = bossBarOffsetX;
    }

    public int getBossBarOffsetY() {
        return bossBarOffsetY;
    }

    public void setBossBarOffsetY(int bossBarOffsetY) {
        this.bossBarOffsetY = bossBarOffsetY;
    }

    public NBTTagCompound writeToNBT(NBTTagCompound nbttagcompound) {
        nbttagcompound.setBoolean("BossBarEnabled", bossBarEnabled);
        nbttagcompound.setString("BossBarTexture", bossBarTexture);
        nbttagcompound.setString("BossBarBackgroundTexture", bossBarBackgroundTexture);
        nbttagcompound.setInteger("BossBarColor", bossBarColor);
        nbttagcompound.setInteger("BossBarBackgroundColor", bossBarBackgroundColor);
        nbttagcompound.setFloat("BossBarScale", bossBarScale);
        nbttagcompound.setInteger("BossBarOffsetX", bossBarOffsetX);
        nbttagcompound.setInteger("BossBarOffsetY", bossBarOffsetY);
        return nbttagcompound;
    }

    public void readFromNBT(NBTTagCompound nbttagcompound) {
        bossBarEnabled = nbttagcompound.getBoolean("BossBarEnabled");
        bossBarTexture = nbttagcompound.getString("BossBarTexture");
        bossBarBackgroundTexture = nbttagcompound.getString("BossBarBackgroundTexture");
        bossBarColor = nbttagcompound.getInteger("BossBarColor");
        bossBarBackgroundColor = nbttagcompound.getInteger("BossBarBackgroundColor");
        bossBarScale = nbttagcompound.getFloat("BossBarScale");
        bossBarOffsetX = nbttagcompound.getInteger("BossBarOffsetX");
        bossBarOffsetY = nbttagcompound.getInteger("BossBarOffsetY");
    }

}
