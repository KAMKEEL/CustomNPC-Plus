package noppes.npcs.client.gui.util.script.interpreter.hover;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import noppes.npcs.client.ClientProxy;
import org.lwjgl.opengl.GL11;

import java.util.List;

/**
 * Renders hover tooltips for tokens in the script editor.
 * 
 * Renders IntelliJ-style tooltips with:
 * - Package name in gray
 * - Icon indicator (C/I/E/m/f/v/p)
 * - Colored declaration line
 * - Documentation/Javadoc
 * - Error messages in red
 * 
 * Handles positioning to keep tooltip on screen.
 */
public class TokenHoverRenderer {

    // ==================== CONSTANTS ====================
    
    /** Padding inside the tooltip box */
    private static final int PADDING = 6;
    
    /** Gap between icon and text */
    private static final int ICON_GAP = 4;
    
    /** Icon box size */
    private static final int ICON_SIZE = 12;
    
    /** Line spacing */
    private static final int LINE_SPACING = 2;
    
    /** Vertical offset from token */
    private static final int VERTICAL_OFFSET = 4;
    
    /** Maximum tooltip width */
    private static final int MAX_WIDTH = 400;
    
    /** Minimum tooltip width */
    private static final int MIN_WIDTH = 150;

    // ==================== COLORS ====================
    
    /** Background color (dark gray like IntelliJ) */
    private static final int BG_COLOR = 0xF0313335;
    
    /** Border color */
    private static final int BORDER_COLOR = 0xFF3C3F41;
    
    /** Package text color */
    private static final int PACKAGE_COLOR = 0xFF808080;
    
    /** Error text color */
    private static final int ERROR_COLOR = 0xFFFF6B68;
    
    /** Info text color */
    private static final int INFO_COLOR = 0xFF808080;

    // ==================== ICON COLORS ====================
    
    private static final int ICON_CLASS_BG = 0xFF4A6B8A;      // Blue for classes
    private static final int ICON_INTERFACE_BG = 0xFF8A6B4A;  // Orange-brown for interfaces
    private static final int ICON_ENUM_BG = 0xFF6B8A4A;       // Green for enums
    private static final int ICON_METHOD_BG = 0xFF8A4A6B;     // Purple for methods
    private static final int ICON_FIELD_BG = 0xFF4A8A6B;      // Teal for fields
    private static final int ICON_VAR_BG = 0xFF6B6B8A;        // Blue-gray for variables
    private static final int ICON_PARAM_BG = 0xFF8A8A4A;      // Yellow for parameters
    private static final int ICON_UNKNOWN_BG = 0xFF8A4A4A;    // Red for unknown

    // ==================== RENDERING ====================

    /**
     * Render the hover tooltip.
     * 
     * @param hoverState The current hover state
     * @param screenWidth Screen width for positioning
     * @param screenHeight Screen height for positioning
     */
    public static void render(HoverState hoverState, int screenWidth, int screenHeight) {
        if (!hoverState.isTooltipVisible()) return;
        
        TokenHoverInfo info = hoverState.getHoverInfo();
        if (info == null || !info.hasContent()) return;
        
        // Calculate content dimensions
        int contentWidth = calculateContentWidth(info);
        int contentHeight = calculateContentHeight(info);
        
        // Add padding
        int boxWidth = Math.min(MAX_WIDTH, Math.max(MIN_WIDTH, contentWidth + PADDING * 2 + ICON_SIZE + ICON_GAP));
        int boxHeight = contentHeight + PADDING * 2;
        
        // Position the tooltip
        int tokenX = hoverState.getTokenScreenX();
        int tokenY = hoverState.getTokenScreenY();
        int lineHeight = ClientProxy.Font.height();
        
        // Default: show below the token
        int tooltipX = tokenX;
        int tooltipY = tokenY + lineHeight + VERTICAL_OFFSET;
        
        // Adjust if tooltip would go off-screen
        if (tooltipX + boxWidth > screenWidth - 10) {
            tooltipX = screenWidth - boxWidth - 10;
        }
        if (tooltipX < 10) {
            tooltipX = 10;
        }
        
        if (tooltipY + boxHeight > screenHeight - 10) {
            // Show above the token instead
            tooltipY = tokenY - boxHeight - VERTICAL_OFFSET;
        }
        if (tooltipY < 10) {
            tooltipY = 10;
        }
        
        // Render the tooltip
        renderTooltipBox(tooltipX, tooltipY, boxWidth, boxHeight, info);
    }

