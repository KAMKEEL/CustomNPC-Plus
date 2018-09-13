package noppes.npcs.client.renderer;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.resources.IResource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.client.model.ModelPony;
import noppes.npcs.client.model.ModelPonyArmor;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.entity.EntityNpcPony;

import org.lwjgl.opengl.GL11;

public class RenderNPCPony extends RenderNPCInterface
{

    private ModelPony modelBipedMain;
    private ModelPonyArmor modelArmorChestplate;
    private ModelPonyArmor modelArmor;

    public RenderNPCPony()
    {
        super(new ModelPony(0.0F), 0.5F);
        modelBipedMain = (ModelPony)mainModel;
        modelArmorChestplate = new ModelPonyArmor(1.0F);
        modelArmor = new ModelPonyArmor(0.5F);
    }

    protected int setArmorModel(EntityNPCInterface entityplayer, int i, float f)
    {
        ItemStack itemstack = entityplayer.inventory.armorItemInSlot(i);
        if(itemstack != null)
        {
            Item item = itemstack.getItem();
            if(item instanceof ItemArmor)
            {
                ItemArmor itemarmor = (ItemArmor)item;
                this.bindTexture(RenderBiped.getArmorResource(entityplayer, itemstack, i, null));
                ModelPonyArmor modelbiped = i != 2 ? modelArmorChestplate : modelArmor;
                modelbiped.head.showModel = i == 0;

                modelbiped.Body.showModel = i == 1 ;
                modelbiped.BodyBack.showModel = i == 1 ;
                modelbiped.rightarm.showModel = i == 3;
                modelbiped.LeftArm.showModel = i == 3;
                modelbiped.RightLeg.showModel =  i == 3;
                modelbiped.LeftLeg.showModel = i == 3;

                modelbiped.rightarm2.showModel = i == 2;
                modelbiped.LeftArm2.showModel = i == 2;
                modelbiped.RightLeg2.showModel = i == 2;
                modelbiped.LeftLeg2.showModel = i == 2;
                setRenderPassModel(modelbiped);

                float var8 = 1.0F;

                if (itemarmor.getArmorMaterial() == ArmorMaterial.CLOTH)
                {
                    int var9 = itemarmor.getColor(itemstack);
                    float var10 = (float)(var9 >> 16 & 255) / 255.0F;
                    float var11 = (float)(var9 >> 8 & 255) / 255.0F;
                    float var12 = (float)(var9 & 255) / 255.0F;
                    GL11.glColor3f(var8 * var10, var8 * var11, var8 * var12);

                    if (itemstack.isItemEnchanted())
                    {
                        return 31;
                    }

                    return 16;
                }

                GL11.glColor3f(var8, var8, var8);
                
                return !itemstack.isItemEnchanted() ? 1 : 15;
            }
        }
        return -1;
    }

	@Override
	public ResourceLocation getEntityTexture(Entity entity) {
		EntityNpcPony pony = (EntityNpcPony) entity;
		boolean check = pony.textureLocation == null || pony.textureLocation != pony.checked;
		ResourceLocation loc = super.getEntityTexture(pony);
    	if(check){
    		try {
				IResource resource = Minecraft.getMinecraft().getResourceManager().getResource(loc);
				BufferedImage bufferedimage = ImageIO.read(resource.getInputStream());

				pony.isPegasus = false;
				pony.isUnicorn = false;
		        Color color = new Color(bufferedimage.getRGB(0, 0), true);
		        Color color1 = new Color(249, 177, 49, 255);
		        Color color2 = new Color(136, 202, 240, 255);
		        Color color3 = new Color(209, 159, 228, 255);
		        Color color4 = new Color(254, 249, 252, 255);
		        if(color.equals(color1))
		        {
		        }
		        if(color.equals(color2))
		        {
		        	pony.isPegasus = true;
		        }
		        if(color.equals(color3))
		        {
		        	pony.isUnicorn = true;
		        }
		        if(color.equals(color4))
		        {
		        	pony.isPegasus = true;
		        	pony.isUnicorn = true;
		        }
		        pony.checked = loc;
    		
    		} catch (IOException e) {
				
			}
    	}
		return loc;
	}

