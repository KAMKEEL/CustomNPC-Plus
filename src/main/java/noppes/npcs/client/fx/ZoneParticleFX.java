package noppes.npcs.client.fx;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.world.World;

/**
 * Simple particle for zone visual effects (Trap/Hazard styles).
 * Uses vanilla particle atlas with color tinting. Fades out over lifetime.
 */
@SideOnly(Side.CLIENT)
public class ZoneParticleFX extends EntityFX {

    private final float startAlpha;
    private final boolean glow;

    public ZoneParticleFX(World world, double x, double y, double z,
                          double motionX, double motionY, double motionZ,
                          float r, float g, float b, float alpha,
                          float scale, int maxAge, float gravity, boolean glow) {
        super(world, x, y, z, 0, 0, 0);
        this.motionX = motionX;
        this.motionY = motionY;
        this.motionZ = motionZ;
        this.particleRed = r;
        this.particleGreen = g;
        this.particleBlue = b;
        this.particleAlpha = alpha;
        this.startAlpha = alpha;
        this.particleScale = scale;
        this.particleMaxAge = maxAge;
        this.particleGravity = gravity;
        this.glow = glow;
        this.noClip = true;
        this.setParticleTextureIndex(0);
    }

    @Override
    public int getBrightnessForRender(float partialTick) {
        return glow ? 0xF000F0 : super.getBrightnessForRender(partialTick);
    }

    @Override
    public void onUpdate() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;

        if (this.particleAge++ >= this.particleMaxAge) {
            this.setDead();
            return;
        }

        // Linear fade out
        float lifeRatio = (float) this.particleAge / (float) this.particleMaxAge;
        this.particleAlpha = this.startAlpha * (1.0f - lifeRatio);

        // Movement
        this.motionY -= 0.04D * (double) this.particleGravity;
        this.posX += this.motionX;
        this.posY += this.motionY;
        this.posZ += this.motionZ;
    }
}
