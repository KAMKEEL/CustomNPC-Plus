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


public class GuiNPCFactionSelection extends GuiNPCInterface implements IScrollData
{
	private GuiNPCStringSlot slot;
	private GuiScreen parent;
	private HashMap<String,Integer> data = new HashMap<String,Integer>();
	private int factionId;
	public GuiSelectionListener listener;
    public GuiNPCFactionSelection(EntityNPCInterface npc,GuiScreen parent,int dialog)
    {
    	super(npc);
    	drawDefaultBackground = false;
		title = "Select Dialog Category";
    	this.parent = parent;
    	this.factionId = dialog;
    	
    	Client.sendData(EnumPacketServer.FactionsGet);
    	
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
        	close();
        	NoppesUtil.openGUI(player, parent);
        }
        if(id == 4)
        {
        	doubleClicked();
        }
    }
	public void doubleClicked() {
    	if(slot.selected == null || slot.selected.isEmpty())
    		return;
		factionId = data.get(slot.selected);
		close();
		NoppesUtil.openGUI(player, parent);
		
	}
	public void save() {
		if(factionId >= 0){
			if(listener != null)
				listener.selected(factionId, slot.selected);
		}
	}

	@Override
	public void setData(Vector<String> list, HashMap<String, Integer> data) {
		this.data = data;
		this.slot.setList(list);
		if(factionId >= 0)
			for(String name : data.keySet())
				if(data.get(name) == factionId)
					slot.selected = name;
	}

	@Override
	public void setSelected(String selected) {
		
	}


}
