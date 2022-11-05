package noppes.npcs.ai.pathfinder;

import com.google.common.collect.Maps;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.init.Blocks;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.MathHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.IntHashMap;
import net.minecraft.world.IBlockAccess;
import noppes.npcs.entity.EntityNPCInterface;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.Map;

public class FlyPathFinder extends PathFinder
{
    /** Used to find obstacles */
    private IBlockAccess worldMap;
    /** The path being generated */
    private FlyPath path = new FlyPath();
    /** The points in the path */
    protected final IntHashMap<FlyPathPoint> pointMap = new IntHashMap<FlyPathPoint>();
    /** Selection of path points to add to the path */
    private FlyPathPoint[] pathOptions = new FlyPathPoint[32];
    /** should the PathFinder go through wodden door blocks */
    private boolean isWoddenDoorAllowed;
    /** should the PathFinder disregard BlockMovement type materials in its path */
    private boolean isMovementBlockAllowed;
    private boolean isPathingInWater;
    /** tells the FathFinder to not stop pathing underwater */
    private boolean canEntityDrown;
    private static final String __OBFID = "CL_00000576";
    private Entity theEntity;
    private final Map<PathNodeType, Float> mapPathPriority = Maps.newEnumMap(PathNodeType.class);

    public FlyPathFinder(IBlockAccess p_i2137_1_, boolean p_i2137_2_, boolean p_i2137_3_, boolean p_i2137_4_, boolean p_i2137_5_, Entity p_72865_1_)
    {
        super(p_i2137_1_,p_i2137_2_,p_i2137_3_,p_i2137_4_,p_i2137_5_);
        this.theEntity = p_72865_1_;
        this.worldMap = p_i2137_1_;
        this.isWoddenDoorAllowed = p_i2137_2_;
        this.isMovementBlockAllowed = p_i2137_3_;
        this.isPathingInWater = p_i2137_4_;
        this.canEntityDrown = p_i2137_5_;
    }

    /**
     * Creates a path from one entity to another within a minimum distance
     */
    public FlyPathEntity createEntityPathTo(Entity p_75856_1_, Entity p_75856_2_, float p_75856_3_)
    {
        return this.createEntityPathTo(p_75856_1_, p_75856_2_.posX, p_75856_2_.boundingBox.minY, p_75856_2_.posZ, p_75856_3_);
    }

    /**
     * Creates a path from an entity to a specified location within a minimum distance
     */
    public FlyPathEntity createEntityPathTo(Entity p_75859_1_, int p_75859_2_, int p_75859_3_, int p_75859_4_, float p_75859_5_)
    {
        return this.createEntityPathTo(p_75859_1_, (double)((float)p_75859_2_ + 0.5F), (double)((float)p_75859_3_ + 0.5F), (double)((float)p_75859_4_ + 0.5F), p_75859_5_);
    }

    /**
     * Internal implementation of creating a path from an entity to a point
     */

    private FlyPathEntity createEntityPathTo(Entity entityIn, double x, double y, double z, float distance)
    {
        this.path.clearPath();
        this.pointMap.clearMap();
        FlyPathPoint pathpoint = this.getPathPointTo(entityIn);
        FlyPathPoint pathpoint1 = this.getPathPointToCoords(entityIn, x, y, z);
        FlyPathEntity pathentity = addToPath(entityIn, pathpoint, pathpoint1, distance);
        return pathentity;
    }

    public FlyPathPoint getPathPointTo(Entity entity)
    {
        int i;

        if (((EntityNPCInterface) entity).ai.canSwim && entity.isInWater())
        {
            i = (int)entity.boundingBox.minY;
            BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos(MathHelper.floor_double(entity.posX), i, MathHelper.floor_double(entity.posZ));

            for (Block block = this.worldMap.getBlock(blockpos$mutableblockpos.getX(), blockpos$mutableblockpos.getY(), blockpos$mutableblockpos.getZ());
                 block == Blocks.flowing_water || block == Blocks.water;
                 block = this.worldMap.getBlock(blockpos$mutableblockpos.getX(), blockpos$mutableblockpos.getY(), blockpos$mutableblockpos.getZ()))
            {
                ++i;
                blockpos$mutableblockpos.setPos(MathHelper.floor_double(entity.posX), i, MathHelper.floor_double(entity.posZ));
            }
        }
        else
        {
            i = MathHelper.floor_double(entity.boundingBox.minY + 0.5D);
        }

        BlockPos blockpos1 = new BlockPos(entity);

        return this.openPoint(blockpos1.getX(), i, blockpos1.getZ(), i);
    }

