package noppes.npcs.client.gui;

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
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;

import org.lwjgl.opengl.GL11;

public class SubGuiColorSelector extends SubGuiInterface implements ITextfieldListener{

	private final static ResourceLocation resource = new ResourceLocation("customnpcs:textures/gui/color.png");
	
	private int colorX, colorY;
	
	private GuiNpcTextField textfield;
	
	public int color;

	public SubGuiColorSelector(int color){
		xSize = 176;
		ySize = 222;
		this.color = color;
		setBackground("smallbg.png");
	}

    @Override
    public void initGui() {
    	super.initGui();
    	colorX = guiLeft + 30;
    	colorY = guiTop + 50;
    	
		this.addTextField(textfield = new GuiNpcTextField(0, this, guiLeft + 53, guiTop + 20, 70, 20, getColor()));
		textfield.setTextColor(color);

    	addButton(new GuiNpcButton(66, guiLeft + 112, guiTop + 198, 60, 20, "gui.done"));
    }
    
    public String getColor(){
		String str = Integer.toHexString(color);
    	while(str.length() < 6)
    		str = "0" + str;
    	return str;
    }
    
    @Override
    public void keyTyped(char c, int i){
    	String prev = textfield.getText();
    	super.keyTyped(c, i);
    	String newText = textfield.getText();
    	if(newText.equals(prev))
    		return;
		try{
			color = Integer.parseInt(textfield.getText(),16);
			textfield.setTextColor(color);
		}
		catch(NumberFormatException e){
			textfield.setText(prev);
		}
    }

    @Override
    protected void actionPerformed(GuiButton btn) {
    	super.actionPerformed(btn);
		if(btn.id == 66){
        	close();
        }
    }

    @Override
    public void close(){
        super.close();
    }

    @Override
    public void drawScreen(int par1, int par2, float par3){
    	super.drawScreen(par1, par2, par3);

        mc.getTextureManager().bindTexture(resource);
        
        GL11.glColor4f(1, 1, 1, 1);
        this.drawTexturedModalRect(colorX, colorY, 0, 0, 120, 120);
    }
    
	@Override
    public void mouseClicked(int i, int j, int k){
		super.mouseClicked(i, j, k);
		if( i < colorX  || i > colorX + 117 || j < colorY || j > colorY + 117)
			return;
		InputStream stream = null;
		try {
			IResource iresource = this.mc.getResourceManager().getResource(resource);
            BufferedImage bufferedimage = ImageIO.read(stream = iresource.getInputStream());
            color = bufferedimage.getRGB((i - guiLeft - 30) * 4, (j - guiTop - 50) * 4)  & 16777215;
        	textfield.setTextColor(color);
        	textfield.setText(getColor());
			
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
		this.color = color;
		textfield.setTextColor(color);
	}
}
