package noppes.npcs.blocks.tiles;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.StatCollector;
import noppes.npcs.constants.EnumQuestType;
import noppes.npcs.controllers.PartyController;
import noppes.npcs.controllers.PlayerDataController;
import noppes.npcs.controllers.data.*;
import noppes.npcs.quests.QuestLocation;

import java.util.ArrayList;
import java.util.List;

public class TileWaypoint extends TileEntity {

    public String name = "";

    private int ticks = 10;
    private List<EntityPlayer> recentlyChecked = new ArrayList<EntityPlayer>();
    private List<EntityPlayer> toCheck;
    public int range = 10;

    @Override
    public void updateEntity() {
        if (worldObj.isRemote || name.isEmpty())
            return;
        ticks--;
        if (ticks > 0)
            return;
        ticks = 10;

        toCheck = getPlayerList(range, range, range);
        toCheck.removeAll(recentlyChecked);

        List<EntityPlayer> listMax = getPlayerList(range + 10, range + 10, range + 10);
        recentlyChecked.retainAll(listMax);
        recentlyChecked.addAll(toCheck);

        if (toCheck.isEmpty())
            return;
        for (EntityPlayer player : toCheck) {
            PlayerData playerData = PlayerDataController.Instance.getPlayerData(player);
            PlayerQuestData questData = playerData.questData;
            Party party = playerData.getPlayerParty();
            Quest partyQuest = null;
            if (party != null && party.getQuestData() != null && party.getQuestData().quest != null && party.getQuestData().quest.type == EnumQuestType.Location) {
                QuestData partyQuestData = party.getQuestData();
                partyQuest = partyQuestData.quest;
                QuestLocation partyQuestLocation = (QuestLocation) partyQuestData.quest.questInterface;
                boolean isPartyLeader = player.getUniqueID().equals(party.getLeaderUUID());
                if (partyQuestLocation.setFoundParty(party, player, name, isPartyLeader)) {
                    player.addChatMessage(new ChatComponentTranslation(name + " " + StatCollector.translateToLocal("quest.found")));
                    PartyController.Instance().pingPartyQuestObjectiveUpdate(party);
                    PartyController.Instance().checkQuestCompletion(party, EnumQuestType.Location);
                }
            }

            ArrayList<QuestData> activeQuestValues = new ArrayList<>(questData.activeQuests.values());
            for (QuestData data : activeQuestValues) {
                if (data.quest.type != EnumQuestType.Location)
                    continue;

                if (partyQuest != null && partyQuest.id == data.quest.getId())
                    continue;

                if (data.quest.partyOptions.allowParty && data.quest.partyOptions.onlyParty)
                    continue;

                QuestLocation quest = (QuestLocation) data.quest.questInterface;
                if (quest.setFound(data, name)) {
                    player.addChatMessage(new ChatComponentTranslation(name + " " + StatCollector.translateToLocal("quest.found")));
                    questData.checkQuestCompletion(playerData, EnumQuestType.Location);
                }
            }
        }
    }

    private List<EntityPlayer> getPlayerList(int x, int y, int z) {
        return worldObj.getEntitiesWithinAABB(EntityPlayer.class, AxisAlignedBB.getBoundingBox(xCoord, yCoord, zCoord, xCoord + 1, yCoord + 1, zCoord + 1).expand(x, y, z));
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        name = compound.getString("LocationName");
        range = compound.getInteger("LocationRange");
        if (range < 2)
            range = 2;
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        if (!name.isEmpty())
            compound.setString("LocationName", name);
        compound.setInteger("LocationRange", range);
    }
}
