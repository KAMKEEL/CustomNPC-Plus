package kamkeel.npcs.client.renderer;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.entity.EntityAbilityPillar;
import kamkeel.npcs.entity.EntityAbilityPillar.PillarOrigin;
import kamkeel.npcs.entity.EntityAbilityPillar.PillarShape;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/**
 * Renders EntityAbilityPillar as a vertical energy column.
 *
 * Shape modes:
 * - CIRCLE: cylindrical pillar with triangulated segments.
 * - SQUARE: rectangular prism pillar.
 *
 * Both modes render three color layers (outer glow, mid, inner core).
 *
 * Origin modes:
 * - FROM_GROUND: posY = ground base. Pillar grows upward (baseY = posY, tipY = posY + height).
 * - FROM_ABOVE:  posY = top anchor (ceiling). Pillar grows downward
 *                (baseY = posY, tipY = posY - height).
 *
 * During charging, a flat disc grows at ground level (FROM_GROUND) or ceiling level (FROM_ABOVE).
 */
@SideOnly(Side.CLIENT)
public class RenderEnergyPillar extends RenderEnergy {

    private static final int CIRCLE_SEGMENTS = 24;
    private static final float INNER_SCALE = 0.6f;

    @Override
    public void doRender(Entity entity, double x, double y, double z, float yaw, float partialTicks) {
        EntityAbilityPillar pillar = (EntityAbilityPillar) entity;

        if (shouldSkipInitialActiveRender(pillar)) return;

        setupRenderState();

        float proximityAlpha = getProximityAlphaFactor(pillar, x, y, z);

        if (pillar.isCharging()) {
            renderCharging(pillar, x, y, z, partialTicks, proximityAlpha);
            restoreRenderState();
            return;
        }

        float radius = pillar.getInterpolatedPillarRadius(partialTicks);
        float height = pillar.getInterpolatedPillarHeight(partialTicks);

        if (radius <= 0.01f || height <= 0.01f) {
            restoreRenderState();
            return;
        }

        // FROM_GROUND: base at y, tip grows up.
        // FROM_ABOVE:  posY is the top anchor. Base = y, tip = y - height (grows down).
        float baseY, tipY;
        if (pillar.getPillarOrigin() == PillarOrigin.FROM_ABOVE) {
            baseY = (float) y;
            tipY = (float) y - height;
        } else {
            baseY = (float) y;
            tipY = (float) y + height;
        }

        float rotationAngle = pillar.getRotationSpeed() != 0f
            ? (pillar.ticksExisted + partialTicks) * pillar.getRotationSpeed() : 0f;

        float innerRadius = radius * INNER_SCALE;
        int innerColor = pillar.getInnerColor();
        int outerColor = pillar.getOuterColor();
        float innerAlpha = pillar.getInnerAlpha() * proximityAlpha;

        if (pillar.isOuterColorEnabled()) {
            float outerRadius = innerRadius + pillar.getOuterColorWidth() * radius;
            float outerAlpha = pillar.getOuterColorAlpha() * proximityAlpha;
            float midRadius = innerRadius + (outerRadius - innerRadius) * 0.5f;

            GL11.glDepthMask(false);
            renderPillarShape(pillar.getPillarShape(), x, baseY, tipY, z, outerRadius, outerColor, outerAlpha * 0.4f, rotationAngle);
            renderPillarShape(pillar.getPillarShape(), x, baseY, tipY, z, midRadius, outerColor, outerAlpha * 0.7f, rotationAngle);
            GL11.glDepthMask(true);
        }

        renderPillarShape(pillar.getPillarShape(), x, baseY, tipY, z, innerRadius, innerColor, innerAlpha, rotationAngle);

        if (pillar.hasLightningEffect()) {
            float midWorldY = (baseY + tipY) * 0.5f;
            GL11.glPushMatrix();
            GL11.glTranslated(x, midWorldY, z);
            renderAttachedLightning(pillar, INNER_SCALE, radius);
            GL11.glPopMatrix();
        }

        restoreRenderState();
    }

    // ==================== CHARGING ====================

