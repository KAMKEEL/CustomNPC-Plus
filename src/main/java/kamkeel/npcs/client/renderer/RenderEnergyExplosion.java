package kamkeel.npcs.client.renderer;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.entity.EntityEnergyExplosion;
import net.minecraft.entity.Entity;
import org.lwjgl.opengl.GL11;

/**
 * Voxel-style renderer for {@link EntityEnergyExplosion}.
 * Draws expanding cubic shells with a short fade-out.
 */
@SideOnly(Side.CLIENT)
public class RenderEnergyExplosion extends RenderEnergyAbility {

    private static final float MAX_OUTER_ALPHA = 0.85f;
    private static final float MAX_INNER_ALPHA = 1.0f;
    private static final float MAX_SMOKE_CUBE_ALPHA = 0.55f;

    @Override
    public void doRender(Entity entity, double x, double y, double z, float yaw, float partialTicks) {
        if (!(entity instanceof EntityEnergyExplosion)) return;
        EntityEnergyExplosion explosion = (EntityEnergyExplosion) entity;

        setupRenderState();

        GL11.glPushMatrix();
        GL11.glTranslated(x, y, z);

        float radius = explosion.getInterpolatedRadius(partialTicks);
        float life = explosion.getLifeProgress(partialTicks);
        if (radius > 0.01f && life < 1.0f) {
            renderVoxelBurst(explosion, radius, life);
            if (explosion.hasLightningEffect()) {
                renderAttachedLightning(explosion, 0.55f, Math.max(0.7f, radius * 0.45f));
            }
        }

        GL11.glPopMatrix();
        restoreRenderState();
    }

    private void renderVoxelBurst(EntityEnergyExplosion explosion, float radius, float life) {
        int outerColor = explosion.getOuterColor();
        int innerColor = explosion.getInnerColor();
        boolean hasOuter = explosion.isOuterColorEnabled();

        float fade = 1.0f - life;
        float outerAlpha = Math.min(MAX_OUTER_ALPHA, (0.25f + fade * 0.75f) * explosion.getOuterColorAlpha());
        float innerAlpha = Math.min(MAX_INNER_ALPHA, (0.30f + fade * 0.80f) * explosion.getInnerAlpha());

        int steps = Math.max(2, Math.min(6, (int) Math.ceil(radius * 0.65f) + 1));
        float cell = radius / steps;
        float cubeHalf = Math.max(0.04f, cell * 0.22f * (0.90f - life * 0.25f));
        long seed = explosion.getRenderSeed();

        GL11.glDepthMask(false);
        renderSmokeVoxelShell(radius, life, seed);

        for (int ix = -steps; ix <= steps; ix++) {
            for (int iy = -steps; iy <= steps; iy++) {
                for (int iz = -steps; iz <= steps; iz++) {
                    int edge = Math.max(Math.abs(ix), Math.max(Math.abs(iy), Math.abs(iz)));
                    if (edge != steps) continue;
                    if (!shouldRenderVoxel(ix, iy, iz, steps, seed, life)) continue;

                    float px = ix * cell + jitter(ix, iy, iz, seed, 1) * cell * 0.18f;
                    float py = iy * cell + jitter(ix, iy, iz, seed, 2) * cell * 0.18f;
                    float pz = iz * cell + jitter(ix, iy, iz, seed, 3) * cell * 0.18f;

                    GL11.glPushMatrix();
                    GL11.glTranslatef(px, py, pz);

                    if (hasOuter) {
                        GL11.glPushMatrix();
                        float outerScale = 1.0f + explosion.getOuterColorWidth() * 0.35f;
                        GL11.glScalef(outerScale, outerScale, outerScale);
                        renderCube(outerColor, outerAlpha * fade, cubeHalf);
                        GL11.glPopMatrix();
                    }

                    renderCube(innerColor, innerAlpha * fade, cubeHalf * 0.9f);
                    GL11.glPopMatrix();
                }
            }
        }
        renderCoreFlash(innerColor, innerAlpha, radius, cubeHalf, life);
        GL11.glDepthMask(true);
    }

