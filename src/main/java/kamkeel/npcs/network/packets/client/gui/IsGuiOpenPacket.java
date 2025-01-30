package kamkeel.npcs.network.packets.client.gui;

import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumClientPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.NoppesUtil;

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
        if(CustomNpcs.side() != Side.CLIENT)
            return;

        boolean isGuiOpen = Minecraft.getMinecraft().currentScreen != null;
        NoppesUtil.isGUIOpen(isGuiOpen);
    }
}
