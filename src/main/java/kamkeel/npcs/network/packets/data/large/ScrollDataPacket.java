package kamkeel.npcs.network.packets.data.large;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import kamkeel.npcs.network.LargeAbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumDataPacket;
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.constants.EnumScrollData;

import java.io.IOException;
import java.util.Map;

public final class ScrollDataPacket extends LargeAbstractPacket {
    public static final String packetName = "Large|ScrollData";

    private Map<String, Integer> data;
    private EnumScrollData dataType;

    public ScrollDataPacket() {
    }

    public ScrollDataPacket(Map<String, Integer> data, EnumScrollData dataType) {
        this.data = data;
        this.dataType = dataType;
    }

    public static void sendScrollData(EntityPlayerMP player, Map<String, Integer> map, EnumScrollData type) {
        ScrollDataPacket packet = new ScrollDataPacket(map, type);
        PacketHandler.Instance.sendToPlayer(packet, player);
    }

    @Override
    public Enum getType() {
        return EnumDataPacket.SCROLL_DATA;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.DATA_PACKET;
    }

    @Override
    protected byte[] getData() throws IOException {
        ByteBuf buffer = Unpooled.buffer();
        buffer.writeInt(this.dataType.ordinal());
        buffer.writeInt(data.size());
        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            buffer.writeInt(entry.getValue());
            ByteBufUtils.writeString(buffer, entry.getKey());
        }
        byte[] bytes = new byte[buffer.readableBytes()];
        buffer.readBytes(bytes);
        return bytes;
    }

    @Override
    protected void handleCompleteData(ByteBuf data, EntityPlayer player) throws IOException {
        NoppesUtil.setScrollData(data);
    }
}
