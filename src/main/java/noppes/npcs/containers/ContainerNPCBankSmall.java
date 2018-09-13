package noppes.npcs.containers;

import net.minecraft.entity.player.EntityPlayer;

public class ContainerNPCBankSmall extends ContainerNPCBankInterface
{

    public ContainerNPCBankSmall(EntityPlayer player, int slot, int bankid)
    {
    	super(player,slot,bankid);
    }
    public boolean isAvailable(){
    	return true;
    }
    public int getRowNumber() {
		return 3;
	}
}
