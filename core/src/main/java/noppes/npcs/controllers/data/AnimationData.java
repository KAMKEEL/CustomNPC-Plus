package noppes.npcs.controllers.data;


import noppes.npcs.constants.ClientOnly;
import kamkeel.npcs.util.IVector3;
import kamkeel.npcs.controllers.data.ability.preview.PreviewEntityHandler;
import kamkeel.npcs.controllers.data.ability.gui.SubGuiAbilityConfig;
import kamkeel.npcs.controllers.data.ability.gui.IAbilityConfigCallback;
import kamkeel.npcs.controllers.data.ability.gui.FieldDef;
import kamkeel.npcs.controllers.data.ability.gui.IChainedAbilityFieldProvider;
import kamkeel.npcs.controllers.data.ability.gui.IAbilityFieldProvider;
import noppes.npcs.entity.EntityNPCInterface;
import kamkeel.npcs.entity.EntityEnergyDome;
import kamkeel.npcs.entity.EntityEnergyBarrier;
import kamkeel.npcs.entity.EntityEnergyPanel;
import kamkeel.npcs.entity.EntityAbilityOrb;
import kamkeel.npcs.entity.EntityAbilityLaser;
import kamkeel.npcs.entity.EntityAbilityDisc;
import kamkeel.npcs.entity.EntityAbilityBeam;
import kamkeel.npcs.entity.EntityEnergyProjectile;
import kamkeel.npcs.util.ByteBufUtils;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import noppes.npcs.api.entity.IAnimatable;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IEntityLiving;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.handler.data.IAnimation;
import noppes.npcs.api.handler.data.IAnimationData;
import noppes.npcs.api.IDamageSource;
import noppes.npcs.api.INbt;
import noppes.npcs.api.INbtList;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.IWorld;
import noppes.npcs.constants.EnumAnimationPart;
import noppes.npcs.CustomNpcs;
import noppes.npcs.DataDisplay;
import noppes.npcs.EventHooks;

public class AnimationData implements IAnimationData {
    //Server-side: DataDisplay, PlayerData
    //Client-side: DataDisplay, IPlayer
    public Object parent;

    public Animation animation;
    public boolean allowAnimation = false;
    public long animatingTime = 0;

    public Animation currentClientAnimation;
    private boolean isClientAnimating;

    //Client-side values
    public int finishedTime = -1;
    public int finishedFrame = -1;

    public AnimationData(Object parent) {
        this.parent = parent;
    }

    public static AnimationData getData(IEntity IEntity) {
        if (IEntity == null || IEntity.worldObj == null) {
            return null;
        }
        if (IEntity.worldObj.isRemote) {
            return CustomNpcs.proxy.getClientAnimationData(IEntity);
        }

        if (IEntity instanceof IPlayer) {
            return PlayerData.get((IPlayer) IEntity).animationData;
        } else if (IEntity instanceof EntityNPCInterface) {
            return ((EntityNPCInterface) IEntity).display.animationData;
        } else {
            return null;
        }
    }

    public IAnimatable getEntity() {
        IEntity<?> IEntity = NpcAPI.Instance().getIEntity(this.getMCEntity());
        if (IEntity instanceof IAnimatable) {
            return (IAnimatable) IEntity;
        }
        return null;
    }

    public IEntityLivingBase getMCEntity() {
        if (this.parent instanceof DataDisplay) {
            return ((DataDisplay) this.parent).npc;
        } else {
            if (this.parent instanceof PlayerData) {
                return ((PlayerData) this.parent).player;
            } else {
                return (IPlayer) this.parent;
            }
        }
    }

    public void updateClient() {
        this.updateClient(new IPlayer[0]);
    }

