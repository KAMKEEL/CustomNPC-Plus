package noppes.npcs.client.fx;

import cpw.mods.fml.common.eventhandler.Cancelable;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityEvent.EntityConstructing;
import org.lwjgl.opengl.GL11;

public class EntityBigSmokeFX extends EntityFX {

    /**
     * there are 12 smoke textures.
     */
    public static final int TEXTURE_COUNT = 12;
    public static final ResourceLocation[] TEXTURES = new ResourceLocation[TEXTURE_COUNT];

    static {
        for (int i = 0; i < TEXTURE_COUNT; i++)
            TEXTURES[i] = new ResourceLocation("customnpcs" + ":" + "textures/particle/big_smoke_" + i + ".png");
    }

    protected final int texIndex;
    protected boolean alphaFading = false;
    protected float alphaFadePerTick = -0.015F;
    protected boolean localizedCombustion = true;
    protected boolean isColored = false;

    /**
     * Posts a {@link EntityBigSmokeFXConstructingEvent}.
     */
    public EntityBigSmokeFX(World world, int x, int y, int z, boolean signalFire, float[] colours) {
        super(world, x, y, z);

        // Position: Centered above the spawn point with slight randomness
        this.setPosition(
            x + 0.5 + (rand.nextDouble() / 10.0 * (rand.nextBoolean() ? 1 : -1)), // Small horizontal offset
            y + 0.1 + (rand.nextDouble() * 0.1), // Start slightly above y
            z + 0.5 + (rand.nextDouble() / 10.0 * (rand.nextBoolean() ? 1 : -1))
        );

        // Scale: Smoke size
        this.particleScale = 4.0F * (rand.nextFloat() * 0.5F + 0.5F); // Smaller size
        this.setSize(0.25F, 0.25F);

        // Gravity: Slows upward motion gradually
        this.particleGravity = 0.005F;

        // Upward motion: Slow, with slight randomness
        this.motionX = 0; // No horizontal motion
        this.motionY = 0.02F + rand.nextFloat() * 0.01F;
        this.motionZ = 0; // No horizontal motion

        this.particleMaxAge = rand.nextInt(50) + 100; // Shorter lifetime for normal smoke
        this.particleAlpha = 0.8F;

        if (colours.length == 3) {
            this.setRBGColorF(colours[0], colours[1], colours[2]);
        }

        // Smoke appearance
        this.texIndex = rand.nextInt(TEXTURE_COUNT);
        this.isColored = this.particleRed != 1.0F || this.particleGreen != 1.0F || this.particleBlue != 1.0F;
    }

    @Override
    public void renderParticle(Tessellator tess, float partialTicks, float rotX, float rotXZ, float rotZ, float rotYZ, float rotXY) {
        rotX = ActiveRenderInfo.rotationX;
        rotXZ = ActiveRenderInfo.rotationXZ;
        rotZ = ActiveRenderInfo.rotationZ;
        rotYZ = ActiveRenderInfo.rotationYZ;
        rotXY = ActiveRenderInfo.rotationXY;

        EntityLivingBase view = Minecraft.getMinecraft().renderViewEntity;
        double interpX = view.lastTickPosX + (view.posX - view.lastTickPosX) * partialTicks;
        double interpY = view.lastTickPosY + (view.posY - view.lastTickPosY) * partialTicks;
        double interpZ = view.lastTickPosZ + (view.posZ - view.lastTickPosZ) * partialTicks;

        float partialPosX = (float) (prevPosX + (posX - prevPosX) * partialTicks - interpX);
        float partialPosY = (float) (prevPosY + (posY - prevPosY) * partialTicks - interpY);
        float partialPosZ = (float) (prevPosZ + (posZ - prevPosZ) * partialTicks - interpZ);

        float scale = 0.1F * particleScale;

        double minU = this.particleTextureIndexX * 0.25;
        double maxU = minU + 0.25;
        double minV = this.particleTextureIndexY * 0.125;
        double maxV = minV + 0.125;

        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glAlphaFunc(GL11.GL_GREATER, 0.003921569F);

        Minecraft.getMinecraft().renderEngine.bindTexture(TEXTURES[this.texIndex % TEXTURES.length]);

        tess.startDrawingQuads();

        tess.setColorRGBA_F(particleRed, particleGreen, particleBlue, particleAlpha);
        tess.setBrightness(15 << 20 | 14 << 4); // slightly less than MiscUtil.MAX_LIGHT_BRIGHTNESS so that smoke doesn't glow super bright with shaders

        tess.addVertexWithUV(partialPosX - rotX * scale - rotYZ * scale, partialPosY - rotXZ * scale, partialPosZ - rotZ * scale - rotXY * scale, 1, 1);
        tess.addVertexWithUV(partialPosX - rotX * scale + rotYZ * scale, partialPosY + rotXZ * scale, partialPosZ - rotZ * scale + rotXY * scale, 1, 0);
        tess.addVertexWithUV(partialPosX + rotX * scale + rotYZ * scale, partialPosY + rotXZ * scale, partialPosZ + rotZ * scale + rotXY * scale, 0, 0);
        tess.addVertexWithUV(partialPosX + rotX * scale - rotYZ * scale, partialPosY - rotXZ * scale, partialPosZ + rotZ * scale - rotXY * scale, 0, 1);

        tess.draw();
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDepthMask(false);
        GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
    }

