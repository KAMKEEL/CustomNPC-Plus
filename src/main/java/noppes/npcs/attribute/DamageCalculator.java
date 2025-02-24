package noppes.npcs.attribute;

import java.util.Map;
import java.util.Random;
import java.util.Collection;
import net.minecraft.item.ItemStack;

/**
 * Calculates a weapon’s total damage against an armor set.
 * It reads the weapon’s main attack (flat and percent) plus any magic-based attributes.
 * Then applies a critical chance (if the random roll is below the crit chance,
 * the damage is doubled and a flat crit bonus is added).
 */
public class DamageCalculator {
    private static final Random random = new Random();

    public static double calculateTotalDamage(ItemStack weapon, Collection<ItemStack> armorSet) {
        // Read non-magic weapon attributes:
        Map<String, Double> weaponAttrs = CNPCItemAttributeHelper.readAttributes(weapon);
        double mainFlat = weaponAttrs.getOrDefault("cnpc:main_attack_flat", 0.0);
        double mainPercent = weaponAttrs.getOrDefault("cnpc:main_attack_percent", 0.0);
        double baseDamage = mainFlat * (1 + mainPercent);

        double totalMagicDamage = 0.0;
        Map<Integer, Double> weaponMagicFlat = CNPCItemAttributeHelper.readMagicAttributeMap(weapon, "cnpc:magic_damage_flat");
        Map<Integer, Double> weaponMagicPercent = CNPCItemAttributeHelper.readMagicAttributeMap(weapon, "cnpc:magic_damage_percent");

        for(Map.Entry<Integer, Double> entry : weaponMagicFlat.entrySet()) {
            int magicId = entry.getKey();
            double flat = entry.getValue();
            double percent = weaponMagicPercent.getOrDefault(magicId, 0.0);
            double weaponMagicDamage = flat * (1 + percent);

            // Aggregate defenses from all armor pieces for this magic.
            double armorMagicDefense = 0.0;
            double armorMagicResistance = 0.0;
            for(ItemStack armor : armorSet) {
                Map<Integer, Double> armorDefFlat = CNPCItemAttributeHelper.readMagicAttributeMap(armor, "cnpc:magic_defense_flat");
                Map<Integer, Double> armorResistPercent = CNPCItemAttributeHelper.readMagicAttributeMap(armor, "cnpc:magic_resistance_percent");
                armorMagicDefense += armorDefFlat.getOrDefault(magicId, 0.0);
                armorMagicResistance += armorResistPercent.getOrDefault(magicId, 0.0);
            }
            double effectiveMagicDamage = weaponMagicDamage - armorMagicDefense;
            effectiveMagicDamage *= (1 - armorMagicResistance);
            if(effectiveMagicDamage < 0) effectiveMagicDamage = 0;
            totalMagicDamage += effectiveMagicDamage;
        }

        double totalDamage = baseDamage + totalMagicDamage;

        // Process critical hit chance.
        double critChance = weaponAttrs.getOrDefault("cnpc:critical_chance_percent", 0.0);
        double critBonus = weaponAttrs.getOrDefault("cnpc:critical_damage_flat", 0.0);
        if(random.nextDouble() < critChance) {
            totalDamage = totalDamage * 2 + critBonus;
        }
        return Math.max(totalDamage, 0);
    }
}
