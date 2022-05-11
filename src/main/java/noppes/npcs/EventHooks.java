//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package noppes.npcs;

import cpw.mods.fml.common.eventhandler.Event;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.world.WorldEvent;
import noppes.npcs.controllers.*;
import noppes.npcs.controllers.data.ForgeDataScript;
import noppes.npcs.controllers.data.PlayerDataScript;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.scripted.*;
import noppes.npcs.scripted.entity.ScriptEntity;
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
import noppes.npcs.scripted.interfaces.*;
import noppes.npcs.scripted.item.ScriptCustomItem;
import noppes.npcs.scripted.item.ScriptItemStack;
import noppes.npcs.scripted.wrapper.WrapperNpcAPI;
import noppes.npcs.constants.EnumScriptType;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

public class EventHooks {
    public EventHooks() {
    }

    public static void onScriptItemInit(ScriptCustomItem handler) {
        if (!handler.isClient()) {
            ItemEvent.InitEvent event = new ItemEvent.InitEvent(handler);
            handler.callScript(EnumScriptType.INIT, event);
            WrapperNpcAPI.EVENT_BUS.post(event);
        }
    }

    public static void onScriptItemUpdate(ScriptCustomItem handler, EntityLivingBase player) {
        if (!handler.isClient()) {
            ItemEvent.UpdateEvent event = new ItemEvent.UpdateEvent(handler, ScriptController.Instance.getScriptForEntity(player));
            handler.callScript(EnumScriptType.TICK, event);
            WrapperNpcAPI.EVENT_BUS.post(event);
        }
    }

