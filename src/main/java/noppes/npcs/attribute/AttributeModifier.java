package noppes.npcs.attribute;

import java.util.UUID;

/**
 * An AttributeModifier represents a bonus or penalty that’s applied to an attribute.
 * It stores a unique UUID, a name, an amount, and whether it is FLAT (added directly)
 * or PERCENT (applied as a multiplier) to the base value.
 */
public class AttributeModifier {
    private final UUID id;
    private final String name;
    private final double amount;
    private final AttributeValueType attributeValueType;

    public AttributeModifier(UUID id, String name, double amount, AttributeValueType attributeValueType) {
        this.id = id;
        this.name = name;
        this.amount = amount;
        this.attributeValueType = attributeValueType;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getAmount() {
        return amount;
    }

    public AttributeValueType getOperation() {
        return attributeValueType;
    }
}
