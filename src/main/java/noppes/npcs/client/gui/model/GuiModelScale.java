package noppes.npcs.client.gui.model;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import noppes.npcs.ModelData;
import noppes.npcs.ModelPartConfig;
import noppes.npcs.client.gui.util.GuiModelInterface;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcSlider;
import noppes.npcs.client.gui.util.ISliderListener;
import noppes.npcs.entity.EntityCustomNpc;

public class GuiModelScale extends GuiModelInterface implements ISliderListener{

	private GuiScreen parent;
	private int type = 0;
	public GuiModelScale(GuiScreen parent, ModelData data, EntityCustomNpc npc){
		super(npc);
		this.parent = parent;
		this.xOffset = 100;
		ySize = 230;
	}

    @Override
    public void initGui() {
    	super.initGui();

		int y = guiTop + 2;

		addLabel(new GuiNpcLabel(20, "Head", guiLeft + 55, y + 5, 0xFFFFFF));
		if(type == 0){
			drawSlider(y, playerdata.head);
			y += 88;
		}
		else{
			addButton(new GuiNpcButton(0, guiLeft + 110, y , 60, 20, "Edit"));
			y += 24;
		}
		
		addLabel(new GuiNpcLabel(21, "Body", guiLeft + 55, y + 5, 0xFFFFFF));
		if(type == 1){
			drawSlider(y, playerdata.body);
			y += 88;
		}
		else{
			addButton(new GuiNpcButton(1, guiLeft + 110, y , 60, 20, "Edit"));
			y += 24;
		}
		
		addLabel(new GuiNpcLabel(22, "Arms", guiLeft + 55, y + 5, 0xFFFFFF));
		if(type == 2){
			drawSlider(y, playerdata.arms);
			y += 88;
		}
		else{
			addButton(new GuiNpcButton(2, guiLeft + 110, y , 60, 20, "Edit"));
			y += 24;
		}
		
		addLabel(new GuiNpcLabel(23, "Legs", guiLeft + 55, y + 5, 0xFFFFFF));
		if(type == 3){
			drawSlider(y, playerdata.legs);
			y += 88;
		}
		else{
			addButton(new GuiNpcButton(3, guiLeft + 110, y , 60, 20, "Edit"));
			y += 24;
		}

    }
    
    private void drawSlider(int y, ModelPartConfig config){
		y += 15;
		addLabel(new GuiNpcLabel(10, "Width", guiLeft, y + 5, 0xFFFFFF));
		addSlider(new GuiNpcSlider(this, 10, guiLeft + 50, y, config.scaleX - 0.5f));
		y += 22;
		addLabel(new GuiNpcLabel(11, "Height", guiLeft, y + 5, 0xFFFFFF));
		addSlider(new GuiNpcSlider(this, 11, guiLeft + 50, y, config.scaleY - 0.5f));
		y += 22;
		addLabel(new GuiNpcLabel(12, "Depth", guiLeft, y + 5, 0xFFFFFF));
		addSlider(new GuiNpcSlider(this, 12, guiLeft + 50, y, config.scaleZ - 0.5f));
    }


    @Override
    protected void actionPerformed(GuiButton btn) {
    	super.actionPerformed(btn);
    	if(btn.id < 4){
    		type = btn.id;
    		initGui();
    	}
    }

    @Override
    public void close(){
        this.mc.displayGuiScreen(parent);
    }

	@Override
	public void mouseDragged(GuiNpcSlider slider) {
		int percent = (int) (50 + slider.sliderValue * 100);
		slider.setString(percent + "%");
		ModelPartConfig config = playerdata.head;
		if(type == 1)
			config = playerdata.body;
		else if(type == 2)
			config = playerdata.arms;
		else if(type == 3)
			config = playerdata.legs;
		
		if(slider.id == 10)
			config.scaleX = slider.sliderValue + 0.5f;
		if(slider.id == 11)
			config.scaleY = slider.sliderValue + 0.5f;
		if(slider.id == 12)
			config.scaleZ = slider.sliderValue + 0.5f;
		npc.updateHitbox();
	}

	@Override
	public void mousePressed(GuiNpcSlider slider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(GuiNpcSlider slider) {
	}
}
