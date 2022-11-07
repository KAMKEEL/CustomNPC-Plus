package noppes.npcs.ai.pathfinder;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.World;
import noppes.npcs.entity.EntityCustomNpc;
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
    private static final String __OBFID = "CL_00001627";

    protected BlockPos targetPos;
    protected boolean tryUpdatePath;
    protected long lastTimeUpdated;

    protected FlyPathFinder pathFinder;

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

        if (!this.noPath())
        {
            if (this.canNavigate())
            {
                this.pathFollow();
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

    private void pathFollow()
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
        int i1 = k;

        for (int j1 = i - 1; j1 >= this.currentPath.getCurrentPathIndex(); --j1)
        {
            if (this.isDirectPathBetweenPoints(vec3, this.currentPath.getVectorFromIndex(this.theEntity, j1), k, l, i1))
            {
                this.currentPath.setCurrentPathIndex(j1);
                break;
            }
        }

        this.checkForStuck(vec3);
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
    private boolean isDirectPathBetweenPoints(Vec3 p_75493_1_, Vec3 p_75493_2_, int entityXSize, int entityYSize, int entityZSize)
    {
        int l = MathHelper.floor_double(p_75493_1_.xCoord);
        int i1 = MathHelper.floor_double(p_75493_1_.zCoord);
        double d0 = p_75493_2_.xCoord - p_75493_1_.xCoord;
        double d1 = p_75493_2_.zCoord - p_75493_1_.zCoord;
        double d2 = d0 * d0 + d1 * d1;

        if (d2 < 1.0E-8D)
        {
            return false;
        }
        else
        {
            double d3 = 1.0D / Math.sqrt(d2);
            d0 *= d3;
            d1 *= d3;
            entityXSize += 2;
            entityZSize += 2;

            if (!this.isSafeToStandAt(l, (int)p_75493_1_.yCoord, i1, entityXSize, entityYSize, entityZSize, p_75493_1_, d0, d1))
            {
                return false;
            }
            else
            {
                entityXSize -= 2;
                entityZSize -= 2;
                double d4 = 1.0D / Math.abs(d0);
                double d5 = 1.0D / Math.abs(d1);
                double d6 = (double)(l * 1) - p_75493_1_.xCoord;
                double d7 = (double)(i1 * 1) - p_75493_1_.zCoord;

                if (d0 >= 0.0D)
                {
                    ++d6;
                }

                if (d1 >= 0.0D)
                {
                    ++d7;
                }

                d6 /= d0;
                d7 /= d1;
                int j1 = d0 < 0.0D ? -1 : 1;
                int k1 = d1 < 0.0D ? -1 : 1;
                int l1 = MathHelper.floor_double(p_75493_2_.xCoord);
                int i2 = MathHelper.floor_double(p_75493_2_.zCoord);
                int j2 = l1 - l;
                int k2 = i2 - i1;

                do
                {
                    if (j2 * j1 <= 0 && k2 * k1 <= 0)
                    {
                        return true;
                    }

                    if (d6 < d7)
                    {
                        d6 += d4;
                        l += j1;
                        j2 = l1 - l;
                    }
                    else
                    {
                        d7 += d5;
                        i1 += k1;
                        k2 = i2 - i1;
                    }
                }
                while (this.isSafeToStandAt(l, (int)p_75493_1_.yCoord, i1, entityXSize, entityYSize, entityZSize, p_75493_1_, d0, d1));

                return false;
            }
        }
    }

    private boolean isSafeToStandAt(int p_75483_1_, int p_75483_2_, int p_75483_3_, int p_75483_4_, int p_75483_5_, int p_75483_6_, Vec3 p_75483_7_, double p_75483_8_, double p_75483_10_)
    {
        int k1 = p_75483_1_ - p_75483_4_ / 2;
        int l1 = p_75483_3_ - p_75483_6_ / 2;

        if (!this.isPositionClear(k1, p_75483_2_, l1, p_75483_4_, p_75483_5_, p_75483_6_, p_75483_7_, p_75483_8_, p_75483_10_))
        {
            return false;
        }
        else
        {
            for (int i2 = k1; i2 < k1 + p_75483_4_; ++i2)
            {
                for (int j2 = l1; j2 < l1 + p_75483_6_; ++j2)
                {
                    double d2 = (double)i2 + 0.5D - p_75483_7_.xCoord;
                    double d3 = (double)j2 + 0.5D - p_75483_7_.zCoord;

                    if (d2 * p_75483_8_ + d3 * p_75483_10_ >= 0.0D)
                    {
                        Block block = this.worldObj.getBlock(i2, p_75483_2_ - 1, j2);
                        Material material = block.getMaterial();

                        if (material == Material.water && !this.theEntity.isInWater())
                        {
                            return false;
                        }

                        if (material == Material.lava)
                        {
                            return false;
                        }
                    }
                }
            }

            return true;
        }
    }

    private boolean isPositionClear(int x, int y, int z, int entityXSize, int entityYSize, int entityZSize, Vec3 p_75483_7_, double p_75483_8_, double p_75483_10_)
    {
        return pathFinder.getPathNodeType(this.theEntity.worldObj, x, y, z, (EntityLiving) this.theEntity, entityXSize, entityYSize, entityZSize, ((EntityCustomNpc)this.theEntity).ai.doorInteract == 0, ((EntityCustomNpc)this.theEntity).ai.doorInteract == 1).getPriority() == 0;
    }

    /*private boolean isPositionClear(int p_75496_1_, int p_75496_2_, int p_75496_3_, int p_75496_4_, int p_75496_5_, int p_75496_6_, Vec3 p_75496_7_, double p_75496_8_, double p_75496_10_)
    {
        for (int k1 = p_75496_1_; k1 < p_75496_1_ + p_75496_4_; ++k1)
        {
            for (int l1 = p_75496_2_; l1 < p_75496_2_ + p_75496_5_; ++l1)
            {
                for (int i2 = p_75496_3_; i2 < p_75496_3_ + p_75496_6_; ++i2)
                {
                    double d2 = (double)k1 + 0.5D - p_75496_7_.xCoord;
                    double d3 = (double)i2 + 0.5D - p_75496_7_.zCoord;

                    if (d2 * p_75496_8_ + d3 * p_75496_10_ >= 0.0D)
                    {
                        Block block = this.worldObj.getBlock(k1, l1, i2);

                        if (!block.getBlocksMovement(this.worldObj, k1, l1, i2))
                        {
                            return false;
                        }
                    }
                }
            }
        }

        return true;
    }*/

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
