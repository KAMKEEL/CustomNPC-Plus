//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package noppes.npcs.scripted.event;

import cpw.mods.fml.common.eventhandler.Cancelable;
import cpw.mods.fml.common.eventhandler.Event;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import noppes.npcs.scripted.IContainer;
import noppes.npcs.scripted.IDamageSource;
import noppes.npcs.scripted.NpcAPI;
import noppes.npcs.scripted.IEntity;
import noppes.npcs.scripted.IEntityLivingBase;
import noppes.npcs.scripted.IPlayer;
import noppes.npcs.scripted.IItemStack;

public class PlayerEvent extends CustomNPCsEvent {
    public final IPlayer player;

    public PlayerEvent(IPlayer player) {
        this.player = player;
    }

    @Cancelable
    public static class ChatEvent extends PlayerEvent {
        public String message;

        public ChatEvent(IPlayer player, String message) {
            super(player);
            this.message = message;
        }
    }

    public static class KeyPressedEvent extends PlayerEvent {
        public final int key;
        public final boolean isCtrlPressed;
        public final boolean isAltPressed;
        public final boolean isShiftPressed;
        public final boolean isMetaPressed;

        public KeyPressedEvent(IPlayer player, int key, boolean isCtrlPressed, boolean isAltPressed, boolean isShiftPressed, boolean isMetaPressed) {
            super(player);
            this.key = key;
            this.isCtrlPressed = isCtrlPressed;
            this.isAltPressed = isAltPressed;
            this.isShiftPressed = isShiftPressed;
            this.isMetaPressed = isMetaPressed;
        }
    }

    public static class LevelUpEvent extends PlayerEvent {
        public final int change;

        public LevelUpEvent(IPlayer player, int change) {
            super(player);
            this.change = change;
        }
    }

    public static class LogoutEvent extends PlayerEvent {
        public LogoutEvent(IPlayer player) {
            super(player);
        }
    }

    public static class LoginEvent extends PlayerEvent {
        public LoginEvent(IPlayer player) {
            super(player);
        }
    }

    public static class TimerEvent extends PlayerEvent {
        public final int id;

        public TimerEvent(IPlayer player, int id) {
            super(player);
            this.id = id;
        }
    }

    @Cancelable
    public static class DamagedEvent extends PlayerEvent {
        public final IDamageSource damageSource;
        public final IEntity source;
        public float damage;
        public boolean clearTarget = false;

        public DamagedEvent(IPlayer player, Entity source, float damage, DamageSource damagesource) {
            super(player);
            this.source = NpcAPI.Instance().getIEntity(source);
            this.damage = damage;
            this.damageSource = NpcAPI.Instance().getIDamageSource(damagesource);
        }
    }

    public static class KilledEntityEvent extends PlayerEvent {
        public final IEntityLivingBase entity;

        public KilledEntityEvent(IPlayer player, EntityLivingBase entity) {
            super(player);
            this.entity = (IEntityLivingBase)NpcAPI.Instance().getIEntity(entity);
        }
    }

    @Cancelable
    public static class DiedEvent extends PlayerEvent {
        public final IDamageSource damageSource;
        public final String type;
        public final IEntity source;

        public DiedEvent(IPlayer player, DamageSource damagesource, Entity entity) {
            super(player);
            this.damageSource = NpcAPI.Instance().getIDamageSource(damagesource);
            this.type = damagesource.getDamageType();
            this.source = NpcAPI.Instance().getIEntity(entity);
        }
    }

    @Cancelable
    public static class RangedLaunchedEvent extends PlayerEvent {
        public RangedLaunchedEvent(IPlayer player) {
            super(player);
        }
    }

    @Cancelable
    public static class DamagedEntityEvent extends PlayerEvent {
        public final IDamageSource damageSource;
        public final IEntity target;
        public float damage;

        public DamagedEntityEvent(IPlayer player, Entity target, float damage, DamageSource damagesource) {
            super(player);
            this.target = NpcAPI.Instance().getIEntity(target);
            this.damage = damage;
            this.damageSource = NpcAPI.Instance().getIDamageSource(damagesource);
        }
    }

    public static class ContainerClosed extends PlayerEvent {
        public final IContainer container;

        public ContainerClosed(IPlayer player, IContainer container) {
            super(player);
            this.container = container;
        }
    }

    public static class ContainerOpen extends PlayerEvent {
        public final IContainer container;

        public ContainerOpen(IPlayer player, IContainer container) {
            super(player);
            this.container = container;
        }
    }

    @Cancelable
    public static class PickUpEvent extends PlayerEvent {
        public final IItemStack item;

        public PickUpEvent(IPlayer player, IItemStack item) {
            super(player);
            this.item = item;
        }
    }

    @Cancelable
    public static class TossEvent extends PlayerEvent {
        public final IItemStack item;

        public TossEvent(IPlayer player, IItemStack item) {
            super(player);
            this.item = item;
        }
    }

    @Cancelable
    public static class AttackEvent extends PlayerEvent {
        public final int type;
        public final Object target;

        public AttackEvent(IPlayer player, int type, Object target) {
            super(player);
            this.type = type;
            this.target = target;
        }
    }

    @Cancelable
    public static class InteractEvent extends PlayerEvent {
        public final int type;
        public final Object target;

        public InteractEvent(IPlayer player, int type, Object target) {
            super(player);
            this.type = type;
            this.target = target;
        }
    }

    public static class UpdateEvent extends PlayerEvent {
        public UpdateEvent(IPlayer player) {
            super(player);
        }
    }

    public static class InitEvent extends PlayerEvent {
        public InitEvent(IPlayer player) {
            super(player);
        }
    }
}
