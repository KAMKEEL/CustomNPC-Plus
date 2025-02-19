package tconstruct.client.tabs;

import kamkeel.npcs.addon.client.DBCClient;
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
                tabHelper();
			}
		};
		t.start();
	}

	@Override
	public boolean shouldAddToList() {
		return true;
	}

    public static void tabHelper(){
        Minecraft mc = Minecraft.getMinecraft();
        int tab = GuiCNPCInventory.activeTab;
        if(tab == -100){
            mc.displayGuiScreen(new GuiQuestLog());
        }
        if(tab == -101){
            mc.displayGuiScreen(new GuiParty());
        }
        if(tab == -102){
            mc.displayGuiScreen(new GuiFaction());
        }
        if(tab == -103){
            mc.displayGuiScreen(new GuiSettings());
        }
        if(tab == -104){
            mc.displayGuiScreen(new GuiProfiles());
        }
    }
}
