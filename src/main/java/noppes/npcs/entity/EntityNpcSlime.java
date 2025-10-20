package noppes.npcs.entity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import noppes.npcs.entity.data.ModelData;

public class EntityNpcSlime extends EntityNPCInterface {
    public EntityNpcSlime(World world) {
        super(world);
        scaleX = 2f;
        scaleY = 2f;
        scaleZ = 2f;
        display.texture = "customnpcs:textures/entity/slime/Slime.png";
        width = 0.8f;
        height = 0.8f;
    }

    @Override
    public void updateHitbox() {
        setSize(0.8f, 0.8f);
        if (width / 2 > World.MAX_ENTITY_RADIUS) {
            World.MAX_ENTITY_RADIUS = width / 2;
        }
    }

    @Override
    public void onUpdate() {
        isDead = true;

        if (!worldObj.isRemote) {
            NBTTagCompound compound = new NBTTagCompound();

            writeToNBT(compound);
            EntityCustomNpc npc = new EntityCustomNpc(worldObj);
            npc.readFromNBT(compound);
            ModelData data = npc.modelData;
            data.setEntityClass(EntityNpcSlime.class);


            worldObj.spawnEntityInWorld(npc);
        }
        super.onUpdate();
    }
}
