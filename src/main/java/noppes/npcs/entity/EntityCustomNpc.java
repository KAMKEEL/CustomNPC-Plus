package noppes.npcs.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import noppes.npcs.CustomNpcs;
import noppes.npcs.VersionCompatibility;
import noppes.npcs.client.EntityUtil;
import noppes.npcs.entity.data.ModelData;
import noppes.npcs.entity.data.ModelPartData;

public class EntityCustomNpc extends EntityNPCFlying {
    public ModelData modelData = new ModelData();

    public EntityCustomNpc(World world) {
        super(world);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        if (compound.hasKey("NpcModelData")) {
            npcVersion = compound.getInteger("ModRev");
            VersionCompatibility.CheckModelCompatibility(this, compound);
            modelData.readFromNBT(compound.getCompoundTag("NpcModelData"));
        }
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setTag("NpcModelData", modelData.writeToNBT());
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (isRemote()) {
            ModelPartData particles = modelData.getPartData("particles");
            if (particles != null && !isKilled()) {
                CustomNpcs.proxy.spawnParticle(this, "ModelData", modelData, particles);
            }
            EntityLivingBase entity = modelData.getEntity(this);
            if (entity != null) {
                try {
                    entity.onUpdate();
                } catch (Exception e) {

                }
                EntityUtil.Copy(this, entity);
            }
        }
    }

    @Override
    public void mountEntity(Entity par1Entity) {
        super.mountEntity(par1Entity);
        updateHitbox();
    }

    @Override
    public void updateHitbox() {
        Entity entity = modelData.getEntity(this);
        if (modelData == null || entity == null) {
            baseHeight = 1.9f - modelData.getBodyY() + (modelData.modelScale.head.scaleY - 1) / 2;
            super.updateHitbox();
        } else {
            if (entity instanceof EntityNPCInterface)
                ((EntityNPCInterface) entity).updateHitbox();
            width = (entity.width / 5f) * display.modelSize;
            height = (entity.height / 5f) * display.modelSize;
            if (display.hitboxData.isHitboxEnabled()) {
                width = width * display.hitboxData.getWidthScale();
                height = height * display.hitboxData.getHeightScale();
            }

            if (width < 0.1f)
                width = 0.1f;
            if (height < 0.1f)
                height = 0.1f;
            if (isKilled() && stats.hideKilledBody) {
                width = 0.00001f;
            }
            if (width / 2 > World.MAX_ENTITY_RADIUS) {
                World.MAX_ENTITY_RADIUS = width / 2;
            }

            this.setPosition(posX, posY, posZ);
        }
    }

}
