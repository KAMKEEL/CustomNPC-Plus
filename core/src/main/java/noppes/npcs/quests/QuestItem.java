package noppes.npcs.quests;

import noppes.npcs.api.entity.IEntityLiving;
import noppes.npcs.api.IDamageSource;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.INbtList;
import noppes.npcs.api.INbt;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.NpcMiscInventory;
import noppes.npcs.api.handler.data.IQuestItem;
import noppes.npcs.api.handler.data.IQuestObjective;
import noppes.npcs.constants.EnumPartyObjectives;
import noppes.npcs.controllers.data.Party;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.util.ValueUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Vector;

public class QuestItem extends QuestInterface implements IQuestItem {
    public NpcMiscInventory items = new NpcMiscInventory(3);
    public static IPlayer pickedUpPlayerSolo;
    public static IItemStack pickedUp;

    public static IItemStack pickedUpParty;
    public static IPlayer pickedUpPlayer;

    public boolean leaveItems = false;
    public boolean ignoreDamage = false;
    public boolean ignoreNBT = false;

    @Override
    public void readEntityFromNBT(INbt compound) {
        items.setFromNBT(compound.getCompoundTag("Items"));
        leaveItems = compound.getBoolean("LeaveItems");
        ignoreDamage = compound.getBoolean("IgnoreDamage");
        ignoreNBT = compound.getBoolean("IgnoreNBT");
    }

    @Override
    public void writeEntityToNBT(INbt compound) {
        compound.setTag("Items", items.getToNBT());
        compound.setBoolean("LeaveItems", leaveItems);
        compound.setBoolean("IgnoreDamage", ignoreDamage);
        compound.setBoolean("IgnoreNBT", ignoreNBT);
    }

    @Override
    public boolean isCompleted(PlayerData playerData) {
        if (playerData == null || playerData.player == null)
            return false;

        List<IItemStack> requiredStacks = NoppesUtilPlayer.countStacks(this.items, this.ignoreDamage, this.ignoreNBT);
        List<IItemStack> availableStacks = aggregateAvailableStacks(collectAvailableStacks(playerData.player, pickedUp));

        for (IItemStack required : requiredStacks) {
            boolean done = false;
            for (IItemStack available : availableStacks) {
                if (NoppesUtilPlayer.compareItems(required, available, ignoreDamage, ignoreNBT) && available.stackSize >= required.stackSize) {
                    done = true;
                    break;
                }
            }
            if (!done)
                return false;
        }

        return true;
    }

    public HashMap<Integer, IItemStack> getProcessSet(IPlayer player) {
        return buildProgressMap(collectAvailableStacks(player, pickedUp));
    }

    @Override
    public void handleComplete(IPlayer player) {
        super.handleComplete(player);
        if (leaveItems)
            return;
        removeItems(player);
    }

    public void handlePartyComplete(IPlayer player, Party party, boolean isLeader, EnumPartyObjectives objectives) {
        super.handlePartyComplete(player, party, isLeader, objectives);
        if (leaveItems)
            return;

        if (isLeader && objectives == EnumPartyObjectives.Leader) {
            removeItems(player);
        } else if (objectives == EnumPartyObjectives.All) {
            removeItems(player);
        } else if (objectives == EnumPartyObjectives.Shared) {
            // Shared Case
            // Do nothing, handled later in quest completion
        }
    }

