package noppes.npcs.client.gui.util.script.interpreter.jsdoc;

import noppes.npcs.client.gui.util.script.interpreter.type.TypeInfo;

/**
 * Represents an @return or @returns JSDoc tag.
 * Used to declare the return type of a function.
 * Example: @return {number} The calculated result
 */
public class JSDocReturnTag extends JSDocTag {

    public JSDocReturnTag(String tagName, int atSignOffset, int tagNameStart, int tagNameEnd) {
        super(tagName, atSignOffset, tagNameStart, tagNameEnd);
    }

    public static JSDocReturnTag create(String tagName, int atSignOffset, int tagNameStart, int tagNameEnd,
                                         String typeName, TypeInfo typeInfo, int typeStart, int typeEnd,
                                         String description) {
        JSDocReturnTag tag = new JSDocReturnTag(tagName, atSignOffset, tagNameStart, tagNameEnd);
        tag.setType(typeName, typeInfo, typeStart, typeEnd);
        tag.setDescription(description);
        return tag;
    }

    @Override
    public String toString() {
        return "JSDocReturnTag{@" + tagName + " " + (typeName != null ? "{" + typeName + "}" : "") + "}";
    }
}
