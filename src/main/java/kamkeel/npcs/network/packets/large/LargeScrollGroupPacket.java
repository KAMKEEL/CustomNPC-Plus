package kamkeel.npcs.network.packets.large;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import kamkeel.npcs.network.LargeAbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumClientPacket;
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.entity.player.EntityPlayer;
import noppes.npcs.client.NoppesUtil;

import java.io.IOException;
import java.util.Map;

public final class LargeScrollGroupPacket extends LargeAbstractPacket {
    public static final String packetName = "Large|ScrollGroup";

    private Map<String, Integer> data;

    public LargeScrollGroupPacket() {}

    public LargeScrollGroupPacket(Map<String, Integer> data) {
        this.data = data;
    }

    @Override
    public Enum getType() {
        return EnumClientPacket.SCROLL_GROUP;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.LARGE_PACKET;
    }

    @Override
    protected byte[] getData() throws IOException {
        ByteBuf buffer = Unpooled.buffer();
        buffer.writeInt(data.size());
        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            ByteBufUtils.writeString(buffer, entry.getKey());
            buffer.writeInt(entry.getValue());
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
