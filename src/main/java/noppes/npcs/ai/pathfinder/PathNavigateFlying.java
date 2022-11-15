package noppes.npcs.ai.pathfinder;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.init.Blocks;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.World;
import noppes.npcs.entity.EntityNPCInterface;
import javax.annotation.Nullable;

public class PathNavigateFlying extends PathNavigate {

    private EntityNPCInterface theEntity;
    private World worldObj;
    /** The PathEntity being followed. */
    protected NPCPath currentPath;
    private double speed;
    /** The number of blocks (extra) +/- in each axis that get pulled out as cache for the pathfinder's search space */
    private IAttributeInstance pathSearchRange;
    protected float maxDistanceToWaypoint = 0.5F;
    private boolean noSunPathfind;
    /** Time, in number of ticks, following the current path */
    private int totalTicks;
    /** The time when the last position check was done (to detect successful movement) */
    private int ticksAtLastPos;
    /** Coordinates of the entity's position last time a check was done (part of monitoring getting 'stuck') */
    private Vec3 lastPosCheck = Vec3.createVectorHelper(0.0D, 0.0D, 0.0D);
    private Vec3 timeoutCachedNode = Vec3.createVectorHelper(0.0D, 0.0D, 0.0D);
    private long timeoutTimer;
    private long lastTimeoutCheck;
    private double timeoutLimit;
    /** Specifically, if a wooden door block is even considered to be passable by the pathfinder */
    private boolean canPassOpenWoodenDoors = true;
    /** If door blocks are considered passable even when closed */
    private boolean canPassClosedWoodenDoors;
    /** If water blocks are avoided (at least by the pathfinder) */
    private boolean avoidsWater;
    /**
     * If the entity can swim. Swimming AI enables this and the pathfinder will also cause the entity to swim straight
     * upwards when underwater
     */
    private boolean canSwim;

    public BlockPos targetPos;
    protected boolean tryUpdatePath;
    protected long lastTimeUpdated;

    public FlyPathFinder pathFinder;

    public PathNavigateFlying(EntityLiving entityIN, World worldIn) {
        super(entityIN, worldIn);
        this.theEntity = (EntityNPCInterface)entityIN;
        this.worldObj = worldIn;
        this.pathSearchRange = entityIN.getEntityAttribute(SharedMonsterAttributes.followRange);
    }

    public void updatePath()
    {
        if (this.worldObj.getTotalWorldTime() - this.lastTimeUpdated > 20L)
        {
            if (this.targetPos != null)
            {
                this.currentPath = null;
                this.currentPath = this.getPathToXYZ(this.targetPos.getX(), this.targetPos.getY(), this.targetPos.getZ());
                this.lastTimeUpdated = this.worldObj.getTotalWorldTime();
                this.tryUpdatePath = false;
            }
        }
        else
        {
            this.tryUpdatePath = true;
        }
    }

    public void setAvoidsWater(boolean p_75491_1_)
    {
        this.avoidsWater = p_75491_1_;
    }

    public boolean getAvoidsWater()
    {
        return this.avoidsWater;
    }

    public void setBreakDoors(boolean p_75498_1_)
    {
        this.canPassClosedWoodenDoors = p_75498_1_;
    }

    /**
     * Sets if the entity can enter open doors
     */
    public void setEnterDoors(boolean p_75490_1_)
    {
        this.canPassOpenWoodenDoors = p_75490_1_;
    }

    /**
     * Returns true if the entity can break doors, false otherwise
     */
    public boolean getCanBreakDoors()
    {
        return this.canPassClosedWoodenDoors;
    }

    /**
     * Sets if the path should avoid sunlight
     */
    public void setAvoidSun(boolean p_75504_1_)
    {
        this.noSunPathfind = p_75504_1_;
    }

    /**
     * Sets the speed
     */
    public void setSpeed(double p_75489_1_)
    {
        this.speed = p_75489_1_;
    }

    /**
     * Sets if the entity can swim
     */
    public void setCanSwim(boolean p_75495_1_)
    {
        this.canSwim = p_75495_1_;
    }

    /**
     * Gets the maximum distance that the path finding will search in.
     */
    public float getPathSearchRange()
    {
        return (float)this.pathSearchRange.getAttributeValue();
    }

