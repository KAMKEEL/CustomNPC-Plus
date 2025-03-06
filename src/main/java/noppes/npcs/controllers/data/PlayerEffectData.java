package noppes.npcs.controllers.data;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerEffectData {
    private final PlayerData parent;
    private ConcurrentHashMap<EffectKey, PlayerEffect> effects = new ConcurrentHashMap<>();

    public PlayerEffectData(PlayerData playerData){
        this.parent = playerData;
    }

    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        NBTTagList list = new NBTTagList();
        for (Map.Entry<EffectKey, PlayerEffect> entry : effects.entrySet()) {
            NBTTagCompound effectCompound = new NBTTagCompound();
            effectCompound.setInteger("id", entry.getKey().getId());
            effectCompound.setInteger("index", entry.getKey().getIndex());
            effectCompound.setInteger("duration", entry.getValue().duration);
            effectCompound.setByte("level", entry.getValue().level);
            list.appendTag(effectCompound);
        }
        compound.setTag("Effects", list);
        return compound;
    }

    public void readFromNBT(NBTTagCompound compound) {
        HashSet<EffectKey> newKeys = new HashSet<>();
        if (compound.hasKey("Effects")) {
            NBTTagList list = compound.getTagList("Effects", 10);
            for (int i = 0; i < list.tagCount(); i++) {
                NBTTagCompound effectCompound = list.getCompoundTagAt(i);
                int id = effectCompound.getInteger("id");
                int index = effectCompound.getInteger("index");
                int duration = effectCompound.getInteger("duration");
                byte level = effectCompound.getByte("level");
                EffectKey key = new EffectKey(id, index);
                newKeys.add(key);
                PlayerEffect effect = effects.get(key);
                if (effect != null) {
                    effect.duration = duration;
                    effect.level = level;
                } else {
                    effect = new PlayerEffect(id, duration, level, index);
                    effects.put(key, effect);
                }
            }
        }
        Iterator<EffectKey> it = effects.keySet().iterator();
        while (it.hasNext()) {
            EffectKey key = it.next();
            if (!newKeys.contains(key)) {
                it.remove();
            }
        }
    }

    public ConcurrentHashMap<EffectKey, PlayerEffect> getEffects() {
        return effects;
    }

    public void setEffects(ConcurrentHashMap<EffectKey, PlayerEffect> effects) {
        this.effects = effects;
    }

    public PlayerEffect getPlayerEffect(int id, int index) {
        return effects.get(new EffectKey(id, index));
    }

    public PlayerEffect getPlayerEffect(int id) {
        return getPlayerEffect(id, 0);
    }

    public boolean hasPlayerEffect(int id, int index) {
        return effects.containsKey(new EffectKey(id, index));
    }

    public boolean hasPlayerEffect(int id) {
        return hasPlayerEffect(id, 0);
    }
}
