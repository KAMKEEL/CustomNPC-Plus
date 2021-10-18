//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package noppes.npcs.scripted.event;

import cpw.mods.fml.common.eventhandler.Cancelable;
import cpw.mods.fml.common.eventhandler.Event;
import noppes.npcs.scripted.interfaces.IWorld;
import noppes.npcs.scripted.interfaces.IEntity;

@Cancelable
public class ForgeEvent extends CustomNPCsEvent {
    public final Event event;

    public ForgeEvent(Event event) {
        this.event = event;
    }

    @Cancelable
    public static class WorldEvent extends ForgeEvent {
        public final IWorld world;

        public WorldEvent(net.minecraftforge.event.world.WorldEvent event, IWorld world) {
            super(event);
            this.world = world;
        }
    }

    @Cancelable
    public static class EntityEvent extends ForgeEvent {
        public final IEntity entity;

        public EntityEvent(net.minecraftforge.event.entity.EntityEvent event, IEntity entity) {
            super(event);
            this.entity = entity;
        }
    }

    public static class InitEvent extends ForgeEvent {
        public InitEvent() {
            super((Event)null);
        }
    }
}
