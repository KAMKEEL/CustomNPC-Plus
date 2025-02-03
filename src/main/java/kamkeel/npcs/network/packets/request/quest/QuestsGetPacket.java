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
import noppes.npcs.controllers.DialogController;
import noppes.npcs.controllers.QuestController;
import noppes.npcs.controllers.data.QuestCategory;

import java.io.IOException;

public final class QuestsGetPacket extends AbstractPacket {
    public static String packetName = "Request|QuestsGet";

    private int categoryID;
    private boolean sendGroup;

    public QuestsGetPacket() {}

    public QuestsGetPacket(int categoryID, boolean send) {
        this.categoryID = categoryID;
        this.sendGroup = send;
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.QuestsGet;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {
        out.writeInt(this.categoryID);
        out.writeBoolean(this.sendGroup);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;

        if (!PacketUtil.verifyItemPacket(player, EnumItemPacketType.WAND, EnumItemPacketType.BLOCK))
            return;

        QuestCategory category = QuestController.Instance.categories.get(in.readInt());
        boolean sendGroup = in.readBoolean();
        if(sendGroup){
            NoppesUtilServer.sendQuestGroup((EntityPlayerMP) player,category);
        }
        else {
            NoppesUtilServer.sendQuestData((EntityPlayerMP) player,category);
        }
    }
}