    /**
     * Returns the path to the given coordinates
     */
    public NPCPath getPathToXYZ(double x, double y, double z)
    {
        BlockPos pos = new BlockPos(x, y, z);
        if (!this.canNavigate())
        {
            return null;
        }
        else if (this.currentPath != null && !this.currentPath.isFinished() && pos.equals(this.targetPos))
        {
            return this.currentPath;
        }
        else {
            this.targetPos = pos;
            return this.getEntityPathToXYZ(this.theEntity, MathHelper.floor_double(x), (int)y, MathHelper.floor_double(z), this.getPathSearchRange(), this.canPassOpenWoodenDoors, this.canPassClosedWoodenDoors, this.avoidsWater, this.canSwim);
        }
    }

    /**
     * Try to find and set a path to XYZ. Returns true if successful.
     */
    public boolean tryMoveToXYZ(double p_75492_1_, double p_75492_3_, double p_75492_5_, double p_75492_7_)
    {
        NPCPath pathentity = this.getPathToXYZ((double)MathHelper.floor_double(p_75492_1_), (double)((int)p_75492_3_), (double)MathHelper.floor_double(p_75492_5_));
        return this.setPath(pathentity, p_75492_7_);
    }

    /**
     * Returns the path to the given EntityLiving
     */
    public NPCPath getPathToEntityLiving(Entity entity)
    {
        if (!this.canNavigate())
        {
            return null;
        }
        else
        {
            BlockPos blockpos = new BlockPos(entity);
            if (this.currentPath != null && !this.currentPath.isFinished() && blockpos.equals(this.targetPos))
            {
                return this.currentPath;
            }
            else
            {
                this.targetPos = blockpos;
                return this.getPathEntityToEntity(this.theEntity, entity, this.getPathSearchRange(), this.canPassOpenWoodenDoors, this.canPassClosedWoodenDoors, this.avoidsWater, this.canSwim);
            }
        }
    }

    /**
     * Try to find and set a path to EntityLiving. Returns true if successful.
     */
    public boolean tryMoveToEntityLiving(Entity p_75497_1_, double p_75497_2_)
    {
        NPCPath pathentity = this.getPathToEntityLiving(p_75497_1_);
        return pathentity != null && this.setPath(pathentity, p_75497_2_);
    }

    /**
     * sets the active path data if path is 100% unique compared to old path, checks to adjust path for sun avoiding
     * ents and stores end coords
     */
    public boolean setPath(@Nullable PathEntity pathEntity, double speed)
    {
        NPCPath NPCPath = (NPCPath) pathEntity;
        if (NPCPath == null)
        {
            this.currentPath = null;
            return false;
        }
        else
        {
            if (!NPCPath.isSamePath(this.currentPath))
            {
                this.currentPath = NPCPath;
            }

            if (this.noSunPathfind)
            {
                this.removeSunnyPath();
            }

            if (this.currentPath.getCurrentPathLength() == 0)
            {
                return false;
            }
            else
            {
                this.speed = speed;
                Vec3 vec3 = this.getEntityPosition();
                this.ticksAtLastPos = this.totalTicks;
                this.lastPosCheck = vec3;
                return true;
            }
        }
    }

    /**
     * gets the actively used PathEntity
     */
    public PathEntity getPath()
    {
        return this.currentPath;
    }

    private boolean isInLiquid()
    {
        return this.theEntity.isInWater() || this.theEntity.handleLavaMovement();
    }

    private boolean canNavigate()
    {
        return !this.isInLiquid() || (this.canSwim && this.isInLiquid());
    }

    public void onUpdateNavigation()
    {

        ++this.totalTicks;

        if (this.tryUpdatePath)
        {
            this.updatePath();
        }

        if (!this.noPath())
        {
            if (this.canNavigate())
            {
                if (((EntityNPCInterface)this.theEntity).display.modelSize >= 5)
                    this.pathFollowBig();
                else
                    this.pathFollowSmall();
            }
            else if (this.currentPath != null && this.currentPath.getCurrentPathIndex() < this.currentPath.getCurrentPathLength())
            {
                if (!this.noPath())
                {
                    Vec3 vec3d = this.currentPath.getVectorFromIndex(this.theEntity, this.currentPath.getCurrentPathIndex());
                    Vec3 vec3 = this.currentPath.getPosition(this.theEntity);

                    if (MathHelper.floor_double(this.theEntity.posX) == MathHelper.floor_double(vec3d.xCoord) && MathHelper.floor_double(this.theEntity.posY) == MathHelper.floor_double(vec3d.yCoord) && MathHelper.floor_double(this.theEntity.posZ) == MathHelper.floor_double(vec3d.zCoord))
                        if (vec3 != null)
                        {
                            this.currentPath.setCurrentPathIndex(this.currentPath.getCurrentPathIndex() + 1);
                            this.theEntity.getMoveHelper().setMoveTo(vec3.xCoord, vec3.yCoord, vec3.zCoord, this.speed);
                        }
                }
            }
            if (!this.noPath())
            {
                Vec3 vec3 = this.currentPath.getPosition(this.theEntity);
                if (vec3 != null)
                {
                    this.theEntity.getMoveHelper().setMoveTo(vec3.xCoord, vec3.yCoord, vec3.zCoord, this.speed);
                }
            }
        }
    }

