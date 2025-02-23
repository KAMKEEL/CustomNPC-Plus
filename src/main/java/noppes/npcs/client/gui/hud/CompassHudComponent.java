package noppes.npcs.client.gui.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.MathHelper;
import noppes.npcs.config.ConfigClient;
import org.lwjgl.opengl.GL11;
import java.util.ArrayList;
import java.util.List;

public class CompassHudComponent extends HudComponent {
    private final Minecraft mc;
    private final List<TargetEntry> targets = new ArrayList<>();
    private final int BAR_HEIGHT = 20;
    private final int BASE_ICON_SIZE = 8;
    private final int MAX_ICON_SIZE = 16;
    private final int SCALE_DISTANCE_MIN = 10;
    private final int SCALE_DISTANCE_MAX = 40;
    private static final int HANDLE_SIZE = 10;

    public static class TargetEntry {
        public int x, z;
        public int color;

        public TargetEntry(int x, int z, int color) {
            this.x = x;
            this.z = z;
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
        targets.clear();
        NBTTagList list = compound.getTagList("Targets", 10);
        for(int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound entry = list.getCompoundTagAt(i);
            targets.add(new TargetEntry(
                entry.getInteger("x"),
                entry.getInteger("z"),
                entry.getInteger("color")
            ));
        }
        hasData = !targets.isEmpty();
    }

    @Override
    public void load() {
        posX = ConfigClient.CompassOverlayX;
        posY = ConfigClient.CompassOverlayY;
        scale = ConfigClient.CompassOverlayScale;
        overlayWidth = ConfigClient.CompassOverlayWidth;
    }

    @Override
    public void save() {
        // Save to config
        ConfigClient.CompassOverlayX = posX;
        ConfigClient.CompassOverlayY = posY;
        ConfigClient.CompassOverlayScale = scale;
        ConfigClient.CompassOverlayWidth = overlayWidth;
        ConfigClient.config.save();
    }

    @Override
    public void renderOnScreen(float partialTicks) {
        if (!hasData || isEditting) return;

        ScaledResolution res = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        int actualX = (int)((float) posX / 100F * res.getScaledWidth());
        int actualY = (int)((float) posY / 100F * res.getScaledHeight());
        float scaleFactor = scale / 100.0F;

        GL11.glPushMatrix();
        GL11.glTranslatef(actualX, actualY, 0);
        GL11.glScalef(scaleFactor, scaleFactor, scaleFactor);

        // Draw background bar
        drawRect(0, 0, overlayWidth, BAR_HEIGHT, 0x80000000);

        // Render all targets
        for(TargetEntry target : targets) {
            renderTargetIcon(target);
        }

        GL11.glPopMatrix();
    }

    private void renderTargetIcon(TargetEntry target) {
        if (mc.thePlayer == null) return;

        // Calculate position on bar
        float iconPos = calculateIconPosition(target.x, target.z);
        int barWidth = overlayWidth;

        // Calculate dynamic size
        float distance = (float) Math.sqrt(
            Math.pow(target.x - mc.thePlayer.posX, 2) +
                Math.pow(target.z - mc.thePlayer.posZ, 2)
        );

        int iconSize = calculateIconSize(distance);
        int iconX = MathHelper.clamp_int((int)iconPos, iconSize/2, barWidth - iconSize/2);

        // Calculate vertical position
        int iconY = (BAR_HEIGHT - iconSize)/2;

        // Draw icon
        drawRect(iconX - iconSize/2, iconY,
            iconX + iconSize/2, iconY + iconSize, target.color);
    }

    private float calculateIconPosition(double targetX, double targetZ) {
        double dx = targetX - mc.thePlayer.posX;
        double dz = targetZ - mc.thePlayer.posZ;

        double angle = Math.toDegrees(Math.atan2(dz, dx)) - 90;
        angle = (angle + 360) % 360;

        double playerYaw = (mc.thePlayer.rotationYaw % 360 + 360) % 360;
        double relativeAngle = (angle - playerYaw + 360) % 360;

        return (float)(relativeAngle / 360.0 * overlayWidth);
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
        int actualX = (int)((float) posX / 100F * res.getScaledWidth());
        int actualY = (int)((float) posY / 100F * res.getScaledHeight());
        float scaleFactor = scale / 100.0F;

        GL11.glPushMatrix();
        GL11.glTranslatef(actualX, actualY, 0);
        GL11.glScalef(scaleFactor, scaleFactor, scaleFactor);

        // Draw editor background and handles
        drawRect(0, 0, overlayWidth, overlayHeight, 0x80000000);
        drawRectOutline(0, 0, overlayWidth, overlayHeight, 0xFF00FF00);

        // Add resize handle (bottom-right corner)
        drawRect(overlayWidth - HANDLE_SIZE, overlayHeight - HANDLE_SIZE,
            overlayWidth, overlayHeight, 0xFFCCCCCC);

        // Demo targets
        renderDemoIcon(overlayWidth * 0.25f, 0xFF0000FF);
        renderDemoIcon(overlayWidth * 0.75f, 0xFF00FF00);

        GL11.glPopMatrix();
    }

    private void renderDemoIcon(float position, int color) {
        // Animate size
        float pulse = (System.currentTimeMillis() % 1000) / 1000f;
        int size = (int)(BASE_ICON_SIZE + (MAX_ICON_SIZE - BASE_ICON_SIZE) * pulse);

        int iconX = (int)position - size/2;
        int iconY = (BAR_HEIGHT - size)/2;
        drawRect(iconX, iconY, iconX + size, iconY + size, color);
    }

    // Management methods
    public void addTarget(int x, int z, int color) {
        targets.add(new TargetEntry(x, z, color));
        hasData = true;
    }

    public void removeTarget(int index) {
        if(index >= 0 && index < targets.size()) {
            targets.remove(index);
            hasData = !targets.isEmpty();
        }
    }

    public void clearTargets() {
        targets.clear();
        hasData = false;
    }

    // Helper drawing methods (same as in QuestTrackingComponent)
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
