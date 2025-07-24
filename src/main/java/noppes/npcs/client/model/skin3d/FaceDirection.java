package noppes.npcs.client.model.skin3d;

import net.minecraft.util.Vec3;

public enum FaceDirection {
    DOWN(0, -1, 0),
    UP(0, 1, 0),
    NORTH(0, 0, -1),
    SOUTH(0, 0, 1),
    WEST(-1, 0, 0),
    EAST(1, 0, 0);

    private final int stepX;
    private final int stepY;
    private final int stepZ;

    FaceDirection(int x, int y, int z) {
        this.stepX = x;
        this.stepY = y;
        this.stepZ = z;
    }

    public int getStepX() { return stepX; }
    public int getStepY() { return stepY; }
    public int getStepZ() { return stepZ; }

    public Vec3 step() { return Vec3.createVectorHelper(stepX, stepY, stepZ); }
}
