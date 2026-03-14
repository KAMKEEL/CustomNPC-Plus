package kamkeel.npcs.controllers.data.profile;

import noppes.npcs.api.handler.data.ISlot;
import noppes.npcs.core.NBT;
import noppes.npcs.api.INbt;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Slot implements ISlot {
    private int id;
    private String name;
    private long lastLoaded;
    private boolean temporary;
    private Map<String, INbt> components = new HashMap<>();

    public Slot(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public Slot(int id, String name, long lastLoaded, boolean temporary, Map<String, INbt> components) {
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
    public Map<String, INbt> getComponents() {
        return components;
    }

    @Override
    public void setComponentData(String key, INbt data) {
        components.put(key, data);
    }

    @Override
    public INbt getComponentData(String key) {
        return components.get(key);
    }

    @Override
    public INbt toNBT() {
        INbt slotNBT = NBT.compound();
        slotNBT.setString("Name", name);
        slotNBT.setLong("LastLoaded", lastLoaded);
        slotNBT.setBoolean("Temporary", temporary);
        INbt compCompound = NBT.compound();
        for (Map.Entry<String, INbt> entry : components.entrySet()) {
            compCompound.setCompound(entry.getKey(), entry.getValue());
        }
        slotNBT.setCompound("Components", compCompound);
        return slotNBT;
    }

    public static Slot fromNBT(int id, INbt slotNBT) {
        String name = slotNBT.getString("Name");
        long lastLoaded = slotNBT.getLong("LastLoaded");
        boolean temporary = slotNBT.getBoolean("Temporary");
        Slot slot = new Slot(id, name);
        slot.setLastLoaded(lastLoaded);
        slot.setTemporary(temporary);
        if (slotNBT.hasKey("Components")) {
            INbt compCompound = slotNBT.getCompound("Components");
            Set<String> keys = compCompound.getKeySet();
            for (String key : keys) {
                slot.setComponentData(key, compCompound.getCompound(key));
            }
        }
        return slot;
    }
}
