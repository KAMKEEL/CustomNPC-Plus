package noppes.npcs.attribute.player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import noppes.npcs.attribute.*;

public class PlayerAttributeTracker {

    private final UUID playerId;
    private PlayerAttributeMap playerAttributes = new PlayerAttributeMap();

    public final int extraHealth = 0;
    public final Map<Integer, Double> magicDamage = new HashMap<>();
    public final Map<Integer, Double> magicBoost = new HashMap<>();
    public final Map<Integer, Double> magicDefense = new HashMap<>();
    public final Map<Integer, Double> magicResistance = new HashMap<>();

    private PlayerEquipmentTracker equipmentTracker = new PlayerEquipmentTracker();

    public PlayerAttributeTracker(UUID playerId) {
        this.playerId = playerId;
        // Initial recalc with base damage 0.
        recalcAttributes(null);
    }

    public PlayerAttributeMap getPlayerAttributes() {
        return playerAttributes;
    }

    public void recalcAttributes(EntityPlayer player) {
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
            ItemStack[] equipment = new ItemStack[] { currentEquip.heldItem, currentEquip.boots, currentEquip.leggings, currentEquip.chestplate, currentEquip.helmet };
            for (ItemStack piece : equipment) {
                if (piece != null) {
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
                    // Process Magic

                    // OFFENSIVE MAGIC
                    Map<Integer, Double> magicFlat = ItemAttributeHelper.readMagicAttributeMap(piece, ModAttributes.MAGIC_DAMAGE_KEY);
                    for (Entry<Integer, Double> entry : magicFlat.entrySet()) {
                        int magicId = entry.getKey();
                        double value = entry.getValue();
                        magicDamage.put(magicId, magicDamage.getOrDefault(magicId, 0.0) + value);
                    }
                    Map<Integer, Double> magicPercent = ItemAttributeHelper.readMagicAttributeMap(piece, ModAttributes.MAGIC_BOOST_KEY);
                    for (Entry<Integer, Double> entry : magicPercent.entrySet()) {
                        int magicId = entry.getKey();
                        double value = entry.getValue();
                        magicBoost.put(magicId, magicBoost.getOrDefault(magicId, 0.0) + value);
                    }

                    // DEFENSIVE MAGIC
                    Map<Integer, Double> magicDefFlat = ItemAttributeHelper.readMagicAttributeMap(piece, ModAttributes.MAGIC_DEFENSE_KEY);
                    for (Entry<Integer, Double> entry : magicDefFlat.entrySet()) {
                        int magicId = entry.getKey();
                        double value = entry.getValue();
                        magicDefense.put(magicId, magicDefense.getOrDefault(magicId, 0.0) + value);
                    }
                    Map<Integer, Double> magicResist = ItemAttributeHelper.readMagicAttributeMap(piece, ModAttributes.MAGIC_RESISTANCE_KEY);
                    for (Entry<Integer, Double> entry : magicResist.entrySet()) {
                        int magicId = entry.getKey();
                        double value = entry.getValue();
                        magicResistance.put(magicId, magicResistance.getOrDefault(magicId, 0.0) + value);
                    }
                }
            }
            equipmentTracker = currentEquip;
        }
    }

    /**
     * Checks if the player's equipment has changed and recalculates attributes if needed.
     */
    public void updateIfChanged(EntityPlayer player) {
        PlayerEquipmentTracker currentEquip = new PlayerEquipmentTracker();
        currentEquip.updateFrom(player);
        if (!equipmentTracker.equals(player)) {
            recalcAttributes(player);
        }
    }

    public double getAttributeValue(AttributeDefinition def) {
        IAttributeInstance inst = playerAttributes.getAttributeInstance(def);
        return inst != null ? inst.getValue() : 0.0;
    }
}
