package noppes.npcs.client.gui.select;

import com.google.common.collect.Lists;
import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.audio.SoundRegistry;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.client.controllers.MusicController;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.SubGuiInterface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class GuiSoundSelection extends SubGuiInterface implements ICustomScrollListener{

	private GuiCustomScroll scrollCategories;
	private GuiCustomScroll scrollQuests;

	private String selectedDomain;
	public ResourceLocation selectedResource;

	private HashMap<String,List<String>> domains = new HashMap<String,List<String>>();

    public GuiSoundSelection(String sound){
    	drawDefaultBackground = false;
		title = "";
		setBackground("menubg.png");
		xSize = 366;
		ySize = 226;

    	SoundHandler handler = Minecraft.getMinecraft().getSoundHandler();
    	SoundRegistry registry = ReflectionHelper.getPrivateValue(SoundHandler.class, handler, 4);

    	Set<ResourceLocation> set = registry.getKeys();
    	for(ResourceLocation location : set){
    		List<String> list = domains.get(location.getResourceDomain());
    		if(list == null)
    			domains.put(location.getResourceDomain(), list = new ArrayList<String>());
    		list.add(location.getResourcePath());
    		domains.put(location.getResourceDomain(), list);
    	}
    	if(sound != null && !sound.isEmpty()){
    		selectedResource = new ResourceLocation(sound);
    		selectedDomain = selectedResource.getResourceDomain();
    		if(!domains.containsKey(selectedDomain)) {
    			selectedDomain = null;
    		}
    	}
    }

    @Override
    public void initGui(){
        super.initGui();
    	this.addButton(new GuiNpcButton(2, guiLeft + xSize - 26, guiTop + 4, 20, 20, "X"));
    	this.addButton(new GuiNpcButton(1, guiLeft + 160, guiTop + 212, 70, 20, "gui.play"));
        getButton(1).enabled = selectedResource != null;

        if(scrollCategories == null){
	        scrollCategories = new GuiCustomScroll(this,0);
	        scrollCategories.setSize(90, 200);
        }
        scrollCategories.setList(Lists.newArrayList(domains.keySet()));
        if(selectedDomain != null) {
        	scrollCategories.setSelected(selectedDomain);
        }

        scrollCategories.guiLeft = guiLeft + 4;
        scrollCategories.guiTop = guiTop + 14;
        this.addScroll(scrollCategories);

        if(scrollQuests == null){
        	scrollQuests = new GuiCustomScroll(this,1);
        	scrollQuests.setSize(250, 200);
        }
        if(selectedDomain != null) {
        	scrollQuests.setList(domains.get(selectedDomain));
        }
        if(selectedResource != null) {
        	scrollQuests.setSelected(selectedResource.getResourcePath());
        }
        scrollQuests.guiLeft = guiLeft + 95;
        scrollQuests.guiTop = guiTop + 14;
        this.addScroll(scrollQuests);

    }

	@Override
	protected void actionPerformed(GuiButton guibutton){
		super.actionPerformed(guibutton);
        if(guibutton.id == 1){
        	MusicController.Instance.stopMusic();
            MusicController.Instance.playSound(selectedResource.toString(), (float)player.posX, (float)player.posY, (float)player.posZ);
        }
        if(guibutton.id == 2){
    		close();
        }
    }


	@Override
	public void customScrollDoubleClicked(String selection, GuiCustomScroll scroll) {
		if(selectedResource == null)
			return;
		close();
	}

    @Override
    public void customScrollClicked(int i, int j, int k, GuiCustomScroll guiCustomScroll) {
        if(guiCustomScroll.id == 0){
            selectedDomain = guiCustomScroll.getSelected();
            selectedResource = null;
            scrollQuests.selected = -1;
        }
        if(guiCustomScroll.id == 1){
            selectedResource = new ResourceLocation(selectedDomain, guiCustomScroll.getSelected());
        }
        initGui();
    }
}
