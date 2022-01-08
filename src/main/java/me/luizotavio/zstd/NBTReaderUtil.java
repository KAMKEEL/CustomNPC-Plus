package me.luizotavio.zstd;

import com.github.luben.zstd.Zstd;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTSizeTracker;
import net.minecraft.nbt.NBTTagCompound;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;

public class NBTReaderUtil {

    /**
     * Reads a block of zstd-compressed data. This method
     * expects the following ints to be the compressed size,
     * and uncompressed size respectively.
     *
     * @return the uncompressed data
     * @throws IOException if the bytes cannot be read
     * @throws IllegalArgumentException if the uncompressed length doesn't match
     */

    public static byte[] readCompressed(DataInput inputStream) throws IOException {
        int compressedLength = inputStream.readInt();
        int uncompressedLength = inputStream.readInt();

        if (compressedLength < 0 || uncompressedLength < 0) {
            throw new IllegalArgumentException("Invalid length");
        }

        byte[] compressed = new byte[compressedLength];

        inputStream.readFully(compressed);

        return Zstd.decompress(compressed, uncompressedLength);
    }

    /**
     * Reads and parses a block of zstd-compressed bytes as
     * an NBT named compound tag.
     *
     * @return the parsed named compound tag.
     * @throws IOException if the bytes cannot be read
     * @see .readCompressed
     */
    public static NBTTagCompound readCompressCompound(DataInput inputStream) throws IOException {
        NBTTagCompound compound;

        byte[] compressed = readCompressed(inputStream);

        try (DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(compressed))) {
            compound = CompressedStreamTools.func_152456_a(dataInputStream, NBTSizeTracker.field_152451_a);
        }

        return compound;
    }
}
