/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: net.minecraft.util.math
 */

/**
 * @javaFqn net.minecraft.util.math.BlockPos
 */
export class BlockPos extends import('../Vec3i').Vec3i {
    /**
     * The BlockPos with all coordinates 0
     */
    public static final BlockPos ORIGIN = new BlockPos(0, 0, 0);
    private static final int NUM_X_BITS = 26; //1 + MathHelper.calculateLogBaseTwo(MathHelper.roundUpToPowerOfTwo(30000000)); //Manually calculated because of crash
    private static final int NUM_Z_BITS = NUM_X_BITS;
    private static final int NUM_Y_BITS = 64 - NUM_X_BITS - NUM_Z_BITS;
    private static final int Y_SHIFT = 0 + NUM_Z_BITS;
    private static final int X_SHIFT = Y_SHIFT + NUM_Y_BITS;
    private static final long X_MASK = (1L << NUM_X_BITS) - 1L;
    private static final long Y_MASK = (1L << NUM_Y_BITS) - 1L;
    private static final long Z_MASK = (1L << NUM_Z_BITS) - 1L;
    
    public BlockPos(int x, int y, int z) {
    }
    
    public BlockPos(double x, double y, double z) {
    }
    
    public BlockPos(Entity source) {
    }
    
    public BlockPos(Vec3i source) {
    this(source.getXD(), source.getYD(), source.getZD());
    }
    
    /**
     * Add the given coordinates to the coordinates of this BlockPos
     *
     * @param x X offset
     * @param y Y offset
     * @param z Z offset
     * @return a new BlockPos offset by the given values
     */
    add(x: import('./double').double, y: import('./double').double, z: import('./double').double): import('./BlockPos').BlockPos;
    /**
     * Add the given coordinates to the coordinates of this BlockPos
     *
     * @param x X offset
     * @param y Y offset
     * @param z Z offset
     * @return a new BlockPos offset by the given values
     */
    add(x: import('./int').int, y: import('./int').int, z: import('./int').int): import('./BlockPos').BlockPos;
    /**
     * Add the given Vector to this BlockPos
     *
     * @param vec the vector to add
     * @return a new BlockPos offset by the vector
     */
    add(vec: import('../Vec3i').Vec3i): import('./BlockPos').BlockPos;
    /**
     * Offset this BlockPos 1 block up
     *
     * @return the BlockPos one block above
     */
    up(): import('./BlockPos').BlockPos;
    /**
     * Offset this BlockPos n blocks up
     *
     * @param n the distance upward
     * @return the BlockPos n blocks above
     */
    up(n: import('./double').double): import('./BlockPos').BlockPos;
    /**
     * Offset this BlockPos 1 block down
     *
     * @return the BlockPos one block below
     */
    down(): import('./BlockPos').BlockPos;
    /**
     * Offset this BlockPos n blocks down
     *
     * @param n the distance downward
     * @return the BlockPos n blocks below
     */
    down(n: import('./double').double): import('./BlockPos').BlockPos;
    /**
     * Offset this BlockPos 1 block in northern direction
     *
     * @return the BlockPos one block to the north
     */
    north(): import('./BlockPos').BlockPos;
    /**
     * Offset this BlockPos n blocks in northern direction
     *
     * @param n the distance northward
     * @return the BlockPos n blocks to the north
     */
    north(n: import('./double').double): import('./BlockPos').BlockPos;
    /**
     * Offset this BlockPos 1 block in southern direction
     *
     * @return the BlockPos one block to the south
     */
    south(): import('./BlockPos').BlockPos;
    /**
     * Offset this BlockPos n blocks in southern direction
     *
     * @param n the distance southward
     * @return the BlockPos n blocks to the south
     */
    south(n: import('./double').double): import('./BlockPos').BlockPos;
    /**
     * Offset this BlockPos 1 block in western direction
     *
     * @return the BlockPos one block to the west
     */
    west(): import('./BlockPos').BlockPos;
    /**
     * Offset this BlockPos n blocks in western direction
     *
     * @param n the distance westward
     * @return the BlockPos n blocks to the west
     */
    west(n: import('./double').double): import('./BlockPos').BlockPos;
    /**
     * Offset this BlockPos 1 block in eastern direction
     *
     * @return the BlockPos one block to the east
     */
    east(): import('./BlockPos').BlockPos;
    /**
     * Offset this BlockPos n blocks in eastern direction
     *
     * @param n the distance eastward
     * @return the BlockPos n blocks to the east
     */
    east(n: import('./double').double): import('./BlockPos').BlockPos;
    /**
     * Offset this BlockPos 1 block in the given direction
     *
     * @param facing the direction to offset
     * @return the BlockPos one block in the given direction
     */
    offset(facing: import('../EnumFacing').EnumFacing): import('./BlockPos').BlockPos;
    /**
     * Offsets this BlockPos n blocks in the given direction
     *
     * @param facing the direction to offset
     * @param n the distance
     * @return the BlockPos n blocks in the given direction
     */
    offset(facing: import('../EnumFacing').EnumFacing, n: import('./double').double): import('./BlockPos').BlockPos;
    /**
     * Calculate the cross product of this and the given Vector
     */
    crossProduct(vec: import('../Vec3i').Vec3i): import('./BlockPos').BlockPos;
    /**
     * Serialize this BlockPos into a long value
     *
     * @return this position serialized as a long
     */
    toLong(): import('./long').long;
    /**
     * Create a BlockPos from a serialized long value (created by toLong)
     *
     * @param serialized the serialized position
     * @return a BlockPos decoded from the long
     */
    fromLong(serialized: import('./long').long): import('./BlockPos').BlockPos;
    BlockPos(): import('./return new').return new;
    /**
     * Create an Iterable that returns all positions in the box specified by the given corners
     *
     * @param from the starting corner
     * @param to the ending corner
     * @return an iterable of all BlockPos within the bounding box
     */
    getAllInBox(from: import('./BlockPos').BlockPos, to: import('./BlockPos').BlockPos): import('./Iterable').Iterable;
    iterator(): Java.java.util.Iterator<import('./BlockPos').BlockPos>;
    computeNext(): import('./BlockPos').BlockPos;
    LOGGER: Logger;
    /**
     * The BlockPos with all coordinates 0
     */
    ORIGIN: import('./BlockPos').BlockPos;
    NUM_X_BITS: import('./int').int;
    NUM_Z_BITS: import('./int').int;
    NUM_Y_BITS: import('./int').int;
    Y_SHIFT: import('./int').int;
    X_SHIFT: import('./int').int;
    X_MASK: import('./long').long;
    Y_MASK: import('./long').long;
    Z_MASK: import('./long').long;
    lastReturned: import('./BlockPos').BlockPos;
}

