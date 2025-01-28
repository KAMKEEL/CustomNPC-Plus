// src/main/java/kamkeel/network/LargeAbstractPacket.java
package kamkeel.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public abstract class LargeAbstractPacket extends AbstractPacket {
    private static final int CHUNK_SIZE = 30000;
    private static final Map<Integer, ByteBuf> packetChunks = new HashMap<>();
    private static final Map<Integer, Integer> receivedChunks = new HashMap<>();

    public abstract void handleData(ByteBuf data, EntityPlayer player) throws IOException;

    @Override
    public void sendData(ByteBuf out) throws IOException {
        ByteBuf data = Unpooled.buffer();
        writeData(data);
        int totalSize = data.readableBytes();
        int packetId = this.hashCode(); // Unique ID for this packet
        int totalChunks = (int) Math.ceil((double) totalSize / CHUNK_SIZE);

        for (int i = 0; i < totalSize; i += CHUNK_SIZE) {
            int chunkSize = Math.min(CHUNK_SIZE, totalSize - i);
            ByteBuf chunk = data.readBytes(chunkSize);
            out.writeInt(packetId);
            out.writeInt(totalSize);
            out.writeInt(i);
            out.writeInt(totalChunks);
            out.writeBytes(chunk);
        }
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        int packetId = in.readInt();
        int totalSize = in.readInt();
        int offset = in.readInt();
        int totalChunks = in.readInt();
        ByteBuf chunk = in.readBytes(in.readableBytes());

        ByteBuf data = packetChunks.computeIfAbsent(packetId, k -> Unpooled.buffer(totalSize));
        data.setBytes(offset, chunk);

        int receivedChunkCount = receivedChunks.compute(packetId, (k, v) -> (v == null) ? 1 : v + 1);

        if (receivedChunkCount == totalChunks) {
            packetChunks.remove(packetId);
            receivedChunks.remove(packetId);
            handleData(data, player);
        }
    }

    public abstract void writeData(ByteBuf out) throws IOException;
}
