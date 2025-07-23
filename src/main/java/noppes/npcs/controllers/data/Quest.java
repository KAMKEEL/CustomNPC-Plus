package noppes.npcs.controllers.data;

import kamkeel.npcs.controllers.ProfileController;
import kamkeel.npcs.controllers.data.profile.Profile;
import kamkeel.npcs.network.packets.data.QuestCompletionPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.ICompatibilty;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.NpcMiscInventory;
import noppes.npcs.VersionCompatibility;
import noppes.npcs.api.IContainer;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.handler.IPlayerData;
import noppes.npcs.api.handler.IPlayerQuestData;
import noppes.npcs.api.handler.data.IPartyOptions;
import noppes.npcs.api.handler.data.IProfileOptions;
import noppes.npcs.api.handler.data.IQuest;
import noppes.npcs.api.handler.data.IQuestCategory;
import noppes.npcs.api.handler.data.IQuestInterface;
import noppes.npcs.api.handler.data.IQuestObjective;
import noppes.npcs.api.handler.data.ISlot;
import noppes.npcs.config.ConfigMain;
import noppes.npcs.constants.EnumProfileSync;
import noppes.npcs.constants.EnumQuestCompletion;
import noppes.npcs.constants.EnumQuestRepeat;
import noppes.npcs.constants.EnumQuestType;
import noppes.npcs.controllers.QuestController;
import noppes.npcs.quests.QuestDialog;
import noppes.npcs.quests.QuestInterface;
import noppes.npcs.quests.QuestItem;
import noppes.npcs.quests.QuestKill;
import noppes.npcs.quests.QuestLocation;
import noppes.npcs.quests.QuestManual;
import noppes.npcs.scripted.CustomNPCsException;
import noppes.npcs.scripted.NpcAPI;

public class Quest implements ICompatibilty, IQuest {
    public int version = VersionCompatibility.ModRev;
    public int id = -1;
    public EnumQuestType type = EnumQuestType.Item;
    public EnumQuestRepeat repeat = EnumQuestRepeat.NONE;
    public EnumQuestCompletion completion = EnumQuestCompletion.Npc;
    public String title = "default";
    public QuestCategory category;
    public String logText = "";
    public String completeText = "";
    public String completerNpc = "";
    public int nextQuestid = -1;
    public String nextQuestTitle = "";
    public PlayerMail mail = new PlayerMail();
    public String command = "";

    public QuestInterface questInterface = new QuestItem();
    public long customCooldown = 0;

    public int rewardExp = 0;
    public NpcMiscInventory rewardItems = new NpcMiscInventory(9);
    public boolean randomReward = false;
    public FactionOptions factionOptions = new FactionOptions();
    public PartyOptions partyOptions = new PartyOptions();
    public ProfileOptions profileOptions = new ProfileOptions();

    public void readNBT(NBTTagCompound compound) {
        id = compound.getInteger("Id");
        readNBTPartial(compound);
    }

    public void readNBTPartial(NBTTagCompound compound) {
        version = compound.getInteger("ModRev");
        VersionCompatibility.CheckAvailabilityCompatibility(this, compound);

        setType(EnumQuestType.values()[compound.getInteger("Type")]);
        title = compound.getString("Title");
        logText = compound.getString("Text");
        completeText = compound.getString("CompleteText");
        completerNpc = compound.getString("CompleterNpc");
        command = compound.getString("QuestCommand");
        nextQuestid = compound.getInteger("NextQuestId");
        nextQuestTitle = compound.getString("NextQuestTitle");
        if (hasNewQuest())
            nextQuestTitle = getNextQuest().title;
        else
            nextQuestTitle = "";
        randomReward = compound.getBoolean("RandomReward");
        rewardExp = compound.getInteger("RewardExp");
        rewardItems.setFromNBT(compound.getCompoundTag("Rewards"));

        completion = EnumQuestCompletion.values()[compound.getInteger("QuestCompletion")];
        repeat = EnumQuestRepeat.values()[compound.getInteger("QuestRepeat")];
        customCooldown = compound.getLong("CustomCooldown");
        questInterface.readEntityFromNBT(compound);

        factionOptions.readFromNBT(compound.getCompoundTag("QuestFactionPoints"));
        partyOptions.readFromNBT(compound.getCompoundTag("PartyOptions"));
        profileOptions.readFromNBT(compound.getCompoundTag("ProfileOptions"));

        mail.readNBT(compound.getCompoundTag("QuestMail"));
    }

