package noppes.npcs.attribute;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ModifiableAttributeInstance implements IAttributeInstance {
    private final AttributeDefinition attribute;
    private double baseValue;
    private final Set<AttributeModifier> modifiers = new HashSet<>();

    public ModifiableAttributeInstance(AttributeDefinition attribute, double baseValue) {
        this.attribute = attribute;
        this.baseValue = baseValue;
    }

    @Override
    public AttributeDefinition getAttribute() {
        return attribute;
    }

    @Override
    public double getBaseValue() {
        return baseValue;
    }

    @Override
    public void setBaseValue(double value) {
        this.baseValue = value;
    }

    @Override
    public void applyModifier(AttributeModifier modifier) {
        modifiers.add(modifier);
    }

    @Override
    public void removeModifier(AttributeModifier modifier) {
        modifiers.remove(modifier);
    }

    @Override
    public Collection<AttributeModifier> getModifiers() {
        return modifiers;
    }

    @Override
    public double getAttributeValue() {
        double finalValue = baseValue;
        // Apply FLAT modifiers
        for (AttributeModifier mod : modifiers) {
            if (mod.getValueType() == AttributeValueType.FLAT) {
                finalValue += mod.getAmount();
            }
        }
        // Apply PERCENT modifiers additively.
        double percentSum = 0.0;
        for (AttributeModifier mod : modifiers) {
            if (mod.getValueType() == AttributeValueType.PERCENT) {
                percentSum += mod.getAmount();
            }
        }
        finalValue *= (1 + percentSum);
        return finalValue;
    }
}
