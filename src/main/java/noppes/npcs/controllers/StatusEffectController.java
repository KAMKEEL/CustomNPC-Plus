package noppes.npcs.controllers;

import kamkeel.npcs.controllers.SyncController;
import kamkeel.npcs.network.PacketClient;
import kamkeel.npcs.network.enums.EnumSyncType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.handler.IStatusEffectHandler;
import noppes.npcs.api.handler.data.ICustomEffect;
import noppes.npcs.controllers.data.CustomEffect;
import noppes.npcs.controllers.data.EffectKey;
import noppes.npcs.controllers.data.EffectScriptHandler;
import noppes.npcs.controllers.data.PlayerEffect;
import noppes.npcs.scripted.event.PlayerEvent;
import noppes.npcs.util.NBTJsonUtil;

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.GZIPInputStream;

import static noppes.npcs.scripted.event.PlayerEvent.EffectEvent.ExpirationType;

public class StatusEffectController implements IStatusEffectHandler {

    public static StatusEffectController Instance = new StatusEffectController();
    public HashMap<Integer, HashMap<Integer, CustomEffect>> indexMapper = new HashMap<>();

    public HashMap<Integer, CustomEffect> customEffectsSync = new HashMap<>();
    public HashMap<Integer, CustomEffect> customEffects = new HashMap<>();

    public HashMap<Integer, EffectScriptHandler> customEffectScriptHandlers = new HashMap<>();
    private HashMap<Integer, String> bootOrder;

    private int lastUsedID = 0;
    public ConcurrentHashMap<UUID, Map<EffectKey, PlayerEffect>> playerEffects = new ConcurrentHashMap<>();

    public StatusEffectController() {}

    public static StatusEffectController getInstance() {
        return Instance;
    }

    public void registerEffectMap(int index, HashMap<Integer, CustomEffect> effectHashMap){
        indexMapper.put(index, effectHashMap);
    }

    public void load() {
        lastUsedID = 0;
        playerEffects.clear();
        bootOrder = new HashMap<>();
        LogWriter.info("Loading custom effects...");
        readCustomEffectMap();
        loadCustomEffects();
        LogWriter.info("Done loading custom effects.");
    }

    public void runEffects(EntityPlayer player) {
        Map<EffectKey, PlayerEffect> current = getPlayerEffects(player);
        for (Map.Entry<EffectKey, PlayerEffect> entry : current.entrySet()) {
            int id = entry.getKey().getId();
            CustomEffect effect = get(id);
            if (effect != null) {
                effect.runEffect(player, entry.getValue());
            }
        }
    }

