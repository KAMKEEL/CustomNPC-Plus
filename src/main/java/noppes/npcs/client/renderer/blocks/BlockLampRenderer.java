package noppes.npcs.client.renderer.blocks;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IBlockAccess;
import noppes.npcs.CustomItems;
import noppes.npcs.blocks.BlockLamp;
import noppes.npcs.blocks.tiles.TileLamp;
import noppes.npcs.client.model.blocks.ModelLamp;
import noppes.npcs.client.model.blocks.ModelLampCeiling;
import noppes.npcs.client.model.blocks.ModelLampWall;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;

public class BlockLampRenderer extends TileEntitySpecialRenderer implements ISimpleBlockRenderingHandler{

	private final ModelLamp model = new ModelLamp();
	private final ModelLampCeiling model2 = new ModelLampCeiling();
	private final ModelLampWall model3 = new ModelLampWall();
	private static final ResourceLocation resource1 = new ResourceLocation("customnpcs","textures/models/Lamp.png");
   
    public BlockLampRenderer(){
		((BlockLamp)CustomItems.lamp).renderId = RenderingRegistry.getNextAvailableRenderId();
		((BlockLamp)CustomItems.lamp_unlit).renderId = ((BlockLamp)CustomItems.lamp).renderId;
		RenderingRegistry.registerBlockHandler(this);
    }

	@Override
	public void renderTileEntityAt(TileEntity var1, double var2, double var4,
			double var6, float var8) {
		TileLamp tile = (TileLamp) var1;
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glPushMatrix();
        GL11.glTranslatef((float)var2 + 0.5f, (float)var4 + 1.5f, (float)var6 + 0.5f);
        //GL11.glScalef(0.95f, 0.95f, 0.95f);
        GL11.glRotatef(180, 0, 0, 1);
        GL11.glRotatef(45 * tile.rotation, 0, 1, 0);
        GL11.glColor3f(1, 1, 1);
        
        Minecraft.getMinecraft().getTextureManager().bindTexture(resource1);
        if(tile.color == 0)
        	model.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);
        else if(tile.color == 1)
        	model2.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);
        else
        	model3.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);

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

        Minecraft.getMinecraft().getTextureManager().bindTexture(resource1);
        GL11.glColor3f(1, 1, 1);
        model.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);

		GL11.glPopMatrix();
	}


	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z,
			Block block, int modelId, RenderBlocks renderer) {
		return false;
	}


	@Override
	public boolean shouldRender3DInInventory(int modelId) {
		return true;
	}


	@Override
	public int getRenderId() {
		return CustomItems.lamp.getRenderType();
	}
}
