package kamkeel.npcs.controllers.data.ability;

import net.minecraft.nbt.NBTTagCompound;

/**
 * Abstract base class for built-in (non-customizable) abilities.
 * Built-in abilities are registered by key, have fixed configurations,
 * and cannot be modified through the preset GUI system.
 * <p>
 * Examples: simple utility abilities like Jump, Sprint, etc.
 * Third-party mods can extend this class to create their own built-in abilities.
 */
public abstract class BuiltInAbility extends Ability {

    /**
     * The registry key used to look up this ability (e.g., "cnpc:jump", "dbc:ki_blast").
     */
    private final String registryKey;

    public BuiltInAbility(String registryKey) {
        this.registryKey = registryKey;
        this.id = registryKey;
        this.name = registryKey;
        this.typeId = registryKey;
    }

    /**
     * Get the registry key for this built-in ability.
     */
    public String getRegistryKey() {
        return registryKey;
    }

    /**
     * Built-in abilities do not write type-specific NBT (they have fixed configs).
     */
    @Override
    public void writeTypeNBT(NBTTagCompound nbt) {
        // No-op: built-in abilities have fixed configurations
    }

    /**
     * Built-in abilities do not read type-specific NBT.
     */
    @Override
    public void readTypeNBT(NBTTagCompound nbt) {
        // No-op: built-in abilities have fixed configurations
    }

    /**
     * Get the duration of the active phase in ticks.
     * Built-in abilities must define their own active duration.
     */
    public abstract int getActiveDurationTicks();
}
