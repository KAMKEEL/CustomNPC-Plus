package noppes.npcs.client.gui.global;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import net.minecraft.client.gui.GuiButton;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.GuiNPCInterface2;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.IScrollData;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.constants.EnumPlayerData;
import noppes.npcs.entity.EntityNPCInterface;

public class GuiNpcManagePlayerData extends GuiNPCInterface2 implements IScrollData,ICustomScrollListener
{
	private GuiCustomScroll scroll;
	private String selectedPlayer = null;
	private String selected = null;
	private HashMap<String,Integer> data = new HashMap<String,Integer>();
	private EnumPlayerData selection = EnumPlayerData.Players;
	private String search = "";
	
    public GuiNpcManagePlayerData(EntityNPCInterface npc,GuiNPCInterface2 parent)
    {
    	super(npc);
    	Client.sendData(EnumPacketServer.PlayerDataGet,selection);
    }

    public void initGui()
    {
        super.initGui();        
        scroll = new GuiCustomScroll(this,0);
        scroll.setSize(190, 175);
        scroll.guiLeft = guiLeft + 4;
        scroll.guiTop = guiTop + 16;
        addScroll(scroll);
        
        addLabel(new GuiNpcLabel(0,"All Players", guiLeft + 10, guiTop + 6));

        this.addButton(new GuiNpcButton(0, guiLeft + 200, guiTop + 10,98, 20, "selectWorld.deleteButton"));
    	this.addButton(new GuiNpcButton(1, guiLeft + 200, guiTop + 32,98, 20, "Players"));
    	this.addButton(new GuiNpcButton(2, guiLeft + 200, guiTop + 54,98, 20, "Quest Data"));
    	this.addButton(new GuiNpcButton(3, guiLeft + 200, guiTop + 76,98, 20, "Dialog Data"));
    	this.addButton(new GuiNpcButton(4, guiLeft + 200, guiTop + 98,98, 20, "Transport Data"));
    	this.addButton(new GuiNpcButton(5, guiLeft + 200, guiTop + 120,98, 20, "Bank Data"));
    	this.addButton(new GuiNpcButton(6, guiLeft + 200, guiTop + 142,98, 20, "Faction Data"));
    	
    	addTextField(new GuiNpcTextField(0, this, fontRendererObj, guiLeft + 4, guiTop + 193, 190, 20, search));
    	getTextField(0).enabled = selection == EnumPlayerData.Players;
    	
        initButtons();
    }

    public void initButtons(){
    	getButton(1).setEnabled(selection != EnumPlayerData.Players);
    	getButton(2).setEnabled(selection != EnumPlayerData.Quest);
    	getButton(3).setEnabled(selection != EnumPlayerData.Dialog);
    	getButton(4).setEnabled(selection != EnumPlayerData.Transport);
    	getButton(5).setEnabled(selection != EnumPlayerData.Bank);
    	getButton(6).setEnabled(selection != EnumPlayerData.Factions);
    	
    	if(selection == EnumPlayerData.Players)
    		getLabel(0).label = "All Players";
    	else
    		getLabel(0).label = "Selected player: " + selectedPlayer;
    }
    public void drawScreen(int i, int j, float f)
    {
        super.drawScreen(i, j, f);
        scroll.drawScreen(i, j, f);
    }

    public void mouseClicked(int i, int j, int k)
    {
    	super.mouseClicked(i, j, k);
    	if(k == 0 && scroll != null)
    		scroll.mouseClicked(i, j, k);
    }
    
    public void keyTyped(char c, int i)
    {
    	super.keyTyped(c, i);

    	if(selection != EnumPlayerData.Players)
    		return;
    	
    	if(search.equals(getTextField(0).getText()))
    		return;
    	search = getTextField(0).getText().toLowerCase();
    	scroll.setList(getSearchList());
    }
    private List<String> getSearchList(){
    	if(search.isEmpty() || selection != EnumPlayerData.Players)
    		return new ArrayList<String>(this.data.keySet());
    	List<String> list = new ArrayList<String>();
    	for(String name : data.keySet()){
    		if(name.toLowerCase().contains(search))
    			list.add(name);
    	}
    	return list;
    }
    
	protected void actionPerformed(GuiButton guibutton)
    {
		int id = guibutton.id;
        if(id == 0)
        {
        	if(selected != null){
        		if(selection == EnumPlayerData.Players)
        			Client.sendData(EnumPacketServer.PlayerDataRemove, selection,selectedPlayer,selected);
        		else
        			Client.sendData(EnumPacketServer.PlayerDataRemove, selection,selectedPlayer,data.get(selected));
        		data.clear();
        	}
        	selected = null;
        }
        if(id >= 1 && id <= 6)
        {
        	if(selectedPlayer == null && id != 1)
        		return;
        	selection = EnumPlayerData.values()[id - 1];
        	initButtons();
        	scroll.clear();
        	data.clear();
        	Client.sendData(EnumPacketServer.PlayerDataGet, selection, selectedPlayer);
        	selected = null;
        }
    }
	
	@Override
	public void save() {
	}

	@Override
	public void setData(Vector<String> list, HashMap<String, Integer> data) {
		this.data.putAll(data);
		
    	scroll.setList(getSearchList());
    	
		if(selection == EnumPlayerData.Players && selectedPlayer != null){
			scroll.setSelected(selectedPlayer);
			selected = selectedPlayer;
		}
	}

	@Override
	public void setSelected(String selected) {
	}

	@Override
	public void customScrollClicked(int i, int j, int k, GuiCustomScroll guiCustomScroll) {
		selected = guiCustomScroll.getSelected();
		if(selection == EnumPlayerData.Players)
			selectedPlayer = selected;
	}


}
