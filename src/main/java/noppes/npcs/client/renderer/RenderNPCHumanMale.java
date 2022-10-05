package noppes.npcs.client.renderer;

import static net.minecraftforge.client.IItemRenderer.ItemRenderType.EQUIPPED;
import static net.minecraftforge.client.IItemRenderer.ItemRendererHelper.BLOCK_3D;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.*;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.MinecraftForgeClient;
import noppes.npcs.client.model.ModelMPM;
import noppes.npcs.client.model.ModelNPCMale;
import noppes.npcs.constants.EnumAnimation;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;

import org.lwjgl.opengl.GL11;

public class RenderNPCHumanMale extends RenderNPCInterface
{

    private ModelNPCMale modelBipedMain;
    protected ModelNPCMale modelArmorChestplate;
    protected ModelNPCMale modelArmor;

    protected final ModelNPCMale originalBipedMain;
    protected final static ModelMPM steve64 = new ModelMPM(0, false);
    protected final static ModelMPM alex = new ModelMPM(0, true);

    protected final static ModelMPM steveArmorChest = new ModelMPM(1,0);
    protected final static ModelMPM steveArmor = new ModelMPM(0.5F,0);

    protected final static ModelMPM alex32armorChest = new ModelMPM(1,1);
    protected final static ModelMPM alex32armor = new ModelMPM(0.5F,1);

    public RenderNPCHumanMale(ModelNPCMale mainmodel, ModelNPCMale armorChest, ModelNPCMale armor)
    {
        super(mainmodel, 0.5F);
        modelBipedMain = mainmodel;
        originalBipedMain = mainmodel;
        modelArmorChestplate = armorChest;
        modelArmor = armor;
    }

    protected int func_130006_a(EntityLiving par1EntityLiving, int par2, float par3)
    {
        ItemStack itemstack = par1EntityLiving.func_130225_q(3 - par2);

        if (itemstack != null)
        {
            Item item = itemstack.getItem();

            if (item instanceof ItemArmor)
            {
                ItemArmor itemarmor = (ItemArmor)item;
                this.bindTexture(RenderBiped.getArmorResource(par1EntityLiving, itemstack, par2, null));
                ModelBiped modelbiped = par2 == 2 ? this.modelArmor : this.modelArmorChestplate;
                modelbiped.bipedHead.showModel = par2 == 0;
                modelbiped.bipedHeadwear.showModel = par2 == 0;
                modelbiped.bipedBody.showModel = par2 == 1 || par2 == 2;
                modelbiped.bipedRightArm.showModel = par2 == 1;
                modelbiped.bipedLeftArm.showModel = par2 == 1;
                modelbiped.bipedRightLeg.showModel = par2 == 2 || par2 == 3;
                modelbiped.bipedLeftLeg.showModel = par2 == 2 || par2 == 3;
                modelbiped = ForgeHooksClient.getArmorModel(par1EntityLiving, itemstack, par2, modelbiped);
                this.setRenderPassModel(modelbiped);
                modelbiped.onGround = this.mainModel.onGround;
                modelbiped.isRiding = this.mainModel.isRiding;
                modelbiped.isChild = this.mainModel.isChild;
                float f1 = 1.0F;

                //Move out of if to allow for more then just CLOTH to have color
                int j = itemarmor.getColor(itemstack);
                if (j != -1)
                {
                    float f2 = (float)(j >> 16 & 255) / 255.0F;
                    float f3 = (float)(j >> 8 & 255) / 255.0F;
                    float f4 = (float)(j & 255) / 255.0F;
                    GL11.glColor3f(f1 * f2, f1 * f3, f1 * f4);

                    if (itemstack.isItemEnchanted())
                    {
                        return 31;
                    }

                    return 16;
                }

                GL11.glColor3f(f1, f1, f1);

                if (itemstack.isItemEnchanted())
                {
                    return 15;
                }

                return 1;
            }
        }

        return -1;
    }
    @Override
    protected int shouldRenderPass(EntityLivingBase par1EntityLivingBase, int par2, float par3){
        return this.func_130006_a((EntityLiving)par1EntityLivingBase, par2, par3);
    }

