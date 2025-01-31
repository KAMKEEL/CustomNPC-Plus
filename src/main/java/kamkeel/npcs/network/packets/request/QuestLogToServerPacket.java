package kamkeel.npcs.network.packets.request;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumRequestPacket;
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.CustomNpcsPermissions;
import noppes.npcs.NoppesUtilPlayer;

import java.io.IOException;

public final class QuestLogToServerPacket extends AbstractPacket {
    public static final String packetName = "Request|QuestLogToServer";

    private NBTTagCompound compound;
    private String key;

    public QuestLogToServerPacket() {}

    public QuestLogToServerPacket(NBTTagCompound compound, String key) {
        this.compound = compound;
        this.key = key;
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.QuestLogToServer;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {
        ByteBufUtils.writeNBT(out, this.compound);
        ByteBufUtils.writeString(out, this.key);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        NoppesUtilPlayer.updateQuestLogData(in, (EntityPlayerMP) player);
    }


}
