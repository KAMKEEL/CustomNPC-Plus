package noppes.npcs;


import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.api.handler.data.IAnimationData;
import noppes.npcs.api.handler.data.IAnimation;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.controllers.data.Animation;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.entity.EntityNPCInterface;

public class AnimationData implements IAnimationData {
    //Server-side: DataDisplay, PlayerData
    //Client-side: DataDisplay, EntityPlayer
    public Object parent;

    public Animation animation;
    public boolean allowAnimation = false;

    public AnimationData(Object parent){
        this.parent = parent;
    }

    public void updateClient() {
        NBTTagCompound compound = new NBTTagCompound();
        compound = this.writeToNBT(compound);
        compound.setTag("Animation",this.animation.writeToNBT());
        if (parent instanceof PlayerData) {
            Server.sendToAll(EnumPacketClient.UPDATE_ANIMATIONS, compound, ((PlayerData) parent).player.getCommandSenderName());
        } else if (parent instanceof DataDisplay) {
            compound.setInteger("EntityId",((DataDisplay) parent).npc.getEntityId());
            Server.sendAssociatedData(((DataDisplay) parent).npc, EnumPacketClient.UPDATE_ANIMATIONS, compound);
        }
    }

    public boolean isActive() {
        if (!this.allowAnimation || this.animation == null || this.animation.currentFrame == this.animation.frames.size())
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

    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setBoolean("PuppetEnabled", allowAnimation);
        return compound;
    }

    public void readFromNBT(NBTTagCompound compound) {
        this.allowAnimation = compound.getBoolean("PuppetEnabled");
    }

    public void setEnabled(boolean enabled) {
        this.allowAnimation = enabled;
    }

    public boolean enabled() {
        return this.allowAnimation;
    }

    public void setAnimation(IAnimation animation) {
        this.animation = (Animation) animation;
    }

    public IAnimation getAnimation() {
        return animation;
    }
}
