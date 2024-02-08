package noppes.npcs.mixin;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.MinecraftForgeClient;
import noppes.npcs.client.ClientCacheHandler;
import noppes.npcs.client.ClientEventHandler;
import noppes.npcs.constants.EnumAnimationPart;
import noppes.npcs.controllers.data.Animation;
import noppes.npcs.controllers.data.AnimationData;
import noppes.npcs.controllers.data.Frame;
import noppes.npcs.controllers.data.FramePart;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.minecraftforge.client.IItemRenderer.ItemRenderType.EQUIPPED_FIRST_PERSON;

@Mixin(ItemRenderer.class)
public class MixinItemRenderer {

    @Inject(method = "renderItemInFirstPerson", at = @At(value = "HEAD"), cancellable = true)
    public void renderItemInFirstPerson(float p_78440_1_, CallbackInfo callbackInfo)
    {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        Render renderer = RenderManager.instance.getEntityRenderObject(player);
        if (renderer instanceof RendererLivingEntity && ((RendererLivingEntity) renderer).mainModel instanceof ModelBiped) {
            Render playerRenderer = RenderManager.instance.getEntityRenderObject(player);
            if (playerRenderer instanceof RendererLivingEntity) {
                ClientEventHandler.renderer = (RendererLivingEntity) playerRenderer;
            }
            ClientEventHandler.partialRenderTick = Minecraft.getMinecraft().timer.renderPartialTicks;
            ClientEventHandler.partialHandTicks = p_78440_1_;

            ClientEventHandler.renderingPlayer = player;
            ClientEventHandler.firstPersonAnimation = true;
            if (mixin_renderFirstPersonAnimation(p_78440_1_, player, (ModelBiped) ((RendererLivingEntity) renderer).mainModel)) {
                callbackInfo.cancel();
            }
            ClientEventHandler.firstPersonAnimation = false;
            ClientEventHandler.renderingPlayer = null;
        }
    }

