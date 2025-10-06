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
import noppes.npcs.CustomNpcsPermissions;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.constants.EnumPlayerData;

import java.io.IOException;

public final class PlayerDataRemovePacket extends AbstractPacket {
    public static String packetName = "Request|PlayerDataRemove";

    private EnumPlayerData playerData;
    private String name;

    public PlayerDataRemovePacket() {
    }

    public PlayerDataRemovePacket(EnumPlayerData playerData, String name) {
        this.playerData = playerData;
        this.name = name;
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.PlayerDataRemove;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }

    @Override
    public CustomNpcsPermissions.Permission getPermission() {
        return CustomNpcsPermissions.GLOBAL_PLAYERDATA;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {
        out.writeInt(playerData.ordinal());
        ByteBufUtils.writeString(out, this.name);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;

        if (!PacketUtil.verifyItemPacket(packetName, EnumItemPacketType.WAND, player))
            return;

        NoppesUtilServer.removePlayerData(in, (EntityPlayerMP) player);
    }
}
