package kamkeel.npcs.controllers.data.ability.util;

import noppes.npcs.constants.ClientOnly;
import kamkeel.npcs.util.IVector3;
import kamkeel.npcs.controllers.data.ability.preview.PreviewEntityHandler;
import kamkeel.npcs.controllers.data.ability.gui.SubGuiAbilityConfig;
import kamkeel.npcs.controllers.data.ability.gui.IAbilityConfigCallback;
import kamkeel.npcs.controllers.data.ability.gui.FieldDef;
import kamkeel.npcs.controllers.data.ability.gui.IChainedAbilityFieldProvider;
import kamkeel.npcs.controllers.data.ability.gui.IAbilityFieldProvider;
import noppes.npcs.entity.EntityNPCInterface;
import kamkeel.npcs.entity.EntityEnergyDome;
import kamkeel.npcs.entity.EntityEnergyBarrier;
import kamkeel.npcs.entity.EntityEnergyPanel;
import kamkeel.npcs.entity.EntityAbilityOrb;
import kamkeel.npcs.entity.EntityAbilityLaser;
import kamkeel.npcs.entity.EntityAbilityDisc;
import kamkeel.npcs.entity.EntityAbilityBeam;
import kamkeel.npcs.entity.EntityEnergyProjectile;
import kamkeel.npcs.util.ByteBufUtils;
import noppes.npcs.api.entity.IEntityLiving;
import noppes.npcs.api.IDamageSource;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.INbtList;
import noppes.npcs.api.INbt;
import kamkeel.npcs.controllers.data.ability.enums.TargetFilter;
import noppes.npcs.controllers.PartyController;
import noppes.npcs.controllers.data.Party;
import noppes.npcs.controllers.data.PlayerData;
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
    public static boolean isAlly(IEntityLivingBase caster, IEntity target) {
        if (caster == null || target == null) return false;
        if (target == caster) return true;
        // Ally resolution relies on server-owned player/party/faction state.
        if (caster.worldObj == null || caster.worldObj.isRemote) return false;

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
            if (caster instanceof IPlayer) {
                IPlayer casterPlayer = (IPlayer) caster;
                if (PlayerData.get(casterPlayer) == null) return false;
                if (targetNpc.faction.isFriendlyToPlayer(casterPlayer)) return true;
            }

            return false;
        }

        // Player target checks
        if (target instanceof IPlayer && caster instanceof EntityNPCInterface) {
            // NPC caster vs Player target: faction friendly to player = ally
            EntityNPCInterface casterNpc = (EntityNPCInterface) caster;
            IPlayer targetPlayer = (IPlayer) target;
            if (PlayerData.get(targetPlayer) == null) return false;
            return casterNpc.faction.isFriendlyToPlayer(targetPlayer);
        }

        if (target instanceof IPlayer && caster instanceof IPlayer) {
            // Player vs Player: same party = ally
            IPlayer casterPlayer = (IPlayer) caster;
            IPlayer targetPlayer = (IPlayer) target;
            PlayerData casterData = PlayerData.get(casterPlayer);
            PlayerData targetData = PlayerData.get(targetPlayer);
            if (casterData == null || targetData == null) return false;
            if (casterData.partyUUID != null && casterData.partyUUID.equals(targetData.partyUUID)) {
                Party party = PartyController.Instance().getParty(casterData.partyUUID);
                if (party != null && !party.friendlyFire()) return true;
            }
            return false;
        }

        // Unknown IEntity types: not an ally
        return false;
    }

    /**
     * Determines if a target should be affected by an ability, given the
     * filter mode and includeSelf setting.
     *
     * @param caster      The IEntity using the ability
     * @param target      The potential target IEntity
     * @param filter      ALLIES, ENEMIES, or ALL
     * @param includeSelf Whether to include the caster themselves
     * @return true if the target should be affected
     */
    public static boolean shouldAffect(IEntityLivingBase caster, IEntity target,
                                       TargetFilter filter, boolean includeSelf) {
        if (caster == null || target == null || filter == null) return false;
        if (caster.worldObj == null || caster.worldObj.isRemote) return false;
        if (target == caster) return includeSelf;
        if (!(target instanceof IEntityLivingBase)) return false;
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
