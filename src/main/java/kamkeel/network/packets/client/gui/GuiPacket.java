package kamkeel.network.packets.client.gui;

import io.netty.buffer.ByteBuf;
import kamkeel.network.AbstractPacket;
import kamkeel.network.PacketChannel;
import kamkeel.network.PacketHandler;
import kamkeel.network.enums.EnumClientPacket;
import net.minecraft.entity.player.EntityPlayer;
import noppes.npcs.CustomNpcs;
import noppes.npcs.constants.EnumGuiType;

import java.io.IOException;

public final class GuiPacket extends AbstractPacket {
    public static final String packetName = "Client|Gui";

    public GuiPacket() {}

    @Override
    public Enum getType() {
        return EnumClientPacket.GUI;
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
        EnumGuiType gui = EnumGuiType.values()[in.readInt()];
        int x = in.readInt();
        int y = in.readInt();
        int z = in.readInt();
        CustomNpcs.proxy.openGui(null, gui, x, y, z);
    }
}
