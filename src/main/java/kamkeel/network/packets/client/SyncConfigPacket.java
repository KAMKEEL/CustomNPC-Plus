package kamkeel.network.packets.client;

import io.netty.buffer.ByteBuf;
import kamkeel.network.AbstractPacket;
import kamkeel.network.PacketChannel;
import kamkeel.network.PacketHandler;
import kamkeel.network.enums.EnumClientPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.Server;
import noppes.npcs.controllers.SyncController;

import java.io.IOException;

public final class SyncConfigPacket extends AbstractPacket {
    public static final String packetName = "Client|SyncConfig";

    public SyncConfigPacket() {}

    @Override
    public Enum getType() {
        return EnumClientPacket.SYNC_CONFIG;
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
        NBTTagCompound configNBT = Server.readNBT(in);
        SyncController.receiveConfigs(configNBT);
    }
}
