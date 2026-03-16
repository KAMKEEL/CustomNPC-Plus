package kamkeel.npcs.controllers.data.energy;


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
 * Extension point for handling energy IEntity damage without a sourceAbility.
 * Registered with EnergyController. DBC Addon implements this to route
 * script-created energy IEntity damage through the DBC damage pipeline.
 */
public interface IEnergyExtender {

    /**
     * Handle damage for an energy IEntity with custom damage data.
     * Chain of Responsibility — first handler returning true claims the damage.
     *
     * @param energyIEntity    The energy IEntity dealing damage
     * @param owner           The IEntity that owns/created the energy IEntity
     * @param target          The IEntity being damaged
     * @param damage          Base damage amount
     * @param knockback       Horizontal knockback strength
     * @param knockbackUp     Vertical knockback strength
     * @param kbDirX          Knockback direction X
     * @param kbDirZ          Knockback direction Z
     * @param damageMultiplier Damage multiplier (e.g. falloff)
     * @param damageData      Custom damage IConfiguration NBT (e.g. DBC stats)
     * @return true if this handler processed the damage, false to pass to next handler
     */
    boolean onEnergyDamage(IEntity energyIEntity, IEntityLivingBase owner,
                           IEntityLivingBase target, float damage,
                           float knockback, float knockbackUp,
                           double kbDirX, double kbDirZ,
                           float damageMultiplier,
                           INbt damageData);

    /**
     * Modify outgoing damage for an energy IEntity with custom damage data.
     * Cumulative — each handler receives the previous handler's output.
     *
     * @param energyIEntity The energy IEntity dealing damage
     * @param owner        The IEntity that owns/created the energy IEntity
     * @param baseDamage   Current damage value (after previous handlers)
     * @param damageData   Custom damage IConfiguration NBT
     * @return Modified damage value
     */
    float modifyEnergyDamage(IEntity energyIEntity, IEntityLivingBase owner,
                              float baseDamage, INbt damageData);
}
