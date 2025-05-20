package noppes.npcs;

import kamkeel.npcs.addon.DBCAddon;
import kamkeel.npcs.addon.client.DBCClient;
import kamkeel.npcs.addon.client.DBCClientAnimations;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.entity.Entity;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;

import static net.minecraftforge.client.IItemRenderer.ItemRenderType.EQUIPPED_FIRST_PERSON;

public class AnimationMixinFunctions {

    public static boolean applyValues(ModelRenderer modelRenderer) {
        if (ClientEventHandler.renderingPlayer == null && ClientEventHandler.renderingNpc == null) {
            return false;
        }

        if (DBCAddon.IsAvailable()) {
            if (ClientEventHandler.renderingPlayer != null) {
                return DBCClient.Instance.applyRenderModel(modelRenderer);
            } else if (DBCClient.Instance.applyRenderModel(modelRenderer)) {
                return false;
            }
        }

        if (ClientEventHandler.renderingPlayer != null) {
            ClientEventHandler.playerModel = (modelRenderer).baseModel;
            if (ClientCacheHandler.playerAnimations.containsKey(ClientEventHandler.renderingPlayer.getUniqueID())) {
                AnimationData animData = ClientCacheHandler.playerAnimations.get(ClientEventHandler.renderingPlayer.getUniqueID());
                EnumAnimationPart mainPartType = getPlayerPartType(modelRenderer);
                EnumAnimationPart partType = mainPartType != null ? mainPartType : pivotEqualPart(modelRenderer);
                if (partType != null && animData != null && animData.animation != null && animData.isActive()) {
                    if (!ClientEventHandler.originalValues.containsKey(modelRenderer)) {
                        FramePart part = new FramePart();
                        part.pivot = new float[]{modelRenderer.rotationPointX, modelRenderer.rotationPointY, modelRenderer.rotationPointZ};
                        part.rotation = new float[]{modelRenderer.rotateAngleX, modelRenderer.rotateAngleY, modelRenderer.rotateAngleZ};
                        ClientEventHandler.originalValues.put(modelRenderer, part);
                    }
                    FramePart originalPart = ClientEventHandler.originalValues.get(modelRenderer);
                    Frame frame = (Frame) animData.animation.currentFrame();
                    if (frame != null && frame.frameParts.containsKey(partType)) {
                        FramePart part = frame.frameParts.get(partType);
                        if (partType == mainPartType) {
                            part.interpolateAngles();
                            part.interpolateOffset();
                            modelRenderer.rotationPointX = originalPart.pivot[0] + part.prevPivots[0];
                            modelRenderer.rotationPointY = originalPart.pivot[1] + part.prevPivots[1];
                            modelRenderer.rotationPointZ = originalPart.pivot[2] + part.prevPivots[2];
                            modelRenderer.rotateAngleX = part.prevRotations[0];
                            modelRenderer.rotateAngleY = part.prevRotations[1];
                            modelRenderer.rotateAngleZ = part.prevRotations[2];
                        } else {
                            modelRenderer.rotateAngleZ += part.prevRotations[2];
                            return true;
                        }
                    }
                }
            }
        } else if (ClientEventHandler.renderingNpc.display.animationData.isActive()) {
            AnimationData animData = ClientEventHandler.renderingNpc.display.animationData;
            EnumAnimationPart partType = getPartType(modelRenderer);
            if (partType != null && animData != null) {
                Frame frame = (Frame) animData.animation.currentFrame();
                if (frame.frameParts.containsKey(partType)) {
                    FramePart part = frame.frameParts.get(partType);
                    part.interpolateOffset();
                    part.interpolateAngles();
                    modelRenderer.rotationPointX += part.prevPivots[0];
                    modelRenderer.rotationPointY += part.prevPivots[1];
                    modelRenderer.rotationPointZ += part.prevPivots[2];
                    modelRenderer.rotateAngleX = part.prevRotations[0];
                    modelRenderer.rotateAngleY = part.prevRotations[1];
                    modelRenderer.rotateAngleZ = part.prevRotations[2];
                    return true;
                }
            }
        }
        return false;
    }

