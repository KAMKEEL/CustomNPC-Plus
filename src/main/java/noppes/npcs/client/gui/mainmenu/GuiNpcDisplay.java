package noppes.npcs.client.gui.mainmenu;

import com.mojang.authlib.GameProfile;
import kamkeel.addon.client.GeckoAddonClient;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.DataDisplay;
import noppes.npcs.client.Client;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.*;
import noppes.npcs.client.gui.model.GuiCreationScreen;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.config.ConfigMain;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.controllers.data.SkinOverlay;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;

public class GuiNpcDisplay extends GuiNPCInterface2 implements ITextfieldListener, IGuiData{

	private DataDisplay display;
	public GuiNpcTextField nameText;

	public GuiNpcDisplay(EntityNPCInterface npc) {
		super(npc,1);
		display = npc.display;
		Client.sendData(EnumPacketServer.MainmenuDisplayGet);
	}

    public void initGui(){
        super.initGui();
        int y = guiTop + 4;

		addLabel(new GuiNpcLabel(0,"gui.name", guiLeft + 5, y + 5));
		nameText = new GuiNpcTextField(0,this, fontRendererObj, guiLeft + 50, y, 206, 20, display.name);
		addTextField(nameText);
		this.addButton(new GuiNpcButton(0, guiLeft + 253+52, y , 110, 20, new String[]{"display.show","display.hide","display.showAttacking"} ,display.showName));


		this.addButton(new GuiNpcButton(14, guiLeft + 259, y , 20, 20, Character.toString('\u21bb')));
		this.addButton(new GuiNpcButton(15, guiLeft + 259 + 22, y , 20, 20, Character.toString('\u22EE')));

    	y+=23;
        addLabel(new GuiNpcLabel(11,"gui.title", guiLeft + 5, y + 5));
        addTextField(new GuiNpcTextField(11,this, fontRendererObj, guiLeft + 50, y, 206, 20, display.title));

    	y+=23;
        addLabel(new GuiNpcLabel(1,"display.model", guiLeft + 5, y + 5));
    	this.addButton(new GuiNpcButton(1, guiLeft + 50, y,110,20, "selectServer.edit"));
    	addLabel(new GuiNpcLabel(2,"display.size", guiLeft + 175, y + 5));
        addTextField(new GuiNpcTextField(2,this, fontRendererObj, guiLeft + 203, y, 40, 20, display.modelSize + ""));
        getTextField(2).integersOnly = true;
        getTextField(2).setMinMaxDefault(1, ConfigMain.NpcSizeLimit, 5);

    	y+=23;
    	addLabel(new GuiNpcLabel(4,"display.texture", guiLeft + 5, y + 5));
        addTextField(new GuiNpcTextField(3,this, fontRendererObj, guiLeft + 80, y, 200, 20, display.skinType == 0?display.texture:display.url));
    	this.addButton(new GuiNpcButton(3, guiLeft + 325, y, 38, 20, "gui.select"));
    	this.addButton(new GuiNpcButton(2, guiLeft + 283, y, 40, 20, new String[]{"display.texture","display.player", "display.url", "display.urlSix"},display.skinType));
    	getButton(3).setEnabled(display.skinType == 0);
    	if(display.skinType == 1 && display.playerProfile != null)
    		getTextField(3).setText(display.playerProfile.getName());

    	y+=23;
    	addLabel(new GuiNpcLabel(8,"display.cape", guiLeft + 5, y + 5));
        addTextField(new GuiNpcTextField(8,this, fontRendererObj, guiLeft + 80, y, 200, 20, display.cloakTexture));
    	this.addButton(new GuiNpcButton(8, guiLeft + 283, y, 80, 20, "display.selectTexture"));

    	y+=23;
    	addLabel(new GuiNpcLabel(9,"display.overlay", guiLeft + 5, y + 5));

        addTextField(new GuiNpcTextField(9,this, fontRendererObj, guiLeft + 80, y, 200, 20, npc.display.skinOverlayData.overlayList.containsKey(0) ? npc.display.skinOverlayData.overlayList.get(0).getTexture() : ""));
    	this.addButton(new GuiNpcButton(9, guiLeft + 283, y, 80, 20, "display.selectTexture"));
		this.addButton(new GuiNpcButton(11, guiLeft + 365, y, 50, 20, new String[]{"display.glow","display.solid"}, !npc.display.skinOverlayData.overlayList.containsKey(0) ? 1 : npc.display.skinOverlayData.overlayList.get(0).getGlow() ? 0 : 1));
		if (!npc.display.skinOverlayData.overlayList.containsKey(0)) {
			this.getButton(11).setVisible(false);
			this.getButton(11).setEnabled(false);
		}

    	y+=23;
    	addLabel(new GuiNpcLabel(5,"display.livingAnimation", guiLeft + 5, y + 5));
    	this.addButton(new GuiNpcButton(5, guiLeft + 120, y, 50, 20, new String[]{"gui.yes","gui.no"}, display.disableLivingAnimation?1:0));

		addLabel(new GuiNpcLabel(6,"display.collides", guiLeft + 185, y + 5));
		this.addButton(new GuiNpcButton(6, guiLeft + 300, y, 100, 20, new String[]{"gui.yes","gui.no","Only CNPCs","Only Players","CNPCs & Players"}, display.collidesWith));

		y+=23;
    	addLabel(new GuiNpcLabel(7,"display.visible", guiLeft + 5, y + 5));
    	this.addButton(new GuiNpcButton(7, guiLeft + 120, y, 50, 20, new String[]{"gui.yes","gui.no","gui.partly"}, display.visible));

        this.addButton(new GuiNpcButton(13, guiLeft + 185, y , 100, 20, "display.tintsettings"));

    	y+=23;
    	addLabel(new GuiNpcLabel(10,"display.bossbar", guiLeft + 5, y + 5));
    	this.addButton(new GuiNpcButton(10, guiLeft + 60, y, 110, 20, new String[]{"display.hide","display.show","display.showAttacking"}, display.showBossBar));

		GeckoAddonClient.Instance.geckoNpcDisplayInitGui(this);
	}

