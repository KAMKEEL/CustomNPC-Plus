package noppes.npcs.client.gui;

import net.minecraft.client.gui.GuiButton;
import noppes.npcs.Resistances;
import noppes.npcs.client.gui.util.*;

public class SubGuiNpcResistanceProperties extends SubGuiInterface implements ISliderListener
{
	private Resistances resistances;
    public SubGuiNpcResistanceProperties(Resistances resistances)
    {
    	this.resistances = resistances;
		setBackground("menubg.png");
		xSize = 256;
		ySize = 216;
		closeOnEsc = true;
    }

    public void initGui()
    {
        super.initGui();
        int y = guiTop + 10;
        addLabel(new GuiNpcLabel(0,"enchantment.knockback", guiLeft + 4, y + 5));
        addSlider(new GuiNpcSlider(this, 0, guiLeft + 94, y, (int)(resistances.knockback * 100 - 100)  + "%", resistances.knockback / 2));

        y += 22;

        addLabel(new GuiNpcLabel(1,"item.arrow.name", guiLeft + 4, y + 5));
        addSlider(new GuiNpcSlider(this, 1, guiLeft + 94, y, (int)(resistances.arrow * 100 - 100)  + "%", resistances.arrow / 2));
        getSlider(1).enabled = !resistances.disableDamage;

        y += 22;

        addLabel(new GuiNpcLabel(2,"stats.melee", guiLeft + 4, y + 5));
        addSlider(new GuiNpcSlider(this, 2, guiLeft + 94, y, (int)(resistances.playermelee * 100 - 100)  + "%", resistances.playermelee / 2));
        getSlider(2).enabled = !resistances.disableDamage;

        y += 22;

        addLabel(new GuiNpcLabel(3,"stats.explosion", guiLeft + 4, y + 5));
        addSlider(new GuiNpcSlider(this, 3, guiLeft + 94, y, (int)(resistances.explosion * 100 - 100)  + "%", resistances.explosion / 2));
        getSlider(3).enabled = !resistances.disableDamage;

        y += 22;

        addLabel(new GuiNpcLabel(21,"stats.disabledamage", guiLeft + 4, y + 5));
        this.addButton(new GuiNpcButton(21, guiLeft + 94, y, 56, 20, new String[]{"gui.no","gui.yes"}, this.resistances.disableDamage? 1:0));

		addButton(new GuiNpcButton(66, guiLeft + 190, guiTop + 190, 60, 20, "gui.done"));
    }

	protected void actionPerformed(GuiButton guibutton)
    {
		int id = guibutton.id;
        if(id == 21){
            resistances.disableDamage = ((GuiNpcButton)guibutton).getValue() == 1;
            initGui();
        }
        if(id == 66)
        {
        	close();
        }
    }

	@Override
	public void mouseDragged(GuiNpcSlider slider) {
		slider.displayString = (int)(slider.sliderValue * 200 - 100) + "%";
	}

	@Override
	public void mousePressed(GuiNpcSlider slider) {


	}

	@Override
	public void mouseReleased(GuiNpcSlider slider) {
		if(slider.id == 0){
			resistances.knockback = slider.sliderValue * 2;
		}
		if(slider.id == 1){
			resistances.arrow = slider.sliderValue * 2;
		}
		if(slider.id == 2){
			resistances.playermelee = slider.sliderValue * 2;
		}
		if(slider.id == 3){
			resistances.explosion = slider.sliderValue * 2;
		}

	}

}
