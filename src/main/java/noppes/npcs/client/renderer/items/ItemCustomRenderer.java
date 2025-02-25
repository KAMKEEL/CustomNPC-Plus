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
import noppes.npcs.scripted.NpcAPI;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.util.Random;

import static net.minecraft.client.renderer.entity.RenderItem.renderInFrame;

public class ItemCustomRenderer implements IItemRenderer {
    private static final ResourceLocation RES_ITEM_GLINT = new ResourceLocation("textures/misc/enchanted_item_glint.png");
    private static final ResourceLocation enchant = new ResourceLocation("textures/misc/enchanted_item_glint.png");
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
        IItemStack iItemStack = NpcAPI.Instance().getIItemStack(itemStack);
        if (!(iItemStack instanceof IItemCustomizable)) {
            return;
        }

        IItemCustomizable scriptCustomItem = (IItemCustomizable) iItemStack;
        ImageData imageData = ClientCacheHandler.getImageData(scriptCustomItem.getTexture());
        if (!imageData.imageLoaded()) {
            return;
        }

        if (type == ItemRenderType.INVENTORY) {
            GL11.glPushMatrix();
            renderInventoryCustomItem(scriptCustomItem);
            GL11.glPopMatrix();
            return;
        }

        if (type == ItemRenderType.ENTITY) {
            GL11.glPushMatrix();
            EntityItem entityItem = (EntityItem) data[1];
            if (!entityItem.ignoreFrustumCheck) {
                entityItem.ignoreFrustumCheck = true;

                float entityXZSize = (float) Math.sqrt(Math.pow(scriptCustomItem.getScaleX(),2) + Math.pow(scriptCustomItem.getScaleZ(),2));
                this.setEntitySize(entityItem, entityXZSize * 0.25F, scriptCustomItem.getScaleY() * 0.25F);

                double XSize = entityItem.boundingBox.maxX - entityItem.boundingBox.minX;
                double YSize = entityItem.boundingBox.maxY - entityItem.boundingBox.minY;
                double ZSize = entityItem.boundingBox.maxZ - entityItem.boundingBox.minZ;
                entityItem.boundingBox.maxX = entityItem.boundingBox.minX + XSize * scriptCustomItem.getScaleX();
                entityItem.boundingBox.maxY = entityItem.boundingBox.minY + YSize * scriptCustomItem.getScaleY();
                entityItem.boundingBox.maxZ = entityItem.boundingBox.minZ + ZSize * scriptCustomItem.getScaleZ();
            }

            float entityRenderTicks = Minecraft.getMinecraft().timer.renderPartialTicks;
            float bobbing = MathHelper.sin(((float)entityItem.age + entityRenderTicks) / 10.0F + entityItem.hoverStart) * 0.1F + 0.1F;

            if(!scriptCustomItem.isNormalItem()){
                GL11.glRotatef(scriptCustomItem.getRotationX(), 1, 0, 0);
                GL11.glRotatef(scriptCustomItem.getRotationY(), 0, 1, 0);
                GL11.glRotatef(scriptCustomItem.getRotationZ(), 0, 0, 1);
            }

            GL11.glRotatef(scriptCustomItem.getRotationXRate() * entityRenderTicks %360, 1, 0, 0);
            GL11.glRotatef(scriptCustomItem.getRotationYRate() * entityRenderTicks %360, 0, 1, 0);
            GL11.glRotatef(scriptCustomItem.getRotationZRate() * entityRenderTicks %360, 0, 0, 1);

            if (!renderInFrame) {
                if(!scriptCustomItem.isNormalItem()){
                    GL11.glScalef(scriptCustomItem.getScaleX(), scriptCustomItem.getScaleY(), scriptCustomItem.getScaleZ());
                    GL11.glTranslatef(0.0F, (Math.max(scriptCustomItem.getScaleY(), 1) - 1) * (1.0F / 4), 0.0F);
                }
                GL11.glTranslatef(0.0F, -bobbing, 0.0F);
            }

            int color = scriptCustomItem.getColor();
            float itemRed = (color >> 16 & 255) / 255f;
            float itemGreen = (color >> 8  & 255) / 255f;
            float itemBlue = (color & 255) / 255f;
            GL11.glColor4f(itemRed, itemGreen, itemBlue, 1.0F);

            renderEntityCustomItem(scriptCustomItem, itemStack, entityItem);

            GL11.glPopMatrix();
            return;
        }

