package kamkeel.npcs.controllers.data.attribute;

public class AttributeDefinition {
    private final String key;
    private String translationKey;// e.g., "main_attack_flat"
    private final String displayName; // Human‑readable name
    private final char colorCode;
    private final AttributeValueType attributeValueType;
    private final AttributeSection section;

    public AttributeDefinition(String key, String displayName, char colorCode, AttributeValueType attributeValueType, AttributeSection section) {
        this.key = key;
        this.translationKey = "rpgcore:attribute." + key;
        this.displayName = displayName;
        this.attributeValueType = attributeValueType;
        this.section = section;
        this.colorCode = colorCode;
    }

    public String getKey() {
        return key;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getTranslationKey() {
        return translationKey;
    }

    public void setTranslationKey(String translationKey) {
        this.translationKey = translationKey;
    }

    public AttributeValueType getValueType() {
        return attributeValueType;
    }

    public AttributeSection getSection() {
        return section;
    }

    public char getColorCode() {
        return colorCode;
    }

    public enum AttributeSection {
        BASE,
        MODIFIER,
        STATS,
        INFO,
        EXTRA
    }
}
