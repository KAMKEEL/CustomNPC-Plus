package noppes.npcs;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.entity.EntityNPCInterface;

public class AnimationData extends AnimationDataShared {
    public EntityNPCInterface npc;

    public boolean whileStanding = true;
    public boolean whileAttacking = false;
    public boolean whileMoving = false;

    public AnimationData(EntityNPCInterface npc, Object parent){
        super(null);
        this.npc = npc;
    }

    // CHANGE to CHECK IF PARENT IS NPC LOUIS
    @Override
    public void updateClient() {
        Server.sendToAll(EnumPacketClient.PLAYER_UPDATE_MODEL_DATA, ((PlayerData) parent).player.getCommandSenderName(), this.writeToNBT(new NBTTagCompound()));
    }

    public boolean isActive() {
        if (!this.allowAnimation)
            return false;

        if(!npc.isEntityAlive())
            return false;

        if(whileAttacking && npc.isAttacking() || whileMoving && npc.isWalking() || whileStanding && !npc.isWalking())
            return true;

        return false;
    }

}
