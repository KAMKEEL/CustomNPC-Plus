package kamkeel.npcs.client.renderer.lightning;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.client.renderer.lightning.LightningBolt.Segment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import org.lwjgl.opengl.GL11;

import java.awt.Color;

/**
 * Handles rendering of lightning bolts in the world.
 * Adapted from Botania's LightningHandler by Vazkii/ChickenBones.
 *
 * Register this handler with: MinecraftForge.EVENT_BUS.register(new LightningHandler());
 */
@SideOnly(Side.CLIENT)
public class LightningHandler {

    private static final ResourceLocation TEXTURE_OUTER = new ResourceLocation("customnpcs", "textures/effects/lightning_outer.png");
    private static final ResourceLocation TEXTURE_INNER = new ResourceLocation("customnpcs", "textures/effects/lightning_inner.png");

    public static int lightningCount = 0;

    private static Vec3d getRelativeViewVector(Vec3d pos) {
        Entity renderEntity = Minecraft.getMinecraft().renderViewEntity;
        return new Vec3d(
            (float) renderEntity.posX - pos.x,
            (float) renderEntity.posY + renderEntity.getEyeHeight() - pos.y,
            (float) renderEntity.posZ - pos.z
        );
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        float partialTicks = event.partialTicks;
        Entity entity = Minecraft.getMinecraft().thePlayer;
        TextureManager textureManager = Minecraft.getMinecraft().renderEngine;

        double interpPosX = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks;
        double interpPosY = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks;
        double interpPosZ = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks;

        if (LightningBolt.boltList.isEmpty()) {
            return;
        }

        GL11.glPushMatrix();
        GL11.glTranslated(-interpPosX, -interpPosY, -interpPosZ);

        Tessellator tessellator = Tessellator.instance;

        GL11.glDepthMask(false);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_CULL_FACE);

        lightningCount = 0;

        // Render outer layer (translucent glow)
        for (LightningBolt bolt : LightningBolt.boltList) {
            renderBolt(bolt, tessellator, partialTicks, false);
        }

        // Render inner layer (bright core)
        for (LightningBolt bolt : LightningBolt.boltList) {
            renderBolt(bolt, tessellator, partialTicks, true);
        }

        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDepthMask(true);

