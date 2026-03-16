package common.minecraft.util;

/**
 * Platform abstraction for Minecraft's ResourceLocation.
 * MC 1.7.10: net.minecraft.util.ResourceLocation
 * MC 1.12+: net.minecraft.util.ResourceLocation
 */
public interface IResourceLocation {
    String getResourceDomain();
    String getResourcePath();
    String toString();
}
