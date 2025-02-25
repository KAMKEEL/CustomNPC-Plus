package noppes.npcs.attribute;

import java.util.Random;

/**
 * Calculates damage using cached (aggregated) offensive and defensive ratings from players.
 * The formula here is simple: baseDamage = attacker.getOffense() - defender.getDefense(),
 * then a critical chance is applied (if a random roll is below the attacker's crit chance,
 * damage is doubled and a flat crit bonus is added).
 */
public class DamageCalculator {
    private static final Random random = new Random();

    /**
     * Calculates damage given attacker and defender attribute trackers.
     */
    public static double calculateDamage(PlayerAttributeTracker attacker, PlayerAttributeTracker defender) {
        double offense = attacker.getOffense();
        double defense = defender.getDefense();
        double damage = offense - defense;

        // Process critical hit chance from attacker's aggregated non-magic attributes.
        double critChance = attacker.getAttributeValue(ModAttributes.CRITICAL_CHANCE_PERCENT);
        double critBonus = attacker.getAttributeValue(ModAttributes.CRITICAL_DAMAGE_FLAT);
        if (random.nextDouble() < critChance) {
            damage = damage * 2 + critBonus;
        }
        return Math.max(damage, 0);
    }

    /**
     * Optionally, calculates damage for a specific magic type.
     */
    public static double calculateMagicDamage(PlayerAttributeTracker attacker, PlayerAttributeTracker defender, int magicId) {
        double offense = attacker.getOffense(magicId);
        double defense = defender.getDefense(magicId);
        return Math.max(offense - defense, 0);
    }
}
