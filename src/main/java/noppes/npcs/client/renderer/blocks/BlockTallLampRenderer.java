package noppes.npcs.client.renderer.blocks;

import cpw.mods.fml.client.registry.RenderingRegistry;
import kamkeel.npcs.util.ColorUtil;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.CustomItems;
import noppes.npcs.blocks.BlockTallLamp;
import noppes.npcs.blocks.tiles.TileTallLamp;
import noppes.npcs.client.model.blocks.lamp.ModelTallLamp;
import noppes.npcs.client.model.blocks.legacy.ModelLegacyTallLamp;
import noppes.npcs.client.model.blocks.legacy.ModelLegacyTallLampTop;
import noppes.npcs.config.ConfigClient;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public class BlockTallLampRenderer extends BlockRendererInterface{

	private final ModelLegacyTallLamp modelLegacy = new ModelLegacyTallLamp();
    private final ModelLegacyTallLampTop topLegacy = new ModelLegacyTallLampTop();
    public static final ResourceLocation resourceTop = new ResourceLocation("customnpcs","textures/cache/wool_colored_white.png");


    private final ModelTallLamp model = new ModelTallLamp();

    public static final ResourceLocation wood = new ResourceLocation("customnpcs","textures/models/lamp/tall/wood.png");
    public static final ResourceLocation stone = new ResourceLocation("customnpcs","textures/models/lamp/tall/stone.png");
    public static final ResourceLocation iron = new ResourceLocation("customnpcs","textures/models/lamp/tall/iron.png");
    public static final ResourceLocation gold = new ResourceLocation("customnpcs","textures/models/lamp/tall/gold.png");
    public static final ResourceLocation diamond = new ResourceLocation("customnpcs","textures/models/lamp/tall/diamond.png");

    public BlockTallLampRenderer(){
		((BlockTallLamp)CustomItems.tallLamp).renderId = RenderingRegistry.getNextAvailableRenderId();
		RenderingRegistry.registerBlockHandler(this);
    }

	@Override
	public void renderTileEntityAt(TileEntity var1, double var2, double var4,
			double var6, float var8) {
		TileTallLamp tile = (TileTallLamp) var1;
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glPushMatrix();
        GL11.glTranslatef((float)var2 + 0.5f, (float)var4 + 1.5f, (float)var6 + 0.5f);
        GL11.glRotatef(180, 0, 0, 1);
        GL11.glRotatef(90 * tile.rotation, 0, 1, 0);
        GL11.glColor3f(1, 1, 1);

        if(ConfigClient.LegacyTallLamp){
            setMaterialTexture(var1.getBlockMetadata());
            modelLegacy.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);

            this.bindTexture(resourceTop);
            float[] color = ColorUtil.hexToRGB(tile.color);
            GL11.glColor3f(color[0], color[1], color[2]);
            topLegacy.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);
        } else {
            setLampTexture(var1.getBlockMetadata());
            model.Lamp.render(0.0625F);
            int light = var1.getWorldObj().getLightBrightnessForSkyBlocks(var1.xCoord, var1.yCoord, var1.zCoord, 0);
            int brightX = light % 65536;
            int brightY = light / 65536;

            // Set full brightness for the light
            int fullBright = 0xF000F0;
            int fullBrightX = fullBright % 65536;
            int fullBrightY = fullBright / 65536;
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)fullBrightX, (float)fullBrightY);
            model.Light.render(0.0625F);
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)brightX, (float)brightY);

            float[] color = ColorUtil.hexToRGB(tile.color);
            GL11.glColor3f(color[0], color[1], color[2]);
            model.Shade.render(0.0625F);
        }

        GL11.glPopAttrib();
		GL11.glPopMatrix();
	}

	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelId,
			RenderBlocks renderer) {
        GL11.glPushMatrix();
        GL11.glTranslatef(0, 0.42f, 0);
        GL11.glScalef(0.76f, 0.66f, 0.76f);
        GL11.glRotatef(180, 0, 0, 1);
        GL11.glRotatef(180, 0, 1, 0);
        if(ConfigClient.LegacyTallLamp){
            setMaterialTexture(metadata);
            GL11.glColor3f(1, 1, 1);
            modelLegacy.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);

            this.bindTexture(resourceTop);
            float[] color =  ColorUtil.hexToRGB(ColorUtil.colorTableInts[15 - metadata]);
            GL11.glColor3f(color[0], color[1], color[2]);
            topLegacy.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);
        } else {
            setLampTexture(metadata);
            model.Lamp.render(0.0625F);
            model.Light.render(0.0625F);
            float[] color =  ColorUtil.hexToRGB(ColorUtil.colorTableInts[15 - metadata]);
            GL11.glColor3f(color[0], color[1], color[2]);
            model.Shade.render(0.0625F);
        }

		GL11.glPopMatrix();
	}

    public void setLampTexture(int meta){
        TextureManager manager = Minecraft.getMinecraft().getTextureManager();
        if(meta == 1)
            manager.bindTexture(stone);
        else if(meta == 2)
            manager.bindTexture(iron);
        else if(meta == 3)
            manager.bindTexture(gold);
        else if(meta == 4)
            manager.bindTexture(diamond);
        else
            manager.bindTexture(wood);
    }

	@Override
	public int getRenderId() {
		return CustomItems.tallLamp.getRenderType();
	}
}
