package kamkeel.npcs.client.renderer;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.entity.EntityAbilityOrb;
import net.minecraft.entity.Entity;
import net.minecraft.util.Vec3;
import noppes.npcs.util.MathUtil;
import org.lwjgl.opengl.GL11;

/**
 * Renders the AbilityOrb entity as a cube with inner and outer layers.
 * Minecraft-style blocky appearance using Tessellator.
 */
@SideOnly(Side.CLIENT)
public class RenderAbilityOrb extends RenderEnergyAbility {

    private static final int LIGHTNING_FADE_TICKS = 6;

    @Override
    public void doRender(Entity entity, double x, double y, double z, float yaw, float partialTicks) {
        EntityAbilityOrb orb = (EntityAbilityOrb) entity;

        if (shouldSkipInitialActiveRender(entity)) {
            return;
        }

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
            renderAttachedLightning(orb, 0.6f * 0.5f, scale);
        }

        double motionX = entity.motionX;
        double motionY = entity.motionY;
        double motionZ = entity.motionZ;
        double speedSq = motionX * motionX + motionY * motionY + motionZ * motionZ;

        // Only apply motion-based facing when speed is meaningful.
        // Near-zero motion makes atan2 unstable, producing jittery angles.
        if (speedSq > 0.0001) {
            Vec3 motionVec = Vec3.createVectorHelper(motionX, motionY, motionZ);
            float motionYaw = MathUtil.getYaw(motionVec);
            float motionPitch = MathUtil.getPitch(motionVec);
            GL11.glRotatef(motionYaw, 0, 1, 0);
            GL11.glRotatef(motionPitch, 1, 0, 0);
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
            renderCube(orb.getOuterColor(), orb.getOuterColorAlpha(), 0.5f);
            GL11.glPopMatrix();
            GL11.glDepthMask(true);
        }

        // Render inner cube (solid)
        GL11.glScalef(innerScale, innerScale, innerScale);
        renderCube(orb.getInnerColor(), orb.getInnerAlpha(), 0.5f);

        GL11.glPopMatrix();
        GL11.glPopMatrix();

        restoreRenderState();
    }

}
