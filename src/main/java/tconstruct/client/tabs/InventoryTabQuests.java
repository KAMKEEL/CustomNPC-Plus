package tconstruct.client.tabs;

import net.minecraft.client.Minecraft;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C0DPacketCloseWindow;
import noppes.npcs.CustomItems;
import noppes.npcs.client.gui.player.GuiFaction;
import noppes.npcs.client.gui.player.GuiQuestLog;
import noppes.npcs.util.CustomNPCsScheduler;

public class InventoryTabQuests extends AbstractTab {
	public InventoryTabQuests() {
		super(0, 0, 0, new ItemStack(CustomItems.letter));

		if(CustomItems.letter == null)
			renderStack = new ItemStack(Items.book);
	}

	@Override
	public void onTabClicked() {
		CustomNPCsScheduler.runTack(() -> {
			Minecraft mc = Minecraft.getMinecraft();
			mc.displayGuiScreen(new GuiQuestLog(mc.thePlayer));
		}, 100);
	}

	@Override
	public boolean shouldAddToList() {
		return true;
	}
}