package noppes.npcs.client.renderer;

import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import kamkeel.addon.client.GeckoAddonClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.BossStatus;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.MinecraftForge;
import noppes.npcs.api.ISkinOverlay;
import noppes.npcs.client.ClientCacheHandler;
import noppes.npcs.client.ImageDownloadAlt;
import noppes.npcs.client.model.ModelMPM;
import noppes.npcs.constants.EnumAnimation;
import noppes.npcs.constants.EnumStandingType;
import noppes.npcs.controllers.data.CustomTintData;
import noppes.npcs.controllers.data.SkinOverlay;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.Map;


public class RenderNPCInterface extends RenderLiving{
	public static long LastTextureTick = 0;
	public static RenderManager staticRenderManager;
	public ModelBase originalModel;

	public RenderNPCInterface(ModelBase model, float f){
		super(model, f);
		this.originalModel = model;
	}

	protected void renderName(EntityNPCInterface npc, double d, double d1, double d2) {
		if (!this.func_110813_b(npc))
			return;
		float f2 = npc.getDistanceToEntity(renderManager.livingPlayer);
		float f3 = npc.isSneaking() ? 32F : 64F;

		if (f2 > f3)
			return;
		if (npc.messages != null){
			float height = ((npc.baseHeight / 5f) * npc.display.modelSize);
			float offset = npc.height * (1.2f + (!npc.display.showName()?0:npc.display.title.isEmpty()?0.15f:0.25f));

			npc.messages.renderMessages(d, d1 + offset, d2, 0.666667F * height);
		}
		float scale = (npc.baseHeight / 5f) * npc.display.modelSize;
		int height = 0;
		if (npc.display.showName()) {
			String s = npc.getCommandSenderName();
			if(!npc.display.title.isEmpty()){
				renderLivingLabel(npc, d, d1 + npc.height - 0.06f * scale, d2, 64, "<" + npc.display.title + ">", 0.6f, s, 1f);
				height = 2;
			}
			else{
				renderLivingLabel(npc, d, d1 + npc.height - 0.06f * scale, d2, 64, s, 1f);
				height = 1;
			}
		}
	}

	public void doRenderShadowAndFire(Entity par1Entity, double par2, double par4, double par6, float par8, float par9){
		EntityNPCInterface npc = (EntityNPCInterface) par1Entity;
		if(!npc.isKilled() && !npc.scriptInvisibleToPlayer(Minecraft.getMinecraft().thePlayer))
			super.doRenderShadowAndFire(par1Entity, par2, par4, par6, par8, par9);
	}

