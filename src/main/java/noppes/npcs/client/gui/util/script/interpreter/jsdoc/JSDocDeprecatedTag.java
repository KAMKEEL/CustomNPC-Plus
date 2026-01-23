package noppes.npcs.client.gui.util.script.interpreter.jsdoc;

public class JSDocDeprecatedTag extends JSDocTag {

    private final String reason;

    public JSDocDeprecatedTag(String tagName, int atSignOffset, int tagNameStart, int tagNameEnd, String reason) {
        super(tagName, atSignOffset, tagNameStart, tagNameEnd);
        this.reason = reason;
        this.setDescription(reason);
    }

    public static JSDocDeprecatedTag create(String tagName, int atSignOffset, int tagNameStart, int tagNameEnd,
                                             String reason) {
        return new JSDocDeprecatedTag(tagName, atSignOffset, tagNameStart, tagNameEnd, reason);
    }

    public static JSDocDeprecatedTag createSimple(String reason) {
        return new JSDocDeprecatedTag("deprecated", -1, -1, -1, reason);
    }

    public String getReason() {
        return reason;
    }

    public boolean hasReason() {
        return reason != null && !reason.isEmpty();
    }

    @Override
    public String toString() {
        return "JSDocDeprecatedTag{@deprecated" + (hasReason() ? " " + reason : "") + "}";
    }
}
