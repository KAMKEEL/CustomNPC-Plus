package noppes.npcs.client.gui.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import noppes.npcs.NoppesStringUtils;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

public class GuiNpcSlider extends GuiButton {
	private ISliderListener listener;
	public int id;
	
    public float sliderValue = 1.0F;

    public boolean dragging;
	
	public GuiNpcSlider(GuiScreen parent, int id, int xPos, int yPos, String displayString, float sliderValue) {
		super(id, xPos, yPos, 150, 20, NoppesStringUtils.translate(displayString));
		this.id = id;
        this.sliderValue = sliderValue;
		if(parent instanceof ISliderListener)
			listener = (ISliderListener) parent;
	}
	
	public GuiNpcSlider(GuiScreen parent, int id, int xPos, int yPos, float sliderValue) {
		this(parent, id, xPos, yPos, "", sliderValue);
		if(listener != null)
			listener.mouseDragged(this);
	}
	
	public GuiNpcSlider(GuiScreen parent, int id, int xPos, int yPos, int width, int height, float sliderValue) {
		this(parent, id, xPos, yPos, "", sliderValue);
		this.width = width;
		this.height = height;
		if(listener != null)
			listener.mouseDragged(this);
	}
	
	@Override
    public void mouseDragged(Minecraft mc, int par2, int par3){
        if (!this.visible)
        	return;
        
        mc.getTextureManager().bindTexture(buttonTextures);
        if (this.dragging){
            this.sliderValue = (float)(par2 - (this.xPosition + 4)) / (float)(this.width - 8);

            if (this.sliderValue < 0.0F){
                this.sliderValue = 0.0F;
            }

            if (this.sliderValue > 1.0F){
                this.sliderValue = 1.0F;
            }

    		if(listener != null)
    			listener.mouseDragged(this);
    		
        	if(!Mouse.isButtonDown(0)){
        		this.func_146111_b(0, 0);
        	}
        }

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.drawTexturedModalRect(this.xPosition + (int)(this.sliderValue * (float)(this.width - 8)), this.yPosition, 0, 66, 4, 20);
        this.drawTexturedModalRect(this.xPosition + (int)(this.sliderValue * (float)(this.width - 8)) + 4, this.yPosition, 196, 66, 4, 20);
    }
	
	public String getDisplayString(){
		return displayString;
	}
	public void setString(String str) {
		displayString = NoppesStringUtils.translate(str);
	}
	
	@Override
    public boolean mousePressed(Minecraft par1Minecraft, int par2, int par3){
        if (this.enabled && this.visible && par2 >= this.xPosition && par3 >= this.yPosition && par2 < this.xPosition + this.width && par3 < this.yPosition + this.height){
            this.sliderValue = (float)(par2 - (this.xPosition + 4)) / (float)(this.width - 8);

            if (this.sliderValue < 0.0F){
                this.sliderValue = 0.0F;
            }

            if (this.sliderValue > 1.0F){
                this.sliderValue = 1.0F;
            }

    		if(listener != null)
    			listener.mousePressed(this);

            this.dragging = true;
            return true;
        }
        return false;
    }
	
	@Override
    public void func_146111_b(int par1, int par2)
    {
        this.dragging = false;
        
        if(listener != null)
			listener.mouseReleased(this);
    }
	
    @Override
    public int getHoverState(boolean par1){
        return 0;
    }
}
