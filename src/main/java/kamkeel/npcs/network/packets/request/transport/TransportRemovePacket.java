package kamkeel.npcs.network.packets.request.transport;

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
import noppes.npcs.controllers.TransportController;
import noppes.npcs.controllers.data.TransportLocation;

import java.io.IOException;

public final class TransportRemovePacket extends AbstractPacket {
    public static String packetName = "Request|TransportRemove";

    private int locationId;

    public TransportRemovePacket(int locationId) {
        this.locationId = locationId;
    }

    public TransportRemovePacket() {
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.TransportRemove;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }

    @Override
    public CustomNpcsPermissions.Permission getPermission() {
        return CustomNpcsPermissions.GLOBAL_TRANSPORT;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {
        out.writeInt(locationId);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP)) return;
        if (!PacketUtil.verifyItemPacket(EnumItemPacketType.WAND, player)) return;
        int id = in.readInt();
        TransportLocation loc = TransportController.getInstance().removeLocation(id);
        if (loc != null)
            NoppesUtilServer.sendTransportData((EntityPlayerMP) player, loc.category.id);
    }
}
