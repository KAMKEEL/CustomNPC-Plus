package noppes.npcs.client.model.util;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Tessellator;
import org.lwjgl.opengl.GL11;

public class Model2DRenderer extends ModelRenderer {

	private boolean compiledModel;

	private int displayListModel;

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
		if(!compiledModel)
			compileDisplayListModel(par1);

		GL11.glPushMatrix();
		this.postRender(par1);

		GL11.glCallList(this.displayListModel);
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
	private void compileDisplayListModel(float par1)
	{
		this.displayListModel = GLAllocation.generateDisplayLists(1);
		GL11.glNewList(this.displayListModel, GL11.GL_COMPILE);

		GL11.glScalef(scaleX * width / height, scaleY, thickness);
		GL11.glRotatef(180, 1F, 0F, 0F);
		if(mirror){
			GL11.glTranslatef(0, 0, -1f * par1);
			GL11.glRotatef(180, 0, 1F, 0F);
		}

		GL11.glTranslated( rotationOffsetX * par1, rotationOffsetY * par1, 0);
		renderItemIn2D(x1, y1, x2, y2, width, height, par1);

		GL11.glEndList();
		this.compiledModel = true;
	}

    public static void renderItemIn2D(float p_78439_1_, float p_78439_2_, float p_78439_3_, float p_78439_4_, int p_78439_5_, int p_78439_6_, float p_78439_7_) {
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.setNormal(0.0F, 0.0F, 1.0F);
        tessellator.addVertexWithUV(0.0D, 0.0D, 0.0D, p_78439_1_, p_78439_4_);
        tessellator.addVertexWithUV(1.0D, 0.0D, 0.0D, p_78439_3_, p_78439_4_);
        tessellator.addVertexWithUV(1.0D, 1.0D, 0.0D, p_78439_3_, p_78439_2_);
        tessellator.addVertexWithUV(0.0D, 1.0D, 0.0D, p_78439_1_, p_78439_2_);
        tessellator.draw();

        tessellator.startDrawingQuads();
        tessellator.setNormal(0.0F, 0.0F, -1.0F);
        tessellator.addVertexWithUV(0.0D, 1.0D, 0.0F - p_78439_7_, p_78439_1_, p_78439_2_);
        tessellator.addVertexWithUV(1.0D, 1.0D, 0.0F - p_78439_7_, p_78439_3_, p_78439_2_);
        tessellator.addVertexWithUV(1.0D, 0.0D, 0.0F - p_78439_7_, p_78439_3_, p_78439_4_);
        tessellator.addVertexWithUV(0.0D, 0.0D, 0.0F - p_78439_7_, p_78439_1_, p_78439_4_);
        tessellator.draw();

        float f5 = 0.5F * (p_78439_1_ - p_78439_3_) / (float) p_78439_5_;
        float f6 = 0.5F * (p_78439_4_ - p_78439_2_) / (float) p_78439_6_;

        tessellator.startDrawingQuads();
        tessellator.setNormal(-1.0F, 0.0F, 0.0F);

        for (int k = 0; k < p_78439_5_; ++k) {
            float f7 = (float) k / (float) p_78439_5_;
            float f8 = p_78439_1_ + (p_78439_3_ - p_78439_1_) * f7 - f5;
            tessellator.addVertexWithUV(f7, 0.0D, 0.0F - p_78439_7_, f8, p_78439_4_);
            tessellator.addVertexWithUV(f7, 0.0D, 0.0D, f8, p_78439_4_);
            tessellator.addVertexWithUV(f7, 1.0D, 0.0D, f8, p_78439_2_);
            tessellator.addVertexWithUV(f7, 1.0D, 0.0F - p_78439_7_, f8, p_78439_2_);
        }

        tessellator.draw();
        tessellator.startDrawingQuads();
        tessellator.setNormal(1.0F, 0.0F, 0.0F);

        for (int k = 0; k < p_78439_5_; ++k) {
            float f7 = (float) k / (float) p_78439_5_;
            float f8 = p_78439_1_ + (p_78439_3_ - p_78439_1_) * f7 - f5;
            float f9 = f7 + 1.0F / (float) p_78439_5_;
            tessellator.addVertexWithUV(f9, 1.0D, 0.0F - p_78439_7_, f8, p_78439_2_);
            tessellator.addVertexWithUV(f9, 1.0D, 0.0D, f8, p_78439_2_);
            tessellator.addVertexWithUV(f9, 0.0D, 0.0D, f8, p_78439_4_);
            tessellator.addVertexWithUV(f9, 0.0D, 0.0F - p_78439_7_, f8, p_78439_4_);
        }

        tessellator.draw();
        tessellator.startDrawingQuads();
        tessellator.setNormal(0.0F, 1.0F, 0.0F);

        for (int k = 0; k < p_78439_6_; ++k) {
            float f7 = (float) k / (float) p_78439_6_;
            float f8 = p_78439_4_ + (p_78439_2_ - p_78439_4_) * f7 - f6;
            float f9 = f7 + 1.0F / (float) p_78439_6_;
            tessellator.addVertexWithUV(0.0D, f9, 0.0D, p_78439_1_, f8);
            tessellator.addVertexWithUV(1.0D, f9, 0.0D, p_78439_3_, f8);
            tessellator.addVertexWithUV(1.0D, f9, 0.0F - p_78439_7_, p_78439_3_, f8);
            tessellator.addVertexWithUV(0.0D, f9, 0.0F - p_78439_7_, p_78439_1_, f8);
        }

        tessellator.draw();
        tessellator.startDrawingQuads();
        tessellator.setNormal(0.0F, -1.0F, 0.0F);

        for (int k = 0; k < p_78439_6_; ++k) {
            float f7 = (float) k / (float) p_78439_6_;
            float f8 = p_78439_4_ + (p_78439_2_ - p_78439_4_) * f7 - f6;
            tessellator.addVertexWithUV(1.0D, f7, 0.0D, p_78439_3_, f8);
            tessellator.addVertexWithUV(0.0D, f7, 0.0D, p_78439_1_, f8);
            tessellator.addVertexWithUV(0.0D, f7, 0.0F - p_78439_7_, p_78439_1_, f8);
            tessellator.addVertexWithUV(1.0D, f7, 0.0F - p_78439_7_, p_78439_3_, f8);
        }

        tessellator.draw();
    }
}
