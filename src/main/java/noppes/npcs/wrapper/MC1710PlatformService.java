package noppes.npcs.wrapper;

import noppes.npcs.CustomNpcs;
import noppes.npcs.platform.PlatformService;
import noppes.npcs.platform.nbt.INBTCompound;
import noppes.npcs.platform.nbt.INBTList;
import noppes.npcs.platform.nbt.NBTFactory;
import noppes.npcs.wrapper.nbt.MC1710NBTFactory;
import noppes.npcs.wrapper.nbt.MC1710NBTIO;

import java.io.File;
import java.io.IOException;

/**
 * 1.7.10 implementation of PlatformService.
 * Bridges CORE code to MC 1.7.10 APIs.
 */
public class MC1710PlatformService implements PlatformService {

    private final MC1710NBTFactory nbtFactory = new MC1710NBTFactory();
    private final MC1710NBTIO nbtIO = new MC1710NBTIO();

    @Override
    public NBTFactory nbt() {
        return nbtFactory;
    }

    @Override
    public INBTCompound readCompressedNBT(File file) throws IOException {
        return nbtIO.readCompressed(file);
    }

    @Override
    public void writeCompressedNBT(INBTCompound compound, File file) throws IOException {
        nbtIO.writeCompressed(compound, file);
    }

    @Override
    public File getWorldSaveDirectory() {
        return CustomNpcs.getWorldSaveDirectory();
    }

    @Override
    public File getWorldSaveDirectory(String subdir) {
        return CustomNpcs.getWorldSaveDirectory(subdir);
    }

    @Override
    public File getConfigDirectory() {
        return new File(CustomNpcs.configPath);
    }

    @Override
    public boolean isPhysicalClient() {
        try {
            // If Minecraft class is loadable, we're on the client
            Class.forName("net.minecraft.client.Minecraft");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @Override
    public String getModVersion() {
        return "1.11";
    }

    @Override
    public void logInfo(String message) {
        System.out.println("[CustomNPC+] " + message);
    }

    @Override
    public void logWarn(String message) {
        System.out.println("[CustomNPC+ WARN] " + message);
    }

    @Override
    public void logError(String message) {
        System.err.println("[CustomNPC+ ERROR] " + message);
    }

    @Override
    public void logError(String message, Throwable t) {
        System.err.println("[CustomNPC+ ERROR] " + message);
        t.printStackTrace();
    }

    @Override
    public void logDebug(String message) {
        System.out.println("[CustomNPC+ DEBUG] " + message);
    }
}
