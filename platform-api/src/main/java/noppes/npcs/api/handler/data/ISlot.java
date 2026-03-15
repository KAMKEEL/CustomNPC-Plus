package noppes.npcs.api.handler.data;

import noppes.npcs.api.INbt;

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
    Map<String, INbt> getComponents();

    /**
     * Sets the NBT data for a specific component key.
     *
     * @param key  the component key (e.g. "CNPC+", "DBC").
     * @param data the NBT data to store.
     */
    void setComponentData(String key, INbt data);

    /**
     * @param key - The KEY of the NBT for the Slot: [CNPC+, DBC... etc]
     * @return NBT for that that key
     */
    INbt getComponentData(String key);

    /**
     * @return The full NBT of the Slot
     */
    INbt toNBT();
}
