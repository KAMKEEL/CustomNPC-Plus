package noppes.npcs.client.renderer.blocks;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import noppes.npcs.CustomItems;
import noppes.npcs.blocks.BlockChair;
import noppes.npcs.blocks.BlockRotated;
import noppes.npcs.blocks.tiles.TileColorable;
import noppes.npcs.blocks.tiles.TileShelf;
import noppes.npcs.client.model.blocks.ModelShelf;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import cpw.mods.fml.client.registry.RenderingRegistry;

public class BlockShelfRenderer extends BlockRendererInterface{

	private final ModelShelf model = new ModelShelf();
    
	public BlockShelfRenderer(){
		((BlockRotated)CustomItems.shelf).renderId = RenderingRegistry.getNextAvailableRenderId();
		RenderingRegistry.registerBlockHandler(this);
	}
	@Override
	public void renderTileEntityAt(TileEntity var1, double var2, double var4,
			double var6, float var8) {
		TileColorable tile = (TileColorable) var1;
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glPushMatrix();
        GL11.glTranslatef((float)var2 + 0.5f, (float)var4 + 1.5f, (float)var6 + 0.5f);
        //GL11.glScalef(1.2f, 1.1f, 1.2f);
        GL11.glRotatef(180, 0, 0, 1);
        GL11.glRotatef(90 * tile.rotation, 0, 1, 0);
        GL11.glColor3f(1, 1, 1);
        
        boolean drawLeft = true;
        boolean drawRight = true;

        if(tile.rotation == 3){
        	drawLeft = shouldDraw(var1.getWorldObj(), var1.xCoord, var1.yCoord, var1.zCoord - 1, 3);
        	drawRight = shouldDraw(var1.getWorldObj(), var1.xCoord, var1.yCoord, var1.zCoord + 1, 3);
        }
        else if(tile.rotation == 1){
        	drawLeft = shouldDraw(var1.getWorldObj(), var1.xCoord, var1.yCoord, var1.zCoord + 1, 1);
        	drawRight = shouldDraw(var1.getWorldObj(), var1.xCoord, var1.yCoord, var1.zCoord - 1, 1);
        }
        else if(tile.rotation == 0){
        	drawLeft =  shouldDraw(var1.getWorldObj(), var1.xCoord + 1, var1.yCoord, var1.zCoord, 0);
        	drawRight =  shouldDraw(var1.getWorldObj(), var1.xCoord - 1, var1.yCoord, var1.zCoord, 0);
        }
        else if(tile.rotation == 2){
        	drawLeft =  shouldDraw(var1.getWorldObj(), var1.xCoord - 1, var1.yCoord, var1.zCoord, 2);
        	drawRight =  shouldDraw(var1.getWorldObj(), var1.xCoord + 1, var1.yCoord, var1.zCoord, 2);
        }
        
        
        model.SupportLeft1.showModel = model.SupportLeft2.showModel = drawLeft;
        model.SupportRight1.showModel = model.SupportRight2.showModel = drawRight;
        
        setWoodTexture(var1.getBlockMetadata());
        model.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);

		GL11.glPopMatrix();
	}
	private boolean shouldDraw(World world,int x, int y, int z, int rotation){
		TileEntity tile = world.getTileEntity(x, y, z);
		if(tile == null || !(tile instanceof TileShelf))
			return true;
		return ((TileShelf)tile).rotation != rotation;
	}

	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelId,
			RenderBlocks renderer) {
        GL11.glPushMatrix();
        GL11.glTranslatef(0, 0.6f, 0);
        GL11.glScalef(1f, 1f, 1f);
        GL11.glRotatef(180, 0, 0, 1);
        GL11.glRotatef(180, 0, 1, 0);

        setWoodTexture(metadata);
        GL11.glColor3f(1, 1, 1);
        model.SupportLeft1.showModel = model.SupportLeft2.showModel = true;
        model.SupportRight1.showModel = model.SupportRight2.showModel = true;
        model.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);
        
		GL11.glPopMatrix();
	}



	@Override
	public int getRenderId() {
		return CustomItems.shelf.getRenderType();
	}
}