export namespace BlockPos {
    /**
     * @javaFqn net.minecraft.util.math.BlockPos.MutableBlockPos
     */
    export class MutableBlockPos extends BlockPos {
        /**
         * Mutable X Coordinate
         */
        protected int x;
        /**
         * Mutable Y Coordinate
         */
        protected int y;
        /**
         * Mutable Z Coordinate
         */
        protected int z;
        
        public MutableBlockPos() {
        }
        
        public MutableBlockPos(BlockPos pos) {
        this(pos.getX(), pos.getY(), pos.getZ());
        }
        
        public MutableBlockPos(int x_, int y_, int z_) {
        this.x = x_;
        this.y = y_;
        this.z = z_;
        }
        
        /**
         * Gets the X coordinate.
         */
        getX(): import('./int').int;
        /**
         * Gets the Y coordinate.
         */
        getY(): import('./int').int;
        /**
         * Gets the Z coordinate.
         */
        getZ(): import('./int').int;
        /**
         * Sets the position, MUST not be name 'set' as that causes obfusication conflicts with func_185343_d
         *
         * @param x the X coordinate
         * @param y the Y coordinate
         * @param z the Z coordinate
         * @return this mutable position
         */
        setPos(x: import('./int').int, y: import('./int').int, z: import('./int').int): BlockPos.MutableBlockPos;
        setPos(p_189532_1_: import('./double').double, p_189532_3_: import('./double').double, p_189532_5_: import('./double').double): BlockPos.MutableBlockPos;
        setPos(p_189533_1_: import('../Vec3i').Vec3i): BlockPos.MutableBlockPos;
        move(p_189536_1_: import('../EnumFacing').EnumFacing): BlockPos.MutableBlockPos;
        move(p_189534_1_: import('../EnumFacing').EnumFacing, p_189534_2_: import('./int').int): BlockPos.MutableBlockPos;
        setY(yIn: import('./int').int): import('./void').void;
        /**
         * Returns a version of this BlockPos that is guaranteed to be immutable.
         *
         * <p>When storing a BlockPos given to you for an extended period of time, make sure you
         * use this in case the value is changed internally.</p>
         *
         * @return an immutable copy of this position
         */
        toImmutable(): import('./BlockPos').BlockPos;
        BlockPos(): import('./return new').return new;
    }
    /**
     * @javaFqn net.minecraft.util.math.BlockPos.PooledMutableBlockPos
     */
    export class PooledMutableBlockPos extends BlockPos.MutableBlockPos {
        retain(): BlockPos.PooledMutableBlockPos;
        retain(): import('./return').return;
        retain(xIn: import('./double').double, yIn: import('./double').double, zIn: import('./double').double): BlockPos.PooledMutableBlockPos;
        retain(xIn: import('./int').int, yIn: import('./int').int, zIn: import('./int').int): BlockPos.PooledMutableBlockPos;
        release(): import('./void').void;
        set(xIn: import('./int').int, yIn: import('./int').int, zIn: import('./int').int): BlockPos.PooledMutableBlockPos;
        set(xIn: import('./double').double, yIn: import('./double').double, zIn: import('./double').double): BlockPos.PooledMutableBlockPos;
        set(vec: import('../Vec3i').Vec3i): BlockPos.PooledMutableBlockPos;
        offsetMutable(facing: import('../EnumFacing').EnumFacing): BlockPos.PooledMutableBlockPos;
        movePos(p_189538_1_: import('../EnumFacing').EnumFacing, p_189538_2_: import('./int').int): BlockPos.PooledMutableBlockPos;
    }
}

