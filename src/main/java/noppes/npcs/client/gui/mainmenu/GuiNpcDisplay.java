package noppes.npcs.client.gui.mainmenu;

import com.mojang.authlib.GameProfile;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.DataDisplay;
import noppes.npcs.client.Client;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.GuiNPCTextures;
import noppes.npcs.client.gui.GuiNpcTextureCloaks;
import noppes.npcs.client.gui.GuiNpcTextureOverlays;
import noppes.npcs.client.gui.model.GuiCreationScreen;
import noppes.npcs.client.gui.util.GuiHoverText;
import noppes.npcs.client.gui.util.GuiNPCInterface2;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;

public class GuiNpcDisplay extends GuiNPCInterface2 implements ITextfieldListener, IGuiData{

	private DataDisplay display;
	
	public GuiNpcDisplay(EntityNPCInterface npc) {
		super(npc,1);
		display = npc.display;
		Client.sendData(EnumPacketServer.MainmenuDisplayGet);
	}

    public void initGui(){
        super.initGui();
        int y = guiTop + 4;
        addLabel(new GuiNpcLabel(0,"gui.name", guiLeft + 5, y + 5));
        addTextField(new GuiNpcTextField(0,this, fontRendererObj, guiLeft + 50, y, 200, 20, display.name));
    	this.addButton(new GuiNpcButton(0, guiLeft + 253, y , 110, 20, new String[]{"display.show","display.hide","display.showAttacking"} ,display.showName));

    	y+=23;
        addLabel(new GuiNpcLabel(11,"gui.title", guiLeft + 5, y + 5));
        addTextField(new GuiNpcTextField(11,this, fontRendererObj, guiLeft + 50, y, 200, 20, display.title));

    	y+=23;
        addLabel(new GuiNpcLabel(1,"display.model", guiLeft + 5, y + 5));
    	this.addButton(new GuiNpcButton(1, guiLeft + 50, y,110,20, "selectServer.edit"));
    	addLabel(new GuiNpcLabel(2,"display.size", guiLeft + 175, y + 5));
        addTextField(new GuiNpcTextField(2,this, fontRendererObj, guiLeft + 203, y, 40, 20, display.modelSize + ""));
        getTextField(2).numbersOnly = true;
        getTextField(2).setMinMaxDefault(1, 30, 5);
        addLabel(new GuiNpcLabel(3,"(1-30)", guiLeft + 246 , y + 5));

    	y+=23;
    	addLabel(new GuiNpcLabel(4,"display.texture", guiLeft + 5, y + 5));
        addTextField(new GuiNpcTextField(3,this, fontRendererObj, guiLeft + 80, y, 200, 20, display.skinType == 0?display.texture:display.url));
    	this.addButton(new GuiNpcButton(3, guiLeft + 325, y, 38, 20, "mco.template.button.select"));
    	this.addButton(new GuiNpcButton(2, guiLeft + 283, y, 40, 20, new String[]{"display.texture","display.player", "display.url"},display.skinType));
    	getButton(3).setEnabled(display.skinType == 0);
    	if(display.skinType == 1 && display.playerProfile != null)
    		getTextField(3).setText(display.playerProfile.getName());

    	y+=23;
    	addLabel(new GuiNpcLabel(8,"display.cape", guiLeft + 5, y + 5));
        addTextField(new GuiNpcTextField(8,this, fontRendererObj, guiLeft + 80, y, 200, 20, display.cloakTexture));
    	this.addButton(new GuiNpcButton(8, guiLeft + 283, y, 80, 20, "display.selectTexture"));

    	y+=23;
    	addLabel(new GuiNpcLabel(9,"display.overlay", guiLeft + 5, y + 5));
        addTextField(new GuiNpcTextField(9,this, fontRendererObj, guiLeft + 80, y, 200, 20, display.glowTexture));
    	this.addButton(new GuiNpcButton(9, guiLeft + 283, y, 80, 20, "display.selectTexture"));

    	y+=23;
    	addLabel(new GuiNpcLabel(5,"display.livingAnimation", guiLeft + 5, y + 5));
    	this.addButton(new GuiNpcButton(5, guiLeft + 120, y, 50, 20, new String[]{"gui.yes","gui.no"},display.disableLivingAnimation?1:0));

    	y+=23;
    	addLabel(new GuiNpcLabel(7,"display.visible", guiLeft + 5, y + 5));
    	this.addButton(new GuiNpcButton(7, guiLeft + 120, y, 50, 20, new String[]{"gui.yes","gui.no","gui.partly"}, display.visible));

    	y+=23;
    	addLabel(new GuiNpcLabel(10,"display.bossbar", guiLeft + 5, y + 5));
    	this.addButton(new GuiNpcButton(10, guiLeft + 120, y, 110, 20, new String[]{"display.hide","display.show","display.showAttacking"}, display.showBossBar));
    
    	//addExtra(new GuiHoverText(0, "testing", guiLeft, guiTop));
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
			if(display.skinType == 2)
				display.url = textfield.getText();
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
			npc.textureCloakLocation = null;
			display.cloakTexture = textfield.getText();
		}
		else if(textfield.id == 9){
			npc.textureGlowLocation = null;
			display.glowTexture = textfield.getText();
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
			display.url = "";
			display.playerProfile = null;
			initGui();
		}
		else if(button.id == 3){
			NoppesUtil.openGUI(player, new GuiNPCTextures(npc, this));
		}
		else if(button.id == 5){
			display.disableLivingAnimation = button.getValue() == 1;
		}
		else if(button.id == 7){
			display.visible = button.getValue();
		}
		else if(button.id == 8){
			NoppesUtil.openGUI(player, new GuiNpcTextureCloaks(npc, this));
		}
		else if(button.id == 9){
			NoppesUtil.openGUI(player, new GuiNpcTextureOverlays(npc, this));
		}
		else if(button.id == 10){
			display.showBossBar = (byte)button.getValue();
		}
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
