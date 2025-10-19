package noppes.npcs.client.gui.player.inventory;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.client.ClientCacheHandler;
import noppes.npcs.client.gui.util.GuiEffectBar;
import noppes.npcs.client.gui.util.GuiMenuSideButton;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.config.ConfigClient;
import noppes.npcs.controllers.CustomEffectController;
import noppes.npcs.controllers.data.CustomEffect;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.controllers.data.PlayerEffect;
import org.lwjgl.input.Mouse;
import tconstruct.client.tabs.InventoryTabCustomNpc;
import tconstruct.client.tabs.TabRegistry;

public class GuiCNPCInventory extends GuiNPCInterface {
    public static final ResourceLocation specialIcons = new ResourceLocation("customnpcs", "textures/gui/icons.png");

    public static int activeTab = -100;
    protected Minecraft mc = Minecraft.getMinecraft();
    private GuiEffectBar effectBar;

    public GuiCNPCInventory() {
        super();
        xSize = 280;
        ySize = 180;
        drawDefaultBackground = false;
    }

    public void initGui() {
        super.initGui();
        guiTop += 10;

        TabRegistry.updateTabValues(guiLeft, guiTop, InventoryTabCustomNpc.class);
        TabRegistry.addTabsToList(buttonList);


        int y = 3;
        GuiMenuSideButton questsButton = new GuiMenuSideButton(-100, guiLeft + xSize + 37, this.guiTop + y, 22, 22, "");
        questsButton.rightSided = true;
        questsButton.active = activeTab == -100;
        questsButton.renderIconPosX = 32;
        questsButton.renderResource = specialIcons;
        addSideButton(questsButton);

        if (ClientCacheHandler.allowParties) {
            y += 21;
            GuiMenuSideButton partyButton = new GuiMenuSideButton(-101, guiLeft + xSize + 37, this.guiTop + y, 22, 22, "");
            partyButton.rightSided = true;
            partyButton.active = activeTab == -101;
            partyButton.renderResource = specialIcons;
            addSideButton(partyButton);
        }
        if (ConfigClient.enableFactionTab) {
            y += 21;
            GuiMenuSideButton factionButton = new GuiMenuSideButton(-102, guiLeft + xSize + 37, this.guiTop + y, 22, 22, "");
            factionButton.rightSided = true;
            factionButton.active = activeTab == -102;
            factionButton.renderIconPosX = 48;
            factionButton.renderResource = specialIcons;
            addSideButton(factionButton);
        }
        if (ClientCacheHandler.allowProfiles) {
            y += 21;
            GuiMenuSideButton profileButton = new GuiMenuSideButton(-104, guiLeft + xSize + 37, this.guiTop + y, 22, 22, "");
            profileButton.rightSided = true;
            profileButton.active = activeTab == -104;
            profileButton.renderIconPosX = 64;
            profileButton.renderResource = specialIcons;
            addSideButton(profileButton);
        }

        y += 21;
        GuiMenuSideButton clientButton = new GuiMenuSideButton(-103, guiLeft + xSize + 37, this.guiTop + y, 22, 22, "");
        clientButton.rightSided = true;
        clientButton.active = activeTab == -103;
        clientButton.renderIconPosX = 16;
        clientButton.renderResource = specialIcons;
        addSideButton(clientButton);

        int effectBarX = guiLeft - 40;
        int effectBarY = guiTop + 10;
        int effectBarWidth = 28;
        int effectBarHeight = ySize;
        effectBar = new GuiEffectBar(effectBarX, effectBarY, effectBarWidth, effectBarHeight);
    }

    private void updateEffectBar() {
        if (effectBar != null) {
            effectBar.entries.clear();
            PlayerData data = PlayerData.get(mc.thePlayer);
            if (data != null && data.effectData != null) {
                for (PlayerEffect pe : data.effectData.getEffects().values()) {
                    // TODO: Ask for information where the client uses the controllers so we can later optimize them for ON-REQUEST.
                    CustomEffect effect = CustomEffectController.getInstance().get(pe.id, pe.index);
                    if (effect != null) {
                        effectBar.entries.add(new GuiEffectBar.EffectEntry(effect, pe));
                    }
                }
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        updateEffectBar();
        super.drawScreen(mouseX, mouseY, partialTicks);
        if (!ConfigClient.HideEffectsBar && effectBar != null && !effectBar.entries.isEmpty()) {
            if (activeTab != -100)
                effectBar.drawScreen(mouseX, mouseY, partialTicks);
        }
    }

    @Override
    public void handleMouseInput() {
        int delta = Mouse.getDWheel();
        if (delta != 0 && effectBar != null) {
            int mouseX = Mouse.getX() * this.width / mc.displayWidth;
            int mouseY = this.height - Mouse.getY() * this.height / mc.displayHeight - 1;
            if (mouseX >= effectBar.x && mouseX < effectBar.x + effectBar.width &&
                mouseY >= effectBar.y && mouseY < effectBar.y + effectBar.height) {
                // Use delta/120 to convert the typical scroll wheel values to ±1
                effectBar.mouseScrolled(delta / 120);
            }
        }
        super.handleMouseInput();
    }


    @Override
    protected void actionPerformed(GuiButton guibutton) {
        if (guibutton.id <= -100) {
            if (guibutton.id == -100 && activeTab != -100) {
                activeTab = -100;
                mc.displayGuiScreen(new GuiQuestLog());
            }
            if (guibutton.id == -101 && activeTab != -101) {
                activeTab = -101;
                mc.displayGuiScreen(new GuiParty());
            }
            if (guibutton.id == -102 && activeTab != -102) {
                activeTab = -102;
                mc.displayGuiScreen(new GuiFaction());
            }
            if (guibutton.id == -103 && activeTab != -103) {
                activeTab = -103;
                mc.displayGuiScreen(new GuiSettings());
            }
            if (guibutton.id == -104 && activeTab != -104) {
                activeTab = -104;
                mc.displayGuiScreen(new GuiProfiles());
            }
        }
    }

    @Override
    public void save() {
    }
}
