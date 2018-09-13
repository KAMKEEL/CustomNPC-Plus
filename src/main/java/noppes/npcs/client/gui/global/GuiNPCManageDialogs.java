package noppes.npcs.client.gui.global;

import java.util.HashMap;
import java.util.Vector;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.client.Client;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.GuiNpcSoundSelection;
import noppes.npcs.client.gui.SubGuiMailmanSendSetup;
import noppes.npcs.client.gui.SubGuiNpcAvailability;
import noppes.npcs.client.gui.SubGuiNpcCommand;
import noppes.npcs.client.gui.SubGuiNpcDialogExtra;
import noppes.npcs.client.gui.SubGuiNpcDialogOption;
import noppes.npcs.client.gui.SubGuiNpcDialogOptions;
import noppes.npcs.client.gui.SubGuiNpcFactionOptions;
import noppes.npcs.client.gui.SubGuiNpcTextArea;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.GuiNPCInterface2;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.GuiSelectionListener;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.client.gui.util.IScrollData;
import noppes.npcs.client.gui.util.ISubGuiListener;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.controllers.Dialog;
import noppes.npcs.controllers.DialogCategory;
import noppes.npcs.controllers.PlayerMail;
import noppes.npcs.entity.EntityNPCInterface;

public class GuiNPCManageDialogs extends GuiNPCInterface2 implements IScrollData, ISubGuiListener, GuiSelectionListener,ICustomScrollListener,ITextfieldListener, IGuiData
{
	private GuiCustomScroll scroll;
	private HashMap<String,Integer> data = new HashMap<String,Integer>();
	private Dialog dialog = new Dialog();
	private DialogCategory category = new DialogCategory();
	private boolean categorySelection = true;

	private GuiNpcSoundSelection gui;
	
    public GuiNPCManageDialogs(EntityNPCInterface npc)
    {
    	super(npc);
    	Client.sendData(EnumPacketServer.DialogCategoriesGet);
    }

    public void initGui()
    {
        super.initGui();
       	this.addButton(new GuiNpcButton(0,guiLeft + 358, guiTop + 8, 58, 20,categorySelection?"dialog.dialogs":"gui.categories"));
        
       	this.addButton(new GuiNpcButton(1,guiLeft + 358, guiTop + 38, 58, 20, "gui.add"));
    	this.addButton(new GuiNpcButton(2,guiLeft + 358, guiTop + 61, 58, 20, "gui.remove"));
    	
        if(scroll == null){
	        scroll = new GuiCustomScroll(this,0);
	        scroll.setSize(143, 208);
        }
        scroll.guiLeft = guiLeft + 214;
        scroll.guiTop = guiTop + 4;
        this.addScroll(scroll);
        
        if(categorySelection && category.id >= 0)
        	categoryGuiInit();
        if(!categorySelection && dialog.id >= 0)
        	dialogGuiInit();
    }

	private void dialogGuiInit() {
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

		addButton(new GuiNpcButton(7, guiLeft + 4, guiTop + 114, 144, 20, "availability.selectquest"));
		addButton(new GuiNpcButton(8, guiLeft + 150, guiTop + 114, 20, 20, "X"));

		addLabel(new GuiNpcLabel(9, "gui.selectSound", guiLeft + 4, guiTop + 138));
		addTextField(new GuiNpcTextField(2, this, fontRendererObj, guiLeft + 4, guiTop + 148, 144, 20, dialog.sound));
		addButton(new GuiNpcButton(9, guiLeft + 150, guiTop + 148, 60, 20, "mco.template.button.select"));

		addButton(new GuiNpcButton(10, guiLeft + 4, guiTop + 172, 120, 20, "gui.showmore"));
	}

	private void categoryGuiInit() {
        addTextField(new GuiNpcTextField(0,this, this.fontRendererObj, guiLeft+8, guiTop + 8, 160, 16, category.title));
	}

	@Override
	public void elementClicked() {
		getTextField(2).setText(gui.getSelected());
		unFocused(getTextField(2));
	}
	
