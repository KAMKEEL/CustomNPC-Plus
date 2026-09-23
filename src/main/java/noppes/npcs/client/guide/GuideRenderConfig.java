package noppes.npcs.client.guide;

/**
 * 引路功能的全部可调常量。调手感时只需要动这一个文件。
 *
 * 跟 GuideCurve 一样刻意不引用任何 Minecraft 类型，好让依赖它的 GuideFade 能一起被
 * 单元测试覆盖。
 */
public final class GuideRenderConfig {

    private GuideRenderConfig() {}

    // ---- 路径带的距离衰减（顶点到玩家的三维距离，单位：格）----
    /** 这个距离以内完全不透明。 */
    public static final double PATH_FULL_DISTANCE = 15.0;
    /** 这个距离以外完全不画。两者之间线性衰减。 */
    public static final double PATH_FADE_DISTANCE = 20.0;

    // ---- 菱形终点标记的距离渐显（玩家到目标的三维距离，单位：格）----
    public static final double MARKER_FULL_DISTANCE = 5.0;
    public static final double MARKER_FADE_DISTANCE = 15.0;

    // ---- 能量脉冲 ----
    /** 波谷也保留的底亮度。必须大于 AMPLITUDE，否则波谷处线会完全消失。 */
    public static final double PULSE_BASE = 0.65;
    public static final double PULSE_AMPLITUDE = 0.35;
    /** 弧长系数，2π/0.35 ≈ 每 18 格一个波。 */
    public static final double PULSE_SPATIAL_FREQ = 0.35;
    /** 每 tick 的相位推进，换算下来波约每秒前进 3 格。 */
    public static final double PULSE_TIME_SPEED = 0.15;

    // ---- 发光带几何 ----
    public static final int CHAIKIN_ROUNDS = 2;
    /** 抬离地面一点，防 z-fighting。 */
    public static final double BAND_GROUND_OFFSET = 0.05;
    public static final double GLOW_HALF_WIDTH = 0.30;
    public static final double CORE_HALF_WIDTH = 0.10;
    public static final float GLOW_ALPHA_SCALE = 0.35f;

    // ---- 颜色 ----
    public static final int GLOW_R = 80;
    public static final int GLOW_G = 170;
    public static final int GLOW_B = 255;
    public static final int CORE_R = 200;
    public static final int CORE_G = 240;
    public static final int CORE_B = 255;

    // ---- 菱形标记 ----
    public static final double MARKER_SIZE = 0.6;
    /** 外发光菱形相对芯的放大倍数。 */
    public static final double MARKER_GLOW_SCALE = 1.6;
    public static final double MARKER_HEIGHT_ABOVE_EYES = 0.5;
    /** 呼吸幅度：尺寸在 (1 - 该值) 到 (1 + 该值) 倍之间缓慢起伏。 */
    public static final double MARKER_BREATHE_AMPLITUDE = 0.1;
    public static final double MARKER_BREATHE_SPEED = 0.1;
    /** 线框相对芯的放大倍数：贴着芯外面一圈，不要跟外发光重合。 */
    public static final double MARKER_OUTLINE_SCALE = 1.25;
    /** 线框线宽（像素）。驱动普遍只保证到 1~10，别往大了写。 */
    public static final float MARKER_OUTLINE_WIDTH = 2.0f;
    /** 线框比芯稍暗一点，免得糊成一块实心。 */
    public static final float MARKER_OUTLINE_ALPHA_SCALE = 0.8f;
    /** 自转速度：每 tick 转多少度。1.5 度/tick ≈ 12 秒一圈，属于「缓慢」。 */
    public static final double MARKER_SPIN_DEGREES_PER_TICK = 1.5;

    // ---- 会话节奏 ----
    /** 每秒重算一次路径。 */
    public static final int REPLAN_INTERVAL_TICKS = 20;
    /** 每 tick 给 A* 的时间片：20 毫秒。 */
    public static final long STEP_BUDGET_NANOS = 20_000_000L;
    /** 目标不在加载范围时，每 5 秒向服务端刷新一次坐标。 */
    public static final int REFRESH_INTERVAL_TICKS = 100;
    /** 连续这么多次刷新都找不到就判定跟丢。 */
    public static final int MAX_MISSED_REFRESHES = 3;
    /**
     * 纯本地的跟丢兜底：距上一次真正拿到目标位置超过这么多 tick 就判跟丢，与「连续 3 次刷新
     * 都找不到」等价（3 × 5 秒 = 15 秒）。
     *
     * 不能只靠服务端主动回的「找不到」——请求被权限闸丢掉、服务端那侧抛异常被吞、网络丢包，
     * 任何一种都会让那条回包永远不来，于是会话再也结束不了。这条只看本地时钟，不依赖对端。
     */
    public static final int TARGET_STALE_TIMEOUT_TICKS = REFRESH_INTERVAL_TICKS * MAX_MISSED_REFRESHES;
    public static final double ARRIVAL_DISTANCE = 3.0;

    // ---- 寻路预算 ----
    /**
     * 单次搜索的节点上限。引路是每秒重算的常驻功能，不能像 GTButler 的 /goto 那样不限时。
     */
    public static final int PATHFIND_MAX_NODES = 30000;
    /** 一个游戏 tick 的名义墙钟长度，毫秒。 */
    private static final long TICK_MILLIS = 50L;
    /**
     * 单次搜索的墙钟总时限，取一个 replan 周期的长度（20 tick × 50 毫秒 = 1 秒）。
     *
     * 原来是写死的 3 秒，但每 20 tick 就会起一个新搜索把旧的覆盖掉，一次搜索实际只活约 1 秒，
     * 3 秒的时限永远命中不了，等于只剩 MAX_NODES 一条上限在起作用。取成一个 replan 周期后
     * 它才真正成为兜底：算不完的搜索会自己收工并交出「目前最接近终点」的那条路，而不是被静默
     * 丢弃、屏幕上一条线都没有。
     */
    public static final long PATHFIND_TIME_BUDGET_NANOS =
        REPLAN_INTERVAL_TICKS * TICK_MILLIS * 1_000_000L;
}
