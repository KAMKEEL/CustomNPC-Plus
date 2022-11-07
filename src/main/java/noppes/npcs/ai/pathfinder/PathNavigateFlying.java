package noppes.npcs.ai.pathfinder;

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

        if (this.tryUpdatePath)
        {
            this.updatePath();
        }

        if (!this.noPath())
        {
            if (this.canNavigate())
            {
                this.pathFollow();
            }
            else if (this.currentPath != null && this.currentPath.getCurrentPathIndex() < this.currentPath.getCurrentPathLength())
            {
                Vec3 vec3d = this.currentPath.getVectorFromIndex(this.theEntity, this.currentPath.getCurrentPathIndex());

                if (MathHelper.floor_double(this.theEntity.posX) == MathHelper.floor_double(vec3d.xCoord) && MathHelper.floor_double(this.theEntity.posY) == MathHelper.floor_double(vec3d.yCoord) && MathHelper.floor_double(this.theEntity.posZ) == MathHelper.floor_double(vec3d.zCoord))
                {
                    this.currentPath.setCurrentPathIndex(this.currentPath.getCurrentPathIndex() + 1);
                }
            }

            if (!this.noPath())
            {
                Vec3 vec3d1 = this.currentPath.getPosition(this.theEntity);
                this.theEntity.getMoveHelper().setMoveTo(vec3d1.xCoord, vec3d1.yCoord, vec3d1.zCoord, this.speed);
            }
        }
    }

    private void pathFollow()
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
        int l = MathHelper.ceiling_double_int(this.theEntity.height);
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
    private boolean isDirectPathBetweenPoints(Vec3 posVec31, Vec3 posVec32, int sizeX, int sizeY, int sizeZ)
    {
        int i = MathHelper.floor_double(posVec31.xCoord);
        int j = MathHelper.floor_double(posVec31.yCoord);
        int k = MathHelper.floor_double(posVec31.zCoord);
        double d0 = posVec32.xCoord - posVec31.xCoord;
        double d1 = posVec32.yCoord - posVec31.yCoord;
        double d2 = posVec32.zCoord - posVec31.zCoord;
        double d3 = d0 * d0 + d1 * d1 + d2 * d2;

        if (d3 < 1.0E-8D)
        {
            return false;
        }
        else
        {
            double d4 = 1.0D / Math.sqrt(d3);
            d0 = d0 * d4;
            d1 = d1 * d4;
            d2 = d2 * d4;
            double d5 = 1.0D / Math.abs(d0);
            double d6 = 1.0D / Math.abs(d1);
            double d7 = 1.0D / Math.abs(d2);
            double d8 = (double)i - posVec31.xCoord;
            double d9 = (double)j - posVec31.yCoord;
            double d10 = (double)k - posVec31.zCoord;

            if (d0 >= 0.0D)
            {
                ++d8;
            }

            if (d1 >= 0.0D)
            {
                ++d9;
            }

            if (d2 >= 0.0D)
            {
                ++d10;
            }

            d8 = d8 / d0;
            d9 = d9 / d1;
            d10 = d10 / d2;
            int l = d0 < 0.0D ? -1 : 1;
            int i1 = d1 < 0.0D ? -1 : 1;
            int j1 = d2 < 0.0D ? -1 : 1;
            int k1 = MathHelper.floor_double(posVec32.xCoord);
            int l1 = MathHelper.floor_double(posVec32.yCoord);
            int i2 = MathHelper.floor_double(posVec32.zCoord);
            int j2 = k1 - i;
            int k2 = l1 - j;
            int l2 = i2 - k;

            while (j2 * l > 0 || k2 * i1 > 0 || l2 * j1 > 0)
            {
                if (d8 < d10 && d8 <= d9)
                {
                    d8 += d5;
                    i += l;
                    j2 = k1 - i;
                }
                else if (d9 < d8 && d9 <= d10)
                {
                    d9 += d6;
                    j += i1;
                    k2 = l1 - j;
                }
                else
                {
                    d10 += d7;
                    k += j1;
                    l2 = i2 - k;
                }
            }

            return true;
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
