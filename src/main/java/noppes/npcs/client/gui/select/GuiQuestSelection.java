//package noppes.npcs.client.gui.select;
//
//import com.google.common.collect.Lists;
//import net.minecraft.client.gui.GuiButton;
//import noppes.npcs.client.gui.util.*;
//import noppes.npcs.controllers.QuestController;
//import noppes.npcs.controllers.data.Quest;
//import noppes.npcs.controllers.data.QuestCategory;
//
//import java.util.HashMap;
//
//public class GuiQuestSelection extends SubGuiInterface implements ICustomScrollListener{
//	private HashMap<String,QuestCategory> categoryData = new HashMap<String,QuestCategory>();
//	private HashMap<String,Quest> questData = new HashMap<String,Quest>();
//
//	private GuiCustomScroll scrollCategories;
//	private GuiCustomScroll scrollQuests;
//
//	private QuestCategory selectedCategory;
//	public Quest selectedQuest;
//
//	private GuiSelectionListener listener;
//
//    public GuiQuestSelection(int quest){
//    	drawDefaultBackground = false;
//		title = "";
//		setBackground("menubg.png");
//		xSize = 366;
//		ySize = 226;
//    	this.selectedQuest = QuestController.instance.quests.get(quest);
//    	if(selectedQuest != null) {
//    		selectedCategory = selectedQuest.category;
//    	}
//    }
//
//    @Override
//    public void initGui(){
//        super.initGui();
//
//		if(parent instanceof GuiSelectionListener){
//			listener = (GuiSelectionListener) parent;
//		}
//        this.addLabel(new GuiNpcLabel(0, "gui.categories", guiLeft + 8, guiTop + 4));
//        this.addLabel(new GuiNpcLabel(1, "quest.quests", guiLeft + 175, guiTop + 4));
//
//    	this.addButton(new GuiNpcButton(2, guiLeft + xSize - 26, guiTop + 4, 20, 20, "X"));
//
//    	HashMap<String,QuestCategory> categoryData = new HashMap<String,QuestCategory>();
//    	HashMap<String,Quest> questData = new HashMap<String,Quest>();
//
//    	for(QuestCategory category : QuestController.instance.categories.values()) {
//    		categoryData.put(category.title, category);
//    	}
//    	this.categoryData = categoryData;
//
//		if(selectedCategory != null) {
//			for(Quest quest : selectedCategory.quests.values()) {
//				questData.put(quest.title, quest);
//			}
//		}
//		this.questData = questData;
//
//        if(scrollCategories == null){
//	        scrollCategories = new GuiCustomScroll(this,0);
//	        scrollCategories.setSize(170, 200);
//        }
//        scrollCategories.setList(Lists.newArrayList(categoryData.keySet()));
//        if(selectedCategory != null) {
//        	scrollCategories.setSelected(selectedCategory.title);
//        }
//
//        scrollCategories.guiLeft = guiLeft + 4;
//        scrollCategories.guiTop = guiTop + 14;
//        this.addScroll(scrollCategories);
//
//        if(scrollQuests == null){
//        	scrollQuests = new GuiCustomScroll(this,1);
//        	scrollQuests.setSize(170, 200);
//        }
//        scrollQuests.setList(Lists.newArrayList(questData.keySet()));
//        if(selectedQuest != null) {
//        	scrollQuests.setSelected(selectedQuest.title);
//        }
//        scrollQuests.guiLeft = guiLeft + 175;
//        scrollQuests.guiTop = guiTop + 14;
//        this.addScroll(scrollQuests);
//    }
//
//	@Override
//	public void scrollClicked(int i, int j, int k, GuiCustomScroll guiCustomScroll) {
//		if(guiCustomScroll.id == 0){
//			selectedCategory = categoryData.get(scrollCategories.getSelected());
//			selectedQuest = null;
//			scrollQuests.selected = -1;
//		}
//		if(guiCustomScroll.id == 1){
//			selectedQuest = questData.get(scrollQuests.getSelected());
//		}
//		initGui();
//	}
//
//	@Override
//	public void scrollDoubleClicked(String selection, GuiCustomScroll scroll) {
//		if(selectedQuest == null)
//			return;
//		if(listener != null) {
//			listener.selected(selectedQuest.id, selectedQuest.title);
//		}
//		close();
//	}
//
//    @Override
//	protected void actionPerformed(GuiButton guibutton){
//		int id = guibutton.id;
//        if(id == 2){
//        	if(selectedQuest != null) {
//            	scrollDoubleClicked(null, null);
//        	}
//        	else {
//        		close();
//        	}
//        }
//    }
//}