    public FlyPathPoint getPathPointToCoords(Entity entityIn, double x, double y, double z)
    {
        return openPoint(MathHelper.floor_double(x), MathHelper.floor_double(y), MathHelper.floor_double(z), (int)z);
    }


    /**
     * Adds a path from start to end and returns the whole path (args: unused, start, end, unused, maxDistance)
     */
    private FlyPathEntity addToPath(Entity entityIn, FlyPathPoint pathpointStart, FlyPathPoint pathpointEnd, float maxDistance)
    {
        pathpointStart.totalPathDistance = 0.0F;
        pathpointStart.distanceToNext = pathpointStart.distanceToSquared(pathpointEnd);
        pathpointStart.distanceToTarget = pathpointStart.distanceToNext;
        this.path.clearPath();
        this.path.addPoint(pathpointStart);
        FlyPathPoint pathpoint3 = pathpointStart;

        while (!this.path.isPathEmpty())
        {
            FlyPathPoint pathpoint4 = this.path.dequeue();

            if (pathpoint4.equals(pathpointEnd))
            {
                return this.createEntityPath(pathpointStart, pathpointEnd);
            }

            if (pathpoint4.distanceToSquared(pathpointEnd) < pathpoint3.distanceToSquared(pathpointEnd))
            {
                pathpoint3 = pathpoint4;
            }

            pathpoint4.isFirst = true;
            int i = this.findPathOptions(this.pathOptions, entityIn, pathpoint4, pathpointEnd, maxDistance);

            for (int j = 0; j < i; ++j)
            {
                FlyPathPoint pathpoint5 = this.pathOptions[j];
                float f1 = pathpoint4.totalPathDistance + pathpoint4.distanceToSquared(pathpoint5);

                if (!pathpoint5.isAssigned() || f1 < pathpoint5.totalPathDistance)
                {
                    pathpoint5.previous = pathpoint4;
                    pathpoint5.totalPathDistance = f1;
                    pathpoint5.distanceToNext = pathpoint5.distanceToSquared(pathpointEnd);

                    if (pathpoint5.isAssigned())
                    {
                        this.path.changeDistance(pathpoint5, pathpoint5.totalPathDistance + pathpoint5.distanceToNext);
                    }
                    else
                    {
                        pathpoint5.distanceToTarget = pathpoint5.totalPathDistance + pathpoint5.distanceToNext;
                        this.path.addPoint(pathpoint5);
                    }
                }
            }
        }

        if (pathpoint3 == pathpointStart)
        {
            return null;
        }
        else
        {
            return this.createEntityPath(pathpointStart, pathpoint3);
        }
    }

