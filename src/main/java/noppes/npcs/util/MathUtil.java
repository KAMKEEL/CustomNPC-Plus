package noppes.npcs.util;

import net.minecraft.util.Vec3;

public class MathUtil {

    public static float getYaw(Vec3 vector) {
        double x = vector.xCoord;
        double z = vector.zCoord;
        double yawRad   = Math.atan2(z, x);

        return 90F - (float) Math.toDegrees(yawRad);
    }

    public static float getPitch(Vec3 vector) {
        double x = vector.xCoord;
        double y = vector.yCoord;
        double z = vector.zCoord;
        double horizontalMag = Math.sqrt(x * x + z * z);
        double pitchRad = Math.atan2(y, horizontalMag);

        return (float) -Math.toDegrees(pitchRad);
    }
}