    @Override
    public int getFXLayer() {
        return 3;
    }

    @Override
    public void onUpdate() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;

        // Age-based behavior: Fade out near the end of lifetime
        if (this.particleAge++ >= this.particleMaxAge - 40) {
            this.alphaFading = true;
        }

        if (this.particleAge < this.particleMaxAge && this.particleAlpha > 0.0F) {
            // Minimal horizontal drift for slight randomness
            float horizontalMotionScale = 0.001F; // Very small horizontal movement
            this.motionX += this.rand.nextFloat() * horizontalMotionScale * (this.rand.nextBoolean() ? 1 : -1);
            this.motionZ += this.rand.nextFloat() * horizontalMotionScale * (this.rand.nextBoolean() ? 1 : -1);

            // Move the particle
            this.moveEntity(this.motionX, this.motionY, this.motionZ);

            // Fade out alpha if needed
            if (this.alphaFading) {
                this.particleAlpha = MathHelper.clamp_float(this.particleAlpha + this.alphaFadePerTick, 0.0F, 1.0F);
            }
        } else {
            // Kill the particle if it has faded out or reached max age
            this.setDead();
        }
    }


    /**
     * Allows you to change various aspects of campfire smoke particles:<br>
     * - {@link #particleRed}, {@link #particleGreen}, and {@link #particleBlue} are the float r, g, b values of the smoke. <br>
     * - {@link #motionX}, {@link #motionY}, and {@link #motionZ} are the x, y, z motion of the smoke.<br>
     * - {@link #particleGravity} is the amount the smoke's y motion changes per tick. <br>
     * - {@link #particleAlpha} is the smoke's initial alpha. <br>
     * - {@link #alphaFading} is whether the smoke's alpha should currently be changing (fading in or out). <br>
     * - {@link #alphaFadePerTick} is the amount the smoke's alpha changes per tick if {@link #alphaFading} is true. Negative values make it fade out, positive values make it fade
     * back in. <br>
     * - {@link #particleMaxAge} is the maximum number of ticks the particle will live for. <br>
     * <br>
     * You're also given {@link #campfirePosition}, which is the x, y, z position of the campfire tile entity where the smoke was created. <br>
     * <br>
     * Also, {@link EntityBigSmokeFX} are only created on the client side, so you should mark your subscribe event with {@link Side#CLIENT}. <br>
     * <br>
     * This event is posted from {@link EntityBigSmokeFX#EntityBigSmokeFX} on the {@link MinecraftForge#EVENT_BUS}.<br>
     * This event is {@link Cancelable}. If canceled, the entity will be removed.<br>
     */
    @Cancelable
    public static class EntityBigSmokeFXConstructingEvent extends EntityConstructing {
        public final int[] campfirePosition;

        public float particleRed;
        public float particleGreen;
        public float particleBlue;
        public double motionX;
        public double motionY;
        public double motionZ;
        public float particleGravity;
        public float particleAlpha;
        public boolean alphaFading;
        public float alphaFadePerTick;
        public int particleMaxAge;

        public EntityBigSmokeFXConstructingEvent(EntityBigSmokeFX entity, int x, int y, int z) {
            super(entity);

            campfirePosition = new int[]{x, y, z};

            particleRed = entity.particleRed;
            particleGreen = entity.particleGreen;
            particleBlue = entity.particleBlue;
            motionX = entity.motionX;
            motionY = entity.motionY;
            motionZ = entity.motionZ;
            particleGravity = entity.particleGravity;
            particleAlpha = entity.particleAlpha;
            alphaFading = entity.alphaFading;
            alphaFadePerTick = entity.alphaFadePerTick;
            particleMaxAge = entity.particleMaxAge;
        }

    }

}
