package kamkeel.npcs.controllers.data.ability.data;


import java.util.List;
import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.AbilityAction;
import kamkeel.npcs.controllers.data.ability.conditions.AbilityCondition;
import kamkeel.npcs.controllers.data.ability.enums.UserType;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IEntityLiving;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.IDamageSource;
import noppes.npcs.api.INbt;
import noppes.npcs.api.INbtList;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.IWorld;

/**
 * Common interface for all combat actions that can be assigned to NPCs or players.
 * Implemented by {@link Ability} (individual ability) and {@link ChainedAbility} (sequence).
 * <p>
 * This enables unified storage via {@link AbilityAction}, unified weighted random selection,
 * and unified eligibility checking without instanceof or separate codepaths.
 */
public interface IAbilityAction {

    String getName();

    boolean isEnabled();

    void setEnabled(boolean enabled);

    int getWeight();

    int getCooldownTicks();

    float getMinRange();

    float getMaxRange();

    UserType getAllowedBy();

    List<AbilityCondition> getConditions();

    boolean checkConditions(IEntityLivingBase caster, IEntityLivingBase target);

    boolean checkConditionsForPlayer(IEntityLivingBase caster);

    /**
     * Check if this action is available for the given player.
     * Used for visibility filtering (HUD, selection) and activation gating.
     * Returns true by default (no requirement).
     */
    default boolean isAvailableFor(IPlayer player) {
        return true;
    }

    /**
     * Whether this action is a chained ability (sequence of abilities) vs an individual ability.
     */
    boolean isChain();

    /**
     * Create a deep copy of this action.
     */
    IAbilityAction deepCopyAction();

    INbt writeNBT(boolean saveScripts);
}
