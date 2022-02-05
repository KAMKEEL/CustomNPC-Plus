package noppes.npcs.client.gui.roles;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.ModelData;
import noppes.npcs.ModelPartConfig;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.util.GuiModelInterface;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcSlider;
import noppes.npcs.client.gui.util.ISliderListener;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.roles.JobPuppet;
import noppes.npcs.roles.JobPuppet.PartConfig;

public class GuiNpcPuppet extends GuiModelInterface implements ISliderListener{

	private GuiScreen parent;
	private int type = 6;
	private JobPuppet job;
	private PartConfig part;
	
	public GuiNpcPuppet(GuiScreen parent, EntityCustomNpc npc){
		super(npc);
		this.parent = parent;
		this.xOffset = 100;
		ySize = 230;
		job = (JobPuppet) npc.jobInterface;
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
			drawSlider(y, job.head);
			y += 90;
		}
		else{
			addButton(new GuiNpcButton(0, guiLeft + 110, y , 60, 20, "selectServer.edit"));
			y += 24;
		}
		
		addLabel(new GuiNpcLabel(21, "model.body", guiLeft + 55, y + 5, 0xFFFFFF));
		if(type == 1){
			drawSlider(y, job.body);
			y += 90;
		}
		else{
			addButton(new GuiNpcButton(1, guiLeft + 110, y , 60, 20, "selectServer.edit"));
			y += 24;
		}
		
		addLabel(new GuiNpcLabel(22, "model.larm", guiLeft + 55, y + 5, 0xFFFFFF));
		if(type == 2){
			drawSlider(y, job.larm);
			y += 90;
		}
		else{
			addButton(new GuiNpcButton(2, guiLeft + 110, y , 60, 20, "selectServer.edit"));
			y += 24;
		}
		
		addLabel(new GuiNpcLabel(23, "model.rarm", guiLeft + 55, y + 5, 0xFFFFFF));
		if(type == 3){
			drawSlider(y, job.rarm);
			y += 90;
		}
		else{
			addButton(new GuiNpcButton(3, guiLeft + 110, y , 60, 20, "selectServer.edit"));
			y += 24;
		}
		
		addLabel(new GuiNpcLabel(24, "model.lleg", guiLeft + 55, y + 5, 0xFFFFFF));
		if(type == 4){
			drawSlider(y, job.lleg);
			y += 90;
		}
		else{
			addButton(new GuiNpcButton(4, guiLeft + 110, y , 60, 20, "selectServer.edit"));
			y += 24;
		}
		
		addLabel(new GuiNpcLabel(25, "model.rleg", guiLeft + 55, y + 5, 0xFFFFFF));
		if(type == 5){
			drawSlider(y, job.rleg);
			y += 90;
		}
		else{
			addButton(new GuiNpcButton(5, guiLeft + 110, y , 60, 20, "selectServer.edit"));
			y += 24;
		}

    }
    
    private void drawSlider(int y, PartConfig config){
    	part = config;
		addButton(new GuiNpcButton(29, guiLeft + 100, y , 80, 20, new String[]{"gui.enabled","gui.disabled"}, config.disabled?1:0));
		y += 22;
		addLabel(new GuiNpcLabel(10, "X", guiLeft, y + 5, 0xFFFFFF));
		addSlider(new GuiNpcSlider(this, 10, guiLeft + 50, y, config.rotationX + 0.5f));
		y += 22;
		addLabel(new GuiNpcLabel(11, "Y", guiLeft, y + 5, 0xFFFFFF));
		addSlider(new GuiNpcSlider(this, 11, guiLeft + 50, y, config.rotationY + 0.5f));
		y += 22;
		addLabel(new GuiNpcLabel(12, "Z", guiLeft, y + 5, 0xFFFFFF));
		addSlider(new GuiNpcSlider(this, 12, guiLeft + 50, y, config.rotationZ + 0.5f));
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
    		part.disabled = button.getValue() == 1;
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
