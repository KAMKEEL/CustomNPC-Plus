package kamkeel.npcs.controllers.data.attribute;


import java.util.HashMap;
import java.util.Map;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IEntityLiving;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.IDamageSource;
import noppes.npcs.api.INbt;
import noppes.npcs.api.INbtList;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.IWorld;

/**
 * CustomAttributeMap holds a set of attribute instances for an IEntity (or item).
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
