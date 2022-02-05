package noppes.npcs.client.renderer;

import java.io.File;
import java.security.MessageDigest;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureManager;
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
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;

import org.lwjgl.opengl.GL11;

import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;


public class RenderNPCInterface extends RenderLiving{	
	public static long LastTextureTick = 0;
    public RenderNPCInterface(ModelBase model, float f){
        super(model, f);
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
    	if(!npc.isKilled())
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
//            if(npc.deathTime > 0)
//            {      
//                float x = (float) -Math.cos(Math.toRadians(180 - f1));
//                float y = (float) -Math.sin(Math.toRadians(f1));
//                x = (x/ 5f) * npc.getModelSize();
//                y = (y/ 5f) * npc.getModelSize();
//                GL11.glTranslatef(x, 0, y);
//                
//                
//                float f3 = ((((float)npc.deathTime+ f2) - 1.0F) / 20F) * 1.6F;
//                f3 = MathHelper.sqrt_float(f3);
//                if(f3 > 1.0F)
//                {
//                    f3 = 1.0F;
//                }
//                GL11.glRotatef(f1, 0.0F, 1.0F, 0.0F);  
//                GL11.glRotatef(f3 * getDeathMaxRotation(npc), 0.0F, 0.0F, 1.0F);
//                GL11.glRotatef(180F - f1, 0.0F, 1.0F, 0.0F);
//            }
//	        else
//	        {
//	            GL11.glRotatef(180F - f1, 0.0F, 1.0F, 0.0F);
//	        }
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
    	super.doRender(entityliving, d, d1, d2, f, f1);
	}
    
    protected void renderModel(EntityLivingBase entityliving, float par2, float par3, float par4, float par5, float par6, float par7){
    	super.renderModel(entityliving, par2, par3, par4, par5, par6, par7);
    	EntityNPCInterface npc = (EntityNPCInterface) entityliving;
    	 if (!npc.display.glowTexture.isEmpty())
         {
            GL11.glDepthFunc(GL11.GL_LEQUAL);
        	if(npc.textureGlowLocation == null){
        		npc.textureGlowLocation = new ResourceLocation(npc.display.glowTexture);
        	}
        	bindTexture((ResourceLocation) npc.textureGlowLocation);
        	float f1 = 1.0F;
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE);
            GL11.glDisable(GL11.GL_LIGHTING);
            if (npc.isInvisible())
            {
                GL11.glDepthMask(false);
            }
            else
            {
                GL11.glDepthMask(true);
            }
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glPushMatrix();
            GL11.glScalef(1.001f, 1.001f, 1.001f);
            if(mainModel instanceof ModelMPM){
	            ((ModelMPM)mainModel).isArmor = true;
	            mainModel.render(entityliving, par2, par3, par4, par5, par6, par7);
	            ((ModelMPM)mainModel).isArmor = false;
            }
            else
	            mainModel.render(entityliving, par2, par3, par4, par5, par6, par7);
            GL11.glPopMatrix();
            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, f1);
 	        

            GL11.glDepthFunc(GL11.GL_LEQUAL);
            GL11.glDisable(GL11.GL_BLEND);
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
		if(npc.textureLocation == null){
			if(npc.display.skinType == 0)
				npc.textureLocation = new ResourceLocation(npc.display.texture);
			else if(LastTextureTick < 5){ //fixes request flood somewhat
				return AbstractClientPlayer.locationStevePng;
			}
			else if(npc.display.skinType == 1 && npc.display.playerProfile != null){
                Minecraft minecraft = Minecraft.getMinecraft();
                Map map = minecraft.func_152342_ad().func_152788_a(npc.display.playerProfile);
                if (map.containsKey(Type.SKIN)){
                	npc.textureLocation = minecraft.func_152342_ad().func_152792_a((MinecraftProfileTexture)map.get(Type.SKIN), Type.SKIN);
                }
				LastTextureTick = 0;
			}
			else if(npc.display.skinType == 2){
				try{
					MessageDigest digest = MessageDigest.getInstance("MD5");
					byte[] hash = digest.digest(npc.display.url.getBytes("UTF-8"));
					StringBuilder sb = new StringBuilder(2*hash.length);
					for(byte b : hash){
						sb.append(String.format("%02x", b&0xff));
					}
					npc.textureLocation = new ResourceLocation("skins/" + sb.toString());
					func_110301_a(null, npc.textureLocation, npc.display.url);
					LastTextureTick = 0;
				}
				catch(Exception ex){
					
				}
			}
		}
		if(npc.textureLocation == null)
			return AbstractClientPlayer.locationStevePng;
		return npc.textureLocation;
	}

    private void func_110301_a(File file, ResourceLocation resource, String par1Str){
        TextureManager texturemanager = Minecraft.getMinecraft().getTextureManager();
        ITextureObject object = new ImageDownloadAlt(file, par1Str, SkinManager.field_152793_a, new ImageBufferDownloadAlt());
        texturemanager.loadTexture(resource, object);
    }
}
