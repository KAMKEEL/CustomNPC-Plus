package noppes.npcs.attribute;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import java.util.Map.Entry;

/**
 * Aggregates a player's attributes based on the attributes found on their held item and armor.
 * Non–magic attributes are stored in a CustomAttributeMap (using string keys via AttributeController).
 * Magic attributes (offense and defense) are aggregated as maps, keyed by magic type.
 * This tracker uses a PlayerEquipmentTracker to keep track of the player's equipment,
 * and recalculates only when the player's held item or armor changes.
 */
public class PlayerAttributeTracker {
    private final UUID playerId;
    private CustomAttributeMap aggregatedAttributes = new CustomAttributeMap();

    // Aggregated magic offensive values:
    private final Map<Integer, Double> aggregatedMagicDamageFlat = new HashMap<>();
    private final Map<Integer, Double> aggregatedMagicDamagePercent = new HashMap<>();
    // Aggregated magic defensive values:
    private final Map<Integer, Double> aggregatedMagicDefenseFlat = new HashMap<>();
    private final Map<Integer, Double> aggregatedMagicResistancePercent = new HashMap<>();

    // Stores the last-known equipment.
    private PlayerEquipmentTracker equipmentTracker = new PlayerEquipmentTracker();

    public PlayerAttributeTracker(UUID playerId) {
        this.playerId = playerId;
        // Initialize equipmentTracker from a null player (will be updated on first tick).
        recalcAttributes(null);
    }

    public CustomAttributeMap getAggregatedAttributes() {
        return aggregatedAttributes;
    }

    /**
     * Recalculates the aggregated attributes from the player's equipment.
     * Non–magic attributes are read from each item's CNPCAttributes, and magic attributes
     * are aggregated by summing values from their NBT maps.
     */
    public void recalcAttributes(EntityPlayer player) {
        aggregatedAttributes = new CustomAttributeMap();

        // Reset magic aggregates.
        aggregatedMagicDamageFlat.clear();
        aggregatedMagicDamagePercent.clear();
        aggregatedMagicDefenseFlat.clear();
        aggregatedMagicResistancePercent.clear();

        if (player != null) {
            // Update equipmentTracker with the player's current equipment.
            PlayerEquipmentTracker currentEquip = new PlayerEquipmentTracker();
            currentEquip.updateFrom(player);

            // Process held item (offense).
            ItemStack held = currentEquip.heldItem;
            if (held != null) {
                // Process non-magic attributes.
                for (Entry<String, Double> entry : CNPCItemAttributeHelper.readAttributes(held).entrySet()) {
                    String key = entry.getKey();
                    double value = entry.getValue();
                    AttributeDefinition def = AttributeController.getAttribute(key);
                    if (def != null) {
                        IAttributeInstance inst = aggregatedAttributes.getAttributeInstance(def);
                        if (inst == null) {
                            inst = aggregatedAttributes.registerAttribute(def, 0.0);
                        }
                        inst.setBaseValue(inst.getBaseValue() + value);
                    }
                }
                // Process magic offense.
                Map<Integer, Double> magicFlat = CNPCItemAttributeHelper.readMagicAttributeMap(held, AttributeKeys.MAGIC_DAMAGE_FLAT);
                for (Entry<Integer, Double> entry : magicFlat.entrySet()) {
                    int magicId = entry.getKey();
                    double value = entry.getValue();
                    aggregatedMagicDamageFlat.put(magicId, aggregatedMagicDamageFlat.getOrDefault(magicId, 0.0) + value);
                }
                Map<Integer, Double> magicPercent = CNPCItemAttributeHelper.readMagicAttributeMap(held, AttributeKeys.MAGIC_DAMAGE_PERCENT);
                for (Entry<Integer, Double> entry : magicPercent.entrySet()) {
                    int magicId = entry.getKey();
                    double value = entry.getValue();
                    aggregatedMagicDamagePercent.put(magicId, aggregatedMagicDamagePercent.getOrDefault(magicId, 0.0) + value);
                }
            }
            // Process armor (defense).
            // Assuming armor order: 0: boots, 1: leggings, 2: chestplate, 3: helmet.
            ItemStack[] armor = new ItemStack[] { currentEquip.boots, currentEquip.leggings, currentEquip.chestplate, currentEquip.helmet };
            for (ItemStack piece : armor) {
                if (piece != null) {
                    // Process non-magic attributes.
                    for (Entry<String, Double> entry : CNPCItemAttributeHelper.readAttributes(piece).entrySet()) {
                        String key = entry.getKey();
                        double value = entry.getValue();
                        AttributeDefinition def = AttributeController.getAttribute(key);
                        if (def != null) {
                            IAttributeInstance inst = aggregatedAttributes.getAttributeInstance(def);
                            if (inst == null) {
                                inst = aggregatedAttributes.registerAttribute(def, 0.0);
                            }
                            inst.setBaseValue(inst.getBaseValue() + value);
                        }
                    }
                    // Process magic defense.
                    Map<Integer, Double> magicDefFlat = CNPCItemAttributeHelper.readMagicAttributeMap(piece, AttributeKeys.MAGIC_DEFENSE_FLAT);
                    for (Entry<Integer, Double> entry : magicDefFlat.entrySet()) {
                        int magicId = entry.getKey();
                        double value = entry.getValue();
                        aggregatedMagicDefenseFlat.put(magicId, aggregatedMagicDefenseFlat.getOrDefault(magicId, 0.0) + value);
                    }
                    Map<Integer, Double> magicResist = CNPCItemAttributeHelper.readMagicAttributeMap(piece, AttributeKeys.MAGIC_RESISTANCE_PERCENT);
                    for (Entry<Integer, Double> entry : magicResist.entrySet()) {
                        int magicId = entry.getKey();
                        double value = entry.getValue();
                        aggregatedMagicResistancePercent.put(magicId, aggregatedMagicResistancePercent.getOrDefault(magicId, 0.0) + value);
                    }
                }
            }
            // Update our stored equipment.
            equipmentTracker = currentEquip;
        }
    }

