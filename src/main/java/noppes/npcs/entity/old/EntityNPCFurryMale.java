// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) braces deadcode fieldsfirst 

package noppes.npcs.entity.old;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import noppes.npcs.ModelData;
import noppes.npcs.ModelPartData;
import noppes.npcs.client.controllers.Preset;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;

// Referenced classes of package net.minecraft.src:
//            EntityAnimal, Item, EntityPlayer, InventoryPlayer, 
//            ItemStack, World, NBTTagCompound

public class EntityNPCFurryMale extends EntityNPCInterface
{
    public EntityNPCFurryMale(World world)
    {
        super(world);
        display.texture = "customnpcs:textures/entity/furrymale/WolfGrey.png";
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
	    	ModelPartData hair = data.getOrCreatePart("ears");
			hair.playerTexture = true;
			ModelPartData snout = data.getOrCreatePart("snout");
			snout.playerTexture = true;
			snout.type = 1;
			ModelPartData tail = data.getOrCreatePart("tail");
			tail.playerTexture = true;
	    	
	    	
	    	worldObj.spawnEntityInWorld(npc);
    	}
        super.onUpdate();
    }

}
