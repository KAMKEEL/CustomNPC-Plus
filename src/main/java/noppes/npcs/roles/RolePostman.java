package noppes.npcs.roles;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.NpcMiscInventory;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.controllers.PlayerDataController;
import noppes.npcs.entity.EntityNPCInterface;

public class RolePostman extends RoleInterface{

	public NpcMiscInventory inventory = new NpcMiscInventory(1);
	private List<EntityPlayer> recentlyChecked = new ArrayList<EntityPlayer>();
	private List<EntityPlayer> toCheck;
	
	public RolePostman(EntityNPCInterface npc) {
		super(npc);
	}

	public boolean aiShouldExecute() {
		if(npc.ticksExisted % 20 != 0)
			return false;

		toCheck = npc.worldObj.getEntitiesWithinAABB(EntityPlayer.class, npc.boundingBox.expand(10, 10, 10));
		toCheck.removeAll(recentlyChecked);

		List<EntityPlayer> listMax = npc.worldObj.getEntitiesWithinAABB(EntityPlayer.class, npc.boundingBox.expand(20, 20, 20));
		recentlyChecked.retainAll(listMax);
		recentlyChecked.addAll(toCheck);
		
		for(EntityPlayer player : toCheck){
			if(PlayerDataController.instance.hasMail(player))
				player.addChatMessage(new ChatComponentTranslation("You've got mail"));			
		}
		return false;
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbttagcompound) {
    	nbttagcompound.setTag("PostInv", inventory.getToNBT());
    	return nbttagcompound;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		inventory.setFromNBT(nbttagcompound.getCompoundTag("PostInv"));
	}


	@Override
	public void interact(EntityPlayer player) {
		player.openGui(CustomNpcs.instance, EnumGuiType.PlayerMailman.ordinal(), player.worldObj, 1, 1, 0);
	}

}
