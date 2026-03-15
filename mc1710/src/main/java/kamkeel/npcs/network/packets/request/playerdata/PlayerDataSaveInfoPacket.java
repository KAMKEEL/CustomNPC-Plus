package kamkeel.npcs.network.packets.request.playerdata;

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
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.constants.EnumPlayerData;

import java.io.IOException;

public final class PlayerDataSaveInfoPacket extends AbstractPacket {
    public static final String packetName = "Request|PlayerDataSave";

    private String playerName;
    private EnumPlayerData tabType;
    private NBTTagCompound value;

    public PlayerDataSaveInfoPacket() {
    }

    public PlayerDataSaveInfoPacket(String playerName, EnumPlayerData tabType, NBTTagCompound compound) {
        this.playerName = playerName;
        this.tabType = tabType;
        this.value = compound;
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.PlayerDataSave;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }

    @Override
    public CustomNpcsPermissions.Permission getPermission() {
        return CustomNpcsPermissions.GLOBAL_PLAYERDATA;
    }


    @Override
    public void sendData(ByteBuf out) throws IOException {
        ByteBufUtils.writeString(out, playerName);
        out.writeInt(tabType.ordinal());
        ByteBufUtils.writeNBT(out, this.value);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;
        String playerName = ByteBufUtils.readString(in);
        int tabType = in.readInt();
        NBTTagCompound value = ByteBufUtils.readNBT(in);
        NoppesUtilServer.savePlayerDataInfo(playerName, tabType, value, (EntityPlayerMP) player);
    }
}
