package kamkeel.npcs.controllers.data.profile;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.api.handler.data.ISlot;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Slot implements ISlot {
    private int id;
    private String name;
    private long lastLoaded;
    private boolean temporary;
    private Map<String, NBTTagCompound> components = new HashMap<>();

    public Slot(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public Slot(int id, String name, long lastLoaded, boolean temporary, Map<String, NBTTagCompound> components) {
        this.id = id;
        this.name = name;
        this.lastLoaded = lastLoaded;
        this.temporary = temporary;
        this.components = components;
    }

    @Override
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public long getLastLoaded() {
        return lastLoaded;
    }

    @Override
    public void setLastLoaded(long time) {
        this.lastLoaded = time;
    }

    @Override
    public boolean isTemporary() {
        return temporary;
    }

    @Override
    public void setTemporary(boolean temporary) {
        this.temporary = temporary;
    }

    @Override
    public Map<String, NBTTagCompound> getComponents() {
        return components;
    }

    @Override
    public void setComponentData(String key, NBTTagCompound data) {
        components.put(key, data);
    }

    @Override
    public NBTTagCompound getComponentData(String key) {
        return components.get(key);
    }

    @Override
    public NBTTagCompound toNBT() {
        NBTTagCompound slotNBT = new NBTTagCompound();
        slotNBT.setString("Name", name);
        slotNBT.setLong("LastLoaded", lastLoaded);
        slotNBT.setBoolean("Temporary", temporary);
        NBTTagCompound compCompound = new NBTTagCompound();
        for (Map.Entry<String, NBTTagCompound> entry : components.entrySet()) {
            compCompound.setTag(entry.getKey(), entry.getValue());
        }
        slotNBT.setTag("Components", compCompound);
        return slotNBT;
    }

    public static Slot fromNBT(int id, NBTTagCompound slotNBT) {
        String name = slotNBT.getString("Name");
        long lastLoaded = slotNBT.getLong("LastLoaded");
        boolean temporary = slotNBT.getBoolean("Temporary");
        Slot slot = new Slot(id, name);
        slot.setLastLoaded(lastLoaded);
        slot.setTemporary(temporary);
        if (slotNBT.hasKey("Components")) {
            NBTTagCompound compCompound = slotNBT.getCompoundTag("Components");
            Set<String> keys = compCompound.func_150296_c();
            for (String key : keys) {
                slot.setComponentData(key, compCompound.getCompoundTag(key));
            }
        }
        return slot;
    }
}
