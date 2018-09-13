package noppes.npcs.client.gui;

import net.minecraft.client.gui.GuiButton;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;

public class SubGuiEditText extends SubGuiInterface{
	public String text;
	public boolean cancelled = true;
	public SubGuiEditText(String text){
		this.text = text;
		setBackground("extrasmallbg.png");
		closeOnEsc = true;
		xSize = 176;
		ySize = 71;
	}
	
	@Override
	public void initGui(){
		super.initGui();
		addTextField(new GuiNpcTextField(0, parent, guiLeft + 4, guiTop + 14, 168, 20, text));
		
		this.addButton(new GuiNpcButton(0, guiLeft + 4, guiTop + 44, 80, 20, "gui.done"));
		this.addButton(new GuiNpcButton(1, guiLeft + 90, guiTop + 44, 80, 20, "gui.cancel"));
	}
	
	@Override
	public void buttonEvent(GuiButton button){
		if(button.id == 0){
			cancelled = false;
			text = getTextField(0).getText();
		}
		close();
	}
	
	@Override
	public void save() {
	}
}
