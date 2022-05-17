package noppes.npcs.controllers.data;

import net.minecraft.util.ResourceLocation;

public class SkinOverlayData {
    public ResourceLocation location;
    public boolean glow;
    public float alpha = 1.0F;

    public SkinOverlayData(ResourceLocation location, boolean glow, float alpha) {
        this.location = location;
        this.glow = glow;
        this.alpha = alpha;
    }
}
