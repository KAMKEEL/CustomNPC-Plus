package noppes.npcs.client.gui;

import net.minecraft.client.gui.GuiButton;
import noppes.npcs.DataStats;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcButtonYesNo;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;

public class SubGuiNpcRangeProperties extends SubGuiInterface implements ITextfieldListener
{
	private DataStats stats;
	private GuiNpcSoundSelection gui;
	
    public SubGuiNpcRangeProperties(DataStats stats)
    {
    	this.stats = stats;
		setBackground("menubg.png");
		xSize = 256;
		ySize = 216;
		closeOnEsc = true;
    }

    public void initGui(){
        super.initGui();
        int y = guiTop + 4;
        addTextField(new GuiNpcTextField(1,this, fontRendererObj, guiLeft + 85, y, 50, 18, stats.accuracy+""));
        addLabel(new GuiNpcLabel(1,"stats.accuracy", guiLeft + 5, y + 5));
        getTextField(1).numbersOnly = true;
        getTextField(1).setMinMaxDefault(0, 100, 90);
        addTextField(new GuiNpcTextField(2,this, fontRendererObj, guiLeft + 85, y += 22, 50, 18, stats.rangedRange+""));
        addLabel(new GuiNpcLabel(2,"stats.rangedrange", guiLeft + 5, y + 5));
        getTextField(2).numbersOnly = true;
        getTextField(2).setMinMaxDefault(1, 64, 2);
        addTextField(new GuiNpcTextField(3,this, fontRendererObj, guiLeft + 85, y += 22, 50, 18, stats.minDelay+""));
        addLabel(new GuiNpcLabel(3,"stats.mindelay", guiLeft + 5, y + 5));
        getTextField(3).numbersOnly = true;
        getTextField(3).setMinMaxDefault(1, 9999, 20);
        addTextField(new GuiNpcTextField(4,this, fontRendererObj, guiLeft + 85, y += 22, 50, 18, stats.maxDelay+""));
        addLabel(new GuiNpcLabel(4,"stats.maxdelay", guiLeft + 5, y + 5));
        getTextField(4).numbersOnly = true;
        getTextField(4).setMinMaxDefault(1, 9999, 20);

		addTextField(new GuiNpcTextField(5, this, fontRendererObj, guiLeft + 85, y += 22, 50, 18, stats.fireRate + ""));
        addLabel(new GuiNpcLabel(5, "stats.burstspeed", guiLeft + 5, y + 5));
		getTextField(5).numbersOnly = true;
		getTextField(5).setMinMaxDefault(0, 30, 0);
    	addTextField(new GuiNpcTextField(6, this, fontRendererObj, guiLeft + 85, y += 22, 50, 18, stats.burstCount + ""));
    	addLabel(new GuiNpcLabel(6, "stats.burstcount", guiLeft + 5, y + 5));
    	getTextField(6).numbersOnly = true;
    	getTextField(6).setMinMaxDefault(1, 100, 20);
    	addTextField(new GuiNpcTextField(7,this, fontRendererObj, guiLeft + 85, y += 22, 100, 20, stats.fireSound));
    	addLabel(new GuiNpcLabel(7, "stats.firesound:", guiLeft + 5, y + 5));
    	addButton(new GuiNpcButton(7, guiLeft + 187, y, 60, 20, "mco.template.button.select"));
    	addTextField(new GuiNpcTextField(8, this, fontRendererObj, guiLeft + 85, y += 22, 50, 18, stats.shotCount + ""));
    	addLabel(new GuiNpcLabel(8, "stats.shotcount", guiLeft + 5, y + 5));
    	getTextField(8).numbersOnly = true;
    	getTextField(8).setMinMaxDefault(1, 10, 1);

    	addButton(new GuiNpcButtonYesNo(9, guiLeft + 100, y += 22, stats.aimWhileShooting));
    	addLabel(new GuiNpcLabel(9, "stats.aimWhileShooting", guiLeft + 5, y + 5));
    	
    	addButton(new GuiNpcButton(66, guiLeft + 190, guiTop + 190, 60, 20, "gui.done"));
    }

	public void unFocused(GuiNpcTextField textfield) {
		if(textfield.id == 1){
			stats.accuracy = textfield.getInteger();
		}
		else if(textfield.id == 2){
			stats.rangedRange = textfield.getInteger();
		}
		else if(textfield.id == 3){
			if (textfield.getInteger() > stats.maxDelay)
			{
				stats.minDelay = stats.maxDelay;
				textfield.setText(stats.minDelay + "");
			}
			else
			{
				stats.minDelay = textfield.getInteger();
			}
		}
		else if(textfield.id == 4){
			if (textfield.getInteger() < stats.minDelay)
			{
				stats.maxDelay = stats.minDelay;
				textfield.setText(stats.maxDelay + "");
			}
			else
			{
				stats.maxDelay = textfield.getInteger();
			}
		}
		else if(textfield.id == 5){
			stats.fireRate = textfield.getInteger();
		}
		else if(textfield.id == 6){
			stats.burstCount = textfield.getInteger();
		}
		else if(textfield.id == 7){
			stats.fireSound = textfield.getText();
		}
		else if(textfield.id == 8){
			stats.shotCount = textfield.getInteger();
		}
	}
	
	@Override
	public void elementClicked(){
		getTextField(7).setText(gui.getSelected());
		unFocused(getTextField(7));
	}

	@Override
	protected void actionPerformed(GuiButton guibutton){
		int id = guibutton.id;
        if(id == 7)
        {
        	NoppesUtil.openGUI(player, gui = new GuiNpcSoundSelection(npc, parent, getTextField(7).getText()));
        }
        if(id == 66)
        {
        	close();
        }
		else if(id == 9){
			stats.aimWhileShooting = ((GuiNpcButtonYesNo)guibutton).getBoolean();
		}
    }
}