        GL11.glPopMatrix();
    }

    private void renderBolt(LightningBolt bolt, Tessellator tessellator, float partialTicks, boolean inner) {
        lightningCount++;

        float boltAge = bolt.particleAge < 0 ? 0 : (float) bolt.particleAge / (float) bolt.particleMaxAge;
        float mainAlpha;
        if (!inner) {
            mainAlpha = (1 - boltAge) * 0.4F;
        } else {
            mainAlpha = 1 - boltAge * 0.5F;
        }

        int expandTime = (int) (bolt.length * bolt.speed);

        int renderStart = (int) ((expandTime / 2 - bolt.particleMaxAge + bolt.particleAge) / (float) (expandTime / 2) * bolt.numSegments0);
        int renderEnd = (int) ((bolt.particleAge + expandTime) / (float) expandTime * bolt.numSegments0);

        for (Segment segment : bolt.segments) {
            if (segment.segmentNo < renderStart || segment.segmentNo > renderEnd) {
                continue;
            }

            Vec3d playerVec = getRelativeViewVector(segment.startPoint.point).multiply(-1);

            double width = 0.008F * (playerVec.mag() / 8 + 1) * (1 + segment.light) * 0.4F;

            Vec3d diff1 = playerVec.copy().crossProduct(segment.prevDiff).normalize().multiply(width / segment.sinPrev);
            Vec3d diff2 = playerVec.copy().crossProduct(segment.nextDiff).normalize().multiply(width / segment.sinNext);

            Vec3d startVec = segment.startPoint.point;
            Vec3d endVec = segment.endPoint.point;

            int color = inner ? bolt.colorInner : bolt.colorOuter;
            Color c = new Color(color);
            int alpha = (int) (mainAlpha * segment.light * c.getAlpha());

            tessellator.startDrawingQuads();
            tessellator.setBrightness(0xF000F0);
            tessellator.setColorRGBA(c.getRed(), c.getGreen(), c.getBlue(), alpha);

            tessellator.addVertex(endVec.x - diff2.x, endVec.y - diff2.y, endVec.z - diff2.z);
            tessellator.addVertex(startVec.x - diff1.x, startVec.y - diff1.y, startVec.z - diff1.z);
            tessellator.addVertex(startVec.x + diff1.x, startVec.y + diff1.y, startVec.z + diff1.z);
            tessellator.addVertex(endVec.x + diff2.x, endVec.y + diff2.y, endVec.z + diff2.z);

            tessellator.draw();

            // Rounded end caps
            if (segment.next == null) {
                Vec3d roundEnd = segment.endPoint.point.copy().add(segment.diff.copy().normalize().multiply(width));

                tessellator.startDrawingQuads();
                tessellator.setBrightness(0xF000F0);
                tessellator.setColorRGBA(c.getRed(), c.getGreen(), c.getBlue(), alpha);

                tessellator.addVertex(roundEnd.x - diff2.x, roundEnd.y - diff2.y, roundEnd.z - diff2.z);
                tessellator.addVertex(endVec.x - diff2.x, endVec.y - diff2.y, endVec.z - diff2.z);
                tessellator.addVertex(endVec.x + diff2.x, endVec.y + diff2.y, endVec.z + diff2.z);
                tessellator.addVertex(roundEnd.x + diff2.x, roundEnd.y + diff2.y, roundEnd.z + diff2.z);

                tessellator.draw();
            }

            if (segment.prev == null) {
                Vec3d roundEnd = segment.startPoint.point.copy().subtract(segment.diff.copy().normalize().multiply(width));

                tessellator.startDrawingQuads();
                tessellator.setBrightness(0xF000F0);
                tessellator.setColorRGBA(c.getRed(), c.getGreen(), c.getBlue(), alpha);

                tessellator.addVertex(startVec.x - diff1.x, startVec.y - diff1.y, startVec.z - diff1.z);
                tessellator.addVertex(roundEnd.x - diff1.x, roundEnd.y - diff1.y, roundEnd.z - diff1.z);
                tessellator.addVertex(roundEnd.x + diff1.x, roundEnd.y + diff1.y, roundEnd.z + diff1.z);
                tessellator.addVertex(startVec.x + diff1.x, startVec.y + diff1.y, startVec.z + diff1.z);

                tessellator.draw();
            }
        }
    }

    /**
     * Spawn a lightning bolt between two points.
     *
     * @param world        The world
     * @param start        Start position
     * @param end          End position
     * @param ticksPerMeter Speed of bolt expansion
     * @param seed         Random seed for consistent appearance
     * @param colorOuter   Outer glow color (ARGB)
     * @param colorInner   Inner core color (ARGB)
     */
    public static void spawnLightningBolt(World world, Vec3d start, Vec3d end, float ticksPerMeter, long seed, int colorOuter, int colorInner) {
        LightningBolt bolt = new LightningBolt(world, start, end, ticksPerMeter, seed, colorOuter, colorInner);
        bolt.defaultFractal();
        bolt.finalizeBolt();
        LightningBolt.boltList.add(bolt);
    }

    /**
     * Spawn a lightning bolt with automatic seed.
     */
    public static void spawnLightningBolt(World world, Vec3d start, Vec3d end, float ticksPerMeter, int colorOuter, int colorInner) {
        spawnLightningBolt(world, start, end, ticksPerMeter, System.nanoTime(), colorOuter, colorInner);
    }

    /**
     * Spawn a simple lightning bolt for projectiles - less detail, faster.
     */
    public static void spawnSimpleLightningBolt(World world, Vec3d start, Vec3d end, float ticksPerMeter, int colorOuter, int colorInner) {
        LightningBolt bolt = new LightningBolt(world, start, end, ticksPerMeter, System.nanoTime(), colorOuter, colorInner);
        bolt.simpleFractal();
        bolt.finalizeBolt();
        LightningBolt.boltList.add(bolt);
    }
}
