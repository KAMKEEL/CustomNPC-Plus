package noppes.npcs.client.gui.global;

import java.util.HashMap;
import java.util.Vector;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.client.Client;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.SubGuiMailmanSendSetup;
import noppes.npcs.client.gui.SubGuiNpcCommand;
import noppes.npcs.client.gui.SubGuiNpcFactionOptions;
import noppes.npcs.client.gui.SubGuiNpcTextArea;
import noppes.npcs.client.gui.questtypes.GuiNpcQuestTypeDialog;
import noppes.npcs.client.gui.questtypes.GuiNpcQuestTypeKill;
import noppes.npcs.client.gui.questtypes.GuiNpcQuestTypeLocation;
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
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.constants.EnumQuestCompletion;
import noppes.npcs.constants.EnumQuestRepeat;
import noppes.npcs.constants.EnumQuestType;
import noppes.npcs.controllers.PlayerMail;
import noppes.npcs.controllers.Quest;
import noppes.npcs.controllers.QuestCategory;
import noppes.npcs.entity.EntityNPCInterface;

public class GuiNPCManageQuest extends GuiNPCInterface2 implements IScrollData, ISubGuiListener, GuiSelectionListener,ICustomScrollListener,ITextfieldListener, IGuiData
{
	private GuiCustomScroll scroll;
	private HashMap<String,Integer> data = new HashMap<String,Integer>();
	public static Quest quest = new Quest();
	private QuestCategory category = new QuestCategory();
	private boolean categorySelection = true;
	private boolean questlogTA = false;
	
	public static GuiScreen Instance;
	
    public GuiNPCManageQuest(EntityNPCInterface npc)
    {
    	super(npc);
    	Instance = this;
		Client.sendData(EnumPacketServer.QuestCategoriesGet);
    }

    public void initGui()
    {
        super.initGui();
       	this.addButton(new GuiNpcButton(0,guiLeft + 358, guiTop + 8, 58, 20,categorySelection?"quest.quests":"gui.categories"));
        
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
        if(!categorySelection && quest.id >= 0)
        	dialogGuiInit();
        
    }

	private void dialogGuiInit() {
		addLabel(new GuiNpcLabel(1,"gui.title", guiLeft + 4, guiTop + 8));
		addTextField(new GuiNpcTextField(1, this, this.fontRendererObj, guiLeft + 36 , guiTop + 3, 140, 20, quest.title));

		addLabel(new GuiNpcLabel(0,"ID", guiLeft + 178, guiTop + 4));
		addLabel(new GuiNpcLabel(2,	quest.id + "", guiLeft + 178, guiTop + 14));
		
		addLabel(new GuiNpcLabel(3, "quest.completedtext", guiLeft + 4, guiTop + 30));
    	addButton(new GuiNpcButton(3, guiLeft + 120, guiTop + 25, 50, 20, "selectServer.edit"));
    	
		addLabel(new GuiNpcLabel(4, "quest.questlogtext", guiLeft + 4, guiTop + 51));
		addButton(new GuiNpcButton(4, guiLeft + 120, guiTop + 46, 50, 20, "selectServer.edit"));

		addLabel(new GuiNpcLabel(5, "quest.reward", guiLeft + 4, guiTop + 72));
    	addButton(new GuiNpcButton(5, guiLeft + 120, guiTop + 67, 50, 20, "selectServer.edit"));

		addLabel(new GuiNpcLabel(6, "gui.type", guiLeft + 4, guiTop + 93));
    	addButton(new GuiNpcButton(6, guiLeft + 90, guiTop + 88, 70, 20, new String[]{"quest.item","quest.dialog","quest.kill","quest.location","quest.areakill"},quest.type.ordinal()));
    	addButton(new GuiNpcButton(7, guiLeft + 162, guiTop + 88,50, 20, "selectServer.edit"));


		addLabel(new GuiNpcLabel(8, "quest.repeatable", guiLeft + 4, guiTop + 114));
    	this.addButton(new GuiNpcButton(8, guiLeft + 110, guiTop + 109,70, 20, new String[]{"gui.no","gui.yes","quest.mcdaily","quest.mcweekly","quest.rldaily","quest.rlweekly"}, quest.repeat.ordinal()));

    	this.addButton(new GuiNpcButton(9, guiLeft + 4, guiTop + 131,90, 20, new String[]{"quest.npc","quest.instant"},quest.completion.ordinal()));

    	if(quest.completerNpc.isEmpty())
    		quest.completerNpc = npc.display.name;
    	
		this.addTextField(new GuiNpcTextField(2,this, this.fontRendererObj, guiLeft + 96, guiTop + 131, 114, 20, quest.completerNpc));
		this.getTextField(2).enabled = quest.completion == EnumQuestCompletion.Npc;

		addLabel(new GuiNpcLabel(10, "menu.advanced", guiLeft + 4, guiTop + 158));
		addButton(new GuiNpcButton(10, guiLeft + 120, guiTop + 153, 50, 20, "selectServer.edit"));
		
	}

	private void categoryGuiInit() {
        addTextField(new GuiNpcTextField(0,this, this.fontRendererObj, guiLeft+8, guiTop + 8, 160, 16, category.title));
	}

