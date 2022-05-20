package noppes.npcs.client.renderer;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.client.Client;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.controllers.data.SkinOverlayData;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Project;

public class RenderCNPCPlayer extends RenderPlayer {
    public RenderCNPCPlayerItemRenderer itemRenderer = new RenderCNPCPlayerItemRenderer(Minecraft.getMinecraft());
    private float debugCamFOV;
    private float prevDebugCamFOV;
    private float fovModifierHand;
    private float fovModifierHandPrev;
    private float fovMultiplierTemp;

    public RenderCNPCPlayer() {
        super();
        this.modelBipedMain = (ModelBiped)this.mainModel;
        this.modelArmorChestplate = new ModelBiped(1.0F);
        this.modelArmor = new ModelBiped(0.5F);
        this.setRenderManager(RenderManager.instance);
    }

    private boolean preRenderOverlay(float partialTickTime, EntityPlayer player, ResourceLocation overlayLocation, boolean glow,
                                     float alpha, float size, float speedX, float speedY, float scaleX, float scaleY,
                                     float offsetX, float offsetY, float offsetZ) {
        try {
            this.bindTexture(overlayLocation);
        } catch (Exception exception) {
            return false;
        }

        if (partialTickTime == 0) {
            if (!Client.entitySkinOverlayTicks.containsKey(player.getUniqueID())) {
                Client.entitySkinOverlayTicks.put(player.getUniqueID(), 1L);
            } else {
                long ticks = Client.entitySkinOverlayTicks.get(player.getUniqueID());
                Client.entitySkinOverlayTicks.put(player.getUniqueID(), ticks + 1);
            }
            partialTickTime = Client.entitySkinOverlayTicks.get(player.getUniqueID());;
        }

        // Overlay & Glow
        if (glow) {
            GL11.glDepthFunc(GL11.GL_LEQUAL);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_DST_ALPHA);
            Minecraft.getMinecraft().entityRenderer.disableLightmap((double)0);
            RenderHelper.disableStandardItemLighting();
        }

        GL11.glColor4f(1.0F, 1.0F, 1.0F, alpha);

        GL11.glDepthMask(!player.isInvisible());

        GL11.glPushMatrix();
        GL11.glMatrixMode(GL11.GL_TEXTURE);
        GL11.glLoadIdentity();
        GL11.glTranslatef(partialTickTime * 0.001F * speedX, partialTickTime * 0.001F * speedY, 0.0F);
        GL11.glScalef(scaleX, scaleY, 1.0F);

        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glTranslatef(offsetX, offsetY, offsetZ);
        GL11.glScalef(size, size, size);

