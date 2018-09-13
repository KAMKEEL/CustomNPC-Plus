package noppes.npcs.client.gui.global;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.SubGuiColorSelector;
import noppes.npcs.client.gui.SubGuiNpcFactionPoints;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.GuiNPCInterface2;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.client.gui.util.IScrollData;
import noppes.npcs.client.gui.util.ISubGuiListener;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.controllers.Faction;
import noppes.npcs.entity.EntityNPCInterface;

public class GuiNPCManageFactions extends GuiNPCInterface2 implements IScrollData,ICustomScrollListener,ITextfieldListener, IGuiData, ISubGuiListener
{
	private GuiCustomScroll scrollFactions;
	private HashMap<String,Integer> data = new HashMap<String,Integer>();
	private Faction faction = new Faction();
	private String selected = null;
	
    public GuiNPCManageFactions(EntityNPCInterface npc)
    {
    	super(npc);
    	Client.sendData(EnumPacketServer.FactionsGet);
    }

    public void initGui()
    {
        super.initGui();
        
       	this.addButton(new GuiNpcButton(0,guiLeft + 368, guiTop + 8, 45, 20, "gui.add"));
    	this.addButton(new GuiNpcButton(1,guiLeft + 368, guiTop + 32, 45, 20, "gui.remove"));
        
    	if(scrollFactions == null){
	        scrollFactions = new GuiCustomScroll(this,0);
	        scrollFactions.setSize(143, 208);
    	}
        scrollFactions.guiLeft = guiLeft + 220;
        scrollFactions.guiTop = guiTop + 4;
    	addScroll(scrollFactions);
        
    	if (faction.id == -1)
    		return;
           	
    	this.addTextField(new GuiNpcTextField(0, this, guiLeft + 40, guiTop + 4, 136, 20, faction.name));
    	getTextField(0).setMaxStringLength(20);
    	addLabel(new GuiNpcLabel(0,"gui.name", guiLeft + 8, guiTop + 9));

		addLabel(new GuiNpcLabel(10,"ID", guiLeft + 178, guiTop + 4));
		addLabel(new GuiNpcLabel(11, faction.id + "", guiLeft + 178, guiTop + 14));

    	String color = Integer.toHexString(faction.color);
    	while(color.length() < 6)
    		color = "0" + color;
    	addButton(new GuiNpcButton(10, guiLeft + 40, guiTop + 26, 60, 20, color));
    	addLabel(new GuiNpcLabel(1,"gui.color", guiLeft + 8, guiTop + 31));
    	getButton(10).setTextColor(faction.color);

    	addLabel(new GuiNpcLabel(2,"faction.points", guiLeft + 8, guiTop + 53));
       	this.addButton(new GuiNpcButton(2,guiLeft + 100, guiTop + 48, 45, 20, "selectServer.edit"));

    	addLabel(new GuiNpcLabel(3,"faction.hidden", guiLeft + 8, guiTop + 75));
       	this.addButton(new GuiNpcButton(3,guiLeft + 100, guiTop + 70, 45, 20, new String[]{"gui.no","gui.yes"},faction.hideFaction?1:0));


    	addLabel(new GuiNpcLabel(4,"faction.attacked", guiLeft + 8, guiTop + 97));
       	this.addButton(new GuiNpcButton(4,guiLeft + 100, guiTop + 92, 45, 20, new String[]{"gui.no","gui.yes"},faction.getsAttacked?1:0));
    	
    	addLabel(new GuiNpcLabel(6,"faction.hostiles", guiLeft + 8, guiTop + 145));
    	
		ArrayList<String> hostileList = new ArrayList<String>(scrollFactions.getList());
		hostileList.remove(faction.name);

		HashSet<String> set = new HashSet<String>();
		for(String s : data.keySet()){
			if(!s.equals(faction.name) && faction.attackFactions.contains(data.get(s)))
				set.add(s);
		}
		
    	GuiCustomScroll scrollHostileFactions = new GuiCustomScroll(this,1,true);
        scrollHostileFactions.setSize(163, 58);
        scrollHostileFactions.guiLeft = guiLeft + 4;
        scrollHostileFactions.guiTop = guiTop + 154;
		scrollHostileFactions.setList(hostileList);
		scrollHostileFactions.setSelectedList(set);
        addScroll(scrollHostileFactions);
    }

