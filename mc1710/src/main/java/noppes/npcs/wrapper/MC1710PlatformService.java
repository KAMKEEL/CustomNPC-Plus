package noppes.npcs.wrapper;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import noppes.npcs.CustomNpcs;
import kamkeel.npcs.platform.PlatformService;
import noppes.npcs.api.IDamageSource;
import noppes.npcs.api.INbt;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.platform.nbt.NBTFactory;
import noppes.npcs.scripted.NpcAPI;
import noppes.npcs.scripted.ScriptDamageSource;
import noppes.npcs.scripted.ScriptWorld;
import noppes.npcs.scripted.item.ScriptItemStack;
import noppes.npcs.wrapper.nbt.NBTWrapperFactory;
import noppes.npcs.wrapper.nbt.NBTWrapperIO;

import java.io.File;
import java.io.IOException;

/**
 * 1.7.10 implementation of PlatformService.
 * Bridges CORE code to MC 1.7.10 APIs via Script* wrappers.
 */
public class MC1710PlatformService implements PlatformService {

    private final NBTWrapperFactory nbtFactory = new NBTWrapperFactory();
    private final NBTWrapperIO nbtIO = new NBTWrapperIO();

    @Override
    public NBTFactory nbt() {
        return nbtFactory;
    }

    @Override
    public INbt readCompressedNBT(File file) throws IOException {
        return nbtIO.readCompressed(file);
    }

    @Override
    public void writeCompressedNBT(INbt compound, File file) throws IOException {
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
    @SuppressWarnings("unchecked")
    public IPlayer wrapPlayer(Object mcPlayer) {
        return (IPlayer) NpcAPI.Instance().getIEntity((Entity) mcPlayer);
    }

    @Override
    public IEntity wrapEntity(Object mcEntity) {
        return NpcAPI.Instance().getIEntity((Entity) mcEntity);
    }

    @Override
    @SuppressWarnings("unchecked")
    public IEntityLivingBase wrapLiving(Object mcLiving) {
        return (IEntityLivingBase) NpcAPI.Instance().getIEntity((Entity) mcLiving);
    }

    @Override
    public IItemStack wrapStack(Object mcStack) {
        return NpcAPI.Instance().getIItemStack((ItemStack) mcStack);
    }

    @Override
    public IWorld wrapWorld(Object mcWorld) {
        return NpcAPI.Instance().getIWorld((World) mcWorld);
    }

    @Override
    public IDamageSource wrapDamageSource(Object mcSource) {
        return NpcAPI.Instance().getIDamageSource((DamageSource) mcSource);
    }

    // --- Scheduling ---

    @Override
    public void runOnMainThread(Runnable task) {
        MinecraftServer server = MinecraftServer.getServer();
        if (server != null) {
            task.run();
        } else {
            task.run();
        }
    }
}
