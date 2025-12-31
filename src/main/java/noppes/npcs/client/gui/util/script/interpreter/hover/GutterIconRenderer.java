package noppes.npcs.client.gui.util.script.interpreter.hover;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.client.ClientProxy;
import noppes.npcs.client.gui.util.GuiUtil;
import noppes.npcs.client.gui.util.script.interpreter.method.MethodInfo;
import noppes.npcs.client.gui.util.script.interpreter.token.TokenType;
import noppes.npcs.client.gui.util.script.interpreter.type.TypeInfo;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

/**
 * Renders gutter icons for method inheritance (override/implements) in the script editor.
 *
 * Renders IntelliJ-style tooltips when hovering over gutter icons:
 * - Background + border matching TokenHoverRenderer style
 * - Colored declaration showing which class/interface the method overrides/implements
 * - Uses the same token highlighting system for type names
 */
public class GutterIconRenderer {

    // ==================== CONSTANTS ====================

    /** Texture resource for script icons (64x32: first 32x32 = override, second 32x32 = implements) */
    private static final ResourceLocation SCRIPT_ICONS = new ResourceLocation("customnpcs",
            "textures/gui/script/icons.png");

    /** Icon size when rendered in the gutter (scaled from 32x32) */
    private static final int GUTTER_ICON_SIZE = 10;

    /** Extra gutter width to accommodate inheritance icons */
    public static final int ICON_GUTTER_WIDTH = 12;

    /** Padding inside the tooltip box */
    private static final int PADDING = 6;

    /** Line spacing between rows */
    private static final int LINE_SPACING = 2;

    /** Vertical offset from mouse */
    private static final int VERTICAL_OFFSET = 10;

    // ==================== COLORS ====================

    /** Background color (dark gray like IntelliJ) */
    private static final int BG_COLOR = 0xF0313335;

    /** Border color */
    private static final int BORDER_COLOR = 0xFF3C3F41;

    /** Info text color */
    private static final int INFO_COLOR = 0xFFA9B7C6;

    // ==================== RENDERING ====================

    /**
     * Render gutter icons for a range of lines.
     *
     * @param lineHeight Height of each line
     * @param gutterX X position for the icon in the gutter
     * @param gutterY Base Y position of the gutter
     * @param renderStart First line index to render
     * @param renderEnd Last line index to render
     * @param scrolledLine Current scroll position
     * @param stringYOffset Y offset for text positioning
     * @param methods All methods in the document
     * @param lines Line data for position calculation
     * @param xMouse Mouse X position
     * @param yMouse Mouse Y position
     * @param fracPixels Fractional scroll offset
     * @return The hovered method, or null if no icon is hovered
     */
    public static MethodInfo renderIcons(
            int lineHeight,
            int gutterX,
            int gutterY,
            int renderStart,
            int renderEnd,
            int scrolledLine,
            int stringYOffset,
            List<MethodInfo> methods,
            List<?> lines,
            int xMouse,
            int yMouse,
            float fracPixels) {

        if (methods == null || methods.isEmpty())
            return null;

        MethodInfo hoveredMethod = null;
        float adjustedMouseY = yMouse + fracPixels;

        for (int lineIndex = renderStart; lineIndex <= renderEnd; lineIndex++) {
            MethodInfo method = getMethodAtLine(lineIndex, methods, lines);
            if (method == null || !method.hasInheritanceMarker())
                continue;

            int posY = gutterY + (lineIndex - scrolledLine) * lineHeight + stringYOffset;
            int iconY = posY + (lineHeight - GUTTER_ICON_SIZE) / 2 - 1;

            // Draw the icon
            renderIcon(gutterX, iconY, method.isOverride());

            // Check for hover
            int iconScaleOffsetX = -4;
            int screenPosY = gutterY + (lineIndex - scrolledLine) * lineHeight;
            if (xMouse >= gutterX + iconScaleOffsetX && xMouse < gutterX + GUTTER_ICON_SIZE + iconScaleOffsetX &&
                    adjustedMouseY >= screenPosY && adjustedMouseY < screenPosY + lineHeight) {
                hoveredMethod = method;
            }
        }

        return hoveredMethod;
    }

    /**
     * Render a single gutter icon.
     */
    private static void renderIcon(int x, int y, boolean isOverride) {
        int iconU = isOverride ? 0 : 32;

        Minecraft.getMinecraft().renderEngine.bindTexture(SCRIPT_ICONS);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

        GL11.glPushMatrix();
        float scale = 2.0f, scaleOffsetX = -4, scaleOffsetY = -3.25f;
        GL11.glScalef(scale, scale, scale);
        GL11.glTranslatef(scaleOffsetX, scaleOffsetY, 0);
        GuiUtil.drawScaledTexturedRect(
                (int) (x / scale), (int) (y / scale),
                iconU, 0, 32, 32,
                GUTTER_ICON_SIZE, GUTTER_ICON_SIZE,
                64, 32
        );
        GL11.glPopMatrix();
    }

