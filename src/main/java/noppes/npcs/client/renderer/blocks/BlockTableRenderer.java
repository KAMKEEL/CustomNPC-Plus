package noppes.npcs.client.renderer.blocks;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IBlockAccess;
import noppes.npcs.CustomItems;
import noppes.npcs.blocks.BlockTable;
import noppes.npcs.blocks.tiles.TileColorable;
import noppes.npcs.client.model.blocks.ModelTable;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import cpw.mods.fml.client.registry.RenderingRegistry;

public class BlockTableRenderer extends BlockRendererInterface{

	private final ModelTable model = new ModelTable();
	private static final ResourceLocation resource1 = new ResourceLocation("customnpcs","textures/cache/planks_oak.png");
	private static final ResourceLocation resource2 = new ResourceLocation("customnpcs","textures/cache/planks_big_oak.png");
	private static final ResourceLocation resource3 = new ResourceLocation("customnpcs","textures/cache/planks_spruce.png");
	private static final ResourceLocation resource4 = new ResourceLocation("customnpcs","textures/cache/planks_birch.png");
	private static final ResourceLocation resource5 = new ResourceLocation("customnpcs","textures/cache/planks_acacia.png");
	private static final ResourceLocation resource6 = new ResourceLocation("customnpcs","textures/cache/planks_jungle.png");
    
	public BlockTableRenderer(){
		((BlockTable)CustomItems.table).renderId = RenderingRegistry.getNextAvailableRenderId();
		RenderingRegistry.registerBlockHandler(this);
	}
	@Override
	public void renderTileEntityAt(TileEntity var1, double var2, double var4,
			double var6, float var8) {
		TileColorable tile = (TileColorable) var1;
        GL11.glPushMatrix();
        GL11.glTranslatef((float)var2 + 0.5f, (float)var4 + 1.5f, (float)var6 + 0.5f);
        GL11.glRotatef(180, 0, 0, 1);
        GL11.glColor3f(1, 1, 1);

        boolean south = var1.getWorldObj().getBlock(var1.xCoord + 1, var1.yCoord, var1.zCoord) == CustomItems.table;
        boolean north = var1.getWorldObj().getBlock(var1.xCoord - 1, var1.yCoord, var1.zCoord) == CustomItems.table;
        boolean east = var1.getWorldObj().getBlock(var1.xCoord, var1.yCoord, var1.zCoord + 1) == CustomItems.table;
        boolean west = var1.getWorldObj().getBlock(var1.xCoord, var1.yCoord, var1.zCoord - 1) == CustomItems.table;

        model.Shape1.showModel = !south && !east;
        model.Shape3.showModel = !north && !west;
        model.Shape4.showModel = !north && !east;
        model.Shape5.showModel = !south && !west;
        
        setWoodTexture(var1.getBlockMetadata());
        model.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);
        GL11.glRotatef(90 * tile.rotation, 0, 1, 0);
        model.Table.render(0.0625f);

		GL11.glPopMatrix();
	}

	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelId,
			RenderBlocks renderer) {
        GL11.glPushMatrix();
        GL11.glTranslatef(0, 0.9f, 0);
        GL11.glRotatef(180, 0, 0, 1);
        GL11.glRotatef(180, 0, 1, 0);

        setWoodTexture(metadata);
        GL11.glColor3f(1, 1, 1);
        model.Table.render(0.0625f);
        model.Shape1.showModel = true;
        model.Shape3.showModel = true;
        model.Shape4.showModel = true;
        model.Shape5.showModel = true;
        model.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);
        
		GL11.glPopMatrix();
	}

	@Override
	public int getRenderId() {
		return CustomItems.table.getRenderType();
	}
}
