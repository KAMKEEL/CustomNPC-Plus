package noppes.npcs.entity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import noppes.npcs.ModelData;

public class EntityNpcDragon extends EntityNPCInterface{
	public EntityNpcDragon(World world)
    {
        super(world);
        field_40162_d = new double[64][3];
        field_40164_e = -1;
        field_40173_aw = 0.0F;
        field_40172_ax = 0.0F;
        field_40178_aA = 0;
		scaleX = 0.4f;
		scaleY = 0.4f;
		scaleZ = 0.4f;
		display.texture = "customnpcs:textures/entity/dragon/BlackDragon.png";

		width = 1.8f;
		height = 1.4f;
	}
    public double field_40162_d[][];
    public int field_40164_e;
    public float field_40173_aw;
    public float field_40172_ax;
    public int field_40178_aA;
    
    public boolean isFlying = false;
    
    @Override
    public double getMountedYOffset(){
    	return 1.1;
    }

    public double[] func_40160_a(int i, float f)
    {
        f = 1.0F - f;
        int j = field_40164_e - i * 1 & 0x3f;
        int k = field_40164_e - i * 1 - 1 & 0x3f;
        double ad[] = new double[3];
        double d = field_40162_d[j][0];
        double d1;
        for(d1 = field_40162_d[k][0] - d; d1 < -180D; d1 += 360D) { }
        for(; d1 >= 180D; d1 -= 360D) { }
        ad[0] = d + d1 * (double)f;
        d = field_40162_d[j][1];
        d1 = field_40162_d[k][1] - d;
        ad[1] = d + d1 * (double)f;
        ad[2] = field_40162_d[j][2] + (field_40162_d[k][2] - field_40162_d[j][2]) * (double)f;
        return ad;
    }
    public void onUpdate()
    {
    	isDead = true;

    	if(!worldObj.isRemote){
	    	NBTTagCompound compound = new NBTTagCompound();
	    	
	    	writeToNBT(compound);
	    	EntityCustomNpc npc = new EntityCustomNpc(worldObj);
	    	npc.readFromNBT(compound);
	    	ModelData data = npc.modelData;
			data.setEntityClass(EntityNpcDragon.class);
	    	
	    	
	    	worldObj.spawnEntityInWorld(npc);
    	}
        super.onUpdate();
    }
    private boolean exploded = false;
    @Override
    public void onLivingUpdate()
    {
        field_40173_aw = field_40172_ax;
        if(worldObj.isRemote && getHealth() <= 0)
        {
        	if(!exploded){
	        	exploded = true;
	            float f = (rand.nextFloat() - 0.5F) * 8F;
	            float f2 = (rand.nextFloat() - 0.5F) * 4F;
	            float f4 = (rand.nextFloat() - 0.5F) * 8F;
	            worldObj.spawnParticle("largeexplode", posX + (double)f, posY + 2D + (double)f2, posZ + (double)f4, 0.0D, 0.0D, 0.0D);
        	}
        }
        else{
	        exploded = false;
	        //func_41011_ay();
	        float f1 = 0.2F / (MathHelper.sqrt_double(motionX * motionX + motionZ * motionZ) * 10F + 1.0F);
	        f1 = 0.045f;
	        f1 *= (float)Math.pow(2D, motionY);
	        field_40172_ax += f1 * 0.5F;
        }
        super.onLivingUpdate();
    }

//    public float ticks = 0.0f;
//    public void updateRiderPosition()
//    {
//        if (riddenByEntity == null)
//        {
//        	super.updateRiderPosition();
//            return;
//        }
//        else
//        {	
//            float f6 = field_40173_aw + (field_40172_ax - field_40173_aw) * ticks;
//            float f7 = (float)(Math.sin(f6 * 3.141593F * 2.0F - 1.0F) + 1.0D);
//            riddenByEntity.setPosition(posX , posY + getMountedYOffset() + riddenByEntity.getYOffset() - f7 / 12, posZ);
//            return;
//        }
//    }
    @Override
	public void updateHitbox() {
		width = 1.8f;
		height = 1.4f;
	}

}
