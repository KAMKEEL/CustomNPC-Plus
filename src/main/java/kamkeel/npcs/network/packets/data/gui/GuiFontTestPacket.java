package kamkeel.npcs.network.packets.data.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumDataPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import noppes.npcs.client.gui.GuiFontTest;

import java.io.IOException;

public final class GuiFontTestPacket extends AbstractPacket {

    public static void open(EntityPlayerMP player) {
        PacketHandler.Instance.sendToPlayer(new GuiFontTestPacket(), player);
    }

    @Override
    public Enum getType() {
        return EnumDataPacket.GUI_FONT_TEST;
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
        GuiFontTest.open();
    }
}
