// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) braces deadcode fieldsfirst

package noppes.npcs.entity.old;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.entity.data.ModelData;

// Referenced classes of package net.minecraft.src:
//            EntityAnimal, Item, EntityPlayer, InventoryPlayer,
//            ItemStack, World, NBTTagCompound

public class EntityNPCDwarfFemale extends EntityNPCInterface
{
    public EntityNPCDwarfFemale(World world)
    {
        super(world);
        scaleX = scaleZ = 0.75f;
        scaleY = 0.6275f;
        display.texture = "customnpcs:textures/entity/dwarffemale/Simone.png";
    }

    public void onUpdate(){
    	isDead = true;

    	if(!worldObj.isRemote){
	    	NBTTagCompound compound = new NBTTagCompound();

	    	writeToNBT(compound);
	    	EntityCustomNpc npc = new EntityCustomNpc(worldObj);
	    	npc.readFromNBT(compound);
	    	ModelData data = npc.modelData;
			data.breasts = 2;
			data.modelScale.legs.setScale(0.8f,1.05f);
			data.modelScale.arms.setScale(0.8f,1.05f);
			data.modelScale.body.setScale(0.8f,1.05f);
			data.modelScale.head.setScale(0.8f,0.85f);
	    	worldObj.spawnEntityInWorld(npc);
    	}
    	super.onUpdate();
    }
}
