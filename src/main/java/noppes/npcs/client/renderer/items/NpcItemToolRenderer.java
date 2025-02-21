package noppes.npcs.client.renderer.items;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer;
import noppes.npcs.items.ItemNpcTool;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public class NpcItemToolRenderer implements IItemRenderer {

    // Textures for the assembled paintbrush parts:
    private static final ResourceLocation PAINTBRUSH_HANDLE = new ResourceLocation("customnpcs", "textures/items/paintbrush_handle.png");
    private static final ResourceLocation PAINTBRUSH_BRUSH = new ResourceLocation("customnpcs", "textures/items/paintbrush_brush.png");
    private static final ResourceLocation ENCHANT = new ResourceLocation("textures/misc/enchanted_item_glint.png");

    @Override
    public boolean handleRenderType(ItemStack item, ItemRenderType type) {
        // Only handle equipped types (first person or third person equipped)
        return type == ItemRenderType.EQUIPPED || type == ItemRenderType.EQUIPPED_FIRST_PERSON;
    }

    @Override
    public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
        return false;
    }

    @Override
    public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
        // Ensure the item implements our render interface
        if (!(item.getItem() instanceof ItemNpcTool))
            return;

        EntityLivingBase entity = (EntityLivingBase) data[1];

        GL11.glPushMatrix();
        // For meta==1 (paintbrush) we want to assemble the texture from two parts
        if (item.getItemDamage() == 1) {
            renderPaintbrush(entity, item);
        } else {
            // For other tools, fall back on the normal special render routine.
            renderItem3d(entity, item);
        }
        GL11.glPopMatrix();
    }

    /**
     * Renders the paintbrush by drawing its handle first, then overlaying the brush part tinted
     * by the NBT "BrushColor" value (an integer/hex value).
     */
    private void renderPaintbrush(EntityLivingBase entity, ItemStack item) {
        Minecraft mc = Minecraft.getMinecraft();
        TextureManager textureManager = mc.getTextureManager();
        Tessellator tessellator = Tessellator.instance;
        int width = 16;
        int height = 16;
        float thickness = 0.0625F;

        GL11.glEnable(GL12.GL_RESCALE_NORMAL);

        // Render the handle (untinted)
        textureManager.bindTexture(PAINTBRUSH_HANDLE);
        // Using full texture coordinates (0→1) in the same order as vanilla's renderItemIn2D:
        ItemRenderer.renderItemIn2D(tessellator, 1.0F, 0.0F, 0.0F, 1.0F, width, height, thickness);

        // Get the NBT color (default is white if not present)
        int color = ItemNpcTool.getColor(item.getTagCompound());
        float red = ((color >> 16) & 0xFF) / 255.0F;
        float green = ((color >> 8) & 0xFF) / 255.0F;
        float blue = (color & 0xFF) / 255.0F;
        GL11.glColor3f(red, green, blue);

        // Render the brush (tinted by the color)
        textureManager.bindTexture(PAINTBRUSH_BRUSH);
        ItemRenderer.renderItemIn2D(tessellator, 1.0F, 0.0F, 0.0F, 1.0F, width, height, thickness);

        // Reset GL color to white so that subsequent rendering is not tinted
        GL11.glColor3f(1.0F, 1.0F, 1.0F);
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);

        // If the item is enchanted, render the enchanted glint effect on top
        if (item.hasEffect(0)) {
            renderEnchantedEffect(tessellator, textureManager);
        }
    }

    /**
     * Fallback 3D item renderer (for non-paintbrush items)
     */
    private void renderItem3d(EntityLivingBase entity, ItemStack item) {
        Minecraft mc = Minecraft.getMinecraft();
        TextureManager textureManager = mc.getTextureManager();
        Tessellator tessellator = Tessellator.instance;
        textureManager.bindTexture(textureManager.getResourceLocation(item.getItemSpriteNumber()));
        IIcon icon = entity.getItemIcon(item, 0);
        if (icon == null) {
            return;
        }
        float minU = icon.getMinU();
        float maxU = icon.getMaxU();
        float minV = icon.getMinV();
        float maxV = icon.getMaxV();
        int width = icon.getIconWidth();
        int height = icon.getIconHeight();

        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        // Note: The order here mimics vanilla's 2D item rendering
        ItemRenderer.renderItemIn2D(tessellator, maxU, minV, minU, maxV, width, height, 0.0625F);
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
    }

    /**
     * Renders the enchanted glint overlay.
     */
    private void renderEnchantedEffect(Tessellator tessellator, TextureManager textureManager) {
        GL11.glDepthFunc(GL11.GL_EQUAL);
        GL11.glDisable(GL11.GL_LIGHTING);
        textureManager.bindTexture(ENCHANT);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_COLOR, GL11.GL_ONE);
        float f7 = 0.76F;
        GL11.glColor4f(0.5F * f7, 0.25F * f7, 0.8F * f7, 1.0F);
        GL11.glMatrixMode(GL11.GL_TEXTURE);
        GL11.glPushMatrix();
        float f8 = 0.125F;
        GL11.glScalef(f8, f8, f8);
        float f9 = (float) (Minecraft.getSystemTime() % 3000L) / 3000.0F * 8.0F;
        GL11.glTranslatef(f9, 0.0F, 0.0F);
        GL11.glRotatef(-50.0F, 0.0F, 0.0F, 1.0F);
        ItemRenderer.renderItemIn2D(tessellator, 0.0F, 0.0F, 1.0F, 1.0F, 256, 256, 0.0625F);
        GL11.glPopMatrix();
        GL11.glPushMatrix();
        GL11.glScalef(f8, f8, f8);
        f9 = (float) (Minecraft.getSystemTime() % 4873L) / 4873.0F * 8.0F;
        GL11.glTranslatef(-f9, 0.0F, 0.0F);
        GL11.glRotatef(10.0F, 0.0F, 0.0F, 1.0F);
        ItemRenderer.renderItemIn2D(tessellator, 0.0F, 0.0F, 1.0F, 1.0F, 256, 256, 0.0625F);
        GL11.glPopMatrix();
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glDepthFunc(GL11.GL_LEQUAL);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
