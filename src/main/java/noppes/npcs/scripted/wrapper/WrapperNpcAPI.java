//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package noppes.npcs.scripted.wrapper;

import java.io.File;
import java.util.Map;

import cpw.mods.fml.common.eventhandler.EventBus;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.scripted.*;
import noppes.npcs.containers.ContainerNpcInterface;
import noppes.npcs.controllers.DialogController;
import noppes.npcs.controllers.FactionController;
import noppes.npcs.controllers.QuestController;
import noppes.npcs.controllers.RecipeController;
import noppes.npcs.controllers.ServerCloneController;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.LRUHashMap;
import noppes.npcs.util.NBTJsonUtil;
import noppes.npcs.util.JsonException;

public class WrapperNpcAPI extends NpcAPI {
    private static final Map<Integer, ScriptWorld> worldCache = new LRUHashMap(10);
    public static final EventBus EVENT_BUS = new EventBus();
    private static NpcAPI instance = null;

    public WrapperNpcAPI() {
    }

    public static void clearCache() {
        worldCache.clear();
    }

    public IEntity getIEntity(Entity entity) {
        if (entity != null && !entity.worldObj.isRemote) {
            return (IEntity)(entity instanceof EntityNPCInterface ? ((EntityNPCInterface)entity).wrappedNPC : WrapperEntityData.get(entity));
        } else {
            return null;
        }
    }

    public ICustomNpc createNPC(World world) {
        if (world.isRemote) {
            return null;
        } else {
            EntityCustomNpc npc = new EntityCustomNpc(world);
            return npc.wrappedNPC;
        }
    }

    public ICustomNpc spawnNPC(World world, int x, int y, int z) {
        if (world.isRemote) {
            return null;
        } else {
            EntityCustomNpc npc = new EntityCustomNpc(world);
            npc.setPositionAndRotation((double)x + 0.5D, (double)y, (double)z + 0.5D, 0.0F, 0.0F);
            npc.setHealth(npc.getMaxHealth());
            world.spawnEntityInWorld(npc);
            return npc.wrappedNPC;
        }
    }

    public static NpcAPI Instance() {
        if (instance == null) {
            instance = new WrapperNpcAPI();
        }

        return instance;
    }

    public EventBus events() {
        return EVENT_BUS;
    }

    public IItemStack getIItemStack(ItemStack itemstack) {
        return (IItemStack)(itemstack != null && itemstack.stackSize > 0 ? new ScriptItemStack(itemstack) : new ScriptItemStack(new ItemStack(Item.getItemById(0))));

    }

    public IWorld getIWorld(WorldServer world) {
        ScriptWorld w = (ScriptWorld)worldCache.get(world.provider.dimensionId);
        if (w != null) {
            w.world = world;
            return w;
        } else {
            worldCache.put(world.provider.dimensionId, w = ScriptWorld.createNew(world));
            return w;
        }
    }

    public IWorld getIWorld(int dimensionId) {
        WorldServer[] var2 = CustomNpcs.getServer().worldServers;
        int var3 = var2.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            WorldServer world = var2[var4];
            if (world.provider.dimensionId == dimensionId) {
                return this.getIWorld(world);
            }
        }

        throw new CustomNPCsException("Unknown dimension id: " + dimensionId, new Object[0]);
    }

    public IContainer getIContainer(IInventory inventory) {
        return new ScriptContainer(inventory);
    }

    public IContainer getIContainer(Container container) {
        return (IContainer)(container instanceof ContainerNpcInterface ? ContainerNpcInterface.getOrCreateIContainer((ContainerNpcInterface)container) : new ScriptContainer(container));
    }

    private void checkWorld() {
        if (CustomNpcs.getServer() == null || CustomNpcs.getServer().isServerStopped()) {
            throw new CustomNPCsException("No world is loaded right now", new Object[0]);
        }
    }

    public IWorld[] getIWorlds() {
        this.checkWorld();
        IWorld[] worlds = new IWorld[CustomNpcs.getServer().worldServers.length];

        for(int i = 0; i < CustomNpcs.getServer().worldServers.length; ++i) {
            worlds[i] = this.getIWorld(CustomNpcs.getServer().worldServers[i]);
        }

        return worlds;
    }

    public File getGlobalDir() {
        return CustomNpcs.Dir;
    }

    public File getWorldDir() {
        return CustomNpcs.getWorldSaveDirectory();
    }

    public IDamageSource getIDamageSource(DamageSource damagesource) {
        return new ScriptDamageSource(damagesource);
    }

    public String executeCommand(IWorld world, String command) {
        FakePlayer player = EntityNPCInterface.CommandPlayer;
        player.setWorld(world.getMCWorld());
        player.setPosition(0.0D, 0.0D, 0.0D);
        return NoppesUtilServer.runCommand(world.getMCWorld(), BlockPos.ORIGIN, "API", command, (EntityPlayer)null, player);
    }
    public String getRandomName(int dictionary, int gender) {
        return CustomNpcs.MARKOV_GENERATOR[dictionary].fetch(gender);
    }
}
