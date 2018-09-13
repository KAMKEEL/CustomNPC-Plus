package noppes.npcs.client.gui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.audio.SoundRegistry;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import noppes.npcs.client.controllers.MusicController;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.GuiNPCInterface2;
import noppes.npcs.client.gui.util.GuiNPCStringSlot;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.entity.EntityNPCInterface;
import cpw.mods.fml.common.ObfuscationReflectionHelper;

public class GuiNpcSoundSelection extends GuiNPCInterface{

	public GuiNPCStringSlot slot;
	private String domain;
	private GuiScreen parent;
	
	private String up = "..<" + StatCollector.translateToLocal("gui.up") + ">..";
	
	private HashMap<String,List<String>> domains = new HashMap<String,List<String>>();
		
    public GuiNpcSoundSelection(EntityNPCInterface npc, GuiScreen parent, String sound)
    {
    	super(npc);
    	SoundHandler handler = Minecraft.getMinecraft().getSoundHandler();
    	SoundRegistry registry = ObfuscationReflectionHelper.getPrivateValue(SoundHandler.class, handler, 4);
    	
    	Set<ResourceLocation> set = registry.getKeys();
    	for(ResourceLocation location : set){
    		List<String> list = domains.get(location.getResourceDomain());
    		if(list == null)
    			domains.put(location.getResourceDomain(), list = new ArrayList<String>());
    		list.add(location.getResourcePath());
    		domains.put(location.getResourceDomain(), list);
    	}
    	drawDefaultBackground = false;
    	this.parent = parent;
    }

    public void initGui()
    {
        super.initGui();
        String ss = "Current domain: " + domain;
        if(domain == null)
        	ss = "Select domain";
        addLabel(new GuiNpcLabel(0,ss, width / 2 - (this.fontRendererObj.getStringWidth(ss)/2), 20, 0xffffff));
        
        Collection<String> col = domains.keySet();
        
        if(domain != null){
        	col = domains.get(domain);
        	if(!col.contains(up))
        		col.add(up);
        }
        slot = new GuiNPCStringSlot(col, this, false, 18);
        slot.registerScrollButtons(4, 5);
        if(domain != null){
        	this.addButton(new GuiNpcButton(1, width / 2 - 100, height - 27,198, 20, "gui.play"));
        	this.addButton(new GuiNpcButton(3, width / 2 - 100, height - 50,98, 20, "gui.done"));
        }
        else
        	this.addButton(new GuiNpcButton(4, width / 2 - 100, height - 50,98, 20, "gui.open"));
        	
    	this.addButton(new GuiNpcButton(2, width / 2 + 2, height - 50,98, 20, "gui.cancel"));
    }

    public void drawScreen(int i, int j, float f)
    {        
    	slot.drawScreen(i, j, f);
        super.drawScreen(i, j, f);
    }

	public void doubleClicked() {
		if(slot.selected == null || slot.selected.isEmpty())
			return;
		if(slot.selected.equals(up)){
			domain = null;
			initGui();
		}
		else if(domain == null){
			domain = slot.selected;
			initGui();
		}
		else{
    		if(parent instanceof GuiNPCInterface){
    			((GuiNPCInterface)parent).elementClicked();
    		}
    		else if(parent instanceof GuiNPCInterface2){
    			((GuiNPCInterface2)parent).elementClicked();
    		}
			displayGuiScreen(parent);
		}
	}

	protected void actionPerformed(GuiButton guibutton)
    {
		super.actionPerformed(guibutton);
        if(guibutton.id == 1){
        	MusicController.Instance.stopMusic();
        	MusicController.Instance.playSound(getSelected(), (float)player.posX, (float)player.posY, (float)player.posZ);
        }
        if(guibutton.id == 2){
			displayGuiScreen(parent);
        }
        if(guibutton.id == 3){
    		if(slot.selected == null || slot.selected.equals(up))
    			return;
			doubleClicked();
        }
        if(guibutton.id == 4){
			doubleClicked();
        }
    }
	public void save() {
		
	}
	
	public String getSelected(){
		if(slot.selected == null || slot.selected.isEmpty())
			return "";
		return domain + ":" + slot.selected;
	}

}
