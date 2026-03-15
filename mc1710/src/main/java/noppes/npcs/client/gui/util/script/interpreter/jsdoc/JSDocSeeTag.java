package noppes.npcs.client.gui.util.script.interpreter.jsdoc;

public class JSDocSeeTag extends JSDocTag {

    private final String reference;
    private String url;
    private String linkText;

    public JSDocSeeTag(String tagName, int atSignOffset, int tagNameStart, int tagNameEnd, String reference) {
        super(tagName, atSignOffset, tagNameStart, tagNameEnd);
        this.reference = reference;
        this.setDescription(reference);
        parseReference(reference);
    }

    public static JSDocSeeTag create(String tagName, int atSignOffset, int tagNameStart, int tagNameEnd,
                                      String reference) {
        return new JSDocSeeTag(tagName, atSignOffset, tagNameStart, tagNameEnd, reference);
    }

    public static JSDocSeeTag createSimple(String reference) {
        return new JSDocSeeTag("see", -1, -1, -1, reference);
    }

    private void parseReference(String ref) {
        if (ref == null) return;
        
        if (ref.contains("<a href=")) {
            int hrefStart = ref.indexOf("href=\"");
            if (hrefStart != -1) {
                hrefStart += 6;
                int hrefEnd = ref.indexOf("\"", hrefStart);
                if (hrefEnd != -1) {
                    this.url = ref.substring(hrefStart, hrefEnd);
                }
            }
            int textStart = ref.indexOf(">");
            int textEnd = ref.indexOf("</a>");
            if (textStart != -1 && textEnd != -1 && textStart < textEnd) {
                this.linkText = ref.substring(textStart + 1, textEnd);
            }
        } else if (ref.contains("{@link")) {
            int linkStart = ref.indexOf("{@link");
            int linkEnd = ref.indexOf("}", linkStart);
            if (linkStart != -1 && linkEnd != -1) {
                this.linkText = ref.substring(linkStart + 6, linkEnd).trim();
            }
        }
    }

    public String getReference() {
        return reference;
    }

    public String getUrl() {
        return url;
    }

    public boolean hasUrl() {
        return url != null && !url.isEmpty();
    }

    public String getLinkText() {
        return linkText;
    }

    public boolean hasLinkText() {
        return linkText != null && !linkText.isEmpty();
    }

    @Override
    public String toString() {
        return "JSDocSeeTag{@see " + reference + "}";
    }
}