    public void removePartyItems(Party party) {
        // Iterate through each quest item in the list
        for (IItemStack questItem : items.items.values()) {
            int remainingItems = questItem.stackSize;

            // Iterate through all members of the party
            for (UUID uuid : party.getPlayerUUIDs()) {
                IPlayer player = NoppesUtilServer.getPlayer(uuid);
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


    private int removeItemFromPlayer(IPlayer player, IItemStack questItem, int numItemsToRemove) {
        int itemsRemoved = 0;

        // Iterate through the player's inventory
        for (int i = 0; i < player.inventory.mainInventory.length && itemsRemoved < numItemsToRemove; i++) {
            IItemStack IItemStack = player.inventory.mainInventory[i];
            if (IItemStack == null)
                continue;
            if (NoppesUtilPlayer.compareItems(IItemStack, questItem, ignoreDamage, ignoreNBT)) {
                int itemsToRemoveFromStack = Math.min(IItemStack.stackSize, numItemsToRemove - itemsRemoved);
                int size = IItemStack.stackSize;
                if (itemsToRemoveFromStack >= size) {
                    player.inventory.setInventorySlotContents(i, null);
                    IItemStack.splitStack(size);
                } else {
                    IItemStack.splitStack(itemsToRemoveFromStack);
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


    public void removeItems(IPlayer player) {
        for (IItemStack questitem : items.items.values()) {
            int stacksize = questitem.stackSize;
            for (int i = 0; i < player.inventory.mainInventory.length; i++) {
                IItemStack item = player.inventory.mainInventory[i];
                if (item == null)
                    continue;
                if (NoppesUtilPlayer.compareItems(item, questitem, ignoreDamage, ignoreNBT)) {
                    int size = item.stackSize;
                    if (stacksize - size >= 0) {
                        player.inventory.setInventorySlotContents(i, null);
                        item.splitStack(size);
                    } else {
                        item.splitStack(stacksize);
                    }
                    stacksize -= size;
                    if (stacksize <= 0)
                        break;
                }
            }
        }
    }

    @Override
    public Vector<String> getQuestLogStatus(IPlayer player) {
        Vector<String> vec = new Vector<String>();

        HashMap<Integer, IItemStack> map = getProcessSet(player);
        for (int slot : map.keySet()) {
            IItemStack item = map.get(slot);
            IItemStack quest = items.items.get(slot);
            if (item == null)
                continue;
            String process = item.stackSize + "";
            if (item.stackSize > quest.stackSize)
                process = quest.stackSize + "";
            process += "/" + quest.stackSize + "";
            if (item.hasDisplayName())
                vec.add(item.getDisplayName() + ": " + process);
            else
                vec.add(item.getUnlocalizedName() + ".name" + ": " + process);
        }
        return vec;
    }

    public IQuestObjective[] getObjectives(IPlayer player) {
        List<IQuestObjective> list = new ArrayList<>();
        List<IItemStack> questItems = NoppesUtilPlayer.countStacks(this.items, this.ignoreDamage, this.ignoreNBT);

        for (IItemStack stack : questItems) {
            if (stack.stackSize > 0) {
                list.add(new QuestItemObjective(this, player, stack));
            }
        }

        return list.toArray(new IQuestObjective[0]);
    }

    @Override
    public IQuestObjective[] getPartyObjectives(Party party) {
        List<IQuestObjective> list = new ArrayList<>();
        List<IItemStack> questItems = NoppesUtilPlayer.countStacks(this.items, this.ignoreDamage, this.ignoreNBT);

        for (IItemStack stack : questItems) {
            if (stack.stackSize > 0) {
                list.add(new QuestItemObjective(this, party, stack));
            }
        }

        return list.toArray(new IQuestObjective[0]);
    }

    @Override
    public Vector<String> getPartyQuestLogStatus(Party party) {
        Vector<String> vec = new Vector<String>();
        if (party == null || party.getObjectiveRequirement() == null)
            return vec;

        EnumPartyObjectives objectives = party.getObjectiveRequirement();
        if (objectives == EnumPartyObjectives.All) {
            // Slot, QuestItem Vector
            HashMap<Integer, String> questVector = new HashMap<>();
            HashMap<Integer, List<String>> playerVector = new HashMap<>();
            for (int slot : items.items.keySet()) {
                IItemStack questItem = items.items.get(slot);
                if (questItem != null) {
                    if (questItem.hasDisplayName())
                        questVector.put(slot, questItem.getDisplayName() + ": " + questItem.stackSize);
                    else
                        questVector.put(slot, questItem.getUnlocalizedName() + ".name" + ": " + questItem.stackSize);

                    playerVector.put(slot, new ArrayList<>());
                }
            }

            // Slot, List of Players and Their Item Counts

            for (UUID uuid : party.getPlayerUUIDs()) {
                IPlayer player = NoppesUtilServer.getPlayer(uuid);
                if (player != null) {
                    HashMap<Integer, IItemStack> perPlayerMap = getProcessSetParty(player);
                    for (int slot : perPlayerMap.keySet()) {
                        List<String> slotCurrent = playerVector.get(slot);
                        IItemStack item = perPlayerMap.get(slot);
                        IItemStack quest = items.items.get(slot);
                        if (item == null)
                            continue;
                        if (quest == null)
                            continue;
                        if (item.stackSize > quest.stackSize)
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
        } else if (objectives == EnumPartyObjectives.Shared || objectives == EnumPartyObjectives.Leader) {
            HashMap<Integer, Integer> totals = new HashMap<Integer, Integer>();
            for (int slot : items.items.keySet()) {
                IItemStack item = items.items.get(slot);
                if (item != null) {
                    totals.put(slot, 0);
                }
            }
            // Iterate over each player in the party
            for (UUID uuid : party.getPlayerUUIDs()) {
                if (uuid == null)
                    continue;

                if (objectives == EnumPartyObjectives.Leader && !party.getLeaderUUID().equals(uuid))
                    continue;

                IPlayer player = NoppesUtilServer.getPlayer(uuid);
                if (player != null) {
                    HashMap<Integer, IItemStack> perPlayerMap = getProcessSetParty(player);
                    for (Map.Entry<Integer, IItemStack> entry : items.items.entrySet()) {
                        int slot = entry.getKey();
                        IItemStack reqItem = entry.getValue();
                        if (reqItem == null)
                            continue;
                        IItemStack item = perPlayerMap.get(slot);
                        if (item != null && NoppesUtilPlayer.compareItems(reqItem, item, ignoreDamage, ignoreNBT)) {
                            int count = totals.get(slot);
                            count += item.stackSize;
                            totals.put(slot, count);
                        }
                    }
                }
            }

            // Check if the total count of required items collected by the party exceeds the required amount for each slot
            for (int slot : items.items.keySet()) {
                IItemStack reqItem = items.items.get(slot);
                if (reqItem == null)
                    continue;

                int totalCount = totals.get(slot);
                if (totalCount > reqItem.stackSize) {
                    totalCount = reqItem.stackSize;
                }
                String process = totalCount + "/" + reqItem.stackSize;
                if (reqItem.hasDisplayName())
                    vec.add(reqItem.getDisplayName() + ": " + process);
                else
                    vec.add(reqItem.getUnlocalizedName() + ".name" + ": " + process);
            }
        }
        return vec;
    }

    @Override
    public boolean isPartyCompleted(Party party) {
        if (party == null || party.getObjectiveRequirement() == null)
            return false;

        EnumPartyObjectives objectives = party.getObjectiveRequirement();
        if (objectives == EnumPartyObjectives.All) {
            for (UUID uuid : party.getPlayerUUIDs()) {
                IPlayer player = NoppesUtilServer.getPlayer(uuid);
                if (player != null) {
                    HashMap<Integer, IItemStack> perPlayerMap = getProcessSetParty(player);
                    for (IItemStack reqItem : items.items.values()) {
                        boolean done = false;
                        for (IItemStack item : perPlayerMap.values()) {
                            if (NoppesUtilPlayer.compareItems(reqItem, item, ignoreDamage, ignoreNBT) && item.stackSize >= reqItem.stackSize) {
                                done = true;
                                break;
                            }
                        }
                        if (!done)
                            return false;
                    }
                }
            }
        } else if (objectives == EnumPartyObjectives.Shared || objectives == EnumPartyObjectives.Leader) {
            HashMap<Integer, Integer> totals = new HashMap<Integer, Integer>();
            for (int slot : items.items.keySet()) {
                IItemStack item = items.items.get(slot);
                if (item != null) {
                    totals.put(slot, 0);
                }
            }

            // Iterate over each player in the party
            for (UUID uuid : party.getPlayerUUIDs()) {
                if (uuid == null)
                    continue;

                if (objectives == EnumPartyObjectives.Leader && !party.getLeaderUUID().equals(uuid))
                    continue;

                IPlayer player = NoppesUtilServer.getPlayer(uuid);
                if (player != null) {
                    HashMap<Integer, IItemStack> perPlayerMap = getProcessSetParty(player);
                    for (Map.Entry<Integer, IItemStack> entry : items.items.entrySet()) {
                        int slot = entry.getKey();
                        IItemStack reqItem = entry.getValue();
                        if (reqItem == null)
                            continue;
                        IItemStack item = perPlayerMap.get(slot);
                        if (item != null && NoppesUtilPlayer.compareItems(reqItem, item, ignoreDamage, ignoreNBT)) {
                            int count = totals.get(slot);
                            count += item.stackSize;
                            totals.put(slot, count);
                        }
                    }
                }
            }

            // Check if the total count of required items collected by the party exceeds the required amount for each slot
            for (int slot : items.items.keySet()) {
                IItemStack reqItem = items.items.get(slot);
                int totalCount = totals.get(slot);
                if (totalCount < reqItem.stackSize) {
                    return false; // Party has not completed the objective
                }
            }
        }
        return true;
    }

    public HashMap<Integer, IItemStack> getProcessSetParty(IPlayer player) {
        IItemStack extra = null;
        if (pickedUpPlayer != null && player != null && player.getCommandSenderName().equals(pickedUpPlayer.getCommandSenderName()))
            extra = pickedUpParty;
        return buildProgressMap(collectAvailableStacks(player, extra));
    }

    private List<IItemStack> collectAvailableStacks(IPlayer player, IItemStack extra) {
        List<IItemStack> available = new ArrayList<>();
        if (player == null)
            return available;

        for (IItemStack stack : player.inventory.mainInventory) {
            if (stack != null && stack.stackSize > 0) {
                available.add(stack.copy());
            }
        }

        if (extra != null && extra.stackSize > 0) {
            available.add(extra.copy());
        }

        return available;
    }

    private HashMap<Integer, IItemStack> buildProgressMap(List<IItemStack> available) {
        HashMap<Integer, IItemStack> map = new HashMap<Integer, IItemStack>();
        List<Integer> slots = new ArrayList<>(items.items.keySet());
        Collections.sort(slots);

        for (int slot : slots) {
            IItemStack requirement = items.items.get(slot);
            if (requirement == null)
                continue;

            IItemStack progress = requirement.copy();
            progress.stackSize = 0;
            int remaining = requirement.stackSize;

            for (IItemStack stack : available) {
                if (stack == null || stack.stackSize <= 0)
                    continue;

                if (NoppesUtilPlayer.compareItems(requirement, stack, ignoreDamage, ignoreNBT)) {
                    int used = Math.min(stack.stackSize, remaining);
                    if (used > 0) {
                        progress.stackSize += used;
                        stack.stackSize -= used;
                        remaining -= used;
                    }

                    if (remaining <= 0)
                        break;
                }
            }

            map.put(slot, progress);
        }

        return map;
    }

    private List<IItemStack> aggregateAvailableStacks(List<IItemStack> available) {
        List<IItemStack> aggregated = new ArrayList<>();
        for (IItemStack stack : available) {
            if (stack == null || stack.stackSize <= 0)
                continue;

            boolean found = false;
            for (IItemStack existing : aggregated) {
                if (NoppesUtilPlayer.compareItems(existing, stack, ignoreDamage, ignoreNBT)) {
                    existing.stackSize += stack.stackSize;
                    found = true;
                    break;
                }
            }

            if (!found) {
                aggregated.add(stack.copy());
            }
        }
        return aggregated;
    }

    public void setLeaveItems(boolean leaveItems) {
        this.leaveItems = leaveItems;
    }

    public boolean getLeaveItems() {
        return this.leaveItems;
    }

    public void setIgnoreDamage(boolean ignoreDamage) {
        this.ignoreDamage = ignoreDamage;
    }

    public boolean getIgnoreDamage() {
        return this.ignoreDamage;
    }

    public void setIgnoreNbt(boolean ignoreNbt) {
        this.ignoreNBT = ignoreNbt;
    }

    public boolean getIgnoreNbt() {
        return this.ignoreNBT;
    }

    class QuestItemObjective implements IQuestObjective {
        private final QuestItem parent;
        private final IPlayer player;
        private final Party party;
        private final IItemStack questItem;

        public QuestItemObjective(QuestItem this$0, IPlayer player, IItemStack item) {
            this.parent = this$0;
            this.player = player;
            this.questItem = item;
            this.party = null;
        }

        public QuestItemObjective(QuestItem this$0, Party party, IItemStack item) {
            this.parent = this$0;
            this.player = null;
            this.questItem = item;
            this.party = party;
        }

        public int getProgress() {
            int count = 0;
            if (player != null) {
                IItemStack item = QuestItem.pickedUp;
                if (!NoppesUtilServer.IsItemStackNull(item) && NoppesUtilPlayer.compareItems(this.questItem, item, this.parent.ignoreDamage, this.parent.ignoreNBT)) {
                    count += item.stackSize;
                }

                for (int i = 0; i < this.player.inventory.getSizeInventory(); ++i) {
                    item = this.player.inventory.getStackInSlot(i);
                    if (!NoppesUtilServer.IsItemStackNull(item) && NoppesUtilPlayer.compareItems(this.questItem, item, this.parent.ignoreDamage, this.parent.ignoreNBT)) {
                        count += item.stackSize;
                    }
                }
            } else if (party != null && party.getObjectiveRequirement() != null) {
                IItemStack item;
                EnumPartyObjectives objectives = party.getObjectiveRequirement();
                for (String name : party.getPlayerNames()) {
                    IPlayer playerMP = NoppesUtilServer.getPlayerByName(name);
                    if (playerMP == null)
                        continue;

                    if (!playerMP.getUniqueID().equals(party.getLeaderUUID()) && objectives == EnumPartyObjectives.Leader)
                        continue;

                    int amount = 0;
                    for (int i = 0; i < playerMP.inventory.getSizeInventory(); ++i) {
                        item = playerMP.inventory.getStackInSlot(i);
                        if (!NoppesUtilServer.IsItemStackNull(item) && NoppesUtilPlayer.compareItems(this.questItem, item, this.parent.ignoreDamage, this.parent.ignoreNBT)) {
                            amount += item.stackSize;
                        }
                    }

                    if (objectives == EnumPartyObjectives.All) {
                        count += ValueUtil.clamp(amount, 0, this.questItem.stackSize);
                    } else {
                        count += amount;
                    }
                }

                if (objectives == EnumPartyObjectives.All) {
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
            if (party != null && party.getObjectiveRequirement() != null) {
                EnumPartyObjectives objectives = party.getObjectiveRequirement();
                if (objectives == EnumPartyObjectives.All) {
                    return this.questItem.stackSize * party.getPlayerNames().size();
                }
            }

            return this.questItem.stackSize;
        }

        public boolean isCompleted() {
            if (player != null)
                return NoppesUtilPlayer.compareItems(this.player, this.questItem, this.parent.ignoreDamage, this.parent.ignoreNBT);
            else if (party != null)
                return comparePartyItems(this.party, this.questItem, this.parent.ignoreDamage, this.parent.ignoreNBT);

            return false;
        }

        public boolean comparePartyItems(Party party, IItemStack item, boolean ignoreDamage, boolean ignoreNBT) {
            int size = 0;
            if (party.getObjectiveRequirement() == null)
                return false;

            EnumPartyObjectives objectives = party.getObjectiveRequirement();
            for (String name : party.getPlayerNames()) {
                IPlayer playerMP = NoppesUtilServer.getPlayerByName(name);
                if (playerMP == null)
                    continue;

                if (objectives == EnumPartyObjectives.Leader && !playerMP.getUniqueID().equals(party.getLeaderUUID()))
                    continue;

                int amount = 0;
                for (IItemStack is : playerMP.inventory.mainInventory) {
                    if (NoppesUtilPlayer.compareItems(item, is, ignoreDamage, ignoreNBT))
                        amount += is.stackSize;
                }

                if (objectives == EnumPartyObjectives.All) {
                    size += ValueUtil.clamp(amount, 0, this.questItem.stackSize);
                } else {
                    size += amount;
                }
            }

            if (objectives == EnumPartyObjectives.All) {
                return size >= item.stackSize * party.getPlayerNames().size();
            }

            return size >= item.stackSize;
        }

        public String getText() {
            return this.questItem.getDisplayName() + ": " + this.getProgress() + "/" + this.getMaxProgress();
        }

        @Override
        public String getAdditionalText() {
            if (party != null && party.getObjectiveRequirement() == EnumPartyObjectives.All) {
                List<String> incompletePlayers = new ArrayList<>();
                EnumPartyObjectives objectives = party.getObjectiveRequirement();
                for (String name : party.getPlayerNames()) {
                    IPlayer playerMP = NoppesUtilServer.getPlayerByName(name);
                    if (playerMP == null) {
                        incompletePlayers.add(name + ": " + "N/A");
                        continue;
                    }

                    int amount = 0;
                    for (IItemStack is : playerMP.inventory.mainInventory) {
                        if (NoppesUtilPlayer.compareItems(this.questItem, is, ignoreDamage, ignoreNBT))
                            amount += is.stackSize;
                    }

                    int completedSize = ValueUtil.clamp(amount, 0, this.questItem.stackSize);
                    if (completedSize < this.questItem.stackSize)
                        incompletePlayers.add(name + ": " + completedSize);
                }
                if (!incompletePlayers.isEmpty())
                    return "[" + String.join(", ", incompletePlayers) + "]";
            }
            return null;
        }
    }
}