    private void pathFollowBig()
    {
        Vec3 vec3 = this.getEntityPosition();
        int i = this.currentPath.getCurrentPathLength();

        float f = this.theEntity.width * this.theEntity.width;
        int k;

        for (k = this.currentPath.getCurrentPathIndex(); k < i; ++k)
        {
            if (vec3.squareDistanceTo(this.currentPath.getVectorFromIndex(this.theEntity, k)) < (double)f)
            {
                this.currentPath.setCurrentPathIndex(k + 1);
            }
        }

        k = MathHelper.floor_double(this.theEntity.width + 1.0F);
        int l = MathHelper.floor_double(this.theEntity.height + 1.0F);
        this.trimPath(vec3,i,k,l);

        this.checkForStuck(vec3);
    }

    private void pathFollowSmall()
    {
        Vec3 vec3 = this.getEntityPosition();
        int i = this.currentPath.getCurrentPathLength();
        for (int j = this.currentPath.getCurrentPathIndex(); j < this.currentPath.getCurrentPathLength(); ++j)
        {
            if ((double)this.currentPath.getPathPointFromIndex(j).yCoord != Math.floor(vec3.yCoord))
            {
                i = j;
                break;
            }
        }

        this.maxDistanceToWaypoint = this.theEntity.width > 0.75F ? this.theEntity.width / 2.0F : 0.75F - this.theEntity.width / 2.0F;
        Vec3 vec3d1 = this.currentPath.getCurrentPos();

        if (MathHelper.abs((float)(this.theEntity.posX - (vec3d1.xCoord + 0.5D))) < this.maxDistanceToWaypoint && MathHelper.abs((float)(this.theEntity.posZ - (vec3d1.zCoord + 0.5D))) < this.maxDistanceToWaypoint && Math.abs(this.theEntity.posY - vec3d1.yCoord) < 1.0D)
        {
            this.currentPath.setCurrentPathIndex(this.currentPath.getCurrentPathIndex() + 1);
        }

        int k = MathHelper.ceiling_double_int(this.theEntity.width);
        int l = MathHelper.ceiling_double_int(this.theEntity.height + 1.0F);
        this.trimPath(vec3,i,k,l);

        this.checkForStuck(vec3);
    }

    public void trimPath(Vec3 position, int pathLength, int width, int height) {
        for (int j1 = pathLength - 1; j1 >= this.currentPath.getCurrentPathIndex(); --j1)
        {
            Vec3 pathPoint = this.currentPath.getVectorFromIndex(this.theEntity, j1);
            if (position.distanceTo(pathPoint) > 6)
                continue;

            if (this.isDirectPathBetweenPoints(position, pathPoint, width, height))
            {
                this.currentPath.setCurrentPathIndex(j1);
                break;
            }
        }
    }


    /**
     * If null path or reached the end
     */
    public boolean noPath()
    {
        return this.currentPath == null || this.currentPath.isFinished();
    }

    /**
     * sets active PathEntity to null
     */
    @Override
    public void clearPathEntity()
    {
        this.currentPath = null;
    }

    private Vec3 getEntityPosition()
    {
        return Vec3.createVectorHelper(this.theEntity.posX, this.getPathableYPos(), this.theEntity.posZ);
    }

    /**
     * Gets the safe pathing Y position for the entity depending on if it can path swim or not
     */
    private double getPathableYPos()
    {
        return this.theEntity.boundingBox.minY + 0.05D;
    }

