package kamkeel.npcs.controllers.data.ability;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Interface for mod-registered effect actions that can be applied by AbilityEffect.
 * <p>
 * Mods register implementations via {@code Register.EffectActions} or
 * {@code AbilityController.Instance.registerEffectAction()}.
 * Each action has a unique ID, display name, and apply logic.
 * Actions can optionally provide custom configuration fields for the GUI.
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
     * Apply this action to a target entity.
     * Called once per affected entity during ability execution.
     *
     * @param caster The entity that used the ability
     * @param target The entity being affected
     * @param config Action-specific configuration NBT (from GUI)
     */
    void apply(EntityLivingBase caster, EntityLivingBase target, NBTTagCompound config);

    /**
     * Create default configuration NBT for a new instance of this action.
     */
    default NBTTagCompound createDefaultConfig() {
        return new NBTTagCompound();
    }
}