    /**
     * populates pathOptions with available points and returns the number of options found (args: unused1, currentPoint,
     * unused2, targetPoint, maxDistance)
     */
    public int findPathOptions(FlyPathPoint[] pathOptions, Entity entityIn, FlyPathPoint currentPoint, FlyPathPoint targetPoint, float maxDistance)
    {
        int i = 0;
        FlyPathPoint pathpoint = this.openPoint(currentPoint.xCoord, currentPoint.yCoord, currentPoint.zCoord + 1);
        FlyPathPoint pathpoint1 = this.openPoint(currentPoint.xCoord - 1, currentPoint.yCoord, currentPoint.zCoord);
        FlyPathPoint pathpoint2 = this.openPoint(currentPoint.xCoord + 1, currentPoint.yCoord, currentPoint.zCoord);
        FlyPathPoint pathpoint3 = this.openPoint(currentPoint.xCoord, currentPoint.yCoord, currentPoint.zCoord - 1);
        FlyPathPoint pathpoint4 = this.openPoint(currentPoint.xCoord, currentPoint.yCoord + 1, currentPoint.zCoord);
        FlyPathPoint pathpoint5 = this.openPoint(currentPoint.xCoord, currentPoint.yCoord - 1, currentPoint.zCoord);

        if (pathpoint != null && !pathpoint.isFirst && pathpoint.distanceTo(targetPoint) < maxDistance)
        {
            pathOptions[i++] = pathpoint;
        }

        if (pathpoint1 != null && !pathpoint1.isFirst && pathpoint1.distanceTo(targetPoint) < maxDistance)
        {
            pathOptions[i++] = pathpoint1;
        }

        if (pathpoint2 != null && !pathpoint2.isFirst && pathpoint2.distanceTo(targetPoint) < maxDistance)
        {
            pathOptions[i++] = pathpoint2;
        }

        if (pathpoint3 != null && !pathpoint3.isFirst && pathpoint3.distanceTo(targetPoint) < maxDistance)
        {
            pathOptions[i++] = pathpoint3;
        }

        if (pathpoint4 != null && !pathpoint4.isFirst && pathpoint4.distanceTo(targetPoint) < maxDistance)
        {
            pathOptions[i++] = pathpoint4;
        }

        if (pathpoint5 != null && !pathpoint5.isFirst && pathpoint5.distanceTo(targetPoint) < maxDistance)
        {
            pathOptions[i++] = pathpoint5;
        }

        boolean flag = pathpoint3 == null;
        boolean flag1 = pathpoint == null;
        boolean flag2 = pathpoint2 == null;
        boolean flag3 = pathpoint1 == null;
        boolean flag4 = pathpoint4 == null;
        boolean flag5 = pathpoint5 == null;

        if (flag && flag3)
        {
            FlyPathPoint pathpoint6 = this.openPoint(currentPoint.xCoord - 1, currentPoint.yCoord, currentPoint.zCoord - 1);

            if (pathpoint6 != null && !pathpoint6.isFirst && pathpoint6.distanceTo(targetPoint) < maxDistance)
            {
                pathOptions[i++] = pathpoint6;
            }
        }

        if (flag && flag2)
        {
            FlyPathPoint pathpoint7 = this.openPoint(currentPoint.xCoord + 1, currentPoint.yCoord, currentPoint.zCoord - 1);

            if (pathpoint7 != null && !pathpoint7.isFirst && pathpoint7.distanceTo(targetPoint) < maxDistance)
            {
                pathOptions[i++] = pathpoint7;
            }
        }

        if (flag1 && flag3)
        {
            FlyPathPoint pathpoint8 = this.openPoint(currentPoint.xCoord - 1, currentPoint.yCoord, currentPoint.zCoord + 1);

            if (pathpoint8 != null && !pathpoint8.isFirst && pathpoint8.distanceTo(targetPoint) < maxDistance)
            {
                pathOptions[i++] = pathpoint8;
            }
        }

        if (flag1 && flag2)
        {
            FlyPathPoint pathpoint9 = this.openPoint(currentPoint.xCoord + 1, currentPoint.yCoord, currentPoint.zCoord + 1);

            if (pathpoint9 != null && !pathpoint9.isFirst && pathpoint9.distanceTo(targetPoint) < maxDistance)
            {
                pathOptions[i++] = pathpoint9;
            }
        }

        if (flag && flag4)
        {
            FlyPathPoint pathpoint10 = this.openPoint(currentPoint.xCoord, currentPoint.yCoord + 1, currentPoint.zCoord - 1);

            if (pathpoint10 != null && !pathpoint10.isFirst && pathpoint10.distanceTo(targetPoint) < maxDistance)
            {
                pathOptions[i++] = pathpoint10;
            }
        }

        if (flag1 && flag4)
        {
            FlyPathPoint pathpoint11 = this.openPoint(currentPoint.xCoord, currentPoint.yCoord + 1, currentPoint.zCoord + 1);

            if (pathpoint11 != null && !pathpoint11.isFirst && pathpoint11.distanceTo(targetPoint) < maxDistance)
            {
                pathOptions[i++] = pathpoint11;
            }
        }

        if (flag2 && flag4)
        {
            FlyPathPoint pathpoint12 = this.openPoint(currentPoint.xCoord + 1, currentPoint.yCoord + 1, currentPoint.zCoord);

            if (pathpoint12 != null && !pathpoint12.isFirst && pathpoint12.distanceTo(targetPoint) < maxDistance)
            {
                pathOptions[i++] = pathpoint12;
            }
        }

        if (flag3 && flag4)
        {
            FlyPathPoint pathpoint13 = this.openPoint(currentPoint.xCoord - 1, currentPoint.yCoord + 1, currentPoint.zCoord);

            if (pathpoint13 != null && !pathpoint13.isFirst && pathpoint13.distanceTo(targetPoint) < maxDistance)
            {
                pathOptions[i++] = pathpoint13;
            }
        }

        if (flag && flag5)
        {
            FlyPathPoint pathpoint14 = this.openPoint(currentPoint.xCoord, currentPoint.yCoord - 1, currentPoint.zCoord - 1);

            if (pathpoint14 != null && !pathpoint14.isFirst && pathpoint14.distanceTo(targetPoint) < maxDistance)
            {
                pathOptions[i++] = pathpoint14;
            }
        }

        if (flag1 && flag5)
        {
            FlyPathPoint pathpoint15 = this.openPoint(currentPoint.xCoord, currentPoint.yCoord - 1, currentPoint.zCoord + 1);

            if (pathpoint15 != null && !pathpoint15.isFirst && pathpoint15.distanceTo(targetPoint) < maxDistance)
            {
                pathOptions[i++] = pathpoint15;
            }
        }

        if (flag2 && flag5)
        {
            FlyPathPoint pathpoint16 = this.openPoint(currentPoint.xCoord + 1, currentPoint.yCoord - 1, currentPoint.zCoord);

            if (pathpoint16 != null && !pathpoint16.isFirst && pathpoint16.distanceTo(targetPoint) < maxDistance)
            {
                pathOptions[i++] = pathpoint16;
            }
        }

        if (flag3 && flag5)
        {
            FlyPathPoint pathpoint17 = this.openPoint(currentPoint.xCoord - 1, currentPoint.yCoord - 1, currentPoint.zCoord);

            if (pathpoint17 != null && !pathpoint17.isFirst && pathpoint17.distanceTo(targetPoint) < maxDistance)
            {
                pathOptions[i++] = pathpoint17;
            }
        }

        return i;
    }

