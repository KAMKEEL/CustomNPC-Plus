package noppes.npcs.attribute;

import java.util.Collection;

/**
 * Represents an attribute instance – a value (plus any applied modifiers)
 * corresponding to a given AttributeDefinition.
 */
public interface IAttributeInstance {
    AttributeDefinition getAttribute();
    double getBaseValue();
    void setBaseValue(double value);
    void applyModifier(AttributeModifier modifier);
    void removeModifier(AttributeModifier modifier);
    Collection<AttributeModifier> getModifiers();
    double getAttributeValue(); // Final value after modifiers.
}
