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
import noppes.npcs.controllers.data.Quest;

import java.io.IOException;

public final class QuestRemovePacket extends AbstractPacket {
    public static String packetName = "Request|DialogRemove";

    private int questID;
    private boolean sendGroup;

    public QuestRemovePacket(int questID, boolean sendGroup) {
        this.questID = questID;
        this.sendGroup = sendGroup;
    }

    public QuestRemovePacket() {
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.QuestRemove;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }

    @Override
    public CustomNpcsPermissions.Permission getPermission() {
        return CustomNpcsPermissions.GLOBAL_DIALOG;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {
        out.writeInt(questID);
        out.writeBoolean(sendGroup);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;
        if (!PacketUtil.verifyItemPacket(packetName, EnumItemPacketType.WAND, player))
            return;

        Quest quest = QuestController.Instance.quests.get(in.readInt());
        boolean sendGroup = in.readBoolean();
        if (quest != null) {
            QuestController.Instance.removeQuest(quest);
            if (sendGroup) {
                NoppesUtilServer.sendQuestGroup((EntityPlayerMP) player, quest.category);
            } else {
                NoppesUtilServer.sendQuestData((EntityPlayerMP) player, quest.category);
            }
        }
    }
}
