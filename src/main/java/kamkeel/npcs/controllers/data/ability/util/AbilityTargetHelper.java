package kamkeel.npcs.controllers.data.ability.util;

import kamkeel.npcs.controllers.data.ability.enums.TargetFilter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import noppes.npcs.controllers.PartyController;
import noppes.npcs.controllers.data.Party;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.entity.EntityNPCInterface;

/**
 * Utility class for determining friend/foe relationships between entities.
 * Extracted from EntityEnergyProjectile.shouldIgnoreEntity() for reuse
 * across all AoE abilities and AbilityEffect targeting.
 */
public class AbilityTargetHelper {

    /**
     * Determines if target is an ally of caster.
     * <ul>
     *   <li>NPC vs NPC: same faction ID = ally</li>
     *   <li>NPC vs Player: faction friendly to player = ally</li>
     *   <li>Player vs NPC: NPC's faction friendly to player = ally</li>
     *   <li>Player vs Player: same party (if exists) = ally</li>
     *   <li>Passive NPCs are always considered allies (never enemies)</li>
     * </ul>
     */
    public static boolean isAlly(EntityLivingBase caster, Entity target) {
        if (target == caster) return true;

        // NPC target checks
        if (target instanceof EntityNPCInterface) {
            EntityNPCInterface targetNpc = (EntityNPCInterface) target;

            // Passive NPCs are always considered allies
            if (targetNpc.faction.isPassive) return true;

            // NPC caster: same faction = ally
            if (caster instanceof EntityNPCInterface) {
                EntityNPCInterface casterNpc = (EntityNPCInterface) caster;
                if (casterNpc.faction.id == targetNpc.faction.id) return true;
            }

            // Player caster: friendly faction = ally
            if (caster instanceof EntityPlayer) {
                if (targetNpc.faction.isFriendlyToPlayer((EntityPlayer) caster)) return true;
            }

            return false;
        }

        // Player target checks
        if (target instanceof EntityPlayer && caster instanceof EntityNPCInterface) {
            // NPC caster vs Player target: faction friendly to player = ally
            EntityNPCInterface casterNpc = (EntityNPCInterface) caster;
            return casterNpc.faction.isFriendlyToPlayer((EntityPlayer) target);
        }

        if (target instanceof EntityPlayer && caster instanceof EntityPlayer) {
            // Player vs Player: same party = ally
            EntityPlayer casterPlayer = (EntityPlayer) caster;
            EntityPlayer targetPlayer = (EntityPlayer) target;
            PlayerData casterData = PlayerData.get(casterPlayer);
            PlayerData targetData = PlayerData.get(targetPlayer);
            if (casterData.partyUUID != null && casterData.partyUUID.equals(targetData.partyUUID)) {
                Party party = PartyController.Instance().getParty(casterData.partyUUID);
                if (party != null && !party.friendlyFire()) return true;
            }
            return false;
        }

        // Unknown entity types: not an ally
        return false;
    }

    /**
     * Determines if a target should be affected by an ability, given the
     * filter mode and includeSelf setting.
     *
     * @param caster      The entity using the ability
     * @param target      The potential target entity
     * @param filter      ALLIES, ENEMIES, or ALL
     * @param includeSelf Whether to include the caster themselves
     * @return true if the target should be affected
     */
    public static boolean shouldAffect(EntityLivingBase caster, Entity target,
                                       TargetFilter filter, boolean includeSelf) {
        if (target == caster) return includeSelf;
        if (!(target instanceof EntityLivingBase)) return false;
        if (!target.isEntityAlive()) return false;

        switch (filter) {
            case ALLIES:
                return isAlly(caster, target);
            case ENEMIES:
                return !isAlly(caster, target);
            case ALL:
                return true;
            default:
                return false;
        }
    }
}
