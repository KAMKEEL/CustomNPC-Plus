package kamkeel.npcs.controllers.data.attribute.tracker;


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
 * Implement this interface to receive a callback after a player's attributes have been recalculated.
 */
public interface IAttributeRecalcListener {
    void onAttributesRecalculated(IPlayer player, PlayerAttributeTracker tracker);
}
