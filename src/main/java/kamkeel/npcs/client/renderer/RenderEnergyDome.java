package kamkeel.npcs.client.renderer;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.client.renderer.lightning.AttachedLightningRenderer;
import kamkeel.npcs.entity.EntityEnergyDome;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.Entity;
import org.lwjgl.opengl.GL11;

/**
 * Renders the Energy Dome entity as a translucent blocky hemisphere.
 * Built from cube panels forming a dome shape - voxel-blocky aesthetic.
 * Inner layer more opaque, outer layer translucent glow.
 */
@SideOnly(Side.CLIENT)
public class RenderEnergyDome extends RenderAbilityProjectile {

    @Override
    public void doRender(Entity entity, double x, double y, double z, float yaw, float partialTicks) {
        EntityEnergyDome dome = (EntityEnergyDome) entity;
        float radius = dome.getDomeRadius();
        float healthPercent = dome.getHealthPercent();

        setupRenderState();

        GL11.glPushMatrix();
        GL11.glTranslated(x, y, z);

        // Lightning
        if (dome.hasLightningEffect()) {
            renderAttachedLightning(dome, radius);
        }

        // Hit flash effect
        float flashAlpha = 0.0f;
        if (dome.getHitFlash() > 0) {
            flashAlpha = dome.getHitFlash() / 4.0f * 0.3f;
        }

        float innerScale = 0.95f;

        // Render outer dome (translucent)
        if (dome.isOuterColorEnabled()) {
            float outerScale = 1.0f + dome.getOuterColorWidth() * 0.1f;
            float outerAlpha = dome.getOuterColorAlpha() * healthPercent;
            GL11.glDepthMask(false);
            renderBlockyDome(dome.getOuterColor(), outerAlpha + flashAlpha, radius * outerScale, 8);
            GL11.glDepthMask(true);
        }

        // Render inner dome (more opaque)
        float innerAlpha = (0.5f + 0.3f * healthPercent);
        renderBlockyDome(dome.getInnerColor(), innerAlpha + flashAlpha, radius * innerScale, 8);

        GL11.glPopMatrix();

        restoreRenderState();
    }

    /**
     * Render a dome made of blocky quad panels - voxel style.
     * Uses latitude/longitude segments to form a hemisphere of flat panels.
     */
    private void renderBlockyDome(int color, float alpha, float radius, int segments) {
        float[] rgb = extractRGB(color);
        float r = rgb[0], g = rgb[1], b = rgb[2];

        Tessellator tess = Tessellator.instance;
        int latSegments = segments / 2; // Half sphere

        for (int lat = 0; lat < latSegments; lat++) {
            float theta1 = (float) (Math.PI * 0.5 * lat / latSegments);
            float theta2 = (float) (Math.PI * 0.5 * (lat + 1) / latSegments);

            float y1 = (float) Math.sin(theta1) * radius;
            float y2 = (float) Math.sin(theta2) * radius;
            float r1 = (float) Math.cos(theta1) * radius;
            float r2 = (float) Math.cos(theta2) * radius;

            for (int lon = 0; lon < segments; lon++) {
                float phi1 = (float) (2 * Math.PI * lon / segments);
                float phi2 = (float) (2 * Math.PI * (lon + 1) / segments);

                float x1 = (float) Math.cos(phi1) * r1;
                float z1 = (float) Math.sin(phi1) * r1;
                float x2 = (float) Math.cos(phi2) * r1;
                float z2 = (float) Math.sin(phi2) * r1;
                float x3 = (float) Math.cos(phi2) * r2;
                float z3 = (float) Math.sin(phi2) * r2;
                float x4 = (float) Math.cos(phi1) * r2;
                float z4 = (float) Math.sin(phi1) * r2;

                // Normal pointing outward
                float nx = (x1 + x2 + x3 + x4) * 0.25f;
                float ny = (y1 + y1 + y2 + y2) * 0.25f;
                float nz = (z1 + z2 + z3 + z4) * 0.25f;
                float nLen = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
                if (nLen > 0) { nx /= nLen; ny /= nLen; nz /= nLen; }

                tess.startDrawingQuads();
                tess.setColorRGBA_F(r, g, b, alpha);
                tess.setNormal(nx, ny, nz);
                tess.addVertex(x1, y1, z1);
                tess.addVertex(x2, y1, z2);
                tess.addVertex(x3, y2, z3);
                tess.addVertex(x4, y2, z4);
                tess.draw();
            }
        }

        // Bottom ring (ground level circle)
        tess.startDrawingQuads();
        tess.setColorRGBA_F(r, g, b, alpha * 0.5f);
        tess.setNormal(0, -1, 0);
        for (int lon = 0; lon < segments; lon++) {
            float phi1 = (float) (2 * Math.PI * lon / segments);
            float phi2 = (float) (2 * Math.PI * (lon + 1) / segments);
            float x1 = (float) Math.cos(phi1) * radius;
            float z1 = (float) Math.sin(phi1) * radius;
            float x2 = (float) Math.cos(phi2) * radius;
            float z2 = (float) Math.sin(phi2) * radius;

            tess.addVertex(0, 0, 0);
            tess.addVertex(x1, 0, z1);
            tess.addVertex(x2, 0, z2);
            tess.addVertex(0, 0, 0);
        }
        tess.draw();
    }

    /**
     * Render lightning arcs on the dome surface.
     */
    private void renderAttachedLightning(EntityEnergyDome dome, float radius) {
        AttachedLightningRenderer.LightningState state = getLightningState(dome);
        float density = dome.getLightningDensity();
        float lightningRadius = radius + dome.getLightningRadius();
        int outerColor = dome.getOuterColor();
        int innerColor = dome.getInnerColor();
        int fadeTime = dome.getLightningFadeTime();

        state.update(density, lightningRadius, outerColor, innerColor, fadeTime);
        state.render();
    }

    private AttachedLightningRenderer.LightningState getLightningState(EntityEnergyDome dome) {
        if (dome.lightningState == null) {
            dome.lightningState = new AttachedLightningRenderer.LightningState();
        }
        return (AttachedLightningRenderer.LightningState) dome.lightningState;
    }
}
