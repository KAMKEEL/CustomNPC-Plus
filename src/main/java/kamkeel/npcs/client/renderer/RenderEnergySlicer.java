package kamkeel.npcs.client.renderer;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.client.renderer.lightning.AttachedLightningRenderer;
import kamkeel.npcs.entity.EntityEnergySlicer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.Entity;
import org.lwjgl.opengl.GL11;

/**
 * Renders the Energy Slicer entity as a thin wide blade.
 * Oriented perpendicular to travel direction.
 * Inner solid core with outer translucent glow.
 */
@SideOnly(Side.CLIENT)
public class RenderEnergySlicer extends RenderAbilityProjectile {

    @Override
    public void doRender(Entity entity, double x, double y, double z, float yaw, float partialTicks) {
        EntityEnergySlicer slicer = (EntityEnergySlicer) entity;

        float size = slicer.getInterpolatedSize(partialTicks);
        float width = slicer.getSliceWidth();
        float thickness = slicer.getSliceThickness();

        // Scale with interpolated size for charging grow effect
        float sizeRatio = size > 0.01f ? size / Math.max(0.01f, slicer.getSliceWidth()) : 0.01f;
        if (sizeRatio > 1.0f) sizeRatio = 1.0f;

        setupRenderState();

        GL11.glPushMatrix();
        GL11.glTranslated(x, y, z);

        // Lightning before rotation
        if (slicer.hasLightningEffect()) {
            renderAttachedLightning(slicer, width * 0.5f * sizeRatio);
        }

        // Orient perpendicular to travel direction
        float travelYaw = slicer.getTravelYaw();
        float travelPitch = slicer.getTravelPitch();
        GL11.glRotatef(travelYaw, 0.0f, 1.0f, 0.0f);
        GL11.glRotatef(travelPitch, 1.0f, 0.0f, 0.0f);

        GL11.glPushMatrix();
        GL11.glScalef(sizeRatio, sizeRatio, sizeRatio);

        float innerScale = 0.7f;

        // Render outer blade (translucent glow)
        if (slicer.isOuterColorEnabled()) {
            float outerScale = 1.0f + slicer.getOuterColorWidth() * 0.2f;
            GL11.glDepthMask(false);
            renderBlade(slicer.getOuterColor(), slicer.getOuterColorAlpha(),
                width * 0.5f * outerScale, thickness * 2.0f, 0.05f);
            GL11.glDepthMask(true);
        }

        // Render inner blade (solid core)
        renderBlade(slicer.getInnerColor(), 1.0f,
            width * 0.5f * innerScale, thickness, 0.03f);

        GL11.glPopMatrix();
        GL11.glPopMatrix();

        restoreRenderState();
    }

    /**
     * Render a thin wide blade shape.
     * The blade extends along X (width), thin on Y (thickness), and very thin on Z (depth).
     */
    private void renderBlade(int color, float alpha, float halfWidth, float halfHeight, float halfDepth) {
        float[] rgb = extractRGB(color);
        float r = rgb[0], g = rgb[1], b = rgb[2];

        Tessellator tess = Tessellator.instance;

        // Front face (z+)
        tess.startDrawingQuads();
        tess.setColorRGBA_F(r, g, b, alpha);
        tess.setNormal(0, 0, 1);
        tess.addVertex(-halfWidth, -halfHeight, halfDepth);
        tess.addVertex(halfWidth, -halfHeight, halfDepth);
        tess.addVertex(halfWidth, halfHeight, halfDepth);
        tess.addVertex(-halfWidth, halfHeight, halfDepth);
        tess.draw();

        // Back face (z-)
        tess.startDrawingQuads();
        tess.setColorRGBA_F(r, g, b, alpha);
        tess.setNormal(0, 0, -1);
        tess.addVertex(halfWidth, -halfHeight, -halfDepth);
        tess.addVertex(-halfWidth, -halfHeight, -halfDepth);
        tess.addVertex(-halfWidth, halfHeight, -halfDepth);
        tess.addVertex(halfWidth, halfHeight, -halfDepth);
        tess.draw();

        // Top edge (y+)
        tess.startDrawingQuads();
        tess.setColorRGBA_F(r, g, b, alpha * 0.8f);
        tess.setNormal(0, 1, 0);
        tess.addVertex(-halfWidth, halfHeight, halfDepth);
        tess.addVertex(halfWidth, halfHeight, halfDepth);
        tess.addVertex(halfWidth, halfHeight, -halfDepth);
        tess.addVertex(-halfWidth, halfHeight, -halfDepth);
        tess.draw();

        // Bottom edge (y-)
        tess.startDrawingQuads();
        tess.setColorRGBA_F(r, g, b, alpha * 0.8f);
        tess.setNormal(0, -1, 0);
        tess.addVertex(-halfWidth, -halfHeight, -halfDepth);
        tess.addVertex(halfWidth, -halfHeight, -halfDepth);
        tess.addVertex(halfWidth, -halfHeight, halfDepth);
        tess.addVertex(-halfWidth, -halfHeight, halfDepth);
        tess.draw();

        // Right edge (x+)
        tess.startDrawingQuads();
        tess.setColorRGBA_F(r, g, b, alpha * 0.6f);
        tess.setNormal(1, 0, 0);
        tess.addVertex(halfWidth, -halfHeight, halfDepth);
        tess.addVertex(halfWidth, -halfHeight, -halfDepth);
        tess.addVertex(halfWidth, halfHeight, -halfDepth);
        tess.addVertex(halfWidth, halfHeight, halfDepth);
        tess.draw();

        // Left edge (x-)
        tess.startDrawingQuads();
        tess.setColorRGBA_F(r, g, b, alpha * 0.6f);
        tess.setNormal(-1, 0, 0);
        tess.addVertex(-halfWidth, -halfHeight, -halfDepth);
        tess.addVertex(-halfWidth, -halfHeight, halfDepth);
        tess.addVertex(-halfWidth, halfHeight, halfDepth);
        tess.addVertex(-halfWidth, halfHeight, -halfDepth);
        tess.draw();
    }

    /**
     * Render lightning arcs on the slicer.
     */
    private void renderAttachedLightning(EntityEnergySlicer slicer, float size) {
        AttachedLightningRenderer.LightningState state = getLightningState(slicer);
        float density = slicer.getLightningDensity();
        float radius = size + slicer.getLightningRadius();
        int outerColor = slicer.getOuterColor();
        int innerColor = slicer.getInnerColor();
        int fadeTime = slicer.getLightningFadeTime();

        state.update(density, radius, outerColor, innerColor, fadeTime);
        state.render();
    }

    private AttachedLightningRenderer.LightningState getLightningState(EntityEnergySlicer slicer) {
        if (slicer.lightningState == null) {
            slicer.lightningState = new AttachedLightningRenderer.LightningState();
        }
        return (AttachedLightningRenderer.LightningState) slicer.lightningState;
    }
}