    @Override
	protected void actionPerformed(GuiButton guibutton){
		GuiNpcButton button = (GuiNpcButton) guibutton;
        if(button.id == 0){
        	save();
        	String name = "New";
        	while(data.containsKey(name))
        		name += "_";
        	Faction faction = new Faction(-1, name, 0x00FF00, 1000);
        	
			NBTTagCompound compound = new NBTTagCompound();
			faction.writeNBT(compound);
			Client.sendData(EnumPacketServer.FactionSave, compound);
        }
        if(button.id == 1){
        	if(data.containsKey(scrollFactions.getSelected())) {
        		Client.sendData(EnumPacketServer.FactionRemove, data.get(selected));
        		scrollFactions.clear();
        		faction = new Faction();
        		initGui();
        	}
        }
        if(button.id == 2){
        	this.setSubGui(new SubGuiNpcFactionPoints(faction));
        }
        if(button.id == 3){
        	faction.hideFaction = button.getValue() == 1;
        }
        if(button.id == 4){
        	faction.getsAttacked = button.getValue() == 1;
        }
        if(button.id == 10){
        	this.setSubGui(new SubGuiColorSelector(faction.color));
        }
    }

	@Override
	public void setGuiData(NBTTagCompound compound) {
		this.faction = new Faction();
		faction.readNBT(compound);
		
		setSelected(faction.name);
		initGui();
	}
	

	@Override
	public void setData(Vector<String> list, HashMap<String, Integer> data) {
		String name = scrollFactions.getSelected();
		this.data = data;
		scrollFactions.setList(list);
		
		if(name != null)
			scrollFactions.setSelected(name);
	}
    
	@Override
	public void setSelected(String selected) {
		this.selected = selected;
		scrollFactions.setSelected(selected);
	}
    
	@Override
	public void customScrollClicked(int i, int j, int k, GuiCustomScroll guiCustomScroll) {
		if(guiCustomScroll.id == 0)
		{
			save();
			selected = scrollFactions.getSelected();
			Client.sendData(EnumPacketServer.FactionGet, data.get(selected));
		}
		else if(guiCustomScroll.id == 1)
		{
			HashSet<Integer> set = new HashSet<Integer>();
			for(String s : guiCustomScroll.getSelectedList()){
				if(data.containsKey(s))
					set.add(data.get(s));
			}
			faction.attackFactions = set;
			save();
		}
	}
	
	public void save() {
		if(selected != null && data.containsKey(selected) && faction != null){
			NBTTagCompound compound = new NBTTagCompound();
			faction.writeNBT(compound);
    	
			Client.sendData(EnumPacketServer.FactionSave, compound);
		}
	}
		
	@Override
	public void unFocused(GuiNpcTextField guiNpcTextField) {
		if(faction.id == -1) 
			return;
		
		if(guiNpcTextField.id == 0) {
			String name = guiNpcTextField.getText();
			if(!name.isEmpty() && !data.containsKey(name)){
				String old = faction.name;
				data.remove(faction.name);
				faction.name = name;
				data.put(faction.name, faction.id);
				selected = name;
				scrollFactions.replace(old,faction.name);
			}
		} else if(guiNpcTextField.id == 1) {
			int color = 0;
			try{
				color = Integer.parseInt(guiNpcTextField.getText(),16);
			}
			catch(NumberFormatException e){
				color = 0;
			}
	    	faction.color = color;
	    	guiNpcTextField.setTextColor(faction.color);
		} 
		
	}

	@Override
	public void subGuiClosed(SubGuiInterface subgui) {
		if(subgui instanceof SubGuiColorSelector){
	    	faction.color = ((SubGuiColorSelector)subgui).color;
	    	initGui();
		}
	}

}
