package noppes.npcs.quests;

import java.util.*;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.NpcMiscInventory;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.scripted.CustomNPCsException;
import noppes.npcs.api.handler.data.IQuestItem;
import noppes.npcs.api.handler.data.IQuestObjective;
import noppes.npcs.util.ValueUtil;

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
		List<IQuestObjective> list = new ArrayList();
		List<ItemStack> questItems = NoppesUtilPlayer.countStacks(this.items, this.ignoreDamage, this.ignoreNBT);
		Iterator var4 = questItems.iterator();

		while(var4.hasNext()) {
			ItemStack stack = (ItemStack)var4.next();
			if (stack.stackSize > 0) {
				list.add(new noppes.npcs.quests.QuestItem.QuestItemObjective(this, player, stack));
			}
		}

		return (IQuestObjective[])list.toArray(new IQuestObjective[list.size()]);
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
		private final ItemStack questItem;

		public QuestItemObjective(QuestItem this$0, EntityPlayer player, ItemStack item) {
			this.parent = this$0;
			this.player = player;
			this.questItem = item;
		}

		public int getProgress() {
			int count = 0;

			for(int i = 0; i < this.player.inventory.getSizeInventory(); ++i) {
				ItemStack item = this.player.inventory.getStackInSlot(i);
				if (!NoppesUtilServer.IsItemStackNull(item) && NoppesUtilPlayer.compareItems(this.questItem, item, this.parent.ignoreDamage, this.parent.ignoreNBT)) {
					count += item.stackSize;
				}
			}

			return ValueUtil.CorrectInt(count, 0, this.questItem.stackSize);
		}

		public void setProgress(int progress) {
			throw new CustomNPCsException("Cant set the progress of ItemQuests", new Object[0]);
		}

		public int getMaxProgress() {
			return this.questItem.stackSize;
		}

		public boolean isCompleted() {
			return NoppesUtilPlayer.compareItems(this.player, this.questItem, this.parent.ignoreDamage, this.parent.ignoreNBT);
		}

		public String getText() {
			return this.questItem.getDisplayName() + ": " + this.getProgress() + "/" + this.getMaxProgress();
		}
	}
}
