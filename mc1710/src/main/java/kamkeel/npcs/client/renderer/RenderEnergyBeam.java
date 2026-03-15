package kamkeel.npcs.client.renderer;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.entity.EntityAbilityBeam;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

/**
 * Renders the AbilityBeam entity with a head, tail orb, and curving trail.
 * The trail shows the path the head has traveled using laser-style rectangular segments.
 * <p>
 * IMPORTANT: Trail points are stored RELATIVE to the origin position.
 * We render by translating to the origin first, then drawing all points relative to that.
 * <p>
 * Trail segments now share vertices at connection points to prevent gaps during curves.
 * <p>
 * Design inspired by LouisXIV's energy rendering system.
 */
@SideOnly(Side.CLIENT)
public class RenderEnergyBeam extends RenderEnergy {

    @Override
    public void doRender(Entity entity, double x, double y, double z, float yaw, float partialTicks) {
        if (shouldSkipInitialActiveRender(entity)) {
            return;
        }
        EntityAbilityBeam beam = (EntityAbilityBeam) entity;

        setupRenderState();

        // Proximity alpha fade for owner (attached beams always use proximity, never age out)
        float proximityAlpha = getProximityAlphaFactor(beam, x, y, z, beam.isAttachedToOwner());

        // Handle charging state (windup phase) - render only growing orb
        if (beam.isCharging()) {
            renderChargingOrb(beam, x, y, z, partialTicks, proximityAlpha);
            restoreRenderState();
            return;
        }

        // Get trail points (these are RELATIVE to origin)
        List<Vec3> trail = beam.getTrailPoints();
        if (trail.isEmpty()) {
            restoreRenderState();
            return;
        }

        // Get interpolated head offset (for head/trail rendering relative to origin)
        double headOffsetX = beam.getInterpolatedHeadOffsetX(partialTicks);
        double headOffsetY = beam.getInterpolatedHeadOffsetY(partialTicks);
        double headOffsetZ = beam.getInterpolatedHeadOffsetZ(partialTicks);

        // Compute render origin directly from the world origin and camera position.
        // We can't derive it from MC's x,y,z because syncPositionStateToCurrent() snaps
        // lastTickPos = posX on every sync packet, killing MC's position interpolation.
        // The headOffset interpolation then mismatches, causing the origin to wobble.
        double renderOriginX = beam.getOriginX() - RenderManager.renderPosX;
        double renderOriginY = beam.getOriginY() - RenderManager.renderPosY;
        double renderOriginZ = beam.getOriginZ() - RenderManager.renderPosZ;

        // Get beam properties (lerp-smoothed for smooth size transitions)
        float beamWidth = beam.getInterpolatedBeamWidth(partialTicks);
        float headSize = beam.getInterpolatedHeadSize(partialTicks);
        int innerColor = beam.getInnerColor();
        int outerColor = beam.getOuterColor();
        float outerAlpha = beam.getOuterColorAlpha() * proximityAlpha;

        // Render everything relative to origin position
        GL11.glPushMatrix();
        GL11.glTranslated(renderOriginX, renderOriginY, renderOriginZ);

        // Calculate head distance from origin (for overlap check)
        double headDistFromOrigin = Math.sqrt(headOffsetX * headOffsetX + headOffsetY * headOffsetY + headOffsetZ * headOffsetZ);

        float innerAlpha = beam.getInnerAlpha() * proximityAlpha;

        // Render trail segments first (with fading for non-anchored)
        if (beam.hasFadingTrail()) {
            renderFadingTrail(beam, trail, beamWidth, innerColor, outerColor, beam.isOuterColorEnabled(), beam.getOuterColorWidth(), innerAlpha);
        } else {
            renderSmoothTrail(trail, beamWidth, innerColor, outerColor, beam.isOuterColorEnabled(), beam.getOuterColorWidth(), innerAlpha);
        }

        // Render tail orb at origin (0,0,0) AFTER trail so it draws on top
        // Don't render if head is too close to origin (would cause overlap/extra orb appearance)
        // Use target headSize (not lerped) so the threshold isn't inflated by lerp lag
        if (beam.shouldRenderTailOrb() && headDistFromOrigin > beam.getHeadSize() * 0.5) {
            renderTailOrb(beam.getHeadSize() * 0.8f, innerColor, outerColor, beam.isOuterColorEnabled(), beam.getOuterColorWidth(), outerAlpha, innerAlpha);
        }

        // Render head at interpolated offset position
        renderHead(beam, headOffsetX, headOffsetY, headOffsetZ, headSize, innerColor, outerColor, outerAlpha, innerAlpha, partialTicks);

        GL11.glPopMatrix();

        restoreRenderState();
    }