    public void renderPlayer(EntityNPCInterface npc, double d, double d1, double d2, 
            float f, float f1)
    {
        if(npc instanceof EntityCustomNpc){
            EntityCustomNpc test = (EntityCustomNpc) npc;
            if(test.modelData.entityClass == null){
                int modelVal = npc.display.modelType;
                if(modelVal ==  1){
                    this.mainModel = steve64;
                    this.modelBipedMain = steve64;
                    this.modelArmorChestplate = steveArmorChest;
                    this.modelArmor = steveArmor;
                }
                else if(modelVal ==  2){
                    this.mainModel = alex;
                    this.modelBipedMain = alex;
                    this.modelArmorChestplate = alex32armorChest;
                    this.modelArmor = alex32armor;
                }
                else{
                    ((EntityCustomNpc) npc).modelData.bodywear = 0;
                    ((EntityCustomNpc) npc).modelData.armwear = 0;
                    ((EntityCustomNpc) npc).modelData.legwear = 0;
                    this.mainModel = originalModel;
                    this.modelBipedMain = originalBipedMain;
                    this.modelArmorChestplate = steveArmorChest;
                    this.modelArmor = steveArmor;
                }
            }
        }

        ItemStack itemstack = npc.getHeldItem();
        modelArmorChestplate.heldItemRight = modelArmor.heldItemRight = modelBipedMain.heldItemRight =
        		itemstack == null ? 0 : npc.hurtResistantTime > 0 ? 3 : 1;

        modelArmorChestplate.heldItemLeft = modelArmor.heldItemLeft = modelBipedMain.heldItemLeft = 
        		npc.getOffHand() == null ? 0 : npc.hurtResistantTime > 0 ? 3 : 1;
        
        modelArmorChestplate.isSneak = modelArmor.isSneak = modelBipedMain.isSneak = npc.isSneaking();

        modelArmorChestplate.isSleeping = modelArmor.isSleeping = modelBipedMain.isSleeping = npc.isPlayerSleeping();
        modelArmorChestplate.isDancing = modelArmor.isDancing = modelBipedMain.isDancing = npc.currentAnimation == EnumAnimation.DANCING;
        modelArmorChestplate.aimedBow = modelArmor.aimedBow = modelBipedMain.aimedBow = npc.currentAnimation == EnumAnimation.AIMING;
        
        modelArmorChestplate.isRiding = modelArmor.isRiding = modelBipedMain.isRiding = npc.isRiding();
        
        double d3 = d1 - (double)npc.yOffset;
        if(npc.isSneaking())
        {
            d3 -= 0.125D;
        }
        super.doRender(npc, d, d3, d2, f, f1);
        modelArmorChestplate.aimedBow = modelArmor.aimedBow = modelBipedMain.aimedBow = false;
        modelArmorChestplate.isSneak = modelArmor.isSneak = modelBipedMain.isSneak = false;
        modelArmorChestplate.heldItemRight = modelArmor.heldItemRight = modelBipedMain.heldItemRight = 0;
        modelArmorChestplate.heldItemLeft = modelArmor.heldItemLeft = modelBipedMain.heldItemLeft = 0;
    }

