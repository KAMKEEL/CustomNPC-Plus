package kamkeel.npcs.controllers.data;

import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.handler.IPlayerData;

public interface IProfileController {

    IProfile getProfile(IPlayer player);

    boolean changeSlot(IPlayer player, int slotID);

    boolean hasSlot(IPlayer player, int slotID);

    boolean removeSlot(IPlayer player, int slotID);

    IPlayerData getSlotPlayerData(IPlayer player, int slotID);

    void saveSlotData(IPlayer player);
}
