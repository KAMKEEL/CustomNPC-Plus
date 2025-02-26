package noppes.npcs.attribute.player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import noppes.npcs.attribute.*;

public class PlayerAttributeTracker {

    public static final int BASE_ID = -100;

    private final UUID playerId;
    private PlayerAttributeMap playerAttributes = new PlayerAttributeMap();

    private final Map<Integer, Double> damageMap = new HashMap<>();
    private final Map<Integer, Double> defenseMap = new HashMap<>();

    private final Map<Integer, Double> magicDamage = new HashMap<>();
    private final Map<Integer, Double> magicBoost = new HashMap<>();
    private final Map<Integer, Double> magicDefense = new HashMap<>();
    private final Map<Integer, Double> magicResistance = new HashMap<>();

    private PlayerEquipmentTracker equipmentTracker = new PlayerEquipmentTracker();

    public PlayerAttributeTracker(UUID playerId) {
        this.playerId = playerId;
        // Initial recalc with base damage 0.
        recalcAttributes(null, 0);
    }

    public PlayerAttributeMap getPlayerAttributes() {
        return playerAttributes;
    }

    public Map<Integer, Double> getOffenseMap() {
        return damageMap;
    }

    public Map<Integer, Double> getDefenseMap() {
        return defenseMap;
    }

    public void recalcAttributes(EntityPlayer player, int baseDamage) {
        // Reset aggregated attributes and magic maps.
        playerAttributes = new PlayerAttributeMap();
        magicDamage.clear();
        magicBoost.clear();
        magicDefense.clear();
        magicResistance.clear();

        if (player != null) {
            // Update equipment tracker.
            PlayerEquipmentTracker currentEquip = new PlayerEquipmentTracker();
            currentEquip.updateFrom(player);

            // Process held item (offense).
            ItemStack held = currentEquip.heldItem;
            if (held != null) {
                // Process non-magic attributes.
                for (Entry<String, Double> entry : ItemAttributeHelper.readAttributes(held).entrySet()) {
                    String key = entry.getKey();
                    double value = entry.getValue();
                    AttributeDefinition def = AttributeController.getAttribute(key);
                    if (def != null) {
                        IAttributeInstance inst = playerAttributes.getAttributeInstance(def);
                        if (inst == null) {
                            inst = playerAttributes.registerAttribute(def, 0.0);
                        }
                        inst.setValue(inst.getValue() + value);
                    }
                }
                // Process magic offense.
                Map<Integer, Double> magicFlat = ItemAttributeHelper.readMagicAttributeMap(held, AttributeKeys.MAGIC_DAMAGE);
                for (Entry<Integer, Double> entry : magicFlat.entrySet()) {
                    int magicId = entry.getKey();
                    double value = entry.getValue();
                    magicDamage.put(magicId, magicDamage.getOrDefault(magicId, 0.0) + value);
                }
                Map<Integer, Double> magicPercent = ItemAttributeHelper.readMagicAttributeMap(held, AttributeKeys.MAGIC_BOOST);
                for (Entry<Integer, Double> entry : magicPercent.entrySet()) {
                    int magicId = entry.getKey();
                    double value = entry.getValue();
                    magicBoost.put(magicId, magicBoost.getOrDefault(magicId, 0.0) + value);
                }
            }
            // Process armor (defense).
            ItemStack[] armor = new ItemStack[] { currentEquip.boots, currentEquip.leggings, currentEquip.chestplate, currentEquip.helmet };
            for (ItemStack piece : armor) {
                if (piece != null) {
                    // Process non-magic attributes.
                    for (Entry<String, Double> entry : ItemAttributeHelper.readAttributes(piece).entrySet()) {
                        String key = entry.getKey();
                        double value = entry.getValue();
                        AttributeDefinition def = AttributeController.getAttribute(key);
                        if (def != null) {
                            IAttributeInstance inst = playerAttributes.getAttributeInstance(def);
                            if (inst == null) {
                                inst = playerAttributes.registerAttribute(def, 0.0);
                            }
                            inst.setValue(inst.getValue() + value);
                        }
                    }
                    // Process magic defense.
                    Map<Integer, Double> magicDefFlat = ItemAttributeHelper.readMagicAttributeMap(piece, AttributeKeys.MAGIC_DEFENSE);
                    for (Entry<Integer, Double> entry : magicDefFlat.entrySet()) {
                        int magicId = entry.getKey();
                        double value = entry.getValue();
                        magicDefense.put(magicId, magicDefense.getOrDefault(magicId, 0.0) + value);
                    }
                    Map<Integer, Double> magicResist = ItemAttributeHelper.readMagicAttributeMap(piece, AttributeKeys.MAGIC_RESISTANCE);
                    for (Entry<Integer, Double> entry : magicResist.entrySet()) {
                        int magicId = entry.getKey();
                        double value = entry.getValue();
                        magicResistance.put(magicId, magicResistance.getOrDefault(magicId, 0.0) + value);
                    }
                }
            }
            equipmentTracker = currentEquip;
        }
        // Recalculate the maps using the provided baseDamage.
        recalcOffenseMap(baseDamage);
        recalcDefenseMap();
    }

    private void recalcOffenseMap(int baseDamage) {
        damageMap.clear();
        // Compute non-magic offense = (baseDamage + MAIN_ATTACK_FLAT) * (1 + MAIN_ATTACK_PERCENT)
        double mainFlat = getAttributeValue(ModAttributes.MAIN_ATTACK_FLAT);
        double mainPercent = getAttributeValue(ModAttributes.MAIN_ATTACK_PERCENT);
        double nonMagicOffense = (baseDamage + mainFlat) * (1 + mainPercent);
        damageMap.put(BASE_ID, nonMagicOffense);

        // For each magic type: offense = (magicDamageFlat) * (1 + magicDamagePercent)
        for (Entry<Integer, Double> entry : magicDamage.entrySet()) {
            int magicId = entry.getKey();
            double flat = entry.getValue();
            double percent = magicBoost.getOrDefault(magicId, 0.0);
            double magicOffense = flat * (1 + percent);
            damageMap.put(magicId, magicOffense);
        }
    }

    private void recalcDefenseMap() {
        defenseMap.clear();
        // For each magic type: defense = (magicDefenseFlat) * (1 + magicResistancePercent)
        for (Entry<Integer, Double> entry : magicDefense.entrySet()) {
            int magicId = entry.getKey();
            double flat = entry.getValue();
            double percent = magicResistance.getOrDefault(magicId, 0.0);
            double magicDef = flat * (1 + percent);
            defenseMap.put(magicId, magicDef);
        }
        // Set non-magic defense to 0.
        defenseMap.put(BASE_ID, 0.0);
    }

    /**
     * Checks if the player's equipment has changed and recalculates attributes if needed.
     */
    public void updateIfChanged(EntityPlayer player) {
        PlayerEquipmentTracker currentEquip = new PlayerEquipmentTracker();
        currentEquip.updateFrom(player);
        if (!equipmentTracker.equals(player)) {
            recalcAttributes(player, 0);
        }
    }

    public double getAttributeValue(AttributeDefinition def) {
        IAttributeInstance inst = playerAttributes.getAttributeInstance(def);
        return inst != null ? inst.getValue() : 0.0;
    }
}
