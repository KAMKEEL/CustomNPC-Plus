package kamkeel.npcs.util;

import kamkeel.npcs.CustomAttributes;
import kamkeel.npcs.controllers.data.attribute.tracker.PlayerAttributeTracker;
import net.minecraft.entity.player.EntityPlayer;
import noppes.npcs.config.ConfigMain;
import noppes.npcs.controllers.MagicController;
import noppes.npcs.controllers.data.Magic;
import noppes.npcs.controllers.data.MagicEntry;
import noppes.npcs.controllers.data.MagicData;
import noppes.npcs.controllers.data.PlayerData;
import net.minecraft.entity.EntityLivingBase;
import noppes.npcs.entity.EntityNPCInterface;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import static kamkeel.npcs.controllers.AttributeController.getTracker;

public class AttributeAttackUtil {
    private static final Random random = new Random();

    /**
     * Crit decision from the last LivingAttackEvent for vanilla/modded mob targets.
     * Set in LivingAttackEvent, consumed in LivingHurtEvent.
     * null = no roll occurred, true = critted, false = did not crit.
     */
    public static Boolean lastAttackCritted = null;
    // --- Helper Classes & Methods ---

    // Container for allocation results.
    public static class AllocationResult {
        public Map<Integer, Float> allocation;
        public float leftover;

        public AllocationResult(Map<Integer, Float> allocation, float leftover) {
            this.allocation = allocation;
            this.leftover = leftover;
        }
    }

    /**
     * Allocates physical damage into magic allocations based on MagicData splits.
     * Returns both a mapping (magic ID -> allocated damage) and any leftover physical damage.
     */
    public static AllocationResult allocateMagicDamage(float physicalDamage, Map<Integer, MagicEntry> magicData) {
        Map<Integer, Float> allocation = new HashMap<>();
        float totalSplit = 0f;
        if (magicData != null) {
            for (Map.Entry<Integer, MagicEntry> entry : magicData.entrySet()) {
                int magicId = entry.getKey();
                float splitVal = entry.getValue().split;
                float allocated = (physicalDamage * splitVal) + entry.getValue().damage;
                allocation.put(magicId, allocated);
                totalSplit += splitVal;
            }
        }
        // Ensure double arithmetic for subtraction, then cast back to float.
        float leftover = physicalDamage * (float) Math.max(0d, 1d - totalSplit);
        return new AllocationResult(allocation, leftover);
    }

    /**
     * Adds extra magic damage from attribute sources if attributes are enabled.
     */
    public static void addAttributeMagicDamage(Map<Integer, Float> allocation,
                                                Map<Integer, Float> attributeDamage,
                                                Map<Integer, Float> magicBoost) {
        if (!ConfigMain.AttributesEnabled) return;
        for (Map.Entry<Integer, Float> entry : attributeDamage.entrySet()) {
            int magicId = entry.getKey();
            float damage = entry.getValue();
            float boost = magicBoost.getOrDefault(magicId, 0f);
            damage *= (1 + (boost / 100f));
            allocation.put(magicId, allocation.getOrDefault(magicId, 0f) + damage);
        }
    }

    /**
     * Applies magic interactions. For each magic in the allocation, multiplies its damage by (1 + bonus)
     * for each opposing magic the defender possesses.
     */
    public static void applyMagicInteractions(Map<Integer, Float> allocation,
                                               Set<Integer> defenderMagicIDs,
                                               MagicController magicController) {
        for (Map.Entry<Integer, Float> entry : allocation.entrySet()) {
            int magicId = entry.getKey();
            float magicDamage = entry.getValue();
            Magic magic = magicController.getMagic(magicId);
            if (magic != null && magic.interactions != null) {
                float multiplier = 1f;
                for (Map.Entry<Integer, Float> inter : magic.interactions.entrySet()) {
                    if (defenderMagicIDs.contains(inter.getKey())) {
                        multiplier *= (1 + inter.getValue());
                    }
                }
                entry.setValue(magicDamage * multiplier);
            }
        }
    }

    /**
     * Applies defender magic defenses if attributes are enabled.
     * Subtracts tracker-based magic defense (scaled by resistance) from each magic allocation.
     */
    public static float applyDefenderMagicDefense(Map<Integer, Float> allocation,
                                                   PlayerAttributeTracker defender,
                                                   MagicController magicController) {
        if (!ConfigMain.AttributesEnabled) return 0f;
        float adjusted = 0f;
        for (Map.Entry<Integer, Float> entry : allocation.entrySet()) {
            int magicId = entry.getKey();
            if (magicController.getMagic(magicId) == null) continue;
            float damage = entry.getValue();
            float defense = defender.magicDefense.getOrDefault(magicId, 0f);
            float resistance = defender.magicResistance.getOrDefault(magicId, 0f);
            float totalDefense = defense * (1 + (resistance / 100));
            damage -= totalDefense;
            adjusted += Math.max(0, damage);
        }
        return adjusted;
    }

