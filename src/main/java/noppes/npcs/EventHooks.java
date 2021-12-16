//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package noppes.npcs;

import cpw.mods.fml.common.eventhandler.Event;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.world.WorldEvent;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.controllers.data.ForgeDataScript;
import noppes.npcs.controllers.data.PlayerDataScript;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.scripted.*;
import noppes.npcs.scripted.event.*;
import noppes.npcs.scripted.event.PlayerEvent.ChatEvent;
import noppes.npcs.scripted.event.PlayerEvent.ContainerOpen;
import noppes.npcs.scripted.event.PlayerEvent.DamagedEntityEvent;
import noppes.npcs.scripted.event.PlayerEvent.KeyPressedEvent;
import noppes.npcs.scripted.event.PlayerEvent.LoginEvent;
import noppes.npcs.scripted.event.PlayerEvent.LogoutEvent;
import noppes.npcs.scripted.event.PlayerEvent.PickUpEvent;
import noppes.npcs.scripted.event.PlayerEvent.DropEvent;
import noppes.npcs.scripted.interfaces.IEntity;
import noppes.npcs.scripted.interfaces.IItemStack;
import noppes.npcs.scripted.interfaces.IWorld;
import noppes.npcs.scripted.wrapper.WrapperNpcAPI;
import noppes.npcs.constants.EnumScriptType;
import noppes.npcs.controllers.PlayerData;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

public class EventHooks {
    public EventHooks() {
    }

