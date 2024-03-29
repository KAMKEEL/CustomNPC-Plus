package noppes.npcs.client.gui.global;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
import noppes.npcs.client.Client;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.SubGuiEditText;
import noppes.npcs.client.gui.SubGuiNpcDialog;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.controllers.data.Dialog;
import noppes.npcs.controllers.data.DialogCategory;
import noppes.npcs.entity.EntityNPCInterface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class GuiNPCManageDialogs extends GuiNPCInterface2 implements IScrollGroup, IScrollData, ISubGuiListener,ICustomScrollListener, IGuiData, GuiYesNoCallback
{
	private GuiCustomScroll catScroll;
	public GuiCustomScroll dialogScroll;

	private String prevCatName = "";
	private String prevDialogName = "";

	public DialogCategory category = new DialogCategory();
	public Dialog dialog = new Dialog();
	public String dialogQuestName = "";

	private HashMap<String,Integer> catData = new HashMap<String,Integer>();
	public HashMap<String,Integer> dialogData = new HashMap<String,Integer>();

	private String catSearch = "";
	private String diagSearch = "";

    public GuiNPCManageDialogs(EntityNPCInterface npc)
    {
    	super(npc);
    	Client.sendData(EnumPacketServer.DialogCategoriesGet);
    }

    public void initGui()
    {
        super.initGui();

		if(catScroll == null){
			catScroll = new GuiCustomScroll(this,0, 0);
			catScroll.setSize(143, 185);
		}
		catScroll.guiLeft = guiLeft + 64;
		catScroll.guiTop = guiTop + 4;
		this.addScroll(catScroll);

		if(dialogScroll == null){
			dialogScroll = new GuiCustomScroll(this,1, 0);
			dialogScroll.setSize(143, 185);
		}
		dialogScroll.guiLeft = guiLeft + 212;
		dialogScroll.guiTop = guiTop + 4;
		this.addScroll(dialogScroll);

		addTextField(new GuiNpcTextField(55, this, fontRendererObj, guiLeft + 64, guiTop + 4 + 3 + 185, 143, 20, catSearch));

		this.addButton(new GuiNpcButton(44,guiLeft + 3, guiTop + 8, 58, 20, "gui.categories"));
		getButton(44).setEnabled(false);
		this.addButton(new GuiNpcButton(4,guiLeft + 3, guiTop + 38, 58, 20, "gui.add"));
		this.addButton(new GuiNpcButton(5,guiLeft + 3, guiTop + 61, 58, 20, "gui.remove"));
		this.addButton(new GuiNpcButton(6,guiLeft + 3, guiTop + 94, 58, 20, "gui.edit"));

		addTextField(new GuiNpcTextField(66, this, fontRendererObj, guiLeft + 212, guiTop + 4 + 3 + 185, 143, 20, diagSearch));

		this.addButton(new GuiNpcButton(33,guiLeft + 358, guiTop + 8, 58, 20, "dialog.dialogs"));
		getButton(33).setEnabled(false);
		this.addButton(new GuiNpcButton(0,guiLeft + 358, guiTop + 94, 58, 20, "gui.edit"));
		this.addButton(new GuiNpcButton(1,guiLeft + 358, guiTop + 38, 58, 20, "gui.add"));
		this.addButton(new GuiNpcButton(2,guiLeft + 358, guiTop + 61, 58, 20, "gui.remove"));
		this.addButton(new GuiNpcButton(3,guiLeft + 358, guiTop + 117, 58, 20, "gui.copy"));

		if(dialog != null) {
			if(dialog.id != -1){
				addLabel(new GuiNpcLabel(0, "ID", guiLeft + 358, guiTop + 4 + 3 + 185));
				addLabel(new GuiNpcLabel(1, dialog.id + "", guiLeft + 358, guiTop + 4 + 3 + 195));
			}
		}

		updateButtons();
	}

	@Override
	public void keyTyped(char c, int i)
	{
		super.keyTyped(c, i);
		if(getTextField(55) != null){
			if(getTextField(55).isFocused()){
				if(catSearch.equals(getTextField(55).getText()))
					return;
				catSearch = getTextField(55).getText().toLowerCase();
				catScroll.setList(getCatSearch());
                catScroll.resetScroll();
			}
		}
		if(getTextField(66) != null){
			if(getTextField(66).isFocused()){
				if(diagSearch.equals(getTextField(66).getText()))
					return;
				diagSearch = getTextField(66).getText().toLowerCase();
				dialogScroll.setList(getDiagSearch());
                dialogScroll.resetScroll();
			}
		}
	}

	public void resetDiagList(){
		if(dialogScroll != null){
			diagSearch = "";
			if(getTextField(66) != null){
				getTextField(66).setText("");
			}
			dialogScroll.setList(getDiagSearch());
		}
	}

	private List<String> getCatSearch(){
		if(catSearch.isEmpty()){
			return new ArrayList<String>(this.catData.keySet());
		}
		List<String> list = new ArrayList<String>();
		for(String name : this.catData.keySet()){
			if(name.toLowerCase().contains(catSearch))
				list.add(name);
		}
		return list;
	}

	private List<String> getDiagSearch(){
		if(category != null){
			if(category.id < 0){
				return new ArrayList<String>();
			}
		}
		else {
			return new ArrayList<String>();
		}

		if(diagSearch.isEmpty()){
			return new ArrayList<String>(this.dialogData.keySet());
		}
		List<String> list = new ArrayList<String>();
		for(String name : this.dialogData.keySet()){
			if(name.toLowerCase().contains(diagSearch))
				list.add(name);
		}
		return list;
	}

	public void buttonEvent(GuiButton guibutton)
    {
		int id = guibutton.id;
		// Edit Cat
        if(id == 6){
			if(category != null && category.id > -1){
				setSubGui(new SubGuiEditText(category.title));
			}
			else {
				getCategory(false);
			}
        }
		// Add Cat
		if(id == 4){
			String name = "New";
			while(catData.containsKey(name))
				name += "_";

			if(catScroll != null){
				setPrevCatName(name);
			}
			DialogCategory category = new DialogCategory();
			category.title = name;
			Client.sendData(EnumPacketServer.DialogCategorySave, category.writeNBT(new NBTTagCompound()));
		}
		// Remove Cat
		if(id == 5){
        	if(catData.containsKey(catScroll.getSelected())) {
                GuiYesNo guiyesno = new GuiYesNo(this, catScroll.getSelected(), StatCollector.translateToLocal("gui.delete"), 5);
                displayGuiScreen(guiyesno);
        	}
		}
		if(category != null && category.id >= 0){
			// Add Dialog
			if(id == 1){
				String name = "New";
				while(dialogData.containsKey(name))
					name += "_";

				if(dialogScroll != null){
					setPrevDialogName(name);
				}
				Dialog dialog = new Dialog();
				dialog.title = name;
				Client.sendData(EnumPacketServer.DialogSave, category.id, dialog.writeToNBT(new NBTTagCompound()), true);
			}
			// Remove Dialog
			if(id == 2) {
				if (dialogData.containsKey(dialogScroll.getSelected())) {
                    GuiYesNo guiyesno = new GuiYesNo(this, dialogScroll.getSelected(), StatCollector.translateToLocal("gui.delete"), 2);
                    displayGuiScreen(guiyesno);
				}
			}
			// Edit Dialog
			if(id == 0) {
				if (dialogData.containsKey(dialogScroll.getSelected()) && dialog != null && dialog.id >= 0) {
					setSubGui(new SubGuiNpcDialog(this, dialog, category.id));
				}
			}
			// Clone Dialog
			if(id == 3) {
				if (dialogData.containsKey(dialogScroll.getSelected()) && dialog != null && dialog.id >= 0) {
					String name = dialog.title;
					while(dialogData.containsKey(name))
						name += "_";

					if(dialogScroll != null){
						setPrevDialogName(name);
					}

					Dialog dialog = new Dialog();
					dialog.readNBTPartial(this.dialog.writeToNBT(new NBTTagCompound()));
					dialog.title = name;
					Client.sendData(EnumPacketServer.DialogSave, category.id, dialog.writeToNBT(new NBTTagCompound()), true);
				}
			}
        }
		updateButtons();
    }

	public void updateButtons(){
		boolean enabled = category != null;
		if(enabled){
			if(!(category.id >= 0)){
				enabled = false;
			}
		}

		boolean diagEnabled = dialogData != null;
		if(diagEnabled){
			if(dialog == null ||!(dialog.id >= 0)){
				diagEnabled = false;
			}
		}
		getButton(6).setEnabled(enabled);

		getButton(1).setEnabled(enabled);
		getButton(2).setEnabled(enabled);

		getButton(0).setEnabled(enabled && diagEnabled);
		getButton(3).setEnabled(enabled && diagEnabled);
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		if(compound.hasKey("DialogTitle")){
			dialog.readNBT(compound);
			if(compound.hasKey("DialogQuestName")){
				dialogQuestName = compound.getString("DialogQuestName");
			}
			else {
				dialogQuestName = "";
			}
			setPrevDialogName(dialog.title);
		}
		else {
			category.readNBT(compound);
			setPrevCatName(category.title);
			Client.sendData(EnumPacketServer.DialogsGet, category.id, true);
			resetDiagList();
		}
		initGui();
	}

	@Override
	public void subGuiClosed(SubGuiInterface subgui){
		if(subgui instanceof SubGuiEditText){
			if(!((SubGuiEditText)subgui).cancelled){
				if(category != null && category.id > -1){
					String name = ((SubGuiEditText)subgui).text;
					if(name != null && !name.equalsIgnoreCase(category.title)){
						if(!(name.isEmpty() || catData.containsKey(name))){
							String old = category.title;
							catData.remove(category.title);
							category.title = name;
							catData.put(category.title, category.id);
							catScroll.replace(old,category.title);
						}
						saveType(false);
					}
				}
			}
			clearCategory();
		}
		if(subgui instanceof SubGuiNpcDialog){
			saveType(true);
		}
	}

	public void setPrevCatName(String selectedCat){
		prevCatName = selectedCat;
		this.catScroll.setSelected(prevCatName);
	}

	public void setPrevDialogName(String selectedCat){
		prevDialogName = selectedCat;
		this.dialogScroll.setSelected(prevDialogName);
	}

	@Override
	public void customScrollClicked(int i, int j, int k, GuiCustomScroll guiCustomScroll) {
		if(guiCustomScroll.id == 0)
		{
			getCategory(false);
		}
		if(guiCustomScroll.id == 1)
		{
			getDialog(false);
		}
	}

	public void getCategory(boolean override){
		if(catScroll.selected != -1){
			String selected = catScroll.getSelected();
			if(!selected.equals(prevCatName) || override){
				category = new DialogCategory();

                dialogScroll.selected = -1;
                dialogScroll.resetScroll();
                diagSearch = "";
                dialog = null;
                getTextField(66).setText("");

				Client.sendData(EnumPacketServer.DialogCategoryGet, catData.get(selected));
				setPrevCatName(selected);
			}
		}
	}

	public void getDialog(boolean override){
		if(dialogScroll.selected != -1){
			String selected = dialogScroll.getSelected();
			if(!selected.equals(prevDialogName) || override){
				dialog = new Dialog();
				Client.sendData(EnumPacketServer.DialogGet, dialogData.get(selected));
				setPrevDialogName(selected);
			}
		}
	}

	public void clearCategory(){
		catScroll.setList(getCatSearch());
		catScroll.selected = -1;
		prevCatName = "";
		category = new DialogCategory();
		this.dialogData.clear();
		resetDiagList();
	}

	public void saveType(boolean saveDiag){
		if(saveDiag){
			if(dialogScroll.selected != -1 && dialog.id >= 0){
				if(catScroll.selected != -1 && category.id >= 0){
					Client.sendData(EnumPacketServer.DialogSave, category.id, dialog.writeToNBT(new NBTTagCompound()), true);
				}
			}
		}
		else{
			if(catScroll.selected != -1 && category.id >= 0)
				Client.sendData(EnumPacketServer.DialogCategorySave, category.writeNBT(new NBTTagCompound()));
		}
	}

	public void save() {}

	@Override
	public void setData(Vector<String> list, HashMap<String, Integer> data) {
		String name = catScroll.getSelected();
		this.catData = data;
		catScroll.setList(getCatSearch());
		if(name != null){
			catScroll.setSelected(name);
			getCategory(false);
		} else {
			catScroll.setSelected(prevCatName);
			getCategory(true);
		}
		initGui();
	}

	@Override
	public void setSelected(String selected) {}

	@Override
	public void setScrollGroup(Vector<String> list, HashMap<String, Integer> data) {
		String name = dialogScroll.getSelected();
		this.dialogData = data;
		dialogScroll.setList(getDiagSearch());
		if(name != null){
			dialogScroll.setSelected(name);
			getDialog(false);
		} else {
			dialogScroll.setSelected(prevDialogName);
			getDialog(true);
		}
		initGui();
	}

	@Override
	public void setSelectedGroup(String selected) {}

    @Override
    public void confirmClicked(boolean result, int id) {
        NoppesUtil.openGUI(player, this);
        if(!result)
            return;
        if(id == 5) {
            if(catData.containsKey(catScroll.getSelected())) {
                Client.sendData(EnumPacketServer.DialogCategoryRemove, category.id);
                clearCategory();
            }
        }
        if(id == 2) {
            if (dialogData.containsKey(dialogScroll.getSelected())) {
                Client.sendData(EnumPacketServer.DialogRemove, dialog.id, true);
                dialog = new Dialog();
                dialogData.clear();
            }
        }
        updateButtons();
    }
}
