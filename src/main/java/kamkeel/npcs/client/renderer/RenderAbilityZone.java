package kamkeel.npcs.client.renderer;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.client.renderer.lightning.AttachedLightningRenderer;
import kamkeel.npcs.entity.EntityAbilityZone;
import kamkeel.npcs.entity.EntityAbilityZone.AccentStyle;
import kamkeel.npcs.entity.EntityAbilityZone.ParticleMotion;
import kamkeel.npcs.entity.EntityAbilityZone.ZoneShape;
import kamkeel.npcs.entity.EntityAbilityZone.ZoneType;
import noppes.npcs.client.fx.CustomFX;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.client.fx.ZoneParticleFX;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Renders zone effects for Trap and Hazard abilities.
 * Supports CIRCLE and SQUARE shapes with composable visual layers.
 */
@SideOnly(Side.CLIENT)
public class RenderAbilityZone extends Render {

    private static final int CIRCLE_SEGMENTS = 32;
    private static final int ACCENT_LINE_COUNT = 12;
    private static final Random RANDOM = new Random();

    // Track last particle spawn tick per entity to avoid duplicate spawns
    private final Map<Integer, Integer> lastParticleTick = new HashMap<>();

    // Persistent lightning state
    private final Map<Integer, AttachedLightningRenderer.LightningState> lightningStates = new HashMap<>();

    // ═══════════════════════════════════════════════════════════════════
    // RENDER CONTEXT
    // ═══════════════════════════════════════════════════════════════════

    private static class Ctx {
        EntityAbilityZone zone;
        ZoneShape shape;
        float radius, height;
        float ir, ig, ib;    // inner color
        float outerR, outerG, outerB;
        boolean outerEnabled;
        float time, pulse, rotation, brightness;
        boolean flash, newTick;
        float particleDensity, particleScale, animSpeed, lightningDensity;
        // Visual layer fields (always populated)
        boolean groundFill, rings, border, accents, lightning, particles;
        float groundAlpha, borderSpeed;
        int ringCount, accentStyle, particleMotion, particleSize;
        boolean particleGlow;
        String particleDir;
    }

    // ═══════════════════════════════════════════════════════════════════
    // MAIN RENDER
    // ═══════════════════════════════════════════════════════════════════

    @Override
    public void doRender(Entity entity, double x, double y, double z, float yaw, float partialTicks) {
        EntityAbilityZone zone = (EntityAbilityZone) entity;

        // Build context
        Ctx c = new Ctx();
        c.zone = zone;
        c.shape = zone.getShape();
        c.radius = zone.getRadius();
        c.height = zone.getZoneHeight();

        int inner = zone.getInnerColor();
        c.ir = ((inner >> 16) & 0xFF) / 255.0f;
        c.ig = ((inner >> 8) & 0xFF) / 255.0f;
        c.ib = (inner & 0xFF) / 255.0f;

        int outer = zone.getOuterColor();
        c.outerR = ((outer >> 16) & 0xFF) / 255.0f;
        c.outerG = ((outer >> 8) & 0xFF) / 255.0f;
        c.outerB = (outer & 0xFF) / 255.0f;
        c.outerEnabled = zone.isOuterColorEnabled();

        c.particleDensity = zone.getParticleDensity();
        c.particleScale = zone.getParticleScale();
        c.animSpeed = zone.getAnimSpeed();
        c.lightningDensity = zone.getLightningDensity();

        // Visual layer fields — always populated
        c.groundFill = zone.isGroundFill();
        c.groundAlpha = zone.getGroundAlpha();
        c.rings = zone.isRings();
        c.ringCount = zone.getRingCount();
        c.border = zone.isBorder();
        c.borderSpeed = zone.getBorderSpeed();
        c.accents = zone.isAccents();
        c.accentStyle = zone.getAccentStyle();
        c.lightning = zone.isLightning();
        c.particles = zone.isParticles();
        c.particleMotion = zone.getParticleMotion();
        c.particleDir = zone.getParticleDir();
        c.particleSize = zone.getParticleSize();
        c.particleGlow = zone.isParticleGlow();

        c.time = zone.ticksExisted + partialTicks;
        c.pulse = c.time * 0.1f * c.animSpeed;
        c.rotation = c.time * 1.5f * c.animSpeed;

        c.brightness = 1.0f;
        if (zone.getZoneType() == ZoneType.TRAP) {
            if (!zone.isArmed()) {
                c.brightness = 0.4f;
            } else {
                c.pulse = c.time * 0.2f;
            }
        }

        c.flash = zone.getTriggerFlashTick() >= 0
            && zone.ticksExisted - zone.getTriggerFlashTick() < 4;

        c.newTick = isNewTick(zone);

        // Render
        setupRenderState();
        GL11.glPushMatrix();
        GL11.glTranslated(x, y, z);

        GL11.glDepthMask(false);

        renderZone(c);

        GL11.glDepthMask(true);
        GL11.glPopMatrix();
        restoreRenderState();
    }

