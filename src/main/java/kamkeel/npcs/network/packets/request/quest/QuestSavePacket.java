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
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.CustomNpcsPermissions;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.controllers.QuestController;
import noppes.npcs.controllers.data.Quest;

import java.io.IOException;

public final class QuestSavePacket extends AbstractPacket {
    public static String packetName = "Request|QuestSave";

    private int categoryId;
    private NBTTagCompound questNBT;
    private boolean sendGroup;

    public QuestSavePacket(int categoryId, NBTTagCompound questNBT, boolean sendGroup) {
        this.categoryId = categoryId;
        this.questNBT = questNBT;
        this.sendGroup = sendGroup;
    }

    public QuestSavePacket() {
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.QuestSave;
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
        ByteBufUtils.writeNBT(out, questNBT);
        out.writeBoolean(sendGroup);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;

        if (!PacketUtil.verifyItemPacket(EnumItemPacketType.WAND, player))
            return;

        int cat = in.readInt();
        NBTTagCompound compound = ByteBufUtils.readNBT(in);
        boolean sendGroup = in.readBoolean();
        Quest quest = new Quest();
        quest.readNBT(compound);
        QuestController.Instance.saveQuest(cat, quest);
        if (quest.category != null) {
            if (sendGroup) {
                NoppesUtilServer.sendQuestGroup((EntityPlayerMP) player, quest.category);
            } else {
                NoppesUtilServer.sendQuestData((EntityPlayerMP) player, quest.category);
            }
        }
    }
}
