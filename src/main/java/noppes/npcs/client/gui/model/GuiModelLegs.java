package noppes.npcs.client.gui.model;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import noppes.npcs.ModelPartData;
import noppes.npcs.client.gui.util.GuiModelInterface;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.entity.EntityCustomNpc;

public class GuiModelLegs extends GuiModelInterface{

	private GuiScreen parent;
	private final String[] arrLeg = new String[]{"gui.no","Both","Right","Left"};
	private final String[] arrLegwear = new String[]{"gui.no","Both","Left","Right"};
	private final String[] arrSolidLegwear = new String[]{"gui.no","Both","Left","Right"};
	private final String[] arrLegs = new String[]{"gui.no","Player","Player Naga","Spider","Horse","Naga", "Mermaid", "Mermaid 2", "Digitigrade"};
	private final String[] arrTail = new String[]{"gui.no","Player", "Player Dragon","Cat","Wolf","Horse","Dragon", "Squirrel", "Fin", "Rodent", "Feather"};
	public GuiModelLegs(GuiScreen parent, EntityCustomNpc npc){
		super(npc);
		this.parent = parent;
		this.xOffset = 60;
	}

    @Override
    public void initGui() {
    	super.initGui();
		int y = guiTop + 20;

		addButton(new GuiNpcButton(30, guiLeft + 50, y += 22, 70, 20, arrLeg, playerdata.hideLegs));
		addLabel(new GuiNpcLabel(30, "Hide", guiLeft, y + 5, 0xFFFFFF));

		if (npc.display.modelType == 1 || npc.display.modelType == 2) {
			addButton(new GuiNpcButton(8, guiLeft + 50, y += 22, 70, 20, arrLegwear, playerdata.legwear));
			addLabel(new GuiNpcLabel(8, "Legwear", guiLeft, y + 5, 0xFFFFFF));

			addButton(new GuiNpcButton(9, guiLeft + 50, y += 22, 70, 20, arrSolidLegwear, playerdata.solidLegwear));
			addLabel(new GuiNpcLabel(9, "Solid", guiLeft, y + 5, 0xFFFFFF));
		}

    	addButton(new GuiNpcButton(1, guiLeft + 50, y += 22, 70, 20, arrLegs, getLegIndex(playerdata.legParts)));
		addLabel(new GuiNpcLabel(1, "Legs", guiLeft, y + 5, 0xFFFFFF));
		if(playerdata.legParts.type > 0)
			addButton(new GuiNpcButton(11, guiLeft + 122, y, 40, 20, playerdata.legParts.getColor()));

		ModelPartData tail = playerdata.getPartData("tail");
    	addButton(new GuiNpcButton(2, guiLeft + 50, y += 22, 70, 20, arrTail, getTailIndex(tail)));
		addLabel(new GuiNpcLabel(2, "Tail", guiLeft, y + 5, 0xFFFFFF));
		if(tail != null)
			addButton(new GuiNpcButton(12, guiLeft + 122, y, 40, 20, tail.getColor()));
    }

    private int getLegIndex(ModelPartData data) {
    	if(!data.playerTexture && data.type == 1)
    		return 5;
		if(data.type == 4)
			return 6;
		if(data.type == 5)
			return 8;
		return data.type + 1;
	}

	private int getTailIndex(ModelPartData data){
		if(data == null)
			return 0;
		if(data.playerTexture && data.type == 0)
			return 1;
		if(data.type == 0 && data.texture.contains("tail1"))
			return 3;
		if(data.type == 0 && data.texture.contains("tail2"))
			return 4;
		if(data.playerTexture && data.type == 1)
			return 2;
		if(data.type == 1)
			return 6;
		if(data.type == 2)
			return 5;
		if(data.type == 3)
			return 7;
		if(data.type == 4)
			return 8;
		if(data.type == 5)
			return 9;
		if(data.type == 6)
			return 10;
    	
    	return 0;
    }

    @Override
    protected void actionPerformed(GuiButton btn) {
    	super.actionPerformed(btn);
    	GuiNpcButton button = (GuiNpcButton) btn;

		if(button.id == 8){
			playerdata.legwear = (byte) button.getValue();
		}
		if(button.id == 9){
			playerdata.solidLegwear = (byte) button.getValue();
		}
		if(button.id == 30){
			playerdata.hideLegs = (byte) button.getValue();
		}
    	if(button.id == 1){
    		ModelPartData data = playerdata.legParts;
    		int value = button.getValue() - 1;
    		if(value < 1)
    			data.color = 0xFFFFFF;
    		if(value < 2){
    			data.setTexture("", value);
    		}
    		if(value == 2)
    			data.setTexture("legs/spider1", 2);
    		if(value == 3)
    			data.setTexture("legs/horse1", 3);
    		if(value == 4)
    			if (npc.display.modelType == 1 || npc.display.modelType == 2){
					data.setTexture("legs/naga2", 1);
				}
    			else{
					data.setTexture("legs/naga1", 1);
				}
    		if(value == 5)
    			data.setTexture("legs/mermaid1", 4);
			if(value == 6)
				data.setTexture("legs/mermaid1", 6);
    		if(value == 7)
    			data.setTexture("", 5);

    		initGui();

    	}

			if(button.id == 2){
			int value = button.getValue();
			if(value == 0)
				playerdata.removePart("tail");
			else{
				ModelPartData data = playerdata.getOrCreatePart("tail");
				if(value == 1)
					data.setTexture("", 0);
				if(value == 2)
					data.setTexture("", 1);
				if(value == 3)
					data.setTexture("tail/tail1", 0);
				if(value == 4)
					data.setTexture("tail/tail2", 0);
				if(value == 5)
					data.setTexture("tail/horse1", 2);
				if(value == 6)
					data.setTexture("tail/dragon1", 1);
				if(value == 7)
					data.setTexture("tail/squirrel1", 3);
				if(value == 8)
					data.setTexture("tail/fin1", 4);
				if(value == 9)
					data.setTexture("tail/rodent1", 5);
				if(value == 10)
					data.setTexture("tail/feather1", 6);

			}
			initGui();
		}
    	if(button.id == 11){
    		this.mc.displayGuiScreen(new GuiModelColor(this, playerdata.legParts, npc));
    	}

    	if(button.id == 12){
    		this.mc.displayGuiScreen(new GuiModelColor(this, playerdata.getPartData("tail"), npc));
    	}
    }
    
    @Override
    public void close(){
        this.mc.displayGuiScreen(parent);
    }
}
