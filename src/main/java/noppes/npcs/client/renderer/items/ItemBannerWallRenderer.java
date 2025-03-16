package noppes.npcs.client.renderer.items;

import kamkeel.npcs.util.ColorUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;
import noppes.npcs.client.renderer.blocks.BlockBannerRenderer;
import noppes.npcs.client.renderer.blocks.BlockWallBannerRenderer;
import noppes.npcs.config.ConfigClient;
import org.lwjgl.opengl.GL11;

public class ItemBannerWallRenderer implements IItemRenderer {

    @Override
    public boolean handleRenderType(ItemStack item, ItemRenderType type) {
        return true;
    }

    @Override
    public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
        return true;
    }

    @Override
    public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
        int meta = item.getItemDamage();
        int colorValue = ColorUtil.colorTableInts[15 - meta];
        if (item.hasTagCompound() && item.getTagCompound().hasKey("BrushColor")) {
            colorValue = item.getTagCompound().getInteger("BrushColor");
        }
        float[] color = ColorUtil.hexToRGB(colorValue);

        TextureManager manager = Minecraft.getMinecraft().getTextureManager();

        GL11.glPushMatrix();
        if (type == ItemRenderType.EQUIPPED || type == ItemRenderType.EQUIPPED_FIRST_PERSON) {
            GL11.glTranslatef(0.5F, 0.5F, 0.5F);
        }

        GL11.glTranslatef(0, 0.26f, 0.3f);
        GL11.glScalef(0.95f, 0.85f, 0.95f);
        GL11.glRotatef(180, 0, 0, 1);
        GL11.glRotatef(180, 0, 1, 0);

        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glEnable(GL11.GL_ALPHA_TEST);

        if (ConfigClient.LegacyBanner) {
            BlockWallBannerRenderer.setMaterialTexture(meta);
            GL11.glColor3f(1, 1, 1);
            BlockWallBannerRenderer.modelLegacyWallBanner.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);

            manager.bindTexture(BlockBannerRenderer.legacyFlagResource);
            GL11.glColor3f(color[0], color[1], color[2]);
            BlockWallBannerRenderer.modelLegacyWallBannerFlag.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);
        } else {
            BlockWallBannerRenderer.flag.BannerFlag.rotateAngleX = 0;
            BlockBannerRenderer.setBannerMaterial(meta);
            BlockWallBannerRenderer.model.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);

            manager.bindTexture(BlockBannerRenderer.normalFlag);
            GL11.glColor3f(color[0], color[1], color[2]);
            BlockWallBannerRenderer.flag.render(null, 0, 0, 0, 0, 0.0F, 0.0625F);
        }

        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glPopAttrib();

        GL11.glPopMatrix();
    }
}
