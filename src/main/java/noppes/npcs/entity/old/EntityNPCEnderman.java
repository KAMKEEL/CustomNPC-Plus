// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) braces deadcode fieldsfirst

package noppes.npcs.entity.old;

import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import noppes.npcs.constants.EnumAnimation;
import noppes.npcs.controllers.data.SkinOverlay;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.data.ModelData;

public class EntityNPCEnderman extends EntityNpcEnderchibi {
    public EntityNPCEnderman(World world) {
        super(world);
        display.texture = "customnpcs:textures/entity/enderman/enderman.png";
        //display.glowTexture = ;
        display.skinOverlayData.overlayList.put(0, new SkinOverlay("customnpcs:textures/overlays/ender_eyes.png"));
        this.width = 0.6F;
        this.height = 2.9F;
    }

    public void updateHitbox() {

        float newWidth;
        float newHeight;
        if (currentAnimation == EnumAnimation.LYING) {
            newWidth = 0.2f;
            newHeight = 0.2f;
        } else if (currentAnimation == EnumAnimation.SITTING) {
            newWidth = 0.6f;
            newHeight = 2.3f;
        } else {
            newWidth = 0.6f;
            newHeight = 2.9f;
        }
        newWidth = (newWidth / 5f) * display.modelSize;
        newHeight = (newHeight / 5f) * display.modelSize;
        newWidth = Math.max(newWidth, 0.00001f);
        newHeight = Math.max(newHeight, 0.00001f);
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
            data.setEntityClass(EntityEnderman.class);

            worldObj.spawnEntityInWorld(npc);
        }
        super.onUpdate();
    }
}
