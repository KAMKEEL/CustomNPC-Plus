package noppes.npcs.scripted;

import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.controllers.PixelmonHelper;

public class ScriptPixelmonPlayerData {
	private EntityPlayerMP player;
	public ScriptPixelmonPlayerData(EntityPlayerMP player){
		this.player = player;
	}
	
	public ScriptPixelmon getPartySlot(int slot){
		NBTTagCompound compound = PixelmonHelper.getPartySlot(slot, player);
		if(compound == null)
			return null;
		EntityTameable pixelmon = PixelmonHelper.pixelmonFromNBT(compound, player);
		return new ScriptPixelmon(pixelmon, compound);
	}
	
	public int countPCPixelmon(){
		return PixelmonHelper.countPCPixelmon(player);
	}
}
