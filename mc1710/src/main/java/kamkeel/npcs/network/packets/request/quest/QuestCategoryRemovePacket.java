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
import noppes.npcs.CustomNpcsPermissions;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.controllers.QuestController;

import java.io.IOException;

public final class QuestCategoryRemovePacket extends AbstractPacket {
    public static String packetName = "Request|QuestCategoryRemove";

    private int categoryId;

    public QuestCategoryRemovePacket(int categoryId) {
        this.categoryId = categoryId;
    }

    public QuestCategoryRemovePacket() {
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.QuestCategoryRemove;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }

    @Override
    public CustomNpcsPermissions.Permission getPermission() {
        return CustomNpcsPermissions.GLOBAL_QUEST;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {
        out.writeInt(categoryId);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;

        if (!PacketUtil.verifyItemPacket(packetName, EnumItemPacketType.WAND, player))
            return;

        int id = in.readInt();
        QuestController.Instance.removeCategory(id);
        NoppesUtilServer.sendQuestCategoryData((EntityPlayerMP) player);
    }
}
