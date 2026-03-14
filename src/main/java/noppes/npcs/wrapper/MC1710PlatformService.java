package noppes.npcs.wrapper;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import noppes.npcs.CustomNpcs;
import noppes.npcs.platform.PlatformService;
import kamkeel.npcs.platform.entity.*;
import noppes.npcs.platform.nbt.INBTCompound;
import noppes.npcs.platform.nbt.INBTList;
import noppes.npcs.platform.nbt.NBTFactory;
import noppes.npcs.wrapper.nbt.NBTWrapperFactory;
import noppes.npcs.wrapper.nbt.NBTWrapperIO;
import kamkeel.npcs.wrapper.platform.*;

import java.io.File;
import java.io.IOException;

/**
 * 1.7.10 implementation of PlatformService.
 * Bridges CORE code to MC 1.7.10 APIs.
 */
public class MC1710PlatformService implements PlatformService {

    private final NBTWrapperFactory nbtFactory = new NBTWrapperFactory();
    private final NBTWrapperIO nbtIO = new NBTWrapperIO();

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

    // --- Entity Wrapping ---

    @Override
    public IUser wrapPlayer(Object mcPlayer) {
        return new PlayerWrapper((EntityPlayerMP) mcPlayer);
    }

    @Override
    public IMob wrapEntity(Object mcEntity) {
        return new EntityWrapper((Entity) mcEntity);
    }

    @Override
    public ILiving wrapLiving(Object mcLiving) {
        return new LivingWrapper((EntityLivingBase) mcLiving);
    }

    @Override
    public IStack wrapStack(Object mcStack) {
        return new StackWrapper((ItemStack) mcStack);
    }

    @Override
    public IGameWorld wrapWorld(Object mcWorld) {
        return new WorldWrapper((World) mcWorld);
    }

    @Override
    public IDamage wrapDamageSource(Object mcSource) {
        return new DamageWrapper((DamageSource) mcSource);
    }

    // --- Scheduling ---

    @Override
    public void runOnMainThread(Runnable task) {
        MinecraftServer server = MinecraftServer.getServer();
        if (server != null) {
            // 1.7.10 doesn't have addScheduledTask — just run directly if on main thread
            // or queue via the server's next tick
            task.run();
        } else {
            task.run();
        }
    }
}
