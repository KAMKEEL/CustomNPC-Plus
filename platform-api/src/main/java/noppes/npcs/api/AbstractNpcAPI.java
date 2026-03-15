package noppes.npcs.api;

import noppes.npcs.api.entity.ICustomNpc;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.gui.ICustomGui;
import noppes.npcs.api.handler.*;
import noppes.npcs.api.handler.data.IAnimation;
import noppes.npcs.api.handler.data.IFrame;
import noppes.npcs.api.handler.data.IFramePart;
import noppes.npcs.api.handler.data.ISound;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.overlay.ICustomOverlay;

import java.io.File;
import java.util.HashMap;

/**
 * This object stores functions available to all scripting handlers through the "API" keyword.
 *
 */
public abstract class AbstractNpcAPI {
    private static AbstractNpcAPI instance = null;

    public AbstractNpcAPI() {
    }

    public abstract Object getTempData(String key);

    public abstract void setTempData(String key, Object value);

    public abstract boolean hasTempData(String key);

    public abstract void removeTempData(String key);

    public abstract void clearTempData();

    public abstract String[] getTempDataKeys();

    public abstract Object getStoredData(String key);

    public abstract void setStoredData(String key, Object value);

    public abstract boolean hasStoredData(String key);

    public abstract void removeStoredData(String key);

    public abstract void clearStoredData();

    public abstract String[] getStoredDataKeys();

    public abstract void registerICommand(ICommand command);

    public abstract ICommand getICommand(String commandName, int priorityLevel);

    public abstract void addGlobalObject(String key, Object obj);

    public abstract void removeGlobalObject(String key);

    public abstract boolean hasGlobalObject(String key);

    public abstract HashMap<String, Object> getEngineObjects();

    public abstract long sizeOfObject(Object obj);

    public abstract void stopServer();

    public abstract int getCurrentPlayerCount();

    public abstract int getMaxPlayers();

    public abstract void kickAllPlayers();

    public abstract boolean isHardcore();

    public abstract File getFile(String path);

    public abstract String getServerOwner();

    public abstract IFactionHandler getFactions();

    public abstract IRecipeHandler getRecipes();

    public abstract IQuestHandler getQuests();

    public abstract IDialogHandler getDialogs();

    public abstract ICloneHandler getClones();

    public abstract INaturalSpawnsHandler getNaturalSpawns();

    public abstract IProfileHandler getProfileHandler();

    public abstract ICustomEffectHandler getCustomEffectHandler();

    public abstract IMagicHandler getMagicHandler();

    public abstract IPartyHandler getPartyHandler();

    public abstract ITransportHandler getLocations();

    public abstract IAnimationHandler getAnimations();

    public abstract ILinkedItemHandler getLinkedItems();

    public abstract IScriptHookHandler getScriptHooks();

    public abstract IAbilityHandler getAbilities();

    public abstract ITelegraphHandler getTelegraphs();

    public abstract IAuctionHandler getAuctions();

    public abstract ITelegraph createTelegraph(String type);

    public abstract String[] getAllBiomeNames();

    public abstract ICustomNpc createNPC(IWorld var1);

    public abstract ICustomNpc spawnNPC(IWorld var1, int var2, int var3, int var4);

    public abstract ICustomNpc spawnNPC(IWorld world, IPos pos);

    public abstract IEntity getIEntity(Object var1);

    public abstract IPlayer getPlayer(String username);

    public abstract Object[] getChunkLoadingNPCs();

    public abstract IEntity[] getLoadedEntities();

    public abstract IBlock getIBlock(IWorld world, int x, int y, int z);

    public abstract IBlock getIBlock(IWorld world, IPos pos);

    public abstract ITileEntity getITileEntity(IWorld world, IPos pos);

    public abstract ITileEntity getITileEntity(IWorld world, int x, int y, int z);

    public abstract ITileEntity getITileEntity(Object tileEntity);

    public abstract IPos getIPos(Object pos);

    public abstract IPos getIPos(int x, int y, int z);

    public abstract IPos getIPos(double x, double y, double z);

