package kamkeel.npcs.client.renderer;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.client.renderer.lightning.AttachedLightningRenderer;
import kamkeel.npcs.entity.EntityAbilityOrb;
import net.minecraft.entity.Entity;
import org.lwjgl.opengl.GL11;

/**
 * Renders the AbilityOrb entity as a cube with inner and outer layers.
 * Minecraft-style blocky appearance using Tessellator.
 */
@SideOnly(Side.CLIENT)
public class RenderAbilityOrb extends RenderAbilityProjectile {

    private static final int LIGHTNING_FADE_TICKS = 6;

    @Override
    public void doRender(Entity entity, double x, double y, double z, float yaw, float partialTicks) {
        EntityAbilityOrb orb = (EntityAbilityOrb) entity;

        setupRenderState();

        GL11.glPushMatrix();
        GL11.glTranslated(x, y, z);

        // Get interpolated size for smooth rendering
        float size = orb.getInterpolatedSize(partialTicks);

        // Pulsing scale effect
        float pulseTime = entity.ticksExisted + partialTicks;
        float scaleModifier = (float) Math.sin(pulseTime * 0.15f) * 0.05f;
        float scale = size * (1.0f + scaleModifier);

        // Render lightning BEFORE rotation so it crackles in all directions
        if (orb.hasLightningEffect()) {
            renderAttachedLightning(orb, scale);
        }

        // Use entity's interpolated rotation for smooth spinning
        GL11.glRotatef(orb.getInterpolatedRotationX(partialTicks), 1.0f, 0.0f, 0.0f);
        GL11.glRotatef(orb.getInterpolatedRotationY(partialTicks), 0.0f, 1.0f, 0.0f);
        GL11.glRotatef(orb.getInterpolatedRotationZ(partialTicks), 0.0f, 0.0f, 1.0f);

        GL11.glPushMatrix();
        GL11.glScalef(scale, scale, scale);

        // Inner scale defines the core size
        float innerScale = 0.6f;

        // Render outer cube (translucent) - only if enabled
        // outerColorWidth is an additive offset from inner size
        if (orb.isOuterColorEnabled()) {
            float outerScale = innerScale + orb.getOuterColorWidth();
            GL11.glDepthMask(false);
            GL11.glPushMatrix();
            GL11.glScalef(outerScale, outerScale, outerScale);
            renderCube(orb.getOuterColor(), 0.5f, 0.5f);
            GL11.glPopMatrix();
            GL11.glDepthMask(true);
        }

        // Render inner cube (solid)
        GL11.glScalef(innerScale, innerScale, innerScale);
        renderCube(orb.getInnerColor(), 1.0f, 0.5f);

        GL11.glPopMatrix();
        GL11.glPopMatrix();

        restoreRenderState();
    }

    /**
     * Render fading lightning arcs attached to the orb (in local space).
     * Arcs persist for a few ticks and fade out, while staying attached to the orb.
     */
    private void renderAttachedLightning(EntityAbilityOrb orb, float orbScale) {
        // Get or create the lightning state for this entity
        AttachedLightningRenderer.LightningState state = getLightningState(orb);

        float density = orb.getLightningDensity();
        // Lightning radius extends outward from inner surface (innerScale = 0.6)
        float innerRadius = 0.6f * orbScale * 0.5f; // Half size of inner cube
        float radius = innerRadius + orb.getLightningRadius() * orbScale;
        int outerColor = orb.getOuterColor();
        int innerColor = orb.getInnerColor();
        int fadeTime = orb.getLightningFadeTime();

        // Update: age existing arcs, spawn new ones based on density
        state.update(density, radius, outerColor, innerColor, fadeTime);

        // Render all active arcs in local space (they move with the orb)
        state.render();
    }

    /**
     * Get or create the lightning state for an entity.
     */
    private AttachedLightningRenderer.LightningState getLightningState(EntityAbilityOrb orb) {
        if (orb.lightningState == null) {
            orb.lightningState = new AttachedLightningRenderer.LightningState();
        }
        return (AttachedLightningRenderer.LightningState) orb.lightningState;
    }
}
