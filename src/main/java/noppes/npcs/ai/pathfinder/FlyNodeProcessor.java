package noppes.npcs.ai.pathfinder;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import noppes.npcs.entity.EntityNPCInterface;

public class FlyNodeProcessor extends NodeProcessor{

    public PathPoint getPathPointTo(Entity p_176161_1_)
    {
        return this.openPoint(MathHelper.floor_double(p_176161_1_.boundingBox.minX), MathHelper.floor_double(p_176161_1_.boundingBox.minY + 0.5D), MathHelper.floor_double(p_176161_1_.boundingBox.minZ));
    }

    public PathPoint getPathPointToCoords(Entity p_176160_1_, double p_176160_2_, double p_176160_4_, double p_176160_6_)
    {
        return this.openPoint(MathHelper.floor_double(p_176160_2_ - (double)(p_176160_1_.width / 2.0F)), MathHelper.floor_double(p_176160_4_ + 0.5D), MathHelper.floor_double(p_176160_6_ - (double)(p_176160_1_.width / 2.0F)));
    }

    public int findPathOptions(PathPoint[] p_176164_1_, Entity p_176164_2_, PathPoint p_176164_3_, PathPoint p_176164_4_, float p_176164_5_)
    {
        int i = 0;
        EnumFacing[] aenumfacing = EnumFacing.values();
        int j = aenumfacing.length;

        for (int k = 0; k < j; ++k)
        {
            EnumFacing enumfacing = aenumfacing[k];
            PathPoint pathpoint2 = this.getSafePoint(p_176164_2_, p_176164_3_.xCoord + enumfacing.getFrontOffsetX(), p_176164_3_.yCoord + enumfacing.getFrontOffsetY(), p_176164_3_.zCoord + enumfacing.getFrontOffsetZ());

            if (pathpoint2 != null && !pathpoint2.isFirst && pathpoint2.distanceTo(p_176164_4_) < p_176164_5_)
            {
                p_176164_1_[i++] = pathpoint2;
            }
        }

        return i;
    }

    private PathPoint getSafePoint(Entity p_176185_1_, int p_176185_2_, int p_176185_3_, int p_176185_4_)
    {
        int l = this.func_176186_b(p_176185_1_, p_176185_2_, p_176185_3_, p_176185_4_);
        return l == -1 ? this.openPoint(p_176185_2_, p_176185_3_, p_176185_4_) : null;
    }

    private int func_176186_b(Entity entityIn, int x, int y, int z)
    {

        for (int i = x; i < x + this.entitySizeX; ++i)
        {
            for (int j = y; j < y + this.entitySizeY; ++j)
            {
                for (int k = z; k < z + this.entitySizeZ; ++k)
                {
                    Block block = this.blockaccess.getBlock(i, j, k);
                    if(block.getMaterial() == Material.water && entityIn instanceof EntityNPCInterface && ((EntityNPCInterface)entityIn).ai.canSwim)
                    	continue;

                    if (block.getMaterial() != Material.air)
                    {
                        return 0;
                    }
                }
            }
        }

        return -1;
    }
}
