package noppes.npcs.client.gui.util.script.interpreter.jsdoc;

import noppes.npcs.client.gui.util.script.interpreter.type.TypeInfo;

/**
 * Represents an @param JSDoc tag.
 * Used to declare the type and description of a function parameter.
 * Example: @param {string} name - The name of the person
 */
public class JSDocParamTag extends JSDocTag {

    private final String paramName;
    private final int paramNameStart;
    private final int paramNameEnd;

    public JSDocParamTag(int atSignOffset, int tagNameStart, int tagNameEnd,
                         String paramName, int paramNameStart, int paramNameEnd) {
        super("param", atSignOffset, tagNameStart, tagNameEnd);
        this.paramName = paramName;
        this.paramNameStart = paramNameStart;
        this.paramNameEnd = paramNameEnd;
    }

    public static JSDocParamTag create(int atSignOffset, int tagNameStart, int tagNameEnd,
                                        String typeName, TypeInfo typeInfo, int typeStart, int typeEnd,
                                        String paramName, int paramNameStart, int paramNameEnd,
                                        String description) {
        JSDocParamTag tag = new JSDocParamTag(atSignOffset, tagNameStart, tagNameEnd,
                                               paramName, paramNameStart, paramNameEnd);
        tag.setType(typeName, typeInfo, typeStart, typeEnd);
        tag.setDescription(description);
        return tag;
    }

    public String getParamName() { return paramName; }
    public int getParamNameStart() { return paramNameStart; }
    public int getParamNameEnd() { return paramNameEnd; }

    @Override
    public String toString() {
        return "JSDocParamTag{@param " + (typeName != null ? "{" + typeName + "} " : "") + paramName + "}";
    }
}
