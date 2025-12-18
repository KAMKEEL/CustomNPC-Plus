package noppes.npcs.client.gui.util;

import noppes.npcs.client.ClientProxy;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextContainer {
    protected final static char colorChar = '\u00A7';

    //public final Pattern regexString = Pattern.compile("\"(?:[^\"\\\\]|\\\\.)*?(\"|$)|'(?:[^'\\\\]|\\\\.)*?('|$)");
    //public final Pattern regexString = Pattern.compile("\"(.*?(?<![\\\\])(\\s)*?)*?\"");
    public final Pattern regexString = Pattern.compile("([\"'])(?:(?=(\\\\?))\\2.)*?\\1", Pattern.MULTILINE);
    public final Pattern regexFunction = Pattern.compile("\\b(if|else|switch|with|for|while|in|var|const|let|throw|then|function|continue|break|foreach|return|try|catch|finally|do|this|typeof|instanceof|new)(?=[^\\w])");
    public final Pattern regexType = Pattern.compile("\\b(Array|Date|eval|hasOwnProperty|Infinity|isFinite|isNaN|isPrototypeOf|Math|NaN|Number|Object|prototype|String|toString|undefined|valueOf|parseInt|parseFloat|print)(?=[^\\w])");
    // Include letters, numbers and underscore so single chars like "x" and numeric tokens match
    public final Pattern regexWord = Pattern.compile("[\\p{L}\\p{N}_-]+|\\n|$");
    public final Pattern regexNumber = Pattern.compile("\\b-?(?:0[xX][\\dA-Fa-f]+|0[bB][01]+|0[oO][0-7]+|\\d*\\.?\\d+(?:[Ee][+-]?\\d+)?(?:[fFbBdDlLsS])?|NaN|null|Infinity|unidentified|true|false)\\b");
    public final Pattern regexComment = Pattern.compile("\\/\\*[\\s\\S]*?(?:\\*\\/|$)|\\/\\/.*|#.*");

    public String text;

    public List<MarkUp> makeup = new ArrayList<MarkUp>();
    public List<LineData> lines = new ArrayList<LineData>();

    public int lineHeight;
    public int totalHeight;
    public int visibleLines = 1;
    public int linesCount;


    public TextContainer(String text) {
        this.text = text.replaceAll("\\r?\\n|\\r", "\n");
    }

    public void init(int width, int height) {
        lineHeight = ClientProxy.Font.height();
        if (lineHeight == 0)
            lineHeight = 12;
        String[] split = text.split("\n");

        int totalChars = 0;
        for (String l : split) {
            StringBuilder line = new StringBuilder();
            Matcher m = regexWord.matcher(l);
            int i = 0;
            while (m.find()) {
                String word = l.substring(i, m.start());
                if (ClientProxy.Font.width(line + word) > width - 10) {
                    lines.add(new LineData(line.toString(), totalChars, totalChars + line.length()));
                    totalChars += line.length();
                    line = new StringBuilder();
                }
                line.append(word);
                i = m.start();
            }
            lines.add(new LineData(line.toString(), totalChars, totalChars + line.length() + 1));

            totalChars += line.length() + 1;
        }
        linesCount = lines.size();
        totalHeight = linesCount * lineHeight;
        visibleLines = Math.max(height / lineHeight, 1);
    }

    public void formatCodeText() {
        MarkUp markup = null;
        int start = 0;
        while ((markup = getNextMatching(start)) != null) {
            makeup.add(markup);
            start = markup.end;
        }
    }

    protected MarkUp getNextMatching(int start) {
        MarkUp markup = null;
        String s = text.substring(start);
        Matcher matcher = regexNumber.matcher(s);
        if (matcher.find()) {
            markup = new MarkUp(matcher.start(), matcher.end(), '6', 0);
        }
        matcher = regexFunction.matcher(s);
        if (matcher.find()) {
            MarkUp markup2 = new MarkUp(matcher.start(), matcher.end(), '2', 0);
            if (compareMarkUps(markup, markup2)) {
                markup = markup2;
            }
        }
        matcher = regexType.matcher(s);
        if (matcher.find()) {
            MarkUp markup2 = new MarkUp(matcher.start(), matcher.end(), '3', 0);
            if (compareMarkUps(markup, markup2)) {
                markup = markup2;
            }
        }
        matcher = regexString.matcher(s);
        if (matcher.find()) {
            MarkUp markup2 = new MarkUp(matcher.start(), matcher.end(), '4', 7);
            if (compareMarkUps(markup, markup2)) {
                markup = markup2;
            }
        }
        matcher = regexComment.matcher(s);
        if (matcher.find()) {
            MarkUp markup2 = new MarkUp(matcher.start(), matcher.end(), '8', 7);
            if (compareMarkUps(markup, markup2)) {
                markup = markup2;
            }
        }

        if (markup != null) {
            markup.start += start;
            markup.end += start;
        }
        return markup;
    }

    public boolean compareMarkUps(MarkUp mu1, MarkUp mu2) {
        if (mu1 == null)
            return true;
        return mu1.start > mu2.start;
    }

    public void addMakeUp(int start, int end, char c, int level) {
        if (!removeConflictingMarkUp(start, end, level))
            return;
        makeup.add(new MarkUp(start, end, c, level));
    }

    protected boolean removeConflictingMarkUp(int start, int end, int level) {
        List<MarkUp> conflicting = new ArrayList<MarkUp>();
        for (MarkUp m : makeup) {
            if (start >= m.start && start <= m.end || end >= m.start && end <= m.end || start < m.start && end > m.start) {
                if (level < m.level || level == m.level && m.start <= start)
                    return false;
                conflicting.add(m);
            }
        }
        makeup.removeAll(conflicting);
        return true;
    }

    class LineData {
        public String text;
        public int start, end;

        public LineData(String text, int start, int end) {
            this.text = text;
            this.start = start;
            this.end = end;
        }

        public void drawString(int x, int y, int color) {
            ClientProxy.Font.drawString(this.getFormattedString(), x, y, color);
        }

        public String getFormattedString() {
            StringBuilder builder = new StringBuilder(text);
            int found = 0;
            for (MarkUp entry : makeup) {
                if (entry.start >= start && entry.start < end) {
                    builder.insert(entry.start - start + found * 2, Character.toString(colorChar) + Character.toString(entry.c));
                    found++;
                }
                if (entry.start < start && entry.end > start) {
                    builder.insert(0, Character.toString(colorChar) + Character.toString(entry.c));
                    found++;
                }
                if (entry.end >= start && entry.end < end) {
                    builder.insert(entry.end - start + found * 2, Character.toString(colorChar) + Character.toString('r'));
                    found++;
                }
            }
            return builder.toString();
        }
    }

    class MarkUp {
        public int start, end, level;
        public char c;

        public MarkUp(int start, int end, char c, int level) {
            this.start = start;
            this.end = end;
            this.c = c;
            this.level = level;
        }
    }
}
