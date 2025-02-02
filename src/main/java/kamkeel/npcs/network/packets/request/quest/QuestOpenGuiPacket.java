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
import noppes.npcs.CustomNpcs;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.controllers.data.Quest;

import java.io.IOException;

public final class QuestOpenGuiPacket extends AbstractPacket {
    public static String packetName = "Request|QuestOpenGui";

    private int guiId;
    private NBTTagCompound questNBT;

    public QuestOpenGuiPacket(int guiId, NBTTagCompound questNBT) {
        this.guiId = guiId;
        this.questNBT = questNBT;
    }

    public QuestOpenGuiPacket() {
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.QuestOpenGui;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {
        out.writeInt(guiId);
        ByteBufUtils.writeNBT(out, questNBT);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;
        if (!PacketUtil.verifyItemPacket(EnumItemPacketType.WAND, player))
            return;

        int gui = in.readInt();
        NBTTagCompound compound = ByteBufUtils.readNBT(in);
        Quest quest = new Quest();
        quest.readNBT(compound);
        NoppesUtilServer.setEditingQuest(player, quest);
        player.openGui(CustomNpcs.instance, gui, player.worldObj, 0, 0, 0);
    }
}
