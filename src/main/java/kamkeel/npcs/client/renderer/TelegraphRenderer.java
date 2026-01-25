package kamkeel.npcs.client.renderer;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import kamkeel.npcs.controllers.data.telegraph.Telegraph;
import kamkeel.npcs.controllers.data.telegraph.TelegraphInstance;
import kamkeel.npcs.controllers.data.telegraph.TelegraphManager;
import kamkeel.npcs.controllers.data.telegraph.TelegraphType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import org.lwjgl.opengl.GL11;

/**
 * Renders telegraph warning shapes in the world.
 * Register this as an event handler on the Forge event bus.
 */
public class TelegraphRenderer {

    public static TelegraphRenderer Instance;

    private static final int CIRCLE_SEGMENTS = 32;
    private static final int CONE_SEGMENTS = 16;

    public TelegraphRenderer() {
        Instance = this;
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        if (TelegraphManager.ClientInstance == null || !TelegraphManager.ClientInstance.hasTelegraphs()) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = mc.thePlayer;
        if (player == null) return;

        float partialTicks = event.partialTicks;

        // Get player interpolated position for camera offset
        double playerX = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks;
        double playerY = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks;
        double playerZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks;

        // Setup GL state for transparent rendering
        GL11.glPushMatrix();
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDepthMask(false);

        // Render each telegraph
        for (TelegraphInstance instance : TelegraphManager.ClientInstance.getTelegraphs()) {
            renderTelegraph(instance, playerX, playerY, playerZ, partialTicks);
        }

        // Restore GL state
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        GL11.glPopAttrib();
        GL11.glPopMatrix();
    }

    private void renderTelegraph(TelegraphInstance instance, double playerX, double playerY, double playerZ, float partialTicks) {
        Telegraph telegraph = instance.getTelegraph();
        if (telegraph == null || telegraph.getType() == TelegraphType.NONE) {
            return;
        }

        // Calculate render position relative to camera
        double renderX = instance.getX() - playerX;
        double renderY = instance.getY() - playerY + telegraph.getHeightOffset();
        double renderZ = instance.getZ() - playerZ;

        // Get animated color
        int color = instance.getAnimatedColor(partialTicks);
        float alpha = ((color >> 24) & 0xFF) / 255.0f;
        float red = ((color >> 16) & 0xFF) / 255.0f;
        float green = ((color >> 8) & 0xFF) / 255.0f;
        float blue = (color & 0xFF) / 255.0f;

        GL11.glPushMatrix();
        GL11.glTranslated(renderX, renderY, renderZ);

        // Rotate for directional shapes
        GL11.glRotatef(-instance.getYaw(), 0, 1, 0);

        switch (telegraph.getType()) {
            case CIRCLE:
                renderCircle(telegraph.getRadius(), red, green, blue, alpha);
                break;
            case RING:
                renderRing(telegraph.getRadius(), telegraph.getInnerRadius(), red, green, blue, alpha);
                break;
            case LINE:
                renderLine(telegraph.getLength(), telegraph.getWidth(), red, green, blue, alpha);
                break;
            case CONE:
                renderCone(telegraph.getLength(), telegraph.getAngle(), red, green, blue, alpha);
                break;
            case POINT:
                renderPoint(red, green, blue, alpha);
                break;
            default:
                break;
        }

        GL11.glPopMatrix();
    }

    private void renderCircle(float radius, float r, float g, float b, float a) {
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawing(GL11.GL_TRIANGLE_FAN);
        tessellator.setColorRGBA_F(r, g, b, a);

        // Center point
        tessellator.addVertex(0, 0, 0);

        // Circle points
        for (int i = 0; i <= CIRCLE_SEGMENTS; i++) {
            double angle = (Math.PI * 2 * i) / CIRCLE_SEGMENTS;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            tessellator.addVertex(x, 0, z);
        }

        tessellator.draw();

        // Draw border
        renderCircleBorder(radius, r, g, b, Math.min(1.0f, a * 2));
    }

