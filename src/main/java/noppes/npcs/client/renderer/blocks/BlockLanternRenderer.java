package noppes.npcs.client.renderer.blocks;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IBlockAccess;
import noppes.npcs.CustomItems;
import noppes.npcs.blocks.BlockLantern;
import noppes.npcs.blocks.tiles.TileLamp;
import noppes.npcs.client.model.blocks.lantern.LanternCeiling;
import noppes.npcs.client.model.blocks.lantern.LanternFloor;
import noppes.npcs.client.model.blocks.lantern.LanternWall;
import noppes.npcs.client.model.blocks.legacy.ModelLegacyLantern;
import noppes.npcs.client.model.blocks.legacy.ModelLegacyCeiling;
import noppes.npcs.client.model.blocks.legacy.ModelLegacyWall;
import noppes.npcs.config.ConfigClient;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public class BlockLanternRenderer extends TileEntitySpecialRenderer implements ISimpleBlockRenderingHandler{

	private final ModelLegacyLantern modelLegacyLantern = new ModelLegacyLantern();
	private final ModelLegacyCeiling modelLegacyCeiling = new ModelLegacyCeiling();
	private final ModelLegacyWall modelLegacyWall = new ModelLegacyWall();
	private static final ResourceLocation legacyTexture = new ResourceLocation("customnpcs","textures/models/legacy/Lamp.png");

    private final LanternFloor modelLantern = new LanternFloor();
    private final LanternWall modelLanternWall = new LanternWall();
    private final LanternCeiling modelLanternCeiling = new LanternCeiling();
    private static final ResourceLocation texture = new ResourceLocation("customnpcs","textures/models/lantern.png");

    public BlockLanternRenderer(){
		((BlockLantern)CustomItems.lantern).renderId = RenderingRegistry.getNextAvailableRenderId();
		((BlockLantern)CustomItems.lantern_unlit).renderId = ((BlockLantern)CustomItems.lantern).renderId;
		RenderingRegistry.registerBlockHandler(this);
    }

	@Override
	public void renderTileEntityAt(TileEntity var1, double var2, double var4,
			double var6, float var8) {
		TileLamp tile = (TileLamp) var1;
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glPushMatrix();
        GL11.glTranslatef((float)var2 + 0.5f, (float)var4 + 1.5f, (float)var6 + 0.5f);
        GL11.glRotatef(180, 0, 0, 1);
        GL11.glColor3f(1, 1, 1);
        GL11.glEnable(GL11.GL_ALPHA_TEST);

        if(ConfigClient.LegacyLantern){
            GL11.glRotatef(45 * tile.rotation, 0, 1, 0);
            Minecraft.getMinecraft().getTextureManager().bindTexture(legacyTexture);
            if(tile.color == 0)
                modelLegacyLantern.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);
            else if(tile.color == 1)
                modelLegacyCeiling.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);
            else
                modelLegacyWall.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);
        } else {
            Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
            if(tile.color == 0){
                GL11.glRotatef(45 * tile.rotation, 0, 1, 0);
                modelLantern.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);
            }
            else if(tile.color == 1){
                GL11.glRotatef(90, 0, 1, 0);
                modelLanternCeiling.Chain.render(0.0625F);
                GL11.glRotatef(45 * tile.rotation, 0, 1, 0);
                modelLanternCeiling.Lantern.render(0.0625F);
                modelLanternCeiling.Light.render(0.0625F);
            }
            else {
                GL11.glRotatef(45 * tile.rotation, 0, 1, 0);
                GL11.glTranslatef(0, 0.3f, 0);
                modelLanternWall.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);
            }
        }
        GL11.glDisable(GL11.GL_ALPHA_TEST);


		GL11.glPopMatrix();
	}

	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelId,
			RenderBlocks renderer) {
        GL11.glPushMatrix();
        GL11.glTranslatef(0, 2.2f, 0);
        GL11.glScalef(2f, 2f, 2f);
        GL11.glRotatef(180, 0, 0, 1);
        GL11.glRotatef(180, 0, 1, 0);

        if(ConfigClient.LegacyLantern){
            Minecraft.getMinecraft().getTextureManager().bindTexture(legacyTexture);
            GL11.glColor3f(1, 1, 1);
            modelLegacyLantern.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);
        }
        else {
            GL11.glScalef(0.7f, 0.7f, 0.7f);
            GL11.glTranslatef(0, 0.4f, 0);
            Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
            GL11.glColor3f(1, 1, 1);
            modelLantern.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);
        }

		GL11.glPopMatrix();
	}


	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z,
			Block block, int modelId, RenderBlocks renderer) {
		return false;
	}


	@Override
	public boolean shouldRender3DInInventory(int modelId) {
        return ConfigClient.LegacyLantern;
	}


	@Override
	public int getRenderId() {
		return CustomItems.lantern.getRenderType();
	}
}
