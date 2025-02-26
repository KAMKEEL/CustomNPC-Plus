package noppes.npcs.attribute;

/**
 * IAttributeInstance represents an instance of an attribute (with a base value and applied modifiers).
 */
public interface IAttributeInstance {
    AttributeDefinition getAttribute();
    double getValue();
    void setValue(double value);
}