    @Nullable
    protected FlyPathPoint openPoint(int x, int y, int z)
    {
        FlyPathPoint pathpoint = null;
        BlockPos blockPos = new BlockPos(x, y, z);
        PathNodeType pathnodetype = this.getPathNodeType((EntityLiving) this.theEntity, blockPos);
        float f = getPathPriority(pathnodetype);

        if (f >= 0.0F)
        {
            pathpoint = openPoint(x, y, z, x);
            pathpoint.nodeType = pathnodetype;
            pathpoint.costMalus = Math.max(pathpoint.costMalus, f);

            if (pathnodetype == PathNodeType.WALKABLE)
            {
                ++pathpoint.costMalus;
            }
        }

        return pathnodetype != PathNodeType.OPEN && pathnodetype != PathNodeType.WALKABLE ? pathpoint : pathpoint;
    }

    protected FlyPathPoint openPoint(int x, int y, int z, int b)
    {
        int i = FlyPathPoint.makeHash(x, y, z);
        FlyPathPoint pathpoint = this.pointMap.lookup(i);

        if (pathpoint == null)
        {
            pathpoint = new FlyPathPoint(x, y, z);
            this.pointMap.addKey(i, pathpoint);
        }

        return pathpoint;
    }

    /**
     * Returns a new FlyPathEntity for a given start and end point
     */
    private FlyPathEntity createEntityPath(FlyPathPoint p_75853_1_, FlyPathPoint p_75853_2_)
    {
        int i = 1;
        FlyPathPoint pathpoint2;

        for (pathpoint2 = p_75853_2_; pathpoint2.previous != null; pathpoint2 = pathpoint2.previous)
        {
            ++i;
        }

        FlyPathPoint[] apathpoint = new FlyPathPoint[i];
        pathpoint2 = p_75853_2_;
        --i;

        for (apathpoint[i] = p_75853_2_; pathpoint2.previous != null; apathpoint[i] = pathpoint2)
        {
            pathpoint2 = pathpoint2.previous;
            --i;
        }

        return new FlyPathEntity(apathpoint);
    }




