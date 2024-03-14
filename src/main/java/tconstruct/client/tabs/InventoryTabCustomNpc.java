package tconstruct.client.tabs;

import net.minecraft.client.Minecraft;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import noppes.npcs.CustomItems;
import noppes.npcs.client.gui.player.inventory.*;

public class InventoryTabCustomNpc extends AbstractTab {
	public InventoryTabCustomNpc() {
		super(0, 0, 0, new ItemStack(CustomItems.letter));

		if(CustomItems.letter == null)
			renderStack = new ItemStack(Items.book);
	}

	@Override
	public void onTabClicked() {


		Thread t = new Thread(){
			@Override
			public void run(){
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				Minecraft mc = Minecraft.getMinecraft();
                switch (GuiCNPCInventory.activeTab){
                    case 0:
                        mc.displayGuiScreen(new GuiQuestLog());
                        break;
                    case 1:
                        mc.displayGuiScreen(new GuiParty());
                        break;
                    case 2:
                        mc.displayGuiScreen(new GuiFaction());
                        break;
                    case 3:
                        mc.displayGuiScreen(new GuiSettings());
                        break;
                }
			}
		};
		t.start();
	}

	@Override
	public boolean shouldAddToList() {
		return true;
	}
}
