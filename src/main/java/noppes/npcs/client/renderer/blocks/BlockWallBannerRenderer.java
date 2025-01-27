package noppes.npcs.client.renderer.blocks;

import cpw.mods.fml.client.registry.RenderingRegistry;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.init.Blocks;
import net.minecraft.item.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.CustomItems;
import noppes.npcs.blocks.BlockWallBanner;
import noppes.npcs.blocks.tiles.TileWallBanner;
import noppes.npcs.client.model.blocks.banner.ModelBannerWall;
import noppes.npcs.client.model.blocks.banner.ModelBannerWallFlag;
import noppes.npcs.client.model.blocks.legacy.ModelLegacyWallBanner;
import noppes.npcs.client.model.blocks.legacy.ModelLegacyWallBannerFlag;
import noppes.npcs.config.ConfigClient;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public class BlockWallBannerRenderer extends BlockRendererInterface{

	private final ModelLegacyWallBanner modelLegacyWallBanner = new ModelLegacyWallBanner();
	private final ModelLegacyWallBannerFlag modelLegacyWallBannerFlag = new ModelLegacyWallBannerFlag();

    private final ModelBannerWall model = new ModelBannerWall();
    private final ModelBannerWallFlag flag = new ModelBannerWallFlag();

    public BlockWallBannerRenderer(){
		((BlockWallBanner)CustomItems.wallBanner).renderId = RenderingRegistry.getNextAvailableRenderId();
		RenderingRegistry.registerBlockHandler(this);
    }

	@Override
	public void renderTileEntityAt(TileEntity var1, double var2, double var4,
			double var6, float var8) {
		TileWallBanner tile = (TileWallBanner) var1;
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glPushMatrix();
        GL11.glTranslatef((float)var2 + 0.5f, (float)var4 +0.4f, (float)var6 + 0.5f);
        //GL11.glScalef(0.95f, 0.95f, 0.95f);
        GL11.glRotatef(180, 0, 0, 1);
        GL11.glRotatef(90 * tile.rotation, 0, 1, 0);

        if(ConfigClient.LegacyBanner){
            setMaterialTexture(var1.getBlockMetadata());
            GL11.glColor3f(1, 1, 1);
            modelLegacyWallBanner.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);

            this.bindTexture(BlockBannerRenderer.legacyFlagResource);
            float[] color = BlockBannerRenderer.colorTable[tile.color];
            GL11.glColor3f(color[0], color[1], color[2]);
            modelLegacyWallBannerFlag.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);

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

            BlockBannerRenderer.setBannerMaterial(var1.getBlockMetadata());
            model.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);

            BlockBannerRenderer.setFlagType(tile.variant);
            float[] color = colorTable[tile.color];
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
	}
    public void doRender(double par2, double par4, double par6, int meta, ItemStack iicon, float rotate)
    {
        GL11.glPushMatrix();
        bindTexture(TextureMap.locationItemsTexture);
        GL11.glTranslatef((float)par2 + 0.5f, (float)par4 + 0.2f, (float)par6 + 0.5f);
        GL11.glRotatef(180, 0, 0, 1);
        GL11.glRotatef(90 * meta, 0, 1, 0);

        if(ConfigClient.LegacyBanner){
            GL11.glTranslatef(0, 0, 0.26f);
        }
        else {
            GL11.glTranslatef(0, 0, 0.3f);

            GL11.glTranslatef(0, -0.6f, 0); // Translate to pivot point
            GL11.glRotatef((float) Math.toDegrees(rotate), 1, 0, 0); // Apply sway rotation
            GL11.glTranslatef(0, 0.6f, 0); // Translate to pivot point
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

        Item loadingItem = item.getItem();
        if(loadingItem instanceof ItemBlock) {
            Block block = ((ItemBlock) loadingItem).field_150939_a;
            if(block != null){
                if(block == Blocks.enchanting_table || block == Blocks.end_portal_frame){
                    object = block.getIcon(1, item.getItemDamage());
                }
                else if (block == Blocks.furnace || block == Blocks.tnt){
                    object = block.getIcon(2, 1);
                }
                else {
                    object = block.getIcon(0, item.getItemDamage());
                }
            }
        }

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
        GL11.glTranslatef(0, 0.26f, 0.3f);
        GL11.glScalef(0.95f, 0.85f, 0.95f);
        GL11.glRotatef(180, 0, 0, 1);
        GL11.glRotatef(180, 0, 1, 0);

        if(ConfigClient.LegacyBanner){
            setMaterialTexture(metadata);
            GL11.glColor3f(1, 1, 1);
            modelLegacyWallBanner.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);

            this.bindTexture(BlockBannerRenderer.legacyFlagResource);
            float[] color = BlockBannerRenderer.colorTable[15 - metadata];
            GL11.glColor3f(color[0], color[1], color[2]);
            modelLegacyWallBannerFlag.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);
        }
        else {
            BlockBannerRenderer.setBannerMaterial(metadata);
            model.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);

            this.bindTexture(BlockBannerRenderer.normalFlag);
            float[] color = colorTable[15 - metadata];
            GL11.glColor3f(color[0], color[1], color[2]);
            flag.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);
        }
		GL11.glPopMatrix();
	}

	@Override
	public int getRenderId() {
		return CustomItems.wallBanner.getRenderType();
	}

	public int specialRenderDistance(){
		return 26;
	}
}
