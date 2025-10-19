package kamkeel.npcs.network;

import cpw.mods.fml.common.network.internal.FMLProxyPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayer;
import noppes.npcs.LogWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public abstract class LargeAbstractPacket extends AbstractPacket {

    private static final Map<UUID, PacketStorage> packetChunks = new ConcurrentHashMap<>();
    protected static final int CHUNK_SIZE = 10000;

    /**
     * Create multiple FMLProxyPacket objects, each containing up to CHUNK_SIZE bytes.
     */
    @Override
    public List<FMLProxyPacket> generatePackets() {
        List<FMLProxyPacket> packets = new ArrayList<>();
        PacketChannel packetChannel = getChannel();

        byte[] fullData;
        try {
            fullData = getData();
        } catch (IOException e) {
            e.printStackTrace();
            return packets;
        }

        int totalSize = fullData.length;
        UUID packetId = UUID.randomUUID();

        // Break data into CHUNK_SIZE chunks
        for (int offset = 0; offset < totalSize; offset += CHUNK_SIZE) {
            int chunkSize = Math.min(CHUNK_SIZE, totalSize - offset);
            ByteBuf chunkBuf = Unpooled.buffer();

            chunkBuf.writeInt(packetChannel.getChannelType().ordinal());
            chunkBuf.writeInt(getType().ordinal());

            chunkBuf.writeLong(packetId.getMostSignificantBits());
            chunkBuf.writeLong(packetId.getLeastSignificantBits());
            chunkBuf.writeInt(totalSize);
            chunkBuf.writeInt(offset);
            chunkBuf.writeInt((totalSize + CHUNK_SIZE - 1) / CHUNK_SIZE);

            chunkBuf.writeBytes(fullData, offset, chunkSize);

            FMLProxyPacket proxyPacket = new FMLProxyPacket(chunkBuf, packetChannel.getChannelName());
            packets.add(proxyPacket);
        }
        return packets;
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        UUID packetId = null;
        PacketStorage storage = null;
        ByteBuf chunk = null;
        ByteBuf completeData = null;

        try {
            // Read UUID from the buffer
            long mostSigBits = in.readLong();
            long leastSigBits = in.readLong();
            packetId = new UUID(mostSigBits, leastSigBits);

            // Read packet metadata
            int totalSize = in.readInt();
            int offset = in.readInt();
            int totalChunks = in.readInt(); // Debugging info
            int chunkSize = Math.min(CHUNK_SIZE, totalSize - offset);

            // Basic validation to ensure the chunk won't overwrite out of bounds
            if (chunkSize <= 0 || offset < 0 || (offset + chunkSize) > totalSize) {
                throw new IndexOutOfBoundsException("Invalid chunk size/offset: chunkSize="
                    + chunkSize + ", offset=" + offset + ", totalSize=" + totalSize);
            }

            // Read the exact chunk data
            chunk = in.readBytes(chunkSize);

            // Store the received chunk in a PacketStorage
            storage = packetChunks.computeIfAbsent(packetId, k -> new PacketStorage(Unpooled.buffer(totalSize), 0, totalSize));

            // Write this chunk into the buffer at the correct offset
            storage.data.setBytes(offset, chunk);
            storage.receivedSoFar += chunkSize;

            // If we've received all bytes, finalize
            if (storage.receivedSoFar >= totalSize) {
                packetChunks.remove(packetId);
                completeData = storage.data.copy(0, totalSize);

                try {
                    handleCompleteData(completeData, player);
                } finally {
                    completeData.release();
                }

                storage.data.release();
            }
        } catch (Exception e) {
            // Log the exception with contextual information
            LogWriter.error("Error in receiveData for packetId " + (packetId != null ? packetId.toString() : "null") + ": " + e.getMessage());
            e.printStackTrace();

            // If storage was created but the packet is incomplete, release storage.data
            if (storage != null && storage.receivedSoFar < storage.totalSize) {
                storage.data.release();
                if (packetId != null) {
                    packetChunks.remove(packetId);
                }
            }

            throw e; // Rethrow the exception after logging
        } finally {
            // Ensure that 'chunk' is released if it was allocated
            if (chunk != null && chunk.refCnt() > 0) {
                chunk.release();
            }

            // Ensure that 'completeData' is released if it was created and not yet released
            if (completeData != null && completeData.refCnt() > 0) {
                completeData.release();
            }
        }
    }

    protected abstract byte[] getData() throws IOException;

    protected abstract void handleCompleteData(ByteBuf data, EntityPlayer player) throws IOException;

    @Override
    public final FMLProxyPacket generatePacket() {
        return null;
    }

    @Override
    public void sendData(ByteBuf out) throws IOException {
    }

    /**
     * Simple storage class to keep track of partial data and how many bytes we've received so far.
     */
    private static class PacketStorage {
        final ByteBuf data;
        int receivedSoFar;
        final int totalSize;

        PacketStorage(ByteBuf data, int receivedSoFar, int totalSize) {
            this.data = data;
            this.receivedSoFar = receivedSoFar;
            this.totalSize = totalSize;
        }
    }
}
