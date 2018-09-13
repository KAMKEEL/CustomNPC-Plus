package noppes.npcs.client.gui;

import net.minecraft.client.gui.GuiButton;
import noppes.npcs.NoppesStringUtils;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcTextArea;
import noppes.npcs.client.gui.util.SubGuiInterface;

public class SubGuiNpcTextArea extends SubGuiInterface{
	public String text;
	private GuiNpcTextArea textarea;

	public SubGuiNpcTextArea(String text){
		this.text = text;
		setBackground("bgfilled.png");
		xSize = 256;
		ySize = 256;
		closeOnEsc = true;
	}
	
	@Override
	public void initGui(){
		xSize = (int) (width * 0.88);
		ySize = (int) (xSize * 0.56);
		bgScale = xSize / 440f;
		super.initGui();
		if(textarea != null)
			this.text = textarea.getText();
		int yoffset = (int) (ySize * 0.02);
		this.addTextField(textarea = new GuiNpcTextArea(2, this, guiLeft + yoffset, guiTop + yoffset, xSize - 100 - yoffset * 2, ySize - yoffset * 2, text));

		
		this.buttonList.add(new GuiNpcButton(102, guiLeft + xSize - 90 - yoffset, guiTop + 20, 56, 20, "gui.clear"));
		this.buttonList.add(new GuiNpcButton(101, guiLeft + xSize - 90 - yoffset, guiTop + 43, 56, 20, "gui.paste"));
		this.buttonList.add(new GuiNpcButton(100, guiLeft + xSize - 90 - yoffset, guiTop + 66, 56, 20, "gui.copy"));

		this.buttonList.add(new GuiNpcButton(0, guiLeft + xSize - 90 - yoffset, guiTop + 160, 56, 20, "gui.close"));

		xSize = 420;
		ySize = 256;
	}


	@Override
    public void close(){
		text = getTextField(2).getText();
		super.close();
	}

	@Override
	public void buttonEvent(GuiButton guibutton) {
		int id = guibutton.id;
		if (id == 100) {
			NoppesStringUtils.setClipboardContents(getTextField(2).getText());
		}
		if (id == 101) {
			getTextField(2).setText(NoppesStringUtils.getClipboardContents());
		}
		if (id == 102) {
			getTextField(2).setText("");
		}
		if(id == 0){
			close();
		}
	}
}
