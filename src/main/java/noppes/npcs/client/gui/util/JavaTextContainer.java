package noppes.npcs.client.gui.util;

import noppes.npcs.client.ClientProxy;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavaTextContainer extends TextContainer {
    public static final char colorChar = '\u00A7';

    public static final Pattern MODIFIER = Pattern.compile(
            "\\b(public|protected|private|static|final|abstract|synchronized|native|default)\\b");
    public static final Pattern KEYWORD = Pattern.compile(
            "\\b(null|boolean|int|float|double|long|char|byte|short|void|if|else|switch|case|for|while|do|try|catch|finally|return|throw|var|let|const|function|continue|break|this|new|typeof|instanceof)\\b");

    public static final Pattern TYPE_DECL = Pattern.compile(
            "\\b([A-Z][a-zA-Z0-9_]*)" +   // Group 1 → main type
                    "\\s*(<([^>]+)>)?" +          // Group 2 → <…>, Group 3 → inner type
                    "\\s+[a-zA-Z_][a-zA-Z0-9_]*" // variable name
    );

    public static final Pattern NEW_TYPE = Pattern.compile("\\bnew\\s+([A-Z][a-zA-Z0-9_]*)");

    public static final Pattern METHOD_DECL = Pattern.compile(
            "\\b([a-zA-Z_][a-zA-Z0-9_<>\\[\\]]*)\\s+" + // return type
                    "([a-zA-Z_][a-zA-Z0-9_]*)\\s*\\(" // method name
    );
    public static final Pattern METHOD_CALL = Pattern.compile("([a-zA-Z_][a-zA-Z0-9_]*)\\s*\\(");

    public static final Pattern STRING = Pattern.compile("([\"'])(?:(?=(\\\\?))\\2.)*?\\1");
    public static final Pattern COMMENT = Pattern.compile("/\\*[\\s\\S]*?(?:\\*/|$)|//.*|#.*");
    public static final Pattern NUMBER = Pattern.compile(
            "\\b-?(?:0[xX][\\dA-Fa-f]+|0[bB][01]+|0[oO][0-7]+|\\d*\\.?\\d+(?:[Ee][+-]?\\d+)?(?:[fFbBdDlLsS])?|NaN|null|Infinity|true|false)\\b");

    public String text;
    public List<LineData> lines = new ArrayList<>();
    public int lineHeight;
    public int totalHeight;
    public int visibleLines = 1;

    public JavaTextContainer(String text) {
        super(text);
        this.text = text.replaceAll("\\r?\\n|\\r", "\n");
    }

    public void init(int width, int height) {
        lineHeight = ClientProxy.Font.height();
        if (lineHeight == 0)
            lineHeight = 12;

        lines.clear();

        String[] split = text.split("\n");
        int charIndex = 0;

        for (String rawLine : split) {
            StringBuilder currentLine = new StringBuilder();
            int startIndex = charIndex;

            int i = 0;
            Matcher m = Pattern.compile(".").matcher(rawLine); // iterate each char
            while (m.find()) {
                String c = rawLine.substring(m.start(), m.end());
                if (ClientProxy.Font.width(currentLine.toString() + c) > width - 10) {
                    LineData lineData = new LineData(currentLine.toString(), startIndex,
                            startIndex + currentLine.length());
                    startIndex += currentLine.length();
                    lines.add(lineData);
                    currentLine = new StringBuilder();
                }
                currentLine.append(c);
                i = m.end();
            }

            if (currentLine.length() > 0) {
                LineData lineData = new LineData(currentLine.toString(), startIndex, startIndex + currentLine.length());
                lines.add(lineData);
            }

            charIndex += rawLine.length() + 1; // +1 for newline
        }

        totalHeight = lines.size() * lineHeight;
        visibleLines = Math.max(height / lineHeight, 1);
    }

    public void formatCodeText() {
        // Step 1: Tokenize the full text
        List<Mark> marks = new ArrayList<>();

        collectPatternMatches(marks, COMMENT, TokenType.COMMENT);
        collectPatternMatches(marks, STRING, TokenType.STRING);
        collectPatternMatches(marks, KEYWORD, TokenType.KEYWORD);
        collectPatternMatches(marks, MODIFIER, TokenType.MODIFIER);

        collectTypeDeclarations(marks);
        collectPatternMatches(marks, NEW_TYPE, TokenType.NEW_TYPE, 1);

        collectMethodDeclarations(marks);
        collectPatternMatches(marks, METHOD_CALL, TokenType.METHOD_CALL);

        collectPatternMatches(marks, NUMBER, TokenType.NUMBER);

        marks = resolveConflicts(marks);

        // Step 2: Clear existing tokens
        for (LineData line : lines) {
            line.tokens.clear();
        }

        // Step 3: Assign tokens to the correct lines
        for (LineData line : lines) {
            int cursor = line.start;

            for (Mark mark : marks) {
                if (mark.end <= line.start || mark.start >= line.end)
                    continue;

                int tokenStart = Math.max(mark.start, line.start);
                int tokenEnd = Math.min(mark.end, line.end);

                // plain text before token
                if (cursor < tokenStart) {
                    line.tokens.add(
                            new Token(text.substring(cursor, tokenStart), TokenType.DEFAULT, cursor, tokenStart));
                }

                // token text
                line.tokens.add(new Token(text.substring(tokenStart, tokenEnd), mark.type, tokenStart, tokenEnd));

                cursor = tokenEnd;
            }

            // trailing plain text
            if (cursor < line.end) {
                line.tokens.add(new Token(text.substring(cursor, line.end), TokenType.DEFAULT, cursor, line.end));
            }
        }
    }

    private void collectMethodDeclarations(List<Mark> marks) {
        Matcher m = METHOD_DECL.matcher(text);
        while (m.find()) { // method name
            marks.add(new Mark(m.start(2), m.end(2), TokenType.METHOD_DECARE));
            // return type
            marks.add(new Mark(m.start() + m.group(1).length(), m.start(2) - 1, TokenType.TYPE_DECL));
        }
    }

    private void collectTypeDeclarations(List<Mark> marks) {
        Matcher m = TYPE_DECL.matcher(text);
        while (m.find()) { // method name
            // main type
            marks.add(new Mark(m.start(1), m.end(1), TokenType.TYPE_DECL));

            if (m.group(2) != null) {
                int start = m.start(2);
                int end = m.end(2);
                // Color < and > as white
                marks.add(new Mark(start, start + 1, TokenType.DEFAULT)); // <
                marks.add(new Mark(end - 1, end, TokenType.DEFAULT));       // >

                // Inner type (String)
                if (m.group(3) != null) {
                    marks.add(new Mark(m.start(3), m.end(3), TokenType.NEW_TYPE)); // or another color
                }
            }
        }
    }

    private void collectPatternMatches(List<Mark> marks, Pattern pattern, TokenType type, int group) {
        Matcher m = pattern.matcher(text);
        while (m.find()) {
            marks.add(new Mark(m.start(group), m.end(group), type));
        }
    }

    private void collectPatternMatches(List<Mark> marks, Pattern pattern, TokenType type) {
        collectPatternMatches(marks, pattern, type, 0);
    }

    private List<Mark> resolveConflicts(List<Mark> marks) {
        // Sort by start index first, then by descending priority
        marks.sort((a, b) -> {
            if (a.start != b.start)
                return Integer.compare(a.start, b.start);
            return Integer.compare(b.type.priority, a.type.priority);
        });

        List<Mark> result = new ArrayList<>();
        for (Mark m : marks) {
            boolean overlap = false;
            for (Mark r : result) {
                if (m.start < r.end && m.end > r.start && r.type.priority >= m.type.priority) {
                    overlap = true;
                    break;
                }
            }
            if (!overlap)
                result.add(m);
        }
        return result;
    }
    
    public class LineData {
        public String text;
        public int start, end;
        public List<Token> tokens = new ArrayList<>();

        public LineData(String text, int startIndex, int end) {
            this.text = text;
            this.start = startIndex;
            this.end = end;
        }

        public void drawString(int x, int y, int color) {
            StringBuilder builder = new StringBuilder();
            int lastIndex = 0;

            for (Token t : tokens) {
                int tokenStart = text.indexOf(t.text, lastIndex); // find actual index in line
                if (tokenStart > lastIndex) {
                    // append the text before the token (spaces, punctuation)
                    builder.append(text, lastIndex, tokenStart);
                }
                builder.append(colorChar).append(t.type.color).append(t.text).append(colorChar).append('f');
                lastIndex = tokenStart + t.text.length();
            }

            // append any remaining text after the last token
            if (lastIndex < text.length()) {
                builder.append(text.substring(lastIndex));
            }

            ClientProxy.Font.drawString(builder.toString(), x, y, color);
        }
    }

    public static class Token {
        public String text;
        public TokenType type;
        public int start, end; // start/end relative to the line

        public Token(String text, TokenType type, int start, int end) {
            this.text = text;
            this.type = type;
            this.start = start;
            this.end = end;
        }

        @Override
        public String toString() {
            return "Token{'" + text + '\'' + ", " + type + ", (color=" + type.color + ", priority=" + type.priority + ")" + '}';
        }
    }

    private static class Mark {
        public int start, end;
        public TokenType type;

        public Mark(int start, int end, TokenType type) {
            this.start = start;
            this.end = end;
            this.type = type;
        }
        
    }

    public enum TokenType {
        COMMENT('7', 130),
        STRING('5', 120),
        KEYWORD('c', 100),
        MODIFIER('6', 90),
        NEW_TYPE('d', 80),
        TYPE_DECL('3', 70),
        METHOD_DECARE('2', 60),
        METHOD_CALL('a', 50),
        NUMBER('7', 40),
        VARIABLE('f', 30),
        DEFAULT('f', 0);

        public final char color;
        public final int priority;

        TokenType(char color, int priority) {
            this.color = color;
            this.priority = priority;
        }
    }
}
