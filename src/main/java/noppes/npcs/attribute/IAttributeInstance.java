package noppes.npcs.attribute;

import java.util.Collection;

/**
 * IAttributeInstance represents an instance of an attribute (with a base value and applied modifiers).
 */
public interface IAttributeInstance {
    AttributeDefinition getAttribute();
    double getBaseValue();
    void setBaseValue(double value);
    void applyModifier(AttributeModifier modifier);
    void removeModifier(AttributeModifier modifier);
    Collection<AttributeModifier> getModifiers();
    double getAttributeValue(); // Final computed value after modifiers
}
