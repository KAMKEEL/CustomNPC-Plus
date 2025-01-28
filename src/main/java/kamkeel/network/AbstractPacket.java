package kamkeel.network;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.internal.FMLProxyPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayer;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;

import java.io.IOException;

public abstract class AbstractPacket {

    public FMLProxyPacket generatePacket() {
        PacketChannel packetChannel = getChannel();
        ByteBuf buf = Unpooled.buffer();
        try {
            buf.writeInt(packetChannel.getChannelType().ordinal());
            buf.writeInt(getType().ordinal());
            sendData(buf);
            return new FMLProxyPacket(buf, getChannel().getChannelName());
        } catch (Exception ignored) {}
        return null;
    }

    public abstract Enum getType();
    public abstract PacketChannel getChannel();
    public abstract void sendData(ByteBuf out) throws IOException;

    //"player" on the server side is the client who sent this packet
    //"player" on the client side is the client player
    public abstract void receiveData(ByteBuf in, EntityPlayer player) throws IOException;
}
