package noppes.npcs.client.gui.util;

public class GuiNpcButtonYesNo extends GuiNpcButton{

	public GuiNpcButtonYesNo(int id, int x, int y, boolean bo) {
		this(id, x, y, 50, 20, bo);
	}
	
	public GuiNpcButtonYesNo(int id, int x, int y, int width, int height, boolean bo) {
		super(id, x, y, width, height, new String[]{"gui.no", "gui.yes"}, bo?1:0);
	}
	
	public boolean getBoolean(){
		return getValue() == 1;
	}
}
