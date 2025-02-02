package kamkeel.npcs.network.packets.request.quest;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.*;
import kamkeel.npcs.network.enums.EnumItemPacketType;
import kamkeel.npcs.network.enums.EnumRequestPacket;
import kamkeel.npcs.network.packets.data.large.GuiDataPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.controllers.DialogController;
import noppes.npcs.controllers.QuestController;
import noppes.npcs.controllers.data.Dialog;
import noppes.npcs.controllers.data.Quest;

import java.io.IOException;

public final class QuestGetPacket extends AbstractPacket {
    public static String packetName = "Request|QuestGet";

    private int questID;

    public QuestGetPacket() {}

    public QuestGetPacket(int questID) {
        this.questID = questID;
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.QuestGet;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {
        out.writeInt(this.questID);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;

        if (!PacketUtil.verifyItemPacket(player, EnumItemPacketType.WAND, EnumItemPacketType.BLOCK))
            return;

        Quest quest = QuestController.Instance.quests.get(in.readInt());
        if(quest != null){
            NBTTagCompound compound = new NBTTagCompound();
            if(quest.hasNewQuest())
                compound.setString("NextQuestTitle", quest.getNextQuest().title);
            GuiDataPacket.sendGuiData((EntityPlayerMP) player, quest.writeToNBT(compound));
        }
    }

    public static void getQuest(int id){
        PacketClient.sendClient(new QuestGetPacket(id));
    }
}
