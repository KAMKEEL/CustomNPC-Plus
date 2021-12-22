package noppes.npcs.client.gui.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

public class GuiNpcTextField extends GuiTextField{
	public boolean enabled = true;
	public boolean inMenu = true;
	public boolean integersOnly = false;
	public boolean doublesOnly = false;
	public boolean floatsOnly = false;
	private ITextfieldListener listener;
    public int id;
    public int min = 0,max = Integer.MAX_VALUE,def = 0;
	public double minDouble = 0, maxDouble = Double.MAX_VALUE, defDouble = 0;
	public float minFloat = 0, maxFloat = Float.MAX_VALUE, defFloat = 0;
    private static GuiNpcTextField activeTextfield = null;
	public boolean canEdit = true;
    
    private final int[] allowedSpecialChars = {14,211,203,205};
	
	public GuiNpcTextField(int id,GuiScreen parent, FontRenderer fontRenderer,
			int i, int j, int k, int l, String s) {
		super(fontRenderer, i, j, k, l);
		setMaxStringLength(500);
		this.setText(s);
		this.id = id;
		if(parent instanceof ITextfieldListener)
			listener = (ITextfieldListener) parent;
	}
	
	public static boolean isFieldActive(){
		return activeTextfield != null;
	}
	
	public GuiNpcTextField(int id,GuiScreen parent,
			int i, int j, int k, int l, String s) {
		this(id, parent, Minecraft.getMinecraft().fontRenderer, i, j, k, l, s);
	}

	private boolean charAllowed(char c, int i){
		if(!integersOnly || Character.isDigit(c))
			return true;
		for(int j : allowedSpecialChars)
			if(j == i)
				return true;
		
		return false;
	}
	
	@Override
    public boolean textboxKeyTyped(char c, int i)
    {
    	if(!charAllowed(c,i) || !canEdit)
    		return false;
		return super.textboxKeyTyped(c, i);
    }
	
    public boolean isEmpty(){
    	return getText().trim().length() == 0;
    }

	public int getInteger(){
    	return Integer.parseInt(getText());
    }

    public boolean isInteger(){
    	try{
    		Integer.parseInt(getText());
    		return true;
    	}catch(NumberFormatException e){
    		return false;
    	}
    }

	public double getDouble(){
		return Double.parseDouble(getText());
	}

	public boolean isDouble() {
		try{
			Double.parseDouble(getText());
			return true;
		}catch(NumberFormatException e){
			return false;
		}
	}

	public float getFloat(){
		return Float.parseFloat(getText());
	}

	public boolean isFloat() {
		try{
			Float.parseFloat(getText());
			return true;
		}catch(NumberFormatException e){
			return false;
		}
	}

	@Override
    public void mouseClicked(int i, int j, int k){
		if(!canEdit)
			return;
    	boolean wasFocused = this.isFocused();
    	super.mouseClicked(i, j, k);
    	if(wasFocused != isFocused()){
    		if(wasFocused){
    			unFocused();
    		}
    	}
    	if(isFocused())
    		activeTextfield = this;
    }

	public void unFocused(){
		if(integersOnly && !doublesOnly && !floatsOnly){
			if(isEmpty() || !isInteger())
				setText(def+"");
			else if(getInteger() < min)
				setText(min+"");
			else if(getInteger() > max)
				setText(max+"");
		}
		else if(doublesOnly && !floatsOnly){
			if(isEmpty() || !isDouble())
				setText(defDouble +"");
			else if(getDouble() < minDouble)
				setText(minDouble +"");
			else if(getDouble() > maxDouble)
				setText(maxDouble +"");
		}
		else if(floatsOnly){
			if(isEmpty() || !isFloat())
				setText(defFloat +"");
			else if(getFloat() < minFloat)
				setText(minFloat +"");
			else if(getFloat() > maxFloat)
				setText(maxFloat +"");
		}

		if(listener != null)
			listener.unFocused(this);
		
		if(this == activeTextfield)
			activeTextfield = null;
	}

	@Override
    public void drawTextBox()
    {
    	if(enabled)
    		super.drawTextBox();
    }
	public void setMinMaxDefault(int min, int max, int def) {
		this.min = min;
		this.max = max;
		this.def = def;
	}
	public void setMinMaxDefaultDouble(double min, double max, double def) {
		minDouble = min;
		maxDouble = max;
		defDouble = def;
	}
	public void setMinMaxDefaultFloat(float min, float max, float def) {
		minFloat = min;
		maxFloat = max;
		defFloat = def;
	}
	public static void unfocus() {
		GuiNpcTextField prev = activeTextfield;
		activeTextfield = null;
		if(prev != null){
			prev.unFocused();
		}
	}

	public void drawTextBox(int mousX, int mousY) {
		drawTextBox();
	}

	public GuiNpcTextField setIntegersOnly() {
		integersOnly = true;
		return this;
	}

	public GuiNpcTextField setDoublesOnly() {
		doublesOnly = true;
		return this;
	}
	public GuiNpcTextField setFloatsOnly() {
		floatsOnly = true;
		return this;
	}

}
