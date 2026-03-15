/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api
 */

/**
 * @javaFqn noppes.npcs.api.IPos
 */
export interface IPos {
    getX(): import('./int').int;
    getY(): import('./int').int;
    getZ(): import('./int').int;
    getXD(): import('./double').double;
    getYD(): import('./double').double;
    getZD(): import('./double').double;
    /**
     * Shifts the IPos up by 1 block and returns the new object.
     *
     * @return the position above
     */
    up(): import('./IPos').IPos;
    /**
     *
     * @param n The number of blocks to move the position up by.
     * @return The new IPos object.
     */
    up(n: import('./double').double): import('./IPos').IPos;
    /**
     * Shifts the IPos down by 1 block and returns the new object.
     *
     * @return the position below
     */
    down(): import('./IPos').IPos;
    /**
     *
     * @param n The number of blocks to move the position down by.
     * @return The new IPos object.
     */
    down(n: import('./double').double): import('./IPos').IPos;
    /**
     * Shifts the IPos north by 1 block and returns the new object.
     *
     * @return the position to the north
     */
    north(): import('./IPos').IPos;
    /**
     *
     * @param n The number of blocks to move the position north by.
     * @return The new IPos object.
     */
    north(n: import('./double').double): import('./IPos').IPos;
    /**
     * Shifts the IPos east by 1 block and returns the new object.
     *
     * @return the position to the east
     */
    east(): import('./IPos').IPos;
    /**
     *
     * @param n The number of blocks to move the position east by.
     * @return The new IPos object.
     */
    east(n: import('./double').double): import('./IPos').IPos;
    /**
     * Shifts the IPos south by 1 block and returns the new object.
     *
     * @return the position to the south
     */
    south(): import('./IPos').IPos;
    /**
     *
     * @param n The number of blocks to move the position south by.
     * @return The new IPos object.
     */
    south(n: import('./double').double): import('./IPos').IPos;
    /**
     * Shifts the IPos west by 1 block and returns the new object.
     *
     * @return the position to the west
     */
    west(): import('./IPos').IPos;
    /**
     *
     * @param n The number of blocks to move the position west by.
     * @return The new IPos object.
     */
    west(n: import('./double').double): import('./IPos').IPos;
    /**
     * Adds the IPos' coordinates by each of the parameters given.
     *
     * @param x X coordinate amount to be added
     * @param y Y coordinate amount to be added
     * @param z Z coordinate amount to be added
     * @return The resulting IPos from the addition.
     */
    add(x: import('./double').double, y: import('./double').double, z: import('./double').double): import('./IPos').IPos;
    /**
     * Directly adds the coordinates of two IPos objects and returns the resulting IPos sum.
     * For example, if one IPos was represented by the coordinates (0,60,5), and another by (-10,-30,25),
     * the resulting object's coordinates are (0 - 10,60 - 30,5 + 25) = (-10,30,30).
     *
     * @param pos The position to be added to this IPos object.
     * @return The sum of the two IPos objects as a new IPos object.
     */
    add(pos: import('./IPos').IPos): import('./IPos').IPos;
    /**
     * Subtracts the IPos' coordinates by each of the parameters given.
     *
     * @param x X coordinate amount to be subtracted
     * @param y Y coordinate amount to be subtracted
     * @param z Z coordinate amount to be subtracted
     * @return The resulting IPos from the subtraction.
     */
    subtract(x: import('./double').double, y: import('./double').double, z: import('./double').double): import('./IPos').IPos;
    /**
     * Directly subtracts the coordinates of two IPos objects and returns the resulting IPos difference.
     * For example, if one IPos was represented by the coordinates (0,60,5), and another by (-10,-30,25),
     * the resulting object's coordinates are (0 + 10,60 + 30,5 - 25) = (10,90,-20).
     *
     * @param pos The position to be subtracted from this IPos object.
     * @return The difference of the two IPos objects as a new IPos object.
     */
    subtract(pos: import('./IPos').IPos): import('./IPos').IPos;
    /**
     * Returns a normalized vector of this block's position, calculated by:
     *
     * @return The normalized vector of this block position.
     */
    normalize(): import('./IPos').IPos;
    normalizeDouble(): import('./double').double[];
    /**
     * Offsets the block by 1 block in the given direction and returns the resulting IPos object
     *
     * @param direction the direction ordinal
     * @return the offset position
     */
    offset(direction: import('./int').int): import('./IPos').IPos;
    /**
     * Offsets the block by n blocks in the given direction and returns the resulting IPos object
     *
     * @param direction the direction ordinal
     * @param n the distance
     * @return the offset position
     */
    offset(direction: import('./int').int, n: import('./double').double): import('./IPos').IPos;
    crossProduct(x: import('./double').double, y: import('./double').double, z: import('./double').double): import('./IPos').IPos;
    crossProduct(pos: import('./IPos').IPos): import('./IPos').IPos;
    divide(scalar: import('./double').double): import('./IPos').IPos;
    toLong(): import('./long').long;
    fromLong(serialized: import('./long').long): import('./IPos').IPos;
    /**
     *
     * @param pos The IPos object to calculate the distance to
     * @return The distance between this IPos object and the other.
     */
    distanceTo(pos: import('./IPos').IPos): import('./double').double;
    distanceTo(x: import('./double').double, y: import('./double').double, z: import('./double').double): import('./double').double;
    getMCPos(): import('../../../net/minecraft/util/math/BlockPos').BlockPos;
    blockPos: import('../../../net/minecraft/util/math/BlockPos').BlockPos;
}
