// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) braces deadcode fieldsfirst

package noppes.npcs.entity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import noppes.npcs.constants.EnumAnimation;
import noppes.npcs.entity.data.ModelData;

// Referenced classes of package net.minecraft.src:
//            EntityAnimal, Item, EntityPlayer, InventoryPlayer,
//            ItemStack, World, NBTTagCompound

public class EntityNPCGolem extends EntityNPCInterface {

    public EntityNPCGolem(World world) {
        super(world);
        display.texture = "customnpcs:textures/entity/golem/Iron Golem.png";

        width = 1.4f;
        height = 2.5f;
    }

    public void updateHitbox() {
        currentAnimation = EnumAnimation.values()[dataWatcher.getWatchableObjectInt(14)];
        float newWidth;
        float newHeight;
        if (currentAnimation == EnumAnimation.LYING) {
            newWidth = 0.5f;
            newHeight = 0.5f;
        } else if (currentAnimation == EnumAnimation.SITTING) {
            newWidth = 1.4f;
            newHeight = 2f;
        } else {
            newWidth = 1.4f;
            newHeight = 2.5f;
        }
        setSize(newWidth, newHeight);
        if (width / 2 > World.MAX_ENTITY_RADIUS) {
            World.MAX_ENTITY_RADIUS = width / 2;
        }
    }

    public void onUpdate() {
        isDead = true;

        if (!worldObj.isRemote) {
            NBTTagCompound compound = new NBTTagCompound();

            writeToNBT(compound);
            EntityCustomNpc npc = new EntityCustomNpc(worldObj);
            npc.readFromNBT(compound);
            ModelData data = npc.modelData;
            data.setEntityClass(EntityNPCGolem.class);


            worldObj.spawnEntityInWorld(npc);
        }
        super.onUpdate();
    }
}
