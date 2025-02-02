package kamkeel.npcs.network.packets.player;

import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumPlayerPacket;
import kamkeel.npcs.network.packets.data.large.GuiDataPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.constants.EnumJobType;
import noppes.npcs.entity.EntityNPCInterface;

import java.io.IOException;

public class FollowerPacket extends AbstractPacket {
    public static final String packetName = "Player|Follower";

    private final Action type;

    public FollowerPacket(Action action) {
        this.type = action;
    }

    @Override
    public Enum getType() {
        return EnumPlayerPacket.FollowerAction;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.PLAYER_PACKET;
    }

    @Override
    public void sendData(ByteBuf out) throws IOException {
        out.writeInt(type.ordinal());
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        Action requestedAction = Action.values()[in.readInt()];
        EntityNPCInterface npc = NoppesUtilServer.getEditingNpc(player);

        if (npc == null || npc.advanced.job != EnumJobType.Follower)
            return;

        EntityPlayerMP entityPlayerMP = (EntityPlayerMP) player;

        switch (requestedAction) {
            case Hire:
                NoppesUtilPlayer.hireFollower(entityPlayerMP, npc);
                break;
            case Extend:
                NoppesUtilPlayer.extendFollower(entityPlayerMP, npc);
                GuiDataPacket.sendGuiData(entityPlayerMP, npc.roleInterface.writeToNBT(new NBTTagCompound()));
                break;
            case State:
                NoppesUtilPlayer.changeFollowerState(entityPlayerMP, npc);
                GuiDataPacket.sendGuiData(entityPlayerMP, npc.roleInterface.writeToNBT(new NBTTagCompound()));
                break;
        }

    }

    public enum Action {
        Hire,
        Extend,
        State
    }
}
