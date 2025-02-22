package noppes.npcs.client.renderer.items;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer;
import noppes.npcs.api.item.IItemCustomizable;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.client.ClientCacheHandler;
import noppes.npcs.client.renderer.ImageData;
import noppes.npcs.items.ItemCustomizable;
import noppes.npcs.items.ItemNpcTool;
import noppes.npcs.scripted.NpcAPI;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.util.Random;

import static net.minecraft.client.renderer.entity.RenderItem.renderInFrame;

public class ToolItemRenderer implements IItemRenderer {

    private static final ResourceLocation RES_ITEM_GLINT = new ResourceLocation("textures/misc/enchanted_item_glint.png");
    private static final ResourceLocation PAINTBRUSH_HANDLE = new ResourceLocation("customnpcs", "textures/items/npcBrushHandle.png");
    private static final ResourceLocation PAINTBRUSH_BRUSH = new ResourceLocation("customnpcs", "textures/items/npcBrushHair.png");
    private final Random random = new Random();

    private int item3dRenderTicks = 1;

    @Override
    public boolean handleRenderType(ItemStack item, ItemRenderType type) {
        return type != ItemRenderType.FIRST_PERSON_MAP;
    }

    @Override
    public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
        return helper == ItemRendererHelper.ENTITY_ROTATION || helper == ItemRendererHelper.ENTITY_BOBBING;
    }

    @Override
    public void renderItem(ItemRenderType type, ItemStack itemStack, Object... data) {
        if (itemStack == null || !(itemStack.getItem() instanceof ItemNpcTool)) {
            return;
        }

        if (type == ItemRenderType.INVENTORY) {
            GL11.glPushMatrix();
            renderInventoryCustomItem(itemStack);
            GL11.glPopMatrix();
            return;
        }
        if (type == ItemRenderType.ENTITY) {
            GL11.glPushMatrix();
            EntityItem entityItem = (EntityItem) data[1];
            if (!entityItem.ignoreFrustumCheck) {
                entityItem.ignoreFrustumCheck = true;

                float entityXZSize = (float) Math.sqrt(Math.pow(1,2) + Math.pow(1,2));
                this.setEntitySize(entityItem, entityXZSize * 0.25F, 1 * 0.25F);

                double XSize = entityItem.boundingBox.maxX - entityItem.boundingBox.minX;
                double YSize = entityItem.boundingBox.maxY - entityItem.boundingBox.minY;
                double ZSize = entityItem.boundingBox.maxZ - entityItem.boundingBox.minZ;
                entityItem.boundingBox.maxX = entityItem.boundingBox.minX + XSize * 1;
                entityItem.boundingBox.maxY = entityItem.boundingBox.minY + YSize * 1;
                entityItem.boundingBox.maxZ = entityItem.boundingBox.minZ + ZSize * 1;
            }

            float entityRenderTicks = Minecraft.getMinecraft().timer.renderPartialTicks;
            float bobbing = MathHelper.sin(((float)entityItem.age + entityRenderTicks) / 10.0F + entityItem.hoverStart) * 0.1F + 0.1F;

            GL11.glRotatef(1 * entityRenderTicks %360, 1, 0, 0);
            GL11.glRotatef(1 * entityRenderTicks %360, 0, 1, 0);
            GL11.glRotatef(1 * entityRenderTicks %360, 0, 0, 1);

            if (!renderInFrame) {
                GL11.glTranslatef(0.0F, -bobbing, 0.0F);
            }

            renderEntityCustomItem(itemStack, entityItem, PAINTBRUSH_HANDLE);
            int color = ItemNpcTool.getColor(itemStack.getTagCompound());
            float red = ((color >> 16) & 0xFF) / 255.0F;
            float green = ((color >> 8) & 0xFF) / 255.0F;
            float blue = (color & 0xFF) / 255.0F;
            GL11.glColor3f(red, green, blue);
            renderEntityCustomItem(itemStack, entityItem, PAINTBRUSH_BRUSH);

            GL11.glPopMatrix();
            return;
        }
        GL11.glPushMatrix();

        GL11.glTranslatef(0.9375F, 0.0625F, 0.0F);
        GL11.glRotatef(-315.0F, 0.0F, 0.0F, 1.0F);
        GL11.glTranslatef(0.135F * 1, 0.2F * 1, 0.07F * 1);

        GL11.glScalef(1, 1, 1);
        GL11.glRotatef(-20.0F, 0.0F, 0.0F, 1.0F);
        GL11.glRotatef(-50.0F, 0.0F, 1.0F, 0.0F);
        GL11.glTranslatef(-0.09375F, 0.0625F, 0.0F);


        EntityLivingBase entityLivingBase = (EntityLivingBase) data[1];

        GL11.glPushMatrix();
        renderItem3d(entityLivingBase, itemStack, PAINTBRUSH_HANDLE);
        GL11.glPopMatrix();

        int color = ItemNpcTool.getColor(itemStack.getTagCompound());
        float red = ((color >> 16) & 0xFF) / 255.0F;
        float green = ((color >> 8) & 0xFF) / 255.0F;
        float blue = (color & 0xFF) / 255.0F;
        GL11.glColor3f(red, green, blue);

        GL11.glPushMatrix();
        renderItem3d(entityLivingBase, itemStack, PAINTBRUSH_BRUSH);
        GL11.glPopMatrix();

        GL11.glPopMatrix();
    }

    private void setEntitySize(EntityItem entityItem, float p_70105_1_, float p_70105_2_)
    {
        float f2;

        if (p_70105_1_ != entityItem.width || p_70105_2_ != entityItem.height)
        {
            f2 = entityItem.width;
            entityItem.width = p_70105_1_;
            entityItem.height = p_70105_2_;
            entityItem.boundingBox.maxX = entityItem.boundingBox.minX + (double)entityItem.width;
            entityItem.boundingBox.maxZ = entityItem.boundingBox.minZ + (double)entityItem.width;
            entityItem.boundingBox.maxY = entityItem.boundingBox.minY + (double)entityItem.height;

            if (entityItem.width > f2 && !entityItem.firstUpdate && !entityItem.worldObj.isRemote)
            {
                entityItem.moveEntity((double)(f2 - entityItem.width), 0.0D, (double)(f2 - entityItem.width));
            }
        }

        f2 = p_70105_1_ % 2.0F;

        if ((double)f2 < 0.375D)
        {
            entityItem.myEntitySize = Entity.EnumEntitySize.SIZE_1;
        }
        else if ((double)f2 < 0.75D)
        {
            entityItem.myEntitySize = Entity.EnumEntitySize.SIZE_2;
        }
        else if ((double)f2 < 1.0D)
        {
            entityItem.myEntitySize = Entity.EnumEntitySize.SIZE_3;
        }
        else if ((double)f2 < 1.375D)
        {
            entityItem.myEntitySize = Entity.EnumEntitySize.SIZE_4;
        }
        else if ((double)f2 < 1.75D)
        {
            entityItem.myEntitySize = Entity.EnumEntitySize.SIZE_5;
        }
        else
        {
            entityItem.myEntitySize = Entity.EnumEntitySize.SIZE_6;
        }
    }

    public void renderEntityCustomItem(ItemStack itemStack, EntityItem entityItem, ResourceLocation location) {
        int pass = 0;

        GL11.glPushMatrix();
        TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();
        textureManager.bindTexture(location);

        if (renderInFrame)
        {
            GL11.glTranslatef(0.0F, -0.05F, 0.0F);
            GL11.glScalef(1.025641F,1.025641F,1.025641F);
            GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
        }

        Tessellator tessellator = Tessellator.instance;

        float f14 = 0.0F;
        float f15 = 1.0F;
        float f4 = 0.0F;
        float f5 = 1.0F;
        float f6 = 1.0F;
        float f7 = 0.5F;
        float f8 = 0.25F;
        float f10;

        if (RenderManager.instance.options.fancyGraphics)
        {
            GL11.glPushMatrix();

            float f9 = 0.0625F;
            f10 = 0.021875F;
            int j = itemStack.stackSize;
            byte b0;

            if (j < 2)
            {
                b0 = 1;
            }
            else if (j < 16)
            {
                b0 = 2;
            }
            else if (j < 32)
            {
                b0 = 3;
            }
            else
            {
                b0 = 4;
            }

            GL11.glTranslatef(-f7, -f8, -((f9 + f10) * (float)b0 / 2.0F));

            for (int k = 0; k < b0; ++k)
            {
                GL11.glTranslatef(0f, 0f, f9 + f10);
                // TODO: May need to rebind here
                ItemRenderer.renderItemIn2D(tessellator, 1.0F, 0.0F, 0.0F, 1.0F, 16, 16, f9);
                if (itemStack.hasEffect(pass))
                {
                    GL11.glDepthFunc(GL11.GL_EQUAL);
                    GL11.glDisable(GL11.GL_LIGHTING);
                    RenderManager.instance.renderEngine.bindTexture(RES_ITEM_GLINT);
                    GL11.glEnable(GL11.GL_BLEND);
                    GL11.glBlendFunc(GL11.GL_SRC_COLOR, GL11.GL_ONE);
                    float f11 = 0.76F;
                    GL11.glColor4f(0.5F * f11, 0.25F * f11, 0.8F * f11, 1.0F);
                    GL11.glMatrixMode(GL11.GL_TEXTURE);
                    GL11.glPushMatrix();
                    float f12 = 0.125F;
                    GL11.glScalef(f12, f12, f12);
                    float f13 = (float)(Minecraft.getSystemTime() % 3000L) / 3000.0F * 8.0F;
                    GL11.glTranslatef(f13, 0.0F, 0.0F);
                    GL11.glRotatef(-50.0F, 0.0F, 0.0F, 1.0F);
                    ItemRenderer.renderItemIn2D(tessellator, 0.0F, 0.0F, 1.0F, 1.0F, 255, 255, f9);
                    GL11.glPopMatrix();
                    GL11.glPushMatrix();
                    GL11.glScalef(f12, f12, f12);
                    f13 = (float)(Minecraft.getSystemTime() % 4873L) / 4873.0F * 8.0F;
                    GL11.glTranslatef(-f13, 0.0F, 0.0F);
                    GL11.glRotatef(10.0F, 0.0F, 0.0F, 1.0F);
                    ItemRenderer.renderItemIn2D(tessellator, 0.0F, 0.0F, 1.0F, 1.0F, 255, 255, f9);
                    GL11.glPopMatrix();
                    GL11.glMatrixMode(GL11.GL_MODELVIEW);
                    GL11.glDisable(GL11.GL_BLEND);
                    GL11.glEnable(GL11.GL_LIGHTING);
                    GL11.glDepthFunc(GL11.GL_LEQUAL);
                }
            }

            GL11.glPopMatrix();
        }
        else
        {
            int j = itemStack.stackSize;
            int b0;

            if (j < 2)
            {
                b0 = 1;
            }
            else if (j < 16)
            {
                b0 = 2;
            }
            else if (j < 32)
            {
                b0 = 3;
            }
            else
            {
                b0 = 4;
            }

            for (int l = 0; l < b0; ++l)
            {
                GL11.glPushMatrix();

                if (l > 0)
                {
                    f10 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.3F;
                    float f16 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.3F;
                    float f17 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.3F;
                    GL11.glTranslatef(f10, f16, f17);
                }

                if (!renderInFrame)
                {
                    GL11.glRotatef(180.0F - RenderManager.instance.playerViewY, 0.0F, 1.0F, 0.0F);
                }

                tessellator.startDrawingQuads();
                tessellator.setNormal(0.0F, 1.0F, 0.0F);
                tessellator.addVertexWithUV((double)(0.0F - f7), (double)(0.0F - f8), 0.0D, (double)f14, (double)f5);
                tessellator.addVertexWithUV((double)(f6 - f7), (double)(0.0F - f8), 0.0D, (double)f15, (double)f5);
                tessellator.addVertexWithUV((double)(f6 - f7), (double)(1.0F - f8), 0.0D, (double)f15, (double)f4);
                tessellator.addVertexWithUV((double)(0.0F - f7), (double)(1.0F - f8), 0.0D, (double)f14, (double)f4);
                tessellator.draw();
                GL11.glPopMatrix();
            }
        }
        GL11.glPopMatrix();
    }

    public void renderInventoryCustomItem(ItemStack itemStack) {
        GL11.glPushMatrix();
        TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();

        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_ALPHA_TEST);

        textureManager.bindTexture(PAINTBRUSH_HANDLE);
        renderCustomItemSlot(0,0,16,16, 1.0f, 1.0f, 1.0f);

        int color = ItemNpcTool.getColor(itemStack.getTagCompound());
        float red = ((color >> 16) & 0xFF) / 255.0F;
        float green = ((color >> 8) & 0xFF) / 255.0F;
        float blue = (color & 0xFF) / 255.0F;
        textureManager.bindTexture(PAINTBRUSH_BRUSH);
        renderCustomItemSlot(0,0,16,16, red, green, blue);

        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_ALPHA_TEST);

        if (itemStack.hasEffect(0))
        {
            renderEffect(Minecraft.getMinecraft().getTextureManager(), 0, 0);
        }

        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glPopMatrix();
    }

    public void renderEffect(TextureManager manager, int x, int y)
    {
        GL11.glDepthFunc(GL11.GL_EQUAL);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDepthMask(false);
        manager.bindTexture(RES_ITEM_GLINT);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glColor4f(0.5F, 0.25F, 0.8F, 1.0F);
        this.renderGlint(x - 2, y - 2, 20, 20);
        GL11.glDepthMask(true);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glDepthFunc(GL11.GL_LEQUAL);
    }

    private void renderGlint(int p_77018_2_, int p_77018_3_, int p_77018_4_, int p_77018_5_)
    {
        for (int j1 = 0; j1 < 2; ++j1)
        {
            OpenGlHelper.glBlendFunc(772, 1, 0, 0);
            float f = 0.00390625F;
            float f1 = 0.00390625F;
            float f2 = (float)(Minecraft.getSystemTime() % (long)(3000 + j1 * 1873)) / (3000.0F + (float)(j1 * 1873)) * 256.0F;
            float f3 = 0.0F;
            Tessellator tessellator = Tessellator.instance;
            float f4 = 4.0F;

            if (j1 == 1)
            {
                f4 = -1.0F;
            }

            tessellator.startDrawingQuads();
            tessellator.addVertexWithUV((double)(p_77018_2_ + 0), (double)(p_77018_3_ + p_77018_5_), 0, (double)((f2 + (float)p_77018_5_ * f4) * f), (double)((f3 + (float)p_77018_5_) * f1));
            tessellator.addVertexWithUV((double)(p_77018_2_ + p_77018_4_), (double)(p_77018_3_ + p_77018_5_), 0, (double)((f2 + (float)p_77018_4_ + (float)p_77018_5_ * f4) * f), (double)((f3 + (float)p_77018_5_) * f1));
            tessellator.addVertexWithUV((double)(p_77018_2_ + p_77018_4_), (double)(p_77018_3_ + 0), 0, (double)((f2 + (float)p_77018_4_) * f), (double)((f3 + 0.0F) * f1));
            tessellator.addVertexWithUV((double)(p_77018_2_ + 0), (double)(p_77018_3_ + 0), 0, (double)((f2 + 0.0F) * f), (double)((f3 + 0.0F) * f1));
            tessellator.draw();
        }
    }

    public void renderCustomItemSlot(int posX, int posY, int imageWidth, int imageHeight, float itemRed, float itemGreen, float itemBlue) {
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.setColorRGBA_F(itemRed, itemGreen, itemBlue, 1.0F);
        tessellator.addVertexWithUV((double)(posX), (double)(posY + imageHeight), 0, 0, 1);
        tessellator.addVertexWithUV((double)(posX + imageWidth), (double)(posY + imageHeight), 0, 1, 1);
        tessellator.addVertexWithUV((double)(posX + imageWidth), (double)(posY), 0, 1, 0);
        tessellator.addVertexWithUV((double)(posX), (double)(posY), 0, 0, 0);
        tessellator.draw();
    }

    public void renderItem3d(EntityLivingBase entityLivingBase, ItemStack itemStack, ResourceLocation location) {
        item3dRenderTicks++;

        Minecraft mc = Minecraft.getMinecraft();
        TextureManager texturemanager = mc.getTextureManager();
        int par3 = 0;

        texturemanager.bindTexture(location);

        Tessellator tessellator = Tessellator.instance;
        IIcon icon = entityLivingBase.getItemIcon(itemStack, par3);

        if (icon == null) {
            return;
        }

        GL11.glEnable(GL12.GL_RESCALE_NORMAL);

        float f4 = 0.0F;
        float f5 = 0.3F;
        GL11.glTranslatef(-f4, -f5, 0.0F);
        GL11.glRotatef(50.0F, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(335.0F, 0.0F, 0.0F, 1.0F);
        GL11.glTranslatef(-0.9375F, -0.0625F, 0.0F);

        renderCustomItemIn2D(tessellator, 1.0F, 0.0F, 0.0F, 1.0F, 0.0625F);

        if (itemStack.hasEffect(par3)) {
            GL11.glDepthFunc(GL11.GL_EQUAL);
            GL11.glDisable(GL11.GL_LIGHTING);
            texturemanager.bindTexture(RES_ITEM_GLINT);
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
        }

        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
    }

    public static void renderCustomItemIn2D(Tessellator p_78439_0_, float p_78439_1_, float p_78439_2_, float p_78439_3_, float p_78439_4_, float p_78439_7_)
    {
        int width = 16;
        int height = 16;

        p_78439_0_.startDrawingQuads();
        p_78439_0_.setNormal(0.0F, 0.0F, 1.0F);
        p_78439_0_.addVertexWithUV(0.0D, 0.0D, 0.0D, (double)p_78439_1_, (double)p_78439_4_);
        p_78439_0_.addVertexWithUV(1.0D, 0.0D, 0.0D, (double)p_78439_3_, (double)p_78439_4_);
        p_78439_0_.addVertexWithUV(1.0D, 1.0D, 0.0D, (double)p_78439_3_, (double)p_78439_2_);
        p_78439_0_.addVertexWithUV(0.0D, 1.0D, 0.0D, (double)p_78439_1_, (double)p_78439_2_);
        p_78439_0_.draw();
        p_78439_0_.startDrawingQuads();
        p_78439_0_.setNormal(0.0F, 0.0F, -1.0F);
        p_78439_0_.addVertexWithUV(0.0D, 1.0D, (double)(0.0F - p_78439_7_), (double)p_78439_1_, (double)p_78439_2_);
        p_78439_0_.addVertexWithUV(1.0D, 1.0D, (double)(0.0F - p_78439_7_), (double)p_78439_3_, (double)p_78439_2_);
        p_78439_0_.addVertexWithUV(1.0D, 0.0D, (double)(0.0F - p_78439_7_), (double)p_78439_3_, (double)p_78439_4_);
        p_78439_0_.addVertexWithUV(0.0D, 0.0D, (double)(0.0F - p_78439_7_), (double)p_78439_1_, (double)p_78439_4_);
        p_78439_0_.draw();
        float f5 = 0.5F * (p_78439_1_ - p_78439_3_) / (float)width;
        float f6 = 0.5F * (p_78439_4_ - p_78439_2_) / (float)height;
        p_78439_0_.startDrawingQuads();
        p_78439_0_.setNormal(-1.0F, 0.0F, 0.0F);
        int k;
        float f7;
        float f8;

        for (k = 0; k < width; ++k)
        {
            f7 = (float)k / (float)width;
            f8 = p_78439_1_ + (p_78439_3_ - p_78439_1_) * f7 - f5;
            p_78439_0_.addVertexWithUV((double)f7, 0.0D, (double)(0.0F - p_78439_7_), (double)f8, (double)p_78439_4_);
            p_78439_0_.addVertexWithUV((double)f7, 0.0D, 0.0D, (double)f8, (double)p_78439_4_);
            p_78439_0_.addVertexWithUV((double)f7, 1.0D, 0.0D, (double)f8, (double)p_78439_2_);
            p_78439_0_.addVertexWithUV((double)f7, 1.0D, (double)(0.0F - p_78439_7_), (double)f8, (double)p_78439_2_);
        }

        p_78439_0_.draw();
        p_78439_0_.startDrawingQuads();
        p_78439_0_.setNormal(1.0F, 0.0F, 0.0F);
        float f9;

        for (k = 0; k < width; ++k)
        {
            f7 = (float)k / (float)width;
            f8 = p_78439_1_ + (p_78439_3_ - p_78439_1_) * f7 - f5;
            f9 = f7 + 1.0F / (float)width;
            p_78439_0_.addVertexWithUV((double)f9, 1.0D, (double)(0.0F - p_78439_7_), (double)f8, (double)p_78439_2_);
            p_78439_0_.addVertexWithUV((double)f9, 1.0D, 0.0D, (double)f8, (double)p_78439_2_);
            p_78439_0_.addVertexWithUV((double)f9, 0.0D, 0.0D, (double)f8, (double)p_78439_4_);
            p_78439_0_.addVertexWithUV((double)f9, 0.0D, (double)(0.0F - p_78439_7_), (double)f8, (double)p_78439_4_);
        }

        p_78439_0_.draw();
        p_78439_0_.startDrawingQuads();
        p_78439_0_.setNormal(0.0F, 1.0F, 0.0F);

        for (k = 0; k < height; ++k)
        {
            f7 = (float)k / (float)height;
            f8 = p_78439_4_ + (p_78439_2_ - p_78439_4_) * f7 - f6;
            f9 = f7 + 1.0F / (float)height;
            p_78439_0_.addVertexWithUV(0.0D, (double)f9, 0.0D, (double)p_78439_1_, (double)f8);
            p_78439_0_.addVertexWithUV(1.0D, (double)f9, 0.0D, (double)p_78439_3_, (double)f8);
            p_78439_0_.addVertexWithUV(1.0D, (double)f9, (double)(0.0F - p_78439_7_), (double)p_78439_3_, (double)f8);
            p_78439_0_.addVertexWithUV(0.0D, (double)f9, (double)(0.0F - p_78439_7_), (double)p_78439_1_, (double)f8);
        }

        p_78439_0_.draw();
        p_78439_0_.startDrawingQuads();
        p_78439_0_.setNormal(0.0F, -1.0F, 0.0F);

        for (k = 0; k < height; ++k)
        {
            f7 = (float)k / (float)height;
            f8 = p_78439_4_ + (p_78439_2_ - p_78439_4_) * f7 - f6;
            p_78439_0_.addVertexWithUV(1.0D, (double)f7, 0.0D, (double)p_78439_3_, (double)f8);
            p_78439_0_.addVertexWithUV(0.0D, (double)f7, 0.0D, (double)p_78439_1_, (double)f8);
            p_78439_0_.addVertexWithUV(0.0D, (double)f7, (double)(0.0F - p_78439_7_), (double)p_78439_1_, (double)f8);
            p_78439_0_.addVertexWithUV(1.0D, (double)f7, (double)(0.0F - p_78439_7_), (double)p_78439_3_, (double)f8);
        }

        p_78439_0_.draw();
    }
}
