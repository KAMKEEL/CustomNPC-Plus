package kamkeel.npcs.network.packets.player;

import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumPlayerPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.entity.EntityNPCInterface;

import java.io.IOException;

public class DialogSelectPacket extends AbstractPacket {
    public static final String packetName = "Player|DialogSelect";

    private int dialogID, optionID;

    public DialogSelectPacket(){
    }
    public DialogSelectPacket(int dialogID, int optionID) {
        this.dialogID = dialogID;
        this.optionID = optionID;
    }

    @Override
    public Enum getType() {
        return EnumPlayerPacket.DialogSelect;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.PLAYER_PACKET;
    }

    @Override
    public void sendData(ByteBuf out) throws IOException {
        out.writeInt(dialogID);
        out.writeInt(optionID);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;
        EntityPlayerMP playerMP = (EntityPlayerMP) player;
        EntityNPCInterface npc = NoppesUtilServer.getEditingNpc(playerMP);
        if(npc == null)
            return;
        NoppesUtilPlayer.dialogSelected(in.readInt(), in.readInt(), playerMP, npc);
    }
}
