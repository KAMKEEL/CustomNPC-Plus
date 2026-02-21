package kamkeel.npcs.client.renderer;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.entity.EntityEnergyPanel;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.Entity;
import org.lwjgl.opengl.GL11;

/**
 * Renders the Energy Panel entity as a flat translucent rectangle.
 * Used by both Energy Wall and Energy Shield abilities.
 * Voxel-blocky style with inner/outer layers.
 */
@SideOnly(Side.CLIENT)
public class RenderEnergyPanel extends RenderEnergyBarrier {

    @Override
    public void doRender(Entity entity, double x, double y, double z, float yaw, float partialTicks) {
        EntityEnergyPanel panel = (EntityEnergyPanel) entity;

        float width = panel.getPanelData().panelWidth;
        float height = panel.getPanelData().panelHeight;
        float panelYaw = panel.getPanelYaw();
        float healthPercent = panel.getHealthPercent();

        setupRenderState();

        GL11.glPushMatrix();
        GL11.glTranslated(x, y, z);

        // Lightning BEFORE rotation for uniform spread (like Orb pattern)
        if (panel.hasLightningEffect()) {
            renderAttachedLightning(panel, 0.9f, Math.max(width, height) * 0.5f);
        }

        // Rotate panel to face the correct direction
        GL11.glRotatef(-panelYaw, 0.0f, 1.0f, 0.0f);

        // Hit flash
        float flashAlpha = computeFlashAlpha(panel);

        // Subtle pulsing
        float pulseTime = entity.ticksExisted + partialTicks;
        float pulse = (float) Math.sin(pulseTime * 0.08f) * 0.02f;
        float scale = 1.0f + pulse;

        GL11.glPushMatrix();
        GL11.glScalef(scale, scale, scale);

        float innerScale = 1.0f;
        float panelThickness = 0.1f;

        // Render outer panel (translucent glow)
        if (panel.isOuterColorEnabled()) {
            float outerScale = 1.0f + panel.getOuterColorWidth() * 0.15f;
            float outerAlpha = panel.getOuterColorAlpha() * healthPercent;
            GL11.glDepthMask(false);
            renderPanel(panel.getOuterColor(), outerAlpha + flashAlpha,
                width * 0.5f * outerScale, height * 0.5f * outerScale, panelThickness * 1.5f);
            GL11.glDepthMask(true);
        }

        // Render inner panel (uses configurable innerAlpha for semi-transparency)
        renderPanel(panel.getInnerColor(), panel.getInnerAlpha(),
            width * 0.5f * innerScale, height * 0.5f * innerScale, panelThickness);

        GL11.glPopMatrix();
        GL11.glPopMatrix();

        restoreRenderState();
    }

    /**
     * Render a flat panel (thin box) centered at origin.
     * Panel faces along the Z axis (width on X, height on Y).
     * All 6 faces batched into a single draw call.
     */
    private void renderPanel(int color, float alpha, float halfWidth, float halfHeight, float halfThickness) {
        float[] rgb = extractRGB(color);
        float r = rgb[0], g = rgb[1], b = rgb[2];

        Tessellator tess = Tessellator.instance;
        tess.startDrawingQuads();
        tess.setColorRGBA_F(r, g, b, alpha);

        // Front face (z+)
        tess.setNormal(0, 0, 1);
        tess.addVertex(-halfWidth, -halfHeight, halfThickness);
        tess.addVertex(halfWidth, -halfHeight, halfThickness);
        tess.addVertex(halfWidth, halfHeight, halfThickness);
        tess.addVertex(-halfWidth, halfHeight, halfThickness);

        // Back face (z-)
        tess.setNormal(0, 0, -1);
        tess.addVertex(halfWidth, -halfHeight, -halfThickness);
        tess.addVertex(-halfWidth, -halfHeight, -halfThickness);
        tess.addVertex(-halfWidth, halfHeight, -halfThickness);
        tess.addVertex(halfWidth, halfHeight, -halfThickness);

        // Top edge (y+)
        tess.setNormal(0, 1, 0);
        tess.addVertex(-halfWidth, halfHeight, halfThickness);
        tess.addVertex(halfWidth, halfHeight, halfThickness);
        tess.addVertex(halfWidth, halfHeight, -halfThickness);
        tess.addVertex(-halfWidth, halfHeight, -halfThickness);

        // Bottom edge (y-)
        tess.setNormal(0, -1, 0);
        tess.addVertex(-halfWidth, -halfHeight, -halfThickness);
        tess.addVertex(halfWidth, -halfHeight, -halfThickness);
        tess.addVertex(halfWidth, -halfHeight, halfThickness);
        tess.addVertex(-halfWidth, -halfHeight, halfThickness);

        // Right edge (x+)
        tess.setNormal(1, 0, 0);
        tess.addVertex(halfWidth, -halfHeight, halfThickness);
        tess.addVertex(halfWidth, -halfHeight, -halfThickness);
        tess.addVertex(halfWidth, halfHeight, -halfThickness);
        tess.addVertex(halfWidth, halfHeight, halfThickness);

        // Left edge (x-)
        tess.setNormal(-1, 0, 0);
        tess.addVertex(-halfWidth, -halfHeight, -halfThickness);
        tess.addVertex(-halfWidth, -halfHeight, halfThickness);
        tess.addVertex(-halfWidth, halfHeight, halfThickness);
        tess.addVertex(-halfWidth, halfHeight, -halfThickness);

        tess.draw();
    }

}
