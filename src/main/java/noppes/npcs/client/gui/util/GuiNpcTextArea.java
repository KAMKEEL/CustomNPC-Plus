package noppes.npcs.client.gui.util;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.MathHelper;
import noppes.npcs.client.ClientProxy;
import noppes.npcs.client.ClientProxy.FontContainer;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

public class GuiNpcTextArea extends GuiNpcTextField{
	public boolean inMenu = true;
	public boolean numbersOnly = false;
	private int posX,posY,width,height;
    private int cursorCounter;
	private FontContainer font;
	private int cursorPosition = 0;
	private int listHeight;
	private float scrolledY = 0;
	private int startClick = -1;
	private boolean clickVerticalBar = false;
	private boolean wrapLine = true;
	
	public GuiNpcTextArea(int id,GuiScreen guiscreen, int i, int j, int k, int l, String s) {
		super(id,guiscreen, i, j, k, l, s);
		posX = i;
		posY = j;
		width = k;
		listHeight = height = l;
		this.font = ClientProxy.Font;
		setMaxStringLength(Integer.MAX_VALUE);
		this.setText(s);
	}

	@Override
    public void updateCursorCounter(){
        cursorCounter++;
    }
    
	@Override
    public boolean textboxKeyTyped(char c, int i){
        if (isFocused() && canEdit){
        	String originalText = getText();       
        	this.setText(originalText);	
        	if(c == '\r' || c == '\n'){
        		this.setText(originalText.substring(0, cursorPosition) + c + originalText.substring(cursorPosition));
        	}
        	this.setCursorPositionZero();
        	this.moveCursorBy(cursorPosition);
        	boolean bo = super.textboxKeyTyped(c, i);
        	String newText = getText();  
        	if(i != Keyboard.KEY_DELETE)
        		cursorPosition += newText.length() - originalText.length();
    		if(i == Keyboard.KEY_LEFT && cursorPosition > 0)
    			cursorPosition--;
    		if(i == Keyboard.KEY_RIGHT && cursorPosition < newText.length())
    			cursorPosition++;
        	return bo;
    		
        }
        return false;
    }
	
	@Override
    public void mouseClicked(int i, int j, int k){
    	boolean wasFocused = isFocused();
        super.mouseClicked(i, j, k);
        
        if(hoverVerticalScrollBar(i, j)){
        	clickVerticalBar = true;
        	startClick = -1;
        	return;
        }
        if(k != 0 || !canEdit)
        	return;
        
    	int x = i - posX;
    	int y = (j - posY - 4) / font.height() + getStartLineY();
    	cursorPosition = 0;
        List<String> lines = getLines();
        int charCount = 0;
        int lineCount = 0;
        int maxSize = width - (isScrolling()?14:4);
        for(int g = 0; g < lines.size(); g++){
        	String wholeLine = lines.get(g);
        	String line = "";
    		for(char c : wholeLine.toCharArray()){
				cursorPosition = charCount;
        		if(font.width(line + c) > maxSize && wrapLine){
        			lineCount++;
        			line = "";
                    if(y < lineCount){
                    	break;
                    }
        		}
        		if(lineCount == y && x <= font.width(line + c))
            		return;
        		charCount++;
        		line += c;
        		
    		}
    		cursorPosition = charCount;

            	

    		lineCount++;
			charCount++;
            if(y < lineCount){
            	break;
            }
		}        
        if(y >= lineCount)
        	cursorPosition = getText().length();
    }
	
	private List<String> getLines(){
		List<String> list = new ArrayList<String>();
		String line = "";
		for(char c : getText().toCharArray()){
        	if(c == '\r' || c == '\n'){
        		list.add(line);
        		line = "";
        	}
        	else
        		line += c;
		}
    	list.add(line);
		return list;
	}
	
	private int getStartLineY(){
		if(!isScrolling())
			scrolledY = 0;
		return MathHelper.ceiling_double_int(scrolledY * (listHeight) / font.height());
	}

