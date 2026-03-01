package kamkeel.npcs.client.renderer;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.entity.EntityAbilityDome;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.Entity;
import org.lwjgl.opengl.GL11;

/**
 * Renders the Dome entity as a translucent sphere.
 * Inner layer solid, outer layer translucent glow.
 * Optional skybox texture rendered on the interior surface.
 */
@SideOnly(Side.CLIENT)
public class RenderAbilityDome extends RenderEnergyBarrier {

    @Override
    public void doRender(Entity entity, double x, double y, double z, float yaw, float partialTicks) {
        if (shouldSkipInitialActiveRender(entity)) {
            return;
        }
        EntityAbilityDome dome = (EntityAbilityDome) entity;
        float radius = dome.getDomeRadius();
        float healthPercent = dome.getHealthPercent();

        setupRenderState();

        GL11.glPushMatrix();
        GL11.glTranslated(x, y, z);

        // Lightning BEFORE any rotation (uniform spread)
        if (dome.hasLightningEffect()) {
            renderAttachedLightning(dome, 0.95f, radius);
        }

        // Hit flash effect
        float flashAlpha = computeFlashAlpha(dome);

        float innerScale = 1.0f;

        // Render outer dome (translucent glow)
        if (dome.isOuterColorEnabled()) {
            float outerScale = 1.0f + dome.getOuterColorWidth() * 0.1f;
            float outerAlpha = dome.getOuterColorAlpha() * healthPercent;
            GL11.glDepthMask(false);
            renderSphere(dome.getOuterColor(), outerAlpha + (dome.getOuterColorAlpha() > 0 ? flashAlpha : 0), radius * outerScale, 8);
        }

        // TODO: Skybox Feature
        // if (dome.isSkyboxEnabled() && !dome.getSkyboxTexture().isEmpty()) {
        //     renderSkyboxSphere(dome.getSkyboxTexture(), radius, radius * 0.94f, 32, x, y, z);
        // }

        GL11.glDepthMask(true);
        // Render inner dome (depth write enabled so inner sphere properly occludes)
        renderSphere(dome.getInnerColor(), dome.getInnerAlpha() * healthPercent, radius * innerScale, 8);
        GL11.glPopMatrix();

        restoreRenderState();
    }

    /**
     * Render a sphere made of quad panels.
     * Uses latitude/longitude segments to form a full sphere.
     */
    private void renderSphere(int color, float alpha, float radius, int segments) {
        float[] rgb = extractRGB(color);

        Tessellator tess = Tessellator.instance;
        tess.startDrawingQuads();
        tess.setColorRGBA_F(rgb[0], rgb[1], rgb[2], alpha);

        for (int lat = 0; lat < segments; lat++) {
            float theta1 = (float) (Math.PI * lat / segments - Math.PI * 0.5);
            float theta2 = (float) (Math.PI * (lat + 1) / segments - Math.PI * 0.5);

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

                float nx = (x1 + x2 + x3 + x4) * 0.25f;
                float ny = (y1 + y1 + y2 + y2) * 0.25f;
                float nz = (z1 + z2 + z3 + z4) * 0.25f;
                float nLen = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
                if (nLen > 0) {
                    nx /= nLen;
                    ny /= nLen;
                    nz /= nLen;
                }

                tess.setNormal(nx, ny, nz);
                tess.addVertex(x1, y1, z1);
                tess.addVertex(x2, y1, z2);
                tess.addVertex(x3, y2, z3);
                tess.addVertex(x4, y2, z4);
            }
        }

        tess.draw();
    }

    // TODO: Skybox Feature — renderSkyboxSphere method removed
}