    private void renderCharging(EntityAbilityPillar pillar, double x, double y, double z,
                                float partialTicks, float proximityAlpha) {
        float radius = pillar.getTargetRadius() * 2;
        float chargeProgress = pillar.getInterpolatedChargeProgress(partialTicks);
        float renderRadius = radius * chargeProgress;

        if (renderRadius <= 0.01f) return;

        float pulseTime = pillar.ticksExisted + partialTicks;
        renderRadius *= (1.0f + (float) Math.sin(pulseTime * 0.2f) * 0.08f);

        // Flat disc at the anchor point
        float baseY = (float) y;
        float tipY = baseY + 0.05f;

        float rotationAngle = pillar.getRotationSpeed() != 0f
            ? (pillar.ticksExisted + partialTicks) * pillar.getRotationSpeed() : 0f;

        float innerRadius = renderRadius * INNER_SCALE;
        int innerColor = pillar.getInnerColor();
        int outerColor = pillar.getOuterColor();
        float innerAlpha = pillar.getInnerAlpha() * proximityAlpha;

        if (pillar.hasLightningEffect()) {
            GL11.glPushMatrix();
            GL11.glTranslated(x, y + 0.025f, z);
            renderAttachedLightning(pillar, INNER_SCALE, renderRadius);
            GL11.glPopMatrix();
        }

        if (pillar.isOuterColorEnabled()) {
            float outerRadius = innerRadius + pillar.getOuterColorWidth() * renderRadius;
            float outerAlpha = pillar.getOuterColorAlpha() * proximityAlpha;
            float midRadius = innerRadius + (outerRadius - innerRadius) * 0.5f;

            GL11.glDepthMask(false);
            renderPillarShape(pillar.getPillarShape(), x, baseY, tipY, z, outerRadius, outerColor, outerAlpha * 0.4f, rotationAngle);
            renderPillarShape(pillar.getPillarShape(), x, baseY, tipY, z, midRadius, outerColor, outerAlpha * 0.7f, rotationAngle);
            GL11.glDepthMask(true);
        }

        renderPillarShape(pillar.getPillarShape(), x, baseY, tipY, z, innerRadius, innerColor, innerAlpha, rotationAngle);
    }

    // ==================== SHAPE DISPATCH ====================

    private void renderPillarShape(PillarShape shape, double x, float baseY, float tipY,
                                   double z, float radius, int color, float alpha, float rotationAngle) {
        if (shape == PillarShape.SQUARE) {
            renderSquarePillar(x, baseY, tipY, z, radius, color, alpha, rotationAngle);
        } else {
            renderCylinderPillar(x, baseY, tipY, z, radius, color, alpha, rotationAngle);
        }
    }

    // ==================== CYLINDER ====================

    private void renderCylinderPillar(double cx, float baseY, float tipY, double cz,
                                      float radius, int color, float alpha, float rotationAngle) {
        float[] rgb = extractRGB(color);
        float r = rgb[0], g = rgb[1], b = rgb[2];

        float[] cosTable = new float[CIRCLE_SEGMENTS + 1];
        float[] sinTable = new float[CIRCLE_SEGMENTS + 1];
        double rotRad = Math.toRadians(rotationAngle);
        for (int i = 0; i <= CIRCLE_SEGMENTS; i++) {
            double angle = 2.0 * Math.PI * i / CIRCLE_SEGMENTS + rotRad;
            cosTable[i] = (float) Math.cos(angle);
            sinTable[i] = (float) Math.sin(angle);
        }

        Tessellator tess = Tessellator.instance;

        // Lateral faces
        tess.startDrawingQuads();
        tess.setColorRGBA_F(r, g, b, alpha);
        for (int i = 0; i < CIRCLE_SEGMENTS; i++) {
            float x1 = (float) cx + cosTable[i] * radius;
            float z1 = (float) cz + sinTable[i] * radius;
            float x2 = (float) cx + cosTable[i + 1] * radius;
            float z2 = (float) cz + sinTable[i + 1] * radius;
            float nx = (cosTable[i] + cosTable[i + 1]) * 0.5f;
            float nz = (sinTable[i] + sinTable[i + 1]) * 0.5f;
            tess.setNormal(nx, 0, nz);
            tess.addVertex(x1, baseY, z1);
            tess.addVertex(x2, baseY, z2);
            tess.addVertex(x2, tipY, z2);
            tess.addVertex(x1, tipY, z1);
        }
        tess.draw();

        float capMinY = Math.min(baseY, tipY);
        float capMaxY = Math.max(baseY, tipY);

        // Bottom cap
        tess.startDrawingQuads();
        tess.setColorRGBA_F(r, g, b, alpha);
        tess.setNormal(0, -1, 0);
        for (int i = 0; i < CIRCLE_SEGMENTS; i++) {
            float x1 = (float) cx + cosTable[i] * radius;
            float z1 = (float) cz + sinTable[i] * radius;
            float x2 = (float) cx + cosTable[i + 1] * radius;
            float z2 = (float) cz + sinTable[i + 1] * radius;
            tess.addVertex((float) cx, capMinY, (float) cz);
            tess.addVertex(x2, capMinY, z2);
            tess.addVertex(x1, capMinY, z1);
            tess.addVertex((float) cx, capMinY, (float) cz);
        }
        tess.draw();

        // Top cap
        tess.startDrawingQuads();
        tess.setColorRGBA_F(r, g, b, alpha);
        tess.setNormal(0, 1, 0);
        for (int i = 0; i < CIRCLE_SEGMENTS; i++) {
            float x1 = (float) cx + cosTable[i] * radius;
            float z1 = (float) cz + sinTable[i] * radius;
            float x2 = (float) cx + cosTable[i + 1] * radius;
            float z2 = (float) cz + sinTable[i + 1] * radius;
            tess.addVertex((float) cx, capMaxY, (float) cz);
            tess.addVertex(x1, capMaxY, z1);
            tess.addVertex(x2, capMaxY, z2);
            tess.addVertex((float) cx, capMaxY, (float) cz);
        }
        tess.draw();
    }

