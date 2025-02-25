package noppes.npcs.attribute;

/**
 * Registers core (non‑magic) attribute definitions.
 * For example, main attack damage, health, critical chance, etc.
 */
public class ModAttributes {
    public static final AttributeDefinition MAIN_ATTACK_FLAT;
    public static final AttributeDefinition MAIN_ATTACK_PERCENT;
    public static final AttributeDefinition HEALTH;
    public static final AttributeDefinition CRITICAL_CHANCE_PERCENT;
    public static final AttributeDefinition CRITICAL_DAMAGE_FLAT;

    static {
        MAIN_ATTACK_FLAT = AttributeController.registerAttribute("cnpc:main_attack_flat", "Main Attack Damage (Flat)", AttributeValueType.FLAT);
        MAIN_ATTACK_PERCENT = AttributeController.registerAttribute("cnpc:main_attack_percent", "Main Attack Damage (%)", AttributeValueType.PERCENT);
        HEALTH = AttributeController.registerAttribute("cnpc:health", "Health", AttributeValueType.FLAT);
        CRITICAL_CHANCE_PERCENT = AttributeController.registerAttribute("cnpc:critical_chance_percent", "Critical Chance (%)", AttributeValueType.PERCENT);
        CRITICAL_DAMAGE_FLAT = AttributeController.registerAttribute("cnpc:critical_damage_flat", "Critical Damage Bonus (Flat)", AttributeValueType.FLAT);
    }
}
