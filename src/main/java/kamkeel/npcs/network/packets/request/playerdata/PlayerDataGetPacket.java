package kamkeel.npcs.network.packets.request.playerdata;

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
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.constants.EnumPlayerData;

import java.io.IOException;

public final class PlayerDataGetPacket extends AbstractPacket {
    public static String packetName = "Request|PlayerDataGet";

    private EnumPlayerData playerData;
    private String name;

    public PlayerDataGetPacket() {}

    public PlayerDataGetPacket(EnumPlayerData playerData, String name) {
        this.playerData = playerData;
        this.name = name;
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.PlayerDataGet;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {
        out.writeInt(playerData.ordinal());
        if(playerData != EnumPlayerData.Players)
            ByteBufUtils.writeString(out, this.name);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;

        if (!PacketUtil.verifyItemPacket(EnumItemPacketType.WAND, player))
            return;

        int id = in.readInt();
        if (EnumPlayerData.values().length <= id)
            return;

        String name = null;
        EnumPlayerData datatype = EnumPlayerData.values()[id];
        if (datatype != EnumPlayerData.Players)
            name = ByteBufUtils.readString(in);

        NoppesUtilServer.sendPlayerData(datatype, (EntityPlayerMP) player, name);
    }
}
