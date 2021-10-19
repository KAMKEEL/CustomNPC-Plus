//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package noppes.npcs.client.gui.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import noppes.npcs.config.TrueTypeFont;

public class TextContainer {
    private static final char colorChar = '\uffff';
    private static final Comparator<TextContainer.MarkUp> MarkUpComparator = (o1, o2) -> {
        return o1.start > o2.start?1:(o1.start < o2.start?-1:0);
    };
    public final Pattern regexString = Pattern.compile("([\"\'])(?:(?=(\\\\?))\\2.)*?\\1", 8);
    public final Pattern regexFunction = Pattern.compile("\\b(if|else|switch|with|for|while|in|var|const|let|throw|then|function|continue|break|foreach|return|try|catch|finally|do|this|typeof|instanceof|new)(?=[^\\w])");
    public final Pattern regexWord = Pattern.compile("[\\p{L}-]+|\\n|$");
    public final Pattern regexNumber = Pattern.compile("\\b-?(?:0[xX][\\dA-Fa-f]+|0[bB][01]+|0[oO][0-7]+|\\d*\\.?\\d+(?:[Ee][+-]?\\d+)?(?:[fFbBdDlLsS])?|NaN|null|Infinity|unidentified|true|false)\\b");
    public final Pattern regexComment = Pattern.compile("\\/\\*[\\s\\S]*?(?:\\*\\/|$)|\\/\\/.*|#.*");
    public String text;
    public List<TextContainer.MarkUp> makeup = new ArrayList();
    public List<TextContainer.LineData> lines = new ArrayList();
    private TrueTypeFont font;
    public int lineHeight;
    public int totalHeight;
    public int visibleLines = 1;
    public int linesCount;

    public TextContainer(String text) {
        this.text = text;
        text.replaceAll("\\r?\\n|\\r", "\n");
        double l = 1.0D;
    }

    public void init(TrueTypeFont font, int width, int height) {
        this.font = font;
        this.lineHeight = font.height(this.text);
        if(this.lineHeight == 0) {
            this.lineHeight = 12;
        }

        String[] split = this.text.split("\n");
        int totalChars = 0;
        String[] var6 = split;
        int var7 = split.length;

        for(int var8 = 0; var8 < var7; ++var8) {
            String l = var6[var8];
            StringBuilder line = new StringBuilder();
            Matcher m = this.regexWord.matcher(l);

            for(int i = 0; m.find(); i = m.start()) {
                String word = l.substring(i, m.start());
                if(font.width(line + word) > width - 10) {
                    this.lines.add(new TextContainer.LineData(line.toString(), totalChars, totalChars + line.length()));
                    totalChars += line.length();
                    line = new StringBuilder();
                }

                line.append(word);
            }

            this.lines.add(new TextContainer.LineData(line.toString(), totalChars, totalChars + line.length() + 1));
            totalChars += line.length() + 1;
        }

        this.linesCount = this.lines.size();
        this.totalHeight = this.linesCount * this.lineHeight;
        this.visibleLines = Math.max(height / this.lineHeight, 1);
    }

    public void formatCodeText() {
        TextContainer.MarkUp markup = null;

        for(int start = 0; (markup = this.getNextMatching(start)) != null; start = markup.end) {
            this.makeup.add(markup);
        }

    }

    private TextContainer.MarkUp getNextMatching(int start) {
        TextContainer.MarkUp markup = null;
        String s = this.text.substring(start);
        Matcher matcher = this.regexNumber.matcher(s);
        if(matcher.find()) {
            markup = new TextContainer.MarkUp(matcher.start(), matcher.end(), '6', 0);
        }

        matcher = this.regexFunction.matcher(s);
        TextContainer.MarkUp markup2;
        if(matcher.find()) {
            markup2 = new TextContainer.MarkUp(matcher.start(), matcher.end(), '2', 0);
            if(this.compareMarkUps(markup, markup2)) {
                markup = markup2;
            }
        }

        matcher = this.regexString.matcher(s);
        if(matcher.find()) {
            markup2 = new TextContainer.MarkUp(matcher.start(), matcher.end(), '4', 7);
            if(this.compareMarkUps(markup, markup2)) {
                markup = markup2;
            }
        }

        matcher = this.regexComment.matcher(s);
        if(matcher.find()) {
            markup2 = new TextContainer.MarkUp(matcher.start(), matcher.end(), '8', 7);
            if(this.compareMarkUps(markup, markup2)) {
                markup = markup2;
            }
        }

        if(markup != null) {
            markup.start += start;
            markup.end += start;
        }

        return markup;
    }

    public boolean compareMarkUps(TextContainer.MarkUp mu1, TextContainer.MarkUp mu2) {
        return mu1 == null?true:mu1.start > mu2.start;
    }

    public void addMakeUp(int start, int end, char c, int level) {
        if(this.removeConflictingMarkUp(start, end, level)) {
            this.makeup.add(new TextContainer.MarkUp(start, end, c, level));
        }
    }

    private boolean removeConflictingMarkUp(int start, int end, int level) {
        ArrayList conflicting = new ArrayList();
        Iterator var5 = this.makeup.iterator();

        while(true) {
            TextContainer.MarkUp m;
            do {
                if(!var5.hasNext()) {
                    this.makeup.removeAll(conflicting);
                    return true;
                }

                m = (TextContainer.MarkUp)var5.next();
            } while((start < m.start || start > m.end) && (end < m.start || end > m.end) && (start >= m.start || end <= m.start));

            if(level < m.level || level == m.level && m.start <= start) {
                return false;
            }

            conflicting.add(m);
        }
    }

    public String getFormattedString() {
        StringBuilder builder = new StringBuilder(this.text);
        Iterator var2 = this.makeup.iterator();

        while(var2.hasNext()) {
            TextContainer.MarkUp entry = (TextContainer.MarkUp)var2.next();
            builder.insert(entry.start, Character.toString('\uffff') + Character.toString(entry.c));
            builder.insert(entry.end, Character.toString('\uffff') + Character.toString('r'));
        }

        return builder.toString();
    }

    class MarkUp {
        public int start;
        public int end;
        public int level;
        public char c;

        public MarkUp(int start, int end, char c, int level) {
            this.start = start;
            this.end = end;
            this.c = c;
            this.level = level;
        }
    }

    class LineData {
        public String text;
        public int start;
        public int end;

        public LineData(String text, int start, int end) {
            this.text = text;
            this.start = start;
            this.end = end;
        }

        public String getFormattedString() {
            StringBuilder builder = new StringBuilder(this.text);
            int found = 0;
            Iterator var3 = TextContainer.this.makeup.iterator();

            while(var3.hasNext()) {
                TextContainer.MarkUp entry = (TextContainer.MarkUp)var3.next();
                if(entry.start >= this.start && entry.start < this.end) {
                    builder.insert(entry.start - this.start + found * 2, Character.toString('\uffff') + Character.toString(entry.c));
                    ++found;
                }

                if(entry.start < this.start && entry.end > this.start) {
                    builder.insert(0, Character.toString('\uffff') + Character.toString(entry.c));
                    ++found;
                }

                if(entry.end >= this.start && entry.end < this.end) {
                    builder.insert(entry.end - this.start + found * 2, Character.toString('\uffff') + Character.toString('r'));
                    ++found;
                }
            }

            return builder.toString();
        }
    }
}
