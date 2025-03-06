package kamkeel.npcs.network.packets.data.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumDataPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import noppes.npcs.client.NoppesUtil;

import java.io.IOException;

public final class IsGuiOpenPacket extends AbstractPacket {
    public static final String packetName = "Data|IsGuiOpen";

    public IsGuiOpenPacket() {
    }

    @Override
    public Enum getType() {
        return EnumDataPacket.ISGUIOPEN;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.DATA_PACKET;
    }

    @Override
    public void sendData(ByteBuf out) throws IOException {
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        boolean isGuiOpen = Minecraft.getMinecraft().currentScreen != null;
        NoppesUtil.isGUIOpen(isGuiOpen);
    }
}
