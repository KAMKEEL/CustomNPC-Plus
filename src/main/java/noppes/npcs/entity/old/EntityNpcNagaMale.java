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

public class EntityNpcNagaMale extends EntityNPCInterface {
    public EntityNpcNagaMale(World world) {
        super(world);
        display.texture = "customnpcs:textures/entity/nagamale/Cobra.png";
    }

    public void onUpdate() {
        isDead = true;

        if (!worldObj.isRemote) {
            NBTTagCompound compound = new NBTTagCompound();

            writeToNBT(compound);
            EntityCustomNpc npc = new EntityCustomNpc(worldObj);
            npc.readFromNBT(compound);
            ModelData data = npc.modelData;
            ModelPartData legs = data.legParts;
            legs.playerTexture = true;
            legs.type = 1;

            worldObj.spawnEntityInWorld(npc);
        }
        super.onUpdate();
    }
}