    private static EnumAnimationPart getPlayerPartType(ModelRenderer renderer) {
        if (renderer.baseModel instanceof ModelBiped) {
            if (renderer == ((ModelBiped) renderer.baseModel).bipedHead
                || renderer == ((ModelBiped) renderer.baseModel).bipedHeadwear) {
                return EnumAnimationPart.HEAD;
            }
            if (renderer == ((ModelBiped) renderer.baseModel).bipedBody) {
                return EnumAnimationPart.BODY;
            }
            if (renderer == ((ModelBiped) renderer.baseModel).bipedRightArm) {
                return EnumAnimationPart.RIGHT_ARM;
            }
            if (renderer == ((ModelBiped) renderer.baseModel).bipedLeftArm) {
                return EnumAnimationPart.LEFT_ARM;
            }
            if (renderer == ((ModelBiped) renderer.baseModel).bipedRightLeg) {
                return EnumAnimationPart.RIGHT_LEG;
            }
            if (renderer == ((ModelBiped) renderer.baseModel).bipedLeftLeg) {
                return EnumAnimationPart.LEFT_LEG;
            }
        }
        return getPartType(renderer);
    }

    private static EnumAnimationPart pivotEqualPart(ModelRenderer renderer) {
        if (renderer.baseModel instanceof ModelBiped) {
            ModelRenderer head = ((ModelBiped) renderer.baseModel).bipedHead;
            ModelRenderer body = ((ModelBiped) renderer.baseModel).bipedBody;
            ModelRenderer larm = ((ModelBiped) renderer.baseModel).bipedLeftArm;
            ModelRenderer rarm = ((ModelBiped) renderer.baseModel).bipedRightArm;
            ModelRenderer lleg = ((ModelBiped) renderer.baseModel).bipedLeftLeg;
            ModelRenderer rleg = ((ModelBiped) renderer.baseModel).bipedRightLeg;

            if (pivotsEqual(renderer, head)) {
                return EnumAnimationPart.HEAD;
            }
            if (pivotsEqual(renderer, body)) {
                return EnumAnimationPart.BODY;
            }
            if (pivotsEqual(renderer, rarm)) {
                return EnumAnimationPart.RIGHT_ARM;
            }
            if (pivotsEqual(renderer, larm)) {
                return EnumAnimationPart.LEFT_ARM;
            }
            if (pivotsEqual(renderer, rleg)) {
                return EnumAnimationPart.RIGHT_LEG;
            }
            if (pivotsEqual(renderer, lleg)) {
                return EnumAnimationPart.LEFT_LEG;
            }
        }

        return null;
    }

    private static boolean pivotsEqual(ModelRenderer m1, ModelRenderer m2) {
        return m1.rotationPointX == m2.rotationPointX && m1.rotationPointY == m2.rotationPointY && m1.rotationPointZ == m2.rotationPointZ;
    }

    private static EnumAnimationPart getPartType(ModelRenderer renderer) {
        Class<?> RenderClass = renderer.baseModel.getClass();
        Object model = renderer.baseModel;

        Set<Map.Entry<EnumAnimationPart, String[]>> entrySet = ClientEventHandler.partNames.entrySet();
        while (RenderClass != Object.class) {
            Field[] declared;
            if (ClientEventHandler.declaredFieldCache.containsKey(RenderClass)) {
                declared = ClientEventHandler.declaredFieldCache.get(RenderClass);
            } else {
                declared = RenderClass.getDeclaredFields();
                ClientEventHandler.declaredFieldCache.put(RenderClass, declared);
            }
            for (Field f : declared) {
                f.setAccessible(true);
                for (Map.Entry<EnumAnimationPart, String[]> entry : entrySet) {
                    String[] names = entry.getValue();
                    for (String partName : names) {
                        try {
                            if (partName.equals(f.getName()) && renderer == f.get(model)) {
                                return entry.getKey();
                            }
                        } catch (IllegalAccessException ignored) {
                        }
                    }
                }
            }
            RenderClass = RenderClass.getSuperclass();
        }

        return null;
    }

    public static void playerFullModel_head(Entity p_78088_1_, CallbackInfo callbackInfo) {
        if (!DBCAddon.IsAvailable() && ClientCacheHandler.playerAnimations.containsKey(p_78088_1_.getUniqueID())) {
            AnimationData animData = ClientCacheHandler.playerAnimations.get(p_78088_1_.getUniqueID());
            if (animData != null && animData.isActive()) {
                Frame frame = (Frame) animData.animation.currentFrame();
                if (frame.frameParts.containsKey(EnumAnimationPart.FULL_MODEL)) {
                    FramePart part = frame.frameParts.get(EnumAnimationPart.FULL_MODEL);
                    part.interpolateOffset();
                    part.interpolateAngles();
                    float pi = 180 / (float) Math.PI;
                    GL11.glTranslatef(part.prevPivots[0], -part.prevPivots[1], part.prevPivots[2]);
                    GL11.glRotatef(part.prevRotations[0] * pi, 1, 0, 0);
                    GL11.glRotatef(part.prevRotations[1] * pi, 0, 1, 0);
                    GL11.glRotatef(part.prevRotations[2] * pi, 0, 0, 1);
                }
            }
        }
    }

