package noppes.npcs.client.renderer;

import net.minecraft.client.Minecraft;

/**
 * Static utility for computing animated texture UV offsets.
 * Used by renderers when animation parameters come from data class fields
 * rather than .mcmeta auto-detection on ImageData.
 */
public class AnimationHelper {

    /**
     * Returns the V-coordinate offset (0.0-1.0) for the current animation frame.
     * Based on system time for consistent sync across all animated textures.
     *
     * @param totalHeight Total pixel height of the sprite strip
     * @param frameCount  Number of frames in the strip
     * @param frametime   Ticks per frame (1 tick = 50ms)
     * @return V offset in 0.0-1.0 range, or 0.0 if not animated
     */
    public static float getFrameVOffset(int totalHeight, int frameCount, int frametime) {
        if (frameCount <= 1 || totalHeight <= 0) {
            return 0f;
        }

        frametime = Math.max(1, frametime);
        long millis = Minecraft.getSystemTime();
        int tick = (int) (millis / 50L);
        int frameIdx = (tick / frametime) % frameCount;
        int frameHeight = totalHeight / frameCount;

        return (float) (frameIdx * frameHeight) / (float) totalHeight;
    }

    /**
     * Returns the V-coordinate size of a single frame in 0.0-1.0 range.
     *
     * @param totalHeight Total pixel height of the sprite strip
     * @param frameCount  Number of frames in the strip
     * @return V size of one frame, or 1.0 if not animated
     */
    public static float getFrameVSize(int totalHeight, int frameCount) {
        if (frameCount <= 1 || totalHeight <= 0) {
            return 1f;
        }
        int frameHeight = totalHeight / frameCount;
        return (float) frameHeight / (float) totalHeight;
    }
}