    public void setType(EnumQuestType questType) {
        type = questType;
        if (type == EnumQuestType.Item)
            questInterface = new QuestItem();
        else if (type == EnumQuestType.Dialog)
            questInterface = new QuestDialog();
        else if (type == EnumQuestType.Kill || type == EnumQuestType.AreaKill)
            questInterface = new QuestKill();
        else if (type == EnumQuestType.Location)
            questInterface = new QuestLocation();
        else if (type == EnumQuestType.Manual)
            questInterface = new QuestManual();

        if (questInterface != null)
            questInterface.questId = id;
    }

    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setInteger("Id", id);
        return writeToNBTPartial(compound);
    }

    public NBTTagCompound writeToNBTPartial(NBTTagCompound compound) {
        compound.setInteger("ModRev", version);
        compound.setInteger("Type", type.ordinal());
        compound.setString("Title", title);
        compound.setString("Text", logText);
        compound.setString("CompleteText", completeText);
        compound.setString("CompleterNpc", completerNpc);
        compound.setInteger("NextQuestId", nextQuestid);
        compound.setString("NextQuestTitle", nextQuestTitle);
        compound.setInteger("RewardExp", rewardExp);
        compound.setTag("Rewards", rewardItems.getToNBT());
        compound.setString("QuestCommand", command);
        compound.setBoolean("RandomReward", randomReward);
        compound.setLong("CustomCooldown", customCooldown);

        compound.setInteger("QuestCompletion", completion.ordinal());
        compound.setInteger("QuestRepeat", repeat.ordinal());

        this.questInterface.writeEntityToNBT(compound);
        compound.setTag("QuestFactionPoints", factionOptions.writeToNBT(new NBTTagCompound()));
        compound.setTag("PartyOptions", partyOptions.writeToNBT());
        compound.setTag("ProfileOptions", profileOptions.writeToNBT());
        compound.setTag("QuestMail", mail.writeNBT());

        return compound;
    }

    public boolean hasNewQuest() {
        return getNextQuest() != null;
    }

    public Quest getNextQuest() {
        return QuestController.Instance == null ? null : QuestController.Instance.quests.get(nextQuestid);
    }

    public boolean instantComplete(EntityPlayer player, QuestData data) {
        if (completion == EnumQuestCompletion.Instant && NoppesUtilPlayer.questCompletion((EntityPlayerMP) player, data.quest.id)) {
            QuestCompletionPacket.sendQuestComplete((EntityPlayerMP) player, data.quest.writeToNBT(new NBTTagCompound()));
            return true;
        }
        return false;
    }

    public boolean instantPartyComplete(Party party) {
        return completion == EnumQuestCompletion.Instant && NoppesUtilPlayer.questPartyCompletion(party);
    }

    public Quest copy() {
        Quest quest = new Quest();
        quest.readNBT(this.writeToNBT(new NBTTagCompound()));
        return quest;
    }

    public long getTimeUntilRepeat(EntityPlayer player) {
        if (ConfigMain.ProfilesEnabled && profileOptions.enableOptions && profileOptions.cooldownControl == EnumProfileSync.Shared) {
            Profile profile = ProfileController.Instance.getProfile(player);
            IPlayer iPlayer = NoppesUtilServer.getIPlayer(player);
            long timeRemaining = 0L;
            for (ISlot slot : profile.getSlots().values()) {
                IPlayerData playerData = ProfileController.Instance.getSlotPlayerData(iPlayer, slot.getId());
                IPlayerQuestData iPlayerQuestData = playerData.getQuestData();
                timeRemaining = Math.max(timeRemaining, getTimeUntilRepeatQuestData(player, iPlayerQuestData));
            }
            return timeRemaining;
        }
        IPlayerQuestData questData = PlayerData.get(player).getQuestData();
        return getTimeUntilRepeatQuestData(player, questData);
    }

    public long getTimeUntilRepeatQuestData(EntityPlayer player, IPlayerQuestData questData) {
        long questTime = questData.getLastCompletedTime(this.id);

        switch (repeat) {
            case MCDAILY: {
                long now = player.worldObj.getTotalWorldTime();
                long allowedTicks = 24000;
                // If questTime is in the future and the excess is greater than allowed, reset it.
                if (questTime > now && (questTime - now) > allowedTicks) {
                    questTime = now;
                    questData.setLastCompletedTime(this.id, questTime);
                }
                long remainingTicks = questTime + allowedTicks - now;
                return Math.max(0, remainingTicks * 50);
            }
            case MCWEEKLY: {
                long now = player.worldObj.getTotalWorldTime();
                long allowedTicks = 168000;
                if (questTime > now && (questTime - now) > allowedTicks) {
                    questTime = now;
                    questData.setLastCompletedTime(this.id, questTime);
                }
                long remainingTicks = questTime + allowedTicks - now;
                return Math.max(0, remainingTicks * 50);
            }
            case RLDAILY: {
                long now = System.currentTimeMillis();
                long allowedMillis = 86400000;
                if (questTime > now && (questTime - now) > allowedMillis) {
                    questTime = now;
                    questData.setLastCompletedTime(this.id, questTime);
                }
                long remainingMillis = questTime + allowedMillis - now;
                return Math.max(0, remainingMillis);
            }
            case RLWEEKLY: {
                long now = System.currentTimeMillis();
                long allowedMillis = 604800000;
                if (questTime > now && (questTime - now) > allowedMillis) {
                    questTime = now;
                    questData.setLastCompletedTime(this.id, questTime);
                }
                long remainingMillis = questTime + allowedMillis - now;
                return Math.max(0, remainingMillis);
            }
            case MCCUSTOM: {
                // For MCCUSTOM, customCooldown is assumed to be defined in ticks.
                long now = player.worldObj.getTotalWorldTime();
                long allowedTicks = this.customCooldown;
                if (questTime > now && (questTime - now) > allowedTicks) {
                    questTime = now;
                    questData.setLastCompletedTime(this.id, questTime);
                }
                long remainingTicks = questTime + allowedTicks - now;
                return Math.max(0, remainingTicks * 50);
            }
            case RLCUSTOM: {
                // For RLCUSTOM, customCooldown is assumed to be in milliseconds.
                long now = System.currentTimeMillis();
                long allowedMillis = this.customCooldown;
                if (questTime > now && (questTime - now) > allowedMillis) {
                    questTime = now;
                    questData.setLastCompletedTime(this.id, questTime);
                }
                long remainingMillis = questTime + allowedMillis - now;
                return Math.max(0, remainingMillis);
            }
            default:
                return 0L;
        }
    }


    @Override
    public int getVersion() {
        return version;
    }

    @Override
    public void setVersion(int version) {
        this.version = version;
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.title;
    }

    public int getType() {
        return this.type.ordinal();
    }

    public void setType(int questType) {
        if (questType < 0 || questType >= EnumQuestType.values().length)
            return;

        EnumQuestType type = EnumQuestType.values()[questType];
        setType(type);
    }

    public IQuestCategory getCategory() {
        return this.category;
    }

    public void save() {
        QuestController.Instance.saveQuest(this.category.id, this);
    }

    public void setName(String name) {
        this.title = name;
    }

    public String getLogText() {
        return this.logText;
    }

    public void setLogText(String text) {
        this.logText = text;
    }

    public String getCompleteText() {
        return this.completeText;
    }

    public void setCompleteText(String text) {
        this.completeText = text;
    }

    public void setNextQuest(IQuest quest) {
        if (quest == null) {
            this.nextQuestid = -1;
            this.nextQuestTitle = "";
        } else {
            if (quest.getId() < 0) {
                throw new CustomNPCsException("Quest id is lower than 0", new Object[0]);
            }

            this.nextQuestid = quest.getId();
            this.nextQuestTitle = quest.getName();
        }

    }

    public String getNpcName() {
        return this.completerNpc;
    }

    public void setNpcName(String name) {
        this.completerNpc = name;
    }

    public IQuestObjective[] getObjectives(IPlayer player) {
        if (!player.hasActiveQuest(this.id)) {
            throw new CustomNPCsException("Player doesnt have this quest active.", new Object[0]);
        } else {
            return this.questInterface.getObjectives((EntityPlayer) player.getMCEntity());
        }
    }

    public boolean getIsRepeatable() {
        return this.repeat != EnumQuestRepeat.NONE;
    }

    public long getTimeUntilRepeat(IPlayer player) {
        return this.getTimeUntilRepeat((EntityPlayer) player.getMCEntity());
    }

    public void setRepeatType(int type) {
        if (type < 0 || type >= EnumQuestRepeat.values().length) {
            return;
        }
        this.repeat = EnumQuestRepeat.values()[type];
    }

    public int getRepeatType() {
        return this.repeat.ordinal();
    }

    public IContainer getRewards() {
        return NpcAPI.Instance().getIContainer(this.rewardItems);
    }

    public IQuestInterface getQuestInterface() {
        return this.questInterface;
    }

    public IPartyOptions getPartyOptions() {
        return this.partyOptions;
    }

    public IProfileOptions getProfileOptions() {
        return this.profileOptions;
    }

    public long getCustomCooldown() {
        return this.customCooldown;
    }

    public void setCustomCooldown(long newCooldown) {
        this.customCooldown = newCooldown;
    }
}
