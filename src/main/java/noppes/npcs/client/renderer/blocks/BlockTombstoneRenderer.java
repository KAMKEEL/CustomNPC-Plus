package noppes.npcs.client.renderer.blocks;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.tileentity.TileEntity;
import noppes.npcs.CustomItems;
import noppes.npcs.blocks.BlockTombstone;
import noppes.npcs.blocks.tiles.TileTombstone;
import noppes.npcs.client.TextBlockClient;
import noppes.npcs.client.model.blocks.ModelTombstone1;
import noppes.npcs.client.model.blocks.ModelTombstone2;
import noppes.npcs.client.model.blocks.ModelTombstone3;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import cpw.mods.fml.client.registry.RenderingRegistry;

public class BlockTombstoneRenderer extends BlockRendererInterface{

	private final ModelTombstone1 model = new ModelTombstone1();
	private final ModelTombstone2 model2 = new ModelTombstone2();
	private final ModelTombstone3 model3 = new ModelTombstone3();
   
    public BlockTombstoneRenderer(){
		((BlockTombstone)CustomItems.tombstone).renderId = RenderingRegistry.getNextAvailableRenderId();
		RenderingRegistry.registerBlockHandler(this);
    }

	@Override
	public void renderTileEntityAt(TileEntity var1, double var2, double var4,
			double var6, float var8) {
		TileTombstone tile = (TileTombstone) var1;
        int meta = tile.getBlockMetadata();
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glPushMatrix();
        GL11.glTranslatef((float)var2 + 0.5f, (float)var4 + 1.5f, (float)var6 + 0.5f);
        if(meta == 2)
        	GL11.glScalef(1f, 1f, 1.14f);
        GL11.glRotatef(180, 0, 0, 1);
        GL11.glRotatef(90 * tile.rotation, 0, 1, 0);
        GL11.glColor3f(1, 1, 1);
        
        
        Minecraft.getMinecraft().getTextureManager().bindTexture(Stone);
        if(meta == 0)
        	model.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);
        else if(meta == 1)
        	model2.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);
        else
        	model3.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);
        if(meta < 2 && !playerTooFar(tile))
        	renderText(tile, meta);

		GL11.glPopMatrix();
	}

	
	private void renderText(TileTombstone tile, int meta){
        
		if(tile.block == null || tile.hasChanged){
			tile.block = new TextBlockClient(tile.getText(), 94, true, Minecraft.getMinecraft().thePlayer);
			tile.hasChanged = false;
		}
		if(!tile.block.lines.isEmpty()){
	        GL11.glRotatef(180, 1, 0, 0);
	        float f3 = 0.0133F * 0.5f;
	        GL11.glTranslatef(0.0F, -0.64f, meta == 0?0.095F:0.126f);
	        GL11.glScalef(f3, -f3, f3);
	        GL11.glNormal3f(0.0F, 0.0F, -1.0F * f3);
	        GL11.glDepthMask(false);
	        FontRenderer fontrenderer = this.func_147498_b();
	        
	        float lineOffset = 0;
	        if(tile.block.lines.size() < 11)
	        	lineOffset = (11f - tile.block.lines.size()) / 2;
	    	for(int i = 0; i < tile.block.lines.size(); i++){
	    		String text = tile.block.lines.get(i).getFormattedText();
	    		fontrenderer.drawString(text, -fontrenderer.getStringWidth(text) / 2, (int)((lineOffset + i) * (fontrenderer.FONT_HEIGHT - 0.3)), 0xffffff);
	    		if(i == 13)
	    			break;
	    	}
	
	        GL11.glDepthMask(true);
	        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		}
	}
	

	@Override
	public void renderInventoryBlock(Block block, int meta, int modelId,
			RenderBlocks renderer) {
        GL11.glPushMatrix();
        GL11.glTranslatef(0, 1f, 0);
        GL11.glScalef(1f, 1f, 1f);
        GL11.glRotatef(180, 0, 0, 1);
        GL11.glRotatef(180, 0, 1, 0);

        Minecraft.getMinecraft().getTextureManager().bindTexture(Stone);
        GL11.glColor3f(1, 1, 1);
        if(meta == 0)
        	model.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);
        else if(meta == 1)
        	model2.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);
        else
        	model3.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);

		GL11.glPopMatrix();
	}


	@Override
	public int getRenderId() {
		return CustomItems.tombstone.getRenderType();
	}
}
