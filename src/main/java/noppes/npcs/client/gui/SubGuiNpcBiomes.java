package noppes.npcs.client.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.BiomeGenMutated;
import net.minecraftforge.common.BiomeManager;
import noppes.npcs.DataAI;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.constants.EnumAnimation;
import noppes.npcs.constants.EnumMovingType;
import noppes.npcs.constants.EnumStandingType;
import noppes.npcs.controllers.SpawnData;

public class SubGuiNpcBiomes extends SubGuiInterface
{
	private SpawnData data;
	private GuiCustomScroll scroll1;
	private GuiCustomScroll scroll2;
	
    public SubGuiNpcBiomes(SpawnData data){
    	this.data = data;
		setBackground("menubg.png");
		xSize = 346;
		ySize = 216;
		closeOnEsc = true;
    }

    public void initGui(){
        super.initGui();
        if(scroll1 == null){
        	scroll1 = new GuiCustomScroll(this,0);
        	scroll1.setSize(140, 180);
        }
        scroll1.guiLeft = guiLeft + 4;
        scroll1.guiTop = guiTop + 14;
        this.addScroll(scroll1);
        addLabel(new GuiNpcLabel(1, "spawning.availableBiomes", guiLeft + 4, guiTop + 4));
        
        if(scroll2 == null){
        	scroll2 = new GuiCustomScroll(this,1);
        	scroll2.setSize(140, 180);
        }
        scroll2.guiLeft = guiLeft + 200;
        scroll2.guiTop = guiTop + 14;
        this.addScroll(scroll2);
        addLabel(new GuiNpcLabel(2, "spawning.spawningBiomes", guiLeft + 200, guiTop + 4));
        
        List<String> biomes = new ArrayList<String>();
        for (BiomeGenBase base : BiomeGenBase.getBiomeGenArray()) {
            if (base != null && base.biomeName != null && !data.biomes.contains(base.biomeName)) {
            	biomes.add(base.biomeName);
            }
        }
        scroll1.setList(biomes);
        scroll2.setList(data.biomes);

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
				data.biomes.add(scroll1.getSelected());
				scroll1.selected = -1;
				scroll1.selected = -1;
				initGui();
			}				
		}
		if(button.id == 2){
			if(scroll2.hasSelected()){
				data.biomes.remove(scroll2.getSelected());
				scroll2.selected = -1;
				initGui();
			}				
		}
		if(button.id == 3){
			data.biomes.clear();
	        for (BiomeGenBase base : BiomeGenBase.getBiomeGenArray()) {
	            if (base != null) {
	            	data.biomes.add(base.biomeName);
	            }
	        }
			scroll1.selected = -1;
			scroll1.selected = -1;
			initGui();
		}
		if(button.id == 4){
			data.biomes.clear();
			scroll1.selected = -1;
			scroll1.selected = -1;
			initGui();
		}
		if(button.id == 66){
        	close();
        }
    }

}
