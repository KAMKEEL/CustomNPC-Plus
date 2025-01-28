package kamkeel.network.packets.client;

import io.netty.buffer.ByteBuf;
import kamkeel.network.AbstractPacket;
import kamkeel.network.PacketChannel;
import kamkeel.network.PacketHandler;
import kamkeel.network.enums.EnumClientPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.Server;
import noppes.npcs.client.gui.OverlayQuestTracking;
import noppes.npcs.client.ClientCacheHandler;

import java.io.IOException;

public final class OverlayQuestTrackingPacket extends AbstractPacket {
    public static final String packetName = "Client|OverlayQuestTracking";

    public OverlayQuestTrackingPacket() {}

    @Override
    public Enum getType() {
        return EnumClientPacket.OVERLAY_QUEST_TRACKING;
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
        try {
            NBTTagCompound nbt = Server.readNBT(in);
            ClientCacheHandler.questTrackingOverlay = new OverlayQuestTracking(Minecraft.getMinecraft());
            ClientCacheHandler.questTrackingOverlay.setOverlayData(nbt);
        } catch (IOException e) {
            ClientCacheHandler.questTrackingOverlay = null;
        }
    }
}