    public abstract IPos getIPos(float x, float y, float z);

    public abstract IPos getIPos(long serializedPos);

    public abstract IPos[] getAllInBox(IPos from, IPos to, boolean sortByDistance);

    public abstract IPos[] getAllInBox(IPos from, IPos to);

    public abstract IContainer getIContainer(Object var1);

    public abstract IItemStack getIItemStack(Object var1);

    public abstract IWorld getIWorld(Object var1);

    public abstract IWorld getIWorld(int var1);

    public abstract IWorld getIWorldLoad(int var1);

    public abstract IActionManager getActionManager();

    public abstract IWorld[] getIWorlds();

    public abstract IDamageSource getIDamageSource(Object var1);

    public abstract IDamageSource getIDamageSource(IEntity entity);

    public abstract IEnergyHandler getEnergyHandler();

    public abstract Object events();

    public abstract File getGlobalDir();

    public abstract File getWorldDir();

    public static boolean IsAvailable() {
        try {
            Class.forName("noppes.npcs.scripted.NpcAPI");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static AbstractNpcAPI Instance() {
        if (instance != null) {
            return instance;
        } else if (!IsAvailable()) {
            return null;
        } else {
            try {
                Class<?> c = Class.forName("noppes.npcs.scripted.NpcAPI");
                instance = (AbstractNpcAPI) c.getMethod("Instance").invoke((Object) null);
            } catch (Exception var1) {
                var1.printStackTrace();
            }

            return instance;
        }
    }

    public abstract void executeCommand(IWorld var1, String var2);

    public abstract String getRandomName(int dictionary, int gender);

    public abstract INbt getINbt(Object nbtTagCompound);

    public abstract INbt stringToNbt(String str);

    public abstract IPlayer[] getAllServerPlayers();

    public abstract String[] getPlayerNames();

    public abstract IItemStack createItemFromNBT(INbt nbt);

    public abstract IItemStack createItem(String id, int damage, int size);

    public abstract void playSoundAtEntity(IEntity entity, String sound, float volume, float pitch);

    public abstract void playSoundToNearExcept(IPlayer player, String sound, float volume, float pitch);

    public abstract String getMOTD();

    public abstract void setMOTD(String motd);

    public abstract IParticle createParticle(String directory);

    @Deprecated
    public abstract IParticle createEntityParticle(String directory);

    public abstract ISound createSound(String directory);

    public abstract void playSound(int id, ISound sound);

    public abstract void playSound(ISound sound);

    public abstract void stopSound(int id);

    public abstract void pauseSounds();

    public abstract void continueSounds();

    public abstract void stopSounds();

    public abstract int getServerTime();

    public abstract boolean arePlayerScriptsEnabled();

    public abstract boolean areForgeScriptsEnabled();

    public abstract boolean areGlobalNPCScriptsEnabled();

    public abstract void enablePlayerScripts(boolean enable);

    public abstract void enableForgeScripts(boolean enable);

    public abstract void enableGlobalNPCScripts(boolean enable);

    public abstract ICustomGui createCustomGui(int id, int width, int height, boolean pauseGame);

    public abstract ICustomOverlay createCustomOverlay(int id);

    public abstract ISkinOverlay createSkinOverlay(String texture);

    public abstract String millisToTime(long millis);

    public abstract String ticksToTime(long ticks);

    public abstract IAnimation createAnimation(String name);

    public abstract IAnimation createAnimation(String name, float speed, byte smooth);

    public abstract IFrame createFrame(int duration);

    public abstract IFrame createFrame(int duration, float speed, byte smooth);

    public abstract IFramePart createPart(String name);

    public abstract IFramePart createPart(String name, float[] rotation, float[] pivot);

    public abstract IFramePart createPart(String name, float[] rotation, float[] pivot, float speed, byte smooth);

    public abstract IFramePart createPart(int partId);

    public abstract IFramePart createPart(int partId, float[] rotation, float[] pivot);

    public abstract IFramePart createPart(int partId, float[] rotation, float[] pivot, float speed, byte smooth);
}
