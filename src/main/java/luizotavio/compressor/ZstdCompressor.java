package luizotavio.compressor;

import com.github.luben.zstd.Zstd;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTSizeTracker;
import net.minecraft.nbt.NBTTagCompound;

import java.io.*;

public class ZstdCompressor {

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

    public static NBTTagCompound readCompressCompound(DataInput inputStream) throws IOException {
        NBTTagCompound compound;

        byte[] compressed = readCompressed(inputStream);

        try (DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(compressed))) {
            compound = CompressedStreamTools.func_152456_a(dataInputStream, NBTSizeTracker.field_152451_a);
        }

        return compound;
    }

    public static byte[] writeCompound(NBTTagCompound compound) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(),
            copy = new ByteArrayOutputStream();

        try (DataOutputStream dataOutputStream = new DataOutputStream(outputStream)) {
            CompressedStreamTools.write(compound, dataOutputStream);
        }

        byte[] uncompressed = outputStream.toByteArray();

        byte[] compress = Zstd.compress(uncompressed);

        try (DataOutputStream dataOutputStream = new DataOutputStream(copy)) {
            dataOutputStream.writeInt(compress.length);
            dataOutputStream.writeInt(uncompressed.length);

            dataOutputStream.write(compress);

            return copy.toByteArray();
        }
    }

}
