package noppes.npcs.util;

public class ValueUtil {
    public static float clamp(float given, float min, float max) {
        if (given < min)
            return min;
        return Math.min(given, max);
    }

    public static int clamp(int given, int min, int max) {
        if (given < min)
            return min;
        return Math.min(given, max);
    }

    public static byte clamp(byte given, byte min, byte max) {
        if (given < min)
            return min;
        if (given > max)
            return max;
        return given;
    }

    public static double clamp(double given, double min, double max) {
        if (given < min)
            return min;
        return Math.min(given, max);
    }

    public static long clamp(long given, long min, long max) {
        if (given < min)
            return min;
        return Math.min(given, max);
    }

    public static float correctFloat(float given, float min, float max) {
        if (given < min)
            return min;
        if (given > max)
            return max;
        return given;
    }

    public static int CorrectInt(int given, int min, int max) {
        if (given < min)
            return min;
        if (given > max)
            return max;
        return given;
    }
}
