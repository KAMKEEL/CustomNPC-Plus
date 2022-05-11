package noppes.npcs.scripted.event;

import cpw.mods.fml.common.eventhandler.Cancelable;
import noppes.npcs.scripted.entity.ScriptEntity;
import noppes.npcs.scripted.interfaces.IEntity;
import noppes.npcs.scripted.interfaces.IPlayer;
import noppes.npcs.scripted.item.ScriptCustomItem;

public class ItemEvent extends CustomNPCsEvent {
    public ScriptCustomItem item;

    public ItemEvent(ScriptCustomItem item) {
        this.item = item;
    }

    public static class InitEvent extends ItemEvent {
        public InitEvent(ScriptCustomItem item) {
            super(item);
        }
    }

    public static class UpdateEvent extends ItemEvent {
        public IEntity entity;

        public UpdateEvent(ScriptCustomItem item, IEntity entity) {
            super(item);
            this.entity = entity;
        }
    }

    @Cancelable
    public static class TossedEvent extends ItemEvent {
        public IEntity entity;
        public IPlayer player;

        public TossedEvent(ScriptCustomItem item, IPlayer player, IEntity entity) {
            super(item);
            this.entity = entity;
            this.player = player;
        }
    }

    public static class PickedUpEvent extends ItemEvent {
        public IPlayer player;

        public PickedUpEvent(ScriptCustomItem item, IPlayer player) {
            super(item);
            this.player = player;
        }
    }

    @Cancelable
    public static class SpawnEvent extends ItemEvent {
        public IEntity entity;
        public SpawnEvent(ScriptCustomItem item, IEntity entity){
            super(item);
            this.entity = entity;
        }
    }

    @Cancelable
    public static class InteractEvent extends ItemEvent {
        public final int type;
        public final Object target;
        public IPlayer player;

        public InteractEvent(ScriptCustomItem item, IPlayer player, int type, Object target) {
            super(item);
            this.type = type;
            this.target = target;
            this.player = player;
        }
    }

    @Cancelable
    public static class AttackEvent extends ItemEvent {
        public final int type;//1: Hit, 2: Whiff //TODO: Implement type 2
        public final Object target;
        public IEntity swingingEntity;

        public AttackEvent(ScriptCustomItem item, IEntity swingingEntity, int type, Object target) {
            super(item);
            this.type = type;
            this.target = target;
            this.swingingEntity = swingingEntity;
        }
    }

}
