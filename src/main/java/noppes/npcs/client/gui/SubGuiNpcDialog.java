package noppes.npcs.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.client.Client;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.global.GuiNPCManageDialogs;
import noppes.npcs.client.gui.global.GuiNPCQuestSelection;
import noppes.npcs.client.gui.global.SubGuiNPCQuestSelection;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.controllers.data.Dialog;
import noppes.npcs.controllers.data.PlayerMail;

public class SubGuiNpcDialog extends SubGuiInterface implements ISubGuiListener, GuiSelectionListener,ITextfieldListener
{

	public int dialogCategoryID;
	public Dialog dialog;
	private final GuiNPCManageDialogs parent;
	private SubGuiNpcSoundSelection gui;
	private GuiNPCQuestSelection questSelection;

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
		addTextField(new GuiNpcTextField(1, this, this.fontRendererObj, guiLeft + 36, guiTop + 3, 200, 20, dialog.title));

		addLabel(new GuiNpcLabel(0,"ID", guiLeft + 238, guiTop + 4));
		addLabel(new GuiNpcLabel(2,	dialog.id + "", guiLeft + 238, guiTop + 14));

		addLabel(new GuiNpcLabel(3, "dialog.dialogtext", guiLeft + 7, guiTop + 33));
		addButton(new GuiNpcButton(3, guiLeft + 120, guiTop + 28, 50, 20, "selectServer.edit"));

		addLabel(new GuiNpcLabel(6, "dialog.options", guiLeft + 7, guiTop + 57));
		addButton(new GuiNpcButton(6, guiLeft + 120, guiTop + 52, 50, 20, "selectServer.edit"));

		addLabel(new GuiNpcLabel(11, "dialog.visualOption", guiLeft + 7, guiTop + 81));
		addButton(new GuiNpcButton(11, guiLeft + 120, guiTop + 76, 50, 20, "selectServer.edit"));

		addLabel(new GuiNpcLabel(9, "gui.selectSound", guiLeft + 7, guiTop + 105));
		addTextField(new GuiNpcTextField(2, this, fontRendererObj, guiLeft + 90, guiTop + 100, 194, 20, dialog.sound));
		addButton(new GuiNpcButton(9, guiLeft + 293, guiTop + 100, 60, 20, "mco.template.button.select"));

		addLabel(new GuiNpcLabel(4, "availability.options", guiLeft + 180, guiTop + 33));
		addButton(new GuiNpcButton(4, guiLeft + 303, guiTop + 28, 50, 20, "selectServer.edit"));

		addLabel(new GuiNpcLabel(5, "faction.options", guiLeft + 180, guiTop + 57));
		addButton(new GuiNpcButton(5, guiLeft + 303, guiTop + 52, 50, 20, "selectServer.edit"));

		addButton(new GuiNpcButton(7, guiLeft + 4, guiTop + 130, 144, 20, "availability.selectquest"));
		addButton(new GuiNpcButton(8, guiLeft + 150, guiTop + 130, 20, 20, "X"));

		addButton(new GuiNpcButton(13, guiLeft + 4, guiTop + 154, 144, 20, "mailbox.setup"));
		addButton(new GuiNpcButton(14, guiLeft + 150, guiTop + 154, 20, 20, "X"));

		addButton(new GuiNpcButton(10, guiLeft + 303, guiTop + 130, 50, 20, "selectServer.edit"));
		addLabel(new GuiNpcLabel(10, "advMode.command", guiLeft + 180, guiTop + 130 + 5));

		addButton(new GuiNpcButtonYesNo(15, guiLeft + 303, guiTop + 154, dialog.disableEsc));
		addLabel(new GuiNpcLabel(15, "dialog.disableEsc", guiLeft + 180, guiTop + 154 + 5));

		addButton(new GuiNpcButton(16, guiLeft + 303, guiTop + 192, 50, 20, "gui.done"));

		if(!parent.dialogQuestName.equals(""))
			getButton(7).setDisplayText(parent.dialogQuestName);

		if(!dialog.mail.subject.isEmpty())
			getButton(13).setDisplayText(dialog.mail.subject);
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
			questSelection = new GuiNPCQuestSelection(npc, getParent(), dialog.quest);
			questSelection.listener = this;
			NoppesUtil.openGUI(player, questSelection);
		}
		if(id == 8 && dialog.id >= 0){
			dialog.quest = -1;
			parent.dialogQuestName = "";
			initGui();
		}
		if(id == 9 && dialog.id >= 0){
			setSubGui(gui = new SubGuiNpcSoundSelection(this, getTextField(2).getText()));
		}
		if(id == 10){
			setSubGui(new SubGuiNpcCommand(dialog.command));
		}
		if(id == 11){
			setSubGui(new SubGuiNpcDialogVisual(dialog));
		}
		if(id == 13){
			setSubGui(new SubGuiMailmanSendSetup(dialog.mail, getParent()));
		}
		if(id == 14){
			dialog.mail = new PlayerMail();
			initGui();
		}
		if(id == 15){
			if(guibutton instanceof GuiNpcButton){
				dialog.disableEsc = ((GuiNpcButton) guibutton).getValue() == 1;
			}
		}
		if(id == 16){
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
		if(subgui instanceof SubGuiNpcCommand){
			dialog.command = ((SubGuiNpcCommand) subgui).command;
		}
	}

	@Override
	public void elementClicked(){
		getTextField(2).setText(gui.getSelected());
		unFocused(getTextField(2));
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
