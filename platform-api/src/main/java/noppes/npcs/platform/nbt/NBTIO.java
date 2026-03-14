package noppes.npcs.platform.nbt;

import noppes.npcs.api.INbt;

import java.io.File;
import java.io.IOException;

/**
 * Abstraction for NBT file I/O operations.
 * Maps to CompressedStreamTools in MC.
 */
public interface NBTIO {

    INbt readCompressed(File file) throws IOException;

    void writeCompressed(INbt compound, File file) throws IOException;

    INbt read(File file) throws IOException;

    void safeWrite(INbt compound, File file) throws IOException;
}
