package noppes.npcs.client.gui.global;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.*;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.*;
import noppes.npcs.controllers.data.Quest;
import noppes.npcs.controllers.data.QuestCategory;
import noppes.npcs.entity.EntityNPCInterface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class GuiNPCManageQuest extends GuiNPCInterface2 implements IScrollGroup, IScrollData, ISubGuiListener, ICustomScrollListener, IGuiData
{
	private GuiCustomScroll catScroll;
	public GuiCustomScroll questScroll;

	private String prevCatName = "";
	private String prevQuestName = "";

	public QuestCategory category = new QuestCategory();
	public static Quest quest = new Quest();
	public String nextQuestName = "";

	private HashMap<String,Integer> catData = new HashMap<String,Integer>();
	public HashMap<String,Integer> questData = new HashMap<String,Integer>();

	private String catSearch = "";
	private String questSearch = "";

	public static GuiScreen Instance;

	public GuiNPCManageQuest(EntityNPCInterface npc)
	{
		super(npc);
		Instance = this;
		quest = new Quest();
		Client.sendData(EnumPacketServer.QuestCategoriesGet);
	}

	public void initGui()
	{
		super.initGui();

		if(catScroll == null){
			catScroll = new GuiCustomScroll(this,0);
			catScroll.setSize(143, 185);
		}
		catScroll.guiLeft = guiLeft + 64;
		catScroll.guiTop = guiTop + 4;
		this.addScroll(catScroll);
		addTextField(new GuiNpcTextField(55, this, fontRendererObj, guiLeft + 64, guiTop + 4 + 3 + 185, 143, 20, catSearch));

		this.addButton(new GuiNpcButton(44,guiLeft + 3, guiTop + 8, 58, 20, "gui.categories"));
		getButton(44).setEnabled(false);
		this.addButton(new GuiNpcButton(4,guiLeft + 3, guiTop + 38, 58, 20, "gui.add"));
		this.addButton(new GuiNpcButton(5,guiLeft + 3, guiTop + 61, 58, 20, "gui.remove"));
		this.addButton(new GuiNpcButton(6,guiLeft + 3, guiTop + 94, 58, 20, "gui.edit"));


		if(questScroll == null){
			questScroll = new GuiCustomScroll(this,1);
			questScroll.setSize(143, 185);
		}
		questScroll.guiLeft = guiLeft + 212;
		questScroll.guiTop = guiTop + 4;
		this.addScroll(questScroll);
		addTextField(new GuiNpcTextField(66, this, fontRendererObj, guiLeft + 212, guiTop + 4 + 3 + 185, 143, 20, questSearch));

		this.addButton(new GuiNpcButton(33,guiLeft + 358, guiTop + 8, 58, 20, "quest.quests"));
		getButton(33).setEnabled(false);
		this.addButton(new GuiNpcButton(0,guiLeft + 358, guiTop + 94, 58, 20, "gui.edit"));
		this.addButton(new GuiNpcButton(1,guiLeft + 358, guiTop + 38, 58, 20, "gui.add"));
		this.addButton(new GuiNpcButton(2,guiLeft + 358, guiTop + 61, 58, 20, "gui.remove"));
		this.addButton(new GuiNpcButton(3,guiLeft + 358, guiTop + 117, 58, 20, "gui.copy"));

		if(quest != null) {
			if(quest.id != -1){
				addLabel(new GuiNpcLabel(0, "ID:", guiLeft + 358, guiTop + 4 + 3 + 185));
				addLabel(new GuiNpcLabel(1, quest.id + "", guiLeft + 358, guiTop + 4 + 3 + 195));
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
			}
		}
		if(getTextField(66) != null){
			if(getTextField(66).isFocused()){
				if(questSearch.equals(getTextField(66).getText()))
					return;
				questSearch = getTextField(66).getText().toLowerCase();
				questScroll.setList(getQuestSearch());
			}
		}
	}

	public void resetQuestList(){
		if(questScroll != null){
			questSearch = "";
			if(getTextField(66) != null){
				getTextField(66).setText("");
			}
			questScroll.setList(getQuestSearch());
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

	private List<String> getQuestSearch(){
		if(category != null){
			if(category.id < 0){
				return new ArrayList<String>();
			}
		}
		else {
			return new ArrayList<String>();
		}

		if(questSearch.isEmpty()){
			return new ArrayList<String>(this.questData.keySet());
		}
		List<String> list = new ArrayList<String>();
		for(String name : this.questData.keySet()){
			if(name.toLowerCase().contains(questSearch))
				list.add(name);
		}
		return list;
	}

	public void buttonEvent(GuiButton guibutton)
	{
		int id = guibutton.id;
		// Edit Cat
		if(id == 6){
			saveType(false);
			if(category != null && category.id > -1){
				setSubGui(new SubGuiEditText(category.title));
			}
			else {
				getCategory(false);
			}
		}
		// Add Cat
		if(id == 4){
			saveType(false);
			String name = "New";
			while(catData.containsKey(name))
				name += "_";

			if(catScroll != null){
				setPrevCatName(name);
			}
			QuestCategory category = new QuestCategory();
			category.title = name;
			Client.sendData(EnumPacketServer.QuestCategorySave, category.writeNBT(new NBTTagCompound()));
		}
		// Remove Cat
		if(id == 5){
			saveType(false);
			if(catData.containsKey(catScroll.getSelected())) {
				Client.sendData(EnumPacketServer.QuestCategoryRemove, category.id);
				clearCategory();
			}
		}
		if(category != null && category.id >= 0){
			// Add Quest
			if(id == 1){
				saveType(true);
				String name = "New";
				while(questData.containsKey(name))
					name += "_";

				if(questScroll != null){
					setPrevQuestName(name);
				}
				Quest quest = new Quest();
				quest.title = name;
				Client.sendData(EnumPacketServer.QuestSave, category.id, quest.writeToNBT(new NBTTagCompound()), true);
			}
			// Remove Quest
			if(id == 2) {
				saveType(true);
				if (questData.containsKey(questScroll.getSelected())) {
					Client.sendData(EnumPacketServer.QuestRemove, quest.id, true);
					quest = new Quest();
					questData.clear();
				}
			}
			// Edit Quest
			if(id == 0) {
				saveType(true);
				if (questData.containsKey(questScroll.getSelected()) && quest != null && quest.id >= 0) {
					setSubGui(new SubGuiNpcQuest(this, quest, category.id));
				}
			}
			// Clone Quest
			if(id == 3) {
				saveType(true);
				if (questData.containsKey(questScroll.getSelected()) && quest != null && quest.id >= 0) {
					String name = quest.title;
					while(questData.containsKey(name))
						name += "_";

					if(questScroll != null){
						setPrevQuestName(name);
					}

					Quest quest = new Quest();
					quest.readNBTPartial(this.quest.writeToNBT(new NBTTagCompound()));
					quest.title = name;
					Client.sendData(EnumPacketServer.QuestSave, category.id, quest.writeToNBT(new NBTTagCompound()), true);
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

		boolean questEnabled = questData != null;
		if(questEnabled){
			if(!(quest.id >= 0)){
				questEnabled = false;
			}
		}
		getButton(6).setEnabled(enabled);

		getButton(1).setEnabled(enabled);
		getButton(2).setEnabled(enabled);

		getButton(0).setEnabled(enabled && questEnabled);
		getButton(3).setEnabled(enabled && questEnabled);
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		if(compound.hasKey("NextQuestId")){
			quest.readNBT(compound);
			setPrevQuestName(quest.title);
			if(compound.hasKey("NextQuestTitle")){
				nextQuestName = compound.getString("NextQuestTitle");
			}
			else {
				nextQuestName = "";
			}
		}
		else {
			category.readNBT(compound);
			setPrevCatName(category.title);
			Client.sendData(EnumPacketServer.QuestsGet, category.id, true);
			resetQuestList();
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
		if(subgui instanceof SubGuiNpcQuest){
			saveType(true);
		}
	}

	public void setPrevCatName(String selectedCat){
		prevCatName = selectedCat;
		this.catScroll.setSelected(prevCatName);
	}

	public void setPrevQuestName(String selectedCat){
		prevQuestName = selectedCat;
		this.questScroll.setSelected(prevQuestName);
	}

	@Override
	public void customScrollClicked(int i, int j, int k, GuiCustomScroll guiCustomScroll) {
		if(guiCustomScroll.id == 0)
		{
			saveType(false);
			getCategory(false);
		}
		if(guiCustomScroll.id == 1)
		{
			saveType(false);
			getQuest(false);
		}
	}

	public void getCategory(boolean override){
		if(catScroll.selected != -1){
			String selected = catScroll.getSelected();
			if(!selected.equals(prevCatName) || override){
				category = new QuestCategory();
				Client.sendData(EnumPacketServer.QuestCategoryGet, catData.get(selected));
				setPrevCatName(selected);
			}
		}
	}

	public void getQuest(boolean override){
		if(questScroll.selected != -1){
			String selected = questScroll.getSelected();
			if(!selected.equals(prevQuestName) || override){
				quest = new Quest();
				Client.sendData(EnumPacketServer.QuestGet, questData.get(selected));
				setPrevQuestName(selected);
			}
		}
	}

	public void clearCategory(){
		catScroll.setList(getCatSearch());
		catScroll.selected = -1;
		prevCatName = "";
		category = new QuestCategory();
		this.questData.clear();
		resetQuestList();
	}

	public void saveType(boolean saveQuest){
		if(saveQuest){
			if(questScroll.selected != -1 && quest.id >= 0){
				if(catScroll.selected != -1 && category.id >= 0){
					Client.sendData(EnumPacketServer.QuestSave, category.id, quest.writeToNBT(new NBTTagCompound()), true);
				}
			}
		}
		else{
			if(catScroll.selected != -1 && category.id >= 0)
				Client.sendData(EnumPacketServer.QuestCategorySave, category.writeNBT(new NBTTagCompound()));
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
		String name = questScroll.getSelected();
		this.questData = data;
		questScroll.setList(getQuestSearch());
		if(name != null){
			questScroll.setSelected(name);
			getQuest(false);
		} else {
			questScroll.setSelected(prevQuestName);
			getQuest(true);
		}
		initGui();
	}

	@Override
	public void setSelectedGroup(String selected) {}

}
