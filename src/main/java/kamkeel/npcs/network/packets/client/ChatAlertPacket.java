package kamkeel.npcs.network.packets.client;

import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumClientPacket;
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.Server;
import noppes.npcs.config.ConfigClient;

import java.io.IOException;

public final class ChatAlertPacket extends AbstractPacket {
    public static final String packetName = "Client|ChatAlert";

    private Object[] objects;

    public ChatAlertPacket() {}

    public ChatAlertPacket(final Object... obs) {
        this.objects = obs;
    }

    @Override
    public Enum getType() {
        return EnumClientPacket.CHAT_ALERT;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.CLIENT_PACKET;
    }

    @Override
    public void sendData(ByteBuf out) throws IOException {
        ByteBufUtils.fillBuffer(out, this.objects);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if(!ConfigClient.ChatAlerts)
            return;

        if(CustomNpcs.side() != Side.CLIENT)
            return;

        StringBuilder message = new StringBuilder();
        String str;
        while ((str = Server.readString(in)) != null && !str.isEmpty()) {
            message.append(str);
        }
        player.addChatMessage(new ChatComponentTranslation(message.toString()));
    }
}
