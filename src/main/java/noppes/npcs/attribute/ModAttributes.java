package noppes.npcs.attribute;

/**
 * ModAttributes registers the core attributes (both non–magic and magic–based).
 */
public class ModAttributes {
    public static AttributeDefinition MAIN_ATTACK_FLAT;
    public static AttributeDefinition MAIN_ATTACK_PERCENT;
    public static AttributeDefinition HEALTH;
    public static AttributeDefinition CRITICAL_CHANCE_PERCENT;
    public static AttributeDefinition CRITICAL_DAMAGE_FLAT;

    public static AttributeDefinition MAGIC_DAMAGE_FLAT;
    public static AttributeDefinition MAGIC_DAMAGE_PERCENT;
    public static AttributeDefinition MAGIC_DEFENSE_FLAT;
    public static AttributeDefinition MAGIC_RESISTANCE_PERCENT;

    public ModAttributes(){
        MAIN_ATTACK_FLAT = AttributeController.registerAttribute(AttributeKeys.MAIN_ATTACK_FLAT, "Main Attack Damage", AttributeValueType.FLAT);
        MAIN_ATTACK_PERCENT = AttributeController.registerAttribute(AttributeKeys.MAIN_ATTACK_PERCENT, "Main Attack Damage (%)", AttributeValueType.PERCENT);
        HEALTH = AttributeController.registerAttribute(AttributeKeys.HEALTH, "Health", AttributeValueType.FLAT);
        CRITICAL_CHANCE_PERCENT = AttributeController.registerAttribute(AttributeKeys.CRITICAL_CHANCE_PERCENT, "Critical Chance (%)", AttributeValueType.PERCENT);
        CRITICAL_DAMAGE_FLAT = AttributeController.registerAttribute(AttributeKeys.CRITICAL_DAMAGE_FLAT, "Critical Damage Bonus", AttributeValueType.FLAT);

        MAGIC_DAMAGE_FLAT = AttributeController.registerAttribute(AttributeKeys.MAGIC_DAMAGE_FLAT, "Magic Damage", AttributeValueType.MAGIC);
        MAGIC_DAMAGE_PERCENT = AttributeController.registerAttribute(AttributeKeys.MAGIC_DAMAGE_PERCENT, "Magic Damage (%)", AttributeValueType.MAGIC);
        MAGIC_DEFENSE_FLAT = AttributeController.registerAttribute(AttributeKeys.MAGIC_DEFENSE_FLAT, "Magic Defense", AttributeValueType.MAGIC);
        MAGIC_RESISTANCE_PERCENT = AttributeController.registerAttribute(AttributeKeys.MAGIC_RESISTANCE_PERCENT, "Magic Resistance (%)", AttributeValueType.MAGIC);
    }
}
