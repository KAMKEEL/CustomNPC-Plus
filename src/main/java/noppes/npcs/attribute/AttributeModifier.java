package noppes.npcs.attribute;

import java.util.UUID;

/**
 * An AttributeModifier represents a bonus (or penalty) that is applied to an attribute.
 * It carries a UUID, a human–readable name, an amount, and a type (FLAT, PERCENT, etc).
 */
public class AttributeModifier {
    private final UUID id;
    private final String name;
    private final double amount;
    private final AttributeValueType valueType;

    public AttributeModifier(UUID id, String name, double amount, AttributeValueType valueType) {
        this.id = id;
        this.name = name;
        this.amount = amount;
        this.valueType = valueType;
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

    public AttributeValueType getValueType() {
        return valueType;
    }
}
