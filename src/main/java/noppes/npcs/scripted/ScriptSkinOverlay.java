package noppes.npcs.scripted;

import noppes.npcs.scripted.interfaces.ISkinOverlay;

public class ScriptSkinOverlay implements ISkinOverlay {
    public String texture;
    public boolean glow;
    public float alpha = 1.0F;
    public float size = 1.0F;

    public float speedX = 0.0F;
    public float speedY = 0.0F;

    public float scaleX = 1.0F;
    public float scaleY = 1.0F;

    public float offsetX = 0.0F;
    public float offsetY = 0.0F;
    public float offsetZ = 0.0F;

    public ScriptSkinOverlay(String texture) {
        this.texture = texture;
    }

    public void setTexture(String texture) {
        this.texture = texture;
    }
    public String getTexture() {
        return texture;
    }

    public void setGlow(boolean glow) {
        this.glow = glow;
    }
    public boolean getGlow() {
        return glow;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }
    public float getAlpha() {
        return alpha;
    }

    public void setSize(float size) {
        this.size = size;
    }
    public float getSize() {
        return size;
    }

    public void setScale(float scaleX, float scaleY) {
        this.scaleX = scaleX;
        this.scaleY = scaleY;
    }
    public float getScaleX() {
        return scaleX;
    }
    public float getScaleY() {
        return scaleY;
    }

    public void setSpeed(float speedX, float speedY) {
        this.speedX = speedX;
        this.speedY = speedY;
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
}
