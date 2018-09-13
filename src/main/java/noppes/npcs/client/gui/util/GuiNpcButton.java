package noppes.npcs.client.gui.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.StatCollector;

public class GuiNpcButton extends GuiButton{
	
	protected String[] display;
	private int displayValue = 0;
	public int id;
	
	public GuiNpcButton(int i, int j, int k,  String s) {
		super(i, j, k,  StatCollector.translateToLocal(s));
		id = i;
	}
	public GuiNpcButton(int i, int j, int k,  String[] display, int val) {
		this(i, j, k, display[val]);
		this.display = display;
		this.displayValue = val;
	}
	public GuiNpcButton(int i, int j, int k, int l, int m, String string) {
		super(i, j, k, l, m, StatCollector.translateToLocal(string));
		id = i;
	}
	public GuiNpcButton(int i, int j, int k, int l, int m, String[] display,
			int val) {
		this(i, j, k, l, m, display.length == 0?"":display[val % display.length]);
		this.display = display;
		this.displayValue = display.length == 0?0:val % display.length;
	}
	public void setDisplayText(String text){
		this.displayString = StatCollector.translateToLocal(text);
	}
	public int getValue(){
		return displayValue;
	}
	
	public void setEnabled(boolean bo){
		this.enabled = bo;
	}
	public void setVisible(boolean b) {
		this.visible = b;
	}   
    public boolean getVisible() {
		return visible;
	}
	
    public void setDisplay(int value){
    	this.displayValue = value;
    	this.setDisplayText(display[value]);
    } 
    
    public void setTextColor(int color){
    	this.packedFGColour = color;
    }

    @Override
    public boolean mousePressed(Minecraft minecraft, int i, int j)
    {
    	boolean bo = super.mousePressed(minecraft, i, j);
    	if(bo && display != null && display.length != 0){
    		displayValue = (displayValue+1) % display.length;
    		this.setDisplayText(display[displayValue]);
    	}
    	return bo;
    }
    
	public int getWidth() {
		return width;
	}
}