    /**
     * Render the charging orb during windup phase.
     * Grows from 0 to headSize based on charge progress.
     */
    private void renderChargingOrb(EntityAbilityBeam beam, double x, double y, double z, float partialTicks, float proximityAlpha) {
        float headSize = beam.getInterpolatedHeadSize(partialTicks);
        float chargeProgress = beam.getInterpolatedChargeProgress(partialTicks);
        float size = headSize * chargeProgress;

        if (size <= 0.01f) return;

        int innerColor = beam.getInnerColor();
        int outerColor = beam.getOuterColor();

        GL11.glPushMatrix();
        GL11.glTranslated(x, y, z);

        // Pulsing effect
        float pulseTime = beam.ticksExisted + partialTicks;
        float scaleModifier = (float) Math.sin(pulseTime * 0.2f) * 0.08f;
        float scale = size * (1.0f + scaleModifier);

        // Render lightning during charging if enabled
        if (beam.hasLightningEffect()) {
            renderAttachedLightning(beam, 0.6f * 0.5f, scale);
        }

        GL11.glPushMatrix();
        GL11.glScalef(scale, scale, scale);

        // Inner scale defines the core size
        float innerScale = 0.6f;

        // Render outer glow only if enabled
        if (beam.isOuterColorEnabled()) {
            float outerScale = innerScale + beam.getOuterColorWidth();
            float outerAlpha = beam.getOuterColorAlpha() * proximityAlpha;
            GL11.glDepthMask(false);
            GL11.glPushMatrix();
            GL11.glScalef(outerScale, outerScale, outerScale);
            renderCube(outerColor, outerAlpha, 0.5f);
            GL11.glPopMatrix();
            GL11.glDepthMask(true);
        }

        // Render inner core
        GL11.glScalef(innerScale, innerScale, innerScale);
        renderCube(innerColor, beam.getInnerAlpha() * proximityAlpha, 0.5f);

        GL11.glPopMatrix();
        GL11.glPopMatrix();
    }

    /**
     * Render the tail orb at the origin (where the beam emanates from).
     * This orb does not rotate - it's a stable anchor point.
     */
    private void renderTailOrb(float size, int innerColor, int outerColor, boolean outerColorEnabled, float outerColorWidth, float outerAlpha, float innerAlpha) {
        GL11.glPushMatrix();
        // Already at origin (0,0,0)

        // Inner scale defines the core size
        float innerScale = 0.6f;

        // Render outer glow only if enabled
        // outerColorWidth is an additive offset from inner size
        if (outerColorEnabled) {
            float outerScale = innerScale + outerColorWidth;
            GL11.glDepthMask(false);
            GL11.glPushMatrix();
            GL11.glScalef(size * outerScale, size * outerScale, size * outerScale);
            renderCube(outerColor, outerAlpha, 0.5f);
            GL11.glPopMatrix();
            GL11.glDepthMask(true);
        }

        // Render inner core (solid)
        GL11.glPushMatrix();
        GL11.glScalef(size * innerScale, size * innerScale, size * innerScale);
        renderCube(innerColor, innerAlpha, 0.5f);
        GL11.glPopMatrix();

        GL11.glPopMatrix();
    }

    /**
     * Render the beam trail with smooth connections between segments.
     * Uses averaged perpendicular vectors at connection points to prevent gaps.
     */
    private void renderSmoothTrail(List<Vec3> trail, float width, int innerColor, int outerColor,
                                   boolean outerColorEnabled, float outerColorWidth, float innerAlpha) {
        if (trail.size() < 2) return;

        int trailSize = trail.size();

        // Pre-compute perpendicular frames at each point
        // At connection points, average the perpendiculars for smooth transitions
        List<double[]> perpFrames = computePerpendiculars(trail);

        // Inner scale defines the core width
        float innerScale = 0.6f;
        float innerWidth = width * innerScale;

        // Render outer glow trail (wider, translucent) - only if enabled
        // outerColorWidth is an additive offset from inner width
        if (outerColorEnabled) {
            float outerWidth = innerWidth + outerColorWidth * width;
            GL11.glDepthMask(false);
            renderTrailTube(trail, perpFrames, outerWidth, outerColor, 0.3f, true);
            GL11.glDepthMask(true);
        }

        // Render inner core trail
        renderTrailTube(trail, perpFrames, innerWidth, innerColor, innerAlpha, false);
    }

