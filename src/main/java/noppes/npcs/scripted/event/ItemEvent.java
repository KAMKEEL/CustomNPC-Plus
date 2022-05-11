package noppes.npcs.scripted.event;

import cpw.mods.fml.common.eventhandler.Cancelable;
import noppes.npcs.scripted.interfaces.item.ICustomItem;
import noppes.npcs.scripted.interfaces.entity.IEntity;
import noppes.npcs.scripted.interfaces.entity.IPlayer;

public class ItemEvent extends CustomNPCsEvent {
    public ICustomItem item;

    public ItemEvent(ICustomItem item) {
        this.item = item;
    }

    public static class InitEvent extends ItemEvent {
        public InitEvent(ICustomItem item) {
            super(item);
        }
    }

    public static class UpdateEvent extends ItemEvent {
        public IEntity entity;

        public UpdateEvent(ICustomItem item, IEntity entity) {
            super(item);
            this.entity = entity;
        }
    }

    @Cancelable
    public static class TossedEvent extends ItemEvent {
        public IEntity entity;
        public IPlayer player;

        public TossedEvent(ICustomItem item, IPlayer player, IEntity entity) {
            super(item);
            this.entity = entity;
            this.player = player;
        }
    }

    public static class PickedUpEvent extends ItemEvent {
        public IPlayer player;

        public PickedUpEvent(ICustomItem item, IPlayer player) {
            super(item);
            this.player = player;
        }
    }

    @Cancelable
    public static class SpawnEvent extends ItemEvent {
        public IEntity entity;
        public SpawnEvent(ICustomItem item, IEntity entity){
            super(item);
            this.entity = entity;
        }
    }

    @Cancelable
    public static class InteractEvent extends ItemEvent {
        public final int type;
        public final Object target;
        public IPlayer player;

        public InteractEvent(ICustomItem item, IPlayer player, int type, Object target) {
            super(item);
            this.type = type;
            this.target = target;
            this.player = player;
        }
    }

    @Cancelable
    public static class AttackEvent extends ItemEvent {
        public final int type;//1: Hit, 2: Whiff
        public final Object target;
        public IEntity swingingEntity;

        public AttackEvent(ICustomItem item, IEntity swingingEntity, int type, Object target) {
            super(item);
            this.type = type;
            this.target = target;
            this.swingingEntity = swingingEntity;
        }
    }

    public static class StartUsingItem extends ItemEvent {
        public final IPlayer player;
        public final int duration;

        public StartUsingItem(ICustomItem item, IPlayer player, int duration){
            super(item);
            this.player = player;
            this.duration = duration;
        }
    }
    public static class UsingItem extends ItemEvent {
        public final IPlayer player;
        public final int duration;

        public UsingItem(ICustomItem item, IPlayer player, int duration){
            super(item);
            this.player = player;
            this.duration = duration;
        }
    }
    public static class StopUsingItem extends ItemEvent {
        public final IPlayer player;
        public final int duration;

        public StopUsingItem(ICustomItem item, IPlayer player, int duration){
            super(item);
            this.player = player;
            this.duration = duration;
        }
    }
    public static class FinishUsingItem extends ItemEvent {
        public final IPlayer player;
        public final int duration;

        public FinishUsingItem(ICustomItem item, IPlayer player, int duration){
            super(item);
            this.player = player;
            this.duration = duration;
        }
    }
}
