package noppes.npcs.client.gui.util.script.interpreter;

import noppes.npcs.client.ClientProxy;
import noppes.npcs.client.gui.util.script.interpreter.field.AssignmentInfo;
import noppes.npcs.client.gui.util.script.interpreter.field.FieldInfo;
import noppes.npcs.client.gui.util.script.interpreter.method.MethodCallInfo;
import noppes.npcs.client.gui.util.script.interpreter.method.MethodInfo;
import org.lwjgl.opengl.GL11;

/**
 * Helper class for rendering error underlines in script editor lines.
 * Handles the repetitive calculation of underline positions and drawing of curly underlines.
 */
public class ErrorUnderlineRenderer {
    private static final int ERROR_COLOR = 0xFF5555;

    // Holds the calculation result for an underline
    private static class UnderlinePosition {
        final int x;
        final int width;

        UnderlinePosition(int x, int width) {
            this.x = x;
            this.width = width;
        }

        boolean isValid() {
            return width > 0;
        }
    }

    /**
     * Draw error underlines for all validation errors in the document that intersect the given line.
     *
     * @param doc The script document containing all errors
     * @param lineStartX X coordinate where the line starts rendering
     * @param baselineY Y coordinate for the underline baseline
     * @param lineText The text content of the line
     * @param lineStart Global offset where this line starts
     * @param lineEnd Global offset where this line ends
     */
    public static void drawErrorUnderlines(
            ScriptDocument doc,
            int lineStartX, int baselineY,
            String lineText, int lineStart, int lineEnd) {

        if (doc == null)
            return;
        

        // Check all method calls in the document
        for (MethodCallInfo call : doc.getMethodCalls()) {
            // Skip method declarations that were erroneously recorded as calls
            boolean isDeclaration = false;
            int methodStart = call.getMethodNameStart();
            for (MethodInfo mi : doc.getMethods()) {
                if (mi.getDeclarationOffset() <= methodStart && mi.getBodyStart() >= methodStart) {
                    isDeclaration = true;
                    break;
                }
            }
            if (isDeclaration)
                continue;

            // Skip if this call doesn't intersect this line
            if (call.getCloseParenOffset() < lineStart || call.getOpenParenOffset() > lineEnd)
                continue;

            // Handle arg count errors (underline the method name)
            if (call.hasArgCountError()) {
                int methodEnd = methodStart + call.getMethodName().length();
                drawUnderlineForSpan(methodStart, methodEnd, lineStartX, baselineY,
                        lineText, lineStart, lineEnd, ERROR_COLOR);
            }

            // Handle return type mismatch (underline the method name) - currently commented out
            // if (call.hasReturnTypeMismatch()) { ... }

            // Handle arg type errors (underline specific arguments)
            if (call.hasArgTypeError()) {
                for (MethodCallInfo.ArgumentTypeError error : call.getArgumentTypeErrors()) {
                    MethodCallInfo.Argument arg = error.getArg();
                    drawUnderlineForSpan(arg.getStartOffset(), arg.getEndOffset(),
                            lineStartX, baselineY, lineText, lineStart, lineEnd, ERROR_COLOR);
                }
            }
        }

        // Check all field accesses in the document (currently commented out)
        // for (FieldAccessInfo access : doc.getFieldAccesses()) { ... }

        // Check all errored assignments in the document
        for (AssignmentInfo assign : doc.getAllErroredAssignments()) {
            int underlineStart, underlineEnd;

            if (assign.isLhsError()) {
                underlineStart = assign.getLhsStart();
                underlineEnd = assign.getLhsEnd();
            } else if (assign.isRhsError()) {
                underlineStart = assign.getRhsStart();
                underlineEnd = assign.getRhsEnd();
            } else if (assign.isFullLineError()) {
                underlineStart = assign.getStatementStart();
                underlineEnd = assign.getRhsEnd();
            } else {
                continue;
            }

            drawUnderlineForSpan(underlineStart, underlineEnd, lineStartX, baselineY,
                    lineText, lineStart, lineEnd, ERROR_COLOR);
        }

        // Check all method declarations for errors
        for (MethodInfo method : doc.getMethods()) {
            if (!method.isDeclaration() || !method.hasError())
                continue;

            // Handle return statement type errors
            if (method.hasReturnStatementErrors()) {
                for (MethodInfo.ReturnStatementError returnError : method.getReturnStatementErrors()) {
                    drawUnderlineForSpan(returnError.getStartOffset(), returnError.getEndOffset(),
                            lineStartX, baselineY, lineText, lineStart, lineEnd, ERROR_COLOR);
                }
            }

            // Handle missing return error (underline the method name)
            if (method.hasMissingReturnError()) {
                int methodNameStart = method.getNameOffset();
                int methodNameEnd = methodNameStart + method.getName().length();
                drawUnderlineForSpan(methodNameStart, methodNameEnd, lineStartX, baselineY,
                        lineText, lineStart, lineEnd, ERROR_COLOR);
            }

            // Handle duplicate method error (underline from full declaration start to closing paren)
            if (method.getErrorType() == MethodInfo.ErrorType.DUPLICATE_METHOD) {
                drawUnderlineForSpan(method.getFullDeclarationOffset(), method.getDeclarationEnd(),
                        lineStartX, baselineY, lineText, lineStart, lineEnd, ERROR_COLOR);
            }

            // Handle parameter errors
            if (method.hasParameterErrors()) {
                for (MethodInfo.ParameterError paramError : method.getParameterErrors()) {
                    FieldInfo param = paramError.getParameter();
                    if (param == null || param.getDeclarationOffset() < 0)
                        continue;

                    int paramStart = param.getDeclarationOffset();
                    int paramEnd = paramStart + param.getName().length();
                    drawUnderlineForSpan(paramStart, paramEnd, lineStartX, baselineY,
                            lineText, lineStart, lineEnd, ERROR_COLOR);
                }
            }
        }
    }

