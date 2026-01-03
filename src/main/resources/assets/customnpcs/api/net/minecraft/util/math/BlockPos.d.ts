/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: net.minecraft.util.math
 */

export class BlockPos extends Vec3i {

    // Fields
    LOGGER: Logger;
    ORIGIN: import('./BlockPos').BlockPos;
    NUM_X_BITS: number;
    NUM_Z_BITS: number;
    NUM_Y_BITS: number;
    Y_SHIFT: number;
    X_SHIFT: number;
    X_MASK: number;
    Y_MASK: number;
    Z_MASK: number;
    x: any;
    x: any;
    n: any;
    0: <<;
    i: number;
    j: number;
    k: number;
    blockpos: import('./BlockPos').BlockPos;
    blockpos1: import('./BlockPos').BlockPos;
    lastReturned: import('./BlockPos').BlockPos;
    i: number;
    j: number;
    k: number;
    x: number;
    y: number;
    z: number;
    this: any;
    released: boolean;
    POOL: Array<PooledMutableBlockPos;

    // Methods
    add(x: number, y: number, z: number): import('./BlockPos').BlockPos;
    add(x: number, y: number, z: number): import('./BlockPos').BlockPos;
    add(vec: import('../Vec3i').Vec3i): import('./BlockPos').BlockPos;
    up(): import('./BlockPos').BlockPos;
    up(n: number): import('./BlockPos').BlockPos;
    down(): import('./BlockPos').BlockPos;
    down(n: number): import('./BlockPos').BlockPos;
    north(): import('./BlockPos').BlockPos;
    north(n: number): import('./BlockPos').BlockPos;
    south(): import('./BlockPos').BlockPos;
    south(n: number): import('./BlockPos').BlockPos;
    west(): import('./BlockPos').BlockPos;
    west(n: number): import('./BlockPos').BlockPos;
    east(): import('./BlockPos').BlockPos;
    east(n: number): import('./BlockPos').BlockPos;
    offset(facing: EnumFacing): import('./BlockPos').BlockPos;
    offset(facing: EnumFacing, n: number): import('./BlockPos').BlockPos;
    crossProduct(vec: import('../Vec3i').Vec3i): import('./BlockPos').BlockPos;
    toLong(): number;
    getAllInBox(from: import('./BlockPos').BlockPos, to: import('./BlockPos').BlockPos): Array<BlockPos;
    iterator(): Iterator<BlockPos;
    computeNext(): import('./BlockPos').BlockPos;
    getX(): number;
    getY(): number;
    getZ(): number;
    setPos(xIn: number, yIn: number, zIn: number): MutableBlockPos;
    setPos(p_189532_1_: number, p_189532_3_: number, p_189532_5_: number): MutableBlockPos;
    setPos(p_189533_1_: import('../Vec3i').Vec3i): MutableBlockPos;
    move(p_189536_1_: EnumFacing): MutableBlockPos;
    move(p_189534_1_: EnumFacing, p_189534_2_: number): MutableBlockPos;
    setY(yIn: number): void;
    toImmutable(): import('./BlockPos').BlockPos;
    retain(): PooledMutableBlockPos;
    retain(xIn: number, yIn: number, zIn: number): PooledMutableBlockPos;
    retain(xIn: number, yIn: number, zIn: number): PooledMutableBlockPos;
    release(): void;
    set(xIn: number, yIn: number, zIn: number): PooledMutableBlockPos;
    set(xIn: number, yIn: number, zIn: number): PooledMutableBlockPos;
    set(vec: import('../Vec3i').Vec3i): PooledMutableBlockPos;
    offsetMutable(facing: EnumFacing): PooledMutableBlockPos;
    movePos(p_189538_1_: EnumFacing, p_189538_2_: number): PooledMutableBlockPos;

}
