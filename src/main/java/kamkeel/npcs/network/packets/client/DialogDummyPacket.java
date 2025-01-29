package kamkeel.npcs.network.packets.client;

import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumClientPacket;
import net.minecraft.entity.player.EntityPlayer;
import noppes.npcs.Server;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.entity.EntityDialogNpc;

import java.io.IOException;

public final class DialogDummyPacket extends AbstractPacket {
    public static final String packetName = "Client|DialogDummy";

    public DialogDummyPacket() {}

    @Override
    public Enum getType() {
        return EnumClientPacket.DIALOG_DUMMY;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.CLIENT_PACKET;
    }

    @Override
    public void sendData(ByteBuf out) throws IOException {
        // TODO: Send Packet
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        EntityDialogNpc npc = new EntityDialogNpc(player.worldObj);
        npc.display.name = Server.readString(in);
        NoppesUtil.openDialog(Server.readNBT(in), npc, player);
    }
}
