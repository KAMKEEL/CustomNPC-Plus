package noppes.npcs.client.gui;

import net.minecraft.client.gui.GuiButton;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;

public class SubGuiNpcCommand extends SubGuiInterface implements ITextfieldListener
{
	public String command;
	
    public SubGuiNpcCommand(String command)
    {
    	this.command = command;
		setBackground("menubg.png");
		xSize = 256;
		ySize = 216;
		closeOnEsc = true;
    }

    public void initGui()
    {
        super.initGui();
    	this.addTextField(new GuiNpcTextField(4, this, this.fontRendererObj, guiLeft + 4,  guiTop + 84, 248, 20, command));
    	this.getTextField(4).setMaxStringLength(32767);

    	this.addLabel(new GuiNpcLabel(4, "advMode.command", guiLeft + 4, guiTop + 110));
    	this.addLabel(new GuiNpcLabel(5, "advMode.nearestPlayer", guiLeft + 4, guiTop + 125));
    	this.addLabel(new GuiNpcLabel(6, "advMode.randomPlayer", guiLeft + 4, guiTop + 140));
    	this.addLabel(new GuiNpcLabel(7, "advMode.allPlayers", guiLeft + 4, guiTop + 155));
    	this.addLabel(new GuiNpcLabel(8, "dialog.commandoptionplayer", guiLeft + 4, guiTop + 170));

    	this.addButton(new GuiNpcButton(66, guiLeft + 82, guiTop + 190,98, 20, "gui.done"));
    	
    }

	protected void actionPerformed(GuiButton guibutton)
    {
		int id = guibutton.id;
        if(id == 66){
        	close();
        }
    }

	@Override
	public void unFocused(GuiNpcTextField textfield) {
		if(textfield.id == 4){
			command = textfield.getText();
		}
	}

}
