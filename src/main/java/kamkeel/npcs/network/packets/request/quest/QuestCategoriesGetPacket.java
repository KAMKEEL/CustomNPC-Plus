package kamkeel.npcs.network.packets.request.quest;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.PacketUtil;
import kamkeel.npcs.network.enums.EnumItemPacketType;
import kamkeel.npcs.network.enums.EnumRequestPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import noppes.npcs.NoppesUtilServer;

import java.io.IOException;

public final class QuestCategoriesGetPacket extends AbstractPacket {
    public static String packetName = "Request|QuestCategoriesGet";

    public QuestCategoriesGetPacket() {}

    @Override
    public Enum getType() {
        return EnumRequestPacket.QuestCategoriesGet;
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

        NoppesUtilServer.sendQuestCategoryData((EntityPlayerMP) player);
    }
}
