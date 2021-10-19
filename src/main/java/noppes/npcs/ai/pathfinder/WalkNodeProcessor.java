package noppes.npcs.ai.pathfinder;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.MathHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class WalkNodeProcessor extends NodeProcessor {
    private boolean canEnterDoors;
    private boolean canBreakDoors;
    private boolean avoidsWater;
    private boolean canSwim;
    private boolean shouldAvoidWater;

    public static int func_176170_a(IBlockAccess blockaccessIn, Entity entityIn, int x, int y, int z, int sizeX, int sizeY, int sizeZ, boolean avoidWater, boolean breakDoors, boolean enterDoors) {
        boolean flag = false;
        BlockPos blockpos = new BlockPos(entityIn);
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

        for (int i = x; i < x + sizeX; ++i) {
            for (int j = y; j < y + sizeY; ++j) {
                for (int k = z; k < z + sizeZ; ++k) {
                    blockpos$mutableblockpos.set(i, j, k);
                    Block block = blockaccessIn.getBlock(blockpos$mutableblockpos.getX(), blockpos$mutableblockpos.getY(), blockpos$mutableblockpos.getZ());

                    if (block.getMaterial() != Material.air) {
                        if (block != Blocks.trapdoor) {
                            if (block != Blocks.flowing_water && block != Blocks.water) {
                                if (!enterDoors && block instanceof BlockDoor && block.getMaterial() == Material.wood) {
                                    return 0;
                                }
                            } else {
                                if (avoidWater) {
                                    return -1;
                                }

                                flag = true;
                            }
                        } else {
                            flag = true;
                        }

                        if (entityIn.worldObj.getBlock(blockpos$mutableblockpos.getX(), blockpos$mutableblockpos.getY(), blockpos$mutableblockpos.getZ()) instanceof BlockRailBase) {
                            if (!(entityIn.worldObj.getBlock(blockpos.getX(), blockpos.getY(), blockpos.getZ()) instanceof BlockRailBase) && !(entityIn.worldObj.getBlock(blockpos.down().getX(), blockpos.down().getY(), blockpos.down().getZ()) instanceof BlockRailBase)) {
                                return -3;
                            }
                        } else if (!block.getBlocksMovement(blockaccessIn, blockpos$mutableblockpos.getX(), blockpos$mutableblockpos.getY(), blockpos$mutableblockpos.getZ()) && (!breakDoors || !(block instanceof BlockDoor) || block.getMaterial() != Material.wood)) {
                            if (block instanceof BlockFence || block instanceof BlockFenceGate || block instanceof BlockWall) {
                                return -3;
                            }

                            if (block == Blocks.trapdoor) {
                                return -4;
                            }

                            Material material = block.getMaterial();

                            if (material != Material.lava) {
                                return 0;
                            }

                            if (!entityIn.handleLavaMovement()) {
                                return -2;
                            }
                        }
                    }
                }
            }
        }

        return flag ? 2 : 1;
    }

    public void initProcessor(IBlockAccess iblockaccessIn, Entity entityIn) {
        super.initProcessor(iblockaccessIn, entityIn);
        this.shouldAvoidWater = this.avoidsWater;
    }

    /**
     * This method is called when all nodes have been processed and PathEntity is created.
     * {@link noppes.npcs.ai.pathfinder.WalkNodeProcessor WalkNodeProcessor} uses this to change its field {@link
     * noppes.npcs.ai.pathfinder.WalkNodeProcessor#avoidsWater avoidsWater}
     */
    public void postProcess() {
        super.postProcess();
        this.avoidsWater = this.shouldAvoidWater;
    }

    /**
     * Returns given entity's position as PathPoint
     */
    public PathPoint getPathPointTo(Entity entityIn) {
        int i;

        if (this.canSwim && entityIn.isInWater()) {
            i = (int) entityIn.boundingBox.minY;
            BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos(MathHelper.floor_double(entityIn.posX), i, MathHelper.floor_double(entityIn.posZ));

            for (Block block = this.blockaccess.getBlock(blockpos$mutableblockpos.getX(), blockpos$mutableblockpos.getY(), blockpos$mutableblockpos.getZ()); block == Blocks.flowing_water || block == Blocks.water; block = this.blockaccess.getBlock(blockpos$mutableblockpos.getX(), blockpos$mutableblockpos.getY(), blockpos$mutableblockpos.getZ())) {
                ++i;
                blockpos$mutableblockpos.set(MathHelper.floor_double(entityIn.posX), i, MathHelper.floor_double(entityIn.posZ));
            }

            this.avoidsWater = false;
        } else {
            i = MathHelper.floor_double(entityIn.boundingBox.minY + 0.5D);
        }

        return this.openPoint(MathHelper.floor_double(entityIn.boundingBox.minX), i, MathHelper.floor_double(entityIn.boundingBox.minZ));
    }

    /**
     * Returns PathPoint for given coordinates
     */
    public PathPoint getPathPointToCoords(Entity entityIn, double x, double y, double target) {
        return this.openPoint(MathHelper.floor_double(x - (double) (entityIn.width / 2.0F)), MathHelper.floor_double(y), MathHelper.floor_double(target - (double) (entityIn.width / 2.0F)));
    }

    public int findPathOptions(PathPoint[] pathOptions, Entity entityIn, PathPoint currentPoint, PathPoint targetPoint, float maxDistance) {
        int i = 0;
        int j = 0;

        if (this.getVerticalOffset(entityIn, currentPoint.xCoord, currentPoint.yCoord + 1, currentPoint.zCoord) == 1) {
            j = 1;
        }

        PathPoint pathpoint = this.getSafePoint(entityIn, currentPoint.xCoord, currentPoint.yCoord, currentPoint.zCoord + 1, j);
        PathPoint pathpoint1 = this.getSafePoint(entityIn, currentPoint.xCoord - 1, currentPoint.yCoord, currentPoint.zCoord, j);
        PathPoint pathpoint2 = this.getSafePoint(entityIn, currentPoint.xCoord + 1, currentPoint.yCoord, currentPoint.zCoord, j);
        PathPoint pathpoint3 = this.getSafePoint(entityIn, currentPoint.xCoord, currentPoint.yCoord, currentPoint.zCoord - 1, j);

        if (pathpoint != null && !pathpoint.isFirst && pathpoint.distanceTo(targetPoint) < maxDistance) {
            pathOptions[i++] = pathpoint;
        }

        if (pathpoint1 != null && !pathpoint1.isFirst && pathpoint1.distanceTo(targetPoint) < maxDistance) {
            pathOptions[i++] = pathpoint1;
        }

        if (pathpoint2 != null && !pathpoint2.isFirst && pathpoint2.distanceTo(targetPoint) < maxDistance) {
            pathOptions[i++] = pathpoint2;
        }

        if (pathpoint3 != null && !pathpoint3.isFirst && pathpoint3.distanceTo(targetPoint) < maxDistance) {
            pathOptions[i++] = pathpoint3;
        }

        return i;
    }

    /**
     * Returns a point that the entity can safely move to
     */
    private PathPoint getSafePoint(Entity entityIn, int x, int y, int z, int p_176171_5_) {
        PathPoint pathpoint = null;
        int i = this.getVerticalOffset(entityIn, x, y, z);

        if (i == 2) {
            return this.openPoint(x, y, z);
        } else {
            if (i == 1) {
                pathpoint = this.openPoint(x, y, z);
            }

            if (pathpoint == null && p_176171_5_ > 0 && i != -3 && i != -4 && this.getVerticalOffset(entityIn, x, y + p_176171_5_, z) == 1) {
                pathpoint = this.openPoint(x, y + p_176171_5_, z);
                y += p_176171_5_;
            }

            if (pathpoint != null) {
                int j = 0;
                int k;

                for (k = 0; y > 0; pathpoint = this.openPoint(x, y, z)) {
                    k = this.getVerticalOffset(entityIn, x, y - 1, z);

                    if (this.avoidsWater && k == -1) {
                        return null;
                    }

                    if (k != 1) {
                        break;
                    }

                    if (j++ >= entityIn.getMaxSafePointTries()) {
                        return null;
                    }

                    --y;

                    if (y <= 0) {
                        return null;
                    }
                }

                if (k == -2) {
                    return null;
                }
            }

            return pathpoint;
        }
    }

    /**
     * Checks if an entity collides with blocks at a position.
     * Returns 1 if clear, 0 for colliding with any solid block, -1 for water(if avoids water),
     * -2 for lava, -3 for fence and wall, -4 for closed trapdoor, 2 if otherwise clear except for open trapdoor or
     * water(if not avoiding)
     */
    private int getVerticalOffset(Entity entityIn, int x, int y, int z) {
        return func_176170_a(this.blockaccess, entityIn, x, y, z, this.entitySizeX, this.entitySizeY, this.entitySizeZ, this.avoidsWater, this.canBreakDoors, this.canEnterDoors);
    }

    public void setBreakDoors(boolean canBreakDoorsIn) {
        this.canBreakDoors = canBreakDoorsIn;
    }

    public boolean getEnterDoors() {
        return this.canEnterDoors;
    }

    public void setEnterDoors(boolean canEnterDoorsIn) {
        this.canEnterDoors = canEnterDoorsIn;
    }

    public boolean getCanSwim() {
        return this.canSwim;
    }

    public void setCanSwim(boolean canSwimIn) {
        this.canSwim = canSwimIn;
    }

    public boolean getAvoidsWater() {
        return this.avoidsWater;
    }

    public void setAvoidsWater(boolean avoidsWaterIn) {
        this.avoidsWater = avoidsWaterIn;
    }
}