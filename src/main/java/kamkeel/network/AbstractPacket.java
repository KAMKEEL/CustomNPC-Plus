package kamkeel.network;

import cpw.mods.fml.common.network.internal.FMLProxyPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayer;

import java.io.IOException;

public abstract class AbstractPacket {

    public final FMLProxyPacket generatePacket() {
        ByteBuf buf = Unpooled.buffer();
        try {
            sendData(buf);
            return new FMLProxyPacket(buf, getChannel());
        } catch (Exception ignored) {}
        return null;
    }

    public abstract String getName();
    public abstract String getChannel();
    public abstract void sendData(ByteBuf out) throws IOException;

    //"player" on the server side is the client who sent this packet
    //"player" on the client side is the client player
    public abstract void receiveData(ByteBuf in, EntityPlayer player) throws IOException;
}
