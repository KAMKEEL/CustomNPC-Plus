package noppes.npcs.entity;

import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import noppes.npcs.ModelData;

public class EntityNpcPony extends EntityNPCInterface
{
    public boolean isPegasus = false;
    public boolean isUnicorn = false;
    public boolean isFlying = false;
    
    public ResourceLocation checked = null;
    
    public EntityNpcPony(World world)
    {
        super(world);
        display.texture = "customnpcs:textures/entity/ponies/MineLP Derpy Hooves.png";
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
			data.setEntityClass(EntityNpcPony.class);
	    	
	    	
	    	worldObj.spawnEntityInWorld(npc);
    	}
        super.onUpdate();
    }
    
}
