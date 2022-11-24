package noppes.npcs.scripted.event;

import cpw.mods.fml.common.eventhandler.Cancelable;
import noppes.npcs.api.event.IItemEvent;
import noppes.npcs.api.item.IItemCustom;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IPlayer;

public class ItemEvent extends CustomNPCsEvent implements IItemEvent {
    public final IItemCustom item;

    public ItemEvent(IItemCustom item) {
        this.item = item;
    }

    public IItemCustom getItem() {
        return item;
    }

    public static class InitEvent extends ItemEvent implements IItemEvent.InitEvent {
        public InitEvent(IItemCustom item) {
            super(item);
        }
    }

    public static class UpdateEvent extends ItemEvent implements IItemEvent.UpdateEvent {
        public final IEntity entity;

        public UpdateEvent(IItemCustom item, IEntity entity) {
            super(item);
            this.entity = entity;
        }

        public IEntity getEntity() {
            return entity;
        }
    }

    @Cancelable
    public static class TossedEvent extends ItemEvent implements IItemEvent.TossedEvent {
        public final IEntity entity;
        public final IPlayer player;

        public TossedEvent(IItemCustom item, IPlayer player, IEntity entity) {
            super(item);
            this.entity = entity;
            this.player = player;
        }

        public IEntity getEntity() {
            return entity;
        }

        public IPlayer getPlayer() {
            return player;
        }
    }

    public static class PickedUpEvent extends ItemEvent implements IItemEvent.PickedUpEvent {
        public final IPlayer player;

        public PickedUpEvent(IItemCustom item, IPlayer player) {
            super(item);
            this.player = player;
        }

        public IPlayer getPlayer() {
            return player;
        }
    }

    @Cancelable
    public static class SpawnEvent extends ItemEvent implements IItemEvent.SpawnEvent {
        public final IEntity entity;

        public SpawnEvent(IItemCustom item, IEntity entity){
            super(item);
            this.entity = entity;
        }

        public IEntity getEntity() {
            return entity;
        }
    }

    @Cancelable
    public static class InteractEvent extends ItemEvent implements IItemEvent.InteractEvent {
        public final int type;
        public final IEntity target;
        public final IPlayer player;

        public InteractEvent(IItemCustom item, IPlayer player, int type, IEntity target) {
            super(item);
            this.type = type;
            this.target = target;
            this.player = player;
        }

        public int getType() {
            return type;
        }

        public IEntity getTarget() {
            return target;
        }

        public IPlayer getPlayer() {
            return player;
        }
    }

    @Cancelable
    public static class AttackEvent extends ItemEvent implements IItemEvent.AttackEvent {
        public final int type;//1: Hit, 2: Whiff
        public final IEntity target;
        public final IEntity swingingEntity;

        public AttackEvent(IItemCustom item, IEntity swingingEntity, int type, IEntity target) {
            super(item);
            this.type = type;
            this.target = target;
            this.swingingEntity = swingingEntity;
        }

        public int getType() {
            return type;
        }

        public IEntity getTarget() {
            return target;
        }

        public IEntity getSwingingEntity() {
            return swingingEntity;
        }
    }

    public static class StartUsingItem extends ItemEvent implements IItemEvent.StartUsingItem {
        public final IPlayer player;
        public final int duration;

        public StartUsingItem(IItemCustom item, IPlayer player, int duration){
            super(item);
            this.player = player;
            this.duration = duration;
        }

        public IPlayer getPlayer() {
            return player;
        }

        public int getDuration() {
            return duration;
        }
    }
    public static class UsingItem extends ItemEvent implements IItemEvent.UsingItem {
        public final IPlayer player;
        public final int duration;

        public UsingItem(IItemCustom item, IPlayer player, int duration){
            super(item);
            this.player = player;
            this.duration = duration;
        }

        public IPlayer getPlayer() {
            return player;
        }

        public int getDuration() {
            return duration;
        }
    }
    public static class StopUsingItem extends ItemEvent implements IItemEvent.StopUsingItem {
        public final IPlayer player;
        public final int duration;

        public StopUsingItem(IItemCustom item, IPlayer player, int duration){
            super(item);
            this.player = player;
            this.duration = duration;
        }

        public IPlayer getPlayer() {
            return player;
        }

        public int getDuration() {
            return duration;
        }
    }
    public static class FinishUsingItem extends ItemEvent implements IItemEvent.FinishUsingItem {
        public final IPlayer player;
        public final int duration;

        public FinishUsingItem(IItemCustom item, IPlayer player, int duration){
            super(item);
            this.player = player;
            this.duration = duration;
        }

        public IPlayer getPlayer() {
            return player;
        }

        public int getDuration() {
            return duration;
        }
    }
}
