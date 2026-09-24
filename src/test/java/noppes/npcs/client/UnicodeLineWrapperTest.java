package noppes.npcs.client;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class UnicodeLineWrapperTest {
    private static final UnicodeLineWrapper.WidthMeasurer CODE_POINT_WIDTH =
        new UnicodeLineWrapper.WidthMeasurer() {
            @Override
            public int width(String text) {
                String visible = stripFormatting(text);
                return visible.codePointCount(0, visible.length());
            }
        };

    @Test
    public void wrapsUnspacedChineseText() {
        assertLines(Arrays.asList("这是中文", "没有空格"), wrap("这是中文没有空格", 4));
    }

    @Test
    public void observesCjkClosingAndOpeningPunctuationRules() {
        assertLines(Arrays.asList("你", "好，", "世", "界。"), wrap("你好，世界。", 2));
        assertLines(Arrays.asList("（你", "好）", "世界"), wrap("（你好）世界", 2));
    }

    @Test
    public void keepsLatinWordsTogetherInMixedText() {
        assertLines(Arrays.asList("中文", "English", "混排"), wrap("中文 English 混排", 7));
    }

    @Test
    public void splitsAWordOnlyWhenItHasNoFittingLegalBreak() {
        assertLines(Arrays.asList("abcd", "efgh", "i"), wrap("abcdefghi", 4));
    }

    @Test
    public void acceptsAnExactWidthBoundary() {
        assertLines(Arrays.asList("abcd", "ef"), wrap("abcd ef", 4));
        assertLines(Arrays.asList("abcd"), wrap("abcd", 4));
    }

    @Test
    public void nonPositiveWidthsStillAdvanceOneGraphemeAtATime() {
        assertLines(Arrays.asList("中", "文"), wrap("中文", 0));
        assertLines(Arrays.asList("A", "B"), wrap("AB", -1));
    }

    @Test
    public void normalizesOrdinarySpacesAndDropsSpacesAtSoftBreaks() {
        assertLines(Arrays.asList("alpha beta"), wrap("  alpha   beta  ", 100));
        assertLines(Arrays.asList("alpha", "beta"), wrap("  alpha   beta  ", 5));
    }

    @Test
    public void recognizesLfCrAndCrLfAsHardBreaks() {
        assertLines(Arrays.asList("a", "b", "c", "d"), wrap("a\r\nb\rc\nd", 100));
    }

    @Test
    public void preservesConsecutiveHardBreaksButNotAnExtraTrailingLine() {
        assertLines(Arrays.asList("one", "", "two"), wrap("one\n\ntwo", 100));
        assertLines(Arrays.asList("one"), wrap("one\n", 100));
        assertLines(Arrays.asList("one", ""), wrap("one\n\n", 100));
        assertLines(Arrays.asList(""), wrap("\n", 100));
    }

    @Test
    public void continuesColorAndStyleAcrossSoftBreaks() {
        assertLines(Arrays.asList("§a§lAB", "§a§lCD"), wrap("§a§lABCD", 2));
        assertLines(Arrays.asList("§aA", "§a§bB", "§bC"), wrap("§aA§bBC", 1));
        assertLines(Arrays.asList("§lA", "§lB", "§l§rC"), wrap("§lAB§rC", 1));
    }

    @Test
    public void doesNotContinueFormattingAcrossHardBreaks() {
        assertLines(Arrays.asList("§aA", "B"), wrap("§aA\nB", 100));
    }

    @Test
    public void formattingCodesAreZeroWidthAndDoNotCreateFakeLines() {
        assertLines(Arrays.asList("§aAB"), wrap("§aAB", 2));
        assertLines(Arrays.<String>asList(), wrap("§a", 2));
        assertLines(Arrays.asList(""), wrap("§a\n§b", 2));

        for (String line : wrap("§a§lABCD", 1)) {
            assertFalse("format-only line: " + line, stripFormatting(line).isEmpty());
        }
    }

    @Test
    public void doesNotSplitCombiningCharacters() {
        assertLines(Arrays.asList("e\u0301", "x"), wrap("e\u0301x", 1));
    }

    @Test
    public void doesNotSplitSurrogatePairs() {
        assertLines(Arrays.asList("😀", "x"), wrap("😀x", 1));
    }

    @Test
    public void doesNotSplitEmojiModifiersOrVariationSelectors() {
        assertLines(Arrays.asList("👍🏽", "x"), wrap("👍🏽x", 1));
        assertLines(Arrays.asList("❤️", "x"), wrap("❤️x", 1));
    }

    @Test
    public void doesNotSplitZwjEmojiSequences() {
        String family = "👨‍👩‍👧‍👦";
        assertLines(Arrays.asList(family, "x"), wrap(family + "x", 1));
    }

    @Test
    public void pairsRegionalIndicatorsAndDoesNotOverJoinPlainZwjText() {
        assertLines(Arrays.asList("🇦🇧", "🇨"), wrap("🇦🇧🇨", 1));
        assertLines(Arrays.asList("a", "‍", "b"), wrap("a‍b", 1));
    }

    private static List<String> wrap(String text, int maxWidth) {
        return UnicodeLineWrapper.wrap(text, maxWidth, CODE_POINT_WIDTH);
    }

    private static void assertLines(List<String> expected, List<String> actual) {
        assertEquals(expected, actual);
    }

    private static String stripFormatting(String text) {
        StringBuilder visible = new StringBuilder(text.length());
        for (int i = 0; i < text.length();) {
            if (text.charAt(i) == '§' && i + 1 < text.length()) {
                i += 2;
            } else {
                visible.append(text.charAt(i++));
            }
        }
        return visible.toString();
    }
}
