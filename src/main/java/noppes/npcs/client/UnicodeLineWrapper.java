package noppes.npcs.client;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Wraps formatted Unicode text using the JRE's locale-neutral line breaking
 * rules. Minecraft formatting codes are retained in the result, but omitted
 * from the text passed to the line and grapheme break iterators.
 */
final class UnicodeLineWrapper {
    private static final char FORMAT_MARKER = '\u00a7';

    interface WidthMeasurer {
        int width(String text);
    }

    private UnicodeLineWrapper() {
    }

    static List<String> wrap(String text, int maxWidth, WidthMeasurer measurer) {
        if (text == null || text.isEmpty()) {
            return Collections.emptyList();
        }
        if (measurer == null) {
            throw new IllegalArgumentException("measurer must not be null");
        }

        List<String> lines = new ArrayList<String>();
        int paragraphStart = 0;
        for (int i = 0; i < text.length(); i++) {
            char current = text.charAt(i);
            if (current != '\r' && current != '\n') {
                continue;
            }

            addParagraph(lines, text.substring(paragraphStart, i), maxWidth, measurer, true);
            if (current == '\r' && i + 1 < text.length() && text.charAt(i + 1) == '\n') {
                i++;
            }
            paragraphStart = i + 1;
        }

        // A final hard break terminates the current line; it does not create an
        // additional empty line after it.
        if (paragraphStart < text.length()) {
            addParagraph(lines, text.substring(paragraphStart), maxWidth, measurer, false);
        }
        return lines;
    }

    private static void addParagraph(List<String> lines, String paragraph, int maxWidth,
                                     WidthMeasurer measurer, boolean endedByHardBreak) {
        Paragraph parsed = Paragraph.parse(normalizeSpaces(paragraph));
        if (parsed.visible.isEmpty()) {
            if (endedByHardBreak) {
                lines.add("");
            }
            return;
        }

        BreakIterator lineIterator = BreakIterator.getLineInstance(Locale.ROOT);
        lineIterator.setText(parsed.visible);
        BreakIterator characterIterator = BreakIterator.getCharacterInstance(Locale.ROOT);
        characterIterator.setText(parsed.visible);

        int start = 0;
        while (start < parsed.visible.length()) {
            int end;
            if (maxWidth <= 0) {
                end = nextGraphemeBoundary(parsed.visible, start, characterIterator);
            } else {
                end = findLegalBreak(parsed, start, maxWidth, measurer, lineIterator);
                if (end < 0) {
                    end = findGraphemeBreak(parsed, start, maxWidth, measurer, characterIterator);
                }
            }

            lines.add(parsed.line(start, end));
            start = skipSpaces(parsed.visible, end);
        }
    }

    private static int findLegalBreak(Paragraph paragraph, int start, int maxWidth,
                                      WidthMeasurer measurer, BreakIterator iterator) {
        int best = -1;
        int boundary = iterator.following(start);
        while (boundary != BreakIterator.DONE) {
            int contentEnd = trimSpaces(paragraph.visible, start, boundary);
            if (contentEnd > start) {
                if (!fits(paragraph, start, contentEnd, maxWidth, measurer)) {
                    break;
                }
                best = contentEnd;
            }
            boundary = iterator.next();
        }
        return best;
    }

    private static int findGraphemeBreak(Paragraph paragraph, int start, int maxWidth,
                                         WidthMeasurer measurer, BreakIterator iterator) {
        int best = -1;
        int cursor = start;
        int first = nextGraphemeBoundary(paragraph.visible, start, iterator);
        while (cursor < paragraph.visible.length()) {
            int next = nextGraphemeBoundary(paragraph.visible, cursor, iterator);
            if (!fits(paragraph, start, next, maxWidth, measurer)) {
                break;
            }
            best = next;
            cursor = next;
        }

        // Even a single grapheme can be wider than the container. Keeping it
        // intact on its own line guarantees progress without corrupting text.
        return best < 0 ? first : best;
    }

    private static boolean fits(Paragraph paragraph, int start, int end, int maxWidth,
                                WidthMeasurer measurer) {
        return measurer.width(paragraph.line(start, end)) <= maxWidth;
    }

