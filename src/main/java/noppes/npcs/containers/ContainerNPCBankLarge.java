package noppes.npcs.containers;

import net.minecraft.entity.player.EntityPlayer;

public class ContainerNPCBankLarge extends ContainerNPCBankInterface
{

    public ContainerNPCBankLarge(EntityPlayer player, int slot, int bankid)
    {
    	super(player,slot,bankid);
    }
    
    public boolean isUpgraded(){
    	return true;
    }
    public boolean isAvailable(){
    	return true;
    }
    public int getRowNumber() {
		return 6;
	}
}
