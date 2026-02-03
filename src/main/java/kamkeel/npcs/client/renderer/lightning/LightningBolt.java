package kamkeel.npcs.client.renderer.lightning;

import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Lightning bolt data model with fractal segment generation.
 * Adapted from Botania's LightningBolt by Vazkii/ChickenBones.
 */
public class LightningBolt {

    public static ConcurrentLinkedQueue<LightningBolt> boltList = new ConcurrentLinkedQueue<>();

    public ArrayList<Segment> segments = new ArrayList<>();
    public Vec3d start;
    public Vec3d end;

    public double length;
    public int numSegments0;
    private int numSplits;
    private boolean finalized;
    private Random rand;
    public long seed;

    public int particleAge;
    public int particleMaxAge;
    public boolean isDead;

    private World world;
    private HashMap<Integer, Integer> splitParents = new HashMap<>();

    public float speed = 1.5F;
    public static final int FADE_TIME = 4; // Short lifespan for projectile-attached lightning

    public int colorOuter;
    public int colorInner;

    public LightningBolt(World world, Vec3d start, Vec3d end, float ticksPerMeter, long seed, int colorOuter, int colorInner) {
        this.world = world;
        this.seed = seed;
        this.rand = new Random(seed);

        this.start = start;
        this.end = end;
        this.speed = ticksPerMeter;

        this.colorOuter = colorOuter;
        this.colorInner = colorInner;

        this.numSegments0 = 1;
        this.length = end.copy().subtract(start).mag();
        this.particleMaxAge = FADE_TIME + rand.nextInt(FADE_TIME) - FADE_TIME / 2;
        this.particleAge = -(int) (length * speed);

        segments.add(new Segment(start, end));
    }

    /**
     * Apply default fractal splitting for natural-looking lightning.
     */
    public void defaultFractal() {
        fractal(2, length / 1.5, 0.7F, 0.7F, 45);
        fractal(2, length / 4, 0.5F, 0.8F, 50);
        fractal(2, length / 15, 0.5F, 0.9F, 55);
        fractal(2, length / 30, 0.5F, 1.0F, 60);
        fractal(2, length / 60, 0, 0, 0);
        fractal(2, length / 100, 0, 0, 0);
        fractal(2, length / 400, 0, 0, 0);
    }

    /**
     * Simplified fractal for short-lived projectile lightning.
     * Less detail but faster and more appropriate for fast-moving objects.
     */
    public void simpleFractal() {
        fractal(2, length / 2.0, 0.4F, 0.6F, 35);
        fractal(2, length / 6, 0.3F, 0.7F, 40);
        fractal(2, length / 20, 0, 0, 0);
    }

    /**
     * Apply fractal splitting to create branching lightning.
     *
     * @param splits      Number of splits per segment
     * @param amount      Displacement amount
     * @param splitChance Probability of branching (0-1)
     * @param splitLength Length multiplier for branches
     * @param splitAngle  Angle for branch splits in degrees
     */
    public void fractal(int splits, double amount, double splitChance, double splitLength, double splitAngle) {
        if (finalized) return;

        ArrayList<Segment> oldSegments = segments;
        segments = new ArrayList<>();

        Segment prev = null;

        for (Segment segment : oldSegments) {
            prev = segment.prev;

            Vec3d subSegment = segment.diff.copy().multiply(1.0 / splits);

            BoltPoint[] newPoints = new BoltPoint[splits + 1];

            Vec3d startPoint = segment.startPoint.point;
            newPoints[0] = segment.startPoint;
            newPoints[splits] = segment.endPoint;

            for (int i = 1; i < splits; i++) {
                Vec3d randOff = segment.diff.copy().perpendicular().normalize()
                    .rotate(rand.nextFloat() * 360, segment.diff);
                randOff.multiply((rand.nextFloat() - 0.5F) * amount * 2);

                Vec3d basePoint = startPoint.copy().add(subSegment.copy().multiply(i));

                newPoints[i] = new BoltPoint(basePoint, randOff);
            }

            for (int i = 0; i < splits; i++) {
                Segment next = new Segment(newPoints[i], newPoints[i + 1], segment.light,
                    segment.segmentNo * splits + i, segment.splitNo);
                next.prev = prev;
                if (prev != null) {
                    prev.next = next;
                }

                if (i != 0 && rand.nextFloat() < splitChance) {
                    Vec3d splitRot = next.diff.copy().xCrossProduct()
                        .rotate(rand.nextFloat() * 360, next.diff);
                    Vec3d diff = next.diff.copy()
                        .rotate((rand.nextFloat() * 0.66F + 0.33F) * splitAngle, splitRot)
                        .multiply(splitLength);

                    numSplits++;
                    splitParents.put(numSplits, next.splitNo);

                    Segment split = new Segment(newPoints[i],
                        new BoltPoint(newPoints[i + 1].basePoint, newPoints[i + 1].offsetVec.copy().add(diff)),
                        segment.light / 2F, next.segmentNo, numSplits);
                    split.prev = prev;

                    segments.add(split);
                }

                prev = next;
                segments.add(next);
            }

            if (segment.next != null) {
                segment.next.prev = prev;
            }
        }

        numSegments0 *= splits;
    }