    public static boolean mixin_renderFirstPersonAnimation(float partialRenderTick, EntityPlayer player, ModelBiped model, RenderBlocks renderBlocksIr, ResourceLocation resItemGlint) {
        if (DBCAddon.IsAvailable()) {
            return DBCClient.Instance.firstPersonAnimation(partialRenderTick, player, model, renderBlocksIr, resItemGlint);
        }

        AnimationData animationData = ClientCacheHandler.playerAnimations.get(player.getUniqueID());
        if (animationData != null && animationData.isActive()) {
            Frame frame = (Frame) animationData.animation.currentFrame();
            if (frame.frameParts.containsKey(EnumAnimationPart.FULL_MODEL)) {
                FramePart part = frame.frameParts.get(EnumAnimationPart.FULL_MODEL);
                part.interpolateOffset();
                part.interpolateAngles();
            }
        }

        ModelRenderer[] parts = new ModelRenderer[]{model.bipedRightArm, model.bipedLeftArm, model.bipedRightLeg, model.bipedLeftLeg};
        EnumAnimationPart[] enumParts = new EnumAnimationPart[]{EnumAnimationPart.RIGHT_ARM, EnumAnimationPart.LEFT_ARM, EnumAnimationPart.RIGHT_LEG, EnumAnimationPart.LEFT_LEG};
        Frame frame;

        if (animationData != null && animationData.isActive()) {
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

        if (animationData != null && animationData.isActive()) {
            if (frame.frameParts.containsKey(EnumAnimationPart.FULL_MODEL)) {
                FramePart part = frame.frameParts.get(EnumAnimationPart.FULL_MODEL);
                float pi = 180 / (float) Math.PI;
                GL11.glRotatef(-part.prevRotations[1] * pi, 0, 1, 0);
            }
        }

        EntityClientPlayerMP entityclientplayermp = Minecraft.getMinecraft().thePlayer;
        RenderHelper.enableStandardItemLighting();
        int i = Minecraft.getMinecraft().theWorld.getLightBrightnessForSkyBlocks(MathHelper.floor_double(entityclientplayermp.posX), MathHelper.floor_double(entityclientplayermp.posY), MathHelper.floor_double(entityclientplayermp.posZ), 0);
        int j = i % 65536;
        int k = i / 65536;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) j, (float) k);

        float f3 = entityclientplayermp.prevRenderArmPitch + (entityclientplayermp.renderArmPitch - entityclientplayermp.prevRenderArmPitch) * partialRenderTick;
        float f4 = entityclientplayermp.prevRenderArmYaw + (entityclientplayermp.renderArmYaw - entityclientplayermp.prevRenderArmYaw) * partialRenderTick;
        GL11.glRotatef((entityclientplayermp.rotationPitch - f3) * 0.1F, 1.0F, 0.0F, 0.0F);
        GL11.glRotatef((entityclientplayermp.rotationYaw - f4) * 0.1F, 0.0F, 1.0F, 0.0F);

        try {
            DBCClientAnimations.doDBCRender(player);
        } catch (Exception e) {
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            Minecraft.getMinecraft().getTextureManager().bindTexture(entityclientplayermp.getLocationSkin());
            renderLimbs();
        }

