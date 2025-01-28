package kamkeel.network.packets.client.gui;

import io.netty.buffer.ByteBuf;
import kamkeel.network.AbstractPacket;
import kamkeel.network.PacketChannel;
import kamkeel.network.PacketHandler;
import kamkeel.network.enums.EnumClientPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import noppes.npcs.NoppesUtil;

import java.io.IOException;

public final class IsGuiOpenPacket extends AbstractPacket {
    public static final String packetName = "Client|IsGuiOpen";

    public IsGuiOpenPacket() {}

    @Override
    public Enum getType() {
        return EnumClientPacket.ISGUIOPEN;
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
        boolean isGuiOpen = Minecraft.getMinecraft().currentScreen != null;
        NoppesUtil.isGUIOpen(isGuiOpen);
    }
}
