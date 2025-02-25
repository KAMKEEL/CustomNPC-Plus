package noppes.npcs.attribute;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import java.util.Map.Entry;

/**
 * Aggregates a player's attributes based on their held item and armor.
 * Non‑magic attributes are stored in a CustomAttributeMap.
 * Magic–based attributes (offense and defense) are aggregated as maps:
 * for each magic type, we sum the FLAT and PERCENT values.
 *
 * This tracker is updated (for example, every 10 ticks) if the player's equipment changes.
 */
public class PlayerAttributeTracker {
    private final UUID playerId;
    private CustomAttributeMap aggregatedAttributes = new CustomAttributeMap();
    // Aggregated magic offensive values from items (weapon, etc.)
    private final Map<Integer, Double> aggregatedMagicDamageFlat = new HashMap<>();
    private final Map<Integer, Double> aggregatedMagicDamagePercent = new HashMap<>();
    // Aggregated magic defensive values from armor
    private final Map<Integer, Double> aggregatedMagicDefenseFlat = new HashMap<>();
    private final Map<Integer, Double> aggregatedMagicResistancePercent = new HashMap<>();

    private String equipmentSnapshot = "";

    public PlayerAttributeTracker(UUID playerId) {
        this.playerId = playerId;
        recalcAttributes(null);
    }

    public CustomAttributeMap getAggregatedAttributes() {
        return aggregatedAttributes;
    }

    /**
     * Builds a snapshot string from the player's held item and armor.
     */
    private String computeEquipmentSnapshot(EntityPlayer player) {
        StringBuilder sb = new StringBuilder();
        ItemStack held = player.getHeldItem();
        if (held != null) {
            sb.append(held.getItem().toString()).append(held.getItemDamage());
        }
        for (ItemStack armor : player.inventory.armorInventory) {
            if (armor != null) {
                sb.append(armor.getItem().toString()).append(armor.getItemDamage());
            }
        }
        return sb.toString();
    }

    /**
     * Recalculates the aggregated attributes from the player's equipment.
     * Non‑magic attributes are processed into a CustomAttributeMap,
     * while magic attributes (offense and defense) are aggregated into separate maps.
     */
    public void recalcAttributes(EntityPlayer player) {
        // Reinitialize non-magic attributes:
        CustomAttributeMap newMap = new CustomAttributeMap();
        newMap.registerAttribute(ModAttributes.MAIN_ATTACK_FLAT, 0.0);
        newMap.registerAttribute(ModAttributes.MAIN_ATTACK_PERCENT, 0.0);
        newMap.registerAttribute(ModAttributes.HEALTH, 20.0);
        newMap.registerAttribute(ModAttributes.CRITICAL_CHANCE_PERCENT, 0.0);
        newMap.registerAttribute(ModAttributes.CRITICAL_DAMAGE_FLAT, 0.0);
        aggregatedAttributes = newMap;

        // Reset magic aggregates:
        aggregatedMagicDamageFlat.clear();
        aggregatedMagicDamagePercent.clear();
        aggregatedMagicDefenseFlat.clear();
        aggregatedMagicResistancePercent.clear();

        if (player != null) {
            // Process held item (assumed to be offense—weapon):
            ItemStack held = player.getHeldItem();
            if (held != null) {
                // Non-magic:
                for (Entry<String, Double> entry : CNPCItemAttributeHelper.readAttributes(held).entrySet()) {
                    String key = entry.getKey();
                    double value = entry.getValue();
                    if (AttributeKeys.MAIN_ATTACK_FLAT.equals(key)) {
                        IAttributeInstance inst = aggregatedAttributes.getAttributeInstance(ModAttributes.MAIN_ATTACK_FLAT);
                        inst.setBaseValue(inst.getBaseValue() + value);
                    } else if (AttributeKeys.MAIN_ATTACK_PERCENT.equals(key)) {
                        IAttributeInstance inst = aggregatedAttributes.getAttributeInstance(ModAttributes.MAIN_ATTACK_PERCENT);
                        inst.setBaseValue(inst.getBaseValue() + value);
                    } else if (AttributeKeys.CRITICAL_CHANCE_PERCENT.equals(key)) {
                        IAttributeInstance inst = aggregatedAttributes.getAttributeInstance(ModAttributes.CRITICAL_CHANCE_PERCENT);
                        inst.setBaseValue(inst.getBaseValue() + value);
                    } else if (AttributeKeys.CRITICAL_DAMAGE_FLAT.equals(key)) {
                        IAttributeInstance inst = aggregatedAttributes.getAttributeInstance(ModAttributes.CRITICAL_DAMAGE_FLAT);
                        inst.setBaseValue(inst.getBaseValue() + value);
                    }
                }
                // Magic offense:
                Map<Integer, Double> weaponMagicFlat = CNPCItemAttributeHelper.readMagicAttributeMap(held, AttributeKeys.MAGIC_DAMAGE_FLAT);
                for (Entry<Integer, Double> entry : weaponMagicFlat.entrySet()) {
                    int magicId = entry.getKey();
                    double value = entry.getValue();
                    aggregatedMagicDamageFlat.put(magicId, aggregatedMagicDamageFlat.getOrDefault(magicId, 0.0) + value);
                }
                Map<Integer, Double> weaponMagicPercent = CNPCItemAttributeHelper.readMagicAttributeMap(held, AttributeKeys.MAGIC_DAMAGE_PERCENT);
                for (Entry<Integer, Double> entry : weaponMagicPercent.entrySet()) {
                    int magicId = entry.getKey();
                    double value = entry.getValue();
                    aggregatedMagicDamagePercent.put(magicId, aggregatedMagicDamagePercent.getOrDefault(magicId, 0.0) + value);
                }
            }
            // Process armor (assumed to contribute defense):
            for (ItemStack armor : player.inventory.armorInventory) {
                if (armor != null) {
                    // Non-magic:
                    for (Entry<String, Double> entry : CNPCItemAttributeHelper.readAttributes(armor).entrySet()) {
                        String key = entry.getKey();
                        double value = entry.getValue();
                        if (AttributeKeys.HEALTH.equals(key)) {
                            IAttributeInstance inst = aggregatedAttributes.getAttributeInstance(ModAttributes.HEALTH);
                            inst.setBaseValue(inst.getBaseValue() + value);
                        }
                        // Could add other non-magic defensive bonuses here.
                    }
                    // Magic defense:
                    Map<Integer, Double> armorMagicFlat = CNPCItemAttributeHelper.readMagicAttributeMap(armor, AttributeKeys.MAGIC_DEFENSE_FLAT);
                    for (Entry<Integer, Double> entry : armorMagicFlat.entrySet()) {
                        int magicId = entry.getKey();
                        double value = entry.getValue();
                        aggregatedMagicDefenseFlat.put(magicId, aggregatedMagicDefenseFlat.getOrDefault(magicId, 0.0) + value);
                    }
                    Map<Integer, Double> armorMagicPercent = CNPCItemAttributeHelper.readMagicAttributeMap(armor, AttributeKeys.MAGIC_RESISTANCE_PERCENT);
                    for (Entry<Integer, Double> entry : armorMagicPercent.entrySet()) {
                        int magicId = entry.getKey();
                        double value = entry.getValue();
                        aggregatedMagicResistancePercent.put(magicId, aggregatedMagicResistancePercent.getOrDefault(magicId, 0.0) + value);
                    }
                }
            }
        }
    }

