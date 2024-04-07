package noppes.npcs.quests;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.NpcMiscInventory;
import noppes.npcs.api.handler.data.IQuestItem;
import noppes.npcs.api.handler.data.IQuestObjective;
import noppes.npcs.constants.EnumPartyObjectives;
import noppes.npcs.controllers.data.Party;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.scripted.CustomNPCsException;
import noppes.npcs.util.ValueUtil;

import java.util.*;

public class QuestItem extends QuestInterface implements IQuestItem {
	public NpcMiscInventory items = new NpcMiscInventory(3);
    public static EntityPlayer pickedUpPlayerSolo;
	public static ItemStack pickedUp;

    public static ItemStack pickedUpParty;
    public static EntityPlayer pickedUpPlayer;

	public boolean leaveItems = false;
	public boolean ignoreDamage = false;
	public boolean ignoreNBT = false;

	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		items.setFromNBT(compound.getCompoundTag("Items"));
		leaveItems = compound.getBoolean("LeaveItems");
		ignoreDamage = compound.getBoolean("IgnoreDamage");
		ignoreNBT = compound.getBoolean("IgnoreNBT");
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
		compound.setTag("Items", items.getToNBT());
		compound.setBoolean("LeaveItems", leaveItems);
		compound.setBoolean("IgnoreDamage", ignoreDamage);
		compound.setBoolean("IgnoreNBT", ignoreNBT);
	}

	@Override
	public boolean isCompleted(PlayerData playerData) {
		HashMap<Integer,ItemStack> map = getProcessSet(playerData.player);
		for(ItemStack reqItem : items.items.values()){
			boolean done = false;
			for(ItemStack item : map.values()){
				if(NoppesUtilPlayer.compareItems(reqItem, item, ignoreDamage, ignoreNBT) && item.stackSize >= reqItem.stackSize){
					done = true;
					break;
				}
			}
			if(!done)
				return false;
		}


		return true;
	}
	public HashMap<Integer,ItemStack> getProcessSet(EntityPlayer player){
		HashMap<Integer,ItemStack> map = new HashMap<Integer,ItemStack>();
		for(int slot : items.items.keySet()){
			ItemStack item = items.items.get(slot);
			if(item == null)
				continue;
			ItemStack is = item.copy();
			is.stackSize = 0;
			map.put(slot,is);
		}

		ArrayList<ItemStack> list = new ArrayList<>(Arrays.asList(player.inventory.mainInventory));
		list.add(pickedUp);

		for(ItemStack item : list){
			if(item == null)
				continue;
			for(ItemStack questItem : map.values()){
				if(NoppesUtilPlayer.compareItems(questItem, item, ignoreDamage, ignoreNBT)){
					questItem.stackSize += item.stackSize;
				}
			}
		}
		return map;
	}

	@Override
	public void handleComplete(EntityPlayer player) {
		super.handleComplete(player);
		if(leaveItems)
			return;
        removeItems(player);
	}

    public void handlePartyComplete(EntityPlayer player, Party party, boolean isLeader, EnumPartyObjectives objectives){
        super.handlePartyComplete(player, party, isLeader, objectives);
        if(leaveItems)
            return;

        if(isLeader && objectives == EnumPartyObjectives.Leader){
            removeItems(player);
        } else if (objectives == EnumPartyObjectives.All){
            removeItems(player);
        } else if (objectives == EnumPartyObjectives.Shared){
            // Shared Case
            // Do nothing, handled later in quest completion
        }
    }

    public void removePartyItems(Party party) {
        // Iterate through each quest item in the list
        for (ItemStack questItem : items.items.values()) {
            int remainingItems = questItem.stackSize;

            // Iterate through all members of the party
            for (UUID uuid : party.getPlayerUUIDs()) {
                EntityPlayer player = NoppesUtilServer.getPlayer(uuid);
                if (player != null) {
                    int removedItems = removeItemFromPlayer(player, questItem, remainingItems);
                    remainingItems -= removedItems;

                    // If no remaining items are needed for this quest item, move to the next one
                    if (remainingItems <= 0) {
                        break;
                    }
                }
            }
        }
    }


    private int removeItemFromPlayer(EntityPlayer player, ItemStack questItem, int numItemsToRemove) {
        int itemsRemoved = 0;

        // Iterate through the player's inventory
        for (int i = 0; i < player.inventory.mainInventory.length && itemsRemoved < numItemsToRemove; i++) {
            ItemStack itemStack = player.inventory.mainInventory[i];
            if (itemStack == null)
                continue;
            if (NoppesUtilPlayer.compareItems(itemStack, questItem, ignoreDamage, ignoreNBT)) {
                int itemsToRemoveFromStack = Math.min(itemStack.stackSize, numItemsToRemove - itemsRemoved);
                int size = itemStack.stackSize;
                if (itemsToRemoveFromStack >= size) {
                    player.inventory.setInventorySlotContents(i, null);
                    itemStack.splitStack(size);
                } else {
                    itemStack.splitStack(itemsToRemoveFromStack);
                }

                itemsRemoved += size;

                // If there are no more remaining items to remove, break out of the loop
                if (itemsRemoved >= numItemsToRemove) {
                    break;
                }
            }
        }
        return itemsRemoved;
    }



    public void removeItems(EntityPlayer player){
        for(ItemStack questitem : items.items.values()){
            int stacksize = questitem.stackSize;
            for(int i = 0; i < player.inventory.mainInventory.length; i++){
                ItemStack item = player.inventory.mainInventory[i];
                if(item == null)
                    continue;
                if(NoppesUtilPlayer.compareItems(item, questitem, ignoreDamage, ignoreNBT)){
                    int size = item.stackSize;
                    if(stacksize - size >= 0){
                        player.inventory.setInventorySlotContents(i, null);
                        item.splitStack(size);
                    }
                    else{
                        item.splitStack(stacksize);
                    }
                    stacksize -= size;
                    if(stacksize <= 0)
                        break;
                }
            }
        }
    }

	@Override
	public Vector<String> getQuestLogStatus(EntityPlayer player) {
		Vector<String> vec = new Vector<String>();

		HashMap<Integer,ItemStack> map = getProcessSet(player);
		for(int slot : map.keySet()){
			ItemStack item = map.get(slot);
			ItemStack quest = items.items.get(slot);
			if(item == null)
				continue;
			String process = item.stackSize + "";
			if(item.stackSize > quest.stackSize)
				process = quest.stackSize + "";
			process += "/" + quest.stackSize + "";
			if(item.hasDisplayName())
				vec.add(item.getDisplayName() + ": " + process);
			else
				vec.add(item.getUnlocalizedName() + ".name" + ": " + process);
		}
		return vec;
	}

	public IQuestObjective[] getObjectives(EntityPlayer player) {
		List<IQuestObjective> list = new ArrayList<>();
		List<ItemStack> questItems = NoppesUtilPlayer.countStacks(this.items, this.ignoreDamage, this.ignoreNBT);

		for (ItemStack stack : questItems) {
			if (stack.stackSize > 0) {
				list.add(new QuestItemObjective(this, player, stack));
			}
		}

		return list.toArray(new IQuestObjective[0]);
	}

    @Override
    public IQuestObjective[] getPartyObjectives(Party party) {
        List<IQuestObjective> list = new ArrayList<>();
        List<ItemStack> questItems = NoppesUtilPlayer.countStacks(this.items, this.ignoreDamage, this.ignoreNBT);

        for (ItemStack stack : questItems) {
            if (stack.stackSize > 0) {
                list.add(new QuestItemObjective(this, party, stack));
            }
        }

        return list.toArray(new IQuestObjective[0]);
    }

    @Override
    public Vector<String> getPartyQuestLogStatus(Party party) {
        Vector<String> vec = new Vector<String>();
        if(party == null || party.getObjectiveRequirement() == null)
            return vec;

        EnumPartyObjectives objectives = party.getObjectiveRequirement();
        if(objectives == EnumPartyObjectives.All){
            // Slot, QuestItem Vector
            HashMap<Integer, String> questVector = new HashMap<>();
            HashMap<Integer, List<String>> playerVector = new HashMap<>();
            for(int slot : items.items.keySet()){
                ItemStack questItem = items.items.get(slot);
                if(questItem != null){
                    if(questItem.hasDisplayName())
                        questVector.put(slot, questItem.getDisplayName() + ": " + questItem.stackSize);
                    else
                        questVector.put(slot, questItem.getUnlocalizedName() + ".name" + ": " + questItem.stackSize);

                    playerVector.put(slot, new ArrayList<>());
                }
            }

            // Slot, List of Players and Their Item Counts

            for (UUID uuid: party.getPlayerUUIDs()) {
                EntityPlayer player = NoppesUtilServer.getPlayer(uuid);
                if(player != null){
                    HashMap<Integer,ItemStack> perPlayerMap = getProcessSetParty(player);
                    for(int slot : perPlayerMap.keySet()){
                        List<String> slotCurrent = playerVector.get(slot);
                        ItemStack item = perPlayerMap.get(slot);
                        ItemStack quest = items.items.get(slot);
                        if(item == null)
                            continue;
                        if(quest == null)
                            continue;
                        if(item.stackSize > quest.stackSize)
                            continue;
                        slotCurrent.add(player.getCommandSenderName() + ": " + item.stackSize);
                        playerVector.put(slot, slotCurrent);
                    }
                }
            }

            // Add entries to the vec vector
            for (int slot : questVector.keySet()) {
                String questEntry = questVector.get(slot);
                vec.add(questEntry);
                List<String> playerItems = playerVector.get(slot);
                if (playerItems != null && !playerItems.isEmpty()) {
                    StringBuilder playerNamesBuilder = new StringBuilder();
                    for (String playerName : playerItems) {
                        if (playerNamesBuilder.length() > 0) {
                            playerNamesBuilder.append(", ");
                        }
                        playerNamesBuilder.append(playerName);
                    }
                    vec.add("[" + playerNamesBuilder.toString() + "]");
                }
            }
        } else if (objectives == EnumPartyObjectives.Shared || objectives == EnumPartyObjectives.Leader){
            HashMap<Integer, Integer> totals = new HashMap<Integer, Integer>();
            for (int slot : items.items.keySet()) {
                ItemStack item = items.items.get(slot);
                if (item != null) {
                    totals.put(slot, 0);
                }
            }
            // Iterate over each player in the party
            for (UUID uuid : party.getPlayerUUIDs()) {
                if(uuid == null)
                    continue;

                if(objectives == EnumPartyObjectives.Leader && !party.getLeaderUUID().equals(uuid))
                    continue;

                EntityPlayer player = NoppesUtilServer.getPlayer(uuid);
                if (player != null) {
                    HashMap<Integer, ItemStack> perPlayerMap = getProcessSetParty(player);
                    for (Map.Entry<Integer, ItemStack> entry : items.items.entrySet()) {
                        int slot = entry.getKey();
                        ItemStack reqItem = entry.getValue();
                        for(ItemStack item : perPlayerMap.values()){
                            if(NoppesUtilPlayer.compareItems(reqItem, item, ignoreDamage, ignoreNBT)){
                                int count = totals.get(slot);
                                count += item.stackSize;
                                totals.put(slot, count);
                            }
                        }
                    }
                }
            }

            // Check if the total count of required items collected by the party exceeds the required amount for each slot
            for (int slot : items.items.keySet()) {
                ItemStack reqItem = items.items.get(slot);
                if(reqItem == null)
                    continue;

                int totalCount = totals.get(slot);
                if (totalCount > reqItem.stackSize) {
                    totalCount = reqItem.stackSize;
                }
                String process = totalCount + "/" + reqItem.stackSize;
                if(reqItem.hasDisplayName())
                    vec.add(reqItem.getDisplayName() + ": " + process);
                else
                    vec.add(reqItem.getUnlocalizedName() + ".name" + ": " + process);
            }
        }
        return vec;
    }

    @Override
    public boolean isPartyCompleted(Party party) {
        if(party == null || party.getObjectiveRequirement() == null)
            return false;

        EnumPartyObjectives objectives = party.getObjectiveRequirement();
        if(objectives == EnumPartyObjectives.All){
            for (UUID uuid: party.getPlayerUUIDs()) {
                EntityPlayer player = NoppesUtilServer.getPlayer(uuid);
                if(player != null){
                    HashMap<Integer,ItemStack> perPlayerMap = getProcessSetParty(player);
                    for(ItemStack reqItem : items.items.values()){
                        boolean done = false;
                        for(ItemStack item : perPlayerMap.values()){
                            if(NoppesUtilPlayer.compareItems(reqItem, item, ignoreDamage, ignoreNBT) && item.stackSize >= reqItem.stackSize){
                                done = true;
                                break;
                            }
                        }
                        if(!done)
                            return false;
                    }
                }
            }
        } else if (objectives == EnumPartyObjectives.Shared || objectives == EnumPartyObjectives.Leader){
            HashMap<Integer, Integer> totals = new HashMap<Integer, Integer>();
            for (int slot : items.items.keySet()) {
                ItemStack item = items.items.get(slot);
                if (item != null) {
                    totals.put(slot, 0);
                }
            }

            // Iterate over each player in the party
            for (UUID uuid : party.getPlayerUUIDs()) {
                if(uuid == null)
                    continue;

                if(objectives == EnumPartyObjectives.Leader && !party.getLeaderUUID().equals(uuid))
                    continue;

                EntityPlayer player = NoppesUtilServer.getPlayer(uuid);
                if (player != null) {
                    HashMap<Integer, ItemStack> perPlayerMap = getProcessSetParty(player);
                    for (Map.Entry<Integer, ItemStack> entry : items.items.entrySet()) {
                        int slot = entry.getKey();
                        ItemStack reqItem = entry.getValue();
                        for(ItemStack item : perPlayerMap.values()){
                            if(NoppesUtilPlayer.compareItems(reqItem, item, ignoreDamage, ignoreNBT)){
                                int count = totals.get(slot);
                                count += item.stackSize;
                                totals.put(slot, count);
                            }
                        }
                    }
                }
            }

            // Check if the total count of required items collected by the party exceeds the required amount for each slot
            for (int slot : items.items.keySet()) {
                ItemStack reqItem = items.items.get(slot);
                int totalCount = totals.get(slot);
                if (totalCount < reqItem.stackSize) {
                    return false; // Party has not completed the objective
                }
            }
        }
        return true;
    }

    public HashMap<Integer,ItemStack> getProcessSetParty(EntityPlayer player){
        HashMap<Integer,ItemStack> map = new HashMap<Integer,ItemStack>();
        for(int slot : items.items.keySet()){
            ItemStack item = items.items.get(slot);
            if(item == null)
                continue;
            ItemStack is = item.copy();
            is.stackSize = 0;
            map.put(slot,is);
        }

        ArrayList<ItemStack> list = new ArrayList<>(Arrays.asList(player.inventory.mainInventory));
        if(pickedUpPlayer != null && player.getCommandSenderName().equals(pickedUpPlayer.getCommandSenderName()))
            list.add(pickedUpParty);

        for(ItemStack item : list){
            if(item == null)
                continue;
            for(ItemStack questItem : map.values()){
                if(NoppesUtilPlayer.compareItems(questItem, item, ignoreDamage, ignoreNBT)){
                    questItem.stackSize += item.stackSize;
                }
            }
        }
        return map;
    }

    public void setLeaveItems(boolean leaveItems){
		this.leaveItems = leaveItems;
	}
	public boolean getLeaveItems(){
		return this.leaveItems;
	}

	public void setIgnoreDamage(boolean ignoreDamage){
		this.ignoreDamage = ignoreDamage;
	}
	public boolean getIgnoreDamage(){
		return this.ignoreDamage;
	}

	public void setIgnoreNbt(boolean ignoreNbt){
		this.ignoreNBT = ignoreNbt;
	}
	public boolean getIgnoreNbt(){
		return this.ignoreNBT;
	}

	class QuestItemObjective implements IQuestObjective {
		private final QuestItem parent;
		private final EntityPlayer player;
        private final Party party;
		private final ItemStack questItem;

		public QuestItemObjective(QuestItem this$0, EntityPlayer player, ItemStack item) {
			this.parent = this$0;
			this.player = player;
			this.questItem = item;
            this.party = null;
		}
        public QuestItemObjective(QuestItem this$0, Party party, ItemStack item) {
            this.parent = this$0;
            this.player = null;
            this.questItem = item;
            this.party = party;
        }

		public int getProgress() {
			int count = 0;
            if(player != null){
                ItemStack item = QuestItem.pickedUp;
                if (!NoppesUtilServer.IsItemStackNull(item) && NoppesUtilPlayer.compareItems(this.questItem, item, this.parent.ignoreDamage, this.parent.ignoreNBT)) {
                    count += item.stackSize;
                }

                for(int i = 0; i < this.player.inventory.getSizeInventory(); ++i) {
                    item = this.player.inventory.getStackInSlot(i);
                    if (!NoppesUtilServer.IsItemStackNull(item) && NoppesUtilPlayer.compareItems(this.questItem, item, this.parent.ignoreDamage, this.parent.ignoreNBT)) {
                        count += item.stackSize;
                    }
                }
            } else if (party != null && party.getObjectiveRequirement() != null){
                ItemStack item;
                EnumPartyObjectives objectives = party.getObjectiveRequirement();
                for(String name : party.getPlayerNames()){
                    EntityPlayer playerMP = NoppesUtilServer.getPlayerByName(name);
                    if(playerMP == null)
                        continue;

                    if(!playerMP.getUniqueID().equals(party.getLeaderUUID()) && objectives == EnumPartyObjectives.Leader)
                        continue;

                    int amount = 0;
                    for(int i = 0; i < playerMP.inventory.getSizeInventory(); ++i) {
                        item = playerMP.inventory.getStackInSlot(i);
                        if (!NoppesUtilServer.IsItemStackNull(item) && NoppesUtilPlayer.compareItems(this.questItem, item, this.parent.ignoreDamage, this.parent.ignoreNBT)) {
                            amount += item.stackSize;
                        }
                    }

                    if(objectives == EnumPartyObjectives.All){
                        count += ValueUtil.clamp(amount, 0, this.questItem.stackSize);
                    } else {
                        count += amount;
                    }
                }

                if(objectives == EnumPartyObjectives.All){
                    return ValueUtil.clamp(count, 0, this.questItem.stackSize * party.getPlayerNames().size());
                }
            }

			return ValueUtil.clamp(count, 0, this.questItem.stackSize);
		}

		public void setProgress(int progress) {
			throw new CustomNPCsException("Cant set the progress of ItemQuests", new Object[0]);
		}

        @Override
        public void setPlayerProgress(String playerName, int progress) {
            throw new CustomNPCsException("Cant set the progress of ItemQuests", new Object[0]);
        }

        public int getMaxProgress() {
            if(party != null && party.getObjectiveRequirement() != null){
                EnumPartyObjectives objectives = party.getObjectiveRequirement();
                if(objectives == EnumPartyObjectives.All){
                    return this.questItem.stackSize * party.getPlayerNames().size();
                }
            }

			return this.questItem.stackSize;
		}

		public boolean isCompleted() {
            if(player != null)
                return NoppesUtilPlayer.compareItems(this.player, this.questItem, this.parent.ignoreDamage, this.parent.ignoreNBT);
            else if(party != null)
                return comparePartyItems(this.party, this.questItem, this.parent.ignoreDamage, this.parent.ignoreNBT);

            return false;
		}

        public boolean comparePartyItems(Party party, ItemStack item, boolean ignoreDamage, boolean ignoreNBT){
            int size = 0;
            if(party.getObjectiveRequirement() == null)
                return false;

            EnumPartyObjectives objectives = party.getObjectiveRequirement();
            for(String name : party.getPlayerNames()){
                EntityPlayer playerMP = NoppesUtilServer.getPlayerByName(name);
                if(playerMP == null)
                    continue;

                if(objectives == EnumPartyObjectives.Leader && !playerMP.getUniqueID().equals(party.getLeaderUUID()))
                    continue;

                int amount = 0;
                for(ItemStack is : playerMP.inventory.mainInventory){
                    if(NoppesUtilPlayer.compareItems(item, is, ignoreDamage, ignoreNBT))
                        amount += is.stackSize;
                }

                if(objectives == EnumPartyObjectives.All){
                    size += ValueUtil.clamp(amount, 0, this.questItem.stackSize);
                } else {
                    size += amount;
                }
            }

            if(objectives == EnumPartyObjectives.All){
                return size >= item.stackSize * party.getPlayerNames().size();
            }

            return size >= item.stackSize;
        }

		public String getText() {
			return this.questItem.getDisplayName() + ": " + this.getProgress() + "/" + this.getMaxProgress();
		}

        @Override
        public String getAdditionalText() {
            if(party != null && party.getObjectiveRequirement() == EnumPartyObjectives.All) {
                List<String> incompletePlayers = new ArrayList<>();
                EnumPartyObjectives objectives = party.getObjectiveRequirement();
                for (String name : party.getPlayerNames()) {
                    EntityPlayer playerMP = NoppesUtilServer.getPlayerByName(name);
                    if (playerMP == null){
                        incompletePlayers.add(name + ": " + "N/A");
                        continue;
                    }

                    int amount = 0;
                    for (ItemStack is : playerMP.inventory.mainInventory) {
                        if (NoppesUtilPlayer.compareItems(this.questItem, is, ignoreDamage, ignoreNBT))
                            amount += is.stackSize;
                    }

                    int completedSize = ValueUtil.clamp(amount, 0, this.questItem.stackSize);
                    if(completedSize < this.questItem.stackSize)
                        incompletePlayers.add(name + ": " + completedSize);
                }
                if(!incompletePlayers.isEmpty())
                    return  "[" + String.join(", ", incompletePlayers) + "]";
            }
            return null;
        }
    }
}
