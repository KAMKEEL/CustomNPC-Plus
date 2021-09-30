//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package noppes.npcs;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.util.DamageSource;
import noppes.npcs.controllers.data.PlayerScriptData;
import noppes.npcs.scripted.event.PlayerEvent.AttackEvent;
import noppes.npcs.scripted.event.PlayerEvent.ChatEvent;
import noppes.npcs.scripted.event.PlayerEvent.ContainerClosed;
import noppes.npcs.scripted.event.PlayerEvent.ContainerOpen;
import noppes.npcs.scripted.event.PlayerEvent.DamagedEntityEvent;
import noppes.npcs.scripted.event.PlayerEvent.FactionUpdateEvent;
import noppes.npcs.scripted.event.PlayerEvent.KeyPressedEvent;
import noppes.npcs.scripted.event.PlayerEvent.LevelUpEvent;
import noppes.npcs.scripted.event.PlayerEvent.LoginEvent;
import noppes.npcs.scripted.event.PlayerEvent.LogoutEvent;
import noppes.npcs.scripted.event.PlayerEvent.PickUpEvent;
import noppes.npcs.scripted.event.PlayerEvent.TossEvent;
import noppes.npcs.scripted.wrapper.PlayerWrapper;
import noppes.npcs.scripted.wrapper.WrapperNpcAPI;
import noppes.npcs.constants.EnumScriptType;
import noppes.npcs.controllers.data.PlayerData;

public class EventHooks {
    public EventHooks() {
    }

    public static void onForgeInit(EventDataScript handler) {
        noppes.npcs.scripted.event.ForgeEvent.InitEvent event = new noppes.npcs.scripted.event.ForgeEvent.InitEvent();
        handler.runScript(EnumScriptType.INIT, event);
        WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static void onPlayerInit(PlayerScriptData handler) {
        noppes.npcs.scripted.event.PlayerEvent.InitEvent event = new noppes.npcs.scripted.event.PlayerEvent.InitEvent(handler.getPlayer());
        handler.runScript(EnumScriptType.INIT, event);
        WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static void onPlayerTick(PlayerScriptData handler) {
        noppes.npcs.scripted.event.PlayerEvent.UpdateEvent event = new noppes.npcs.scripted.event.PlayerEvent.UpdateEvent(handler.getPlayer());
        handler.runScript(EnumScriptType.TICK, event);
        WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onPlayerInteract(PlayerScriptData handler, noppes.npcs.scripted.event.PlayerEvent.InteractEvent event) {
        handler.runScript(EnumScriptType.INTERACT, event);
        return WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onPlayerAttack(PlayerScriptData handler, AttackEvent event) {
        handler.runScript(EnumScriptType.ATTACK, event);
        return WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onPlayerBreak(PlayerScriptData handler, noppes.npcs.scripted.event.PlayerEvent.BreakEvent event) {
        handler.runScript(EnumScriptType.BROKEN, event);
        return WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onPlayerToss(PlayerScriptData handler, EntityItem entityItem) {
        TossEvent event = new TossEvent(handler.getPlayer(), NpcAPI.Instance().getIItemStack(entityItem.func_92059_d()));
        handler.runScript(EnumScriptType.TOSS, event);
        return WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static void onPlayerLevelUp(PlayerScriptData handler, int change) {
        LevelUpEvent event = new LevelUpEvent(handler.getPlayer(), change);
        handler.runScript(EnumScriptType.LEVEL_UP, event);
        WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onPlayerPickUp(PlayerScriptData handler, EntityItem entityItem) {
        PickUpEvent event = new PickUpEvent(handler.getPlayer(), NpcAPI.Instance().getIItemStack(entityItem.func_92059_d()));
        handler.runScript(EnumScriptType.PICKUP, event);
        return WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static void onPlayerContainerOpen(PlayerScriptData handler, Container container) {
        ContainerOpen event = new ContainerOpen(handler.getPlayer(), NpcAPI.Instance().getIContainer(container));
        handler.runScript(EnumScriptType.CONTAINER_OPEN, event);
        WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static void onPlayerContainerClose(PlayerScriptData handler, Container container) {
        ContainerClosed event = new ContainerClosed(handler.getPlayer(), NpcAPI.Instance().getIContainer(container));
        handler.runScript(EnumScriptType.CONTAINER_CLOSED, event);
        WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static void onPlayerDeath(PlayerScriptData handler, DamageSource source, Entity entity) {
        noppes.npcs.scripted.event.PlayerEvent.DiedEvent event = new noppes.npcs.scripted.event.PlayerEvent.DiedEvent(handler.getPlayer(), source, entity);
        handler.runScript(EnumScriptType.KILLED, event);
        WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static void onPlayerKills(PlayerScriptData handler, EntityLivingBase entityLiving) {
        noppes.npcs.scripted.event.PlayerEvent.KilledEntityEvent event = new noppes.npcs.scripted.event.PlayerEvent.KilledEntityEvent(handler.getPlayer(), entityLiving);
        handler.runScript(EnumScriptType.KILLS, event);
        WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onPlayerDamaged(PlayerScriptData handler, noppes.npcs.scripted.event.PlayerEvent.DamagedEvent event) {
        handler.runScript(EnumScriptType.DAMAGED, event);
        return WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static void onPlayerLogin(PlayerScriptData handler) {
        LoginEvent event = new LoginEvent(handler.getPlayer());
        handler.runScript(EnumScriptType.LOGIN, event);
        WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static void onPlayerLogout(PlayerScriptData handler) {
        LogoutEvent event = new LogoutEvent(handler.getPlayer());
        handler.runScript(EnumScriptType.LOGOUT, event);
        WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static void onPlayerChat(PlayerScriptData handler, ChatEvent event) {
        handler.runScript(EnumScriptType.CHAT, event);
        WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onPlayerRanged(PlayerScriptData handler, noppes.npcs.scripted.event.PlayerEvent.RangedLaunchedEvent event) {
        handler.runScript(EnumScriptType.RANGED_LAUNCHED, event);
        return WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onPlayerDamagedEntity(PlayerScriptData handler, DamagedEntityEvent event) {
        handler.runScript(EnumScriptType.DAMAGED_ENTITY, event);
        return WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static void OnPlayerFactionChange(PlayerScriptData handler, FactionUpdateEvent event) {
        if(!handler.isClient()) {
            handler.runScript(EnumScriptType.FACTION_UPDATE, event);
            WrapperNpcAPI.EVENT_BUS.post(event);
        }
    }

    public static void onPlayerKeyPressed(EntityPlayerMP player, int button, boolean isCtrlPressed, boolean isShiftPressed, boolean isAltPressed, boolean isMetaPressed) {
        PlayerScriptData handler = PlayerData.get(player).scriptData;
        KeyPressedEvent event = new KeyPressedEvent(handler.getPlayer(), button, isCtrlPressed, isAltPressed, isShiftPressed, isMetaPressed);
        handler.runScript(EnumScriptType.KEY_PRESSED, event);
        WrapperNpcAPI.EVENT_BUS.post(event);
    }
}
