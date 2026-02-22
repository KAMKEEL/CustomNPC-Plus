package kamkeel.npcs.client.renderer;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.client.renderer.lightning.AttachedLightningRenderer;
import kamkeel.npcs.entity.EntityEnergyAbility;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

/**
 * Base renderer for ability projectiles.
 * Provides common GL state setup and rendering utilities.
 * <p>
 * Design inspired by LouisXIV's energy rendering system.
 */
@SideOnly(Side.CLIENT)
public abstract class RenderEnergyAbility extends Render {

    protected static final ResourceLocation WHITE_TEXTURE = new ResourceLocation("customnpcs", "textures/entity/white.png");
    protected static final int HIDE_INITIAL_ACTIVE_TICKS = 1;

    public RenderEnergyAbility() {
        this.shadowSize = 0.0f;
    }

    /**
     * Hide the first active tick to avoid the visible spawn-frame pause at fire time.
     * Charging previews are never hidden.
     */
    protected boolean shouldSkipInitialActiveRender(Entity entity) {
        if (!(entity instanceof EntityEnergyAbility)) return false;
        EntityEnergyAbility ability = (EntityEnergyAbility) entity;
        return !ability.isPreviewMode() && !ability.isCharging() && ability.ticksExisted <= HIDE_INITIAL_ACTIVE_TICKS;
    }