    public static void onNPCTimer(EntityNPCInterface npc, int id) {
        NpcEvent.TimerEvent event = new NpcEvent.TimerEvent(npc.wrappedNPC, id);
        ScriptEventTimer scriptEvent = new ScriptEventTimer(id);
        npc.script.callScript(EnumScriptType.TIMER, "event", scriptEvent);
        WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static void onPlayerInit(PlayerDataScript handler) {
        noppes.npcs.scripted.event.PlayerEvent.InitEvent event = new noppes.npcs.scripted.event.PlayerEvent.InitEvent(handler.getPlayer());
        handler.callScript(EnumScriptType.INIT, event, "event", event);
        WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static void onPlayerTick(PlayerDataScript handler) {
        noppes.npcs.scripted.event.PlayerEvent.UpdateEvent event = new noppes.npcs.scripted.event.PlayerEvent.UpdateEvent(handler.getPlayer());
        handler.callScript(EnumScriptType.TICK, event, "event", event);
        WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onPlayerInteract(PlayerDataScript handler, noppes.npcs.scripted.event.PlayerEvent.InteractEvent event) {
        handler.callScript(EnumScriptType.INTERACT, event,"event", event);
        return WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onStartUsingItem(PlayerDataScript handler, int duration, ItemStack item){
        PlayerEvent.StartUsingItem event = new PlayerEvent.StartUsingItem(handler.getPlayer(), item, duration);
        handler.callScript(EnumScriptType.START_USING_ITEM, event,"event", event);
        return WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onUsingItem(PlayerDataScript handler, int duration, ItemStack item){
        PlayerEvent.UsingItem event = new PlayerEvent.UsingItem(handler.getPlayer(), item, duration);
        handler.callScript(EnumScriptType.USING_ITEM, event,"event", event);
        return WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onStopUsingItem(PlayerDataScript handler, int duration, ItemStack item){
        PlayerEvent.StopUsingItem event = new PlayerEvent.StopUsingItem(handler.getPlayer(), item, duration);
        handler.callScript(EnumScriptType.STOP_USING_ITEM, event,"event", event);
        return WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static void onFinishUsingItem(PlayerDataScript handler, int duration, ItemStack item){
        PlayerEvent.FinishUsingItem event = new PlayerEvent.FinishUsingItem(handler.getPlayer(), item, duration);
        handler.callScript(EnumScriptType.FINISH_USING_ITEM, event,"event", event);
        WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onPlayerDropItems(PlayerDataScript handler, ArrayList<EntityItem> entityItems) {
        IItemStack[] items = new IItemStack[entityItems.size()];
        for(int i = 0; i < entityItems.size(); i++){ items[i] = NpcAPI.Instance().getIItemStack(entityItems.get(i).getEntityItem()); }

        DropEvent event = new DropEvent(handler.getPlayer(), items);
        handler.callScript(EnumScriptType.DROP, event,"event", event);
        return WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static void onPlayerPickupXP(PlayerDataScript handler, EntityXPOrb orb) {
        PlayerEvent.PickupXPEvent event = new PlayerEvent.PickupXPEvent(handler.getPlayer(), orb);
        handler.callScript(EnumScriptType.PICKUP_XP, event,"event", event);
        WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onPlayerToss(PlayerDataScript handler, EntityItem entityItem) {
        PlayerEvent.TossEvent event = new PlayerEvent.TossEvent(handler.getPlayer(), NpcAPI.Instance().getIItemStack(entityItem.getEntityItem()));
        handler.callScript(EnumScriptType.TOSS, event,"event", event);
        return WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onPlayerPickUp(PlayerDataScript handler, EntityItem entityItem) {
        PickUpEvent event = new PickUpEvent(handler.getPlayer(), new ScriptItemStack(entityItem.getEntityItem()));//NpcAPI.Instance().getIItemStack(entityItem.getEntityItem()));
        handler.callScript(EnumScriptType.PICKUP, event,"event", event);
        return WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static void onPlayerContainerOpen(PlayerDataScript handler, Container container) {
        ContainerOpen event = new ContainerOpen(handler.getPlayer(), NpcAPI.Instance().getIContainer(container));
        handler.callScript(EnumScriptType.CONTAINER_OPEN, event,"event", event);
        WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static void onPlayerUseHoe(PlayerDataScript handler, ItemStack hoe, int x, int y, int z) {
        noppes.npcs.scripted.event.PlayerEvent.UseHoe event = new noppes.npcs.scripted.event.PlayerEvent.UseHoe(handler.getPlayer(), hoe, x, y, z);
        handler.callScript(EnumScriptType.USE_HOE, event,"event", event);
        WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static void onPlayerSleep(PlayerDataScript handler, int x, int y, int z) {
        noppes.npcs.scripted.event.PlayerEvent.Sleep event = new noppes.npcs.scripted.event.PlayerEvent.Sleep(handler.getPlayer(), x, y, z);
        handler.callScript(EnumScriptType.SLEEP, event,"event", event);
        WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static void onPlayerWakeUp(PlayerDataScript handler, boolean setSpawn) {
        noppes.npcs.scripted.event.PlayerEvent.WakeUp event = new noppes.npcs.scripted.event.PlayerEvent.WakeUp(handler.getPlayer(), setSpawn);
        handler.callScript(EnumScriptType.WAKE_UP, event,"event", event);
        WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static void onPlayerDeath(PlayerDataScript handler, DamageSource source, Entity entity) {
        noppes.npcs.scripted.event.PlayerEvent.DiedEvent event = new noppes.npcs.scripted.event.PlayerEvent.DiedEvent(handler.getPlayer(), source, entity);
        handler.callScript(EnumScriptType.KILLED, event,"event", event);
        WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static void onPlayerKills(PlayerDataScript handler, EntityLivingBase entityLiving) {
        noppes.npcs.scripted.event.PlayerEvent.KilledEntityEvent event = new noppes.npcs.scripted.event.PlayerEvent.KilledEntityEvent(handler.getPlayer(), entityLiving);
        handler.callScript(EnumScriptType.KILLS, event,"event", event);
        WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onPlayerDamaged(PlayerDataScript handler, noppes.npcs.scripted.event.PlayerEvent.DamagedEvent event) {
        handler.callScript(EnumScriptType.DAMAGED, event,"event", event);
        return WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onPlayerLightning(PlayerDataScript handler) {
        PlayerEvent.LightningEvent event = new PlayerEvent.LightningEvent(handler.getPlayer());
        handler.callScript(EnumScriptType.LIGHTNING, event, "event", event);
        return WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onPlayerSound(PlayerDataScript handler, String name, float pitch, float volume) {
        PlayerEvent.SoundEvent event = new PlayerEvent.SoundEvent(handler.getPlayer(), name, pitch, volume);
        handler.callScript(EnumScriptType.PLAYSOUND, event,"event", event);
        return WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static void onPlayerFall(PlayerDataScript handler, float distance) {
        PlayerEvent.FallEvent event = new PlayerEvent.FallEvent(handler.getPlayer(),distance);
        handler.callScript(EnumScriptType.FALL, event,"event", event);
        WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static void onPlayerJump(PlayerDataScript handler) {
        PlayerEvent.JumpEvent event = new PlayerEvent.JumpEvent(handler.getPlayer());
        handler.callScript(EnumScriptType.JUMP, event,"event", event);
        WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static void onPlayerLogin(PlayerDataScript handler) {
        LoginEvent event = new LoginEvent(handler.getPlayer());
        handler.callScript(EnumScriptType.LOGIN, event, "event", event);
        WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static void onPlayerAchievement(PlayerDataScript handler, String description) {
        PlayerEvent.Achievement event = new PlayerEvent.Achievement(handler.getPlayer(), description);
        handler.callScript(EnumScriptType.ACHIEVEMENT, event, "event", event);
        WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static void onPlayerFillBucket(PlayerDataScript handler, ItemStack current, ItemStack result) {
        PlayerEvent.FillBucket event = new PlayerEvent.FillBucket(handler.getPlayer(), current, result);
        handler.callScript(EnumScriptType.FILL_BUCKET, event, "event", event);
        WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static void onPlayerBonemeal(PlayerDataScript handler, int x, int y, int z, World world) {
        PlayerEvent.Bonemeal event = new PlayerEvent.Bonemeal(handler.getPlayer(), x, y, z, world);
        handler.callScript(EnumScriptType.BONEMEAL, event, "event", event);
        WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static void onPlayerRespawn(PlayerDataScript handler) {
        PlayerEvent.RespawnEvent event = new PlayerEvent.RespawnEvent(handler.getPlayer());
        handler.callScript(EnumScriptType.RESPAWN, event, "event", event);
        WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static void onPlayerLogout(PlayerDataScript handler) {
        LogoutEvent event = new LogoutEvent(handler.getPlayer());
        handler.callScript(EnumScriptType.LOGOUT, event, "event", event);
        WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static void onPlayerChat(PlayerDataScript handler, ChatEvent event) {
        handler.callScript(EnumScriptType.CHAT, event,"event", event);
        WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onPlayerBowCharge(PlayerDataScript handler, noppes.npcs.scripted.event.PlayerEvent.RangedChargeEvent event) {
        handler.callScript(EnumScriptType.RANGED_CHARGE, event,"event", event);
        return WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onPlayerRanged(PlayerDataScript handler, noppes.npcs.scripted.event.PlayerEvent.RangedLaunchedEvent event) {
        handler.callScript(EnumScriptType.RANGED_LAUNCHED, event,"event", event);
        return WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onPlayerDamagedEntity(PlayerDataScript handler, DamagedEntityEvent event) {
        handler.callScript(EnumScriptType.DAMAGED_ENTITY, event,"event", event);
        return WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static void onPlayerChangeDim(PlayerDataScript handler, int fromDim, int toDim) {
        PlayerEvent.ChangedDimension event = new PlayerEvent.ChangedDimension(handler.getPlayer(), fromDim, toDim);
        handler.callScript(EnumScriptType.CHANGED_DIM, event,"event", event);
        WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static void onPlayerMouseClicked(EntityPlayerMP player, int button, int mouseWheel, boolean buttonDown) {
        PlayerDataScript handler = PlayerData.get(player).scriptData;
        PlayerEvent.MouseClickedEvent event = new PlayerEvent.MouseClickedEvent(handler.getPlayer(), button, mouseWheel, buttonDown);
        handler.callScript(EnumScriptType.MOUSE_CLICKED, event,"event", event);
        WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static void onPlayerKeyPressed(EntityPlayerMP player, int button, boolean isCtrlPressed, boolean isShiftPressed, boolean isAltPressed, boolean isMetaPressed, boolean buttonDown) {
        PlayerDataScript handler = PlayerData.get(player).scriptData;
        KeyPressedEvent event = new KeyPressedEvent(handler.getPlayer(), button, isCtrlPressed, isAltPressed, isShiftPressed, isMetaPressed, buttonDown);
        handler.callScript(EnumScriptType.KEY_PRESSED, event,"event", event);
        WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static void onPlayerTimer(PlayerData data, int id) {
        PlayerDataScript handler = data.scriptData;
        noppes.npcs.scripted.event.PlayerEvent.TimerEvent event = new noppes.npcs.scripted.event.PlayerEvent.TimerEvent(handler.getPlayer(), id);
        handler.callScript(EnumScriptType.TIMER, event,"event", event);
        WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onPlayerBreak(PlayerDataScript handler, PlayerEvent.BreakEvent event) {
        handler.callScript(EnumScriptType.BREAK_BLOCK, event,"event", event);
        return WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static void onForgeEntityEvent(EntityEvent event) {
        IEntity e = NpcAPI.Instance().getIEntity(event.entity);
        onForgeEvent(new noppes.npcs.scripted.event.ForgeEvent.EntityEvent(event, e), event);
    }

    public static void onForgeWorldEvent(WorldEvent event) {
        if(ScriptController.Instance.forgeScripts.isEnabled()) {
            IWorld e = NpcAPI.Instance().getIWorld((WorldServer)event.world);
            onForgeEvent(new noppes.npcs.scripted.event.ForgeEvent.WorldEvent(event, e), event);
        }
    }

    public static void onForgeInit(ForgeDataScript handler) {
        noppes.npcs.scripted.event.ForgeEvent.InitEvent event = new noppes.npcs.scripted.event.ForgeEvent.InitEvent();
        handler.callScript("init", event);
        WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static void onForgeEvent(ForgeEvent ev, Event event) {
        ForgeDataScript handler = ScriptController.Instance.forgeScripts;
        if(handler.isEnabled()) {
            String eventName = event.getClass().getName();
            int i = eventName.lastIndexOf(".");
            eventName = StringUtils.uncapitalize(eventName.substring(i + 1).replace("$", ""));
            if(event.isCancelable()) {
                ev.setCanceled(event.isCanceled());
            }

            handler.callScript(eventName, event);
            WrapperNpcAPI.EVENT_BUS.post(ev);
            if(event.isCancelable()) {
                event.setCanceled(ev.isCanceled());
            }

        }
    }
}