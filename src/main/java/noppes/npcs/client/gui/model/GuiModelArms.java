package noppes.npcs.client.gui.model;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import noppes.npcs.ModelPartData;
import noppes.npcs.client.gui.util.GuiModelInterface;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.entity.EntityCustomNpc;

public class GuiModelArms extends GuiModelInterface{

	private final String[] arrParticles = new String[]{"gui.no","Both","Left","Right"};

	private GuiScreen parent;
	public GuiModelArms(GuiScreen parent, EntityCustomNpc npc){
		super(npc);
		this.parent = parent;
		this.xOffset = 60;
	}

    @Override
    public void initGui() {
    	super.initGui();
		int y = guiTop + 20;

		ModelPartData claws = playerdata.getPartData("claws");
    	addButton(new GuiNpcButton(0, guiLeft + 50, y += 22, 70, 20, arrParticles, claws == null?0:claws.type + 1));
		addLabel(new GuiNpcLabel(0, "Claws", guiLeft, y + 5, 0xFFFFFF));
		if(claws != null)
			addButton(new GuiNpcButton(10, guiLeft + 122, y, 40, 20, claws.getColor()));
    }


    @Override
    protected void actionPerformed(GuiButton btn) {
    	super.actionPerformed(btn);
    	GuiNpcButton button = (GuiNpcButton) btn;

    	if(button.id == 0){
    		if(button.getValue() == 0)
    			playerdata.removePart("claws");
    		else{
    			ModelPartData data = playerdata.getOrCreatePart("claws");
    			data.type = (byte) (button.getValue() - 1);
    		}
    		initGui();
    	}
    	if(button.id == 10){
    		this.mc.displayGuiScreen(new GuiModelColor(this, playerdata.getPartData("claws"), npc));
    	}
    }
    
    @Override
    public void close(){
        this.mc.displayGuiScreen(parent);
    }
}
