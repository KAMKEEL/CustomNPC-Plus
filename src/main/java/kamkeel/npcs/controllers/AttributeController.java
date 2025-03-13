package kamkeel.npcs.controllers;

import kamkeel.npcs.CustomAttributes;
import kamkeel.npcs.controllers.data.attribute.AttributeDefinition;
import kamkeel.npcs.controllers.data.attribute.AttributeValueType;
import kamkeel.npcs.controllers.data.attribute.tracker.PlayerAttributeTracker;
import net.minecraft.entity.player.EntityPlayer;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * AttributeController is the central registry for attribute definitions.
 * It ensures that keys are unique.
 */
public class AttributeController {

    public AttributeController Instance;
    public CustomAttributes attributes;

    // Tracking Player Attributes
    private static final Map<UUID, PlayerAttributeTracker> trackers = new HashMap<>();
    private static final Map<String, AttributeDefinition> definitions = new HashMap<>();

    public AttributeController() {
        this.Instance = this;
        definitions.clear();
        trackers.clear();
        attributes = new CustomAttributes();
    }


    public static AttributeDefinition registerAttribute(String key, String displayName, char colorCode, AttributeValueType valueType, AttributeDefinition.AttributeSection section) {
        if (definitions.containsKey(key)) {
            throw new IllegalArgumentException("Attribute already registered with key: " + key);
        }
        AttributeDefinition def = new AttributeDefinition(key, displayName, colorCode, valueType, section);
        definitions.put(key, def);
        return def;
    }

    public static AttributeDefinition registerAttribute(AttributeDefinition definition) {
        if (definitions.containsKey(definition.getKey())) {
            throw new IllegalArgumentException("Attribute already registered with key: " + definition.getKey());
        } else {
            definitions.put(definition.getKey(), definition);
            return definition;
        }
    }

    public static AttributeDefinition getAttribute(String key) {
        return definitions.get(key);
    }

    public static Collection<AttributeDefinition> getAllAttributes() {
        return definitions.values();
    }

    public static PlayerAttributeTracker getTracker(EntityPlayer player) {
        return trackers.computeIfAbsent(player.getUniqueID(), id -> new PlayerAttributeTracker(id));
    }

    public static void removeTracker(UUID playerId) {
        trackers.remove(playerId);
    }

    /**
     * Update all trackers (e.g. called every 10 ticks for all online players).
     */
    public static void updateAllTrackers(Iterable<EntityPlayer> players) {
        for (EntityPlayer player : players) {
            PlayerAttributeTracker tracker = getTracker(player);
            tracker.updateIfChanged(player);
        }
    }
}
