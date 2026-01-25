package noppes.npcs.client.gui.util.script.interpreter.jsdoc;

import noppes.npcs.client.gui.util.script.interpreter.type.TypeInfo;

/**
 * Base class for JSDoc tags.
 * Each tag stores its position information for syntax highlighting.
 */
public class JSDocTag {

    protected final String tagName;
    protected final int atSignOffset;
    protected final int tagNameStart;
    protected final int tagNameEnd;

    protected String typeName;
    protected TypeInfo typeInfo;
    protected int typeStart = -1;
    protected int typeEnd = -1;
    protected String description;

    public JSDocTag(String tagName, int atSignOffset, int tagNameStart, int tagNameEnd) {
        this.tagName = tagName;
        this.atSignOffset = atSignOffset;
        this.tagNameStart = tagNameStart;
        this.tagNameEnd = tagNameEnd;
    }

    public void setType(String typeName, TypeInfo typeInfo, int typeStart, int typeEnd) {
        this.typeName = typeName;
        this.typeInfo = typeInfo;
        this.typeStart = typeStart;
        this.typeEnd = typeEnd;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTagName() { return tagName; }
    public int getAtSignOffset() { return atSignOffset; }
    public int getTagNameStart() { return tagNameStart; }
    public int getTagNameEnd() { return tagNameEnd; }
    public String getTypeName() { return typeName; }
    public TypeInfo getTypeInfo() { return typeInfo; }
    public boolean hasType() { return typeName != null && !typeName.isEmpty(); }
    public int getTypeStart() { return typeStart; }
    public int getTypeEnd() { return typeEnd; }
    public String getDescription() { return description; }

    @Override
    public String toString() {
        return "JSDocTag{@" + tagName + (typeName != null ? " {" + typeName + "}" : "") + "}";
    }
}
