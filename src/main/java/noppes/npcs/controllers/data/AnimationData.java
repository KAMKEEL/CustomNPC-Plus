package noppes.npcs.controllers.data;


import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import noppes.npcs.CommonProxy;
import noppes.npcs.CustomNpcs;
import noppes.npcs.DataDisplay;
import noppes.npcs.Server;
import noppes.npcs.api.handler.data.IAnimation;
import noppes.npcs.api.handler.data.IAnimationData;
import noppes.npcs.client.ClientCacheHandler;
import noppes.npcs.constants.EnumAnimationPart;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.controllers.PlayerDataController;
import noppes.npcs.entity.EntityNPCInterface;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class AnimationData implements IAnimationData {
    //Server-side: DataDisplay, PlayerData
    //Client-side: DataDisplay, EntityPlayer
    public Object parent;

    public Animation animation;
    public boolean allowAnimation = false;

    private final HashSet<Integer> cachedAnimationIDs = new HashSet<>();

    //Client-side values
    public EntityLivingBase animationEntity;
    public int finishedTime = -1;
    public int finishedFrame = -1;

    public AnimationData(Object parent){
        this.parent = parent;
        if (parent instanceof DataDisplay) {
            this.animationEntity = ((DataDisplay) parent).npc;
        } else if (parent instanceof EntityPlayer) {
            this.animationEntity = (EntityLivingBase) parent;
        }
    }

    public void updateClient() {
        this.updateClient(new EntityPlayer[0]);
    }

    public void updateClient(EntityPlayer... excludedPlayers) {
        EntityLivingBase sendingEntity = parent instanceof PlayerData ? ((PlayerData) parent).player : parent instanceof DataDisplay ? ((DataDisplay) parent).npc : null;
        float range = parent instanceof PlayerData ? 160 : 60;
        if (sendingEntity != null) {
            if (!CustomNpcs.proxy.hasClient()) {
                CommonProxy.serverPlayingAnimations.add(this.animation);
            }

            List<EntityPlayer> entities = sendingEntity.worldObj.getEntitiesWithinAABB(EntityPlayer.class, AxisAlignedBB.getBoundingBox(
                sendingEntity.posX - range, sendingEntity.posY - range, sendingEntity.posZ - range,
                sendingEntity.posX + range, sendingEntity.posY + range, sendingEntity.posZ + range));

            entities.removeIf(player -> Arrays.stream(excludedPlayers).anyMatch(exp -> player == exp));

            NBTTagCompound animationNBT = null;
            for (EntityPlayer player : entities) {
                AnimationData animationData = PlayerDataController.Instance.getPlayerData(player).animationData;
                if (animationNBT == null && this.animation != null && !animationData.isCached(this.animation.getID())) {
                    animationNBT = this.animation.writeToNBT();
                }
                animationData.viewAnimation(this.animation, sendingEntity, this, animationNBT);
            }
        }
    }

    public boolean isActive() {
        if (!this.allowAnimation || this.animation == null || this.animation.currentFrame == this.animation.frames.size() || animation.currentFrame() == null)
            return false;

        if (this.parent instanceof DataDisplay) {
            EntityNPCInterface npc = ((DataDisplay)this.parent).npc;
            if (!npc.isEntityAlive())
                return false;

            return animation.whileAttacking && npc.isAttacking() || animation.whileMoving && npc.isWalking() || animation.whileStanding && !npc.isWalking();
        } else {
            EntityPlayer player;
            if (this.parent instanceof PlayerData) {
                player = ((PlayerData) this.parent).player;
            } else {
                player = (EntityPlayer) this.parent;
            }
            if (!player.isEntityAlive())
                return false;

            boolean moving = Math.sqrt(player.motionX*player.motionX + player.motionY*player.motionY + player.motionZ*player.motionZ) != 0.0D;

            return animation.whileAttacking && player.getLastAttackerTime() - player.ticksExisted < 20 || animation.whileMoving && moving || animation.whileStanding && !moving;
        }
    }

    public boolean isCached(int id) {
        return this.cachedAnimationIDs.contains(id);
    }

    public void cacheAnimation(int id) {
        this.cachedAnimationIDs.add(id);
    }

    public void uncacheAnimation(int id) {
        this.cachedAnimationIDs.remove(id);
    }

    public void clearCache() {
        this.cachedAnimationIDs.clear();
    }

    public void viewAnimation(Animation animation, EntityLivingBase entity, AnimationData animationData, NBTTagCompound animationNBT) {
        NBTTagCompound data = animationData.writeToNBT(new NBTTagCompound());
        if (animation != null) {
            if (this.cachedAnimationIDs.contains(animation.getID())) {
                data.setInteger("AnimationID", animation.getID());
            } else {
                data.setTag("Animation", animationNBT);
            }
        }
        if (!(entity instanceof EntityPlayer)) {
            data.setInteger("EntityId", entity.getEntityId());
        }
        Server.sendData((EntityPlayerMP) ((PlayerData) parent).player, EnumPacketClient.UPDATE_ANIMATIONS, data, entity.getCommandSenderName());
    }

    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setBoolean("AllowAnimation", allowAnimation);
        return compound;
    }

    public void readFromNBT(NBTTagCompound compound) {
        this.setEnabled(compound.getBoolean("AllowAnimation"));
    }

    public void setEnabled(boolean enabled) {
        if (this.allowAnimation != enabled) {
            this.allowAnimation = enabled;
            if (this.parent instanceof EntityPlayer) {
                this.finishedTime = enabled ? -1 : ((EntityPlayer) this.parent).getAge();
            }
        }
    }

    public boolean enabled() {
        return this.allowAnimation;
    }

    public void setAnimation(IAnimation animation) {
        Animation newAnim = null;
        if (animation != null) {
            newAnim = new Animation();
            newAnim.readFromNBT(((Animation) animation).writeToNBT());
            newAnim.parent = this;
        }

        if (CustomNpcs.proxy.hasClient() && newAnim != null) {
            CommonProxy.clientPlayingAnimations.remove(this.animation);
            CommonProxy.clientPlayingAnimations.add(newAnim);
        }
        this.animation = newAnim;

        if (this.isActive() && newAnim != null && !newAnim.frames.isEmpty()) {
            Frame frame = (Frame) this.animation.currentFrame();
            if (frame != null) {
                Frame firstFrame = newAnim.frames.get(0);
                for (Map.Entry<EnumAnimationPart, FramePart> entry : frame.frameParts.entrySet()) {
                    if (firstFrame.frameParts.containsKey(entry.getKey())) {
                        FramePart prevFramePart = entry.getValue();
                        FramePart newFramePart = firstFrame.frameParts.get(entry.getKey());
                        for (int i = 0; i < 3; i++) {
                            newFramePart.prevPivots[i] = prevFramePart.prevPivots[i];
                            newFramePart.prevRotations[i] = prevFramePart.prevRotations[i];
                        }
                    }
                }
            }
        }
    }

    public IAnimation getAnimation() {
        return animation;
    }
}
