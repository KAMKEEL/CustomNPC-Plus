package noppes.npcs.attribute.player;

import noppes.npcs.attribute.AttributeDefinition;
import noppes.npcs.attribute.IAttributeInstance;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * CustomAttributeMap holds a set of attribute instances for an entity (or item).
 */
public class PlayerAttributeMap {
    private final Map<AttributeDefinition, IAttributeInstance> map = new HashMap<>();

    public IAttributeInstance registerAttribute(AttributeDefinition attribute, double baseValue) {
        if (map.containsKey(attribute)) {
            throw new IllegalArgumentException("Attribute already registered: " + attribute.getKey());
        }
        IAttributeInstance instance = new PlayerAttribute(attribute, baseValue);
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
