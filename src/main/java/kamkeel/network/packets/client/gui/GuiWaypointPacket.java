package kamkeel.network.packets.client.gui;

import io.netty.buffer.ByteBuf;
import kamkeel.network.AbstractPacket;
import kamkeel.network.PacketChannel;
import kamkeel.network.PacketHandler;
import kamkeel.network.enums.EnumClientPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.Server;
import noppes.npcs.NoppesUtil;

import java.io.IOException;

public final class GuiWaypointPacket extends AbstractPacket {
    public static final String packetName = "Client|GuiWaypoint";

    public GuiWaypointPacket() {}

    @Override
    public Enum getType() {
        return EnumClientPacket.GUI_WAYPOINT;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.CLIENT_PACKET;
    }

    @Override
    public void sendData(ByteBuf out) throws IOException {
        // TODO: Send Packet
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        NBTTagCompound nbt = Server.readNBT(in);
        NoppesUtil.saveWayPointBlock(player, nbt);
    }
}
