package noppes.npcs.attribute.player;


import noppes.npcs.attribute.AttributeDefinition;
import noppes.npcs.attribute.ICustomAttribute;

public class PlayerAttribute implements ICustomAttribute {
    private final AttributeDefinition attribute;
    private float baseValue;

    public PlayerAttribute(AttributeDefinition attribute, float baseValue) {
        this.attribute = attribute;
        this.baseValue = baseValue;
    }

    @Override
    public AttributeDefinition getAttribute() {
        return attribute;
    }

    @Override
    public float getValue() {
        return baseValue;
    }

    @Override
    public void setValue(float value) {
        this.baseValue = value;
    }
}
