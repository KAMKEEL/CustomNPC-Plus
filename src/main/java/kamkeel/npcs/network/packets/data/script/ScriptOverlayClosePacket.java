package kamkeel.npcs.network.packets.data.script;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumDataPacket;
import net.minecraft.entity.player.EntityPlayer;
import noppes.npcs.client.ClientCacheHandler;

import java.io.IOException;

public final class ScriptOverlayClosePacket extends AbstractPacket {
    public static final String packetName = "Data|ScriptOverlayClose";

    private int particleId;

    public ScriptOverlayClosePacket() {}

    public ScriptOverlayClosePacket(int particleId) {
        this.particleId = particleId;
    }

    @Override
    public Enum getType() {
        return EnumDataPacket.SCRIPT_OVERLAY_CLOSE;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.DATA_PACKET;
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