    /**
     * Finalize the bolt after all fractal passes.
     */
    public void finalizeBolt() {
        if (finalized) return;
        finalized = true;

        calculateEndDiffs();
        Collections.sort(segments, new SegmentLightSorter());
    }

    private void calculateEndDiffs() {
        Collections.sort(segments, new SegmentSorter());

        for (Segment segment : segments) {
            segment.calcEndDiffs();
        }
    }

    public void onUpdate() {
        particleAge++;
        if (particleAge >= particleMaxAge) {
            isDead = true;
        }
    }

    /**
     * Update all active lightning bolts. Called from client tick handler.
     */
    public static void updateAll() {
        for (Iterator<LightningBolt> iterator = boltList.iterator(); iterator.hasNext(); ) {
            LightningBolt bolt = iterator.next();
            bolt.onUpdate();
            if (bolt.isDead) {
                iterator.remove();
            }
        }
    }

    // ==================== INNER CLASSES ====================

    public class BoltPoint {
        public Vec3d point;
        public Vec3d basePoint;
        public Vec3d offsetVec;

        public BoltPoint(Vec3d basePoint, Vec3d offsetVec) {
            this.point = basePoint.copy().add(offsetVec);
            this.basePoint = basePoint;
            this.offsetVec = offsetVec;
        }
    }

    public class Segment {
        public BoltPoint startPoint;
        public BoltPoint endPoint;

        public Vec3d diff;
        public Segment prev;
        public Segment next;

        public Vec3d nextDiff;
        public Vec3d prevDiff;

        public float sinPrev;
        public float sinNext;
        public float light;

        public int segmentNo;
        public int splitNo;

        public Segment(BoltPoint start, BoltPoint end, float light, int segmentNo, int splitNo) {
            this.startPoint = start;
            this.endPoint = end;
            this.light = light;
            this.segmentNo = segmentNo;
            this.splitNo = splitNo;
            calcDiff();
        }

        public Segment(Vec3d start, Vec3d end) {
            this(new BoltPoint(start, new Vec3d(0, 0, 0)),
                 new BoltPoint(end, new Vec3d(0, 0, 0)), 1, 0, 0);
        }

        public void calcDiff() {
            diff = endPoint.point.copy().subtract(startPoint.point);
        }

        public void calcEndDiffs() {
            if (prev != null) {
                Vec3d prevDiffNorm = prev.diff.copy().normalize();
                Vec3d thisDiffNorm = diff.copy().normalize();

                prevDiff = thisDiffNorm.copy().add(prevDiffNorm).normalize();
                sinPrev = (float) Math.sin(thisDiffNorm.angle(prevDiffNorm.multiply(-1)) / 2);
            } else {
                prevDiff = diff.copy().normalize();
                sinPrev = 1;
            }

            if (next != null) {
                Vec3d nextDiffNorm = next.diff.copy().normalize();
                Vec3d thisDiffNorm = diff.copy().normalize();

                nextDiff = thisDiffNorm.add(nextDiffNorm).normalize();
                sinNext = (float) Math.sin(thisDiffNorm.angle(nextDiffNorm.multiply(-1)) / 2);
            } else {
                nextDiff = diff.copy().normalize();
                sinNext = 1;
            }
        }
    }

    public class SegmentSorter implements Comparator<Segment> {
        @Override
        public int compare(Segment o1, Segment o2) {
            int comp = Integer.compare(o1.splitNo, o2.splitNo);
            if (comp == 0) {
                return Integer.compare(o1.segmentNo, o2.segmentNo);
            }
            return comp;
        }
    }

    public class SegmentLightSorter implements Comparator<Segment> {
        @Override
        public int compare(Segment o1, Segment o2) {
            return Float.compare(o2.light, o1.light);
        }
    }
}
