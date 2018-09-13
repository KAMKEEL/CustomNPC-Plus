package noppes.npcs.client.gui;

import java.util.HashMap;
import java.util.Vector;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import noppes.npcs.client.Client;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.GuiNPCStringSlot;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiSelectionListener;
import noppes.npcs.client.gui.util.IScrollData;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.entity.EntityNPCInterface;

public class GuiNPCDialogSelection extends GuiNPCInterface implements IScrollData
{
	private GuiNPCStringSlot slot;
	private GuiScreen parent;
	private HashMap<String,Integer> data = new HashMap<String,Integer>();
	private int dialog;
	private boolean selectCategory = true;
	public GuiSelectionListener listener;
    public GuiNPCDialogSelection(EntityNPCInterface npc,GuiScreen parent,int dialog)
    {
    	super(npc);
    	drawDefaultBackground = false;
		title = "Select Dialog Category";
    	this.parent = parent;
    	this.dialog = dialog;
    	
    	
    	if(dialog >= 0){
    		Client.sendData(EnumPacketServer.DialogsGetFromDialog, dialog);
    		selectCategory = false;
    		title = "Select Dialog";
    	}else
    		Client.sendData(EnumPacketServer.DialogCategoriesGet, dialog);
    	
    	if(parent instanceof GuiSelectionListener){
    		listener = (GuiSelectionListener) parent;
    	}
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
            	NoppesUtil.openGUI(player, parent);
        	}else{
    			title = "Select Dialog Category";
        		selectCategory = true;
        		Client.sendData(EnumPacketServer.DialogCategoriesGet, dialog);
        	}
        }
        if(id == 4)
        {
        	doubleClicked();
        }
    }
	public void doubleClicked() {
    	if(slot.selected == null || slot.selected.isEmpty())
    		return;
		if(selectCategory){
			selectCategory = false;
			title = "Select Dialog";
			Client.sendData(EnumPacketServer.DialogsGet, data.get(slot.selected));
		}
		else{
			dialog = data.get(slot.selected);
			close();
			NoppesUtil.openGUI(player, parent);
		}
		
	}
	public void save() {
		if(dialog >= 0){
			if(listener != null)
				listener.selected(dialog, slot.selected);
		}
	}

	@Override
	public void setData(Vector<String> list, HashMap<String, Integer> data) {
		this.data = data;
		this.slot.setList(list);
		if(dialog >= 0)
			for(String name : data.keySet())
				if(data.get(name) == dialog)
					slot.selected = name;
	}

	@Override
	public void setSelected(String selected) {
		
	}


}
