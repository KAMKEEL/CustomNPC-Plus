package noppes.npcs.scripted.event;

import noppes.npcs.api.handler.data.IFaction;
import noppes.npcs.api.entity.IPlayer;

public class FactionEvent extends CustomNPCsEvent {
    public final IFaction faction;
    public final IPlayer player;

    public FactionEvent(IPlayer player, IFaction faction){
        this.faction = faction;
        this.player = player;
    }

    public static class FactionPoints extends FactionEvent {
        public boolean decrease;
        public int points;

        public FactionPoints(IPlayer player, IFaction faction, boolean decrease, int points) {
            super(player, faction);
            this.decrease = decrease;
            this.points = points;
        }
    }
}
