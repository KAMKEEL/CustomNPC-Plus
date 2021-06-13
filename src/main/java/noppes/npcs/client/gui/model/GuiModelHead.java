package noppes.npcs.client.gui.model;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import noppes.npcs.ModelPartData;
import noppes.npcs.client.gui.util.GuiModelInterface;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.entity.EntityCustomNpc;

public class GuiModelHead extends GuiModelInterface{

	private GuiScreen parent;

	private final String[] arrHead = new String[]{"gui.no","gui.yes"};
	private final String[] arrHeadwear = new String[]{"gui.no","gui.yes","Solid"};
	private final String[] arrHair = new String[]{"gui.no","Player","Long","Thin","Stylish","Ponytail"};
	private final String[] arrBeard = new String[]{"gui.no","Player","Standard","Viking","Long","Short"};
	private final String[] arrMohawk = new String[]{"gui.no","Type1","Type2","Type3"};
    private final String[] arrSnout = new String[]{"gui.no","Player Small","Player Medium", "Player Large", "Player Bunny",
            "Small1", "Medium1", "Large1", "Bunny1", "Beak1"};
	private final String[] arrEars = new String[]{"gui.no","Player","Player Bunny","Bunny","Type1"};
	private final String[] arrHorns = new String[]{"gui.no", "Player Bull", "Player Antlers", "Player AntennasB", "Player AntennasF", "Bull", "Antlers", "AntennasB", "AntennasF" };

	public GuiModelHead(GuiScreen parent, EntityCustomNpc npc){
		super(npc);
		this.parent = parent;
		this.xOffset = 60;
	}

    @Override
    public void initGui() {
		super.initGui();

		int y = guiTop + 20;

		addButton(new GuiNpcButton(30, guiLeft + 50, y += 22, 70, 20, arrHead, playerdata.hideHead));
		addLabel(new GuiNpcLabel(30, "Hide", guiLeft, y + 5, 0xFFFFFF));

		addButton(new GuiNpcButton(0, guiLeft + 50, y += 22, 70, 20, arrHeadwear, playerdata.headwear));
		addLabel(new GuiNpcLabel(0, "Headwear", guiLeft, y + 5, 0xFFFFFF));

		ModelPartData hair = playerdata.getPartData("hair");
		addButton(new GuiNpcButton(1, guiLeft + 50, y += 22, 70, 20, arrHair, hair == null ? 0 : hair.type + 1));
		addLabel(new GuiNpcLabel(1, "Hair", guiLeft, y + 5, 0xFFFFFF));
		if (hair != null)
			addButton(new GuiNpcButton(11, guiLeft + 122, y, 40, 20, hair.getColor()));

		ModelPartData mohawk = playerdata.getPartData("mohawk");
		addButton(new GuiNpcButton(2, guiLeft + 50, y += 22, 70, 20, arrMohawk, mohawk == null ? 0 : mohawk.type));
		addLabel(new GuiNpcLabel(2, "Mohawk", guiLeft, y + 5, 0xFFFFFF));
		if (mohawk != null)
			addButton(new GuiNpcButton(12, guiLeft + 122, y, 40, 20, mohawk.getColor()));

		ModelPartData beard = playerdata.getPartData("beard");
		addButton(new GuiNpcButton(3, guiLeft + 50, y += 22, 70, 20, arrBeard, beard == null ? 0 : beard.type + 1));
		addLabel(new GuiNpcLabel(3, "Beard", guiLeft, y + 5, 0xFFFFFF));
		if (beard != null)
			addButton(new GuiNpcButton(13, guiLeft + 122, y, 40, 20, beard.getColor()));

		ModelPartData snout = playerdata.getPartData("snout");
		addButton(new GuiNpcButton(4, guiLeft + 50, y += 22, 70, 20, arrSnout, snout == null ? 0 : snout.type + (snout.playerTexture ? 1 : 5)));
		addLabel(new GuiNpcLabel(4, "Snout", guiLeft, y + 5, 0xFFFFFF));
		if (snout != null)
			addButton(new GuiNpcButton(14, guiLeft + 122, y, 40, 20, snout.getColor()));

		ModelPartData ears = playerdata.getPartData("ears");
		addButton(new GuiNpcButton(5, guiLeft + 50, y += 22, 70, 20, arrEars, getEars(ears)));
		addLabel(new GuiNpcLabel(5, "Ears", guiLeft, y + 5, 0xFFFFFF));
		if (ears != null)
			addButton(new GuiNpcButton(15, guiLeft + 122, y, 40, 20, ears.getColor()));

		ModelPartData horns = playerdata.getPartData("horns");
		y += 22;addButton(new GuiNpcButton(6, guiLeft + 50, y, 70, 20, arrHorns, getHorns(horns)));
		addLabel(new GuiNpcLabel(6, "Horns", guiLeft, y + 5, 16777215));
		if (horns != null) {
			addButton(new GuiNpcButton(16, guiLeft + 122, y, 40, 20, horns.getColor()));
		}
    }


