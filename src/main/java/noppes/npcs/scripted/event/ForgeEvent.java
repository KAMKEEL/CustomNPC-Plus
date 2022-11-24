//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package noppes.npcs.scripted.event;

import cpw.mods.fml.common.eventhandler.Cancelable;
import cpw.mods.fml.common.eventhandler.Event;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.event.IForgeEvent;
import noppes.npcs.constants.EnumScriptType;

@Cancelable
public class ForgeEvent extends CustomNPCsEvent implements IForgeEvent {
    public final Event event;

    public ForgeEvent(Event event) {
        this.event = event;
    }

    public Event getEvent() {
        return event;
    }

    public String getHookName() {
        return EnumScriptType.FORGE_EVENT.function;
    }

    @Cancelable
    public static class WorldEvent extends ForgeEvent implements IForgeEvent.WorldEvent {
        public final IWorld world;

        public WorldEvent(net.minecraftforge.event.world.WorldEvent event, IWorld world) {
            super(event);
            this.world = world;
        }

        public String getHookName() {
            return EnumScriptType.FORGE_WORLD.function;
        }

        public IWorld getWorld() {
            return world;
        }
    }

    @Cancelable
    public static class EntityEvent extends ForgeEvent implements IForgeEvent.EntityEvent {
        public final IEntity entity;

        public EntityEvent(net.minecraftforge.event.entity.EntityEvent event, IEntity entity) {
            super(event);
            this.entity = entity;
        }

        public String getHookName() {
            return EnumScriptType.FORGE_ENTITY.function;
        }

        public IEntity getEntity() {
            return entity;
        }
    }

    public static class InitEvent extends ForgeEvent implements IForgeEvent.InitEvent {
        public InitEvent() {
            super((Event)null);
        }

        public String getHookName() {
            return EnumScriptType.FORGE_INIT.function;
        }
    }
}
