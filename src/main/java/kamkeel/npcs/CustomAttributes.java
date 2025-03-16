package kamkeel.npcs;

import kamkeel.npcs.controllers.AttributeController;
import kamkeel.npcs.controllers.data.attribute.AttributeDefinition;
import kamkeel.npcs.controllers.data.attribute.AttributeValueType;
import kamkeel.npcs.controllers.data.attribute.requirement.RequirementCheckerRegistry;
import kamkeel.npcs.controllers.data.attribute.requirement.types.QuestActiveRequirement;
import kamkeel.npcs.controllers.data.attribute.requirement.types.QuestCompletedRequirement;

/**
 * ModAttributes registers the core attributes (both non–magic and magic–based).
 */
public class CustomAttributes {
    public static final String HEALTH_KEY = "health";
    public static final String HEALTH_BOOST_KEY = "health_boost";
    public static final String MAIN_ATTACK_KEY = "main_attack";
    public static final String MAIN_ATTACK_BOOST_KEY = "main_attack_boost";
    public static final String NEUTRAL_KEY = "neutral";
    public static final String NEUTRAL_BOOST_KEY = "neutral_boost";
    public static final String CRITICAL_DAMAGE_KEY = "critical_damage";
    public static final String CRITICAL_CHANCE_KEY = "critical_chance";
    public static final String MAGIC_DAMAGE_KEY = "magic_damage";
    public static final String MAGIC_BOOST_KEY = "magic_boost";
    public static final String MAGIC_DEFENSE_KEY = "magic_defense";
    public static final String MAGIC_RESISTANCE_KEY = "magic_resistance";
    public static final String MOVEMENT_SPEED_KEY = "movement_speed";
    public static final String KNOCKBACK_RES_KEY = "knockback_res";

    public static AttributeDefinition HEALTH;
    public static AttributeDefinition HEALTH_BOOST;
    public static AttributeDefinition MAIN_ATTACK;
    public static AttributeDefinition MAIN_BOOST;
    public static AttributeDefinition NEUTRAL_ATTACK;
    public static AttributeDefinition NEUTRAL_BOOST;
    public static AttributeDefinition CRITICAL_CHANCE;
    public static AttributeDefinition CRITICAL_DAMAGE;
    public static AttributeDefinition MAGIC_DAMAGE;
    public static AttributeDefinition MAGIC_BOOST;
    public static AttributeDefinition MAGIC_DEFENSE;
    public static AttributeDefinition MAGIC_RESISTANCE;
    public static AttributeDefinition MOVEMENT_SPEED;
    public static AttributeDefinition KNOCKBACK_RES;

    public CustomAttributes() {
        HEALTH = AttributeController.registerAttribute(HEALTH_KEY, "Health", 'c', AttributeValueType.FLAT, AttributeDefinition.AttributeSection.BASE);
        HEALTH_BOOST = AttributeController.registerAttribute(HEALTH_BOOST_KEY, "Health Boost", 'c', AttributeValueType.PERCENT, AttributeDefinition.AttributeSection.MODIFIER);
        HEALTH_BOOST.setTranslationKey("attribute.name.generic.maxHealth");

        MAIN_ATTACK = AttributeController.registerAttribute(MAIN_ATTACK_KEY, "Main Attack Damage", '4', AttributeValueType.FLAT, AttributeDefinition.AttributeSection.BASE);
        MAIN_BOOST = AttributeController.registerAttribute(MAIN_ATTACK_BOOST_KEY, "Main Attack Damage", '4', AttributeValueType.PERCENT, AttributeDefinition.AttributeSection.MODIFIER);

        NEUTRAL_ATTACK = AttributeController.registerAttribute(NEUTRAL_KEY, "Neutral Damage", '6', AttributeValueType.FLAT, AttributeDefinition.AttributeSection.BASE);
        NEUTRAL_BOOST = AttributeController.registerAttribute(NEUTRAL_BOOST_KEY, "Neutral Damage", '6', AttributeValueType.PERCENT, AttributeDefinition.AttributeSection.MODIFIER);

        CRITICAL_CHANCE = AttributeController.registerAttribute(CRITICAL_CHANCE_KEY, "Critical Chance", '6', AttributeValueType.PERCENT, AttributeDefinition.AttributeSection.INFO);
        CRITICAL_DAMAGE = AttributeController.registerAttribute(CRITICAL_DAMAGE_KEY, "Critical Damage", '6', AttributeValueType.FLAT, AttributeDefinition.AttributeSection.INFO);

        MAGIC_DAMAGE = AttributeController.registerAttribute(MAGIC_DAMAGE_KEY, "Magic Damage", '3', AttributeValueType.MAGIC, AttributeDefinition.AttributeSection.BASE);
        MAGIC_BOOST = AttributeController.registerAttribute(MAGIC_BOOST_KEY, "Magic Damage", '3', AttributeValueType.MAGIC, AttributeDefinition.AttributeSection.MODIFIER);

        MAGIC_DEFENSE = AttributeController.registerAttribute(MAGIC_DEFENSE_KEY, "Magic Defense", 'b', AttributeValueType.MAGIC, AttributeDefinition.AttributeSection.BASE);
        MAGIC_RESISTANCE = AttributeController.registerAttribute(MAGIC_RESISTANCE_KEY, "Magic Resistance", 'b', AttributeValueType.MAGIC, AttributeDefinition.AttributeSection.MODIFIER);

        MOVEMENT_SPEED = AttributeController.registerAttribute(MOVEMENT_SPEED_KEY, "Movement Speed", 'a', AttributeValueType.PERCENT, AttributeDefinition.AttributeSection.MODIFIER);
        MOVEMENT_SPEED.setTranslationKey("attribute.name.generic.movementSpeed");

        KNOCKBACK_RES = AttributeController.registerAttribute(KNOCKBACK_RES_KEY, "Knockback Resistance", 'b', AttributeValueType.PERCENT, AttributeDefinition.AttributeSection.MODIFIER);
        KNOCKBACK_RES.setTranslationKey("attribute.name.generic.knockbackResistance");

        // TODO: Attributes
        // Durability Attribute
        // Unique Name Attribute

        // Requirement
        RequirementCheckerRegistry.registerChecker(new QuestCompletedRequirement());
        RequirementCheckerRegistry.registerChecker(new QuestActiveRequirement());

        // TODO: Requirements
        // Player Requirement : Soulbound UUID
        // Profile Requirement : Slot Number
    }
}
