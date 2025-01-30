package kamkeel.npcs.network.packets.client;

import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumClientPacket;
import net.minecraft.entity.player.EntityPlayer;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.ClientCacheHandler;

import java.io.IOException;

public final class ScriptOverlayClosePacket extends AbstractPacket {
    public static final String packetName = "Client|ScriptOverlayClose";

    public ScriptOverlayClosePacket() {}

    @Override
    public Enum getType() {
        return EnumClientPacket.SCRIPT_OVERLAY_CLOSE;
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
        if(CustomNpcs.side() != Side.CLIENT)
            return;

        int overlayId = in.readInt();
        ClientCacheHandler.customOverlays.remove(overlayId);
    }
}
