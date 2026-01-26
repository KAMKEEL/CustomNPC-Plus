package kamkeel.npcs.client.renderer;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.entity.EntityAbilityOrb;
import net.minecraft.entity.Entity;
import org.lwjgl.opengl.GL11;

/**
 * Renders the AbilityOrb entity as a cube with inner and outer layers.
 * Minecraft-style blocky appearance using Tessellator.
 *
 * Design inspired by LouisXIV's energy rendering system.
 */
@SideOnly(Side.CLIENT)
public class RenderAbilityOrbCube extends RenderAbilityProjectile {

    @Override
    public void doRender(Entity entity, double x, double y, double z, float yaw, float partialTicks) {
        EntityAbilityOrb orb = (EntityAbilityOrb) entity;

        setupRenderState();

        GL11.glTranslated(x, y, z);

        // Get interpolated size for smooth rendering
        float size = orb.getInterpolatedSize(partialTicks);

        // Pulsing scale effect
        float pulseTime = entity.ticksExisted + partialTicks;
        float scaleModifier = (float) Math.sin(pulseTime * 0.15f) * 0.05f;
        float scale = size * (1.0f + scaleModifier);

        // Use entity's interpolated rotation for smooth spinning
        GL11.glRotatef(orb.getInterpolatedRotationX(partialTicks), 1.0f, 0.0f, 0.0f);
        GL11.glRotatef(orb.getInterpolatedRotationY(partialTicks), 0.0f, 1.0f, 0.0f);
        GL11.glRotatef(orb.getInterpolatedRotationZ(partialTicks), 0.0f, 0.0f, 1.0f);

        GL11.glPushMatrix();
        GL11.glScalef(scale, scale, scale);

        // Render outer cube (translucent)
        GL11.glDepthMask(false);
        renderCube(orb.getOuterColor(), 0.5f, 0.5f);
        GL11.glDepthMask(true);

        // Render inner cube (solid, smaller)
        float innerScale = 0.6f;
        GL11.glScalef(innerScale, innerScale, innerScale);
        renderCube(orb.getInnerColor(), 1.0f, 0.5f);

        GL11.glPopMatrix();

        restoreRenderState();
    }
}
