package noppes.npcs.client.gui.global;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.controllers.data.Animation;
import noppes.npcs.controllers.data.AnimationData;
import noppes.npcs.controllers.data.Frame;
import noppes.npcs.controllers.data.FramePart;
import noppes.npcs.entity.EntityNPCInterface;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class GuiNPCManageAnimations extends GuiModelInterface2 implements IScrollData,ICustomScrollListener,ITextfieldListener, IGuiData, ISubGuiListener
{
	private GuiCustomScroll scrollAnimations;
	private HashMap<String,Integer> data = new HashMap<String,Integer>();
	private Animation animation = new Animation();
	public boolean playingAnimation = false;
	private String selected = null;
	private String search = "";

    public GuiNPCManageAnimations(EntityNPCInterface npc)
    {
    	super(npc);
		this.xOffset = -148 + 70;
		this.yOffset = -170 + 137;
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

		addLabel(new GuiNpcLabel(10,"ID", guiLeft + 368, guiTop + 54));
		addLabel(new GuiNpcLabel(11, animation.id + "", guiLeft + 368, guiTop + 64));

		AnimationData data = npc.display.animationData;
		if (!playingAnimation) {
			data.animation = new Animation();
			data.animation.smooth = animation.smooth;
			data.animation.renderTicks = animation.renderTicks;
			data.animation.loop = 0;
		}

		String animTexture = "customnpcs:textures/gui/animation.png";
		if (data.animation != null && data.animation.frames.size() > 0) {
			if (!this.playingAnimation || data.animation.paused) {//Play
				this.addLabel(new GuiNpcLabel(90, data.animation.paused ? "animation.paused" : "animation.stopped", guiLeft - 15, guiTop + 206, 0xFFFFFF));
				if (data.animation.paused) {
					this.addLabel(new GuiNpcLabel(94, "", guiLeft + 21, guiTop + 206, 0xFFFFFF));
				}
				this.addButton(new GuiTexturedButton(91, "", guiLeft + 35, guiTop + 200, 11, 20, animTexture, 18, 71));
			} else {//Pause
				this.addLabel(new GuiNpcLabel(90, "animation.playing", guiLeft - 15, guiTop + 206, 0xFFFFFF));
				this.addLabel(new GuiNpcLabel(94, "", guiLeft + 20, guiTop + 206, 0xFFFFFF));
				this.addButton(new GuiTexturedButton(92, "", guiLeft + 35, guiTop + 200, 14, 20, animTexture, 0, 71));
			}
			if (this.playingAnimation) {//Stop
				this.addButton(new GuiTexturedButton(93, "", guiLeft + 55, guiTop + 200, 14, 20, animTexture, 33, 71));
			}
		}
    }

	@Override
	public void drawScreen(int i, int j, float f) {
		super.drawScreen(i, j, f);
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

		AnimationData data = npc.display.animationData;

		if (guibutton.id == 91) {
			if (!this.playingAnimation || !data.isActive()) {
				animation.currentFrame = 0;
				animation.currentFrameTime = 0;
				for (Frame frame : animation.frames) {
					for (FramePart framePart : frame.frameParts.values()) {
						framePart.prevRotations = new float[]{0,0,0};
						framePart.prevPivots = new float[]{0, 0, 0};
					}
				}
			}
			this.playingAnimation = true;
			data.animation = animation;
			data.animation.paused = false;
		} else if (guibutton.id == 92) {
			data.animation.paused = true;
		} else if (guibutton.id == 93) {
			this.playingAnimation = false;
			data.animation.paused = false;
		}
    }

	@Override
	public void setGuiData(NBTTagCompound compound) {
		this.animation = new Animation();
		animation.readFromNBT(compound);
		setSelected(animation.name);
		this.playingAnimation = false;
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
