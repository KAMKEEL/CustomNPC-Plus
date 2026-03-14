package noppes.npcs.wrapper.nbt;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.api.INbt;
import noppes.npcs.platform.nbt.NBTIO;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 1.7.10 implementation of NBTIO.
 * Delegates to CompressedStreamTools.
 */
public class NBTWrapperIO implements NBTIO {

    @Override
    public INbt readCompressed(File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        try {
            NBTTagCompound tag = CompressedStreamTools.readCompressed(fis);
            return new NBTWrapper(tag);
        } finally {
            fis.close();
        }
    }

    @Override
    public void writeCompressed(INbt compound, File file) throws IOException {
        NBTTagCompound tag = ((NBTWrapper) compound).getMCTag();
        FileOutputStream fos = new FileOutputStream(file);
        try {
            CompressedStreamTools.writeCompressed(tag, fos);
        } finally {
            fos.close();
        }
    }

    @Override
    public INbt read(File file) throws IOException {
        NBTTagCompound tag = CompressedStreamTools.read(file);
        return new NBTWrapper(tag);
    }

    @Override
    public void safeWrite(INbt compound, File file) throws IOException {
        NBTTagCompound tag = ((NBTWrapper) compound).getMCTag();
        File tempFile = new File(file.getAbsolutePath() + ".tmp");
        FileOutputStream fos = new FileOutputStream(tempFile);
        try {
            CompressedStreamTools.writeCompressed(tag, fos);
        } finally {
            fos.close();
        }
        if (file.exists()) {
            file.delete();
        }
        tempFile.renameTo(file);
    }
}
