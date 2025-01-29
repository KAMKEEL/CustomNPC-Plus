package kamkeel.npcs.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.Sys;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public abstract class LargeAbstractPacket extends AbstractPacket {

    private static final Map<UUID, PacketStorage> packetChunks = new ConcurrentHashMap<>();
    private static final int CHUNK_SIZE = 5000;

    @Override
    public void sendData(ByteBuf out) throws IOException {
        byte[] data = getData();
        int totalSize = data.length;
        UUID packetId = UUID.randomUUID();

        System.out.println("Sending data with packetId: " + packetId + ", totalSize: " + totalSize);

        // Break the data into chunks of CHUNK_SIZE bytes each
        for (int offset = 0; offset < totalSize; offset += CHUNK_SIZE) {
            int chunkSize = Math.min(CHUNK_SIZE, totalSize - offset);

            // Write a header for each chunk
            out.writeLong(packetId.getMostSignificantBits());
            out.writeLong(packetId.getLeastSignificantBits());
            out.writeInt(totalSize);
            out.writeInt(offset);
            out.writeInt((totalSize + CHUNK_SIZE - 1) / CHUNK_SIZE); // totalChunks

            // Write the chunk bytes
            out.writeBytes(data, offset, chunkSize);

            System.out.println("Sent chunk with offset: " + offset + ", size: " + chunkSize);
        }
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        long mostSigBits = in.readLong();
        long leastSigBits = in.readLong();
        UUID packetId = new UUID(mostSigBits, leastSigBits);

        int totalSize = in.readInt();
        int offset = in.readInt();
        int totalChunks = in.readInt(); // Currently unused aside from potential debugging/verification
        int chunkSize = Math.min(CHUNK_SIZE, totalSize - offset);

        // Basic validation to ensure the chunk won't overwrite out of bounds
        if (chunkSize <= 0 || offset < 0 || (offset + chunkSize) > totalSize) {
            throw new IndexOutOfBoundsException("Invalid chunk size/offset: chunkSize="
                + chunkSize + ", offset=" + offset + ", totalSize=" + totalSize);
        }

        // Read the exact chunk data
        ByteBuf chunk = in.readBytes(chunkSize);

        System.out.println("Receiving data: packetId=" + packetId
            + ", totalSize=" + totalSize
            + ", offset=" + offset
            + ", chunkSize=" + chunkSize);

        // Store the received chunk in a PacketStorage
        PacketStorage storage = packetChunks.computeIfAbsent(packetId,
            k -> new PacketStorage(Unpooled.buffer(totalSize), 0, totalSize));

        // Write this chunk into the buffer at the correct offset
        storage.data.setBytes(offset, chunk);
        storage.receivedSoFar += chunkSize;

        // If we've received all bytes, finalize
        if (storage.receivedSoFar >= totalSize) {
            // Remove from the map to clean up
            packetChunks.remove(packetId);

            // We now have the complete data in storage.data
            // Copy (or slice) the exact bytes, then handle them
            ByteBuf completeData = storage.data.copy(0, totalSize);

            System.out.println("Received complete data for packetId: " + packetId);

            // Clean up and handle
            storage.data.release();
            handleCompleteData(completeData, player);
            completeData.release();
        }

        chunk.release();
    }

    protected abstract byte[] getData() throws IOException;

    protected abstract void handleCompleteData(ByteBuf data, EntityPlayer player) throws IOException;

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
