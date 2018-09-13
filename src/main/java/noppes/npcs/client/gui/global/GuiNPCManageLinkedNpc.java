package noppes.npcs.client.gui.global;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.apache.logging.log4j.status.StatusConsoleListener;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.SubGuiEditText;
import noppes.npcs.client.gui.SubGuiMailmanSendSetup;
import noppes.npcs.client.gui.SubGuiNpcCommand;
import noppes.npcs.client.gui.SubGuiNpcFactionOptions;
import noppes.npcs.client.gui.SubGuiNpcTextArea;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNPCInterface2;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.GuiSelectionListener;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.client.gui.util.IScrollData;
import noppes.npcs.client.gui.util.ISubGuiListener;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.controllers.Quest;
import noppes.npcs.controllers.QuestCategory;
import noppes.npcs.entity.EntityNPCInterface;

public class GuiNPCManageLinkedNpc extends GuiNPCInterface2 implements IScrollData, ISubGuiListener{
	private GuiCustomScroll scroll;
	private List<String> data = new ArrayList<String>();
	
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
	        scroll = new GuiCustomScroll(this,0);
	        scroll.setSize(143, 208);
        }
        scroll.guiLeft = guiLeft + 214;
        scroll.guiTop = guiTop + 4;
        scroll.setList(data);
        this.addScroll(scroll);
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
		initGui();
	}

	@Override
	public void setSelected(String selected) {
		
	}

	@Override
	public void save() {
		
	}

}
