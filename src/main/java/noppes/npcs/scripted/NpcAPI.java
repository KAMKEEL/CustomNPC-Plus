//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package noppes.npcs.scripted;

import cpw.mods.fml.common.eventhandler.EventBus;
import jdk.nashorn.internal.ir.debug.ObjectSizeCalculator;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.INpc;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.BiomeGenBase;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.api.*;
import noppes.npcs.containers.ContainerNpcInterface;
import noppes.npcs.controllers.*;
import noppes.npcs.controllers.data.SkinOverlay;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.items.ItemScripted;
import noppes.npcs.scripted.entity.*;
import noppes.npcs.scripted.gui.ScriptGui;
import noppes.npcs.api.entity.ICustomNpc;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.gui.ICustomGui;
import noppes.npcs.api.handler.*;
import noppes.npcs.api.handler.data.ISound;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.overlay.ICustomOverlay;
import noppes.npcs.scripted.item.*;
import noppes.npcs.scripted.overlay.ScriptOverlay;
import noppes.npcs.util.JsonException;
import noppes.npcs.util.LRUHashMap;
import noppes.npcs.util.NBTJsonUtil;

import java.io.File;
import java.util.*;

public class NpcAPI extends AbstractNpcAPI {
    private static final Map<Integer, ScriptWorld> worldCache = new LRUHashMap<>(10);
    public static final EventBus EVENT_BUS = new EventBus();
    private static AbstractNpcAPI instance = null;

    public NpcAPI() {
    }

    public static void clearCache() {
        worldCache.clear();
    }

    public long sizeOfObject(Object obj) {
        return ObjectSizeCalculator.getObjectSize(obj);
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
        return RecipeController.instance;
    }

    public IQuestHandler getQuests() {
        this.checkWorld();
        return QuestController.instance;
    }

    public IDialogHandler getDialogs() {
        return DialogController.instance;
    }

    public ICloneHandler getClones() {
        return ServerCloneController.Instance;
    }

    public INaturalSpawnsHandler getNaturalSpawns() { return SpawnController.instance; }

    public ITransportHandler getLocations() {
        return TransportController.getInstance();
    }

    @Override
    public String[] getAllBiomeNames() {
        List<String> biomes = new ArrayList<>();
        for (BiomeGenBase base : BiomeGenBase.getBiomeGenArray()) {
            if (base != null && base.biomeName != null) {
                biomes.add(base.biomeName);
            }
        }

        return biomes.toArray(new String[]{});
    }

    public IEntity<?> getIEntity(Entity entity) {
        if(entity == null)
            return null;
        if(entity instanceof EntityNPCInterface)
            return ((EntityNPCInterface)entity).wrappedNPC;
        else{
            ScriptEntityData data = (ScriptEntityData) entity.getExtendedProperties("ScriptedObject");
            if(data != null)
                return data.base;
            if(entity instanceof EntityPlayerMP)
                data = new ScriptEntityData(new ScriptPlayer<>((EntityPlayerMP) entity));
            else if(PixelmonHelper.isPixelmon(entity))
                return new ScriptPixelmon<EntityTameable>((EntityTameable) entity);
            else if(entity instanceof EntityAnimal)
                data = new ScriptEntityData(new ScriptAnimal<>((EntityAnimal) entity));
            else if(entity instanceof EntityMob)
                data = new ScriptEntityData(new ScriptMonster<>((EntityMob) entity));
            else if(entity instanceof EntityLiving)
                data = new ScriptEntityData(new ScriptLiving<>((EntityLiving) entity));
            else if(entity instanceof EntityLivingBase)
                data = new ScriptEntityData(new ScriptLivingBase<>((EntityLivingBase)entity));
            else
                data = new ScriptEntityData(new ScriptEntity<>(entity));
            entity.registerExtendedProperties("ScriptedObject", data);
            return data.base;
        }
    }

    public INpc[] getChunkLoadingNPCs() {
        ArrayList<INpc> list = new ArrayList<>();
        Set<Entity> npcSet = ChunkController.instance.tickets.keySet();
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
        if(block == null || block.isAir(world.getMCWorld(), x, y, z))
            return null;

        return new ScriptBlock(world.getMCWorld(), world.getMCWorld().getBlock(x, y, z), new BlockPos(x,y,z));
    }

    public IBlock getIBlock(IWorld world, IPos pos) {
        return this.getIBlock(world, pos.getX(),pos.getY(),pos.getZ());
    }

    public ITileEntity getITileEntity(IWorld world, IPos pos) {
        TileEntity tileEntity = world.getMCWorld().getTileEntity(pos.getX(), pos.getY(),pos.getZ());
        return this.getITileEntity(tileEntity);
    }

    public ITileEntity getITileEntity(IWorld world, int x, int y, int z) {
        TileEntity tileEntity = world.getMCWorld().getTileEntity(x,y,z);
        return this.getITileEntity(tileEntity);
    }

    public ITileEntity getITileEntity(TileEntity tileEntity) {
        return tileEntity == null ? null : new ScriptTileEntity<>(tileEntity);
    }

    public IPos getIPos(BlockPos pos) {
        return new ScriptBlockPos(pos);
    }

    public IPos getIPos(int x, int y, int z) {
        return new ScriptBlockPos(new BlockPos(x,y,z));
    }