	protected void renderLivingLabel(EntityNPCInterface npc, double d, double d1, double d2, int i, Object... obs){
		FontRenderer fontrenderer = getFontRendererFromRenderManager();

		i = npc.getBrightnessForRender(0);
		int j = i % 65536;
		int k = i / 65536;
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)j / 1.0F, (float)k / 1.0F);

		float f1 = (npc.baseHeight / 5f) * npc.display.modelSize;
		float f2 = 0.01666667F * f1;
		GL11.glPushMatrix();
		GL11.glTranslatef((float)d + 0.0F, (float)d1, (float)d2);
		GL11.glNormal3f(0.0F, 1.0F, 0.0F);
		GL11.glRotatef(-renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
		GL11.glRotatef(renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
		Tessellator tessellator = Tessellator.instance;
		float height = f1 / 6.5f;
		for(j = 0; j < obs.length; j += 2){
			float scale = (Float) obs[j + 1];
			height += f1 / 6.5f * scale;
			GL11.glPushMatrix();
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glDepthMask(false);
			GL11.glDisable(GL11.GL_ALPHA_TEST);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			String s = obs[j].toString();
			GL11.glTranslatef(0, height, 0);
			GL11.glScalef(-f2 * scale, -f2 * scale, f2 * scale);
			tessellator.startDrawingQuads();
			int size = fontrenderer.getStringWidth(s) / 2;
			tessellator.setColorRGBA_F(0.0F, 0.0F, 0.0F, 0.25F);
			tessellator.addVertex(-size - 1, -1, 0.0D);
			tessellator.addVertex(-size - 1, 8 , 0.0D);
			tessellator.addVertex(size + 1, 8 , 0.0D);
			tessellator.addVertex(size + 1, -1 , 0.0D);
			tessellator.draw();
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			GL11.glDepthMask(true);
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

			int color = npc.faction.color;
			fontrenderer.drawString(s, -fontrenderer.getStringWidth(s) / 2, 0, color);
			GL11.glPopMatrix();
		}
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glEnable(GL11.GL_LIGHTING /*GL_LIGHTING*/);
		GL11.glDisable(GL11.GL_BLEND /*GL_BLEND*/);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glPopMatrix();
	}

	protected void renderPlayerScale(EntityNPCInterface npc, float f){
		GL11.glScalef((npc.scaleX / 5) * npc.display.modelSize, (npc.scaleY / 5) * npc.display.modelSize, (npc.scaleZ / 5) * npc.display.modelSize);
	}

	protected void renderPlayerSleep(EntityNPCInterface npc, double d, double d1, double d2){
		shadowSize = npc.display.modelSize / 10f;

		float xOffset = 0;
		float yOffset = npc.currentAnimation == EnumAnimation.NONE?npc.ai.bodyOffsetY / 10 - 0.5f:0;
		float zOffset = 0;

		if(npc.isEntityAlive()){
			if(npc.isPlayerSleeping()){
				xOffset = (float) -Math.cos(Math.toRadians(180 - npc.ai.orientation));
				zOffset = (float) -Math.sin(Math.toRadians(npc.ai.orientation));
				yOffset += 0.14f;
			}
			else if(npc.isRiding()){
				yOffset -= 0.5f - ((EntityCustomNpc)npc).modelData.getLegsY() * 0.8f;
			}
		}
		renderLiving(npc, d, d1, d2, xOffset, yOffset, zOffset);
	}
	private void renderLiving(EntityNPCInterface npc, double d, double d1, double d2,float xoffset,float yoffset,float zoffset){
		xoffset = (xoffset/ 5f) * npc.display.modelSize;
		yoffset = (yoffset/ 5f) * npc.display.modelSize;
		zoffset = (zoffset/ 5f) * npc.display.modelSize;
		super.renderLivingAt(npc, d+xoffset, d1+yoffset, d2 + zoffset);
	}

	@Override
	protected void rotateCorpse(EntityLivingBase entity, float f, float f1, float f2){
		EntityNPCInterface npc = (EntityNPCInterface) entity;
		if(npc.isEntityAlive() && npc.isPlayerSleeping())
		{
			GL11.glRotatef(npc.ai.orientation, 0.0F, 1.0F, 0.0F);
			GL11.glRotatef(getDeathMaxRotation(npc), 0.0F, 0.0F, 1.0F);
			GL11.glRotatef(270F, 0.0F, 1.0F, 0.0F);
		}
		else if(npc.isEntityAlive() && npc.currentAnimation == EnumAnimation.CRAWLING){
			GL11.glRotatef(270.0F - f1, 0.0F, 1.0F, 0.0F);
			float scale = ((EntityCustomNpc)npc).display.modelSize / 5f;
			GL11.glTranslated(-scale +((EntityCustomNpc)npc).modelData.getLegsY() * scale, 0.14f, 0);
			GL11.glRotatef(270F, 0.0F, 0.0F, 1.0F);
			GL11.glRotatef(270F, 0.0F, 1.0F, 0.0F);
		}
		else {
			super.rotateCorpse(npc, f, f1, f2);
		}
	}

	@Override
	public void passSpecialRender(EntityLivingBase par1EntityLivingBase, double par2, double par4, double par6){
		renderName((EntityNPCInterface)par1EntityLivingBase, par2, par4, par6);
	}

	@Override
	protected void preRenderCallback(EntityLivingBase entityliving, float f){
		renderPlayerScale((EntityNPCInterface)entityliving, f);
	}

	@Override
	public void doRender(EntityLiving entityliving, double d, double d1, double d2, float f, float f1){
		EntityNPCInterface npc = (EntityNPCInterface) entityliving;

		if(npc.isKilled() && npc.stats.hideKilledBody && npc.deathTime > 20){
			return;
		}
		if((npc.display.showBossBar == 1 || npc.display.showBossBar == 2 && npc.isAttacking()) && !npc.isKilled() && npc.deathTime <= 20 && npc.canSee(Minecraft.getMinecraft().thePlayer))
			BossStatus.setBossStatus(npc, true);

		if(npc.ai.standingType == EnumStandingType.HeadRotation && !npc.isWalking() && !npc.isInteracting()){
			npc.prevRenderYawOffset = npc.renderYawOffset = npc.ai.orientation;
		}

		staticRenderManager = this.renderManager;

		doRenderLiving(npc, d, d1, d2, f, f1);
	}

    public void doRenderLiving(EntityNPCInterface p_76986_1_, double p_76986_2_, double p_76986_4_, double p_76986_6_, float p_76986_8_, float p_76986_9_)
    {
        if (MinecraftForge.EVENT_BUS.post(new RenderLivingEvent.Pre(p_76986_1_, this, p_76986_2_, p_76986_4_, p_76986_6_))) return;
        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_CULL_FACE);
        this.mainModel.onGround = this.renderSwingProgress(p_76986_1_, p_76986_9_);

        if (this.renderPassModel != null)
        {
            this.renderPassModel.onGround = this.mainModel.onGround;
        }

        this.mainModel.isRiding = p_76986_1_.isRiding();

        if (this.renderPassModel != null)
        {
            this.renderPassModel.isRiding = this.mainModel.isRiding;
        }

        this.mainModel.isChild = p_76986_1_.isChild();

        if (this.renderPassModel != null)
        {
            this.renderPassModel.isChild = this.mainModel.isChild;
        }

        try
        {
            float f2 = this.interpolateRotation(p_76986_1_.prevRenderYawOffset, p_76986_1_.renderYawOffset, p_76986_9_);
            float f3 = this.interpolateRotation(p_76986_1_.prevRotationYawHead, p_76986_1_.rotationYawHead, p_76986_9_);
            float f4;

            if (p_76986_1_.isRiding() && p_76986_1_.ridingEntity instanceof EntityLivingBase)
            {
                EntityLivingBase entitylivingbase1 = (EntityLivingBase)p_76986_1_.ridingEntity;
                f2 = this.interpolateRotation(entitylivingbase1.prevRenderYawOffset, entitylivingbase1.renderYawOffset, p_76986_9_);
                f4 = MathHelper.wrapAngleTo180_float(f3 - f2);

                if (f4 < -85.0F)
                {
                    f4 = -85.0F;
                }

                if (f4 >= 85.0F)
                {
                    f4 = 85.0F;
                }

                f2 = f3 - f4;

                if (f4 * f4 > 2500.0F)
                {
                    f2 += f4 * 0.2F;
                }
            }

            float f13 = p_76986_1_.prevRotationPitch + (p_76986_1_.rotationPitch - p_76986_1_.prevRotationPitch) * p_76986_9_;
            this.renderLivingAt(p_76986_1_, p_76986_2_, p_76986_4_, p_76986_6_);
            f4 = this.handleRotationFloat(p_76986_1_, p_76986_9_);
            this.rotateCorpse(p_76986_1_, f4, f2, p_76986_9_);
            float f5 = 0.0625F;
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
            GL11.glScalef(-1.0F, -1.0F, 1.0F);
            this.preRenderCallback(p_76986_1_, p_76986_9_);
            GL11.glTranslatef(0.0F, -24.0F * f5 - 0.0078125F, 0.0F);
            float f6 = p_76986_1_.prevLimbSwingAmount + (p_76986_1_.limbSwingAmount - p_76986_1_.prevLimbSwingAmount) * p_76986_9_;
            float f7 = p_76986_1_.limbSwing - p_76986_1_.limbSwingAmount * (1.0F - p_76986_9_);

            if (p_76986_1_.isChild())
            {
                f7 *= 3.0F;
            }

            if (f6 > 1.0F)
            {
                f6 = 1.0F;
            }

            GL11.glEnable(GL11.GL_ALPHA_TEST);
            this.mainModel.setLivingAnimations(p_76986_1_, f7, f6, p_76986_9_);
            this.renderModel(p_76986_1_, f7, f6, f4, f3 - f2, f13, f5);
            int j;
            float f8;
            float f9;
            float f10;

            for (int i = 0; i < 4; ++i)
            {
                j = this.shouldRenderPass(p_76986_1_, i, p_76986_9_);

                if (j > 0)
                {
                    this.renderPassModel.setLivingAnimations(p_76986_1_, f7, f6, p_76986_9_);
                    this.renderPassModel.render(p_76986_1_, f7, f6, f4, f3 - f2, f13, f5);

                    if ((j & 240) == 16)
                    {
                        this.func_82408_c(p_76986_1_, i, p_76986_9_);
                        this.renderPassModel.render(p_76986_1_, f7, f6, f4, f3 - f2, f13, f5);
                    }

                    if ((j & 15) == 15)
                    {
                        f8 = (float)p_76986_1_.ticksExisted + p_76986_9_;
                        this.bindTexture(new ResourceLocation("textures/misc/enchanted_item_glint.png"));
                        GL11.glEnable(GL11.GL_BLEND);
                        f9 = 0.5F;
                        GL11.glColor4f(f9, f9, f9, 1.0F);
                        GL11.glDepthFunc(GL11.GL_EQUAL);
                        GL11.glDepthMask(false);

                        for (int k = 0; k < 2; ++k)
                        {
                            GL11.glDisable(GL11.GL_LIGHTING);
                            f10 = 0.76F;
                            GL11.glColor4f(0.5F * f10, 0.25F * f10, 0.8F * f10, 1.0F);
                            GL11.glBlendFunc(GL11.GL_SRC_COLOR, GL11.GL_ONE);
                            GL11.glMatrixMode(GL11.GL_TEXTURE);
                            GL11.glLoadIdentity();
                            float f11 = f8 * (0.001F + (float)k * 0.003F) * 20.0F;
                            float f12 = 0.33333334F;
                            GL11.glScalef(f12, f12, f12);
                            GL11.glRotatef(30.0F - (float)k * 60.0F, 0.0F, 0.0F, 1.0F);
                            GL11.glTranslatef(0.0F, f11, 0.0F);
                            GL11.glMatrixMode(GL11.GL_MODELVIEW);
                            this.renderPassModel.render(p_76986_1_, f7, f6, f4, f3 - f2, f13, f5);
                        }

                        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                        GL11.glMatrixMode(GL11.GL_TEXTURE);
                        GL11.glDepthMask(true);
                        GL11.glLoadIdentity();
                        GL11.glMatrixMode(GL11.GL_MODELVIEW);
                        GL11.glEnable(GL11.GL_LIGHTING);
                        GL11.glDisable(GL11.GL_BLEND);
                        GL11.glDepthFunc(GL11.GL_LEQUAL);
                    }

                    GL11.glDisable(GL11.GL_BLEND);
                    GL11.glEnable(GL11.GL_ALPHA_TEST);
                }
            }

            GL11.glDepthMask(true);
            this.renderEquippedItems(p_76986_1_, p_76986_9_);
            float f14 = p_76986_1_.getBrightness(p_76986_9_);
            j = this.getColorMultiplier(p_76986_1_, f14, p_76986_9_);
            OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
            CustomTintData customTintData = p_76986_1_.display.customTintData;
            if ((j >> 24 & 255) > 0 || p_76986_1_.hurtTime > 0 || p_76986_1_.deathTime > 0 || (customTintData.isTintEnabled() && customTintData.isGeneralTintEnabled()))
            {
                GL11.glDisable(GL11.GL_TEXTURE_2D);
                GL11.glDisable(GL11.GL_ALPHA_TEST);
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                GL11.glDepthFunc(GL11.GL_EQUAL);

                if ((p_76986_1_.hurtTime > 0 || p_76986_1_.deathTime > 0) && (!customTintData.isTintEnabled() || customTintData.isHurtTintEnabled()))
                {
                    float r, g, b;
                    if(customTintData.isTintEnabled()) {
                        r = (float) (customTintData.getHurtTint() >> 16 & 255) / 255.0F * f14;
                        g = (float) (customTintData.getHurtTint() >> 8 & 255) / 255.0F * f14;
                        b = (float) (customTintData.getHurtTint() & 255) / 255.0F * f14;
                    }else{
                        r=f14;
                        g=0;
                        b=0;
                    }
                    GL11.glColor4f(r,g,b, 0.4F);
                    this.mainModel.render(p_76986_1_, f7, f6, f4, f3 - f2, f13, f5);

                    for (int l = 0; l < 4; ++l)
                    {
                        if (this.inheritRenderPass(p_76986_1_, l, p_76986_9_) >= 0)
                        {
                            GL11.glColor4f(r,g,b, 0.4F);
                            this.renderPassModel.render(p_76986_1_, f7, f6, f4, f3 - f2, f13, f5);
                        }
                    }
                }

                if ((j >> 24 & 255) > 0)
                {
                    f8 = (float)(j >> 16 & 255) / 255.0F;
                    f9 = (float)(j >> 8 & 255) / 255.0F;
                    float f15 = (float)(j & 255) / 255.0F;
                    f10 = (float)(j >> 24 & 255) / 255.0F;
                    GL11.glColor4f(f8, f9, f15, f10);
                    this.mainModel.render(p_76986_1_, f7, f6, f4, f3 - f2, f13, f5);

                    for (int i1 = 0; i1 < 4; ++i1)
                    {
                        if (this.inheritRenderPass(p_76986_1_, i1, p_76986_9_) >= 0)
                        {
                            GL11.glColor4f(f8, f9, f15, f10);
                            this.renderPassModel.render(p_76986_1_, f7, f6, f4, f3 - f2, f13, f5);
                        }
                    }
                }

                GL11.glDepthFunc(GL11.GL_LEQUAL);
                GL11.glDisable(GL11.GL_BLEND);
                GL11.glEnable(GL11.GL_ALPHA_TEST);
                GL11.glEnable(GL11.GL_TEXTURE_2D);
            }

            GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        }
        catch (Exception exception)
        {

        }

        OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glPopMatrix();
        this.passSpecialRender(p_76986_1_, p_76986_2_, p_76986_4_, p_76986_6_);
        MinecraftForge.EVENT_BUS.post(new RenderLivingEvent.Post(p_76986_1_, this, p_76986_2_, p_76986_4_, p_76986_6_));
        this.func_110827_b(p_76986_1_, p_76986_2_, p_76986_4_, p_76986_6_, p_76986_8_, p_76986_9_);
    }

    private float interpolateRotation(float p_77034_1_, float p_77034_2_, float p_77034_3_)
    {
        float f3;

        for (f3 = p_77034_2_ - p_77034_1_; f3 < -180.0F; f3 += 360.0F)
        {
            ;
        }

        while (f3 >= 180.0F)
        {
            f3 -= 360.0F;
        }

        return p_77034_1_ + p_77034_3_ * f3;
    }

    protected int getColorMultiplier(EntityLivingBase p_77030_1_, float p_77030_2_, float p_77030_3_)
    {
        EntityNPCInterface npc = (EntityNPCInterface) p_77030_1_;
        CustomTintData tintData = npc.display.customTintData;
        int alpha = (int) (0xff*((double)tintData.getGeneralAlpha()/100d)) << 24;
        return (tintData.isTintEnabled() && tintData.isGeneralTintEnabled())?tintData.getGeneralTint()+alpha:0;
    }

	protected void renderModel(EntityLivingBase entityliving, float par2, float par3, float par4, float par5, float par6, float par7) {
		EntityNPCInterface npc = (EntityNPCInterface) entityliving;
		if(GeckoAddonClient.Instance.isGeckoModel(mainModel)){
			GeckoAddonClient.Instance.geckoRenderModel((ModelMPM) mainModel, npc, npc.rotationYaw, Minecraft.getMinecraft().timer.renderPartialTicks);
		} else if (this.getEntityTexture(entityliving) != null) {
			super.renderModel(entityliving, par2, par3, par4, par5, par6, par7);
		}

		if (!npc.display.skinOverlayData.overlayList.isEmpty()) {
			for (ISkinOverlay overlayData : npc.display.skinOverlayData.overlayList.values()) {
				try {
					if (((SkinOverlay)overlayData).texture.isEmpty())
						continue;

					ImageData imageData = ClientCacheHandler.getImageData(((SkinOverlay)overlayData).texture);
					if (!imageData.imageLoaded())
						continue;

					try {
						imageData.bindTexture();
					} catch (Exception e) { continue; }

					// Overlay & Glow
					GL11.glEnable(GL11.GL_BLEND);
					if (overlayData.getBlend()) {
						GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE);
					} else {
						GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
					}
					GL11.glAlphaFunc(GL11.GL_GREATER, 0.003921569F);

					if (overlayData.getGlow()) {
						GL11.glDisable(GL11.GL_LIGHTING);
						Minecraft.getMinecraft().entityRenderer.disableLightmap((double) 0);
					}

					GL11.glColor4f(1.0F, 1.0F, 1.0F, overlayData.getAlpha());

					GL11.glDepthMask(!npc.isInvisible());

					GL11.glPushMatrix();
					GL11.glMatrixMode(GL11.GL_TEXTURE);
					GL11.glLoadIdentity();
					GL11.glTranslatef(npc.display.overlayRenderTicks * 0.001F * overlayData.getSpeedX(), npc.display.overlayRenderTicks * 0.001F * overlayData.getSpeedY(), 0.0F);
					GL11.glScalef(overlayData.getTextureScaleX(), overlayData.getTextureScaleY(), 1.0F);

					GL11.glMatrixMode(GL11.GL_MODELVIEW);
					float scale = 1.005f * overlayData.getSize();
					GL11.glTranslatef(overlayData.getOffsetX(), overlayData.getOffsetY(), overlayData.getOffsetZ());
					GL11.glScalef(scale, scale, scale);
					if(mainModel instanceof ModelMPM){
						((ModelMPM)mainModel).isArmor = true;
						mainModel.render(entityliving, par2, par3, par4, par5, par6, par7);
						((ModelMPM)mainModel).isArmor = false;
					}
					else
						mainModel.render(entityliving, par2, par3, par4, par5, par6, par7);
					GL11.glPopMatrix();

					GL11.glMatrixMode(GL11.GL_TEXTURE);
					GL11.glLoadIdentity();
					GL11.glMatrixMode(GL11.GL_MODELVIEW);

					GL11.glEnable(GL11.GL_LIGHTING);
					GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
					GL11.glDepthFunc(GL11.GL_LEQUAL);
					GL11.glDisable(GL11.GL_BLEND);
					GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
					Minecraft.getMinecraft().entityRenderer.enableLightmap((double) 0);
				} catch (Exception ignored) {
				}
			}
			npc.display.overlayRenderTicks++;
		}
	}

	@Override
	protected float handleRotationFloat(EntityLivingBase par1EntityLiving, float par2){
		EntityNPCInterface npc = (EntityNPCInterface) par1EntityLiving;
		if(npc.isKilled() || npc.display.disableLivingAnimation)
			return 0;
		return super.handleRotationFloat(par1EntityLiving, par2);
	}

	@Override
	protected void renderLivingAt(EntityLivingBase entityliving, double d, double d1, double d2){
		renderPlayerSleep((EntityNPCInterface)entityliving, d, d1, d2);
	}

	@Override
	public ResourceLocation getEntityTexture(Entity entity) {

		EntityNPCInterface npc = (EntityNPCInterface) entity;
		if (npc.textureLocation == null) {
			if (npc.display.skinType == 0) {
				if (npc instanceof EntityCustomNpc && ((EntityCustomNpc) npc).modelData.entityClass == null) {
					if (!(npc.display.texture).isEmpty()) {
						try {
							npc.textureLocation = adjustLocalTexture(npc, new ResourceLocation(npc.display.texture));
						} catch (IOException ignored) {}
					}
				} else {
					npc.textureLocation = new ResourceLocation(npc.display.texture);
				}
			} else if(npc.display.skinType == 1 && npc.display.playerProfile != null) {
				Minecraft minecraft = Minecraft.getMinecraft();
				Map map = minecraft.func_152342_ad().func_152788_a(npc.display.playerProfile);
				if (map.containsKey(Type.SKIN)){
					npc.textureLocation = minecraft.func_152342_ad().func_152792_a((MinecraftProfileTexture)map.get(Type.SKIN), Type.SKIN);
				}
				LastTextureTick = 0;
			} else if (npc.display.skinType == 2 || npc.display.skinType == 3) {
                ResourceLocation location = new ResourceLocation("skins/" + (npc.display.modelType + npc.display.url).hashCode());
                // If URL Empty Steve
                if(npc.display.url.isEmpty()){ return AbstractClientPlayer.locationStevePng; }
                // If URL Cached then grab it
                else if(ClientCacheHandler.isCachedNPC(location)){
                    try {
                        npc.textureLocation = ClientCacheHandler.getNPCTexture(npc.display.url, npc.display.modelType > 0, location).getLocation();
                    } catch(Exception ignored){}
                }
                // For New URL Requests do not spam it
                else if(LastTextureTick < 5) { //fixes request flood somewhat
                    return AbstractClientPlayer.locationStevePng;
                }
                else {
                    try {
                        npc.textureLocation = ClientCacheHandler.getNPCTexture(npc.display.url, npc.display.modelType > 0, location).getLocation();
                        LastTextureTick = 0;
                    } catch(Exception ignored){}
                }
			} else {
				return AbstractClientPlayer.locationStevePng;
			}
		}
		return npc.textureLocation;
	}

	private ResourceLocation adjustLocalTexture(EntityNPCInterface npc, ResourceLocation location) throws IOException {
		InputStream inputstream = null;
		try {
			TextureManager texturemanager = Minecraft.getMinecraft().getTextureManager();
			texturemanager.deleteTexture(location);

			IResource iresource = Minecraft.getMinecraft().getResourceManager().getResource(location);
			inputstream = iresource.getInputStream();

			BufferedImage bufferedimage = ImageIO.read(inputstream);

			int totalWidth = bufferedimage.getWidth();
			int totalHeight = bufferedimage.getHeight();
			if (totalWidth == totalHeight && npc.display.modelType == 0) {
				bufferedimage = bufferedimage.getSubimage(0, 0, totalWidth, totalWidth / 2);
			}

			ImageDownloadAlt object = new ImageDownloadAlt(null, npc.display.texture, SkinManager.field_152793_a, new ImageBufferDownloadAlt(true));
			object.setBufferedImage(bufferedimage);

			try {
				MessageDigest digest = MessageDigest.getInstance("MD5");
				byte[] hash = digest.digest(npc.display.texture.getBytes("UTF-8"));
				StringBuilder sb = new StringBuilder(2*hash.length);
				for (byte b : hash) {
					sb.append(String.format("%02x", b&0xff));
				}
				if (npc.display.modelType == 0) {
					location = new ResourceLocation("skin/" + sb.toString());
				} else {
					location = new ResourceLocation("skin64/" + sb.toString());
				}
			} catch(Exception ignored){}
			texturemanager.loadTexture(location, object);
			return location;
		} finally {
			if (inputstream != null) {
				inputstream.close();
			}
		}
	}

}
