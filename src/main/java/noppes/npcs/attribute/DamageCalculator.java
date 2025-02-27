package noppes.npcs.attribute;

import noppes.npcs.attribute.player.PlayerAttributeTracker;
import noppes.npcs.controllers.MagicController;

import java.util.Map;
import java.util.Random;

public class DamageCalculator {
    private static final Random random = new Random();

    public static double calculateDamage(PlayerAttributeTracker attacker, PlayerAttributeTracker defender, double baseDamage) {

        // Calculate physical damage
        double mainAttack = attacker.getAttributeValue(ModAttributes.MAIN_ATTACK);
        double mainBoost = attacker.getAttributeValue(ModAttributes.MAGIC_BOOST) + 1;
        double physicalDamage = (baseDamage * mainBoost) + mainAttack;

        // Calculate neutral damage
        double neutralDamage = attacker.getAttributeValue(ModAttributes.NEUTRAL_ATTACK);
        double neutralBoost = attacker.getAttributeValue(ModAttributes.NEUTRAL_BOOST) + 1;
        double neutralTotal = neutralDamage * neutralBoost;

        // Calculate magic damage
        double magicTotal = 0.0;
        MagicController magicController = MagicController.getInstance();
        for (Map.Entry<Integer, Double> entry : attacker.magicDamage.entrySet()) {
            int magicId = entry.getKey();
            if (magicController.getMagic(magicId) != null) {
                double attackMagic = entry.getValue();
                double defenseMagic = defender.magicDefense.getOrDefault(magicId, 0.0);
                double effectiveMagic = attackMagic - defenseMagic;

                double boostMagic = attacker.magicBoost.getOrDefault(magicId, 0.0);
                double resistanceMagic = defender.magicResistance.getOrDefault(magicId, 0.0);
                double effectiveBoost = boostMagic - resistanceMagic;

                double magicDamageForMagic = effectiveMagic * (effectiveBoost + 1);
                // Ensure magic damage doesn't drop below 0 for this magic
                magicTotal += Math.max(0, magicDamageForMagic);
            }
        }

        // Sum all damage components
        double totalDamage = physicalDamage + neutralTotal + magicTotal;

        // Critical hit: if critical, double damage and add bonus
        double criticalChance = attacker.getAttributeValue(ModAttributes.CRITICAL_CHANCE);
        double criticalBonus = attacker.getAttributeValue(ModAttributes.CRITICAL_DAMAGE);
        if (random.nextDouble() < (criticalChance / 100)) {
            totalDamage = (totalDamage * 2) + criticalBonus;
        }

        return totalDamage;
    }



}
