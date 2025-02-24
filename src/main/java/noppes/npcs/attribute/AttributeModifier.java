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
    private final Operation operation;

    public AttributeModifier(UUID id, String name, double amount, Operation operation) {
        this.id = id;
        this.name = name;
        this.amount = amount;
        this.operation = operation;
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

    public Operation getOperation() {
        return operation;
    }
}
