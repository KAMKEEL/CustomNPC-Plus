package noppes.npcs.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.StatCollector;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.controllers.Faction;

public class SubGuiNpcFactionPoints extends SubGuiInterface implements ITextfieldListener
{
	private Faction faction;
    public SubGuiNpcFactionPoints(Faction faction){
    	this.faction = faction;
		setBackground("menubg.png");
		xSize = 256;
		ySize = 216;
		closeOnEsc = true;
    }

    @Override
    public void initGui(){
        super.initGui();
    	
    	addLabel(new GuiNpcLabel(2,"faction.default", guiLeft + 4, guiTop + 33));
    	
    	this.addTextField(new GuiNpcTextField(2, this, guiLeft + 8 + fontRendererObj.getStringWidth(getLabel(2).label), guiTop + 28, 70, 20, faction.defaultPoints + ""));
    	getTextField(2).setMaxStringLength(6);
    	getTextField(2).numbersOnly = true;
    	
    	String title = StatCollector.translateToLocal("faction.unfriendly") + "<->" + StatCollector.translateToLocal("faction.neutral");
    	addLabel(new GuiNpcLabel(3, title, guiLeft + 4, guiTop + 80));
    	addTextField(new GuiNpcTextField(3, this, guiLeft + 8 + fontRendererObj.getStringWidth(title), guiTop + 75, 70, 20, faction.neutralPoints + ""));

    	title = StatCollector.translateToLocal("faction.neutral") + "<->" + StatCollector.translateToLocal("faction.friendly");
    	addLabel(new GuiNpcLabel(4, title, guiLeft + 4, guiTop + 105));
    	addTextField(new GuiNpcTextField(4, this, guiLeft +  8 + fontRendererObj.getStringWidth(title), guiTop + 100, 70, 20, faction.friendlyPoints + ""));

    	getTextField(3).numbersOnly = true;
    	getTextField(4).numbersOnly = true;
    	
    	if(getTextField(3).xPosition > getTextField(4).xPosition)
    		getTextField(4).xPosition = getTextField(3).xPosition;
    	else
    		getTextField(3).xPosition = getTextField(4).xPosition;
    	
    	addButton(new GuiNpcButton(66, guiLeft + 20, guiTop + 192, 90, 20, "gui.done"));
    }

    @Override
	public void unFocused(GuiNpcTextField textfield) {
		 if(textfield.id == 2) {
			faction.defaultPoints = textfield.getInteger();
		}else if(textfield.id == 3) {
			faction.neutralPoints = textfield.getInteger();
		}else if(textfield.id == 4) {
			faction.friendlyPoints = textfield.getInteger();
		}
	}

    @Override
	protected void actionPerformed(GuiButton guibutton){
		int id = guibutton.id;
        if(id == 66){
        	close();
        }
    }

}
