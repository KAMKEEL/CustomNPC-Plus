package noppes.npcs.client.gui.roles;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.AnimationData;
import noppes.npcs.AnimationPartConfig;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.entity.EntityCustomNpc;

public class GuiNpcPuppet extends GuiModelInterface implements ISliderListener, ITextfieldListener {

	private GuiScreen parent;
	private int type = 6;
	private AnimationData job;
	private AnimationPartConfig part;

	private GuiNpcSlider rotateX;
	private GuiNpcSlider rotateY;
	private GuiNpcSlider rotateZ;
	
	public GuiNpcPuppet(GuiScreen parent, EntityCustomNpc npc){
		super(npc);
		this.parent = parent;
		this.xOffset = 100;
		ySize = 230;
		job = npc.display.animationData;
	}

    @Override
    public void initGui() {
    	super.initGui();

		int y = guiTop;
		addLabel(new GuiNpcLabel(26, "gui.settings", guiLeft + 55, y + 5, 0xFFFFFF));
		if(type == 6){
			addButton(new GuiNpcButton(30, guiLeft + 120, y += 14, 60, 20, new String[]{"gui.yes", "gui.no"}, job.whileStanding?0:1));
			addLabel(new GuiNpcLabel(30, "puppet.standing", guiLeft + 30, y + 5, 0xFFFFFF));
			addButton(new GuiNpcButton(31, guiLeft + 120, y += 22, 60, 20, new String[]{"gui.yes", "gui.no"}, job.whileMoving?0:1));
			addLabel(new GuiNpcLabel(31, "puppet.walking", guiLeft + 30, y + 5, 0xFFFFFF));
			addButton(new GuiNpcButton(32, guiLeft + 120, y += 22, 60, 20, new String[]{"gui.yes", "gui.no"}, job.whileAttacking?0:1));
			addLabel(new GuiNpcLabel(32, "puppet.attacking", guiLeft + 30, y + 5, 0xFFFFFF));

			y += 24;
		}
		else{
			addButton(new GuiNpcButton(6, guiLeft + 110, y , 60, 20, "selectServer.edit"));
			y += 24;
		}
		
		addLabel(new GuiNpcLabel(20, "model.head", guiLeft + 55, y + 5, 0xFFFFFF));
		if(type == 0){
			y += drawSlider(y, job.head);
		}
		else{
			addButton(new GuiNpcButton(0, guiLeft + 110, y , 60, 20, "selectServer.edit"));
			y += 24;
		}
		
		addLabel(new GuiNpcLabel(21, "model.body", guiLeft + 55, y + 5, 0xFFFFFF));
		if(type == 1){
			y += drawSlider(y, job.body);
		}
		else{
			addButton(new GuiNpcButton(1, guiLeft + 110, y , 60, 20, "selectServer.edit"));
			y += 24;
		}
		
		addLabel(new GuiNpcLabel(22, "model.larm", guiLeft + 55, y + 5, 0xFFFFFF));
		if(type == 2){
			y += drawSlider(y, job.larm);
		}
		else{
			addButton(new GuiNpcButton(2, guiLeft + 110, y , 60, 20, "selectServer.edit"));
			y += 24;
		}
		
		addLabel(new GuiNpcLabel(23, "model.rarm", guiLeft + 55, y + 5, 0xFFFFFF));
		if(type == 3){
			y += drawSlider(y, job.rarm);
		}
		else{
			addButton(new GuiNpcButton(3, guiLeft + 110, y , 60, 20, "selectServer.edit"));
			y += 24;
		}
		
		addLabel(new GuiNpcLabel(24, "model.lleg", guiLeft + 55, y + 5, 0xFFFFFF));
		if(type == 4){
			y += drawSlider(y, job.lleg);;
		}
		else{
			addButton(new GuiNpcButton(4, guiLeft + 110, y , 60, 20, "selectServer.edit"));
			y += 24;
		}
		
		addLabel(new GuiNpcLabel(25, "model.rleg", guiLeft + 55, y + 5, 0xFFFFFF));
		if(type == 5){
			y += drawSlider(y, job.rleg);;
		}
		else{
			addButton(new GuiNpcButton(5, guiLeft + 110, y , 60, 20, "selectServer.edit"));
			y += 24;
		}

    }
    
