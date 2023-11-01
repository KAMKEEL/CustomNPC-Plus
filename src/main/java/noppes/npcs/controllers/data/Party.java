package noppes.npcs.controllers.data;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.entity.player.EntityPlayer;
import noppes.npcs.api.handler.IPlayerQuestData;
import noppes.npcs.api.handler.data.IQuest;
import noppes.npcs.controllers.PlayerDataController;
import noppes.npcs.controllers.QuestController;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;

public class Party {

    private final UUID partyUUID = UUID.randomUUID();
    private UUID partyLeader;

    private final HashMap<UUID, String> partyMembers = new HashMap<>();
    private final ArrayList<UUID> partyOrder = new ArrayList<>();

    private int currentQuestID = -1;

    public UUID getPartyLeader() {
        return partyLeader;
    }

    public void setPartyLeader(UUID partyLeader) {
        this.partyLeader = partyLeader;
    }

    public int getCurrentQuestID() {
        return currentQuestID;
    }

    public void setCurrentQuestID(int currentQuestID) {
        this.currentQuestID = currentQuestID;
    }

    public boolean addPlayer(EntityPlayer player) {
        if(partyMembers.containsKey(player.getUniqueID())){
            return false;
        }

        partyMembers.put(player.getUniqueID(), player.getCommandSenderName());
        partyOrder.add(player.getUniqueID());
        return true;
    }

    public boolean removePlayer(String playerName) {
        UUID uuid = getUUID(playerName);
        if (uuid == null){
            return false;
        }

        if(partyMembers.containsKey(uuid)){
            partyMembers.remove(uuid);
            partyOrder.remove(uuid);

            if(uuid.equals(partyLeader)){
                if(partyMembers.size() > 0){
                    partyLeader = partyOrder.get(0);
                }
            }

            return true;
        }

        return false;
    }

    public boolean removePlayer(UUID player) {
        if(partyMembers.containsKey(player)){
            partyMembers.remove(player);
            partyOrder.remove(player);

            if(player.equals(partyLeader)){
                if(partyMembers.size() > 0){
                    partyLeader = partyOrder.get(0);
                }
            }

            return true;
        }

        return false;
    }

    public boolean hasPlayer(UUID player) {
        return partyMembers.containsKey(player);
    }

    public boolean hasPlayer(String playerName) {
        UUID uuid = getUUID(playerName);
        if (uuid == null){
            return false;
        }
        return partyMembers.containsKey(uuid);
    }

    public boolean setLeader(UUID uuid){
        if(partyLeader.equals(uuid)){
            return false;
        }

        if(!partyMembers.containsKey(uuid)){
            return false;
        }

        // Remove New Leader
        partyOrder.remove(uuid);
        
        // Add New Leader to Front
        partyOrder.add(0, uuid);

        // Set New Leader
        partyLeader = uuid;
        return true;
    }

    // To Be Called DURING Invite, Leave, Quest Switch, Leader Switch, etc.
    public static boolean validateQuest(int questID, Collection<EntityPlayer> players){
        IQuest quest = QuestController.Instance.get(questID);
        if(quest == null){
            return false;
        }

        boolean everyoneNeedsQuest = quest.getPartyRequirements() == 1;
        if(everyoneNeedsQuest){
            // TODO: Get EntityPlayer from WORLD using UUID Collection

//            for(EntityPlayer player: players){
//                if(player != null){
//                    PlayerData playerData = PlayerDataController.Instance.getPlayerData(player);
//                    if(playerData != null){
//                        IPlayerQuestData questData = playerData.getQuestData();
//                        if(questData != null){
//                            if(!questData.hasActiveQuest(questID)){
//                                return false;
//                            }
//                        } else{
//                            return false;
//                        }
//                    }
//                }
//            }
            return true;
        }
        else {
            // Check if Party Leader has Quest
            return true;
        }
    }

    public static UUID getUUID(String name) {
        UUID uuid = null;
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(new URL("https://api.mojang.com/users/profiles/minecraft/" + name).openStream()));
            String uuidString;
            uuidString = (((JsonObject)new JsonParser().parse(in)).get("id")).toString().replaceAll("\"", "");
            uuidString = uuidString.replaceAll("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5");
            in.close();

            uuid = UUID.fromString(uuidString);
        } catch (Exception ignored) {}
        return uuid;
    }

}
