package kamkeel.npcs.network.packets.request.dialog;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.PacketUtil;
import kamkeel.npcs.network.enums.EnumItemPacketType;
import kamkeel.npcs.network.enums.EnumRequestPacket;
import kamkeel.npcs.network.packets.data.large.ScrollDataPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import noppes.npcs.controllers.DialogController;

import java.io.IOException;

public final class DialogCategoriesGetPacket extends AbstractPacket {
    public static String packetName = "Request|DialogCategoriesGet";

    public DialogCategoriesGetPacket() {}

    @Override
    public Enum getType() {
        return EnumRequestPacket.DialogCategoriesGet;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {}

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;

        if (!PacketUtil.verifyItemPacket(player, EnumItemPacketType.WAND, EnumItemPacketType.BLOCK))
            return;

        ScrollDataPacket.sendScrollData((EntityPlayerMP) player, DialogController.Instance.getScroll());
    }
}