    /**
     * Adds large translucent white/gray cubes to fake a smoky volumetric expansion.
     */
    private void renderSmokeVoxelShell(float radius, float life, long seed) {
        int smokeSteps = Math.max(2, Math.min(5, (int) Math.ceil(radius * 0.5f) + 1));
        float smokeCell = radius / smokeSteps * 1.2f;
        float smokeJitter = smokeCell * 0.28f;
        float smokeCubeHalf = Math.max(0.07f, smokeCell * 0.24f * (0.95f - life * 0.25f));
        float smokeAlpha = Math.min(MAX_SMOKE_CUBE_ALPHA, (0.15f + (1.0f - life) * 0.55f));

        for (int ix = -smokeSteps; ix <= smokeSteps; ix++) {
            for (int iy = -smokeSteps; iy <= smokeSteps; iy++) {
                for (int iz = -smokeSteps; iz <= smokeSteps; iz++) {
                    int edge = Math.max(Math.abs(ix), Math.max(Math.abs(iy), Math.abs(iz)));
                    if (edge < smokeSteps - 1) continue;

                    int h = hash(ix * 5, iy * 7, iz * 11, seed ^ 0x4f1bbcdcL);
                    if ((h & 3) != 0) continue;
                    if (life > 0.72f && ((h >>> 5) & 7) > 2) continue;

                    float px = ix * smokeCell + jitter(ix, iy, iz, seed ^ 0x2299aa11L, 1) * smokeJitter;
                    float py = iy * smokeCell + jitter(ix, iy, iz, seed ^ 0x2299aa11L, 2) * smokeJitter;
                    float pz = iz * smokeCell + jitter(ix, iy, iz, seed ^ 0x2299aa11L, 3) * smokeJitter;

                    int smokeColor = ((h >>> 9) & 1) == 0 ? 0xFFFFFF : 0xDCDCDC;
                    float alpha = smokeAlpha * (0.78f + (((h >>> 10) & 15) / 15.0f) * 0.22f);

                    GL11.glPushMatrix();
                    GL11.glTranslatef(px, py, pz);
                    renderCube(smokeColor, alpha, smokeCubeHalf);
                    GL11.glPopMatrix();
                }
            }
        }
    }

    /**
     * White-hot core flash pass for the initial frames.
     */
    private void renderCoreFlash(int innerColor, float innerAlpha, float radius, float cubeHalf, float life) {
        float flash = saturate((0.60f - life) / 0.60f);
        if (flash <= 0.0f) return;

        float coreHalf = Math.max(cubeHalf, radius * 0.07f) * (1.0f + flash * 0.55f);
        float whiteAlpha = Math.min(1.0f, 0.25f + flash * 0.95f);
        float innerFlashAlpha = Math.min(1.0f, innerAlpha * (0.45f + flash * 0.95f));

        GL11.glPushMatrix();
        renderCube(0xFFFFFF, whiteAlpha, coreHalf * 1.45f);
        GL11.glScalef(0.82f, 0.82f, 0.82f);
        renderCube(innerColor, innerFlashAlpha, coreHalf);
        GL11.glPopMatrix();
    }

    private float saturate(float value) {
        if (value < 0.0f) return 0.0f;
        if (value > 1.0f) return 1.0f;
        return value;
    }

    private boolean shouldRenderVoxel(int ix, int iy, int iz, int steps, long seed, float life) {
        int h = hash(ix, iy, iz, seed);

        int keepMask = steps >= 5 ? 3 : 1;
        if ((h & keepMask) != 0) return false;

        if (life > 0.55f) {
            int decay = (h >>> 3) & 7;
            if (decay > 2) return false;
        }

        return true;
    }

    private float jitter(int ix, int iy, int iz, long seed, int channel) {
        int h = hash(ix + channel * 17, iy - channel * 31, iz + channel * 47, seed);
        return (((h >>> 8) & 1023) / 1023.0f) - 0.5f;
    }

    private int hash(int x, int y, int z, long seed) {
        long h = seed;
        h ^= (long) x * 73428767L;
        h ^= (long) y * 912931L;
        h ^= (long) z * 19349663L;
        h ^= (h >>> 33);
        h *= 0xff51afd7ed558ccdL;
        h ^= (h >>> 33);
        return (int) h;
    }
}