	public void buttonEvent(GuiButton guibutton)
    {
		GuiNpcButton button = (GuiNpcButton) guibutton;
        if(button.id == 0)
        {
        	save();
        	if(categorySelection){
        		if(category.id < 0)
        			return;
            	quest = new Quest();
            	Client.sendData(EnumPacketServer.QuestsGet, category.id);
        	}
        	else if(!categorySelection){
            	quest = new Quest();
            	category = new QuestCategory();
            	Client.sendData(EnumPacketServer.QuestCategoriesGet);
        	}
        	categorySelection = !categorySelection;
        	getButton(0).setEnabled(false);
    		scroll.clear();
    		data.clear();
        }
        if(button.id == 1)
        {
        	save();
        	String name = "New";
        	while(data.containsKey(name))
        		name += "_";
        	if(categorySelection){
        		QuestCategory category = new QuestCategory();
        		category.title = name;
        		Client.sendData(EnumPacketServer.QuestCategorySave, category.writeNBT(new NBTTagCompound()));
        	}
        	else{
        		Quest quest = new Quest();
        		quest.title = name;
        		Client.sendData(EnumPacketServer.QuestSave, category.id, quest.writeToNBT(new NBTTagCompound()));
        	}
        }
        if(button.id == 2)
        {
        	if(data.containsKey(scroll.getSelected())) {
				if(categorySelection){
					Client.sendData(EnumPacketServer.QuestCategoryRemove, category.id);
					category = new QuestCategory();
				}
				else{
					Client.sendData(EnumPacketServer.QuestRemove, quest.id);
					quest = new Quest();
				}
        		scroll.clear();
        	}
        }
        if(button.id == 3 && quest.id >= 0){
        	questlogTA = false;
        	setSubGui(new SubGuiNpcTextArea(quest.completeText));
        }
        if(button.id == 4 && quest.id >= 0){
        	questlogTA = true;
        	setSubGui(new SubGuiNpcTextArea(quest.logText));
        }
        if(button.id == 5 && quest.id >= 0){
        	Client.sendData(EnumPacketServer.QuestOpenGui, EnumGuiType.QuestReward, quest.writeToNBT(new NBTTagCompound()));
        }
        if(button.id == 6 && quest.id >= 0){
        	quest.setType(EnumQuestType.values()[button.getValue()]);
        }
        if(button.id == 7)
        {
        	if(quest.type == EnumQuestType.Item)
        		Client.sendData(EnumPacketServer.QuestOpenGui, EnumGuiType.QuestItem, quest.writeToNBT(new NBTTagCompound()));
        	
        	if(quest.type == EnumQuestType.Dialog)
        		setSubGui(new GuiNpcQuestTypeDialog(npc, quest, this)); 
        	
        	if(quest.type == EnumQuestType.Kill)
        		setSubGui(new GuiNpcQuestTypeKill(npc, quest, this)); 
        	
        	if(quest.type == EnumQuestType.Location)
        		setSubGui(new GuiNpcQuestTypeLocation(npc, quest, this)); 
        	
        	if(quest.type == EnumQuestType.AreaKill)
        		setSubGui(new GuiNpcQuestTypeKill(npc, quest, this)); 
        }
        if(button.id == 8)
        {
        	quest.repeat = EnumQuestRepeat.values()[button.getValue()];
        }
        if(button.id == 9)
        {	
        	quest.completion = EnumQuestCompletion.values()[button.getValue()];
    		this.getTextField(2).enabled = quest.completion == EnumQuestCompletion.Npc;
        }
        if(button.id == 10)
        {	
			setSubGui(new SubGuiNpcQuestAdvanced(quest, this));
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
			if(quest.id < 0)
				guiNpcTextField.setText("");
			else{
				String name = guiNpcTextField.getText();
				if(name.isEmpty() || data.containsKey(name)){
					guiNpcTextField.setText(quest.title);
				}
				else if(!categorySelection && quest.id >= 0){
					String old = quest.title;
					data.remove(old);
					quest.title = name;
					data.put(quest.title, quest.id);
					scroll.replace(old, quest.title);
				}
			}
		}
		if(guiNpcTextField.id == 2){
	    	quest.completerNpc = guiNpcTextField.getText();
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
			quest.readNBT(compound);
			setSelected(quest.title);
			initGui();
		}
	}

	@Override
	public void subGuiClosed(SubGuiInterface subgui){
		if(subgui instanceof SubGuiNpcTextArea){
			SubGuiNpcTextArea gui = (SubGuiNpcTextArea) subgui;
			if(questlogTA)
				quest.logText = gui.text;
			else
				quest.completeText = gui.text;
		}
		else if(subgui instanceof SubGuiNpcFactionOptions || subgui instanceof SubGuiMailmanSendSetup){
			setSubGui(new SubGuiNpcQuestAdvanced(quest, this));
		}
		else if(subgui instanceof SubGuiNpcCommand){
			SubGuiNpcCommand sub = (SubGuiNpcCommand) subgui;
			quest.command = sub.command;
			setSubGui(new SubGuiNpcQuestAdvanced(quest, this));
		}
		else{
			initGui();
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
	public void selected(int id, String name) {
		quest.nextQuestid = id;
		quest.nextQuestTitle = name;
	}
	
	@Override
	public void customScrollClicked(int i, int j, int k, GuiCustomScroll guiCustomScroll) {
		if(guiCustomScroll.id == 0)
		{
			save();
			String selected = scroll.getSelected();
			if(categorySelection){
				category = new QuestCategory();
				Client.sendData(EnumPacketServer.QuestCategoryGet, data.get(selected));
			}
			else{
				quest = new Quest();
				Client.sendData(EnumPacketServer.QuestGet, data.get(selected));
			}
			
		}
	}
	
	@Override
	public void close(){
		super.close();
		quest = new Quest();
	}

	@Override
	public void save() {
    	GuiNpcTextField.unfocus();
		if(!categorySelection && quest.id >= 0)
			Client.sendData(EnumPacketServer.QuestSave, category.id, quest.writeToNBT(new NBTTagCompound()));
		else if(categorySelection && category.id >= 0)
			Client.sendData(EnumPacketServer.QuestCategorySave, category.writeNBT(new NBTTagCompound()));
	}

	@Override
	public void setSelected(String selected) {
		
	}

}
