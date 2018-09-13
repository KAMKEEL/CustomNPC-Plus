package noppes.npcs.client.renderer;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionHelper;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.entity.EntityProjectile;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderProjectile extends Render
{
	
	public boolean renderWithColor = true;
	private static final ResourceLocation field_110780_a = new ResourceLocation("textures/entity/arrow.png");
	private static final ResourceLocation field_110798_h = new ResourceLocation("textures/misc/enchanted_item_glint.png");
	private RenderBlocks itemRenderBlocks = new RenderBlocks();

    public void doRenderProjectile(EntityProjectile par1EntityProjectile, double par2, double par4, double par6, float par8, float par9)
    {
        GL11.glPushMatrix();
        GL11.glTranslatef((float)par2, (float)par4, (float)par6);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        float f = (float)par1EntityProjectile.getDataWatcher().getWatchableObjectInt(23) / 10.0F;
        ItemStack item = par1EntityProjectile.getItemDisplay();
        GL11.glScalef(f, f, f);
        Tessellator tessellator = Tessellator.instance;
        
        if (par1EntityProjectile.isArrow()) { //If it's the special case we are rendering an arrow

        	this.bindEntityTexture(par1EntityProjectile);
            GL11.glRotatef(par1EntityProjectile.prevRotationYaw + (par1EntityProjectile.rotationYaw - par1EntityProjectile.prevRotationYaw) * par9 - 90.0F, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(par1EntityProjectile.prevRotationPitch + (par1EntityProjectile.rotationPitch - par1EntityProjectile.prevRotationPitch) * par9, 0.0F, 0.0F, 1.0F);
            byte b0 = 0;
            float f2 = 0.0F;
            float f3 = 0.5F;
            float f4 = (float)(0 + b0 * 10) / 32.0F;
            float f5 = (float)(5 + b0 * 10) / 32.0F;
            float f6 = 0.0F;
            float f7 = 0.15625F;
            float f8 = (float)(5 + b0 * 10) / 32.0F;
            float f9 = (float)(10 + b0 * 10) / 32.0F;
            float f10 = 0.05625F;
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
            float f11 = (float)par1EntityProjectile.arrowShake - par9;

            if (f11 > 0.0F)
            {
                float f12 = -MathHelper.sin(f11 * 3.0F) * f11;
                GL11.glRotatef(f12, 0.0F, 0.0F, 1.0F);
            }

            GL11.glRotatef(45.0F, 1.0F, 0.0F, 0.0F);
            GL11.glScalef(f10, f10, f10);
            GL11.glTranslatef(-4.0F, 0.0F, 0.0F);
            GL11.glNormal3f(f10, 0.0F, 0.0F);
            tessellator.startDrawingQuads();
            tessellator.addVertexWithUV(-7.0D, -2.0D, -2.0D, (double)f6, (double)f8);
            tessellator.addVertexWithUV(-7.0D, -2.0D, 2.0D, (double)f7, (double)f8);
            tessellator.addVertexWithUV(-7.0D, 2.0D, 2.0D, (double)f7, (double)f9);
            tessellator.addVertexWithUV(-7.0D, 2.0D, -2.0D, (double)f6, (double)f9);
            tessellator.draw();
            GL11.glNormal3f(-f10, 0.0F, 0.0F);
            tessellator.startDrawingQuads();
            tessellator.addVertexWithUV(-7.0D, 2.0D, -2.0D, (double)f6, (double)f8);
            tessellator.addVertexWithUV(-7.0D, 2.0D, 2.0D, (double)f7, (double)f8);
            tessellator.addVertexWithUV(-7.0D, -2.0D, 2.0D, (double)f7, (double)f9);
            tessellator.addVertexWithUV(-7.0D, -2.0D, -2.0D, (double)f6, (double)f9);
            tessellator.draw();

            for (int i = 0; i < 4; ++i)
            {
                GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
                GL11.glNormal3f(0.0F, 0.0F, f10);
                tessellator.startDrawingQuads();
                tessellator.addVertexWithUV(-8.0D, -2.0D, 0.0D, (double)f2, (double)f4);
                tessellator.addVertexWithUV(8.0D, -2.0D, 0.0D, (double)f3, (double)f4);
                tessellator.addVertexWithUV(8.0D, 2.0D, 0.0D, (double)f3, (double)f5);
                tessellator.addVertexWithUV(-8.0D, 2.0D, 0.0D, (double)f2, (double)f5);
                tessellator.draw();
            }
        }
        
        else if (par1EntityProjectile.is3D()) {
        	GL11.glRotatef(par1EntityProjectile.prevRotationYaw + (par1EntityProjectile.rotationYaw - par1EntityProjectile.prevRotationYaw) * par9 - 90.0F, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(par1EntityProjectile.prevRotationPitch + (par1EntityProjectile.rotationPitch - par1EntityProjectile.prevRotationPitch) * par9 - 180, 0.0F, 0.0F, 1.0F);
             
             //Render a block
             if (item.getItemSpriteNumber() == 0 && item.getItem() instanceof ItemBlock && RenderBlocks.renderItemIn3d(Block.getBlockFromItem(item.getItem()).getRenderType()))
             {
                 Block block = Block.getBlockFromItem(item.getItem());
            	 this.bindTexture(TextureMap.locationBlocksTexture);
                 float f7 = 0.25F;
                 int j = block.getRenderType();

                 if (j == 1 || j == 19 || j == 12 || j == 2)
                 {
                     f7 = 0.5F;
                 }

                 float f5 = 1.0F;
                 this.itemRenderBlocks.renderBlockAsItem(block, item.getItemDamage(), 1.0F);
                 //this.renderBlocks.renderBlockAsItem(block, item.getItemDamage(), f5);
             }
             else //render a 3D item
             {
            	 float f4, f5, f6, f8;
            	 GL11.glTranslatef(-0.6f, -0.6f, 0);

                 if (item.getItem().requiresMultipleRenderPasses())
                 {

                     for (int k = 0; k < item.getItem().getRenderPasses(item.getItemDamage()); ++k)
                     {
                         IIcon icon = item.getItem().getIcon(item, k);
                         f8 = 1.0F;

                         if (this.renderWithColor)
                         {
                             int i = item.getItem().getColorFromItemStack(item, k);
                             f5 = (float)(i >> 16 & 255) / 255.0F;
                             f4 = (float)(i >> 8 & 255) / 255.0F;
                             f6 = (float)(i & 255) / 255.0F;
                             GL11.glColor4f(f5 * f8, f4 * f8, f6 * f8, 1.0F);
                     		this.renderManager.itemRenderer.renderItem(Minecraft.getMinecraft().thePlayer, item, 0);
                         }
                         else
                         {
                      		this.renderManager.itemRenderer.renderItem(Minecraft.getMinecraft().thePlayer, item, 0);
                         }
                     }
                 }
                 else
                 {

                     IIcon icon1 = item.getIconIndex();

                     if (this.renderWithColor)
                     {
                         int l = item.getItem().getColorFromItemStack(item, 0);
                         f8 = (float)(l >> 16 & 255) / 255.0F;
                         float f9 = (float)(l >> 8 & 255) / 255.0F;
                         f5 = (float)(l & 255) / 255.0F;
                         f4 = 1.0F;
                         this.renderDroppedItem(item, icon1, par9, f8 * f4, f9 * f4, f5 * f4, f);
                     }
                     else
                     {
                         this.renderDroppedItem(item, icon1, par9, 1.0F, 1.0F, 1.0F, f);
                     }
                 }
             }
        }
        else //If the render is a sprite
        {
//	        IIcon icon = item.getIconIndex();
	        IIcon icon = item.getItem().getIconFromDamage(item.getItemDamage());
	        
	        this.bindTexture(TextureMap.locationItemsTexture);
            if (item.getItem().requiresMultipleRenderPasses())
            {
                for (int k = 0; k < item.getItem().getRenderPasses(item.getItemDamage()); ++k)
                {
                    int i = item.getItem().getColorFromItemStack(item, k);
                    float f5 = (float)(i >> 16 & 255) / 255.0F;
                    float f4 = (float)(i >> 8 & 255) / 255.0F;
                    float f6 = (float)(i & 255) / 255.0F;
                    GL11.glColor4f(f5, f4, f6, 1.0F);
                }
            }
	        if (icon == ItemPotion.func_94589_d("bottle_splash") || icon == ItemPotion.func_94589_d("bottle_drinkable"))
            {
	        	int var12 = PotionHelper.func_77915_a(item.getItemDamage(), false);
                float var13 = (float)(var12 >> 16 & 255) / 255.0F;
                float var14 = (float)(var12 >> 8 & 255) / 255.0F;
                float var15 = (float)(var12 & 255) / 255.0F;
                GL11.glColor3f(var13, var14, var15);
                GL11.glPushMatrix();
                this.renderSprite(tessellator, ItemPotion.func_94589_d("overlay"));
                GL11.glPopMatrix();
                GL11.glColor3f(1.0F, 1.0F, 1.0F);
            }
	        this.renderSprite(tessellator, icon);
        }
        if (par1EntityProjectile.is3D() && par1EntityProjectile.glows()) {
        	GL11.glDisable(GL11.GL_LIGHTING);
        }
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glPopMatrix();
    	GL11.glEnable(GL11.GL_LIGHTING);
    }

    /**
     * Actually renders the given argument. This is a synthetic bridge method, always casting down its argument and then
     * handing it off to a worker function which does the actual work. In all probabilty, the class Render is generic
     * (Render<T extends Entity) and this method has signature public void doRender(T entity, double d, double d1,
     * double d2, float f, float f1). But JAD is pre 1.5 so doesn't do that.
     */
    
    private void renderSprite(Tessellator par1Tessellator, IIcon par2Icon)
    {
        float f = par2Icon.getMinU();
        float f1 = par2Icon.getMaxU();
        float f2 = par2Icon.getMinV();
        float f3 = par2Icon.getMaxV();
        float f4 = 1.0F;
        float f5 = 0.5F;
        float f6 = 0.25F;
        GL11.glRotatef(180.0F - this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(-this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
        par1Tessellator.startDrawingQuads();
        par1Tessellator.setNormal(0.0F, 1.0F, 0.0F);
        par1Tessellator.addVertexWithUV((double)(0.0F - f5), (double)(0.0F - f6), 0.0D, (double)f, (double)f3);
        par1Tessellator.addVertexWithUV((double)(f4 - f5), (double)(0.0F - f6), 0.0D, (double)f1, (double)f3);
        par1Tessellator.addVertexWithUV((double)(f4 - f5), (double)(f4 - f6), 0.0D, (double)f1, (double)f2);
        par1Tessellator.addVertexWithUV((double)(0.0F - f5), (double)(f4 - f6), 0.0D, (double)f, (double)f2);
        par1Tessellator.draw();
    }
    
    private void renderDroppedItem(ItemStack item, IIcon par2Icon, float par4, float par5, float par6, float par7, float par8)
    {
    	Tessellator tessellator = Tessellator.instance;

        if (par2Icon == null)
        {
        	TextureManager texturemanager = Minecraft.getMinecraft().getTextureManager();
            ResourceLocation resourcelocation = texturemanager.getResourceLocation(item.getItemSpriteNumber());
            par2Icon = ((TextureMap)texturemanager.getTexture(resourcelocation)).registerIcon("missingno");
        }

        float f4 = par2Icon.getMinU();
        float f5 = par2Icon.getMaxU();
        float f6 = par2Icon.getMinV();
        float f7 = par2Icon.getMaxV();
        float f8 = 1.0F;
        float f9 = 0.5F;
        float f10 = 0.25F;
        float f11;
        float f12 = 0.0625F;
        
        if (item.getItemSpriteNumber() == 0)
        {
            this.bindTexture(TextureMap.locationBlocksTexture);
        }
        else
        {
            this.bindTexture(TextureMap.locationItemsTexture);
        }

        GL11.glColor4f(par5, par6, par7, 1.0F);
        ItemRenderer.renderItemIn2D(tessellator, f5, f6, f4, f7, ((IIcon)par2Icon).getIconWidth(), ((IIcon)par2Icon).getIconHeight(), f12);

        if (item != null && item.hasEffect(0))
        {
            GL11.glDepthFunc(GL11.GL_EQUAL);
            GL11.glDisable(GL11.GL_LIGHTING);
            this.renderManager.renderEngine.bindTexture(field_110798_h);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_COLOR, GL11.GL_ONE);
            float f13 = 0.76F;
            GL11.glColor4f(0.5F * f13, 0.25F * f13, 0.8F * f13, 1.0F);
            GL11.glMatrixMode(GL11.GL_TEXTURE);
            GL11.glPushMatrix();
            GL11.glScalef(par8, par8, par8);
            float f15 = (float)(Minecraft.getSystemTime() % 3000L) / 3000.0F * 8.0F;
            GL11.glTranslatef(f15, 0.0F, 0.0F);
            GL11.glRotatef(-50.0F, 0.0F, 0.0F, 1.0F);
            ItemRenderer.renderItemIn2D(tessellator, 0.0F, 0.0F, 1.0F, 1.0F, 255, 255, f12);
            GL11.glPopMatrix();
            GL11.glPushMatrix();
            GL11.glScalef(par8, par8, par8);
            f15 = (float)(Minecraft.getSystemTime() % 4873L) / 4873.0F * 8.0F;
            GL11.glTranslatef(-f15, 0.0F, 0.0F);
            GL11.glRotatef(10.0F, 0.0F, 0.0F, 1.0F);
            ItemRenderer.renderItemIn2D(tessellator, 0.0F, 0.0F, 1.0F, 1.0F, 255, 255, f12);
            GL11.glPopMatrix();
            GL11.glMatrixMode(GL11.GL_MODELVIEW);
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glDepthFunc(GL11.GL_LEQUAL);
        }
    }
    
    @Override
    public void doRender(Entity par1Entity, double par2, double par4, double par6, float par8, float par9)
    {
        this.doRenderProjectile((EntityProjectile)par1Entity, par2, par4, par6, par8, par9);
    }

	protected ResourceLocation func_110779_a(EntityProjectile par1EntityProjectile)
    {
        return par1EntityProjectile.isArrow() ? field_110780_a : this.renderManager.renderEngine.getResourceLocation(par1EntityProjectile.getItemDisplay().getItemSpriteNumber());
    }

    @Override
	protected ResourceLocation getEntityTexture(Entity par1Entity) {
		return this.func_110779_a((EntityProjectile)par1Entity);
	}
}
