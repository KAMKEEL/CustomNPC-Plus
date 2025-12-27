package noppes.npcs.client.gui.util.script.interpreter.hover;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import noppes.npcs.client.ClientProxy;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
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
 * Uses a unified approach for width/height calculation based on max available width.
 */
public class TokenHoverRenderer {

    // ==================== CONSTANTS ====================
    
    /** Padding inside the tooltip box */
    private static final int PADDING = 6;
    
    /** Line spacing between rows */
    private static final int LINE_SPACING = 2;
    
    /** Separator line height */
    private static final int SEPARATOR_HEIGHT = 1;
    
    /** Spacing above and below separator */
    private static final int SEPARATOR_SPACING = 5;
    
    /** Vertical offset from token */
    private static final int VERTICAL_OFFSET = 4;
    
    /** Maximum tooltip width as percentage of viewport */
    private static final float MAX_WIDTH_RATIO = 0.9f;
    
    /** Minimum tooltip width */
    private static final int MIN_WIDTH = 50;

    // ==================== COLORS ====================
    
    /** Background color (dark gray like IntelliJ) */
    private static final int BG_COLOR = 0xF0313335;
    
    /** Border color */
    private static final int BORDER_COLOR = 0xFF3C3F41;
    
    /** Package text color */
    private static int PACKAGE_COLOR = 0xFF6490e2;
    
    /** Error text color */
    private static final int ERROR_COLOR = 0xFFFF6B68;
    
    /** Info text color */
    private static final int INFO_COLOR = 0xFF808080;
    
    /** Documentation text color */
    private static final int DOC_COLOR = 0xFFA9B7C6;

    // ==================== RENDERING ====================

    /**
     * Render the hover tooltip.
     */
    public static void render(HoverState hoverState, int viewportX, int viewportWidth, int viewportY, int viewportHeight) {
        if (!hoverState.isTooltipVisible()) return;
        
        TokenHoverInfo info = hoverState.getHoverInfo();
        if (info == null || !info.hasContent()) return;
        
        int lineHeight = ClientProxy.Font.height();
        int tokenX = hoverState.getTokenScreenX();
        int tokenY = hoverState.getTokenScreenY();
        
        // Calculate max available width for content
        int maxContentWidth = getMaxContentWidth(viewportX, viewportWidth, tokenX);
        
        // Calculate actual content width needed (clamped to max)
        int contentWidth = calculateContentWidth(info, maxContentWidth);
        
        // Calculate height based on the actual content width (accounts for wrapping)
        int contentHeight = calculateContentHeight(info, contentWidth);
        
        // Box dimensions
        int boxWidth = contentWidth + PADDING * 2;
        int boxHeight = contentHeight + PADDING * 2;
        
        // Position the tooltip
        int tooltipX = tokenX;
        int tooltipY = tokenY + lineHeight + VERTICAL_OFFSET;
        
        // Clamp X position to viewport
        int rightBound = viewportX + viewportWidth;
        if (tooltipX + boxWidth > rightBound) {
            tooltipX = rightBound - boxWidth;
        }
        if (tooltipX < viewportX) {
            tooltipX = viewportX;
        }
        
        // If box still doesn't fit horizontally, shrink it
        int availableWidth = rightBound - tooltipX;
        if (boxWidth > availableWidth) {
            boxWidth = availableWidth;
        }
        
        // Clamp Y position to viewport
        int bottomBound = viewportY + viewportHeight;
        if (tooltipY + boxHeight > bottomBound) {
            // Try rendering above the token
            tooltipY = tokenY - boxHeight - VERTICAL_OFFSET;
        }
        // If still doesn't fit above, clamp to viewport top
        if (tooltipY < viewportY) {
            tooltipY = viewportY;
        }
        
        // Render the tooltip - use maxContentWidth for consistent wrapping
        renderTooltipBox(tooltipX, tooltipY, boxWidth, maxContentWidth, info);
    }

    /**
     * Calculate the maximum content width based on viewport and token position.
     */
    private static int getMaxContentWidth(int viewportX, int viewportWidth, int tokenX) {
        // Max width is based on viewport size (80% by default)
        // This scales naturally with viewport size - no artificial hard cap
        int maxWidth = (int)(viewportWidth * MAX_WIDTH_RATIO);
        
        // The tooltip can expand across the entire viewport width
        // (positioning logic will shift it left if needed)
        return Math.max(MIN_WIDTH, maxWidth);
    }

