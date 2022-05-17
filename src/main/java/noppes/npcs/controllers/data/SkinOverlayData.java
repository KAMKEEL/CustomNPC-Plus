package noppes.npcs.controllers.data;

import net.minecraft.util.ResourceLocation;

public class SkinOverlayData {
    public ResourceLocation location = null;
    public String directory;
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

    public SkinOverlayData(String directory, boolean glow, float alpha, float size, float speedX, float speedY,
                           float scaleX, float scaleY, float offsetX, float offsetY, float offsetZ) {
        this.directory = directory;
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
}