    /**
     * Trims path data from the end to the first sun covered block
     */
    private void removeSunnyPath()
    {
        if (!this.worldObj.canBlockSeeTheSky(MathHelper.floor_double(this.theEntity.posX), (int)(this.theEntity.boundingBox.minY + 0.5D), MathHelper.floor_double(this.theEntity.posZ)))
        {
            for (int i = 0; i < this.currentPath.getCurrentPathLength(); ++i)
            {
                PathPoint pathpoint = this.currentPath.getPathPointFromIndex(i);

                if (this.worldObj.canBlockSeeTheSky(pathpoint.xCoord, pathpoint.yCoord, pathpoint.zCoord))
                {
                    this.currentPath.setCurrentPathLength(i - 1);
                    return;
                }
            }
        }
    }

    /**
     * Returns true when an entity of specified size could safely walk in a straight line between the two points. Args:
     * pos1, pos2, entityXSize, entityYSize, entityZSize
     */
    private boolean isDirectPathBetweenPoints(Vec3 startPos, Vec3 endPos, int width, int height)
    {
        Vec3 pos17 = Vec3.createVectorHelper(startPos.xCoord,startPos.yCoord, startPos.zCoord);
        Vec3 pos18 = Vec3.createVectorHelper(endPos.xCoord,endPos.yCoord, endPos.zCoord);
        if (this.rayTraceBlocks(pos17, pos18, true, false, false, width, height))
            return false;

        return true;
    }

