package noppes.npcs.scripted.event;

import noppes.npcs.api.entity.ICustomNpc;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.scripted.event.player.PlayerEvent;
import noppes.npcs.scripted.event.NpcEvent;

/**
 * Event fired by EventAction when its condition is satisfied.
 * The hook name for scripts is provided by {@link #getHookName()}.
 */
public class ActionTriggerEvent extends CustomNPCsEvent {
    private final String id;

    public ActionTriggerEvent(String id) {
        this.id = id;
    }

    @Override
    public String getHookName() {
        return id;
    }

    public String getId() {
        return id;
    }

    /** Event fired for players */
    public static class PlayerAction extends PlayerEvent {
        private final String id;
        public PlayerAction(IPlayer player, String id) {
            super(player);
            this.id = id;
        }
        @Override
        public String getHookName() {
            return id;
        }
        public String getId() {
            return id;
        }
    }

    /** Event fired for NPCs */
    public static class NpcAction extends NpcEvent {
        private final String id;
        public NpcAction(ICustomNpc npc, String id) {
            super(npc);
            this.id = id;
        }
        @Override
        public String getHookName() {
            return id;
        }
        public String getId() {
            return id;
        }
    }
}
