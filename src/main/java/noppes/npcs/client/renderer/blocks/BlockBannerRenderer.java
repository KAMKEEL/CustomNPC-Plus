package noppes.npcs.client.renderer.blocks;

import cpw.mods.fml.client.registry.RenderingRegistry;
import kamkeel.npcs.util.ColorUtil;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.CustomItems;
import noppes.npcs.blocks.BlockBanner;
import noppes.npcs.blocks.tiles.TileBanner;
import noppes.npcs.client.model.blocks.banner.ModelBannerFloor;
import noppes.npcs.client.model.blocks.banner.ModelBannerFloorFlag;
import noppes.npcs.client.model.blocks.legacy.ModelLegacyBanner;
import noppes.npcs.client.model.blocks.legacy.ModelLegacyBannerFlag;
import noppes.npcs.config.ConfigClient;
import noppes.npcs.constants.EnumBannerVariant;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public class BlockBannerRenderer extends BlockRendererInterface{

	private final ModelLegacyBanner legacyBanner = new ModelLegacyBanner();
	private final ModelLegacyBannerFlag legacyFlag = new ModelLegacyBannerFlag();

    private final ModelBannerFloor banner = new ModelBannerFloor();
    private final ModelBannerFloorFlag flag = new ModelBannerFloorFlag();

    public static final ResourceLocation legacyFlagResource = new ResourceLocation("customnpcs","textures/models/legacy/banner.png");

    public static final ResourceLocation normalFlag = new ResourceLocation("customnpcs","textures/models/banner/flag/normal.png");
    public static final ResourceLocation pointyFlag = new ResourceLocation("customnpcs","textures/models/banner/flag/pointy.png");
    public static final ResourceLocation triangleFlag = new ResourceLocation("customnpcs","textures/models/banner/flag/triangle.png");
    public static final ResourceLocation tornFlag = new ResourceLocation("customnpcs","textures/models/banner/flag/torn.png");
    public static final ResourceLocation curvedFlag = new ResourceLocation("customnpcs","textures/models/banner/flag/curved.png");

    protected static final ResourceLocation BannerWood = new ResourceLocation("customnpcs","textures/models/banner/BannerFloorWood.png");
    protected static final ResourceLocation BannerStone = new ResourceLocation("customnpcs","textures/models/banner/BannerFloorStone.png");
    protected static final ResourceLocation BannerIron = new ResourceLocation("customnpcs","textures/models/banner/BannerFloorIron.png");
    protected static final ResourceLocation BannerGold = new ResourceLocation("customnpcs","textures/models/banner/BannerFloorGold.png");
    protected static final ResourceLocation BannerDiamond = new ResourceLocation("customnpcs","textures/models/banner/BannerFloorDiamond.png");

    public BlockBannerRenderer(){
		((BlockBanner)CustomItems.banner).renderId = RenderingRegistry.getNextAvailableRenderId();
		RenderingRegistry.registerBlockHandler(this);
    }

	@Override
	public void renderTileEntityAt(TileEntity var1, double var2, double var4,
			double var6, float var8) {
		TileBanner tile = (TileBanner) var1;
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glPushMatrix();
        GL11.glTranslatef((float)var2 + 0.5f, (float)var4 + 1.5f, (float)var6 + 0.5f);
        //GL11.glScalef(0.95f, 0.95f, 0.95f);
        GL11.glRotatef(180, 0, 0, 1);
        GL11.glRotatef(90 * tile.rotation, 0, 1, 0);
        GL11.glColor3f(1, 1, 1);

        if(ConfigClient.LegacyBanner){
            setMaterialTexture(var1.getBlockMetadata());
            legacyBanner.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);

            this.bindTexture(legacyFlagResource);
            float[] color = ColorUtil.hexToRGB(tile.color);
            GL11.glColor3f(color[0], color[1], color[2]);
            legacyFlag.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);

            GL11.glPopMatrix();
            GL11.glColor3f(1, 1, 1);
            if(tile.icon != null && !this.playerTooFar(tile)){
                doRender(var2, var4, var6, tile.rotation, tile.icon, 0);
            }
        }
        else {
            long worldTime = tile.getWorldObj() != null ? tile.getWorldObj().getTotalWorldTime() : 0;
            // 100 is one full revolution, so we can just mod by 100 without affecting the result
            int animationProgress100 = (((tile.xCoord % 100) * 7 + (tile.yCoord % 100) * 9 + (tile.zCoord % 100) * 13) + (int) (worldTime % 100)) % 100;
            float f3 = (float) animationProgress100 + var8;
            float angle_x = (-0.0125F + 0.01F * MathHelper.cos(f3 * 0.01F * 2F * (float) Math.PI)) * (float) Math.PI;
            flag.BannerFlag.rotateAngleX = angle_x;

            setBannerMaterial(var1.getBlockMetadata());
            banner.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);

            setFlagType(tile.bannerTrim);
            float[] color =  ColorUtil.hexToRGB(tile.color);
            GL11.glPushMatrix();
            GL11.glColor3f(color[0], color[1], color[2]);
            flag.render(null, 0, 0, 0, f3, 0.0F, 0.0625F);
            GL11.glPopMatrix();

            GL11.glPopMatrix();
            GL11.glColor3f(1, 1, 1);
            if(tile.icon != null && !this.playerTooFar(tile)){
                doRender(var2, var4, var6, tile.rotation, tile.icon, angle_x);
            }
        }
        GL11.glPopAttrib();
	}
    public void doRender(double par2, double par4, double par6, int meta, ItemStack iicon, float rotate)
    {
        GL11.glPushMatrix();
        bindTexture(TextureMap.locationItemsTexture);
        GL11.glTranslatef((float)par2 + 0.5f, (float)par4 +1.3f, (float)par6 + 0.5f);
        GL11.glRotatef(180, 0, 0, 1);
    	GL11.glRotatef(90 * meta, 0, 1, 0);
        if(ConfigClient.LegacyBanner){
            GL11.glTranslatef(0, 0, -0.14f);
        }
        else {
            GL11.glTranslatef(0, 0, -0.075f);

            GL11.glTranslatef(0, -0.6f, 0); // Translate to pivot point
            GL11.glRotatef((float) Math.toDegrees(rotate), 1, 0, 0); // Apply sway rotation
            GL11.glTranslatef(0, 0.6f, 0); // Translate back after rotation
        }

        GL11.glDepthMask(false);
        float f2 = 0.05f;
        Minecraft mc = Minecraft.getMinecraft();
        GL11.glScalef(f2, f2, f2);
        renderItemBanner(mc.renderEngine, iicon, -8, -8, false);
        GL11.glDepthMask(true);
        GL11.glPopMatrix();
    }

    public void renderItemBanner(TextureManager txtMng, ItemStack item, int p_77015_4_, int p_77015_5_, boolean renderEffect)
    {
        Object object = item.getIconIndex();
        int l;
        float f;
        float f3;
        float f4;

        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        ResourceLocation resourcelocation = txtMng.getResourceLocation(item.getItemSpriteNumber());
        txtMng.bindTexture(resourcelocation);

        if (object == null)
        {
            object = ((TextureMap)Minecraft.getMinecraft().getTextureManager().getTexture(resourcelocation)).getAtlasSprite("missingno");
        }

        l = item.getItem().getColorFromItemStack(item, 0);
        f3 = (float)(l >> 16 & 255) / 255.0F;
        f4 = (float)(l >> 8 & 255) / 255.0F;
        f = (float)(l & 255) / 255.0F;

        if (renderer.renderWithColor)
        {
            GL11.glColor4f(f3, f4, f, 1.0F);
        }

        GL11.glDisable(GL11.GL_LIGHTING); //Forge: Make sure that render states are reset, a renderEffect can derp them up.
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_BLEND);

        renderer.renderIcon(p_77015_4_, p_77015_5_, (IIcon)object, 16, 16);

        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glDisable(GL11.GL_BLEND);

        if (renderEffect && item.hasEffect(0))
        {
            renderer.renderEffect(txtMng, p_77015_4_, p_77015_5_);
        }
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_CULL_FACE);
    }

	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelId,
			RenderBlocks renderer) {
        GL11.glPushMatrix();
        GL11.glTranslatef(0, 0.44f, 0);
        GL11.glScalef(0.76f, 0.66f, 0.76f);
        GL11.glRotatef(180, 0, 0, 1);
        GL11.glRotatef(180, 0, 1, 0);

        if(ConfigClient.LegacyBanner){
            setMaterialTexture(metadata);
            GL11.glColor3f(1, 1, 1);
            legacyBanner.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);

            this.bindTexture(legacyFlagResource);
            float[] color =  ColorUtil.hexToRGB(ColorUtil.colorTableInts[15 - metadata]);
            GL11.glColor3f(color[0], color[1], color[2]);
            legacyFlag.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);
        }
        else {
            setBannerMaterial(metadata);
            banner.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);

            this.bindTexture(normalFlag);
            float[] color = ColorUtil.hexToRGB(ColorUtil.colorTableInts[15 - metadata]);
            GL11.glColor3f(color[0], color[1], color[2]);
            flag.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);
        }

		GL11.glPopMatrix();
	}

	@Override
	public int getRenderId() {
		return CustomItems.banner.getRenderType();
	}

	public int specialRenderDistance(){
		return 26;
	}

    public static void setBannerMaterial(int meta){
        TextureManager manager = Minecraft.getMinecraft().getTextureManager();
        if(meta == 1)
            manager.bindTexture(BannerStone);
        else if(meta == 2)
            manager.bindTexture(BannerIron);
        else if(meta == 3)
            manager.bindTexture(BannerGold);
        else if(meta == 4)
            manager.bindTexture(BannerDiamond);
        else
            manager.bindTexture(BannerWood);
    }

    public static void setFlagType(EnumBannerVariant variant){
        TextureManager manager = Minecraft.getMinecraft().getTextureManager();
        switch (variant){
            case Pointy:
                manager.bindTexture(pointyFlag);
                break;
            case Triangle:
                manager.bindTexture(triangleFlag);
                break;
            case Torn:
                manager.bindTexture(tornFlag);
                break;
            case Curved:
                manager.bindTexture(curvedFlag);
                break;
            default:
                manager.bindTexture(normalFlag);
                break;
        }
    }
}
