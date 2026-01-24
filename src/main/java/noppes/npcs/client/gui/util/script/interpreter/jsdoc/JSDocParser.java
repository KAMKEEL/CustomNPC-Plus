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
            "^@(type)\\s*(?:\\{([^}]+)\\})?(?:\\s*-?\\s*(.*))?$", Pattern.CASE_INSENSITIVE);

    private static final Pattern PARAM_TAG_PATTERN = Pattern.compile(
            "^@param\\s*(?:\\{([^}]+)\\})?\\s*(\\w+)?(?:\\s*-?\\s*(.*))?$", Pattern.CASE_INSENSITIVE);

    private static final Pattern RETURN_TAG_PATTERN = Pattern.compile(
            "^@(returns?)\\s*(?:\\{([^}]+)\\})?(?:\\s*-?\\s*(.*))?$", Pattern.CASE_INSENSITIVE);

    private static final Pattern GENERIC_TAG_PATTERN = Pattern.compile(
            "^@(\\w+)(?:\\s*\\{([^}]+)\\})?(?:\\s*-?\\s*(.*))?$", Pattern.CASE_INSENSITIVE);

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

        StringBuilder descriptionBuilder = new StringBuilder();
        boolean foundFirstTag = false;
        JSDocTag lastTag = null;

        int position = 0;
        while (position <= content.length()) {
            int lineEnd = content.indexOf('\n', position);
            if (lineEnd == -1) {
                lineEnd = content.length();
            }

            String rawLine = content.substring(position, lineEnd);
            if (rawLine.endsWith("\r")) {
                rawLine = rawLine.substring(0, rawLine.length() - 1);
            }

            CleanLine cleanLine = cleanLine(rawLine, contentOffset + position);
            String line = cleanLine.text;

            if (!line.isEmpty()) {
                if (line.startsWith("@")) {
                    foundFirstTag = true;
                    lastTag = parseTagLine(line, cleanLine.offset, info);
                } else if (!foundFirstTag) {
                    if (descriptionBuilder.length() > 0) {
                        descriptionBuilder.append(" ");
                    }
                    descriptionBuilder.append(line);
                } else if (lastTag != null) {
                    String existing = lastTag.getDescription();
                    if (existing == null || existing.isEmpty()) {
                        lastTag.setDescription(line);
                    } else {
                        lastTag.setDescription(existing + " " + line);
                    }
                }
            }

            if (lineEnd == content.length()) {
                break;
            }
            position = lineEnd + 1;
        }

        if (descriptionBuilder.length() > 0) {
            info.setDescription(descriptionBuilder.toString().trim());
        }

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

    private JSDocTag parseTagLine(String line, int lineOffset, JSDocInfo info) {
        Matcher paramMatcher = PARAM_TAG_PATTERN.matcher(line);
        if (paramMatcher.matches()) {
            String typeName = paramMatcher.group(1);
            String paramName = paramMatcher.group(2);
            String description = normalizeDescription(paramMatcher.group(3));

            int atSignOffset = lineOffset + paramMatcher.start();
            int tagNameStart = lineOffset + 1;
            int tagNameEnd = tagNameStart + "param".length();

            int typeStart = -1;
            int typeEnd = -1;
            TypeInfo typeInfo = null;

            if (typeName != null) {
                typeName = typeName.trim();
                typeStart = lineOffset + paramMatcher.start(1);
                typeEnd = lineOffset + paramMatcher.end(1);
                typeInfo = resolveType(typeName);
            }

            int paramNameStart = -1;
            int paramNameEnd = -1;
            if (paramName != null) {
                paramNameStart = lineOffset + paramMatcher.start(2);
                paramNameEnd = lineOffset + paramMatcher.end(2);
            }

            JSDocParamTag tag = JSDocParamTag.create(atSignOffset, tagNameStart, tagNameEnd,
                    typeName, typeInfo, typeStart, typeEnd,
                    paramName, paramNameStart, paramNameEnd,
                    description);
            info.addParamTag(tag);
            return tag;
        }

        Matcher returnMatcher = RETURN_TAG_PATTERN.matcher(line);
        if (returnMatcher.matches()) {
            String tagName = returnMatcher.group(1);
            String typeName = returnMatcher.group(2);
            String description = normalizeDescription(returnMatcher.group(3));

            int atSignOffset = lineOffset + returnMatcher.start();
            int tagNameStart = lineOffset + returnMatcher.start(1);
            int tagNameEnd = lineOffset + returnMatcher.end(1);

            int typeStart = -1;
            int typeEnd = -1;
            TypeInfo typeInfo = null;

            if (typeName != null) {
                typeName = typeName.trim();
                typeStart = lineOffset + returnMatcher.start(2);
                typeEnd = lineOffset + returnMatcher.end(2);
                typeInfo = resolveType(typeName);
            }

            JSDocReturnTag tag = JSDocReturnTag.create(tagName, atSignOffset, tagNameStart, tagNameEnd,
                    typeName, typeInfo, typeStart, typeEnd,
                    description);
            info.setReturnTag(tag);
            return tag;
        }

        Matcher typeMatcher = TYPE_TAG_PATTERN.matcher(line);
        if (typeMatcher.matches()) {
            String typeName = typeMatcher.group(2);
            String description = normalizeDescription(typeMatcher.group(3));

            int atSignOffset = lineOffset + typeMatcher.start();
            int tagNameStart = lineOffset + typeMatcher.start(1);
            int tagNameEnd = lineOffset + typeMatcher.end(1);

            int typeStart = -1;
            int typeEnd = -1;
            TypeInfo typeInfo = null;

            if (typeName != null) {
                typeName = typeName.trim();
                typeStart = lineOffset + typeMatcher.start(2);
                typeEnd = lineOffset + typeMatcher.end(2);
                typeInfo = resolveType(typeName);
            }

            JSDocTypeTag tag = JSDocTypeTag.create(atSignOffset, tagNameStart, tagNameEnd,
                    typeName, typeInfo, typeStart, typeEnd, description);
            info.setTypeTag(tag);
            return tag;
        }

        Matcher genericMatcher = GENERIC_TAG_PATTERN.matcher(line);
        if (genericMatcher.matches()) {
            String tagName = genericMatcher.group(1);
            String typeName = genericMatcher.group(2);
            String description = normalizeDescription(genericMatcher.group(3));

            int atSignOffset = lineOffset + genericMatcher.start();
            int tagNameStart = lineOffset + genericMatcher.start(1);
            int tagNameEnd = lineOffset + genericMatcher.end(1);

            JSDocTag tag = new JSDocTag(tagName, atSignOffset, tagNameStart, tagNameEnd);

            if (typeName != null) {
                typeName = typeName.trim();
                int typeStart = lineOffset + genericMatcher.start(2);
                int typeEnd = lineOffset + genericMatcher.end(2);
                TypeInfo typeInfo = resolveType(typeName);
                tag.setType(typeName, typeInfo, typeStart, typeEnd);
            }

            if (description != null && !description.isEmpty()) {
                tag.setDescription(description);
            }

            info.addTag(tag);
            return tag;
        }

        return null;
    }

    private String normalizeDescription(String description) {
        if (description == null) {
            return null;
        }
        String trimmed = description.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private CleanLine cleanLine(String line, int lineStartOffset) {
        int index = 0;
        while (index < line.length() && Character.isWhitespace(line.charAt(index))) {
            index++;
        }

        if (index + 2 < line.length() && line.startsWith("/**", index)) {
            index += 3;
            while (index < line.length() && Character.isWhitespace(line.charAt(index))) {
                index++;
            }
        }

        if (index < line.length() && line.charAt(index) == '*') {
            index++;
            if (index < line.length() && line.charAt(index) == ' ') {
                index++;
            }
        }

        String cleaned = line.substring(index);
        int end = cleaned.length();
        while (end > 0 && Character.isWhitespace(cleaned.charAt(end - 1))) {
            end--;
        }
        cleaned = cleaned.substring(0, end);

        return new CleanLine(cleaned, lineStartOffset + index);
    }

    private TypeInfo resolveType(String typeName) {
        return document.resolveType(typeName);
    }

    private static class CleanLine {
        private final String text;
        private final int offset;

        private CleanLine(String text, int offset) {
            this.text = text;
            this.offset = offset;
        }
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
