//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package noppes.npcs.scripted.wrapper;

import java.io.File;
import java.util.Map;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import cpw.mods.fml.common.eventhandler.EventBus;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.scripted.CommandNoppesBase;
import noppes.npcs.scripted.CustomNPCsException;
import noppes.npcs.scripted.IContainer;
import noppes.npcs.scripted.IDamageSource;
import noppes.npcs.scripted.INbt;
import noppes.npcs.scripted.IWorld;
import noppes.npcs.scripted.NpcAPI;
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
import noppes.npcs.scripted.wrapper.ContainerWrapper;
import noppes.npcs.scripted.wrapper.DamageSourceWrapper;
import noppes.npcs.scripted.wrapper.ItemStackWrapper;
import noppes.npcs.scripted.wrapper.NBTWrapper;
import noppes.npcs.scripted.wrapper.WorldWrapper;
import noppes.npcs.scripted.wrapper.WrapperEntityData;
import noppes.npcs.scripted.wrapper.gui.CustomGuiWrapper;
import noppes.npcs.containers.ContainerNpcInterface;
import noppes.npcs.controllers.DialogController;
import noppes.npcs.controllers.FactionController;
import noppes.npcs.controllers.QuestController;
import noppes.npcs.controllers.RecipeController;
import noppes.npcs.controllers.ServerCloneController;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.controllers.data.PlayerMail;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.LRUHashMap;
import noppes.npcs.util.NBTJsonUtil;
import noppes.npcs.util.JsonException;

public class WrapperNpcAPI extends NpcAPI {
    private static final Map<Integer, WorldWrapper> worldCache = new LRUHashMap(10);
    public static final EventBus EVENT_BUS = new EventBus();
    private static NpcAPI instance = null;

    public WrapperNpcAPI() {
    }

    public static void clearCache() {
        worldCache.clear();
    }

    public IEntity getIEntity(Entity entity) {
        return (IEntity)(entity != null && !entity.field_70170_p.field_72995_K?(entity instanceof EntityNPCInterface?((EntityNPCInterface)entity).wrappedNPC:WrapperEntityData.get(entity)):null);
    }

    public ICustomNpc createNPC(World world) {
        if(world.field_72995_K) {
            return null;
        } else {
            EntityCustomNpc npc = new EntityCustomNpc(world);
            return npc.wrappedNPC;
        }
    }

    public ICustomNpc spawnNPC(World world, int x, int y, int z) {
        if(world.field_72995_K) {
            return null;
        } else {
            EntityCustomNpc npc = new EntityCustomNpc(world);
            npc.func_70080_a((double)x + 0.5D, (double)y, (double)z + 0.5D, 0.0F, 0.0F);
            npc.ais.setStartPos((double)x, (double)y, (double)z);
            npc.func_70606_j(npc.func_110138_aP());
            world.func_72838_d(npc);
            return npc.wrappedNPC;
        }
    }

    public static NpcAPI Instance() {
        if(instance == null) {
            instance = new WrapperNpcAPI();
        }

        return instance;
    }

    public EventBus events() {
        return EVENT_BUS;
    }

    public IItemStack getIItemStack(ItemStack itemstack) {
        return (IItemStack)(itemstack != null && !itemstack.func_190926_b()?(IItemStack)itemstack.getCapability(ItemStackWrapper.ITEMSCRIPTEDDATA_CAPABILITY, (EnumFacing)null):ItemStackWrapper.AIR);
    }

    public IWorld getIWorld(WorldServer world) {
        WorldWrapper w = (WorldWrapper)worldCache.get(Integer.valueOf(world.field_73011_w.getDimension()));
        if(w != null) {
            w.world = world;
            return w;
        } else {
            worldCache.put(Integer.valueOf(world.field_73011_w.getDimension()), w = WorldWrapper.createNew(world));
            return w;
        }
    }

    public IWorld getIWorld(int dimensionId) {
        WorldServer[] var2 = CustomNpcs.Server.field_71305_c;
        int var3 = var2.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            WorldServer world = var2[var4];
            if(world.field_73011_w.getDimension() == dimensionId) {
                return this.getIWorld(world);
            }
        }

        throw new CustomNPCsException("Unknown dimension id: " + dimensionId, new Object[0]);
    }

    public IContainer getIContainer(IInventory inventory) {
        return new ContainerWrapper(inventory);
    }

    public IContainer getIContainer(Container container) {
        return (IContainer)(container instanceof ContainerNpcInterface?ContainerNpcInterface.getOrCreateIContainer((ContainerNpcInterface)container):new ContainerWrapper(container));
    }

    public IFactionHandler getFactions() {
        this.checkWorld();
        return FactionController.instance;
    }

    private void checkWorld() {
        if(CustomNpcs.Server == null || CustomNpcs.Server.func_71241_aa()) {
            throw new CustomNPCsException("No world is loaded right now", new Object[0]);
        }
    }

    public IRecipeHandler getRecipes() {
        this.checkWorld();
        return RecipeController.instance;
    }

    public IQuestHandler getQuests() {
        this.checkWorld();
        return QuestController.instance;
    }

    public IWorld[] getIWorlds() {
        this.checkWorld();
        IWorld[] worlds = new IWorld[CustomNpcs.Server.field_71305_c.length];

        for(int i = 0; i < CustomNpcs.Server.field_71305_c.length; ++i) {
            worlds[i] = this.getIWorld(CustomNpcs.Server.field_71305_c[i]);
        }

        return worlds;
    }

    public File getGlobalDir() {
        return CustomNpcs.Dir;
    }

    public File getWorldDir() {
        return CustomNpcs.getWorldSaveDirectory();
    }

    public void registerCommand(CommandNoppesBase command) {
        CustomNpcs.NoppesCommand.registerCommand(command);
    }

    public INbt getINbt(NBTTagCompound compound) {
        return compound == null?new NBTWrapper(new NBTTagCompound()):new NBTWrapper(compound);
    }

    public INbt stringToNbt(String str) {
        if(str != null && !str.isEmpty()) {
            try {
                return this.getINbt(NBTJsonUtil.Convert(str));
            } catch (JsonException var3) {
                throw new CustomNPCsException(var3, "Failed converting " + str, new Object[0]);
            }
        } else {
            throw new CustomNPCsException("Cant cast empty string to nbt", new Object[0]);
        }
    }

    public IDamageSource getIDamageSource(DamageSource damagesource) {
        return new DamageSourceWrapper(damagesource);
    }

    public IDialogHandler getDialogs() {
        return DialogController.instance;
    }

    public ICloneHandler getClones() {
        return ServerCloneController.Instance;
    }

    public String executeCommand(IWorld world, String command) {
        FakePlayer player = EntityNPCInterface.CommandPlayer;
        player.func_70029_a(world.getMCWorld());
        player.func_70107_b(0.0D, 0.0D, 0.0D);
        return NoppesUtilServer.runCommand(world.getMCWorld(), BlockPos.field_177992_a, "API", command, (EntityPlayer)null, player);
    }

    public INbt getRawPlayerData(String uuid) {
        return this.getINbt(PlayerData.loadPlayerData(uuid));
    }

    public IPlayerMail createMail(String sender, String subject) {
        PlayerMail mail = new PlayerMail();
        mail.sender = sender;
        mail.subject = subject;
        return mail;
    }

    public ICustomGui createCustomGui(int id, int width, int height, boolean pauseGame) {
        return new CustomGuiWrapper(id, width, height, pauseGame);
    }

    public String getRandomName(int dictionary, int gender) {
        return CustomNpcs.MARKOV_GENERATOR[dictionary].fetch(gender);
    }
}
