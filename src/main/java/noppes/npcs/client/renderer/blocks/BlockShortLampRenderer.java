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
import noppes.npcs.blocks.BlockShortLamp;
import noppes.npcs.blocks.tiles.TileShortLamp;
import noppes.npcs.client.model.blocks.lamp.ModelShortLamp;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public class BlockShortLampRenderer extends BlockRendererInterface {

    private final ModelShortLamp model = new ModelShortLamp();

    public static final ResourceLocation wood = new ResourceLocation("customnpcs","textures/models/lamp/short/wood.png");
    public static final ResourceLocation stone = new ResourceLocation("customnpcs","textures/models/lamp/short/stone.png");
    public static final ResourceLocation iron = new ResourceLocation("customnpcs","textures/models/lamp/short/iron.png");
    public static final ResourceLocation gold = new ResourceLocation("customnpcs","textures/models/lamp/short/gold.png");
    public static final ResourceLocation diamond = new ResourceLocation("customnpcs","textures/models/lamp/short/diamond.png");

    public BlockShortLampRenderer(){
        ((BlockShortLamp)CustomItems.shortLamp).renderId = RenderingRegistry.getNextAvailableRenderId();
        RenderingRegistry.registerBlockHandler(this);
    }

    @Override
    public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float partialTicks) {
        TileShortLamp tile = (TileShortLamp) tileEntity;
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glPushMatrix();

        GL11.glTranslatef((float)x + 0.5f, (float)y + 1.5f, (float)z + 0.5f);
        GL11.glRotatef(180, 0, 0, 1);
        GL11.glRotatef(90 * tile.rotation, 0, 1, 0);
        GL11.glColor3f(1, 1, 1);

        setLampTexture(tileEntity.getBlockMetadata());
        model.Lamp.render(0.0625F);
        int light = tileEntity.getWorldObj().getLightBrightnessForSkyBlocks(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord, 0);
        int brightX = light % 65536;
        int brightY = light / 65536;

        // Set full brightness for the light
        int fullBright = 0xF000F0;
        int fullBrightX = fullBright % 65536;
        int fullBrightY = fullBright / 65536;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)fullBrightX, (float)fullBrightY);
        model.Light.render(0.0625F);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)brightX, (float)brightY);
        float[] color =  ColorUtil.hexToRGB(tile.color);
        GL11.glColor3f(color[0], color[1], color[2]);
        model.Shade.render(0.0625F);

        GL11.glPopAttrib();
        GL11.glPopMatrix();
    }

    @Override
    public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {
        GL11.glPushMatrix();
        // Adjust transformation for inventory rendering.
        GL11.glTranslatef(0, 0.42f, 0);
        GL11.glScalef(0.76f, 0.76f, 0.76f);
        GL11.glRotatef(180, 0, 0, 1);
        GL11.glRotatef(180, 0, 1, 0);

        setLampTexture(metadata);
        model.Lamp.render(0.0625F);
        model.Light.render(0.0625F);
        float[] color =  ColorUtil.hexToRGB(ColorUtil.colorTableInts[15 - metadata]);
        GL11.glColor3f(color[0], color[1], color[2]);
        model.Shade.render(0.0625F);

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
        return CustomItems.shortLamp.getRenderType();
    }
}
