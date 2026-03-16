package kamkeel.npcs.controllers.data.attribute.tracker;

import noppes.npcs.api.entity.IEntityLiving;
import noppes.npcs.api.IDamageSource;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.INbtList;
import noppes.npcs.api.INbt;
import java.util.ArrayList;
import java.util.List;

public final class AttributeRecalcEvent {
    @FunctionalInterface
    public interface PreListener {
        void onPre(IPlayer player, PlayerAttributeTracker tracker);
    }

    private static final List<PreListener> preListeners = new ArrayList<>();
    private static final List<Listener> postListeners = new ArrayList<>();

    public static void pre(IPlayer player, PlayerAttributeTracker tracker) {
        for (PreListener l : preListeners) l.onPre(player, tracker);
    }

    @FunctionalInterface
    public interface Listener {
        void onPost(IPlayer player, PlayerAttributeTracker tracker);
    }

    public static void registerListener(Listener l) {
        postListeners.add(l);
    }

    public static void post(IPlayer player, PlayerAttributeTracker tracker) {
        for (Listener l : postListeners) l.onPost(player, tracker);
    }

    public static void registerPreListener(PreListener l) {
        preListeners.add(l);
    }
}
