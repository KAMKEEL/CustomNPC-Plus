package noppes.npcs.client.renderer.blocks;

import net.minecraft.block.Block;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import noppes.npcs.CustomItems;
import noppes.npcs.blocks.BlockCouchWool;
import noppes.npcs.blocks.tiles.TileCouchWool;
import noppes.npcs.client.model.blocks.ModelCouchCorner;
import noppes.npcs.client.model.blocks.ModelCouchCornerWool;
import noppes.npcs.client.model.blocks.ModelCouchLeft;
import noppes.npcs.client.model.blocks.ModelCouchLeftWool;
import noppes.npcs.client.model.blocks.ModelCouchMiddle;
import noppes.npcs.client.model.blocks.ModelCouchMiddleWool;
import noppes.npcs.client.model.blocks.ModelCouchRight;
import noppes.npcs.client.model.blocks.ModelCouchRightWool;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;

public class BlockCouchWoolRenderer extends BlockRendererInterface{

	private final ModelBase model = new ModelCouchMiddle();
	private final ModelBase model2 = new ModelCouchMiddleWool();

	private final ModelBase modelLeft = new ModelCouchLeft();
	private final ModelBase modelLeft2 = new ModelCouchLeftWool();

	private final ModelBase modelRight = new ModelCouchRight();
	private final ModelBase modelRight2 = new ModelCouchRightWool();

	private final ModelBase modelCorner = new ModelCouchCorner();
	private final ModelBase modelCorner2 = new ModelCouchCornerWool();
	
    public BlockCouchWoolRenderer(){
		((BlockCouchWool)CustomItems.couchWool).renderId = RenderingRegistry.getNextAvailableRenderId();
		RenderingRegistry.registerBlockHandler(this);
    }
	@Override
	public void renderTileEntityAt(TileEntity var1, double var2, double var4,
			double var6, float var8) {
		TileCouchWool tile = (TileCouchWool) var1;
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glPushMatrix();
        GL11.glTranslatef((float)var2 + 0.5f, (float)var4 + 1.5f, (float)var6 + 0.5f);
        //GL11.glScalef(0.95f, 0.95f, 0.95f);
        GL11.glRotatef(180, 0, 0, 1);
        GL11.glRotatef(90 * tile.rotation, 0, 1, 0);
        GL11.glColor3f(1, 1, 1);
        
        setWoodTexture(var1.getBlockMetadata());
        if(tile.hasCornerLeft)
        	modelCorner.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);
        else if(tile.hasCornerRight){
            GL11.glRotatef(90, 0, 1, 0);
        	modelCorner.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);
        }
        else if(tile.hasLeft && tile.hasRight)
        	model.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);
        else if(tile.hasLeft)
        	modelLeft.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);
        else if(tile.hasRight)
        	modelRight.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);
        else
        	model.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);

        this.bindTexture(BlockTallLampRenderer.resourceTop);
        float[] color = BlockBannerRenderer.colorTable[tile.color];
        GL11.glColor3f(color[0], color[1], color[2]);

        if(tile.hasCornerLeft || tile.hasCornerRight)
        	modelCorner2.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);
        else if(tile.hasLeft && tile.hasRight)
        	model2.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);
        else if(tile.hasLeft)
        	modelLeft2.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);
        else if(tile.hasRight)
        	modelRight2.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);
        else
        	model2.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);

		GL11.glPopMatrix();
	}

	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelId,
			RenderBlocks renderer) {
        GL11.glPushMatrix();
        GL11.glTranslatef(0, 0.9f, 0.1f);
        GL11.glScalef(0.9f, 0.9f, 0.9f);
        GL11.glRotatef(180, 0, 0, 1);
        GL11.glRotatef(180, 0, 1, 0);

        setWoodTexture(metadata);
        GL11.glColor3f(1, 1, 1);
        model.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);

        this.bindTexture(BlockTallLampRenderer.resourceTop);
        float[] color = BlockBannerRenderer.colorTable[15 - metadata];
        GL11.glColor3f(color[0], color[1], color[2]);
        model2.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);
		GL11.glPopMatrix();
	}

	@Override
	public int getRenderId() {
		return CustomItems.couchWool.getRenderType();
	}
}
