package kamkeel.npcs.controllers.data.attribute.tracker;

import net.minecraft.entity.player.EntityPlayer;

/**
 * Implement this interface to receive a callback after a player's attributes have been recalculated.
 */
public interface IAttributeRecalcListener {
    void onAttributesRecalculated(EntityPlayer player, PlayerAttributeTracker tracker);
}
