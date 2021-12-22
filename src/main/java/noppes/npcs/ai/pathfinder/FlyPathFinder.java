package noppes.npcs.ai.pathfinder;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.util.IntHashMap;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;

public class FlyPathFinder extends PathFinder
{
    /** Used to find obstacles */
    private IBlockAccess worldMap;
    /** The path being generated */
    private FlyPath path = new FlyPath();
    /** The points in the path */
    private IntHashMap pointMap = new IntHashMap();
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

    public FlyPathFinder(IBlockAccess p_i2137_1_, boolean p_i2137_2_, boolean p_i2137_3_, boolean p_i2137_4_, boolean p_i2137_5_)
    {
        super(p_i2137_1_,p_i2137_2_,p_i2137_3_,p_i2137_4_,p_i2137_5_);
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
    private FlyPathEntity createEntityPathTo(Entity p_75857_1_, double p_75857_2_, double p_75857_4_, double p_75857_6_, float p_75857_8_)
    {
        this.path.clearPath();
        this.pointMap.clearMap();
        boolean flag = this.isPathingInWater;
        int i = MathHelper.floor_double(p_75857_1_.boundingBox.minY + 0.5D);

        if (this.canEntityDrown && p_75857_1_.isInWater())
        {
            i = (int)p_75857_1_.boundingBox.minY;

            for (Block block = this.worldMap.getBlock(MathHelper.floor_double(p_75857_1_.posX), i, MathHelper.floor_double(p_75857_1_.posZ)); block == Blocks.flowing_water || block == Blocks.water; block = this.worldMap.getBlock(MathHelper.floor_double(p_75857_1_.posX), i, MathHelper.floor_double(p_75857_1_.posZ)))
            {
                ++i;
            }

            flag = this.isPathingInWater;
            this.isPathingInWater = false;
        }
        else
        {
            i = MathHelper.floor_double(p_75857_1_.boundingBox.minY + 0.5D);
        }

        FlyPathPoint pathpoint2 = this.openPoint(MathHelper.floor_double(p_75857_1_.boundingBox.minX), i, MathHelper.floor_double(p_75857_1_.boundingBox.minZ));
        FlyPathPoint pathpoint = this.openPoint(MathHelper.floor_double(p_75857_2_ - (double)(p_75857_1_.width / 2.0F)), MathHelper.floor_double(p_75857_4_), MathHelper.floor_double(p_75857_6_ - (double)(p_75857_1_.width / 2.0F)));
        FlyPathPoint pathpoint1 = new FlyPathPoint(MathHelper.floor_float(p_75857_1_.width + 1.0F), MathHelper.floor_float(p_75857_1_.height + 1.0F), MathHelper.floor_float(p_75857_1_.width + 1.0F));
        FlyPathEntity pathentity = this.addToPath(p_75857_1_, pathpoint2, pathpoint, pathpoint1, p_75857_8_);
        this.isPathingInWater = flag;
        return pathentity;
    }

    /**
     * Adds a path from start to end and returns the whole path (args: unused, start, end, unused, maxDistance)
     */
    private FlyPathEntity addToPath(Entity p_75861_1_, FlyPathPoint p_75861_2_, FlyPathPoint p_75861_3_, FlyPathPoint p_75861_4_, float p_75861_5_)
    {
        p_75861_2_.totalPathDistance = 0.0F;
        p_75861_2_.distanceToNext = p_75861_2_.distanceToSquared(p_75861_3_);
        p_75861_2_.distanceToTarget = p_75861_2_.distanceToNext;
        this.path.clearPath();
        this.path.addPoint(p_75861_2_);
        FlyPathPoint pathpoint3 = p_75861_2_;

        while (!this.path.isPathEmpty())
        {
            FlyPathPoint pathpoint4 = this.path.dequeue();

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
                FlyPathPoint pathpoint5 = this.pathOptions[j];
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
    private int findPathOptions(Entity p_75860_1_, FlyPathPoint p_75860_2_, FlyPathPoint p_75860_3_, FlyPathPoint p_75860_4_, float p_75860_5_)
    {
        int i = 0;
        byte b0 = 0;

        if (this.getVerticalOffset(p_75860_1_, p_75860_2_.xCoord, p_75860_2_.yCoord + 1, p_75860_2_.zCoord, p_75860_3_) == 1)
        {
            b0 = 1;
        }

        FlyPathPoint pathpoint3 = this.getSafePoint(p_75860_1_, p_75860_2_.xCoord, p_75860_2_.yCoord, p_75860_2_.zCoord + 1, p_75860_3_, b0);
        FlyPathPoint pathpoint4 = this.getSafePoint(p_75860_1_, p_75860_2_.xCoord - 1, p_75860_2_.yCoord, p_75860_2_.zCoord, p_75860_3_, b0);
        FlyPathPoint pathpoint5 = this.getSafePoint(p_75860_1_, p_75860_2_.xCoord + 1, p_75860_2_.yCoord, p_75860_2_.zCoord, p_75860_3_, b0);
        FlyPathPoint pathpoint6 = this.getSafePoint(p_75860_1_, p_75860_2_.xCoord, p_75860_2_.yCoord, p_75860_2_.zCoord - 1, p_75860_3_, b0);

        FlyPathPoint pathpoint7 = this.getSafePoint(p_75860_1_, p_75860_2_.xCoord, p_75860_2_.yCoord + 1, p_75860_2_.zCoord + 1, p_75860_3_, b0);
        FlyPathPoint pathpoint8 = this.getSafePoint(p_75860_1_, p_75860_2_.xCoord - 1, p_75860_2_.yCoord + 1, p_75860_2_.zCoord, p_75860_3_, b0);
        FlyPathPoint pathpoint9 = this.getSafePoint(p_75860_1_, p_75860_2_.xCoord + 1, p_75860_2_.yCoord + 1, p_75860_2_.zCoord, p_75860_3_, b0);
        FlyPathPoint pathpoint10 = this.getSafePoint(p_75860_1_, p_75860_2_.xCoord, p_75860_2_.yCoord + 1, p_75860_2_.zCoord - 1, p_75860_3_, b0);

        FlyPathPoint pathpoint11 = this.getSafePoint(p_75860_1_, p_75860_2_.xCoord, p_75860_2_.yCoord - 1, p_75860_2_.zCoord + 1, p_75860_3_, b0);
        FlyPathPoint pathpoint12 = this.getSafePoint(p_75860_1_, p_75860_2_.xCoord - 1, p_75860_2_.yCoord - 1, p_75860_2_.zCoord, p_75860_3_, b0);
        FlyPathPoint pathpoint13 = this.getSafePoint(p_75860_1_, p_75860_2_.xCoord + 1, p_75860_2_.yCoord - 1, p_75860_2_.zCoord, p_75860_3_, b0);
        FlyPathPoint pathpoint14 = this.getSafePoint(p_75860_1_, p_75860_2_.xCoord, p_75860_2_.yCoord - 1, p_75860_2_.zCoord - 1, p_75860_3_, b0);

        if (pathpoint3 != null && !pathpoint3.isFirst && pathpoint3.distanceTo(p_75860_4_) < p_75860_5_)
        {
            this.pathOptions[i++] = pathpoint3;
        }

        if (pathpoint4 != null && !pathpoint4.isFirst && pathpoint4.distanceTo(p_75860_4_) < p_75860_5_)
        {
            this.pathOptions[i++] = pathpoint4;
        }

        if (pathpoint5 != null && !pathpoint5.isFirst && pathpoint5.distanceTo(p_75860_4_) < p_75860_5_)
        {
            this.pathOptions[i++] = pathpoint5;
        }

        if (pathpoint6 != null && !pathpoint6.isFirst && pathpoint6.distanceTo(p_75860_4_) < p_75860_5_)
        {
            this.pathOptions[i++] = pathpoint6;
        }

        if (pathpoint7 != null && !pathpoint7.isFirst && pathpoint7.distanceTo(p_75860_4_) < p_75860_5_)
        {
            this.pathOptions[i++] = pathpoint7;
        }

        if (pathpoint8 != null && !pathpoint8.isFirst && pathpoint8.distanceTo(p_75860_4_) < p_75860_5_)
        {
            this.pathOptions[i++] = pathpoint8;
        }

        if (pathpoint9 != null && !pathpoint9.isFirst && pathpoint9.distanceTo(p_75860_4_) < p_75860_5_)
        {
            this.pathOptions[i++] = pathpoint9;
        }

        if (pathpoint10 != null && !pathpoint10.isFirst && pathpoint10.distanceTo(p_75860_4_) < p_75860_5_)
        {
            this.pathOptions[i++] = pathpoint10;
        }

        if (pathpoint11 != null && !pathpoint11.isFirst && pathpoint11.distanceTo(p_75860_4_) < p_75860_5_)
        {
            this.pathOptions[i++] = pathpoint11;
        }

        if (pathpoint12 != null && !pathpoint12.isFirst && pathpoint12.distanceTo(p_75860_4_) < p_75860_5_)
        {
            this.pathOptions[i++] = pathpoint12;
        }

        if (pathpoint13 != null && !pathpoint13.isFirst && pathpoint13.distanceTo(p_75860_4_) < p_75860_5_)
        {
            this.pathOptions[i++] = pathpoint13;
        }

        if (pathpoint14 != null && !pathpoint14.isFirst && pathpoint14.distanceTo(p_75860_4_) < p_75860_5_)
        {
            this.pathOptions[i++] = pathpoint14;
        }

        return i;
    }

    /**
     * Returns a point that the entity can safely move to
     */
    private FlyPathPoint getSafePoint(Entity entity, int blockX, int blockY, int blockZ, FlyPathPoint pathPoint, int verticalOffsetFlag)
    {
        FlyPathPoint pathpoint1 = null;
        int verticalOffset = this.getVerticalOffset(entity, blockX, blockY, blockZ, pathPoint);

        if (verticalOffset == 2)
        {
            return this.openPoint(blockX, blockY, blockZ);
        }
        else
        {
            if (verticalOffset == 1)
            {
                pathpoint1 = this.openPoint(blockX, blockY, blockZ);
            }

            if (pathpoint1 == null && verticalOffsetFlag > 0 && verticalOffset != -3 && verticalOffset != -4 && this.getVerticalOffset(entity, blockX, blockY + verticalOffsetFlag, blockZ, pathPoint) == 1)
            {
                pathpoint1 = this.openPoint(blockX, blockY + verticalOffsetFlag, blockZ);
                blockY += verticalOffsetFlag;
            }

            if (pathpoint1 != null)
            {
                int j1 = 0;
                int k1 = 0;

                while (blockY > 0)
                {
                    k1 = this.getVerticalOffset(entity, blockX, blockY - 1, blockZ, pathPoint);

                    if (this.isPathingInWater && k1 == -1)
                    {
                        return null;
                    }

                    if (k1 != 1)
                    {
                        break;
                    }

                    if (j1++ >= entity.getMaxSafePointTries())
                    {
                        return null;
                    }

                    --blockY;

                    if (blockY > 0)
                    {
                        pathpoint1 = this.openPoint(blockX, blockY, blockZ);
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

    /**
     * Returns a mapped point or creates and adds one
     */
    private final FlyPathPoint openPoint(int p_75854_1_, int p_75854_2_, int p_75854_3_)
    {
        int l = FlyPathPoint.makeHash(p_75854_1_, p_75854_2_, p_75854_3_);
        FlyPathPoint pathpoint = (FlyPathPoint)this.pointMap.lookup(l);

        if (pathpoint == null)
        {
            pathpoint = new FlyPathPoint(p_75854_1_, p_75854_2_, p_75854_3_);
            this.pointMap.addKey(l, pathpoint);
        }

        return pathpoint;
    }

    /**
     * Checks if an entity collides with blocks at a position. Returns 1 if clear, 0 for colliding with any solid block,
     * -1 for water(if avoiding water) but otherwise clear, -2 for lava, -3 for fence, -4 for closed trapdoor, 2 if
     * otherwise clear except for open trapdoor or water(if not avoiding)
     */
    public int getVerticalOffset(Entity p_75855_1_, int p_75855_2_, int p_75855_3_, int p_75855_4_, FlyPathPoint p_75855_5_)
    {
        return canEntityStandAt(p_75855_1_, p_75855_2_, p_75855_3_, p_75855_4_, p_75855_5_, this.isPathingInWater, this.isMovementBlockAllowed, this.isWoddenDoorAllowed);
    }

    public static int canEntityStandAt(Entity entity, int blockX, int blockY, int blockZ, FlyPathPoint pathPoint, boolean isPathingInWater, boolean isMovementBlockAllowed, boolean isWoddenDoorAllowed)
    {
        boolean flag3 = false;

        for (int l = blockX; l < blockX + pathPoint.xCoord; ++l)
        {
            for (int i1 = blockY; i1 < blockY + pathPoint.yCoord; ++i1)
            {
                for (int j1 = blockZ; j1 < blockZ + pathPoint.zCoord; ++j1)
                {
                    Block block = entity.worldObj.getBlock(l, i1, j1);

                    if(block == Blocks.air)
                    {
                        return 2;
                    }

                    if (block == Blocks.trapdoor)
                    {
                        flag3 = true;
                    }
                    else if (block != Blocks.flowing_water && block != Blocks.water)
                    {
                        if (!isWoddenDoorAllowed && block == Blocks.wooden_door)
                        {
                            return 0;
                        }
                    }
                    else
                    {
                        if (isPathingInWater)
                        {
                            return -1;
                        }

                        flag3 = true;
                    }

                    int k1 = block.getRenderType();

                    if (entity.worldObj.getBlock(l, i1, j1).getRenderType() == 9)
                    {
                        int j2 = MathHelper.floor_double(entity.posX);
                        int l1 = MathHelper.floor_double(entity.posY);
                        int i2 = MathHelper.floor_double(entity.posZ);

                        if (entity.worldObj.getBlock(j2, l1, i2).getRenderType() != 9 && entity.worldObj.getBlock(j2, l1 - 1, i2).getRenderType() != 9)
                        {
                            return -3;
                        }
                    }
                    else if (!block.getBlocksMovement(entity.worldObj, l, i1, j1) && (!isMovementBlockAllowed || block != Blocks.wooden_door))
                    {
                        if (k1 == 11 || block == Blocks.fence_gate || k1 == 32)
                        {
                            return -3;
                        }

                        if (block == Blocks.trapdoor)
                        {
                            return -4;
                        }

                        Material material = block.getMaterial();

                        if (material != Material.lava)
                        {
                            return 0;
                        }

                        if (!entity.handleLavaMovement())
                        {
                            return -2;
                        }
                    }
                }
            }
        }

        return flag3 ? 2 : 1;
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
}
