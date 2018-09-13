package noppes.npcs.ai;

import java.util.Random;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import noppes.npcs.constants.AiMutex;

public class EntityAIMoveIndoors extends EntityAIBase
{
    private EntityCreature theCreature;
    private double shelterX;
    private double shelterY;
    private double shelterZ;
    private World theWorld;

    public EntityAIMoveIndoors(EntityCreature par1EntityCreature)
    {
        this.theCreature = par1EntityCreature;
        this.theWorld = par1EntityCreature.worldObj;
        this.setMutexBits(AiMutex.PASSIVE);
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute()
    {
    	
    	int x = MathHelper.floor_double(this.theCreature.posX);
    	int y = (int)this.theCreature.boundingBox.minY;
    	int z = MathHelper.floor_double(this.theCreature.posZ);
    	if ((!this.theCreature.worldObj.isDaytime() || this.theCreature.worldObj.isRaining()) && !this.theCreature.worldObj.provider.hasNoSky)
        {
    		if (!this.theWorld.canBlockSeeTheSky(x, y, z) && this.theWorld.getFullBlockLightValue(x, y, z) > 8)
            {
                return false;
            }
    		else
    		{
	            Vec3 var1 = this.findPossibleShelter();
	
	            if (var1 == null)
	            {
	                return false;
	            }
	            else
	            {
	                this.shelterX = var1.xCoord;
	                this.shelterY = var1.yCoord;
	                this.shelterZ = var1.zCoord;
	                return true;
	            }
    		}
        }
    	else
    	{
    		return false;
    	}
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    public boolean continueExecuting()
    {
        return !this.theCreature.getNavigator().noPath();
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void startExecuting()
    {
        this.theCreature.getNavigator().tryMoveToXYZ(this.shelterX, this.shelterY, this.shelterZ, 1.0D);
    }

    private Vec3 findPossibleShelter()
    {
        Random var1 = this.theCreature.getRNG();

        for (int var2 = 0; var2 < 10; ++var2)
        {
            int var3 = MathHelper.floor_double(this.theCreature.posX + (double)var1.nextInt(20) - 10.0D);
            int var4 = MathHelper.floor_double(this.theCreature.boundingBox.minY + (double)var1.nextInt(6) - 3.0D);
            int var5 = MathHelper.floor_double(this.theCreature.posZ + (double)var1.nextInt(20) - 10.0D);

            if (!this.theWorld.canBlockSeeTheSky(var3, var4, var5) && this.theWorld.getFullBlockLightValue(var3, var4, var5) > 8)
            {
                return Vec3.createVectorHelper((double)var3, (double)var4, (double)var5);
            }
        }

        return null;
    }
}
