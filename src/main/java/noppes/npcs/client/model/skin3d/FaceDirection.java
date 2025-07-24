package noppes.npcs.client.model.skin3d;

import net.minecraft.util.Vec3i;
import net.minecraft.util.Vec3;

public enum FaceDirection {
    DOWN(new Vec3i(0, -1, 0)),
    UP(new Vec3i(0, 1, 0)),
    NORTH(new Vec3i(0, 0, -1)),
    SOUTH(new Vec3i(0, 0, 1)),
    WEST(new Vec3i(-1, 0, 0)),
    EAST(new Vec3i(1, 0, 0));

    private final Vec3i normal;

    FaceDirection(Vec3i normal) {
        this.normal = normal;
    }

    public int getStepX() { return normal.getX(); }
    public int getStepY() { return normal.getY(); }
    public int getStepZ() { return normal.getZ(); }

    public Vec3 step() { return Vec3.createVectorHelper(getStepX(), getStepY(), getStepZ()); }
}
