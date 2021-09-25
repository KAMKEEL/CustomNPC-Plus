package noppes.npcs.client.gui.advanced;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.entity.EntityNPCInterface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class GuiNPCAdvancedLinkedNpc extends GuiNPCInterface2 implements IScrollData, ICustomScrollListener{
	private GuiCustomScroll scroll;
	private List<String> data = new ArrayList<String>();
	
	public static GuiScreen Instance;
	
    public GuiNPCAdvancedLinkedNpc(EntityNPCInterface npc){
    	super(npc);
    	Instance = this;
		Client.sendData(EnumPacketServer.LinkedGetAll);
    }

    @Override
    public void initGui(){
        super.initGui();
        
       	this.addButton(new GuiNpcButton(1,guiLeft + 358, guiTop + 38, 58, 20, "gui.clear"));
    	
        if(scroll == null){
	        scroll = new GuiCustomScroll(this,0);
	        scroll.setSize(143, 208);
        }
        scroll.guiLeft = guiLeft + 137;
        scroll.guiTop = guiTop + 4;
        scroll.setSelected(npc.linkedName);
        scroll.setList(data);
        this.addScroll(scroll);
    }

    @Override
	public void buttonEvent(GuiButton button){
        if(button.id == 1){
        	Client.sendData(EnumPacketServer.LinkedSet, "");
        }
        
    }

	@Override
	public void setData(Vector<String> list, HashMap<String, Integer> data) {
		this.data = new ArrayList<String>(list);
		initGui();
	}

	@Override
	public void setSelected(String selected) {
		scroll.setSelected(selected);
	}

	@Override
	public void save() {
		
	}

	@Override
	public void customScrollClicked(int i, int j, int k,
			GuiCustomScroll guiCustomScroll) {
    	Client.sendData(EnumPacketServer.LinkedSet, guiCustomScroll.getSelected());
	}

}
