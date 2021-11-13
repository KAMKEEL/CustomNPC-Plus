//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package noppes.npcs.scripted.event;

import cpw.mods.fml.common.eventhandler.Cancelable;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import noppes.npcs.scripted.*;
import noppes.npcs.scripted.entity.ScriptLivingBase;
import noppes.npcs.scripted.interfaces.*;

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
        public final boolean keyDown;

        public KeyPressedEvent(IPlayer player, int key, boolean isCtrlPressed, boolean isAltPressed, boolean isShiftPressed, boolean isMetaPressed, boolean keyDown) {
            super(player);
            this.key = key;
            this.isCtrlPressed = isCtrlPressed;
            this.isAltPressed = isAltPressed;
            this.isShiftPressed = isShiftPressed;
            this.isMetaPressed = isMetaPressed;
            this.keyDown = keyDown;
        }
    }

    public static class MouseClickedEvent extends PlayerEvent {
        public final int button;
        public final int mouseWheel;
        public final boolean buttonDown;

        public MouseClickedEvent(IPlayer player, int button, int mouseWheel, boolean buttonDown){
            super(player);
            this.button = button;
            this.mouseWheel = mouseWheel;
            this.buttonDown = buttonDown;
        }
    }

    public static class PickupXPEvent extends PlayerEvent {
        public final int amount;

        public PickupXPEvent(IPlayer player, EntityXPOrb orb) {
            super(player);
            this.amount = orb.xpValue;
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

    public static class RespawnEvent extends PlayerEvent {
        public RespawnEvent(IPlayer player) {
            super(player);
        }
    }

    public static class ChangedDimension extends PlayerEvent {
        public final int fromDim;
        public final int toDim;

        public ChangedDimension(IPlayer player, int fromDim, int toDim){
            super(player);
            this.fromDim = fromDim;
            this.toDim = toDim;
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

    @Cancelable
    public static class LightningEvent extends PlayerEvent {
        public LightningEvent(IPlayer player) {
            super(player);
        }
    }

    @Cancelable
    public static class SoundEvent extends PlayerEvent {
        public final String name;
        public final float pitch;
        public final float volume;

        public SoundEvent(IPlayer player, String name, float pitch, float volume) {
            super(player);
            this.name = name;
            this.pitch = pitch;
            this.volume = volume;
        }
    }

    public static class FallEvent extends PlayerEvent {
        public final float distance;

        public FallEvent(IPlayer player, float distance){
            super(player);
            this.distance = distance;
        }
    }

    public static class JumpEvent extends PlayerEvent {
        public JumpEvent(IPlayer player){
            super(player);
        }
    }

    public static class KilledEntityEvent extends PlayerEvent {
        public final IEntityLivingBase entity;

        public KilledEntityEvent(IPlayer player, EntityLivingBase entity) {
            super(player);
            this.entity = new ScriptLivingBase<>(entity);//NpcAPI.Instance().getIEntity(entity);
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
        public final IItemStack bow;
        public int charge;

        public RangedLaunchedEvent(IPlayer player, ItemStack bow, int charge) {
            super(player);
            this.bow = NpcAPI.Instance().getIItemStack(bow);
            this.charge = charge;
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
    public static class DropEvent extends PlayerEvent {
        public final IItemStack[] items;

        public DropEvent(IPlayer player, IItemStack[] items) {
            super(player);
            this.items = items;
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

    public static class StartUsingItem extends PlayerEvent {
        public final IItemStack item;
        public final int duration;

        public StartUsingItem(IPlayer player, ItemStack item, int duration){
            super(player);

            this.item = NpcAPI.Instance().getIItemStack(item);
            this.duration = duration;
        }
    }
    public static class UsingItem extends PlayerEvent {
        public final IItemStack item;
        public final int duration;

        public UsingItem(IPlayer player, ItemStack item, int duration){
            super(player);

            this.item = NpcAPI.Instance().getIItemStack(item);
            this.duration = duration;
        }
    }
    public static class StopUsingItem extends PlayerEvent {
        public final IItemStack item;
        public final int duration;

        public StopUsingItem(IPlayer player, ItemStack item, int duration){
            super(player);

            this.item = NpcAPI.Instance().getIItemStack(item);
            this.duration = duration;
        }
    }
    public static class FinishUsingItem extends PlayerEvent {
        public final IItemStack item;
        public final int duration;

        public FinishUsingItem(IPlayer player, ItemStack item, int duration){
            super(player);

            this.item = NpcAPI.Instance().getIItemStack(item);
            this.duration = duration;
        }
    }

    @Cancelable
    public static class BreakEvent extends PlayerEvent {
        public final IBlock block;
        public int exp;

        public BreakEvent(IPlayer player, IBlock block, int exp) {
            super(player);
            this.block = block;
            this.exp = exp;
        }
    }

    public static class UseHoe extends PlayerEvent {
        public final IItemStack hoe;
        public final int x;
        public final int y;
        public final int z;

        public UseHoe(IPlayer player, ItemStack item, int x, int y, int z) {
            super(player);
            this.hoe = new ScriptItemStack(item);
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    public static class WakeUp extends PlayerEvent{
        public final boolean setSpawn;

        public WakeUp(IPlayer player, boolean setSpawn) {
            super(player);
            this.setSpawn = setSpawn;
        }
    }

    public static class Sleep extends PlayerEvent {
        public final int x;
        public final int y;
        public final int z;

        public Sleep(IPlayer player, int x, int y, int z) {
            super(player);
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    public static class Achievement extends PlayerEvent {
        public final String description;

        public Achievement(IPlayer player, String description) {
            super(player);
            this.description = description;
        }
    }

    public static class FillBucket extends PlayerEvent {
        public final IItemStack current;
        public final IItemStack result;

        public FillBucket(IPlayer player, ItemStack current, ItemStack result) {
            super(player);
            this.current = new ScriptItemStack(current);
            this.result = new ScriptItemStack(result);
        }
    }

    public static class Bonemeal extends PlayerEvent {
        public final IBlock block;
        public final int x;
        public final int y;
        public final int z;

        public Bonemeal(IPlayer player, int x, int y, int z, World world) {
            super(player);
            this.block = API.getIBlock(world, x, y, z);
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    public static class RangedChargeEvent extends PlayerEvent {
        public RangedChargeEvent(IPlayer player) {
            super(player);
        }
    }
}
