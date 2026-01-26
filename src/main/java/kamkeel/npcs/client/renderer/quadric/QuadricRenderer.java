package kamkeel.npcs.client.renderer.quadric;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.GLAllocation;
import org.lwjgl.opengl.GL11;

/**
 * Base class for rendering GLU quadric shapes (spheres, cylinders, etc.)
 * Uses display lists for efficient rendering.
 *
 * Design inspired by LouisXIV's quadric rendering system.
 */
@SideOnly(Side.CLIENT)
public abstract class QuadricRenderer {
    protected int displayList;
    private boolean compiled;

    public final float radius;
    public final int slices;
    public final int stacks;

    // Current render state
    protected float scaleX = 1.0f;
    protected float scaleY = 1.0f;
    protected float scaleZ = 1.0f;

    protected float rotationX = 0.0f;
    protected float rotationY = 0.0f;
    protected float rotationZ = 0.0f;

    protected int color = 0xFFFFFF;
    protected float alpha = 1.0f;

    public QuadricRenderer(float radius, int slices, int stacks) {
        this.radius = radius;
        this.slices = slices;
        this.stacks = stacks;
    }

    public QuadricRenderer(float radius, int resolution) {
        this(radius, resolution, resolution);
    }

    public QuadricRenderer() {
        this(0.5f, 16);
    }

    /**
     * Render the quadric shape with current transform and color settings.
     */
    public void render(float partialTicks) {
        if (!compiled) {
            displayList = GLAllocation.generateDisplayLists(1);
            GL11.glNewList(displayList, GL11.GL_COMPILE);
            compile();
            GL11.glEndList();
            compiled = true;
        }

        GL11.glPushMatrix();

        // Apply scale
        GL11.glScalef(scaleX, scaleY, scaleZ);

        // Apply rotation
        GL11.glRotatef(rotationX, 1.0f, 0.0f, 0.0f);
        GL11.glRotatef(rotationY, 0.0f, 1.0f, 0.0f);
        GL11.glRotatef(rotationZ, 0.0f, 0.0f, 1.0f);

        // Apply color
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;
        GL11.glColor4f(r, g, b, alpha);

        GL11.glCallList(displayList);

        GL11.glPopMatrix();
    }

    /**
     * Compile the quadric geometry into the display list.
     * Called once, then cached.
     */
    protected abstract void compile();

    // Setters for render state
    public void setScale(float scale) {
        this.scaleX = this.scaleY = this.scaleZ = scale;
    }

    public void setScale(float x, float y, float z) {
        this.scaleX = x;
        this.scaleY = y;
        this.scaleZ = z;
    }

    public void setRotation(float x, float y, float z) {
        this.rotationX = x;
        this.rotationY = y;
        this.rotationZ = z;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    /**
     * Linear interpolation helper.
     */
    public static float lerp(float current, float target, float factor) {
        return current + (target - current) * factor;
    }

    /**
     * Color interpolation helper.
     */
    public static int lerpColor(int from, int to, float factor) {
        int r1 = (from >> 16) & 0xFF;
        int g1 = (from >> 8) & 0xFF;
        int b1 = from & 0xFF;

        int r2 = (to >> 16) & 0xFF;
        int g2 = (to >> 8) & 0xFF;
        int b2 = to & 0xFF;

        int r = (int) (r1 + (r2 - r1) * factor);
        int g = (int) (g1 + (g2 - g1) * factor);
        int b = (int) (b1 + (b2 - b1) * factor);

        return (r << 16) | (g << 8) | b;
    }
}
