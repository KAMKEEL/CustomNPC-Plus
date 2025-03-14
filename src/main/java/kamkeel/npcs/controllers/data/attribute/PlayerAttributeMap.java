package kamkeel.npcs.controllers.data.attribute;

import noppes.npcs.api.handler.data.ICustomAttribute;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * CustomAttributeMap holds a set of attribute instances for an entity (or item).
 */
public class PlayerAttributeMap {
    public final Map<AttributeDefinition, PlayerAttribute> map = new HashMap<>();

    public PlayerAttribute registerAttribute(AttributeDefinition attribute, float baseValue) {
        if (map.containsKey(attribute)) {
            throw new IllegalArgumentException("Attribute already registered: " + attribute.getKey());
        }
        PlayerAttribute instance = new PlayerAttribute(attribute, baseValue);
        map.put(attribute, instance);
        return instance;
    }

    public PlayerAttribute getAttributeInstance(AttributeDefinition attribute) {
        return map.get(attribute);
    }
}