    /**
     * Render the tooltip for a hovered gutter icon.
     */
    public static void renderTooltip(MethodInfo method, int mouseX, int mouseY, int viewportX, int viewportWidth,
                                     int viewportY, int viewportHeight) {
        if (method == null)
            return;

        // Build tooltip content
        List<TextSegment> segments = buildTooltipContent(method);
        if (segments.isEmpty())
            return;

        int lineHeight = ClientProxy.Font.height();

        // Calculate dimensions
        int contentWidth = calculateContentWidth(segments);
        int contentHeight = lineHeight;
        int boxWidth = contentWidth + PADDING * 2;
        int boxHeight = contentHeight + PADDING * 2;

        // Position tooltip
        int tooltipX = mouseX + VERTICAL_OFFSET;
        int tooltipY = mouseY - 5;

        // Clamp to viewport
        int rightBound = viewportX + viewportWidth;
        int bottomBound = viewportY + viewportHeight;

        if (tooltipX + boxWidth > rightBound) {
            tooltipX = mouseX - boxWidth - 5;
        }
        if (tooltipX < viewportX) {
            tooltipX = viewportX;
        }

        if (tooltipY + boxHeight > bottomBound) {
            tooltipY = bottomBound - boxHeight;
        }
        if (tooltipY < viewportY) {
            tooltipY = viewportY;
        }

        // Render tooltip box
        renderTooltipBox(tooltipX, tooltipY, boxWidth, boxHeight, segments);
    }

    // ==================== PRIVATE HELPERS ====================

    /**
     * Get the method that starts on the given line.
     */
    private static MethodInfo getMethodAtLine(int lineIndex, List<MethodInfo> methods, List<?> lines) {
        if (lineIndex < 0 || lineIndex >= lines.size())
            return null;

        Object lineObj = lines.get(lineIndex);
        int lineStart, lineEnd;

        try {
            java.lang.reflect.Field startField = lineObj.getClass().getField("start");
            java.lang.reflect.Field endField = lineObj.getClass().getField("end");
            lineStart = startField.getInt(lineObj);
            lineEnd = endField.getInt(lineObj);
        } catch (Exception e) {
            return null;
        }

        for (MethodInfo method : methods) {
            if (!method.hasInheritanceMarker())
                continue;
            int nameOffset = method.getNameOffset();
            if (nameOffset >= lineStart && nameOffset < lineEnd) {
                return method;
            }
        }

        return null;
    }

    /**
     * Build the tooltip content as colored text segments.
     */
    private static List<TextSegment> buildTooltipContent(MethodInfo method) {
        List<TextSegment> segments = new ArrayList<>();

        if (method.isOverride()) {
            TypeInfo overridesFrom = method.getOverridesFrom();
            segments.add(new TextSegment("Overrides method in ", INFO_COLOR));

            if (overridesFrom != null) {
                int color = TokenType.getColor(overridesFrom);
                segments.add(new TextSegment(overridesFrom.getSimpleName(), color));
            } else {
                segments.add(new TextSegment("parent class", INFO_COLOR));
            }
        } else if (method.isImplements()) {
            TypeInfo implementsFrom = method.getImplementsFrom();
            segments.add(new TextSegment("Implements method from ", INFO_COLOR));

            if (implementsFrom != null) {
                int color = TokenType.getColor(implementsFrom);
                segments.add(new TextSegment(implementsFrom.getSimpleName(), color));
            } else {
                segments.add(new TextSegment("interface", INFO_COLOR));
            }
        }

        return segments;
    }

    /**
     * Calculate the width needed for the segments.
     */
    private static int calculateContentWidth(List<TextSegment> segments) {
        int totalWidth = 0;
        for (TextSegment segment : segments) {
            totalWidth += ClientProxy.Font.width(segment.text);
        }
        return totalWidth;
    }

    /**
     * Render the tooltip box with background, border, and content.
     */
    private static void renderTooltipBox(int x, int y, int width, int height, List<TextSegment> segments) {
        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        // Draw background
        int paddedHeight = y + height - 4;
        Gui.drawRect(x, y, x + width, paddedHeight, BG_COLOR);

        // Draw border
        Gui.drawRect(x, y, x + width, y + 1, BORDER_COLOR); // Top
        Gui.drawRect(x, paddedHeight - 1, x + width, paddedHeight, BORDER_COLOR); // Bottom
        Gui.drawRect(x, y, x + 1, paddedHeight, BORDER_COLOR); // Left
        Gui.drawRect(x + width - 1, y, x + width, paddedHeight, BORDER_COLOR); // Right

        // Draw text segments
        int currentX = x + PADDING;
        int currentY = y + PADDING;

        for (TextSegment segment : segments) {
            ClientProxy.Font.drawString(segment.text, currentX, currentY, 0xFF000000 | segment.color);
            currentX += ClientProxy.Font.width(segment.text);
        }
    }

    // ==================== DATA CLASSES ====================

    /**
     * A colored text segment.
     */
    private static class TextSegment {
        final String text;
        final int color;

        TextSegment(String text, int color) {
            this.text = text;
            this.color = color;
        }
    }
}
