package noppes.npcs.client.gui.global;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.controllers.data.Animation;
import noppes.npcs.entity.EntityNPCInterface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class GuiNPCManageAnimations extends GuiNPCInterface2 implements IScrollData,ICustomScrollListener,ITextfieldListener, IGuiData, ISubGuiListener
{
	private GuiCustomScroll scrollAnimations;
	private HashMap<String,Integer> data = new HashMap<String,Integer>();
	private Animation animation = new Animation();
	private String selected = null;
	private String search = "";

    public GuiNPCManageAnimations(EntityNPCInterface npc)
    {
    	super(npc);
    	Client.sendData(EnumPacketServer.AnimationsGet);
    }

    public void initGui()
    {
        super.initGui();
        
       	this.addButton(new GuiNpcButton(0,guiLeft + 368, guiTop + 8, 45, 20, "gui.add"));
    	this.addButton(new GuiNpcButton(1,guiLeft + 368, guiTop + 32, 45, 20, "gui.remove"));
        
    	if(scrollAnimations == null){
	        scrollAnimations = new GuiCustomScroll(this,0, 0);
			scrollAnimations.setSize(143, 185);
    	}
        scrollAnimations.guiLeft = guiLeft + 220;
        scrollAnimations.guiTop = guiTop + 4;
    	addScroll(scrollAnimations);
		addTextField(new GuiNpcTextField(55, this, fontRendererObj, guiLeft + 220, guiTop + 4 + 3 + 185, 143, 20, search));

		if (animation.id == -1)
    		return;

		addLabel(new GuiNpcLabel(10,"ID", guiLeft + 178, guiTop + 4));
		addLabel(new GuiNpcLabel(11, animation.id + "", guiLeft + 178, guiTop + 14));
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
				scrollAnimations.setList(getSearchList());
			}
		}
	}

	private List<String> getSearchList(){
		if(search.isEmpty()){
			return new ArrayList<String>(this.data.keySet());
		}
		List<String> list = new ArrayList<String>();
		for(String name : this.data.keySet()){
			if(name.toLowerCase().contains(search))
				list.add(name);
		}
		return list;
	}

    @Override
	protected void actionPerformed(GuiButton guibutton){
		GuiNpcButton button = (GuiNpcButton) guibutton;
        if(button.id == 0){
        	save();
        	String name = "New";
        	while(data.containsKey(name))
        		name += "_";
        	Animation animation = new Animation(-1, name);
			Client.sendData(EnumPacketServer.AnimationSave, animation.writeToNBT());
        }
        if(button.id == 1){
        	if(data.containsKey(scrollAnimations.getSelected())) {
        		Client.sendData(EnumPacketServer.AnimationRemove, data.get(selected));
        		scrollAnimations.clear();
        		animation = new Animation();
        		initGui();
        	}
        }
    }

	@Override
	public void setGuiData(NBTTagCompound compound) {
		this.animation = new Animation();
		animation.readFromNBT(compound);
		
		setSelected(animation.name);
		initGui();
	}
	

	@Override
	public void setData(Vector<String> list, HashMap<String, Integer> data) {
		String name = scrollAnimations.getSelected();
		this.data = data;
		scrollAnimations.setList(getSearchList());
		
		if(name != null)
			scrollAnimations.setSelected(name);
	}
    
	@Override
	public void setSelected(String selected) {
		this.selected = selected;
		scrollAnimations.setSelected(selected);
	}
    
	@Override
	public void customScrollClicked(int i, int j, int k, GuiCustomScroll guiCustomScroll) {
		if(guiCustomScroll.id == 0)
		{
			save();
			selected = scrollAnimations.getSelected();
			Client.sendData(EnumPacketServer.AnimationGet, data.get(selected));
		}
	}
	
	public void save() {
		if(selected != null && data.containsKey(selected) && animation != null){
			Client.sendData(EnumPacketServer.AnimationSave, animation.writeToNBT());
		}
	}
		
	@Override
	public void unFocused(GuiNpcTextField guiNpcTextField) {
		if(animation.id == -1)
			return;
		
		if(guiNpcTextField.id == 0) {
			String name = guiNpcTextField.getText();
			if(!name.isEmpty() && !data.containsKey(name)){
				String old = animation.name;
				data.remove(animation.name);
				animation.name = name;
				data.put(animation.name, animation.id);
				selected = name;
				scrollAnimations.replace(old, animation.name);
			}
		}
	}

	@Override
	public void subGuiClosed(SubGuiInterface subgui) {
	}

}