    private boolean isNewTick(EntityAbilityZone zone) {
        int id = zone.getEntityId();
        int tick = zone.ticksExisted;
        Integer last = lastParticleTick.get(id);
        if (last == null || last != tick) {
            lastParticleTick.put(id, tick);
            return true;
        }
        return false;
    }

    private AttachedLightningRenderer.LightningState getLightningState(EntityAbilityZone zone) {
        int id = zone.getEntityId();
        AttachedLightningRenderer.LightningState state = lightningStates.get(id);
        if (state == null) {
            state = new AttachedLightningRenderer.LightningState();
            lightningStates.put(id, state);
        }
        return state;
    }

    // ═══════════════════════════════════════════════════════════════════
    // UNIFIED RENDER — Composable visual layers
    // ═══════════════════════════════════════════════════════════════════

    private void renderZone(Ctx c) {
        float rR = ringR(c), rG = ringG(c), rB = ringB(c);

        // 1. Ground fill
        if (c.groundFill) {
            if (c.flash) {
                renderShapeFill(c.shape, c.radius, 1, 1, 1, 0.5f);
            } else {
                renderShapeFill(c.shape, c.radius,
                    c.ir, c.ig, c.ib, c.groundAlpha * c.brightness);
            }
        }

        // 2. Pulse rings
        if (c.rings) {
            int count = Math.max(1, Math.min(5, c.ringCount));
            for (int i = 0; i < count; i++) {
                float frac = (i + 1.0f) / (count + 1);
                float ra = (0.4f + 0.3f * sin(c.pulse + i * Math.PI / 3)) * c.brightness;
                float yOff = 0.02f + i * 0.01f;
                renderRingLine(c.shape, c.radius * frac,
                    rR, rG, rB, ra, yOff);
            }
            // Always include outer edge ring
            float edgeA = (0.5f + 0.4f * sin(c.pulse + count * Math.PI / 3)) * c.brightness;
            renderRingLine(c.shape, c.radius, rR, rG, rB, edgeA, 0.02f + count * 0.01f);
        }

        // 3. Rotating border
        if (c.border) {
            renderRotatingBorder(c.shape, c.radius, rR, rG, rB,
                0.7f * c.brightness, c.rotation * c.borderSpeed, 0.05f);
        }

        // 4. Vertical accents
        if (c.accents) {
            float aa = 0.5f * c.brightness * (0.3f + 0.2f * sin(c.pulse * 0.7f));
            AccentStyle style = AccentStyle.values()[Math.min(c.accentStyle, AccentStyle.values().length - 1)];
            switch (style) {
                case SWAYING:
                    renderSwayingAccents(c, c.ir, c.ig, c.ib, aa, c.height);
                    break;
                case FLICKERING:
                    renderFlickeringAccents(c, c.ir, c.ig, c.ib, aa, c.height);
                    break;
                default:
                    renderVerticalAccents(c.shape, c.radius, c.ir, c.ig, c.ib, aa, c.height, c.rotation * 0.3f);
                    break;
            }
        }

        // 5. Lightning
        if (c.lightning) {
            int innerColor = c.zone.getInnerColor();
            int outerColor = c.outerEnabled ? c.zone.getOuterColor() : innerColor;
            AttachedLightningRenderer.LightningState state = getLightningState(c.zone);
            if (c.newTick) {
                state.tick();
                float density = 0.4f * c.brightness * c.lightningDensity;
                if (RANDOM.nextFloat() < density) {
                    double[] p1 = randomPosInZone(c);
                    double[] p2 = randomPosInZone(c);
                    double y1 = RANDOM.nextDouble() * c.height * 0.4;
                    double y2 = RANDOM.nextDouble() * c.height * 0.4;
                    float disp = c.radius * 0.15f;
                    state.addArc(AttachedLightningRenderer.createArcBetween(
                        p1[0], y1, p1[2], p2[0], y2, p2[2], disp, outerColor, innerColor, 6));
                }
                if (RANDOM.nextFloat() < density * 0.5f * c.lightningDensity) {
                    double[] edgePos = randomEdgePos(c);
                    double ex = edgePos[0];
                    double ez = edgePos[1];
                    float disp = c.radius * 0.12f;
                    state.addArc(AttachedLightningRenderer.createArcBetween(
                        ex, 0, ez, ex + (RANDOM.nextDouble() - 0.5) * 0.5,
                        c.height * (0.3 + RANDOM.nextDouble() * 0.4),
                        ez + (RANDOM.nextDouble() - 0.5) * 0.5,
                        disp, outerColor, innerColor, 5));
                }
            }
            state.render();
            GL11.glDepthMask(false); // Reset after lightning
        }

        // 6. Particles
        if (c.particles && c.newTick && c.particleDensity > 0) {
            if (c.particleDir != null && !c.particleDir.isEmpty()) {
                if (c.particleDir.startsWith("mc:")) {
                    spawnVanillaParticle(c);
                } else {
                    spawnCustomFXParticle(c);
                }
            } else {
                spawnDefaultParticles(c);
            }
        }
    }

