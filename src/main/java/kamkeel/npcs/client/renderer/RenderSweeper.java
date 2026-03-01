package kamkeel.npcs.client.renderer;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.entity.EntityEnergySweeper;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/**
 * Renders the Sweeper beam as a thin rotating laser attached to the NPC.
 * Uses similar rendering style to the Beam trail for visual consistency.
 */
@SideOnly(Side.CLIENT)
public class RenderSweeper extends Render {

    @Override
    public void doRender(Entity entity, double x, double y, double z, float yaw, float partialTicks) {
        EntityEnergySweeper sweeper = (EntityEnergySweeper) entity;

        setupRenderState();

        float beamLength = sweeper.getBeamLength();
        float beamWidth = sweeper.getBeamWidth();
        float beamHeight = sweeper.getBeamHeight();
        int innerColor = sweeper.getInnerColor();
        int outerColor = sweeper.getOuterColor();
        boolean outerColorEnabled = sweeper.isOuterColorEnabled();
        float outerColorWidth = sweeper.getOuterColorWidth();
        float angle = sweeper.getInterpolatedAngle(partialTicks);

        GL11.glPushMatrix();
        GL11.glTranslated(x, y, z);

        // Rotate the beam around Y axis (angle already includes baseYaw)
        GL11.glRotatef(-angle, 0, 1, 0);  // Negative for correct direction

        // Render the beam extending in the +Z direction (like beam trail segments)
        renderBeam(beamLength, beamWidth, beamHeight, innerColor, outerColor, outerColorEnabled, outerColorWidth);

        GL11.glPopMatrix();

        restoreRenderState();
    }

    private void setupRenderState() {
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glShadeModel(GL11.GL_SMOOTH);

        // Force full brightness - prevents world lighting from darkening sweeper
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0f, 240.0f);
    }

    private void restoreRenderState() {
        GL11.glShadeModel(GL11.GL_FLAT);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glPopAttrib();
    }

    /**
     * Render beam using the same style as the Beam entity trail.
     * Three layers: outer glow, middle, inner core.
     * outerColorWidth is an additive offset from inner width (consistent with other renderers).
     * Width and height control the rectangular cross-section independently.
     */
    private void renderBeam(float length, float width, float height, int innerColor, int outerColor,
                            boolean outerColorEnabled, float outerColorWidth) {
        // Extract colors
        float outerR = ((outerColor >> 16) & 0xFF) / 255.0f;
        float outerG = ((outerColor >> 8) & 0xFF) / 255.0f;
        float outerB = (outerColor & 0xFF) / 255.0f;

        float innerR = ((innerColor >> 16) & 0xFF) / 255.0f;
        float innerG = ((innerColor >> 8) & 0xFF) / 255.0f;
        float innerB = (innerColor & 0xFF) / 255.0f;

        // Inner scale defines the core size (consistent with other renderers)
        float innerScale = 0.6f;
        float innerWidth = width * innerScale;
        float innerHeight = height * innerScale;

        // Render from origin (0,0,0) to (0,0,length) in local coords
        // After rotation this extends in the correct world direction

        // Outer glow (wider, translucent) - only if enabled
        // outerColorWidth is additive offset from inner size
        if (outerColorEnabled) {
            float outerWidth = innerWidth + outerColorWidth * width;
            float outerHeight = innerHeight + outerColorWidth * height;
            GL11.glDepthMask(false);
            renderBeamSegment(0, 0, 0, 0, 0, length, outerWidth, outerHeight, outerR, outerG, outerB, 0.3f);
            GL11.glDepthMask(true);

            // Middle layer (halfway between inner and outer)
            float midWidth = innerWidth + (outerWidth - innerWidth) * 0.5f;
            float midHeight = innerHeight + (outerHeight - innerHeight) * 0.5f;
            renderBeamSegment(0, 0, 0, 0, 0, length, midWidth, midHeight, outerR, outerG, outerB, 0.6f);
        }

        // Inner core (solid)
        renderBeamSegment(0, 0, 0, 0, 0, length, innerWidth, innerHeight, innerR, innerG, innerB, 1.0f);
    }

    /**
     * Render a beam segment as a 3D rectangular prism with independent width and height.
     * Uses the same approach as RenderEnergyBeam.renderBeamRectangle.
     */
    private void renderBeamSegment(double x1, double y1, double z1,
                                   double x2, double y2, double z2,
                                   float width, float height, float r, float g, float b, float a) {
        // Calculate direction
        double dx = x2 - x1;
        double dy = y2 - y1;
        double dz = z2 - z1;
        double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len < 0.001) return;

        // Normalize direction
        dx /= len;
        dy /= len;
        dz /= len;

        // Calculate perpendicular vectors
        double upX = 0, upY = 1, upZ = 0;
        if (Math.abs(dy) > 0.9) {
            upX = 1;
            upY = 0;
            upZ = 0;
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

        float halfWidth = width * 0.5f;
        float halfHeight = height * 0.5f;

        // Calculate 8 corners of the box (horz scaled by halfWidth, vert scaled by halfHeight)
        double s1x = x1 - horzX * halfWidth - vertX * halfHeight;
        double s1y = y1 - horzY * halfWidth - vertY * halfHeight;
        double s1z = z1 - horzZ * halfWidth - vertZ * halfHeight;

        double s2x = x1 + horzX * halfWidth - vertX * halfHeight;
        double s2y = y1 + horzY * halfWidth - vertY * halfHeight;
        double s2z = z1 + horzZ * halfWidth - vertZ * halfHeight;

        double s3x = x1 + horzX * halfWidth + vertX * halfHeight;
        double s3y = y1 + horzY * halfWidth + vertY * halfHeight;
        double s3z = z1 + horzZ * halfWidth + vertZ * halfHeight;

        double s4x = x1 - horzX * halfWidth + vertX * halfHeight;
        double s4y = y1 - horzY * halfWidth + vertY * halfHeight;
        double s4z = z1 - horzZ * halfWidth + vertZ * halfHeight;

        double e1x = x2 - horzX * halfWidth - vertX * halfHeight;
        double e1y = y2 - horzY * halfWidth - vertY * halfHeight;
        double e1z = z2 - horzZ * halfWidth - vertZ * halfHeight;

        double e2x = x2 + horzX * halfWidth - vertX * halfHeight;
        double e2y = y2 + horzY * halfWidth - vertY * halfHeight;
        double e2z = z2 + horzZ * halfWidth - vertZ * halfHeight;

        double e3x = x2 + horzX * halfWidth + vertX * halfHeight;
        double e3y = y2 + horzY * halfWidth + vertY * halfHeight;
        double e3z = z2 + horzZ * halfWidth + vertZ * halfHeight;

        double e4x = x2 - horzX * halfWidth + vertX * halfHeight;
        double e4y = y2 - horzY * halfWidth + vertY * halfHeight;
        double e4z = z2 - horzZ * halfWidth + vertZ * halfHeight;

        Tessellator tess = Tessellator.instance;
        tess.startDrawingQuads();
        tess.setColorRGBA_F(r, g, b, a);

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

        // End cap (tip of beam)
        tess.addVertex(e1x, e1y, e1z);
        tess.addVertex(e2x, e2y, e2z);
        tess.addVertex(e3x, e3y, e3z);
        tess.addVertex(e4x, e4y, e4z);

        tess.draw();
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity entity) {
        return null;
    }
}
