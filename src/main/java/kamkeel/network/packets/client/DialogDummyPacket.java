package kamkeel.network.packets.client;

import io.netty.buffer.ByteBuf;
import kamkeel.network.AbstractPacket;
import kamkeel.network.PacketChannel;
import kamkeel.network.PacketHandler;
import kamkeel.network.enums.EnumClientPacket;
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