        return true;
    }
    public void postRenderOverlay(EntityPlayer player) {
        GL11.glPopMatrix();

        GL11.glMatrixMode(GL11.GL_TEXTURE);
        GL11.glLoadIdentity();
        GL11.glMatrixMode(GL11.GL_MODELVIEW);

        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glDepthFunc(GL11.GL_LEQUAL);
        GL11.glDisable(GL11.GL_BLEND);
        Minecraft.getMinecraft().entityRenderer.enableLightmap((double)0);
        RenderHelper.enableStandardItemLighting();
    }

    @Override
    protected void renderModel(EntityLivingBase p_77036_1_, float p_77036_2_, float p_77036_3_, float p_77036_4_, float p_77036_5_, float p_77036_6_, float p_77036_7_) {
        EntityPlayer player = (EntityPlayer) p_77036_1_;

        if (!p_77036_1_.isInvisible())
        {
            if (Client.skinOverlays.containsKey(player.getUniqueID())) {
                for (SkinOverlayData overlayData : Client.skinOverlays.get(player.getUniqueID()).values()) {
                    if (overlayData.location == null) {
                        overlayData.location = new ResourceLocation(overlayData.texture);
                    } else {
                        String str = overlayData.location.getResourceDomain()+":"+overlayData.location.getResourcePath();
                        if (!str.equals(overlayData.texture)) {
                            overlayData.location = new ResourceLocation(overlayData.texture);
                        }
                    }

                    if (!preRenderOverlay(0, player, overlayData.location, overlayData.glow, overlayData.alpha, overlayData.size,
                            overlayData.speedX, overlayData.speedY, overlayData.scaleX, overlayData.scaleY,
                            overlayData.offsetX, overlayData.offsetY, overlayData.offsetZ
                            ))
                        return;
                    this.modelBipedMain.render(p_77036_1_, p_77036_2_, p_77036_3_, p_77036_4_, p_77036_5_, p_77036_6_, p_77036_7_);
                    postRenderOverlay(player);
                }
            } else if(player.getEntityData().hasKey("SkinOverlayData")) {
                Client.sendData(EnumPacketServer.SERVER_UPDATE_SKIN_OVERLAYS, new Object[0]);
            }
        }
    }
    
    public void renderHand(float partialTicks, int renderPass) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityRenderer entityRenderer = mc.entityRenderer;
        EntityClientPlayerMP player = mc.thePlayer;

        if (entityRenderer.debugViewDirection <= 0)
        {
            GL11.glMatrixMode(GL11.GL_PROJECTION);
            GL11.glLoadIdentity();
            float f1 = 0.07F;

            if (mc.gameSettings.anaglyph)
            {
                GL11.glTranslatef((float)(-(renderPass * 2 - 1)) * f1, 0.0F, 0.0F);
            }

            /*if (entityRenderer.cameraZoom != 1.0D) //EntityRenderer's "cameraZoom" field is always 1.0D??? Why is this here??? o_O
            {
                GL11.glTranslatef((float)player.cameraYaw, (float)(-player.cameraPitch), 0.0F);
                GL11.glScaled(entityRenderer.cameraZoom, entityRenderer.cameraZoom, 1.0D);
            }*/

            Project.gluPerspective(this.getFOVModifier(partialTicks, false), (float)mc.displayWidth / (float)mc.displayHeight, 0.05F, mc.gameSettings.renderDistanceChunks * 16 * 2.0F);

            if (mc.playerController.enableEverythingIsScrewedUpMode())
            {
                float f2 = 0.6666667F;
                GL11.glScalef(1.0F, f2, 1.0F);
            }

            GL11.glMatrixMode(GL11.GL_MODELVIEW);
            GL11.glLoadIdentity();

            if (mc.gameSettings.anaglyph)
            {
                GL11.glTranslatef((float)(renderPass * 2 - 1) * 0.1F, 0.0F, 0.0F);
            }

            GL11.glPushMatrix();
            hurtCameraEffect(partialTicks);

            if (mc.gameSettings.viewBobbing)
            {
                setupViewBobbing(partialTicks);
            }

            if (mc.gameSettings.thirdPersonView == 0 && !mc.renderViewEntity.isPlayerSleeping() && !mc.gameSettings.hideGUI && !mc.playerController.enableEverythingIsScrewedUpMode())
            {
                itemRenderer.updateEquippedItem();
                entityRenderer.enableLightmap((double)partialTicks);
                itemRenderer.renderItemInFirstPerson(partialTicks);
                entityRenderer.disableLightmap((double)partialTicks);
            }

            GL11.glPopMatrix();

            if (mc.gameSettings.thirdPersonView == 0 && !mc.renderViewEntity.isPlayerSleeping())
            {
                entityRenderer.itemRenderer.renderOverlays(partialTicks);
                hurtCameraEffect(partialTicks);
            }

            if (mc.gameSettings.viewBobbing)
            {
                setupViewBobbing(partialTicks);
            }
        }
    }

    private float getFOVModifier(float p_78481_1_, boolean p_78481_2_)
    {
        Minecraft mc = Minecraft.getMinecraft();
        EntityLivingBase entityplayer = (EntityLivingBase)mc.renderViewEntity;
        float f1 = 70.0F;

        if (p_78481_2_)
        {
            f1 = mc.gameSettings.fovSetting;
            f1 *= this.fovModifierHandPrev + (this.fovModifierHand - this.fovModifierHandPrev) * p_78481_1_;
        }

        if (entityplayer.getHealth() <= 0.0F)
        {
            float f2 = (float)entityplayer.deathTime + p_78481_1_;
            f1 /= (1.0F - 500.0F / (f2 + 500.0F)) * 2.0F + 1.0F;
        }

        Block block = ActiveRenderInfo.getBlockAtEntityViewpoint(mc.theWorld, entityplayer, p_78481_1_);

        if (block.getMaterial() == Material.water)
        {
            f1 = f1 * 60.0F / 70.0F;
        }

        return f1 + this.prevDebugCamFOV + (this.debugCamFOV - this.prevDebugCamFOV) * p_78481_1_;
    }

    public void updateFovModifierHand()
    {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.renderViewEntity instanceof EntityPlayerSP)
        {
            EntityPlayerSP entityplayersp = (EntityPlayerSP)mc.renderViewEntity;
            this.fovMultiplierTemp = entityplayersp.getFOVMultiplier();
        }
        else
        {
            this.fovMultiplierTemp = mc.thePlayer.getFOVMultiplier();
        }
        this.fovModifierHandPrev = this.fovModifierHand;
        this.fovModifierHand += (this.fovMultiplierTemp - this.fovModifierHand) * 0.5F;

        if (this.fovModifierHand > 1.5F)
        {
            this.fovModifierHand = 1.5F;
        }

        if (this.fovModifierHand < 0.1F)
        {
            this.fovModifierHand = 0.1F;
        }
    }

    private void hurtCameraEffect(float p_78482_1_)
    {
        Minecraft mc = Minecraft.getMinecraft();
        EntityLivingBase entitylivingbase = mc.renderViewEntity;
        float f1 = (float)entitylivingbase.hurtTime - p_78482_1_;
        float f2;

        if (entitylivingbase.getHealth() <= 0.0F)
        {
            f2 = (float)entitylivingbase.deathTime + p_78482_1_;
            GL11.glRotatef(40.0F - 8000.0F / (f2 + 200.0F), 0.0F, 0.0F, 1.0F);
        }

        if (f1 >= 0.0F)
        {
            f1 /= (float)entitylivingbase.maxHurtTime;
            f1 = MathHelper.sin(f1 * f1 * f1 * f1 * (float)Math.PI);
            f2 = entitylivingbase.attackedAtYaw;
            GL11.glRotatef(-f2, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(-f1 * 14.0F, 0.0F, 0.0F, 1.0F);
            GL11.glRotatef(f2, 0.0F, 1.0F, 0.0F);
        }
    }

    private void setupViewBobbing(float p_78475_1_)
    {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.renderViewEntity instanceof EntityPlayer)
        {
            EntityPlayer entityplayer = (EntityPlayer)mc.renderViewEntity;
            float f1 = entityplayer.distanceWalkedModified - entityplayer.prevDistanceWalkedModified;
            float f2 = -(entityplayer.distanceWalkedModified + f1 * p_78475_1_);
            float f3 = entityplayer.prevCameraYaw + (entityplayer.cameraYaw - entityplayer.prevCameraYaw) * p_78475_1_;
            float f4 = entityplayer.prevCameraPitch + (entityplayer.cameraPitch - entityplayer.prevCameraPitch) * p_78475_1_;
            GL11.glTranslatef(MathHelper.sin(f2 * (float)Math.PI) * f3 * 0.5F, -Math.abs(MathHelper.cos(f2 * (float)Math.PI) * f3), 0.0F);
            GL11.glRotatef(MathHelper.sin(f2 * (float)Math.PI) * f3 * 3.0F, 0.0F, 0.0F, 1.0F);
            GL11.glRotatef(Math.abs(MathHelper.cos(f2 * (float)Math.PI - 0.2F) * f3) * 5.0F, 1.0F, 0.0F, 0.0F);
            GL11.glRotatef(f4, 1.0F, 0.0F, 0.0F);
        }
    }

    public void renderFirstPersonArm(EntityPlayer player, float partialTickTime)
    {
        Render render = RenderManager.instance.getEntityRenderObject(player);
        RenderPlayer renderplayer = (RenderPlayer)render;
        renderplayer.renderFirstPersonArm(player);

        if (Client.skinOverlays.containsKey(player.getUniqueID())) {
            for (SkinOverlayData overlayData : Client.skinOverlays.get(player.getUniqueID()).values()) {
                if (overlayData.location == null) {
                    overlayData.location = new ResourceLocation(overlayData.texture);
                } else {
                    String str = overlayData.location.getResourceDomain()+":"+overlayData.location.getResourcePath();
                    if (!str.equals(overlayData.texture)) {
                        overlayData.location = new ResourceLocation(overlayData.texture);
                    }
                }

                if (!preRenderOverlay(partialTickTime, player, overlayData.location, overlayData.glow, overlayData.alpha, overlayData.size,
                        overlayData.speedX, overlayData.speedY, overlayData.scaleX, overlayData.scaleY,
                        overlayData.offsetX, overlayData.offsetY, overlayData.offsetZ
                ))
                    return;
                this.modelBipedMain.onGround = 0.0F;
                this.modelBipedMain.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, player);
                this.modelBipedMain.bipedRightArm.render(0.0625F);
                postRenderOverlay(player);
            }
        } else if(player.getEntityData().hasKey("SkinOverlayData")) {
            Client.sendData(EnumPacketServer.SERVER_UPDATE_SKIN_OVERLAYS, new Object[0]);
        }
    }
}
