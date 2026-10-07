package noppes.npcs.client.guide;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;

public class GuideCurveTest {

    private static final double EPS = 1.0e-9;

    @Test
    public void chaikinKeepsEndpointsAndDoublesPointCount() {
        double[][] input = {{0, 0, 0}, {4, 0, 0}};
        double[][] out = GuideCurve.chaikinOnce(input);

        assertEquals(4, out.length);
        assertArrayEquals(new double[]{0, 0, 0}, out[0], EPS);
        assertArrayEquals(new double[]{1, 0, 0}, out[1], EPS);
        assertArrayEquals(new double[]{3, 0, 0}, out[2], EPS);
        assertArrayEquals(new double[]{4, 0, 0}, out[3], EPS);
    }

    @Test
    public void chaikinCutsTheCornerOfARightAngle() {
        // 直角折线：往 +x 走 4 格再往 +z 走 4 格。切角后拐点处不该再有 (4,0,0) 这个尖点。
        double[][] input = {{0, 0, 0}, {4, 0, 0}, {4, 0, 4}};
        double[][] out = GuideCurve.chaikinOnce(input);

        assertEquals(6, out.length);
        for (double[] p : out) {
            boolean isSharpCorner = Math.abs(p[0] - 4) < EPS && Math.abs(p[2] - 0) < EPS;
            org.junit.Assert.assertFalse("拐点尖角应该被切掉", isSharpCorner);
        }
    }

    @Test
    public void subdivideAppliesRoundsRepeatedly() {
        double[][] input = {{0, 0, 0}, {4, 0, 0}};

        assertEquals(4, GuideCurve.subdivide(input, 1).length);
        assertEquals(8, GuideCurve.subdivide(input, 2).length);
        assertEquals(2, GuideCurve.subdivide(input, 0).length);
    }

    @Test
    public void degenerateInputIsReturnedUnchanged() {
        assertEquals(0, GuideCurve.chaikinOnce(null).length);
        assertEquals(0, GuideCurve.chaikinOnce(new double[0][]).length);
        assertEquals(1, GuideCurve.chaikinOnce(new double[][]{{1, 2, 3}}).length);
    }

    @Test
    public void subdivideDoesNotMutateTheInput() {
        double[][] input = {{0, 0, 0}, {4, 0, 0}};
        GuideCurve.subdivide(input, 2);

        assertArrayEquals(new double[]{0, 0, 0}, input[0], EPS);
        assertArrayEquals(new double[]{4, 0, 0}, input[1], EPS);
    }

    @Test
    public void cumulativeLengthStartsAtZeroAndAccumulates() {
        double[][] input = {{0, 0, 0}, {3, 0, 4}, {3, 0, 4}, {3, 6, 4}};
        double[] out = GuideCurve.cumulativeLength(input);

        assertEquals(4, out.length);
        assertEquals(0.0, out[0], EPS);
        assertEquals(5.0, out[1], EPS);   // 3-4-5 直角三角形
        assertEquals(5.0, out[2], EPS);   // 重复点不增加弧长
        assertEquals(11.0, out[3], EPS);
    }

    @Test
    public void cumulativeLengthOfEmptyIsEmpty() {
        assertEquals(0, GuideCurve.cumulativeLength(new double[0][]).length);
        assertEquals(0, GuideCurve.cumulativeLength(null).length);
    }

    @Test
    public void nearestIndexFindsClosestPoint() {
        double[][] input = {{0, 0, 0}, {10, 0, 0}, {20, 0, 0}};

        assertEquals(0, GuideCurve.nearestIndex(input, 1, 0, 0));
        assertEquals(1, GuideCurve.nearestIndex(input, 9, 0, 0));
        assertEquals(2, GuideCurve.nearestIndex(input, 100, 0, 0));
    }

    @Test
    public void nearestIndexOfEmptyIsMinusOne() {
        assertEquals(-1, GuideCurve.nearestIndex(new double[0][], 0, 0, 0));
        assertEquals(-1, GuideCurve.nearestIndex(null, 0, 0, 0));
    }
}
