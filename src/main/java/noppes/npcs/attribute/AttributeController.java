package noppes.npcs.attribute;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * AttributeController is the central registry for attribute definitions.
 * It ensures that keys are unique.
 */
public class AttributeController {

    public AttributeController Instance;
    public ModAttributes attributes;

    private static final Map<String, AttributeDefinition> definitions = new HashMap<>();

    public AttributeController(){
        this.Instance = this;
        definitions.clear();
        attributes = new ModAttributes();
    }


    public static AttributeDefinition registerAttribute(String key, String displayName, AttributeValueType valueType) {
        if (definitions.containsKey(key)) {
            throw new IllegalArgumentException("Attribute already registered with key: " + key);
        }
        AttributeDefinition def = new AttributeDefinition(key, displayName, valueType);
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
