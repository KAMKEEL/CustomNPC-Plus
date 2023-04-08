package noppes.npcs.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.client.Client;
import noppes.npcs.client.controllers.ClientCloneController;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumPacketServer;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;

public class GuiNpcMobSpawnerAdd extends GuiNPCInterface implements GuiYesNoCallback, IGuiData, ISubGuiListener{
	
	private Entity toClone;
	private boolean isNPC;
	private NBTTagCompound compound;
	public HashSet<UUID> npcTags = new HashSet<UUID>();

	public static ArrayList<String> allTags = new ArrayList<>();
	private static boolean serverSide = false;
	private static int tab = 1;

	public GuiNpcMobSpawnerAdd(NBTTagCompound compound){
		this.toClone = EntityList.createEntityFromNBT(compound, Minecraft.getMinecraft().theWorld);
		this.compound = compound;

		// Get Tag UUIDs
		this.getTagUUIDs();
		if(isNPC){
			Client.sendData(EnumPacketServer.TagsGet);
		}

		setBackground("menubg.png");
		xSize = 256;
		ySize = 216;
		closeOnEsc = true;
	}

	@Override
	public void initGui(){
		super.initGui();
		String name = toClone.getCommandSenderName();
		addLabel(new GuiNpcLabel(0, "Save as", guiLeft + 4, guiTop + 6));
		addTextField(new GuiNpcTextField(0, this, fontRendererObj, guiLeft + 4, guiTop + 18, 200, 20, name));
		

		addLabel(new GuiNpcLabel(1, "Tab", guiLeft + 10, guiTop + 50));
		addButton(new GuiButtonBiDirectional(2, guiLeft + 40, guiTop + 45, 60, 20, new String[]{"1","2","3","4","5","6","7","8","9","10","11","12","13","14","15"}, tab - 1));

		addButton(new GuiNpcButton(3, guiLeft + 4, guiTop + 95, new String[]{"Client side", "Server side"}, serverSide?1:0));
		addLabel(new GuiNpcLabel(1, "Tab", guiLeft + 10, guiTop + 50));

		addButton(new GuiNpcButton(0, guiLeft + 4, guiTop + 70, 80, 20, "gui.save"));
		addButton(new GuiNpcButton(1, guiLeft + 86, guiTop + 70, 80, 20, "gui.cancel"));

		if(isNPC){
			addButton(new GuiNpcButton(4, guiLeft + 4, guiTop + 120, 99, 20, "cloner.wandTags"));
			addButton(new GuiNpcButton(5, guiLeft + 106, guiTop + 120, 99, 20, "cloner.npcTags"));
		}
	}

	public void getTagUUIDs(){
		// Is an NPC
		if(compound.hasKey("ModRev")){
			this.isNPC = true;
			if(compound.hasKey("TagUUIDs")){
				NBTTagList nbtTagList = compound.getTagList("TagUUIDs",8);
				for (int i = 0; i < nbtTagList.tagCount(); i++) {
					npcTags.add(UUID.fromString(nbtTagList.getStringTagAt(i)));
				}
			}
		}
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
		if(compound.hasKey("TagNames")){
			NBTTagList tagList = compound.getTagList("TagNames",8);
			allTags.clear();
			for (int i = 0; i < tagList.tagCount(); i++) {
				allTags.add(tagList.getStringTagAt(i));
			}
		}
	}

	@Override
	public void subGuiClosed(SubGuiInterface subgui) {
		SubGuiNpcQuickTags filterGui = (SubGuiNpcQuickTags) subgui;
		// filter = filterGui.filterScroll.getSelectedList();
		initGui();
	}
}
