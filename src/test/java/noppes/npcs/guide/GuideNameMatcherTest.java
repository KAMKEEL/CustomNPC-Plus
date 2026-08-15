package noppes.npcs.guide;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class GuideNameMatcherTest {

    private static GuideNameMatcher.Candidate c(String name, double distSq) {
        return new GuideNameMatcher.Candidate(name, distSq, name);
    }

    @Test
    public void exactMatchWins() {
        List<GuideNameMatcher.Candidate> all = Arrays.asList(
            c("铁匠学徒", 1.0),
            c("铁匠", 900.0)
        );
        GuideNameMatcher.Result r = GuideNameMatcher.match(all, "铁匠");

        assertEquals("铁匠", r.chosen.name);
        assertEquals(1, r.matchCount);
    }

    @Test
    public void amongEqualMatchesTheNearestWins() {
        List<GuideNameMatcher.Candidate> all = Arrays.asList(
            c("铁匠", 900.0),
            c("铁匠", 16.0),
            c("铁匠", 400.0)
        );
        GuideNameMatcher.Result r = GuideNameMatcher.match(all, "铁匠");

        assertEquals(16.0, r.chosen.distanceSq, 1.0e-9);
        assertEquals("同名候选总数应该报出来", 3, r.matchCount);
    }

    @Test
    public void fallsBackToCaseInsensitiveWhenNoExactMatch() {
        List<GuideNameMatcher.Candidate> all = Arrays.asList(c("Blacksmith", 100.0));
        GuideNameMatcher.Result r = GuideNameMatcher.match(all, "blacksmith");

        assertEquals("Blacksmith", r.chosen.name);
    }

    @Test
    public void caseInsensitiveLevelBeatsSubstringLevel() {
        List<GuideNameMatcher.Candidate> all = Arrays.asList(
            c("BlacksmithApprentice", 1.0),
            c("Blacksmith", 900.0)
        );
        GuideNameMatcher.Result r = GuideNameMatcher.match(all, "blacksmith");

        assertEquals("忽略大小写这一级应该先命中，不该退到子串", "Blacksmith", r.chosen.name);
        assertEquals(1, r.matchCount);
    }

    @Test
    public void fallsBackToSubstringLast() {
        List<GuideNameMatcher.Candidate> all = Arrays.asList(c("村口的铁匠老王", 100.0));
        GuideNameMatcher.Result r = GuideNameMatcher.match(all, "铁匠");

        assertEquals("村口的铁匠老王", r.chosen.name);
    }

    @Test
    public void colorCodesAreIgnoredInSubstringMatching() {
        List<GuideNameMatcher.Candidate> all = Arrays.asList(c("§6村口的§c铁匠", 100.0));
        GuideNameMatcher.Result r = GuideNameMatcher.match(all, "铁匠");

        assertEquals("§6村口的§c铁匠", r.chosen.name);
    }

    @Test
    public void payloadIsCarriedThrough() {
        Object marker = new Object();
        List<GuideNameMatcher.Candidate> all =
            Arrays.asList(new GuideNameMatcher.Candidate("铁匠", 1.0, marker));

        assertSame(marker, GuideNameMatcher.match(all, "铁匠").chosen.payload);
    }

    @Test
    public void noMatchYieldsNullChosen() {
        List<GuideNameMatcher.Candidate> all = Arrays.asList(c("铁匠", 1.0));
        GuideNameMatcher.Result r = GuideNameMatcher.match(all, "药剂师");

        assertNull(r.chosen);
        assertEquals(0, r.matchCount);
    }

    @Test
    public void emptyInputsYieldNullChosen() {
        assertNull(GuideNameMatcher.match(null, "铁匠").chosen);
        assertNull(GuideNameMatcher.match(new ArrayList<GuideNameMatcher.Candidate>(), "铁匠").chosen);
        assertNull(GuideNameMatcher.match(Arrays.asList(c("铁匠", 1.0)), "").chosen);
        assertNull(GuideNameMatcher.match(Arrays.asList(c("铁匠", 1.0)), null).chosen);
    }

    @Test
    public void stripColorsRemovesFormattingPairs() {
        assertEquals("村口的铁匠", GuideNameMatcher.stripColors("§6村口的§c铁匠"));
        assertEquals("abc", GuideNameMatcher.stripColors("§labc"));
        assertEquals("", GuideNameMatcher.stripColors(""));
        assertEquals("", GuideNameMatcher.stripColors(null));
    }

    @Test
    public void trailingLoneSectionSignIsKept() {
        // 末尾孤立的 § 后面没有格式字符，不该越界，原样保留即可。
        assertEquals("abc§", GuideNameMatcher.stripColors("abc§"));
    }
}
