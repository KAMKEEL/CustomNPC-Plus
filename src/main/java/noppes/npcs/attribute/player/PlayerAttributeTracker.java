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

    public int extraHealth = 0;
    public float maximumOutput = 0.0f;

    public final Map<Integer, Float> magicDamage = new HashMap<>();
    public final Map<Integer, Float> magicBoost = new HashMap<>();
    public final Map<Integer, Float> magicDefense = new HashMap<>();
    public final Map<Integer, Float> magicResistance = new HashMap<>();

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
                    for (Entry<String, Float> entry : ItemAttributeHelper.readAttributes(piece).entrySet()) {
                        String key = entry.getKey();
                        float value = entry.getValue();
                        AttributeDefinition def = AttributeController.getAttribute(key);
                        if (def != null) {
                            IAttributeInstance inst = playerAttributes.getAttributeInstance(def);
                            if (inst == null) {
                                inst = playerAttributes.registerAttribute(def, 0.0f);
                            }
                            inst.setValue(inst.getValue() + value);
                        }
                    }
                    // Process Magic

                    // OFFENSIVE MAGIC
                    Map<Integer, Float> magicFlat = ItemAttributeHelper.readMagicAttributeMap(piece, ModAttributes.MAGIC_DAMAGE_KEY);
                    for (Entry<Integer, Float> entry : magicFlat.entrySet()) {
                        int magicId = entry.getKey();
                        float value = entry.getValue();
                        magicDamage.put(magicId, magicDamage.getOrDefault(magicId, 0.0f) + value);
                    }
                    Map<Integer, Float> magicPercent = ItemAttributeHelper.readMagicAttributeMap(piece, ModAttributes.MAGIC_BOOST_KEY);
                    for (Entry<Integer, Float> entry : magicPercent.entrySet()) {
                        int magicId = entry.getKey();
                        float value = entry.getValue();
                        magicBoost.put(magicId, magicBoost.getOrDefault(magicId, 0.0f) + value);
                    }

                    // DEFENSIVE MAGIC
                    Map<Integer, Float> magicDefFlat = ItemAttributeHelper.readMagicAttributeMap(piece, ModAttributes.MAGIC_DEFENSE_KEY);
                    for (Entry<Integer, Float> entry : magicDefFlat.entrySet()) {
                        int magicId = entry.getKey();
                        float value = entry.getValue();
                        magicDefense.put(magicId, magicDefense.getOrDefault(magicId, 0.0f) + value);
                    }
                    Map<Integer, Float> magicResist = ItemAttributeHelper.readMagicAttributeMap(piece, ModAttributes.MAGIC_RESISTANCE_KEY);
                    for (Entry<Integer, Float> entry : magicResist.entrySet()) {
                        int magicId = entry.getKey();
                        float value = entry.getValue();
                        magicResistance.put(magicId, magicResistance.getOrDefault(magicId, 0.0f) + value);
                    }
                }
            }
            equipmentTracker = currentEquip;
            maximumOutput = AttributeAttackHelper.calculateMaximumOutput(this);
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

    public float getAttributeValue(AttributeDefinition def) {
        IAttributeInstance inst = playerAttributes.getAttributeInstance(def);
        return inst != null ? inst.getValue() : 0.0f;
    }
}