    public boolean rayTraceBlocks(Vec3 p_147447_1_, Vec3 p_147447_2_, boolean p_147447_3_, boolean p_147447_4_, boolean p_147447_5_, int width, int height)
    {
        if (!Double.isNaN(p_147447_1_.xCoord) && !Double.isNaN(p_147447_1_.yCoord) && !Double.isNaN(p_147447_1_.zCoord))
        {
            if (!Double.isNaN(p_147447_2_.xCoord) && !Double.isNaN(p_147447_2_.yCoord) && !Double.isNaN(p_147447_2_.zCoord))
            {
                int i = MathHelper.floor_double(p_147447_2_.xCoord);
                int j = MathHelper.floor_double(p_147447_2_.yCoord);
                int k = MathHelper.floor_double(p_147447_2_.zCoord);
                int l = MathHelper.floor_double(p_147447_1_.xCoord);
                int i1 = MathHelper.floor_double(p_147447_1_.yCoord);
                int j1 = MathHelper.floor_double(p_147447_1_.zCoord);
                Block block = this.worldObj.getBlock(l, i1, j1);
                int k1 = this.worldObj.getBlockMetadata(l, i1, j1);

                if ((!p_147447_4_ || block.getCollisionBoundingBoxFromPool(this.worldObj, l, i1, j1) != null) && block.canCollideCheck(k1, p_147447_3_))
                {
                    MovingObjectPosition movingobjectposition = block.collisionRayTrace(this.worldObj, l, i1, j1, p_147447_1_, p_147447_2_);

                    if (movingobjectposition != null && movingobjectposition.typeOfHit != MovingObjectPosition.MovingObjectType.MISS)
                    {
                        return true;
                    }
                }

                MovingObjectPosition movingobjectposition2 = null;
                k1 = 200;

                while (k1-- >= 0)
                {
                    if (Double.isNaN(p_147447_1_.xCoord) || Double.isNaN(p_147447_1_.yCoord) || Double.isNaN(p_147447_1_.zCoord))
                    {
                        return false;
                    }

                    if (l == i && i1 == j && j1 == k)
                    {
                        if (p_147447_5_) {
                            return movingobjectposition2 != null && movingobjectposition2.typeOfHit != MovingObjectPosition.MovingObjectType.MISS;
                        }
                        return false;
                    }

                    boolean flag6 = true;
                    boolean flag3 = true;
                    boolean flag4 = true;
                    double d0 = 999.0D;
                    double d1 = 999.0D;
                    double d2 = 999.0D;

                    if (i > l)
                    {
                        d0 = (double)l + 1.0D;
                    }
                    else if (i < l)
                    {
                        d0 = (double)l + 0.0D;
                    }
                    else
                    {
                        flag6 = false;
                    }

                    if (j > i1)
                    {
                        d1 = (double)i1 + 1.0D;
                    }
                    else if (j < i1)
                    {
                        d1 = (double)i1 + 0.0D;
                    }
                    else
                    {
                        flag3 = false;
                    }

                    if (k > j1)
                    {
                        d2 = (double)j1 + 1.0D;
                    }
                    else if (k < j1)
                    {
                        d2 = (double)j1 + 0.0D;
                    }
                    else
                    {
                        flag4 = false;
                    }

                    double d3 = 999.0D;
                    double d4 = 999.0D;
                    double d5 = 999.0D;
                    double d6 = p_147447_2_.xCoord - p_147447_1_.xCoord;
                    double d7 = p_147447_2_.yCoord - p_147447_1_.yCoord;
                    double d8 = p_147447_2_.zCoord - p_147447_1_.zCoord;

                    if (flag6)
                    {
                        d3 = (d0 - p_147447_1_.xCoord) / d6;
                    }

                    if (flag3)
                    {
                        d4 = (d1 - p_147447_1_.yCoord) / d7;
                    }

                    if (flag4)
                    {
                        d5 = (d2 - p_147447_1_.zCoord) / d8;
                    }

                    boolean flag5 = false;
                    byte b0;

                    if (d3 < d4 && d3 < d5)
                    {
                        if (i > l)
                        {
                            b0 = 4;
                        }
                        else
                        {
                            b0 = 5;
                        }

                        p_147447_1_.xCoord = d0;
                        p_147447_1_.yCoord += d7 * d3;
                        p_147447_1_.zCoord += d8 * d3;
                    }
                    else if (d4 < d5)
                    {
                        if (j > i1)
                        {
                            b0 = 0;
                        }
                        else
                        {
                            b0 = 1;
                        }

                        p_147447_1_.xCoord += d6 * d4;
                        p_147447_1_.yCoord = d1;
                        p_147447_1_.zCoord += d8 * d4;
                    }
                    else
                    {
                        if (k > j1)
                        {
                            b0 = 2;
                        }
                        else
                        {
                            b0 = 3;
                        }

                        p_147447_1_.xCoord += d6 * d5;
                        p_147447_1_.yCoord += d7 * d5;
                        p_147447_1_.zCoord = d2;
                    }

                    Vec3 vec32 = Vec3.createVectorHelper(p_147447_1_.xCoord, p_147447_1_.yCoord, p_147447_1_.zCoord);
                    l = (int)(vec32.xCoord = (double)MathHelper.floor_double(p_147447_1_.xCoord));

                    if (b0 == 5)
                    {
                        --l;
                        ++vec32.xCoord;
                    }

                    i1 = (int)(vec32.yCoord = (double)MathHelper.floor_double(p_147447_1_.yCoord));

                    if (b0 == 1)
                    {
                        --i1;
                        ++vec32.yCoord;
                    }

                    j1 = (int)(vec32.zCoord = (double)MathHelper.floor_double(p_147447_1_.zCoord));

                    if (b0 == 3)
                    {
                        --j1;
                        ++vec32.zCoord;
                    }

                    boolean value = false;
                    for (int x = -width; x <= width; x++) {
                        if (value) {
                            break;
                        }
                        for (int y = 0; y <= height; y++) {
                            if (value) {
                                break;
                            }
                            for (int z = -width; z <= width; z++) {
                                if (Math.abs(x) == Math.abs(z)) {
                                    continue;
                                }

                                l += x;
                                i1 += y;
                                j1 += z;

                                Block block2 = this.worldObj.getBlock(l, i1, j1);
                                int meta2 = this.worldObj.getBlockMetadata(l, i1, j1);

                                boolean collideCheck = (!p_147447_4_ || block2.getCollisionBoundingBoxFromPool(this.worldObj, l, i1, j1) != null) && block2.canCollideCheck(meta2, p_147447_3_);
                                boolean noCollisionBlock = block2 == Blocks.air || block2.getMaterial() == Material.lava && this.theEntity.isImmuneToFire() ||
                                        block2.getMaterial() == Material.water && this.theEntity.stats.drowningType == 2;

                                if (collideCheck && !noCollisionBlock) {
                                    if (!block2.getBlocksMovement(this.worldObj, l, i1, j1)) {
                                        value = true;
                                        l -= x;
                                        i1 -= y;
                                        j1 -= z;
                                        break;
                                    }
                                    MovingObjectPosition movingobjectposition = block2.collisionRayTrace(this.worldObj, l, i1, j1, p_147447_1_, p_147447_2_);
                                    if (movingobjectposition != null && movingobjectposition.typeOfHit != MovingObjectPosition.MovingObjectType.MISS) {
                                        value = true;
                                        l -= x;
                                        i1 -= y;
                                        j1 -= z;
                                        break;
                                    } else {
                                        movingobjectposition2 = new MovingObjectPosition(l, i1, j1, b0, p_147447_1_, false);
                                    }
                                }

                                l -= x;
                                i1 -= y;
                                j1 -= z;
                            }
                        }
                    }

                    if (value) {
                        return true;
                    }
                }

                if (p_147447_5_) {
                    return movingobjectposition2 != null && movingobjectposition2.typeOfHit != MovingObjectPosition.MovingObjectType.MISS;
                }
                return false;
            }
            else
            {
                return false;
            }
        }
        else
        {
            return false;
        }
    }

