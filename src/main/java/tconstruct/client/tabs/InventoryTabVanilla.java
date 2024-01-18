package tconstruct.client.tabs;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

public class InventoryTabVanilla extends AbstractTab {
	public InventoryTabVanilla() {
		super(0, 0, 0, new ItemStack(Blocks.crafting_table));
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
				TabRegistry.openInventoryGui();
			}
		};
		t.start();
	}

	@Override
	public boolean shouldAddToList() {
		return true;
	}
}