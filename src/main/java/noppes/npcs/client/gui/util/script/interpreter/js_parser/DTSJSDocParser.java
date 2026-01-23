package noppes.npcs.client.gui.util.script.interpreter.js_parser;

import noppes.npcs.client.gui.util.script.interpreter.jsdoc.*;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DTSJSDocParser {
    
    private static final Pattern JSDOC_BLOCK_PATTERN = Pattern.compile(
        "/\\*\\*\\s*(.*?)\\*/", Pattern.DOTALL);
    
    private static final Pattern TAG_PATTERN = Pattern.compile(
        "@(\\w+)(?:\\s+(.*))?");
    
    private static final Pattern PARAM_PATTERN = Pattern.compile(
        "@param\\s+(?:\\{([^}]+)\\}\\s+)?(\\w+)(?:\\s+(.*))?");
    
    private static final Pattern RETURN_PATTERN = Pattern.compile(
        "@returns?\\s+(?:\\{([^}]+)\\}\\s*)?(.*)");
    
    private static final Pattern TYPE_PATTERN = Pattern.compile(
        "@type\\s+\\{([^}]+)\\}(?:\\s+(.*))?");
    
    public static JSDocInfo parseJSDocBlock(String jsDocContent) {
        if (jsDocContent == null || jsDocContent.isEmpty()) {
            return null;
        }
        
        JSDocInfo info = new JSDocInfo(jsDocContent, -1, -1);
        
        String[] lines = jsDocContent.split("\\r?\\n");
        StringBuilder descriptionBuilder = new StringBuilder();
        boolean foundFirstTag = false;
        JSDocTag lastTag = null;
        
        for (String line : lines) {
            line = cleanLine(line);
            if (line.isEmpty()) continue;
            
            if (line.startsWith("@")) {
                foundFirstTag = true;
                lastTag = parseTag(line, info);
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
        
        if (descriptionBuilder.length() > 0) {
            info.setDescription(descriptionBuilder.toString().trim());
        }
        
        return info;
    }
    
    private static String cleanLine(String line) {
        line = line.trim();
        if (line.startsWith("/**")) {
            line = line.substring(3).trim();
        }
        if (line.endsWith("*/")) {
            line = line.substring(0, line.length() - 2).trim();
        }
        if (line.startsWith("*")) {
            line = line.substring(1).trim();
        }
        return line;
    }
    
    private static JSDocTag parseTag(String line, JSDocInfo info) {
        Matcher paramMatcher = PARAM_PATTERN.matcher(line);
        if (paramMatcher.find()) {
            String type = paramMatcher.group(1);
            String name = paramMatcher.group(2);
            String desc = paramMatcher.group(3);
            JSDocParamTag tag = JSDocParamTag.create(-1, -1, -1,
                type, null, -1, -1, name, -1, -1, desc != null ? desc.trim() : null);
            info.addParamTag(tag);
            return tag;
        }
        
        Matcher returnMatcher = RETURN_PATTERN.matcher(line);
        if (returnMatcher.find()) {
            String type = returnMatcher.group(1);
            String desc = returnMatcher.group(2);
            if (desc != null) {
                desc = desc.trim();
                if (desc.isEmpty()) {
                    desc = null;
                }
            }
            JSDocReturnTag tag = JSDocReturnTag.create("return", -1, -1, -1,
                type, null, -1, -1, desc);
            info.setReturnTag(tag);
            return tag;
        }
        
        Matcher typeMatcher = TYPE_PATTERN.matcher(line);
        if (typeMatcher.find()) {
            String type = typeMatcher.group(1);
            String desc = typeMatcher.group(2);
            JSDocTypeTag tag = JSDocTypeTag.create(-1, -1, -1,
                type, null, -1, -1, desc != null ? desc.trim() : null);
            info.setTypeTag(tag);
            return tag;
        }
        
        Matcher tagMatcher = TAG_PATTERN.matcher(line);
        if (tagMatcher.find()) {
            String tagName = tagMatcher.group(1);
            String rest = tagMatcher.group(2);
            
            switch (tagName) {
                case "since":
                    JSDocSinceTag sinceTag = JSDocSinceTag.createSimple(rest != null ? rest.trim() : "");
                    info.setSinceTag(sinceTag);
                    return sinceTag;
                case "deprecated":
                    JSDocDeprecatedTag deprecatedTag = JSDocDeprecatedTag.createSimple(rest != null ? rest.trim() : "");
                    info.setDeprecatedTag(deprecatedTag);
                    return deprecatedTag;
                case "see":
                    JSDocSeeTag seeTag = JSDocSeeTag.createSimple(rest != null ? rest.trim() : "");
                    info.addSeeTag(seeTag);
                    return seeTag;
                default:
                    JSDocTag genericTag = new JSDocTag(tagName, -1, -1, -1);
                    genericTag.setDescription(rest != null ? rest.trim() : "");
                    info.addTag(genericTag);
                    return genericTag;
            }
        }
        return null;
    }
    
    public static String extractJSDocBefore(String content, int elementStart) {
        if (elementStart <= 0) return null;
        
        int searchStart = Math.max(0, elementStart - 2000);
        String searchArea = content.substring(searchStart, elementStart);
        
        int lastJSDocEnd = searchArea.lastIndexOf("*/");
        if (lastJSDocEnd < 0) return null;
        
        int jsDocStart = searchArea.lastIndexOf("/**", lastJSDocEnd);
        if (jsDocStart < 0) return null;
        
        String between = searchArea.substring(lastJSDocEnd + 2).trim();
        if (between.isEmpty() || isOnlyWhitespaceOrModifiers(between)) {
            return searchArea.substring(jsDocStart, lastJSDocEnd + 2);
        }
        
        return null;
    }
    
    private static boolean isOnlyWhitespaceOrModifiers(String text) {
        String cleaned = text.replaceAll("\\s+", " ").trim();
        return cleaned.isEmpty() 
            || cleaned.matches("^(export\\s*)?(readonly\\s*)?(interface|class|type|function)?\\s*$");
    }
    
    public static List<JSDocBlock> extractAllJSDocBlocks(String content) {
        List<JSDocBlock> blocks = new ArrayList<>();
        Matcher matcher = JSDOC_BLOCK_PATTERN.matcher(content);
        
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            String docContent = matcher.group(0);
            blocks.add(new JSDocBlock(start, end, docContent));
        }
        
        return blocks;
    }
    
    public static class JSDocBlock {
        public final int start;
        public final int end;
        public final String content;
        
        public JSDocBlock(int start, int end, String content) {
            this.start = start;
            this.end = end;
            this.content = content;
        }
        
        public JSDocInfo parse() {
            return parseJSDocBlock(content);
        }
    }
}
