package noppes.npcs.client.gui;

import java.util.HashMap;
import java.util.Vector;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.StatCollector;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.IScrollData;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.controllers.FactionOptions;

public class SubGuiNpcFactionOptions extends SubGuiInterface implements IScrollData,ICustomScrollListener
{
	private FactionOptions options;
	private HashMap<String,Integer> data = new HashMap<String,Integer>();
	private GuiCustomScroll scrollFactions;
	
	private int selected = -1;
	
    public SubGuiNpcFactionOptions(FactionOptions options)
    {
    	this.options = options;
		setBackground("menubg.png");
		xSize = 256;
		ySize = 216;
		closeOnEsc = true;
    	Client.sendData(EnumPacketServer.FactionsGet);
    }

    public void initGui()
    {
        super.initGui();
        if(scrollFactions == null){
	        scrollFactions = new GuiCustomScroll(this,0);
	        scrollFactions.setSize(120, 208);
        }
        scrollFactions.guiLeft = guiLeft + 130;
        scrollFactions.guiTop = guiTop + 4;
        addScroll(scrollFactions);

        addLabel(new GuiNpcLabel(0, "1: ", guiLeft + 4,  guiTop + 12));
        if(data.containsValue(options.factionId)){
            addLabel(new GuiNpcLabel(1, getFactionName(options.factionId), guiLeft + 12,  guiTop + 8));
            
            String label = "";
            if(options.decreaseFactionPoints)
            	label += StatCollector.translateToLocal("gui.decrease");
            else
            	label += StatCollector.translateToLocal("gui.increase");
            label += " " + options.factionPoints + " " + StatCollector.translateToLocal("faction.points");

            addLabel(new GuiNpcLabel(3, label, guiLeft + 12,  guiTop + 16));
            addButton(new GuiNpcButton(0, guiLeft + 110, guiTop + 7, 20, 20, "X"));
        }

        addLabel(new GuiNpcLabel(4, "2: ", guiLeft + 4,  guiTop + 40));
        if(data.containsValue(options.faction2Id)){
            addLabel(new GuiNpcLabel(5, getFactionName(options.faction2Id), guiLeft + 12,  guiTop + 36));

            String label = "";
            if(options.decreaseFaction2Points)
            	label += StatCollector.translateToLocal("gui.decrease");
            else
            	label += StatCollector.translateToLocal("gui.increase");
            label += " " + options.faction2Points + " " + StatCollector.translateToLocal("faction.points");

            addLabel(new GuiNpcLabel(6, label, guiLeft + 12,  guiTop + 44));
            addButton(new GuiNpcButton(1, guiLeft + 110, guiTop + 35, 20, 20, "X"));
        }

        
        if(selected >= 0 && (!data.containsValue(options.faction2Id) || !data.containsValue(options.factionId)) && !options.hasFaction(selected)){
            addButton(new GuiNpcButton(2, guiLeft + 4, guiTop + 60, 90, 20, new String[]{"gui.increase","gui.decrease"},0));
            
            addTextField(new GuiNpcTextField(1, this, fontRendererObj, guiLeft + 4, guiTop + 82, 110, 20, "10"));
            getTextField(1).numbersOnly = true;
            getTextField(1).setMinMaxDefault(1, 100000, 10);
            
            addButton(new GuiNpcButton(3, guiLeft + 4, guiTop + 104, 60, 20, "gui.add"));
        }

        addButton(new GuiNpcButton(66, guiLeft + 20, guiTop + 192, 90, 20, "gui.done"));
    }
    private String getFactionName(int faction){
    	for(String s : data.keySet())
    		if(data.get(s) == faction)
    			return s;
    	return null;
    }
    
	protected void actionPerformed(GuiButton guibutton)
    {
		int id = guibutton.id;
        if(id == 0)
        {
        	options.factionId = -1;
        	initGui();
        }
        if(id == 1)
        {
        	options.faction2Id = -1;
        	initGui();
        }
        if(id == 3)
        {
        	if(!data.containsValue(options.factionId)){
        		options.factionId = selected;
        		options.decreaseFactionPoints = getButton(2).getValue() == 1;
        		options.factionPoints = getTextField(1).getInteger();
        	}
        	else if(!data.containsValue(options.faction2Id)){
        		options.faction2Id = selected;
        		options.decreaseFaction2Points = getButton(2).getValue() == 1;
        		options.faction2Points = getTextField(1).getInteger();
        	}
        	initGui();
        }
        if(id == 66)
        {
        	close();
        }
    }

	@Override
	public void customScrollClicked(int i, int j, int k,
			GuiCustomScroll guiCustomScroll) {
		selected = data.get(guiCustomScroll.getSelected());
		initGui();
	}

	@Override
	public void setData(Vector<String> list, HashMap<String, Integer> data) {
		GuiCustomScroll scroll = getScroll(0);
		String name = scroll.getSelected();
		this.data = data;
		scroll.setList(list);
		
		if(name != null)
			scroll.setSelected(name);
		
		initGui();
	}

	@Override
	public void setSelected(String selected) {
		
	}

}
