// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) braces deadcode fieldsfirst 

package noppes.npcs.entity.old;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import noppes.npcs.ModelData;
import noppes.npcs.ModelPartData;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;

// Referenced classes of package net.minecraft.src:
//            EntityAnimal, Item, EntityPlayer, InventoryPlayer, 
//            ItemStack, World, NBTTagCompound

public class EntityNpcNagaFemale extends EntityNPCInterface
{
    public EntityNpcNagaFemale(World world)
    {
        super(world);
        scaleX = scaleY = scaleZ = 0.9075f;
        display.texture = "customnpcs:textures/entity/nagafemale/Claire.png";
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
			data.breasts = 2;
			data.head.setScale(0.95f,0.95f);
			data.legs.setScale(0.92f,0.92f);
			data.arms.setScale(0.80f,0.92f);
			data.body.setScale(0.92f, 0.92f);
	    	ModelPartData legs = data.legParts;
	    	legs.playerTexture = true;
	    	legs.type = 1;
	    	
	    	worldObj.spawnEntityInWorld(npc);
    	}
        super.onUpdate();
    }
}
