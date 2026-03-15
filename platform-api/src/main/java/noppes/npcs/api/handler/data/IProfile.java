package noppes.npcs.api.handler.data;

import noppes.npcs.api.entity.IPlayer;

import java.util.Map;

public interface IProfile {


    /**
     * @return the IPlayer attached to the Profile
     */
    IPlayer getPlayer();


    /**
     * @return The profiles current Slot ID
     */
    int getCurrentSlotId();


    /**
     * @return Map of all Slot IDs
     */
    Map<Integer, ISlot> getSlots();


    /**
     * @return FULL NBT of the Profile
     */
    Object writeToNBT();
}
