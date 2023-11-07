package noppes.npcs.client.gui.global;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.SubGuiEditText;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.entity.EntityNPCInterface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class GuiNPCManageLinkedNpc extends GuiNPCInterface2 implements IScrollData, ISubGuiListener{
	private GuiCustomScroll scroll;
	private List<String> data = new ArrayList<String>();
	private String search = "";
	
	public static GuiScreen Instance;
	
    public GuiNPCManageLinkedNpc(EntityNPCInterface npc){
    	super(npc);
    	Instance = this;
		Client.sendData(EnumPacketServer.LinkedGetAll);
    }

    @Override
    public void initGui(){
        super.initGui();
        
       	this.addButton(new GuiNpcButton(1,guiLeft + 358, guiTop + 38, 58, 20, "gui.add"));
    	this.addButton(new GuiNpcButton(2,guiLeft + 358, guiTop + 61, 58, 20, "gui.remove"));
    	
        if(scroll == null){
	        scroll = new GuiCustomScroll(this,0,0);
	        scroll.setSize(143, 185);
        }
        scroll.guiLeft = guiLeft + 214;
        scroll.guiTop = guiTop + 4;
        scroll.setList(getSearchList());
        this.addScroll(scroll);
		addTextField(new GuiNpcTextField(55, this, fontRendererObj, guiLeft + 220, guiTop + 4 + 3 + 185, 143, 20, search));
    }

	@Override
	public void keyTyped(char c, int i)
	{
		super.keyTyped(c, i);
		if(getTextField(55) != null){
			if(getTextField(55).isFocused()){
				if(search.equals(getTextField(55).getText()))
					return;
				search = getTextField(55).getText().toLowerCase();
				scroll.setList(getSearchList());
			}
		}
	}

	private List<String> getSearchList(){
		if(search.isEmpty()){
			return new ArrayList<String>(this.data);
		}
		List<String> list = new ArrayList<String>();
		for(String name : this.data){
			if(name.toLowerCase().contains(search))
				list.add(name);
		}
		return list;
	}

    @Override
	public void buttonEvent(GuiButton button){
        if(button.id == 1){
        	save();
        	setSubGui(new SubGuiEditText("New"));
        }
        if(button.id == 2){
        	if(scroll.hasSelected())
        		Client.sendData(EnumPacketServer.LinkedRemove, scroll.getSelected());
        }
        
    }

	@Override
	public void subGuiClosed(SubGuiInterface subgui) {
		if(!((SubGuiEditText)subgui).cancelled){
			Client.sendData(EnumPacketServer.LinkedAdd, ((SubGuiEditText)subgui).text);
		}
	}

	@Override
	public void setData(Vector<String> list, HashMap<String, Integer> data) {
		this.data = new ArrayList<String>(list);
		this.scroll.setList(getSearchList());
		initGui();
	}

	@Override
	public void setSelected(String selected) {
		
	}

	@Override
	public void save() {
		
	}

}