    /**
     * Render the tooltip box with all content.
     */
    private static void renderTooltipBox(int x, int y, int width, int height, TokenHoverInfo info) {
        // Disable scissor test temporarily for tooltip rendering
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        
        // Draw background
        Gui.drawRect(x, y, x + width, y + height, BG_COLOR);
        
        // Draw border
        Gui.drawRect(x, y, x + width, y + 1, BORDER_COLOR);
        Gui.drawRect(x, y + height - 1, x + width, y + height, BORDER_COLOR);
        Gui.drawRect(x, y, x + 1, y + height, BORDER_COLOR);
        Gui.drawRect(x + width - 1, y, x + width, y + height, BORDER_COLOR);
        
        int contentX = x + PADDING;
        int contentY = y + PADDING;
        int textX = contentX + ICON_SIZE + ICON_GAP;
        int maxTextWidth = width - PADDING * 2 - ICON_SIZE - ICON_GAP;
        
        // Draw icon
        String icon = info.getIconIndicator();
        if (icon != null && !icon.isEmpty()) {
            drawIcon(contentX, contentY, icon);
        }
        
        int currentY = contentY;
        
        // Draw errors first (if any)
        List<String> errors = info.getErrors();
        if (!errors.isEmpty()) {
            for (String error : errors) {
                drawText(textX, currentY, error, ERROR_COLOR);
                currentY += ClientProxy.Font.height() + LINE_SPACING;
            }
            currentY += LINE_SPACING; // Extra space after errors
        }
        
        // Draw package name
        String packageName = info.getPackageName();
        if (packageName != null && !packageName.isEmpty()) {
            // Draw package icon
            drawText(textX, currentY, "\u25CB " + packageName, PACKAGE_COLOR);
            currentY += ClientProxy.Font.height() + LINE_SPACING;
        }
        
        // Draw declaration
        List<TokenHoverInfo.TextSegment> declaration = info.getDeclaration();
        if (!declaration.isEmpty()) {
            int segmentX = textX;
            for (TokenHoverInfo.TextSegment segment : declaration) {
                int segmentWidth = ClientProxy.Font.width(segment.text);
                
                // Check if we need to wrap (simple wrapping for now)
                if (segmentX + segmentWidth > x + width - PADDING && segmentX > textX) {
                    currentY += ClientProxy.Font.height() + LINE_SPACING;
                    segmentX = textX;
                }
                
                drawText(segmentX, currentY, segment.text, 0xFF000000 | segment.color);
                segmentX += segmentWidth;
            }
            currentY += ClientProxy.Font.height() + LINE_SPACING;
        }
        
        // Draw documentation
        List<String> docs = info.getDocumentation();
        if (!docs.isEmpty()) {
            currentY += LINE_SPACING; // Extra space before docs
            for (String doc : docs) {
                drawText(textX, currentY, doc, 0xFFA9B7C6);
                currentY += ClientProxy.Font.height() + LINE_SPACING;
            }
        }
        
        // Draw additional info
        List<String> additionalInfo = info.getAdditionalInfo();
        if (!additionalInfo.isEmpty()) {
            currentY += LINE_SPACING;
            for (String line : additionalInfo) {
                drawText(textX, currentY, line, INFO_COLOR);
                currentY += ClientProxy.Font.height() + LINE_SPACING;
            }
        }
        
        // Re-enable scissor test
      //  GL11.glEnable(GL11.GL_SCISSOR_TEST);
    }