    public PathNodeType getPathNodeType(IBlockAccess blockaccessIn, int x, int y, int z, EntityLiving entitylivingIn, int xSize, int ySize, int zSize, boolean canBreakDoorsIn, boolean canEnterDoorsIn)
    {
        EnumSet<PathNodeType> enumset = EnumSet.<PathNodeType>noneOf(PathNodeType.class);
        PathNodeType pathnodetype = PathNodeType.BLOCKED;
        double d0 = (double)entitylivingIn.width / 2.0D;
        BlockPos blockpos = new BlockPos(entitylivingIn);
        pathnodetype = this.getPathNodeType(blockaccessIn, x, y, z, xSize, ySize, zSize, canBreakDoorsIn, canEnterDoorsIn, enumset, pathnodetype, blockpos);

        if (enumset.contains(PathNodeType.FENCE))
        {
            return PathNodeType.FENCE;
        }
        else
        {
            PathNodeType pathnodetype1 = PathNodeType.BLOCKED;

            for (PathNodeType pathnodetype2 : enumset)
            {
                if (getPathPriority(pathnodetype2) < 0.0F)
                {
                    return pathnodetype2;
                }

                if (getPathPriority(pathnodetype2) >= getPathPriority(pathnodetype1))
                {
                    pathnodetype1 = pathnodetype2;
                }
            }

            if (pathnodetype == PathNodeType.OPEN && getPathPriority(pathnodetype1) == 0.0F)
            {
                return PathNodeType.OPEN;
            }
            else
            {
                return pathnodetype1;
            }
        }
    }

    public PathNodeType getPathNodeType(IBlockAccess p_193577_1_, int x, int y, int z, int xSize, int ySize, int zSize, boolean canOpenDoorsIn, boolean canEnterDoorsIn, EnumSet<PathNodeType> p_193577_10_, PathNodeType p_193577_11_, BlockPos p_193577_12_)
    {
        for (int i = 0; i < xSize; ++i)
        {
            for (int j = 0; j < ySize; ++j)
            {
                for (int k = 0; k < zSize; ++k)
                {
                    int l = i + x;
                    int i1 = j + y;
                    int j1 = k + z;
                    PathNodeType pathnodetype = this.getPathNodeType(p_193577_1_, l, i1, j1);

                    if (pathnodetype == PathNodeType.DOOR_WOOD_CLOSED && canOpenDoorsIn && canEnterDoorsIn)
                    {
                        pathnodetype = PathNodeType.WALKABLE;
                    }

                    if (pathnodetype == PathNodeType.DOOR_OPEN && !canEnterDoorsIn)
                    {
                        pathnodetype = PathNodeType.BLOCKED;
                    }

                    if (pathnodetype == PathNodeType.RAIL && !(p_193577_1_.getBlock(p_193577_12_.getX(), p_193577_12_.getY(), p_193577_12_.getZ()) instanceof BlockRailBase) && !(p_193577_1_.getBlock(p_193577_12_.down().getX(), p_193577_12_.down().getY(), p_193577_12_.down().getZ()) instanceof BlockRailBase))
                    {
                        pathnodetype = PathNodeType.FENCE;
                    }

                    if (i == 0 && j == 0 && k == 0)
                    {
                        p_193577_11_ = pathnodetype;
                    }

                    p_193577_10_.add(pathnodetype);
                }
            }
        }

        return p_193577_11_;
    }

    public PathNodeType getPathNodeType(IBlockAccess blockaccessIn, int x, int y, int z)
    {
        PathNodeType pathnodetype = this.getPathNodeTypeRaw(blockaccessIn, x, y, z);

        if (pathnodetype == PathNodeType.OPEN && y >= 1)
        {
            PathNodeType pathnodetype1 = this.getPathNodeTypeRaw(blockaccessIn, x, y - 1, z);
            pathnodetype = pathnodetype1 != PathNodeType.WALKABLE && pathnodetype1 != PathNodeType.OPEN && pathnodetype1 != PathNodeType.WATER && pathnodetype1 != PathNodeType.LAVA ? PathNodeType.WALKABLE : PathNodeType.OPEN;

            if (pathnodetype1 == PathNodeType.DAMAGE_FIRE)
            {
                pathnodetype = PathNodeType.DAMAGE_FIRE;
            }

            if (pathnodetype1 == PathNodeType.DAMAGE_CACTUS)
            {
                pathnodetype = PathNodeType.DAMAGE_CACTUS;
            }

            if (pathnodetype1 == PathNodeType.DAMAGE_OTHER) pathnodetype = PathNodeType.DAMAGE_OTHER;
        }

        pathnodetype = this.checkNeighborBlocks(blockaccessIn, x, y, z, pathnodetype);
        return pathnodetype;
    }

