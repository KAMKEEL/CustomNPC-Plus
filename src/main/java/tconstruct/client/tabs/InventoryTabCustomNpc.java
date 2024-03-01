package tconstruct.client.tabs;

import net.minecraft.client.Minecraft;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import noppes.npcs.CustomItems;
import noppes.npcs.client.gui.player.inventory.GuiCNPCInventory;
import noppes.npcs.client.gui.player.inventory.GuiQuestLog;

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
                if(mc.currentScreen instanceof GuiCNPCInventory)
                    return;

				mc.displayGuiScreen(new GuiQuestLog(mc.thePlayer));
			}
		};
		t.start();
	}

	@Override
	public boolean shouldAddToList() {
		return true;
	}
}
