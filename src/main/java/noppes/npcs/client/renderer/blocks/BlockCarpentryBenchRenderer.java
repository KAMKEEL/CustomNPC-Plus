package noppes.npcs.client.renderer.blocks;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IBlockAccess;
import noppes.npcs.CustomItems;
import noppes.npcs.blocks.BlockCarpentryBench;
import noppes.npcs.client.model.blocks.ModelAnvil;
import noppes.npcs.client.model.blocks.ModelCarpentryBench;
import noppes.npcs.client.model.blocks.legacy.ModelLegacyCarpentryBench;
import noppes.npcs.config.ConfigClient;
import org.lwjgl.opengl.GL11;

public class BlockCarpentryBenchRenderer extends TileEntitySpecialRenderer implements ISimpleBlockRenderingHandler{

	private final ModelCarpentryBench modelCarpentryBench = new ModelCarpentryBench();
    private static final ResourceLocation carpentryBenchTexture = new ResourceLocation("customnpcs","textures/models/CarpentryBench.png");

	private final ModelAnvil anvil = new ModelAnvil();

    private static final ResourceLocation anvilTexture = new ResourceLocation("customnpcs","textures/models/Steel.png");

    private final ModelLegacyCarpentryBench legacyBench = new ModelLegacyCarpentryBench();
    private static final ResourceLocation legacyCarpentryBench = new ResourceLocation("customnpcs","textures/models/legacy/bench.png");

    public BlockCarpentryBenchRenderer(){
		((BlockCarpentryBench)CustomItems.carpentyBench).renderId = RenderingRegistry.getNextAvailableRenderId();
		RenderingRegistry.registerBlockHandler(this);
    }

	@Override
	public void renderTileEntityAt(TileEntity var1, double var2, double var4,
			double var6, float var8) {
		int meta = var1.getBlockMetadata();
        GL11.glPushMatrix();
        GL11.glTranslatef((float)var2 + 0.5f, (float)var4 +1.4f, (float)var6 + 0.5f);
        GL11.glScalef(0.95f, 0.95f, 0.95f);
        GL11.glRotatef(180, 0, 0, 1);
        GL11.glRotatef(90 * (meta % 4), 0, 1, 0);
        if(meta >= 4){
            this.bindTexture(anvilTexture);
        	anvil.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);
        }
        else {

            GL11.glEnable(GL11.GL_ALPHA_TEST);
            if(ConfigClient.LegacyCarpentryBench){
                this.bindTexture(legacyCarpentryBench);
                legacyBench.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);
            } else {
                this.bindTexture(carpentryBenchTexture);
                modelCarpentryBench.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);
            }
            GL11.glDisable(GL11.GL_ALPHA_TEST);
        }
		GL11.glPopMatrix();
	}


	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelId,
			RenderBlocks renderer) {
        GL11.glPushMatrix();
        GL11.glTranslatef(0, 0.85f, 0);
        GL11.glRotatef(180, 0, 0, 1);
        GL11.glRotatef(180, 0, 1, 0);

        GL11.glColor3f(1, 1, 1);
        if(metadata == 0){
            if(ConfigClient.LegacyCarpentryBench){
                this.bindTexture(legacyCarpentryBench);
                legacyBench.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);
            } else {
                this.bindTexture(carpentryBenchTexture);
                modelCarpentryBench.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);
            }
        }
        else{
            this.bindTexture(anvilTexture);
            anvil.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);
        }

		GL11.glPopMatrix();
	}


	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z,
			Block block, int modelId, RenderBlocks renderer) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public boolean shouldRender3DInInventory(int modelId) {
		return true;
	}


	@Override
	public int getRenderId() {
		return CustomItems.carpentyBench.getRenderType();
	}
}