/**
 * @javaFqn net.minecraft.util.math.MutableBlockPos
 */
export class MutableBlockPos extends import('./BlockPos').BlockPos {
    /**
     * Mutable X Coordinate
     */
    protected int x;
    /**
     * Mutable Y Coordinate
     */
    protected int y;
    /**
     * Mutable Z Coordinate
     */
    protected int z;
    
    public MutableBlockPos() {
    }
    
    public MutableBlockPos(BlockPos pos) {
    this(pos.getX(), pos.getY(), pos.getZ());
    }
    
    public MutableBlockPos(int x_, int y_, int z_) {
    this.x = x_;
    this.y = y_;
    this.z = z_;
    }
    
    /**
     * Gets the X coordinate.
     */
    getX(): import('./int').int;
    /**
     * Gets the Y coordinate.
     */
    getY(): import('./int').int;
    /**
     * Gets the Z coordinate.
     */
    getZ(): import('./int').int;
    /**
     * Sets the position, MUST not be name 'set' as that causes obfusication conflicts with func_185343_d
     *
     * @param x the X coordinate
     * @param y the Y coordinate
     * @param z the Z coordinate
     * @return this mutable position
     */
    setPos(x: import('./int').int, y: import('./int').int, z: import('./int').int): BlockPos.MutableBlockPos;
    setPos(p_189532_1_: import('./double').double, p_189532_3_: import('./double').double, p_189532_5_: import('./double').double): BlockPos.MutableBlockPos;
    setPos(p_189533_1_: import('../Vec3i').Vec3i): BlockPos.MutableBlockPos;
    move(p_189536_1_: import('../EnumFacing').EnumFacing): BlockPos.MutableBlockPos;
    move(p_189534_1_: import('../EnumFacing').EnumFacing, p_189534_2_: import('./int').int): BlockPos.MutableBlockPos;
    setY(yIn: import('./int').int): import('./void').void;
    /**
     * Returns a version of this BlockPos that is guaranteed to be immutable.
     *
     * <p>When storing a BlockPos given to you for an extended period of time, make sure you
     * use this in case the value is changed internally.</p>
     *
     * @return an immutable copy of this position
     */
    toImmutable(): import('./BlockPos').BlockPos;
    BlockPos(): import('./return new').return new;
    /**
     * Mutable X Coordinate
     */
    x: import('./int').int;
    /**
     * Mutable Y Coordinate
     */
    y: import('./int').int;
    /**
     * Mutable Z Coordinate
     */
    z: import('./int').int;
}

/**
 * @javaFqn net.minecraft.util.math.PooledMutableBlockPos
 */
export class PooledMutableBlockPos extends BlockPos.MutableBlockPos {
    retain(): BlockPos.PooledMutableBlockPos;
    retain(): import('./return').return;
    retain(xIn: import('./double').double, yIn: import('./double').double, zIn: import('./double').double): BlockPos.PooledMutableBlockPos;
    retain(xIn: import('./int').int, yIn: import('./int').int, zIn: import('./int').int): BlockPos.PooledMutableBlockPos;
    release(): import('./void').void;
    set(xIn: import('./int').int, yIn: import('./int').int, zIn: import('./int').int): BlockPos.PooledMutableBlockPos;
    set(xIn: import('./double').double, yIn: import('./double').double, zIn: import('./double').double): BlockPos.PooledMutableBlockPos;
    set(vec: import('../Vec3i').Vec3i): BlockPos.PooledMutableBlockPos;
    offsetMutable(facing: import('../EnumFacing').EnumFacing): BlockPos.PooledMutableBlockPos;
    movePos(p_189538_1_: import('../EnumFacing').EnumFacing, p_189538_2_: import('./int').int): BlockPos.PooledMutableBlockPos;
    released: import('./boolean').boolean;
    POOL: BlockPos.PooledMutableBlockPos[];
}
