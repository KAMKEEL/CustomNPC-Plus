package noppes.npcs.client.gui.util.script;

import noppes.npcs.client.ClientProxy;
import noppes.npcs.client.gui.util.TextContainer;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavaTextContainer extends TextContainer {

    public static final Pattern MODIFIER = Pattern.compile(
            "\\b(public|protected|private|static|final|abstract|synchronized|native|default)\\b");
    public static final Pattern KEYWORD = Pattern.compile(
            "\\b(null|boolean|int|float|double|long|char|byte|short|void|if|else|switch|case|for|while|do|try|catch|finally|return|throw|var|let|const|function|continue|break|this|new|typeof|instanceof)\\b");

    public static final Pattern TYPE_DECL = Pattern.compile("\\b([A-Za-z_][a-zA-Z0-9_]*)" + // Group 1 → main type
            "\\s*(<([^>]+)>)?" + // Group c2 → <…>, Group 3 → inner type
                    "\\s+[a-zA-Z_][a-zA-Z0-9_]*" // variable name
    );

    public static final Pattern NEW_TYPE = Pattern.compile("\\bnew\\s+([A-Za-z_][a-zA-Z0-9_]*)");

    public static final Pattern METHOD_DECL = Pattern.compile("\\b([A-Za-z_][a-zA-Z0-9_<>\\[\\]]*)\\s+" + // return type
            "([a-zA-Z_][a-zA-Z0-9_]*)\\s*\\(" // method name
    );
    public static final Pattern METHOD_CALL = Pattern.compile("([a-zA-Z_][a-zA-Z0-9_]*)\\s*\\(");

    // Class-level global fields
    public static final Pattern GLOBAL_FIELD_DECL = Pattern.compile(
            "\\b([A-Za-z_][a-zA-Z0-9_<>\\[\\]]*)\\s+([a-zA-Z_][a-zA-Z0-9_]*)\\s*(=|;)");

    // Local fields inside methods (simplified)
    public static final Pattern LOCAL_FIELD_DECL = Pattern.compile(
            "\\b([A-Z][a-zA-Z0-9_<>\\[\\]]*|[a-z][a-zA-Z0-9_]*)\\s+([a-zA-Z_][a-zA-Z0-9_]*)\\s*(=|;)");

    public static final Pattern STRING = Pattern.compile("([\"'])(?:(?=(\\\\?))\\2.)*?\\1");
    public static final Pattern COMMENT = Pattern.compile("/\\*[\\s\\S]*?(?:\\*/|$)|//.*|#.*");
    public static final Pattern NUMBER = Pattern.compile(
            "\\b-?(?:0[xX][\\dA-Fa-f]+|0[bB][01]+|0[oO][0-7]+|\\d*\\.?\\d+(?:[Ee][+-]?\\d+)?(?:[fFbBdDlLsS])?|NaN|null|Infinity|true|false)\\b");

    public List<LineData> lines = new ArrayList<>();
    public List<MethodBlock> methodBlocks = new ArrayList<>();

    public JavaTextContainer(String text) {
        super(text);
    }

    public void init(String text,int width, int height) {
        this.text = text == null ? "" : text.replaceAll("\\r?\\n|\\r", "\n");
        lines.clear();
        String[] split = text.split("\n",-1);

        int totalChars = 0;
        for (String l : split) {
            StringBuilder line = new StringBuilder();
            // Break the source line `l` into layout-friendly segments using `regexWord`.
            // `regexWord` finds word tokens (letters/numbers/underscore/hyphen), newlines
            // or the end-of-line. The loop takes substrings from the previous match
            // index `i` up to the current `m.start()` so each `word` contains the
            // next token plus any following delimiters (spaces, punctuation).
            // This lets us measure and wrap at token boundaries rather than mid-word.
            Matcher m = regexWord.matcher(l);
            int i = 0;
            while (m.find()) {
                String word = l.substring(i, m.start());
                if (ClientProxy.Font.width(line + word) > width - 10) {
                    // Note: `end` is an exclusive offset into the full text (start..end).
                    // For wrapped lines we record the current `totalChars` as the start
                    // and compute the exclusive end as `start + line.length()`.
                    lines.add(new LineData(line.toString(), totalChars, totalChars += line.length()));
                    line = new StringBuilder();
                }
                line.append(word);
                i = m.start();
            }
            lines.add(new LineData(line.toString(), totalChars, totalChars += line.length() + 1));
        }
        linesCount = lines.size();
        totalHeight = linesCount * lineHeight;
        // Number of fully-visible lines that fit in the given viewport height.
        // Use floor division and ensure at least 1 line is visible.
        // Don't forget -1, fixes enter auto-scrolling properly
        visibleLines = Math.max(height / lineHeight - 1, 1);
    }

    private List<String> globalFields = new ArrayList<>();
    private List<String> localFields = new ArrayList<>();

    private void collectFields() {
        globalFields.clear();
        localFields.clear();
        methodBlocks.clear();

        // Extract method blocks first
        methodBlocks = MethodBlock.collectMethodBlocks(text);

        // Global fields (excluding those inside methods)
        Matcher mGlobal = GLOBAL_FIELD_DECL.matcher(text);
        while (mGlobal.find()) {
            String varName = mGlobal.group(2);
            int varPosition = mGlobal.start(2);

            // Check if this variable is inside a method
            boolean isInsideMethod = false;
            for (MethodBlock block : methodBlocks) {
                if (block.containsPosition(varPosition)) {
                    isInsideMethod = true;
                    break;
                }
            }

            // Only add as global if it's not inside any method
            if (!isInsideMethod) {
                globalFields.add(varName);
            }
        }

        // Extract local variables from each method block
        for (MethodBlock block : methodBlocks) {
            // localVariables are already extracted in MethodBlock constructor
            for (String var : block.localVariables) {
                if (!globalFields.contains(var) && !localFields.contains(var)) {
                    localFields.add(var);
                }
            }
        }
    }

    // Find which method block contains a given position
    private MethodBlock findMethodBlockAtPosition(int position) {
        for (MethodBlock block : methodBlocks) {
            if (block.containsPosition(position)) {
                return block;
            }
        }
        return null;
    }

    private void highlightVariableReferences(List<Mark> marks) {
        // Known Java keywords and types that shouldn't be flagged as undefined
        java.util.Set<String> knownIdentifiers = new java.util.HashSet<>(java.util.Arrays.asList(
            // Primitive types
            "boolean", "int", "float", "double", "long", "char", "byte", "short", "void",
            // Keywords
            "null", "true", "false", "if", "else", "switch", "case", "for", "while", "do",
            "try", "catch", "finally", "return", "throw", "var", "let", "const", "function",
            "continue", "break", "this", "new", "typeof", "instanceof", "class", "interface",
            "extends", "implements", "import", "package", "public", "private", "protected",
            "static", "final", "abstract", "synchronized", "native", "default", "enum",
            "throws", "super", "assert", "volatile", "transient", "strictfp", "goto",
            // Common JS/scripting keywords
            "undefined", "NaN", "Infinity", "arguments", "prototype", "constructor",
            // Common types (first letter uppercase pattern handles most)
            "String", "Object", "Array", "Math", "System", "Integer", "Double", "Float",
            "Boolean", "Long", "Byte", "Short", "Character", "List", "Map", "Set"
        ));
        
        Pattern identifier = Pattern.compile("\\b([a-zA-Z_][a-zA-Z0-9_]*)\\b");
        Matcher m = identifier.matcher(text);
        while (m.find()) {
            String name = m.group(1);
            int position = m.start(1);
            
            // Skip known keywords and types
            if (knownIdentifiers.contains(name)) continue;
            
            // Skip type names (first letter uppercase)
            if (Character.isUpperCase(name.charAt(0))) continue;
            
            // Check if inside a method
            MethodBlock methodBlock = findMethodBlockAtPosition(position);
            
            if (methodBlock != null) {
                // Inside a method - check in order: parameter, local, global
                if (methodBlock.parameters.contains(name)) {
                    marks.add(new Mark(m.start(1), m.end(1), TokenType.PARAMETER));
                } else if (methodBlock.localVariables.contains(name)) {
                    marks.add(new Mark(m.start(1), m.end(1), TokenType.LOCAL_FIELD));
                } else if (globalFields.contains(name)) {
                    marks.add(new Mark(m.start(1), m.end(1), TokenType.GLOBAL_FIELD));
                } else {
                    // Unknown variable - mark as undefined (Bug 11)
                    // But only if it looks like a variable reference (not a method call)
                    if (!isMethodCall(position) && !isTypeReference(name, position)) {
                        marks.add(new Mark(m.start(1), m.end(1), TokenType.UNDEFINED_VAR));
                    }
                }
            } else {
                // Outside any method - check global fields
                if (globalFields.contains(name)) {
                    marks.add(new Mark(m.start(1), m.end(1), TokenType.GLOBAL_FIELD));
                }
                // Don't mark as undefined outside methods - could be a type name or declaration
            }
        }
    }
    
    /**
     * Check if the identifier at this position is a method call (followed by parenthesis)
     */
    private boolean isMethodCall(int position) {
        // Skip whitespace after identifier
        int i = position;
        while (i < text.length() && Character.isLetterOrDigit(text.charAt(i)) || text.charAt(i) == '_') {
            i++;
        }
        // Skip whitespace
        while (i < text.length() && Character.isWhitespace(text.charAt(i))) {
            i++;
        }
        // Check for opening paren
        return i < text.length() && text.charAt(i) == '(';
    }
    
    /**
     * Check if this looks like a type reference (e.g., part of a declaration or generic)
     */
    private boolean isTypeReference(String name, int position) {
        // Check if preceded by 'new ', '<', or ',<space>Type'
        if (position > 4) {
            String before = text.substring(Math.max(0, position - 5), position);
            if (before.endsWith("new ") || before.endsWith("< ") || before.endsWith("<")) {
                return true;
            }
        }
        return false;
    }
    
    public void formatCodeText() {
        // Step 1: Tokenize the full text
        List<Mark> marks = new ArrayList<>();

        collectFields(); // Extract global and local fields with method scoping
        collectPatternMatches(marks, COMMENT, TokenType.COMMENT);
        collectPatternMatches(marks, STRING, TokenType.STRING);
        collectPatternMatches(marks, KEYWORD, TokenType.KEYWORD);
        collectPatternMatches(marks, MODIFIER, TokenType.MODIFIER);

        collectTypeDeclarations(marks);
        collectPatternMatches(marks, NEW_TYPE, TokenType.NEW_TYPE, 1);

        collectMethodDeclarations(marks);
        collectPatternMatches(marks, METHOD_CALL, TokenType.METHOD_CALL);

        collectPatternMatches(marks, NUMBER, TokenType.NUMBER);

        highlightVariableReferences(marks);

        marks = resolveConflicts(marks);

        // Compute indent guides based on matched braces, ignoring strings/comments
        computeIndentGuides(marks);
        
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

                tokenStart = Math.max(0, Math.min(tokenStart, text.length()));
                tokenEnd = Math.max(0, Math.min(tokenEnd, text.length()));

                // plain text before token
                if (cursor < tokenStart) {
                    int end = Math.min(tokenStart, text.length());
                    line.tokens.add(
                            new Token(text.substring(cursor, end), TokenType.DEFAULT, cursor, end));
                }

                // token text
                if (tokenStart < tokenEnd) {
                    line.tokens.add(new Token(text.substring(tokenStart, Math.min(tokenEnd, text.length())), mark.type, tokenStart, Math.min(tokenEnd, text.length())));
                }

                cursor = tokenEnd;
            }

            // trailing plain text
            if (cursor < line.end) {
                int end = Math.min(line.end, text.length());
                line.tokens.add(new Token(text.substring(cursor, end), TokenType.DEFAULT, cursor, end));
            }
        }
    }

    private void collectMethodDeclarations(List<Mark> marks) {
        Matcher m = METHOD_DECL.matcher(text);
        while (m.find()) { // method name
            marks.add(new Mark(m.start(2), m.end(2), TokenType.METHOD_DECARE));
            // return type
            marks.add(new Mark(m.start(1), m.end(1), TokenType.TYPE_DECL));
        }
    }

    private void collectTypeDeclarations(List<Mark> marks) {
        Matcher m = TYPE_DECL.matcher(text);
        while (m.find()) {
            // main type
            marks.add(new Mark(m.start(1), m.end(1), TokenType.TYPE_DECL));

            if (m.group(2) != null) {
                int start = m.start(2);
                int end = m.end(2);
                // Color < and > as white
                marks.add(new Mark(start, start + 1, TokenType.DEFAULT)); // <
                marks.add(new Mark(end - 1, end, TokenType.DEFAULT)); // >

                // Inner type (String)
                if (m.group(3) != null) {
                    marks.add(new Mark(m.start(3), m.end(3), TokenType.NEW_TYPE));
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

    public static class LineData {
        public String text;
        public int start, end;
        public List<Token> tokens = new ArrayList<>();
        public List<Integer> indentCols = new ArrayList<>();

        public LineData(String text, int startIndex, int end) {
            this.text = text;
            this.start = startIndex;
            this.end = end;
        }

        public void drawString(int x, int y, int color) {
            StringBuilder builder = new StringBuilder();
            int lastIndex = 0;

            for (Token t : tokens) {
                int tokenStart = t.start - this.start; // relative position in line
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

        @Override
        public String toString() {
            return "Mark{" + type + ", (start=" + start + ", end=" + end + ")" + '}';
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
        GLOBAL_FIELD('b', 35), // aqua - class-level fields
        LOCAL_FIELD('e', 25),  // yellow - local variables
        PARAMETER('9', 36),    // blue - method parameters (Bug 10)
        UNDEFINED_VAR('4', 10), // dark red - undefined variables (Bug 11)
        DEFAULT('f', 0);

        public final char color;
        public final int priority;

        TokenType(char color, int priority) {
            this.color = color;
            this.priority = priority;
        }
    }

    // Compute indent guide columns per line based on brace matching.
    private void computeIndentGuides(List<Mark> marks) {
        // Clear existing guides
        for (LineData ld : lines) {
            ld.indentCols.clear();
        }

        // Build ignored ranges from STRING and COMMENT tokens
        List<int[]> ignored = new ArrayList<>();
        for (Mark m : marks) {
            if (m.type == TokenType.STRING || m.type == TokenType.COMMENT) {
                ignored.add(new int[]{m.start, m.end});
            }
        }

        java.util.function.Predicate<Integer> isIgnored = (pos) -> {
            for (int[] r : ignored) {
                if (pos >= r[0] && pos < r[1])
                    return true;
            }
            return false;
        };

        class OpenBrace {
            int line, col;

            OpenBrace(int l, int c) {
                line = l;
                col = c;
            }
        }
        java.util.Deque<OpenBrace> stack = new java.util.ArrayDeque<>();

        final int tabSize = 4;

        for (int li = 0; li < lines.size(); li++) {
            LineData ld = lines.get(li);
            String s = ld.text;
            for (int i = 0; i < s.length(); i++) {
                int absPos = ld.start + i;
                if (isIgnored.test(absPos))
                    continue;
                char c = s.charAt(i);
                if (c == '{') {
                    // compute leading indentation (expanded) up to this char
                    int leading = 0;
                    for (int k = 0; k < i; k++) {
                        char ch = s.charAt(k);
                        if (ch == '\t')
                            leading += tabSize;
                        else
                            leading += 1;
                    }
                    stack.push(new OpenBrace(li, leading));
                } else if (c == '}') {
                    if (!stack.isEmpty()) {
                        OpenBrace open = stack.pop();
                        int startLine = open.line;
                        int col = open.col;
                        if (startLine == li)
                            continue; // same-line block, skip
                        // add guide column to lines inside the block
                        int from = Math.max(0, startLine + 1);
                        int to = Math.min(lines.size() - 1, li);
                        for (int l = from; l <= to; l++) {
                            List<Integer> list = lines.get(l).indentCols;
                            if (!list.contains(col))
                                list.add(col);
                        }
                    }
                }
            }
        }
    }
}
