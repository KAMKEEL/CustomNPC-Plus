// src/main/java/kamkeel/network/packets/large/LargeScrollListPacket.java
package kamkeel.network.packets.large;

import io.netty.buffer.ByteBuf;
import kamkeel.network.LargeAbstractPacket;
import kamkeel.network.PacketChannel;
import kamkeel.network.PacketHandler;
import kamkeel.network.enums.EnumClientPacket;
import kamkeel.util.ByteBufUtils;
import net.minecraft.entity.player.EntityPlayer;
import noppes.npcs.client.NoppesUtil;

import java.io.IOException;
import java.util.List;

public final class LargeScrollListPacket extends LargeAbstractPacket {
    public static final String packetName = "Large|ScrollList";

    private List<String> data;

    public LargeScrollListPacket() {}

    public LargeScrollListPacket(List<String> data) {
        this.data = data;
    }

    @Override
    public Enum getType() {
        return EnumClientPacket.SCROLL_LIST;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.LARGE_PACKET;
    }

    @Override
    public void writeData(ByteBuf out) throws IOException {
        out.writeInt(data.size());
        for (String entry : data) {
            ByteBufUtils.writeString(out, entry);
        }
    }

    @Override
    public void handleData(ByteBuf data, EntityPlayer player) throws IOException {
        NoppesUtil.setScrollList(data);
    }
}
