package noppes.npcs.client.guide;

/**
 * 引路的两条透明度曲线和能量脉冲。纯数学，不引用 Minecraft 类型，可单元测试。
 */
public final class GuideFade {

    private GuideFade() {}

    /**
     * 路径带某个顶点的距离衰减：近处实、远处淡出、超出范围完全不画。
     * 玩家往前走时，前方原本淡掉的线段会依次浮现——这是整个视觉效果的核心。
     */
    public static double pathAlpha(double distance) {
        if (distance <= GuideRenderConfig.PATH_FULL_DISTANCE) {
            return 1.0;
        }
        if (distance >= GuideRenderConfig.PATH_FADE_DISTANCE) {
            return 0.0;
        }
        double span = GuideRenderConfig.PATH_FADE_DISTANCE - GuideRenderConfig.PATH_FULL_DISTANCE;
        return (GuideRenderConfig.PATH_FADE_DISTANCE - distance) / span;
    }

    /** 菱形终点标记的渐显：远处完全不可见，越靠近越实。 */
    public static double markerAlpha(double distance) {
        if (distance <= GuideRenderConfig.MARKER_FULL_DISTANCE) {
            return 1.0;
        }
        if (distance >= GuideRenderConfig.MARKER_FADE_DISTANCE) {
            return 0.0;
        }
        double span = GuideRenderConfig.MARKER_FADE_DISTANCE - GuideRenderConfig.MARKER_FULL_DISTANCE;
        return (GuideRenderConfig.MARKER_FADE_DISTANCE - distance) / span;
    }

    /**
     * 能量脉冲。弧长参与相位所以波沿线往目标方向跑；time 传连续量
     * （worldTime + partialTicks），逐帧平滑不跳变。
     *
     * 返回值恒在 [BASE-AMP, BASE+AMP] 内且不会到 0，波谷处线仍然可见——这是刻意的，
     * 完全断开的虚线会让人误以为路径本身断了。
     */
    public static double pulse(double arcLength, double time) {
        double phase = arcLength * GuideRenderConfig.PULSE_SPATIAL_FREQ
                     - time * GuideRenderConfig.PULSE_TIME_SPEED;
        return GuideRenderConfig.PULSE_BASE
             + GuideRenderConfig.PULSE_AMPLITUDE * Math.sin(phase);
    }
}
