package noppes.npcs.client.gui.global;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumPacketServer;

import java.util.HashMap;
import java.util.Vector;

public class GuiNPCQuestSelection extends SubGuiInterface implements IScrollData
{
	private GuiNPCStringSlot slot;
	private HashMap<String,Integer> data;
	private boolean selectCategory = true;
	public GuiSelectionListener listener;
	private int quest;
	
    public GuiNPCQuestSelection(GuiScreen parent, int quest)
    {
    	drawDefaultBackground = false;
		title = "Select Quest Category";
    	data = new HashMap<String, Integer>();
    	this.quest = quest;
    	
    	if(quest >= 0){
    		Client.sendData(EnumPacketServer.QuestsGetFromQuest, quest);
    		selectCategory = false;
    		title = "Select Dialog";
    	}else
    		Client.sendData(EnumPacketServer.QuestCategoriesGet, quest);
    	
    	if(parent instanceof GuiSelectionListener)
    		listener = (GuiSelectionListener) parent;
    }

    public void initGui()
    {
        super.initGui();
        Vector<String> list = new Vector<String>();
        
        slot = new GuiNPCStringSlot(list,this,false,18);
        slot.registerScrollButtons(4, 5);
        

    	this.addButton(new GuiNpcButton(2, width / 2 -100, height - 41,98, 20, "gui.back"));
    	this.addButton(new GuiNpcButton(4, width / 2  + 2, height - 41,98, 20, "mco.template.button.select"));
    }


    public void drawScreen(int i, int j, float f)
    {
    	slot.drawScreen(i, j, f);
        super.drawScreen(i, j, f);
    }

	protected void actionPerformed(GuiButton guibutton)
    {
		int id = guibutton.id;
        if(id == 2)
        {
        	if(selectCategory){
            	close();
        	}else{
    			title = "Select Dialog Category";
        		selectCategory = true;
        		Client.sendData(EnumPacketServer.QuestCategoriesGet, quest);
        	}
        }
        if(id == 4)
        {
        	if(slot.selected == null || slot.selected.isEmpty())
        		return;
        	doubleClicked();
        }
    }
	public String getSelected(){
		return slot.selected;
	}
	public void doubleClicked() {
    	if(slot.selected == null || slot.selected.isEmpty())
    		return;
		if(selectCategory){
			selectCategory = false;
			title = "Select Quest";
			Client.sendData(EnumPacketServer.QuestsGet, data.get(slot.selected));
		}
		else{
			quest = data.get(slot.selected);
			close();
		}
		
	}

	@Override
	public void close() {
		this.save();
		super.close();
	}

	public void save() {
		if(quest >= 0){
			if(listener != null){
				listener.selected(quest, slot.selected);
			}
		}
	}

	@Override
	public void setData(Vector<String> list, HashMap<String, Integer> data) {
		this.data = data;
		this.slot.setList(list);
		if(quest >= 0)
			for(String name : data.keySet())
				if(data.get(name) == quest)
					slot.selected = name;
	}

	@Override
	public void setSelected(String selected) {
		
	}

}
