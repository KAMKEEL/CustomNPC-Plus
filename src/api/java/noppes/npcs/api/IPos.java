//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package noppes.npcs.api;

import net.minecraft.util.math.BlockPos;

public interface IPos {
    int getX();

    int getY();

    int getZ();

    /**
     * Shifts the IPos up by 1 block and returns the new object.
     *
     */
    IPos up();

    /**
     *
     * @param n The number of blocks to move the position up by.
     * @return The new IPos object.
     */
    IPos up(int n);

    /**
     * Shifts the IPos down by 1 block and returns the new object.
     *
     */
    IPos down();

    /**
     *
     * @param n The number of blocks to move the position down by.
     * @return The new IPos object.
     */
    IPos down(int n);

    /**
     * Shifts the IPos north by 1 block and returns the new object.
     *
     */
    IPos north();

    /**
     *
     * @param n The number of blocks to move the position north by.
     * @return The new IPos object.
     */
    IPos north(int n);

    /**
     * Shifts the IPos east by 1 block and returns the new object.
     *
     */
    IPos east();

    /**
     *
     * @param n The number of blocks to move the position east by.
     * @return The new IPos object.
     */
    IPos east(int n);

    /**
     * Shifts the IPos south by 1 block and returns the new object.
     *
     */
    IPos south();

    /**
     *
     * @param n The number of blocks to move the position south by.
     * @return The new IPos object.
     */
    IPos south(int n);

    /**
     * Shifts the IPos west by 1 block and returns the new object.
     *
     */
    IPos west();

    /**
     *
     * @param n The number of blocks to move the position west by.
     * @return The new IPos object.
     */
    IPos west(int n);

    /**
     * Adds the IPos' coordinates by each of the parameters given.
     *
     * @param x X coordinate amount to be added
     * @param y Y coordinate amount to be added
     * @param z Z coordinate amount to be added
     * @return The resulting IPos from the addition.
     */
    IPos add(int x, int y, int z);

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
    IPos subtract(int x, int y, int z);

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
     *
     * @return The normalized vector of this block position.
     */
    double[] normalize();

    /**
     * Offsets the block by 1 block in the given direction and returns the resulting IPos object
     *
     */
    IPos offset(int direction);

    /**
     * Offsets the block by n blocks in the given direction and returns the resulting IPos object
     *
     */
    IPos offset(int direction, int n);

    IPos crossProduct(int x, int y, int z);

    IPos crossProduct(IPos pos);

    IPos divide(float scalar);

    long toLong();

    IPos fromLong(long serialized);

    /**
     *
     * @param pos The IPos object to calculate the distance to
     * @return The distance between this IPos object and the other.
     */
    double distanceTo(IPos pos);

    BlockPos getMCPos();
}
