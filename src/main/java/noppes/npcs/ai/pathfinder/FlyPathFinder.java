package noppes.npcs.ai.pathfinder;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.init.Blocks;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.util.MathHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.IntHashMap;
import net.minecraft.world.IBlockAccess;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public class FlyPathFinder extends PathFinder
{
    /** Used to find obstacles */
    private IBlockAccess worldMap;
    /** The path being generated */
    private PathHeap path = new PathHeap();
    /** The points in the path */
    protected final IntHashMap<NPCPathPoint> pointMap = new IntHashMap<NPCPathPoint>();
    /** Selection of path points to add to the path */
    private NPCPathPoint[] pathOptions = new NPCPathPoint[32];
    private Entity theEntity;
    private final Map<PathNodeType, Float> mapPathPriority = Maps.newEnumMap(PathNodeType.class);

    public FlyPathFinder(IBlockAccess _worldMap, boolean doorsAllowed, boolean closedDoors, boolean canPathWater, boolean canDrown, Entity entityIn)
    {
        super(_worldMap,doorsAllowed,closedDoors,canPathWater,canDrown);
        this.theEntity = entityIn;
        this.worldMap = _worldMap;
    }

    /**
     * Creates a path from one entity to another within a minimum distance
     */
    public NPCPath createEntityPathTo(Entity entityFrom, Entity entityTo, float distance)
    {
        return this.createEntityPathTo(entityFrom, entityTo.posX, entityTo.boundingBox.minY, entityTo.posZ, distance);
    }

    /**
     * Creates a path from an entity to a specified location within a minimum distance
     */
    public NPCPath createEntityPathTo(Entity entityFrom, int xTo, int yTo, int zTo, float distance)
    {
        return this.createEntityPathTo(entityFrom, (double)((float)xTo + 0.5F), (double)((float)yTo + 0.5F), (double)((float)zTo + 0.5F), distance);
    }

    /**
     * Internal implementation of creating a path from an entity to a point
     */

    private NPCPath createEntityPathTo(Entity entityIn, double x, double y, double z, float distance)
    {
        this.path.clearPath();
        this.pointMap.clearMap();
        NPCPathPoint pathpoint = this.getStart();
        NPCPathPoint pathpoint1 = this.getPathPointToCoords(entityIn, x, y, z);
        NPCPath pathentity = addToPath(entityIn, pathpoint, pathpoint1, distance);
        return pathentity;
    }

    public NPCPathPoint getStart()
    {
        int i;

        if (((EntityNPCInterface)theEntity).ai.canSwim && this.theEntity.isInWater())
        {
            i = (int)this.theEntity.boundingBox.minY;
            BlockPos.MutableBlockPos muteBlock = new BlockPos.MutableBlockPos(MathHelper.floor_double(this.theEntity.posX), i, MathHelper.floor_double(this.theEntity.posZ));

            for (Block block = this.worldMap.getBlock(muteBlock.getX(), muteBlock.getY(), muteBlock.getZ()); block == Blocks.flowing_water || block == Blocks.water; block = this.worldMap.getBlock(muteBlock.getX(), muteBlock.getY(), muteBlock.getZ()))
            {
                ++i;
                muteBlock.setPos(MathHelper.floor_double(this.theEntity.posX), i, MathHelper.floor_double(this.theEntity.posZ));
            }
        }
        else
        {
            i = MathHelper.floor_double(this.theEntity.boundingBox.minY + 0.5D);
        }

        BlockPos blockpos1 = new BlockPos(this.theEntity);
        PathNodeType pathnodetype1 = this.getPathNodeType((EntityLiving) this.theEntity, blockpos1.getX(), i, blockpos1.getZ());

        if (this.getPathPriority(pathnodetype1) < 0.0F)
        {
            Set<BlockPos> set = Sets.<BlockPos>newHashSet();
            set.add(new BlockPos(this.theEntity.boundingBox.minX, (double)i, this.theEntity.boundingBox.minZ));
            set.add(new BlockPos(this.theEntity.boundingBox.minX, (double)i, this.theEntity.boundingBox.maxZ));
            set.add(new BlockPos(this.theEntity.boundingBox.maxX, (double)i, this.theEntity.boundingBox.minZ));
            set.add(new BlockPos(this.theEntity.boundingBox.maxX, (double)i, this.theEntity.boundingBox.maxZ));

            for (BlockPos blockpos : set)
            {
                PathNodeType pathnodetype = this.getPathNodeType((EntityLiving)this.theEntity, blockpos);

                if (this.getPathPriority(pathnodetype) >= 0.0F)
                {
                    return openPoint(blockpos.getX(), blockpos.getY(), blockpos.getZ(), 0);
                }
            }
        }

        return openPoint(blockpos1.getX(), i, blockpos1.getZ(), 0);
    }
    

    public NPCPathPoint getPathPointToCoords(Entity entityIn, double x, double y, double z)
    {
        return openPoint(MathHelper.floor_double(x), MathHelper.floor_double(y), MathHelper.floor_double(z), 0);
    }


    /**
     * Adds a path from start to end and returns the whole path (args: unused, start, end, unused, maxDistance)
     */
    private NPCPath addToPath(Entity entityIn, NPCPathPoint pathFrom, NPCPathPoint pathTo, float maxDistance)
    {
        pathFrom.totalPathDistance = 0.0F;
        pathFrom.distanceToNext = pathFrom.distanceManhattan(pathTo);
        pathFrom.distanceToTarget = pathFrom.distanceToNext;
        this.path.clearPath();
        this.path.addPoint(pathFrom);
        NPCPathPoint pathpoint = pathFrom;
        int i = 0;

        while (!this.path.isPathEmpty())
        {
            ++i;

            if (i >= 200)
            {
                break;
            }

            NPCPathPoint pathpoint1 = this.path.dequeue();

            if (pathpoint1.equals(pathTo))
            {
                pathpoint = pathTo;
                break;
            }

            if (pathpoint1.distanceManhattan(pathTo) < pathpoint.distanceManhattan(pathTo))
            {
                pathpoint = pathpoint1;
            }

            pathpoint1.isFirst = true;
            int j = this.findPathOptions(this.pathOptions, entityIn, pathpoint1, pathTo, maxDistance);

            for (int k = 0; k < j; ++k)
            {
                NPCPathPoint pathpoint2 = this.pathOptions[k];
                float f = pathpoint1.distanceManhattan(pathpoint2);
                pathpoint2.distanceFromOrigin = pathpoint1.distanceFromOrigin + f;
                pathpoint2.cost = f + pathpoint2.costMalus;
                float f1 = pathpoint1.totalPathDistance + pathpoint2.cost;

                if (pathpoint2.distanceFromOrigin < maxDistance && (!pathpoint2.isAssigned() || f1 < pathpoint2.totalPathDistance))
                {
                    pathpoint2.previous = pathpoint1;
                    pathpoint2.totalPathDistance = f1;
                    pathpoint2.distanceToNext = pathpoint2.distanceManhattan(pathTo) + pathpoint2.costMalus;

                    if (pathpoint2.isAssigned())
                    {
                        this.path.changeDistance(pathpoint2, pathpoint2.totalPathDistance + pathpoint2.distanceToNext);
                    }
                    else
                    {
                        pathpoint2.distanceToTarget = pathpoint2.totalPathDistance + pathpoint2.distanceToNext;
                        this.path.addPoint(pathpoint2);
                    }
                }
            }
        }

        if (pathpoint == pathFrom)
        {
            return null;
        }
        else
        {
            return this.createEntityPath(pathFrom, pathpoint);
        }
    }

    /**
     * populates pathOptions with available points and returns the number of options found (args: unused1, currentPoint,
     * unused2, targetPoint, maxDistance)
     */
    public int findPathOptions(NPCPathPoint[] pathOptions, Entity entityIn, NPCPathPoint currentPoint, NPCPathPoint targetPoint, float maxDistance)
    {
        int i = 0;
        NPCPathPoint pathpoint = this.openPoint(currentPoint.xCoord, currentPoint.yCoord, currentPoint.zCoord + 1);
        NPCPathPoint pathpoint1 = this.openPoint(currentPoint.xCoord - 1, currentPoint.yCoord, currentPoint.zCoord);
        NPCPathPoint pathpoint2 = this.openPoint(currentPoint.xCoord + 1, currentPoint.yCoord, currentPoint.zCoord);
        NPCPathPoint pathpoint3 = this.openPoint(currentPoint.xCoord, currentPoint.yCoord, currentPoint.zCoord - 1);
        NPCPathPoint pathpoint4 = this.openPoint(currentPoint.xCoord, currentPoint.yCoord + 1, currentPoint.zCoord);
        NPCPathPoint pathpoint5 = this.openPoint(currentPoint.xCoord, currentPoint.yCoord - 1, currentPoint.zCoord);

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

        boolean flag = pathpoint3 != null && pathpoint3.costMalus != 0.0F;
        boolean flag1 = pathpoint != null && pathpoint.costMalus != 0.0F;
        boolean flag2 = pathpoint2 != null && pathpoint2.costMalus != 0.0F;
        boolean flag3 = pathpoint1 != null && pathpoint1.costMalus != 0.0F;
        boolean flag4 = pathpoint4 != null && pathpoint4.costMalus != 0.0F;
        boolean flag5 = pathpoint5 != null && pathpoint5.costMalus != 0.0F;

        if (flag && flag3)
        {
            NPCPathPoint pathpoint6 = this.openPoint(currentPoint.xCoord - 1, currentPoint.yCoord, currentPoint.zCoord - 1);

            if (pathpoint6 != null && !pathpoint6.isFirst && pathpoint6.distanceTo(targetPoint) < maxDistance)
            {
                pathOptions[i++] = pathpoint6;
            }
        }

        if (flag && flag2)
        {
            NPCPathPoint pathpoint7 = this.openPoint(currentPoint.xCoord + 1, currentPoint.yCoord, currentPoint.zCoord - 1);

            if (pathpoint7 != null && !pathpoint7.isFirst && pathpoint7.distanceTo(targetPoint) < maxDistance)
            {
                pathOptions[i++] = pathpoint7;
            }
        }

        if (flag1 && flag3)
        {
            NPCPathPoint pathpoint8 = this.openPoint(currentPoint.xCoord - 1, currentPoint.yCoord, currentPoint.zCoord + 1);

            if (pathpoint8 != null && !pathpoint8.isFirst && pathpoint8.distanceTo(targetPoint) < maxDistance)
            {
                pathOptions[i++] = pathpoint8;
            }
        }

        if (flag1 && flag2)
        {
            NPCPathPoint pathpoint9 = this.openPoint(currentPoint.xCoord + 1, currentPoint.yCoord, currentPoint.zCoord + 1);

            if (pathpoint9 != null && !pathpoint9.isFirst && pathpoint9.distanceTo(targetPoint) < maxDistance)
            {
                pathOptions[i++] = pathpoint9;
            }
        }

        if (flag && flag4)
        {
            NPCPathPoint pathpoint10 = this.openPoint(currentPoint.xCoord, currentPoint.yCoord + 1, currentPoint.zCoord - 1);

            if (pathpoint10 != null && !pathpoint10.isFirst && pathpoint10.distanceTo(targetPoint) < maxDistance)
            {
                pathOptions[i++] = pathpoint10;
            }
        }

        if (flag1 && flag4)
        {
            NPCPathPoint pathpoint11 = this.openPoint(currentPoint.xCoord, currentPoint.yCoord + 1, currentPoint.zCoord + 1);

            if (pathpoint11 != null && !pathpoint11.isFirst && pathpoint11.distanceTo(targetPoint) < maxDistance)
            {
                pathOptions[i++] = pathpoint11;
            }
        }

        if (flag2 && flag4)
        {
            NPCPathPoint pathpoint12 = this.openPoint(currentPoint.xCoord + 1, currentPoint.yCoord + 1, currentPoint.zCoord);

            if (pathpoint12 != null && !pathpoint12.isFirst && pathpoint12.distanceTo(targetPoint) < maxDistance)
            {
                pathOptions[i++] = pathpoint12;
            }
        }

        if (flag3 && flag4)
        {
            NPCPathPoint pathpoint13 = this.openPoint(currentPoint.xCoord - 1, currentPoint.yCoord + 1, currentPoint.zCoord);

            if (pathpoint13 != null && !pathpoint13.isFirst && pathpoint13.distanceTo(targetPoint) < maxDistance)
            {
                pathOptions[i++] = pathpoint13;
            }
        }

        if (flag && flag5)
        {
            NPCPathPoint pathpoint14 = this.openPoint(currentPoint.xCoord, currentPoint.yCoord - 1, currentPoint.zCoord - 1);

            if (pathpoint14 != null && !pathpoint14.isFirst && pathpoint14.distanceTo(targetPoint) < maxDistance)
            {
                pathOptions[i++] = pathpoint14;
            }
        }

        if (flag1 && flag5)
        {
            NPCPathPoint pathpoint15 = this.openPoint(currentPoint.xCoord, currentPoint.yCoord - 1, currentPoint.zCoord + 1);

            if (pathpoint15 != null && !pathpoint15.isFirst && pathpoint15.distanceTo(targetPoint) < maxDistance)
            {
                pathOptions[i++] = pathpoint15;
            }
        }

        if (flag2 && flag5)
        {
            NPCPathPoint pathpoint16 = this.openPoint(currentPoint.xCoord + 1, currentPoint.yCoord - 1, currentPoint.zCoord);

            if (pathpoint16 != null && !pathpoint16.isFirst && pathpoint16.distanceTo(targetPoint) < maxDistance)
            {
                pathOptions[i++] = pathpoint16;
            }
        }

        if (flag3 && flag5)
        {
            NPCPathPoint pathpoint17 = this.openPoint(currentPoint.xCoord - 1, currentPoint.yCoord - 1, currentPoint.zCoord);

            if (pathpoint17 != null && !pathpoint17.isFirst && pathpoint17.distanceTo(targetPoint) < maxDistance)
            {
                pathOptions[i++] = pathpoint17;
            }
        }

        return i;
    }

    @Nullable
    protected NPCPathPoint openPoint(int x, int y, int z)
    {
        NPCPathPoint pathpoint = null;
        BlockPos blockPos = new BlockPos(x, y, z);
        PathNodeType pathnodetype = this.getPathNodeType((EntityLiving) this.theEntity, blockPos);
        float f = getPathPriority(pathnodetype);

        if (f >= 0.0F)
        {
            pathpoint = this.openPoint(x, y, z, 0);
            pathpoint.nodeType = pathnodetype;
            pathpoint.costMalus = Math.max(pathpoint.costMalus, f);

            if (pathnodetype == PathNodeType.WALKABLE)
            {
                ++pathpoint.costMalus;
            }
        }

        return pathnodetype != PathNodeType.OPEN && pathnodetype != PathNodeType.WALKABLE ? pathpoint : pathpoint;
    }

    protected NPCPathPoint openPoint(int x, int y, int z, int b)
    {
        int i = NPCPathPoint.makeHash(x, y, z);
        NPCPathPoint pathpoint = this.pointMap.lookup(i);

        if (pathpoint == null)
        {
            pathpoint = new NPCPathPoint(x, y, z);
            this.pointMap.addKey(i, pathpoint);
        }

        return pathpoint;
    }

    /**
     * Returns a new FlyPathEntity for a given start and end point
     */
    private NPCPath createEntityPath(NPCPathPoint pathFrom, NPCPathPoint pathTo)
    {
        int i = 1;
        NPCPathPoint pathpoint2;

        for (pathpoint2 = pathTo; pathpoint2.previous != null; pathpoint2 = pathpoint2.previous)
        {
            ++i;
        }

        NPCPathPoint[] apathpoint = new NPCPathPoint[i];
        pathpoint2 = pathTo;
        --i;

        for (apathpoint[i] = pathTo; pathpoint2.previous != null; apathpoint[i] = pathpoint2)
        {
            pathpoint2 = pathpoint2.previous;
            --i;
        }

        return new NPCPath(apathpoint);
    }


    public PathNodeType getPathNodeType(IBlockAccess blockaccessIn, int x, int y, int z, EntityLiving entitylivingIn, int xSize, int ySize, int zSize, boolean canBreakDoorsIn, boolean canEnterDoorsIn)
    {
        EnumSet<PathNodeType> enumset = EnumSet.<PathNodeType>noneOf(PathNodeType.class);
        PathNodeType pathnodetype = PathNodeType.BLOCKED;
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

    public PathNodeType getPathNodeType(IBlockAccess blockaccessIn, int x, int y, int z)
    {
        PathNodeType pathnodetype = this.getPathNodeTypeRaw(blockaccessIn, x, y, z);

        if (pathnodetype == PathNodeType.OPEN && y >= 1)
        {
            PathNodeType pathnodetype1 = this.getPathNodeTypeRaw(blockaccessIn, x, y - 1, z);

            if (pathnodetype1 != PathNodeType.DAMAGE_FIRE && pathnodetype1 != PathNodeType.LAVA)
            {
                if (pathnodetype1 == PathNodeType.DAMAGE_CACTUS)
                {
                    pathnodetype = PathNodeType.DAMAGE_CACTUS;
                }
                else if (pathnodetype1 == PathNodeType.DAMAGE_OTHER) pathnodetype = PathNodeType.DAMAGE_OTHER;
                else
                {
                    pathnodetype = pathnodetype1 != PathNodeType.WALKABLE && pathnodetype1 != PathNodeType.OPEN && pathnodetype1 != PathNodeType.WATER ? PathNodeType.WALKABLE : PathNodeType.OPEN;
                }
            }
            else
            {
                pathnodetype = PathNodeType.DAMAGE_FIRE;
            }
        }

        pathnodetype = this.checkNeighborBlocks(blockaccessIn, x, y, z, pathnodetype);
        return pathnodetype;
    }

    public PathNodeType getPathNodeType(IBlockAccess blockAccess, int x, int y, int z, int xSize, int ySize, int zSize, boolean canOpenDoorsIn, boolean canEnterDoorsIn, EnumSet<PathNodeType> p_193577_10_, PathNodeType p_193577_11_, BlockPos p_193577_12_)
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
                    PathNodeType pathnodetype = this.getPathNodeType(blockAccess, l, i1, j1);

                    if (pathnodetype == PathNodeType.DOOR_WOOD_CLOSED && canOpenDoorsIn && canEnterDoorsIn)
                    {
                        pathnodetype = PathNodeType.WALKABLE;
                    }

                    if (pathnodetype == PathNodeType.DOOR_OPEN && !canEnterDoorsIn)
                    {
                        pathnodetype = PathNodeType.BLOCKED;
                    }

                    if (pathnodetype == PathNodeType.RAIL && !(blockAccess.getBlock(p_193577_12_.getX(), p_193577_12_.getY(), p_193577_12_.getZ()) instanceof BlockRailBase) && !(blockAccess.getBlock(p_193577_12_.down().getX(), p_193577_12_.down().getY(), p_193577_12_.down().getZ()) instanceof BlockRailBase))
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

    private PathNodeType getPathNodeType(EntityLiving p_192559_1_, BlockPos p_192559_2_)
    {
        return this.getPathNodeType(p_192559_1_, p_192559_2_.getX(), p_192559_2_.getY(), p_192559_2_.getZ());
    }

    private PathNodeType getPathNodeType(EntityLiving p_192558_1_, int p_192558_2_, int p_192558_3_, int p_192558_4_)
    {
        int entitySizeX = MathHelper.floor_double(this.theEntity.width + 1.0F);
        int entitySizeY = MathHelper.floor_double(this.theEntity.height + 1.0F);
        int entitySizeZ = MathHelper.floor_double(this.theEntity.width + 1.0F);
        return this.getPathNodeType(this.worldMap, p_192558_2_, p_192558_3_, p_192558_4_, p_192558_1_, entitySizeX, entitySizeY, entitySizeZ, ((EntityCustomNpc)this.theEntity).ai.doorInteract == 0, ((EntityCustomNpc)this.theEntity).ai.doorInteract == 1);
    }

    public PathNodeType checkNeighborBlocks(IBlockAccess worldmap, int x, int y, int z, PathNodeType type)
    {
        BlockPos.PooledMutableBlockPos blockpos$pooledmutableblockpos = BlockPos.PooledMutableBlockPos.retain();

        if (type == PathNodeType.WALKABLE)
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
                            type = PathNodeType.DANGER_CACTUS;
                        }
                        else if (block == Blocks.fire)
                        {
                            type = PathNodeType.DANGER_FIRE;
                        }
                    }
                }
            }
        }

        blockpos$pooledmutableblockpos.release();
        return type;
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
            else if (block instanceof BlockDoor && material == Material.wood && !((BlockDoor)block).getBlocksMovement(this.worldMap, p_189553_2_, p_189553_3_, p_189553_4_))
            {
                return PathNodeType.DOOR_WOOD_CLOSED;
            }
            else if (block instanceof BlockDoor && material == Material.wood && !((BlockDoor)block).getBlocksMovement(this.worldMap, p_189553_2_, p_189553_3_, p_189553_4_))
            {
                return PathNodeType.DOOR_IRON_CLOSED;
            }
            else if (block instanceof BlockDoor && ((BlockDoor)block).getBlocksMovement(this.worldMap, p_189553_2_, p_189553_3_, p_189553_4_))
            {
                return PathNodeType.DOOR_OPEN;
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

    public float getPathPriority(PathNodeType nodeType)
    {
        Float f = this.mapPathPriority.get(nodeType);
        return f == null ? nodeType.getPriority() : f.floatValue();
    }
}
