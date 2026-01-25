package noppes.npcs.client.gui.util.script.interpreter.jsdoc;

import noppes.npcs.client.gui.util.script.interpreter.type.TypeInfo;

/**
 * Represents an @type JSDoc tag.
 * Used to declare the type of a variable.
 * Example: @type {string}
 */
public class JSDocTypeTag extends JSDocTag {

    public JSDocTypeTag(int atSignOffset, int tagNameStart, int tagNameEnd) {
        super("type", atSignOffset, tagNameStart, tagNameEnd);
    }

    public static JSDocTypeTag create(int atSignOffset, int tagNameStart, int tagNameEnd, String typeName,
                                      TypeInfo typeInfo, int typeStart, int typeEnd, String description) {
        JSDocTypeTag tag = new JSDocTypeTag(atSignOffset, tagNameStart, tagNameEnd);
        tag.setType(typeName, typeInfo, typeStart, typeEnd);
        tag.setDescription(description);
        return tag;
    }

    @Override
    public String toString() {
        return "JSDocTypeTag{@type " + (typeName != null ? "{" + typeName + "}" : "") + "}";
    }
}
