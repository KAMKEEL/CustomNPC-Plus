package noppes.npcs.client.renderer.blocks;

import cpw.mods.fml.client.registry.RenderingRegistry;
import net.minecraft.block.Block;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.tileentity.TileEntity;
import noppes.npcs.CustomItems;
import noppes.npcs.blocks.BlockCouchWool;
import noppes.npcs.blocks.tiles.TileCouchWool;
import noppes.npcs.client.model.blocks.couch.*;
import noppes.npcs.client.model.blocks.legacy.couch.*;
import noppes.npcs.config.ConfigClient;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public class BlockCouchWoolRenderer extends BlockRendererInterface{

    private final ModelBase modelCouchLeft = new ModelCouchLeft();
    private final ModelBase modelCouchRight = new ModelCouchRight();
    private final ModelBase modelCouchCorner = new ModelCouchCorner();
    private final ModelBase modelCouch = new ModelCouch();
    private final ModelBase modelCouchSingle = new ModelCouchSingle();

	private final ModelBase modelLegacyCouchMiddle = new ModelLegacyCouchMiddle();
	private final ModelBase modelLegacyCouchMiddleWool = new ModelLegacyCouchMiddleWool();

	private final ModelBase modelLegacyCouchLeft = new ModelLegacyCouchLeft();
	private final ModelBase modelLegacyCouchLeftWool = new ModelLegacyCouchLeftWool();

	private final ModelBase modelLegacyCouchRight = new ModelLegacyCouchRight();
	private final ModelBase modelLegacyCouchRightWool = new ModelLegacyCouchRightWool();

	private final ModelBase modelLegacyCouchCorner = new ModelLegacyCouchCorner();
	private final ModelBase modelLegacyCouchCornerWool = new ModelLegacyCouchCornerWool();

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
        //GL11.glS calef(0.95f, 0.95f, 0.95f);
        GL11.glRotatef(180, 0, 0, 1);
        GL11.glRotatef(90 * tile.rotation, 0, 1, 0);
        GL11.glColor3f(1, 1, 1);

        if(false){
            setWoodTexture(var1.getBlockMetadata());
            if(tile.hasCornerLeft)
                modelLegacyCouchCorner.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);
            else if(tile.hasCornerRight){
                GL11.glRotatef(90, 0, 1, 0);
                modelLegacyCouchCorner.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);
            }
            else if(tile.hasLeft && tile.hasRight)
                modelLegacyCouchMiddle.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);
            else if(tile.hasLeft)
                modelLegacyCouchLeft.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);
            else if(tile.hasRight)
                modelLegacyCouchRight.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);
            else
                modelLegacyCouchMiddle.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);

            this.bindTexture(BlockTallLampRenderer.resourceTop);
            float[] color = BlockBannerRenderer.colorTable[tile.color];
            GL11.glColor3f(color[0], color[1], color[2]);

            if(tile.hasCornerLeft || tile.hasCornerRight)
                modelLegacyCouchCornerWool.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);
            else if(tile.hasLeft && tile.hasRight)
                modelLegacyCouchMiddleWool.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);
            else if(tile.hasLeft)
                modelLegacyCouchLeftWool.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);
            else if(tile.hasRight)
                modelLegacyCouchRightWool.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);
            else
                modelLegacyCouchMiddleWool.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);
        } else {
            setWoodTexture(var1.getBlockMetadata());
            if(tile.hasCornerLeft)
                modelCouchCorner.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);
            else if(tile.hasCornerRight){
                GL11.glRotatef(90, 0, 1, 0);
                modelCouchCorner.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);
            }
            else if(tile.hasLeft && tile.hasRight)
                modelCouch.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);
            else if(tile.hasLeft)
                modelCouchLeft.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);
            else if(tile.hasRight)
                modelCouchRight.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);
            else
                modelCouchSingle.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);
        }

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
        modelLegacyCouchMiddle.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);

        this.bindTexture(BlockTallLampRenderer.resourceTop);
        float[] color = BlockBannerRenderer.colorTable[15 - metadata];
        GL11.glColor3f(color[0], color[1], color[2]);
        modelLegacyCouchMiddleWool.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);
		GL11.glPopMatrix();
	}

	@Override
	public int getRenderId() {
		return CustomItems.couchWool.getRenderType();
	}
}
