package noppes.npcs.scripted;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.eventhandler.EventBus;
import foxz.command.ScriptedCommand;
import kamkeel.npcs.controllers.AttributeController;
import kamkeel.npcs.controllers.ProfileController;
import net.minecraft.block.Block;
import net.minecraft.command.CommandHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.INpc;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemEditableBook;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemWritableBook;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.api.AbstractNpcAPI;
import noppes.npcs.api.IBlock;
import noppes.npcs.api.ICommand;
import noppes.npcs.api.IContainer;
import noppes.npcs.api.IDamageSource;
import noppes.npcs.api.INbt;
import noppes.npcs.api.IParticle;
import noppes.npcs.api.IPos;
import noppes.npcs.api.ISkinOverlay;
import noppes.npcs.api.ITileEntity;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.entity.ICustomNpc;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.gui.ICustomGui;
import noppes.npcs.api.handler.IActionManager;
import noppes.npcs.api.handler.IAnimationHandler;
import noppes.npcs.api.handler.IAttributeHandler;
import noppes.npcs.api.handler.ICloneHandler;
import noppes.npcs.api.handler.ICustomEffectHandler;
import noppes.npcs.api.handler.IDialogHandler;
import noppes.npcs.api.handler.IFactionHandler;
import noppes.npcs.api.handler.IMagicHandler;
import noppes.npcs.api.handler.INaturalSpawnsHandler;
import noppes.npcs.api.handler.IPartyHandler;
import noppes.npcs.api.handler.IProfileHandler;
import noppes.npcs.api.handler.IQuestHandler;
import noppes.npcs.api.handler.IRecipeHandler;
import noppes.npcs.api.handler.ITransportHandler;
import noppes.npcs.api.handler.data.IAnimation;
import noppes.npcs.api.handler.data.IFrame;
import noppes.npcs.api.handler.data.IFramePart;
import noppes.npcs.api.handler.data.ISound;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.overlay.ICustomOverlay;
import noppes.npcs.compat.PixelmonHelper;
import noppes.npcs.config.ConfigScript;
import noppes.npcs.constants.EnumAnimationPart;
import noppes.npcs.containers.ContainerNpcInterface;
import noppes.npcs.controllers.AnimationController;
import noppes.npcs.controllers.ChunkController;
import noppes.npcs.controllers.CustomEffectController;
import noppes.npcs.controllers.DialogController;
import noppes.npcs.controllers.FactionController;
import noppes.npcs.controllers.MagicController;
import noppes.npcs.controllers.PartyController;
import noppes.npcs.controllers.QuestController;
import noppes.npcs.controllers.RecipeController;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.controllers.ScriptEntityData;
import noppes.npcs.controllers.ServerCloneController;
import noppes.npcs.controllers.SpawnController;
import noppes.npcs.controllers.TransportController;
import noppes.npcs.controllers.data.Animation;
import noppes.npcs.controllers.data.Frame;
import noppes.npcs.controllers.data.FramePart;
import noppes.npcs.controllers.data.SkinOverlay;
import noppes.npcs.controllers.data.action.ActionManager;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.entity.EntityProjectile;
import noppes.npcs.items.ItemLinked;
import noppes.npcs.items.ItemScripted;
import noppes.npcs.scripted.entity.ScriptAnimal;
import noppes.npcs.scripted.entity.ScriptArrow;
import noppes.npcs.scripted.entity.ScriptDBCPlayer;
import noppes.npcs.scripted.entity.ScriptEntity;
import noppes.npcs.scripted.entity.ScriptEntityItem;
import noppes.npcs.scripted.entity.ScriptFishHook;
import noppes.npcs.scripted.entity.ScriptLiving;
import noppes.npcs.scripted.entity.ScriptLivingBase;
import noppes.npcs.scripted.entity.ScriptMonster;
import noppes.npcs.scripted.entity.ScriptPixelmon;
import noppes.npcs.scripted.entity.ScriptPlayer;
import noppes.npcs.scripted.entity.ScriptProjectile;
import noppes.npcs.scripted.entity.ScriptThrowable;
import noppes.npcs.scripted.entity.ScriptVillager;
import noppes.npcs.scripted.gui.ScriptGui;
import noppes.npcs.scripted.item.ScriptCustomItem;
import noppes.npcs.scripted.item.ScriptItemArmor;
import noppes.npcs.scripted.item.ScriptItemBlock;
import noppes.npcs.scripted.item.ScriptItemBook;
import noppes.npcs.scripted.item.ScriptItemStack;
import noppes.npcs.scripted.item.ScriptLinkedItem;
import noppes.npcs.scripted.overlay.ScriptOverlay;
import noppes.npcs.util.CacheHashMap;
import noppes.npcs.util.JsonException;
import noppes.npcs.util.LRUHashMap;
import noppes.npcs.util.NBTJsonUtil;
import noppes.npcs.util.SizeOfObjectUtil;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NpcAPI extends AbstractNpcAPI {
    private static final Map<String, Object> tempData = new HashMap<>();
    private static final Map<Integer, ScriptWorld> worldCache = new LRUHashMap<>(10);
    private static final CacheHashMap<ItemStack, CacheHashMap.CachedObject<ScriptItemStack>> scriptItemCache = new CacheHashMap<>(60 * 1000);
    public static final HashMap<String, Object> engineObjects = new HashMap<>();
    public static final EventBus EVENT_BUS = new EventBus();
    private static AbstractNpcAPI instance = null;

    private final static String API_USER_AGENT = "CNPC+API";
    private static final ExecutorService executorService = Executors.newFixedThreadPool(10);

    private NpcAPI() {
    }

    public static void clearCache() {
        worldCache.clear();
        scriptItemCache.clear();
    }

    public Object getTempData(String key) {
        return tempData.get(key);
    }

    public void setTempData(String key, Object value) {
        tempData.put(key, value);
    }

    public boolean hasTempData(String key) {
        return tempData.containsKey(key);
    }

    public void removeTempData(String key) {
        tempData.remove(key);
    }

    public void clearTempData() {
        tempData.clear();
    }

    public String[] getTempDataKeys() {
        return tempData.keySet().toArray(new String[0]);
    }

    public Object getStoredData(String key) {
        NBTTagCompound compound = ScriptController.Instance.compound;
        if (!compound.hasKey(key))
            return null;
        NBTBase base = compound.getTag(key);
        if (base instanceof NBTBase.NBTPrimitive)
            return ((NBTBase.NBTPrimitive) base).func_150286_g();
        return ((NBTTagString) base).func_150285_a_();
    }

    public void setStoredData(String key, Object value) {
        NBTTagCompound compound = ScriptController.Instance.compound;
        if (value instanceof Number)
            compound.setDouble(key, ((Number) value).doubleValue());
        else if (value instanceof String)
            compound.setString(key, (String) value);
        ScriptController.Instance.shouldSave = true;
    }

    public boolean hasStoredData(String key) {
        return ScriptController.Instance.compound.hasKey(key);
    }

    public void removeStoredData(String key) {
        ScriptController.Instance.compound.removeTag(key);
        ScriptController.Instance.shouldSave = true;
    }

    public void clearStoredData() {
        ScriptController.Instance.compound = new NBTTagCompound();
        ScriptController.Instance.shouldSave = true;
    }

    public String[] getStoredDataKeys() {
        NBTTagCompound compound = ScriptController.Instance.compound;
        if (compound != null) {
            Set keySet = compound.func_150296_c();
            List<String> list = new ArrayList<>();
            for (Object o : keySet) {
                list.add((String) o);
            }
            String[] array = list.toArray(new String[list.size()]);
            return array;
        }
        return new String[0];
    }

    public void registerICommand(ICommand command) {
        ((CommandHandler) CustomNpcs.getServer().getCommandManager()).registerCommand((ScriptedCommand) command);
    }

    public ICommand getICommand(String commandName, int priorityLevel) {
        return new ScriptedCommand(commandName, priorityLevel);
    }

    public void addGlobalObject(String key, Object obj) {
        NpcAPI.engineObjects.put(key, obj);
    }

    public void removeGlobalObject(String key) {
        NpcAPI.engineObjects.remove(key);
    }

    public boolean hasGlobalObject(String key) {
        return NpcAPI.engineObjects.containsKey(key);
    }

    public HashMap<String, Object> getEngineObjects() {
        return NpcAPI.engineObjects;
    }

    public long sizeOfObject(Object obj) {
        return SizeOfObjectUtil.sizeOfObject(obj);
    }

    public void stopServer() {
        MinecraftServer.getServer().initiateShutdown();
    }

    public int getCurrentPlayerCount() {
        return MinecraftServer.getServer().getConfigurationManager().getCurrentPlayerCount();
    }

    public int getMaxPlayers() {
        return MinecraftServer.getServer().getConfigurationManager().getMaxPlayers();
    }

    public void kickAllPlayers() {
        MinecraftServer.getServer().getConfigurationManager().removeAllPlayers();
    }

    public boolean isHardcore() {
        return MinecraftServer.getServer().isHardcore();
    }

    public File getFile(String path) {
        return MinecraftServer.getServer().getFile(path);
    }

    public String getServerOwner() {
        return MinecraftServer.getServer().getServerOwner();
    }

    public IFactionHandler getFactions() {
        this.checkWorld();
        return FactionController.getInstance();
    }

    public IRecipeHandler getRecipes() {
        this.checkWorld();
        return RecipeController.Instance;
    }

    public IQuestHandler getQuests() {
        this.checkWorld();
        return QuestController.Instance;
    }

    public IDialogHandler getDialogs() {
        return DialogController.Instance;
    }

    public ICloneHandler getClones() {
        return ServerCloneController.Instance;
    }

    public INaturalSpawnsHandler getNaturalSpawns() {
        return SpawnController.Instance;
    }

    public IProfileHandler getProfileHandler() {
        return ProfileController.Instance;
    }

    public ICustomEffectHandler getCustomEffectHandler() {
        return CustomEffectController.Instance;
    }

    public IMagicHandler getMagicHandler() {
        return MagicController.getInstance();
    }

    public IPartyHandler getPartyHandler() {
        return PartyController.Instance();
    }

    public IAttributeHandler getAttributeHandler() {
        return AttributeController.Instance;
    }

    public ITransportHandler getLocations() {
        return TransportController.getInstance();
    }

    public IAnimationHandler getAnimations() {
        this.checkWorld();
        return AnimationController.Instance;
    }

    @Override
    public String[] getAllBiomeNames() {
        List<String> biomes = new ArrayList<>();
        Set<BiomeGenBase> allBiomes = new HashSet<BiomeGenBase>();
        for (Type type : BiomeDictionary.Type.values()) {
            Collections.addAll(allBiomes, BiomeDictionary.getBiomesForType(type));
        }
        for (BiomeGenBase base : BiomeGenBase.getBiomeGenArray()) {
            if (base != null)
                allBiomes.add(base);
        }
        for (BiomeGenBase base : allBiomes) {
            if (base != null && base.biomeName != null) {
                biomes.add(base.biomeName);
            }
        }

        return biomes.toArray(new String[]{});
    }


    public static Boolean dbcLoaded = null;

    public IEntity<?> getIEntity(Entity entity) {
        if (entity == null)
            return null;
        if (entity instanceof EntityNPCInterface)
            return ((EntityNPCInterface) entity).wrappedNPC;
        else {
            ScriptEntityData data = (ScriptEntityData) entity.getExtendedProperties("ScriptedObject");
            if (data == null) {
                if (entity instanceof EntityPlayerMP) {
                    if (dbcLoaded == null) {
                        dbcLoaded = Loader.isModLoaded("jinryuujrmcore");
                    }
                    data = dbcLoaded ? new ScriptEntityData(new ScriptDBCPlayer<>((EntityPlayerMP) entity)) : new ScriptEntityData(new ScriptPlayer<>((EntityPlayerMP) entity));
                } else if (PixelmonHelper.isPixelmon(entity))
                    return new ScriptPixelmon<EntityTameable>((EntityTameable) entity);
                else if (entity instanceof EntityAnimal)
                    data = new ScriptEntityData(new ScriptAnimal<>((EntityAnimal) entity));
                else if (entity instanceof EntityMob)
                    data = new ScriptEntityData(new ScriptMonster<>((EntityMob) entity));
                else if (entity instanceof EntityVillager)
                    data = new ScriptEntityData(new ScriptVillager<>((EntityVillager) entity));
                else if (entity instanceof EntityLiving)
                    data = new ScriptEntityData(new ScriptLiving<>((EntityLiving) entity));
                else if (entity instanceof EntityLivingBase)
                    data = new ScriptEntityData(new ScriptLivingBase<>((EntityLivingBase) entity));
                else if (entity instanceof EntityProjectile)
                    data = new ScriptEntityData(new ScriptProjectile<>((EntityProjectile) entity));
                else if (entity instanceof EntityItem)
                    data = new ScriptEntityData(new ScriptEntityItem<>((EntityItem) entity));
                else if (entity instanceof EntityThrowable)
                    data = new ScriptEntityData(new ScriptThrowable<>((EntityThrowable) entity));
                else if (entity instanceof EntityArrow)
                    data = new ScriptEntityData(new ScriptArrow<>((EntityArrow) entity));
                else if (entity instanceof EntityFishHook)
                    data = new ScriptEntityData(new ScriptFishHook<>((EntityFishHook) entity));
                else
                    data = new ScriptEntityData(new ScriptEntity<>(entity));
                entity.registerExtendedProperties("ScriptedObject", data);
            }
            return data.base;
        }
    }

    public INpc[] getChunkLoadingNPCs() {
        ArrayList<INpc> list = new ArrayList<>();
        Set<Entity> npcSet = ChunkController.Instance.tickets.keySet();
        for (Entity entity : npcSet) {
            if (entity instanceof EntityNPCInterface) {
                list.add((INpc) NpcAPI.Instance().getIEntity(entity));
            }
        }

        return list.toArray(new INpc[0]);
    }

    public IEntity<?>[] getLoadedEntities() {
        ArrayList<IEntity<?>> list = new ArrayList<>();

        for (IWorld world : worldCache.values()) {
            for (Object obj : world.getMCWorld().loadedEntityList) {
                list.add(NpcAPI.Instance().getIEntity((Entity) obj));
            }
        }

        return list.toArray(new IEntity[0]);
    }

    public IBlock getIBlock(IWorld world, int x, int y, int z) {
        Block block = world.getMCWorld().getBlock(x, y, z);
        if (block == null || block.isAir(world.getMCWorld(), x, y, z))
            return null;

        return new ScriptBlock(world.getMCWorld(), world.getMCWorld().getBlock(x, y, z), new BlockPos(x, y, z));
    }

    public IBlock getIBlock(IWorld world, IPos pos) {
        return pos == null ? null : this.getIBlock(world, pos.getX(), pos.getY(), pos.getZ());
    }

    public ITileEntity getITileEntity(IWorld world, IPos pos) {
        if (pos == null) {
            return null;
        }

        TileEntity tileEntity = world.getMCWorld().getTileEntity(pos.getX(), pos.getY(), pos.getZ());
        return this.getITileEntity(tileEntity);
    }

    public ITileEntity getITileEntity(IWorld world, int x, int y, int z) {
        TileEntity tileEntity = world.getMCWorld().getTileEntity(x, y, z);
        return this.getITileEntity(tileEntity);
    }

    public ITileEntity getITileEntity(TileEntity tileEntity) {
        return tileEntity == null ? null : new ScriptTileEntity<>(tileEntity);
    }

    public IPos getIPos(BlockPos pos) {
        return new ScriptBlockPos(pos);
    }

    public IPos getIPos(double x, double y, double z) {
        return this.getIPos(new BlockPos(x, y, z));
    }

    public IPos getIPos(int x, int y, int z) {
        return this.getIPos((double) x, (double) y, (double) z);
    }

    public IPos getIPos(float x, float y, float z) {
        return this.getIPos((double) x, (double) y, (double) z);
    }

    public IPos getIPos(long serializedPos) {
        return this.getIPos(BlockPos.fromLong(serializedPos));
    }

    public IPos[] getAllInBox(IPos from, IPos to, boolean sortByDistance) {
        ArrayList<IPos> list = new ArrayList<>();
        if (from != null && to != null) {
            Iterator<BlockPos> posIterable = BlockPos.getAllInBox(from.getMCPos(), to.getMCPos()).iterator();
            posIterable.forEachRemaining(BlockPos -> list.add(this.getIPos(BlockPos)));
            if (sortByDistance) {
                list.sort(Comparator.comparingDouble(pos -> pos.distanceTo(from)));
            }
        }
        return list.toArray(new IPos[0]);
    }

    public IPos[] getAllInBox(IPos from, IPos to) {
        return this.getAllInBox(from, to, true);
    }

    public INbt getINbt(NBTTagCompound nbtTagCompound) {
        return nbtTagCompound == null ? new ScriptNbt(new NBTTagCompound()) : new ScriptNbt(nbtTagCompound);
    }

    public INbt stringToNbt(String str) {
        if (str != null && !str.isEmpty()) {
            try {
                return this.getINbt(NBTJsonUtil.Convert(str));
            } catch (JsonException var3) {
                throw new CustomNPCsException(var3, "Failed converting " + str);
            }
        } else {
            throw new CustomNPCsException("Cant cast empty string to nbt");
        }
    }

    public ICustomNpc<?> createNPC(IWorld world) {
        if (world.getMCWorld().isRemote) {
            return null;
        } else {
            EntityCustomNpc npc = new EntityCustomNpc(world.getMCWorld());
            return npc.wrappedNPC;
        }
    }

    public ICustomNpc<?> spawnNPC(IWorld world, int x, int y, int z) {
        if (world.getMCWorld().isRemote) {
            return null;
        } else {
            EntityCustomNpc npc = new EntityCustomNpc(world.getMCWorld());
            npc.setPositionAndRotation((double) x + 0.5D, y, (double) z + 0.5D, 0.0F, 0.0F);
            npc.setHealth(npc.getMaxHealth());
            world.getMCWorld().spawnEntityInWorld(npc);
            return npc.wrappedNPC;
        }
    }

    public ICustomNpc<?> spawnNPC(IWorld world, IPos pos) {
        if (pos == null) {
            return null;
        }
        return this.spawnNPC(world, pos.getX(), pos.getY(), pos.getZ());
    }

    public static AbstractNpcAPI Instance() {
        if (instance == null) {
            instance = new NpcAPI();
        }

        return instance;
    }

    public EventBus events() {
        return EVENT_BUS;
    }

    public IItemStack getIItemStack(ItemStack itemstack) {
        if (itemstack == null)
            return null;

        synchronized (scriptItemCache) {
            ScriptItemStack scriptStack;
            if (scriptItemCache.containsKey(itemstack)) {
                scriptStack = scriptItemCache.get(itemstack).getObject();
            } else {
                if (itemstack.getItem() instanceof ItemLinked) {
                    scriptStack = new ScriptLinkedItem(itemstack);
                } else if (itemstack.getItem() instanceof ItemScripted) {
                    scriptStack = new ScriptCustomItem(itemstack);
                } else if (itemstack.getItem() instanceof ItemArmor) {
                    scriptStack = new ScriptItemArmor(itemstack);
                } else if (itemstack.getItem() instanceof ItemWritableBook || itemstack.getItem() instanceof ItemEditableBook || itemstack.getItem() == Items.written_book || itemstack.getItem() == Items.writable_book) {
                    scriptStack = new ScriptItemBook(itemstack);
                } else if (itemstack.getItem() instanceof ItemBlock) {
                    scriptStack = new ScriptItemBlock(itemstack);
                } else {
                    scriptStack = new ScriptItemStack(itemstack);
                }
                scriptItemCache.put(itemstack, new CacheHashMap.CachedObject<>(scriptStack));
            }
            return scriptStack;
        }
    }

    public IItemStack createItemFromNBT(INbt nbt) {
        return getIItemStack(ItemStack.loadItemStackFromNBT(nbt.getMCNBT()));
    }

    public IItemStack createItem(String id, int damage, int size) {
        Item item = (Item) Item.itemRegistry.getObject(id);
        if (item == null)
            return null;

        return getIItemStack(new ItemStack(item, size, damage));
    }

    public IWorld getIWorld(World world) {
        ScriptWorld w = worldCache.get(world.provider.dimensionId);
        if (w != null) {
            w.world.provider.dimensionId = world.provider.dimensionId;
        } else {
            worldCache.put(world.provider.dimensionId, w = ScriptWorld.createNew(world.provider.dimensionId));
        }
        return w;
    }

    public IWorld getIWorld(int dimensionId) {
        WorldServer[] var2 = CustomNpcs.getServer().worldServers;

        for (WorldServer world : var2) {
            if (world.provider.dimensionId == dimensionId) {
                return this.getIWorld(world);
            }
        }

        throw new CustomNPCsException("Unknown dimension id: " + dimensionId);
    }

    public IWorld getIWorldLoad(int dimensionId) {
        try {
            IWorld iWorld = this.getIWorld(dimensionId);
            if (iWorld != null)
                return iWorld;
        } catch (CustomNPCsException ignored) {
        }

        WorldServer worldServer = CustomNpcs.getServer().worldServerForDimension(dimensionId);
        if (worldServer != null) {
            return this.getIWorld(worldServer);
        }

        throw new CustomNPCsException("Unknown dimension id: " + dimensionId);
    }

    public IContainer getIContainer(IInventory inventory) {
        return new ScriptContainer(inventory);
    }

    public IContainer getIContainer(Container container) {
        return container instanceof ContainerNpcInterface ? ContainerNpcInterface.getOrCreateIContainer((ContainerNpcInterface) container) : new ScriptContainer(container);
    }

    public IActionManager getActionManager() {
        return ActionManager.GLOBAL;
    }

    private void checkWorld() {
        if (CustomNpcs.getServer() == null || CustomNpcs.getServer().isServerStopped()) {
            throw new CustomNPCsException("No world is loaded right now");
        }
    }

    public IWorld[] getIWorlds() {
        this.checkWorld();
        IWorld[] worlds = new IWorld[CustomNpcs.getServer().worldServers.length];

        for (int i = 0; i < CustomNpcs.getServer().worldServers.length; ++i) {
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

    public IDamageSource getIDamageSource(IEntity<?> entity) {
        if (entity.getType() == 1)//if player
            return new ScriptDamageSource(new EntityDamageSource("player", entity.getMCEntity()));
        else
            return new ScriptDamageSource(new EntityDamageSource(entity.getTypeName(), entity.getMCEntity()));
    }

    public void executeCommand(IWorld world, String command) {
        //if(!world.getMCWorld().isRemote)
        NoppesUtilServer.runCommand(world.getMCWorld(), "API", command);
    }

    public String getRandomName(int dictionary, int gender) {
        return CustomNpcs.MARKOV_GENERATOR[dictionary].fetch(gender);
    }

    /**
     * @param username The username of the player to be returned
     * @return The Player with name. Null is returned when the player isn't found
     */
    public IPlayer<?> getPlayer(String username) {
        EntityPlayerMP player = MinecraftServer.getServer().getConfigurationManager().func_152612_a(username);
        return player == null ? null : (IPlayer<?>) NpcAPI.Instance().getIEntity(player);
    }

    public IPlayer<?>[] getAllServerPlayers() {
        List<?> list = MinecraftServer.getServer().getConfigurationManager().playerEntityList;
        IPlayer<?>[] arr = new IPlayer[list.size()];
        for (int i = 0; i < list.size(); i++) {
            arr[i] = (IPlayer<?>) NpcAPI.Instance().getIEntity((EntityPlayerMP) list.get(i));
        }

        return arr;
    }

    public String[] getPlayerNames() {
        return MinecraftServer.getServer().getConfigurationManager().getAllUsernames();
    }

    public void playSoundAtEntity(IEntity<?> entity, String sound, float volume, float pitch) {
        entity.getWorld().getMCWorld().playSoundAtEntity(entity.getMCEntity(), sound, volume, pitch);
    }

    public void playSoundToNearExcept(IPlayer<?> player, String sound, float volume, float pitch) {
        player.getWorld().getMCWorld().playSoundToNearExcept(player.getMCEntity(), sound, volume, pitch);
    }

    public String getMOTD() {
        return MinecraftServer.getServer().getMOTD();
    }

    public void setMOTD(String motd) {
        MinecraftServer.getServer().setMOTD(motd);
    }

    public IParticle createParticle(String directory) {
        return new ScriptParticle(directory);
    }

    @Deprecated
    public IParticle createEntityParticle(String directory) {
        return new ScriptParticle(directory);
    }

    public ISound createSound(String directory) {
        return new ScriptSound(directory);
    }

    public void playSound(int id, ISound sound) {
        NoppesUtilServer.playSound(id, (ScriptSound) sound);
    }

    public void playSound(ISound sound) {
        NoppesUtilServer.playSound((ScriptSound) sound);
    }

    public void stopSound(int id) {
        NoppesUtilServer.stopSound(id);
    }

    public void pauseSounds() {
        NoppesUtilServer.pauseSounds();
    }

    public void continueSounds() {
        NoppesUtilServer.continueSounds();
    }

    public void stopSounds() {
        NoppesUtilServer.stopSounds();
    }

    public int getServerTime() {
        return MinecraftServer.getServer().getTickCounter();
    }

    public boolean arePlayerScriptsEnabled() {
        return ConfigScript.GlobalPlayerScripts;
    }

    public boolean areForgeScriptsEnabled() {
        return ConfigScript.GlobalForgeScripts;
    }

    public boolean areGlobalNPCScriptsEnabled() {
        return ConfigScript.GlobalNPCScripts;
    }

    public void enablePlayerScripts(boolean enable) {
        ConfigScript.GlobalPlayerScripts = enable;
    }

    public void enableForgeScripts(boolean enable) {
        ConfigScript.GlobalForgeScripts = enable;
    }

    public void enableGlobalNPCScripts(boolean enable) {
        ConfigScript.GlobalNPCScripts = enable;
    }

    public ICustomGui createCustomGui(int id, int width, int height, boolean pauseGame) {
        return new ScriptGui(id, width, height, pauseGame);
    }

    public ICustomOverlay createCustomOverlay(int id) {
        return new ScriptOverlay(id);
    }

    public ISkinOverlay createSkinOverlay(String texture) {
        return new SkinOverlay(texture);
    }

    public String millisToTime(long millis) {
        return NoppesUtilServer.millisToTime(millis);
    }

    public String ticksToTime(long ticks) {
        return this.millisToTime(ticks * 50);
    }

    public IAnimation createAnimation(String name) {
        return new Animation(-1, name);
    }

    public IAnimation createAnimation(String name, float speed, byte smooth) {
        return new Animation(-1, name, speed, smooth);
    }

    public IFrame createFrame(int duration) {
        return new Frame(duration);
    }

    public IFrame createFrame(int duration, float speed, byte smooth) {
        return new Frame(duration, speed, smooth);
    }

    public IFramePart createPart(String name) {
        try {
            return new FramePart(EnumAnimationPart.valueOf(name));
        } catch (IllegalArgumentException ignored) {
            throw new CustomNPCsException("Invalid frame part name: " + name);
        }
    }

    public IFramePart createPart(String name, float[] rotation, float[] pivot) {
        if (rotation.length != 3 || pivot.length != 3) {
            throw new CustomNPCsException("Rotation and pivot arrays for frame parts must have a length of 3.");
        }

        FramePart part = (FramePart) this.createPart(name);
        part.setRotations(rotation);
        part.setPivots(pivot);
        return part;
    }

    public IFramePart createPart(String name, float[] rotation, float[] pivot, float speed, byte smooth) {
        FramePart part = (FramePart) this.createPart(name, rotation, pivot);
        part.setSpeed(speed);
        part.setSmooth(smooth);
        return part;
    }

    public IFramePart createPart(int partId) {
        for (EnumAnimationPart part : EnumAnimationPart.values()) {
            if (part.ordinal() == partId) {
                return new FramePart(part);
            }
        }
        throw new CustomNPCsException("Invalid frame part ID: " + partId);
    }

    public IFramePart createPart(int partId, float[] rotation, float[] pivot) {
        if (rotation.length != 3 || pivot.length != 3) {
            throw new CustomNPCsException("Rotation and pivot arrays for frame parts must have a length of 3.");
        }

        FramePart part = (FramePart) this.createPart(partId);
        part.setRotations(rotation);
        part.setPivots(pivot);
        return part;
    }

    public IFramePart createPart(int partId, float[] rotation, float[] pivot, float speed, byte smooth) {
        FramePart part = (FramePart) this.createPart(partId, rotation, pivot);
        part.setSpeed(speed);
        part.setSmooth(smooth);
        return part;
    }

    public void postJsonHTTP(String url, String jsonPayload) {
        this.postJsonHTTP(url, jsonPayload, API_USER_AGENT);
    }

    public void postJsonHTTP(String url, String jsonPayload, String userAgent) {
        this.postHTTP(url, jsonPayload, "application/json", userAgent);
    }

    public void postHTTP(String url, String params, String contentType) {
        this.postHTTP(url, params, contentType, API_USER_AGENT);
    }

    public void postHTTP(String url, String params, String contentType, String userAgent) {
        executorService.submit(() -> {
            try {
                HttpURLConnection con = NpcAPI.getConnection("POST", url, userAgent, contentType);

                // For POST only - set doOutput to true
                con.setDoOutput(true);
                DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                wr.writeBytes(params);
                wr.flush();
                wr.close();

                int responseCode = con.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) { // success
                    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();
                }
            } catch (Exception ignored) {
            }
        });
    }

    private static HttpURLConnection getConnection(String requestMethod, String url, String userAgent, String contentType) throws IOException {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        con.setRequestMethod(requestMethod);
        con.setRequestProperty("User-Agent", userAgent);
        //con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        if (!contentType.isEmpty()) {
            con.setRequestProperty("Content-Type", contentType);
        }

        return con;
    }
}
