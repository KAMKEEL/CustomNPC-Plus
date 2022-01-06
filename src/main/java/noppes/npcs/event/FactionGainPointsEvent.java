package noppes.npcs.event;

import cpw.mods.fml.common.eventhandler.Cancelable;
import cpw.mods.fml.common.eventhandler.Event;
import net.minecraft.entity.Entity;
import noppes.npcs.controllers.FactionOptions;

@Cancelable
public class FactionGainPointsEvent extends Event {

    private FactionOptions faction;
    private Entity source;

    public FactionGainPointsEvent(FactionOptions faction, Entity source) {
        this.faction = faction;
        this.source = source;
    }

    public FactionOptions getFaction() {
        return faction;
    }

    public Entity getSource() {
        return source;
    }
}
