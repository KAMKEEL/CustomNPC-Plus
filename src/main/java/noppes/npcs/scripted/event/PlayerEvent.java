//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package noppes.npcs.scripted.event;

import cpw.mods.fml.common.eventhandler.Cancelable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import noppes.npcs.api.IBlock;
import noppes.npcs.api.IContainer;
import noppes.npcs.api.IDamageSource;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.event.IPlayerEvent;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.constants.EnumScriptType;
import noppes.npcs.scripted.NpcAPI;

public class PlayerEvent extends CustomNPCsEvent implements IPlayerEvent {
    public final IPlayer player;

    public PlayerEvent(IPlayer player) {
        this.player = player;
    }

    public IPlayer getPlayer() {
        return player;
    }

    @Cancelable
    public static class ChatEvent extends PlayerEvent implements IPlayerEvent.ChatEvent {
        public String message;

        public ChatEvent(IPlayer player, String message) {
            super(player);
            this.message = message;
        }

        public String getHookName() {
            return EnumScriptType.CHAT.function;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    public static class KeyPressedEvent extends PlayerEvent implements IPlayerEvent.KeyPressedEvent {
        public final int key;
        public final boolean isCtrlPressed;
        public final boolean isAltPressed;
        public final boolean isShiftPressed;
        public final boolean isMetaPressed;
        public final boolean keyDown;
        public final int[] keysDown;

        public KeyPressedEvent(IPlayer player, int key, boolean isCtrlPressed, boolean isAltPressed, boolean isShiftPressed, boolean isMetaPressed, boolean keyDown, int[] heldKeys) {
            super(player);
            this.key = key;
            this.isCtrlPressed = isCtrlPressed;
            this.isAltPressed = isAltPressed;
            this.isShiftPressed = isShiftPressed;
            this.isMetaPressed = isMetaPressed;
            this.keyDown = keyDown;
            this.keysDown = heldKeys;
        }

        public String getHookName() {
            return EnumScriptType.KEY_PRESSED.function;
        }

        public int getKey() {
            return key;
        }

        public boolean isCtrlPressed() {
            return isCtrlPressed;
        }

        public boolean isAltPressed() {
            return isAltPressed;
        }

        public boolean isShiftPressed() {
            return isShiftPressed;
        }

        public boolean isMetaPressed() {
            return isMetaPressed;
        }

        public boolean keyDown() {
            return keyDown;
        }

        public int[] getKeysDown() {
            return keysDown;
        }
    }

    public static class MouseClickedEvent extends PlayerEvent implements IPlayerEvent.MouseClickedEvent {
        public final boolean isCtrlPressed;
        public final boolean isAltPressed;
        public final boolean isShiftPressed;
        public final boolean isMetaPressed;
        public final int[] keysDown;

        public final int button;
        public final int mouseWheel;
        public final boolean buttonDown;

        public MouseClickedEvent(IPlayer player, int button, int mouseWheel, boolean buttonDown, boolean isCtrlPressed, boolean isAltPressed, boolean isShiftPressed, boolean isMetaPressed, int[] heldKeys){
            super(player);
            this.button = button;
            this.mouseWheel = mouseWheel;
            this.buttonDown = buttonDown;
            this.isCtrlPressed = isCtrlPressed;
            this.isAltPressed = isAltPressed;
            this.isShiftPressed = isShiftPressed;
            this.isMetaPressed = isMetaPressed;
            this.keysDown = heldKeys;
        }

        public String getHookName() {
            return EnumScriptType.MOUSE_CLICKED.function;
        }

        public int getButton() {
            return button;
        }

        public int getMouseWheel() {
            return mouseWheel;
        }

        public boolean buttonDown() {
            return buttonDown;
        }

        public boolean isCtrlPressed() {
            return isCtrlPressed;
        }

        public boolean isAltPressed() {
            return isAltPressed;
        }

        public boolean isShiftPressed() {
            return isShiftPressed;
        }

        public boolean isMetaPressed() {
            return isMetaPressed;
        }

        public int[] getKeysDown() {
            return keysDown;
        }
    }

    public static class PickupXPEvent extends PlayerEvent implements IPlayerEvent.PickupXPEvent {
        public final int amount;

        public PickupXPEvent(IPlayer player, EntityXPOrb orb) {
            super(player);
            this.amount = orb.xpValue;
        }

        public String getHookName() {
            return EnumScriptType.PICKUP_XP.function;
        }

        public int getAmount() {
            return amount;
        }
    }

    public static class LevelUpEvent extends PlayerEvent implements IPlayerEvent.LevelUpEvent {
        public final int change;

        public LevelUpEvent(IPlayer player, int change) {
            super(player);
            this.change = change;
        }

        public String getHookName() {
            return EnumScriptType.LEVEL_UP.function;
        }

        public int getChange() {
            return change;
        }
    }

    public static class LogoutEvent extends PlayerEvent implements IPlayerEvent.LogoutEvent {
        public LogoutEvent(IPlayer player) {
            super(player);
        }

        public String getHookName() {
            return EnumScriptType.LOGOUT.function;
        }
    }

    public static class LoginEvent extends PlayerEvent implements IPlayerEvent.LoginEvent {
        public LoginEvent(IPlayer player) {
            super(player);
        }

        public String getHookName() {
            return EnumScriptType.LOGIN.function;
        }
    }

    public static class RespawnEvent extends PlayerEvent implements IPlayerEvent.RespawnEvent {
        public RespawnEvent(IPlayer player) {
            super(player);
        }

        public String getHookName() {
            return EnumScriptType.RESPAWN.function;
        }
    }

    public static class ChangedDimension extends PlayerEvent implements IPlayerEvent.ChangedDimension {
        public final int fromDim;
        public final int toDim;

        public ChangedDimension(IPlayer player, int fromDim, int toDim){
            super(player);
            this.fromDim = fromDim;
            this.toDim = toDim;
        }

        public String getHookName() {
            return EnumScriptType.CHANGED_DIM.function;
        }

        public int getFromDim() {
            return fromDim;
        }

        public int getToDim() {
            return toDim;
        }
    }

    public static class TimerEvent extends PlayerEvent implements IPlayerEvent.TimerEvent {
        public final int id;

        public TimerEvent(IPlayer player, int id) {
            super(player);
            this.id = id;
        }

        public String getHookName() {
            return EnumScriptType.TIMER.function;
        }

        public int getId() {
            return id;
        }
    }

    @Cancelable
    public static class AttackedEvent extends PlayerEvent implements IPlayerEvent.AttackedEvent {
        public final IDamageSource damageSource;
        public final IEntity source;
        public final float damage;

        public AttackedEvent(IPlayer player, Entity source, float damage, DamageSource damagesource) {
            super(player);
            this.source = NpcAPI.Instance().getIEntity(source);
            this.damage = damage;
            this.damageSource = NpcAPI.Instance().getIDamageSource(damagesource);
        }

        public String getHookName() {
            return EnumScriptType.ATTACKED.function;
        }

        public IDamageSource getDamageSource() {
            return damageSource;
        }

        public IEntity getSource() {
            return source;
        }

        public float getDamage() {
            return damage;
        }
    }

    @Cancelable
    public static class DamagedEvent extends PlayerEvent implements IPlayerEvent.DamagedEvent {
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

        public String getHookName() {
            return EnumScriptType.DAMAGED.function;
        }

        public IDamageSource getDamageSource() {
            return damageSource;
        }

        public IEntity getSource() {
            return source;
        }

        public float getDamage() {
            return damage;
        }
    }

    @Cancelable
    public static class LightningEvent extends PlayerEvent implements IPlayerEvent.LightningEvent {
        public LightningEvent(IPlayer player) {
            super(player);
        }

        public String getHookName() {
            return EnumScriptType.LIGHTNING.function;
        }
    }

    @Cancelable
    public static class SoundEvent extends PlayerEvent implements IPlayerEvent.SoundEvent {
        public final String name;
        public final float pitch;
        public final float volume;

        public SoundEvent(IPlayer player, String name, float pitch, float volume) {
            super(player);
            this.name = name;
            this.pitch = pitch;
            this.volume = volume;
        }

        public String getHookName() {
            return EnumScriptType.PLAYSOUND.function;
        }

        public String getName() {
            return name;
        }

        public float getPitch() {
            return pitch;
        }

        public float getVolume() {
            return volume;
        }
    }

    @Cancelable
    public static class FallEvent extends PlayerEvent implements IPlayerEvent.FallEvent {
        public final float distance;

        public FallEvent(IPlayer player, float distance){
            super(player);
            this.distance = distance;
        }

        public String getHookName() {
            return EnumScriptType.FALL.function;
        }

        public float getDistance() {
            return distance;
        }
    }

    public static class JumpEvent extends PlayerEvent implements IPlayerEvent.JumpEvent {
        public JumpEvent(IPlayer player){
            super(player);
        }

        public String getHookName() {
            return EnumScriptType.JUMP.function;
        }
    }

    public static class KilledEntityEvent extends PlayerEvent implements IPlayerEvent.KilledEntityEvent {
        public final IEntityLivingBase entity;

        public KilledEntityEvent(IPlayer player, EntityLivingBase entity) {
            super(player);
            this.entity = (IEntityLivingBase) NpcAPI.Instance().getIEntity(entity);
        }

        public String getHookName() {
            return EnumScriptType.KILLS.function;
        }

        public IEntityLivingBase getEntity() {
            return entity;
        }
    }

    public static class DiedEvent extends PlayerEvent implements IPlayerEvent.DiedEvent {
        public final IDamageSource damageSource;
        public final String type;
        public final IEntity source;

        public DiedEvent(IPlayer player, DamageSource damagesource, Entity entity) {
            super(player);
            this.damageSource = NpcAPI.Instance().getIDamageSource(damagesource);
            this.type = damagesource.getDamageType();
            this.source = NpcAPI.Instance().getIEntity(entity);
        }

        public String getHookName() {
            return EnumScriptType.KILLED.function;
        }

        public IDamageSource getDamageSource() {
            return damageSource;
        }

        public String getType() {
            return type;
        }

        public IEntity getSource() {
            return source;
        }
    }

    @Cancelable
    public static class RangedLaunchedEvent extends PlayerEvent implements IPlayerEvent.RangedLaunchedEvent {
        public final IItemStack bow;
        public int charge;

        public RangedLaunchedEvent(IPlayer player, ItemStack bow, int charge) {
            super(player);
            this.bow = NpcAPI.Instance().getIItemStack(bow);
            this.charge = charge;
        }

        public String getHookName() {
            return EnumScriptType.RANGED_LAUNCHED.function;
        }

        public IItemStack getBow() {
            return bow;
        }

        public int getCharge() {
            return charge;
        }
    }

    @Cancelable
    public static class AttackEvent extends PlayerEvent implements IPlayerEvent.AttackEvent {
        public final IDamageSource damageSource;
        public final IEntity target;
        public float damage;

        public AttackEvent(IPlayer player, Entity target, float damage, DamageSource damagesource) {
            super(player);
            this.target = NpcAPI.Instance().getIEntity(target);
            this.damage = damage;
            this.damageSource = NpcAPI.Instance().getIDamageSource(damagesource);
        }

        public String getHookName() {
            return EnumScriptType.ATTACK.function;
        }

        public IDamageSource getDamageSource() {
            return damageSource;
        }

        public IEntity getTarget() {
            return target;
        }

        public float getDamage() {
            return damage;
        }
    }

    @Cancelable
    public static class DamagedEntityEvent extends PlayerEvent implements IPlayerEvent.DamagedEntityEvent {
        public final IDamageSource damageSource;
        public final IEntity target;
        public float damage;

        public DamagedEntityEvent(IPlayer player, Entity target, float damage, DamageSource damagesource) {
            super(player);
            this.target = NpcAPI.Instance().getIEntity(target);
            this.damage = damage;
            this.damageSource = NpcAPI.Instance().getIDamageSource(damagesource);
        }

        public String getHookName() {
            return EnumScriptType.DAMAGED_ENTITY.function;
        }

        public IDamageSource getDamageSource() {
            return damageSource;
        }

        public IEntity getTarget() {
            return target;
        }

        public float getDamage() {
            return damage;
        }
    }

    public static class ContainerClosed extends PlayerEvent implements IPlayerEvent.ContainerClosed {
        public final IContainer container;

        public ContainerClosed(IPlayer player, IContainer container) {
            super(player);
            this.container = container;
        }

        public String getHookName() {
            return EnumScriptType.CONTAINER_CLOSED.function;
        }

        public IContainer getContainer() {
            return container;
        }
    }

    public static class ContainerOpen extends PlayerEvent implements IPlayerEvent.ContainerOpen {
        public final IContainer container;

        public ContainerOpen(IPlayer player, IContainer container) {
            super(player);
            this.container = container;
        }

        public String getHookName() {
            return EnumScriptType.CONTAINER_OPEN.function;
        }

        public IContainer getContainer() {
            return container;
        }
    }

    @Cancelable
    public static class PickUpEvent extends PlayerEvent implements IPlayerEvent.PickUpEvent {
        public final IItemStack item;

        public PickUpEvent(IPlayer player, IItemStack item) {
            super(player);
            this.item = item;
        }

        public String getHookName() {
            return EnumScriptType.PICKUP.function;
        }

        public IItemStack getItem() {
            return item;
        }
    }

    @Cancelable
    public static class DropEvent extends PlayerEvent implements IPlayerEvent.DropEvent {
        public final IItemStack[] items;

        public DropEvent(IPlayer player, IItemStack[] items) {
            super(player);
            this.items = items;
        }

        public String getHookName() {
            return EnumScriptType.DROP.function;
        }

        public IItemStack[] getItems() {
            return items;
        }
    }

    @Cancelable
    public static class TossEvent extends PlayerEvent implements IPlayerEvent.TossEvent {
        public final IItemStack item;

        public TossEvent(IPlayer player, IItemStack item) {
            super(player);
            this.item = item;
        }

        public String getHookName() {
            return EnumScriptType.TOSS.function;
        }

        public IItemStack getItem() {
            return item;
        }
    }

    @Cancelable
    public static class InteractEvent extends PlayerEvent implements IPlayerEvent.InteractEvent {
        public final int type;
        public final IEntity target;

        public InteractEvent(IPlayer player, int type, IEntity target) {
            super(player);
            this.type = type;
            this.target = target;
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
    }

    public static class UpdateEvent extends PlayerEvent implements IPlayerEvent.UpdateEvent {
        public UpdateEvent(IPlayer player) {
            super(player);
        }

        public String getHookName() {
            return EnumScriptType.TICK.function;
        }
    }

    public static class InitEvent extends PlayerEvent implements IPlayerEvent.InitEvent {
        public InitEvent(IPlayer player) {
            super(player);
        }

        public String getHookName() {
            return EnumScriptType.INIT.function;
        }
    }

    public static class StartUsingItem extends PlayerEvent implements IPlayerEvent.StartUsingItem {
        public final IItemStack item;
        public final int duration;

        public StartUsingItem(IPlayer player, ItemStack item, int duration){
            super(player);

            this.item = NpcAPI.Instance().getIItemStack(item);
            this.duration = duration;
        }

        public String getHookName() {
            return EnumScriptType.START_USING_ITEM.function;
        }

        public IItemStack getItem() {
            return item;
        }

        public int getDuration() {
            return duration;
        }
    }
    public static class UsingItem extends PlayerEvent implements IPlayerEvent.UsingItem {
        public final IItemStack item;
        public final int duration;

        public UsingItem(IPlayer player, ItemStack item, int duration){
            super(player);

            this.item = NpcAPI.Instance().getIItemStack(item);
            this.duration = duration;
        }

        public String getHookName() {
            return EnumScriptType.USING_ITEM.function;
        }

        public IItemStack getItem() {
            return item;
        }

        public int getDuration() {
            return duration;
        }
    }
    public static class StopUsingItem extends PlayerEvent implements IPlayerEvent.StopUsingItem {
        public final IItemStack item;
        public final int duration;

        public StopUsingItem(IPlayer player, ItemStack item, int duration){
            super(player);

            this.item = NpcAPI.Instance().getIItemStack(item);
            this.duration = duration;
        }

        public String getHookName() {
            return EnumScriptType.STOP_USING_ITEM.function;
        }

        public IItemStack getItem() {
            return item;
        }

        public int getDuration() {
            return duration;
        }
    }
    public static class FinishUsingItem extends PlayerEvent implements IPlayerEvent.FinishUsingItem {
        public final IItemStack item;
        public final int duration;

        public FinishUsingItem(IPlayer player, ItemStack item, int duration){
            super(player);

            this.item = NpcAPI.Instance().getIItemStack(item);
            this.duration = duration;
        }

        public String getHookName() {
            return EnumScriptType.FINISH_USING_ITEM.function;
        }

        public IItemStack getItem() {
            return item;
        }

        public int getDuration() {
            return duration;
        }
    }

    @Cancelable
    public static class BreakEvent extends PlayerEvent implements IPlayerEvent.BreakEvent {
        public final IBlock block;
        public int exp;

        public BreakEvent(IPlayer player, IBlock block, int exp) {
            super(player);
            this.block = block;
            this.exp = exp;
        }

        public String getHookName() {
            return EnumScriptType.BREAK_BLOCK.function;
        }

        public IBlock getBlock() {
            return block;
        }

        public int getExp() {
            return exp;
        }
    }

    public static class UseHoe extends PlayerEvent implements UseHoeEvent {
        public final IItemStack hoe;
        public final int x;
        public final int y;
        public final int z;

        public UseHoe(IPlayer player, ItemStack item, int x, int y, int z) {
            super(player);
            this.hoe = NpcAPI.Instance().getIItemStack(item);
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public String getHookName() {
            return EnumScriptType.USE_HOE.function;
        }

        public IItemStack getHoe() {
            return hoe;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public int getZ() {
            return z;
        }
    }

    public static class WakeUp extends PlayerEvent implements WakeUpEvent {
        public final boolean setSpawn;

        public WakeUp(IPlayer player, boolean setSpawn) {
            super(player);
            this.setSpawn = setSpawn;
        }

        public String getHookName() {
            return EnumScriptType.WAKE_UP.function;
        }

        public boolean setSpawn() {
            return setSpawn;
        }
    }

    public static class Sleep extends PlayerEvent implements SleepEvent {
        public final int x;
        public final int y;
        public final int z;

        public Sleep(IPlayer player, int x, int y, int z) {
            super(player);
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public String getHookName() {
            return EnumScriptType.SLEEP.function;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public int getZ() {
            return z;
        }
    }

    public static class Achievement extends PlayerEvent implements AchievementEvent {
        public final String description;

        public Achievement(IPlayer player, String description) {
            super(player);
            this.description = description;
        }

        public String getHookName() {
            return EnumScriptType.ACHIEVEMENT.function;
        }

        public String getDescription() {
            return description;
        }
    }

    public static class FillBucket extends PlayerEvent implements FillBucketEvent {
        public final IItemStack current;
        public final IItemStack result;

        public FillBucket(IPlayer player, ItemStack current, ItemStack result) {
            super(player);
            this.current = NpcAPI.Instance().getIItemStack(current);
            this.result = NpcAPI.Instance().getIItemStack(result);
        }

        public String getHookName() {
            return EnumScriptType.FILL_BUCKET.function;
        }

        public IItemStack getCurrent() {
            return current;
        }

        public IItemStack getFilled() {
            return result;
        }
    }

    public static class Bonemeal extends PlayerEvent implements BonemealEvent {
        public final IBlock block;
        public final int x;
        public final int y;
        public final int z;

        public Bonemeal(IPlayer player, int x, int y, int z, World world) {
            super(player);
            this.block = API.getIBlock(NpcAPI.Instance().getIWorld(world), x, y, z);
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public String getHookName() {
            return EnumScriptType.BONEMEAL.function;
        }

        public IBlock getBlock() {
            return block;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public int getZ() {
            return z;
        }
    }

    public static class RangedChargeEvent extends PlayerEvent implements IPlayerEvent.RangedChargeEvent {
        public RangedChargeEvent(IPlayer player) {
            super(player);
        }

        public String getHookName() {
            return EnumScriptType.RANGED_LAUNCHED.function;
        }
    }
}