    public PathNodeType checkNeighborBlocks(IBlockAccess worldmap, int x, int y, int z, PathNodeType p_193578_5_)
    {
        BlockPos.PooledMutableBlockPos blockpos$pooledmutableblockpos = BlockPos.PooledMutableBlockPos.retain();

        if (p_193578_5_ == PathNodeType.WALKABLE)
        {
            for (int i = -1; i <= 1; ++i)
            {
                for (int j = -1; j <= 1; ++j)
                {
                    if (i != 0 || j != 0)
                    {
                        blockpos$pooledmutableblockpos.setPos(i + x, y, j + z);
                        Block block = worldmap.getBlock(blockpos$pooledmutableblockpos.getX(), blockpos$pooledmutableblockpos.getY(), blockpos$pooledmutableblockpos.getZ());
                        if (block == Blocks.cactus)
                        {
                            p_193578_5_ = PathNodeType.DANGER_CACTUS;
                        }
                        else if (block == Blocks.fire)
                        {
                            p_193578_5_ = PathNodeType.DANGER_FIRE;
                        }
                    }
                }
            }
        }

        blockpos$pooledmutableblockpos.release();
        return p_193578_5_;
    }

    protected PathNodeType getPathNodeTypeRaw(IBlockAccess worldmap, int p_189553_2_, int p_189553_3_, int p_189553_4_)
    {
        BlockPos blockpos = new BlockPos(p_189553_2_, p_189553_3_, p_189553_4_);
        Block block = worldmap.getBlock(blockpos.getX(), blockpos.getY(), blockpos.getZ());
        Material material = block.getMaterial();

        if (material == Material.air)
        {
            return PathNodeType.OPEN;
        }
        else if (block != Blocks.trapdoor && block != Blocks.waterlily)
        {
            if (block == Blocks.fire)
            {
                return PathNodeType.DAMAGE_FIRE;
            }
            else if (block == Blocks.cactus)
            {
                return PathNodeType.DAMAGE_CACTUS;
            }
            else if (block instanceof BlockDoor && material == Material.wood)
            {
                return PathNodeType.DOOR_WOOD_CLOSED;
            }
            else if (block instanceof BlockDoor && material == Material.iron)
            {
                return PathNodeType.DOOR_IRON_CLOSED;
            }
            else if (block instanceof BlockRailBase)
            {
                return PathNodeType.RAIL;
            }
            else if (!(block instanceof BlockFence) && !(block instanceof BlockWall) && (!(block instanceof BlockFenceGate)))
            {
                if (material == Material.water)
                {
                    return PathNodeType.WATER;
                }
                else if (material == Material.lava)
                {
                    return PathNodeType.LAVA;
                }
                else
                {
                    return block.getBlocksMovement(worldmap, blockpos.getX(), blockpos.getY(), blockpos.getZ()) ? PathNodeType.OPEN : PathNodeType.BLOCKED;
                }
            }
            else
            {
                return PathNodeType.FENCE;
            }
        }
        else
        {
            return PathNodeType.TRAPDOOR;
        }
    }

    private PathNodeType getPathNodeType(EntityLiving p_192559_1_, BlockPos p_192559_2_)
    {
        return this.getPathNodeType(p_192559_1_, p_192559_2_.getX(), p_192559_2_.getY(), p_192559_2_.getZ());
    }

    private PathNodeType getPathNodeType(EntityLiving p_192558_1_, int p_192558_2_, int p_192558_3_, int p_192558_4_)
    {
        int entitySizeX = MathHelper.floor_double(p_192558_1_.width + 1.0F);
        int entitySizeY = MathHelper.floor_double(p_192558_1_.height + 1.0F);
        int entitySizeZ = MathHelper.floor_double(p_192558_1_.width + 1.0F);
        return this.getPathNodeType(this.worldMap, p_192558_2_, p_192558_3_, p_192558_4_, p_192558_1_, entitySizeX, entitySizeY, entitySizeZ, false, false);
    }

    public float getPathPriority(PathNodeType nodeType)
    {
        Float f = this.mapPathPriority.get(nodeType);
        return f == null ? nodeType.getPriority() : f.floatValue();
    }


}
