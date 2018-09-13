package noppes.npcs.client.model.util;

import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.model.PositionTextureVertex;
import net.minecraft.client.model.TexturedQuad;
import net.minecraft.client.renderer.Tessellator;
import noppes.npcs.client.renderer.EnumPlanePosition;

public class ModelPlane extends ModelBox{

    private PositionTextureVertex[] vertexPositions;
    private TexturedQuad quad;
    
	public ModelPlane(ModelRenderer par1ModelRenderer, int textureOffsetX, int textureOffsetY, float par4, float par5, float par6,
			int par7, int par8, int par9, float par10, EnumPlanePosition position) {
		super(par1ModelRenderer, textureOffsetX, textureOffsetY, par4, par5, par6, par7, par8, par9, par10);

        this.vertexPositions = new PositionTextureVertex[8];
        float var11 = par4 + (float)par7;
        float var12 = par5 + (float)par8;
        float var13 = par6 + (float)par9;
        par4 -= par10;
        par5 -= par10;
        par6 -= par10;
        var11 += par10;
        var12 += par10;
        var13 += par10;

        if (par1ModelRenderer.mirror)
        {
            float var14 = var11;
            var11 = par4;
            par4 = var14;
        }

        PositionTextureVertex var23 = new PositionTextureVertex(par4, par5, par6, 0.0F, 0.0F);
        PositionTextureVertex var15 = new PositionTextureVertex(var11, par5, par6, 0.0F, 8.0F);
        PositionTextureVertex var16 = new PositionTextureVertex(var11, var12, par6, 8.0F, 8.0F);
        PositionTextureVertex var17 = new PositionTextureVertex(par4, var12, par6, 8.0F, 0.0F);
        PositionTextureVertex var18 = new PositionTextureVertex(par4, par5, var13, 0.0F, 0.0F);
        PositionTextureVertex var19 = new PositionTextureVertex(var11, par5, var13, 0.0F, 8.0F);
        PositionTextureVertex var20 = new PositionTextureVertex(var11, var12, var13, 8.0F, 8.0F);
        PositionTextureVertex var21 = new PositionTextureVertex(par4, var12, var13, 8.0F, 0.0F);
        this.vertexPositions[0] = var23;
        this.vertexPositions[1] = var15;
        this.vertexPositions[2] = var16;
        this.vertexPositions[3] = var17;
        this.vertexPositions[4] = var18;
        this.vertexPositions[5] = var19;
        this.vertexPositions[6] = var20;
        this.vertexPositions[7] = var21;
        
        if(position == EnumPlanePosition.LEFT)
        	this.quad = new TexturedQuad(new PositionTextureVertex[] {var19, var15, var16, var20}, textureOffsetX, textureOffsetY, textureOffsetX + par9, textureOffsetY + par8, par1ModelRenderer.textureWidth, par1ModelRenderer.textureHeight);
        //this.quad = new TexturedQuad(new PositionTextureVertex[] {var23, var18, var21, var17}, textureOffsetX, textureOffsetY + par9, textureOffsetX + par9, textureOffsetY + par9 + par8, par1ModelRenderer.textureWidth, par1ModelRenderer.textureHeight);
        
        if(position == EnumPlanePosition.TOP)
        	this.quad = new TexturedQuad(new PositionTextureVertex[] {var19, var18, var23, var15}, textureOffsetX, textureOffsetY, textureOffsetX + par7, textureOffsetY + par9, par1ModelRenderer.textureWidth, par1ModelRenderer.textureHeight);
        //this.quad = new TexturedQuad(new PositionTextureVertex[] {var16, var17, var21, var20}, textureOffsetX + par9 + par7, textureOffsetY + par9, textureOffsetX + par9 + par7 + par7, textureOffsetY, par1ModelRenderer.textureWidth, par1ModelRenderer.textureHeight);
        
        if(position == EnumPlanePosition.BACK)
        	this.quad = new TexturedQuad(new PositionTextureVertex[] {var15, var23, var17, var16}, textureOffsetX, textureOffsetY, textureOffsetX + par7, textureOffsetY + par8, par1ModelRenderer.textureWidth, par1ModelRenderer.textureHeight);
        //this.quad = new TexturedQuad(new PositionTextureVertex[] {var18, var19, var20, var21}, textureOffsetX + par9 + par7, textureOffsetY + par9, textureOffsetX + par9 + par7 + par7, textureOffsetY + par9 + par8, par1ModelRenderer.textureWidth, par1ModelRenderer.textureHeight);
        
        if (par1ModelRenderer.mirror)
        	quad.flipFace();
	}
	
    @Override
    public void render(Tessellator par1Tessellator, float par2)
    {
    	quad.draw(par1Tessellator, par2);
    }
}
