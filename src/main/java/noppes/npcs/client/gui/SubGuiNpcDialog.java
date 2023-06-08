package noppes.npcs.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.client.Client;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.global.GuiNPCManageDialogs;
import noppes.npcs.client.gui.global.GuiNPCQuestSelection;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.controllers.data.Dialog;

public class SubGuiNpcDialog extends SubGuiInterface implements ISubGuiListener, GuiSelectionListener,ITextfieldListener
{

	public int dialogCategoryID;
	public Dialog dialog;
	private final GuiNPCManageDialogs parent;

	public SubGuiNpcDialog(GuiNPCManageDialogs parent, Dialog dialog, int catId)
	{
		this.parent = parent;
		this.dialog = dialog;
		this.dialogCategoryID = catId;
		setBackground("menubg.png");
		xSize = 360;
		ySize = 216;
	}

    public void initGui()
    {
        super.initGui();

		addLabel(new GuiNpcLabel(1,"gui.title", guiLeft + 4, guiTop + 8));
		addTextField(new GuiNpcTextField(1, this, this.fontRendererObj, guiLeft + 36, guiTop + 3, 140, 20, dialog.title));

		addLabel(new GuiNpcLabel(0,"ID", guiLeft + 178, guiTop + 4));
		addLabel(new GuiNpcLabel(2,	dialog.id + "", guiLeft + 178, guiTop + 14));


		addLabel(new GuiNpcLabel(3, "dialog.dialogtext", guiLeft + 4, guiTop + 30));
		addButton(new GuiNpcButton(3, guiLeft + 120, guiTop + 25, 50, 20, "selectServer.edit"));

		addLabel(new GuiNpcLabel(4, "availability.options", guiLeft + 4, guiTop + 51));
		addButton(new GuiNpcButton(4, guiLeft + 120, guiTop + 46, 50, 20, "selectServer.edit"));

		addLabel(new GuiNpcLabel(5, "faction.options", guiLeft + 4, guiTop + 72));
		addButton(new GuiNpcButton(5, guiLeft + 120, guiTop + 67, 50, 20, "selectServer.edit"));

		addLabel(new GuiNpcLabel(6, "dialog.options", guiLeft + 4, guiTop + 93));
		addButton(new GuiNpcButton(6, guiLeft + 120, guiTop + 89, 50, 20, "selectServer.edit"));

		addLabel(new GuiNpcLabel(11, "dialog.visualOption", guiLeft + 4, guiTop + 115));
		addButton(new GuiNpcButton(11, guiLeft + 120, guiTop + 110, 50, 20, "selectServer.edit"));

		addButton(new GuiNpcButton(7, guiLeft + 4, guiTop + 134, 144, 20, "availability.selectquest"));
		addButton(new GuiNpcButton(8, guiLeft + 150, guiTop + 134, 20, 20, "X"));

		addLabel(new GuiNpcLabel(9, "gui.selectSound", guiLeft + 4, guiTop + 158));
		addTextField(new GuiNpcTextField(2, this, fontRendererObj, guiLeft + 4, guiTop + 168, 144, 20, dialog.sound));
		addButton(new GuiNpcButton(9, guiLeft + 150, guiTop + 168, 60, 20, "mco.template.button.select"));

		addButton(new GuiNpcButton(10, guiLeft + 4, guiTop + 192, 120, 20, "gui.showmore"));

		addButton(new GuiNpcButton(14, guiLeft + 130, guiTop + 192, 50, 20, "gui.done"));

		if(!parent.dialogQuestName.equals(""))
			getButton(7).setDisplayText(parent.dialogQuestName);
	}
	
	public void buttonEvent(GuiButton guibutton)
    {
		int id = guibutton.id;
		if(id == 3 && dialog.id >= 0){
			setSubGui(new SubGuiNpcTextArea(dialog.text));
		}
		if(id == 4 && dialog.id >= 0){
			setSubGui(new SubGuiNpcAvailability(dialog.availability));
		}
		if(id == 5 && dialog.id >= 0){
			setSubGui(new SubGuiNpcFactionOptions(dialog.factionOptions));
		}
		if(id == 6 && dialog.id >= 0){
			setSubGui(new SubGuiNpcDialogOptions(dialog));
		}
		if(id == 7 && dialog.id >= 0){
			this.setSubGui(new GuiNPCQuestSelection(this, dialog.quest));
		}
		if(id == 8 && dialog.id >= 0){
			dialog.quest = -1;
			initGui();
		}
		if(id == 9 && dialog.id >= 0){
			NoppesUtil.openGUI(player, new GuiNpcSoundSelection(npc, this, getTextField(2).getText()));
		}
		if(id == 10){
			setSubGui(new SubGuiNpcDialogExtra(dialog));
		}
		if(id == 11){
			setSubGui(new SubGuiNpcDialogVisual(dialog));
		}
		if(id == 14){
			close();
		}
	}


	@Override
	public void unFocused(GuiNpcTextField guiNpcTextField) {
		if(guiNpcTextField.id == 1) {
			if(dialog.id < 0)
				guiNpcTextField.setText("");
			else{
				String name = guiNpcTextField.getText();
				if(name.isEmpty() || this.parent.dialogData.containsKey(name)){
					guiNpcTextField.setText(dialog.title);
				}
				else if(dialog.id >= 0){
					String old = dialog.title;
					this.parent.dialogData.remove(old);
					dialog.title = name;
					this.parent.dialogData.put(dialog.title, dialog.id);
					this.parent.dialogScroll.replace(old,dialog.title);
				}
			}
		}
		if(guiNpcTextField.id == 2) {
			dialog.sound = guiNpcTextField.getText();
		}
	}

	@Override
	public void subGuiClosed(SubGuiInterface subgui){
		if(subgui instanceof SubGuiNpcTextArea){
			SubGuiNpcTextArea gui = (SubGuiNpcTextArea) subgui;
			dialog.text = gui.text;
		}
	}

	@Override
	public void selected(int ob, String name) {
		dialog.quest = ob;
		parent.dialogQuestName = name;
		initGui();
		Client.sendData(EnumPacketServer.DialogSave, this.dialogCategoryID, dialog.writeToNBT(new NBTTagCompound()));
	}

	public void save(){
		Client.sendData(EnumPacketServer.DialogSave, this.dialogCategoryID, dialog.writeToNBT(new NBTTagCompound()));
	}
}