    private static int trimSpaces(String text, int start, int end) {
        while (end > start && text.charAt(end - 1) == ' ') {
            end--;
        }
        return end;
    }

    private static int skipSpaces(String text, int start) {
        while (start < text.length() && text.charAt(start) == ' ') {
            start++;
        }
        return start;
    }

    private static int nextGraphemeBoundary(String text, int start, BreakIterator iterator) {
        int end = iterator.following(start);
        if (end == BreakIterator.DONE) {
            return text.length();
        }
        if (end < text.length() && Character.isHighSurrogate(text.charAt(end - 1))
            && Character.isLowSurrogate(text.charAt(end))) {
            end++;
        }

        boolean regionalIndicatorPair = isRegionalIndicator(text.codePointAt(start));
        int regionalIndicators = regionalIndicatorPair ? countRegionalIndicators(text, start, end) : 0;
        while (end < text.length()) {
            int previous = text.codePointBefore(end);
            int next = text.codePointAt(end);
            boolean join = isGraphemeExtender(next);
            if (regionalIndicatorPair && regionalIndicators < 2 && isRegionalIndicator(next)) {
                join = true;
                regionalIndicators++;
            } else if (next == 0x200d) {
                join = isExtendedPictographic(previous)
                    && end + 1 < text.length()
                    && isExtendedPictographic(text.codePointAt(end + 1));
            } else if (previous == 0x200d) {
                int beforeZwj = end - 1;
                int preceding = beforeZwj > 0 ? text.codePointBefore(beforeZwj) : -1;
                join = isExtendedPictographic(preceding) && isExtendedPictographic(next);
            }
            if (!join) {
                break;
            }

            int following = iterator.following(end);
            if (following == BreakIterator.DONE) {
                return text.length();
            }
            end = following;
            if (end < text.length() && Character.isHighSurrogate(text.charAt(end - 1))
                && Character.isLowSurrogate(text.charAt(end))) {
                end++;
            }
        }
        return end;
    }

    private static boolean isGraphemeExtender(int codePoint) {
        int type = Character.getType(codePoint);
        return type == Character.NON_SPACING_MARK
            || type == Character.COMBINING_SPACING_MARK
            || type == Character.ENCLOSING_MARK
            || codePoint >= 0xfe00 && codePoint <= 0xfe0f
            || codePoint >= 0xe0100 && codePoint <= 0xe01ef
            || codePoint >= 0x1f3fb && codePoint <= 0x1f3ff
            || codePoint >= 0xe0020 && codePoint <= 0xe007f;
    }

    private static boolean isRegionalIndicator(int codePoint) {
        return codePoint >= 0x1f1e6 && codePoint <= 0x1f1ff;
    }

    private static int countRegionalIndicators(String text, int start, int end) {
        int count = 0;
        for (int index = start; index < end;) {
            int codePoint = text.codePointAt(index);
            if (!isRegionalIndicator(codePoint)) {
                break;
            }
            count++;
            index += Character.charCount(codePoint);
        }
        return count;
    }

    private static boolean isExtendedPictographic(int codePoint) {
        return codePoint >= 0x1f000 && codePoint <= 0x1faff
            || codePoint >= 0x2600 && codePoint <= 0x27bf
            || codePoint == 0x00a9 || codePoint == 0x00ae
            || codePoint == 0x203c || codePoint == 0x2049
            || codePoint == 0x2122 || codePoint == 0x2139
            || codePoint >= 0x2194 && codePoint <= 0x21ff
            || codePoint >= 0x2300 && codePoint <= 0x23ff
            || codePoint >= 0x2b00 && codePoint <= 0x2bff
            || codePoint >= 0x3030 && codePoint <= 0x303d
            || codePoint >= 0x3297 && codePoint <= 0x3299;
    }

