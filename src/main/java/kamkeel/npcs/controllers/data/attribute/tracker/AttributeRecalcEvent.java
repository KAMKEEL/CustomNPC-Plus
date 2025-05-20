package kamkeel.npcs.controllers.data.attribute.tracker;

import net.minecraft.entity.player.EntityPlayer;

import java.util.ArrayList;
import java.util.List;

public final class AttributeRecalcEvent {
    @FunctionalInterface
    public interface PreListener {
        void onPre(EntityPlayer player, PlayerAttributeTracker tracker);
    }

    private static final List<PreListener> preListeners = new ArrayList<>();
    private static final List<Listener> postListeners = new ArrayList<>();

    public static void pre(EntityPlayer player, PlayerAttributeTracker tracker) {
        for (PreListener l : preListeners) l.onPre(player, tracker);
    }

    @FunctionalInterface
    public interface Listener {
        void onPost(EntityPlayer player, PlayerAttributeTracker tracker);
    }

    public static void registerListener(Listener l) {
        postListeners.add(l);
    }

    public static void post(EntityPlayer player, PlayerAttributeTracker tracker) {
        for (Listener l : postListeners) l.onPost(player, tracker);
    }

    public static void registerPreListener(PreListener l) {
        preListeners.add(l);
    }
}
