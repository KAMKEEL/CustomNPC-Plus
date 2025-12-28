package noppes.npcs.client.gui.util.script.interpreter.expression;

import java.util.ArrayList;
import java.util.List;

public class ExpressionTokenizer {
    
    public static List<ExpressionToken> tokenize(String expr) {
        List<ExpressionToken> tokens = new ArrayList<>();
        int pos = 0;
        int len = expr.length();
        
        while (pos < len) {
            char c = expr.charAt(pos);
            
            if (Character.isWhitespace(c)) { pos++; continue; }
            
            if (c == '"') {
                int start = pos++;
                while (pos < len && expr.charAt(pos) != '"') {
                    if (expr.charAt(pos) == '\\' && pos + 1 < len) pos++;
                    pos++;
                }
                if (pos < len) pos++;
                tokens.add(new ExpressionToken(ExpressionToken.TokenKind.STRING_LITERAL, expr.substring(start, pos), start, pos));
                continue;
            }
            
            if (c == '\'') {
                int start = pos++;
                while (pos < len && expr.charAt(pos) != '\'') {
                    if (expr.charAt(pos) == '\\' && pos + 1 < len) pos++;
                    pos++;
                }
                if (pos < len) pos++;
                tokens.add(new ExpressionToken(ExpressionToken.TokenKind.CHAR_LITERAL, expr.substring(start, pos), start, pos));
                continue;
            }
            
            if (Character.isDigit(c) || (c == '.' && pos + 1 < len && Character.isDigit(expr.charAt(pos + 1)))) {
                int start = pos;
                if (c == '0' && pos + 1 < len) {
                    char next = expr.charAt(pos + 1);
                    if (next == 'x' || next == 'X') {
                        pos += 2;
                        while (pos < len && isHexDigit(expr.charAt(pos))) pos++;
                        if (pos < len && (expr.charAt(pos) == 'L' || expr.charAt(pos) == 'l')) {
                            pos++;
                            tokens.add(new ExpressionToken(ExpressionToken.TokenKind.LONG_LITERAL, expr.substring(start, pos), start, pos));
                        } else {
                            tokens.add(new ExpressionToken(ExpressionToken.TokenKind.INT_LITERAL, expr.substring(start, pos), start, pos));
                        }
                        continue;
                    }
                    if (next == 'b' || next == 'B') {
                        pos += 2;
                        while (pos < len && (expr.charAt(pos) == '0' || expr.charAt(pos) == '1')) pos++;
                        tokens.add(new ExpressionToken(ExpressionToken.TokenKind.INT_LITERAL, expr.substring(start, pos), start, pos));
                        continue;
                    }
                }
                
                while (pos < len && Character.isDigit(expr.charAt(pos))) pos++;
                boolean hasDecimal = false;
                if (pos < len && expr.charAt(pos) == '.') {
                    if (pos + 1 < len && Character.isDigit(expr.charAt(pos + 1))) {
                        hasDecimal = true;
                        pos++;
                        while (pos < len && Character.isDigit(expr.charAt(pos))) pos++;
                    }
                }
                if (pos < len && (expr.charAt(pos) == 'e' || expr.charAt(pos) == 'E')) {
                    pos++;
                    if (pos < len && (expr.charAt(pos) == '+' || expr.charAt(pos) == '-')) pos++;
                    while (pos < len && Character.isDigit(expr.charAt(pos))) pos++;
                    hasDecimal = true;
                }
                
                ExpressionToken.TokenKind kind = ExpressionToken.TokenKind.INT_LITERAL;
                if (pos < len) {
                    char suffix = expr.charAt(pos);
                    if (suffix == 'f' || suffix == 'F') { kind = ExpressionToken.TokenKind.FLOAT_LITERAL; pos++; }
                    else if (suffix == 'd' || suffix == 'D') { kind = ExpressionToken.TokenKind.DOUBLE_LITERAL; pos++; }
                    else if (suffix == 'L' || suffix == 'l') { kind = ExpressionToken.TokenKind.LONG_LITERAL; pos++; }
                    else if (hasDecimal) kind = ExpressionToken.TokenKind.DOUBLE_LITERAL;
                } else if (hasDecimal) kind = ExpressionToken.TokenKind.DOUBLE_LITERAL;
                
                tokens.add(new ExpressionToken(kind, expr.substring(start, pos), start, pos));
                continue;
            }
            
            if (Character.isJavaIdentifierStart(c)) {
                int start = pos;
                while (pos < len && Character.isJavaIdentifierPart(expr.charAt(pos))) pos++;
                tokens.add(ExpressionToken.identifier(expr.substring(start, pos), start, pos));
                continue;
            }
            
            int start = pos;
            String op = null;
            if (pos + 3 <= len) {
                String s3 = expr.substring(pos, pos + 3);
                if (">>>".equals(s3) || "<<=".equals(s3) || ">>=".equals(s3)) op = s3;
            }
            if (op == null && pos + 4 <= len) {
                String s4 = expr.substring(pos, pos + 4);
                if (">>>=".equals(s4)) op = s4;
            }
            if (op == null && pos + 2 <= len) {
                String s2 = expr.substring(pos, pos + 2);
                if ("++".equals(s2) || "--".equals(s2) || "+=".equals(s2) || "-=".equals(s2) ||
                    "*=".equals(s2) || "/=".equals(s2) || "%=".equals(s2) || "&=".equals(s2) ||
                    "|=".equals(s2) || "^=".equals(s2) || "==".equals(s2) || "!=".equals(s2) ||
                    "<=".equals(s2) || ">=".equals(s2) || "&&".equals(s2) || "||".equals(s2) ||
                    "<<".equals(s2) || ">>".equals(s2)) op = s2;
            }
            if (op == null) {
                switch (c) {
                    case '+': case '-': case '*': case '/': case '%': case '&': case '|':
                    case '^': case '~': case '!': case '<': case '>': case '=':
                        op = String.valueOf(c); break;
                }
            }
            
            if (op != null) {
                pos += op.length();
                tokens.add(ExpressionToken.operator(op, start, pos));
                continue;
            }
            
            switch (c) {
                case '(': tokens.add(new ExpressionToken(ExpressionToken.TokenKind.LEFT_PAREN, "(", pos, pos + 1)); break;
                case ')': tokens.add(new ExpressionToken(ExpressionToken.TokenKind.RIGHT_PAREN, ")", pos, pos + 1)); break;
                case '[': tokens.add(new ExpressionToken(ExpressionToken.TokenKind.LEFT_BRACKET, "[", pos, pos + 1)); break;
                case ']': tokens.add(new ExpressionToken(ExpressionToken.TokenKind.RIGHT_BRACKET, "]", pos, pos + 1)); break;
                case '.': tokens.add(new ExpressionToken(ExpressionToken.TokenKind.DOT, ".", pos, pos + 1)); break;
                case ',': tokens.add(new ExpressionToken(ExpressionToken.TokenKind.COMMA, ",", pos, pos + 1)); break;
                case '?': tokens.add(new ExpressionToken(ExpressionToken.TokenKind.QUESTION, "?", pos, pos + 1)); break;
                case ':': tokens.add(new ExpressionToken(ExpressionToken.TokenKind.COLON, ":", pos, pos + 1)); break;
                case ';': tokens.add(new ExpressionToken(ExpressionToken.TokenKind.SEMICOLON, ";", pos, pos + 1)); break;
            }
            pos++;
        }
        
        tokens.add(new ExpressionToken(ExpressionToken.TokenKind.EOF, "", len, len));
        return tokens;
    }
    
    private static boolean isHexDigit(char c) {
        return Character.isDigit(c) || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
    }
}
