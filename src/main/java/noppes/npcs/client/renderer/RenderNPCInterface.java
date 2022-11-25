package noppes.npcs.client.renderer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.BossStatus;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.client.ImageDownloadAlt;
import noppes.npcs.client.model.ModelMPM;
import noppes.npcs.constants.EnumAnimation;
import noppes.npcs.constants.EnumStandingType;
import noppes.npcs.controllers.data.SkinOverlay;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;

import noppes.npcs.api.ISkinOverlay;
import org.lwjgl.opengl.GL11;

import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;

import javax.imageio.ImageIO;


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
	protected void passSpecialRender(EntityLivingBase par1EntityLivingBase, double par2, double par4, double par6){
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
		super.doRender(entityliving, d, d1, d2, f, f1);
	}

	protected void renderModel(EntityLivingBase entityliving, float par2, float par3, float par4, float par5, float par6, float par7) {
		EntityNPCInterface npc = (EntityNPCInterface) entityliving;
		if (this.getEntityTexture(entityliving) != null) {
			super.renderModel(entityliving, par2, par3, par4, par5, par6, par7);
		}

		if (!npc.display.skinOverlayData.overlayList.isEmpty()) {
			for (ISkinOverlay overlayData : npc.display.skinOverlayData.overlayList.values()) {
				try {
					if (((SkinOverlay)overlayData).getLocation() == null) {
						((SkinOverlay)overlayData).setLocation(new ResourceLocation(overlayData.getTexture()));
					} else {
						String str = ((SkinOverlay)npc.display.skinOverlayData.overlayList.get(0)).getLocation().getResourceDomain()+":"+((SkinOverlay)npc.display.skinOverlayData.overlayList.get(0)).getLocation().getResourcePath();
						if (!str.equals(overlayData.getTexture())) {
							((SkinOverlay)overlayData).setLocation(new ResourceLocation(overlayData.getTexture()));
						}
					}

					if (overlayData.getTexture().isEmpty() || ((SkinOverlay)overlayData).getLocation() == null
							|| ((SkinOverlay)overlayData).getLocation().getResourcePath().isEmpty())
						continue;

					try {
						this.bindTexture(((SkinOverlay)overlayData).getLocation());
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
					if (!(npc.display.texture).equals("")) {
						try {
							npc.textureLocation = adjustLocalTexture(npc, new ResourceLocation(npc.display.texture));
						} catch (IOException ignored) {
						}
					}
				} else {
					npc.textureLocation = new ResourceLocation(npc.display.texture);
				}
			} else if(LastTextureTick < 5) { //fixes request flood somewhat
				return AbstractClientPlayer.locationStevePng;
			} else if(npc.display.skinType == 1 && npc.display.playerProfile != null) {
				Minecraft minecraft = Minecraft.getMinecraft();
				Map map = minecraft.func_152342_ad().func_152788_a(npc.display.playerProfile);
				if (map.containsKey(Type.SKIN)){
					npc.textureLocation = minecraft.func_152342_ad().func_152792_a((MinecraftProfileTexture)map.get(Type.SKIN), Type.SKIN);
				}
				LastTextureTick = 0;
			} else if (npc.display.skinType == 2 || npc.display.skinType == 3) {
				try {
					MessageDigest digest = MessageDigest.getInstance("MD5");
					byte[] hash = digest.digest(npc.display.url.getBytes("UTF-8"));
					StringBuilder sb = new StringBuilder(2*hash.length);
					for (byte b : hash) {
						sb.append(String.format("%02x", b&0xff));
					}
					if (npc.display.skinType == 2) {
						npc.textureLocation = new ResourceLocation("skins/" + sb.toString());
						loadSkin(null, npc.textureLocation, npc.display.url, false);
					} else {
						npc.textureLocation = new ResourceLocation("skins64/" + sb.toString());
						loadSkin(null, npc.textureLocation, npc.display.url, true);
					}
					LastTextureTick = 0;
				} catch(Exception ignored){}
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

			if (totalHeight > 32 && npc.display.modelType == 0) {
				bufferedimage = bufferedimage.getSubimage(0, 0, totalWidth, 32);
			}

			ImageDownloadAlt object = new ImageDownloadAlt(null, npc.display.texture, SkinManager.field_152793_a, new ImageBufferDownloadAlt(false));
			object.setBufferedImage(bufferedimage);

			try {
				MessageDigest digest = MessageDigest.getInstance("MD5");
				byte[] hash = digest.digest(npc.display.texture.getBytes("UTF-8"));
				StringBuilder sb = new StringBuilder(2*hash.length);
				for (byte b : hash) {
					sb.append(String.format("%02x", b&0xff));
				}
				if (totalHeight > 32 && npc.display.modelType == 0) {
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

	// 64x64 Skin is True
	private void loadSkin(File file, ResourceLocation resource, String par1Str, boolean version){
		TextureManager texturemanager = Minecraft.getMinecraft().getTextureManager();
		ITextureObject object = new ImageDownloadAlt(file, par1Str, SkinManager.field_152793_a, new ImageBufferDownloadAlt(version));
		texturemanager.loadTexture(resource, object);
	}

}
