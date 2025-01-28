package kamkeel.network.packets.client;

import io.netty.buffer.ByteBuf;
import kamkeel.network.AbstractPacket;
import kamkeel.network.PacketChannel;
import kamkeel.network.PacketHandler;
import kamkeel.network.enums.EnumClientPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import noppes.npcs.Server;
import noppes.npcs.client.gui.customoverlay.OverlayCustom;
import noppes.npcs.client.ClientCacheHandler;

import java.io.IOException;

public final class ScriptOverlayDataPacket extends AbstractPacket {
    public static final String packetName = "Client|ScriptOverlayData";

    public ScriptOverlayDataPacket() {}

    @Override
    public Enum getType() {
        return EnumClientPacket.SCRIPT_OVERLAY_DATA;
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
        OverlayCustom overlayCustom = new OverlayCustom(Minecraft.getMinecraft());
        overlayCustom.setOverlayData(Server.readNBT(in));
        ClientCacheHandler.customOverlays.put(overlayCustom.overlay.getID(), overlayCustom);
    }
}
