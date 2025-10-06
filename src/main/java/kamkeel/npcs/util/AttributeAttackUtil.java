package kamkeel.npcs.util;

import kamkeel.npcs.CustomAttributes;
import kamkeel.npcs.controllers.data.attribute.tracker.PlayerAttributeTracker;
import net.minecraft.entity.player.EntityPlayer;
import noppes.npcs.config.ConfigMain;
import noppes.npcs.controllers.MagicController;
import noppes.npcs.controllers.data.Magic;
import noppes.npcs.controllers.data.MagicEntry;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.entity.EntityNPCInterface;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import static kamkeel.npcs.controllers.AttributeController.getTracker;

public class AttributeAttackUtil {
    private static final Random random = new Random();
    // --- Helper Classes & Methods ---

    // Container for allocation results.
    private static class AllocationResult {
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
    private static AllocationResult allocateMagicDamage(float physicalDamage, Map<Integer, MagicEntry> magicData) {
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
    private static void addAttributeMagicDamage(Map<Integer, Float> allocation,
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
    private static void applyMagicInteractions(Map<Integer, Float> allocation,
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
    private static float applyDefenderMagicDefense(Map<Integer, Float> allocation,
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
            float critChance = tracker.getAttributeValue(CustomAttributes.CRITICAL_CHANCE);
            float critBonus = tracker.getAttributeValue(CustomAttributes.CRITICAL_DAMAGE);
            if (random.nextFloat() < (critChance / 100f))
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
}
