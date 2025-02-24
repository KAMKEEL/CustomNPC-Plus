package noppes.npcs.attribute;

import java.util.UUID;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import java.util.Map;

/**
 * A tracker that holds a cached aggregation of a player’s attributes (derived from their held item and armor).
 * It does not “own” the attributes but recalculates them when equipment changes.
 * A simple snapshot of equipment is used to detect changes.
 */
public class PlayerAttributeTracker {
    private final UUID playerId;
    private CustomAttributeMap aggregatedAttributes = new CustomAttributeMap();
    private String equipmentSnapshot = "";

    public PlayerAttributeTracker(UUID playerId) {
        this.playerId = playerId;
        recalcAttributes(null);
    }

    public CustomAttributeMap getAggregatedAttributes() {
        return aggregatedAttributes;
    }

    /**
     * Computes a snapshot string representing current equipment.
     * For simplicity, concatenates item class names and damage values.
     */
    private String computeEquipmentSnapshot(EntityPlayer player) {
        StringBuilder sb = new StringBuilder();
        ItemStack held = player.getHeldItem();
        if(held != null) {
            sb.append(held.getItem().toString()).append(held.getItemDamage());
        }
        for (ItemStack armor : player.inventory.armorInventory) {
            if(armor != null) {
                sb.append(armor.getItem().toString()).append(armor.getItemDamage());
            }
        }
        return sb.toString();
    }

    /**
     * Recalculates the aggregated attribute map from the player's equipment.
     */
    public void recalcAttributes(EntityPlayer player) {
        CustomAttributeMap newMap = new CustomAttributeMap();
        // Register base (non-magic) attributes with default values.
        newMap.registerAttribute(ModAttributes.MAIN_ATTACK_FLAT, 0.0);
        newMap.registerAttribute(ModAttributes.MAIN_ATTACK_PERCENT, 0.0);
        newMap.registerAttribute(ModAttributes.HEALTH, 20.0);
        newMap.registerAttribute(ModAttributes.CRITICAL_CHANCE_PERCENT, 0.0);
        newMap.registerAttribute(ModAttributes.CRITICAL_DAMAGE_FLAT, 0.0);

        if(player != null) {
            // Process held item:
            ItemStack held = player.getHeldItem();
            if(held != null) {
                for (Map.Entry<String, Double> entry : CNPCItemAttributeHelper.readAttributes(held).entrySet()) {
                    String key = entry.getKey();
                    double value = entry.getValue();
                    if("cnpc:main_attack_flat".equals(key)) {
                        IAttributeInstance inst = newMap.getAttributeInstance(ModAttributes.MAIN_ATTACK_FLAT);
                        inst.setBaseValue(inst.getBaseValue() + value);
                    } else if("cnpc:main_attack_percent".equals(key)) {
                        IAttributeInstance inst = newMap.getAttributeInstance(ModAttributes.MAIN_ATTACK_PERCENT);
                        inst.setBaseValue(inst.getBaseValue() + value);
                    } else if("cnpc:critical_chance_percent".equals(key)) {
                        IAttributeInstance inst = newMap.getAttributeInstance(ModAttributes.CRITICAL_CHANCE_PERCENT);
                        inst.setBaseValue(inst.getBaseValue() + value);
                    } else if("cnpc:critical_damage_flat".equals(key)) {
                        IAttributeInstance inst = newMap.getAttributeInstance(ModAttributes.CRITICAL_DAMAGE_FLAT);
                        inst.setBaseValue(inst.getBaseValue() + value);
                    }
                }
            }
            // Process armor pieces:
            for (ItemStack armor : player.inventory.armorInventory) {
                if(armor != null) {
                    for (Map.Entry<String, Double> entry : CNPCItemAttributeHelper.readAttributes(armor).entrySet()) {
                        String key = entry.getKey();
                        double value = entry.getValue();
                        if("cnpc:main_attack_flat".equals(key)) {
                            IAttributeInstance inst = newMap.getAttributeInstance(ModAttributes.MAIN_ATTACK_FLAT);
                            inst.setBaseValue(inst.getBaseValue() + value);
                        } else if("cnpc:main_attack_percent".equals(key)) {
                            IAttributeInstance inst = newMap.getAttributeInstance(ModAttributes.MAIN_ATTACK_PERCENT);
                            inst.setBaseValue(inst.getBaseValue() + value);
                        } else if("cnpc:health".equals(key)) {
                            IAttributeInstance inst = newMap.getAttributeInstance(ModAttributes.HEALTH);
                            inst.setBaseValue(inst.getBaseValue() + value);
                        }
                    }
                }
            }
        }
        aggregatedAttributes = newMap;
    }

    /**
     * Checks if the player's equipment has changed (by comparing a snapshot) and recalculates if needed.
     * This should be called every 10 ticks.
     */
    public void updateIfChanged(EntityPlayer player) {
        String currentSnapshot = computeEquipmentSnapshot(player);
        if (!currentSnapshot.equals(equipmentSnapshot)) {
            equipmentSnapshot = currentSnapshot;
            recalcAttributes(player);
        }
    }

    public double getAttributeValue(AttributeDefinition def) {
        IAttributeInstance inst = aggregatedAttributes.getAttributeInstance(def);
        return inst != null ? inst.getAttributeValue() : 0.0;
    }
}
