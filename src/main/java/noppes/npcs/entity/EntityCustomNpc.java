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
        if (entity == null) {
            baseHeight = 1.9f - modelData.getBodyY() + (modelData.modelScale.head.scaleY - 1) / 2;
            super.updateHitbox();
        } else {
            if (entity instanceof EntityNPCInterface)
                ((EntityNPCInterface) entity).updateHitbox();
            float newWidth = (entity.width / 5f) * display.modelSize;
            float newHeight = (entity.height / 5f) * display.modelSize;
            if (display.hitboxData.isHitboxEnabled()) {
                newWidth = newWidth * display.hitboxData.getWidthScale();
                newHeight = newHeight * display.hitboxData.getHeightScale();
            }

            if (isKilled() && stats.hideKilledBody) {
                newWidth = 0.00001f;
            }

            newWidth = Math.max(newWidth, 0.00001f);
            newHeight = Math.max(newHeight, 0.00001f);

            if (newWidth / 2 > worldObj.MAX_ENTITY_RADIUS) {
                worldObj.MAX_ENTITY_RADIUS = newWidth / 2;
            }

            setSize(newWidth, newHeight);
            this.setPosition(posX, posY, posZ);
        }
    }

}
