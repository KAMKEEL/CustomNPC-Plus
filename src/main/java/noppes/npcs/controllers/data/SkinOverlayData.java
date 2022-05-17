package noppes.npcs.controllers.data;

import net.minecraft.util.ResourceLocation;

public class SkinOverlayData {
    public ResourceLocation location;
    public boolean glow;

    public SkinOverlayData(ResourceLocation location, boolean glow) {
        this.location = location;
        this.glow = glow;
    }
}
