package kamkeel.npcs.network.packets.data;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumDataPacket;
import kamkeel.npcs.util.ByteBufUtils;
import kamkeel.npcs.util.ColorUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.StatCollector;
import noppes.npcs.config.ConfigClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class ChatAlertPacket extends AbstractPacket {
    public static final String packetName = "Data|ChatAlert";

    private Object[] objects;

    public ChatAlertPacket() {
    }

    public ChatAlertPacket(final Object... obs) {
        this.objects = obs;
    }

    public static void sendChatAlert(EntityPlayerMP playerMP, final Object... obs) {
        ChatAlertPacket packet = new ChatAlertPacket(obs);
        PacketHandler.Instance.sendToPlayer(packet, playerMP);
    }

    @Override
    public Enum getType() {
        return EnumDataPacket.CHAT_ALERT;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.DATA_PACKET;
    }

    @Override
    public void sendData(ByteBuf out) throws IOException {
        ByteBufUtils.fillBuffer(out, this.objects);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!ConfigClient.ChatAlerts)
            return;

        // Read all objects (sent as strings) from the buffer.
        List<String> parts = new ArrayList<>();
        String part;
        while ((part = ByteBufUtils.readString(in)) != null && !part.isEmpty()) {
            parts.add(part);
        }
        
        StringBuilder finalMessage = new StringBuilder();
        for (String s : parts) {
            finalMessage.append(StatCollector.translateToLocal(s));
        }

        String text = finalMessage.toString();
        IChatComponent comp = ColorUtil.assembleComponent(text);
        player.addChatMessage(comp);
    }
}
