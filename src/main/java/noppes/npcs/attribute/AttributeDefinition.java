package noppes.npcs.attribute;

public class AttributeDefinition {
    private final String key;         // e.g., "main_attack_flat"
    private final String displayName; // Human‑readable name
    private final AttributeValueType attributeValueType;
    private final AttributeSection section;

    public AttributeDefinition(String key, String displayName, AttributeValueType attributeValueType, AttributeSection section) {
        this.key = key;
        this.displayName = displayName;
        this.attributeValueType = attributeValueType;
        this.section = section;
    }

    public String getKey() {
        return key;
    }

    public String getDisplayName() {
        return displayName;
    }

    public AttributeValueType getValueType() {
        return attributeValueType;
    }

    public AttributeSection getSection() {
        return section;
    }

    public enum AttributeSection {
        BASE,
        MODIFIER,
        INFO,
        EXTRA;
    }
}
