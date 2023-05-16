package tconstruct.client.tabs;

import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import noppes.npcs.CustomItems;
import noppes.npcs.client.gui.player.GuiFaction;
import noppes.npcs.util.CustomNPCsScheduler;

public class InventoryTabFactions extends AbstractTab {
	public InventoryTabFactions() {
		super(0, 0, 0, new ItemStack(CustomItems.wallBanner, 1, 1));
		if(CustomItems.wallBanner == null)
			renderStack = new ItemStack(Blocks.tnt);
	}

	@Override
	public void onTabClicked() {
		CustomNPCsScheduler.runTack(() -> {
			Minecraft mc = Minecraft.getMinecraft();
			mc.displayGuiScreen(new GuiFaction());
		}, 100);
	}

	@Override
	public boolean shouldAddToList() {
		return true;
	}
}