    /**
     * Checks if the player's equipment has changed by comparing stored equipment with the current equipment.
     * This should be called periodically (e.g., every 10 ticks).
     */
    public void updateIfChanged(EntityPlayer player) {
        PlayerEquipmentTracker currentEquip = new PlayerEquipmentTracker();
        currentEquip.updateFrom(player);
        if (!equipmentTracker.equals(player)) {
            recalcAttributes(player);
        }
    }

    /**
     * Returns the aggregated non-magic value for a given attribute definition.
     */
    public double getAttributeValue(AttributeDefinition def) {
        IAttributeInstance inst = aggregatedAttributes.getAttributeInstance(def);
        return inst != null ? inst.getAttributeValue() : 0.0;
    }

    /**
     * Overall offense is calculated as:
     * Non-magic offense (MAIN_ATTACK_FLAT*(1+MAIN_ATTACK_PERCENT))
     * plus the sum over all magic offense: for each magic type, flat*(1+percent).
     */
    public double getOffense() {
        double nonMagic = 0.0;
        AttributeDefinition atkFlat = AttributeController.getAttribute(AttributeKeys.MAIN_ATTACK_FLAT);
        AttributeDefinition atkPercent = AttributeController.getAttribute(AttributeKeys.MAIN_ATTACK_PERCENT);
        if (atkFlat != null && atkPercent != null) {
            nonMagic = getAttributeValue(atkFlat) * (1 + getAttributeValue(atkPercent));
        }
        double magicOffense = 0.0;
        for (Entry<Integer, Double> entry : aggregatedMagicDamageFlat.entrySet()) {
            int magicId = entry.getKey();
            double flat = entry.getValue();
            double percent = aggregatedMagicDamagePercent.getOrDefault(magicId, 0.0);
            magicOffense += flat * (1 + percent);
        }
        return nonMagic + magicOffense;
    }

    /**
     * Returns offense for a specific magic type.
     */
    public double getOffense(int magicId) {
        double flat = aggregatedMagicDamageFlat.getOrDefault(magicId, 0.0);
        double percent = aggregatedMagicDamagePercent.getOrDefault(magicId, 0.0);
        return flat * (1 + percent);
    }

    /**
     * Overall defense is calculated solely from magic defensive attributes.
     * (HEALTH is managed separately.)
     */
    public double getDefense() {
        double magicDefense = 0.0;
        for (Entry<Integer, Double> entry : aggregatedMagicDefenseFlat.entrySet()) {
            int magicId = entry.getKey();
            double flat = entry.getValue();
            double percent = aggregatedMagicResistancePercent.getOrDefault(magicId, 0.0);
            magicDefense += flat * (1 + percent);
        }
        return magicDefense;
    }

    /**
     * Returns defense for a specific magic type.
     */
    public double getDefense(int magicId) {
        double flat = aggregatedMagicDefenseFlat.getOrDefault(magicId, 0.0);
        double percent = aggregatedMagicResistancePercent.getOrDefault(magicId, 0.0);
        return flat * (1 + percent);
    }
}
