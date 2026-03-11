package kamkeel.npcs.client.renderer;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
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
public class RenderEnergySlicer extends RenderEnergy {

    @Override
    public void doRender(Entity entity, double x, double y, double z, float yaw, float partialTicks) {
        if (shouldSkipInitialActiveRender(entity)) {
            return;
        }
        EntityEnergySlicer slicer = (EntityEnergySlicer) entity;

        float width = slicer.getSliceWidth();
        float thickness = slicer.getSliceThickness();

        // During charging, width/thickness grow from 0.01 to target via updateCharging().
        // Use current dimensions directly - no separate sizeRatio needed.
        if (width <= 0.01f) return;

        setupRenderState();

        GL11.glPushMatrix();
        GL11.glTranslated(x, y, z);

        // Lightning BEFORE rotation for uniform spread (like Orb pattern)
        if (slicer.hasLightningEffect()) {
            renderAttachedLightning(slicer, 0.7f, width * 0.5f);
        }

        // Orient perpendicular to travel direction
        float travelYaw = slicer.getTravelYaw();
        float travelPitch = slicer.getTravelPitch();
        GL11.glRotatef(travelYaw, 0.0f, 1.0f, 0.0f);
        GL11.glRotatef(travelPitch, 1.0f, 0.0f, 0.0f);

        float innerScale = 0.7f;

        // Proximity alpha fade for owner
        float proximityAlpha = getProximityAlphaFactor(slicer, x, y, z);

        // Render outer blade (translucent glow)
        if (slicer.isOuterColorEnabled()) {
            float outerScale = 1.0f + slicer.getOuterColorWidth() * 0.2f;
            GL11.glDepthMask(false);
            renderBlade(slicer.getOuterColor(), slicer.getOuterColorAlpha() * proximityAlpha,
                width * 0.5f * outerScale, thickness * 2.0f, 0.05f);
            GL11.glDepthMask(true);
        }

        // Render inner blade (solid core)
        renderBlade(slicer.getInnerColor(), slicer.getInnerAlpha() * proximityAlpha,
            width * 0.5f * innerScale, thickness, 0.03f);

        GL11.glPopMatrix();

        restoreRenderState();
    }

    /**
     * Render a thin wide blade shape.
     * The blade extends along X (width), thin on Y (thickness), and very thin on Z (depth).
     * All 6 faces batched into a single draw call.
     */
    private void renderBlade(int color, float alpha, float halfWidth, float halfHeight, float halfDepth) {
        float[] rgb = extractRGB(color);
        float r = rgb[0], g = rgb[1], b = rgb[2];

        Tessellator tess = Tessellator.instance;
        tess.startDrawingQuads();

        // Front face (z+)
        tess.setColorRGBA_F(r, g, b, alpha);
        tess.setNormal(0, 0, 1);
        tess.addVertex(-halfWidth, -halfHeight, halfDepth);
        tess.addVertex(halfWidth, -halfHeight, halfDepth);
        tess.addVertex(halfWidth, halfHeight, halfDepth);
        tess.addVertex(-halfWidth, halfHeight, halfDepth);

        // Back face (z-)
        tess.setNormal(0, 0, -1);
        tess.addVertex(halfWidth, -halfHeight, -halfDepth);
        tess.addVertex(-halfWidth, -halfHeight, -halfDepth);
        tess.addVertex(-halfWidth, halfHeight, -halfDepth);
        tess.addVertex(halfWidth, halfHeight, -halfDepth);

        // Top edge (y+)
        tess.setColorRGBA_F(r, g, b, alpha * 0.8f);
        tess.setNormal(0, 1, 0);
        tess.addVertex(-halfWidth, halfHeight, halfDepth);
        tess.addVertex(halfWidth, halfHeight, halfDepth);
        tess.addVertex(halfWidth, halfHeight, -halfDepth);
        tess.addVertex(-halfWidth, halfHeight, -halfDepth);

        // Bottom edge (y-)
        tess.setNormal(0, -1, 0);
        tess.addVertex(-halfWidth, -halfHeight, -halfDepth);
        tess.addVertex(halfWidth, -halfHeight, -halfDepth);
        tess.addVertex(halfWidth, -halfHeight, halfDepth);
        tess.addVertex(-halfWidth, -halfHeight, halfDepth);

        // Right edge (x+)
        tess.setColorRGBA_F(r, g, b, alpha * 0.6f);
        tess.setNormal(1, 0, 0);
        tess.addVertex(halfWidth, -halfHeight, halfDepth);
        tess.addVertex(halfWidth, -halfHeight, -halfDepth);
        tess.addVertex(halfWidth, halfHeight, -halfDepth);
        tess.addVertex(halfWidth, halfHeight, halfDepth);

        // Left edge (x-)
        tess.setNormal(-1, 0, 0);
        tess.addVertex(-halfWidth, -halfHeight, -halfDepth);
        tess.addVertex(-halfWidth, -halfHeight, halfDepth);
        tess.addVertex(-halfWidth, halfHeight, halfDepth);
        tess.addVertex(-halfWidth, halfHeight, -halfDepth);

        tess.draw();
    }

}
