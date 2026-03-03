package kamkeel.npcs.controllers.data.energy;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Extension point for handling energy entity damage without a sourceAbility.
 * Registered with EnergyController. DBC Addon implements this to route
 * script-created energy entity damage through the DBC damage pipeline.
 */
public interface IEnergyHandler {

    /**
     * Handle damage for an energy entity with custom damage data.
     * Chain of Responsibility — first handler returning true claims the damage.
     *
     * @param energyEntity    The energy entity dealing damage
     * @param owner           The entity that owns/created the energy entity
     * @param target          The entity being damaged
     * @param damage          Base damage amount
     * @param knockback       Horizontal knockback strength
     * @param knockbackUp     Vertical knockback strength
     * @param kbDirX          Knockback direction X
     * @param kbDirZ          Knockback direction Z
     * @param damageMultiplier Damage multiplier (e.g. falloff)
     * @param damageData      Custom damage configuration NBT (e.g. DBC stats)
     * @return true if this handler processed the damage, false to pass to next handler
     */
    boolean onEnergyDamage(Entity energyEntity, EntityLivingBase owner,
                           EntityLivingBase target, float damage,
                           float knockback, float knockbackUp,
                           double kbDirX, double kbDirZ,
                           float damageMultiplier,
                           NBTTagCompound damageData);

    /**
     * Modify outgoing damage for an energy entity with custom damage data.
     * Cumulative — each handler receives the previous handler's output.
     *
     * @param energyEntity The energy entity dealing damage
     * @param owner        The entity that owns/created the energy entity
     * @param baseDamage   Current damage value (after previous handlers)
     * @param damageData   Custom damage configuration NBT
     * @return Modified damage value
     */
    float modifyEnergyDamage(Entity energyEntity, EntityLivingBase owner,
                              float baseDamage, NBTTagCompound damageData);
}