    /**
     * Setup GL state for translucent, self-illuminated rendering.
     * Forces full brightness so world lighting does not affect projectiles.
     */
    protected void setupRenderState() {
        GL11.glPushMatrix();
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_LIGHTING_BIT);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glAlphaFunc(GL11.GL_GREATER, 0.003921569F);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_TEXTURE_2D);

        // Force full brightness - prevents world lighting from darkening projectiles
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0f, 240.0f);
    }

    /**
     * Restore GL state after rendering.
     */
    protected void restoreRenderState() {
        GL11.glPopAttrib();
        GL11.glPopMatrix();
    }

    /**
     * Extract RGB components from color int.
     */
    protected float[] extractRGB(int color) {
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;
        return new float[]{r, g, b};
    }

    /**
     * Render a cube centered at origin with given color and alpha.
     */
    protected void renderCube(int color, float alpha, float halfSize) {
        float[] rgb = extractRGB(color);
        float r = rgb[0], g = rgb[1], b = rgb[2];

        Tessellator tess = Tessellator.instance;

        // Front face (z+)
        tess.startDrawingQuads();
        tess.setColorRGBA_F(r, g, b, alpha);
        tess.setNormal(0, 0, 1);
        tess.addVertex(-halfSize, -halfSize, halfSize);
        tess.addVertex(halfSize, -halfSize, halfSize);
        tess.addVertex(halfSize, halfSize, halfSize);
        tess.addVertex(-halfSize, halfSize, halfSize);
        tess.draw();

        // Back face (z-)
        tess.startDrawingQuads();
        tess.setColorRGBA_F(r, g, b, alpha);
        tess.setNormal(0, 0, -1);
        tess.addVertex(halfSize, -halfSize, -halfSize);
        tess.addVertex(-halfSize, -halfSize, -halfSize);
        tess.addVertex(-halfSize, halfSize, -halfSize);
        tess.addVertex(halfSize, halfSize, -halfSize);
        tess.draw();

        // Top face (y+)
        tess.startDrawingQuads();
        tess.setColorRGBA_F(r, g, b, alpha);
        tess.setNormal(0, 1, 0);
        tess.addVertex(-halfSize, halfSize, halfSize);
        tess.addVertex(halfSize, halfSize, halfSize);
        tess.addVertex(halfSize, halfSize, -halfSize);
        tess.addVertex(-halfSize, halfSize, -halfSize);
        tess.draw();

        // Bottom face (y-)
        tess.startDrawingQuads();
        tess.setColorRGBA_F(r, g, b, alpha);
        tess.setNormal(0, -1, 0);
        tess.addVertex(-halfSize, -halfSize, -halfSize);
        tess.addVertex(halfSize, -halfSize, -halfSize);
        tess.addVertex(halfSize, -halfSize, halfSize);
        tess.addVertex(-halfSize, -halfSize, halfSize);
        tess.draw();

        // Right face (x+)
        tess.startDrawingQuads();
        tess.setColorRGBA_F(r, g, b, alpha);
        tess.setNormal(1, 0, 0);
        tess.addVertex(halfSize, -halfSize, halfSize);
        tess.addVertex(halfSize, -halfSize, -halfSize);
        tess.addVertex(halfSize, halfSize, -halfSize);
        tess.addVertex(halfSize, halfSize, halfSize);
        tess.draw();

        // Left face (x-)
        tess.startDrawingQuads();
        tess.setColorRGBA_F(r, g, b, alpha);
        tess.setNormal(-1, 0, 0);
        tess.addVertex(-halfSize, -halfSize, -halfSize);
        tess.addVertex(-halfSize, -halfSize, halfSize);
        tess.addVertex(-halfSize, halfSize, halfSize);
        tess.addVertex(-halfSize, halfSize, -halfSize);
        tess.draw();
    }

    /**
     * Render a flat disc (cylinder with very small height) centered at origin.
     * The disc lies flat on the XZ plane.
     */
    protected void renderDisc(int color, float alpha, float radius, float thickness, int segments) {
        float[] rgb = extractRGB(color);
        float r = rgb[0], g = rgb[1], b = rgb[2];

        Tessellator tess = Tessellator.instance;
        float halfThick = thickness * 0.5f;

        // Top face
        tess.startDrawingQuads();
        tess.setColorRGBA_F(r, g, b, alpha);
        tess.setNormal(0, 1, 0);
        for (int i = 0; i < segments; i++) {
            float angle1 = (float) (2 * Math.PI * i / segments);
            float angle2 = (float) (2 * Math.PI * (i + 1) / segments);
            float x1 = (float) Math.cos(angle1) * radius;
            float z1 = (float) Math.sin(angle1) * radius;
            float x2 = (float) Math.cos(angle2) * radius;
            float z2 = (float) Math.sin(angle2) * radius;

            tess.addVertex(0, halfThick, 0);
            tess.addVertex(x1, halfThick, z1);
            tess.addVertex(x2, halfThick, z2);
            tess.addVertex(0, halfThick, 0);
        }
        tess.draw();

        // Bottom face
        tess.startDrawingQuads();
        tess.setColorRGBA_F(r, g, b, alpha);
        tess.setNormal(0, -1, 0);
        for (int i = 0; i < segments; i++) {
            float angle1 = (float) (2 * Math.PI * i / segments);
            float angle2 = (float) (2 * Math.PI * (i + 1) / segments);
            float x1 = (float) Math.cos(angle1) * radius;
            float z1 = (float) Math.sin(angle1) * radius;
            float x2 = (float) Math.cos(angle2) * radius;
            float z2 = (float) Math.sin(angle2) * radius;

            tess.addVertex(0, -halfThick, 0);
            tess.addVertex(x2, -halfThick, z2);
            tess.addVertex(x1, -halfThick, z1);
            tess.addVertex(0, -halfThick, 0);
        }
        tess.draw();

        // Edge
        tess.startDrawingQuads();
        tess.setColorRGBA_F(r, g, b, alpha);
        for (int i = 0; i < segments; i++) {
            float angle1 = (float) (2 * Math.PI * i / segments);
            float angle2 = (float) (2 * Math.PI * (i + 1) / segments);
            float x1 = (float) Math.cos(angle1) * radius;
            float z1 = (float) Math.sin(angle1) * radius;
            float x2 = (float) Math.cos(angle2) * radius;
            float z2 = (float) Math.sin(angle2) * radius;

            // Normal pointing outward
            float nx = (float) Math.cos((angle1 + angle2) * 0.5);
            float nz = (float) Math.sin((angle1 + angle2) * 0.5);
            tess.setNormal(nx, 0, nz);

            tess.addVertex(x1, -halfThick, z1);
            tess.addVertex(x2, -halfThick, z2);
            tess.addVertex(x2, halfThick, z2);
            tess.addVertex(x1, halfThick, z1);
        }
        tess.draw();
    }

    /**
     * Render a line segment as a quad billboard facing the camera.
     */
    protected void renderLineSegment(double x1, double y1, double z1,
                                     double x2, double y2, double z2,
                                     float width, int color, float alpha) {
        float[] rgb = extractRGB(color);
        float r = rgb[0], g = rgb[1], b = rgb[2];

        // Calculate direction
        double dx = x2 - x1;
        double dy = y2 - y1;
        double dz = z2 - z1;
        double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len < 0.001) return;

        // Perpendicular vectors for billboard
        double perpX, perpY, perpZ;
        if (Math.abs(dy) < 0.9) {
            // Cross with up vector
            perpX = -dz;
            perpY = 0;
            perpZ = dx;
        } else {
            // Cross with right vector
            perpX = 0;
            perpY = dz;
            perpZ = -dy;
        }
        double perpLen = Math.sqrt(perpX * perpX + perpY * perpY + perpZ * perpZ);
        if (perpLen > 0) {
            perpX = perpX / perpLen * width * 0.5;
            perpY = perpY / perpLen * width * 0.5;
            perpZ = perpZ / perpLen * width * 0.5;
        }

        Tessellator tess = Tessellator.instance;
        tess.startDrawingQuads();
        tess.setColorRGBA_F(r, g, b, alpha);
        tess.addVertex(x1 - perpX, y1 - perpY, z1 - perpZ);
        tess.addVertex(x2 - perpX, y2 - perpY, z2 - perpZ);
        tess.addVertex(x2 + perpX, y2 + perpY, z2 + perpZ);
        tess.addVertex(x1 + perpX, y1 + perpY, z1 + perpZ);
        tess.draw();
    }

    // ==================== SHARED LIGHTNING ====================

    /**
     * Get or create the lightning state for an energy entity.
     * Uses the lightningState field on EntityEnergyAbility (stored as Object
     * to avoid client class loading on server).
     */
    protected AttachedLightningRenderer.LightningState getLightningState(EntityEnergyAbility entity) {
        if (entity.lightningState == null) {
            entity.lightningState = new AttachedLightningRenderer.LightningState();
        }
        return (AttachedLightningRenderer.LightningState) entity.lightningState;
    }

    /**
     * Render attached lightning arcs around an energy entity.
     *
     * @param entity     the energy entity
     * @param innerScale multiplier for inner radius (e.g. 0.3f for projectiles, 0.95f for dome)
     * @param baseSize   the entity's current render size/scale
     */
    protected void renderAttachedLightning(EntityEnergyAbility entity, float innerScale, float baseSize) {
        AttachedLightningRenderer.LightningState state = getLightningState(entity);

        float density = entity.getLightningDensity();
        float innerRadius = innerScale * baseSize;
        float radius = innerRadius + entity.getLightningRadius() * baseSize;
        int outerColor = entity.getOuterColor();
        int innerColor = entity.getInnerColor();
        int fadeTime = entity.getLightningFadeTime();

        state.update(density, radius, outerColor, innerColor, fadeTime);
        state.render();
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity entity) {
        return WHITE_TEXTURE;
    }
}
