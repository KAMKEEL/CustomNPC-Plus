package noppes.npcs.wrapper.nbt;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.platform.nbt.INBTCompound;
import noppes.npcs.platform.nbt.NBTIO;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 1.7.10 implementation of NBTIO.
 * Delegates to CompressedStreamTools.
 */
public class MC1710NBTIO implements NBTIO {

    @Override
    public INBTCompound readCompressed(File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        try {
            NBTTagCompound tag = CompressedStreamTools.readCompressed(fis);
            return new MC1710NBTCompound(tag);
        } finally {
            fis.close();
        }
    }

    @Override
    public void writeCompressed(INBTCompound compound, File file) throws IOException {
        NBTTagCompound tag = ((MC1710NBTCompound) compound).getMCTag();
        FileOutputStream fos = new FileOutputStream(file);
        try {
            CompressedStreamTools.writeCompressed(tag, fos);
        } finally {
            fos.close();
        }
    }

    @Override
    public INBTCompound read(File file) throws IOException {
        NBTTagCompound tag = CompressedStreamTools.read(file);
        return new MC1710NBTCompound(tag);
    }

    @Override
    public void safeWrite(INBTCompound compound, File file) throws IOException {
        NBTTagCompound tag = ((MC1710NBTCompound) compound).getMCTag();
        // 1.7.10 doesn't have safeWrite, implement with temp file
        File tempFile = new File(file.getAbsolutePath() + ".tmp");
        FileOutputStream fos = new FileOutputStream(tempFile);
        try {
            CompressedStreamTools.writeCompressed(tag, fos);
        } finally {
            fos.close();
        }
        // Atomic rename
        if (file.exists()) {
            file.delete();
        }
        tempFile.renameTo(file);
    }
}
