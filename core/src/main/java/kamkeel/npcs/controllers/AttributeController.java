package kamkeel.npcs.controllers;


import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import kamkeel.npcs.controllers.data.attribute.AttributeDefinition;
import kamkeel.npcs.controllers.data.attribute.AttributeValueType;
import kamkeel.npcs.controllers.data.attribute.tracker.PlayerAttributeTracker;
import kamkeel.npcs.CustomAttributes;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IEntityLiving;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.handler.data.IAttributeDefinition;
import noppes.npcs.api.handler.data.IPlayerAttributes;
import noppes.npcs.api.handler.IAttributeHandler;
import noppes.npcs.api.IDamageSource;
import noppes.npcs.api.INbt;
import noppes.npcs.api.INbtList;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.IWorld;

/**
 * AttributeController is the central registry for attribute definitions.
 * It ensures that keys are unique.
 */
public class AttributeController implements IAttributeHandler {

    public static AttributeController Instance;
    public CustomAttributes attributes;

    // Tracking Player Attributes
    private static final Map<UUID, PlayerAttributeTracker> trackers = new HashMap<>();
    private static final Map<String, AttributeDefinition> definitions = new HashMap<>();

    public AttributeController() {
        Instance = this;
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

    public static PlayerAttributeTracker getTracker(IPlayer player) {
        return trackers.computeIfAbsent(player.getUniqueID(), id -> new PlayerAttributeTracker(id));
    }

    public static void removeTracker(UUID playerId) {
        trackers.remove(playerId);
    }

    /**
     * Update all trackers (e.g. called every 10 ticks for all online players).
     */
    public static void updateAllTrackers(Iterable<IPlayer> players) {
        for (IPlayer player : players) {
            PlayerAttributeTracker tracker = getTracker(player);
            tracker.updateIfChanged(player);
        }
    }

    public IPlayerAttributes getPlayerAttributes(IPlayer player) {
        if (player == null || player.getMCEntity() == null)
            return null;

        IPlayer IPlayer = (IPlayer) player.getMCEntity();
        return trackers.computeIfAbsent(IPlayer.getUniqueID(), id -> new PlayerAttributeTracker(id));
    }

    public IAttributeDefinition getAttributeDefinition(String key) {
        return definitions.get(key);
    }

    public IAttributeDefinition[] getAllAttributesArray() {
        return definitions.values().toArray(new AttributeDefinition[definitions.size()]);
    }
}
