package noppes.npcs.ai.pathfinder;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.init.Blocks;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class PathNavigateNPC extends PathNavigate
{
    protected EntityLiving theEntity;
    protected World worldObj;
    /** The PathEntity being followed. */
    protected PathEntity currentPath;

    /** Time, in number of ticks, following the current path */
    protected int totalTicks;
    /** The time when the last position check was done (to detect successful movement) */
    protected int ticksAtLastPos;
    /** Coordinates of the entity's position last time a check was done (part of monitoring getting 'stuck') */
    protected Vec3 lastPosCheck = Vec3.createVectorHelper(0.0D, 0.0D, 0.0D);

    public PathNavigateNPC(EntityLiving p_i1671_1_, World p_i1671_2_)
    {
        super(p_i1671_1_, p_i1671_2_);
        this.theEntity = p_i1671_1_;
        this.worldObj = p_i1671_2_;
    }

    /** Gets Current Path of NPC **/
    public PathEntity getPath()
    {
        return this.currentPath;
    }


    /** Returns true if the entity is in water or lava, false otherwise **/
    protected boolean isInLiquid()
    {
        return this.theEntity.isInWater() || this.theEntity.handleLavaMovement();
    }

    /** Trims path data from the end to the first sun covered block **/
    protected void removeSunnyPath()
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
     * Checks if entity haven't been moved when last checked and if so, clears current {@link
     * net.minecraft.pathfinding.PathEntity}
     */
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
    }
}
