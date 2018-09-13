package noppes.npcs.client.gui;

import java.util.ArrayList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.client.Client;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.controllers.ClientCloneController;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.entity.EntityNPCInterface;

public class GuiNpcMobSpawnerAdd extends GuiNPCInterface implements GuiYesNoCallback, IGuiData{
	
	private Entity toClone;
	private NBTTagCompound compound;
	private static boolean serverSide = false;
	private static int tab = 1;

	public GuiNpcMobSpawnerAdd(NBTTagCompound compound){
		this.toClone = EntityList.createEntityFromNBT(compound, Minecraft.getMinecraft().theWorld);
		this.compound = compound;
		setBackground("menubg.png");
		xSize = 256;
		ySize = 216;
		
	}
	@Override
	public void initGui(){
		super.initGui();
		String name = toClone.getCommandSenderName();
		addLabel(new GuiNpcLabel(0, "Save as", guiLeft + 4, guiTop + 6));
		addTextField(new GuiNpcTextField(0, this, fontRendererObj, guiLeft + 4, guiTop + 18, 200, 20, name));
		

		addLabel(new GuiNpcLabel(1, "Tab", guiLeft + 10, guiTop + 50));
		addButton(new GuiNpcButton(2, guiLeft + 40, guiTop + 45, 20, 20, new String[]{"1","2","3","4","5","6","7","8","9"}, tab - 1));

		addButton(new GuiNpcButton(3, guiLeft + 4, guiTop + 95, new String[]{"Client side", "Server side"}, serverSide?1:0));
		
		addButton(new GuiNpcButton(0, guiLeft + 4, guiTop + 70, 80, 20, "gui.save"));
		addButton(new GuiNpcButton(1, guiLeft + 86, guiTop + 70, 80, 20, "gui.cancel"));
	}
	public void buttonEvent(GuiButton guibutton) {
		int id = guibutton.id;
		if(id == 0){
			String name = getTextField(0).getText();
			if(name.isEmpty())
				return;
			int tab = ((GuiNpcButton)guibutton).getValue() + 1;
			if(!serverSide){
				if(ClientCloneController.Instance.getCloneData(null, name, tab) != null)
					displayGuiScreen(new GuiYesNo(this, "Warning", "You are about to overwrite a clone", 1));
				else
					confirmClicked(true, 0);
			}
			else
				Client.sendData(EnumPacketServer.ClonePreSave, name, tab);
		}
		if(id == 1){
			close();
		}
		if(id == 2){
			tab = ((GuiNpcButton)guibutton).getValue() + 1;
		}
		if(id == 3){
			serverSide = ((GuiNpcButton)guibutton).getValue() == 1;
		}
	}
	
	@Override
    public void confirmClicked(boolean confirm, int id){
		if(confirm){
			String name = getTextField(0).getText();
			if(!serverSide)
				ClientCloneController.Instance.addClone(compound, name, tab);
			else
				Client.sendData(EnumPacketServer.CloneSave, name, tab);
			close();
		}
		else
			displayGuiScreen(this);
    }
	@Override
	public void save() {
		
	}
	@Override
	public void setGuiData(NBTTagCompound compound) {
		if(compound.hasKey("NameExists")){
			if(compound.getBoolean("NameExists"))
				displayGuiScreen(new GuiYesNo(this, "Warning", "You are about to overwrite a clone", 1));
			else
				confirmClicked(true, 0);
		}
	}

}
