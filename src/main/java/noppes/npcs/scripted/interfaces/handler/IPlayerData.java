package noppes.npcs.scripted.interfaces.handler;
import noppes.npcs.scripted.interfaces.entity.ICustomNpc;

public interface IPlayerData {

    void setCompanion(ICustomNpc npc);

    ICustomNpc getCompanion();

    boolean hasCompanion();

    int getCompanionID();

    IPlayerDialogData getDialogData();

    IPlayerBankData getBankData();

    IPlayerQuestData getQuestData();

    IPlayerTransportData getTransportData();

    IPlayerFactionData getFactionData();

    IPlayerItemGiverData getItemGiverData();

    IPlayerMailData getMailData();

    void save();
}
