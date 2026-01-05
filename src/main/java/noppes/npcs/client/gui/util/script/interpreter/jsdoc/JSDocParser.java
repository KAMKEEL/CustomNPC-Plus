package noppes.npcs.client.gui.util.script.interpreter.jsdoc;

import noppes.npcs.client.gui.util.script.interpreter.ScriptDocument;
import noppes.npcs.client.gui.util.script.interpreter.type.TypeInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses JSDoc comments to extract type information.
 * Handles @type, @param, @return/@returns tags.
 */
public class JSDocParser {

    private static final Pattern JSDOC_PATTERN = Pattern.compile(
            "/\\*\\*([\\s\\S]*?)\\*/", Pattern.MULTILINE);

    private static final Pattern TYPE_TAG_PATTERN = Pattern.compile(
            "@(type)\\s*\\{([^}]+)\\}", Pattern.CASE_INSENSITIVE);

    private static final Pattern PARAM_TAG_PATTERN = Pattern.compile(
            "@(param)\\s*(?:\\{([^}]+)\\})?\\s*(\\w+)(?:\\s*-?\\s*(.*))?", Pattern.CASE_INSENSITIVE);

    private static final Pattern RETURN_TAG_PATTERN = Pattern.compile(
            "@(returns?)\\s*(?:\\{([^}]+)\\})?(?:\\s*-?\\s*(.*))?", Pattern.CASE_INSENSITIVE);

    private static final Pattern ANY_TAG_PATTERN = Pattern.compile(
            "@(\\w+)", Pattern.CASE_INSENSITIVE);

    private final ScriptDocument document;

    public JSDocParser(ScriptDocument typeResolver) {
        this.document = typeResolver;
    }

    public JSDocInfo parse(String comment, int commentStart) {
        if (comment == null || !comment.startsWith("/**")) {
            return null;
        }

        int commentEnd = commentStart + comment.length();
        JSDocInfo info = new JSDocInfo(comment, commentStart, commentEnd);

        String content = comment.substring(3);
        if (content.endsWith("*/")) {
            content = content.substring(0, content.length() - 2);
        }

        int contentOffset = commentStart + 3;

        parseTypeTag(content, contentOffset, info);
        parseParamTags(content, contentOffset, info);
        parseReturnTag(content, contentOffset, info);
        extractDescription(content, info);

        return info;
    }

    public JSDocInfo extractJSDocBefore(String source, int position) {
        if (source == null || position <= 0) {
            return null;
        }

        int searchStart = Math.max(0, position - 1);

        while (searchStart > 0 && Character.isWhitespace(source.charAt(searchStart))) {
            searchStart--;
        }

        int endCommentPos = -1;
        if (searchStart >= 1 && source.charAt(searchStart) == '/' && source.charAt(searchStart - 1) == '*') {
            endCommentPos = searchStart;
        } else if (searchStart >= 1 && source.charAt(searchStart - 1) == '/' && searchStart > 1 && source.charAt(
                searchStart - 2) == '*') {
            endCommentPos = searchStart - 1;
        }

        if (endCommentPos < 0) {
            return null;
        }

        int startCommentPos = -1;
        for (int i = endCommentPos - 1; i >= 2; i--) {
            if (source.charAt(i) == '*' && source.charAt(i - 1) == '*' && source.charAt(i - 2) == '/') {
                startCommentPos = i - 2;
                break;
            }
        }

        if (startCommentPos < 0) {
            return null;
        }

        String comment = source.substring(startCommentPos, endCommentPos + 1);
        return parse(comment, startCommentPos);
    }

    private void parseTypeTag(String content, int contentOffset, JSDocInfo info) {
        Matcher m = TYPE_TAG_PATTERN.matcher(content);
        if (m.find()) {
            int atSignOffset = contentOffset + m.start();
            int tagNameStart = contentOffset + m.start(1);
            int tagNameEnd = contentOffset + m.end(1);

            String typeName = m.group(2).trim();

            int braceStart = content.indexOf('{', m.start());
            int braceEnd = content.indexOf('}', braceStart);
            int typeStart = contentOffset + braceStart + 1;
            int typeEnd = contentOffset + braceEnd;

            TypeInfo typeInfo = resolveType(typeName);

            JSDocTypeTag tag = JSDocTypeTag.create(atSignOffset, tagNameStart, tagNameEnd,
                    typeName, typeInfo, typeStart, typeEnd);
            info.setTypeTag(tag);
        }
    }

    private void parseParamTags(String content, int contentOffset, JSDocInfo info) {
        Matcher m = PARAM_TAG_PATTERN.matcher(content);
        while (m.find()) {
            int atSignOffset = contentOffset + m.start();
            int tagNameStart = contentOffset + m.start(1);
            int tagNameEnd = contentOffset + m.end(1);

            String typeName = m.group(2);
            String paramName = m.group(3);
            String description = m.group(4);

            int typeStart = -1;
            int typeEnd = -1;
            TypeInfo typeInfo = null;

            if (typeName != null) {
                typeName = typeName.trim();
                int braceStart = content.indexOf('{', m.start());
                int braceEnd = content.indexOf('}', braceStart);
                typeStart = contentOffset + braceStart + 1;
                typeEnd = contentOffset + braceEnd;
                typeInfo = resolveType(typeName);
            }

            int paramNameStart = contentOffset + m.start(3);
            int paramNameEnd = contentOffset + m.end(3);

            JSDocParamTag tag = JSDocParamTag.create(atSignOffset, tagNameStart, tagNameEnd,
                    typeName, typeInfo, typeStart, typeEnd,
                    paramName, paramNameStart, paramNameEnd,
                    description != null ? description.trim() : null);
            info.addParamTag(tag);
        }
    }

    private void parseReturnTag(String content, int contentOffset, JSDocInfo info) {
        Matcher m = RETURN_TAG_PATTERN.matcher(content);
        if (m.find()) {
            String tagName = m.group(1);
            int atSignOffset = contentOffset + m.start();
            int tagNameStart = contentOffset + m.start(1);
            int tagNameEnd = contentOffset + m.end(1);

            String typeName = m.group(2);
            String description = m.group(3);

            int typeStart = -1;
            int typeEnd = -1;
            TypeInfo typeInfo = null;

            if (typeName != null) {
                typeName = typeName.trim();
                int braceStart = content.indexOf('{', m.start());
                int braceEnd = content.indexOf('}', braceStart);
                typeStart = contentOffset + braceStart + 1;
                typeEnd = contentOffset + braceEnd;
                typeInfo = resolveType(typeName);
            }

            JSDocReturnTag tag = JSDocReturnTag.create(tagName, atSignOffset, tagNameStart, tagNameEnd,
                    typeName, typeInfo, typeStart, typeEnd,
                    description != null ? description.trim() : null);
            info.setReturnTag(tag);
        }
    }

    private void extractDescription(String content, JSDocInfo info) {
        Matcher m = ANY_TAG_PATTERN.matcher(content);
        String description;

        if (m.find()) {
            description = content.substring(0, m.start());
        } else {
            description = content;
        }

        description = description.replaceAll("(?m)^\\s*\\*\\s?", "").trim();

        if (!description.isEmpty()) {
            info.setDescription(description);
        }
    }

    private TypeInfo resolveType(String typeName) {
        return document.resolveType(typeName);
    }

    public static List<int[]> findAllJSDocComments(String source) {
        List<int[]> positions = new ArrayList<>();
        Matcher m = JSDOC_PATTERN.matcher(source);
        while (m.find()) {
            positions.add(new int[]{m.start(), m.end()});
        }
        return positions;
    }
}
