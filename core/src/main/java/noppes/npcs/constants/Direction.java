package noppes.npcs.constants;

public enum Direction {
    DOWN(0, -1, 0),
    UP(0, 1, 0),
    NORTH(0, 0, -1),
    SOUTH(0, 0, 1),
    WEST(-1, 0, 0),
    EAST(1, 0, 0);

    private final int offsetX, offsetY, offsetZ;

    Direction(int x, int y, int z) {
        this.offsetX = x;
        this.offsetY = y;
        this.offsetZ = z;
    }

    public int getOffsetX() { return offsetX; }
    public int getOffsetY() { return offsetY; }
    public int getOffsetZ() { return offsetZ; }

    public static Direction byIndex(int index) {
        return values()[index % values().length];
    }
}
