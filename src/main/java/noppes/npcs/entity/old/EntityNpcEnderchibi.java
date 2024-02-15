// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) braces deadcode fieldsfirst

package noppes.npcs.entity.old;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.entity.data.ModelData;
import noppes.npcs.entity.data.ModelPartData;

// Referenced classes of package net.minecraft.src:
//            EntityAnimal, Item, EntityPlayer, InventoryPlayer,
//            ItemStack, World, NBTTagCompound

public class EntityNpcEnderchibi extends EntityNPCInterface
{
    public EntityNpcEnderchibi(World world)
    {
        super(world);
        display.texture = "customnpcs:textures/entity/enderchibi/MrEnderchibi.png";
    }
    @Override
    public void onUpdate()
    {
    	isDead = true;

    	if(!worldObj.isRemote){
	    	NBTTagCompound compound = new NBTTagCompound();

	    	writeToNBT(compound);
	    	EntityCustomNpc npc = new EntityCustomNpc(worldObj);
	    	npc.readFromNBT(compound);
	    	ModelData data = npc.modelData;
			data.modelScale.legs.setScale(0.65f,0.75f);
			data.modelScale.arms.setScale(0.50f,1.45f);
			ModelPartData part = data.getOrCreatePart("particles");
			part.playerTexture = true;

	    	worldObj.spawnEntityInWorld(npc);
    	}
        super.onUpdate();
    }

}
