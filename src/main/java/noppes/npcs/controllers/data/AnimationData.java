package noppes.npcs.controllers.data;


import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import noppes.npcs.*;
import noppes.npcs.api.entity.IAnimatable;
import noppes.npcs.api.entity.ICustomNpc;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.handler.data.IAnimation;
import noppes.npcs.api.handler.data.IAnimationData;
import noppes.npcs.constants.EnumAnimationPart;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.controllers.PlayerDataController;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.scripted.NpcAPI;

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

    public Animation currentClientAnimation;
    private boolean isClientAnimating;

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

    public static AnimationData getData(Entity entity) {
        if (entity instanceof EntityPlayerMP) {
            return PlayerData.get((EntityPlayer) entity).animationData;
        } else if (entity instanceof EntityNPCInterface) {
            return ((EntityNPCInterface)entity).display.animationData;
        } else {
            return null;
        }
    }

    public IAnimatable getEntity() {
        if (this.parent instanceof DataDisplay) {
            EntityNPCInterface npc = ((DataDisplay)this.parent).npc;
            return (ICustomNpc<?>) NpcAPI.Instance().getIEntity(npc);
        } else {
            EntityPlayer player;
            if (this.parent instanceof PlayerData) {
                player = ((PlayerData) this.parent).player;
            } else {
                player = (EntityPlayer) this.parent;
            }
            return (IPlayer<?>) NpcAPI.Instance().getIEntity(player);
        }
    }

    public void updateClient() {
        this.updateClient(new EntityPlayer[0]);
    }

    public void updateClient(EntityPlayer... excludedPlayers) {
        EntityLivingBase sendingEntity = parent instanceof PlayerData ? ((PlayerData) parent).player : parent instanceof DataDisplay ? ((DataDisplay) parent).npc : null;
        float range = parent instanceof PlayerData ? 160 : 60;
        if (sendingEntity != null) {
            boolean prevIsClientAnimating = this.isClientAnimating && this.currentClientAnimation.currentFrame() != null;
            this.isClientAnimating = this.allowAnimation && this.animation != null;
            if (prevIsClientAnimating && (!this.isClientAnimating || this.animation != this.currentClientAnimation)) {
                EventHooks.onAnimationEnded(this.currentClientAnimation);
            }
            if (this.isClientAnimating) {
                this.currentClientAnimation = this.animation;
            }

            if (this.animation != null && this.allowAnimation) {
                if (EventHooks.onAnimationStarted(this.animation))
                    return;
                EventHooks.onAnimationFrameEntered(this.animation, this.animation.currentFrame());
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
                animationData.viewAnimation(this.animation, this, animationNBT);
            }
        }
    }

    public boolean isClientAnimating() {
        return this.isClientAnimating;
    }

    public boolean isActive() {
        return this.isActive(this.animation);
    }

    public boolean isActive(Animation animation) {
        if (!this.allowAnimation || animation == null || animation.currentFrame == animation.frames.size() || animation.currentFrame() == null)
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

    public void viewAnimation(Animation animation, AnimationData animationData, NBTTagCompound animationNBT) {
        this.viewAnimation(animation, animationData, animationNBT, animationData.allowAnimation, -1, -1);
    }

    public void viewAnimation(Animation animation, AnimationData animationData, NBTTagCompound animationNBT, boolean enabled, int currentFrame, int time) {
        boolean prevEnabled = animationData.allowAnimation;
        animationData.allowAnimation = enabled;
        NBTTagCompound data = animationData.viewWriteNBT(new NBTTagCompound());
        animationData.allowAnimation = prevEnabled;

        synchronized (CommonProxy.serverPlayingAnimations) {
            if (animationData.animationEntity != null && !animationData.animationEntity.worldObj.isRemote) {
                CommonProxy.serverPlayingAnimations.add(animation);
            }
        }

        if (animation != null && currentFrame >= 0 && currentFrame < animation.frames.size()) {
            data.setInteger("Frame", currentFrame);
            data.setInteger("Time", time);
        }

        if (animation != null) {
            if (this.cachedAnimationIDs.contains(animation.getID()) && !data.hasKey("Frame")) {
                data.setInteger("AnimationID", animation.getID());
            } else {
                data.setTag("Animation", animationNBT);
            }
        }

        IAnimatable animatable = animationData.getEntity();
        Entity entity = ((IEntity<?>) animatable).getMCEntity();
        if (!(entity instanceof EntityPlayer)) {
            data.setInteger("EntityId", entity.getEntityId());
        }

        Server.sendData((EntityPlayerMP) ((PlayerData) parent).player, EnumPacketClient.UPDATE_ANIMATIONS, data, entity.getCommandSenderName());
    }

    public NBTTagCompound viewWriteNBT(NBTTagCompound compound) {
        compound.setBoolean("AllowAnimation", allowAnimation);
        return compound;
    }

    public void viewReadFromNBT(NBTTagCompound compound) {
        this.setEnabled(compound.getBoolean("AllowAnimation"));
    }

    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        if (this.currentClientAnimation != null) {
            compound.setTag("CurrentAnimation", currentClientAnimation.writeToNBT());
            compound.setBoolean("IsClientAnimating", isClientAnimating);
        }
        compound.setBoolean("AllowAnimation", allowAnimation);
        return compound;
    }

    public void readFromNBT(NBTTagCompound compound) {
        if (compound.hasKey("CurrentAnimation")) {
            this.currentClientAnimation = new Animation();
            this.currentClientAnimation.parent = this;
            this.currentClientAnimation.readFromNBT(compound.getCompoundTag("CurrentAnimation"));
            this.isClientAnimating = compound.getBoolean("IsClientAnimating");
        }
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
            newAnim.currentFrame = 0;
            newAnim.currentFrameTime = 0;
            newAnim.parent = this;
        }

        synchronized (CommonProxy.clientPlayingAnimations) {
            if (this.animationEntity != null && this.animationEntity.worldObj.isRemote && newAnim != null) {
                CommonProxy.clientPlayingAnimations.add(newAnim);
            }
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
