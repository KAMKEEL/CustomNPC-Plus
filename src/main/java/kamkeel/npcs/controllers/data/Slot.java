package kamkeel.npcs.controllers.data;

import net.minecraft.nbt.NBTTagCompound;

public class Slot {

    private int id;
    private String name;
    private long lastLoaded;
    private NBTTagCompound compound;
    private boolean temporary = false;

    public Slot(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public Slot(int id, String name, NBTTagCompound compound, long last, boolean temporary) {
        this.id = id;
        this.name = name;
        this.compound = compound;
        this.lastLoaded = last;
        this.temporary = temporary;
    }

    // Getters and setters
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public long getLastLoaded() {
        return lastLoaded;
    }
    public void setLastLoaded(long lastLoaded) {
        this.lastLoaded = lastLoaded;
    }
    public NBTTagCompound getCompound() {
        return compound;
    }
    public void setCompound(NBTTagCompound compound) {
        this.compound = compound;
    }
    public boolean isTemporary() {
        return temporary;
    }
    public void setTemporary(boolean temporary) {
        this.temporary = temporary;
    }

    public NBTTagCompound toNBT() {
        NBTTagCompound slotNBT = new NBTTagCompound();
        slotNBT.setString("Name", name);
        slotNBT.setLong("LastLoaded", lastLoaded);
        slotNBT.setTag("Data", compound);
        slotNBT.setBoolean("Temporary", temporary);
        return slotNBT;
    }

    public static Slot fromNBT(int id, NBTTagCompound slotNBT) {
        String name = slotNBT.getString("Name");
        long lastLoaded = slotNBT.getLong("LastLoaded");
        NBTTagCompound compound = slotNBT.getCompoundTag("Data");
        boolean temporary = slotNBT.hasKey("Temporary") && slotNBT.getBoolean("Temporary");
        return new Slot(id, name, compound, lastLoaded, temporary);
    }
}
