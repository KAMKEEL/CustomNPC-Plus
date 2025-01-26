package noppes.npcs.client.renderer.blocks;

import cpw.mods.fml.client.registry.RenderingRegistry;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import noppes.npcs.CustomItems;
import noppes.npcs.blocks.BlockRotated;
import noppes.npcs.blocks.tiles.TileColorable;
import noppes.npcs.client.model.blocks.ModelCampfire;
import noppes.npcs.client.model.blocks.campfire.ModelCampfireFlame;
import noppes.npcs.client.model.blocks.campfire.ModelCampfireLog;
import noppes.npcs.client.model.blocks.campfire.ModelCampfireStone;
import noppes.npcs.config.ConfigClient;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public class BlockCampfireRenderer extends BlockRendererInterface{

    // Legacy
	private final ModelCampfire legacy_model = new ModelCampfire();

    // Modern
    private final ModelCampfireFlame flameModel = new ModelCampfireFlame();
    private final ModelCampfireLog logsModel = new ModelCampfireLog();
    private final ModelCampfireStone stonesModel = new ModelCampfireStone();


    public BlockCampfireRenderer(){
		((BlockRotated)CustomItems.campfire).renderId = RenderingRegistry.getNextAvailableRenderId();
		((BlockRotated)CustomItems.campfire_unlit).renderId = ((BlockRotated)CustomItems.campfire).renderId;
		RenderingRegistry.registerBlockHandler(this);
    }

	@Override
	public void renderTileEntityAt(TileEntity var1, double var2, double var4,
			double var6, float var8) {
		TileColorable tile = (TileColorable) var1;
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glPushMatrix();
        GL11.glTranslatef((float)var2 + 0.5f, (float)var4 + 1.5f, (float)var6 + 0.5f);

        GL11.glRotatef(180, 0, 0, 1);
        GL11.glRotatef(45 * tile.rotation, 0, 1, 0);
        GL11.glColor3f(1, 1, 1);

        if(ConfigClient.LegacyCampfire){
            Minecraft.getMinecraft().getTextureManager().bindTexture(PlanksOak);
            legacy_model.renderLog(0.0625F);
            Minecraft.getMinecraft().getTextureManager().bindTexture(Stone);
            legacy_model.renderRock(0.0625F);
        }
        else {
            Block block = tile.getBlockType();

            Minecraft.getMinecraft().getTextureManager().bindTexture(Logs);
            logsModel.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);
            Minecraft.getMinecraft().getTextureManager().bindTexture(Cobble);
            stonesModel.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);
            if(block == CustomItems.campfire){
                Minecraft.getMinecraft().getTextureManager().bindTexture(Flame);
                flameModel.render(null, tile.getWorldObj().getWorldTime(), 0, 0, 0, 0.0F, 0.0625F);
            }
        }

		GL11.glPopMatrix();
	}

	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelId,
			RenderBlocks renderer) {
        GL11.glPushMatrix();
        GL11.glTranslatef(0, 1.2f, 0);
        GL11.glScalef(1f, 1f, 1f);
        GL11.glRotatef(180, 0, 0, 1);
        GL11.glRotatef(180, 0, 1, 0);
        GL11.glColor3f(1, 1, 1);

        if(ConfigClient.LegacyCampfire){
            Minecraft.getMinecraft().getTextureManager().bindTexture(PlanksOak);
            legacy_model.renderLog(0.0625F);
            Minecraft.getMinecraft().getTextureManager().bindTexture(Stone);
            legacy_model.renderRock(0.0625F);
        }
        else {
            Minecraft.getMinecraft().getTextureManager().bindTexture(Logs);
            logsModel.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);
            Minecraft.getMinecraft().getTextureManager().bindTexture(Cobble);
            stonesModel.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);
        }

		GL11.glPopMatrix();
	}

	@Override
	public int getRenderId() {
		return CustomItems.campfire.getRenderType();
	}
}
