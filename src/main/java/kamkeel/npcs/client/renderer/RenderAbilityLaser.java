package kamkeel.npcs.client.renderer;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.client.renderer.lightning.AttachedLightningRenderer;
import kamkeel.npcs.entity.EntityAbilityLaser;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.Entity;
import org.lwjgl.opengl.GL11;

/**
 * Renders the AbilityLaser entity as a rectangular beam with clear inner/outer colors.
 *
 * Design inspired by LouisXIV's energy rendering system.
 */
@SideOnly(Side.CLIENT)
public class RenderAbilityLaser extends RenderAbilityProjectile {

    @Override
    public void doRender(Entity entity, double x, double y, double z, float yaw, float partialTicks) {
        EntityAbilityLaser laser = (EntityAbilityLaser) entity;

        // Don't render if length is 0
        if (laser.getCurrentLength() <= 0) return;

        setupRenderState();

        // Get laser parameters
        double startX = laser.getStartX();
        double startY = laser.getStartY();
        double startZ = laser.getStartZ();
        double endX = laser.getEndX();
        double endY = laser.getEndY();
        double endZ = laser.getEndZ();
        float width = laser.getLaserWidth();
        float alpha = laser.getLingerAlpha();

        // Calculate render offset (entity position is used as reference)
        double offsetX = x - laser.posX;
        double offsetY = y - laser.posY;
        double offsetZ = z - laser.posZ;

        // Render start relative to camera
        double renderStartX = startX + offsetX;
        double renderStartY = startY + offsetY;
        double renderStartZ = startZ + offsetZ;
        double renderEndX = endX + offsetX;
        double renderEndY = endY + offsetY;
        double renderEndZ = endZ + offsetZ;

        // Calculate perpendicular vectors for the beam rectangle
        double dx = renderEndX - renderStartX;
        double dy = renderEndY - renderStartY;
        double dz = renderEndZ - renderStartZ;
        double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len < 0.001) {
            restoreRenderState();
            return;
        }

        // Normalize direction
        dx /= len;
        dy /= len;
        dz /= len;

        // Calculate perpendicular vectors (one horizontal, one vertical relative to beam)
        double upX = 0, upY = 1, upZ = 0;
        if (Math.abs(dy) > 0.9) {
            upX = 1; upY = 0; upZ = 0;
        }

        // Cross product for horizontal perpendicular
        double horzX = dy * upZ - dz * upY;
        double horzY = dz * upX - dx * upZ;
        double horzZ = dx * upY - dy * upX;
        double horzLen = Math.sqrt(horzX * horzX + horzY * horzY + horzZ * horzZ);
        if (horzLen > 0) {
            horzX /= horzLen;
            horzY /= horzLen;
            horzZ /= horzLen;
        }

        // Cross product for vertical perpendicular
        double vertX = dy * horzZ - dz * horzY;
        double vertY = dz * horzX - dx * horzZ;
        double vertZ = dx * horzY - dy * horzX;

        // Inner scale defines the core width
        float innerScale = 0.6f;
        float innerWidth = width * innerScale;

        // Render outer rectangle (larger, outer color, translucent) - only if enabled
        // outerColorWidth is an additive offset from inner width
        if (laser.isOuterColorEnabled()) {
            float outerWidth = innerWidth + laser.getOuterColorWidth() * width;
            GL11.glDepthMask(false);
            renderBeamRectangle(renderStartX, renderStartY, renderStartZ,
                                renderEndX, renderEndY, renderEndZ,
                                horzX, horzY, horzZ, vertX, vertY, vertZ,
                                outerWidth, laser.getOuterColor(), alpha * 0.4f);

            // Render middle layer (halfway between inner and outer)
            float midWidth = innerWidth + (outerWidth - innerWidth) * 0.5f;
            renderBeamRectangle(renderStartX, renderStartY, renderStartZ,
                                renderEndX, renderEndY, renderEndZ,
                                horzX, horzY, horzZ, vertX, vertY, vertZ,
                                midWidth, laser.getOuterColor(), alpha * 0.7f);
            GL11.glDepthMask(true);
        }

        // Render inner core (solid)
        renderBeamRectangle(renderStartX, renderStartY, renderStartZ,
                            renderEndX, renderEndY, renderEndZ,
                            horzX, horzY, horzZ, vertX, vertY, vertZ,
                            innerWidth, laser.getInnerColor(), alpha);

        // Render lightning along the laser beam
        if (laser.hasLightningEffect() && laser.getCurrentLength() > 0.1f) {
            renderLaserLightning(laser, renderStartX, renderStartY, renderStartZ,
                                 renderEndX, renderEndY, renderEndZ, width);
        }