    private int getEars(ModelPartData data) {
    	if(data == null)
    		return 0;
    	if(data.playerTexture && data.type == 0)
    		return 1;
    	if(data.playerTexture && data.type == 1)
    		return 2;
    	if(data.type == 0)
    		return 4;
    	if(data.type == 1)
    		return 3;
    	
    	return 0;
	}

	private int getHorns(ModelPartData data) {
		if (data == null)
			return 0;
		if (data.playerTexture) {
			return data.type + 1;
		}
		return data.type + 5;
	}

	@Override
    protected void actionPerformed(GuiButton btn) {
    	super.actionPerformed(btn);
    	GuiNpcButton button = (GuiNpcButton) btn;
    	
    	if(button.id == 0){
    		playerdata.headwear = (byte) button.getValue();
    	}

		if(button.id == 30){
			playerdata.hideHead = (byte) button.getValue();
		}

    	if(button.id == 1){
    		if(button.getValue() == 0)
    			playerdata.removePart("hair");
    		else{
    			ModelPartData data = playerdata.getOrCreatePart("hair");
    			if(button.getValue() > 1)
    				data.setTexture("hair/hair" + (button.getValue() - 1), button.getValue() - 1);
    		}
    		initGui();
    	}

    	if(button.id == 2){
    		if(button.getValue() == 0)
    			playerdata.removePart("mohawk");
    		else{
    			ModelPartData data = playerdata.getOrCreatePart("mohawk");
    			if(button.getValue() > 0)
    				data.setTexture("hair/mohawk" + button.getValue(),button.getValue());
    		}
    		initGui();
    	}

    	if(button.id == 3){
    		if(button.getValue() == 0)
    			playerdata.removePart("beard");
    		else{
    			ModelPartData data = playerdata.getOrCreatePart("beard");
    			if(button.getValue() > 1)
    				data.setTexture("beard/beard" + (button.getValue() - 1),button.getValue() - 1);
    		}
    		initGui();
    	}

        if(button.id == 4){
            if(button.getValue() == 0)
                playerdata.removePart("snout");
            else if(button.getValue() < 5){
                ModelPartData data = playerdata.getOrCreatePart("snout");
                data.type = (byte) (button.getValue() - 1);
            }
            else{
                ModelPartData data = playerdata.getOrCreatePart("snout");
                byte type = 0;
                if(button.displayString.startsWith("Medium"))
                    type = 1;
                if(button.displayString.startsWith("Large"))
                    type = 2;
                if(button.displayString.startsWith("Bunny"))
                    type = 3;
                if(button.displayString.startsWith("Beak"))
                    type = 4;
                data.setTexture("snout/" + button.displayString.toLowerCase(), type);
            }
            initGui();
        }

    	if(button.id == 5){
    		int value = button.getValue();
    		if(value == 0)
    			playerdata.removePart("ears");
    		else{
    			ModelPartData data = playerdata.getOrCreatePart("ears");
    			if(value == 1)
    				data.setTexture("", 0);
    			if(value == 2)
    				data.setTexture("", 1);
    			if(value == 3)
    				data.setTexture("ears/bunny1", 1);
    			if(value == 4)
    				data.setTexture("ears/type1", 0);
    		}
    		initGui();
    	}

        if(button.id == 6){
            int value = button.getValue();
            if(value == 0)
                playerdata.removePart("horns");
            else{
                ModelPartData data = playerdata.getOrCreatePart("horns");
                if(value <= 4)
                    data.setTexture("", value - 1);
                if(value == 5)
                    data.setTexture("horns/bull", 0);
                if(value == 6)
                    data.setTexture("horns/antlers", 1);
                if(value == 7)
                    data.setTexture("horns/antennas", 2);
                if(value == 8)
                    data.setTexture("horns/antennas", 3);
            }
            initGui();
        }

    	if(button.id == 11){
    		this.mc.displayGuiScreen(new GuiModelColor(this, playerdata.getPartData("hair"), npc));
    	}
    	if(button.id == 12){
    		this.mc.displayGuiScreen(new GuiModelColor(this, playerdata.getPartData("mohawk"), npc));
    	}
    	if(button.id == 13){
    		this.mc.displayGuiScreen(new GuiModelColor(this, playerdata.getPartData("beard"), npc));
    	}
    	if(button.id == 14){
    		this.mc.displayGuiScreen(new GuiModelColor(this, playerdata.getPartData("snout"), npc));
    	}
    	if(button.id == 15){
    		this.mc.displayGuiScreen(new GuiModelColor(this, playerdata.getPartData("ears"), npc));
    	}
        if(button.id == 16){
            this.mc.displayGuiScreen(new GuiModelColor(this, playerdata.getPartData("horns"), npc));
        }
    }

    @Override
    public void close(){
        this.mc.displayGuiScreen(parent);
    }
}