    /**
     * Render trail with fading effect (comet style) for non-anchored beams.
     * Older trail points fade out based on their age.
     */
    private void renderFadingTrail(EntityAbilityBeam beam, List<Vec3> trail, float width, int innerColor, int outerColor,
                                   boolean outerColorEnabled, float outerColorWidth, float innerAlpha) {
        if (trail.size() < 2) return;

        List<Integer> ages = beam.getTrailPointAges();
        int fadeTime = beam.getTrailFadeTime();

        // Pre-compute perpendicular frames
        List<double[]> perpFrames = computePerpendiculars(trail);

        // Inner scale defines the core width
        float innerScale = 0.6f;
        float innerWidth = width * innerScale;

        // Render outer glow trail with fading
        if (outerColorEnabled) {
            float outerWidth = innerWidth + outerColorWidth * width;
            GL11.glDepthMask(false);
            renderFadingTrailTube(trail, perpFrames, ages, fadeTime, outerWidth, outerColor, 0.3f);
            GL11.glDepthMask(true);
        }

        // Render inner core trail with fading
        renderFadingTrailTube(trail, perpFrames, ages, fadeTime, innerWidth, innerColor, innerAlpha);
    }

    /**
     * Render trail tube with age-based fading.
     */
    private void renderFadingTrailTube(List<Vec3> trail, List<double[]> perpFrames, List<Integer> ages,
                                       int fadeTime, float width, int color, float baseAlpha) {
        float[] rgb = extractRGB(color);
        float r = rgb[0], g = rgb[1], b = rgb[2];
        float halfWidth = width * 0.5f;
        int size = trail.size();

        Tessellator tess = Tessellator.instance;
        tess.startDrawingQuads();

        for (int i = 0; i < size - 1; i++) {
            Vec3 p1 = trail.get(i);
            Vec3 p2 = trail.get(i + 1);
            double[] frame1 = perpFrames.get(i);
            double[] frame2 = perpFrames.get(i + 1);

            // Calculate alpha based on age (older = more faded)
            float age1 = i < ages.size() ? ages.get(i) : 0;
            float age2 = (i + 1) < ages.size() ? ages.get(i + 1) : 0;
            float fade1 = fadeTime > 0 ? (1.0f - Math.min(1.0f, age1 / fadeTime)) : 0.0f;
            float fade2 = fadeTime > 0 ? (1.0f - Math.min(1.0f, age2 / fadeTime)) : 0.0f;
            float alpha1 = baseAlpha * fade1;
            float alpha2 = baseAlpha * fade2;

            // Skip fully faded segments
            if (alpha1 <= 0.01f && alpha2 <= 0.01f) continue;

            // Calculate 4 corners at start
            double s1x = p1.xCoord - frame1[0] * halfWidth - frame1[3] * halfWidth;
            double s1y = p1.yCoord - frame1[1] * halfWidth - frame1[4] * halfWidth;
            double s1z = p1.zCoord - frame1[2] * halfWidth - frame1[5] * halfWidth;

            double s2x = p1.xCoord + frame1[0] * halfWidth - frame1[3] * halfWidth;
            double s2y = p1.yCoord + frame1[1] * halfWidth - frame1[4] * halfWidth;
            double s2z = p1.zCoord + frame1[2] * halfWidth - frame1[5] * halfWidth;

            double s3x = p1.xCoord + frame1[0] * halfWidth + frame1[3] * halfWidth;
            double s3y = p1.yCoord + frame1[1] * halfWidth + frame1[4] * halfWidth;
            double s3z = p1.zCoord + frame1[2] * halfWidth + frame1[5] * halfWidth;

            double s4x = p1.xCoord - frame1[0] * halfWidth + frame1[3] * halfWidth;
            double s4y = p1.yCoord - frame1[1] * halfWidth + frame1[4] * halfWidth;
            double s4z = p1.zCoord - frame1[2] * halfWidth + frame1[5] * halfWidth;

            // Calculate 4 corners at end
            double e1x = p2.xCoord - frame2[0] * halfWidth - frame2[3] * halfWidth;
            double e1y = p2.yCoord - frame2[1] * halfWidth - frame2[4] * halfWidth;
            double e1z = p2.zCoord - frame2[2] * halfWidth - frame2[5] * halfWidth;

            double e2x = p2.xCoord + frame2[0] * halfWidth - frame2[3] * halfWidth;
            double e2y = p2.yCoord + frame2[1] * halfWidth - frame2[4] * halfWidth;
            double e2z = p2.zCoord + frame2[2] * halfWidth - frame2[5] * halfWidth;

            double e3x = p2.xCoord + frame2[0] * halfWidth + frame2[3] * halfWidth;
            double e3y = p2.yCoord + frame2[1] * halfWidth + frame2[4] * halfWidth;
            double e3z = p2.zCoord + frame2[2] * halfWidth + frame2[5] * halfWidth;

            double e4x = p2.xCoord - frame2[0] * halfWidth + frame2[3] * halfWidth;
            double e4y = p2.yCoord - frame2[1] * halfWidth + frame2[4] * halfWidth;
            double e4z = p2.zCoord - frame2[2] * halfWidth + frame2[5] * halfWidth;

            // Bottom face
            tess.setColorRGBA_F(r, g, b, alpha1);
            tess.addVertex(s1x, s1y, s1z);
            tess.addVertex(s2x, s2y, s2z);
            tess.setColorRGBA_F(r, g, b, alpha2);
            tess.addVertex(e2x, e2y, e2z);
            tess.addVertex(e1x, e1y, e1z);

            // Top face
            tess.setColorRGBA_F(r, g, b, alpha1);
            tess.addVertex(s4x, s4y, s4z);
            tess.setColorRGBA_F(r, g, b, alpha2);
            tess.addVertex(e4x, e4y, e4z);
            tess.addVertex(e3x, e3y, e3z);
            tess.setColorRGBA_F(r, g, b, alpha1);
            tess.addVertex(s3x, s3y, s3z);

            // Left face
            tess.setColorRGBA_F(r, g, b, alpha1);
            tess.addVertex(s1x, s1y, s1z);
            tess.setColorRGBA_F(r, g, b, alpha2);
            tess.addVertex(e1x, e1y, e1z);
            tess.addVertex(e4x, e4y, e4z);
            tess.setColorRGBA_F(r, g, b, alpha1);
            tess.addVertex(s4x, s4y, s4z);

            // Right face
            tess.setColorRGBA_F(r, g, b, alpha1);
            tess.addVertex(s2x, s2y, s2z);
            tess.addVertex(s3x, s3y, s3z);
            tess.setColorRGBA_F(r, g, b, alpha2);
            tess.addVertex(e3x, e3y, e3z);
            tess.addVertex(e2x, e2y, e2z);
        }

        tess.draw();
    }

