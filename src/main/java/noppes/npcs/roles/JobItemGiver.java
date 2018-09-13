package noppes.npcs.roles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.NBTTags;
import noppes.npcs.NpcMiscInventory;
import noppes.npcs.controllers.Availability;
import noppes.npcs.controllers.GlobalDataController;
import noppes.npcs.controllers.Line;
import noppes.npcs.controllers.PlayerDataController;
import noppes.npcs.controllers.PlayerItemGiverData;
import noppes.npcs.entity.EntityNPCInterface;

public class JobItemGiver extends JobInterface{

	public int cooldownType = 0;
	public int givingMethod = 0;
	public int cooldown = 10;
	public NpcMiscInventory inventory;
	public int itemGiverId = 0;
	
	public List<String> lines = new ArrayList<String>();
	
	private int ticks = 10;
	
	private List<EntityPlayer> recentlyChecked = new ArrayList<EntityPlayer>();
	private List<EntityPlayer> toCheck;
	public Availability availability = new Availability();

	public JobItemGiver(EntityNPCInterface npc) {
		super(npc);
		inventory = new NpcMiscInventory(9);
		lines.add("Have these items {player}");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbttagcompound) {
		nbttagcompound.setInteger("igCooldownType", cooldownType);
		nbttagcompound.setInteger("igGivingMethod", givingMethod);
		nbttagcompound.setInteger("igCooldown", cooldown);
		nbttagcompound.setInteger("ItemGiverId", itemGiverId);
    	nbttagcompound.setTag("igLines", NBTTags.nbtStringList(lines));
		nbttagcompound.setTag("igJobInventory", inventory.getToNBT());
		nbttagcompound.setTag("igAvailability", availability.writeToNBT(new NBTTagCompound()));
		return nbttagcompound;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		itemGiverId = nbttagcompound.getInteger("ItemGiverId");
		cooldownType = nbttagcompound.getInteger("igCooldownType");
		givingMethod = nbttagcompound.getInteger("igGivingMethod");
		cooldown = nbttagcompound.getInteger("igCooldown");
    	lines = NBTTags.getStringList(nbttagcompound.getTagList("igLines", 10));
    	inventory.setFromNBT(nbttagcompound.getCompoundTag("igJobInventory"));
    	
    	if(itemGiverId == 0 && GlobalDataController.instance != null)
			itemGiverId = GlobalDataController.instance.incrementItemGiverId();
    	
    	availability.readFromNBT(nbttagcompound.getCompoundTag("igAvailability"));
	}

	public NBTTagList newHashMapNBTList(HashMap<String, Long> lines) {
		NBTTagList nbttaglist = new NBTTagList();
		HashMap<String, Long> lines2 = lines;
		for (String s : lines2.keySet()) {
			NBTTagCompound nbttagcompound = new NBTTagCompound();
			nbttagcompound.setString("Line", s);
			nbttagcompound.setLong("Time", lines.get(s));
			nbttaglist.appendTag(nbttagcompound);
		}
		return nbttaglist;
	}

	public HashMap<String, Long> getNBTLines(NBTTagList tagList) {
		HashMap<String, Long> map = new HashMap<String, Long>();
		for (int i = 0; i < tagList.tagCount(); i++) {
			NBTTagCompound nbttagcompound = tagList.getCompoundTagAt(i);
			String line = nbttagcompound.getString("Line");
			long time = nbttagcompound.getLong("Time");
			map.put(line, time);

		}
		return map;
	}
    