	public void buttonEvent(GuiButton guibutton)
    {
		int id = guibutton.id;
        if(id == 0){
        	save();
        	if(categorySelection){
        		if(category.id < 0)
        			return;
            	dialog = new Dialog();
        		Client.sendData(EnumPacketServer.DialogsGet, category.id);
        	}
        	else if(!categorySelection){
            	dialog = new Dialog();
            	category = new DialogCategory();
        		Client.sendData(EnumPacketServer.DialogCategoriesGet);
        	}
        	categorySelection = !categorySelection;
        	getButton(0).setEnabled(false);
    		scroll.clear();
    		data.clear();
        }
        if(id == 1){
        	save();
        	String name = "New";
        	while(data.containsKey(name))
        		name += "_";
        	if(categorySelection){
        		DialogCategory category = new DialogCategory();
        		category.title = name;
        		Client.sendData(EnumPacketServer.DialogCategorySave, category.writeNBT(new NBTTagCompound()));
        	}
        	else{
        		Dialog dialog = new Dialog();
        		dialog.title = name;
        		Client.sendData(EnumPacketServer.DialogSave, category.id, dialog.writeToNBT(new NBTTagCompound()));
        	}
        }
        if(id == 2){
        	if(data.containsKey(scroll.getSelected())) {
				if(categorySelection){
					Client.sendData(EnumPacketServer.DialogCategoryRemove, category.id);
					category = new DialogCategory();
				}
				else{
					Client.sendData(EnumPacketServer.DialogRemove, dialog.id);
					dialog = new Dialog();
				}
        		scroll.clear();
        	}
        }
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
			NoppesUtil.openGUI(player, new GuiNPCQuestSelection(npc, this, dialog.quest));
        }
        if(id == 8 && dialog.id >= 0){
        	dialog.quest = -1;
        	initGui();
        }
        if(id == 9 && dialog.id >= 0){
        	NoppesUtil.openGUI(player, gui = new GuiNpcSoundSelection(npc, this, getTextField(2).getText()));
        }
        if(id == 10){
        	setSubGui(new SubGuiNpcDialogExtra(dialog, this));
        }
    }
	
	@Override
	public void unFocused(GuiNpcTextField guiNpcTextField) {
		if(guiNpcTextField.id == 0) {
			if(category.id < 0)
				guiNpcTextField.setText("");
			else{
				String name = guiNpcTextField.getText();
				if(name.isEmpty() || data.containsKey(name)){
					guiNpcTextField.setText(category.title);
				}
				else if(categorySelection && category.id >= 0){
					String old = category.title;
					data.remove(category.title);
					category.title = name;
					data.put(category.title, category.id);
					scroll.replace(old,category.title);
				}
			}
		}
		if(guiNpcTextField.id == 1) {
			if(dialog.id < 0)
				guiNpcTextField.setText("");
			else{
				String name = guiNpcTextField.getText();
				if(name.isEmpty() || data.containsKey(name)){
					guiNpcTextField.setText(dialog.title);
				}
				else if(!categorySelection && dialog.id >= 0){
					String old = dialog.title;
					data.remove(old);
					dialog.title = name;
					data.put(dialog.title, dialog.id);
					scroll.replace(old,dialog.title);
				}
			}
		}
		if(guiNpcTextField.id == 2) {
			dialog.sound = guiNpcTextField.getText();
		}
		
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		if(categorySelection){
			category.readNBT(compound);
			setSelected(category.title);
			initGui();
		}
		else{
			dialog.readNBT(compound);
			setSelected(dialog.title);
			initGui();
			if(compound.hasKey("DialogQuestName"))
				getButton(7).setDisplayText(compound.getString("DialogQuestName"));
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
	public void setData(Vector<String> list, HashMap<String, Integer> data) {
		getButton(0).setEnabled(true);
		String name = scroll.getSelected();
		this.data = data;
		scroll.setList(list);
		
		if(name != null)
			scroll.setSelected(name);
		initGui();
	}
    
	@Override
	public void setSelected(String selected) {
		
	}

	@Override
	public void selected(int ob, String name) {
		dialog.quest = ob;
		Client.sendData(EnumPacketServer.DialogSave, category.id, dialog.writeToNBT(new NBTTagCompound()));
		Client.sendData(EnumPacketServer.DialogGet, dialog.id);
	}
	@Override
	public void customScrollClicked(int i, int j, int k, GuiCustomScroll guiCustomScroll) {
		if(guiCustomScroll.id == 0)
		{
			save();
			String selected = scroll.getSelected();
			if(categorySelection){
				category = new DialogCategory();
				Client.sendData(EnumPacketServer.DialogCategoryGet, data.get(selected));
			}
			else{
				dialog = new Dialog();
				Client.sendData(EnumPacketServer.DialogGet, data.get(selected));
			}
			
		}
	}
	
	public void save() {
    	GuiNpcTextField.unfocus();
		if(!categorySelection && dialog.id >= 0)
			Client.sendData(EnumPacketServer.DialogSave, category.id, dialog.writeToNBT(new NBTTagCompound()));
		else if(categorySelection && category.id >= 0)
			Client.sendData(EnumPacketServer.DialogCategorySave, category.writeNBT(new NBTTagCompound()));
	}

}