    public void updateClient(IPlayer... excludedPlayers) {
        IEntityLivingBase sendingEntity = parent instanceof PlayerData ? ((PlayerData) parent).player : parent instanceof DataDisplay ? ((DataDisplay) parent).npc : null;
        float range = parent instanceof PlayerData ? 160 : 60;
        if (sendingEntity != null && sendingEntity.worldObj != null) {
            if (sendingEntity.dimension != sendingEntity.worldObj.provider.dimensionId)
                sendingEntity.dimension = sendingEntity.worldObj.provider.dimensionId;

            this.animatingTime = 0;

            boolean prevIsClientAnimating = this.isClientAnimating && this.currentClientAnimation.currentFrame() != null;
            this.isClientAnimating = this.allowAnimation && this.animation != null;
            if (prevIsClientAnimating && (!this.isClientAnimating || this.animation != this.currentClientAnimation)) {
                EventHooks.onAnimationEnded(this.currentClientAnimation);
                this.currentClientAnimation.fireEndTask();
            }
            if (this.isClientAnimating) {
                this.currentClientAnimation = this.animation;
            }

            if (this.animation != null && this.allowAnimation) {
                this.animation.fireStartTask();
                if (EventHooks.onAnimationStarted(this.animation))
                    return;
                EventHooks.onAnimationFrameEntered(this.animation, this.animation.currentFrame());
                this.animation.fireFrameTask(this.animation.currentFrame);
            }

            List<IPlayer> entities = sendingEntity.worldObj.getEntitiesWithinAABB(IPlayer.class, IBoundingBox.getBoundingBox(
                sendingEntity.posX - range, sendingEntity.posY - range, sendingEntity.posZ - range,
                sendingEntity.posX + range, sendingEntity.posY + range, sendingEntity.posZ + range));

            entities.removeIf(player -> Arrays.stream(excludedPlayers).anyMatch(exp -> player == exp));

            for (IPlayer player : entities) {
                AnimationData animationData = PlayerData.get(player).animationData;
                INbt animationNBT = this.animation != null ? this.animation.writeToNBT() : null;
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
            EntityNPCInterface npc = ((DataDisplay) this.parent).npc;
            if (!npc.isEntityAlive())
                return false;

            return animation.whileAttacking && npc.isAttacking() || animation.whileMoving && npc.isWalking() || animation.whileStanding && !npc.isWalking();
        } else {
            IPlayer player;
            if (this.parent instanceof PlayerData) {
                player = ((PlayerData) this.parent).player;
            } else {
                player = (IPlayer) this.parent;
            }
            if (!player.isEntityAlive())
                return false;

            boolean moving = Math.sqrt(player.motionX * player.motionX + player.motionY * player.motionY + player.motionZ * player.motionZ) != 0.0D;

            return animation.whileAttacking && player.getLastAttackerTime() - player.ticksExisted < 20 || animation.whileMoving && moving || animation.whileStanding && !moving;
        }
    }

    public void increaseTime() {
        Animation updateAnimation = null;
        if (this.animation != null && this.isActive(this.animation.parent.currentClientAnimation)) {
            updateAnimation = this.currentClientAnimation;
        } else {
            this.isClientAnimating = false;
            if (this.isActive()) {
                updateAnimation = this.animation;
            }
        }

        if (updateAnimation != null && updateAnimation.increaseTime()) {
            Frame frame = (Frame) updateAnimation.currentFrame();
            if (frame != null) {
                this.animatingTime++;
            }
        }
    }

    public void viewAnimation(Animation animation, AnimationData animationData, INbt animationNBT) {
        this.viewAnimation(animation, animationData, animationNBT, animationData.allowAnimation, -1, -1);
    }

    public boolean viewAnimation(Animation animation, AnimationData animationData, INbt animationNBT, boolean enabled, int currentFrame, int time) {
        if (animation != null
            && (currentFrame >= animation.frames.size()
            || currentFrame >= 0 && animation.frames.get(currentFrame).getDuration() < time)) {
            return false;
        }

        boolean prevEnabled = animationData.allowAnimation;
        animationData.allowAnimation = enabled;
        INbt data = animationData.viewWriteNBT(new INbt());
        animationData.allowAnimation = prevEnabled;

        if (animation != null && currentFrame >= 0 && currentFrame < animation.frames.size()) {
            data.setInteger("Frame", currentFrame);
            data.setInteger("Time", time);
        }

        if (animationNBT != null) {
            data.setTag("Animation", animationNBT);
        } else if (animation != null) {
            data.setTag("Animation", animation.writeToNBT());
        }

        IAnimatable animatable = animationData.getEntity();
        IEntity IEntity = ((IEntity<?>) animatable).getMCEntity();
        if (!(IEntity instanceof IPlayer)) {
            data.setInteger("EntityId", IEntity.getEntityId());
        }

        PacketHandler.Instance.sendToPlayer(new UpdateAnimationsPacket(data, IEntity.getCommandSenderName()), (IPlayer) ((PlayerData) parent).player);
        return true;
    }

    public INbt viewWriteNBT(INbt compound) {
        compound.setBoolean("AllowAnimation", allowAnimation);
        return compound;
    }

    public void viewReadFromNBT(INbt compound) {
        this.setEnabled(compound.getBoolean("AllowAnimation"));
    }

    public INbt writeToNBT(INbt compound) {
        if (this.currentClientAnimation != null) {
            compound.setBoolean("IsClientAnimating", isClientAnimating);
            if (this.isClientAnimating) {
                compound.setTag("CurrentAnimation", currentClientAnimation.writeToNBT());
            }
        }
        compound.setBoolean("AllowAnimation", allowAnimation);
        return compound;
    }

    public void readFromNBT(INbt compound) {
        IEntity IEntity = this.getMCEntity();
        boolean isServer = IEntity != null && IEntity.worldObj != null && !IEntity.worldObj.isRemote;
        if (compound.hasKey("IsClientAnimating") && isServer) {
            this.isClientAnimating = compound.getBoolean("IsClientAnimating");
            if (this.isClientAnimating) {
                this.currentClientAnimation = new Animation();
                this.currentClientAnimation.parent = this;
                this.currentClientAnimation.readFromNBT(compound.getCompoundTag("CurrentAnimation"));
                this.animation = this.currentClientAnimation;
            }
        }
        this.setEnabled(compound.getBoolean("AllowAnimation"));
    }

    public void setEnabled(boolean enabled) {
        if (this.allowAnimation != enabled) {
            this.allowAnimation = enabled;
            if (this.parent instanceof IPlayer) {
                this.finishedTime = enabled ? -1 : ((IPlayer) this.parent).getAge();
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
            newAnim.moveFromGlobalToLocal((Animation) animation);
        }

        if (this.getMCEntity() != null && this.getMCEntity().worldObj != null && this.getMCEntity().worldObj.isRemote && newAnim != null) {
            this.animatingTime = 0;
        }

        Animation prevAnim = this.animation;
        this.animation = newAnim;

        if (this.getMCEntity() != null && this.getMCEntity().worldObj != null && this.getMCEntity().worldObj.isRemote &&
            this.isActive() && prevAnim != null && newAnim != null && !newAnim.frames.isEmpty()) {
            Frame frame = (Frame) prevAnim.currentFrame();
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

    public long getAnimatingTime() {
        return this.animatingTime;
    }
}
