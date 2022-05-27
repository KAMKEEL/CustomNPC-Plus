package noppes.npcs.scripted.event;

import cpw.mods.fml.common.eventhandler.Cancelable;
import noppes.npcs.scripted.interfaces.item.IItemCustom;
import noppes.npcs.scripted.interfaces.entity.IEntity;
import noppes.npcs.scripted.interfaces.entity.IPlayer;

public class ItemEvent extends CustomNPCsEvent {
    public IItemCustom item;

    public ItemEvent(IItemCustom item) {
        this.item = item;
    }

    public static class InitEvent extends ItemEvent {
        public InitEvent(IItemCustom item) {
            super(item);
        }
    }

    public static class UpdateEvent extends ItemEvent {
        public IEntity entity;

        public UpdateEvent(IItemCustom item, IEntity entity) {
            super(item);
            this.entity = entity;
        }
    }

    @Cancelable
    public static class TossedEvent extends ItemEvent {
        public IEntity entity;
        public IPlayer player;

        public TossedEvent(IItemCustom item, IPlayer player, IEntity entity) {
            super(item);
            this.entity = entity;
            this.player = player;
        }
    }

    public static class PickedUpEvent extends ItemEvent {
        public IPlayer player;

        public PickedUpEvent(IItemCustom item, IPlayer player) {
            super(item);
            this.player = player;
        }
    }

    @Cancelable
    public static class SpawnEvent extends ItemEvent {
        public IEntity entity;
        public SpawnEvent(IItemCustom item, IEntity entity){
            super(item);
            this.entity = entity;
        }
    }

    @Cancelable
    public static class InteractEvent extends ItemEvent {
        public final int type;
        public final Object target;
        public IPlayer player;

        public InteractEvent(IItemCustom item, IPlayer player, int type, Object target) {
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

        public AttackEvent(IItemCustom item, IEntity swingingEntity, int type, Object target) {
            super(item);
            this.type = type;
            this.target = target;
            this.swingingEntity = swingingEntity;
        }
    }

    public static class StartUsingItem extends ItemEvent {
        public final IPlayer player;
        public final int duration;

        public StartUsingItem(IItemCustom item, IPlayer player, int duration){
            super(item);
            this.player = player;
            this.duration = duration;
        }
    }
    public static class UsingItem extends ItemEvent {
        public final IPlayer player;
        public final int duration;

        public UsingItem(IItemCustom item, IPlayer player, int duration){
            super(item);
            this.player = player;
            this.duration = duration;
        }
    }
    public static class StopUsingItem extends ItemEvent {
        public final IPlayer player;
        public final int duration;

        public StopUsingItem(IItemCustom item, IPlayer player, int duration){
            super(item);
            this.player = player;
            this.duration = duration;
        }
    }
    public static class FinishUsingItem extends ItemEvent {
        public final IPlayer player;
        public final int duration;

        public FinishUsingItem(IItemCustom item, IPlayer player, int duration){
            super(item);
            this.player = player;
            this.duration = duration;
        }
    }
}