    /**
     * Compute perpendicular frames for each point in the trail.
     * At interior points, average the perpendiculars from both directions for smooth curves.
     * Returns list of [horzX, horzY, horzZ, vertX, vertY, vertZ] for each point.
     */
    private List<double[]> computePerpendiculars(List<Vec3> trail) {
        List<double[]> frames = new ArrayList<>();
        int size = trail.size();

        for (int i = 0; i < size; i++) {
            double[] frame = new double[6];

            // Calculate direction at this point
            double dx, dy, dz;
            if (i == 0) {
                // First point - use direction to next
                Vec3 p1 = trail.get(0);
                Vec3 p2 = trail.get(1);
                dx = p2.xCoord - p1.xCoord;
                dy = p2.yCoord - p1.yCoord;
                dz = p2.zCoord - p1.zCoord;
            } else if (i == size - 1) {
                // Last point - use direction from previous
                Vec3 p1 = trail.get(size - 2);
                Vec3 p2 = trail.get(size - 1);
                dx = p2.xCoord - p1.xCoord;
                dy = p2.yCoord - p1.yCoord;
                dz = p2.zCoord - p1.zCoord;
            } else {
                // Interior point - average directions
                Vec3 p0 = trail.get(i - 1);
                Vec3 p1 = trail.get(i);
                Vec3 p2 = trail.get(i + 1);
                double dx1 = p1.xCoord - p0.xCoord;
                double dy1 = p1.yCoord - p0.yCoord;
                double dz1 = p1.zCoord - p0.zCoord;
                double dx2 = p2.xCoord - p1.xCoord;
                double dy2 = p2.yCoord - p1.yCoord;
                double dz2 = p2.zCoord - p1.zCoord;
                // Average the directions
                dx = (dx1 + dx2) * 0.5;
                dy = (dy1 + dy2) * 0.5;
                dz = (dz1 + dz2) * 0.5;
            }

            // Normalize direction
            double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (len > 0.001) {
                dx /= len;
                dy /= len;
                dz /= len;
            } else {
                dx = 0;
                dy = 0;
                dz = 1;
            }

            // Calculate perpendicular vectors using consistent up reference
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

            frame[0] = horzX;
            frame[1] = horzY;
            frame[2] = horzZ;
            frame[3] = vertX;
            frame[4] = vertY;
            frame[5] = vertZ;
            frames.add(frame);
        }

        return frames;
    }

