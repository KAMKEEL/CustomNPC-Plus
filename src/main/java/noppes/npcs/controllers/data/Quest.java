package noppes.npcs.controllers.data;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.*;
import noppes.npcs.api.IContainer;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.handler.data.*;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.constants.EnumQuestCompletion;
import noppes.npcs.constants.EnumQuestRepeat;
import noppes.npcs.constants.EnumQuestType;
import noppes.npcs.controllers.PlayerDataController;
import noppes.npcs.controllers.QuestController;
import noppes.npcs.quests.*;
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

	public int rewardExp = 0;
	public NpcMiscInventory rewardItems = new NpcMiscInventory(9);
	public boolean randomReward = false;
	public FactionOptions factionOptions = new FactionOptions();
	public PartyOptions partyOptions = new PartyOptions();


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
		if(hasNewQuest())
			nextQuestTitle = getNextQuest().title;
		else
			nextQuestTitle = "";
		randomReward = compound.getBoolean("RandomReward");
		rewardExp = compound.getInteger("RewardExp");
		rewardItems.setFromNBT(compound.getCompoundTag("Rewards"));

		completion = EnumQuestCompletion.values()[compound.getInteger("QuestCompletion")];
		repeat = EnumQuestRepeat.values()[compound.getInteger("QuestRepeat")];

		questInterface.readEntityFromNBT(compound);

		factionOptions.readFromNBT(compound.getCompoundTag("QuestFactionPoints"));
		partyOptions.readFromNBT(compound.getCompoundTag("PartyOptions"));

		mail.readNBT(compound.getCompoundTag("QuestMail"));
	}

	public void setType(EnumQuestType questType) {
		type = questType;
		if(type == EnumQuestType.Item)
			questInterface = new QuestItem();
		else if(type == EnumQuestType.Dialog)
			questInterface = new QuestDialog();
		else if(type == EnumQuestType.Kill || type == EnumQuestType.AreaKill)
			questInterface = new QuestKill();
		else if(type == EnumQuestType.Location)
			questInterface = new QuestLocation();
		else if(type == EnumQuestType.Manual)
			questInterface = new QuestManual();

		if(questInterface != null)
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

		compound.setInteger("QuestCompletion", completion.ordinal());
		compound.setInteger("QuestRepeat", repeat.ordinal());

		this.questInterface.writeEntityToNBT(compound);
		compound.setTag("QuestFactionPoints", factionOptions.writeToNBT(new NBTTagCompound()));
		compound.setTag("PartyOptions", partyOptions.writeToNBT());
		compound.setTag("QuestMail", mail.writeNBT());

		return compound;
	}

	public boolean hasNewQuest()
	{
		return getNextQuest() != null;
	}
	public Quest getNextQuest()
	{
		return QuestController.Instance == null?null:QuestController.Instance.quests.get(nextQuestid);
	}

	public boolean instantComplete(EntityPlayer player, QuestData data) {
		if(completion == EnumQuestCompletion.Instant && NoppesUtilPlayer.questCompletion((EntityPlayerMP) player, data.quest.id)){
			Server.sendData((EntityPlayerMP)player, EnumPacketClient.QUEST_COMPLETION, data.quest.writeToNBT(new NBTTagCompound()));
			return true;
		}
		return false;
	}
	public Quest copy(){
		Quest quest = new Quest();
		quest.readNBT(this.writeToNBT(new NBTTagCompound()));
		return quest;
	}

	public long getTimeUntilRepeat(EntityPlayer player) {
		long questTime = PlayerDataController.Instance.getPlayerData(player).getQuestData().getLastCompletedTime(this.id);

		switch (repeat) {
			case MCDAILY:
				return Math.max(0, (questTime + 24000 - player.worldObj.getTotalWorldTime()) * 50);
			case MCWEEKLY:
				return Math.max(0, (questTime + 168000 - player.worldObj.getTotalWorldTime()) * 50);
			case RLDAILY:
				return Math.max(0, questTime + 86400000  - System.currentTimeMillis());
			case RLWEEKLY:
				return Math.max(0, questTime + 604800000 - System.currentTimeMillis());
		}
		return 0L;
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
		if(questType < 0 || questType >= EnumQuestType.values().length)
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

	public IQuestInterface getQuestInterface(){
		return this.questInterface;
	}

	public IPartyOptions getPartyOptions() {
		return this.partyOptions;
	}
}
