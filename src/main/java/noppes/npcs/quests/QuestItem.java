package noppes.npcs.quests;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.NpcMiscInventory;
import noppes.npcs.Server;
import noppes.npcs.api.handler.data.IQuestItem;
import noppes.npcs.api.handler.data.IQuestObjective;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.controllers.PlayerDataController;
import noppes.npcs.controllers.data.Party;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.scripted.CustomNPCsException;
import noppes.npcs.util.ValueUtil;

import java.util.*;

public class QuestItem extends QuestInterface implements IQuestItem {
	public NpcMiscInventory items = new NpcMiscInventory(3);
	public static ItemStack pickedUp;
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
        HashMap<Integer,ItemStack> map = getProcessSetParty(party);
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

    @Override
    public boolean isPartyCompleted(Party party) {
        HashMap<Integer,ItemStack> map = getProcessSetParty(party);
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


    public HashMap<Integer,ItemStack> getProcessSetParty(Party party){
        HashMap<Integer,ItemStack> map = new HashMap<Integer,ItemStack>();
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

			ItemStack item = QuestItem.pickedUp;
			if (!NoppesUtilServer.IsItemStackNull(item) && NoppesUtilPlayer.compareItems(this.questItem, item, this.parent.ignoreDamage, this.parent.ignoreNBT)) {
				count += item.stackSize;
			}

            if(player != null){
                for(int i = 0; i < this.player.inventory.getSizeInventory(); ++i) {
                    item = this.player.inventory.getStackInSlot(i);
                    if (!NoppesUtilServer.IsItemStackNull(item) && NoppesUtilPlayer.compareItems(this.questItem, item, this.parent.ignoreDamage, this.parent.ignoreNBT)) {
                        count += item.stackSize;
                    }
                }
            } else if (party != null){
                for(String name : party.getPlayerNames()){
                    EntityPlayer playerMP = NoppesUtilServer.getPlayerByName(name);
                    if(playerMP == null)
                        continue;

                    for(int i = 0; i < playerMP.inventory.getSizeInventory(); ++i) {
                        item = playerMP.inventory.getStackInSlot(i);
                        if (!NoppesUtilServer.IsItemStackNull(item) && NoppesUtilPlayer.compareItems(this.questItem, item, this.parent.ignoreDamage, this.parent.ignoreNBT)) {
                            count += item.stackSize;
                        }
                    }
                }
            }

			return ValueUtil.clamp(count, 0, this.questItem.stackSize);
		}

		public void setProgress(int progress) {
			throw new CustomNPCsException("Cant set the progress of ItemQuests", new Object[0]);
		}

		public int getMaxProgress() {
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
            for(String name : party.getPlayerNames()){
                EntityPlayer playerMP = NoppesUtilServer.getPlayerByName(name);
                if(playerMP == null)
                    continue;
                for(ItemStack is : playerMP.inventory.mainInventory){
                    if(is != null && NoppesUtilPlayer.compareItems(item, is, ignoreDamage, ignoreNBT))
                        size += is.stackSize;
                }
            }
            return size >= item.stackSize;
        }

		public String getText() {
			return this.questItem.getDisplayName() + ": " + this.getProgress() + "/" + this.getMaxProgress();
		}
	}
}
