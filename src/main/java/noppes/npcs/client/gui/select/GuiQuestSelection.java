package noppes.npcs.client.gui.select;

import com.google.common.collect.Lists;
import net.minecraft.client.gui.GuiButton;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.controllers.QuestController;
import noppes.npcs.controllers.data.Quest;
import noppes.npcs.controllers.data.QuestCategory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GuiQuestSelection extends SubGuiInterface implements ICustomScrollListener {
	private HashMap<String,QuestCategory> categoryData = new HashMap<String,QuestCategory>();
	private HashMap<String,Quest> questData = new HashMap<String,Quest>();

	private GuiCustomScroll scrollCategories;
	private GuiCustomScroll scrollQuests;

	private QuestCategory selectedCategory;
	public Quest selectedQuest;

	private GuiSelectionListener listener;
    private String catSearch = "";
    private String questSearch = "";

    public GuiQuestSelection(int quest){
    	drawDefaultBackground = false;
		title = "";
		setBackground("menubg.png");
        xSize = 366;
        ySize = 226;
    	this.selectedQuest = QuestController.Instance.quests.get(quest);
    	if(selectedQuest != null) {
    		selectedCategory = selectedQuest.category;
    	}
    }

    @Override
    public void initGui(){
        super.initGui();

		if(parent instanceof GuiSelectionListener){
			listener = (GuiSelectionListener) parent;
		}
        this.addLabel(new GuiNpcLabel(0, "gui.categories", guiLeft + 8, guiTop + 4));
        this.addLabel(new GuiNpcLabel(1, "quest.quests", guiLeft + 184, guiTop + 4));

        this.addButton(new GuiNpcButton(2, guiLeft + xSize - 56, guiTop + ySize - 35, 50, 20, "gui.done"));
        this.addButton(new GuiNpcButton(3, guiLeft + xSize - 108, guiTop + ySize - 35, 50, 20, "gui.cancel"));

    	HashMap<String,QuestCategory> categoryData = new HashMap<String,QuestCategory>();
    	HashMap<String,Quest> questData = new HashMap<String,Quest>();

    	for(QuestCategory category : QuestController.Instance.categories.values()) {
    		categoryData.put(category.title, category);
    	}
    	this.categoryData = categoryData;

		if(selectedCategory != null) {
			for(Quest quest : selectedCategory.quests.values()) {
				questData.put(quest.title, quest);
			}
		}
		this.questData = questData;

        if(scrollCategories == null){
	        scrollCategories = new GuiCustomScroll(this,0, 0);
	        scrollCategories.setSize(177, 153);
        }
        scrollCategories.setList(getCatSearch());
        if(selectedCategory != null) {
        	scrollCategories.setSelected(selectedCategory.title);
        }
        scrollCategories.guiLeft = guiLeft + 4;
        scrollCategories.guiTop = guiTop + 14;
        this.addScroll(scrollCategories);
        addTextField(new GuiNpcTextField(33, this, fontRendererObj, guiLeft + 4, guiTop + 169, 177, 20, catSearch));

        if(scrollQuests == null){
        	scrollQuests = new GuiCustomScroll(this,1, 0);
        	scrollQuests.setSize(177, 153);
        }
        scrollQuests.setList(getQuestSearch());
        if(selectedQuest != null) {
        	scrollQuests.setSelected(selectedQuest.title);
        }
        scrollQuests.guiLeft = guiLeft + 182;
        scrollQuests.guiTop = guiTop + 14;
        this.addScroll(scrollQuests);
        addTextField(new GuiNpcTextField(44, this, fontRendererObj, guiLeft + 182, guiTop + 169, 177, 20, questSearch));
    }

    @Override
    public void keyTyped(char c, int i)
    {
        super.keyTyped(c, i);
        if(getTextField(33) != null){
            if(getTextField(33).isFocused()){
                if(catSearch.equals(getTextField(33).getText()))
                    return;
                catSearch = getTextField(33).getText().toLowerCase();
                scrollCategories.setList(getCatSearch());
            }
        }
        if(getTextField(44) != null){
            if(getTextField(44).isFocused()){
                if(questSearch.equals(getTextField(44).getText()))
                    return;
                questSearch = getTextField(44).getText().toLowerCase();
                scrollQuests.setList(getQuestSearch());
            }
        }
    }

    private List<String> getCatSearch(){
        if(catSearch.isEmpty()){
            return Lists.newArrayList(categoryData.keySet());
        }
        List<String> list = new ArrayList<String>();
        for(String name : Lists.newArrayList(categoryData.keySet())){
            if(name.toLowerCase().contains(catSearch))
                list.add(name);
        }
        return list;
    }

    private List<String> getQuestSearch(){
        if(selectedCategory == null){
            return Lists.newArrayList(questData.keySet());
        }

        if(questSearch.isEmpty()){
            return new ArrayList<String>(Lists.newArrayList(questData.keySet()));
        }
        List<String> list = new ArrayList<String>();
        for(String name : Lists.newArrayList(questData.keySet())){
            if(name.toLowerCase().contains(questSearch))
                list.add(name);
        }
        return list;
    }

	@Override
	public void customScrollClicked(int i, int j, int k, GuiCustomScroll guiCustomScroll) {
		if(guiCustomScroll.id == 0){
			selectedCategory = categoryData.get(scrollCategories.getSelected());
			selectedQuest = null;
			scrollQuests.selected = -1;
            scrollQuests.resetScroll();
            getTextField(44).setText("");
            questSearch = "";
		}
		if(guiCustomScroll.id == 1){
			selectedQuest = questData.get(scrollQuests.getSelected());
		}
		initGui();
	}

	@Override
	public void customScrollDoubleClicked(String selection, GuiCustomScroll scroll) {
		if(selectedQuest == null)
			return;
		if(listener != null) {
			listener.selected(selectedQuest.id, selectedQuest.title);
		}
		close();
	}

    @Override
	protected void actionPerformed(GuiButton guibutton){
		int id = guibutton.id;
        if(id == 2){
        	if(selectedQuest != null) {
                customScrollDoubleClicked(null, null);
        	}
        	else {
        		close();
        	}
        }
        if(id == 3){
            close();
        }
    }
}