    private float drawSlider(int y, AnimationPartConfig config){
    	part = config;
		addButton(new GuiNpcButton(29, guiLeft + 100, y , 80, 20, new String[]{"gui.enabled","gui.disabled"}, !config.enablePart?1:0));
		y += 22;
		addLabel(new GuiNpcLabel(10, "X", guiLeft, y + 5, 0xFFFFFF));
		rotateX = new GuiNpcSlider(this, 10, guiLeft + 50, y, config.rotationX + 0.5f);
		addSlider(rotateX);
		addButton(new GuiNpcButton(170, guiLeft + 8, y, 40, 20, "Reset"));
		y += 22;
		addLabel(new GuiNpcLabel(11, "Y", guiLeft, y + 5, 0xFFFFFF));
		rotateY = new GuiNpcSlider(this, 11, guiLeft + 50, y, config.rotationY + 0.5f);
		addSlider(rotateY);
		addButton(new GuiNpcButton(171, guiLeft + 8, y, 40, 20, "Reset"));
		y += 22;
		addLabel(new GuiNpcLabel(12, "Z", guiLeft, y + 5, 0xFFFFFF));
		rotateZ = new GuiNpcSlider(this, 12, guiLeft + 50, y, config.rotationZ + 0.5f);
		addSlider(rotateZ);
		addButton(new GuiNpcButton(172, guiLeft + 8, y, 40, 20, "Reset"));

		addTextField(new GuiNpcTextField(15, this, guiLeft + 60, y += 22, 40, 20, "" + config.pivotX));
		addLabel(new GuiNpcLabel(15, "puppet.pivotX", guiLeft, y + 5, 0xFFFFFF));
		getTextField(15).floatsOnly = true;
		getTextField(15).setMinMaxDefaultFloat(-Float.MAX_VALUE,Float.MAX_VALUE,0);

		addTextField(new GuiNpcTextField(16, this, guiLeft + 60, y += 22, 40, 20, "" + config.pivotY));
		addLabel(new GuiNpcLabel(16, "puppet.pivotY", guiLeft, y + 5, 0xFFFFFF));
		getTextField(16).floatsOnly = true;
		getTextField(16).setMinMaxDefaultFloat(-Float.MAX_VALUE,Float.MAX_VALUE,0);

		addTextField(new GuiNpcTextField(17, this, guiLeft + 60, y += 22, 40, 20, "" + config.pivotZ));
		addLabel(new GuiNpcLabel(17, "puppet.pivotZ", guiLeft, y + 5, 0xFFFFFF));
		getTextField(17).floatsOnly = true;
		getTextField(17).setMinMaxDefaultFloat(-Float.MAX_VALUE,Float.MAX_VALUE,0);

		return 160;
	}


    @Override
    protected void actionPerformed(GuiButton btn) {
    	super.actionPerformed(btn);
    	if(btn.id < 7){
    		type = btn.id;
    		initGui();
    	}
    	if(!(btn instanceof GuiNpcButton))
    		return;
    	
    	GuiNpcButton button = (GuiNpcButton) btn;
    	if(btn.id == 29){
    		part.enablePart = button.getValue() == 0;
    	}
    	if(btn.id == 30){
    		job.whileStanding = button.getValue() == 0;
    	}
    	if(btn.id == 31){
    		job.whileMoving = button.getValue() == 0;
    	}
    	if(btn.id == 32){
    		job.whileAttacking = button.getValue() == 0;
    	}
    	if(btn.id == 170 || btn.id == 171 || btn.id == 172){
			if(btn.id == 170){
				part.rotationX = 0.0f;
				rotateX.sliderValue = 0.5f;
				int percent = (int) ((rotateX.sliderValue) * 360);
				rotateX.setString(percent + "%");
			}
			else if(btn.id == 171){
				part.rotationY = 0.0f;
				rotateY.sliderValue = 0.5f;
				int percent = (int) ((rotateY.sliderValue) * 360);
				rotateY.setString(percent + "%");
			}
			else{
				part.rotationZ = 0.0f;
				rotateZ.sliderValue = 0.5f;
				int percent = (int) ((rotateZ.sliderValue) * 360);
				rotateZ.setString(percent + "%");
			}
			npc.updateHitbox();
		}
    }

	@Override
	public void unFocused(GuiNpcTextField textfield) {
		if(textfield.id == 15) {
			part.pivotX = textfield.getFloat();
		}
		if(textfield.id == 16) {
			part.pivotY = textfield.getFloat();
		}
		if(textfield.id == 17) {
			part.pivotZ = textfield.getFloat();
		}
	}

    @Override
    public void close(){
        this.mc.displayGuiScreen(parent);
		Client.sendData(EnumPacketServer.JobSave, job.writeToNBT(new NBTTagCompound()));
    }

	@Override
	public void mouseDragged(GuiNpcSlider slider) {
		int percent = (int) ((slider.sliderValue) * 360);
		slider.setString(percent + "%");
		
		if(slider.id == 10)
			part.rotationX = slider.sliderValue - 0.5f;
		if(slider.id == 11)
			part.rotationY = slider.sliderValue - 0.5f;
		if(slider.id == 12)
			part.rotationZ = slider.sliderValue - 0.5f;
		npc.updateHitbox();
	}

	@Override
	public void mousePressed(GuiNpcSlider slider) {
		
	}

	@Override
	public void mouseReleased(GuiNpcSlider slider) {
	}
}
