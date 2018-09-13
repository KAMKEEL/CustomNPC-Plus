package noppes.npcs.client.renderer.blocks;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import noppes.npcs.CustomItems;
import noppes.npcs.blocks.BlockWallBanner;
import noppes.npcs.blocks.tiles.TileWallBanner;
import noppes.npcs.client.model.blocks.ModelWallBanner;
import noppes.npcs.client.model.blocks.ModelWallBannerFlag;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import cpw.mods.fml.client.registry.RenderingRegistry;

public class BlockWallBannerRenderer extends BlockRendererInterface{

	private final ModelWallBanner model = new ModelWallBanner();
	private final ModelWallBannerFlag flag = new ModelWallBannerFlag();
    
    public BlockWallBannerRenderer(){
		((BlockWallBanner)CustomItems.wallBanner).renderId = RenderingRegistry.getNextAvailableRenderId();
		RenderingRegistry.registerBlockHandler(this);
    }

	@Override
	public void renderTileEntityAt(TileEntity var1, double var2, double var4,
			double var6, float var8) {
		TileWallBanner tile = (TileWallBanner) var1;
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glPushMatrix();
        GL11.glTranslatef((float)var2 + 0.5f, (float)var4 +0.4f, (float)var6 + 0.5f);
        //GL11.glScalef(0.95f, 0.95f, 0.95f);
        GL11.glRotatef(180, 0, 0, 1);
        GL11.glRotatef(90 * tile.rotation, 0, 1, 0);
        
        setMaterialTexture(var1.getBlockMetadata());
        GL11.glColor3f(1, 1, 1);
        model.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);

        this.bindTexture(BlockBannerRenderer.resourceFlag);
        float[] color = BlockBannerRenderer.colorTable[tile.color];
        GL11.glColor3f(color[0], color[1], color[2]);
        flag.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);

		GL11.glPopMatrix();
        GL11.glColor3f(1, 1, 1);
        if(tile.icon != null && !this.playerTooFar(tile)){
        	doRender(var2, var4, var6, tile.rotation, tile.icon);
        }
	}
    public void doRender(double par2, double par4, double par6, int meta, ItemStack iicon)
    {
        if (iicon.getItemSpriteNumber() == 0 && RenderBlocks.renderItemIn3d(Block.getBlockFromItem(iicon.getItem()).getRenderType()))
        	return;
        GL11.glPushMatrix();
        bindTexture(TextureMap.locationItemsTexture);
        GL11.glTranslatef((float)par2 + 0.5f, (float)par4 +0.2f, (float)par6 + 0.5f);
        GL11.glRotatef(180, 0, 0, 1);
    	GL11.glRotatef(90 * meta, 0, 1, 0);
        GL11.glTranslatef(0, 0, 0.26f);
        GL11.glDepthMask(false);
        float f2 = 0.05f;
        Minecraft mc = Minecraft.getMinecraft();
        GL11.glScalef(f2, f2, f2);
    	renderer.renderItemIntoGUI(this.func_147498_b(), mc.renderEngine, iicon, -8, -8);
        GL11.glDepthMask(true);
        GL11.glPopMatrix();
    }
    


	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelId,
			RenderBlocks renderer) {
        GL11.glPushMatrix();
        GL11.glTranslatef(0, 0.26f, 0.3f);
        GL11.glScalef(0.95f, 0.85f, 0.95f);
        GL11.glRotatef(180, 0, 0, 1);
        GL11.glRotatef(180, 0, 1, 0);

        setMaterialTexture(metadata);
        GL11.glColor3f(1, 1, 1);
        model.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);

        this.bindTexture(BlockBannerRenderer.resourceFlag);
        float[] color = BlockBannerRenderer.colorTable[15 - metadata];
        GL11.glColor3f(color[0], color[1], color[2]);
        flag.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);
		GL11.glPopMatrix();
	}

	@Override
	public int getRenderId() {
		return CustomItems.wallBanner.getRenderType();
	}
	
	public int specialRenderDistance(){
		return 26;
	}
}
