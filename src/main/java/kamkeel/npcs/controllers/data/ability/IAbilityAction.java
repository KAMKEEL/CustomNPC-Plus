package kamkeel.npcs.controllers.data.ability;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;

import java.util.List;

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

    List<Condition> getConditions();

    boolean checkConditions(EntityLivingBase caster, EntityLivingBase target);

    boolean checkConditionsForPlayer(EntityLivingBase caster);

    /**
     * Whether this action is a chained ability (sequence of abilities) vs an individual ability.
     */
    boolean isChain();

    /**
     * Create a deep copy of this action.
     */
    IAbilityAction deepCopyAction();

    NBTTagCompound writeNBT();
}
