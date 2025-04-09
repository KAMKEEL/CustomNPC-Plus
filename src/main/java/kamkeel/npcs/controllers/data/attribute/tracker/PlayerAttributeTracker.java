package kamkeel.npcs.controllers.data.attribute.tracker;

import kamkeel.npcs.CustomAttributes;
import kamkeel.npcs.controllers.AttributeController;
import kamkeel.npcs.controllers.data.attribute.AttributeDefinition;
import kamkeel.npcs.controllers.data.attribute.PlayerAttribute;
import kamkeel.npcs.controllers.data.attribute.PlayerAttributeMap;
import kamkeel.npcs.controllers.data.attribute.requirement.RequirementCheckerRegistry;
import kamkeel.npcs.util.AttributeAttackUtil;
import kamkeel.npcs.util.AttributeItemUtil;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.handler.data.ICustomAttribute;
import noppes.npcs.api.handler.data.IPlayerAttributes;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

public class PlayerAttributeTracker implements IPlayerAttributes {

    public static UUID healthUUID = UUID.fromString("48a0ad75-2cf8-4838-ad2f-9a0aadc57dfe");
    public static UUID healthBoostUUID = UUID.fromString("93cfba41-294a-4e6b-a98b-bee76a9f813b");
    public static UUID movementSpeedUUID = UUID.fromString("e7e0dd10-8ed5-42fb-8167-e71f8de4ea0c");
    public static UUID knockbackResUUID = UUID.fromString("b2f261e8-34f0-4140-9828-b56c1f6e0ff2");

    private final UUID playerId;
    private PlayerAttributeMap playerAttributes = new PlayerAttributeMap();

    public float extraHealth = 0;
    public float extraHealthBoost = 0;
    public float movementSpeed = 0;
    public float knockbackRes = 0;

    public float gearOutput = 0.0f;

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
            ItemStack[] equipment = new ItemStack[]{currentEquip.heldItem, currentEquip.boots, currentEquip.leggings, currentEquip.chestplate, currentEquip.helmet};
            for (ItemStack piece : equipment) {
                if (piece != null) {
                    if (piece.stackTagCompound != null && piece.stackTagCompound.hasKey(AttributeItemUtil.TAG_RPGCORE)) {
                        NBTTagCompound rpgCore = piece.stackTagCompound.getCompoundTag(AttributeItemUtil.TAG_RPGCORE);
                        if (rpgCore.hasKey(AttributeItemUtil.TAG_REQUIREMENTS)) {
                            NBTTagCompound reqTag = rpgCore.getCompoundTag(AttributeItemUtil.TAG_REQUIREMENTS);
                            if (!RequirementCheckerRegistry.checkRequirements(player, reqTag)) {
                                continue;
                            }
                        }
                    }

                    for (Entry<String, Float> entry : AttributeItemUtil.readAttributes(piece).entrySet()) {
                        String key = entry.getKey();
                        float value = entry.getValue();
                        AttributeDefinition def = AttributeController.getAttribute(key);
                        if (def != null) {
                            PlayerAttribute inst = playerAttributes.getAttributeInstance(def);
                            if (inst == null) {
                                inst = playerAttributes.registerAttribute(def, 0.0f);
                            }
                            inst.value = (inst.getValue() + value);
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
            gearOutput = AttributeAttackUtil.calculateGearOutput(this);

            extraHealth = getAttributeValue(CustomAttributes.HEALTH);
            extraHealthBoost = getAttributeValue(CustomAttributes.HEALTH_BOOST);
            movementSpeed = getAttributeValue(CustomAttributes.MOVEMENT_SPEED);
            knockbackRes = getAttributeValue(CustomAttributes.KNOCKBACK_RES);

            // Update vanilla attributes with our aggregated values.
            updatePlayerMaxHealth(player);
            updatePlayerMovementSpeed(player);
            updatePlayerKnockbackRes(player);

            // Post Recalculation Event to Listeners
            AttributeRecalcEvent.post(player, this);
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
        float healthBoostPercent = extraHealthBoost  / 100;
        IAttributeInstance maxHealthAttr = player.getAttributeMap().getAttributeInstance(SharedMonsterAttributes.maxHealth);
        try {
            AttributeModifier oldFlat = maxHealthAttr.getModifier(healthUUID);
            if (oldFlat != null) {
                maxHealthAttr.removeModifier(oldFlat);
            }
            AttributeModifier oldBoost = maxHealthAttr.getModifier(healthBoostUUID);
            if (oldBoost != null) {
                maxHealthAttr.removeModifier(oldBoost);
            }
        } catch (Exception ignored) { }
        // Apply the flat bonus (operation 0)
        if (bonusHealth != 0) {
            maxHealthAttr.applyModifier(new AttributeModifier(healthUUID, "RPGCoreHealthBonus", bonusHealth, 0));
        }
        // Apply the percent boost (operation 1 multiplies the base health, here assumed to be 20)
        if (healthBoostPercent != 0) {
            maxHealthAttr.applyModifier(new AttributeModifier(healthBoostUUID, "RPGCoreHealthBoost", healthBoostPercent, 1));
        }
        // Ensure current health is within the new maximum.
        float currentHealth = player.getHealth();
        if (currentHealth > maxHealthAttr.getAttributeValue()) {
            player.setHealth((float) maxHealthAttr.getAttributeValue());
        }
    }

    public void updatePlayerMovementSpeed(EntityPlayer player) {
        float extraSpeed = movementSpeed  / 100;
        IAttributeInstance speedAttr = player.getAttributeMap().getAttributeInstance(SharedMonsterAttributes.movementSpeed);
        try {
            AttributeModifier old = speedAttr.getModifier(movementSpeedUUID);
            if (old != null) {
                speedAttr.removeModifier(old);
            }
        } catch (Exception ignored) { }
        if (extraSpeed != 0) {
            speedAttr.applyModifier(new AttributeModifier(movementSpeedUUID, "RPGCoreMovementSpeed", extraSpeed, 1));
        }
    }

    public void updatePlayerKnockbackRes(EntityPlayer player) {
        float extraRes = knockbackRes / 100;
        IAttributeInstance knockResAttr = player.getAttributeMap().getAttributeInstance(SharedMonsterAttributes.knockbackResistance);
        try {
            AttributeModifier old = knockResAttr.getModifier(knockbackResUUID);
            if (old != null) {
                knockResAttr.removeModifier(old);
            }
        } catch (Exception ignored) { }
        if (extraRes != 0) {
            knockResAttr.applyModifier(new AttributeModifier(knockbackResUUID, "RPGCoreKnockbackRes", extraRes, 1));
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

    public void recalculate(IPlayer player) {
        if(player == null || player.getMCEntity() == null)
            return;

        recalcAttributes((EntityPlayer) player.getMCEntity());
    }

    public ICustomAttribute[] getAttributes() {
        return playerAttributes.map.values().toArray(new ICustomAttribute[playerAttributes.map.size()]);
    }

    public float getAttributeValue(String key) {
        for (ICustomAttribute inst : getAttributes()) {
            if (inst.getAttribute().getKey().equalsIgnoreCase(key)) {
                return inst.getValue();
            }
        }
        return 0.0f;
    }


    public boolean hasAttribute(String key) {
        for (ICustomAttribute inst : getAttributes()) {
            if (inst.getAttribute().getKey().equalsIgnoreCase(key)) {
                return true;
            }
        }
        return false;
    }

    public ICustomAttribute getAttribute(String key) {
        for (ICustomAttribute inst : getAttributes()) {
            if (inst.getAttribute().getKey().equalsIgnoreCase(key)) {
                return inst;
            }
        }
        return null;
    }
}
