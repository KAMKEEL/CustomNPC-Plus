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
import kamkeel.npcs.network.packets.data.ScrollSelectedPacket;
import kamkeel.npcs.network.packets.data.large.GuiDataPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import noppes.npcs.constants.EnumRoleType;
import noppes.npcs.roles.RoleTransporter;

import java.io.IOException;

public final class TransportGetLocationPacket extends AbstractPacket {
    public static String packetName = "Request|TransportGetLocation";

    public TransportGetLocationPacket() {
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.TransportGetLocation;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }

    @Override
    public boolean needsNPC() {
        return true;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;

        if (!PacketUtil.verifyItemPacket(EnumItemPacketType.WAND, player))
            return;

        if (npc.advanced.role != EnumRoleType.Transporter)
            return;

        RoleTransporter role = (RoleTransporter) npc.roleInterface;
        if (role.hasTransport()) {
            GuiDataPacket.sendGuiData((EntityPlayerMP) player, role.getLocation().writeNBT());
            ScrollSelectedPacket.setSelectedList((EntityPlayerMP) player, role.getLocation().category.title);
        }
    }
}
