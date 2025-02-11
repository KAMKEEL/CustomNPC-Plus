package noppes.npcs.client.renderer.blocks;

import cpw.mods.fml.client.registry.RenderingRegistry;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.CustomItems;
import noppes.npcs.blocks.BlockBarrel;
import noppes.npcs.blocks.tiles.TileColorable;
import noppes.npcs.client.model.blocks.barrel.ModelBarrel;
import noppes.npcs.client.model.blocks.legacy.ModelLegacyBarrel;
import noppes.npcs.client.model.blocks.legacy.ModelLegacyBarrelLid;
import noppes.npcs.config.ConfigClient;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public class BlockBarrelRenderer extends BlockRendererInterface{

    // Legacy
	private final ModelLegacyBarrel legacyModel = new ModelLegacyBarrel();
	private final ModelLegacyBarrelLid legacyLid = new ModelLegacyBarrelLid();
	private static final ResourceLocation legacy_texture = new ResourceLocation("customnpcs","textures/models/Barrel.png");


    // Modern
    private final ModelBarrel barrel = new ModelBarrel();
    private static final ResourceLocation trimTexture = new ResourceLocation("customnpcs","textures/models/barrel/trim.png");

    private static final ResourceLocation oak_lid = new ResourceLocation("customnpcs","textures/models/barrel/oak_lid.png");
    private static final ResourceLocation spruce_lid = new ResourceLocation("customnpcs","textures/models/barrel/oak_lid.png");
    private static final ResourceLocation birch_lid = new ResourceLocation("customnpcs","textures/models/barrel/oak_lid.png");
    private static final ResourceLocation jungle_lid = new ResourceLocation("customnpcs","textures/models/barrel/oak_lid.png");
    private static final ResourceLocation acacia_lid = new ResourceLocation("customnpcs","textures/models/barrel/oak_lid.png");
    private static final ResourceLocation dark_oak_lid = new ResourceLocation("customnpcs","textures/models/barrel/oak_lid.png");

	public BlockBarrelRenderer(){
		((BlockBarrel)CustomItems.barrel).renderId = RenderingRegistry.getNextAvailableRenderId();
		RenderingRegistry.registerBlockHandler(this);
	}

	@Override
	public void renderTileEntityAt(TileEntity var1, double var2, double var4,
			double var6, float var8) {
		TileColorable tile = (TileColorable) var1;
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glPushMatrix();


        if(ConfigClient.LegacyBarrel){
            GL11.glTranslatef((float)var2 + 0.5f, (float)var4 + 1.42f, (float)var6 + 0.5f);
            GL11.glScalef(1.2f, 0.94f,1.2f);
            GL11.glRotatef(180, 0, 0, 1);
            GL11.glRotatef(45 * tile.rotation, 0, 1, 0);
            GL11.glColor3f(1, 1, 1);

            GL11.glEnable(GL11.GL_CULL_FACE);

            setWoodTexture(var1.getBlockMetadata());
            legacyModel.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);
            Minecraft.getMinecraft().getTextureManager().bindTexture(legacy_texture);
            legacyLid.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);
        }
        else {
            GL11.glTranslatef((float)var2 + 0.5f, (float)var4 + 1.5f, (float)var6 + 0.5f);
            GL11.glScalef(1.0f, 1.0f,1.0f);
            GL11.glRotatef(180, 0, 0, 1);
            GL11.glRotatef(45 * tile.rotation, 0, 1, 0);
            GL11.glColor3f(1, 1, 1);

            GL11.glEnable(GL11.GL_CULL_FACE);

            setWoodTexture(var1.getBlockMetadata());
            barrel.renderWall(0.0625F);
            Minecraft.getMinecraft().getTextureManager().bindTexture(trimTexture);
            barrel.renderTrim(0.0625F);
            setLidTexture(var1.getBlockMetadata());
            barrel.renderBase(0.0625F);
        }

		GL11.glPopMatrix();
	}

	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelId,
			RenderBlocks renderer) {
        GL11.glPushMatrix();
        if(ConfigClient.LegacyBarrel){
            GL11.glTranslatef(0, 0.75f, 0);
            GL11.glScalef(0.7f, 0.7f, 0.7f);
            GL11.glRotatef(180, 0, 0, 1);
            GL11.glRotatef(180, 0, 1, 0);

            setWoodTexture(metadata);
            GL11.glColor3f(1, 1, 1);
            legacyModel.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);

            Minecraft.getMinecraft().getTextureManager().bindTexture(legacy_texture);
            legacyLid.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);
        }
        else {
            GL11.glTranslatef(0, 0.9f, 0);
            GL11.glScalef(0.9f, 0.9f, 0.9f);
            GL11.glRotatef(180, 0, 0, 1);
            GL11.glRotatef(180, 0, 1, 0);

            setWoodTexture(metadata);
            GL11.glColor3f(1, 1, 1);
            barrel.renderWall(0.0625F);
            Minecraft.getMinecraft().getTextureManager().bindTexture(trimTexture);
            barrel.renderTrim(0.0625F);
            setLidTexture(metadata);
            barrel.renderBase(0.0625F);
        }

		GL11.glPopMatrix();
	}

    public void setLidTexture(int meta){
        TextureManager manager = Minecraft.getMinecraft().getTextureManager();
        if(meta == 1)
            manager.bindTexture(oak_lid);
        else if(meta == 2)
            manager.bindTexture(oak_lid);
        else if(meta == 3)
            manager.bindTexture(oak_lid);
        else if(meta == 4)
            manager.bindTexture(oak_lid);
        else if(meta == 5)
            manager.bindTexture(oak_lid);
        else
            manager.bindTexture(oak_lid);
    }


    @Override
	public int getRenderId() {
		return CustomItems.barrel.getRenderType();
	}
}
