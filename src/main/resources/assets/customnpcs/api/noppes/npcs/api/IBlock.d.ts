/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api
 */

export interface IBlock {

    // Methods
    getX(): number;
    getY(): number;
    getZ(): number;
    getPosition(): import('./IPos').IPos;
    setPosition(pos: import('./IPos').IPos, world: import('./IWorld').IWorld): boolean;
    setPosition(pos: import('./IPos').IPos): boolean;
    setPosition(x: number, y: number, z: number, world: import('./IWorld').IWorld): boolean;
    setPosition(x: number, y: number, z: number): boolean;
    getName(): string;
    remove(): void;
    isAir(): boolean;
    setBlock(blockName: string): import('./IBlock').IBlock;
    setBlock(block: import('./IBlock').IBlock): import('./IBlock').IBlock;
    isContainer(): boolean;
    getContainer(): import('./IContainer').IContainer;
    getWorld(): import('./IWorld').IWorld;
    hasTileEntity(): boolean;
    getTileEntity(): import('./ITileEntity').ITileEntity;
    setTileEntity(tileEntity: import('./ITileEntity').ITileEntity): void;
    getMCTileEntity(): TileEntity;
    getMCBlock(): Block;
    getDisplayName(): string;
    getTileEntityNBT(): import('./INbt').INbt;
    canCollide(maxVolume: number): boolean;
    canCollide(): boolean;
    setBounds(minX: number, minY: number, minZ: number, maxX: number, maxY: number, maxZ: number): void;
    getBlockBoundsMinX(): number;
    getBlockBoundsMinY(): number;
    getBlockBoundsMinZ(): number;
    getBlockBoundsMaxX(): number;
    getBlockBoundsMaxY(): number;
    getBlockBoundsMaxZ(): number;

}
