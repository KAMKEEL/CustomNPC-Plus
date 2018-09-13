package noppes.npcs.client.model.util;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Tessellator;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class Model2DRenderer extends ModelRenderer {

    private boolean compiled;

    private int displayList;
    
	private float x1, x2, y1, y2;
	private int width, height;
	private float rotationOffsetX, rotationOffsetY;
	
	private float scaleX = 1, scaleY = 1, thickness = 1;

	public Model2DRenderer(ModelBase par1ModelBase, float x, float y, int width, int height, float textureWidth, float textureHeight) {
		super(par1ModelBase);
		this.width = width;
		this.height = height;
		this.textureWidth = textureWidth;
		this.textureHeight = textureHeight;
		
		x1 = x / textureWidth;
		y1 = y / textureHeight;

		x2 = (x + width) / textureWidth;
		y2 = (y + height) / textureHeight;
	}

	public Model2DRenderer(ModelBase base, int x, int y, int width, int height) {
		this(base, x, y, width, height, width, height);
	}

	public void render(float par1) {
		if(!showModel || isHidden)
			return;
		if(!compiled)
			compileDisplayList(par1);
		
		GL11.glPushMatrix();
		this.postRender(par1);
    	
        GL11.glCallList(this.displayList);
		GL11.glPopMatrix();
	}
	
	public void setRotationOffset(float x, float y){
		rotationOffsetX = x;
		rotationOffsetY = y;
	}

	public void setScale(float scale){
		this.scaleX = scale;
		this.scaleY = scale;
	}
	public void setScale(float x, float y){
		this.scaleX = x;
		this.scaleY = y;
	}

	public void setThickness(float thickness) {
		this.thickness = thickness;
	}
    @SideOnly(Side.CLIENT)
    private void compileDisplayList(float par1)
    {
        this.displayList = GLAllocation.generateDisplayLists(1);
        GL11.glNewList(this.displayList, GL11.GL_COMPILE);

		GL11.glScalef(scaleX * width / height, scaleY, thickness);
    	GL11.glRotatef(180, 1F, 0F, 0F);
        if(mirror){
    		GL11.glTranslatef(0, 0, -1f * par1);
            GL11.glRotatef(180, 0, 1F, 0F);
        }

		GL11.glTranslated( rotationOffsetX * par1, rotationOffsetY * par1, 0);
		ItemRenderer.renderItemIn2D(Tessellator.instance, x1, y1, x2, y2, width, height, par1);

        GL11.glEndList();
        this.compiled = true;
    }
}
