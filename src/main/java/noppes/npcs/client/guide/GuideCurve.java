package noppes.npcs.client.guide;

/**
 * 引路曲线的纯几何运算：Chaikin 切角细分、累计弧长、最近点查找。
 *
 * 这个类刻意不引用任何 Minecraft 类型——引路里最容易算错的数学都集中在这，放在这里
 * 才能用普通 JVM 单元测试覆盖，不需要拉起游戏运行时。
 *
 * 点一律用 double[]{x, y, z} 表示。
 */
public final class GuideCurve {

    private GuideCurve() {}

    /**
     * Chaikin 切角细分一轮：每条边替换成 1/4 和 3/4 两个点。
     *
     * 首尾端点原样保留——路径的头必须仍在玩家脚下、尾必须仍在 NPC 跟前，标准的闭合
     * Chaikin 会把两端也削掉，这里不能那么做。n 个点进，2n 个点出。
     */
    public static double[][] chaikinOnce(double[][] points) {
        if (points == null || points.length < 2) {
            return copy(points);
        }
        double[][] out = new double[points.length * 2][];
        int w = 0;
        out[w++] = points[0].clone();
        for (int i = 0; i < points.length - 1; i++) {
            double[] a = points[i];
            double[] b = points[i + 1];
            out[w++] = lerp(a, b, 0.25);
            out[w++] = lerp(a, b, 0.75);
        }
        out[w] = points[points.length - 1].clone();
        return out;
    }

    /** 连做 rounds 轮细分。rounds 为 0 时返回一份副本，不改动入参。 */
    public static double[][] subdivide(double[][] points, int rounds) {
        double[][] result = copy(points);
        for (int i = 0; i < rounds; i++) {
            result = chaikinOnce(result);
        }
        return result;
    }

    /**
     * 沿曲线的累计弧长。返回数组长度跟点数一致，第 0 项恒为 0。
     * 能量脉冲拿它当相位，所以波才会沿着线往目标方向跑，而不是整条线一起明灭。
     */
    public static double[] cumulativeLength(double[][] points) {
        if (points == null || points.length == 0) {
            return new double[0];
        }
        double[] out = new double[points.length];
        for (int i = 1; i < points.length; i++) {
            out[i] = out[i - 1] + distance(points[i - 1], points[i]);
        }
        return out;
    }

    /** 离给定位置最近的点的下标；points 为空返回 -1。渲染时用它裁掉玩家已经走过的那一段。 */
    public static int nearestIndex(double[][] points, double x, double y, double z) {
        if (points == null || points.length == 0) {
            return -1;
        }
        int best = 0;
        double bestSq = Double.MAX_VALUE;
        for (int i = 0; i < points.length; i++) {
            double dx = points[i][0] - x;
            double dy = points[i][1] - y;
            double dz = points[i][2] - z;
            double sq = dx * dx + dy * dy + dz * dz;
            if (sq < bestSq) {
                bestSq = sq;
                best = i;
            }
        }
        return best;
    }

    private static double[] lerp(double[] a, double[] b, double t) {
        return new double[]{
            a[0] + (b[0] - a[0]) * t,
            a[1] + (b[1] - a[1]) * t,
            a[2] + (b[2] - a[2]) * t
        };
    }

    private static double distance(double[] a, double[] b) {
        double dx = b[0] - a[0];
        double dy = b[1] - a[1];
        double dz = b[2] - a[2];
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    private static double[][] copy(double[][] points) {
        if (points == null) {
            return new double[0][];
        }
        double[][] out = new double[points.length][];
        for (int i = 0; i < points.length; i++) {
            out[i] = points[i].clone();
        }
        return out;
    }
}
