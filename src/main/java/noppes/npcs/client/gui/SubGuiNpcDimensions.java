package noppes.npcs.client.gui;

import net.minecraft.client.gui.GuiButton;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.controllers.data.SpawnData;

import java.util.*;

public class SubGuiNpcDimensions extends SubGuiInterface implements IScrollData
{
	private final SpawnData data;
	private GuiCustomScroll allDimensions;
	private HashMap<String, Integer> dimensionMap = new HashMap<>();
	private GuiCustomScroll dataDimensions;
	
    public SubGuiNpcDimensions(SpawnData data){
    	this.data = data;
		setBackground("menubg.png");
		xSize = 346;
		ySize = 216;
		closeOnEsc = true;
		Client.sendData(EnumPacketServer.DimensionsGet);
    }

    public void initGui(){
        super.initGui();
        if(allDimensions == null){
        	allDimensions = new GuiCustomScroll(this,0);
        	allDimensions.setSize(140, 180);
        }
        allDimensions.guiLeft = guiLeft + 4;
        allDimensions.guiTop = guiTop + 14;
        this.addScroll(allDimensions);
        addLabel(new GuiNpcLabel(1, "spawning.availableDimensions", guiLeft + 4, guiTop + 4));
        
        if(dataDimensions == null){
        	dataDimensions = new GuiCustomScroll(this,1);
        	dataDimensions.setSize(140, 180);
        }
        dataDimensions.guiLeft = guiLeft + 200;
        dataDimensions.guiTop = guiTop + 14;
        this.addScroll(dataDimensions);
        addLabel(new GuiNpcLabel(2, "spawning.spawningDimensions", guiLeft + 200, guiTop + 4));

		ArrayList<String> dimensions = new ArrayList<>();
		Set<Map.Entry<String,Integer>> entrySet = this.dimensionMap.entrySet();
		for (Map.Entry<String,Integer> entry : entrySet) {
			if (this.data.dimensions.contains(entry.getValue())) {
				dimensions.add(entry.getKey());
			}
		}
		dataDimensions.setList(dimensions);

    	addButton(new GuiNpcButton(1, guiLeft + 145, guiTop + 40, 55, 20, ">"));
    	addButton(new GuiNpcButton(2, guiLeft + 145, guiTop + 62, 55, 20, "<"));

    	addButton(new GuiNpcButton(3, guiLeft + 145, guiTop + 90, 55, 20, ">>"));
    	addButton(new GuiNpcButton(4, guiLeft + 145, guiTop + 112, 55, 20, "<<"));
        
        
    	addButton(new GuiNpcButton(66, guiLeft + 260, guiTop + 194, 60, 20, "gui.done"));
    }

	protected void actionPerformed(GuiButton guibutton){
    	GuiNpcButton button = (GuiNpcButton) guibutton;
		if(button.id == 1){
			if(allDimensions.hasSelected()){
				data.dimensions.add(dimensionMap.get(allDimensions.getSelected()));
				allDimensions.selected = -1;
				initGui();
			}				
		}
		if(button.id == 2){
			if(dataDimensions.hasSelected()){
				data.dimensions.remove(dimensionMap.get(dataDimensions.getSelected()));
				dataDimensions.selected = -1;
				initGui();
			}				
		}
		if(button.id == 3){
			data.dimensions.clear();
			this.data.dimensions.addAll(this.dimensionMap.values());
			allDimensions.selected = -1;
			initGui();
		}
		if(button.id == 4){
			data.dimensions.clear();
			allDimensions.selected = -1;
			initGui();
		}
		if(button.id == 66){
        	close();
        }
    }

	@Override
	public void setData(Vector<String> list, HashMap<String, Integer> data) {
		this.allDimensions.setList(list);
		this.dimensionMap = data;

		ArrayList<String> dimensions = new ArrayList<>();
		Set<Map.Entry<String,Integer>> entrySet = this.dimensionMap.entrySet();
		for (Map.Entry<String,Integer> entry : entrySet) {
			if (this.data.dimensions.contains(entry.getValue())) {
				dimensions.add(entry.getKey());
			}
		}
		dataDimensions.setList(dimensions);

		initGui();
	}

	@Override
	public void setSelected(String selected) {

	}
}