    // ==================== SQUARE ====================

    private void renderSquarePillar(double cx, float baseY, float tipY, double cz,
                                    float radius, int color, float alpha, float rotationAngle) {
        float[] rgb = extractRGB(color);
        float r = rgb[0], g = rgb[1], b = rgb[2];

        double rotRad = Math.toRadians(rotationAngle);
        float cosA = (float) Math.cos(rotRad);
        float sinA = (float) Math.sin(rotRad);

        float[] lx = {-radius, radius, radius, -radius};
        float[] lz = {-radius, -radius, radius, radius};
        float[][] corners = new float[4][2];
        for (int i = 0; i < 4; i++) {
            corners[i][0] = (float) cx + lx[i] * cosA - lz[i] * sinA;
            corners[i][1] = (float) cz + lx[i] * sinA + lz[i] * cosA;
        }

        float capMinY = Math.min(baseY, tipY);
        float capMaxY = Math.max(baseY, tipY);

        Tessellator tess = Tessellator.instance;
        tess.startDrawingQuads();
        tess.setColorRGBA_F(r, g, b, alpha);

        for (int i = 0; i < 4; i++) {
            int next = (i + 1) % 4;
            float x1 = corners[i][0], z1 = corners[i][1];
            float x2 = corners[next][0], z2 = corners[next][1];
            float nx = (x1 + x2) * 0.5f - (float) cx;
            float nz = (z1 + z2) * 0.5f - (float) cz;
            float nlen = (float) Math.sqrt(nx * nx + nz * nz);
            if (nlen > 0) { nx /= nlen; nz /= nlen; }
            tess.setNormal(nx, 0, nz);
            tess.addVertex(x1, baseY, z1);
            tess.addVertex(x2, baseY, z2);
            tess.addVertex(x2, tipY, z2);
            tess.addVertex(x1, tipY, z1);
        }

        tess.setNormal(0, -1, 0);
        tess.addVertex(corners[0][0], capMinY, corners[0][1]);
        tess.addVertex(corners[3][0], capMinY, corners[3][1]);
        tess.addVertex(corners[2][0], capMinY, corners[2][1]);
        tess.addVertex(corners[1][0], capMinY, corners[1][1]);

        tess.setNormal(0, 1, 0);
        tess.addVertex(corners[0][0], capMaxY, corners[0][1]);
        tess.addVertex(corners[1][0], capMaxY, corners[1][1]);
        tess.addVertex(corners[2][0], capMaxY, corners[2][1]);
        tess.addVertex(corners[3][0], capMaxY, corners[3][1]);

        tess.draw();
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity entity) {
        return WHITE_TEXTURE;
    }
}