    /**
     * Render the tooltip box with all content.
     */
    private static void renderTooltipBox(int x, int y, int boxWidth, int wrapWidth, TokenHoverInfo info) {
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        
        int boxHeight = calculateContentHeight(info, wrapWidth) + PADDING * 1;
        
        // Draw background
        Gui.drawRect(x, y, x + boxWidth, y + boxHeight, BG_COLOR);
        
        // Draw border
        Gui.drawRect(x, y, x + boxWidth, y + 1, BORDER_COLOR);
        Gui.drawRect(x, y + boxHeight - 1, x + boxWidth, y + boxHeight, BORDER_COLOR);
        Gui.drawRect(x, y, x + 1, y + boxHeight, BORDER_COLOR);
        Gui.drawRect(x + boxWidth - 1, y, x + boxWidth, y + boxHeight, BORDER_COLOR);
        
        int textX = x + PADDING;
        int currentY = y + PADDING;
        int lineHeight = ClientProxy.Font.height();


        String packageName = info.getPackageName();
        List<TokenHoverInfo.TextSegment> declaration = info.getDeclaration();
        List<String> docs = info.getDocumentation();
        List<String> additionalInfo = info.getAdditionalInfo();
        
        // Draw errors first
        List<String> errors = info.getErrors();
        if (!errors.isEmpty()) {
            for (String error : errors) {
                List<String> wrappedLines = wrapText(error, wrapWidth);
                for (String line : wrappedLines) {
                    drawText(textX, currentY, line, ERROR_COLOR);
                    currentY += lineHeight + LINE_SPACING;
                }
            }
            boolean onlyErrors = errors != null && !errors.isEmpty() && (packageName == null || packageName.isEmpty()) && (declaration == null ||
                    declaration.isEmpty()) && (docs == null || docs.isEmpty()) && (additionalInfo == null || additionalInfo.isEmpty());

            if (!onlyErrors) { //Add error separator line
                currentY += SEPARATOR_HEIGHT + SEPARATOR_SPACING - 5;
                Gui.drawRect(textX, currentY, x + boxWidth - PADDING, currentY + SEPARATOR_HEIGHT, BORDER_COLOR);
                currentY += SEPARATOR_HEIGHT + SEPARATOR_SPACING;
            }
            currentY += LINE_SPACING;
        }
        
        // Draw package name
        if (packageName != null && !packageName.isEmpty()) {
            String packageText = "\u25CB " + packageName;
            List<String> wrappedLines = wrapText(packageText, wrapWidth);
            for (String line : wrappedLines) {
                drawText(textX, currentY, line, PACKAGE_COLOR);
                currentY += lineHeight + LINE_SPACING;
            }
        }
        
        // Draw declaration (colored segments with wrapping)
        if (!declaration.isEmpty()) {
            currentY = drawWrappedSegments(textX, currentY, wrapWidth, declaration);
            currentY += LINE_SPACING;
        }
        // Draw documentation
        if (!docs.isEmpty()) {
            // Draw separator line before documentation
            Gui.drawRect(textX, currentY, x + boxWidth - PADDING, currentY + SEPARATOR_HEIGHT, BORDER_COLOR);
            currentY += SEPARATOR_HEIGHT + SEPARATOR_SPACING;
            for (String doc : docs) {
                List<String> wrappedLines = wrapText(doc, wrapWidth);
                for (String line : wrappedLines) {
                    drawText(textX, currentY, line, DOC_COLOR);
                    currentY += lineHeight + LINE_SPACING;
                }
            }
        }
        
        // Draw additional info

        if (!additionalInfo.isEmpty()) {
            currentY += LINE_SPACING;
            for (String infoLine : additionalInfo) {
                List<String> wrappedLines = wrapText(infoLine, wrapWidth);
                for (String line : wrappedLines) {
                    drawText(textX, currentY, line, INFO_COLOR);
                    currentY += lineHeight + LINE_SPACING;
                }
            }
        }
    }

