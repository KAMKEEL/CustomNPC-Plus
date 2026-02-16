package kamkeel.npcs.client.renderer;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.client.renderer.lightning.AttachedLightningRenderer;
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
public class RenderEnergyPanel extends RenderAbilityProjectile {

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

        // Rotate panel to face the correct direction
        GL11.glRotatef(-panelYaw, 0.0f, 1.0f, 0.0f);

        // Lightning (before rotation for uniform spread)
        if (panel.hasLightningEffect()) {
            renderAttachedLightning(panel, Math.max(width, height) * 0.5f);
        }

        // Hit flash
        float flashAlpha = 0.0f;
        if (panel.getHitFlash() > 0) {
            flashAlpha = panel.getHitFlash() / 4.0f * 0.3f;
        }

        // Subtle pulsing
        float pulseTime = entity.ticksExisted + partialTicks;
        float pulse = (float) Math.sin(pulseTime * 0.08f) * 0.02f;
        float scale = 1.0f + pulse;

        GL11.glPushMatrix();
        GL11.glScalef(scale, scale, scale);

        float innerScale = 0.9f;
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

        // Render inner panel (more opaque core)
        float innerAlpha = (0.6f + 0.3f * healthPercent);
        renderPanel(panel.getInnerColor(), innerAlpha + flashAlpha,
            width * 0.5f * innerScale, height * 0.5f * innerScale, panelThickness);

        GL11.glPopMatrix();
        GL11.glPopMatrix();

        restoreRenderState();
    }

    /**
     * Render a flat panel (thin box) centered at origin.
     * Panel faces along the Z axis (width on X, height on Y).
     */
    private void renderPanel(int color, float alpha, float halfWidth, float halfHeight, float halfThickness) {
        float[] rgb = extractRGB(color);
        float r = rgb[0], g = rgb[1], b = rgb[2];

        Tessellator tess = Tessellator.instance;

        // Front face (z+)
        tess.startDrawingQuads();
        tess.setColorRGBA_F(r, g, b, alpha);
        tess.setNormal(0, 0, 1);
        tess.addVertex(-halfWidth, -halfHeight, halfThickness);
        tess.addVertex(halfWidth, -halfHeight, halfThickness);
        tess.addVertex(halfWidth, halfHeight, halfThickness);
        tess.addVertex(-halfWidth, halfHeight, halfThickness);
        tess.draw();

        // Back face (z-)
        tess.startDrawingQuads();
        tess.setColorRGBA_F(r, g, b, alpha);
        tess.setNormal(0, 0, -1);
        tess.addVertex(halfWidth, -halfHeight, -halfThickness);
        tess.addVertex(-halfWidth, -halfHeight, -halfThickness);
        tess.addVertex(-halfWidth, halfHeight, -halfThickness);
        tess.addVertex(halfWidth, halfHeight, -halfThickness);
        tess.draw();

        // Top edge (y+)
        tess.startDrawingQuads();
        tess.setColorRGBA_F(r, g, b, alpha);
        tess.setNormal(0, 1, 0);
        tess.addVertex(-halfWidth, halfHeight, halfThickness);
        tess.addVertex(halfWidth, halfHeight, halfThickness);
        tess.addVertex(halfWidth, halfHeight, -halfThickness);
        tess.addVertex(-halfWidth, halfHeight, -halfThickness);
        tess.draw();

        // Bottom edge (y-)
        tess.startDrawingQuads();
        tess.setColorRGBA_F(r, g, b, alpha);
        tess.setNormal(0, -1, 0);
        tess.addVertex(-halfWidth, -halfHeight, -halfThickness);
        tess.addVertex(halfWidth, -halfHeight, -halfThickness);
        tess.addVertex(halfWidth, -halfHeight, halfThickness);
        tess.addVertex(-halfWidth, -halfHeight, halfThickness);
        tess.draw();

        // Right edge (x+)
        tess.startDrawingQuads();
        tess.setColorRGBA_F(r, g, b, alpha);
        tess.setNormal(1, 0, 0);
        tess.addVertex(halfWidth, -halfHeight, halfThickness);
        tess.addVertex(halfWidth, -halfHeight, -halfThickness);
        tess.addVertex(halfWidth, halfHeight, -halfThickness);
        tess.addVertex(halfWidth, halfHeight, halfThickness);
        tess.draw();

        // Left edge (x-)
        tess.startDrawingQuads();
        tess.setColorRGBA_F(r, g, b, alpha);
        tess.setNormal(-1, 0, 0);
        tess.addVertex(-halfWidth, -halfHeight, -halfThickness);
        tess.addVertex(-halfWidth, -halfHeight, halfThickness);
        tess.addVertex(-halfWidth, halfHeight, halfThickness);
        tess.addVertex(-halfWidth, halfHeight, -halfThickness);
        tess.draw();
    }

    /**
     * Render lightning arcs on the panel surface.
     */
    private void renderAttachedLightning(EntityEnergyPanel panel, float size) {
        AttachedLightningRenderer.LightningState state = getLightningState(panel);
        float density = panel.getLightningDensity();
        float radius = size + panel.getLightningRadius();
        int outerColor = panel.getOuterColor();
        int innerColor = panel.getInnerColor();
        int fadeTime = panel.getLightningFadeTime();

        state.update(density, radius, outerColor, innerColor, fadeTime);
        state.render();
    }

    private AttachedLightningRenderer.LightningState getLightningState(EntityEnergyPanel panel) {
        if (panel.lightningState == null) {
            panel.lightningState = new AttachedLightningRenderer.LightningState();
        }
        return (AttachedLightningRenderer.LightningState) panel.lightningState;
    }
}