    /**
     * Draw an icon indicator.
     */
    private static void drawIcon(int x, int y, String icon) {
        int bgColor;
        switch (icon) {
            case "C": bgColor = ICON_CLASS_BG; break;
            case "I": bgColor = ICON_INTERFACE_BG; break;
            case "E": bgColor = ICON_ENUM_BG; break;
            case "m": bgColor = ICON_METHOD_BG; break;
            case "f": bgColor = ICON_FIELD_BG; break;
            case "v": bgColor = ICON_VAR_BG; break;
            case "p": bgColor = ICON_PARAM_BG; break;
            default: bgColor = ICON_UNKNOWN_BG; break;
        }
        
        // Draw icon background (rounded effect with small rect)
        int bgY = y-2;
        Gui.drawRect(x, bgY, x + ICON_SIZE, bgY + ICON_SIZE, 0xFF000000 | bgColor);
        
        // Draw icon letter centered
        int textWidth = ClientProxy.Font.width(icon);
        int textX = x + (ICON_SIZE - textWidth) / 2;
        int textY = y + (ICON_SIZE - ClientProxy.Font.height()) / 2;
        drawText(textX, textY, icon, 0xFFFFFFFF);
    }

    /**
     * Draw text using the Minecraft font renderer.
     */
    private static void drawText(int x, int y, String text, int color) {
       ClientProxy.Font.drawString(text, x, y, color);
    }

    // ==================== DIMENSION CALCULATION ====================

    /**
     * Calculate the width needed for the content.
     */
    private static int calculateContentWidth(TokenHoverInfo info) {
        int maxWidth = 0;
        
        // Package name width
        String packageName = info.getPackageName();
        if (packageName != null && !packageName.isEmpty()) {
            maxWidth = Math.max(maxWidth, ClientProxy.Font.width("\u25CB " + packageName));
        }
        
        // Declaration width
        List<TokenHoverInfo.TextSegment> declaration = info.getDeclaration();
        int declWidth = 0;
        for (TokenHoverInfo.TextSegment segment : declaration) {
            declWidth += ClientProxy.Font.width(segment.text);
        }
        maxWidth = Math.max(maxWidth, declWidth);
        
        // Error widths
        for (String error : info.getErrors()) {
            maxWidth = Math.max(maxWidth, ClientProxy.Font.width(error));
        }
        
        // Documentation widths
        for (String doc : info.getDocumentation()) {
            maxWidth = Math.max(maxWidth, ClientProxy.Font.width(doc));
        }
        
        // Additional info widths
        for (String line : info.getAdditionalInfo()) {
            maxWidth = Math.max(maxWidth, ClientProxy.Font.width(line));
        }
        
        return maxWidth;
    }

    /**
     * Calculate the height needed for the content.
     */
    private static int calculateContentHeight(TokenHoverInfo info) {
        int lineHeight = ClientProxy.Font.height();
        int totalHeight = 0;
        int lineCount = 0;
        
        // Errors
        if (!info.getErrors().isEmpty()) {
            lineCount += info.getErrors().size();
            totalHeight += LINE_SPACING; // Extra space after errors
        }
        
        // Package name
        if (info.getPackageName() != null && !info.getPackageName().isEmpty()) {
            lineCount++;
        }
        
        // Declaration
        if (!info.getDeclaration().isEmpty()) {
            lineCount++;
        }
        
        // Documentation
        if (!info.getDocumentation().isEmpty()) {
            lineCount += info.getDocumentation().size();
            totalHeight += LINE_SPACING; // Extra space before docs
        }
        
        // Additional info
        if (!info.getAdditionalInfo().isEmpty()) {
            lineCount += info.getAdditionalInfo().size();
            totalHeight += LINE_SPACING; // Extra space before info
        }
        
        totalHeight += lineCount * (lineHeight + LINE_SPACING);
        
        return Math.max(ICON_SIZE, totalHeight);
    }
}
