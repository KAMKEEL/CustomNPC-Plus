package noppes.npcs.client.renderer;

import cpw.mods.fml.common.Loader;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraftforge.client.event.RenderPlayerEvent;
import noppes.npcs.client.ClientCacheHandler;
import noppes.npcs.controllers.data.SkinOverlay;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Project;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;

public class RenderCNPCPlayer extends RenderPlayer {
    public static boolean hasMPM = false;
    public Minecraft mc = Minecraft.getMinecraft();
    public RenderCNPCHand itemRenderer = new RenderCNPCHand(mc);
    public float tempRenderPartialTicks;
    private float debugCamFOV;
    private float prevDebugCamFOV;
    private float fovModifierHand;
    private float fovModifierHandPrev;
    private float fovMultiplierTemp;

    public RenderCNPCPlayer() {
        super();
        this.modelBipedMain = (ModelBiped) this.mainModel;
        this.modelArmorChestplate = new ModelBiped(1.0F);
        this.modelArmor = new ModelBiped(0.5F);
        this.setRenderManager(RenderManager.instance);

        if (Loader.isModLoaded("moreplayermodels"))
            hasMPM = true;
    }

    private boolean preRenderOverlay(SkinOverlay overlayData, EntityPlayer player) {
        if (overlayData.texture.isEmpty())
            return false;

        ImageData imageData = ClientCacheHandler.getImageData(overlayData.texture);
        if (!imageData.imageLoaded())
            return false;

        try {
            imageData.bindTexture();
        } catch (Exception e) {
            return false;
        }

        // Overlay & Glow
        GL11.glEnable(GL11.GL_BLEND);
        if (overlayData.blend) {
            GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE);
        } else {
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        }
        GL11.glAlphaFunc(GL11.GL_GREATER, 0.003921569F);

        if (overlayData.glow) {
            GL11.glDisable(GL11.GL_LIGHTING);
            Minecraft.getMinecraft().entityRenderer.disableLightmap(0);
            RenderHelper.disableStandardItemLighting();
        }

        GL11.glColor4f(1.0F, 1.0F, 1.0F, overlayData.alpha);

        GL11.glDepthMask(!player.isInvisible());

        GL11.glPushMatrix();
        GL11.glMatrixMode(GL11.GL_TEXTURE);
        GL11.glLoadIdentity();
        GL11.glTranslatef(overlayData.ticks * 0.001F * overlayData.speedX, overlayData.ticks * 0.001F * overlayData.speedY, 0.0F);
        GL11.glScalef(overlayData.scaleX, overlayData.scaleY, 1.0F);
        overlayData.ticks++;

        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glTranslatef(overlayData.offsetX, overlayData.offsetY, overlayData.offsetZ);
        GL11.glScalef(overlayData.size, overlayData.size, overlayData.size);