        if (type == ItemRenderType.EQUIPPED_FIRST_PERSON && scriptCustomItem.isNormalItem()) {
            GL11.glPushMatrix();
            GL11.glTranslatef(0.9375F, 0.0625F, 0.0F);
            GL11.glRotatef(-315.0F, 0.0F, 0.0F, 1.0F);
            GL11.glTranslatef(0.135F, 0.2F, 0.07F);

            int color = scriptCustomItem.getColor();
            float itemRed = (color >> 16 & 255) / 255f;
            float itemGreen = (color >> 8  & 255) / 255f;
            float itemBlue = (color & 255) / 255f;
            GL11.glColor4f(itemRed, itemGreen, itemBlue, 1.0F);

            GL11.glRotatef(-20.0F, 0.0F, 0.0F, 1.0F);
            GL11.glRotatef(-50.0F, 0.0F, 1.0F, 0.0F);
            GL11.glTranslatef(-0.09375F, 0.0625F, 0.0F);

            EntityLivingBase entityLivingBase = (EntityLivingBase) data[1];
            renderItem3d(scriptCustomItem, entityLivingBase, itemStack);

            GL11.glPopMatrix();
            return;
        }

        GL11.glPushMatrix();

        GL11.glTranslatef(0.9375F, 0.0625F, 0.0F);
        GL11.glRotatef(-315.0F, 0.0F, 0.0F, 1.0F);

        ((ItemCustomizable) itemStack.getItem()).renderOffset(scriptCustomItem);
        if(scriptCustomItem.isNormalItem()){
            GL11.glTranslatef(-0.05F, 0.3F, 0.3F);
            GL11.glRotatef(50, 1, 0, 0);
            GL11.glRotatef(-80, 0, 1, 0);
            GL11.glRotatef(80, 0, 0, 1);

            GL11.glRotatef(scriptCustomItem.getRotationXRate() * item3dRenderTicks %360, 1, 0, 0);
            GL11.glRotatef(scriptCustomItem.getRotationYRate() * item3dRenderTicks %360, 0, 1, 0);
            GL11.glRotatef(scriptCustomItem.getRotationZRate() * item3dRenderTicks %360, 0, 0, 1);

            GL11.glScalef(0.6F, 0.6F,0.6F);
        }
        else {
            GL11.glTranslatef(scriptCustomItem.getTranslateX(), scriptCustomItem.getTranslateY(), scriptCustomItem.getTranslateZ());
            GL11.glRotatef(scriptCustomItem.getRotationX(), 1, 0, 0);
            GL11.glRotatef(scriptCustomItem.getRotationY(), 0, 1, 0);
            GL11.glRotatef(scriptCustomItem.getRotationZ(), 0, 0, 1);

            GL11.glRotatef(scriptCustomItem.getRotationXRate() * item3dRenderTicks %360, 1, 0, 0);
            GL11.glRotatef(scriptCustomItem.getRotationYRate() * item3dRenderTicks %360, 0, 1, 0);
            GL11.glRotatef(scriptCustomItem.getRotationZRate() * item3dRenderTicks %360, 0, 0, 1);

            GL11.glScalef(scriptCustomItem.getScaleX(), scriptCustomItem.getScaleY(), scriptCustomItem.getScaleZ());
        }

        int color = scriptCustomItem.getColor();
        float itemRed = (color >> 16 & 255) / 255f;
        float itemGreen = (color >> 8  & 255) / 255f;
        float itemBlue = (color & 255) / 255f;
        GL11.glColor4f(itemRed, itemGreen, itemBlue, 1.0F);

        GL11.glRotatef(-20.0F, 0.0F, 0.0F, 1.0F);
        GL11.glRotatef(-50.0F, 0.0F, 1.0F, 0.0F);
        GL11.glTranslatef(-0.09375F, 0.0625F, 0.0F);