    /**
     * Draw colored text segments with word wrapping.
     * Returns the Y position after drawing.
     */
    private static int drawWrappedSegments(int startX, int startY, int maxWidth, List<TokenHoverInfo.TextSegment> segments) {
        int lineHeight = ClientProxy.Font.height();
        int currentX = startX;
        int currentY = startY;
        
        for (TokenHoverInfo.TextSegment segment : segments) {
            String text = segment.text;
            int color = 0xFF000000 | segment.color;
            
            // Split segment into words for wrapping
            String[] words = text.split("(?<=\\s)|(?=\\s)"); // Keep whitespace as separate tokens
            
            for (String word : words) {
                int wordWidth = ClientProxy.Font.width(word);
                
                // Check if word fits on current line
                if (currentX + wordWidth > startX + maxWidth && currentX > startX) {
                    // Wrap to next line
                    currentY += lineHeight + LINE_SPACING;
                    currentX = startX;
                    
                    // Skip leading whitespace on new line
                    if (word.trim().isEmpty()) {
                        continue;
                    }
                }
                
                drawText(currentX, currentY, word, color);
                currentX += wordWidth;
            }
        }
        
        return currentY + lineHeight;
    }

    /**
     * Wrap text to fit within maxWidth.
     * Returns list of lines.
     */
    private static List<String> wrapText(String text, int maxWidth) {
        List<String> lines = new ArrayList<>();
        
        if (text == null || text.isEmpty()) {
            return lines;
        }
        
        // Use Minecraft's built-in wrapping if available
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.fontRenderer != null) {
            @SuppressWarnings("unchecked")
            List<String> wrapped = mc.fontRenderer.listFormattedStringToWidth(text, maxWidth);
            return wrapped;
        }
        
        // Fallback: simple word wrapping
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();
        
