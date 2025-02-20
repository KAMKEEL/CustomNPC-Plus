package noppes.npcs.client.gui.test;

import java.util.Map;

public interface IPlayerDataInfo {
    void setQuestData(Map<String, Integer> questCategories, Map<String, Integer> questActive, Map<String, Integer> questFinished);
    void setDialogData(Map<String, Integer> dialogCategories, Map<String, Integer> dialogRead);
    void setTransportData(Map<String, Integer> transportCategories, Map<String, Integer> transportLocations);
    void setBankData(Map<String, Integer> bankData);
    void setFactionData(Map<String, Integer> factionData);
}