    // --- Damage Calculation Functions ---

    /**
     * Calculates damage when a player attacks another player.
     */
    public static float calculateDamagePlayerToPlayer(EntityPlayer attackPlayer, EntityPlayer defendPlayer, float baseDamage) {
        PlayerData attackerData = PlayerData.get(attackPlayer);
        PlayerData defenderData = PlayerData.get(defendPlayer);
        PlayerAttributeTracker attacker = getTracker(attackPlayer);
        PlayerAttributeTracker defender = getTracker(defendPlayer);

        // Calculate physical damage.
        float physicalDamage = applyMainAttack(baseDamage, attacker);
        AllocationResult result = allocateMagicDamage(physicalDamage, attackerData.magicData.getMagics());
        float leftover = applyNeutral(result.leftover, attacker);
        addAttributeMagicDamage(result.allocation, attacker.magicDamage, attacker.magicBoost);

        MagicController magicController = MagicController.getInstance();
        Set<Integer> defenderMagicIDs = new HashSet<>(defenderData.magicData.getMagics().keySet());
        applyMagicInteractions(result.allocation, defenderMagicIDs, magicController);
        float adjustedMagic = applyDefenderMagicDefense(result.allocation, defender, magicController);

        return applyCrit(leftover + adjustedMagic, attacker);
    }

    /**
     * Calculates damage when a player attacks an NPC.
     */
    public static float calculateDamagePlayerToNPC(EntityPlayer attackPlayer, EntityNPCInterface npc, float baseDamage) {
        PlayerData attackerData = PlayerData.get(attackPlayer);
        PlayerAttributeTracker attacker = getTracker(attackPlayer);

        float physicalDamage = applyMainAttack(baseDamage, attacker);
        AllocationResult result = allocateMagicDamage(physicalDamage, attackerData.magicData.getMagics());
        float leftover = applyNeutral(result.leftover, attacker);

        addAttributeMagicDamage(result.allocation, attacker.magicDamage, attacker.magicBoost);

        MagicController magicController = MagicController.getInstance();
        Set<Integer> npcMagicIDs = new HashSet<>();
        if (npc.stats != null && npc.stats.magicData != null)
            npcMagicIDs = new HashSet<>(npc.stats.magicData.getMagics().keySet());
        applyMagicInteractions(result.allocation, npcMagicIDs, magicController);

        float adjustedMagic = 0f;
        for (float val : result.allocation.values())
            adjustedMagic += val;

        return applyCrit(leftover + adjustedMagic, attacker);
    }

    public static float applyCrit(float damage, PlayerAttributeTracker tracker) {
        if (ConfigMain.AttributesEnabled) {
            if (rollCrit(tracker))
                damage = applyCritDamage(damage, tracker);
        }
        return damage;
    }

    /**
     * Rolls for a critical hit based on the tracker's CRITICAL_CHANCE attribute.
     */
    public static boolean rollCrit(PlayerAttributeTracker tracker) {
        if (ConfigMain.AttributesEnabled && tracker != null) {
            float critChance = tracker.getAttributeValue(CustomAttributes.CRITICAL_CHANCE);
            return random.nextFloat() < (critChance / 100f);
        }
        return false;
    }

    /**
     * Applies crit damage unconditionally (no chance roll).
     * Use when the crit decision was already made via {@link #rollCrit}.
     */
    public static float applyCritDamage(float damage, PlayerAttributeTracker tracker) {
        if (ConfigMain.AttributesEnabled && tracker != null) {
            float critBonus = tracker.getAttributeValue(CustomAttributes.CRITICAL_DAMAGE);
            damage = (damage * (1 + (float) ConfigMain.AttributesCriticalBoost / 100f)) + critBonus;
        }
        return damage;
    }

    public static float applyMainAttack(float damage, PlayerAttributeTracker tracker) {
        if (ConfigMain.AttributesEnabled) {
            float mainAttack = tracker.getAttributeValue(CustomAttributes.MAIN_ATTACK);
            float mainBoost = tracker.getAttributeValue(CustomAttributes.MAIN_BOOST) / 100f;
            damage = (damage * (1 + mainBoost)) + mainAttack;
        }
        return damage;
    }

