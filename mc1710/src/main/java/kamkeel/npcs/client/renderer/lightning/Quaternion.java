package kamkeel.npcs.client.renderer.lightning;

/**
 * Quaternion class for axis-angle rotations in lightning calculations.
 * Adapted from Botania's Quat by ChickenBones.
 */
public class Quaternion {
    public double x;
    public double y;
    public double z;
    public double s;

    public Quaternion() {
        s = 1.0;
        x = 0.0;
        y = 0.0;
        z = 0.0;
    }

    public Quaternion(Quaternion quat) {
        x = quat.x;
        y = quat.y;
        z = quat.z;
        s = quat.s;
    }

    public Quaternion(double s, double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.s = s;
    }

    public static Quaternion aroundAxis(double ax, double ay, double az, double angle) {
        angle *= 0.5;
        double sinAngle = Math.sin(angle);
        return new Quaternion(Math.cos(angle), ax * sinAngle, ay * sinAngle, az * sinAngle);
    }

    public static Quaternion aroundAxis(Vec3d axis, double angle) {
        return aroundAxis(axis.x, axis.y, axis.z, angle);
    }

    public void multiply(Quaternion quat) {
        double d = s * quat.s - x * quat.x - y * quat.y - z * quat.z;
        double d1 = s * quat.x + x * quat.s - y * quat.z + z * quat.y;
        double d2 = s * quat.y + x * quat.z + y * quat.s - z * quat.x;
        double d3 = s * quat.z - x * quat.y + y * quat.x + z * quat.s;
        s = d;
        x = d1;
        y = d2;
        z = d3;
    }

    public double mag() {
        return Math.sqrt(x * x + y * y + z * z + s * s);
    }

    public void normalize() {
        double d = mag();
        if (d == 0.0) {
            return;
        }
        d = 1.0 / d;
        x *= d;
        y *= d;
        z *= d;
        s *= d;
    }

    public void rotate(Vec3d vec) {
        double d = -x * vec.x - y * vec.y - z * vec.z;
        double d1 = s * vec.x + y * vec.z - z * vec.y;
        double d2 = s * vec.y - x * vec.z + z * vec.x;
        double d3 = s * vec.z + x * vec.y - y * vec.x;
        vec.x = d1 * s - d * x - d2 * z + d3 * y;
        vec.y = d2 * s - d * y + d1 * z - d3 * x;
        vec.z = d3 * s - d * z - d1 * y + d2 * x;
    }

    @Override
    public String toString() {
        return "Quaternion(" + s + ", " + x + ", " + y + ", " + z + ")";
    }
}
