package noppes.npcs.controllers.data;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ChatComponentText;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.api.handler.IPlayerQuestData;
import noppes.npcs.api.handler.data.IQuest;
import noppes.npcs.constants.EnumPartyRequirements;
import noppes.npcs.controllers.PlayerDataController;
import noppes.npcs.controllers.QuestController;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;

public class Party {
    private final UUID partyUUID;
    private UUID partyLeader;

    private final HashMap<UUID, String> partyMembers = new HashMap<>();
    private final ArrayList<UUID> partyOrder = new ArrayList<>();

    private int currentQuestID = -1;
    private String currentQuestName = "";

    private boolean friendlyFire;
    private boolean partyLocked = false;

    //Client-sided
    private String partyLeaderName;

    public Party() {
        this.partyUUID = UUID.randomUUID();
    }

    public Party(UUID uuid) {
        this.partyUUID = uuid;
    }

    public UUID getPartyUUID() {
        return this.partyUUID;
    }

    public boolean getIsLocked() {
        return this.partyLocked;
    }

    public void setQuest(IQuest quest) {
        if (quest != null) {
            this.currentQuestID = quest.getId();
            this.currentQuestName = quest.getName();
        } else {
            this.currentQuestID = -1;
            this.currentQuestName = "";
        }

        this.partyLocked = quest != null;
    }

    public IQuest getQuest() {
        return QuestController.Instance.get(this.currentQuestID);
    }

    public int getCurrentQuestID() {
        return currentQuestID;
    }

    public String getCurrentQuestName() {
        return this.currentQuestName;
    }

    public boolean addPlayer(EntityPlayer player) {
        if(partyMembers.containsKey(player.getUniqueID())){
            return false;
        }

        PlayerData playerData = PlayerDataController.Instance.getPlayerData(player);
        playerData.partyUUID = this.partyUUID;

        partyMembers.put(player.getUniqueID(), player.getCommandSenderName());
        partyOrder.add(player.getUniqueID());
        return true;
    }

    public boolean removePlayer(EntityPlayer player) {
        if (player == null) return false;

        UUID uuid = player.getUniqueID();
        if(partyMembers.containsKey(uuid)){
            partyMembers.remove(uuid);
            partyOrder.remove(uuid);

            if(uuid.equals(partyLeader)){
                if(partyMembers.size() > 0){
                    partyLeader = partyOrder.get(0);
                }
            }

            PlayerData playerData = PlayerDataController.Instance.getPlayerData(player);
            playerData.partyUUID = null;

            return true;
        }

        return false;
    }

    public boolean removePlayer(String playerName) {
        return playerName != null && this.removePlayer(NoppesUtilServer.getPlayerByName(playerName));
    }

    public boolean hasPlayer(EntityPlayer player) {
        return partyMembers.containsKey(player.getUniqueID());
    }

    public boolean hasPlayer(String playerName) {
        UUID uuid = getUUID(playerName);
        if (uuid == null){
            return false;
        }
        return partyMembers.containsKey(uuid);
    }

    public EntityPlayer getPartyLeader() {
        return NoppesUtilServer.getPlayer(this.partyLeader);
    }

