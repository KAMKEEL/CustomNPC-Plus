package noppes.npcs.attribute;

/**
 * Defines a valid attribute – for example, "cnpc:main_attack_flat" (a flat bonus)
 * or "cnpc:main_attack_percent" (a percent bonus). These definitions are registered
 * once in the AttributeController.
 */
public class AttributeDefinition {
    private final int id;
    private final String key;         // e.g., "cnpc:main_attack_flat"
    private final String displayName; // Human‑readable name
    private final AttributeValueType attributeValueType;

    public AttributeDefinition(int id, String key, String displayName, AttributeValueType attributeValueType) {
        this.id = id;
        this.key = key;
        this.displayName = displayName;
        this.attributeValueType = attributeValueType;
    }

    public int getId() {
        return id;
    }

    public String getKey() {
        return key;
    }

    public String getDisplayName() {
        return displayName;
    }

    public AttributeValueType getOperation() {
        return attributeValueType;
    }
}