    /** Spawn vanilla Minecraft particles using mc: prefix (e.g. "mc:flame"). */
    private void spawnVanillaParticle(Ctx c) {
        String particleName = c.particleDir.substring(3);
        int count = Math.round(3 * c.particleDensity);
        for (int i = 0; i < count; i++) {
            double[] pos = randomPosInZone(c);
            double[] motion = getMotionForStyle(c.particleMotion);
            c.zone.worldObj.spawnParticle(particleName,
                c.zone.posX + pos[0], c.zone.posY + pos[1], c.zone.posZ + pos[2],
                motion[0], motion[1], motion[2]);
        }
    }

    /** Spawn CustomFX particles using a texture directory. */
    private void spawnCustomFXParticle(Ctx c) {
        int count = Math.round(3 * c.particleDensity);
        for (int i = 0; i < count; i++) {
            double[] pos = randomPosInZone(c);
            double[] motion = getMotionForStyle(c.particleMotion);
            CustomFX fx = new CustomFX(c.zone.worldObj, null, c.particleDir,
                c.zone.posX + pos[0], c.zone.posY + pos[1], c.zone.posZ + pos[2],
                0, 0, 0);
            fx.motionX = motion[0];
            fx.motionY = motion[1];
            fx.motionZ = motion[2];
            fx.setMaxAge(20 + RANDOM.nextInt(20));
            fx.width = -1;
            fx.height = -1;
            fx.scaleX1 = c.particleScale * 10.0f;
            fx.scaleY1 = c.particleScale * 10.0f;
            fx.scaleX2 = fx.scaleX1;
            fx.scaleY2 = fx.scaleY1;
            fx.scaleXRate = 0;
            fx.scaleYRate = 0;
            fx.HEXColor = c.zone.getInnerColor();
            fx.facePlayer = true;
            fx.glows = c.particleGlow;
            fx.noClip = true;
            fx.alpha1 = 0.8f;
            fx.alpha2 = 0.0f;
            fx.alphaRate = -0.04f;
            Minecraft.getMinecraft().effectRenderer.addEffect(fx);
        }
    }

