package noppes.npcs.client.gui.player.inventory;

import kamkeel.npcs.network.PacketClient;
import kamkeel.npcs.network.packets.request.quest.QuestUntrackPacket;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import noppes.npcs.client.ClientCacheHandler;
import noppes.npcs.client.gui.hud.GuiHudEditor;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.config.ConfigClient;
import noppes.npcs.config.ConfigMain;
import org.lwjgl.opengl.GL11;
import tconstruct.client.tabs.AbstractTab;

public class GuiSettings extends GuiCNPCInventory implements ITextfieldListener, GuiYesNoCallback {

	private final ResourceLocation resource = new ResourceLocation("customnpcs","textures/gui/standardbg.png");

	public GuiSettings() {
		super();
		xSize = 280;
		ySize = 180;
        this.drawDefaultBackground = false;
        title = "";
	}

	@Override
    public void initGui()
    {
		super.initGui();

        int y = 0;
        this.addLabel(new GuiNpcLabel(1, "settings.chatBubbles", guiLeft + 8, guiTop + 14 + y));
        this.addButton(new GuiNpcButton(1, guiLeft + 105, guiTop + 9 + y, 50, 20, new String[]{"gui.no", "gui.yes"}, ConfigClient.EnableChatBubbles?1:0));

        y += 22;
        this.addLabel(new GuiNpcLabel(2, "settings.dialogSound", guiLeft + 8, guiTop + 14 + y));
        this.addButton(new GuiNpcButton(2, guiLeft + 105, guiTop + 9 + y, 50, 20, new String[]{"gui.no", "gui.yes"}, ConfigClient.DialogSound?1:0));

//        this.addLabel(new GuiNpcLabel(12,"settings.alignmentX", guiLeft + 8 + 155, guiTop + 14 + y));
//        this.addTextField(new GuiNpcTextField(12, this, this.fontRendererObj, guiLeft + 107 + 160, guiTop + 9 + y, 45, 20, ConfigClient.TrackingInfoX + ""));
//        getTextField(12).integersOnly = true;
//        getTextField(12).setMinMaxDefault(-2000, 2000, 0);

        y += 22;
        this.addLabel(new GuiNpcLabel(10,"settings.dialogSpeed", guiLeft + 8, guiTop + 14 + y));
        this.addTextField(new GuiNpcTextField(10, this, this.fontRendererObj, guiLeft + 107, guiTop + 9 + y, 45, 20, ConfigClient.DialogSpeed + ""));
        getTextField(10).integersOnly = true;
        getTextField(10).setMinMaxDefault(1, 20, 10);


        y += 22;

        this.addLabel(new GuiNpcLabel(6, "settings.chatAlerts", guiLeft + 8, guiTop + 14 + y));
        this.addButton(new GuiNpcButton(6, guiLeft + 105, guiTop + 9 + y, 50, 20, new String[]{"gui.no", "gui.yes"}, ConfigClient.ChatAlerts?1:0));

        y += 22;

        this.addLabel(new GuiNpcLabel(7, "settings.bannerAlerts", guiLeft + 8, guiTop + 14 + y));
        this.addButton(new GuiNpcButton(7, guiLeft + 105, guiTop + 9 + y, 50, 20, new String[]{"gui.no", "gui.yes"}, ConfigClient.BannerAlerts?1:0));

        y += 22;

        this.addLabel(new GuiNpcLabel(8, "settings.effectsBar", guiLeft + 8, guiTop + 14 + y));
        this.addButton(new GuiNpcButton(8, guiLeft + 105, guiTop + 9 + y, 50, 20, new String[]{"gui.no", "gui.yes"}, ConfigClient.HideEffectsBar?1:0));

        y += 22;

        this.addLabel(new GuiNpcLabel(90, "overlayer", guiLeft + 8, guiTop + 14 + y));
        this.addButton(new GuiNpcButton(90, guiLeft + 105, guiTop + 9 + y, "EDIT"));

        y += 22;
        this.addButton(new GuiNpcButton(5, guiLeft + 8, guiTop + 9 + y, 150, 20, "settings.clearSkin"));
        this.addButton(new GuiNpcButton(4, guiLeft + 8 + 155, guiTop + 9 + y, 150, 20, "settings.clearTrack"));
    }

	@Override
    public void drawScreen(int i, int j, float f)
    {
    	drawDefaultBackground();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(resource);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, 252, 195);
		drawTexturedModalRect(guiLeft + 252, guiTop, 188, 0, 67, 195);
        super.drawScreen(i, j, f);

    }

    @Override
    public void confirmClicked(boolean flag, int i) {
        if (flag) {
            switch (i) {
                case 0:
                    // Untrack Quest
                    PacketClient.sendClient(new QuestUntrackPacket());
                    break;
                case 1:
                    // Clear Skin Cache
                    ClientCacheHandler.clearSkinCache();
                    break;
            }
            initGui();
        }
        displayGuiScreen(this);
    }

    @Override
	protected void actionPerformed(GuiButton btn){
        if(btn instanceof AbstractTab)
            return;

        if (btn.id <= -100) {
            super.actionPerformed(btn);
            return;
        }
        if(!(btn instanceof GuiNpcButton))
            return;
        GuiNpcButton button = (GuiNpcButton) btn;
        if(button.id == 1){
            ConfigClient.EnableChatBubbles = button.getValue() == 1;
            ConfigClient.EnableChatBubblesProperty.set(ConfigClient.EnableChatBubbles);
        }
        if(button.id == 2){
            ConfigClient.DialogSound = button.getValue() == 1;
            ConfigClient.DialogSoundProperty.set(ConfigClient.DialogSound);
        }
        if(button.id == 4){
            GuiYesNo yesNoTrack = new GuiYesNo(this, "Confirm", StatCollector.translateToLocal("settings.confirmClearTrack"), 0);
            displayGuiScreen(yesNoTrack);
        }
        if(button.id == 5){
            GuiYesNo yesNoSkin = new GuiYesNo(this, "Confirm", StatCollector.translateToLocal("settings.confirmClearSkin"), 1);
            displayGuiScreen(yesNoSkin);
        }
        if(button.id == 6){
            ConfigClient.ChatAlerts = button.getValue() == 1;
            ConfigClient.ChatAlertsProperty.set(ConfigClient.ChatAlerts);
        }
        if(button.id == 7){
            ConfigClient.BannerAlerts = button.getValue() == 1;
            ConfigClient.BannerAlertsProperty.set(ConfigClient.BannerAlerts);
        }
        if(button.id == 8){
            ConfigClient.HideEffectsBar = button.getValue() == 1;
            ConfigClient.HideEffectsBarProperty.set(ConfigClient.HideEffectsBar);
        }
        if(button.id == 90){
            mc.displayGuiScreen(new GuiHudEditor(this));
        }

        ConfigClient.config.save();
    }


    @Override
    public void keyTyped(char c, int i)
    {
        super.keyTyped(c, i);
        if (i == 1 || isInventoryKey(i))
        {
            close();
        }
    }

	@Override
	public void save() {}


    @Override
    public void unFocused(GuiNpcTextField textfield) {
        if(textfield.id == 10){
            ConfigClient.DialogSpeed = textfield.getInteger();
            ConfigClient.DialogSpeedProperty.set(ConfigClient.DialogSound);
        }
    }
}
