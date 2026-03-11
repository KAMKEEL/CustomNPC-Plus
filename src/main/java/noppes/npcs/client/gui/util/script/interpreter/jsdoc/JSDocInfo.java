package noppes.npcs.client.gui.util.script.interpreter.jsdoc;

import noppes.npcs.client.gui.util.script.interpreter.type.TypeInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents parsed JSDoc comment information.
 * Captures @type, @param, @return, and other JSDoc tags with their type information.
 */
public class JSDocInfo {

    private final String rawComment;
    private final int startOffset;
    private final int endOffset;

    private JSDocTypeTag typeTag;
    private final List<JSDocParamTag> paramTags = new ArrayList<>();
    private JSDocReturnTag returnTag;
    private final List<JSDocSeeTag> seeTags = new ArrayList<>();
    private String description;
    private final List<JSDocTag> allTags = new ArrayList<>();

    public JSDocInfo(String rawComment, int startOffset, int endOffset) {
        this.rawComment = rawComment;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
    }

    public void setTypeTag(JSDocTypeTag typeTag) {
        this.typeTag = typeTag;
        allTags.add(typeTag);
    }

    public void addParamTag(JSDocParamTag paramTag) {
        this.paramTags.add(paramTag);
        allTags.add(paramTag);
    }

    public void setReturnTag(JSDocReturnTag returnTag) {
        this.returnTag = returnTag;
        allTags.add(returnTag);
    }


    public void addSeeTag(JSDocSeeTag seeTag) {
        this.seeTags.add(seeTag);
        allTags.add(seeTag);
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void addTag(JSDocTag tag) {
        allTags.add(tag);
    }

    public String getRawComment() { return rawComment; }
    public int getStartOffset() { return startOffset; }
    public int getEndOffset() { return endOffset; }
    public JSDocTypeTag getTypeTag() { return typeTag; }
    public boolean hasTypeTag() { return typeTag != null; }

    public TypeInfo getDeclaredType() {
        return typeTag != null ? typeTag.getTypeInfo() : null;
    }

    public List<JSDocParamTag> getParamTags() {
        return Collections.unmodifiableList(paramTags);
    }

    public boolean hasParamTags() { return !paramTags.isEmpty(); }

    public JSDocParamTag getParamTag(String paramName) {
        for (JSDocParamTag tag : paramTags) {
            String tagName = tag.getParamName();
            if (tagName != null && tagName.equals(paramName)) {
                return tag;
            }
        }
        return null;
    }

    public JSDocReturnTag getReturnTag() { return returnTag; }
    public boolean hasReturnTag() { return returnTag != null; }

    public TypeInfo getReturnType() {
        return returnTag != null ? returnTag.getTypeInfo() : null;
    }

    public List<JSDocSeeTag> getSeeTags() { return Collections.unmodifiableList(seeTags); }
    public boolean hasSeeTags() { return !seeTags.isEmpty(); }

    public String getDescription() { return description; }
    public List<JSDocTag> getAllTags() { return Collections.unmodifiableList(allTags); }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("JSDocInfo{");
        if (typeTag != null) sb.append("type=").append(typeTag.getTypeName());
        if (!paramTags.isEmpty()) sb.append(", params=").append(paramTags.size());
        if (returnTag != null) sb.append(", return=").append(returnTag.getTypeName());
        if (!seeTags.isEmpty()) sb.append(", see=").append(seeTags.size());
        sb.append("}");
        return sb.toString();
    }
}
