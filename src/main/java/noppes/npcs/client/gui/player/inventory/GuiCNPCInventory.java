package noppes.npcs.client.gui.player.inventory;

import cpw.mods.fml.common.Loader;
import kamkeel.npcs.addon.client.DBCClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.client.gui.util.GuiMenuSideButton;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.config.ConfigClient;
import noppes.npcs.config.ConfigMain;
import tconstruct.client.tabs.InventoryTabCustomNpc;
import tconstruct.client.tabs.TabRegistry;

public class GuiCNPCInventory extends GuiNPCInterface {
    public static final ResourceLocation specialIcons = new ResourceLocation("customnpcs","textures/gui/icons.png");

    public static int activeTab = 0;
    protected Minecraft mc = Minecraft.getMinecraft();

    public GuiCNPCInventory() {
        super();
        xSize = 280;
        ySize = 180;
        drawDefaultBackground = false;
    }

    public void initGui(){
        super.initGui();
        guiTop +=10;

        TabRegistry.updateTabValues(guiLeft, guiTop, InventoryTabCustomNpc.class);
        TabRegistry.addTabsToList(buttonList);


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

        if(Loader.isModLoaded("npcdbc")){
            y += 21;
            GuiMenuSideButton dbcButton = new GuiMenuSideButton(104, guiLeft + xSize + 37, this.guiTop + y, 22, 22, "");
            dbcButton.rightSided = true;
            dbcButton.active = activeTab == 4;
            dbcButton.renderIconPosX = 64;
            dbcButton.renderResource = specialIcons;
            addButton(dbcButton);
        }

        if(ConfigMain.EnableProfiles){
            y += 21;
            GuiMenuSideButton profileButton = new GuiMenuSideButton(105, guiLeft + xSize + 37, this.guiTop + y, 22, 22, "");
            profileButton.rightSided = true;
            profileButton.active = activeTab == 5;
            profileButton.renderIconPosX = 80;
            profileButton.renderResource = specialIcons;
            addButton(profileButton);
        }

        y += 21;
        GuiMenuSideButton clientButton = new GuiMenuSideButton(103, guiLeft + xSize + 37, this.guiTop + y, 22, 22, "");
        clientButton.rightSided = true;
        clientButton.active = activeTab == 3;
        clientButton.renderIconPosX = 16;
        clientButton.renderResource = specialIcons;
        addButton(clientButton);
    }

    @Override
    protected void actionPerformed(GuiButton guibutton){
        if (guibutton.id >= 100) {
            if (guibutton.id == 100 && activeTab != 0) {
                activeTab = 0;
                mc.displayGuiScreen(new GuiQuestLog());
            }
            if (guibutton.id == 101 && activeTab != 1) {
                activeTab = 1;
                mc.displayGuiScreen(new GuiParty());
            }
            if (guibutton.id == 102 && activeTab != 2) {
                activeTab = 2;
                mc.displayGuiScreen(new GuiFaction());
            }
            if (guibutton.id == 103 && activeTab != 3) {
                activeTab = 3;
                mc.displayGuiScreen(new GuiSettings());
            }
            if (guibutton.id == 104 && activeTab != 4) {
                activeTab = 4;
                mc.displayGuiScreen(DBCClient.Instance.inventoryGUI());
            }
            if (guibutton.id == 105 && activeTab != 5) {
                activeTab = 5;
                mc.displayGuiScreen(new GuiProfiles());
            }
        }
    }

    @Override
    public void save() {}
}
