package kamkeel.npcs.client.renderer.lightning;

import net.minecraft.client.renderer.Tessellator;
import org.lwjgl.opengl.GL11;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Renders lightning arcs attached to an entity (in local space).
 * Supports both instant arcs and fading persistent bolts that move with the entity.
 */
public class AttachedLightningRenderer {

    private static final Random rand = new Random();

    /**
     * Data for a single lightning bolt with fading support.
     */
    public static class LightningArc {
        public List<double[]> points;
        public int age;
        public int maxAge;
        public int outerColor;
        public int innerColor;

        public LightningArc(List<double[]> points, int maxAge, int outerColor, int innerColor) {
            this.points = points;
            this.age = 0;
            this.maxAge = maxAge;
            this.outerColor = outerColor;
            this.innerColor = innerColor;
        }

        public float getAlphaMultiplier() {
            // Fade out over lifetime
            return 1.0f - ((float) age / (float) maxAge);
        }

        public boolean isDead() {
            return age >= maxAge;
        }

        public void tick() {
            age++;
        }
    }

    /**
     * Storage for persistent lightning arcs per entity.
     * Call updateAndRender() each frame from the entity's renderer.
     */
    public static class LightningState {
        private final List<LightningArc> arcs = new ArrayList<>();
        private float spawnAccumulator = 0;

        /**
         * Update arc ages, remove dead arcs, spawn new ones based on density.
         *
         * @param density    Arcs per tick (can be fractional)
         * @param radius     Max arc distance from center
         * @param outerColor Outer glow color
         * @param innerColor Inner core color
         * @param maxAge     How many ticks arcs live (for fading)
         */
        public void update(float density, float radius, int outerColor, int innerColor, int maxAge) {
            // Age existing arcs and remove dead ones
            Iterator<LightningArc> iter = arcs.iterator();
            while (iter.hasNext()) {
                LightningArc arc = iter.next();
                arc.tick();
                if (arc.isDead()) {
                    iter.remove();
                }
            }

            // Spawn new arcs based on density
            spawnAccumulator += density;
            while (spawnAccumulator >= 1.0f) {
                spawnAccumulator -= 1.0f;
                arcs.add(createArc(radius, outerColor, innerColor, maxAge));
            }
        }

        /**
         * Render all active arcs in local space.
         * Call this after glTranslated to entity position.
         */
        public void render() {
            if (arcs.isEmpty()) return;

            // Setup GL state
            GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glDisable(GL11.GL_CULL_FACE);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glDepthMask(false);

            Tessellator tess = Tessellator.instance;

            for (LightningArc arc : arcs) {
                float alphaMult = arc.getAlphaMultiplier();

                // Render outer glow
                renderBoltPath(tess, arc.points, arc.outerColor, 0.025f, 0.35f * alphaMult);

                // Render inner core
                renderBoltPath(tess, arc.points, arc.innerColor, 0.012f, 0.7f * alphaMult);
            }

            GL11.glDepthMask(true);
            GL11.glPopAttrib();
        }

        public int getArcCount() {
            return arcs.size();
        }
    }

    /**
     * Create a new lightning arc from center to a random point.
     */
    private static LightningArc createArc(float radius, int outerColor, int innerColor, int maxAge) {
        // Random end point on sphere
        double theta = rand.nextDouble() * Math.PI * 2;
        double phi = Math.acos(2 * rand.nextDouble() - 1);
        double r = radius * (0.5 + rand.nextDouble() * 0.5);

        double endX = r * Math.sin(phi) * Math.cos(theta);
        double endY = r * Math.sin(phi) * Math.sin(theta);
        double endZ = r * Math.cos(phi);

        int segments = 4 + rand.nextInt(3);
        float displacement = radius * 0.25f;

        List<double[]> points = generateLightningPath(0, 0, 0, endX, endY, endZ, segments, displacement);

        return new LightningArc(points, maxAge, outerColor, innerColor);
    }

