package noppes.npcs.containers;

import net.minecraft.entity.player.EntityPlayer;

public class ContainerNPCBankUpgrade extends ContainerNPCBankInterface
{
    public ContainerNPCBankUpgrade(EntityPlayer player, int slot, int bankid)
    {
    	super(player,slot,bankid);
    }
    public boolean isAvailable(){
    	return true;
    }
    public boolean canBeUpgraded(){
    	return true;
    }
    public int xOffset(){
    	return 54;
    }
    public int getRowNumber() {
		return 3;
	}
}
