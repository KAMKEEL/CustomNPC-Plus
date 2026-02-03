package kamkeel.npcs.client.renderer;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.client.renderer.lightning.AttachedLightningRenderer;
import kamkeel.npcs.entity.EntityAbilityDisc;
import net.minecraft.entity.Entity;
import org.lwjgl.opengl.GL11;

/**
 * Renders the AbilityDisc entity as a flat spinning square.
 * Rotates only on Y axis (flat spin like a saw blade).
 *
 * Design inspired by LouisXIV's energy rendering system.
 */
@SideOnly(Side.CLIENT)
public class RenderAbilityDisc extends RenderAbilityProjectile {

    @Override
    public void doRender(Entity entity, double x, double y, double z, float yaw, float partialTicks) {
        EntityAbilityDisc disc = (EntityAbilityDisc) entity;

        setupRenderState();

        GL11.glPushMatrix();
        GL11.glTranslated(x, y, z);

        // Get interpolated values
        float size = disc.getInterpolatedSize(partialTicks);
        float radius = disc.getDiscRadius() * size;
        float thickness = disc.getDiscThickness() * size;

        // Subtle pulsing effect
        float pulseTime = entity.ticksExisted + partialTicks;
        float scaleModifier = (float) Math.sin(pulseTime * 0.1f) * 0.03f;
        float scale = 1.0f + scaleModifier;

        // Render lightning BEFORE rotation so it crackles in all directions
        if (disc.hasLightningEffect()) {
            renderAttachedLightning(disc, radius * scale);
        }

        GL11.glPushMatrix();
        GL11.glScalef(scale, scale, scale);

        // Apply ONLY Y-axis rotation for flat spin
        GL11.glRotatef(disc.getInterpolatedRotationY(partialTicks), 0.0f, 1.0f, 0.0f);

        // Inner scale defines the core size
        float innerScale = 0.6f;

        // Render outer square (translucent) - only if enabled
        // outerColorWidth is an additive offset from inner size
        if (disc.isOuterColorEnabled()) {
            float outerScale = innerScale + disc.getOuterColorWidth();
            GL11.glDepthMask(false);
            renderFlatSquare(disc.getOuterColor(), disc.getOuterColorAlpha(), radius * outerScale, thickness);
            GL11.glDepthMask(true);
        }

        // Render inner square (solid)
        renderFlatSquare(disc.getInnerColor(), 1.0f, radius * innerScale, thickness * 1.2f);

        GL11.glPopMatrix();
        GL11.glPopMatrix();

        restoreRenderState();
    }

    /**
     * Render fading lightning arcs attached to the disc (in local space).
     */
    private void renderAttachedLightning(EntityAbilityDisc disc, float discSize) {
        AttachedLightningRenderer.LightningState state = getLightningState(disc);

        float density = disc.getLightningDensity();
        // Lightning radius extends outward from inner surface (innerScale = 0.6)
        float innerRadius = 0.6f * discSize * 0.5f;
        float radius = innerRadius + disc.getLightningRadius() * discSize;
        int outerColor = disc.getOuterColor();
        int innerColor = disc.getInnerColor();
        int fadeTime = disc.getLightningFadeTime();

        state.update(density, radius, outerColor, innerColor, fadeTime);
        state.render();
    }

    /**
     * Get or create the lightning state for an entity.
     */
    private AttachedLightningRenderer.LightningState getLightningState(EntityAbilityDisc disc) {
        if (disc.lightningState == null) {
            disc.lightningState = new AttachedLightningRenderer.LightningState();
        }
        return (AttachedLightningRenderer.LightningState) disc.lightningState;
    }

    /**
     * Render a flat square (thin cube) centered at origin.
     * The square lies flat on the XZ plane with thin Y height.
     */
    private void renderFlatSquare(int color, float alpha, float halfSize, float thickness) {
        float halfThick = thickness * 0.5f;

        // Render as a thin cube
        renderCubeCustom(color, alpha, halfSize, halfThick, halfSize);
    }

    /**
     * Render a cube with custom dimensions.
     */
    private void renderCubeCustom(int color, float alpha, float halfX, float halfY, float halfZ) {
        float[] rgb = extractRGB(color);
        float r = rgb[0], g = rgb[1], b = rgb[2];

        net.minecraft.client.renderer.Tessellator tess = net.minecraft.client.renderer.Tessellator.instance;

        // Top face (y+)
        tess.startDrawingQuads();
        tess.setColorRGBA_F(r, g, b, alpha);
        tess.setNormal(0, 1, 0);
        tess.addVertex(-halfX, halfY, halfZ);
        tess.addVertex(halfX, halfY, halfZ);
        tess.addVertex(halfX, halfY, -halfZ);
        tess.addVertex(-halfX, halfY, -halfZ);
        tess.draw();

        // Bottom face (y-)
        tess.startDrawingQuads();
        tess.setColorRGBA_F(r, g, b, alpha);
        tess.setNormal(0, -1, 0);
        tess.addVertex(-halfX, -halfY, -halfZ);
        tess.addVertex(halfX, -halfY, -halfZ);
        tess.addVertex(halfX, -halfY, halfZ);
        tess.addVertex(-halfX, -halfY, halfZ);
        tess.draw();

        // Front face (z+)
        tess.startDrawingQuads();
        tess.setColorRGBA_F(r, g, b, alpha);
        tess.setNormal(0, 0, 1);
        tess.addVertex(-halfX, -halfY, halfZ);
        tess.addVertex(halfX, -halfY, halfZ);
        tess.addVertex(halfX, halfY, halfZ);
        tess.addVertex(-halfX, halfY, halfZ);
        tess.draw();

        // Back face (z-)
        tess.startDrawingQuads();
        tess.setColorRGBA_F(r, g, b, alpha);
        tess.setNormal(0, 0, -1);
        tess.addVertex(halfX, -halfY, -halfZ);
        tess.addVertex(-halfX, -halfY, -halfZ);
        tess.addVertex(-halfX, halfY, -halfZ);
        tess.addVertex(halfX, halfY, -halfZ);
        tess.draw();

        // Right face (x+)
        tess.startDrawingQuads();
        tess.setColorRGBA_F(r, g, b, alpha);
        tess.setNormal(1, 0, 0);
        tess.addVertex(halfX, -halfY, halfZ);
        tess.addVertex(halfX, -halfY, -halfZ);
        tess.addVertex(halfX, halfY, -halfZ);
        tess.addVertex(halfX, halfY, halfZ);
        tess.draw();

        // Left face (x-)
        tess.startDrawingQuads();
        tess.setColorRGBA_F(r, g, b, alpha);
        tess.setNormal(-1, 0, 0);
        tess.addVertex(-halfX, -halfY, -halfZ);
        tess.addVertex(-halfX, -halfY, halfZ);
        tess.addVertex(-halfX, halfY, halfZ);
        tess.addVertex(-halfX, halfY, -halfZ);
        tess.draw();
    }
}
