package noppes.npcs.client.renderer.blocks;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.world.IBlockAccess;
import noppes.npcs.CustomItems;
import noppes.npcs.blocks.BlockBorder;
import noppes.npcs.blocks.tiles.TileBorder;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;

public class BlockBorderRenderer implements ISimpleBlockRenderingHandler{
    public BlockBorderRenderer(){
		((BlockBorder)CustomItems.border).renderId = RenderingRegistry.getNextAvailableRenderId();
    }

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z,
			Block block, int modelId, RenderBlocks renderer) {
		TileBorder tile = (TileBorder) world.getTileEntity(x, y, z);
		
		GL11.glPushMatrix();
		if(tile.rotation == 1)
			renderer.uvRotateTop = 1;
		else if(tile.rotation == 3)
			renderer.uvRotateTop = 2;
		else if(tile.rotation == 2)
			renderer.uvRotateTop = 3;
		
		renderer.renderStandardBlock(CustomItems.border, x, y, z);
		renderer.uvRotateTop = 0;
		GL11.glPopMatrix();
		return true;
	}

	@Override
	public boolean shouldRender3DInInventory(int modelId) {
		return false;
	}


	@Override
	public int getRenderId() {
		return CustomItems.border.getRenderType();
	}

	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelId,
			RenderBlocks renderer) {
	}
}
