package noppes.npcs.client.renderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.client.Client;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.controllers.data.SkinOverlayData;
import org.lwjgl.opengl.GL11;

public class RenderCNPCPlayer extends RenderPlayer {
    public RenderCNPCPlayer() {
    }

    private boolean preRenderOverlay(boolean firstPerson, EntityPlayer player, ResourceLocation overlayLocation, boolean glow,
                                     float alpha, float size, float speedX, float speedY, float scaleX, float scaleY,
                                     float offsetX, float offsetY, float offsetZ) {
        try {
            this.bindTexture(overlayLocation);
        } catch (Exception exception) {
            return false;
        }

        float renderTicks;
        if (firstPerson) {
            if (!Client.fpSkinOverlayTicks.containsKey(player.getUniqueID())) {
                Client.fpSkinOverlayTicks.put(player.getUniqueID(), 1L);
            } else {
                long ticks = Client.fpSkinOverlayTicks.get(player.getUniqueID());
                Client.fpSkinOverlayTicks.put(player.getUniqueID(), ticks + 1);
            }
            renderTicks = Client.fpSkinOverlayTicks.get(player.getUniqueID());
        } else {
            if (!Client.entitySkinOverlayTicks.containsKey(player.getUniqueID())) {
                Client.entitySkinOverlayTicks.put(player.getUniqueID(), 1L);
            } else {
                long ticks = Client.entitySkinOverlayTicks.get(player.getUniqueID());
                Client.entitySkinOverlayTicks.put(player.getUniqueID(), ticks + 1);
            }
            renderTicks = Client.entitySkinOverlayTicks.get(player.getUniqueID());;
        }

        // Overlay & Glow
        if (glow) {
            GL11.glDepthFunc(GL11.GL_LEQUAL);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_DST_ALPHA);
            Minecraft.getMinecraft().entityRenderer.disableLightmap((double)0);
        }

        GL11.glColor4f(1.0F, 1.0F, 1.0F, alpha);

        GL11.glDepthMask(!player.isInvisible());

        GL11.glPushMatrix();
        GL11.glMatrixMode(GL11.GL_TEXTURE);
        GL11.glLoadIdentity();
        GL11.glTranslatef(renderTicks * 0.001F * speedX, renderTicks * 0.001F * speedY, 0.0F);
        GL11.glScalef(scaleX, scaleY, 1.0F);

        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        float scale = 1.005f * size;
        GL11.glTranslatef(offsetX, offsetY, offsetZ);
        GL11.glScalef(scale, scale, scale);

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
    }

    @Override
    protected void renderModel(EntityLivingBase p_77036_1_, float p_77036_2_, float p_77036_3_, float p_77036_4_, float p_77036_5_, float p_77036_6_, float p_77036_7_) {
        EntityPlayer player = (EntityPlayer) p_77036_1_;
        this.bindEntityTexture(p_77036_1_);

        if (!p_77036_1_.isInvisible())
        {
            this.mainModel.render(p_77036_1_, p_77036_2_, p_77036_3_, p_77036_4_, p_77036_5_, p_77036_6_, p_77036_7_);

            if (Client.skinOverlays.containsKey(player.getUniqueID())) {
                for (SkinOverlayData overlayData : Client.skinOverlays.get(player.getUniqueID()).values()) {
                    if (overlayData.location == null) {
                        overlayData.location = new ResourceLocation(overlayData.directory);
                    } else {
                        String str = overlayData.location.getResourceDomain()+":"+overlayData.location.getResourcePath();
                        if (!str.equals(overlayData.directory)) {
                            overlayData.location = new ResourceLocation(overlayData.directory);
                        }
                    }

                    if (!preRenderOverlay(false, player, overlayData.location, overlayData.glow, overlayData.alpha, overlayData.size,
                            overlayData.speedX, overlayData.speedY, overlayData.scaleX, overlayData.scaleY,
                            overlayData.offsetX, overlayData.offsetY, overlayData.offsetZ
                            ))
                        return;
                    this.mainModel.render(p_77036_1_, p_77036_2_, p_77036_3_, p_77036_4_, p_77036_5_, p_77036_6_, p_77036_7_);
                    postRenderOverlay(player);
                }
            } else if(player.getEntityData().hasKey("SkinOverlayData")) {
                Client.sendData(EnumPacketServer.SERVER_UPDATE_SKIN_OVERLAYS, new Object[0]);
            }
        }
        else if (!p_77036_1_.isInvisibleToPlayer(Minecraft.getMinecraft().thePlayer))
        {
            GL11.glPushMatrix();
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.15F);
            GL11.glDepthMask(false);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glAlphaFunc(GL11.GL_GREATER, 0.003921569F);
            this.mainModel.render(p_77036_1_, p_77036_2_, p_77036_3_, p_77036_4_, p_77036_5_, p_77036_6_, p_77036_7_);
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
            GL11.glPopMatrix();
            GL11.glDepthMask(true);
        }
        else
        {
            this.mainModel.setRotationAngles(p_77036_2_, p_77036_3_, p_77036_4_, p_77036_5_, p_77036_6_, p_77036_7_, p_77036_1_);
        }
    }

    public void renderFirstPersonArm(EntityPlayer player)
    {
        float f = 1.0F;
        GL11.glColor3f(f, f, f);
        this.modelBipedMain.onGround = 0.0F;
        this.modelBipedMain.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, player);
        this.modelBipedMain.bipedRightArm.render(0.0625F);

        if (Client.skinOverlays.containsKey(player.getUniqueID())) {
            for (SkinOverlayData overlayData : Client.skinOverlays.get(player.getUniqueID()).values()) {
                if (overlayData.location == null) {
                    overlayData.location = new ResourceLocation(overlayData.directory);
                } else {
                    String str = overlayData.location.getResourceDomain()+":"+overlayData.location.getResourcePath();
                    if (!str.equals(overlayData.directory)) {
                        overlayData.location = new ResourceLocation(overlayData.directory);
                    }
                }

                if (!preRenderOverlay(false, player, overlayData.location, overlayData.glow, overlayData.alpha, overlayData.size,
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