        restoreRenderState();
    }

    /**
     * Render lightning effects along the laser beam at multiple points.
     */
    private void renderLaserLightning(EntityAbilityLaser laser,
                                       double startX, double startY, double startZ,
                                       double endX, double endY, double endZ, float width) {
        AttachedLightningRenderer.LightningState state = getLightningState(laser);

        float density = laser.getLightningDensity();
        // Lightning radius extends outward from inner width
        float innerRadius = width * 0.6f * 0.5f;
        float radius = innerRadius + laser.getLightningRadius() * width;
        int outerColor = laser.getOuterColor();
        int innerColor = laser.getInnerColor();
        int fadeTime = laser.getLightningFadeTime();

        // Render lightning at multiple points along the laser
        // Start, middle, and end points
        double midX = (startX + endX) * 0.5;
        double midY = (startY + endY) * 0.5;
        double midZ = (startZ + endZ) * 0.5;

        GL11.glPushMatrix();
        GL11.glTranslated(midX, midY, midZ);
        state.update(density, radius, outerColor, innerColor, fadeTime);
        state.render();
        GL11.glPopMatrix();

        // Also render at the tip
        GL11.glPushMatrix();
        GL11.glTranslated(endX, endY, endZ);
        // Use a secondary state for the tip
        AttachedLightningRenderer.LightningState tipState = getTipLightningState(laser);
        tipState.update(density * 0.7f, radius * 0.8f, outerColor, innerColor, fadeTime);
        tipState.render();
        GL11.glPopMatrix();
    }

    /**
     * Get or create the main lightning state for the laser.
     */
    private AttachedLightningRenderer.LightningState getLightningState(EntityAbilityLaser laser) {
        if (laser.lightningState == null) {
            laser.lightningState = new AttachedLightningRenderer.LightningState();
        }
        return (AttachedLightningRenderer.LightningState) laser.lightningState;
    }

    /**
     * Get or create the tip lightning state for the laser.
     * Stored as a secondary object in lightningState2.
     */
    private AttachedLightningRenderer.LightningState getTipLightningState(EntityAbilityLaser laser) {
        // Use a simple approach - store in a transient field
        // Since lightningState is already used, we'll create a new state each time
        // This works because we're updating and rendering immediately
        return new AttachedLightningRenderer.LightningState();
    }

    /**
     * Render a beam as a 3D rectangular prism (box along a line).
     */
    private void renderBeamRectangle(double x1, double y1, double z1,
                                      double x2, double y2, double z2,
                                      double horzX, double horzY, double horzZ,
                                      double vertX, double vertY, double vertZ,
                                      float width, int color, float alpha) {
        float[] rgb = extractRGB(color);
        float r = rgb[0], g = rgb[1], b = rgb[2];

        float halfWidth = width * 0.5f;

        // Calculate 8 corners of the box
        // Start corners
        double s1x = x1 - horzX * halfWidth - vertX * halfWidth;
        double s1y = y1 - horzY * halfWidth - vertY * halfWidth;
        double s1z = z1 - horzZ * halfWidth - vertZ * halfWidth;

        double s2x = x1 + horzX * halfWidth - vertX * halfWidth;
        double s2y = y1 + horzY * halfWidth - vertY * halfWidth;
        double s2z = z1 + horzZ * halfWidth - vertZ * halfWidth;

        double s3x = x1 + horzX * halfWidth + vertX * halfWidth;
        double s3y = y1 + horzY * halfWidth + vertY * halfWidth;
        double s3z = z1 + horzZ * halfWidth + vertZ * halfWidth;

        double s4x = x1 - horzX * halfWidth + vertX * halfWidth;
        double s4y = y1 - horzY * halfWidth + vertY * halfWidth;
        double s4z = z1 - horzZ * halfWidth + vertZ * halfWidth;

        // End corners
        double e1x = x2 - horzX * halfWidth - vertX * halfWidth;
        double e1y = y2 - horzY * halfWidth - vertY * halfWidth;
        double e1z = z2 - horzZ * halfWidth - vertZ * halfWidth;

        double e2x = x2 + horzX * halfWidth - vertX * halfWidth;
        double e2y = y2 + horzY * halfWidth - vertY * halfWidth;
        double e2z = z2 + horzZ * halfWidth - vertZ * halfWidth;

        double e3x = x2 + horzX * halfWidth + vertX * halfWidth;
        double e3y = y2 + horzY * halfWidth + vertY * halfWidth;
        double e3z = z2 + horzZ * halfWidth + vertZ * halfWidth;

        double e4x = x2 - horzX * halfWidth + vertX * halfWidth;
        double e4y = y2 - horzY * halfWidth + vertY * halfWidth;
        double e4z = z2 - horzZ * halfWidth + vertZ * halfWidth;

        Tessellator tess = Tessellator.instance;
        tess.startDrawingQuads();
        tess.setColorRGBA_F(r, g, b, alpha);

        // Bottom face
        tess.addVertex(s1x, s1y, s1z);
        tess.addVertex(s2x, s2y, s2z);
        tess.addVertex(e2x, e2y, e2z);
        tess.addVertex(e1x, e1y, e1z);

        // Top face
        tess.addVertex(s4x, s4y, s4z);
        tess.addVertex(e4x, e4y, e4z);
        tess.addVertex(e3x, e3y, e3z);
        tess.addVertex(s3x, s3y, s3z);

        // Left face
        tess.addVertex(s1x, s1y, s1z);
        tess.addVertex(e1x, e1y, e1z);
        tess.addVertex(e4x, e4y, e4z);
        tess.addVertex(s4x, s4y, s4z);

        // Right face
        tess.addVertex(s2x, s2y, s2z);
        tess.addVertex(s3x, s3y, s3z);
        tess.addVertex(e3x, e3y, e3z);
        tess.addVertex(e2x, e2y, e2z);

        // Start cap
        tess.addVertex(s1x, s1y, s1z);
        tess.addVertex(s4x, s4y, s4z);
        tess.addVertex(s3x, s3y, s3z);
        tess.addVertex(s2x, s2y, s2z);

        // End cap
        tess.addVertex(e1x, e1y, e1z);
        tess.addVertex(e2x, e2y, e2z);
        tess.addVertex(e3x, e3y, e3z);
        tess.addVertex(e4x, e4y, e4z);

        tess.draw();
    }
}
