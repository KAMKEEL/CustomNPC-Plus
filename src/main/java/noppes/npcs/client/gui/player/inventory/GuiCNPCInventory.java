package noppes.npcs.client.gui.player.inventory;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.CustomItems;
import noppes.npcs.client.gui.util.GuiMenuSideButton;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.config.ConfigClient;
import tconstruct.client.tabs.InventoryTabCustomNpc;
import tconstruct.client.tabs.TabRegistry;

public class GuiCNPCInventory extends GuiNPCInterface {
    private final ResourceLocation resource = new ResourceLocation("customnpcs","textures/gui/standardbg.png");
    protected static int activeTab = 0;
    private Minecraft mc = Minecraft.getMinecraft();

    public GuiCNPCInventory() {
        super();
        xSize = 280;
        ySize = 180;
        drawDefaultBackground = false;
    }

    public void initGui(){
        super.initGui();
        guiTop +=10;

        TabRegistry.addTabsToList(buttonList);
        TabRegistry.updateTabValues(guiLeft, guiTop, InventoryTabCustomNpc.class);

        GuiMenuSideButton questsButton = new GuiMenuSideButton(100, guiLeft + xSize + 37, this.guiTop + 3, 22, 22, "");
        questsButton.rightSided = true;
        questsButton.active = activeTab == 0;
        questsButton.renderStack = new ItemStack(CustomItems.letter);
        addButton(questsButton);

        GuiMenuSideButton partyButton = new GuiMenuSideButton(101, guiLeft + xSize + 37, this.guiTop + 3 + 21, 22, 22, "");
        partyButton.rightSided = true;
        partyButton.active = activeTab == 1;
        partyButton.renderStack = new ItemStack(CustomItems.bag);
        addButton(partyButton);
        
        if(ConfigClient.enableFactionTab){
            GuiMenuSideButton factionButton = new GuiMenuSideButton(102, guiLeft + xSize + 37, this.guiTop + 3 + 21*2, 22, 22, "");
            factionButton.rightSided = true;
            factionButton.active = activeTab == 2;
            factionButton.renderStack = new ItemStack(CustomItems.wallBanner);
            addButton(factionButton);
        }
    }

    @Override
    public void save() {}
}
