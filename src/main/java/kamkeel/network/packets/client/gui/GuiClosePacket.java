package kamkeel.network.packets.client.gui;

import io.netty.buffer.ByteBuf;
import kamkeel.network.AbstractPacket;
import kamkeel.network.PacketChannel;
import kamkeel.network.PacketHandler;
import kamkeel.network.enums.EnumClientPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.Server;
import noppes.npcs.client.gui.util.IGuiClose;

import java.io.IOException;

public final class GuiClosePacket extends AbstractPacket {
    public static final String packetName = "Client|GuiClose";

    public GuiClosePacket() {}

    @Override
    public Enum getType() {
        return EnumClientPacket.GUI_CLOSE;
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
        GuiScreen gui = Minecraft.getMinecraft().currentScreen;
        if (gui instanceof IGuiClose) {
            int closeCode = in.readInt();
            NBTTagCompound nbt = Server.readNBT(in);
            ((IGuiClose) gui).setClose(closeCode, nbt);
        }
        Minecraft.getMinecraft().displayGuiScreen(null);
        Minecraft.getMinecraft().setIngameFocus();
    }
}