	private boolean giveItems(EntityPlayer player){		
		PlayerItemGiverData data = PlayerDataController.instance.getPlayerData(player).itemgiverData;
		if(!canPlayerInteract(data)){
			return false;
		}
		
		Vector<ItemStack> items = new Vector<ItemStack>();
		Vector<ItemStack> toGive = new Vector<ItemStack>();
		
		for(ItemStack is : inventory.items.values())
			if(is != null)
				items.add(is.copy());
		if(items.isEmpty())
			return false;
		if(isAllGiver()){
			toGive = items;
		}
		else if(isRemainingGiver()){
			for(ItemStack is : items){
				if(!playerHasItem(player,is.getItem()))
					toGive.add(is);
			}
		}
		else if(isRandomGiver()){
			toGive.add(items.get(npc.worldObj.rand.nextInt(items.size())).copy());
		}
		else if(isGiverWhenNotOwnedAny()){
			boolean ownsItems = false;
			for(ItemStack is : items){
				if(playerHasItem(player,is.getItem())){
					ownsItems = true;
					break;
				}
			}
			if(!ownsItems){
				toGive = items;
			}
			else
				return false;
		}
		else if(isChainedGiver()){
			int itemIndex = data.getItemIndex(this);
			int i = 0;
			for(ItemStack item : inventory.items.values()){
				if(i == itemIndex){
					toGive.add(item);
					break;
				}
				i++;
			}
		}
		if(toGive.isEmpty())
			return false;
		if(givePlayerItems(player,toGive)){
			if(!lines.isEmpty()){
				npc.say(player, new Line(lines.get(npc.getRNG().nextInt(lines.size()))));
			}
			if(isDaily())
				data.setTime(this,getDay());
			else
				data.setTime(this,System.currentTimeMillis());
			if(isChainedGiver())
				data.setItemIndex(this, (data.getItemIndex(this) + 1) % inventory.items.size());
			return true;
		}
		return false;
	}
	private int getDay(){
		return (int) (npc.worldObj.getTotalWorldTime() / 24000L);
	}
	private boolean canPlayerInteract(PlayerItemGiverData data) {
		if(inventory.items.isEmpty())
			return false;
		if(isOnTimer()){
			if(!data.hasInteractedBefore(this))
				return true;
			return data.getTime(this) + (cooldown*1000) < System.currentTimeMillis();
		}
		else if(isGiveOnce()){
			return !data.hasInteractedBefore(this);
		}
		else if(isDaily()){
			if(!data.hasInteractedBefore(this))
				return true;
			return getDay() > data.getTime(this);
		}
		return false;
	}

	private boolean givePlayerItems(EntityPlayer player,
			Vector<ItemStack> toGive) {
		if(toGive.isEmpty())
			return false;
		if(freeInventorySlots(player) < toGive.size())
			return false;
		for(ItemStack is : toGive){
			npc.givePlayerItem(player, is);
		}
		return true;
	}
	private boolean playerHasItem(EntityPlayer player, Item item){
		for(ItemStack is : player.inventory.mainInventory){
			if(is != null && is.getItem() == item)
				return true;
		}
		for(ItemStack is : player.inventory.armorInventory){
			if(is != null && is.getItem() == item)
				return true;
		}
		return false;
	}
	private int freeInventorySlots(EntityPlayer player){
		int i = 0;
		for(ItemStack is : player.inventory.mainInventory)
			if(is == null)
				i++;
		return i;
	}
	private boolean isRandomGiver(){
		return givingMethod == 0;
	}
	private boolean isAllGiver(){
		return givingMethod == 1;
	}
	private boolean isRemainingGiver(){
		return givingMethod == 2;
	}
	private boolean isGiverWhenNotOwnedAny(){
		return givingMethod == 3;
	}
	private boolean isChainedGiver(){
		return givingMethod == 4;
	}
	public boolean isOnTimer(){
		return cooldownType == 0;
	}
	private boolean isGiveOnce(){
		return cooldownType == 1;
	}
	private boolean isDaily(){
		return cooldownType == 2;
	}
	
	public boolean aiShouldExecute() {
		if(npc.isAttacking())
			return false;
		ticks--;
		if(ticks > 0)
			return false;
		ticks = 10;
		
		toCheck = npc.worldObj.getEntitiesWithinAABB(EntityPlayer.class, npc.boundingBox.expand(3, 3, 3));
		toCheck.removeAll(recentlyChecked);

		List<EntityPlayer> listMax = npc.worldObj.getEntitiesWithinAABB(EntityPlayer.class, npc.boundingBox.expand(10, 10, 10));
		recentlyChecked.retainAll(listMax);
		recentlyChecked.addAll(toCheck);
		return toCheck.size() > 0;
	}

	public void aiStartExecuting() {
		for(EntityPlayer player : toCheck){
			if(npc.canSee(player) && availability.isAvailable(player)){
				recentlyChecked.add(player);
				interact(player);
			}
		}
	}
	
	@Override
	public void killed() {
		// TODO Auto-generated method stub
		
	}

	private boolean interact(EntityPlayer player) {
		if(!giveItems(player))
			npc.say(player, npc.advanced.getInteractLine());
		return true;
	}

	@Override
	public void delete() {
		// TODO Auto-generated method stub
		
	}
}
