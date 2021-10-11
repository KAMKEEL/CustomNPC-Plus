//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package noppes.npcs;

import cpw.mods.fml.common.eventhandler.Event;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.util.DamageSource;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.world.WorldEvent;
import noppes.npcs.controllers.IScriptHandler;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.controllers.data.ForgeDataScript;
import noppes.npcs.controllers.data.PlayerDataScript;
import noppes.npcs.scripted.IEntity;
import noppes.npcs.scripted.IWorld;
import noppes.npcs.scripted.NpcAPI;
import noppes.npcs.scripted.ScriptPlayer;
import noppes.npcs.scripted.event.ForgeEvent;
import noppes.npcs.scripted.event.PlayerEvent.AttackEvent;
import noppes.npcs.scripted.event.PlayerEvent.ChatEvent;
import noppes.npcs.scripted.event.PlayerEvent.ContainerClosed;
import noppes.npcs.scripted.event.PlayerEvent.ContainerOpen;
import noppes.npcs.scripted.event.PlayerEvent.DamagedEntityEvent;
import noppes.npcs.scripted.event.PlayerEvent.KeyPressedEvent;
import noppes.npcs.scripted.event.PlayerEvent.LevelUpEvent;
import noppes.npcs.scripted.event.PlayerEvent.LoginEvent;
import noppes.npcs.scripted.event.PlayerEvent.LogoutEvent;
import noppes.npcs.scripted.event.PlayerEvent.PickUpEvent;
import noppes.npcs.scripted.event.PlayerEvent.TossEvent;
import noppes.npcs.scripted.wrapper.WrapperNpcAPI;
import noppes.npcs.constants.EnumScriptType;
import noppes.npcs.controllers.PlayerData;
import org.apache.commons.lang3.StringUtils;

public class EventHooks {
    public EventHooks() {
    }

