package kamkeel.npcs.platform;

import noppes.npcs.api.IDamageSource;
import noppes.npcs.api.INbt;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.item.IItemStack;
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

    NBTFactory nbt();

    // --- NBT I/O ---

    INbt readCompressedNBT(File file) throws IOException;

    void writeCompressedNBT(INbt compound, File file) throws IOException;

    // --- Paths ---

    File getWorldSaveDirectory();

    File getWorldSaveDirectory(String subdir);

    File getConfigDirectory();

    // --- Environment ---

    boolean isPhysicalClient();

    String getModVersion();

    // --- Logging ---

    void logInfo(String message);

    void logWarn(String message);

    void logError(String message);

    void logError(String message, Throwable t);

    void logDebug(String message);

    // --- Entity Wrapping ---

    /**
     * Wraps a raw MC player entity.
     */
    IPlayer wrapPlayer(Object mcPlayer);

    /**
     * Wraps a raw MC entity.
     */
    IEntity wrapEntity(Object mcEntity);

    /**
     * Wraps a raw MC living entity.
     */
    IEntityLivingBase wrapLiving(Object mcLiving);

    /**
     * Wraps a raw MC item stack.
     */
    IItemStack wrapStack(Object mcStack);

    /**
     * Wraps a raw MC world.
     */
    IWorld wrapWorld(Object mcWorld);

    /**
     * Wraps a raw MC damage source.
     */
    IDamageSource wrapDamageSource(Object mcSource);

    // --- Scheduling ---

    void runOnMainThread(Runnable task);
}
