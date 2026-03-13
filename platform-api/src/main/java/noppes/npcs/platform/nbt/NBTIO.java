package noppes.npcs.platform.nbt;

import java.io.File;
import java.io.IOException;

/**
 * Abstraction for NBT file I/O operations.
 *
 * Maps to CompressedStreamTools in MC. Used by controllers for
 * reading/writing world save data.
 */
public interface NBTIO {

    /**
     * Reads a compressed NBT compound from a file.
     * Equivalent to CompressedStreamTools.readCompressed(FileInputStream).
     */
    INBTCompound readCompressed(File file) throws IOException;

    /**
     * Writes a compressed NBT compound to a file.
     * Equivalent to CompressedStreamTools.writeCompressed(NBTTagCompound, FileOutputStream).
     */
    void writeCompressed(INBTCompound compound, File file) throws IOException;

    /**
     * Reads an uncompressed NBT compound from a file.
     * Equivalent to CompressedStreamTools.read(File).
     */
    INBTCompound read(File file) throws IOException;

    /**
     * Writes an uncompressed NBT compound to a file (safe write with temp file).
     * Equivalent to CompressedStreamTools.safeWrite(NBTTagCompound, File).
     */
    void safeWrite(INBTCompound compound, File file) throws IOException;
}