        return true;
    }

    public void postRenderOverlay() {
        GL11.glPopMatrix();

        GL11.glMatrixMode(GL11.GL_TEXTURE);
        GL11.glLoadIdentity();
        GL11.glMatrixMode(GL11.GL_MODELVIEW);

        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
        Minecraft.getMinecraft().entityRenderer.enableLightmap(0);
        RenderHelper.enableStandardItemLighting();
    }

    @Override
    protected void renderModel(EntityLivingBase entity, float par2, float par3, float par4, float par5, float par6, float par7) {
        EntityPlayer player = (EntityPlayer) entity;
        if (!entity.isInvisible()) {
            if (ClientCacheHandler.skinOverlays.containsKey(player.getUniqueID())) {
                for (SkinOverlay overlayData : ClientCacheHandler.skinOverlays.get(player.getUniqueID()).values()) {
                    if (overlayData.texture.isEmpty())
                        continue;

                    ImageData imageData = ClientCacheHandler.getImageData(overlayData.texture);
                    if (!imageData.imageLoaded())
                        continue;

                    if (!this.preRenderOverlay(overlayData, player))
                        continue;

                    if (hasMPM) {
                        renderMorePlayerModel(entity, par2, par3, par4, par5, par6, par7);
                    } else {
                        this.modelBipedMain.render(entity, par2, par3, par4, par5, par6, par7);
                    }
                    postRenderOverlay();
                }
            }
        }
    }

    protected int shouldRenderPass(AbstractClientPlayer p_77032_1_, int p_77032_2_, float p_77032_3_) {
        return -1;
    }

    public void passSpecialRender(EntityLivingBase p_77033_1_, double p_77033_2_, double p_77033_4_, double p_77033_6_) {
        Render render = RenderManager.instance.getEntityRenderObject(p_77033_1_);
        if (render instanceof RenderPlayer) {
            RenderPlayer renderPlayer = (RenderPlayer) render;
            renderPlayer.passSpecialRender(p_77033_1_, p_77033_2_, p_77033_4_, p_77033_6_);
        }
    }

    @Override
    public void rotateCorpse(EntityLivingBase p_77043_1_, float p_77043_2_, float p_77043_3_, float p_77043_4_) {
        Render render = RenderManager.instance.getEntityRenderObject(p_77043_1_);
        if (render instanceof RenderPlayer) {
            RenderPlayer renderPlayer = (RenderPlayer) render;
            renderPlayer.rotateCorpse(p_77043_1_, p_77043_2_, p_77043_3_, p_77043_4_);
        }
    }

    protected void func_96449_a(AbstractClientPlayer p_96449_1_, double p_96449_2_, double p_96449_4_, double p_96449_6_, String p_96449_8_, float p_96449_9_, double p_96449_10_) {
    }

    public void renderHand(float partialTicks, int renderPass) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityRenderer entityRenderer = mc.entityRenderer;

        if (entityRenderer.debugViewDirection <= 0) {
            GL11.glMatrixMode(GL11.GL_PROJECTION);
            GL11.glLoadIdentity();
            float f1 = 0.07F;

            if (mc.gameSettings.anaglyph) {
                GL11.glTranslatef((float) (-(renderPass * 2 - 1)) * f1, 0.0F, 0.0F);
            }

            Project.gluPerspective(this.getFOVModifier(partialTicks, false), (float) mc.displayWidth / (float) mc.displayHeight, 0.05F, mc.gameSettings.renderDistanceChunks * 16 * 2.0F);

            if (mc.playerController.enableEverythingIsScrewedUpMode()) {
                float f2 = 0.6666667F;
                GL11.glScalef(1.0F, f2, 1.0F);
            }

            GL11.glMatrixMode(GL11.GL_MODELVIEW);
            GL11.glLoadIdentity();

            if (mc.gameSettings.anaglyph) {
                GL11.glTranslatef((float) (renderPass * 2 - 1) * 0.1F, 0.0F, 0.0F);
            }

            GL11.glPushMatrix();
            hurtCameraEffect(partialTicks);

            if (mc.gameSettings.viewBobbing) {
                setupViewBobbing(partialTicks);
            }

            if (mc.gameSettings.thirdPersonView == 0 && !mc.renderViewEntity.isPlayerSleeping() && !mc.gameSettings.hideGUI && !mc.playerController.enableEverythingIsScrewedUpMode()) {
                entityRenderer.enableLightmap(partialTicks);
                itemRenderer.renderOverlayInFirstPerson(partialTicks);
                entityRenderer.disableLightmap(partialTicks);
            }

            GL11.glPopMatrix();

            if (mc.gameSettings.viewBobbing) {
                setupViewBobbing(partialTicks);
            }
        }
    }

    private float getFOVModifier(float p_78481_1_, boolean p_78481_2_) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityLivingBase entityplayer = mc.renderViewEntity;
        float f1 = 70.0F;

        if (p_78481_2_) {
            f1 = mc.gameSettings.fovSetting;
            f1 *= this.fovModifierHandPrev + (this.fovModifierHand - this.fovModifierHandPrev) * p_78481_1_;
        }

        if (entityplayer.getHealth() <= 0.0F) {
            float f2 = (float) entityplayer.deathTime + p_78481_1_;
            f1 /= (1.0F - 500.0F / (f2 + 500.0F)) * 2.0F + 1.0F;
        }

        Block block = ActiveRenderInfo.getBlockAtEntityViewpoint(mc.theWorld, entityplayer, p_78481_1_);

        if (block.getMaterial() == Material.water) {
            f1 = f1 * 60.0F / 70.0F;
        }

        return f1 + this.prevDebugCamFOV + (this.debugCamFOV - this.prevDebugCamFOV) * p_78481_1_;
    }

    private void hurtCameraEffect(float p_78482_1_) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityLivingBase entitylivingbase = mc.renderViewEntity;
        float f1 = (float) entitylivingbase.hurtTime - p_78482_1_;
        float f2;

        if (entitylivingbase.getHealth() <= 0.0F) {
            f2 = (float) entitylivingbase.deathTime + p_78482_1_;
            GL11.glRotatef(40.0F - 8000.0F / (f2 + 200.0F), 0.0F, 0.0F, 1.0F);
        }

        if (f1 >= 0.0F) {
            f1 /= (float) entitylivingbase.maxHurtTime;
            f1 = MathHelper.sin(f1 * f1 * f1 * f1 * (float) Math.PI);
            f2 = entitylivingbase.attackedAtYaw;
            GL11.glRotatef(-f2, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(-f1 * 14.0F, 0.0F, 0.0F, 1.0F);
            GL11.glRotatef(f2, 0.0F, 1.0F, 0.0F);
        }
    }

    private void setupViewBobbing(float p_78475_1_) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.renderViewEntity instanceof EntityPlayer) {
            EntityPlayer entityplayer = (EntityPlayer) mc.renderViewEntity;
            float f1 = entityplayer.distanceWalkedModified - entityplayer.prevDistanceWalkedModified;
            float f2 = -(entityplayer.distanceWalkedModified + f1 * p_78475_1_);
            float f3 = entityplayer.prevCameraYaw + (entityplayer.cameraYaw - entityplayer.prevCameraYaw) * p_78475_1_;
            float f4 = entityplayer.prevCameraPitch + (entityplayer.cameraPitch - entityplayer.prevCameraPitch) * p_78475_1_;
            GL11.glTranslatef(MathHelper.sin(f2 * (float) Math.PI) * f3 * 0.5F, -Math.abs(MathHelper.cos(f2 * (float) Math.PI) * f3), 0.0F);
            GL11.glRotatef(MathHelper.sin(f2 * (float) Math.PI) * f3 * 3.0F, 0.0F, 0.0F, 1.0F);
            GL11.glRotatef(Math.abs(MathHelper.cos(f2 * (float) Math.PI - 0.2F) * f3) * 5.0F, 1.0F, 0.0F, 0.0F);
            GL11.glRotatef(f4, 1.0F, 0.0F, 0.0F);
        }
    }

    public void renderFirstPersonArmOverlay(EntityPlayer player) {
        // Check if player or model is null
        if (player == null || this.modelBipedMain == null) {
            return;
        }

        float gender = -1F;
        try {
            Class<?> RenderPlayerJBRA = Class.forName("JinRyuu.JBRA.RenderPlayerJBRA");
            gender = (float) RenderPlayerJBRA.getMethod("genGet").invoke(null);
        } catch (Exception ignored) {
        }

        // Check if the skinOverlays map exists for this player's UUID
        UUID uuid = player.getUniqueID();
        if (uuid == null || !ClientCacheHandler.skinOverlays.containsKey(uuid)) {
            return;
        }

        Map<Integer, SkinOverlay> overlayMap = ClientCacheHandler.skinOverlays.get(uuid);
        if (overlayMap == null || overlayMap.isEmpty()) {
            return;
        }
        // Iterate through each SkinOverlay safely
        for (SkinOverlay overlayData : overlayMap.values()) {
            if (overlayData == null || overlayData.texture == null || overlayData.texture.isEmpty()) {
                continue;
            }

            ImageData imageData = ClientCacheHandler.getImageData(overlayData.texture);
            if (imageData == null || !imageData.imageLoaded()) {
                continue;
            }

            // Check your preRenderOverlay return
            if (!this.preRenderOverlay(overlayData, player)) {
                continue;
            }

            // Adjust arm rendering for gender if needed
            if (gender >= 2.0F) {
                GL11.glRotatef(7F, 0, 0, 1);
                GL11.glTranslatef(0.015F, 0.0375F, -0.0025F);
            }

            // Render the arm
            this.modelBipedMain.onGround = 0.0F;
            this.modelBipedMain.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, player);
            this.modelBipedMain.bipedRightArm.render(0.0625F);

            postRenderOverlay();
        }
    }

    public void renderMorePlayerModel(EntityLivingBase entity, float par2, float par3, float par4, float par5, float par6, float par7) {
        Class<?> ModelMPMClass = null;
        Field isArmor = null;

        try {
            ModelMPMClass = Class.forName("noppes.mpm.client.model.ModelMPM");
            isArmor = ModelMPMClass.getDeclaredField("isArmor");
        } catch (Exception ignored) {
        }

        // Assuming this.modelBipedMain is an instance of ModelMPM
        if (ModelMPMClass != null && ModelMPMClass.isInstance(this.modelBipedMain)) {
            try {
                // Set isArmor field to false
                if (isArmor != null) {
                    isArmor.setBoolean(this.modelBipedMain, true);
                    this.modelBipedMain.render(entity, par2, par3, par4, par5, par6, par7);
                    isArmor.setBoolean(this.modelBipedMain, false);
                }
            } catch (IllegalAccessException ignored) {
            }
        }
    }

    public void renderDBCModel(RenderPlayerEvent.Specials.Post event) {
        EntityPlayer player = event.entityPlayer;

        Class<?> RenderPlayerJBRA = null;
        Class<?> ModelBipedDBC = null;
        Class<?> ModelBipedBody = null;
        Method renderDBC = null;
        Field rot1 = null, rot2 = null, rot3 = null, rot4 = null, rot5 = null, rot6 = null;
        Object m = null;
        ModelRenderer bipedHead = null, bipedBody = null, bipedRA = null, bipedLA = null, bipedRL = null, bipedLL = null;
        ModelRenderer Brightarm = null, Bleftarm = null, rightleg = null, leftleg = null, body = null, hip = null;
        ModelRenderer waist = null, bottom = null, Bbreast = null, Bbreast2 = null, breast = null, breast2 = null;
        float childScl = 0.0F;

        try {
            RenderPlayerJBRA = Class.forName("JinRyuu.JBRA.RenderPlayerJBRA");
            ModelBipedDBC = Class.forName("JinRyuu.JBRA.ModelBipedDBC");
            ModelBipedBody = Class.forName("JinRyuu.JRMCore.entity.ModelBipedBody");

            renderDBC = ModelBipedBody.getMethod("render", Entity.class, float.class, float.class, float.class, float.class, float.class, float.class);
            rot1 = ModelBipedDBC.getField("rot1");
            rot2 = ModelBipedDBC.getField("rot2");
            rot3 = ModelBipedDBC.getField("rot3");
            rot4 = ModelBipedDBC.getField("rot4");
            rot5 = ModelBipedDBC.getField("rot5");
            rot6 = ModelBipedDBC.getField("rot6");

            m = RenderPlayerJBRA.getField("modelMain").get(event.renderer);
            ModelBipedBody.getField("isRiding").set(m, player.isRiding());
            ModelBipedBody.getField("isChild").set(m, player.isChild());
            ModelBipedBody.getField("isSneak").set(m, player.isSneaking());
            ModelBipedBody.getField("y").set(null, ModelBipedDBC.getField("y").get(null));

            bipedHead = (ModelRenderer) ModelBipedBody.getField("bipedHead").get(m);
            bipedBody = (ModelRenderer) ModelBipedBody.getField("bipedBody").get(m);
            bipedRA = (ModelRenderer) ModelBipedBody.getField("bipedRightArm").get(m);
            bipedLA = (ModelRenderer) ModelBipedBody.getField("bipedLeftArm").get(m);
            bipedRL = (ModelRenderer) ModelBipedBody.getField("bipedRightLeg").get(m);
            bipedLL = (ModelRenderer) ModelBipedBody.getField("bipedLeftLeg").get(m);

            Brightarm = (ModelRenderer) ModelBipedBody.getField("Brightarm").get(m);
            Bleftarm = (ModelRenderer) ModelBipedBody.getField("Bleftarm").get(m);
            rightleg = (ModelRenderer) ModelBipedBody.getField("rightleg").get(m);
            leftleg = (ModelRenderer) ModelBipedBody.getField("leftleg").get(m);
            body = (ModelRenderer) ModelBipedBody.getField("body").get(m);
            hip = (ModelRenderer) ModelBipedBody.getField("hip").get(m);
            waist = (ModelRenderer) ModelBipedBody.getField("waist").get(m);
            bottom = (ModelRenderer) ModelBipedBody.getField("bottom").get(m);
            Bbreast = (ModelRenderer) ModelBipedBody.getField("Bbreast").get(m);
            Bbreast2 = (ModelRenderer) ModelBipedBody.getField("Bbreast2").get(m);
            breast = (ModelRenderer) ModelBipedBody.getField("breast").get(m);
            breast2 = (ModelRenderer) ModelBipedBody.getField("breast2").get(m);
            childScl = (float) RenderPlayerJBRA.getMethod("childSclGet").invoke(null);
        } catch (Exception ignored) {
        }

        try {
            RenderPlayerJBRA = Class.forName("JinRyuu.JBRA.RenderPlayerJBRA");
            ModelBipedDBC = Class.forName("JinRyuu.JBRA.ModelBipedDBC");
            ModelBipedBody = Class.forName("JinRyuu.JRMCore.entity.ModelBipedBody");

            renderDBC = ModelBipedBody.getMethod("func_78088_a", Entity.class, float.class, float.class, float.class, float.class, float.class, float.class);
            rot1 = ModelBipedDBC.getField("rot1");
            rot2 = ModelBipedDBC.getField("rot2");
            rot3 = ModelBipedDBC.getField("rot3");
            rot4 = ModelBipedDBC.getField("rot4");
            rot5 = ModelBipedDBC.getField("rot5");
            rot6 = ModelBipedDBC.getField("rot6");

            m = RenderPlayerJBRA.getField("modelMain").get(event.renderer);
            ModelBipedBody.getField("field_78093_q").set(m, player.isRiding());
            ModelBipedBody.getField("field_78091_s").set(m, player.isChild());
            ModelBipedBody.getField("field_78117_n").set(m, player.isSneaking());
            ModelBipedBody.getField("y").set(null, ModelBipedDBC.getField("y").get(null));

            bipedHead = (ModelRenderer) ModelBipedBody.getField("field_78116_c").get(m);
            bipedBody = (ModelRenderer) ModelBipedBody.getField("field_78115_e").get(m);
            bipedRA = (ModelRenderer) ModelBipedBody.getField("field_78112_f").get(m);
            bipedLA = (ModelRenderer) ModelBipedBody.getField("field_78113_g").get(m);
            bipedRL = (ModelRenderer) ModelBipedBody.getField("field_78123_h").get(m);
            bipedLL = (ModelRenderer) ModelBipedBody.getField("field_78124_i").get(m);

            Brightarm = (ModelRenderer) ModelBipedBody.getField("Brightarm").get(m);
            Bleftarm = (ModelRenderer) ModelBipedBody.getField("Bleftarm").get(m);
            rightleg = (ModelRenderer) ModelBipedBody.getField("rightleg").get(m);
            leftleg = (ModelRenderer) ModelBipedBody.getField("leftleg").get(m);
            body = (ModelRenderer) ModelBipedBody.getField("body").get(m);
            hip = (ModelRenderer) ModelBipedBody.getField("hip").get(m);
            waist = (ModelRenderer) ModelBipedBody.getField("waist").get(m);
            bottom = (ModelRenderer) ModelBipedBody.getField("bottom").get(m);
            Bbreast = (ModelRenderer) ModelBipedBody.getField("Bbreast").get(m);
            Bbreast2 = (ModelRenderer) ModelBipedBody.getField("Bbreast2").get(m);
            breast = (ModelRenderer) ModelBipedBody.getField("breast").get(m);
            breast2 = (ModelRenderer) ModelBipedBody.getField("breast2").get(m);
            childScl = (float) RenderPlayerJBRA.getMethod("childSclGet").invoke(null);
        } catch (Exception ignored) {
        }

        try {
            if (ClientCacheHandler.skinOverlays.containsKey(player.getUniqueID())) {
                for (SkinOverlay overlayData : ClientCacheHandler.skinOverlays.get(player.getUniqueID()).values()) {
                    if (overlayData.texture.isEmpty())
                        continue;

                    ImageData imageData = ClientCacheHandler.getImageData(overlayData.texture);
                    if (!imageData.imageLoaded())
                        continue;

                    try {
                        imageData.bindTexture();
                    } catch (Exception e) {
                        continue;
                    }

                    if (!this.preRenderOverlay(overlayData, player))
                        continue;

                    bipedHead.isHidden = true;
                    renderDBC.invoke(m, player,
                            rot1.get(m), rot2.get(m), rot3.get(m),
                            rot4.get(m), rot5.get(m), rot6.get(m)
                    );
                    bipedHead.isHidden = false;

                    bipedBody.isHidden = true;
                    bipedRA.isHidden = true;
                    bipedLA.isHidden = true;
                    bipedRL.isHidden = true;
                    bipedLL.isHidden = true;
                    //Female render
                    Brightarm.isHidden = true;
                    Bleftarm.isHidden = true;
                    rightleg.isHidden = true;
                    leftleg.isHidden = true;
                    body.isHidden = true;
                    hip.isHidden = true;
                    waist.isHidden = true;
                    bottom.isHidden = true;
                    Bbreast.isHidden = true;
                    Bbreast2.isHidden = true;
                    breast.isHidden = true;
                    breast2.isHidden = true;
                    if (player.isSneaking()) {
                        GL11.glTranslatef(0, 0.06F, 0);
                    }
                    if (childScl > 1.5F) {
                        GL11.glTranslatef(0, -0.015F, 0);
                        GL11.glScalef(1.025F, 1.025F, 1.025F);
                    } else {
                        if (childScl > 1) {
                            GL11.glTranslatef(0, -0.01F, 0);
                            GL11.glScalef(1.025F, 1.025F, 1.025F);
                        } else {
                            GL11.glTranslatef(0, 0.0025F, 0);
                            GL11.glScalef(1.02F, 1.02F, 1.02F);
                        }
                    }
                    renderDBC.invoke(m, player,
                            rot1.get(m), rot2.get(m), rot3.get(m),
                            rot4.get(m), rot5.get(m), rot6.get(m)
                    );
                    bipedBody.isHidden = false;
                    bipedRA.isHidden = false;
                    bipedLA.isHidden = false;
                    bipedRL.isHidden = false;
                    bipedLL.isHidden = false;
                    //Female render
                    Brightarm.isHidden = false;
                    Bleftarm.isHidden = false;
                    rightleg.isHidden = false;
                    leftleg.isHidden = false;
                    body.isHidden = false;
                    hip.isHidden = false;
                    waist.isHidden = false;
                    bottom.isHidden = false;
                    Bbreast.isHidden = false;
                    Bbreast2.isHidden = false;
                    breast.isHidden = false;
                    breast2.isHidden = false;

                    postRenderOverlay();
                }
            }
        } catch (Exception ignored) {
        }
    }
}
