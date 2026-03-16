package common.minecraft.util;

/**
 * Platform abstraction for Minecraft's axis-aligned bounding box.
 * MC 1.7.10: AxisAlignedBB
 * MC 1.12+: AxisAlignedBB
 */
public interface IBoundingBox {
    double getMinX();
    double getMinY();
    double getMinZ();
    double getMaxX();
    double getMaxY();
    double getMaxZ();
    IBoundingBox expand(double x, double y, double z);
    IBoundingBox offset(double x, double y, double z);
    boolean isVecInside(double x, double y, double z);
}
