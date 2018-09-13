// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) braces deadcode fieldsfirst 

package noppes.npcs.entity.old;

import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import noppes.npcs.ModelData;
import noppes.npcs.constants.EnumAnimation;
import noppes.npcs.entity.EntityCustomNpc;

public class EntityNPCEnderman extends EntityNpcEnderchibi
{
    public EntityNPCEnderman(World world)
    {
        super(world);
        display.texture = "customnpcs:textures/entity/enderman/enderman.png";
        display.glowTexture = "customnpcs:textures/overlays/ender_eyes.png";
        this.width = 0.6F;
        this.height = 2.9F;
    }
    
    public void updateHitbox() {
		
		if(currentAnimation == EnumAnimation.LYING){
			width = height = 0.2f;
		}
		else if (currentAnimation == EnumAnimation.SITTING){
			width = 0.6f;
			height = 2.3f;
		}
		else{
			width = 0.6f;
			height = 2.9f;
		}
		width = (width / 5f) * display.modelSize;
		height = (height / 5f) * display.modelSize;
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
			data.setEntityClass(EntityEnderman.class);
	    	
	    	worldObj.spawnEntityInWorld(npc);
    	}
        super.onUpdate();
    }
}