    public static float applyNeutral(float leftover, PlayerAttributeTracker tracker) {
        if (ConfigMain.AttributesEnabled) {
            float neutralDamage = tracker.getAttributeValue(CustomAttributes.NEUTRAL_ATTACK);
            float neutralBoost = tracker.getAttributeValue(CustomAttributes.NEUTRAL_BOOST);
            leftover += neutralDamage * (1 + (neutralBoost / 100f));
        }
        return leftover;
    }

    /**
     * Calculates damage when an NPC attacks a player.
     */
    public static float calculateDamageNPCtoPlayer(EntityNPCInterface npc, EntityPlayer defendingPlayer, float baseDamage) {
        PlayerData defenderData = PlayerData.get(defendingPlayer);
        PlayerAttributeTracker defender = getTracker(defendingPlayer);

        if (npc.stats == null || npc.stats.magicData == null)
            return baseDamage;

        AllocationResult result = allocateMagicDamage(baseDamage, npc.stats.magicData.getMagics());
        float leftover = result.leftover;

        MagicController magicController = MagicController.getInstance();
        Set<Integer> defenderMagicIDs = new HashSet<>(defenderData.magicData.getMagics().keySet());
        applyMagicInteractions(result.allocation, defenderMagicIDs, magicController);
        float adjustedMagic = applyDefenderMagicDefense(result.allocation, defender, magicController);
        return leftover + adjustedMagic;
    }

    /**
     * Calculates a player's maximum output ignoring defensive modifiers.
     */
    public static float calculateGearOutput(PlayerAttributeTracker attacker) {
        float neutralDamage = attacker.getAttributeValue(CustomAttributes.NEUTRAL_ATTACK);
        float neutralBoost = 1 + (attacker.getAttributeValue(CustomAttributes.NEUTRAL_BOOST) / 100f);
        float neutralTotal = neutralDamage * neutralBoost;

        float magicTotal = 0f;
        MagicController magicController = MagicController.getInstance();
        for (Map.Entry<Integer, Float> entry : attacker.magicDamage.entrySet()) {
            int magicId = entry.getKey();
            if (magicController.getMagic(magicId) != null) {
                float attackMagic = entry.getValue();
                float boostMagic = attacker.magicBoost.getOrDefault(magicId, 0f);
                magicTotal += Math.max(0, attackMagic * ((boostMagic / 100f) + 1f));
            }
        }
        return neutralTotal + magicTotal;
    }

    /**
     * Calculates a player's outgoing damage based on weapon damage + main-attack attributes,
     * plus all neutral/magic damage from gear.
     */
    public static float calculateOutgoing(EntityPlayer player, float baseDamage) {
        if (!ConfigMain.AttributesEnabled) {
            return baseDamage;
        }

        PlayerAttributeTracker tracker = getTracker(player);

        // 1) Main-attack component
        float mainAttackFlat = tracker.getAttributeValue(CustomAttributes.MAIN_ATTACK);
        float mainBoostPercent = tracker.getAttributeValue(CustomAttributes.MAIN_BOOST) / 100f;
        float mainDamage = (baseDamage * (1 + mainBoostPercent)) + mainAttackFlat;

        // 2) Gear component (neutral + magic) – gearOutput must itself divide its boosts by 100 internally
        float gearDamage = tracker.gearOutput;

        return mainDamage + gearDamage;
    }

    // --- Magic Interaction Utility ---

    /**
     * Calculate magic interaction multiplier between attacker's magic types
     * and defender's magic types. Used for barrier defense and entity-vs-entity interactions.
     *
     * @param attackerMagic Attacker's magic data (projectile or weapon)
     * @param defenderMagic Defender's magic data (barrier or entity)
     * @return Multiplier (1.0 = no interaction, >1.0 = bonus damage, <1.0 = resistance)
     */
    public static float calculateMagicInteractionMultiplier(MagicData attackerMagic, MagicData defenderMagic) {
        if (attackerMagic == null || defenderMagic == null) return 1.0f;
        if (attackerMagic.isEmpty() || defenderMagic.isEmpty()) return 1.0f;

        MagicController mc = MagicController.getInstance();
        float multiplier = 1.0f;

        for (int attackMagicId : attackerMagic.getMagics().keySet()) {
            Magic magic = mc.getMagic(attackMagicId);
            if (magic == null || magic.interactions == null) continue;
            for (Map.Entry<Integer, Float> interaction : magic.interactions.entrySet()) {
                if (defenderMagic.getMagics().containsKey(interaction.getKey())) {
                    multiplier *= (1 + interaction.getValue());
                }
            }
        }
        return multiplier;
    }

    // --- Ability Damage Pipeline ---