    /** Spawn default ZoneParticleFX with motion style from ParticleMotion enum. */
    private void spawnDefaultParticles(Ctx c) {
        int count = Math.round(3 * c.particleDensity);
        ParticleMotion motion = ParticleMotion.values()[Math.min(c.particleMotion, ParticleMotion.values().length - 1)];
        for (int i = 0; i < count; i++) {
            double[] pos = randomPosInZone(c);
            double mx, my, mz;
            float gravity;
            int age;
            switch (motion) {
                case DRIFTING:
                    mx = (RANDOM.nextDouble() - 0.5) * 0.005;
                    my = 0.005 + RANDOM.nextDouble() * 0.01;
                    mz = (RANDOM.nextDouble() - 0.5) * 0.005;
                    gravity = 0;
                    age = 40 + RANDOM.nextInt(20);
                    break;
                case SPARKS:
                    mx = (RANDOM.nextDouble() - 0.5) * 0.08;
                    my = RANDOM.nextDouble() * 0.06;
                    mz = (RANDOM.nextDouble() - 0.5) * 0.08;
                    gravity = 0;
                    age = 4 + RANDOM.nextInt(6);
                    break;
                default: // RISING
                    mx = (RANDOM.nextDouble() - 0.5) * 0.01;
                    my = 0.01 + RANDOM.nextDouble() * 0.02;
                    mz = (RANDOM.nextDouble() - 0.5) * 0.01;
                    gravity = -0.01f;
                    age = 30 + RANDOM.nextInt(20);
                    break;
            }
            float scale = (1.5f + RANDOM.nextFloat() * 1.0f) * c.particleScale;
            spawnParticle(c.zone, pos[0], pos[1], pos[2], mx, my, mz,
                c.ir, c.ig, c.ib, 0.7f, scale, age, gravity, c.particleGlow);
        }
    }

