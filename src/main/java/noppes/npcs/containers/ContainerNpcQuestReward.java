package noppes.npcs.containers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.client.gui.global.GuiNPCManageQuest;
import noppes.npcs.controllers.Quest;

public class ContainerNpcQuestReward extends Container{
    public ContainerNpcQuestReward(EntityPlayer player){
    	Quest quest = NoppesUtilServer.getEditingQuest(player);
    	if(player.worldObj.isRemote)
        	quest = GuiNPCManageQuest.quest;
        for(int l = 0; l < 3; l++){
            for(int k1 = 0; k1 < 3; k1++){
            	addSlotToContainer(new Slot(quest.rewardItems, k1 + l * 3, 105 + k1 * 18, 17 + l * 18));
            }
        }
        
        for(int i1 = 0; i1 < 3; i1++){
            for(int l1 = 0; l1 < 9; l1++){
            	addSlotToContainer(new Slot(player.inventory, l1 + i1 * 9 + 9, 8 + l1 * 18, 84 + i1 * 18));
            }
        }

        for(int j1 = 0; j1 < 9; j1++){
        	addSlotToContainer(new Slot(player.inventory, j1, 8 + j1 * 18, 142));
        }
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int i){
        return null;
    }
	@Override
	public boolean canInteractWith(EntityPlayer entityplayer) {
		return true;
	}
}
