package noppes.npcs.api;

import noppes.npcs.api.entity.IPixelmon;

public interface IPixelmonPlayerData {

    IPixelmon getPartySlot(int slot);

    int countPCPixelmon();
}
