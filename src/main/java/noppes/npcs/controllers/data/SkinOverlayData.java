package noppes.npcs.controllers.data;

import net.minecraft.util.ResourceLocation;
import noppes.npcs.scripted.interfaces.ISkinOverlay;

public class SkinOverlayData implements ISkinOverlay {
    public ResourceLocation location = null;
    public String texture;
    public boolean glow = true;
    public float alpha = 1.0F;
    public float size = 1.0F;

    public float speedX = 0.0F;
    public float speedY = 0.0F;

    public float scaleX = 1.0F;
    public float scaleY = 1.0F;

    public float offsetX = 0.0F;
    public float offsetY = 0.0F;
    public float offsetZ = 0.0F;

    public SkinOverlayData(String texture) {
        this.texture = texture;
    }

    public SkinOverlayData(String texture, boolean glow, float alpha, float size, float speedX, float speedY,
                           float scaleX, float scaleY, float offsetX, float offsetY, float offsetZ) {
        this.texture = texture;
        this.glow = glow;
        this.alpha = alpha;
        this.size = size;
        this.speedX = speedX;
        this.speedY = speedY;
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
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

    public void setLocation(ResourceLocation location) {
        this.location = location;
    }
    public ResourceLocation getLocation() {
        return this.location;
    }
}
