/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api
 */

export interface IPos {

    // Methods
    getX(): number;
    getY(): number;
    getZ(): number;
    getXD(): number;
    getYD(): number;
    getZD(): number;
    up(): import('./IPos').IPos;
    up(n: number): import('./IPos').IPos;
    down(): import('./IPos').IPos;
    down(n: number): import('./IPos').IPos;
    north(): import('./IPos').IPos;
    north(n: number): import('./IPos').IPos;
    east(): import('./IPos').IPos;
    east(n: number): import('./IPos').IPos;
    south(): import('./IPos').IPos;
    south(n: number): import('./IPos').IPos;
    west(): import('./IPos').IPos;
    west(n: number): import('./IPos').IPos;
    add(x: number, y: number, z: number): import('./IPos').IPos;
    add(pos: import('./IPos').IPos): import('./IPos').IPos;
    subtract(x: number, y: number, z: number): import('./IPos').IPos;
    subtract(pos: import('./IPos').IPos): import('./IPos').IPos;
    normalize(): import('./IPos').IPos;
    normalizeDouble(): number[];
    offset(direction: number): import('./IPos').IPos;
    offset(direction: number, n: number): import('./IPos').IPos;
    crossProduct(x: number, y: number, z: number): import('./IPos').IPos;
    crossProduct(pos: import('./IPos').IPos): import('./IPos').IPos;
    divide(scalar: number): import('./IPos').IPos;
    toLong(): number;
    fromLong(serialized: number): import('./IPos').IPos;
    distanceTo(pos: import('./IPos').IPos): number;
    distanceTo(x: number, y: number, z: number): number;
    getMCPos(): import('../../../net/minecraft/util/math/BlockPos').BlockPos;

}
