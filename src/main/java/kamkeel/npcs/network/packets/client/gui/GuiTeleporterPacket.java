package kamkeel.npcs.network.packets.client.gui;

import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumClientPacket;
import net.minecraft.entity.player.EntityPlayer;
import noppes.npcs.CustomNpcs;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.entity.EntityNPCInterface;

import java.io.IOException;

public final class GuiTeleporterPacket extends AbstractPacket {
    public static final String packetName = "Client|Teleporter";

    public GuiTeleporterPacket(){}

    @Override
    public Enum getType() {
        return EnumClientPacket.TELEPORTER;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.CLIENT_PACKET;
    }

    @Override
    public void sendData(ByteBuf out) throws IOException {}

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        CustomNpcs.proxy.openGui((EntityNPCInterface)null, EnumGuiType.NpcDimensions);
    }
}