	@Override
	public void unFocused(GuiNpcTextField textfield){
		if(textfield.id == 0){
			if(!textfield.isEmpty())
				display.name = textfield.getText();
			else
				textfield.setText(display.name);
		}
		else if(textfield.id == 2){
			display.modelSize = textfield.getInteger();
		}
		else if(textfield.id == 3){
			// SKIN CHANGE
			if(display.skinType == 2 || display.skinType == 3){
				display.url = textfield.getText();
			}
			else if(display.skinType == 1){
				if(!textfield.isEmpty()){
					display.playerProfile = new GameProfile(null, textfield.getText());
				}
				else
					display.playerProfile = null;
			}
			else
				display.texture = textfield.getText();
		}
		else if(textfield.id == 8){
			display.cloakTexture = textfield.getText();
		}
		else if(textfield.id == 9){
			if (!textfield.getText().isEmpty()) {
				npc.display.skinOverlayData.overlayList.put(0,new SkinOverlay(textfield.getText()));
			} else {
				npc.display.skinOverlayData.overlayList.remove(0);
			}
			initGui();
		}
		else if(textfield.id == 11){
			display.title = textfield.getText();
		}
	}
	protected void actionPerformed(GuiButton guibutton){
		GuiNpcButton button = (GuiNpcButton) guibutton;
		if(button.id == 0){
			display.showName = button.getValue();
		}
		if(button.id == 1){
			NoppesUtil.openGUI(player, new GuiCreationScreen(this, (EntityCustomNpc) npc));
			//NoppesUtil.openGUI(player, new GuiNpcModelSelection(npc,this));
		}
		if(button.id == 2){
			display.skinType = (byte) button.getValue();
			if (display.skinType != 3){
				display.url = "";
			}
			display.playerProfile = null;
			initGui();
		}
		else if(button.id == 3){
			NoppesUtil.openGUI(player, new GuiNPCTextures(npc, this));
		}
		else if(button.id == 5){
			display.disableLivingAnimation = button.getValue() == 1;
		}
		else if(button.id == 6){
			display.collidesWith = button.getValue();
		}
		else if(button.id == 7){
			display.visible = button.getValue();
		}
		// else if(button.id == 300){
			// display.squareTexture = (byte)button.getValue();
		// }
		else if(button.id == 8){
			NoppesUtil.openGUI(player, new GuiNpcTextureCloaks(npc, this));
		}
		else if(button.id == 9){
			NoppesUtil.openGUI(player, new GuiNpcTextureOverlays(npc, this));
		}
		else if(button.id == 10){
			display.showBossBar = (byte)button.getValue();
		}
		else if(button.id == 11){
			npc.display.skinOverlayData.overlayList.get(0).setGlow(button.getValue() == 0);
			npc.display.skinOverlayData.overlayList.get(0).setBlend(button.getValue() == 0);
			initGui();
		}
        else if (button.id == 13){
            setSubGui(new SubGuiNpcTint(display.customTintData));
        }
		else if(button.id == 14){
			String name = display.getRandomName();
			display.setName(name);
			getTextField(0).setText(name);
		}
		else if(button.id == 15){
			setSubGui(new SubGuiNpcName(display));
		}

		GeckoAddonClient.Instance.geckoNpcDisplayActionPerformed(this, button);
    }

	@Override
	public void save() {
		if(display.skinType == 1)
			display.loadProfile();
		npc.textureLocation = null;
		mc.renderGlobal.onEntityDestroy(npc);
		mc.renderGlobal.onEntityCreate(npc);
		Client.sendData(EnumPacketServer.MainmenuDisplaySave, display.writeToNBT(new NBTTagCompound()));

	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		display.readToNBT(compound);
		initGui();
	}

}