    public void killEffects(EntityPlayer player) {
        Map<EffectKey, PlayerEffect> current = getPlayerEffects(player);
        Iterator<Map.Entry<EffectKey, PlayerEffect>> iterator = current.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<EffectKey, PlayerEffect> entry = iterator.next();
            int id = entry.getKey().getId();
            CustomEffect effect = get(id);
            if (effect != null) {
                if (effect.lossOnDeath) {
                    effect.onRemoved(player, entry.getValue(), ExpirationType.DEATH);
                    iterator.remove();
                }
            } else {
                iterator.remove();
            }
        }
    }

    public boolean has(String name) {
        return get(name) != null;
    }

    public boolean has(int id) {
        return get(id) != null;
    }

    @Override
    public ICustomEffect createEffect(String name) {
        if (has(name))
            return get(name);

        CustomEffect effect = new CustomEffect();
        effect.name = name;

        if (effect.id == -1) {
            int id = getUnusedId();
            while (customEffects.containsKey(id)) {
                id = getUnusedId();
            }
            effect.id = id;
        }
        customEffects.put(effect.id, effect);
        return effect;
    }

    @Override
    public ICustomEffect getEffect(String name) {
        return get(name);
    }

    @Override
    public void deleteEffect(String name) {
        ICustomEffect effect = getEffect(name);
        if (effect != null) {
            CustomEffect foundEffect = customEffects.remove(effect.getID());
            customEffectScriptHandlers.remove(effect.getID());
            if (foundEffect != null && foundEffect.name != null) {
                File dir = this.getDir();
                for (File file : dir.listFiles()) {
                    if (!file.isFile() || !file.getName().endsWith(".json"))
                        continue;
                    if (file.getName().equals(foundEffect.name + ".json")) {
                        file.delete();
                        SyncController.syncRemove(EnumSyncType.CUSTOM_EFFECTS, foundEffect.getID());
                        break;
                    }
                }
                saveEffectLoadMap();
            }
        }
    }

    public void delete(int id) {
        ICustomEffect effect = get(id);
        if (effect != null) {
            CustomEffect foundEffect = customEffects.remove(effect.getID());
            customEffectScriptHandlers.remove(effect.getID());
            if (foundEffect != null && foundEffect.name != null) {
                File dir = this.getDir();
                for (File file : dir.listFiles()) {
                    if (!file.isFile() || !file.getName().endsWith(".json"))
                        continue;
                    if (file.getName().equals(foundEffect.name + ".json")) {
                        file.delete();
                        SyncController.syncRemove(EnumSyncType.CUSTOM_EFFECTS, foundEffect.getID());
                        break;
                    }
                }
                saveEffectLoadMap();
            }
        }
    }

    public int getUnusedId() {
        for (int catid : customEffects.keySet()) {
            if (catid > lastUsedID)
                lastUsedID = catid;
        }
        lastUsedID++;
        return lastUsedID;
    }

    public CustomEffect get(String name) {
        for (CustomEffect effect : customEffects.values()) {
            if (effect.getName().equalsIgnoreCase(name)) {
                return effect;
            }
        }
        return null;
    }

    public CustomEffect get(int id) {
        return customEffects.get(id);
    }

    public Map<EffectKey, PlayerEffect> getPlayerEffects(EntityPlayer player) {
        UUID playerId = NoppesUtilServer.getUUID(player);
        playerEffects.computeIfAbsent(playerId, k -> new ConcurrentHashMap<>());
        return playerEffects.get(playerId);
    }

    public void applyEffect(EntityPlayer player, int id) {
        CustomEffect parent = get(id);
        if (parent != null) {
            PlayerEffect playerEffect = new PlayerEffect(id, parent.length, (byte) 1);
            applyEffect(player, playerEffect);
        }
    }

    public void applyEffect(EntityPlayer player, int id, int duration) {
        CustomEffect parent = get(id);
        if (parent != null) {
            PlayerEffect playerEffect = new PlayerEffect(id, duration, (byte) 1);
            applyEffect(player, playerEffect);
        }
    }

    public void applyEffect(EntityPlayer player, PlayerEffect effect) {
        if (effect == null)
            return;

        CustomEffect parent = get(effect.id);
        if (parent != null) {
            Map<EffectKey, PlayerEffect> currentEffects = getPlayerEffects(player);
            currentEffects.put(new EffectKey(effect.id, effect.index), effect);
            parent.onAdded(player, effect);
        }
    }

    public void removeEffect(EntityPlayer player, PlayerEffect effect, ExpirationType type) {
        if (effect == null)
            return;

        Map<EffectKey, PlayerEffect> currentEffects = getPlayerEffects(player);
        EffectKey key = new EffectKey(effect.id, effect.index);
        if (currentEffects.containsKey(key)) {
            CustomEffect parent = get(effect.id);
            if (parent != null) {
                parent.onRemoved(player, effect, type);
            }
            currentEffects.remove(key);
        }
    }

    @Override
    public boolean hasEffect(IPlayer player, int id) {
        if (player == null || player.getMCEntity() == null)
            return false;
        return hasEffect((EntityPlayer) player.getMCEntity(), id);
    }

    @Override
    public boolean hasEffect(IPlayer player, ICustomEffect effect) {
        return hasEffect((EntityPlayer) player, effect.getID(), effect.getIndex());
    }

    @Override
    public int getEffectDuration(IPlayer player, int id) {
        if (player == null || player.getMCEntity() == null)
            return -1;
        return getEffectDuration((EntityPlayer) player.getMCEntity(), id);
    }

    @Override
    public int getEffectDuration(IPlayer player, ICustomEffect effect) {
        return getEffectDuration(player, effect.getID());
    }

    @Override
    public void applyEffect(IPlayer player, int id, int duration, byte level) {
        if (player == null || player.getMCEntity() == null)
            return;
        applyEffect((EntityPlayer) player.getMCEntity(), id, duration, level);
    }

    @Override
    public void applyEffect(IPlayer player, ICustomEffect effect, int duration, byte level) {
        applyEffect(player, effect.getID(), duration, level);
    }

    @Override
    public void removeEffect(IPlayer player, int id) {
        if (player == null || player.getMCEntity() == null)
            return;
        removeEffect((EntityPlayer) player.getMCEntity(), id, ExpirationType.REMOVED);
    }

    @Override
    public void removeEffect(IPlayer player, ICustomEffect effect) {
        removeEffect((EntityPlayer) player.getMCEntity(), (PlayerEffect) effect, ExpirationType.REMOVED);
    }

    @Override
    public void clearEffects(IPlayer player) {
        if (player == null || player.getMCEntity() == null)
            return;
        clearEffects(player.getMCEntity());
    }

    @Override
    public ICustomEffect saveEffect(ICustomEffect customEffect) {
        if (customEffect.getID() < 0) {
            customEffect.setID(getUnusedId());
            while (has(customEffect.getName()))
                customEffect.setName(customEffect.getName() + "_");
        } else {
            CustomEffect existing = customEffects.get(customEffect.getID());
            if (existing != null && !existing.name.equals(customEffect.getName()))
                while (has(customEffect.getName()))
                    customEffect.setName(customEffect.getName() + "_");
        }

        customEffects.remove(customEffect.getID());
        customEffects.put(customEffect.getID(), (CustomEffect) customEffect);

        saveEffectLoadMap();

        File dir = this.getDir();
        if (!dir.exists())
            dir.mkdirs();

        File file = new File(dir, customEffect.getName() + ".json_new");
        File file2 = new File(dir, customEffect.getName() + ".json");

        try {
            NBTTagCompound nbtTagCompound = ((CustomEffect) customEffect).writeToNBT(true);
            NBTJsonUtil.SaveFile(file, nbtTagCompound);
            if (file2.exists())
                file2.delete();
            file.renameTo(file2);
            nbtTagCompound.removeTag("ScriptData");
            SyncController.syncUpdate(EnumSyncType.CUSTOM_EFFECTS, -1, nbtTagCompound);
        } catch (Exception e) {
            LogWriter.except(e);
        }
        return customEffects.get(customEffect.getID());
    }

    public void clearEffects(Entity player) {
        Map<EffectKey, PlayerEffect> effects = playerEffects.get(NoppesUtilServer.getUUID(player));
        if (effects != null) {
            effects.clear();
        }
    }

    public boolean hasEffect(EntityPlayer player, int id) {
        return hasEffect(player, id, 0);
    }

    public boolean hasEffect(EntityPlayer player, int id, int index) {
        return getPlayerEffects(player).containsKey(new EffectKey(id, index));
    }

    public int getEffectDuration(EntityPlayer player, int id) {
        return getEffectDuration(player, id, 0);
    }

    public int getEffectDuration(EntityPlayer player, int id, int index) {
        PlayerEffect effect = getPlayerEffects(player).get(new EffectKey(id, index));
        return effect != null ? effect.duration : -1;
    }

    public void applyEffect(EntityPlayer player, int id, int duration, byte level) {
        applyEffect(player, id, duration, level, 0);
    }

    public void applyEffect(EntityPlayer player, int id, int duration, byte level, int index) {
        if (player == null || id <= 0)
            return;
        Map<EffectKey, PlayerEffect> currentEffects = getPlayerEffects(player);
        CustomEffect parent = get(id);
        if (parent != null) {
            PlayerEffect playerEffect = new PlayerEffect(id, duration, level);
            playerEffect.index = index;
            currentEffects.put(new EffectKey(id, index), playerEffect);
            parent.onAdded(player, playerEffect);
        }
    }

    public void removeEffect(EntityPlayer player, int id, ExpirationType type) {
        removeEffect(player, id, 0, type);
    }

    public void removeEffect(EntityPlayer player, int id, int index, ExpirationType type) {
        if (player == null || id <= 0)
            return;
        Map<EffectKey, PlayerEffect> currentEffects = getPlayerEffects(player);
        EffectKey key = new EffectKey(id, index);
        PlayerEffect effect = currentEffects.get(key);
        if (effect != null) {
            this.removeEffect(player, effect, type);
        }
    }

    public void decrementEffects(EntityPlayer player) {
        Iterator<PlayerEffect> iterator = getPlayerEffects(player).values().iterator();
        IPlayer iPlayer = NoppesUtilServer.getIPlayer(player);
        while (iterator.hasNext()) {
            PlayerEffect effect = iterator.next();
            if (effect == null) {
                iterator.remove();
                continue;
            }
            if (effect.duration == -100)
                continue;
            if (effect.duration <= 0) {
                CustomEffect parent = StatusEffectController.Instance.get(effect.id);
                if (parent != null) {
                    parent.onRemoved(player, effect, ExpirationType.RUN_OUT);
                }
                iterator.remove();
                continue;
            }
            effect.duration--;
        }
    }

    public File getMapDir() {
        File dir = CustomNpcs.getWorldSaveDirectory();
        if (!dir.exists())
            dir.mkdir();
        return dir;
    }

    private void loadCustomEffects() {
        customEffects.clear();

        File dir = getDir();
        if (!dir.exists()) {
            dir.mkdir();
        } else {
            for (File file : dir.listFiles()) {
                if (!file.isFile() || !file.getName().endsWith(".json"))
                    continue;
                try {
                    CustomEffect effect = new CustomEffect();
                    effect.readFromNBT(NBTJsonUtil.LoadFile(file));
                    effect.name = file.getName().substring(0, file.getName().length() - 5);
                    if (effect.id == -1) {
                        effect.id = getUnusedId();
                    }
                    int originalID = effect.id;
                    int setID = effect.id;
                    while (bootOrder.containsKey(setID) || customEffects.containsKey(setID)) {
                        if (bootOrder.containsKey(setID))
                            if (bootOrder.get(setID).equals(effect.name))
                                break;
                        setID++;
                    }
                    effect.id = setID;
                    if (originalID != setID) {
                        LogWriter.info("Found Custom Effect ID Mismatch: " + effect.name + ", New ID: " + setID);
                        effect.save();
                    }
                    customEffects.put(effect.id, effect);
                } catch (Exception e) {
                    LogWriter.error("Error loading: " + file.getAbsolutePath(), e);
                }
            }
        }
        registerEffectMap(0, customEffects);
        saveEffectLoadMap();
    }

    public HashMap<Integer, CustomEffect> getEffectMap(int index){
        if(indexMapper.containsKey(index)){
            return getEffectMap(index);
        }
        return null;
    }

    private File getDir() {
        return new File(CustomNpcs.getWorldSaveDirectory(), "customeffects");
    }

    private void saveEffectLoadMap() {
        try {
            File saveDir = getMapDir();
            File file = new File(saveDir, "customeffects.dat_new");
            File file1 = new File(saveDir, "customeffects.dat_old");
            File file2 = new File(saveDir, "customeffects.dat");
            CompressedStreamTools.writeCompressed(this.writeMapNBT(), new FileOutputStream(file));
            if (file1.exists()) {
                file1.delete();
            }
            file2.renameTo(file1);
            if (file2.exists()) {
                file2.delete();
            }
            file.renameTo(file2);
            if (file.exists()) {
                file.delete();
            }
        } catch (Exception e) {
            LogWriter.except(e);
        }
    }

    private NBTTagCompound writeMapNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        NBTTagList customEffectsList = new NBTTagList();
        for (Integer key : this.customEffects.keySet()) {
            CustomEffect customEffect = this.customEffects.get(key);
            if (!customEffect.getName().isEmpty()) {
                NBTTagCompound effectCompound = new NBTTagCompound();
                effectCompound.setString("Name", customEffect.getName());
                effectCompound.setInteger("ID", key);
                customEffectsList.appendTag(effectCompound);
            }
        }
        nbt.setTag("CustomEffects", customEffectsList);
        nbt.setInteger("lastID", lastUsedID);
        return nbt;
    }

    private void readCustomEffectMap() {
        bootOrder.clear();
        try {
            File file = new File(getMapDir(), "customeffects.dat");
            if (file.exists()) {
                loadCustomEffectMap(file);
            }
        } catch (Exception e) {
            try {
                File file = new File(getMapDir(), "customeffects.dat_old");
                if (file.exists()) {
                    loadCustomEffectMap(file);
                }
            } catch (Exception ignored) {}
        }
    }

    private void loadCustomEffectMap(File file) throws IOException {
        DataInputStream dis = new DataInputStream(new BufferedInputStream(new GZIPInputStream(new FileInputStream(file))));
        readCustomEffectMap(dis);
        dis.close();
    }

    private void readCustomEffectMap(DataInputStream stream) throws IOException {
        NBTTagCompound nbtCompound = CompressedStreamTools.read(stream);
        this.readMapNBT(nbtCompound);
    }

    private void readMapNBT(NBTTagCompound compound) {
        lastUsedID = compound.getInteger("lastID");
        NBTTagList list = compound.getTagList("CustomEffects", 10);
        if (list != null) {
            for (int i = 0; i < list.tagCount(); i++) {
                NBTTagCompound nbttagcompound = list.getCompoundTagAt(i);
                String effectName = nbttagcompound.getString("Name");
                Integer key = nbttagcompound.getInteger("ID");
                bootOrder.put(key, effectName);
            }
        }
    }

    public void deleteEffectFile(String prevName) {
        File dir = this.getDir();
        if (!dir.exists())
            dir.mkdirs();
        File file2 = new File(dir, prevName + ".json");
        if (file2.exists())
            file2.delete();
    }
}