    /**
     * Calculate underline position for a span of text within a line.
     * Handles clipping to line boundaries and pixel width calculation.
     *
     * @param spanStart Global offset where the span starts
     * @param spanEnd Global offset where the span ends
     * @param lineStartX X coordinate where the line starts rendering
     * @param lineText The text content of the line
     * @param lineStart Global offset where this line starts
     * @param lineEnd Global offset where this line ends
     * @return UnderlinePosition with x and width, or null if span doesn't intersect line
     */
    private static UnderlinePosition calculateUnderlinePosition(
            int spanStart, int spanEnd,
            int lineStartX, String lineText, int lineStart, int lineEnd) {

        // Skip if span doesn't intersect this line
        if (spanEnd < lineStart || spanStart > lineEnd)
            return null;

        // Clip to line boundaries
        int clipStart = Math.max(spanStart, lineStart);
        int clipEnd = Math.min(spanEnd, lineEnd);

        if (clipStart >= clipEnd)
            return null;

        // Convert to line-local coordinates
        int lineLocalStart = clipStart - lineStart;
        int lineLocalEnd = clipEnd - lineStart;

        // Bounds check
        if (lineLocalStart < 0 || lineLocalStart >= lineText.length())
            return null;

        // Compute pixel position
        String beforeSpan = lineText.substring(0, lineLocalStart);
        int beforeWidth = ClientProxy.Font.width(beforeSpan);

        int spanWidth;
        if (lineLocalEnd > lineText.length()) {
            // Span extends past line end
            spanWidth = ClientProxy.Font.width(lineText.substring(lineLocalStart));
        } else {
            // Span is fully on this line (or clipped)
            String spanTextOnLine = lineText.substring(lineLocalStart, lineLocalEnd);
            spanWidth = ClientProxy.Font.width(spanTextOnLine);
        }

        return new UnderlinePosition(lineStartX + beforeWidth, spanWidth);
    }

    /**
     * Draw an underline for a simple span if it intersects the line.
     *
     * @param spanStart Global offset where the span starts
     * @param spanEnd Global offset where the span ends
     * @param lineStartX X coordinate where the line starts rendering
     * @param baselineY Y coordinate for the underline baseline
     * @param lineText The text content of the line
     * @param lineStart Global offset where this line starts
     * @param lineEnd Global offset where this line ends
     * @param color Underline color
     */
    public static void drawUnderlineForSpan(
            int spanStart, int spanEnd,
            int lineStartX, int baselineY,
            String lineText, int lineStart, int lineEnd,
            int color) {

        UnderlinePosition pos = calculateUnderlinePosition(
                spanStart, spanEnd, lineStartX, lineText, lineStart, lineEnd);

        if (pos != null && pos.isValid()) {
            drawCurlyUnderline(pos.x, baselineY, pos.width, color);
        }
    }

    /**
     * Draw a curly/wavy underline (like IDE error highlighting).
     * @param x Start X position
     * @param y Y position (bottom of text)
     * @param width Width of the underline
     * @param color Color in ARGB format (e.g., 0xFFFF5555 for red)
     */
    public static void drawCurlyUnderline(int x, int y, int width, int color) {
        if (width <= 0)
            return;

        float a = ((color >> 24) & 0xFF) / 255f;
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;

        // If alpha is 0, assume full opacity
        if (a == 0)
            a = 1.0f;

        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(r, g, b, a);
        GL11.glLineWidth(1.0f);

        GL11.glBegin(GL11.GL_LINE_STRIP);
        // Wave parameters: 2 pixels amplitude, 4 pixels wavelength
        int waveHeight = 1;
        float waveLength = 4f;
        for (float i = -0.5f; i <= width - 1; i += 0.125f) {
            // Create a sine-like wave pattern
            double phase = (double) i / waveLength * Math.PI * 2;
            float yOffset = (float) (Math.sin(phase) * waveHeight) - 0.25f;
            GL11.glVertex2f(x + i + 2f, y + yOffset);
        }
        GL11.glEnd();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glPopMatrix();
    }
}
