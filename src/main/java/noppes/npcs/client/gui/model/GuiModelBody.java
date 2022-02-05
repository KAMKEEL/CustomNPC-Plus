package noppes.npcs.client.gui.model;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import noppes.npcs.ModelPartData;
import noppes.npcs.client.gui.util.GuiModelInterface;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.entity.EntityCustomNpc;

public class GuiModelBody extends GuiModelInterface{

	private GuiScreen parent;
	private final String[] arrWing = new String[]{"gui.no","Player","Type1","Type2","Type3"};
	private final String[] arrBreasts = new String[]{"gui.no","Type1","Type2","Type3"};
	private final String[] arrParticles = new String[]{"gui.no","Player","Type1","Type2", "Rainbow"};
	private final String[] arrfins = new String[]{"gui.no","Player","Type1"};
	
	public GuiModelBody(GuiScreen parent, EntityCustomNpc npc){
		super(npc);
		this.parent = parent;
		this.xOffset = 60;
	}

    @Override
    public void initGui() {
    	super.initGui();
		int y = guiTop + 20;
    	addButton(new GuiNpcButton(1, guiLeft + 50, y += 22, 70, 20, arrBreasts, playerdata.breasts));
		addLabel(new GuiNpcLabel(1, "Breasts", guiLeft, y + 5, 0xFFFFFF));

		ModelPartData wing = playerdata.getPartData("wings");
    	addButton(new GuiNpcButton(0, guiLeft + 50, y += 22, 70, 20, arrWing, wing == null?0:wing.type + 1));
		addLabel(new GuiNpcLabel(0, "Wings", guiLeft, y + 5, 0xFFFFFF));
		if(wing != null)
			addButton(new GuiNpcButton(11, guiLeft + 122, y, 40, 20, wing.getColor()));

		ModelPartData particles = playerdata.getPartData("particles");
    	addButton(new GuiNpcButton(2, guiLeft + 50, y += 22, 70, 20, arrParticles, getParticleIndex(particles)));
		addLabel(new GuiNpcLabel(2, "Particles", guiLeft, y + 5, 0xFFFFFF));
		if(particles != null && particles.type != 1)
			addButton(new GuiNpcButton(12, guiLeft + 122, y, 40, 20, particles.getColor()));

		ModelPartData fin = playerdata.getPartData("fin");
    	addButton(new GuiNpcButton(3, guiLeft + 50, y += 22, 70, 20, arrfins, getFinIndex(fin)));
		addLabel(new GuiNpcLabel(3, "Fin", guiLeft, y + 5, 0xFFFFFF));
		if(fin != null)
			addButton(new GuiNpcButton(13, guiLeft + 122, y, 40, 20, fin.getColor()));

    }


    private int getFinIndex(ModelPartData fin) {
    	if(fin == null)
    		return 0;
		return fin.playerTexture?1:2;
	}

	private int getParticleIndex(ModelPartData particles) {
    	if(particles == null)
    		return 0;
    	if(particles.type == 0){
    		if(particles.playerTexture)
    			return 1;
    		if(particles.texture.contains("1"))
    			return 2;
    		if(particles.texture.contains("2"))
    			return 3;
    	}
    	if(particles.type == 1){
    		return 4;
    	}
    				
		return 0;
	}

	@Override
    protected void actionPerformed(GuiButton btn) {
    	super.actionPerformed(btn);
    	GuiNpcButton button = (GuiNpcButton) btn;

    	if(button.id == 0){
    		if(button.getValue() == 0)
    			playerdata.removePart("wings");
    		else{
    			ModelPartData data = playerdata.getOrCreatePart("wings");
    			if(button.getValue() > 1)
    				data.setTexture("wings/wing" + (button.getValue() - 1), button.getValue() - 1);
    		}
    		initGui();
    	}
    	if(button.id == 1){
    		playerdata.breasts = (byte) button.getValue();
    	}
    	if(button.id == 2){
    		int value = button.getValue();
    		if(value == 0)
    			playerdata.removePart("particles");
    		else{
    			ModelPartData particles = playerdata.getOrCreatePart("particles");
	    		if(value == 1)
	    			particles.setTexture("", 0);
	    		if(value == 2)
	    			particles.setTexture("particle/type1", 0);
	    		if(value == 3)
	    			particles.setTexture("particle/type2", 0);
	    		if(value == 4)
	    			particles.setTexture("", 1);
    		}
    		initGui();
    	}
    	if(button.id == 3){
    		int value = button.getValue();
    		if(value == 0)
    			playerdata.removePart("fin");
    		else{
    			ModelPartData particles = playerdata.getOrCreatePart("fin");
	    		if(value == 1)
	    			particles.setTexture("", 0);
	    		if(value == 2)
	    			particles.setTexture("fin/fin1", 0);
    		}
    		initGui();
    	}

    	if(button.id == 11){
    		this.mc.displayGuiScreen(new GuiModelColor(this, playerdata.getPartData("wings"), npc));
    	}
    	if(button.id == 12){
    		this.mc.displayGuiScreen(new GuiModelColor(this, playerdata.getPartData("particles"), npc));
    	}
    	if(button.id == 13){
    		this.mc.displayGuiScreen(new GuiModelColor(this, playerdata.getPartData("fin"), npc));
    	}
    }
    
    @Override
    public void close(){
        this.mc.displayGuiScreen(parent);
    }
}
