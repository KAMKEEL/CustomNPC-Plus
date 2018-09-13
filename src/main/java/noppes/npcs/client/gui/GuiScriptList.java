package noppes.npcs.client.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.world.biome.BiomeGenBase;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.controllers.ScriptContainer;
import noppes.npcs.entity.EntityNPCInterface;

public class GuiScriptList extends SubGuiInterface{
	private GuiCustomScroll scroll1;
	private GuiCustomScroll scroll2;
	private ScriptContainer container;
	private List<String> scripts;
	
	public GuiScriptList(List<String> scripts, ScriptContainer container){
		this.container = container;
		setBackground("menubg.png");
		xSize = 346;
		ySize = 216;
		if(scripts == null)
			scripts = new ArrayList<String>();
		this.scripts = scripts;
	}
	@Override
	public void initGui(){
		super.initGui();
        if(scroll1 == null){
        	scroll1 = new GuiCustomScroll(this,0);
        	scroll1.setSize(140, 180);
        }
        scroll1.guiLeft = guiLeft + 4;
        scroll1.guiTop = guiTop + 14;
        this.addScroll(scroll1);
        addLabel(new GuiNpcLabel(1, "script.availableScripts", guiLeft + 4, guiTop + 4));
        
        if(scroll2 == null){
        	scroll2 = new GuiCustomScroll(this,1);
        	scroll2.setSize(140, 180);
        }
        scroll2.guiLeft = guiLeft + 200;
        scroll2.guiTop = guiTop + 14;
        this.addScroll(scroll2);
        addLabel(new GuiNpcLabel(2, "script.loadedScripts", guiLeft + 200, guiTop + 4));
        List<String> temp = new ArrayList<String>(scripts);
        temp.removeAll(container.scripts);
        scroll1.setList(temp);
        scroll2.setList(container.scripts);

    	addButton(new GuiNpcButton(1, guiLeft + 145, guiTop + 40, 55, 20, ">"));
    	addButton(new GuiNpcButton(2, guiLeft + 145, guiTop + 62, 55, 20, "<"));

    	addButton(new GuiNpcButton(3, guiLeft + 145, guiTop + 90, 55, 20, ">>"));
    	addButton(new GuiNpcButton(4, guiLeft + 145, guiTop + 112, 55, 20, "<<"));
        
    	addButton(new GuiNpcButton(66, guiLeft + 260, guiTop + 194, 60, 20, "gui.done"));
	}

	protected void actionPerformed(GuiButton guibutton){
    	GuiNpcButton button = (GuiNpcButton) guibutton;
		if(button.id == 1){
			if(scroll1.hasSelected()){
				container.scripts.add(scroll1.getSelected());
				scroll1.selected = -1;
				scroll1.selected = -1;
				initGui();
			}				
		}
		if(button.id == 2){
			if(scroll2.hasSelected()){
				container.scripts.remove(scroll2.getSelected());
				scroll2.selected = -1;
				initGui();
			}				
		}
		if(button.id == 3){
			container.scripts.clear();
			for(String script : scripts){
				container.scripts.add(script);
			}
			scroll1.selected = -1;
			scroll1.selected = -1;
			initGui();
		}
		if(button.id == 4){
			container.scripts.clear();
			scroll1.selected = -1;
			scroll1.selected = -1;
			initGui();
		}
		if(button.id == 66){
        	close();
        }
    }
	@Override
	public void save() {
		
	}

}
