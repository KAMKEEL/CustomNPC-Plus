package noppes.npcs.scripted.event;

import cpw.mods.fml.common.eventhandler.Cancelable;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.event.IItemEvent;
import noppes.npcs.api.item.IItemCustomizable;
import noppes.npcs.constants.EnumScriptType;

public class ItemEvent extends CustomNPCsEvent implements IItemEvent {
    public final IItemCustomizable item;

    public ItemEvent(IItemCustomizable item) {
        this.item = item;
    }

    public IItemCustomizable getItem() {
        return item;
    }

    public String getHookName() {
        return EnumScriptType.CUSTOM_ITEM_EVENT.function;
    }

    public static class InitEvent extends ItemEvent implements IItemEvent.InitEvent {
        public InitEvent(IItemCustomizable item) {
            super(item);
        }

        public String getHookName() {
            return EnumScriptType.INIT.function;
        }
    }

    public static class UpdateEvent extends ItemEvent implements IItemEvent.UpdateEvent {
        public final IEntity entity;

        public UpdateEvent(IItemCustomizable item, IEntity entity) {
            super(item);
            this.entity = entity;
        }

        public String getHookName() {
            return EnumScriptType.TICK.function;
        }

        public IEntity getEntity() {
            return entity;
        }
    }

    @Cancelable
    public static class TossedEvent extends ItemEvent implements IItemEvent.TossedEvent {
        public final IEntity entity;
        public final IPlayer player;

        public TossedEvent(IItemCustomizable item, IPlayer player, IEntity entity) {
            super(item);
            this.entity = entity;
            this.player = player;
        }

        public String getHookName() {
            return EnumScriptType.TOSSED.function;
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

        public PickedUpEvent(IItemCustomizable item, IPlayer player) {
            super(item);
            this.player = player;
        }

        public String getHookName() {
            return EnumScriptType.PICKEDUP.function;
        }

        public IPlayer getPlayer() {
            return player;
        }
    }

    @Cancelable
    public static class SpawnEvent extends ItemEvent implements IItemEvent.SpawnEvent {
        public final IEntity entity;

        public SpawnEvent(IItemCustomizable item, IEntity entity){
            super(item);
            this.entity = entity;
        }

        public String getHookName() {
            return EnumScriptType.SPAWN.function;
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

        public InteractEvent(IItemCustomizable item, IPlayer player, int type, IEntity target) {
            super(item);
            this.type = type;
            this.target = target;
            this.player = player;
        }

        public String getHookName() {
            return EnumScriptType.INTERACT.function;
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
    public static class RightClickEvent extends ItemEvent implements IItemEvent.RightClickEvent {

        /**
         * 0:air, 1:entity, 2:block
         */
        public final int type;
        public final Object target;
        public final IPlayer player;

        public RightClickEvent(IItemCustomizable item, IPlayer player, int type, Object target) {
            super(item);
            this.type = type;
            this.target = target;
            this.player = player;
        }

        public String getHookName() {
            return EnumScriptType.RIGHT_CLICK.function;
        }

        public int getType() {
            return type;
        }

        public Object getTarget() {
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

        public AttackEvent(IItemCustomizable item, IEntity swingingEntity, int type, IEntity target) {
            super(item);
            this.type = type;
            this.target = target;
            this.swingingEntity = swingingEntity;
        }

        public String getHookName() {
            return EnumScriptType.ATTACK.function;
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

    @Cancelable
    public static class StartUsingItem extends ItemEvent implements IItemEvent.StartUsingItem {
        public final IPlayer player;
        public final int duration;

        public StartUsingItem(IItemCustomizable item, IPlayer player, int duration){
            super(item);
            this.player = player;
            this.duration = duration;
        }

        public String getHookName() {
            return EnumScriptType.START_USING_ITEM.function;
        }

        public IPlayer getPlayer() {
            return player;
        }

        public int getDuration() {
            return duration;
        }
    }

    @Cancelable
    public static class UsingItem extends ItemEvent implements IItemEvent.UsingItem {
        public final IPlayer player;
        public final int duration;

        public UsingItem(IItemCustomizable item, IPlayer player, int duration){
            super(item);
            this.player = player;
            this.duration = duration;
        }

        public String getHookName() {
            return EnumScriptType.USING_ITEM.function;
        }

        public IPlayer getPlayer() {
            return player;
        }

        public int getDuration() {
            return duration;
        }
    }

    @Cancelable
    public static class StopUsingItem extends ItemEvent implements IItemEvent.StopUsingItem {
        public final IPlayer player;
        public final int duration;

        public StopUsingItem(IItemCustomizable item, IPlayer player, int duration){
            super(item);
            this.player = player;
            this.duration = duration;
        }

        public String getHookName() {
            return EnumScriptType.STOP_USING_ITEM.function;
        }

        public IPlayer getPlayer() {
            return player;
        }

        public int getDuration() {
            return duration;
        }
    }

    @Cancelable
    public static class FinishUsingItem extends ItemEvent implements IItemEvent.FinishUsingItem {
        public final IPlayer player;
        public final int duration;

        public FinishUsingItem(IItemCustomizable item, IPlayer player, int duration){
            super(item);
            this.player = player;
            this.duration = duration;
        }

        public String getHookName() {
            return EnumScriptType.FINISH_USING_ITEM.function;
        }

        public IPlayer getPlayer() {
            return player;
        }

        public int getDuration() {
            return duration;
        }
    }
}
