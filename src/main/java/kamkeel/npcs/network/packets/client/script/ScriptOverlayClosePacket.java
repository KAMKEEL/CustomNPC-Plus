package kamkeel.npcs.network.packets.client.script;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumClientPacket;
import net.minecraft.entity.player.EntityPlayer;
import noppes.npcs.client.ClientCacheHandler;

import java.io.IOException;

public final class ScriptOverlayClosePacket extends AbstractPacket {
    public static final String packetName = "Client|ScriptOverlayClose";

    private int particleId;

    public ScriptOverlayClosePacket() {}

    public ScriptOverlayClosePacket(int particleId) {
        this.particleId = particleId;
    }

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
        out.writeInt(this.particleId);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        int overlayId = in.readInt();
        ClientCacheHandler.customOverlays.remove(overlayId);
    }
}