    private void renderCircleBorder(float radius, float r, float g, float b, float a) {
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawing(GL11.GL_LINE_LOOP);
        tessellator.setColorRGBA_F(r, g, b, a);

        for (int i = 0; i < CIRCLE_SEGMENTS; i++) {
            double angle = (Math.PI * 2 * i) / CIRCLE_SEGMENTS;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            tessellator.addVertex(x, 0.01, z);
        }

        tessellator.draw();
    }

    private void renderRing(float outerRadius, float innerRadius, float r, float g, float b, float a) {
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawing(GL11.GL_TRIANGLE_STRIP);
        tessellator.setColorRGBA_F(r, g, b, a);

        for (int i = 0; i <= CIRCLE_SEGMENTS; i++) {
            double angle = (Math.PI * 2 * i) / CIRCLE_SEGMENTS;
            double cosA = Math.cos(angle);
            double sinA = Math.sin(angle);

            // Outer point
            tessellator.addVertex(cosA * outerRadius, 0, sinA * outerRadius);
            // Inner point
            tessellator.addVertex(cosA * innerRadius, 0, sinA * innerRadius);
        }

        tessellator.draw();

        // Draw borders
        renderCircleBorder(outerRadius, r, g, b, Math.min(1.0f, a * 2));
        renderCircleBorder(innerRadius, r, g, b, Math.min(1.0f, a * 2));
    }

    private void renderLine(float length, float width, float r, float g, float b, float a) {
        float halfWidth = width / 2;

        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.setColorRGBA_F(r, g, b, a);

        // Rectangle from origin forward
        tessellator.addVertex(-halfWidth, 0, 0);
        tessellator.addVertex(halfWidth, 0, 0);
        tessellator.addVertex(halfWidth, 0, length);
        tessellator.addVertex(-halfWidth, 0, length);

        tessellator.draw();

        // Draw border
        tessellator.startDrawing(GL11.GL_LINE_LOOP);
        tessellator.setColorRGBA_F(r, g, b, Math.min(1.0f, a * 2));

        tessellator.addVertex(-halfWidth, 0.01, 0);
        tessellator.addVertex(halfWidth, 0.01, 0);
        tessellator.addVertex(halfWidth, 0.01, length);
        tessellator.addVertex(-halfWidth, 0.01, length);

        tessellator.draw();
    }

    private void renderCone(float length, float angle, float r, float g, float b, float a) {
        float halfAngleRad = (float) Math.toRadians(angle / 2);

        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawing(GL11.GL_TRIANGLE_FAN);
        tessellator.setColorRGBA_F(r, g, b, a);

        // Origin point
        tessellator.addVertex(0, 0, 0);

        // Arc points
        for (int i = 0; i <= CONE_SEGMENTS; i++) {
            float segmentAngle = -halfAngleRad + (halfAngleRad * 2 * i) / CONE_SEGMENTS;
            double x = Math.sin(segmentAngle) * length;
            double z = Math.cos(segmentAngle) * length;
            tessellator.addVertex(x, 0, z);
        }

        tessellator.draw();

        // Draw border
        tessellator.startDrawing(GL11.GL_LINE_STRIP);
        tessellator.setColorRGBA_F(r, g, b, Math.min(1.0f, a * 2));

        tessellator.addVertex(0, 0.01, 0);

        for (int i = 0; i <= CONE_SEGMENTS; i++) {
            float segmentAngle = -halfAngleRad + (halfAngleRad * 2 * i) / CONE_SEGMENTS;
            double x = Math.sin(segmentAngle) * length;
            double z = Math.cos(segmentAngle) * length;
            tessellator.addVertex(x, 0.01, z);
        }

        tessellator.addVertex(0, 0.01, 0);

        tessellator.draw();
    }

    private void renderPoint(float r, float g, float b, float a) {
        float size = 0.5f;

        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.setColorRGBA_F(r, g, b, a);

        // Diamond shape
        tessellator.addVertex(0, 0, -size);
        tessellator.addVertex(size, 0, 0);
        tessellator.addVertex(0, 0, size);
        tessellator.addVertex(-size, 0, 0);

        tessellator.draw();
    }
}
