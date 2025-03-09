package noppes.npcs.client.gui.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.config.ConfigClient;
import noppes.npcs.constants.MarkType;
import org.lwjgl.opengl.GL11;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CompassHudComponent extends HudComponent {
    private final Minecraft mc;
    private final List<MarkTargetEntry> markTargets = new ArrayList<>();
    private final int BAR_HEIGHT = 20;
    private final int BASE_ICON_SIZE = 8;
    private final int MAX_ICON_SIZE = 16;
    private final int SCALE_DISTANCE_MIN = 10;
    private final int SCALE_DISTANCE_MAX = 40;
    private static final int HANDLE_SIZE = 10;
    private static final int WIDTH_BAR_SIZE = 6; // Width adjust bar width
    private final int SCAN_RANGE = 128;

    // Flag to indicate if the width bar is being dragged in editing mode.
    public boolean resizingWidth = false;

    public static class MarkTargetEntry {
        public double x, z;
        public int type;
        public int color;

        public MarkTargetEntry(double x, double z, int type, int color) {
            this.x = x;
            this.z = z;
            this.type = type;
            this.color = color;
        }
    }

    public CompassHudComponent(Minecraft minecraft) {
        mc = minecraft;
        overlayWidth = 200;
        overlayHeight = BAR_HEIGHT;
        load();
    }

    @Override
    public void loadData(NBTTagCompound compound) {
        markTargets.clear();
        NBTTagList list = compound.getTagList("MarkTargets", 10);
        for(int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound entry = list.getCompoundTagAt(i);
            markTargets.add(new MarkTargetEntry(
                entry.getInteger("x"),
                entry.getInteger("z"),
                entry.getInteger("type"),
                entry.getInteger("color")
            ));
        }
        hasData = !markTargets.isEmpty();
    }

    @Override
    public void load() {
        enabled = ConfigClient.CompassEnabled;
        posX = ConfigClient.CompassOverlayX;
        posY = ConfigClient.CompassOverlayY;
        scale = ConfigClient.CompassOverlayScale;
        overlayWidth = ConfigClient.CompassOverlayWidth;
    }

    @Override
    public void save() {
        ConfigClient.CompassEnabled = enabled;
        ConfigClient.CompassEnabledProperty.set(ConfigClient.CompassEnabled);

        ConfigClient.CompassOverlayX = posX;
        ConfigClient.CompassOverlayXProperty.set(ConfigClient.CompassOverlayX);

        ConfigClient.CompassOverlayY = posY;
        ConfigClient.CompassOverlayYProperty.set(ConfigClient.CompassOverlayY);

        ConfigClient.CompassOverlayScale = scale;
        ConfigClient.CompassOverlayScaleProperty.set(ConfigClient.CompassOverlayScale);

        ConfigClient.CompassOverlayWidth = overlayWidth;
        ConfigClient.CompassOverlayWidthProperty.set(ConfigClient.CompassOverlayWidth);

        if(ConfigClient.config.hasChanged())
            ConfigClient.config.save();
    }

    @Override
    public void renderOnScreen(float partialTicks) {
        if (!hasData || isEditting) return;

        ScaledResolution res = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        int actualX = (int)(posX / 100F * res.getScaledWidth());
        int actualY = (int)(posY / 100F * res.getScaledHeight());
        float effectiveScale = getEffectiveScale(res);

        // Calculate interpolated player position and rotation.
        double interpPosX = mc.thePlayer.lastTickPosX + (mc.thePlayer.posX - mc.thePlayer.lastTickPosX) * partialTicks;
        double interpPosY = mc.thePlayer.lastTickPosY + (mc.thePlayer.posY - mc.thePlayer.lastTickPosY) * partialTicks;
        double interpPosZ = mc.thePlayer.lastTickPosZ + (mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ) * partialTicks;
        float interpYaw = mc.thePlayer.prevRotationYaw + (mc.thePlayer.rotationYaw - mc.thePlayer.prevRotationYaw) * partialTicks;

        GL11.glPushMatrix();
        GL11.glTranslatef(actualX, actualY, 0);
        GL11.glScalef(effectiveScale, effectiveScale, effectiveScale);

        // Draw background bar.
        drawRect(0, 0, overlayWidth, BAR_HEIGHT, 0x80000000);

        // Sort marks using interpolated player position.
        markTargets.sort(Comparator.comparingDouble((MarkTargetEntry mark) ->
            (interpPosX - mark.x) * (interpPosX - mark.x) + (interpPosZ - mark.z) * (interpPosZ - mark.z)
        ).reversed());

        // Render all marks using the interpolated values.
        for (MarkTargetEntry mark : markTargets) {
            renderMarkIcon(mark, interpPosX, interpPosY, interpPosZ, interpYaw);
        }

        GL11.glPopMatrix();
    }

    private void renderMarkIcon(MarkTargetEntry mark, double playerX, double playerY, double playerZ, float playerYaw) {
        if (mc.thePlayer == null) return;

        Float iconPos = calculateIconPosition(mark.x, mark.z, playerX, playerZ, playerYaw);
        if (iconPos == null) {
            // Target is behind the player; don't render.
            return;
        }

        int barWidth = overlayWidth;
        float distance = (float) Math.sqrt(
            Math.pow(mark.x - playerX, 2) +
                Math.pow(mark.z - playerZ, 2)
        );

        int iconSize = calculateIconSize(distance);
        int iconX = MathHelper.clamp_int((int) iconPos.floatValue(), iconSize / 2, barWidth - iconSize / 2);
        int iconY = (BAR_HEIGHT - iconSize) / 2;

        ResourceLocation texture = getTextureForMark(mark.type);
        if (texture != null) {
            renderTextureIcon(iconX - iconSize / 2, iconY, iconSize, texture, mark.color);
        }
    }

    private ResourceLocation getTextureForMark(int type) {
        switch(type) {
            case MarkType.EXCLAMATION: return new ResourceLocation("customnpcs", "textures/marks/exclamation.png");
            case MarkType.QUESTION: return new ResourceLocation("customnpcs", "textures/marks/question.png");
            case MarkType.POINTER: return new ResourceLocation("customnpcs", "textures/marks/pointer.png");
            case MarkType.CROSS: return new ResourceLocation("customnpcs", "textures/marks/cross.png");
            case MarkType.SKULL: return new ResourceLocation("customnpcs", "textures/marks/skull.png");
            case MarkType.STAR: return new ResourceLocation("customnpcs", "textures/marks/star.png");
            default: return null;
        }
    }

    private void renderTextureIcon(int x, int y, int size, ResourceLocation texture, int color) {
        float red = (color >> 16 & 255) / 255.0F;
        float green = (color >> 8 & 255) / 255.0F;
        float blue = (color & 255) / 255.0F;

        GL11.glPushMatrix();
        GL11.glColor4f(red, green, blue, 1);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        mc.getTextureManager().bindTexture(texture);

        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(x, y + size, 0, 0, 1);
        tessellator.addVertexWithUV(x + size, y + size, 0, 1, 1);
        tessellator.addVertexWithUV(x + size, y, 0, 1, 0);
        tessellator.addVertexWithUV(x, y, 0, 0, 0);
        tessellator.draw();

        GL11.glColor4f(1, 1, 1, 1);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }

    private Float calculateIconPosition(double targetX, double targetZ, double playerX, double playerZ, float playerYaw) {
        double dx = targetX - playerX;
        double dz = targetZ - playerZ;

        double angleToTarget = Math.toDegrees(Math.atan2(dz, dx));
        double adjustedPlayerYaw = playerYaw + 90;
        double relativeAngle = angleToTarget - adjustedPlayerYaw;
        while (relativeAngle < -180) relativeAngle += 360;
        while (relativeAngle > 180) relativeAngle -= 360;

        // Only render if the target is within 90° to either side (i.e., 180° front)
        if (Math.abs(relativeAngle) > 90) {
            return null;
        }

        // Map -90..90 to 0..overlayWidth so that 0 becomes the left edge and 90 the right edge.
        return (float) (overlayWidth / 2.0 + (relativeAngle / 90.0) * (overlayWidth / 2.0));
    }

    private int calculateIconSize(float distance) {
        if(distance <= SCALE_DISTANCE_MIN) return MAX_ICON_SIZE;
        if(distance >= SCALE_DISTANCE_MAX) return BASE_ICON_SIZE;

        float t = (distance - SCALE_DISTANCE_MIN) /
            (SCALE_DISTANCE_MAX - SCALE_DISTANCE_MIN);
        return (int)(MAX_ICON_SIZE - (MAX_ICON_SIZE - BASE_ICON_SIZE) * t);
    }

    @Override
    public void renderEditing() {
        isEditting = true;
        ScaledResolution res = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        int actualX = (int)(posX / 100F * res.getScaledWidth());
        int actualY = (int)(posY / 100F * res.getScaledHeight());
        float effectiveScale = getEffectiveScale(res);

        GL11.glPushMatrix();
        GL11.glTranslatef(actualX, actualY, 0);
        GL11.glScalef(effectiveScale, effectiveScale, effectiveScale);

        // Draw background bar and border.
        drawRect(0, 0, overlayWidth, overlayHeight, 0x80000000);
        drawRectOutline(0, 0, overlayWidth, overlayHeight, 0xFF00FF00);
        // Resize handle (bottom-right for scale)
        drawRect(overlayWidth - HANDLE_SIZE, overlayHeight - HANDLE_SIZE,
            overlayWidth, overlayHeight, 0xFFCCCCCC);
        // Width adjust bar on right side (centered vertically)
        int margin = 5;
        int barX = overlayWidth + margin;
        int barY = (overlayHeight - BAR_HEIGHT) / 2;
        drawRect(barX, barY, barX + WIDTH_BAR_SIZE, barY + BAR_HEIGHT, 0xFF888888);

        // Demo marks for preview.
        renderDemoIcon(overlayWidth * 0.25f, MarkType.EXCLAMATION, 0xFF00FF00);
        renderDemoIcon(overlayWidth * 0.75f, MarkType.QUESTION, 0xFFFF0000);

        GL11.glPopMatrix();
    }

    private void renderDemoIcon(float position, int type, int color) {
        float pulse = (System.currentTimeMillis() % 1000) / 1000f;
        int size = (int)(BASE_ICON_SIZE + (MAX_ICON_SIZE - BASE_ICON_SIZE) * pulse);
        ResourceLocation texture = getTextureForMark(type);

        if(texture != null) {
            GL11.glPushMatrix();
            GL11.glTranslatef(position - size/2, (BAR_HEIGHT - size)/2, 0);
            renderTextureIcon(0, 0, size, texture, color);
            GL11.glPopMatrix();
        }
    }

    /**
     * Updates the mark targets directly.
     * (This method assumes external code (CNPC+ mark data) supplies the new marks
     * without having to loop through all loaded entities.)
     */
    public void updateMarkTargets(List<MarkTargetEntry> newMarks) {
        markTargets.clear();
        for (MarkTargetEntry mark : newMarks) {
            // Center the mark on the block
            markTargets.add(new MarkTargetEntry(
                mark.x,
                mark.z,
                mark.type,
                mark.color
            ));
        }
        hasData = !markTargets.isEmpty();
    }

    // Helper drawing methods
    private void drawRect(int left, int top, int right, int bottom, int color) {
        net.minecraft.client.gui.Gui.drawRect(left, top, right, bottom, color);
    }

    private void drawRectOutline(int left, int top, int right, int bottom, int color) {
        drawHorizontalLine(left, right, top, color);
        drawHorizontalLine(left, right, bottom - 1, color);
        net.minecraft.client.gui.Gui.drawRect(left, top, left + 1, bottom, color);
        net.minecraft.client.gui.Gui.drawRect(right - 1, top, right, bottom, color);
    }

    private void drawHorizontalLine(int left, int right, int y, int color) {
        net.minecraft.client.gui.Gui.drawRect(left, y, right, y + 1, color);
    }
}