        ItemRenderer itemRenderer = Minecraft.getMinecraft().entityRenderer.itemRenderer;
        ItemStack itemstack = itemRenderer.itemToRender;
        if (frame.frameParts.containsKey(EnumAnimationPart.RIGHT_ARM) && itemstack != null) {
            float f11, f12;
            GL11.glPushMatrix();

            if (player.fishEntity != null) {
                itemstack = new ItemStack(Items.stick);
            }

            float p_78785_1_ = 0.0625F;
            GL11.glTranslatef(0.1F, 0.4F + -0.75F * 0.8F, -0.3F);
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

            float f6 = 1 / 1.5F;
            GL11.glScalef(f6, f6, f6);

            if (itemstack.getItem().requiresMultipleRenderPasses()) {
                for (k = 0; k < itemstack.getItem().getRenderPasses(itemstack.getItemDamage()); ++k) {
                    i = itemstack.getItem().getColorFromItemStack(itemstack, k);
                    f12 = (float) (i >> 16 & 255) / 255.0F;
                    f3 = (float) (i >> 8 & 255) / 255.0F;
                    f4 = (float) (i & 255) / 255.0F;
                    GL11.glColor4f(f12, f3, f4, 1.0F);
                    mixin_renderItem(player, itemstack, k, EQUIPPED_FIRST_PERSON, renderBlocksIr, resItemGlint);
                }
            } else {
                k = itemstack.getItem().getColorFromItemStack(itemstack, 0);
                f11 = (float) (k >> 16 & 255) / 255.0F;
                f12 = (float) (k >> 8 & 255) / 255.0F;
                f3 = (float) (k & 255) / 255.0F;
                GL11.glColor4f(f11, f12, f3, 1.0F);
                mixin_renderItem(player, itemstack, 0, EQUIPPED_FIRST_PERSON, renderBlocksIr, resItemGlint);
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

    private static void mixin_renderItem(EntityLivingBase p_78443_1_, ItemStack p_78443_2_, int p_78443_3_, IItemRenderer.ItemRenderType type, RenderBlocks renderBlocksIr, ResourceLocation resItemGlint) {
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

            GL11.glTranslatef(0.0F, 0.2F, -0.2F);
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
                texturemanager.bindTexture(resItemGlint);
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

    public static void renderLimbs() {
        AnimationData animationData = ClientCacheHandler.playerAnimations.get(Minecraft.getMinecraft().thePlayer.getUniqueID());
        if (animationData != null && animationData.isActive() && animationData.getAnimation() != null && animationData.getAnimation().currentFrame() != null) {
            Animation animation = (Animation) animationData.getAnimation();
            Frame frame = (Frame) animation.currentFrame();

            ModelBiped model = ClientEventHandler.firstPersonModel;

            if (frame.frameParts.containsKey(EnumAnimationPart.RIGHT_ARM)) {
                GL11.glPushMatrix();
                GL11.glTranslatef(0.1F, 0.4F + -0.75F * 0.8F, -0.3F);
                GL11.glEnable(GL12.GL_RESCALE_NORMAL);
                GL11.glRotatef(180.0F, 1.0F, 0.0F, 0.0F);
                GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
                GL11.glRotatef(0.0F, 0.0F, 0.0F, 1.0F);
                model.bipedRightArm.render(0.0625F);
                GL11.glPopMatrix();
            }


            if (frame.frameParts.containsKey(EnumAnimationPart.LEFT_ARM)) {
                GL11.glPushMatrix();
                GL11.glTranslatef(-0.1F, 0.4F + -0.75F * 0.8F, -0.3F);
                GL11.glEnable(GL12.GL_RESCALE_NORMAL);
                GL11.glRotatef(180.0F, 1.0F, 0.0F, 0.0F);
                GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
                GL11.glRotatef(0.0F, 0.0F, 0.0F, 1.0F);
                model.bipedLeftArm.render(0.0625F);
                GL11.glPopMatrix();
            }


            if (frame.frameParts.containsKey(EnumAnimationPart.RIGHT_LEG)) {
                GL11.glPushMatrix();
                GL11.glTranslatef(0.025F, 0.6F + -0.75F * 0.8F, -0.4F);
                GL11.glEnable(GL12.GL_RESCALE_NORMAL);
                GL11.glRotatef(180.0F, 1.0F, 0.0F, 0.0F);
                GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
                GL11.glRotatef(0.0F, 0.0F, 0.0F, 1.0F);
                model.bipedRightLeg.render(0.0625F);
                GL11.glPopMatrix();
            }

            if (frame.frameParts.containsKey(EnumAnimationPart.LEFT_LEG)) {
                GL11.glPushMatrix();
                GL11.glTranslatef(-0.025F, 0.6F + -0.75F * 0.8F, -0.4F);
                GL11.glEnable(GL12.GL_RESCALE_NORMAL);
                GL11.glRotatef(180.0F, 1.0F, 0.0F, 0.0F);
                GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
                GL11.glRotatef(0.0F, 0.0F, 0.0F, 1.0F);
                model.bipedLeftLeg.render(0.0625F);
                GL11.glPopMatrix();
            }
        }
    }
}
