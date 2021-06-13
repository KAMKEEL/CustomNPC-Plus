package noppes.npcs.client.gui;

import net.minecraft.client.gui.GuiButton;
import noppes.npcs.DataDisplay;
import noppes.npcs.client.gui.mainmenu.GuiNpcDisplay;
import noppes.npcs.client.gui.util.*;

public class SubGuiNpcName extends SubGuiInterface implements ITextfieldListener {
	private DataDisplay display;
	public SubGuiNpcName(DataDisplay display){
    	this.display = display;
		setBackground("menubg.png");
		xSize = 256;
		ySize = 216;
		closeOnEsc = true;
    }

    @Override
    public void initGui(){
        super.initGui();
        int y = guiTop + 4;
    	addButton(new GuiNpcButton(66, guiLeft + xSize - 24, y, 20, 20, "X"));
    	
        addTextField(new GuiNpcTextField(0,this, fontRendererObj, guiLeft + 4, y += 50, 226, 20, display.name));
    	this.addButton(new GuiButtonBiDirectional(1, guiLeft + 4, y += 22 , 200, 20, new String[] {"markov.roman.name", "markov.japanese.name", "markov.slavic.name", "markov.welsh.name", "markov.sami.name", "markov.oldNorse.name", "markov.ancientGreek.name", "markov.aztec.name", "markov.classicCNPCs.name", "markov.spanish.name"}, display.getMarkovGeneratorId()));
    	
    	this.addButton(new GuiButtonBiDirectional(2, guiLeft + 64, y += 22 , 120, 20, new String[] {"markov.gender.either", "markov.gender.male", "markov.gender.female"}, display.getMarkovGender()));
    	addLabel(new GuiNpcLabel(2,"markov.gender.name", guiLeft + 5, y + 5));
    	
    	addButton(new GuiNpcButton(3, guiLeft + 4, y += 42, 70, 20, "markov.generate"));
    }

	@Override
	public void unFocused(GuiNpcTextField textfield) {
		if(textfield.id == 0){
			if(!textfield.isEmpty())
				display.setName(textfield.getText());
			else
				textfield.setText(display.getName());
		}
	}
    
	@Override
	protected void actionPerformed(GuiButton guibutton){
		GuiNpcButton button = (GuiNpcButton) guibutton;

		if(button.id == 1) {
			display.setMarkovGeneratorId(button.getValue());
		}
		if(button.id == 2) {
			display.setMarkovGender(button.getValue());
		}
		if(button.id == 3) {
			String name = display.getRandomName();
			display.setName(name);
			getTextField(0).setText(name);
			((GuiNpcDisplay)parent).nameText.setText(name);
		}
        if(button.id == 66){
        	close();
        }
    }
}
