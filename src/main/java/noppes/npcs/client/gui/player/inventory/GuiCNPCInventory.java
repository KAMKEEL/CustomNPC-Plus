package noppes.npcs.client.gui.player.inventory;

import net.minecraft.client.Minecraft;
import net.minecraft.init.Items;
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
    public static final ResourceLocation specialIcons = new ResourceLocation("customnpcs","textures/gui/icons.png");

    public static int activeTab = 0;
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

        int y = 3;
        GuiMenuSideButton questsButton = new GuiMenuSideButton(100, guiLeft + xSize + 37, this.guiTop + y, 22, 22, "");
        questsButton.rightSided = true;
        questsButton.active = activeTab == 0;
        questsButton.renderIconPosX = 32;
        questsButton.renderResource = specialIcons;
        addButton(questsButton);

        y += 21;
        GuiMenuSideButton partyButton = new GuiMenuSideButton(101, guiLeft + xSize + 37, this.guiTop + y, 22, 22, "");
        partyButton.rightSided = true;
        partyButton.active = activeTab == 1;
        partyButton.renderResource = specialIcons;
        addButton(partyButton);

        if(ConfigClient.enableFactionTab){
            y += 21;
            GuiMenuSideButton factionButton = new GuiMenuSideButton(102, guiLeft + xSize + 37, this.guiTop + y, 22, 22, "");
            factionButton.rightSided = true;
            factionButton.active = activeTab == 2;
            factionButton.renderIconPosX = 48;
            factionButton.renderResource = specialIcons;
            addButton(factionButton);
        }

        y += 21;
        GuiMenuSideButton clientButton = new GuiMenuSideButton(101, guiLeft + xSize + 37, this.guiTop + y, 22, 22, "");
        clientButton.rightSided = true;
        clientButton.active = activeTab == 3;
        clientButton.renderIconPosX = 16;
        clientButton.renderResource = specialIcons;
        addButton(clientButton);
    }

    @Override
    public void save() {}
}
