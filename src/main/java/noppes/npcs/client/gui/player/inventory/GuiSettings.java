package noppes.npcs.client.gui.player.inventory;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.config.ConfigClient;
import org.lwjgl.opengl.GL11;

public class GuiSettings extends GuiCNPCInventory implements ITextfieldListener {

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
        this.addLabel(new GuiNpcLabel(1, "Chat Bubbles", guiLeft + 8, guiTop + 14 + y));
        this.addButton(new GuiNpcButton(1, guiLeft + 105, guiTop + 9 + y, 50, 20, new String[]{"gui.no", "gui.yes"}, ConfigClient.EnableChatBubbles?1:0));

        this.addLabel(new GuiNpcLabel(3, "Tracking Alignment", guiLeft + 8 + 155, guiTop + 14 + y));
        this.addButton(new GuiNpcButton(3, guiLeft + 105 + 160, guiTop + 9 + y, 50, 20, new String[]{"TLeft", "TCenter", "TRight", "Left", "Center", "Right", "BLeft", "BCenter", "BRight"}, ConfigClient.TrackingInfoAlignment));

        y += 22;
        this.addLabel(new GuiNpcLabel(2, "Dialog Sound", guiLeft + 8, guiTop + 14 + y));
        this.addButton(new GuiNpcButton(2, guiLeft + 105, guiTop + 9 + y, 50, 20, new String[]{"gui.no", "gui.yes"}, ConfigClient.DialogSound?1:0));

        this.addLabel(new GuiNpcLabel(12,"Alignment X", guiLeft + 8 + 155, guiTop + 14 + y));
        this.addTextField(new GuiNpcTextField(12, this, this.fontRendererObj, guiLeft + 107 + 160, guiTop + 9 + y, 45, 20, ConfigClient.TrackingInfoX + ""));
        getTextField(12).integersOnly = true;
        getTextField(12).setMinMaxDefault(-2000, 2000, 0);

        y += 22;
        this.addLabel(new GuiNpcLabel(10,"Dialog Speed", guiLeft + 8, guiTop + 14 + y));
        this.addTextField(new GuiNpcTextField(10, this, this.fontRendererObj, guiLeft + 107, guiTop + 9 + y, 45, 20, ConfigClient.DialogSpeed + ""));
        getTextField(10).integersOnly = true;
        getTextField(10).setMinMaxDefault(1, 20, 10);

        this.addLabel(new GuiNpcLabel(13,"Alignment Y", guiLeft + 8 + 155, guiTop + 14 + y));
        this.addTextField(new GuiNpcTextField(13, this, this.fontRendererObj, guiLeft + 107 + 160, guiTop + 9 + y, 45, 20, ConfigClient.TrackingInfoY + ""));
        getTextField(13).integersOnly = true;
        getTextField(13).setMinMaxDefault(-2000, 2000, 0);


        // Planned
        // Clear Skin Cache
        // Clear Tracked Quest
        //
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
	protected void actionPerformed(GuiButton btn){
        if (btn.id >= 100 && btn.id <= 105) {
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
        if(button.id == 3){
            ConfigClient.TrackingInfoAlignment = button.getValue();
            ConfigClient.TrackingInfoAlignmentProperty.set(ConfigClient.TrackingInfoAlignment);
        }
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

        // Quest Tracking
        if(textfield.id == 12){
            ConfigClient.TrackingInfoX = textfield.getInteger();
            ConfigClient.TrackingInfoXProperty.set(ConfigClient.TrackingInfoX);
        }
        if(textfield.id == 13){
            ConfigClient.TrackingInfoY = textfield.getInteger();
            ConfigClient.TrackingInfoYProperty.set(ConfigClient.TrackingInfoY);
        }
    }
}
