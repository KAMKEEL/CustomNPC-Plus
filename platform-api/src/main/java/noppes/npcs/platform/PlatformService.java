package noppes.npcs.platform;

import kamkeel.npcs.platform.entity.IDamage;
import kamkeel.npcs.platform.entity.ILiving;
import kamkeel.npcs.platform.entity.IMob;
import kamkeel.npcs.platform.entity.IUser;
import kamkeel.npcs.platform.entity.IStack;
import kamkeel.npcs.platform.entity.IGameWorld;
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

    void logInfo(String message);

    void logWarn(String message);

    void logError(String message);

    void logError(String message, Throwable t);

    void logDebug(String message);

    // --- Entity Wrapping ---

    /**
     * Wraps a raw MC player entity into a platform-independent player.
     *
     * @param mcPlayer the MC player object (e.g., EntityPlayerMP on 1.7.10)
     * @return a platform player wrapper
     */
    IUser wrapPlayer(Object mcPlayer);

    /**
     * Wraps a raw MC entity into a platform-independent entity.
     *
     * @param mcEntity the MC entity object
     * @return a platform entity wrapper
     */
    IMob wrapEntity(Object mcEntity);

    /**
     * Wraps a raw MC living entity into a platform-independent living entity.
     *
     * @param mcLiving the MC living entity object (e.g., EntityLivingBase on 1.7.10)
     * @return a platform living wrapper
     */
    ILiving wrapLiving(Object mcLiving);

    /**
     * Wraps a raw MC item stack into a platform-independent stack.
     *
     * @param mcStack the MC ItemStack object
     * @return a platform stack wrapper
     */
    IStack wrapStack(Object mcStack);

    /**
     * Wraps a raw MC world into a platform-independent world.
     *
     * @param mcWorld the MC World object
     * @return a platform world wrapper
     */
    IGameWorld wrapWorld(Object mcWorld);

    /**
     * Wraps a raw MC damage source into a platform-independent damage source.
     *
     * @param mcSource the MC DamageSource object
     * @return a platform damage source wrapper
     */
    IDamage wrapDamageSource(Object mcSource);

    // --- Scheduling ---

    /**
     * Runs a task on the main server thread.
     * Safe to call from any thread.
     *
     * @param task the task to run
     */
    void runOnMainThread(Runnable task);
}
