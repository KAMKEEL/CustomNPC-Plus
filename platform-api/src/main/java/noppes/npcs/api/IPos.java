package noppes.npcs.api;


public interface IPos {

    int getX();

    int getY();

    int getZ();

    double getXD();

    double getYD();

    double getZD();

    /**
     * Shifts the IPos up by 1 block and returns the new object.
     *
     * @return the position above
     */
    IPos up();

    /**
     *
     * @param n The number of blocks to move the position up by.
     * @return The new IPos object.
     */
    IPos up(double n);

    /**
     * Shifts the IPos down by 1 block and returns the new object.
     *
     * @return the position below
     */
    IPos down();

    /**
     *
     * @param n The number of blocks to move the position down by.
     * @return The new IPos object.
     */
    IPos down(double n);

    /**
     * Shifts the IPos north by 1 block and returns the new object.
     *
     * @return the position to the north
     */
    IPos north();

    /**
     *
     * @param n The number of blocks to move the position north by.
     * @return The new IPos object.
     */
    IPos north(double n);

    /**
     * Shifts the IPos east by 1 block and returns the new object.
     *
     * @return the position to the east
     */
    IPos east();

    /**
     *
     * @param n The number of blocks to move the position east by.
     * @return The new IPos object.
     */
    IPos east(double n);

    /**
     * Shifts the IPos south by 1 block and returns the new object.
     *
     * @return the position to the south
     */
    IPos south();

    /**
     *
     * @param n The number of blocks to move the position south by.
     * @return The new IPos object.
     */
    IPos south(double n);

    /**
     * Shifts the IPos west by 1 block and returns the new object.
     *
     * @return the position to the west
     */
    IPos west();

    /**
     *
     * @param n The number of blocks to move the position west by.
     * @return The new IPos object.
     */
    IPos west(double n);

    /**
     * Adds the IPos' coordinates by each of the parameters given.
     *
     * @param x X coordinate amount to be added
     * @param y Y coordinate amount to be added
     * @param z Z coordinate amount to be added
     * @return The resulting IPos from the addition.
     */
    IPos add(double x, double y, double z);

    /**
     * Directly adds the coordinates of two IPos objects and returns the resulting IPos sum.
     * For example, if one IPos was represented by the coordinates (0,60,5), and another by (-10,-30,25),
     * the resulting object's coordinates are (0 - 10,60 - 30,5 + 25) = (-10,30,30).
     *
     * @param pos The position to be added to this IPos object.
     * @return The sum of the two IPos objects as a new IPos object.
     */
    IPos add(IPos pos);

    /**
     * Subtracts the IPos' coordinates by each of the parameters given.
     *
     * @param x X coordinate amount to be subtracted
     * @param y Y coordinate amount to be subtracted
     * @param z Z coordinate amount to be subtracted
     * @return The resulting IPos from the subtraction.
     */
    IPos subtract(double x, double y, double z);

    /**
     * Directly subtracts the coordinates of two IPos objects and returns the resulting IPos difference.
     * For example, if one IPos was represented by the coordinates (0,60,5), and another by (-10,-30,25),
     * the resulting object's coordinates are (0 + 10,60 + 30,5 - 25) = (10,90,-20).
     *
     * @param pos The position to be subtracted from this IPos object.
     * @return The difference of the two IPos objects as a new IPos object.
     */
    IPos subtract(IPos pos);

    /**
     * Returns a normalized vector of this block's position, calculated by:
     *
     * @return The normalized vector of this block position.
     */
    IPos normalize();

    double[] normalizeDouble();

    /**
     * Offsets the block by 1 block in the given direction and returns the resulting IPos object
     *
     * @param direction the direction ordinal
     * @return the offset position
     */
    IPos offset(int direction);

    /**
     * Offsets the block by n blocks in the given direction and returns the resulting IPos object
     *
     * @param direction the direction ordinal
     * @param n the distance
     * @return the offset position
     */
    IPos offset(int direction, double n);

    IPos crossProduct(double x, double y, double z);

    IPos crossProduct(IPos pos);

    IPos divide(double scalar);

    long toLong();

    IPos fromLong(long serialized);

    /**
     *
     * @param pos The IPos object to calculate the distance to
     * @return The distance between this IPos object and the other.
     */
    double distanceTo(IPos pos);

    double distanceTo(double x, double y, double z);

    Object getMCPos();
}
