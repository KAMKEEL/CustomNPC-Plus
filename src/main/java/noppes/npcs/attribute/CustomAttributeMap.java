package noppes.npcs.attribute;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Holds a collection of attribute instances (for a weapon, item, or aggregated for a player).
 */
public class CustomAttributeMap {
    private final Map<AttributeDefinition, IAttributeInstance> map = new HashMap<>();

    public IAttributeInstance registerAttribute(AttributeDefinition attribute, double baseValue) {
        if (map.containsKey(attribute)) {
            throw new IllegalArgumentException("Attribute already registered: " + attribute.getKey());
        }
        IAttributeInstance instance = new ModifiableAttributeInstance(attribute, baseValue);
        map.put(attribute, instance);
        return instance;
    }

    public IAttributeInstance getAttributeInstance(AttributeDefinition attribute) {
        return map.get(attribute);
    }

    public Collection<IAttributeInstance> getAllAttributes() {
        return map.values();
    }
}