    private static String normalizeSpaces(String text) {
        StringBuilder normalized = new StringBuilder(text.length());
        StringBuilder controlsAfterSpace = new StringBuilder();
        boolean hasVisibleText = false;
        boolean pendingSpace = false;

        for (int i = 0; i < text.length();) {
            if (isFormatCode(text, i)) {
                if (pendingSpace) {
                    controlsAfterSpace.append(text, i, i + 2);
                } else {
                    normalized.append(text, i, i + 2);
                }
                i += 2;
                continue;
            }

            char current = text.charAt(i++);
            if (current == ' ') {
                if (hasVisibleText) {
                    pendingSpace = true;
                }
                continue;
            }

            if (pendingSpace) {
                normalized.append(' ').append(controlsAfterSpace);
                controlsAfterSpace.setLength(0);
                pendingSpace = false;
            }
            normalized.append(current);
            hasVisibleText = true;
        }

        // Formatting after a trimmed trailing space is harmless, but retaining
        // it keeps the source mapping faithful without retaining the space.
        normalized.append(controlsAfterSpace);
        return normalized.toString();
    }

    private static boolean isFormatCode(String text, int index) {
        return text.charAt(index) == FORMAT_MARKER && index + 1 < text.length();
    }

    private static final class Paragraph {
        private final String formatted;
        private final String visible;
        private final int[] rawBoundary;

        private Paragraph(String formatted, String visible, int[] rawBoundary) {
            this.formatted = formatted;
            this.visible = visible;
            this.rawBoundary = rawBoundary;
        }

        private static Paragraph parse(String formatted) {
            StringBuilder visible = new StringBuilder(formatted.length());
            List<Integer> boundaries = new ArrayList<Integer>();
            int boundaryStart = 0;

            for (int raw = 0; raw < formatted.length();) {
                if (isFormatCode(formatted, raw)) {
                    raw += 2;
                    continue;
                }

                boundaries.add(boundaryStart);
                visible.append(formatted.charAt(raw++));
                boundaryStart = raw;
            }
            boundaries.add(boundaryStart);

            int[] rawBoundary = new int[boundaries.size()];
            for (int i = 0; i < boundaries.size(); i++) {
                rawBoundary[i] = boundaries.get(i);
            }

            return new Paragraph(formatted, visible.toString(), rawBoundary);
        }

        private String line(int visualStart, int visualEnd) {
            int rawStart = rawBoundary[visualStart];
            int rawEnd = rawBoundary[visualEnd];
            String prefix = visualStart == 0 ? "" : formattingAt(rawStart);
            return prefix + formatted.substring(rawStart, rawEnd);
        }

        private String formattingAt(int rawEnd) {
            FormattingState state = new FormattingState();
            for (int i = 0; i < rawEnd;) {
                if (isFormatCode(formatted, i)) {
                    state.apply(formatted.charAt(i + 1));
                    i += 2;
                } else {
                    i++;
                }
            }
            return state.prefix();
        }
    }

    private static final class FormattingState {
        private char color;
        private boolean obfuscated;
        private boolean bold;
        private boolean strikethrough;
        private boolean underline;
        private boolean italic;

        private void apply(char code) {
            code = Character.toLowerCase(code);
            if ((code >= '0' && code <= '9') || (code >= 'a' && code <= 'f')) {
                color = code;
                clearStyles();
            } else if (code == 'k') {
                obfuscated = true;
            } else if (code == 'l') {
                bold = true;
            } else if (code == 'm') {
                strikethrough = true;
            } else if (code == 'n') {
                underline = true;
            } else if (code == 'o') {
                italic = true;
            } else if (code == 'r') {
                color = 0;
                clearStyles();
            }
        }

        private void clearStyles() {
            obfuscated = false;
            bold = false;
            strikethrough = false;
            underline = false;
            italic = false;
        }

        private String prefix() {
            StringBuilder result = new StringBuilder(12);
            append(result, color);
            append(result, obfuscated, 'k');
            append(result, bold, 'l');
            append(result, strikethrough, 'm');
            append(result, underline, 'n');
            append(result, italic, 'o');
            return result.toString();
        }

        private static void append(StringBuilder target, char code) {
            if (code != 0) {
                target.append(FORMAT_MARKER).append(code);
            }
        }

        private static void append(StringBuilder target, boolean active, char code) {
            if (active) {
                target.append(FORMAT_MARKER).append(code);
            }
        }
    }
}
