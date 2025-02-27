package kamkeel.npcs.controllers.data.attribute;

/**
 * IAttributeInstance represents an instance of an attribute (with a base value and applied modifiers).
 */
public interface ICustomAttribute {
    AttributeDefinition getAttribute();
    float getValue();
    void setValue(float value);
}