    /**
     * Render the trail as a continuous tube using pre-computed perpendicular frames.
     */
    private void renderTrailTube(List<Vec3> trail, List<double[]> perpFrames,
                                 float width, int color, float baseAlpha, boolean fadeFromStart) {
        float[] rgb = extractRGB(color);
        float r = rgb[0], g = rgb[1], b = rgb[2];
        float halfWidth = width * 0.5f;
        int size = trail.size();

        Tessellator tess = Tessellator.instance;
        tess.startDrawingQuads();

        for (int i = 0; i < size - 1; i++) {
            Vec3 p1 = trail.get(i);
            Vec3 p2 = trail.get(i + 1);
            double[] frame1 = perpFrames.get(i);
            double[] frame2 = perpFrames.get(i + 1);

            // Calculate alpha based on position (fade from start or constant)
            float alpha1, alpha2;
            if (fadeFromStart) {
                float progress1 = (float) i / size;
                float progress2 = (float) (i + 1) / size;
                alpha1 = baseAlpha * progress1;
                alpha2 = baseAlpha * progress2;
            } else {
                alpha1 = baseAlpha;
                alpha2 = baseAlpha;
            }

            // Calculate 4 corners at start
            double s1x = p1.xCoord - frame1[0] * halfWidth - frame1[3] * halfWidth;
            double s1y = p1.yCoord - frame1[1] * halfWidth - frame1[4] * halfWidth;
            double s1z = p1.zCoord - frame1[2] * halfWidth - frame1[5] * halfWidth;

            double s2x = p1.xCoord + frame1[0] * halfWidth - frame1[3] * halfWidth;
            double s2y = p1.yCoord + frame1[1] * halfWidth - frame1[4] * halfWidth;
            double s2z = p1.zCoord + frame1[2] * halfWidth - frame1[5] * halfWidth;

            double s3x = p1.xCoord + frame1[0] * halfWidth + frame1[3] * halfWidth;
            double s3y = p1.yCoord + frame1[1] * halfWidth + frame1[4] * halfWidth;
            double s3z = p1.zCoord + frame1[2] * halfWidth + frame1[5] * halfWidth;

            double s4x = p1.xCoord - frame1[0] * halfWidth + frame1[3] * halfWidth;
            double s4y = p1.yCoord - frame1[1] * halfWidth + frame1[4] * halfWidth;
            double s4z = p1.zCoord - frame1[2] * halfWidth + frame1[5] * halfWidth;

            // Calculate 4 corners at end
            double e1x = p2.xCoord - frame2[0] * halfWidth - frame2[3] * halfWidth;
            double e1y = p2.yCoord - frame2[1] * halfWidth - frame2[4] * halfWidth;
            double e1z = p2.zCoord - frame2[2] * halfWidth - frame2[5] * halfWidth;

            double e2x = p2.xCoord + frame2[0] * halfWidth - frame2[3] * halfWidth;
            double e2y = p2.yCoord + frame2[1] * halfWidth - frame2[4] * halfWidth;
            double e2z = p2.zCoord + frame2[2] * halfWidth - frame2[5] * halfWidth;

            double e3x = p2.xCoord + frame2[0] * halfWidth + frame2[3] * halfWidth;
            double e3y = p2.yCoord + frame2[1] * halfWidth + frame2[4] * halfWidth;
            double e3z = p2.zCoord + frame2[2] * halfWidth + frame2[5] * halfWidth;

            double e4x = p2.xCoord - frame2[0] * halfWidth + frame2[3] * halfWidth;
            double e4y = p2.yCoord - frame2[1] * halfWidth + frame2[4] * halfWidth;
            double e4z = p2.zCoord - frame2[2] * halfWidth + frame2[5] * halfWidth;

            // Bottom face (s1-s2-e2-e1)
            tess.setColorRGBA_F(r, g, b, alpha1);
            tess.addVertex(s1x, s1y, s1z);
            tess.addVertex(s2x, s2y, s2z);
            tess.setColorRGBA_F(r, g, b, alpha2);
            tess.addVertex(e2x, e2y, e2z);
            tess.addVertex(e1x, e1y, e1z);

            // Top face (s4-e4-e3-s3)
            tess.setColorRGBA_F(r, g, b, alpha1);
            tess.addVertex(s4x, s4y, s4z);
            tess.setColorRGBA_F(r, g, b, alpha2);
            tess.addVertex(e4x, e4y, e4z);
            tess.addVertex(e3x, e3y, e3z);
            tess.setColorRGBA_F(r, g, b, alpha1);
            tess.addVertex(s3x, s3y, s3z);

            // Left face (s1-e1-e4-s4)
            tess.setColorRGBA_F(r, g, b, alpha1);
            tess.addVertex(s1x, s1y, s1z);
            tess.setColorRGBA_F(r, g, b, alpha2);
            tess.addVertex(e1x, e1y, e1z);
            tess.addVertex(e4x, e4y, e4z);
            tess.setColorRGBA_F(r, g, b, alpha1);
            tess.addVertex(s4x, s4y, s4z);

            // Right face (s2-s3-e3-e2)
            tess.setColorRGBA_F(r, g, b, alpha1);
            tess.addVertex(s2x, s2y, s2z);
            tess.addVertex(s3x, s3y, s3z);
            tess.setColorRGBA_F(r, g, b, alpha2);
            tess.addVertex(e3x, e3y, e3z);
            tess.addVertex(e2x, e2y, e2z);
        }

        tess.draw();
    }