	@Override
	public void drawTextBox(int mouseX, int mouseY) {
        drawRect(posX - 1, posY - 1, posX + width + 1, posY + height + 1, 0xffa0a0a0);
        drawRect(posX, posY, posX + width, posY + height, 0xff000000);

        //int color = isEnabled?0xe0e0e0:0x707070;
        int color = 0xe0e0e0;
        boolean flag = isFocused() && (cursorCounter / 6) % 2 == 0;
        
        int startLine = getStartLineY();
        
        int maxLine = height / font.height() + startLine;
        
        List<String> lines = getLines();
        int charCount = 0;
        int lineCount = 0;
        int maxSize = width - (isScrolling()?14:4);
        for(int i = 0; i < lines.size(); i++){
        	String wholeLine = lines.get(i);
        	String line = "";
    		for(char c : wholeLine.toCharArray()){
        		if(font.width(line + c) > maxSize && wrapLine){
        			if(lineCount >= startLine && lineCount < maxLine)
        				drawString(null, line, posX + 4, posY + 4 + ((lineCount - startLine) * font.height()), color);
        			line = "";
        			lineCount++;
        		}
            	if(flag && charCount == cursorPosition && lineCount >= startLine && lineCount < maxLine && canEdit){
        			int xx = posX + font.width(line) + 4;
        			int yy = posY + ((lineCount - startLine) * font.height()) + 4;
            		if(getText().length() == cursorPosition){
            			font.drawString("_", xx, yy, color);  
            		}
            		else{
            			drawCursorVertical(xx, yy, xx + 1, yy + font.height());
            		}
            	}
        		charCount++;
        		line += c;
    		}
			if(lineCount >= startLine && lineCount < maxLine){
				drawString(null, line, posX + 4, posY + 4 + ((lineCount - startLine) * (font.height())), color);
	        	if(flag && charCount == cursorPosition && canEdit){
	    			int xx = posX + font.width(line) + 4;
	    			int yy = posY + ((lineCount - startLine) * font.height()) + 4;
	        		if(getText().length() == cursorPosition){
	        			font.drawString("_", xx, yy, color);  
	        		}
	        		else{
	        			drawCursorVertical(xx, yy, xx + 1, yy + font.height());
	        		}
	        	}
			}
    		lineCount++;
        	charCount++;
        }
        
        
        int k2 = Mouse.getDWheel();
        if(k2 != 0 && isFocused()){
        	addScrollY(k2 < 0?-10:10);
        }

        if(Mouse.isButtonDown(0)){
        	if(clickVerticalBar){
	        	if(startClick >= 0)
	        		addScrollY(startClick - (mouseY - posY));
	        	
	        	if(hoverVerticalScrollBar(mouseX, mouseY))
	        		startClick = mouseY - posY;
	
	    		startClick = mouseY - posY;
        	}
        }
        else
        	clickVerticalBar = false;

        listHeight = lineCount * font.height();
        drawVerticalScrollBar();
    }
	
    public void drawString(FontRenderer fontRendererIn, String text, int x, int y, int color){
    	GL11.glColor4f(1, 1, 1, 1);
    	font.drawString(text, x, y, color);
    	//super.drawString(fontRendererIn, text, x, y, color);
    }
	
	private boolean isScrolling() {
    	return listHeight > height - 4;
	}

	private void addScrollY(int scrolled){
		scrolledY -= 1f * scrolled / height;
		
    	if(scrolledY < 0)
    		scrolledY = 0;

    	float max = 1 - 1f * (height + 2) / listHeight;
    	if(scrolledY > max )
    		scrolledY = max;
	}
	
	private boolean hoverVerticalScrollBar(int x, int y){
    	if(listHeight <= height - 4)
    		return false;
    	
    	if(posY < y && posY + height > y && x < posX + width && x > posX + (width - 8))
    		return true;
    	
		return false;
	}
	
    private void drawCursorVertical(int p_146188_1_, int p_146188_2_, int p_146188_3_, int p_146188_4_){
        int i1;

        if (p_146188_1_ < p_146188_3_){
            i1 = p_146188_1_;
            p_146188_1_ = p_146188_3_;
            p_146188_3_ = i1;
        }

        if (p_146188_2_ < p_146188_4_){
            i1 = p_146188_2_;
            p_146188_2_ = p_146188_4_;
            p_146188_4_ = i1;
        }

        if (p_146188_3_ > this.posX + this.width){
            p_146188_3_ = this.posX + this.width;
        }

        if (p_146188_1_ > this.posX + this.width){
            p_146188_1_ = this.posX + this.width;
        }

        Tessellator tessellator = Tessellator.instance;
        GL11.glColor4f(0.0F, 0.0F, 255.0F, 255.0F);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_COLOR_LOGIC_OP);
        GL11.glLogicOp(GL11.GL_OR_REVERSE);
        tessellator.startDrawingQuads();
        tessellator.addVertex((double)p_146188_1_, (double)p_146188_4_, 0.0D);
        tessellator.addVertex((double)p_146188_3_, (double)p_146188_4_, 0.0D);
        tessellator.addVertex((double)p_146188_3_, (double)p_146188_2_, 0.0D);
        tessellator.addVertex((double)p_146188_1_, (double)p_146188_2_, 0.0D);
        tessellator.draw();
        GL11.glDisable(GL11.GL_COLOR_LOGIC_OP);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }
    private int getVerticalBarSize(){
    	return (int) (1f * height / listHeight * (height - 4));
    }
    private void drawVerticalScrollBar(){
    	if(listHeight <= height - 4)
    		return;
        Minecraft.getMinecraft().renderEngine.bindTexture(GuiCustomScroll.resource);
        int x = posX + width - 6;
        int y = (int) (posY + scrolledY * height) + 2;
        GL11.glColor4f(1, 1, 1, 1);
        int sbSize = getVerticalBarSize();
        drawTexturedModalRect(x, y, width, 9, 5, 1);
        for(int k = 0; k < sbSize; k++){
            drawTexturedModalRect(x, y + k, width, 10, 5, 1);
        }
        drawTexturedModalRect(x, y, width, 11, 5, 1);
    }
}