    private boolean mixin_renderFirstPersonAnimation(float partialRenderTick, EntityPlayer player, ModelBiped model) {
        AnimationData animData = ClientCacheHandler.playerAnimations.get(player.getUniqueID());
        if (animData != null && animData.isActive()) {
            Frame frame = (Frame) animData.animation.currentFrame();
            if (frame.frameParts.containsKey(EnumAnimationPart.FULL_MODEL)) {
                FramePart part = frame.frameParts.get(EnumAnimationPart.FULL_MODEL);
                part.interpolateOffset();
                part.interpolateAngles();
            }
        }

        ModelRenderer[] parts = new ModelRenderer[]{model.bipedRightArm, model.bipedLeftArm, model.bipedRightLeg, model.bipedLeftLeg};
        EnumAnimationPart[] enumParts = new EnumAnimationPart[]{EnumAnimationPart.RIGHT_ARM, EnumAnimationPart.LEFT_ARM, EnumAnimationPart.RIGHT_LEG, EnumAnimationPart.LEFT_LEG};
        Frame frame;

        AnimationData animationData = ClientCacheHandler.playerAnimations.get(player.getUniqueID());
        if (animationData != null && animationData.isActive() && animationData.getAnimation() != null && animationData.getAnimation().currentFrame() != null) {
            Animation animation = (Animation) animationData.getAnimation();
            frame = (Frame) animation.currentFrame();
            for (int i = 0; i < parts.length; i++) {
                ModelRenderer part = parts[i];
                EnumAnimationPart enumPart = enumParts[i];
                FramePart originalPart = ClientEventHandler.originalValues.get(part);
                if (originalPart != null && frame.frameParts.containsKey(enumPart)) {
                    FramePart animatedPart = frame.frameParts.get(enumPart);
                    part.rotationPointX = originalPart.pivot[0] + animatedPart.prevPivots[0];
                    part.rotationPointY = originalPart.pivot[1] + animatedPart.prevPivots[1];
                    part.rotationPointZ = originalPart.pivot[2] + animatedPart.prevPivots[2];
                    part.rotateAngleX = animatedPart.prevRotations[0];
                    part.rotateAngleY = animatedPart.prevRotations[1];
                    part.rotateAngleZ = animatedPart.prevRotations[2];
                }
            }
        } else {
            return false;
        }

        ItemRenderer itemRenderer = Minecraft.getMinecraft().entityRenderer.itemRenderer;
        float f1 = 1.0F;//itemRenderer.prevEquippedProgress + (itemRenderer.equippedProgress - itemRenderer.prevEquippedProgress) * partialRenderTick;

        EntityClientPlayerMP entityclientplayermp = Minecraft.getMinecraft().thePlayer;
//        float f2 = entityclientplayermp.prevRotationPitch + (entityclientplayermp.rotationPitch - entityclientplayermp.prevRotationPitch) * partialRenderTick;
//        GL11.glPushMatrix();
//        GL11.glRotatef(f2, 1.0F, 0.0F, 0.0F);
//        GL11.glRotatef(entityclientplayermp.prevRotationYaw + (entityclientplayermp.rotationYaw - entityclientplayermp.prevRotationYaw) * p_78440_1_, 0.0F, 1.0F, 0.0F);
//        RenderHelper.enableStandardItemLighting();
//        GL11.glPopMatrix();
        EntityPlayerSP entityplayersp = (EntityPlayerSP)entityclientplayermp;
        float f3 = entityplayersp.prevRenderArmPitch + (entityplayersp.renderArmPitch - entityplayersp.prevRenderArmPitch) * partialRenderTick;
        float f4 = entityplayersp.prevRenderArmYaw + (entityplayersp.renderArmYaw - entityplayersp.prevRenderArmYaw) * partialRenderTick;
        GL11.glRotatef((entityclientplayermp.rotationPitch - f3) * 0.1F, 1.0F, 0.0F, 0.0F);
        GL11.glRotatef((entityclientplayermp.rotationYaw - f4) * 0.1F, 0.0F, 1.0F, 0.0F);
        ItemStack itemstack = itemRenderer.itemToRender;

        int i = Minecraft.getMinecraft().theWorld.getLightBrightnessForSkyBlocks(MathHelper.floor_double(entityclientplayermp.posX), MathHelper.floor_double(entityclientplayermp.posY), MathHelper.floor_double(entityclientplayermp.posZ), 0);
        int j = i % 65536;
        int k = i / 65536;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)j / 1.0F, (float)k / 1.0F);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getMinecraft().getTextureManager().bindTexture(entityclientplayermp.getLocationSkin());

        if (frame.frameParts.containsKey(EnumAnimationPart.RIGHT_ARM)) {
            GL11.glPushMatrix();
            GL11.glTranslatef(0.1F, 0.4F + -0.75F * 0.8F - (1.0F - f1) * 0.6F, -0.3F);
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
            GL11.glRotatef(180.0F, 1.0F, 0.0F, 0.0F);
            GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(0.0F, 0.0F, 0.0F, 1.0F);
            model.bipedRightArm.render(0.0625F);
            GL11.glPopMatrix();
        }


        if (frame.frameParts.containsKey(EnumAnimationPart.LEFT_ARM)) {
            GL11.glPushMatrix();
            GL11.glTranslatef(-0.1F, 0.4F + -0.75F * 0.8F - (1.0F - f1) * 0.6F, -0.3F);
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
            GL11.glRotatef(180.0F, 1.0F, 0.0F, 0.0F);
            GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(0.0F, 0.0F, 0.0F, 1.0F);
            model.bipedLeftArm.render(0.0625F);
            GL11.glPopMatrix();
        }


        if (frame.frameParts.containsKey(EnumAnimationPart.RIGHT_LEG)) {
            GL11.glPushMatrix();
            GL11.glTranslatef(0.025F, 0.6F + -0.75F * 0.8F - (1.0F - f1) * 0.6F, -0.4F);
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
            GL11.glRotatef(180.0F, 1.0F, 0.0F, 0.0F);
            GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(0.0F, 0.0F, 0.0F, 1.0F);
            model.bipedRightLeg.render(0.0625F);
            GL11.glPopMatrix();
        }

        if (frame.frameParts.containsKey(EnumAnimationPart.LEFT_LEG)) {
            GL11.glPushMatrix();
            GL11.glTranslatef(-0.025F, 0.6F + -0.75F * 0.8F - (1.0F - f1) * 0.6F, -0.4F);
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
            GL11.glRotatef(180.0F, 1.0F, 0.0F, 0.0F);
            GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(0.0F, 0.0F, 0.0F, 1.0F);
            model.bipedLeftLeg.render(0.0625F);
            GL11.glPopMatrix();
        }

        if (frame.frameParts.containsKey(EnumAnimationPart.RIGHT_ARM) && itemstack != null)
        {
            float f11, f12;
            GL11.glPushMatrix();

            if (player.fishEntity != null)
            {
                itemstack = new ItemStack(Items.stick);
            }

            float p_78785_1_ = 0.0625F;
            GL11.glTranslatef(0.1F, 0.4F + -0.75F * 0.8F - (1.0F - f1) * 0.6F, -0.3F);
            GL11.glRotatef(180.0F, 1.0F, 0.0F, 0.0F);
            GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);

            GL11.glTranslatef(model.bipedRightArm.rotationPointX * p_78785_1_, model.bipedRightArm.rotationPointY * p_78785_1_, model.bipedRightArm.rotationPointZ * p_78785_1_);
            GL11.glRotatef((float) Math.toDegrees(model.bipedRightArm.rotateAngleZ), 0, 0, 1);
            GL11.glRotatef((float) Math.toDegrees(model.bipedRightArm.rotateAngleY), 0, 1, 0);
            GL11.glRotatef((float) Math.toDegrees(model.bipedRightArm.rotateAngleX), 1, 0, 0);
            GL11.glTranslatef(-0.1F, 0.6F, 0.0F);

            GL11.glRotatef(255, 0, 1, 0);
            GL11.glRotatef(45, 1, 0, 0);
            GL11.glRotatef(80, 0, 0, 1);

            float f6 = 1/1.5F;
            GL11.glScalef(f6, f6, f6);

            if (itemstack.getItem().requiresMultipleRenderPasses())
            {
                for (k = 0; k < itemstack.getItem().getRenderPasses(itemstack.getItemDamage()); ++k)
                {
                    i = itemstack.getItem().getColorFromItemStack(itemstack, k);
                    f12 = (float)(i >> 16 & 255) / 255.0F;
                    f3 = (float)(i >> 8 & 255) / 255.0F;
                    f4 = (float)(i & 255) / 255.0F;
                    GL11.glColor4f(f12, f3, f4, 1.0F);
                    mixin_renderItem(player, itemstack, k, EQUIPPED_FIRST_PERSON);
                }
            }
            else
            {
                k = itemstack.getItem().getColorFromItemStack(itemstack, 0);
                f11 = (float)(k >> 16 & 255) / 255.0F;
                f12 = (float)(k >> 8 & 255) / 255.0F;
                f3 = (float)(k & 255) / 255.0F;
                GL11.glColor4f(f11, f12, f3, 1.0F);
                mixin_renderItem(player, itemstack, 0, EQUIPPED_FIRST_PERSON);
            }

            GL11.glPopMatrix();
        }

        for (int p = 0; p < parts.length; p++) {
            ModelRenderer part = parts[p];
            EnumAnimationPart enumPart = enumParts[p];
            FramePart originalPart = ClientEventHandler.originalValues.get(part);
            if (originalPart != null && frame.frameParts.containsKey(enumPart)) {
                part.rotationPointX = originalPart.pivot[0];
                part.rotationPointY = originalPart.pivot[1];
                part.rotationPointZ = originalPart.pivot[2];
                part.rotateAngleX = originalPart.rotation[0];
                part.rotateAngleY = originalPart.rotation[1];
                part.rotateAngleZ = originalPart.rotation[2];
            }
        }

        return true;
    }

    @Shadow private RenderBlocks renderBlocksIr;
    @Shadow private static ResourceLocation RES_ITEM_GLINT;

    private void mixin_renderItem(EntityLivingBase p_78443_1_, ItemStack p_78443_2_, int p_78443_3_, IItemRenderer.ItemRenderType type) {
        GL11.glPushMatrix();
        TextureManager texturemanager = Minecraft.getMinecraft().getTextureManager();
        Item item = p_78443_2_.getItem();
        Block block = Block.getBlockFromItem(item);

        if (p_78443_2_ != null && block != null && block.getRenderBlockPass() != 0) {
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glEnable(GL11.GL_CULL_FACE);
            OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        }
        IItemRenderer customRenderer = MinecraftForgeClient.getItemRenderer(p_78443_2_, type);
        if (customRenderer != null) {
            texturemanager.bindTexture(texturemanager.getResourceLocation(p_78443_2_.getItemSpriteNumber()));
            ForgeHooksClient.renderEquippedItem(type, customRenderer, renderBlocksIr, p_78443_1_, p_78443_2_);
        } else if (p_78443_2_.getItemSpriteNumber() == 0 && item instanceof ItemBlock && RenderBlocks.renderItemIn3d(block.getRenderType())) {
            texturemanager.bindTexture(texturemanager.getResourceLocation(0));

            GL11.glTranslatef(0.0F, 0.2F,-0.2F);
            if (p_78443_2_ != null && block != null && block.getRenderBlockPass() != 0) {
                GL11.glDepthMask(false);
                renderBlocksIr.renderBlockAsItem(block, p_78443_2_.getItemDamage(), 1.0F);
                GL11.glDepthMask(true);
            } else {
                renderBlocksIr.renderBlockAsItem(block, p_78443_2_.getItemDamage(), 1.0F);
            }
        } else {
            IIcon iicon = p_78443_1_.getItemIcon(p_78443_2_, p_78443_3_);

            if (iicon == null) {
                GL11.glPopMatrix();
                return;
            }

            texturemanager.bindTexture(texturemanager.getResourceLocation(p_78443_2_.getItemSpriteNumber()));
            TextureUtil.func_152777_a(false, false, 1.0F);
            Tessellator tessellator = Tessellator.instance;
            float f = iicon.getMinU();
            float f1 = iicon.getMaxU();
            float f2 = iicon.getMinV();
            float f3 = iicon.getMaxV();
            float f4 = 0.0F;
            float f5 = 0.3F;
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
            GL11.glTranslatef(-f4, -f5, 0.0F);
            float f6 = 1.5F;
            GL11.glScalef(f6, f6, f6);
            GL11.glRotatef(50.0F, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(335.0F, 0.0F, 0.0F, 1.0F);
            GL11.glTranslatef(-0.9375F, -0.0625F, 0.0F);
            ItemRenderer.renderItemIn2D(tessellator, f1, f2, f, f3, iicon.getIconWidth(), iicon.getIconHeight(), 0.0625F);

            if (p_78443_2_.hasEffect(p_78443_3_)) {
                GL11.glDepthFunc(GL11.GL_EQUAL);
                GL11.glDisable(GL11.GL_LIGHTING);
                texturemanager.bindTexture(RES_ITEM_GLINT);
                GL11.glEnable(GL11.GL_BLEND);
                OpenGlHelper.glBlendFunc(768, 1, 1, 0);
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
            texturemanager.bindTexture(texturemanager.getResourceLocation(p_78443_2_.getItemSpriteNumber()));
            TextureUtil.func_147945_b();
        }

        GL11.glPopMatrix();
    }
}
