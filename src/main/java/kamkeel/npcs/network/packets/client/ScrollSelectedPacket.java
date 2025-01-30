package kamkeel.npcs.network.packets.client;

import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumClientPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import noppes.npcs.CustomNpcs;
import noppes.npcs.Server;
import noppes.npcs.client.gui.util.IScrollData;

import java.io.IOException;

public final class ScrollSelectedPacket extends AbstractPacket {
    public static final String packetName = "Client|ScrollSelected";

    public ScrollSelectedPacket() {}

    @Override
    public Enum getType() {
        return EnumClientPacket.SCROLL_SELECTED;
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
        if(CustomNpcs.side() != Side.CLIENT)
            return;

        GuiScreen gui = Minecraft.getMinecraft().currentScreen;
        if (gui instanceof IScrollData) {
            String selected = Server.readString(in);
            ((IScrollData) gui).setSelected(selected);
        }
    }
}
