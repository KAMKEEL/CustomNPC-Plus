package tconstruct.client.tabs;

import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import noppes.npcs.CustomItems;
import noppes.npcs.client.gui.player.GuiFaction;

public class InventoryTabFactions extends AbstractTab {
	public InventoryTabFactions() {
		super(0, 0, 0, new ItemStack(CustomItems.wallBanner, 1, 1));
		if(CustomItems.wallBanner == null)
			renderStack = new ItemStack(Blocks.tnt);
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
				mc.displayGuiScreen(new GuiFaction());
			}
		};
		t.start();
	}

	@Override
	public boolean shouldAddToList() {
		return true;
	}
}