package kamkeel.npcs.controllers.data;

import net.minecraft.nbt.NBTTagCompound;

import java.util.Map;

public interface ISlot {
    /**
     * @return id of slot
     */
    int getId();

    /**
     * @return Name of Slot
     */
    String getName();


    /**
     * @param name - New name of slot
     */
    void setName(String name);

    /**
     * @return Last time Slot was Saved
     */
    long getLastLoaded();

    /**
     * @param time - Long time for when it was last saved
     */
    void setLastLoaded(long time);

    /**
     * @return if the slot is temporary
     */
    boolean isTemporary();

    /**
     * @param temporary - Setting a slot to temporary won't save it to Profile
     */
    void setTemporary(boolean temporary);

    /**
     * @return A map of all the NBTs within a slot
     */
    Map<String, NBTTagCompound> getComponents();

    void setComponentData(String key, NBTTagCompound data);

    /**
     * @param key - The KEY of the NBT for the Slot: [CNPC+, DBC... etc]
     * @return NBT for that that key
     */
    NBTTagCompound getComponentData(String key);

    /**
     * @return The full NBT of the Slot
     */
    NBTTagCompound toNBT();
}
