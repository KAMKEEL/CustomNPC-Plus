package noppes.npcs.attribute.player;

import noppes.npcs.attribute.AttributeDefinition;
import noppes.npcs.attribute.ICustomAttribute;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * CustomAttributeMap holds a set of attribute instances for an entity (or item).
 */
public class PlayerAttributeMap {
    private final Map<AttributeDefinition, ICustomAttribute> map = new HashMap<>();

    public ICustomAttribute registerAttribute(AttributeDefinition attribute, float baseValue) {
        if (map.containsKey(attribute)) {
            throw new IllegalArgumentException("Attribute already registered: " + attribute.getKey());
        }
        ICustomAttribute instance = new PlayerAttribute(attribute, baseValue);
        map.put(attribute, instance);
        return instance;
    }

    public ICustomAttribute getAttributeInstance(AttributeDefinition attribute) {
        return map.get(attribute);
    }

    public Collection<ICustomAttribute> getAllAttributes() {
        return map.values();
    }
}