        EntityLivingBase entityLivingBase = (EntityLivingBase) data[1];
        renderItem3d(scriptCustomItem, entityLivingBase, itemStack);

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

    public void renderEntityCustomItem(IItemCustomizable scriptCustomItem, ItemStack itemStack, EntityItem entityItem) {
        int pass = 0;

        GL11.glPushMatrix();
            ClientCacheHandler.getImageData(scriptCustomItem.getTexture()).bindTexture();

            if (renderInFrame)
            {
                GL11.glTranslatef(0.0F, -0.05F, 0.0F);
                GL11.glScalef(1.025641F,1.025641F,1.025641F);
                GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
                GL11.glTranslatef(scriptCustomItem.getTranslateX(), scriptCustomItem.getTranslateY(), scriptCustomItem.getTranslateZ());
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

                if(!scriptCustomItem.isNormalItem())
                    GL11.glScalef(scriptCustomItem.getScaleX(), scriptCustomItem.getScaleY(), scriptCustomItem.getScaleZ());
                GL11.glTranslatef(-f7, -f8, -((f9 + f10) * (float)b0 / 2.0F));

                for (int k = 0; k < b0; ++k)
                {
                    // Makes items offset when in 3D, like when in 2D, looks much better. Considered a vanilla bug...
                    GL11.glTranslatef(0f, 0f, f9 + f10);
                    /*if (renderInFrame) {
                        GL11.glTranslatef(0.5F, 0.0F, 0.0F);
                        GL11.glRotatef(-90.0F, 0.0F, 0.0F, 1.0F);
                    }*/

                    ImageData imageData = ClientCacheHandler.getImageData(scriptCustomItem.getTexture());
                    ItemRenderer.renderItemIn2D(tessellator, f15, f4, f14, f5, imageData.getTotalWidth(), imageData.getTotalHeight(), f9);

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

    public void renderInventoryCustomItem(IItemCustomizable scriptCustomItem) {
        GL11.glPushMatrix();
            int color = scriptCustomItem.getColor();
            float itemRed = (color >> 16 & 255) / 255f;
            float itemGreen = (color >> 8  & 255) / 255f;
            float itemBlue = (color & 255) / 255f;
            GL11.glColor4f(itemRed, itemGreen, itemBlue, 1.0F);

            GL11.glDisable(GL11.GL_LIGHTING); //Forge: Make sure that render states are reset, a renderEffect can derp them up.
            GL11.glEnable(GL11.GL_ALPHA_TEST);

            ClientCacheHandler.getImageData(scriptCustomItem.getTexture()).bindTexture();
            renderCustomItemSlot(0,0,16,16, itemRed, itemGreen, itemBlue);

            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glDisable(GL11.GL_ALPHA_TEST);

            if (scriptCustomItem.getMCItemStack().hasEffect(0))
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

    public void renderItem3d(IItemCustomizable scriptCustomItem, EntityLivingBase entityLivingBase, ItemStack itemStack) {
        item3dRenderTicks++;

        Minecraft mc = Minecraft.getMinecraft();
        TextureManager texturemanager = mc.getTextureManager();
        int par3 = 0;

        ClientCacheHandler.getImageData(scriptCustomItem.getTexture()).bindTexture();

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

        renderCustomItemIn2D(scriptCustomItem, tessellator, 1.0F, 0.0F, 0.0F, 1.0F, 0.0625F);

        if (itemStack.hasEffect(par3)) {
            GL11.glDepthFunc(GL11.GL_EQUAL);
            GL11.glDisable(GL11.GL_LIGHTING);
            texturemanager.bindTexture(enchant);
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

    public static void renderCustomItemIn2D(IItemCustomizable wrapper, Tessellator p_78439_0_, float p_78439_1_, float p_78439_2_, float p_78439_3_, float p_78439_4_, float p_78439_7_)
    {
        ImageData imageData = ClientCacheHandler.getImageData(wrapper.getTexture());
        int width = imageData.getTotalWidth();
        int height = imageData.getTotalHeight();

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
