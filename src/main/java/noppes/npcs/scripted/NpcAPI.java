//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package noppes.npcs.scripted;

import java.io.File;

import net.minecraft.entity.Entity;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.eventhandler.EventBus;
import noppes.npcs.scripted.CommandNoppesBase;
import noppes.npcs.scripted.IContainer;
import noppes.npcs.scripted.IDamageSource;
import noppes.npcs.scripted.INbt;
import noppes.npcs.scripted.IWorld;
import noppes.npcs.scripted.entity.ICustomNpc;
import noppes.npcs.scripted.entity.IEntity;
import noppes.npcs.scripted.entity.data.IPlayerMail;
import noppes.npcs.scripted.gui.ICustomGui;
import noppes.npcs.scripted.handler.ICloneHandler;
import noppes.npcs.scripted.handler.IDialogHandler;
import noppes.npcs.scripted.handler.IFactionHandler;
import noppes.npcs.scripted.handler.IQuestHandler;
import noppes.npcs.scripted.handler.IRecipeHandler;
import noppes.npcs.scripted.item.IItemStack;

public abstract class NpcAPI {
    private static NpcAPI instance = null;

    public NpcAPI() {
    }

    public abstract ICustomNpc createNPC(World var1);

    public abstract ICustomNpc spawnNPC(World var1, int var2, int var3, int var4);

    public abstract IEntity getIEntity(Entity var1);

    public abstract IContainer getIContainer(IInventory var1);

    public abstract IContainer getIContainer(Container var1);

    public abstract IItemStack getIItemStack(ItemStack var1);

    public abstract IWorld getIWorld(WorldServer var1);

    public abstract IWorld getIWorld(int var1);

    public abstract IWorld[] getIWorlds();

    public abstract INbt getINbt(NBTTagCompound var1);

    public abstract IFactionHandler getFactions();

    public abstract IRecipeHandler getRecipes();

    public abstract IQuestHandler getQuests();

    public abstract IDialogHandler getDialogs();

    public abstract ICloneHandler getClones();

    public abstract IDamageSource getIDamageSource(DamageSource var1);

    public abstract INbt stringToNbt(String var1);

    public abstract IPlayerMail createMail(String var1, String var2);

    public abstract ICustomGui createCustomGui(int var1, int var2, int var3, boolean var4);

    public abstract INbt getRawPlayerData(String var1);

    public abstract EventBus events();

    public abstract void registerCommand(CommandNoppesBase var1);

    public abstract File getGlobalDir();

    public abstract File getWorldDir();

    public static boolean IsAvailable() {
        return Loader.isModLoaded("customnpcs");
    }

    public static NpcAPI Instance() {
        if(instance != null) {
            return instance;
        } else if(!IsAvailable()) {
            return null;
        } else {
            try {
                Class e = Class.forName("noppes.npcs.api.wrapper.WrapperNpcAPI");
                instance = (NpcAPI)e.getMethod("Instance", new Class[0]).invoke((Object)null, new Object[0]);
            } catch (Exception var1) {
                var1.printStackTrace();
            }

            return instance;
        }
    }

    public abstract void registerPermissionNode(String var1, int var2);

    public abstract boolean hasPermissionNode(String var1);

    public abstract String executeCommand(IWorld var1, String var2);

    public abstract String getRandomName(int var1, int var2);
}