        for (String word : words) {
            String testLine = currentLine.length() == 0 ? word : currentLine + " " + word;
            int testWidth = ClientProxy.Font.width(testLine);
            
            if (testWidth > maxWidth && currentLine.length() > 0) {
                lines.add(currentLine.toString());
                currentLine = new StringBuilder(word);
            } else {
                if (currentLine.length() > 0) {
                    currentLine.append(" ");
                }
                currentLine.append(word);
            }
        }
        
        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }
        
        if (lines.isEmpty()) {
            lines.add(text);
        }
        return lines;
    }

    /**
     * Draw text using the font renderer.
     */
    private static void drawText(int x, int y, String text, int color) {
        ClientProxy.Font.drawString(text, x, y, color);
    }

    // ==================== DIMENSION CALCULATION ====================

    /**
     * Calculate the width needed for the content, clamped to maxWidth.
     * Returns the width of the longest wrapped line.
     */
    private static int calculateContentWidth(TokenHoverInfo info, int maxWidth) {
        int longestLineWidth = 0;
        
        // Package name
        String packageName = info.getPackageName();
        if (packageName != null && !packageName.isEmpty()) {
            String packageText = "\u25CB " + packageName;
            List<String> wrappedLines = wrapText(packageText, maxWidth);
            for (String line : wrappedLines) {
                longestLineWidth = Math.max(longestLineWidth, ClientProxy.Font.width(line));
            }
        }
        
        // Declaration - wrap segments and find longest line
        if (!info.getDeclaration().isEmpty()) {
            int declarationLongestLine = calculateSegmentsLongestLine(maxWidth, info.getDeclaration());
            longestLineWidth = Math.max(longestLineWidth, declarationLongestLine);
        }
        
        // Errors
        for (String error : info.getErrors()) {
            List<String> wrappedLines = wrapText(error, maxWidth);
            for (String line : wrappedLines) {
                longestLineWidth = Math.max(longestLineWidth, ClientProxy.Font.width(line));
            }
        }
        
        // Documentation
        for (String doc : info.getDocumentation()) {
            List<String> wrappedLines = wrapText(doc, maxWidth);
            for (String line : wrappedLines) {
                longestLineWidth = Math.max(longestLineWidth, ClientProxy.Font.width(line));
            }
        }
        
        // Additional info
        for (String line : info.getAdditionalInfo()) {
            List<String> wrappedLines = wrapText(line, maxWidth);
            for (String wrappedLine : wrappedLines) {
                longestLineWidth = Math.max(longestLineWidth, ClientProxy.Font.width(wrappedLine));
            }
        }
        
        // Ensure minimum width
        return Math.max(50, longestLineWidth);
    }

    /**
     * Calculate the height needed for the content given the available width.
     * Uses the same wrapping logic as rendering.
     */
    private static int calculateContentHeight(TokenHoverInfo info, int contentWidth) {
        int lineHeight = ClientProxy.Font.height();
        int totalHeight = 0;

        String packageName = info.getPackageName();
        List<TokenHoverInfo.TextSegment> declaration = info.getDeclaration();
        List<String> docs = info.getDocumentation();
        List<String> additionalInfo = info.getAdditionalInfo();

        // Errors
        List<String> errors = info.getErrors();
        if (!errors.isEmpty()) {
            for (String error : errors) {
                List<String> wrappedLines = wrapText(error, contentWidth);
                totalHeight += wrappedLines.size() * (lineHeight + LINE_SPACING);
            }

            boolean onlyErrors = errors != null && !errors.isEmpty() && (packageName == null || packageName.isEmpty()) && (declaration == null ||
                    declaration.isEmpty()) && (docs == null || docs.isEmpty()) && (additionalInfo == null || additionalInfo.isEmpty());
            
            if (!onlyErrors) //Add error separator height
                totalHeight += (SEPARATOR_SPACING + SEPARATOR_HEIGHT) * 2 - 5;
            
            totalHeight += LINE_SPACING; // Extra space after errors
        }
        
        // Package name
        if (packageName != null && !packageName.isEmpty()) {
            String packageText = "\u25CB " + packageName;
            List<String> wrappedLines = wrapText(packageText, contentWidth);
            totalHeight += wrappedLines.size() * (lineHeight + LINE_SPACING);
        }
        
        // Declaration (colored segments with wrapping)
        if (!declaration.isEmpty()) {
            totalHeight += calculateSegmentsHeight(contentWidth, declaration);
            totalHeight += LINE_SPACING;
        }
        
        // Documentation
        if (!docs.isEmpty()) {
            // Add space for separator line and spacing
            totalHeight += SEPARATOR_SPACING + SEPARATOR_HEIGHT;
            for (String doc : docs) {
                List<String> wrappedLines = wrapText(doc, contentWidth);
                totalHeight += wrappedLines.size() * (lineHeight + LINE_SPACING);
            }
        }
        
        // Additional info
        if (!additionalInfo.isEmpty()) {
            totalHeight += LINE_SPACING;
            for (String infoLine : additionalInfo) {
                List<String> wrappedLines = wrapText(infoLine, contentWidth);
                totalHeight += wrappedLines.size() * (lineHeight + LINE_SPACING);
            }
        }
        
        return Math.max(lineHeight, totalHeight);
    }

    /**
     * Calculate height needed for wrapped segments.
     */
    private static int calculateSegmentsHeight(int maxWidth, List<TokenHoverInfo.TextSegment> segments) {
        int lineHeight = ClientProxy.Font.height();
        int currentLineWidth = 0;
        int lineCount = 1;
        
        for (TokenHoverInfo.TextSegment segment : segments) {
            String text = segment.text;
            String[] words = text.split("(?<=\\s)|(?=\\s)");
            
            for (String word : words) {
                int wordWidth = ClientProxy.Font.width(word);
                
                if (currentLineWidth + wordWidth > maxWidth && currentLineWidth > 0) {
                    lineCount++;
                    currentLineWidth = word.trim().isEmpty() ? 0 : wordWidth;
                } else {
                    currentLineWidth += wordWidth;
                }
            }
        }
        
        return lineCount * (lineHeight + LINE_SPACING);
    }

    /**
     * Calculate the width of the longest line when segments are wrapped.
     */
    private static int calculateSegmentsLongestLine(int maxWidth, List<TokenHoverInfo.TextSegment> segments) {
        int currentLineWidth = 0;
        int longestLineWidth = 0;
        
        for (TokenHoverInfo.TextSegment segment : segments) {
            String text = segment.text;
            String[] words = text.split("(?<=\\s)|(?=\\s)");
            
            for (String word : words) {
                int wordWidth = ClientProxy.Font.width(word);
                
                if (currentLineWidth + wordWidth > maxWidth && currentLineWidth > 0) {
                    // Line break - record current line width and start new line
                    longestLineWidth = Math.max(longestLineWidth, currentLineWidth);
                    currentLineWidth = word.trim().isEmpty() ? 0 : wordWidth;
                } else {
                    currentLineWidth += wordWidth;
                }
            }
        }
        
        // Don't forget the last line
        longestLineWidth = Math.max(longestLineWidth, currentLineWidth);
        
        return longestLineWidth;
    }
}
