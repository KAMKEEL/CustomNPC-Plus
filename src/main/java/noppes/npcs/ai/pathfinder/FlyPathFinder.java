package noppes.npcs.ai.pathfinder;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.init.Blocks;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.pathfinding.PathPoint;
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


    private boolean isPathingInWater;
    private boolean isMovementBlockAllowed;
    private boolean isWoodenDoorAllowed;
    private boolean canEntityDrown;

    public FlyPathFinder(IBlockAccess _worldMap, boolean doorsAllowed, boolean closedDoors, boolean canPathWater, boolean canDrown, Entity entityIn)
    {
        super(_worldMap,doorsAllowed,closedDoors,canPathWater,canDrown);
        this.theEntity = entityIn;
        this.worldMap = _worldMap;

        this.isWoodenDoorAllowed = doorsAllowed;
        this.isMovementBlockAllowed = closedDoors;
        this.isPathingInWater = canPathWater;
        this.canEntityDrown = canDrown;
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
        NPCPathPoint pathPoint2 = new NPCPathPoint((int)Math.ceil(entityIn.width),(int)Math.ceil(entityIn.height),(int)Math.ceil(entityIn.width));
        NPCPath pathentity = addToPath(entityIn, pathpoint, pathpoint1, pathPoint2, distance);
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
                    return openPoint(blockpos.getX(), blockpos.getY(), blockpos.getZ());
                }
            }
        }

        return openPoint(blockpos1.getX(), i, blockpos1.getZ());
    }
    

    public NPCPathPoint getPathPointToCoords(Entity entityIn, double x, double y, double z)
    {
        return openPoint(MathHelper.floor_double(x), MathHelper.floor_double(y), MathHelper.floor_double(z));
    }


    private NPCPath addToPath(Entity p_75861_1_, NPCPathPoint p_75861_2_, NPCPathPoint p_75861_3_, NPCPathPoint p_75861_4_, float p_75861_5_)
    {
        p_75861_2_.totalPathDistance = 0.0F;
        p_75861_2_.distanceToNext = p_75861_2_.distanceToSquared(p_75861_3_);
        p_75861_2_.distanceToTarget = p_75861_2_.distanceToNext;
        this.path.clearPath();
        this.path.addPoint(p_75861_2_);
        NPCPathPoint pathpoint3 = p_75861_2_;

        while (!this.path.isPathEmpty())
        {
            NPCPathPoint pathpoint4 = this.path.dequeue();

            if (pathpoint4.equals(p_75861_3_))
            {
                return this.createEntityPath(p_75861_2_, p_75861_3_);
            }

            if (pathpoint4.distanceToSquared(p_75861_3_) < pathpoint3.distanceToSquared(p_75861_3_))
            {
                pathpoint3 = pathpoint4;
            }

            pathpoint4.isFirst = true;
            int i = this.findPathOptions(p_75861_1_, pathpoint4, p_75861_4_, p_75861_3_, p_75861_5_);

            for (int j = 0; j < i; ++j)
            {
                NPCPathPoint pathpoint5 = this.pathOptions[j];
                float f1 = pathpoint4.totalPathDistance + pathpoint4.distanceToSquared(pathpoint5);

                if (!pathpoint5.isAssigned() || f1 < pathpoint5.totalPathDistance)
                {
                    pathpoint5.previous = pathpoint4;
                    pathpoint5.totalPathDistance = f1;
                    pathpoint5.distanceToNext = pathpoint5.distanceToSquared(p_75861_3_);

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

        if (pathpoint3 == p_75861_2_)
        {
            return null;
        }
        else
        {
            return this.createEntityPath(p_75861_2_, pathpoint3);
        }
    }

    /**
     * populates pathOptions with available points and returns the number of options found (args: unused1, currentPoint,
     * unused2, targetPoint, maxDistance)
     */
    private int findPathOptions(Entity p_75860_1_, NPCPathPoint p_75860_2_, NPCPathPoint p_75860_3_, NPCPathPoint targetPoint, float maxDistance)
    {
        int i = 0;
        byte b0 = 0;

        /*if (this.getVerticalOffset(p_75860_1_, p_75860_2_.xCoord, p_75860_2_.yCoord + 1, p_75860_2_.zCoord, p_75860_3_) == 1) {
            b0 = 1;
        }*/

        NPCPathPoint pathpoint0 = this.getSafePoint(p_75860_1_, p_75860_2_.xCoord, p_75860_2_.yCoord, p_75860_2_.zCoord + 1, p_75860_3_, b0);
        NPCPathPoint pathpoint1 = this.getSafePoint(p_75860_1_, p_75860_2_.xCoord - 1, p_75860_2_.yCoord, p_75860_2_.zCoord, p_75860_3_, b0);
        NPCPathPoint pathpoint2 = this.getSafePoint(p_75860_1_, p_75860_2_.xCoord + 1, p_75860_2_.yCoord, p_75860_2_.zCoord, p_75860_3_, b0);
        NPCPathPoint pathpoint3 = this.getSafePoint(p_75860_1_, p_75860_2_.xCoord, p_75860_2_.yCoord, p_75860_2_.zCoord - 1, p_75860_3_, b0);
        NPCPathPoint pathpoint4 = this.getSafePoint(p_75860_1_, p_75860_2_.xCoord, p_75860_2_.yCoord + 1, p_75860_2_.zCoord, p_75860_3_, b0);
        NPCPathPoint pathpoint5 = this.getSafePoint(p_75860_1_, p_75860_2_.xCoord, p_75860_2_.yCoord - 1, p_75860_2_.zCoord, p_75860_3_, b0);

        if (pathpoint0 != null && !pathpoint0.isFirst && pathpoint0.distanceTo(targetPoint) < maxDistance)
        {
            this.pathOptions[i++] = pathpoint0;
        }

        if (pathpoint1 != null && !pathpoint1.isFirst && pathpoint1.distanceTo(targetPoint) < maxDistance)
        {
            this.pathOptions[i++] = pathpoint1;
        }

        if (pathpoint2 != null && !pathpoint2.isFirst && pathpoint2.distanceTo(targetPoint) < maxDistance)
        {
            this.pathOptions[i++] = pathpoint2;
        }

        if (pathpoint3 != null && !pathpoint3.isFirst && pathpoint3.distanceTo(targetPoint) < maxDistance)
        {
            this.pathOptions[i++] = pathpoint3;
        }

        if (pathpoint4 != null && !pathpoint4.isFirst && pathpoint4.distanceTo(targetPoint) < maxDistance)
        {
            this.pathOptions[i++] = pathpoint4;
        }

        if (pathpoint5 != null && !pathpoint5.isFirst && pathpoint5.distanceTo(targetPoint) < maxDistance)
        {
            this.pathOptions[i++] = pathpoint5;
        }

        boolean flag = pathpoint3 != null;// && pathpoint3.costMalus != 0.0F;
        boolean flag1 = pathpoint0 != null;// && pathpoint0.costMalus != 0.0F;
        boolean flag2 = pathpoint2 != null;// && pathpoint2.costMalus != 0.0F;
        boolean flag3 = pathpoint1 != null;// && pathpoint1.costMalus != 0.0F;
        boolean flag4 = pathpoint4 != null;// && pathpoint4.costMalus != 0.0F;
        boolean flag5 = pathpoint5 != null;// && pathpoint5.costMalus != 0.0F;

        if (flag && flag3)
        {
            NPCPathPoint pathpoint6 = this.getSafePoint(p_75860_1_, p_75860_2_.xCoord - 1, p_75860_2_.yCoord, p_75860_2_.zCoord - 1, p_75860_3_, b0);

            if (pathpoint6 != null && !pathpoint6.isFirst && pathpoint6.distanceTo(targetPoint) < maxDistance)
            {
                pathOptions[i++] = pathpoint6;
            }
        }

        if (flag && flag2)
        {
            NPCPathPoint pathpoint6 = this.getSafePoint(p_75860_1_, p_75860_2_.xCoord + 1, p_75860_2_.yCoord, p_75860_2_.zCoord - 1, p_75860_3_, b0);

            if (pathpoint6 != null && !pathpoint6.isFirst && pathpoint6.distanceTo(targetPoint) < maxDistance)
            {
                pathOptions[i++] = pathpoint6;
            }
        }

        if (flag1 && flag3)
        {
            NPCPathPoint pathpoint6 = this.getSafePoint(p_75860_1_, p_75860_2_.xCoord - 1, p_75860_2_.yCoord, p_75860_2_.zCoord + 1, p_75860_3_, b0);

            if (pathpoint6 != null && !pathpoint6.isFirst && pathpoint6.distanceTo(targetPoint) < maxDistance)
            {
                pathOptions[i++] = pathpoint6;
            }
        }

        if (flag1 && flag2)
        {
            NPCPathPoint pathpoint6 = this.getSafePoint(p_75860_1_, p_75860_2_.xCoord + 1, p_75860_2_.yCoord, p_75860_2_.zCoord + 1, p_75860_3_, b0);

            if (pathpoint6 != null && !pathpoint6.isFirst && pathpoint6.distanceTo(targetPoint) < maxDistance)
            {
                pathOptions[i++] = pathpoint6;
            }
        }

        if (flag && flag4)
        {
            NPCPathPoint pathpoint6 = this.getSafePoint(p_75860_1_, p_75860_2_.xCoord, p_75860_2_.yCoord + 1, p_75860_2_.zCoord - 1, p_75860_3_, b0);

            if (pathpoint6 != null && !pathpoint6.isFirst && pathpoint6.distanceTo(targetPoint) < maxDistance)
            {
                pathOptions[i++] = pathpoint6;
            }
        }

        if (flag1 && flag4)
        {
            NPCPathPoint pathpoint6 = this.getSafePoint(p_75860_1_, p_75860_2_.xCoord, p_75860_2_.yCoord + 1, p_75860_2_.zCoord + 1, p_75860_3_, b0);

            if (pathpoint6 != null && !pathpoint6.isFirst && pathpoint6.distanceTo(targetPoint) < maxDistance)
            {
                pathOptions[i++] = pathpoint6;
            }
        }

        if (flag2 && flag4)
        {
            NPCPathPoint pathpoint6 = this.getSafePoint(p_75860_1_, p_75860_2_.xCoord + 1, p_75860_2_.yCoord + 1, p_75860_2_.zCoord, p_75860_3_, b0);

            if (pathpoint6 != null && !pathpoint6.isFirst && pathpoint6.distanceTo(targetPoint) < maxDistance)
            {
                pathOptions[i++] = pathpoint6;
            }
        }

        if (flag3 && flag4)
        {
            NPCPathPoint pathpoint6 = this.getSafePoint(p_75860_1_, p_75860_2_.xCoord - 1, p_75860_2_.yCoord + 1, p_75860_2_.zCoord, p_75860_3_, b0);

            if (pathpoint6 != null && !pathpoint6.isFirst && pathpoint6.distanceTo(targetPoint) < maxDistance)
            {
                pathOptions[i++] = pathpoint6;
            }
        }

        if (flag && flag5)
        {
            NPCPathPoint pathpoint6 = this.getSafePoint(p_75860_1_, p_75860_2_.xCoord, p_75860_2_.yCoord - 1, p_75860_2_.zCoord - 1, p_75860_3_, b0);

            if (pathpoint6 != null && !pathpoint6.isFirst && pathpoint6.distanceTo(targetPoint) < maxDistance)
            {
                pathOptions[i++] = pathpoint6;
            }
        }

        if (flag1 && flag5)
        {
            NPCPathPoint pathpoint6 = this.getSafePoint(p_75860_1_, p_75860_2_.xCoord, p_75860_2_.yCoord - 1, p_75860_2_.zCoord + 1, p_75860_3_, b0);

            if (pathpoint6 != null && !pathpoint6.isFirst && pathpoint6.distanceTo(targetPoint) < maxDistance)
            {
                pathOptions[i++] = pathpoint6;
            }
        }

        if (flag2 && flag5)
        {
            NPCPathPoint pathpoint6 = this.getSafePoint(p_75860_1_, p_75860_2_.xCoord + 1, p_75860_2_.yCoord - 1, p_75860_2_.zCoord, p_75860_3_, b0);

            if (pathpoint6 != null && !pathpoint6.isFirst && pathpoint6.distanceTo(targetPoint) < maxDistance)
            {
                pathOptions[i++] = pathpoint6;
            }
        }

        if (flag3 && flag5)
        {
            NPCPathPoint pathpoint6 = this.getSafePoint(p_75860_1_, p_75860_2_.xCoord - 1, p_75860_2_.yCoord - 1, p_75860_2_.zCoord, p_75860_3_, b0);

            if (pathpoint6 != null && !pathpoint6.isFirst && pathpoint6.distanceTo(targetPoint) < maxDistance)
            {
                pathOptions[i++] = pathpoint6;
            }
        }

        return i;
    }

    private NPCPathPoint getSafePoint(Entity p_75858_1_, int p_75858_2_, int p_75858_3_, int p_75858_4_, NPCPathPoint p_75858_5_, int p_75858_6_)
    {
        NPCPathPoint pathpoint1 = null;
        int i1 = this.getVerticalOffset(p_75858_1_, p_75858_2_, p_75858_3_, p_75858_4_, p_75858_5_);

        if (i1 == 2)
        {
            return this.openPoint(p_75858_2_, p_75858_3_, p_75858_4_);
        }
        else
        {
            if (i1 == 1)
            {
                pathpoint1 = this.openPoint(p_75858_2_, p_75858_3_, p_75858_4_);
            }

            if (pathpoint1 == null && p_75858_6_ > 0 && i1 != -3 && i1 != -4 && this.getVerticalOffset(p_75858_1_, p_75858_2_, p_75858_3_ + p_75858_6_, p_75858_4_, p_75858_5_) == 1)
            {
                pathpoint1 = this.openPoint(p_75858_2_, p_75858_3_ + p_75858_6_, p_75858_4_);
                p_75858_3_ += p_75858_6_;
            }

            if (pathpoint1 != null)
            {
                int j1 = 0;
                int k1 = 0;

                while (p_75858_3_ > 0)
                {
                    k1 = this.getVerticalOffset(p_75858_1_, p_75858_2_, p_75858_3_ - 1, p_75858_4_, p_75858_5_);

                    if (this.isPathingInWater && k1 == -1)
                    {
                        return null;
                    }

                    if (k1 != 1)
                    {
                        break;
                    }

                    if (j1++ >= p_75858_1_.getMaxSafePointTries())
                    {
                        return null;
                    }

                    --p_75858_3_;

                    if (p_75858_3_ > 0)
                    {
                        pathpoint1 = this.openPoint(p_75858_2_, p_75858_3_, p_75858_4_);
                    }
                }

                if (k1 == -2)
                {
                    return null;
                }
            }

            return pathpoint1;
        }
    }

    public int getVerticalOffset(Entity entity, int x, int y, int z, PathPoint pathEndpoint)
    {
        return getVerticalOffset(entity, x, y, z, pathEndpoint, this.isPathingInWater, this.isMovementBlockAllowed, this.isWoodenDoorAllowed);
    }

    private int getVerticalOffset(Entity entity, int x, int y, int z, PathPoint pathEndpoint, boolean isPathingInWater, boolean isMovementBlockAllowed, boolean isWoodenDoorAllowed)
    {
        PathNodeType pathnodetype = this.getPathNodeType((EntityLiving) entity, new BlockPos(x,y,z));

        switch (pathnodetype) {
            case BLOCKED:
                return 0;
            case OPEN:
                return 2;
            case WALKABLE:
                return 1;
            case TRAPDOOR:
                return -4;
            case FENCE:
                return -3;
            case LAVA:
                return -2;
            case WATER:
                return -1;
            case RAIL:
                return 2;
            case DANGER_FIRE:
                return -1;
            case DAMAGE_FIRE:
                return -2;
            case DANGER_CACTUS:
                return -1;
            case DAMAGE_CACTUS:
                return -2;
            case DANGER_OTHER:
                return -1;
            case DAMAGE_OTHER:
                return -2;
            case DOOR_OPEN:
                return 1;
            case DOOR_WOOD_CLOSED:
                return 0;
            case DOOR_IRON_CLOSED:
                return 0;
        }

        return 1;
    }

    protected NPCPathPoint openPoint(int x, int y, int z)
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
