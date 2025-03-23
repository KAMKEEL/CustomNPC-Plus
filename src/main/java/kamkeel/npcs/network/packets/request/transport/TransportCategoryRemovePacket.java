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

import java.io.IOException;

public final class TransportCategoryRemovePacket extends AbstractPacket {
    public static String packetName = "Request|TransportCategoryRemove";

    private int categoryId;

    public TransportCategoryRemovePacket(int categoryId) {
        this.categoryId = categoryId;
    }

    public TransportCategoryRemovePacket() {
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.TransportCategoryRemove;
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
        out.writeInt(categoryId);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;

        if (!PacketUtil.verifyItemPacket(packetName, EnumItemPacketType.WAND, player))
            return;

        int id = in.readInt();
        TransportController.getInstance().removeCategory(id);
        NoppesUtilServer.sendTransportCategoryData((EntityPlayerMP) player);
    }
}