    /**
     * Apply gear magic boost percentages to the split-allocated magic damage.
     * This multiplies each magic type's allocated damage by (1 + boost/100).
     * Unlike {@link #addAttributeMagicDamage}, this does NOT add flat gear damage —
     * it only applies the percentage multiplier from gear to the existing allocation.
     */
    public static void applyMagicBoostToAllocation(Map<Integer, Float> allocation, Map<Integer, Float> magicBoost) {
        if (!ConfigMain.AttributesEnabled) return;
        for (Map.Entry<Integer, Float> entry : allocation.entrySet()) {
            float boost = magicBoost.getOrDefault(entry.getKey(), 0f);
            if (boost != 0f) {
                entry.setValue(entry.getValue() * (1 + boost / 100f));
            }
        }
    }

    /**
     * Apply gear magic boost percentages to barrier/dome health.
     * Each magic type's split determines what portion of health is affected by its boost.
     * E.g., Thunder [100%] dome + 50% Thunder gear boost = health * 1.5
     */
    public static float applyMagicBoostToHealth(EntityPlayer caster, MagicData magicData, float health) {
        if (!ConfigMain.AttributesEnabled) return health;
        if (magicData == null || magicData.isEmpty()) return health;

        PlayerAttributeTracker tracker = getTracker(caster);
        if (tracker == null) return health;

        float multiplier = 1.0f;
        for (Map.Entry<Integer, MagicEntry> entry : magicData.getMagics().entrySet()) {
            float split = entry.getValue().split;
            float boost = tracker.magicBoost.getOrDefault(entry.getKey(), 0f);
            multiplier += split * (boost / 100f);
        }
        return health * multiplier;
    }

    /**
     * Apply the magic pipeline to ability damage. Works for both player and NPC casters.
     * Abilities do NOT get main attack or critical boosts — they have their own scaling.
     * This handles:
     * <ul>
     *   <li>Magic allocation (splits from ability/caster magic data)</li>
     *   <li>Gear magic boost multipliers on split damage (player casters only, no flat gear damage)</li>
     *   <li>Magic interactions vs defender</li>
     *   <li>Defender magic defense/resistance (player defenders only)</li>
     * </ul>
     * If no magic data is present, damage passes through unchanged.
     *
     * @param caster       Caster (player or NPC)
     * @param target       Target entity
     * @param damage       Base ability damage (already scaled by ability's own system)
     * @param abilityMagic Resolved magic data (ability's own, or caster's fallback)
     * @return Final damage after magic splits, boosts, interactions, and defense
     */
    public static float calculateAbilityDamage(EntityLivingBase caster, EntityLivingBase target,
                                                float damage, MagicData abilityMagic) {
        if (abilityMagic == null || abilityMagic.isEmpty()) return damage;

        // 1. Allocate base damage into magic types using ability's splits
        AllocationResult result = allocateMagicDamage(damage, abilityMagic.getMagics());
        float leftover = result.leftover;

        // 2. Apply gear magic boost multipliers to the split allocation (player casters only)
        if (caster instanceof EntityPlayer && ConfigMain.AttributesEnabled) {
            PlayerAttributeTracker tracker = getTracker((EntityPlayer) caster);
            if (tracker != null) {
                applyMagicBoostToAllocation(result.allocation, tracker.magicBoost);
            }
        }

        // 3. Magic interactions against target
        MagicController magicController = MagicController.getInstance();
        Set<Integer> defenderMagicIDs = null;

        if (target instanceof EntityPlayer) {
            PlayerData defenderData = PlayerData.get((EntityPlayer) target);
            if (defenderData != null) {
                defenderMagicIDs = new HashSet<>(defenderData.magicData.getMagics().keySet());
            }
        } else if (target instanceof EntityNPCInterface) {
            EntityNPCInterface npc = (EntityNPCInterface) target;
            if (npc.stats != null && npc.stats.magicData != null) {
                defenderMagicIDs = new HashSet<>(npc.stats.magicData.getMagics().keySet());
            }
        }

        if (defenderMagicIDs != null) {
            applyMagicInteractions(result.allocation, defenderMagicIDs, magicController);
        }

        // 4. Apply defender magic defense (player defenders with attributes only)
        if (target instanceof EntityPlayer && ConfigMain.AttributesEnabled) {
            PlayerAttributeTracker defenderTracker = getTracker((EntityPlayer) target);
            if (defenderTracker != null) {
                float adjustedMagic = applyDefenderMagicDefense(result.allocation, defenderTracker, magicController);
                return leftover + adjustedMagic;
            }
        }

        // Sum magic damage
        float magicTotal = 0f;
        for (float val : result.allocation.values()) magicTotal += val;
        return leftover + magicTotal;
    }
}