    /**
     * Checks if the player's equipment has changed (using a snapshot) and recalculates if so.
     * Should be called periodically (e.g. every 10 ticks).
     */
    public void updateIfChanged(EntityPlayer player) {
        String currentSnapshot = computeEquipmentSnapshot(player);
        if (!currentSnapshot.equals(equipmentSnapshot)) {
            equipmentSnapshot = currentSnapshot;
            recalcAttributes(player);
        }
    }

    /**
     * Returns the aggregated non-magic value for a given attribute.
     */
    public double getAttributeValue(AttributeDefinition def) {
        IAttributeInstance inst = aggregatedAttributes.getAttributeInstance(def);
        return inst != null ? inst.getAttributeValue() : 0.0;
    }

    /**
     * Returns overall offensive rating:
     * Non-magic offense (MAIN_ATTACK_FLAT*(1+MAIN_ATTACK_PERCENT))
     * plus the sum over all magic offense: for each magic type, flat*(1+percent).
     */
    public double getOffense() {
        double nonMagic = getAttributeValue(ModAttributes.MAIN_ATTACK_FLAT) * (1 + getAttributeValue(ModAttributes.MAIN_ATTACK_PERCENT));
        double magicOffense = 0.0;
        for (Map.Entry<Integer, Double> entry : aggregatedMagicDamageFlat.entrySet()) {
            int magicId = entry.getKey();
            double flat = entry.getValue();
            double percent = aggregatedMagicDamagePercent.getOrDefault(magicId, 0.0);
            magicOffense += flat * (1 + percent);
        }
        return nonMagic + magicOffense;
    }

    /**
     * Returns offensive value for a specific magic type.
     */
    public double getOffense(int magicId) {
        double flat = aggregatedMagicDamageFlat.getOrDefault(magicId, 0.0);
        double percent = aggregatedMagicDamagePercent.getOrDefault(magicId, 0.0);
        return flat * (1 + percent);
    }

    /**
     * Returns overall defensive rating:
     * Non-magic defense is given by HEALTH.
     * Plus sum over all magic defense: for each magic type, flat*(1+percent).
     */
    public double getDefense() {
        double nonMagic = getAttributeValue(ModAttributes.HEALTH);
        double magicDefense = 0.0;
        for (Map.Entry<Integer, Double> entry : aggregatedMagicDefenseFlat.entrySet()) {
            int magicId = entry.getKey();
            double flat = entry.getValue();
            double percent = aggregatedMagicResistancePercent.getOrDefault(magicId, 0.0);
            magicDefense += flat * (1 + percent);
        }
        return nonMagic + magicDefense;
    }

    /**
     * Returns defensive value for a specific magic type.
     */
    public double getDefense(int magicId) {
        double flat = aggregatedMagicDefenseFlat.getOrDefault(magicId, 0.0);
        double percent = aggregatedMagicResistancePercent.getOrDefault(magicId, 0.0);
        return flat * (1 + percent);
    }
}
