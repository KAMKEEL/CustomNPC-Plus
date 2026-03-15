package noppes.npcs.api.entity;


/**
 * Represents a villager entity with additional trading-related methods.
 *
 */
public interface IVillager extends IEntityLiving {

    /**
     * @return The profession of the villager.
     */
    int getProfession();

    /**
     * @return true if the villager is currently trading.
     */
    boolean getIsTrading();

    /**
     * Returns the customer (player) who is currently trading with the villager.
     *
     * @return the customer as an IEntityLivingBase.
     */
    IEntityLivingBase getCustomer();
}
