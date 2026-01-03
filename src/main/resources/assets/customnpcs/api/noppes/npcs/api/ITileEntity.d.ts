/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api
 */

export interface ITileEntity {

    // Methods
    getBlockMetadata(): number;
    getWorld(): import('./IWorld').IWorld;
    setWorld(world: import('./IWorld').IWorld): void;
    getMCTileEntity(): TileEntity;
    markDirty(): void;
    readFromNBT(nbt: import('./INbt').INbt): void;
    getDistanceFrom(x: number, y: number, z: number): number;
    getDistanceFrom(pos: import('./IPos').IPos): number;
    getBlockType(): import('./IBlock').IBlock;
    isInvalid(): boolean;
    invalidate(): void;
    validate(): void;
    updateContainingBlockInfo(): void;
    getNBT(): import('./INbt').INbt;

}
