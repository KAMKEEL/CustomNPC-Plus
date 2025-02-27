package kamkeel.npcs.controllers.data.attribute.tracker;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;

import kamkeel.npcs.CustomAttributes;
import kamkeel.npcs.controllers.AttributeController;
import kamkeel.npcs.controllers.data.attribute.AttributeDefinition;
import kamkeel.npcs.controllers.data.attribute.ICustomAttribute;
import kamkeel.npcs.controllers.data.attribute.PlayerAttributeMap;
import kamkeel.npcs.util.AttributeAttackUtil;
import kamkeel.npcs.util.AttributeItemUtil;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class PlayerAttributeTracker {

    public  static UUID healthUUID = UUID.fromString("48a0ad75-2cf8-4838-ad2f-9a0aadc57dfe");


    private final UUID playerId;
    private PlayerAttributeMap playerAttributes = new PlayerAttributeMap();

    public float extraHealth = 0;
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

        int newExtraHealth = 0;
        if (player != null) {
            // Update equipment tracker.
            PlayerEquipmentTracker currentEquip = new PlayerEquipmentTracker();
            currentEquip.updateFrom(player);
            ItemStack[] equipment = new ItemStack[] { currentEquip.heldItem, currentEquip.boots, currentEquip.leggings, currentEquip.chestplate, currentEquip.helmet };
            for (ItemStack piece : equipment) {
                if (piece != null) {
                    for (Entry<String, Float> entry : AttributeItemUtil.readAttributes(piece).entrySet()) {
                        String key = entry.getKey();
                        float value = entry.getValue();
                        AttributeDefinition def = AttributeController.getAttribute(key);
                        if (def != null) {
                            ICustomAttribute inst = playerAttributes.getAttributeInstance(def);
                            if (inst == null) {
                                inst = playerAttributes.registerAttribute(def, 0.0f);
                            }
                            inst.setValue(inst.getValue() + value);
                        }
                    }
                    // Process Magic

                    // OFFENSIVE MAGIC
                    Map<Integer, Float> magicFlat = AttributeItemUtil.readMagicAttributeMap(piece, CustomAttributes.MAGIC_DAMAGE_KEY);
                    for (Entry<Integer, Float> entry : magicFlat.entrySet()) {
                        int magicId = entry.getKey();
                        float value = entry.getValue();
                        magicDamage.put(magicId, magicDamage.getOrDefault(magicId, 0.0f) + value);
                    }
                    Map<Integer, Float> magicPercent = AttributeItemUtil.readMagicAttributeMap(piece, CustomAttributes.MAGIC_BOOST_KEY);
                    for (Entry<Integer, Float> entry : magicPercent.entrySet()) {
                        int magicId = entry.getKey();
                        float value = entry.getValue();
                        magicBoost.put(magicId, magicBoost.getOrDefault(magicId, 0.0f) + value);
                    }

                    // DEFENSIVE MAGIC
                    Map<Integer, Float> magicDefFlat = AttributeItemUtil.readMagicAttributeMap(piece, CustomAttributes.MAGIC_DEFENSE_KEY);
                    for (Entry<Integer, Float> entry : magicDefFlat.entrySet()) {
                        int magicId = entry.getKey();
                        float value = entry.getValue();
                        magicDefense.put(magicId, magicDefense.getOrDefault(magicId, 0.0f) + value);
                    }
                    Map<Integer, Float> magicResist = AttributeItemUtil.readMagicAttributeMap(piece, CustomAttributes.MAGIC_RESISTANCE_KEY);
                    for (Entry<Integer, Float> entry : magicResist.entrySet()) {
                        int magicId = entry.getKey();
                        float value = entry.getValue();
                        magicResistance.put(magicId, magicResistance.getOrDefault(magicId, 0.0f) + value);
                    }
                }
            }
            equipmentTracker = currentEquip;
            maximumOutput = AttributeAttackUtil.calculateMaximumOutput(this);
            extraHealth = getAttributeValue(CustomAttributes.HEALTH);

            updatePlayerMaxHealth(player);
        }
    }

    /**
     * Updates the player's maximum health attribute by applying a custom attribute modifier.
     * In this system, the aggregated HEALTH attribute is treated as bonus health on top of a base of 20.
     *
     * @param player The EntityPlayer whose max health should be updated.
     */
    public void updatePlayerMaxHealth(EntityPlayer player) {
        float bonusHealth = extraHealth;
        IAttributeInstance maxHealthAttr = player.getAttributeMap().getAttributeInstance(SharedMonsterAttributes.maxHealth);
        try {
            AttributeModifier old = maxHealthAttr.getModifier(healthUUID);
            if (old != null) {
                maxHealthAttr.removeModifier(old);
            }
        } catch (Exception ignored) {}
        if (bonusHealth != 0) {
            maxHealthAttr.applyModifier(new AttributeModifier(healthUUID, "RPGCoreHealthBonus", bonusHealth, 0));
        }
        // Ensure the player's current health does not exceed the new maximum.
        float currentHealth = player.getHealth();
        if (currentHealth > maxHealthAttr.getAttributeValue()) {
            player.setHealth((float) maxHealthAttr.getAttributeValue());
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
        ICustomAttribute inst = playerAttributes.getAttributeInstance(def);
        return inst != null ? inst.getValue() : 0.0f;
    }
}
