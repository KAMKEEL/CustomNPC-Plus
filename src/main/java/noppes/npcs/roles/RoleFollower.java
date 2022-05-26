package noppes.npcs.roles;

import java.util.HashMap;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.StatCollector;
import noppes.npcs.NBTTags;
import noppes.npcs.NoppesStringUtils;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.NpcMiscInventory;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumJobType;
import noppes.npcs.entity.EntityNPCInterface;

public class RoleFollower extends RoleInterface{
	
	private String ownerUUID;
	public boolean isFollowing = true;
	public HashMap<Integer,Integer> rates;
	public NpcMiscInventory inventory;
	public String dialogHire = StatCollector.translateToLocal("follower.hireText") + " {days} " + StatCollector.translateToLocal("follower.days");
	public String dialogFarewell = StatCollector.translateToLocal("follower.farewellText") + " {player}";
	public int daysHired;
	public long hiredTime;
	public boolean disableGui = false;
	public boolean infiniteDays = false;
	public boolean refuseSoulStone = false;
	
	public EntityPlayer owner = null;
	
	public RoleFollower(EntityNPCInterface npc) {
		super(npc);
		inventory = new NpcMiscInventory(3);
		rates = new HashMap<Integer, Integer>();
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbttagcompound) {
		nbttagcompound.setInteger("MercenaryDaysHired", daysHired);
		nbttagcompound.setLong("MercenaryHiredTime", hiredTime);
		nbttagcompound.setString("MercenaryDialogHired", dialogHire);
		nbttagcompound.setString("MercenaryDialogFarewell", dialogFarewell);
		if(hasOwner())
			nbttagcompound.setString("MercenaryOwner", ownerUUID);
    	nbttagcompound.setTag("MercenaryDayRates", NBTTags.nbtIntegerIntegerMap(rates));
    	nbttagcompound.setTag("MercenaryInv", inventory.getToNBT());
    	nbttagcompound.setBoolean("MercenaryIsFollowing", isFollowing);
    	nbttagcompound.setBoolean("MercenaryDisableGui", disableGui);
    	nbttagcompound.setBoolean("MercenaryInfiniteDays", infiniteDays);
    	nbttagcompound.setBoolean("MercenaryRefuseSoulstone", refuseSoulStone);
    	return nbttagcompound;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		ownerUUID = nbttagcompound.getString("MercenaryOwner");
		daysHired = nbttagcompound.getInteger("MercenaryDaysHired");
		hiredTime = nbttagcompound.getLong("MercenaryHiredTime");
		dialogHire = nbttagcompound.getString("MercenaryDialogHired");
		dialogFarewell = nbttagcompound.getString("MercenaryDialogFarewell");
		rates = NBTTags.getIntegerIntegerMap(nbttagcompound.getTagList("MercenaryDayRates", 10));
		inventory.setFromNBT(nbttagcompound.getCompoundTag("MercenaryInv"));
		isFollowing = nbttagcompound.getBoolean("MercenaryIsFollowing");
		disableGui = nbttagcompound.getBoolean("MercenaryDisableGui");
		infiniteDays = nbttagcompound.getBoolean("MercenaryInfiniteDays");
		refuseSoulStone = nbttagcompound.getBoolean("MercenaryRefuseSoulstone");
	}

	@Override
	public boolean aiShouldExecute() {
		owner = getOwner();
		if(!infiniteDays && owner != null && getDaysLeft() <= 0){
			owner.addChatMessage(new ChatComponentTranslation(NoppesStringUtils.formatText(dialogFarewell, owner, npc)));
			killed();
		}
		return false;
	}
	
	public EntityPlayer getOwner(){
		if(ownerUUID == null || ownerUUID.isEmpty())
			return null;
		try{
	        UUID uuid = UUID.fromString(ownerUUID);
	        if(uuid != null)
	        	return npc.worldObj.func_152378_a(uuid);
		}
		catch(IllegalArgumentException ex){
			
		}
        
		return npc.worldObj.getPlayerEntityByName(ownerUUID);
	}
	public boolean hasOwner(){
		if(!infiniteDays && daysHired <= 0)
			return false;
		return ownerUUID != null && !ownerUUID.isEmpty();
	}
	@Override
	public void killed() {
		ownerUUID = null;
		daysHired = 0;
		hiredTime = 0;
		isFollowing = true;
	}
	
	public int getDaysLeft(){
		if(infiniteDays)
			return 100;
		if(daysHired <= 0)
			return 0;
		int days = (int) ((npc.worldObj.getTotalWorldTime()- hiredTime) / 24000);
		return daysHired - days;
	}
	
	public void addDays(int days) {
		daysHired = days + getDaysLeft();
		hiredTime = npc.worldObj.getTotalWorldTime();
	}
	
	@Override
	public void interact(EntityPlayer player) {
		if(ownerUUID == null || ownerUUID.isEmpty()){
			npc.say(player, npc.advanced.getInteractLine());
			NoppesUtilServer.sendOpenGui(player, EnumGuiType.PlayerFollowerHire, npc);
		}
		else if(player == owner && !disableGui){
			NoppesUtilServer.sendOpenGui(player, EnumGuiType.PlayerFollower, npc);
		}
	}
	
	@Override
	public boolean defendOwner() {
		return isFollowing() && npc.advanced.job == EnumJobType.Guard;
	}

	@Override
	public void delete() {
		
	}
	
	public boolean isFollowing(){
		return owner != null && isFollowing && getDaysLeft() > 0;
	}

	public void setOwner(EntityPlayer player) {
		UUID id = player.getUniqueID();
		if(ownerUUID == null || id == null || !ownerUUID.equals(id))
			killed();
		ownerUUID = id.toString();
	}
	
	
}
