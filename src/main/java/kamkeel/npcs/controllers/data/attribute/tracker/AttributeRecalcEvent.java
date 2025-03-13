package kamkeel.npcs.controllers.data.attribute.tracker;

import net.minecraft.entity.player.EntityPlayer;

import java.util.ArrayList;
import java.util.List;

public class AttributeRecalcEvent {
    private static final List<IAttributeRecalcListener> listeners = new ArrayList<>();

    /**
     * Allows other mods to register a listener that is called after attribute recalculation.
     */
    public static void registerListener(IAttributeRecalcListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * Posts an event to all registered listeners.
     */
    public static void post(EntityPlayer player, PlayerAttributeTracker tracker) {
        for (IAttributeRecalcListener listener : listeners) {
            listener.onAttributesRecalculated(player, tracker);
        }
    }
}
