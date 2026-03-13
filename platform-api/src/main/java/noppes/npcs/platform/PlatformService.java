package noppes.npcs.platform;

import noppes.npcs.platform.nbt.INBTCompound;
import noppes.npcs.platform.nbt.INBTList;
import noppes.npcs.platform.nbt.NBTFactory;

import java.io.File;
import java.io.IOException;

/**
 * Central platform abstraction that bridges CORE logic with MC-version-specific code.
 *
 * Each mc* module (mc1710, mc1122, mc1165, mc1201) provides an implementation
 * that is registered during mod initialization via {@link PlatformServiceHolder#set(PlatformService)}.
 *
 * CORE code accesses this via {@link PlatformServiceHolder#get()}.
 */
public interface PlatformService {

    // --- NBT ---

    /**
     * @return the NBT factory for creating and wrapping NBT objects
     */
    NBTFactory nbt();

    // --- NBT I/O ---

    /**
     * Reads a compressed NBT compound from a file.
     * Maps to CompressedStreamTools.readCompressed() in MC.
     */
    INBTCompound readCompressedNBT(File file) throws IOException;

    /**
     * Writes a compressed NBT compound to a file.
     * Maps to CompressedStreamTools.writeCompressed() in MC.
     */
    void writeCompressedNBT(INBTCompound compound, File file) throws IOException;

    // --- Paths ---

    /**
     * @return the world save directory (server-side), e.g. "saves/MyWorld/customnpcs/"
     */
    File getWorldSaveDirectory();

    /**
     * @return a subdirectory within the world save directory
     */
    File getWorldSaveDirectory(String subdir);

    /**
     * @return the global config directory, e.g. ".minecraft/config/"
     */
    File getConfigDirectory();

    // --- Environment ---

    /**
     * @return true if running on the physical client (has a GUI)
     */
    boolean isPhysicalClient();

    /**
     * @return the mod version string
     */
    String getModVersion();

    // --- Logging ---

    /**
     * Logs at INFO level.
     */
    void logInfo(String message);

    /**
     * Logs at WARN level.
     */
    void logWarn(String message);

    /**
     * Logs at ERROR level.
     */
    void logError(String message);

    /**
     * Logs at ERROR level with a throwable.
     */
    void logError(String message, Throwable t);

    /**
     * Logs at DEBUG level.
     */
    void logDebug(String message);
}
