package noppes.npcs.client.guide;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GuideFadeTest {

    private static final double EPS = 1.0e-9;

    @Test
    public void pathIsFullyOpaqueWithinFifteenBlocks() {
        assertEquals(1.0, GuideFade.pathAlpha(0.0), EPS);
        assertEquals(1.0, GuideFade.pathAlpha(10.0), EPS);
        assertEquals(1.0, GuideFade.pathAlpha(15.0), EPS);
    }

    @Test
    public void pathFadesLinearlyBetweenFifteenAndTwenty() {
        assertEquals(0.5, GuideFade.pathAlpha(17.5), EPS);
        assertEquals(0.8, GuideFade.pathAlpha(16.0), EPS);
        assertEquals(0.2, GuideFade.pathAlpha(19.0), EPS);
    }

    @Test
    public void pathIsInvisibleBeyondTwentyBlocks() {
        assertEquals(0.0, GuideFade.pathAlpha(20.0), EPS);
        assertEquals(0.0, GuideFade.pathAlpha(100.0), EPS);
    }

    @Test
    public void pathAlphaNeverLeavesUnitRange() {
        for (double d = 0.0; d <= 40.0; d += 0.25) {
            double a = GuideFade.pathAlpha(d);
            assertTrue("alpha 越界: d=" + d + " a=" + a, a >= 0.0 && a <= 1.0);
        }
    }

    @Test
    public void markerIsFullyOpaqueWithinFiveBlocks() {
        assertEquals(1.0, GuideFade.markerAlpha(0.0), EPS);
        assertEquals(1.0, GuideFade.markerAlpha(5.0), EPS);
    }

    @Test
    public void markerFadesLinearlyBetweenFiveAndFifteen() {
        assertEquals(0.5, GuideFade.markerAlpha(10.0), EPS);
        assertEquals(0.9, GuideFade.markerAlpha(6.0), EPS);
        assertEquals(0.1, GuideFade.markerAlpha(14.0), EPS);
    }

    @Test
    public void markerIsInvisibleBeyondFifteenBlocks() {
        assertEquals(0.0, GuideFade.markerAlpha(15.0), EPS);
        assertEquals(0.0, GuideFade.markerAlpha(800.0), EPS);
    }

    @Test
    public void pulseStaysWithinBaseAndAmplitude() {
        double min = GuideRenderConfig.PULSE_BASE - GuideRenderConfig.PULSE_AMPLITUDE;
        double max = GuideRenderConfig.PULSE_BASE + GuideRenderConfig.PULSE_AMPLITUDE;
        for (double s = 0.0; s < 200.0; s += 0.5) {
            for (double t = 0.0; t < 200.0; t += 7.0) {
                double p = GuideFade.pulse(s, t);
                assertTrue("脉冲越界: s=" + s + " t=" + t + " p=" + p, p >= min - EPS && p <= max + EPS);
            }
        }
    }

    @Test
    public void pulseNeverReachesZeroSoTheLineStaysVisibleAtTroughs() {
        assertTrue(GuideRenderConfig.PULSE_BASE - GuideRenderConfig.PULSE_AMPLITUDE > 0.0);
    }

    @Test
    public void pulseTravelsAlongTheCurveOverTime() {
        // t=0 时位于弧长 0 的那个相位，到 t=10 时应该出现在弧长更大的位置——
        // 也就是波朝目标跑，不是朝玩家倒流。
        double laterTime = 10.0;
        double sMatchingSamePhase = laterTime * GuideRenderConfig.PULSE_TIME_SPEED
                                  / GuideRenderConfig.PULSE_SPATIAL_FREQ;

        assertTrue("同一相位应随时间往弧长增大的方向移动", sMatchingSamePhase > 0.0);
        assertEquals(GuideFade.pulse(0.0, 0.0), GuideFade.pulse(sMatchingSamePhase, laterTime), EPS);
    }
}
