//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package noppes.npcs.scripted.interfaces;

import java.io.File;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.eventhandler.EventBus;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import noppes.npcs.scripted.interfaces.entity.ICustomNpc;
import noppes.npcs.scripted.interfaces.entity.IEntity;
import noppes.npcs.scripted.interfaces.entity.IPlayer;
import noppes.npcs.scripted.interfaces.item.IItemStack;

public abstract class AbstractNpcAPI {
    private static AbstractNpcAPI instance = null;

    public AbstractNpcAPI() {
    }

    public abstract ICustomNpc createNPC(World var1);

    public abstract ICustomNpc spawnNPC(World var1, int var2, int var3, int var4);

    public abstract IEntity getIEntity(Entity var1);

    public abstract IBlock getIBlock(World var1, BlockPos var2);

    public abstract IBlock getIBlock(World world, int x, int y, int z);

    public abstract IContainer getIContainer(IInventory var1);

    public abstract IContainer getIContainer(Container var1);

    public abstract IItemStack getIItemStack(ItemStack var1);

    public abstract IWorld getIWorld(WorldServer var1);

    public abstract IWorld getIWorld(int var1);

    public abstract IWorld[] getIWorlds();

    public abstract IDamageSource getIDamageSource(DamageSource var1);

    public abstract EventBus events();

    public abstract File getGlobalDir();

    public abstract File getWorldDir();

    public static boolean IsAvailable() {
        return Loader.isModLoaded("customnpcs");
    }

    public static AbstractNpcAPI Instance() {
        if (instance != null) {
            return instance;
        } else if (!IsAvailable()) {
            return null;
        } else {
            try {
                Class c = Class.forName("noppes.npcs.scripted.NpcAPI");
                instance = (AbstractNpcAPI) c.getMethod("Instance").invoke((Object) null);
            } catch (Exception var1) {
                var1.printStackTrace();
            }

            return instance;
        }
    }

    public abstract void executeCommand(IWorld var1, String var2);

    public abstract String getRandomName(int var1, int var2);

    public abstract INbt getINbt(NBTTagCompound entityData);

    public abstract IPlayer[] getAllServerPlayers();

    public abstract IItemStack createItem(String id, int damage, int size);

    public abstract IParticle createParticle(String directory);

    public abstract IParticle createEntityParticle(String directory);

    public abstract int getServerTime();
}
