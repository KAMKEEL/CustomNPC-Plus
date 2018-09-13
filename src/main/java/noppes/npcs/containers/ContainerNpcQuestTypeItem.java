package noppes.npcs.containers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.client.gui.global.GuiNPCManageQuest;
import noppes.npcs.controllers.Quest;
import noppes.npcs.quests.QuestItem;

public class ContainerNpcQuestTypeItem extends Container
{

    public ContainerNpcQuestTypeItem(EntityPlayer player)
    {
    	Quest quest = NoppesUtilServer.getEditingQuest(player);
    	if(player.worldObj.isRemote)
        	quest = GuiNPCManageQuest.quest;
        for(int i1 = 0; i1 < 3; i1++)
        	addSlotToContainer(new Slot(((QuestItem)quest.questInterface).items, i1, 44, 39 + i1 * 25));
                
        for(int i1 = 0; i1 < 3; i1++)
        {
            for(int l1 = 0; l1 < 9; l1++)
            {
            	addSlotToContainer(new Slot(player.inventory, l1 + i1 * 9 + 9, 8 + l1 * 18, 113 + i1 * 18));
            }

        }

        for(int j1 = 0; j1 < 9; j1++)
        {
        	addSlotToContainer(new Slot(player.inventory, j1, 8 + j1 * 18, 171));
        }
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int i)
    {
        return null;
    }
	@Override
	public boolean canInteractWith(EntityPlayer entityplayer) {
		return true;
	}
}