    public String getPartyLeaderName() {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
            return this.partyLeaderName;
        } else {
            return this.getPartyLeader().getCommandSenderName();
        }
    }

    public boolean setLeader(EntityPlayer player){
        UUID uuid = player.getUniqueID();
        if(partyLeader != null && partyLeader.equals(uuid)){
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

    public Collection<String> getPlayerNames() {
        return this.getPlayerNames(false);
    }

    public Collection<String> getPlayerNames(boolean lowercase) {
        Collection<String> names = this.partyMembers.values();
        if (!lowercase) {
            return names;
        }

        ArrayList<String> lowerNames = new ArrayList<>();
        for (String s : names) {
            lowerNames.add(s.toLowerCase());
        }
        return lowerNames;
    }

    public Collection<UUID> getPlayerUUIDs() {
        return this.partyMembers.keySet();
    }

    // To Be Called DURING Invite, Leave, Quest Switch, Leader Switch, etc.
    public boolean validateQuest(int questID) {
        IQuest quest = QuestController.Instance.get(questID);
        if (quest == null) {
            return false;
        }

        EntityPlayer leader = PlayerDataController.getPlayerFromUUID(partyLeader);
        if (leader == null) {
            return false;
        }

        int partyReq = quest.getPartyRequirements();
        if (partyReq < 0 || partyReq >= EnumPartyRequirements.values().length) {
            sendInfoMessage(leader, "Error in quest party requirements");
            return false;
        }

        if (partyMembers.size() > quest.getMaxPartySize()) {
            sendInfoMessage(leader, String.format("Party too large. Max %d members", quest.getMaxPartySize()));
            return false;
        }

        EnumPartyRequirements partyRequirements = EnumPartyRequirements.values()[partyReq];
        if (partyRequirements == EnumPartyRequirements.Leader) {
            boolean leaderBool = isValidLeaderQuest(leader, questID);
            if(leaderBool){
                sendInfoMessage(leader, "\u00A7aQuest set to party!");
            }
            else {
                sendInfoMessage(leader, "You are invalid for this quest");
            }
            return leaderBool;
        }

        return arePlayersValid(leader, partyRequirements, quest);
    }

    private boolean isValidLeaderQuest(EntityPlayer leader, int questID) {
        IPlayerQuestData questData = PlayerDataController.Instance.getPlayerData(leader).getQuestData();
        return questData != null && questData.hasActiveQuest(questID);
    }

    private boolean arePlayersValid(EntityPlayer leader, EnumPartyRequirements requirements, IQuest quest) {
        boolean allowQuest = true;
        for (UUID playerUUID : partyMembers.keySet()) {
            EntityPlayer player = PlayerDataController.getPlayerFromUUID(playerUUID);
            if (player != null) {
                PlayerData playerData = PlayerDataController.Instance.getPlayerData(player);
                if (playerData != null) {
                    IPlayerQuestData questData = playerData.getQuestData();
                    if (questData != null) {
                        if (requirements == EnumPartyRequirements.All && !questData.hasActiveQuest(quest.getId())) {
                            allowQuest = false;
                            sendInfoMessage(leader, String.format("%s does not have the active quest", player.getCommandSenderName()));
                        }

                        if (requirements == EnumPartyRequirements.Valid && !questData.hasActiveQuest(quest.getId()) && !questData.hasFinishedQuest(quest.getId())) {
                            allowQuest = false;
                            sendInfoMessage(leader, String.format("%s has not finished the quest or have it active", player.getCommandSenderName()));
                        }
                    } else {
                        allowQuest = false;
                        sendInfoMessage(leader, String.format("%s has no quest data", player.getCommandSenderName()));
                    }
                } else {
                    allowQuest = false;
                    sendInfoMessage(leader, String.format("%s has no player data", player.getCommandSenderName()));
                }
            } else {
                allowQuest = false;
                String playerName = partyMembers.get(playerUUID);
                sendInfoMessage(leader, String.format("%s was not found", playerName));
            }
        }

        if(allowQuest){
            sendInfoMessage(leader, "\u00A7aQuest set to party!");
        }
        return allowQuest;
    }

    private void sendInfoMessage(EntityPlayer player, String message) {
        player.addChatMessage(new ChatComponentText(String.format("\u00A7c%s", message)));
    }

    public void toggleFriendlyFire() {
        this.friendlyFire = !this.friendlyFire;
    }

    public boolean friendlyFire() {
        return this.friendlyFire;
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

    public void readFromNBT(NBTTagCompound compound) {
        this.partyLeaderName = compound.getString("PartyLeader");
        this.currentQuestID = compound.getInteger("PartyQuestID");
        this.currentQuestName = compound.getString("PartyQuestName");
        this.friendlyFire = compound.getBoolean("FriendlyFire");
        this.partyLocked = compound.getBoolean("PartyLocked");

        NBTTagList list = compound.getTagList("PartyMembers", 10);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound tagCompound = list.getCompoundTagAt(i);
            UUID uuid = UUID.fromString(tagCompound.getString("UUID"));
            String playerName = tagCompound.getString("PlayerName");
            this.partyOrder.add(uuid);
            this.partyMembers.put(uuid, playerName);
        }
    }

    public NBTTagCompound writeToNBT() {
        NBTTagCompound compound = new NBTTagCompound();

        compound.setString("PartyUUID", this.partyUUID.toString());
        compound.setInteger("PartyQuestID", this.currentQuestID);
        compound.setString("PartyQuestName", this.currentQuestName);
        compound.setBoolean("FriendlyFire", this.friendlyFire);
        compound.setBoolean("PartyLocked", this.partyLocked);

        NBTTagList list = new NBTTagList();
        for (UUID uuid : this.partyOrder) {
            NBTTagCompound uuidCompound = new NBTTagCompound();
            uuidCompound.setString("UUID", uuid.toString());
            uuidCompound.setString("PlayerName", this.partyMembers.get(uuid));
            list.appendTag(uuidCompound);
        }
        compound.setTag("PartyMembers", list);

        compound.setString("PartyLeader", getPartyLeader().getCommandSenderName());
        Quest quest = (Quest) QuestController.Instance.get(this.getCurrentQuestID());
        if (quest != null) {
            compound.setString("QuestName", quest.getName());
            compound.setString("QuestCategory", quest.getCategory().getName());
        }

        return compound;
    }

    public void readClientNBT(NBTTagCompound compound) {
        this.currentQuestID = compound.getInteger("PartyQuestID");
        this.currentQuestName = compound.getString("PartyQuestName");
        this.friendlyFire = compound.getBoolean("FriendlyFire");
    }

    public NBTTagCompound writeClientNBT() {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setInteger("PartyQuestID", this.currentQuestID);
        compound.setString("PartyQuestName", this.currentQuestName);
        compound.setBoolean("FriendlyFire", this.friendlyFire);
        return compound;
    }
}