    /** Get motion vector for a CustomFX particle based on ParticleMotion ordinal. */
    private double[] getMotionForStyle(int motionOrdinal) {
        ParticleMotion motion = ParticleMotion.values()[Math.min(motionOrdinal, ParticleMotion.values().length - 1)];
        switch (motion) {
            case DRIFTING:
                return new double[]{
                    (RANDOM.nextDouble() - 0.5) * 0.005,
                    0.005 + RANDOM.nextDouble() * 0.01,
                    (RANDOM.nextDouble() - 0.5) * 0.005
                };
            case SPARKS:
                return new double[]{
                    (RANDOM.nextDouble() - 0.5) * 0.08,
                    RANDOM.nextDouble() * 0.06,
                    (RANDOM.nextDouble() - 0.5) * 0.08
                };
            default: // RISING
                return new double[]{
                    (RANDOM.nextDouble() - 0.5) * 0.01,
                    0.01 + RANDOM.nextDouble() * 0.02,
                    (RANDOM.nextDouble() - 0.5) * 0.01
                };
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // STYLE HELPERS
    // ═══════════════════════════════════════════════════════════════════

    /** Get the ring/outer color (falls back to inner if outer not enabled). */
    private float ringR(Ctx c) { return c.outerEnabled ? c.outerR : c.ir; }
    private float ringG(Ctx c) { return c.outerEnabled ? c.outerG : c.ig; }
    private float ringB(Ctx c) { return c.outerEnabled ? c.outerB : c.ib; }

    private static float sin(double v) { return (float) Math.sin(v); }

    /** Vertical accents with sinusoidal sway per line. */
    private void renderSwayingAccents(Ctx c, float r, float g, float b, float a, float height) {
        if (height <= 0.1f) return;
        float rotRad = (float) Math.toRadians(c.rotation * 0.2f);
        Tessellator tess = Tessellator.instance;

        if (c.shape == ZoneShape.SQUARE) {
            renderSquareSwayingAccents(c, r, g, b, a, height, rotRad, tess);
        } else {
            for (int i = 0; i < ACCENT_LINE_COUNT; i++) {
                float baseAngle = (float) (Math.PI * 2 * i) / ACCENT_LINE_COUNT + rotRad;
                double lx = Math.cos(baseAngle) * c.radius;
                double lz = Math.sin(baseAngle) * c.radius;

                float swayX = 0.15f * sin(c.time * 0.08f + i * 1.7f);
                float swayZ = 0.15f * sin(c.time * 0.06f + i * 2.3f);

                tess.startDrawing(GL11.GL_LINES);
                tess.setColorRGBA_F(r, g, b, a);
                tess.addVertex(lx, 0.01, lz);
                tess.setColorRGBA_F(r, g, b, 0.0f);
                tess.addVertex(lx + swayX, height, lz + swayZ);
                tess.draw();
            }
        }
    }

    private void renderSquareSwayingAccents(Ctx c, float r, float g, float b, float a, float height, float rotRad, Tessellator tess) {
        int perSide = Math.max(1, ACCENT_LINE_COUNT / 4);
        int idx = 0;
        for (int side = 0; side < 4; side++) {
            for (int j = 0; j < perSide; j++) {
                float frac = (j + 0.5f) / perSide;
                double lx, lz;
                switch (side) {
                    case 0: lx = -c.radius + frac * 2 * c.radius; lz = -c.radius; break;
                    case 1: lx = c.radius; lz = -c.radius + frac * 2 * c.radius; break;
                    case 2: lx = c.radius - frac * 2 * c.radius; lz = c.radius; break;
                    default: lx = -c.radius; lz = c.radius - frac * 2 * c.radius; break;
                }
                float swayX = 0.15f * sin(c.time * 0.08f + idx * 1.7f);
                float swayZ = 0.15f * sin(c.time * 0.06f + idx * 2.3f);

                tess.startDrawing(GL11.GL_LINES);
                tess.setColorRGBA_F(r, g, b, a);
                tess.addVertex(lx, 0.01, lz);
                tess.setColorRGBA_F(r, g, b, 0.0f);
                tess.addVertex(lx + swayX, height, lz + swayZ);
                tess.draw();
                idx++;
            }
        }
    }

    /** Vertical accents with per-line random alpha flicker. */
    private void renderFlickeringAccents(Ctx c, float r, float g, float b, float baseAlpha, float height) {
        if (height <= 0.1f) return;
        float rotRad = (float) Math.toRadians(c.rotation * 0.4f);
        Tessellator tess = Tessellator.instance;

        if (c.shape == ZoneShape.SQUARE) {
            renderSquareFlickeringAccents(c, r, g, b, baseAlpha, height, tess);
        } else {
            for (int i = 0; i < ACCENT_LINE_COUNT; i++) {
                float baseAngle = (float) (Math.PI * 2 * i) / ACCENT_LINE_COUNT + rotRad;
                double lx = Math.cos(baseAngle) * c.radius;
                double lz = Math.sin(baseAngle) * c.radius;

                float flicker = 0.3f + 0.7f * Math.abs(sin(c.time * 0.7f + i * 3.14f));
                float lineAlpha = baseAlpha * flicker;
                float lineHeight = height * (0.6f + 0.4f * flicker);

                tess.startDrawing(GL11.GL_LINES);
                tess.setColorRGBA_F(r, g, b, lineAlpha);
                tess.addVertex(lx, 0.01, lz);
                tess.setColorRGBA_F(r, g, b, 0.0f);
                tess.addVertex(lx, lineHeight, lz);
                tess.draw();
            }
        }
    }

    private void renderSquareFlickeringAccents(Ctx c, float r, float g, float b, float baseAlpha, float height, Tessellator tess) {
        int perSide = Math.max(1, ACCENT_LINE_COUNT / 4);
        int idx = 0;
        for (int side = 0; side < 4; side++) {
            for (int j = 0; j < perSide; j++) {
                float frac = (j + 0.5f) / perSide;
                double lx, lz;
                switch (side) {
                    case 0: lx = -c.radius + frac * 2 * c.radius; lz = -c.radius; break;
                    case 1: lx = c.radius; lz = -c.radius + frac * 2 * c.radius; break;
                    case 2: lx = c.radius - frac * 2 * c.radius; lz = c.radius; break;
                    default: lx = -c.radius; lz = c.radius - frac * 2 * c.radius; break;
                }
                float flicker = 0.3f + 0.7f * Math.abs(sin(c.time * 0.7f + idx * 3.14f));
                float lineAlpha = baseAlpha * flicker;
                float lineHeight = height * (0.6f + 0.4f * flicker);

                tess.startDrawing(GL11.GL_LINES);
                tess.setColorRGBA_F(r, g, b, lineAlpha);
                tess.addVertex(lx, 0.01, lz);
                tess.setColorRGBA_F(r, g, b, 0.0f);
                tess.addVertex(lx, lineHeight, lz);
                tess.draw();
                idx++;
            }
        }
    }

    /** Pick a random position within the zone's area. Returns {x, y, z} offsets from zone center. */
    private double[] randomPosInZone(Ctx c) {
        if (c.shape == ZoneShape.SQUARE) {
            double px = (RANDOM.nextDouble() * 2 - 1) * c.radius;
            double pz = (RANDOM.nextDouble() * 2 - 1) * c.radius;
            return new double[]{px, 0, pz};
        } else {
            double angle = RANDOM.nextDouble() * Math.PI * 2;
            double dist = Math.sqrt(RANDOM.nextDouble()) * c.radius;
            double px = Math.cos(angle) * dist;
            double pz = Math.sin(angle) * dist;
            return new double[]{px, 0, pz};
        }
    }

    /** Pick a random position on the edge of the zone. Returns {x, z}. */
    private double[] randomEdgePos(Ctx c) {
        if (c.shape == ZoneShape.SQUARE) {
            int side = RANDOM.nextInt(4);
            double frac = RANDOM.nextDouble() * 2 - 1;
            switch (side) {
                case 0: return new double[]{frac * c.radius, -c.radius};
                case 1: return new double[]{frac * c.radius, c.radius};
                case 2: return new double[]{-c.radius, frac * c.radius};
                default: return new double[]{c.radius, frac * c.radius};
            }
        } else {
            double angle = RANDOM.nextDouble() * Math.PI * 2;
            double dist = c.radius * (0.5 + RANDOM.nextDouble() * 0.5);
            return new double[]{Math.cos(angle) * dist, Math.sin(angle) * dist};
        }
    }

    /** Spawn a zone particle at world position. */
    private void spawnParticle(EntityAbilityZone zone, double offsetX, double offsetY, double offsetZ,
                                double mx, double my, double mz,
                                float r, float g, float b, float alpha,
                                float scale, int maxAge, float gravity, boolean glow) {
        ZoneParticleFX particle = new ZoneParticleFX(
            zone.worldObj,
            zone.posX + offsetX, zone.posY + offsetY, zone.posZ + offsetZ,
            mx, my, mz, r, g, b, alpha, scale, maxAge, gravity, glow);
        Minecraft.getMinecraft().effectRenderer.addEffect(particle);
    }

    // ═══════════════════════════════════════════════════════════════════
    // GL STATE
    // ═══════════════════════════════════════════════════════════════════

    private void setupRenderState() {
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glShadeModel(GL11.GL_SMOOTH);
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

    // ═══════════════════════════════════════════════════════════════════
    // SHAPE FILL (Ground plane)
    // ═══════════════════════════════════════════════════════════════════

    private void renderShapeFill(ZoneShape shape, float radius, float r, float g, float b, float a) {
        GL11.glEnable(GL11.GL_POLYGON_OFFSET_FILL);
        GL11.glPolygonOffset(-1.0f, -1.0f);
        switch (shape) {
            case CIRCLE:
                renderCircleFill(radius, r, g, b, a);
                break;
            case SQUARE:
                renderSquareFill(radius, r, g, b, a);
                break;
        }
        GL11.glDisable(GL11.GL_POLYGON_OFFSET_FILL);
    }

    private void renderCircleFill(float radius, float r, float g, float b, float a) {
        Tessellator tess = Tessellator.instance;
        tess.startDrawing(GL11.GL_TRIANGLE_FAN);
        tess.setColorRGBA_F(r, g, b, a);
        tess.addVertex(0, 0, 0);
        for (int i = 0; i <= CIRCLE_SEGMENTS; i++) {
            double angle = (Math.PI * 2 * i) / CIRCLE_SEGMENTS;
            tess.addVertex(Math.cos(angle) * radius, 0, Math.sin(angle) * radius);
        }
        tess.draw();
    }

    private void renderSquareFill(float radius, float r, float g, float b, float a) {
        Tessellator tess = Tessellator.instance;
        tess.startDrawing(GL11.GL_QUADS);
        tess.setColorRGBA_F(r, g, b, a);
        tess.addVertex(-radius, 0, -radius);
        tess.addVertex(radius, 0, -radius);
        tess.addVertex(radius, 0, radius);
        tess.addVertex(-radius, 0, radius);
        tess.draw();
    }

    // ═══════════════════════════════════════════════════════════════════
    // RING LINES (Concentric pulse rings)
    // ═══════════════════════════════════════════════════════════════════

    private void renderRingLine(ZoneShape shape, float radius,
                                 float r, float g, float b, float a, float yOffset) {
        switch (shape) {
            case CIRCLE:
                renderCircleRingLine(radius, r, g, b, a, yOffset);
                break;
            case SQUARE:
                renderSquareRingLine(radius, r, g, b, a, yOffset);
                break;
        }
    }

    private void renderCircleRingLine(float radius, float r, float g, float b, float a, float yOffset) {
        Tessellator tess = Tessellator.instance;
        tess.startDrawing(GL11.GL_LINE_LOOP);
        tess.setColorRGBA_F(r, g, b, a);
        for (int i = 0; i < CIRCLE_SEGMENTS; i++) {
            double angle = (Math.PI * 2 * i) / CIRCLE_SEGMENTS;
            tess.addVertex(Math.cos(angle) * radius, yOffset, Math.sin(angle) * radius);
        }
        tess.draw();
    }

    private void renderSquareRingLine(float radius, float r, float g, float b, float a, float yOffset) {
        Tessellator tess = Tessellator.instance;
        tess.startDrawing(GL11.GL_LINE_LOOP);
        tess.setColorRGBA_F(r, g, b, a);
        tess.addVertex(-radius, yOffset, -radius);
        tess.addVertex(radius, yOffset, -radius);
        tess.addVertex(radius, yOffset, radius);
        tess.addVertex(-radius, yOffset, radius);
        tess.draw();
    }

    // ═══════════════════════════════════════════════════════════════════
    // ROTATING BORDER
    // ═══════════════════════════════════════════════════════════════════

    private void renderRotatingBorder(ZoneShape shape, float radius,
                                       float r, float g, float b, float a,
                                       float rotationDeg, float yOffset) {
        int dashCount = 16;

        if (shape == ZoneShape.SQUARE) {
            // Dashes along 4 rectangle edges with offset
            float perimeter = radius * 8; // total perimeter = 4 * (2*radius)
            float dashLen = perimeter / dashCount;
            float offset = (rotationDeg % 360.0f) / 360.0f * perimeter;

            Tessellator tess = Tessellator.instance;
            for (int i = 0; i < dashCount; i += 2) {
                float startDist = (i * dashLen + offset) % perimeter;
                float endDist = startDist + dashLen;

                tess.startDrawing(GL11.GL_LINE_STRIP);
                tess.setColorRGBA_F(r, g, b, a);
                int steps = 4;
                for (int s = 0; s <= steps; s++) {
                    float d = startDist + (endDist - startDist) * s / steps;
                    double[] pos = perimeterPos(d % perimeter, radius);
                    tess.addVertex(pos[0], yOffset, pos[1]);
                }
                tess.draw();
            }
        } else {
            float dashAngle = (float) (Math.PI * 2) / dashCount;
            float rotRad = (float) Math.toRadians(rotationDeg);

            Tessellator tess = Tessellator.instance;
            for (int i = 0; i < dashCount; i += 2) {
                float startAngle = i * dashAngle + rotRad;
                float endAngle = startAngle + dashAngle;

                tess.startDrawing(GL11.GL_LINE_STRIP);
                tess.setColorRGBA_F(r, g, b, a);
                int steps = 4;
                for (int s = 0; s <= steps; s++) {
                    float segAngle = startAngle + (endAngle - startAngle) * s / steps;
                    tess.addVertex(Math.cos(segAngle) * radius, yOffset, Math.sin(segAngle) * radius);
                }
                tess.draw();
            }
        }
    }

    /** Convert a distance along the square perimeter to an x,z position. */
    private double[] perimeterPos(float dist, float radius) {
        float sideLen = radius * 2;
        if (dist < sideLen) {
            // Bottom edge: (-radius, -radius) to (radius, -radius)
            return new double[]{-radius + dist, -radius};
        }
        dist -= sideLen;
        if (dist < sideLen) {
            // Right edge: (radius, -radius) to (radius, radius)
            return new double[]{radius, -radius + dist};
        }
        dist -= sideLen;
        if (dist < sideLen) {
            // Top edge: (radius, radius) to (-radius, radius)
            return new double[]{radius - dist, radius};
        }
        dist -= sideLen;
        // Left edge: (-radius, radius) to (-radius, -radius)
        return new double[]{-radius, radius - dist};
    }

    // ═══════════════════════════════════════════════════════════════════
    // VERTICAL ACCENT LINES
    // ═══════════════════════════════════════════════════════════════════

    private void renderVerticalAccents(ZoneShape shape, float radius,
                                        float r, float g, float b, float a,
                                        float height, float rotationDeg) {
        if (height <= 0.1f) return;

        Tessellator tess = Tessellator.instance;

        if (shape == ZoneShape.SQUARE) {
            // Lines at corners + distributed along edges
            int perSide = Math.max(1, ACCENT_LINE_COUNT / 4);
            for (int side = 0; side < 4; side++) {
                for (int j = 0; j < perSide; j++) {
                    float frac = (j + 0.5f) / perSide;
                    double lx, lz;
                    switch (side) {
                        case 0: lx = -radius + frac * 2 * radius; lz = -radius; break;
                        case 1: lx = radius; lz = -radius + frac * 2 * radius; break;
                        case 2: lx = radius - frac * 2 * radius; lz = radius; break;
                        default: lx = -radius; lz = radius - frac * 2 * radius; break;
                    }
                    tess.startDrawing(GL11.GL_LINES);
                    tess.setColorRGBA_F(r, g, b, a);
                    tess.addVertex(lx, 0.01, lz);
                    tess.setColorRGBA_F(r, g, b, 0.0f);
                    tess.addVertex(lx, height, lz);
                    tess.draw();
                }
            }
            // Corner accent lines
            float[][] corners = {{-radius, -radius}, {radius, -radius}, {radius, radius}, {-radius, radius}};
            for (float[] corner : corners) {
                tess.startDrawing(GL11.GL_LINES);
                tess.setColorRGBA_F(r, g, b, a);
                tess.addVertex(corner[0], 0.01, corner[1]);
                tess.setColorRGBA_F(r, g, b, 0.0f);
                tess.addVertex(corner[0], height, corner[1]);
                tess.draw();
            }
        } else {
            float rotRad = (float) Math.toRadians(rotationDeg);
            for (int i = 0; i < ACCENT_LINE_COUNT; i++) {
                float angle = (float) (Math.PI * 2 * i) / ACCENT_LINE_COUNT + rotRad;
                double lx = Math.cos(angle) * radius;
                double lz = Math.sin(angle) * radius;

                tess.startDrawing(GL11.GL_LINES);
                tess.setColorRGBA_F(r, g, b, a);
                tess.addVertex(lx, 0.01, lz);
                tess.setColorRGBA_F(r, g, b, 0.0f);
                tess.addVertex(lx, height, lz);
                tess.draw();
            }
        }
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity entity) {
        return null;
    }
}