    public void renderPlayer(EntityNpcPony pony, double d, double d1, double d2, 
            float f, float f1)
    {    	
        ItemStack itemstack = pony.getHeldItem();
        setRenderPassModel(modelBipedMain);
        
        modelArmorChestplate.heldItemRight = modelArmor.heldItemRight = modelBipedMain.heldItemRight = itemstack == null ? 0 : 1;
        modelArmorChestplate.isSneak = modelArmor.isSneak = modelBipedMain.isSneak = pony.isSneaking();
        modelArmorChestplate.isRiding = modelArmor.isRiding = modelBipedMain.isRiding = false;
        modelArmorChestplate.isSleeping = modelArmor.isSleeping = modelBipedMain.isSleeping = pony.isPlayerSleeping();
        modelArmorChestplate.isUnicorn = modelArmor.isUnicorn = modelBipedMain.isUnicorn = pony.isUnicorn;
        modelArmorChestplate.isPegasus = modelArmor.isPegasus = modelBipedMain.isPegasus = pony.isPegasus;
        double d3 = d1 - (double)pony.yOffset;
        if(pony.isSneaking())
        {
            d3 -= 0.125D;
        }
        super.doRender(pony, d, d3, d2, f, f1);
        modelArmorChestplate.aimedBow = modelArmor.aimedBow = modelBipedMain.aimedBow = false;
        modelArmorChestplate.isRiding = modelArmor.isRiding = modelBipedMain.isRiding = false;
        modelArmorChestplate.isSneak = modelArmor.isSneak = modelBipedMain.isSneak = false;
        modelArmorChestplate.heldItemRight = modelArmor.heldItemRight = modelBipedMain.heldItemRight = 0;
    }
//    @Override
//    protected void rotatePlayer(EntityNPCInterface entityplayer, float f, float f1, float f2)
//    {
//        if(entityplayer.isEntityAlive() && entityplayer.isPlayerSleeping())
//        {
//            GL11.glRotatef(entityplayer.orientation, 0.0F, 1.0F, 0.0F);
//            GL11.glTranslatef(-1.25F, -0.875F, 0.0F);
//            GL11.glRotatef(90F, 0.0F, 1.0F, 0.0F);
//        } else
//        {
//            GL11.glRotatef(180F - f1, 0.0F, 1.0F, 0.0F);
//            if(entityplayer.deathTime > 0)
//            {
//                float f3 = ((((float)entityplayer.deathTime + f2) - 1.0F) / 20F) * 1.6F;
//                f3 = MathHelper.sqrt_float(f3);
//                if(f3 > 1.0F)
//                {
//                    f3 = 1.0F;
//                }
//                GL11.glRotatef(f3 * getDeathMaxRotation(entityplayer), 0.0F, 0.0F, 1.0F);
//            }
//        }
//    }
    protected void renderSpecials(EntityNpcPony entityplayer, float f)
    {
        super.renderEquippedItems(entityplayer, f);
        if(!entityplayer.isPlayerSleeping())
        {
            if(entityplayer.isUnicorn)
            {
                renderDrop(this.renderManager, entityplayer, modelBipedMain.unicornarm, 1.0F, 0.35F, 0.5375F, -0.45F);
            } else
            {
                renderDrop(this.renderManager, entityplayer, modelBipedMain.RightArm, 1.0F, -0.0625F, 0.8375F, 0.0625F);
            }
        }
    }


    protected void renderDrop(RenderManager rendermanager, EntityNpcPony entityplayer, ModelRenderer modelrenderer, float f, float f1, float f2, float f3)
    {
        ItemStack itemstack = entityplayer.getHeldItem();
        if(itemstack == null)
        {
            return;
        }
        GL11.glPushMatrix();
        if(modelrenderer != null)
        {
            modelrenderer.postRender(f * 0.0625F);
        }
        GL11.glTranslatef(f1, f2, f3);
        if(itemstack.getItem() instanceof ItemBlock && RenderBlocks.renderItemIn3d(Block.getBlockFromItem(itemstack.getItem()).getRenderType()))
        {
            GL11.glTranslatef(0.0F, 0.1875F, -0.3125F);
            GL11.glRotatef(20F, 1.0F, 0.0F, 0.0F);
            GL11.glRotatef(45F, 0.0F, 1.0F, 0.0F);
            float f4 = 0.375F * f;
            GL11.glScalef(f4, -f4, f4);
        } else
        if(itemstack.getItem() == Items.bow)
        {
            GL11.glTranslatef(0.0F, 0.125F, 0.3125F);
            GL11.glRotatef(-20F, 0.0F, 1.0F, 0.0F);
            float f5 = 0.625F * f;
            GL11.glScalef(f5, -f5, f5);
            GL11.glRotatef(-100F, 1.0F, 0.0F, 0.0F);
            GL11.glRotatef(45F, 0.0F, 1.0F, 0.0F);
        } else
        if(itemstack.getItem().isFull3D())
        {
            if(itemstack.getItem().shouldRotateAroundWhenRendering())
            {
                GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
                GL11.glTranslatef(0.0F, -0.125F, 0.0F);
            }
            GL11.glTranslatef(0.0F, 0.1875F, 0.0F);
            float f6 = 0.625F * f;
            GL11.glScalef(f6, -f6, f6);
            GL11.glRotatef(-100F, 1.0F, 0.0F, 0.0F);
            GL11.glRotatef(45F, 0.0F, 1.0F, 0.0F);
        } else
        {
            GL11.glTranslatef(0.25F, 0.1875F, -0.1875F);
            float f7 = 0.375F * f;
            GL11.glScalef(f7, f7, f7);
            GL11.glRotatef(60F, 0.0F, 0.0F, 1.0F);
            GL11.glRotatef(-90F, 1.0F, 0.0F, 0.0F);
            GL11.glRotatef(20F, 0.0F, 0.0F, 1.0F);
        }
        if(itemstack.getItem() == Items.potionitem)
        {
            for (int j = 0; j <= 1; j++)
            {
                int k = itemstack.getItem().getColorFromItemStack(itemstack, j);
                float f9 = (float)(k >> 16 & 0xff) / 255F;
                float f10 = (float)(k >> 8 & 0xff) / 255F;
                float f12 = (float)(k & 0xff) / 255F;
                GL11.glColor4f(f9, f10, f12, 1.0F);
                renderManager.itemRenderer.renderItem(entityplayer, itemstack, j);
            }
        } else
        {
            rendermanager.itemRenderer.renderItem(entityplayer, itemstack, 0);
        }
        GL11.glPopMatrix();
    }
    @Override
    protected int shouldRenderPass(EntityLivingBase entityliving, int i, float f)
    {
        return setArmorModel((EntityNPCInterface)entityliving, i, f);
    }

    @Override
    protected void renderEquippedItems(EntityLivingBase entityliving, float f)
    {
        renderSpecials((EntityNpcPony)entityliving, f);
    }

    @Override
    public void doRender(EntityLiving entityliving, double d, double d1, double d2, 
            float f, float f1)
    {
        renderPlayer((EntityNpcPony)entityliving, d, d1, d2, f, f1);
    }

    @Override
    public void doRender(Entity entity, double d, double d1, double d2, 
            float f, float f1)
    {
        renderPlayer((EntityNpcPony)entity, d, d1, d2, f, f1);
    }

}
