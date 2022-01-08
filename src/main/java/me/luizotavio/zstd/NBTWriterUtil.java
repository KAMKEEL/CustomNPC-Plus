package me.luizotavio.zstd;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class NBTWriterUtil {

    public static byte[] writeCompound(NBTTagCompound compound) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try (DataOutputStream dataOutputStream = new DataOutputStream(outputStream)) {
            CompressedStreamTools.write(compound, dataOutputStream);
        }

        return outputStream.toByteArray();
    }
}
