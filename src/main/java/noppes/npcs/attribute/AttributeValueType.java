package noppes.npcs.attribute;

public enum AttributeValueType {
    FLAT,       // e.g., +10 Health or +3 Damage
    PERCENT,    // e.g., +0.25 means +25% bonus
    MAGIC       // Represents a container of magic–based values (summed up as a double)
}
