package common.minecraft.util;

/**
 * Platform abstraction for Minecraft's 3D vector.
 * MC 1.7.10: Vec3
 * MC 1.12+: Vec3d
 */
public interface IVector3 {
    double getX();
    double getY();
    double getZ();
    IVector3 normalize();
    double lengthVector();
    double distanceTo(IVector3 other);
    IVector3 addVector(double x, double y, double z);
    IVector3 subtract(IVector3 other);
    double dotProduct(IVector3 other);
    IVector3 crossProduct(IVector3 other);
}
