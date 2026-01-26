package kamkeel.npcs.client.renderer;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.client.renderer.quadric.SphereRenderer;
import kamkeel.npcs.entity.EntityAbilityOrb;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

/**
 * Renders the AbilityOrb entity as a glowing sphere with inner and outer layers.
 *
 * Design inspired by LouisXIV's energy rendering system.
 */
@SideOnly(Side.CLIENT)
public class RenderAbilityOrb extends Render {

    // White texture for solid color rendering
    private static final ResourceLocation WHITE_TEXTURE = new ResourceLocation("customnpcs", "textures/entity/white.png");

    // Static sphere renderer - shared across all orb renders
    private static final SphereRenderer sphere = new SphereRenderer(0.5f, 16);

    public RenderAbilityOrb() {
        this.shadowSize = 0.0f;
    }

    @Override
    public void doRender(Entity entity, double x, double y, double z, float yaw, float partialTicks) {
        EntityAbilityOrb orb = (EntityAbilityOrb) entity;

        GL11.glPushMatrix();

        // Disable culling, enable rescale normal
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glEnable(GL11.GL_ALPHA_TEST);

        // Enable blending for transparency
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glAlphaFunc(GL11.GL_GREATER, 0.003921569F);

        // Disable lighting for self-illumination
        GL11.glDisable(GL11.GL_LIGHTING);

        // Translate to render position
        GL11.glTranslated(x, y, z);

        // Bind texture
        this.bindTexture(WHITE_TEXTURE);

        // Get interpolated size for smooth rendering
        float size = orb.getInterpolatedSize(partialTicks);

        // Pulsing scale effect
        float pulseTime = entity.ticksExisted + partialTicks;
        float scaleModifier = (float) Math.sin(pulseTime * 0.15f) * 0.05f;
        float scale = size * (1.0f + scaleModifier);

        GL11.glPushMatrix();
        GL11.glScalef(scale, scale, scale);

        // Use entity's interpolated rotation for smooth spinning
        GL11.glRotatef(orb.getInterpolatedRotationX(partialTicks), 1.0f, 0.0f, 0.0f);
        GL11.glRotatef(orb.getInterpolatedRotationY(partialTicks), 0.0f, 1.0f, 0.0f);
        GL11.glRotatef(orb.getInterpolatedRotationZ(partialTicks), 0.0f, 0.0f, 1.0f);

        // Render outer sphere (translucent)
        GL11.glDepthMask(false);
        this.bindTexture(WHITE_TEXTURE);
        renderSphere(orb.getOuterColor(), 0.6f);
        GL11.glDepthMask(true);

        // Render inner sphere (more opaque)
        float innerScale = 0.75f;
        GL11.glPushMatrix();
        GL11.glScalef(innerScale, innerScale, innerScale);
        this.bindTexture(WHITE_TEXTURE);
        renderSphere(orb.getInnerColor(), 1.0f);
        GL11.glPopMatrix();

        GL11.glPopMatrix();

        // Restore GL state
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glPopMatrix();
    }

    private void renderSphere(int color, float alpha) {
        // Set sphere properties and render (rotation already applied to matrix)
        sphere.setScale(1.0f);
        sphere.setAlpha(alpha);
        sphere.setColor(color);
        sphere.render(0);
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity entity) {
        return WHITE_TEXTURE;
    }
}
