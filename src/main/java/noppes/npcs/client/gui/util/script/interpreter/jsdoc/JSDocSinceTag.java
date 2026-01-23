package noppes.npcs.client.gui.util.script.interpreter.jsdoc;

public class JSDocSinceTag extends JSDocTag {

    private final String version;

    public JSDocSinceTag(String tagName, int atSignOffset, int tagNameStart, int tagNameEnd, String version) {
        super(tagName, atSignOffset, tagNameStart, tagNameEnd);
        this.version = version;
        this.setDescription(version);
    }

    public static JSDocSinceTag create(String tagName, int atSignOffset, int tagNameStart, int tagNameEnd,
                                        String version) {
        return new JSDocSinceTag(tagName, atSignOffset, tagNameStart, tagNameEnd, version);
    }

    public static JSDocSinceTag createSimple(String version) {
        return new JSDocSinceTag("since", -1, -1, -1, version);
    }

    public String getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return "JSDocSinceTag{@since " + version + "}";
    }
}