    protected void renderSpecials(EntityNPCInterface npc, float f)
    {
        super.renderEquippedItems(npc, f);
        GL11.glColor3f(1,1,1);
        int i = npc.getBrightnessForRender(f);
        int j = i % 65536;
        int k = i / 65536;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)j / 1.0F, (float)k / 1.0F);
        GL11.glColor3f(1,1,1);
        ItemStack itemstack = npc.inventory.armorItemInSlot(0);
        if(itemstack != null)
        {
            GL11.glPushMatrix();
            if(npc instanceof EntityCustomNpc){
            	EntityCustomNpc cnpc = (EntityCustomNpc) npc;
	            GL11.glTranslatef(0, cnpc.modelData.getBodyY(), 0);
	            this.modelBipedMain.bipedHead.postRender(0.0625F);
	            GL11.glScalef(cnpc.modelData.head.scaleX, cnpc.modelData.head.scaleY, cnpc.modelData.head.scaleZ);
            }
            else
	            this.modelBipedMain.bipedHead.postRender(0.0625F);

            IItemRenderer customRenderer = MinecraftForgeClient.getItemRenderer(itemstack, EQUIPPED);
            boolean is3D = (customRenderer != null && customRenderer.shouldUseRenderHelper(EQUIPPED, itemstack, BLOCK_3D));

            if (itemstack.getItem() instanceof ItemBlock)
            {
                if (is3D || RenderBlocks.renderItemIn3d(Block.getBlockFromItem(itemstack.getItem()).getRenderType()))
                {
                    float var6 = 0.625F;
                    GL11.glTranslatef(0.0F, -0.25F, 0.0F);
                    GL11.glRotatef(90.0F, 0.0F, 1.0F, 0.0F);
                    GL11.glScalef(var6, -var6, -var6);
                }

                this.renderManager.itemRenderer.renderItem(npc, itemstack, 0);
            }

            GL11.glPopMatrix();
        }
        GL11.glColor3f(1,1,1);
        ItemStack itemstack2 = npc.getHeldItem();
        if(itemstack2 != null)
        {
            float var6;
            GL11.glPushMatrix();
            float y = 0;
            float x = 0;
            if(npc instanceof EntityCustomNpc){
            	EntityCustomNpc cnpc = (EntityCustomNpc) npc;
                y = (cnpc.modelData.arms.scaleY - 1) * 0.7f;
        		x = (1 - cnpc.modelData.body.scaleX) * 0.28f + (1 - cnpc.modelData.arms.scaleX) * 0.175f;
                GL11.glTranslatef(x, cnpc.modelData.getBodyY(), 0);
            }
		    this.modelBipedMain.bipedRightArm.postRender(0.0625F);

            if(npc.getModelType() == 2){
                GL11.glTranslatef(-0.0125F, 0.4375F + y, 0.0625F);
            }
            else {
                GL11.glTranslatef(-0.0625F, 0.4375F + y, 0.0625F);
            }

            IItemRenderer customRenderer = MinecraftForgeClient.getItemRenderer(itemstack2, EQUIPPED);
            boolean is3D = (customRenderer != null && customRenderer.shouldUseRenderHelper(EQUIPPED, itemstack2, BLOCK_3D));
            Class<?> clazz = itemstack2.getItem().getClass();
            boolean pluginMod = itemstack2.getItem().getUnlocalizedName().contains("plug:");

            if (itemstack2.getItem() instanceof ItemBlock && (is3D || RenderBlocks.renderItemIn3d(Block.getBlockFromItem(itemstack2.getItem()).getRenderType())))
            {
                var6 = 0.5F;
                GL11.glTranslatef(0.0F, 0.1875F, -0.3125F);
                var6 *= 0.75F;
                GL11.glRotatef(20.0F, 1.0F, 0.0F, 0.0F);
                GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
                GL11.glScalef(-var6, -var6, var6);
            }
            else if (itemstack2.getItem() instanceof ItemBow && (customRenderer == null || pluginMod))
            {
                var6 = 0.625F;
                GL11.glTranslatef(0.0F, 0.125F, 0.3125F);
                GL11.glRotatef(-20.0F, 0.0F, 1.0F, 0.0F);
                GL11.glScalef(var6, -var6, var6);
                GL11.glRotatef(-100.0F, 1.0F, 0.0F, 0.0F);
                GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
            }
            else if (itemstack2.getItem().isFull3D() && !is3D)
            {
                var6 = 0.625F;

                if (itemstack2.getItem().shouldRotateAroundWhenRendering())
                {
                    GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
                    GL11.glTranslatef(0.0F, -0.125F, 0.0F);
                }
                
                if (npc.hurtResistantTime > 0 && npc.stats.resistances.playermelee > 1f)
                {
                    GL11.glTranslatef(0.05F, 0.0F, -0.1F);
                    GL11.glRotatef(-50.0F, 0.0F, 1.0F, 0.0F);
                    GL11.glRotatef(-10.0F, 1.0F, 0.0F, 0.0F);
                    GL11.glRotatef(-60.0F, 0.0F, 0.0F, 1.0F);
                }

                GL11.glTranslatef(0.0F, 0.1875F, 0.0F);
                GL11.glScalef(var6, -var6, var6);
                GL11.glRotatef(-100.0F, 1.0F, 0.0F, 0.0F);
                GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
            }
            else if(is3D){
                var6 = 0.5F;
                GL11.glTranslatef(0.0F, 0.1875F, -0.3125F);
                var6 *= 0.75F;
                GL11.glRotatef(20.0F, 1.0F, 0.0F, 0.0F);
                GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
                GL11.glScalef(-var6, -var6, var6);
            }
            else
            {
                var6 = 0.375F;
                GL11.glTranslatef(0.25F, 0.1875F, -0.1875F);
                GL11.glScalef(var6, var6, var6);
                GL11.glRotatef(60.0F, 0.0F, 0.0F, 1.0F);
                GL11.glRotatef(-90.0F, 1.0F, 0.0F, 0.0F);
                GL11.glRotatef(20.0F, 0.0F, 0.0F, 1.0F);
            }

            if (itemstack2.getItem().requiresMultipleRenderPasses())
            {
                for (int var25 = 0; var25 < itemstack2.getItem().getRenderPasses(itemstack2.getItemDamage()); ++var25)
                {
                    int var24 = itemstack2.getItem().getColorFromItemStack(itemstack2, var25);
                    float var26 = (float)(var24 >> 16 & 255) / 255.0F;
                    float var9 = (float)(var24 >> 8 & 255) / 255.0F;
                    float var10 = (float)(var24 & 255) / 255.0F;
                    GL11.glColor4f(var26, var9, var10, 1.0F);
                    this.renderManager.itemRenderer.renderItem(npc, itemstack2, var25);
                }
            }
            else
                renderManager.itemRenderer.renderItem(npc, itemstack2, 0);
            
            GL11.glPopMatrix();
        }
        GL11.glColor4f(1, 1, 1, 1.0F);
        itemstack2 = npc.getOffHand();
        if(itemstack2 != null)
        {
            GL11.glPushMatrix();
            float y = 0;
            float x = 0;
            if(npc instanceof EntityCustomNpc){
            	EntityCustomNpc cnpc = (EntityCustomNpc) npc;
                y = (cnpc.modelData.arms.scaleY - 1) * 0.7f;
        		x = (1 - cnpc.modelData.body.scaleX) * -0.28f + (1 - cnpc.modelData.arms.scaleX) * -0.175f;
                GL11.glTranslatef(x, cnpc.modelData.getBodyY(), 0);
            }
		    this.modelBipedMain.bipedLeftArm.postRender(0.0625F);

            if(npc.getModelType() == 2){
                GL11.glTranslatef(0.0125F, 0.4375F + y, 0.0625F);
            }
            else {
                GL11.glTranslatef(0.0625F, 0.4375F + y, 0.0625F);
            }

		    float var6;
            
            IItemRenderer customRenderer = MinecraftForgeClient.getItemRenderer(itemstack2, EQUIPPED);
            boolean is3D = (customRenderer != null && customRenderer.shouldUseRenderHelper(EQUIPPED, itemstack2, BLOCK_3D));

            Class<?> clazz = itemstack2.getItem().getClass();
            boolean pluginMod = itemstack2.getItem().getUnlocalizedName().contains("plug:");
            if(clazz.getSimpleName().equals("ItemShield") || clazz.getSimpleName().equals("ItemRotatedShield") || clazz.getSimpleName().equals("ItemClaw"))
            	GL11.glTranslatef(0.30f, 0, 0f);
            
            if (itemstack2.getItem() instanceof ItemBlock && (is3D || RenderBlocks.renderItemIn3d(Block.getBlockFromItem(itemstack2.getItem()).getRenderType())))
            {
                var6 = 0.5F;
                GL11.glTranslatef(0.0F, 0.1875F, -0.3125F);
                var6 *= 0.75F;
                GL11.glRotatef(20.0F, 1.0F, 0.0F, 0.0F);
                GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
                GL11.glScalef(var6, -var6, var6);
            }
            else if (itemstack2.getItem() instanceof ItemBow && (customRenderer == null || pluginMod))
            {
                var6 = 0.625F;
                GL11.glTranslatef(0.0F, 0.125F, 0.3125F);
                GL11.glRotatef(-20.0F, 0.0F, 1.0F, 0.0F);
                GL11.glScalef(var6, -var6, var6);
                GL11.glRotatef(-100.0F, 1.0F, 0.0F, 0.0F);
                GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
            }
            else if (itemstack2.getItem().isFull3D() && !is3D)
            {
                var6 = 0.625F;

                if (itemstack2.getItem().shouldRotateAroundWhenRendering())
                {
                    GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
                    GL11.glTranslatef(0.0F, -0.125F, 0.0F);
                }
                
                if (npc.hurtResistantTime > 0 && npc.stats.resistances.arrow > 1f)
                {
                    GL11.glTranslatef(0.05F, 0.0F, -0.1F);
                    GL11.glRotatef(50.0F, 0.0F, 1.0F, 0.0F);
                    GL11.glRotatef(-10.0F, 1.0F, 0.0F, 0.0F);
                    GL11.glRotatef(60.0F, 0.0F, 0.0F, 1.0F);
                }

                GL11.glTranslatef(0.0F, 0.1875F, 0.0F);
                GL11.glScalef(var6, -var6, var6);
                GL11.glRotatef(-100.0F, 1.0F, 0.0F, 0.0F);
                GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
            }
            else
            {
                var6 = 0.375F;
                GL11.glTranslatef(0.25F, 0.1875F, -0.1875F);
                GL11.glScalef(var6, var6, var6);
                GL11.glRotatef(60.0F, 0.0F, 0.0F, 1.0F);
                GL11.glRotatef(-90.0F, 1.0F, 0.0F, 0.0F);
                GL11.glRotatef(20.0F, 0.0F, 0.0F, 1.0F);
            }

            if (itemstack2.getItem().requiresMultipleRenderPasses())
            {
                for (int var25 = 0; var25 < itemstack2.getItem().getRenderPasses(itemstack2.getItemDamage()); ++var25)
                {
                    int var24 = itemstack2.getItem().getColorFromItemStack(itemstack2, var25);
                    float var26 = (float)(var24 >> 16 & 255) / 255.0F;
                    float var9 = (float)(var24 >> 8 & 255) / 255.0F;
                    float var10 = (float)(var24 & 255) / 255.0F;
                    GL11.glColor4f(var26, var9, var10, 1.0F);
                    this.renderManager.itemRenderer.renderItem(npc, itemstack2, var25);
                }
            }else
            {
                renderManager.itemRenderer.renderItem(npc, itemstack2, 0);
            }
            GL11.glPopMatrix();
        }
    }

    @Override
    protected void renderEquippedItems(EntityLivingBase entityliving, float f){
        renderSpecials((EntityNPCInterface)entityliving, f);
    }

    @Override
    public void doRender(EntityLiving entityliving, double d, double d1, double d2, float f, float f1){
        renderPlayer((EntityNPCInterface)entityliving, d, d1, d2, f, f1);
    }

    @Override
    public void doRender(Entity entity, double d, double d1, double d2, float f, float f1){
        renderPlayer((EntityNPCInterface)entity, d, d1, d2, f, f1);
    }

}
