package noppes.npcs.util;

import cpw.mods.fml.common.eventhandler.Event;
import net.minecraftforge.common.MinecraftForge;

public class EventBus {

    public static <T extends Event> T callTo(T event) {
        MinecraftForge.EVENT_BUS.post(event);

        return event;
    }

}
