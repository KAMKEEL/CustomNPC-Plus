package kamkeel.npcs.network.packets.client;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumClientPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.CustomNpcs;
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

    @SideOnly(Side.CLIENT)
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
