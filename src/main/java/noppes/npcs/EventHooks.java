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
import noppes.npcs.scripted.entity.ScriptPlayer;
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

    public static void onPlayerInit(PlayerDataScript handler, ScriptPlayer player) {
        noppes.npcs.scripted.event.PlayerEvent.InitEvent event = new noppes.npcs.scripted.event.PlayerEvent.InitEvent(player);
        handler.callScript(EnumScriptType.INIT, event);
        WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static void onPlayerTick(PlayerDataScript handler, ScriptPlayer player) {
        noppes.npcs.scripted.event.PlayerEvent.UpdateEvent event = new noppes.npcs.scripted.event.PlayerEvent.UpdateEvent(player);
        handler.callScript(EnumScriptType.TICK, event);
        WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onPlayerInteract(PlayerDataScript handler, noppes.npcs.scripted.event.PlayerEvent.InteractEvent event) {
        handler.callScript(EnumScriptType.INTERACT, event);
        return WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onStartUsingItem(PlayerDataScript handler, ScriptPlayer player, int duration, ItemStack item){
        PlayerEvent.StartUsingItem event = new PlayerEvent.StartUsingItem(player, item, duration);
        handler.callScript(EnumScriptType.START_USING_ITEM, event);
        return WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onUsingItem(PlayerDataScript handler, ScriptPlayer player, int duration, ItemStack item){
        PlayerEvent.UsingItem event = new PlayerEvent.UsingItem(player, item, duration);
        handler.callScript(EnumScriptType.USING_ITEM, event);
        return WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onStopUsingItem(PlayerDataScript handler, ScriptPlayer player, int duration, ItemStack item){
        PlayerEvent.StopUsingItem event = new PlayerEvent.StopUsingItem(player, item, duration);
        handler.callScript(EnumScriptType.STOP_USING_ITEM, event);
        return WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static void onFinishUsingItem(PlayerDataScript handler, ScriptPlayer player, int duration, ItemStack item){
        PlayerEvent.FinishUsingItem event = new PlayerEvent.FinishUsingItem(player, item, duration);
        handler.callScript(EnumScriptType.FINISH_USING_ITEM, event);
        WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onPlayerDropItems(PlayerDataScript handler, ScriptPlayer player, ArrayList<EntityItem> entityItems) {
        IItemStack[] items = new IItemStack[entityItems.size()];
        for(int i = 0; i < entityItems.size(); i++){ items[i] = NpcAPI.Instance().getIItemStack(entityItems.get(i).getEntityItem()); }

        DropEvent event = new DropEvent(player, items);
        handler.callScript(EnumScriptType.DROP, event);
        return WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static void onPlayerPickupXP(PlayerDataScript handler, ScriptPlayer player, EntityXPOrb orb) {
        PlayerEvent.PickupXPEvent event = new PlayerEvent.PickupXPEvent(player, orb);
        handler.callScript(EnumScriptType.PICKUP_XP, event);
        WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onPlayerToss(PlayerDataScript handler, ScriptPlayer player, EntityItem entityItem) {
        PlayerEvent.TossEvent event = new PlayerEvent.TossEvent(player, NpcAPI.Instance().getIItemStack(entityItem.getEntityItem()));
        handler.callScript(EnumScriptType.TOSS, event);
        return WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onPlayerPickUp(PlayerDataScript handler, ScriptPlayer player, EntityItem entityItem) {
        PickUpEvent event = new PickUpEvent(player, new ScriptItemStack(entityItem.getEntityItem()));//NpcAPI.Instance().getIItemStack(entityItem.getEntityItem()));
        handler.callScript(EnumScriptType.PICKUP, event);
        return WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static void onPlayerContainerOpen(PlayerDataScript handler, ScriptPlayer player, Container container) {
        ContainerOpen event = new ContainerOpen(player, NpcAPI.Instance().getIContainer(container));
        handler.callScript(EnumScriptType.CONTAINER_OPEN, event);
        WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static void onPlayerUseHoe(PlayerDataScript handler, ScriptPlayer player, ItemStack hoe, int x, int y, int z) {
        noppes.npcs.scripted.event.PlayerEvent.UseHoe event = new noppes.npcs.scripted.event.PlayerEvent.UseHoe(player, hoe, x, y, z);
        handler.callScript(EnumScriptType.USE_HOE, event);
        WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static void onPlayerSleep(PlayerDataScript handler, ScriptPlayer player, int x, int y, int z) {
        noppes.npcs.scripted.event.PlayerEvent.Sleep event = new noppes.npcs.scripted.event.PlayerEvent.Sleep(player, x, y, z);
        handler.callScript(EnumScriptType.SLEEP, event);
        WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static void onPlayerWakeUp(PlayerDataScript handler, ScriptPlayer player, boolean setSpawn) {
        noppes.npcs.scripted.event.PlayerEvent.WakeUp event = new noppes.npcs.scripted.event.PlayerEvent.WakeUp(player, setSpawn);
        handler.callScript(EnumScriptType.WAKE_UP, event);
        WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static void onPlayerDeath(PlayerDataScript handler, ScriptPlayer player, DamageSource source, Entity entity) {
        noppes.npcs.scripted.event.PlayerEvent.DiedEvent event = new noppes.npcs.scripted.event.PlayerEvent.DiedEvent(player, source, entity);
        handler.callScript(EnumScriptType.KILLED, event);
        WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static void onPlayerKills(PlayerDataScript handler, ScriptPlayer player, EntityLivingBase entityLiving) {
        noppes.npcs.scripted.event.PlayerEvent.KilledEntityEvent event = new noppes.npcs.scripted.event.PlayerEvent.KilledEntityEvent(player, entityLiving);
        handler.callScript(EnumScriptType.KILLS, event);
        WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onPlayerDamaged(PlayerDataScript handler, noppes.npcs.scripted.event.PlayerEvent.DamagedEvent event) {
        handler.callScript(EnumScriptType.DAMAGED, event);
        return WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onPlayerLightning(PlayerDataScript handler, ScriptPlayer player) {
        PlayerEvent.LightningEvent event = new PlayerEvent.LightningEvent(player);
        handler.callScript(EnumScriptType.LIGHTNING, event);
        return WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onPlayerSound(PlayerDataScript handler, ScriptPlayer player, String name, float pitch, float volume) {
        PlayerEvent.SoundEvent event = new PlayerEvent.SoundEvent(player, name, pitch, volume);
        handler.callScript(EnumScriptType.PLAYSOUND, event);
        return WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static void onPlayerFall(PlayerDataScript handler, ScriptPlayer player, float distance) {
        PlayerEvent.FallEvent event = new PlayerEvent.FallEvent(player,distance);
        handler.callScript(EnumScriptType.FALL, event);
        WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static void onPlayerJump(PlayerDataScript handler, ScriptPlayer player) {
        PlayerEvent.JumpEvent event = new PlayerEvent.JumpEvent(player);
        handler.callScript(EnumScriptType.JUMP, event);
        WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static void onPlayerLogin(PlayerDataScript handler, ScriptPlayer player) {
        LoginEvent event = new LoginEvent(player);
        handler.callScript(EnumScriptType.LOGIN, event);
        WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static void onPlayerAchievement(PlayerDataScript handler, ScriptPlayer player, String description) {
        PlayerEvent.Achievement event = new PlayerEvent.Achievement(player, description);
        handler.callScript(EnumScriptType.ACHIEVEMENT, event);
        WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static void onPlayerFillBucket(PlayerDataScript handler, ScriptPlayer player, ItemStack current, ItemStack result) {
        PlayerEvent.FillBucket event = new PlayerEvent.FillBucket(player, current, result);
        handler.callScript(EnumScriptType.FILL_BUCKET, event);
        WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static void onPlayerBonemeal(PlayerDataScript handler, ScriptPlayer player, int x, int y, int z, World world) {
        PlayerEvent.Bonemeal event = new PlayerEvent.Bonemeal(player, x, y, z, world);
        handler.callScript(EnumScriptType.BONEMEAL, event);
        WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static void onPlayerRespawn(PlayerDataScript handler, ScriptPlayer player) {
        PlayerEvent.RespawnEvent event = new PlayerEvent.RespawnEvent(player);
        handler.callScript(EnumScriptType.RESPAWN, event);
        WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static void onPlayerLogout(PlayerDataScript handler, ScriptPlayer player) {
        LogoutEvent event = new LogoutEvent(player);
        handler.callScript(EnumScriptType.LOGOUT, event);
        WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static void onPlayerChat(PlayerDataScript handler, ChatEvent event) {
        handler.callScript(EnumScriptType.CHAT, event);
        WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onPlayerBowCharge(PlayerDataScript handler, noppes.npcs.scripted.event.PlayerEvent.RangedChargeEvent event) {
        handler.callScript(EnumScriptType.RANGED_CHARGE, event);
        return WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onPlayerRanged(PlayerDataScript handler, noppes.npcs.scripted.event.PlayerEvent.RangedLaunchedEvent event) {
        handler.callScript(EnumScriptType.RANGED_LAUNCHED, event);
        return WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onPlayerDamagedEntity(PlayerDataScript handler, DamagedEntityEvent event) {
        handler.callScript(EnumScriptType.DAMAGED_ENTITY, event);
        return WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static void onPlayerChangeDim(PlayerDataScript handler, ScriptPlayer player, int fromDim, int toDim) {
        PlayerEvent.ChangedDimension event = new PlayerEvent.ChangedDimension(player, fromDim, toDim);
        handler.callScript(EnumScriptType.CHANGED_DIM, event);
        WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static void onPlayerMouseClicked(EntityPlayerMP player, int button, int mouseWheel, boolean buttonDown) {
        PlayerDataScript handler = ScriptController.Instance.playerScripts;
        PlayerEvent.MouseClickedEvent event = new PlayerEvent.MouseClickedEvent((ScriptPlayer)ScriptController.Instance.getScriptForEntity(player), button, mouseWheel, buttonDown);
        handler.callScript(EnumScriptType.MOUSE_CLICKED, event);
        WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static void onPlayerKeyPressed(EntityPlayerMP player, int button, boolean isCtrlPressed, boolean isShiftPressed, boolean isAltPressed, boolean isMetaPressed, boolean buttonDown) {
        PlayerDataScript handler = ScriptController.Instance.playerScripts;
        KeyPressedEvent event = new KeyPressedEvent((ScriptPlayer)ScriptController.Instance.getScriptForEntity(player), button, isCtrlPressed, isAltPressed, isShiftPressed, isMetaPressed, buttonDown);
        handler.callScript(EnumScriptType.KEY_PRESSED, event);
        WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static void onPlayerTimer(PlayerData data, int id) {
        PlayerDataScript handler = ScriptController.Instance.playerScripts;
        noppes.npcs.scripted.event.PlayerEvent.TimerEvent event = new noppes.npcs.scripted.event.PlayerEvent.TimerEvent((ScriptPlayer)ScriptController.Instance.getScriptForEntity(data.player), id);
        handler.callScript(EnumScriptType.TIMER, event);
        WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onPlayerBreak(PlayerDataScript handler, PlayerEvent.BreakEvent event) {
        handler.callScript(EnumScriptType.BREAK_BLOCK, event);
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