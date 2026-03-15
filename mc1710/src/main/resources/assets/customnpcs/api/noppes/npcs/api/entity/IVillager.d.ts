/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.entity
 */

/**
 * Represents a villager entity with additional trading-related methods.
 *
 * @param <T> The underlying Minecraft EntityVillager type.
  * @javaFqn noppes.npcs.api.entity.IVillager
*/
export interface IVillager<T extends EntityVillager /* net.minecraft.entity.passive.EntityVillager */> extends import('./IEntityLiving').IEntityLiving {
    /**
     * @return The profession of the villager.
     */
    getProfession(): import('./int').int;
    /**
     * @return true if the villager is currently trading.
     */
    getIsTrading(): import('./boolean').boolean;
    /**
     * Returns the customer (player) who is currently trading with the villager.
     *
     * @return the customer as an IEntityLivingBase.
     */
    getCustomer(): import('./IEntityLivingBase').IEntityLivingBase;
}
