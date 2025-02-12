package noppes.npcs.client.renderer.blocks;

import cpw.mods.fml.client.registry.RenderingRegistry;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.CustomItems;
import noppes.npcs.blocks.BlockTable;
import noppes.npcs.blocks.tiles.TileColorable;
import noppes.npcs.client.model.blocks.ModelTable;
import noppes.npcs.client.model.blocks.legacy.ModelLegacyTable;
import noppes.npcs.config.ConfigClient;
import org.lwjgl.opengl.GL11;

public class BlockTableRenderer extends BlockRendererInterface{

	private final ModelLegacyTable legacyTable = new ModelLegacyTable();
    private final ModelTable table = new ModelTable();

    private static final ResourceLocation oak = new ResourceLocation("customnpcs","textures/models/table/oak.png");
	private static final ResourceLocation spruce = new ResourceLocation("customnpcs","textures/models/table/spruce.png");
	private static final ResourceLocation birch = new ResourceLocation("customnpcs","textures/models/table/birch.png");
	private static final ResourceLocation jungle = new ResourceLocation("customnpcs","textures/models/table/jungle.png");
	private static final ResourceLocation acacia = new ResourceLocation("customnpcs","textures/models/table/acacia.png");
	private static final ResourceLocation dark_oak = new ResourceLocation("customnpcs","textures/models/table/dark_oak.png");

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

        legacyTable.Shape1.showModel = table.Shape1.showModel = !south && !east;
        legacyTable.Shape3.showModel = table.Shape3.showModel = !north && !west;
        legacyTable.Shape4.showModel = table.Shape4.showModel = !north && !east;
        legacyTable.Shape5.showModel = table.Shape5.showModel = !south && !west;

        if(ConfigClient.LegacyTable){
            setWoodTexture(var1.getBlockMetadata());
            legacyTable.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);
            GL11.glRotatef(90 * tile.rotation, 0, 1, 0);
            legacyTable.Table.render(0.0625f);
        } else {
            GL11.glEnable(GL11.GL_ALPHA_TEST);
            setTableTexture(var1.getBlockMetadata());
            table.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);
            GL11.glRotatef(90 * tile.rotation, 0, 1, 0);
            table.Table.render(0.0625f);
            GL11.glDisable(GL11.GL_ALPHA_TEST);
        }

		GL11.glPopMatrix();
	}

	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelId,
			RenderBlocks renderer) {
        GL11.glPushMatrix();
        GL11.glTranslatef(0, 0.9f, 0);
        GL11.glRotatef(180, 0, 0, 1);
        GL11.glRotatef(180, 0, 1, 0);

        if(ConfigClient.LegacyTable){
            setWoodTexture(metadata);
            GL11.glColor3f(1, 1, 1);
            legacyTable.Table.render(0.0625f);
            legacyTable.Shape1.showModel = true;
            legacyTable.Shape3.showModel = true;
            legacyTable.Shape4.showModel = true;
            legacyTable.Shape5.showModel = true;
            legacyTable.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);
        } else {
            setTableTexture(metadata);
            GL11.glColor3f(1, 1, 1);
            table.Table.render(0.0625f);
            table.Shape1.showModel = true;
            table.Shape3.showModel = true;
            table.Shape4.showModel = true;
            table.Shape5.showModel = true;
            table.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);
        }

		GL11.glPopMatrix();
	}

    public void setTableTexture(int meta){
        TextureManager manager = Minecraft.getMinecraft().getTextureManager();
        if(meta == 1)
            manager.bindTexture(spruce);
        else if(meta == 2)
            manager.bindTexture(birch);
        else if(meta == 3)
            manager.bindTexture(jungle);
        else if(meta == 4)
            manager.bindTexture(acacia);
        else if(meta == 5)
            manager.bindTexture(dark_oak);
        else
            manager.bindTexture(oak);
    }

	@Override
	public int getRenderId() {
		return CustomItems.table.getRenderType();
	}
}