    /**
     * Generate a jagged lightning path between two points.
     */
    private static List<double[]> generateLightningPath(double x1, double y1, double z1,
                                                         double x2, double y2, double z2,
                                                         int segments, float displacement) {
        List<double[]> points = new ArrayList<>();
        points.add(new double[]{x1, y1, z1});

        double dx = x2 - x1;
        double dy = y2 - y1;
        double dz = z2 - z1;

        // Create perpendicular vectors for displacement
        Vec3d dir = new Vec3d(dx, dy, dz);
        Vec3d perp1 = dir.copy().perpendicular().normalize();
        Vec3d perp2 = dir.copy().crossProduct(perp1).normalize();

        for (int i = 1; i < segments; i++) {
            double t = (double) i / segments;

            double px = x1 + dx * t;
            double py = y1 + dy * t;
            double pz = z1 + dz * t;

            // Displacement strongest in middle
            float strength = (float) (1.0 - Math.abs(t - 0.5) * 2.0) * displacement;
            double offset1 = (rand.nextDouble() - 0.5) * 2 * strength;
            double offset2 = (rand.nextDouble() - 0.5) * 2 * strength;

            px += perp1.x * offset1 + perp2.x * offset2;
            py += perp1.y * offset1 + perp2.y * offset2;
            pz += perp1.z * offset1 + perp2.z * offset2;

            points.add(new double[]{px, py, pz});
        }

        points.add(new double[]{x2, y2, z2});
        return points;
    }

    /**
     * Render the lightning path as quads.
     */
    private static void renderBoltPath(Tessellator tess, List<double[]> points, int color, float width, float alpha) {
        if (alpha <= 0.01f) return;

        Color c = new Color(color);
        int r = c.getRed();
        int g = c.getGreen();
        int b = c.getBlue();
        int a = Math.min(255, Math.max(0, (int) (alpha * 255)));

        for (int i = 0; i < points.size() - 1; i++) {
            double[] p1 = points.get(i);
            double[] p2 = points.get(i + 1);

            double segDx = p2[0] - p1[0];
            double segDy = p2[1] - p1[1];
            double segDz = p2[2] - p1[2];

            Vec3d segDir = new Vec3d(segDx, segDy, segDz);
            Vec3d perp = segDir.copy().perpendicular().normalize().multiply(width);

            // First quad
            tess.startDrawingQuads();
            tess.setBrightness(0xF000F0);
            tess.setColorRGBA(r, g, b, a);

            tess.addVertex(p1[0] - perp.x, p1[1] - perp.y, p1[2] - perp.z);
            tess.addVertex(p1[0] + perp.x, p1[1] + perp.y, p1[2] + perp.z);
            tess.addVertex(p2[0] + perp.x, p2[1] + perp.y, p2[2] + perp.z);
            tess.addVertex(p2[0] - perp.x, p2[1] - perp.y, p2[2] - perp.z);

            tess.draw();

            // Second quad (perpendicular for 3D look)
            Vec3d perp2 = segDir.copy().crossProduct(perp).normalize().multiply(width);

            tess.startDrawingQuads();
            tess.setBrightness(0xF000F0);
            tess.setColorRGBA(r, g, b, a);

            tess.addVertex(p1[0] - perp2.x, p1[1] - perp2.y, p1[2] - perp2.z);
            tess.addVertex(p1[0] + perp2.x, p1[1] + perp2.y, p1[2] + perp2.z);
            tess.addVertex(p2[0] + perp2.x, p2[1] + perp2.y, p2[2] + perp2.z);
            tess.addVertex(p2[0] - perp2.x, p2[1] - perp2.y, p2[2] - perp2.z);

            tess.draw();
        }
    }

    // ==================== INSTANT RENDERING (no persistence) ====================

    /**
     * Render a single instant lightning arc (no fading, drawn this frame only).
     */
    public static void renderInstantArc(float radius, int outerColor, int innerColor) {
        double theta = rand.nextDouble() * Math.PI * 2;
        double phi = Math.acos(2 * rand.nextDouble() - 1);
        double r = radius * (0.5 + rand.nextDouble() * 0.5);

        double endX = r * Math.sin(phi) * Math.cos(theta);
        double endY = r * Math.sin(phi) * Math.sin(theta);
        double endZ = r * Math.cos(phi);

        int segments = 4 + rand.nextInt(3);
        float displacement = radius * 0.25f;

        List<double[]> points = generateLightningPath(0, 0, 0, endX, endY, endZ, segments, displacement);

        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDepthMask(false);

        Tessellator tess = Tessellator.instance;
        renderBoltPath(tess, points, outerColor, 0.025f, 0.35f);
        renderBoltPath(tess, points, innerColor, 0.012f, 0.7f);

        GL11.glDepthMask(true);
        GL11.glPopAttrib();
    }

    /**
     * Render multiple instant arcs.
     */
    public static void renderCracklingLightning(int count, float radius, int outerColor, int innerColor) {
        for (int i = 0; i < count; i++) {
            renderInstantArc(radius, outerColor, innerColor);
        }
    }
}