    protected void checkForStuck(Vec3 positionVec3)
    {
        if (this.totalTicks - this.ticksAtLastPos > 100)
        {
            if (positionVec3.squareDistanceTo(this.lastPosCheck) < 2.25D)
            {
                this.clearPathEntity();
            }

            this.ticksAtLastPos = this.totalTicks;
            this.lastPosCheck = positionVec3;
        }

        if (this.currentPath != null && !this.currentPath.isFinished())
        {
            Vec3 vec3d = this.currentPath.getCurrentPos();

            if (vec3d.equals(this.timeoutCachedNode))
            {
                this.timeoutTimer += System.currentTimeMillis() - this.lastTimeoutCheck;
            }
            else
            {
                this.timeoutCachedNode = vec3d;
                double d0 = positionVec3.distanceTo(this.timeoutCachedNode);
                this.timeoutLimit = this.theEntity.getAIMoveSpeed() > 0.0F ? d0 / (double)this.theEntity.getAIMoveSpeed() * 1000.0D : 0.0D;
            }

            if (this.timeoutLimit > 0.0D && (double)this.timeoutTimer > this.timeoutLimit * 3.0D)
            {
                this.timeoutCachedNode = Vec3.createVectorHelper(0.0D, 0.0D, 0.0D);;
                this.timeoutTimer = 0L;
                this.timeoutLimit = 0.0D;
                this.clearPathEntity();
            }

            this.lastTimeoutCheck = System.currentTimeMillis();
        }
    }

    public ChunkCache createChunkCache(World world, BlockPos blockFrom, BlockPos blockTo){
        return new ChunkCache(world, blockFrom.getX(), blockFrom.getY(), blockFrom.getZ(), blockTo.getX(), blockTo.getY(), blockTo.getZ(), 0);
    }

    public NPCPath getPathEntityToEntity(Entity entityFrom, Entity entityTo, float distance, boolean openDoors, boolean breakDoors, boolean pathWater, boolean canSwim)
    {
        float f = this.getPathSearchRange();
        this.theEntity.worldObj.theProfiler.startSection("pathfind");
        BlockPos blockpos1 = (new BlockPos(this.theEntity)).up();
        int i = (int)(f + 16.0F);
        ChunkCache chunkcache = createChunkCache(this.worldObj, blockpos1.add(-i, -i, -i), blockpos1.add(i, i, i));
        this.pathFinder = new FlyPathFinder(chunkcache, openDoors, breakDoors, pathWater, canSwim, this.theEntity);
        NPCPath pathentity = this.pathFinder.createEntityPathTo(entityFrom, entityTo, distance);
        this.theEntity.worldObj.theProfiler.endSection();
        return pathentity;
    }

    public NPCPath getEntityPathToXYZ(Entity entityFrom, int x, int y, int z, float distance, boolean openDoors, boolean breakDoors, boolean pathWater, boolean canSwim)
    {
        float f = this.getPathSearchRange();
        this.theEntity.worldObj.theProfiler.startSection("pathfind");
        BlockPos blockpos1 = (new BlockPos(this.theEntity)).up();
        int i = (int)(f + 16.0F);
        ChunkCache chunkcache = createChunkCache(this.worldObj, blockpos1.add(-i, -i, -i), blockpos1.add(i, i, i));
        this.pathFinder = new FlyPathFinder(chunkcache, openDoors, breakDoors, pathWater, canSwim, this.theEntity);
        NPCPath pathentity = this.pathFinder.createEntityPathTo(entityFrom, this.targetPos.getX(), this.targetPos.getY(), this.targetPos.getZ(), f);
        this.theEntity.worldObj.theProfiler.endSection();
        return pathentity;
    }
}
