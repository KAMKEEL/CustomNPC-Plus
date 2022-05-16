package noppes.npcs.client.renderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import noppes.npcs.client.Client;
import org.lwjgl.opengl.GL11;

public class RenderCNPCPlayer extends RenderPlayer {

    public RenderCNPCPlayer() {
    }

    @Override
    protected void renderModel(EntityLivingBase p_77036_1_, float p_77036_2_, float p_77036_3_, float p_77036_4_, float p_77036_5_, float p_77036_6_, float p_77036_7_) {
        this.bindEntityTexture(p_77036_1_);

        if (!p_77036_1_.isInvisible())
        {
            this.mainModel.render(p_77036_1_, p_77036_2_, p_77036_3_, p_77036_4_, p_77036_5_, p_77036_6_, p_77036_7_);

            if (Client.playerOverlay != null) {
                try {
                    this.bindTexture(Client.playerOverlay);
                    float f1 = 1.0F;
                    // Overlay & Glow
                    GL11.glDepthFunc(GL11.GL_LEQUAL);
                    GL11.glEnable(GL11.GL_BLEND);
                    GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE);
                    GL11.glDisable(GL11.GL_LIGHTING);

                    GL11.glDepthMask(!p_77036_1_.isInvisible());

                    GL11.glPushMatrix();
                    GL11.glMatrixMode(GL11.GL_TEXTURE);
                    GL11.glLoadIdentity();
                    GL11.glScalef(1.0F, 1.0F, 1.0F);

                    GL11.glMatrixMode(GL11.GL_MODELVIEW);
                    float scale = 1.001f;
                    GL11.glScalef(scale, scale, scale);
                    this.mainModel.render(p_77036_1_, p_77036_2_, p_77036_3_, p_77036_4_, p_77036_5_, p_77036_6_, p_77036_7_);
                    GL11.glPopMatrix();

                    GL11.glMatrixMode(GL11.GL_TEXTURE);
                    GL11.glLoadIdentity();
                    GL11.glMatrixMode(GL11.GL_MODELVIEW);

                    GL11.glEnable(GL11.GL_LIGHTING);
                    GL11.glColor4f(1.0F, 1.0F, 1.0F, f1);
                    GL11.glDepthFunc(GL11.GL_LEQUAL);
                    GL11.glDisable(GL11.GL_BLEND);
                } catch (Exception ignored) {}
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

    public void renderFirstPersonArm(EntityPlayer p_82441_1_)
    {
        float f = 1.0F;
        GL11.glColor3f(f, f, f);
        this.modelBipedMain.onGround = 0.0F;
        this.modelBipedMain.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, p_82441_1_);
        this.modelBipedMain.bipedRightArm.render(0.0625F);

        if (Client.playerOverlay != null) {
            try {
                this.bindTexture(Client.playerOverlay);
                float f1 = 1.0F;
                // Overlay & Glow
                GL11.glDepthFunc(GL11.GL_LEQUAL);
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE);
                GL11.glDisable(GL11.GL_LIGHTING);

                GL11.glDepthMask(!p_82441_1_.isInvisible());

                GL11.glPushMatrix();
                GL11.glMatrixMode(GL11.GL_TEXTURE);
                GL11.glLoadIdentity();
                GL11.glScalef(1.0F, 1.0F, 1.0F);

                GL11.glMatrixMode(GL11.GL_MODELVIEW);
                float scale = 1.001f;
                GL11.glScalef(scale, scale, scale);
                GL11.glColor3f(1.0F, 1.0F, 1.0F);
                this.modelBipedMain.onGround = 0.0F;
                this.modelBipedMain.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, p_82441_1_);
                this.modelBipedMain.bipedRightArm.render(0.0625F);
                GL11.glPopMatrix();

                GL11.glMatrixMode(GL11.GL_TEXTURE);
                GL11.glLoadIdentity();
                GL11.glMatrixMode(GL11.GL_MODELVIEW);

                GL11.glEnable(GL11.GL_LIGHTING);
                GL11.glColor4f(1.0F, 1.0F, 1.0F, f1);
                GL11.glDepthFunc(GL11.GL_LEQUAL);
                GL11.glDisable(GL11.GL_BLEND);
            } catch (Exception ignored) {}
        }
    }
}
