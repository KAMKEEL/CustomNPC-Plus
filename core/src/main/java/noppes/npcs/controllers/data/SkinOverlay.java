package noppes.npcs.controllers.data;

import noppes.npcs.api.ISkinOverlay;
import noppes.npcs.api.INbt;

public class SkinOverlay implements ISkinOverlay {
    private Runnable onChanged;
    public String texture;

    public boolean glow = true;
    public boolean blend = true;

    public float alpha = 1.0F;
    public float size = 1.0F;
    public int color = 0xFFFFFF;

    public float speedX = 0.0F;
    public float speedY = 0.0F;

    public float scaleX = 1.0F;
    public float scaleY = 1.0F;

    public float offsetX = 0.0F;
    public float offsetY = 0.0F;
    public float offsetZ = 0.0F;

    //Client-sided
    public long ticks = 0;

    public SkinOverlay() {
    }

    public SkinOverlay(String texture) {
        this.texture = texture;
    }

    public void setOnChanged(Runnable onChanged) {
        this.onChanged = onChanged;
    }

    public void setTexture(String texture) {
        this.texture = texture;
        this.notifyChanged();
    }

    public String getTexture() {
        return this.texture;
    }

    public void setGlow(boolean glow) {
        this.glow = glow;
        this.notifyChanged();
    }

    public boolean getGlow() {
        return glow;
    }

    public void setBlend(boolean blend) {
        this.blend = blend;
        this.notifyChanged();
    }

    public boolean getBlend() {
        return blend;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
        this.notifyChanged();
    }

    public float getAlpha() {
        return alpha;
    }

    public void setSize(float size) {
        this.size = size;
        this.notifyChanged();
    }

    public float getSize() {
        return size;
    }

    public void setColor(int color) {
        this.color = Math.max(0, color);
        this.notifyChanged();
    }

    public int getColor() {
        return color;
    }

    public void setTextureScale(float scaleX, float scaleY) {
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        this.notifyChanged();
    }

    public float getTextureScaleX() {
        return scaleX;
    }

    public float getTextureScaleY() {
        return scaleY;
    }

    public void setSpeed(float speedX, float speedY) {
        this.speedX = speedX;
        this.speedY = speedY;
        this.notifyChanged();
    }

    public float getSpeedX() {
        return speedX;
    }

    public float getSpeedY() {
        return speedY;
    }

    public void setOffset(float offsetX, float offsetY, float offsetZ) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
        this.notifyChanged();
    }

    public float getOffsetX() {
        return offsetX;
    }

    public float getOffsetY() {
        return offsetY;
    }

    public float getOffsetZ() {
        return offsetZ;
    }

    public void readFromNBT(INbt compound) {
        this.texture = compound.getString("SkinOverlayTexture");
        this.glow = compound.getBoolean("SkinOverlayGlow");
        this.blend = compound.getBoolean("SkinOverlayBlend");
        this.alpha = compound.getFloat("SkinOverlayAlpha");
        this.size = compound.getFloat("SkinOverlaySize");
        if (compound.hasKey("SkinOverlayColor")) {
            this.color = compound.getInteger("SkinOverlayColor");
        } else {
            this.color = 0xFFFFFF;
        }
        this.speedX = compound.getFloat("SkinOverlaySpeedX");
        this.speedY = compound.getFloat("SkinOverlaySpeedY");
        this.scaleX = compound.getFloat("SkinOverlayScaleX");
        this.scaleY = compound.getFloat("SkinOverlayScaleY");
        this.offsetX = compound.getFloat("SkinOverlayOffsetX");
        this.offsetY = compound.getFloat("SkinOverlayOffsetY");
        this.offsetZ = compound.getFloat("SkinOverlayOffsetZ");
    }

    public INbt writeToNBT(INbt compound) {
        compound.setString("SkinOverlayTexture", this.getTexture());
        compound.setBoolean("SkinOverlayGlow", this.getGlow());
        compound.setBoolean("SkinOverlayBlend", this.getBlend());
        compound.setFloat("SkinOverlayAlpha", this.getAlpha());
        compound.setFloat("SkinOverlaySize", this.getSize());
        compound.setInteger("SkinOverlayColor", this.getColor());
        compound.setFloat("SkinOverlaySpeedX", this.getSpeedX());
        compound.setFloat("SkinOverlaySpeedY", this.getSpeedY());
        compound.setFloat("SkinOverlayScaleX", this.getTextureScaleX());
        compound.setFloat("SkinOverlayScaleY", this.getTextureScaleY());
        compound.setFloat("SkinOverlayOffsetX", this.getOffsetX());
        compound.setFloat("SkinOverlayOffsetY", this.getOffsetY());
        compound.setFloat("SkinOverlayOffsetZ", this.getOffsetZ());

        return compound;
    }

    private void notifyChanged() {
        if (this.onChanged != null) {
            this.onChanged.run();
        }
    }
}
