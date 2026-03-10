package noppes.npcs.client.gui.util.script.interpreter.hover;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import noppes.npcs.client.ClientProxy;
import noppes.npcs.client.gui.util.GuiNPCInterface;
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

    /** Background fill for ``` code blocks */
    private static final int CODE_BG_COLOR = 0xFF1E1F22;
    /** Left accent bar color for code blocks */
    private static final int CODE_ACCENT_COLOR = 0xFF4FC1FF;
    /** Horizontal padding inside code block */
    private static final int CODE_INDENT = 10;
    /** Vertical padding above/below code block content */
    private static final int CODE_VPAD = 6;
    /** Fixed visual gap between last content line and the panel bottom (always visible, regardless of scroll) */
    private static final int BOTTOM_GAP = 4;

    // ==================== RENDERING ====================

    /**
     * Render the hover tooltip.
     */
    public static void render(HoverState hoverState, int viewportX, int viewportWidth, int viewportY, int viewportHeight, GuiNPCInterface gui) {
        if (!hoverState.isTooltipVisible()) return;
        hoverState.updateSmoothScroll();
        
        TokenHoverInfo info = hoverState.getHoverInfo();
        if (info == null || !info.hasContent()) return;
        
        int lineHeight = ClientProxy.Font.height();
        int tokenX = hoverState.getTokenScreenX();
        int tokenY = hoverState.getTokenScreenY();

        // Screen dimensions for clamping when the panel has been dragged outside the editor viewport
        Minecraft mc2 = Minecraft.getMinecraft();
        ScaledResolution sr2 = new ScaledResolution(mc2, mc2.displayWidth, mc2.displayHeight);
        int screenW = sr2.getScaledWidth();
        int screenH = sr2.getScaledHeight();
        boolean positionOverridden = hoverState.hasOverriddenPosition();
        // Use screen bounds whenever the panel has been dragged OR resized — both escape the editor viewport
        boolean useScreenBounds = positionOverridden || hoverState.hasOverriddenSize();
        
        // Calculate max available width for content
        int maxContentWidth = getMaxContentWidth(viewportX, viewportWidth, tokenX);
        
        // Calculate actual content width needed (clamped to max)
        int contentWidth = calculateContentWidth(info, maxContentWidth);
        
        // Cap visible height at 60% of viewport
        int totalContentHeight = calculateContentHeight(info, contentWidth);
        int maxVisibleContentHeight = (int)((useScreenBounds ? screenH : viewportHeight) * 0.60f);
        if (totalContentHeight > maxVisibleContentHeight) {
            // Scrollbar will appear, reducing effective wrap width by 6px — re-measure for accurate height
            totalContentHeight = calculateContentHeight(info, contentWidth - 6);
        }
        int visibleContentHeight = Math.min(totalContentHeight, maxVisibleContentHeight);

        // Box dimensions — use visibleContentHeight so positioning matches rendering
        int boxWidth = contentWidth + PADDING * 2;
        int boxHeight = visibleContentHeight + PADDING * 2;

        // Apply user size override when the panel has been resized
        if (hoverState.hasOverriddenSize()) {
            int overW = Math.max(MIN_WIDTH + PADDING * 2, hoverState.getOverriddenTooltipW());
            int overH = Math.max(lineHeight + PADDING * 2, hoverState.getOverriddenTooltipH());
            boxWidth = overW;
            visibleContentHeight = overH - PADDING * 2;
            boxHeight = overH;
            // Re-measure with new wrap width so scrollbar detection is accurate
            int newContentW = boxWidth - PADDING * 2;
            totalContentHeight = calculateContentHeight(info, newContentW);
            if (totalContentHeight > visibleContentHeight) {
                totalContentHeight = calculateContentHeight(info, newContentW - 6);
            }
        }
        
        // Position the tooltip — use dragged position if user has panned the panel
        int tooltipX = positionOverridden ? hoverState.getOverriddenTooltipX() : tokenX;
        int tooltipY = positionOverridden ? hoverState.getOverriddenTooltipY() : tokenY + lineHeight + VERTICAL_OFFSET;
        
        // Clamp position — use screen bounds when user has dragged the panel outside the editor viewport
        int leftBound   = useScreenBounds ? 0       : viewportX;
        int rightBound  = useScreenBounds ? screenW  : viewportX + viewportWidth;
        int topBound    = useScreenBounds ? 0       : viewportY;
        int bottomBound = useScreenBounds ? screenH  : viewportY + viewportHeight;

        if (tooltipX + boxWidth > rightBound) {
            tooltipX = rightBound - boxWidth;
        }
        if (tooltipX < leftBound) {
            tooltipX = leftBound;
        }

        // If box still doesn't fit horizontally, shrink it
        int availableWidth = rightBound - tooltipX;
        if (boxWidth > availableWidth) {
            boxWidth = availableWidth;
        }

        if (tooltipY + boxHeight > bottomBound) {
            if (useScreenBounds) {
                tooltipY = bottomBound - boxHeight;
            } else {
                tooltipY = tokenY - boxHeight - VERTICAL_OFFSET; // try above token
            }
        }
        if (tooltipY < topBound) {
            tooltipY = topBound;
        }
        
        // Record actual panel rect for drag hit-testing
        hoverState.setTooltipPanel(tooltipX, tooltipY, boxWidth, boxHeight);
        
        // Mouse-over detection bounds:
        // When dragged, use panel rect only — no gap extension (token is unrelated to panel position)
        // When default, union the token bottom and the tooltip rect so both directions are covered
        if (hoverState.hasOverriddenPosition()) {
            hoverState.setTooltipBounds(tooltipX, tooltipY, boxWidth, boxHeight);
        } else {
            int boundsTopY    = Math.min(tokenY + lineHeight, tooltipY);
            int boundsBottomY = Math.max(tooltipY + boxHeight, tokenY + lineHeight);
            hoverState.setTooltipBounds(tooltipX, boundsTopY, boxWidth, boundsBottomY - boundsTopY);
        }
        
        // Render the tooltip
        renderTooltipBox(tooltipX, tooltipY, boxWidth, maxContentWidth, info, hoverState,
                totalContentHeight, visibleContentHeight, gui);
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
    private static void renderTooltipBox(int x, int y, int boxWidth, int wrapWidth, TokenHoverInfo info,
                                         HoverState hoverState, int totalContentHeight, int visibleContentHeight, GuiNPCInterface gui) {

        boolean hasScrollbar = totalContentHeight > visibleContentHeight;
        // Wrap at the actual box content area width, not the max allowed width.
        // Using maxContentWidth here caused lines to never wrap at wide viewports (fullscreen).
        int contentWrapWidth = boxWidth - PADDING * 2;
        int effectiveWrapWidth = hasScrollbar ? contentWrapWidth - 6 : contentWrapWidth;

        int boxHeight = visibleContentHeight + PADDING * 2;

        // Store scroll metadata into HoverState
        hoverState.setTooltipMaxScroll(Math.max(0, totalContentHeight - visibleContentHeight + BOTTOM_GAP));

        // Draw background
        Gui.drawRect(x, y, x + boxWidth, y + boxHeight, BG_COLOR);
        
        // Draw border
        Gui.drawRect(x, y, x + boxWidth, y + 1, BORDER_COLOR);
        Gui.drawRect(x, y + boxHeight - 1, x + boxWidth, y + boxHeight, BORDER_COLOR);
        Gui.drawRect(x, y, x + 1, y + boxHeight, BORDER_COLOR);
        Gui.drawRect(x + boxWidth - 1, y, x + boxWidth, y + boxHeight, BORDER_COLOR);

        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution sr = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        int sf = sr.getScaleFactor();
        double panX = gui != null ? gui.getPanX() : 0;
        double panY = gui != null ? gui.getPanY() : 0;
        int clipX = (int)((x + 1 - panX) * sf);
        int clipY = (int)((sr.getScaledHeight() - (y - panY + boxHeight - 1 - BOTTOM_GAP)) * sf);
        int clipW = (hasScrollbar ? boxWidth - PADDING - 1 : boxWidth - 2) * sf;
        int clipH = (boxHeight - 2 - BOTTOM_GAP) * sf;
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(clipX, clipY, clipW, clipH);

        int textX = x + PADDING;
        int currentY = y + PADDING - hoverState.getTooltipScrollOffset();
        int lineHeight = ClientProxy.Font.height();

        String packageName = info.getPackageName();
        List<TokenHoverInfo.TextSegment> declaration = info.getDeclaration();
        List<String> docs = info.getDocumentation();
        List<String> additionalInfo = info.getAdditionalInfo();
        
        // Draw errors first
        List<String> errors = info.getErrors();
        if (!errors.isEmpty()) {
            for (String error : errors) {
                List<String> wrappedLines = wrapText(error, effectiveWrapWidth);
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
            List<String> wrappedLines = wrapText(packageText, effectiveWrapWidth);
            for (String line : wrappedLines) {
                drawText(textX, currentY, line, PACKAGE_COLOR);
                currentY += lineHeight + LINE_SPACING;
            }
        }
        
        // Draw declaration (colored segments with wrapping)
        if (!declaration.isEmpty()) {
            currentY = drawWrappedSegments(textX, currentY, effectiveWrapWidth, declaration);
            currentY += LINE_SPACING;
        }
        
        // Draw documentation (plain text)
        if (!docs.isEmpty()) {
            // Draw separator line before documentation
            Gui.drawRect(textX, currentY, x + boxWidth - PADDING, currentY + SEPARATOR_HEIGHT, BORDER_COLOR);
            currentY += SEPARATOR_HEIGHT + SEPARATOR_SPACING;
            for (String doc : docs) {
                List<String> wrappedLines = wrapText(doc, effectiveWrapWidth);
                for (String line : wrappedLines) {
                    drawText(textX, currentY, line, DOC_COLOR);
                    currentY += lineHeight + LINE_SPACING;
                }
            }
        }
        
        // Draw JSDoc-formatted documentation with colored segments
        List<TokenHoverInfo.DocumentationLine> jsDocLines = info.getJSDocLines();
        if (!jsDocLines.isEmpty()) {
            // Draw separator line if there was plain documentation or declaration
            if (docs.isEmpty() && !declaration.isEmpty()) {
                Gui.drawRect(textX, currentY, x + boxWidth - PADDING, currentY + SEPARATOR_HEIGHT, BORDER_COLOR);
                currentY += SEPARATOR_HEIGHT + SEPARATOR_SPACING;
            }
            
            boolean previousVisibleJSDocWasCode = false;
            for (int di = 0; di < jsDocLines.size(); di++) {
                TokenHoverInfo.DocumentationLine docLine = jsDocLines.get(di);
                
                if (docLine.isCodeBlockFirst) {
                    // Lookahead: measure full block height
                    int blockH = CODE_VPAD;
                    for (int k = di; k < jsDocLines.size() && jsDocLines.get(k).isCodeLine; k++) {
                        TokenHoverInfo.DocumentationLine cl = jsDocLines.get(k);
                        if (!cl.isEmpty()) {
                            int clIndent = cl.codeLeadingSpaces * ClientProxy.Font.width(" ");
                            int clWrapW = Math.max(10, effectiveWrapWidth - CODE_INDENT - clIndent);
                            blockH += calculateSegmentsHeight(clWrapW, cl.segments) + LINE_SPACING;
                        } else {
                            blockH += lineHeight / 2;
                        }
                    }
                    blockH -= LINE_SPACING * 2; // subtract trailing LINE_SPACING (present inside bg) + one more to visually balance top/bottom
                    blockH += CODE_VPAD;
                    // Draw background
                    Gui.drawRect(textX , currentY - CODE_VPAD,
                                 textX  + effectiveWrapWidth + 4, currentY - CODE_VPAD + blockH,
                                 CODE_BG_COLOR);
                    // Left accent bar
                    Gui.drawRect(textX, currentY - CODE_VPAD,
                                 textX+2, currentY - CODE_VPAD + blockH,
                                 CODE_ACCENT_COLOR);
                }
                
                if (docLine.isCodeLine) {
                    if (!docLine.isEmpty()) {
                        int explicitIndent = docLine.codeLeadingSpaces * ClientProxy.Font.width(" ");
                        int codeX = textX + CODE_INDENT + explicitIndent;
                        int codeWrapW = Math.max(10, effectiveWrapWidth - CODE_INDENT - explicitIndent);
                        currentY = drawWrappedSegments(codeX, currentY, codeWrapW, docLine.segments);
                        currentY += LINE_SPACING;
                        previousVisibleJSDocWasCode = true;
                    } else {
                        currentY += lineHeight / 2;
                    }
                } else if (!docLine.isEmpty()) {
                    if (previousVisibleJSDocWasCode) {
                        currentY += LINE_SPACING;
                    }
                    currentY = drawWrappedSegments(textX, currentY, effectiveWrapWidth, docLine.segments);
                    currentY += LINE_SPACING;
                    previousVisibleJSDocWasCode = false;
                } else {
                    currentY += (di > 0 && jsDocLines.get(di - 1).isCodeLine) ? LINE_SPACING : lineHeight / 2;
                }
            }
        }
        
        // Draw additional info
        if (!additionalInfo.isEmpty()) {
            currentY += LINE_SPACING;
            for (String infoLine : additionalInfo) {
                List<String> wrappedLines = wrapText(infoLine, effectiveWrapWidth);
                for (String line : wrappedLines) {
                    drawText(textX, currentY, line, INFO_COLOR);
                    currentY += lineHeight + LINE_SPACING;
                }
            }
        }

        // Restore scissor state before drawing scrollbar (scrollbar must not be clipped)
        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        // Draw scrollbar outside scissor rect so it is never clipped
        if (hasScrollbar) {
            int scrollbarX = x + boxWidth - PADDING;
            int scrollbarTrackTop = y + PADDING;
            int scrollbarTrackHeight = visibleContentHeight - 4; // leave room for resize handle
            // Track
            Gui.drawRect(scrollbarX, scrollbarTrackTop, scrollbarX + 3,
                scrollbarTrackTop + scrollbarTrackHeight, 0x40888888);
            // Thumb
            float scrollRatio = (float) visibleContentHeight / (totalContentHeight + BOTTOM_GAP);
            int thumbHeight = Math.max(6, (int)(scrollbarTrackHeight * scrollRatio));
            int effectiveMaxScroll = totalContentHeight - visibleContentHeight + BOTTOM_GAP;
            // Clamp scrollProgress to [0,1] — guards against stale offset after resize shrinks content
            float scrollProgress = effectiveMaxScroll > 0
                ? Math.min(1f, (float) hoverState.getTooltipScrollOffset() / effectiveMaxScroll) : 0f;
            int thumbY = scrollbarTrackTop + (int)((scrollbarTrackHeight - thumbHeight) * scrollProgress);
            // Clamp thumb within track bounds defensively
            thumbY = Math.max(scrollbarTrackTop, Math.min(scrollbarTrackTop + scrollbarTrackHeight - thumbHeight, thumbY));
            // Store thumb bounds in HoverState for drag/hover detection
            hoverState.setScrollbarThumb(scrollbarX, thumbY, thumbHeight, scrollbarTrackTop, scrollbarTrackHeight);
            // Highlight thumb when hovered or dragging (darker = active, like AutocompleteMenu)
            int mouseX = hoverState.getLastMouseX();
            int mouseY = hoverState.getLastMouseY();
            boolean thumbActive = hoverState.isDraggingScrollbar()
                || hoverState.isMouseOverScrollbarThumb(mouseX, mouseY);
            int thumbColor = thumbActive ? 0xFFCCCCCC : 0xFF808080;
            Gui.drawRect(scrollbarX, thumbY, scrollbarX + 3, thumbY + thumbHeight, thumbColor);
        }

        // Draw resize handle at bottom-right corner (outside scissor, same as scrollbar)
        {
            int mouseX = hoverState.getLastMouseX();
            int mouseY = hoverState.getLastMouseY();
            boolean resizeActive = hoverState.isResizingTooltip()
                || hoverState.isMouseOverResizeHandle(mouseX, mouseY);
            int dotColor = resizeActive ? 0xFFCCCCCC : 0x80888888;
            int rhX = x + boxWidth - 2;
            int rhY = y + boxHeight - 2;
            // Three-dot diagonal pattern (◢)
            Gui.drawRect(rhX - 1, rhY - 1, rhX,     rhY,     dotColor);
            Gui.drawRect(rhX - 3, rhY - 1, rhX - 2, rhY,     dotColor);
            Gui.drawRect(rhX - 1, rhY - 3, rhX,     rhY - 2, dotColor);
            Gui.drawRect(rhX - 5, rhY - 1, rhX - 4, rhY,     dotColor);
            Gui.drawRect(rhX - 3, rhY - 3, rhX - 2, rhY - 2, dotColor);
            Gui.drawRect(rhX - 1, rhY - 5, rhX,     rhY - 4, dotColor);
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

        // First split by explicit newlines
        String[] paragraphs = text.split("\n", -1);  // -1 to preserve trailing empty lines
        
        for (String paragraph : paragraphs) {
            if (paragraph.isEmpty()) {
                lines.add("");
                continue;
            }
            
            // Then apply word wrapping to each paragraph
            String[] words = paragraph.split(" ");
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
        
        // JSDoc-formatted documentation lines
        List<TokenHoverInfo.DocumentationLine> jsDocLines = info.getJSDocLines();
        if (jsDocLines != null) {
            for (TokenHoverInfo.DocumentationLine docLine : jsDocLines) {
                if (!docLine.isEmpty()) {
                    int explicitIndent = docLine.isCodeLine
                        ? CODE_INDENT + docLine.codeLeadingSpaces * ClientProxy.Font.width(" ")
                        : 0;
                    int lineLongest = calculateSegmentsLongestLine(maxWidth - explicitIndent, docLine.segments) + explicitIndent;
                    longestLineWidth = Math.max(longestLineWidth, lineLongest);
                }
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
        List<TokenHoverInfo.DocumentationLine> jsDocLines = info.getJSDocLines();
        List<String> additionalInfo = info.getAdditionalInfo();

        // Errors
        List<String> errors = info.getErrors();
        if (!errors.isEmpty()) {
            for (String error : errors) {
                List<String> wrappedLines = wrapText(error, contentWidth);
                totalHeight += wrappedLines.size() * (lineHeight + LINE_SPACING);
            }

            boolean onlyErrors = errors != null && !errors.isEmpty() && (packageName == null || packageName.isEmpty()) && (declaration == null ||
                    declaration.isEmpty()) && (docs == null || docs.isEmpty()) && (jsDocLines == null || jsDocLines.isEmpty()) && (additionalInfo == null || additionalInfo.isEmpty());
            
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
        
        // Documentation (plain text)
        if (!docs.isEmpty()) {
            // Add space for separator line and spacing
            totalHeight += SEPARATOR_SPACING + SEPARATOR_HEIGHT;
            for (String doc : docs) {
                List<String> wrappedLines = wrapText(doc, contentWidth);
                totalHeight += wrappedLines.size() * (lineHeight + LINE_SPACING);
            }
        }
        
        // JSDoc-formatted documentation lines
        if (jsDocLines != null && !jsDocLines.isEmpty()) {
            if (docs.isEmpty() && !declaration.isEmpty()) {
                totalHeight += SEPARATOR_HEIGHT + SEPARATOR_SPACING;
            }

            boolean previousVisibleJSDocWasCode = false;
            for (int i = 0; i < jsDocLines.size(); i++) {
                TokenHoverInfo.DocumentationLine docLine = jsDocLines.get(i);
                if (!docLine.isEmpty()) {
                    int explicitIndent = docLine.isCodeLine
                        ? docLine.codeLeadingSpaces * ClientProxy.Font.width(" ")
                        : 0;
                    int wrapW = Math.max(10, contentWidth - (docLine.isCodeLine ? CODE_INDENT : 0) - explicitIndent);
                    if (!docLine.isCodeLine && previousVisibleJSDocWasCode) {
                        totalHeight += LINE_SPACING;
                    }
                    totalHeight += calculateSegmentsHeight(wrapW, docLine.segments);
                    totalHeight += LINE_SPACING;
                    previousVisibleJSDocWasCode = docLine.isCodeLine;
                } else {
                    totalHeight += (i > 0 && jsDocLines.get(i - 1).isCodeLine) ? LINE_SPACING : lineHeight / 2;
                }
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
        return Math.max(lineHeight, Math.max(0, totalHeight - LINE_SPACING))-2;
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
        
        if (lineCount <= 0) {
            return 0;
        }
        
        // Total height = (lines * lineHeight) + (spacing between lines)
        return (lineCount * lineHeight) + ((lineCount - 1) * LINE_SPACING);
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
