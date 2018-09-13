package noppes.npcs.client.gui.model;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.ModelPartData;
import noppes.npcs.client.gui.util.GuiModelInterface;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.entity.EntityCustomNpc;

import org.lwjgl.opengl.GL11;

public class GuiModelColor extends GuiModelInterface implements ITextfieldListener{

	private GuiScreen parent;
	private final static ResourceLocation color = new ResourceLocation("customnpcs:textures/gui/color.png");
	
	private int colorX, colorY;
	
	private GuiNpcTextField textfield;

	private ModelPartData data;
	public GuiModelColor(GuiScreen parent, ModelPartData data, EntityCustomNpc npc){
		super(npc);
		this.parent = parent;
		this.data = data;
		this.xOffset = 60;
		ySize = 230;
	}

    @Override
    public void initGui() {
    	super.initGui();
    	colorX = guiLeft + 4;
    	colorY = guiTop + 50;
		this.addTextField(textfield = new GuiNpcTextField(0, this, guiLeft + 25, guiTop + 20, 70, 20, data.getColor()));
		textfield.setTextColor(data.color);
    }
    
    @Override
    public void keyTyped(char c, int i){
    	String prev = textfield.getText();
    	super.keyTyped(c, i);
    	String newText = textfield.getText();
    	if(newText.equals(prev))
    		return;
		try{
			int color = Integer.parseInt(textfield.getText(),16);
			data.color = color;
			textfield.setTextColor(color);
		}
		catch(NumberFormatException e){
			textfield.setText(prev);
		}
    }

    @Override
    protected void actionPerformed(GuiButton btn) {
    	super.actionPerformed(btn);
    }

    @Override
    public void close(){
        this.mc.displayGuiScreen(parent);
    }

    @Override
    public void drawScreen(int par1, int par2, float par3){
    	super.drawScreen(par1, par2, par3);
        mc.getTextureManager().bindTexture(color);
        
        GL11.glColor4f(1, 1, 1, 1);
        this.drawTexturedModalRect(colorX, colorY, 0, 0, 120, 120);
    }
    
	@Override
    public void mouseClicked(int i, int j, int k)
    {
		super.mouseClicked(i, j, k);
		if(i < colorX  || i > colorX + 120 || j < colorY || j > colorY + 120)
			return;
		InputStream stream = null;
		try {
			IResource resource = this.mc.getResourceManager().getResource(color);
            BufferedImage bufferedimage = ImageIO.read(stream = resource.getInputStream());
            int color = bufferedimage.getRGB((i - guiLeft - 4) * 4, (j - guiTop - 50) * 4)  & 16777215;
            if(color != 0){
            	data.color = color;
            	textfield.setTextColor(color);
            	textfield.setText(data.getColor());
            }
			
		} catch (IOException e) {
		} 
		finally{
			if(stream != null){
				try {
					stream.close();
				} catch (IOException e) {
					
				}
			}
		}
    }

	@Override
	public void unFocused(GuiNpcTextField textfield) {
		int color = 0;
		try{
			color = Integer.parseInt(textfield.getText(),16);
		}
		catch(NumberFormatException e){
			color = 0;
		}
		data.color = color;
		textfield.setTextColor(color);
	}
}