    /**
     * Render the beam head as a glowing cube.
     * Coordinates are relative to origin.
     */
    private void renderHead(EntityAbilityBeam beam, double x, double y, double z,
                            float size, int innerColor, int outerColor, float outerAlpha, float innerAlpha, float partialTicks) {
        GL11.glPushMatrix();
        GL11.glTranslated(x, y, z);

        // Pulsing effect
        float pulseTime = beam.ticksExisted + partialTicks;
        float scaleModifier = (float) Math.sin(pulseTime * 0.2f) * 0.1f;
        float scale = size * (1.0f + scaleModifier);

        // Render lightning BEFORE rotation so it crackles in all directions (only on head)
        if (beam.hasLightningEffect()) {
            renderAttachedLightning(beam, 0.6f * 0.5f, scale);
        }

        // Rotation
        GL11.glRotatef(beam.getInterpolatedRotationX(partialTicks), 1.0f, 0.0f, 0.0f);
        GL11.glRotatef(beam.getInterpolatedRotationY(partialTicks), 0.0f, 1.0f, 0.0f);
        GL11.glRotatef(beam.getInterpolatedRotationZ(partialTicks), 0.0f, 0.0f, 1.0f);

        GL11.glPushMatrix();
        GL11.glScalef(scale, scale, scale);

        // Inner scale defines the core size
        float innerScale = 0.6f;

        // Render outer glow only if enabled
        // outerColorWidth is an additive offset from inner size
        if (beam.isOuterColorEnabled()) {
            float outerScale = innerScale + beam.getOuterColorWidth();
            GL11.glDepthMask(false);
            GL11.glPushMatrix();
            GL11.glScalef(outerScale, outerScale, outerScale);
            renderCube(outerColor, outerAlpha, 0.5f);
            GL11.glPopMatrix();
            GL11.glDepthMask(true);
        }

        // Render inner core
        GL11.glScalef(innerScale, innerScale, innerScale);
        renderCube(innerColor, innerAlpha, 0.5f);

        GL11.glPopMatrix();
        GL11.glPopMatrix();
    }

}
