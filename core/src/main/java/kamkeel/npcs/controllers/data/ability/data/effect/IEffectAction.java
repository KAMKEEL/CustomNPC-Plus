package kamkeel.npcs.controllers.data.ability.data.effect;


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
 * Interface for mod-registered effect actions that can be applied by AbilityEffect.
 * <p>
 * Mods register implementations via {@code Register.EffectActions} or
 * {@code AbilityController.Instance.registerEffectAction()}.
 * Each action has a unique ID, display name, and apply logic.
 * Actions can optionally provide custom IConfiguration fields for the GUI.
 */
public interface IEffectAction {

    /**
     * Unique identifier, e.g. "dbc:kiRestore", "mymod:buffSpeed".
     */
    String getId();

    /**
     * Localized display name shown in the GUI selector.
     */
    String getDisplayName();

    /**
     * Apply this action to a target IEntity.
     * Called once per affected IEntity during ability execution.
     *
     * @param caster The IEntity that used the ability
     * @param target The IEntity being affected
     * @param config Action-specific IConfiguration NBT (from GUI)
     */
    void apply(IEntityLivingBase caster, IEntityLivingBase target, INbt config);

    /**
     * Create default IConfiguration NBT for a new instance of this action.
     */
    default INbt createDefaultConfig() {
        return new INbt();
    }
}
