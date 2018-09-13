package noppes.npcs.client.renderer.blocks;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBook;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.CustomItems;
import noppes.npcs.blocks.BlockCrate;
import noppes.npcs.blocks.BlockRotated;
import noppes.npcs.blocks.tiles.TileColorable;
import noppes.npcs.client.model.blocks.ModelCrate;
import noppes.npcs.client.model.blocks.ModelInk;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import cpw.mods.fml.client.registry.RenderingRegistry;

public class BlockBookRenderer extends BlockRendererInterface{

	private final ModelInk ink = new ModelInk();

    private final ResourceLocation resource = new ResourceLocation("textures/entity/enchanting_table_book.png");
    private final ResourceLocation resource2 = new ResourceLocation("customnpcs:textures/models/Ink.png");
    private final ModelBook book = new ModelBook();
	
	public BlockBookRenderer(){
		((BlockRotated)CustomItems.book).renderId = RenderingRegistry.getNextAvailableRenderId();
		RenderingRegistry.registerBlockHandler(this);
	}
	@Override
	public void renderTileEntityAt(TileEntity var1, double var2, double var4,
			double var6, float var8) {
		TileColorable tile = (TileColorable) var1;
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glPushMatrix();
        GL11.glTranslatef((float)var2 + 0.5f, (float)var4 + 1.5f, (float)var6 + 0.5f);
        //GL11.glScalef(0.85f, 0.85f, 0.7f);
        GL11.glRotatef(180, 0, 0, 1);
        GL11.glRotatef(90 * tile.rotation - 90, 0, 1, 0);
        GL11.glColor3f(1, 1, 1);

    	TextureManager manager = Minecraft.getMinecraft().getTextureManager();
    	manager.bindTexture(resource2);
    	if(!playerTooFar(tile)){
	        GL11.glEnable(GL11.GL_BLEND);
	        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    	}
        ink.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);
    	manager.bindTexture(resource);
        GL11.glRotatef(-90, 0, 0, 1);
        GL11.glTranslatef(-1.49f, -0.18f, 0);
        book.render(null, 0, 0, 1, 1.24f, 1.0F, 0.0625F);
        GL11.glDisable(GL11.GL_BLEND);
		GL11.glPopMatrix();
	}

	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelId,
			RenderBlocks renderer) {
        GL11.glPushMatrix();
        GL11.glTranslatef(0.2f, 1.7f, 0);
        GL11.glScalef(1.4f, 1.4f, 1.4f);
        GL11.glRotatef(180, 0, 0, 1);
        GL11.glRotatef(180, 0, 1, 0);
        GL11.glRotatef( -90, 0, 1, 0);

        GL11.glColor3f(1, 1, 1);
        GL11.glEnable(GL11.GL_CULL_FACE);
    	TextureManager manager = Minecraft.getMinecraft().getTextureManager();
    	manager.bindTexture(resource2);
        ink.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);
    	manager.bindTexture(resource);
        GL11.glRotatef(-90, 0, 0, 1);
        GL11.glTranslatef(-1.45f, -0.18f, 0);
        book.render(null, 0, 0, 1, 1.24f, 1.0F, 0.0625F);
        
		GL11.glPopMatrix();
	}

	@Override
	public int getRenderId() {
		return CustomItems.book.getRenderType();
	}
}