    public static boolean onScriptItemTossed(ScriptCustomItem handler, EntityPlayer player, EntityItem entity) {
        ItemEvent.TossedEvent event = new ItemEvent.TossedEvent(handler, (ScriptPlayer)ScriptController.Instance.getScriptForEntity(player), ScriptController.Instance.getScriptForEntity(entity));
        handler.callScript(EnumScriptType.TOSSED, event);
        return WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onScriptItemPickedUp(ScriptCustomItem handler, EntityPlayer player) {
        ItemEvent.PickedUpEvent event = new ItemEvent.PickedUpEvent(handler, (ScriptPlayer)ScriptController.Instance.getScriptForEntity(player));
        handler.callScript(EnumScriptType.PICKEDUP, event);
        return WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onScriptItemSpawn(ScriptCustomItem handler, EntityItem entity) {
        ItemEvent.SpawnEvent event = new ItemEvent.SpawnEvent(handler, ScriptController.Instance.getScriptForEntity(entity));
        handler.callScript(EnumScriptType.SPAWN, event);
        return WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onScriptItemInteract(ScriptCustomItem handler, noppes.npcs.scripted.event.ItemEvent.InteractEvent event) {
        handler.callScript(EnumScriptType.INTERACT, event);
        return WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onScriptItemAttack(ScriptCustomItem handler, noppes.npcs.scripted.event.ItemEvent.AttackEvent event) {
        handler.callScript(EnumScriptType.ATTACK, event);
        return WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onStartUsingCustomItem(ScriptCustomItem handler, IPlayer player, int duration) {
        ItemEvent.StartUsingItem event = new ItemEvent.StartUsingItem(handler, player, duration);
        handler.callScript(EnumScriptType.START_USING_ITEM, event);
        return WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onUsingCustomItem(ScriptCustomItem handler, IPlayer player, int duration) {
        ItemEvent.UsingItem event = new ItemEvent.UsingItem(handler, player, duration);
        handler.callScript(EnumScriptType.USING_ITEM, event);
        return WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onStopUsingCustomItem(ScriptCustomItem handler, IPlayer player, int duration) {
        ItemEvent.StopUsingItem event = new ItemEvent.StopUsingItem(handler, player, duration);
        handler.callScript(EnumScriptType.STOP_USING_ITEM, event);
        return WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onFinishUsingCustomItem(ScriptCustomItem handler, IPlayer player, int duration) {
        ItemEvent.FinishUsingItem event = new ItemEvent.FinishUsingItem(handler, player, duration);
        handler.callScript(EnumScriptType.FINISH_USING_ITEM, event);
        return WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static void onNPCInit(EntityNPCInterface npc) {
        if(npc == null || npc.wrappedNPC == null)
            return;

        noppes.npcs.scripted.event.NpcEvent.InitEvent event = new noppes.npcs.scripted.event.NpcEvent.InitEvent(npc.wrappedNPC);
        ScriptController.Instance.npcScripts.callScript(EnumScriptType.INIT, event);
        npc.script.callScript(EnumScriptType.INIT);
        WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static void onNPCUpdate(EntityNPCInterface npc) {
        if(npc == null || npc.wrappedNPC == null)
            return;

        noppes.npcs.scripted.event.NpcEvent.UpdateEvent event = new noppes.npcs.scripted.event.NpcEvent.UpdateEvent(npc.wrappedNPC);
        ScriptController.Instance.npcScripts.callScript(EnumScriptType.TICK, event);
        npc.script.callScript(EnumScriptType.TICK);
        WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static void onNPCDialog(EntityNPCInterface npc, EntityPlayer player, int dialogId, int optionId, Dialog dialog) {
        if(npc == null || npc.wrappedNPC == null)
            return;

        NpcEvent.DialogEvent event = new NpcEvent.DialogEvent(npc.wrappedNPC, player, dialogId, optionId, dialog);
        ScriptController.Instance.npcScripts.callScript(EnumScriptType.DIALOG, event);

        ScriptEventDialog npcEvent = new ScriptEventDialog(player,dialogId,optionId,dialog);
        npc.script.callScript(EnumScriptType.DIALOG, "event", npcEvent, "player", npcEvent.getPlayer(), "dialog", npcEvent.getDialogId(), "option", npcEvent.getOptionId(), "dialogObj", npcEvent.getDialog());

        WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static void onNPCDialogClosed(EntityNPCInterface npc, EntityPlayer player, int dialogId, int optionId, Dialog dialog) {
        if(npc == null || npc.wrappedNPC == null)
            return;

        NpcEvent.DialogClosedEvent event = new NpcEvent.DialogClosedEvent(npc.wrappedNPC, player, dialogId, optionId, dialog);
        ScriptController.Instance.npcScripts.callScript(EnumScriptType.DIALOG_CLOSE, event);

        ScriptEventDialog npcEvent = new ScriptEventDialog(player,dialogId,optionId,dialog);
        npc.script.callScript(EnumScriptType.DIALOG_CLOSE, "event", npcEvent, "player", npcEvent.getPlayer(), "dialog", npcEvent.getDialogId(), "option", npcEvent.getOptionId(), "dialogObj", npcEvent.getDialog());

        WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onNPCInteract(EntityNPCInterface npc, EntityPlayer player) {
        if(npc == null || npc.wrappedNPC == null)
            return false;

        NpcEvent.InteractEvent event = new NpcEvent.InteractEvent(npc.wrappedNPC,player);
        ScriptController.Instance.npcScripts.callScript(EnumScriptType.INTERACT, event);
        boolean result = npc.script.callScript(EnumScriptType.INTERACT, "player", player);
        WrapperNpcAPI.EVENT_BUS.post(event);
        return result;
    }

    public static boolean onNPCMeleeAttack(EntityNPCInterface npc, float damage, EntityLivingBase target){
        if(npc == null || npc.wrappedNPC == null)
            return false;

        NpcEvent.MeleeAttackEvent event = new NpcEvent.MeleeAttackEvent(npc.wrappedNPC,damage,target);
        ScriptController.Instance.npcScripts.callScript(EnumScriptType.ATTACK_MELEE, event);
        boolean result = npc.script.callScript(EnumScriptType.ATTACK, "event", new ScriptEventAttack(damage,target,false), "target", target);
        WrapperNpcAPI.EVENT_BUS.post(event);
        return result;
    }

    public static boolean onNPCRangedAttack(EntityNPCInterface npc, float damage, EntityLivingBase target){
        if(npc == null || npc.wrappedNPC == null)
            return false;

        NpcEvent.RangedLaunchedEvent event = new NpcEvent.RangedLaunchedEvent(npc.wrappedNPC,damage,target);
        ScriptController.Instance.npcScripts.callScript(EnumScriptType.RANGED_LAUNCHED, event);
        boolean result = npc.script.callScript(EnumScriptType.ATTACK, "event", new ScriptEventAttack(damage,target,true), "target", target);
        WrapperNpcAPI.EVENT_BUS.post(event);
        return result;
    }

    public static void onNPCKilledEntity(EntityNPCInterface npc, EntityLivingBase entity){
        if(npc == null || npc.wrappedNPC == null)
            return;

        NpcEvent.KilledEntityEvent event = new NpcEvent.KilledEntityEvent(npc.wrappedNPC,entity);
        ScriptController.Instance.npcScripts.callScript(EnumScriptType.KILLS, event);
        npc.script.callScript(EnumScriptType.KILLS, "target", entity);
        WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onNPCTarget(EntityNPCInterface npc, EntityLivingBase target){
        if(npc == null || npc.wrappedNPC == null)
            return false;

        NpcEvent.TargetEvent event = new NpcEvent.TargetEvent(npc.wrappedNPC,target);
        ScriptController.Instance.npcScripts.callScript(EnumScriptType.TARGET, event);
        boolean result = npc.script.callScript(EnumScriptType.TARGET, "event", new ScriptEventTarget(target));
        WrapperNpcAPI.EVENT_BUS.post(event);
        return result;
    }

    public static void onNPCCollide(EntityNPCInterface npc, Entity entity){
        if(npc == null || npc.wrappedNPC == null)
            return;

        NpcEvent.CollideEvent event = new NpcEvent.CollideEvent(npc.wrappedNPC,entity);
        ScriptController.Instance.npcScripts.callScript(EnumScriptType.COLLIDE, event);
        npc.script.callScript(EnumScriptType.COLLIDE, "entity", entity);
        WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onNPCDamaged(EntityNPCInterface npc, Entity source, float damage, DamageSource damagesource, ScriptEventDamaged damagedEvent){
        if(npc == null || npc.wrappedNPC == null)
            return false;

        NpcEvent.DamagedEvent event = new NpcEvent.DamagedEvent(npc.wrappedNPC,source,damage,damagesource);
        ScriptController.Instance.npcScripts.callScript(EnumScriptType.DAMAGED, event);
        boolean result = npc.script.callScript(EnumScriptType.DAMAGED, "event", damagedEvent);
        WrapperNpcAPI.EVENT_BUS.post(event);
        return result;
    }

    public static boolean onNPCKilled(EntityNPCInterface npc, DamageSource damagesource, Entity entity, ScriptEventKilled killedEvent){
        if(npc == null || npc.wrappedNPC == null)
            return false;

        NpcEvent.DiedEvent event = new NpcEvent.DiedEvent(npc.wrappedNPC,damagesource,entity);
        ScriptController.Instance.npcScripts.callScript(EnumScriptType.KILLED, event);
        boolean result = npc.script.callScript(EnumScriptType.KILLED, "event", killedEvent);
        WrapperNpcAPI.EVENT_BUS.post(event);
        return result;
    }

    public static void onNPCTimer(EntityNPCInterface npc, int id) {
        if(npc == null || npc.wrappedNPC == null)
            return;

        NpcEvent.TimerEvent event = new NpcEvent.TimerEvent(npc.wrappedNPC, id);
        ScriptEventTimer scriptEvent = new ScriptEventTimer(id);
        npc.script.callScript(EnumScriptType.TIMER, "event", scriptEvent);
        ScriptController.Instance.npcScripts.callScript(EnumScriptType.TIMER, event);
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

    public static void onPlayerKeyPressed(EntityPlayerMP player, int button, boolean isCtrlPressed, boolean isShiftPressed, boolean isAltPressed, boolean isMetaPressed, boolean buttonDown, int[] heldKeys) {
        PlayerDataScript handler = ScriptController.Instance.playerScripts;
        KeyPressedEvent event = new KeyPressedEvent((ScriptPlayer)ScriptController.Instance.getScriptForEntity(player), button, isCtrlPressed, isAltPressed, isShiftPressed, isMetaPressed, buttonDown, heldKeys);
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
        IEntity e = ScriptController.Instance.getScriptForEntity(event.entity);
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

    public static void onCustomGuiButton(ScriptPlayer player, ICustomGui gui, int buttonId) {
        CustomGuiEvent.ButtonEvent event = new CustomGuiEvent.ButtonEvent(player, gui, buttonId);
        CustomGuiController.onButton(event);
    }

    public static void onCustomGuiSlot(ScriptPlayer player, ICustomGui gui, int slotId) {
        CustomGuiEvent.SlotEvent event = new CustomGuiEvent.SlotEvent(player, gui, slotId, player.getOpenContainer().getSlot(slotId));
        CustomGuiController.onSlotChange(event);
    }

    public static void onCustomGuiUnfocused(ScriptPlayer player, ICustomGui gui, int textfieldId) {
        CustomGuiEvent.UnfocusedEvent event = new CustomGuiEvent.UnfocusedEvent(player, gui, textfieldId);
        CustomGuiController.onCustomGuiUnfocused(event);
    }

    public static void onCustomGuiScrollClick(ScriptPlayer player, ICustomGui gui, int scrollId, int scrollIndex, String[] selection, boolean doubleClick) {
        CustomGuiEvent.ScrollEvent event = new CustomGuiEvent.ScrollEvent(player, gui, scrollId, scrollIndex, selection, doubleClick);
        CustomGuiController.onScrollClick(event);
    }

    public static void onCustomGuiClose(ScriptPlayer player, ICustomGui gui) {
        noppes.npcs.scripted.event.CustomGuiEvent.CloseEvent event = new noppes.npcs.scripted.event.CustomGuiEvent.CloseEvent(player, gui);
        CustomGuiController.onClose(event);
    }

    public static void onQuestFinished(EntityPlayer player, Quest quest){
        PlayerDataScript handler = ScriptController.Instance.playerScripts;
        QuestEvent.QuestCompletedEvent event = new QuestEvent.QuestCompletedEvent(new ScriptPlayer((EntityPlayerMP) player), quest);
        handler.callScript(EnumScriptType.QUEST_COMPLETED, event);
        WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onQuestStarted(EntityPlayer player, Quest quest){
        PlayerDataScript handler = ScriptController.Instance.playerScripts;
        QuestEvent.QuestStartEvent event = new QuestEvent.QuestStartEvent(new ScriptPlayer((EntityPlayerMP) player), quest);
        handler.callScript(EnumScriptType.QUEST_START, event);
        return WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static void onQuestTurnedIn(QuestEvent.QuestTurnedInEvent event){
        PlayerDataScript handler = ScriptController.Instance.playerScripts;
        handler.callScript(EnumScriptType.QUEST_TURNIN, event);
        WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static void onFactionPoints(FactionEvent.FactionPoints event){
        PlayerDataScript handler = ScriptController.Instance.playerScripts;
        handler.callScript(EnumScriptType.FACTION_POINTS, event);
        WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static void onDialogOpen(DialogEvent.DialogOpen event){
        PlayerDataScript handler = ScriptController.Instance.playerScripts;
        handler.callScript(EnumScriptType.DIALOG_OPEN, event);
        WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static void onDialogOption(DialogEvent.DialogOption event){
        PlayerDataScript handler = ScriptController.Instance.playerScripts;
        handler.callScript(EnumScriptType.DIALOG_OPTION, event);
        WrapperNpcAPI.EVENT_BUS.post(event);
    }

    public static void onDialogClosed(DialogEvent.DialogClosed event){
        PlayerDataScript handler = ScriptController.Instance.playerScripts;
        handler.callScript(EnumScriptType.DIALOG_CLOSE, event);
        WrapperNpcAPI.EVENT_BUS.post(event);
    }
}