    public IPos getIPos(double x, double y, double z) {
        return this.getIPos((int)x,(int)y,(int)z);
    }

    public IPos getIPos(float x, float y, float z) {
        return this.getIPos((int)x,(int)y,(int)z);
    }

    public IPos[] getAllInBox(IPos from, IPos to, boolean sortByDistance) {
        Iterator<BlockPos> posIterable = BlockPos.getAllInBox(from.getMCPos(),to.getMCPos()).iterator();
        ArrayList<IPos> list = new ArrayList<>();
        posIterable.forEachRemaining(BlockPos -> list.add(this.getIPos(BlockPos)));
        if (sortByDistance) {
            list.sort(Comparator.comparingDouble(pos -> pos.distanceTo(from)));
        }
        return list.toArray(new IPos[0]);
    }

    public IPos[] getAllInBox(IPos from, IPos to) {
        return this.getAllInBox(from,to,true);
    }

    public INbt getINbt(NBTTagCompound nbtTagCompound) {
        return nbtTagCompound == null?new ScriptNbt(new NBTTagCompound()):new ScriptNbt(nbtTagCompound);
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
            npc.setPositionAndRotation((double)x + 0.5D, y, (double)z + 0.5D, 0.0F, 0.0F);
            npc.setHealth(npc.getMaxHealth());
            world.getMCWorld().spawnEntityInWorld(npc);
            return npc.wrappedNPC;
        }
    }

    public ICustomNpc<?> spawnNPC(IWorld world, IPos pos) {
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
        if(itemstack == null)
            return null;

        if (itemstack.getItem() instanceof ItemScripted) {
            return new ScriptCustomItem(itemstack);
        } else if(itemstack.getItem() instanceof ItemArmor) {
            return new ScriptItemArmor(itemstack);
        } else if(itemstack.getItem() instanceof ItemBook) {
            return new ScriptItemBook(itemstack);
        } else if(itemstack.getItem() instanceof ItemBlock) {
            return new ScriptItemBlock(itemstack);
        } else {
            return new ScriptItemStack(itemstack);
        }
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

    public IContainer getIContainer(IInventory inventory) {
        return new ScriptContainer(inventory);
    }

    public IContainer getIContainer(Container container) {
        return container instanceof ContainerNpcInterface ? ContainerNpcInterface.getOrCreateIContainer((ContainerNpcInterface)container) : new ScriptContainer(container);
    }

    private void checkWorld() {
        if (CustomNpcs.getServer() == null || CustomNpcs.getServer().isServerStopped()) {
            throw new CustomNPCsException("No world is loaded right now");
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

    public IDamageSource getIDamageSource(IEntity<?> entity) {
        if(entity.getType() == 1)//if player
            return new ScriptDamageSource(new EntityDamageSource("player",entity.getMCEntity()));
        else
            return new ScriptDamageSource(new EntityDamageSource(entity.getTypeName(),entity.getMCEntity()));
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

    public IPlayer<?>[] getAllServerPlayers(){
        List<?> list = MinecraftServer.getServer().getConfigurationManager().playerEntityList;
        IPlayer<?>[] arr = new IPlayer[list.size()];
        for(int i = 0; i < list.size(); i++){
            arr[i] = (IPlayer<?>) NpcAPI.Instance().getIEntity((EntityPlayerMP)list.get(i));
        }

        return arr;
    }

    public String[] getPlayerNames() {
        return MinecraftServer.getServer().getConfigurationManager().getAllUsernames();
    }

    public void playSoundAtEntity(IEntity<?> entity, String sound, float volume, float pitch){
        entity.getWorld().getMCWorld().playSoundAtEntity(entity.getMCEntity(), sound, volume, pitch);
    }

    public void playSoundToNearExcept(IPlayer<?> player, String sound, float volume, float pitch){
        player.getWorld().getMCWorld().playSoundToNearExcept(player.getMCEntity(), sound, volume, pitch);
    }

    public String getMOTD()
    {
        return MinecraftServer.getServer().getMOTD();
    }

    public void setMOTD(String motd)
    {
        MinecraftServer.getServer().setMOTD(motd);
    }

    public IParticle createParticle(String directory){
        return new ScriptParticle(directory);
    }

    @Deprecated
    public IParticle createEntityParticle(String directory){
        return new ScriptParticle(directory);
    }

    public ISound createSound(String directory) {
        return new ScriptSound(directory);
    }

    public void playSound(int id, ISound sound) {
        NoppesUtilServer.playSound(id, (ScriptSound) sound);
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
        return CustomNpcs.GlobalPlayerScripts;
    }

    public boolean areForgeScriptsEnabled() {
        return CustomNpcs.GlobalForgeScripts;
    }

    public boolean areGlobalNPCScriptsEnabled() {
        return CustomNpcs.GlobalNPCScripts;
    }

    public void enablePlayerScripts(boolean enable) {
        CustomNpcs.GlobalPlayerScripts = enable;
    }

    public void enableForgeScripts(boolean enable) {
        CustomNpcs.GlobalForgeScripts = enable;
    }

    public void enableGlobalNPCScripts(boolean enable) {
        CustomNpcs.GlobalNPCScripts = enable;
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
}
