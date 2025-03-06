package noppes.npcs.client.renderer.blocks;

import cpw.mods.fml.client.registry.RenderingRegistry;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.CustomItems;
import noppes.npcs.blocks.BlockStool;
import noppes.npcs.blocks.tiles.TileVariant;
import noppes.npcs.client.model.blocks.ModelStool;
import noppes.npcs.client.model.blocks.legacy.ModelLegacyStool;
import noppes.npcs.config.ConfigClient;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public class BlockStoolRenderer extends BlockRendererInterface{

	private final ModelLegacyStool legacyStool = new ModelLegacyStool();
    private final ModelStool stool = new ModelStool();

    private static final ResourceLocation oak = new ResourceLocation("customnpcs","textures/models/stool/oak.png");
    private static final ResourceLocation spruce = new ResourceLocation("customnpcs","textures/models/stool/spruce.png");
    private static final ResourceLocation birch = new ResourceLocation("customnpcs","textures/models/stool/birch.png");
    private static final ResourceLocation jungle = new ResourceLocation("customnpcs","textures/models/stool/jungle.png");
    private static final ResourceLocation dark_oak = new ResourceLocation("customnpcs","textures/models/stool/dark_oak.png");
    private static final ResourceLocation acacia = new ResourceLocation("customnpcs","textures/models/stool/acacia.png");

	public BlockStoolRenderer(){
		((BlockStool)CustomItems.stool).renderId = RenderingRegistry.getNextAvailableRenderId();
		RenderingRegistry.registerBlockHandler(this);
	}
	@Override
	public void renderTileEntityAt(TileEntity var1, double var2, double var4,
			double var6, float var8) {
		TileVariant tile = (TileVariant) var1;
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glPushMatrix();
        GL11.glTranslatef((float)var2 + 0.5f, (float)var4 + 1.65f, (float)var6 + 0.5f);
        GL11.glScalef(1.2f, 1.1f, 1.2f);
        GL11.glRotatef(180, 0, 0, 1);
        GL11.glColor3f(1, 1, 1);

        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        if(ConfigClient.LegacyStool){
            setWoodTexture(var1.getBlockMetadata());
            legacyStool.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);
        }
        else {
            if(ConfigClient.WoodTextures) {
                setWoodTexture(var1.getBlockMetadata());
            }
            else {
                setStoolTexture(var1.getBlockMetadata());
            }
            stool.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);
        }
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glPopAttrib();
		GL11.glPopMatrix();
	}

	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelId,
			RenderBlocks renderer) {
        GL11.glPushMatrix();
        GL11.glTranslatef(0, 1f, 0);
        GL11.glScalef(1.2f, 1.1f, 1.2f);
        GL11.glRotatef(180, 0, 0, 1);
        GL11.glRotatef(180, 0, 1, 0);

        if(ConfigClient.LegacyStool){
            setWoodTexture(metadata);
            GL11.glColor3f(1, 1, 1);
            legacyStool.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);
        }
        else {
            setStoolTexture(metadata);
            GL11.glColor3f(1, 1, 1);
            stool.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);
        }

		GL11.glPopMatrix();
	}

    public void setStoolTexture(int meta){
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
		return CustomItems.stool.getRenderType();
	}
}
