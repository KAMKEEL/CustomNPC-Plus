package kamkeel.npcs.network.packets.data;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumDataPacket;
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.client.NoppesUtil;

import java.io.IOException;

public final class QuestCompletionPacket extends AbstractPacket {
    public static final String packetName = "Client|QuestCompletion";

    private NBTTagCompound compound;

    public QuestCompletionPacket() {}

    public QuestCompletionPacket(NBTTagCompound compound) {
        this.compound = compound;
    }

    public static void sendQuestComplete(EntityPlayerMP playerMP, NBTTagCompound compound){
        QuestCompletionPacket packet = new QuestCompletionPacket(compound);
        PacketHandler.Instance.sendToPlayer(packet, playerMP);
    }

    @Override
    public Enum getType() {
        return EnumDataPacket.QUEST_COMPLETION;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.DATA_PACKET;
    }

    @Override
    public void sendData(ByteBuf out) throws IOException {
        ByteBufUtils.writeNBT(out, this.compound);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        NoppesUtil.guiQuestCompletion(player, ByteBufUtils.readNBT(in));
    }
}
