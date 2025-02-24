package noppes.npcs.attribute;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Central registry for valid attribute definitions.
 * Other mods can register new attributes here so that items only need to store a
 * small namespaced key.
 */
public class AttributeController {
    private static final Map<String, AttributeDefinition> definitions = new HashMap<>();
    private static int nextId = 0;

    public static AttributeDefinition registerAttribute(String key, String displayName, Operation op) {
        if (definitions.containsKey(key)) {
            return definitions.get(key);
        }
        AttributeDefinition def = new AttributeDefinition(nextId++, key, displayName, op);
        definitions.put(key, def);
        return def;
    }

    public static AttributeDefinition getAttribute(String key) {
        return definitions.get(key);
    }

    public static Collection<AttributeDefinition> getAllAttributes() {
        return definitions.values();
    }
}