    public static void onForgeInit(IScriptHandler handler) {
        noppes.npcs.scripted.event.ForgeEvent.InitEvent event = new noppes.npcs.scripted.event.ForgeEvent.InitEvent();
        handler.callScript(EnumScriptType.INIT, event);
        WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static void onPlayerInit(PlayerDataScript handler) {
        noppes.npcs.scripted.event.PlayerEvent.InitEvent event = new noppes.npcs.scripted.event.PlayerEvent.InitEvent(handler.getPlayer());
        handler.callScript(EnumScriptType.INIT, event);
        WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static void onPlayerTick(PlayerDataScript handler) {
        noppes.npcs.scripted.event.PlayerEvent.UpdateEvent event = new noppes.npcs.scripted.event.PlayerEvent.UpdateEvent(handler.getPlayer());
        handler.callScript(EnumScriptType.TICK, event);
        WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onPlayerInteract(PlayerDataScript handler, noppes.npcs.scripted.event.PlayerEvent.InteractEvent event) {
        handler.callScript(EnumScriptType.INTERACT, event);
        return WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onPlayerAttack(PlayerDataScript handler, AttackEvent event) {
        handler.callScript(EnumScriptType.ATTACK, event);
        return WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onPlayerToss(PlayerDataScript handler, EntityItem entityItem) {
        TossEvent event = new TossEvent(handler.getPlayer(), NpcAPI.Instance().getIItemStack(entityItem.getEntityItem()));
        handler.callScript(EnumScriptType.TOSS, event);
        return WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static void onPlayerLevelUp(PlayerDataScript handler, int change) {
        LevelUpEvent event = new LevelUpEvent(handler.getPlayer(), change);
        handler.callScript(EnumScriptType.LEVEL_UP, event);
        WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onPlayerPickUp(PlayerDataScript handler, EntityItem entityItem) {
        PickUpEvent event = new PickUpEvent(handler.getPlayer(), NpcAPI.Instance().getIItemStack(entityItem.getEntityItem()));
        handler.callScript(EnumScriptType.PICKUP, event);
        return WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static void onPlayerContainerOpen(PlayerDataScript handler, Container container) {
        ContainerOpen event = new ContainerOpen(handler.getPlayer(), NpcAPI.Instance().getIContainer(container));
        handler.callScript(EnumScriptType.CONTAINER_OPEN, event);
        WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static void onPlayerContainerClose(PlayerDataScript handler, Container container) {
        ContainerClosed event = new ContainerClosed(handler.getPlayer(), NpcAPI.Instance().getIContainer(container));
        handler.callScript(EnumScriptType.CONTAINER_CLOSED, event);
        WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static void onPlayerDeath(PlayerDataScript handler, DamageSource source, Entity entity) {
        noppes.npcs.scripted.event.PlayerEvent.DiedEvent event = new noppes.npcs.scripted.event.PlayerEvent.DiedEvent(handler.getPlayer(), source, entity);
        handler.callScript(EnumScriptType.KILLED, event);
        WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static void onPlayerKills(PlayerDataScript handler, EntityLivingBase entityLiving) {
        noppes.npcs.scripted.event.PlayerEvent.KilledEntityEvent event = new noppes.npcs.scripted.event.PlayerEvent.KilledEntityEvent(handler.getPlayer(), entityLiving);
        handler.callScript(EnumScriptType.KILLS, event);
        WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onPlayerDamaged(PlayerDataScript handler, noppes.npcs.scripted.event.PlayerEvent.DamagedEvent event) {
        handler.callScript(EnumScriptType.DAMAGED, event);
        return WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static void onPlayerLogin(PlayerDataScript handler) {
        LoginEvent event = new LoginEvent(handler.getPlayer());
        handler.callScript(EnumScriptType.LOGIN, event);
        WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static void onPlayerLogout(PlayerDataScript handler) {
        LogoutEvent event = new LogoutEvent(handler.getPlayer());
        handler.callScript(EnumScriptType.LOGOUT, event);
        WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static void onPlayerChat(PlayerDataScript handler, ChatEvent event) {
        handler.callScript(EnumScriptType.CHAT, event, "message", event.message);
        WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onPlayerRanged(PlayerDataScript handler, noppes.npcs.scripted.event.PlayerEvent.RangedLaunchedEvent event) {
        handler.callScript(EnumScriptType.RANGED_LAUNCHED, event);
        return WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onPlayerDamagedEntity(PlayerDataScript handler, DamagedEntityEvent event) {
        handler.callScript(EnumScriptType.DAMAGED_ENTITY, event);
        return WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static void onPlayerKeyPressed(EntityPlayerMP player, int button, boolean isCtrlPressed, boolean isShiftPressed, boolean isAltPressed, boolean isMetaPressed) {
        PlayerDataScript handler = PlayerData.get(player).scriptData;
        KeyPressedEvent event = new KeyPressedEvent(handler.getPlayer(), button, isCtrlPressed, isAltPressed, isShiftPressed, isMetaPressed);
        handler.callScript(EnumScriptType.KEY_PRESSED, event, "key", button, "isCtrlPressed", isCtrlPressed, "isAltPressed", isAltPressed, "isShiftPressed", isShiftPressed, "isMetaPressed", isMetaPressed);
        WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static void onPlayerTimer(PlayerData data, int id) {
        PlayerDataScript handler = data.scriptData;
        noppes.npcs.scripted.event.PlayerEvent.TimerEvent event = new noppes.npcs.scripted.event.PlayerEvent.TimerEvent(handler.getPlayer(), id);
        handler.callScript(EnumScriptType.TIMER, event);
        WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static void onForgeEntityEvent(EntityEvent event) {
        IEntity e = NpcAPI.Instance().getIEntity(event.entity);
        onForgeEvent(new noppes.npcs.scripted.event.ForgeEvent.EntityEvent(event, e), event);
    }

    public static void onForgeEvent(ForgeEvent ev, Event event) {
        ForgeDataScript data = ScriptController.Instance.forgeScripts;
        if(data.isEnabled()) {
            String eventName = event.getClass().getName();
            int i = eventName.lastIndexOf(".");
            eventName = StringUtils.uncapitalize(eventName.substring(i + 1).replace("$", ""));
            if(event.isCancelable()) {
                ev.setCanceled(event.isCanceled());
            }

            data.callScript(eventName, event);
            WrapperNpcAPI.EVENT_BUS.post(ev);
            if(event.isCancelable()) {
                event.setCanceled(ev.isCanceled());
            }

        }
    }

    public static void onForgeWorldEvent(WorldEvent event) {
        if(ScriptController.Instance.forgeScripts.isEnabled()) {
            IWorld e = NpcAPI.Instance().getIWorld((WorldServer)event.world);
            onForgeEvent(new noppes.npcs.scripted.event.ForgeEvent.WorldEvent(event, e), event);
        }
    }
}
