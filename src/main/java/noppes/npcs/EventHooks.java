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
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.world.WorldEvent;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.entity.IProjectile;
import noppes.npcs.api.gui.ICustomGui;
import noppes.npcs.api.gui.IItemSlot;
import noppes.npcs.api.item.IItemCustom;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.constants.EnumScriptType;
import noppes.npcs.controllers.CustomGuiController;
import noppes.npcs.controllers.ScriptContainer;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.controllers.data.*;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.entity.EntityProjectile;
import noppes.npcs.scripted.NpcAPI;
import noppes.npcs.scripted.event.*;
import noppes.npcs.scripted.event.PlayerEvent.*;
import noppes.npcs.scripted.item.ScriptCustomItem;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

public class EventHooks {
    public EventHooks() {
    }

    public static void onScriptItemInit(IItemCustom handler) {
        if (!((ScriptCustomItem) handler).isClient()) {
            ItemEvent.InitEvent event = new ItemEvent.InitEvent(handler);
            ((ScriptCustomItem) handler).callScript(EnumScriptType.INIT, event);
            NpcAPI.EVENT_BUS.post(event);
        }
    }

    public static void onScriptItemUpdate(IItemCustom handler, EntityLivingBase player) {
        if (!((ScriptCustomItem) handler).isClient()) {
            ItemEvent.UpdateEvent event = new ItemEvent.UpdateEvent(handler, NpcAPI.Instance().getIEntity(player));
            ((ScriptCustomItem) handler).callScript(EnumScriptType.TICK, event);
            NpcAPI.EVENT_BUS.post(event);
        }
    }

    public static boolean onScriptItemTossed(IItemCustom handler, EntityPlayer player, EntityItem entity) {
        ItemEvent.TossedEvent event = new ItemEvent.TossedEvent(handler, (IPlayer)NpcAPI.Instance().getIEntity(player), NpcAPI.Instance().getIEntity(entity));
        ((ScriptCustomItem) handler).callScript(EnumScriptType.TOSSED, event);
        return NpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onScriptItemPickedUp(IItemCustom handler, EntityPlayer player) {
        ItemEvent.PickedUpEvent event = new ItemEvent.PickedUpEvent(handler, (IPlayer)NpcAPI.Instance().getIEntity(player));
        ((ScriptCustomItem) handler).callScript(EnumScriptType.PICKEDUP, event);
        return NpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onScriptItemSpawn(IItemCustom handler, EntityItem entity) {
        ItemEvent.SpawnEvent event = new ItemEvent.SpawnEvent(handler, NpcAPI.Instance().getIEntity(entity));
        ((ScriptCustomItem) handler).callScript(EnumScriptType.SPAWN, event);
        return NpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onScriptItemInteract(IItemCustom handler, noppes.npcs.scripted.event.ItemEvent.InteractEvent event) {
        ((ScriptCustomItem) handler).callScript(EnumScriptType.INTERACT, event);
        return NpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onScriptItemRightClick(IItemCustom handler, noppes.npcs.scripted.event.ItemEvent.RightClickEvent event) {
        ((ScriptCustomItem) handler).callScript(EnumScriptType.RIGHT_CLICK, event);
        return NpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onScriptItemAttack(IItemCustom handler, noppes.npcs.scripted.event.ItemEvent.AttackEvent event) {
        ((ScriptCustomItem) handler).callScript(EnumScriptType.ATTACK, event);
        return NpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onStartUsingCustomItem(IItemCustom handler, IPlayer player, int duration) {
        ItemEvent.StartUsingItem event = new ItemEvent.StartUsingItem(handler, player, duration);
        ((ScriptCustomItem) handler).callScript(EnumScriptType.START_USING_ITEM, event);
        return NpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onUsingCustomItem(IItemCustom handler, IPlayer player, int duration) {
        ItemEvent.UsingItem event = new ItemEvent.UsingItem(handler, player, duration);
        ((ScriptCustomItem) handler).callScript(EnumScriptType.USING_ITEM, event);
        return NpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onStopUsingCustomItem(IItemCustom handler, IPlayer player, int duration) {
        ItemEvent.StopUsingItem event = new ItemEvent.StopUsingItem(handler, player, duration);
        ((ScriptCustomItem) handler).callScript(EnumScriptType.STOP_USING_ITEM, event);
        return NpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onFinishUsingCustomItem(IItemCustom handler, IPlayer player, int duration) {
        ItemEvent.FinishUsingItem event = new ItemEvent.FinishUsingItem(handler, player, duration);
        ((ScriptCustomItem) handler).callScript(EnumScriptType.FINISH_USING_ITEM, event);
        return NpcAPI.EVENT_BUS.post(event);
    }

    public static void onNPCInit(EntityNPCInterface npc) {
        if(npc == null || npc.wrappedNPC == null)
            return;

        noppes.npcs.scripted.event.NpcEvent.InitEvent event = new noppes.npcs.scripted.event.NpcEvent.InitEvent(npc.wrappedNPC);
        ScriptController.Instance.globalNpcScripts.callScript(EnumScriptType.INIT, event);
        npc.script.callScript(EnumScriptType.INIT, event);
        NpcAPI.EVENT_BUS.post(event);
        npc.advanced.soulStoneInit = false;
    }

    public static void onNPCUpdate(EntityNPCInterface npc) {
        if(npc == null || npc.wrappedNPC == null)
            return;

        noppes.npcs.scripted.event.NpcEvent.UpdateEvent event = new noppes.npcs.scripted.event.NpcEvent.UpdateEvent(npc.wrappedNPC);
        ScriptController.Instance.globalNpcScripts.callScript(EnumScriptType.TICK, event);
        npc.script.callScript(EnumScriptType.TICK, event);
        NpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onNPCDialog(EntityNPCInterface npc, EntityPlayer player, int dialogId, int optionId, Dialog dialog) {
        if(npc == null || npc.wrappedNPC == null)
            return false;

        NpcEvent.DialogEvent event = new NpcEvent.DialogEvent(npc.wrappedNPC, player, dialogId, optionId, dialog);
        ScriptController.Instance.globalNpcScripts.callScript(EnumScriptType.DIALOG, event);
        npc.script.callScript(EnumScriptType.DIALOG, event, "player", event.getPlayer(), "dialog", event.getDialogId(), "option", event.getOptionId(), "dialogObj", event.getDialog());

        return NpcAPI.EVENT_BUS.post(event);
    }

    public static void onNPCDialogClosed(EntityNPCInterface npc, EntityPlayer player, int dialogId, int optionId, Dialog dialog) {
        if(npc == null || npc.wrappedNPC == null)
            return;

        NpcEvent.DialogClosedEvent event = new NpcEvent.DialogClosedEvent(npc.wrappedNPC, player, dialogId, optionId, dialog);
        ScriptController.Instance.globalNpcScripts.callScript(EnumScriptType.DIALOG_CLOSE, event);
        npc.script.callScript(EnumScriptType.DIALOG_CLOSE, event, "player", event.getPlayer(), "dialog", event.getDialogId(), "option", event.getOptionId(), "dialogObj", event.getDialog());

        NpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onNPCInteract(EntityNPCInterface npc, EntityPlayer player) {
        if(npc == null || npc.wrappedNPC == null)
            return false;

        NpcEvent.InteractEvent event = new NpcEvent.InteractEvent(npc.wrappedNPC,player);
        ScriptController.Instance.globalNpcScripts.callScript(EnumScriptType.INTERACT, event);
        boolean result = npc.script.callScript(EnumScriptType.INTERACT, event, "player", player);
        NpcAPI.EVENT_BUS.post(event);
        return result;
    }

    public static boolean onNPCMeleeAttack(EntityNPCInterface npc, NpcEvent.MeleeAttackEvent event){
        if(npc == null || npc.wrappedNPC == null)
            return false;

        ScriptController.Instance.globalNpcScripts.callScript(EnumScriptType.ATTACK_MELEE, event);
        npc.script.callScript(EnumScriptType.ATTACK,  event, "target", event.target);
        return NpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onNPCMeleeSwing(EntityNPCInterface npc, NpcEvent.SwingEvent event){
        if(npc == null || npc.wrappedNPC == null)
            return false;

        ScriptController.Instance.globalNpcScripts.callScript(EnumScriptType.ATTACK_SWING, event);
        npc.script.callScript(EnumScriptType.ATTACK_SWING,  event);
        return NpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onNPCRangedAttack(EntityNPCInterface npc, NpcEvent.RangedLaunchedEvent event){
        if(npc == null || npc.wrappedNPC == null)
            return false;

        ScriptController.Instance.globalNpcScripts.callScript(EnumScriptType.RANGED_LAUNCHED, event);
        npc.script.callScript(EnumScriptType.ATTACK, event, "target", event.target);
        return NpcAPI.EVENT_BUS.post(event);
    }

    public static void onNPCKilledEntity(EntityNPCInterface npc, EntityLivingBase entity){
        if(npc == null || npc.wrappedNPC == null)
            return;

        NpcEvent.KilledEntityEvent event = new NpcEvent.KilledEntityEvent(npc.wrappedNPC,entity);
        ScriptController.Instance.globalNpcScripts.callScript(EnumScriptType.KILLS, event);
        npc.script.callScript(EnumScriptType.KILLS, event, "target", entity);
        NpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onNPCTarget(EntityNPCInterface npc, NpcEvent.TargetEvent event){
        if(npc == null || npc.wrappedNPC == null)
            return false;

        ScriptController.Instance.globalNpcScripts.callScript(EnumScriptType.TARGET, event);
        npc.script.callScript(EnumScriptType.TARGET, event);
        return NpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onNPCTargetLost(EntityNPCInterface npc, EntityLivingBase prevtarget, EntityLivingBase newTarget) {
        if(npc.script.isClient())
            return false;
        NpcEvent.TargetLostEvent event = new NpcEvent.TargetLostEvent(npc.wrappedNPC, prevtarget, newTarget);

        ScriptController.Instance.globalNpcScripts.callScript(EnumScriptType.TARGET_LOST, event);
        npc.script.callScript(EnumScriptType.TARGET_LOST, event);
        return NpcAPI.EVENT_BUS.post(event);
    }

    public static void onNPCCollide(EntityNPCInterface npc, Entity entity){
        if(npc == null || npc.wrappedNPC == null)
            return;

        NpcEvent.CollideEvent event = new NpcEvent.CollideEvent(npc.wrappedNPC,entity);
        ScriptController.Instance.globalNpcScripts.callScript(EnumScriptType.COLLIDE, event);
        npc.script.callScript(EnumScriptType.COLLIDE, event, "entity", entity);
        NpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onNPCDamaged(EntityNPCInterface npc, NpcEvent.DamagedEvent event){
        if(npc == null || npc.wrappedNPC == null)
            return false;

        ScriptController.Instance.globalNpcScripts.callScript(EnumScriptType.DAMAGED, event);
        npc.script.callScript(EnumScriptType.DAMAGED, event);
        return NpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onNPCKilled(EntityNPCInterface npc, NpcEvent.DiedEvent event){
        if(npc == null || npc.wrappedNPC == null)
            return false;

        ScriptController.Instance.globalNpcScripts.callScript(EnumScriptType.KILLED, event);
        npc.script.callScript(EnumScriptType.KILLED, event);
        return NpcAPI.EVENT_BUS.post(event);
    }

    public static void onNPCTimer(EntityNPCInterface npc, int id) {
        if(npc == null || npc.wrappedNPC == null)
            return;

        NpcEvent.TimerEvent event = new NpcEvent.TimerEvent(npc.wrappedNPC, id);
        ScriptController.Instance.globalNpcScripts.callScript(EnumScriptType.TIMER, event);
        npc.script.callScript(EnumScriptType.TIMER, event);
        NpcAPI.EVENT_BUS.post(event);
    }

    public static void onProjectileTick(EntityProjectile projectile) {
        ProjectileEvent.UpdateEvent event = new ProjectileEvent.UpdateEvent((IProjectile) NpcAPI.Instance().getIEntity(projectile));
        ScriptController.Instance.globalNpcScripts.callScript(EnumScriptType.PROJECTILE_TICK, event);
        for(ScriptContainer script : projectile.scripts) {
            if(script.isValid()) {
                script.run(EnumScriptType.PROJECTILE_TICK, event);
            }
        }

        EntityLivingBase thrower = projectile.getThrower();
        if(thrower != null){
            if(thrower instanceof EntityNPCInterface){
                ((EntityNPCInterface) thrower).script.callScript(EnumScriptType.PROJECTILE_TICK, event);
            }
        }

        NpcAPI.EVENT_BUS.post(event);
    }

    public static void onProjectileImpact(EntityProjectile projectile, ProjectileEvent.ImpactEvent event) {
        ScriptController.Instance.globalNpcScripts.callScript(EnumScriptType.PROJECTILE_IMPACT, event);
        for(ScriptContainer script : projectile.scripts) {
            if(script.isValid()) {
                script.run(EnumScriptType.PROJECTILE_IMPACT, event);
            }
        }

        EntityLivingBase thrower = projectile.getThrower();
        if(thrower != null){
            if(thrower instanceof EntityNPCInterface){
                ((EntityNPCInterface) thrower).script.callScript(EnumScriptType.PROJECTILE_IMPACT, event);
            }
        }

        NpcAPI.EVENT_BUS.post(event);
    }

    public static void onPlayerInit(PlayerDataScript handler, IPlayer player) {
        noppes.npcs.scripted.event.PlayerEvent.InitEvent event = new noppes.npcs.scripted.event.PlayerEvent.InitEvent(player);
        handler.callScript(EnumScriptType.INIT, event);
        NpcAPI.EVENT_BUS.post(event);
    }

    public static void onPlayerTick(PlayerDataScript handler, IPlayer player) {
        noppes.npcs.scripted.event.PlayerEvent.UpdateEvent event = new noppes.npcs.scripted.event.PlayerEvent.UpdateEvent(player);
        handler.callScript(EnumScriptType.TICK, event);
        NpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onPlayerInteract(PlayerDataScript handler, noppes.npcs.scripted.event.PlayerEvent.InteractEvent event) {
        handler.callScript(EnumScriptType.INTERACT, event);
        return NpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onStartUsingItem(PlayerDataScript handler, IPlayer player, int duration, ItemStack item){
        PlayerEvent.StartUsingItem event = new PlayerEvent.StartUsingItem(player, item, duration);
        handler.callScript(EnumScriptType.START_USING_ITEM, event);
        return NpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onUsingItem(PlayerDataScript handler, IPlayer player, int duration, ItemStack item){
        PlayerEvent.UsingItem event = new PlayerEvent.UsingItem(player, item, duration);
        handler.callScript(EnumScriptType.USING_ITEM, event);
        return NpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onStopUsingItem(PlayerDataScript handler, IPlayer player, int duration, ItemStack item){
        PlayerEvent.StopUsingItem event = new PlayerEvent.StopUsingItem(player, item, duration);
        handler.callScript(EnumScriptType.STOP_USING_ITEM, event);
        return NpcAPI.EVENT_BUS.post(event);
    }

    public static void onFinishUsingItem(PlayerDataScript handler, IPlayer player, int duration, ItemStack item){
        PlayerEvent.FinishUsingItem event = new PlayerEvent.FinishUsingItem(player, item, duration);
        handler.callScript(EnumScriptType.FINISH_USING_ITEM, event);
        NpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onPlayerDropItems(PlayerDataScript handler, IPlayer player, ArrayList<EntityItem> entityItems) {
        IItemStack[] items = new IItemStack[entityItems.size()];
        for(int i = 0; i < entityItems.size(); i++){ items[i] = NpcAPI.Instance().getIItemStack(entityItems.get(i).getEntityItem()); }

        DropEvent event = new DropEvent(player, items);
        handler.callScript(EnumScriptType.DROP, event);
        return NpcAPI.EVENT_BUS.post(event);
    }

    public static void onPlayerPickupXP(PlayerDataScript handler, IPlayer player, EntityXPOrb orb) {
        PlayerEvent.PickupXPEvent event = new PlayerEvent.PickupXPEvent(player, orb);
        handler.callScript(EnumScriptType.PICKUP_XP, event);
        NpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onPlayerToss(PlayerDataScript handler, IPlayer player, EntityItem entityItem) {
        PlayerEvent.TossEvent event = new PlayerEvent.TossEvent(player, NpcAPI.Instance().getIItemStack(entityItem.getEntityItem()));
        handler.callScript(EnumScriptType.TOSS, event);
        return NpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onPlayerPickUp(PlayerDataScript handler, IPlayer player, EntityItem entityItem) {
        PickUpEvent event = new PickUpEvent(player, NpcAPI.Instance().getIItemStack(entityItem.getEntityItem()));//NpcAPI.Instance().getIItemStack(entityItem.getEntityItem()));
        handler.callScript(EnumScriptType.PICKUP, event);
        return NpcAPI.EVENT_BUS.post(event);
    }

    public static void onPlayerContainerOpen(PlayerDataScript handler, IPlayer player, Container container) {
        ContainerOpen event = new ContainerOpen(player, NpcAPI.Instance().getIContainer(container));
        handler.callScript(EnumScriptType.CONTAINER_OPEN, event);
        NpcAPI.EVENT_BUS.post(event);
    }

    public static void onPlayerUseHoe(PlayerDataScript handler, IPlayer player, ItemStack hoe, int x, int y, int z) {
        noppes.npcs.scripted.event.PlayerEvent.UseHoe event = new noppes.npcs.scripted.event.PlayerEvent.UseHoe(player, hoe, x, y, z);
        handler.callScript(EnumScriptType.USE_HOE, event);
        NpcAPI.EVENT_BUS.post(event);
    }

    public static void onPlayerSleep(PlayerDataScript handler, IPlayer player, int x, int y, int z) {
        noppes.npcs.scripted.event.PlayerEvent.Sleep event = new noppes.npcs.scripted.event.PlayerEvent.Sleep(player, x, y, z);
        handler.callScript(EnumScriptType.SLEEP, event);
        NpcAPI.EVENT_BUS.post(event);
    }

    public static void onPlayerWakeUp(PlayerDataScript handler, IPlayer player, boolean setSpawn) {
        noppes.npcs.scripted.event.PlayerEvent.WakeUp event = new noppes.npcs.scripted.event.PlayerEvent.WakeUp(player, setSpawn);
        handler.callScript(EnumScriptType.WAKE_UP, event);
        NpcAPI.EVENT_BUS.post(event);
    }

    public static void onPlayerDeath(PlayerDataScript handler, IPlayer player, DamageSource source, Entity entity) {
        noppes.npcs.scripted.event.PlayerEvent.DiedEvent event = new noppes.npcs.scripted.event.PlayerEvent.DiedEvent(player, source, entity);
        handler.callScript(EnumScriptType.KILLED, event);
        NpcAPI.EVENT_BUS.post(event);
    }

    public static void onPlayerKills(PlayerDataScript handler, IPlayer player, EntityLivingBase entityLiving) {
        noppes.npcs.scripted.event.PlayerEvent.KilledEntityEvent event = new noppes.npcs.scripted.event.PlayerEvent.KilledEntityEvent(player, entityLiving);
        handler.callScript(EnumScriptType.KILLS, event);
        NpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onPlayerAttacked(PlayerDataScript handler, noppes.npcs.scripted.event.PlayerEvent.AttackedEvent event) {
        handler.callScript(EnumScriptType.ATTACKED, event);
        return NpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onPlayerDamaged(PlayerDataScript handler, noppes.npcs.scripted.event.PlayerEvent.DamagedEvent event) {
        handler.callScript(EnumScriptType.DAMAGED, event);
        return NpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onPlayerLightning(PlayerDataScript handler, IPlayer player) {
        PlayerEvent.LightningEvent event = new PlayerEvent.LightningEvent(player);
        handler.callScript(EnumScriptType.LIGHTNING, event);
        return NpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onPlayerSound(PlayerDataScript handler, IPlayer player, String name, float pitch, float volume) {
        PlayerEvent.SoundEvent event = new PlayerEvent.SoundEvent(player, name, pitch, volume);
        handler.callScript(EnumScriptType.PLAYSOUND, event);
        return NpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onPlayerFall(PlayerDataScript handler, IPlayer player, float distance) {
        PlayerEvent.FallEvent event = new PlayerEvent.FallEvent(player,distance);
        handler.callScript(EnumScriptType.FALL, event);
        return NpcAPI.EVENT_BUS.post(event);
    }

    public static void onPlayerJump(PlayerDataScript handler, IPlayer player) {
        PlayerEvent.JumpEvent event = new PlayerEvent.JumpEvent(player);
        handler.callScript(EnumScriptType.JUMP, event);
        NpcAPI.EVENT_BUS.post(event);
    }

    public static void onPlayerLogin(PlayerDataScript handler, IPlayer player) {
        LoginEvent event = new LoginEvent(player);
        handler.callScript(EnumScriptType.LOGIN, event);
        NpcAPI.EVENT_BUS.post(event);
    }

    public static void onPlayerAchievement(PlayerDataScript handler, IPlayer player, String description) {
        PlayerEvent.Achievement event = new PlayerEvent.Achievement(player, description);
        handler.callScript(EnumScriptType.ACHIEVEMENT, event);
        NpcAPI.EVENT_BUS.post(event);
    }

    public static void onPlayerFillBucket(PlayerDataScript handler, IPlayer player, ItemStack current, ItemStack result) {
        PlayerEvent.FillBucket event = new PlayerEvent.FillBucket(player, current, result);
        handler.callScript(EnumScriptType.FILL_BUCKET, event);
        NpcAPI.EVENT_BUS.post(event);
    }

    public static void onPlayerBonemeal(PlayerDataScript handler, IPlayer player, int x, int y, int z, World world) {
        PlayerEvent.Bonemeal event = new PlayerEvent.Bonemeal(player, x, y, z, world);
        handler.callScript(EnumScriptType.BONEMEAL, event);
        NpcAPI.EVENT_BUS.post(event);
    }

    public static void onPlayerRespawn(PlayerDataScript handler, IPlayer player) {
        PlayerEvent.RespawnEvent event = new PlayerEvent.RespawnEvent(player);
        handler.callScript(EnumScriptType.RESPAWN, event);
        NpcAPI.EVENT_BUS.post(event);
    }

    public static void onPlayerLogout(PlayerDataScript handler, IPlayer player) {
        LogoutEvent event = new LogoutEvent(player);
        handler.callScript(EnumScriptType.LOGOUT, event);
        NpcAPI.EVENT_BUS.post(event);
    }

    public static void onPlayerChat(PlayerDataScript handler, ChatEvent event) {
        handler.callScript(EnumScriptType.CHAT, event);
        NpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onPlayerBowCharge(PlayerDataScript handler, noppes.npcs.scripted.event.PlayerEvent.RangedChargeEvent event) {
        handler.callScript(EnumScriptType.RANGED_CHARGE, event);
        return NpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onPlayerRanged(PlayerDataScript handler, noppes.npcs.scripted.event.PlayerEvent.RangedLaunchedEvent event) {
        handler.callScript(EnumScriptType.RANGED_LAUNCHED, event);
        return NpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onPlayerAttack(PlayerDataScript handler, PlayerEvent.AttackEvent event) {
        handler.callScript(EnumScriptType.ATTACK, event);
        return NpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onPlayerDamagedEntity(PlayerDataScript handler, DamagedEntityEvent event) {
        handler.callScript(EnumScriptType.DAMAGED_ENTITY, event);
        return NpcAPI.EVENT_BUS.post(event);
    }

    public static void onPlayerChangeDim(PlayerDataScript handler, IPlayer player, int fromDim, int toDim) {
        PlayerEvent.ChangedDimension event = new PlayerEvent.ChangedDimension(player, fromDim, toDim);
        handler.callScript(EnumScriptType.CHANGED_DIM, event);
        NpcAPI.EVENT_BUS.post(event);
    }

    public static void onPlayerMouseClicked(EntityPlayerMP player, int button, int mouseWheel, boolean buttonDown, boolean isCtrlPressed, boolean isShiftPressed, boolean isAltPressed, boolean isMetaPressed, int[] heldKeys) {
        PlayerDataScript handler = ScriptController.Instance.playerScripts;
        PlayerEvent.MouseClickedEvent event = new PlayerEvent.MouseClickedEvent((IPlayer)NpcAPI.Instance().getIEntity(player), button, mouseWheel, buttonDown, isCtrlPressed, isAltPressed, isShiftPressed, isMetaPressed, heldKeys);
        handler.callScript(EnumScriptType.MOUSE_CLICKED, event);
        NpcAPI.EVENT_BUS.post(event);
    }

    public static void onPlayerKeyPressed(EntityPlayerMP player, int button, boolean isCtrlPressed, boolean isShiftPressed, boolean isAltPressed, boolean isMetaPressed, boolean buttonDown, int[] heldKeys) {
        PlayerDataScript handler = ScriptController.Instance.playerScripts;
        KeyPressedEvent event = new KeyPressedEvent((IPlayer)NpcAPI.Instance().getIEntity(player), button, isCtrlPressed, isAltPressed, isShiftPressed, isMetaPressed, buttonDown, heldKeys);
        handler.callScript(EnumScriptType.KEY_PRESSED, event);
        NpcAPI.EVENT_BUS.post(event);
    }

    public static void onPlayerTimer(PlayerData data, int id) {
        PlayerDataScript handler = ScriptController.Instance.playerScripts;
        noppes.npcs.scripted.event.PlayerEvent.TimerEvent event = new noppes.npcs.scripted.event.PlayerEvent.TimerEvent((IPlayer)NpcAPI.Instance().getIEntity(data.player), id);
        handler.callScript(EnumScriptType.TIMER, event);
        NpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onPlayerBreak(PlayerDataScript handler, PlayerEvent.BreakEvent event) {
        handler.callScript(EnumScriptType.BREAK_BLOCK, event);
        return NpcAPI.EVENT_BUS.post(event);
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
        NpcAPI.EVENT_BUS.post(event);
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
            NpcAPI.EVENT_BUS.post(ev);
            if(event.isCancelable()) {
                event.setCanceled(ev.isCanceled());
            }

        }
    }

    public static boolean onCNPCNaturalSpawn(CustomNPCsEvent.CNPCNaturalSpawnEvent event) {
        ForgeDataScript handler = ScriptController.Instance.forgeScripts;
        if (handler.isEnabled()) {
            handler.callScript(EnumScriptType.CNPC_NATURAL_SPAWN, event);
            return NpcAPI.EVENT_BUS.post(event);
        }
        return false;
    }

    public static void onScriptedCommand(CustomNPCsEvent.ScriptedCommandEvent event) {
        ForgeDataScript handler = ScriptController.Instance.forgeScripts;
        if (handler.isEnabled()) {
            handler.callScript(EnumScriptType.SCRIPT_COMMAND, event);
        }
    }

    public static void onCustomGuiButton(IPlayer player, ICustomGui gui, int buttonId) {
        CustomGuiEvent.ButtonEvent event = new CustomGuiEvent.ButtonEvent(player, gui, buttonId);
        CustomGuiController.onButton(event);
    }

    public static void onCustomGuiSlot(IPlayer player, ICustomGui gui, int slotId, ItemStack stack, IItemSlot slot) {
        CustomGuiEvent.SlotEvent event = new CustomGuiEvent.SlotEvent(player, gui, slotId, stack, slot);
        CustomGuiController.onSlotChange(event);
    }

    public static boolean onCustomGuiSlotClicked(IPlayer player, ICustomGui gui, int slotId, IItemSlot slot, int dragType, int clickType) {
        CustomGuiEvent.SlotClickEvent event = new CustomGuiEvent.SlotClickEvent(player, gui, slotId, slot, player.getOpenContainer().getSlot(slotId), dragType, clickType);
        return CustomGuiController.onSlotClick(event);
    }

    public static void onCustomGuiUnfocused(IPlayer player, ICustomGui gui, int textfieldId) {
        CustomGuiEvent.UnfocusedEvent event = new CustomGuiEvent.UnfocusedEvent(player, gui, textfieldId);
        CustomGuiController.onCustomGuiUnfocused(event);
    }

    public static void onCustomGuiScrollClick(IPlayer player, ICustomGui gui, int scrollId, int scrollIndex, String[] selection, boolean doubleClick) {
        CustomGuiEvent.ScrollEvent event = new CustomGuiEvent.ScrollEvent(player, gui, scrollId, scrollIndex, selection, doubleClick);
        CustomGuiController.onScrollClick(event);
    }

    public static void onCustomGuiClose(IPlayer player, ICustomGui gui) {
        noppes.npcs.scripted.event.CustomGuiEvent.CloseEvent event = new noppes.npcs.scripted.event.CustomGuiEvent.CloseEvent(player, gui);
        CustomGuiController.onClose(event);
    }

    public static void onQuestFinished(EntityPlayer player, Quest quest){
        PlayerDataScript handler = ScriptController.Instance.playerScripts;
        QuestEvent.QuestCompletedEvent event = new QuestEvent.QuestCompletedEvent((IPlayer) NpcAPI.Instance().getIEntity((EntityPlayerMP) player), quest);
        handler.callScript(EnumScriptType.QUEST_COMPLETED, event);
        NpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onQuestStarted(EntityPlayer player, Quest quest){
        PlayerDataScript handler = ScriptController.Instance.playerScripts;
        QuestEvent.QuestStartEvent event = new QuestEvent.QuestStartEvent((IPlayer) NpcAPI.Instance().getIEntity((EntityPlayerMP) player), quest);
        handler.callScript(EnumScriptType.QUEST_START, event);
        return NpcAPI.EVENT_BUS.post(event);
    }

    public static void onQuestTurnedIn(QuestEvent.QuestTurnedInEvent event){
        PlayerDataScript handler = ScriptController.Instance.playerScripts;
        handler.callScript(EnumScriptType.QUEST_TURNIN, event);
        NpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onFactionPoints(FactionEvent.FactionPoints event){
        PlayerDataScript handler = ScriptController.Instance.playerScripts;
        handler.callScript(EnumScriptType.FACTION_POINTS, event);
        return NpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onDialogOpen(DialogEvent.DialogOpen event){
        PlayerDataScript handler = ScriptController.Instance.playerScripts;
        handler.callScript(EnumScriptType.DIALOG_OPEN, event);
        return NpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onDialogOption(DialogEvent.DialogOption event){
        PlayerDataScript handler = ScriptController.Instance.playerScripts;
        handler.callScript(EnumScriptType.DIALOG_OPTION, event);
        return NpcAPI.EVENT_BUS.post(event);
    }

    public static void onDialogClosed(DialogEvent.DialogClosed event){
        PlayerDataScript handler = ScriptController.Instance.playerScripts;
        handler.callScript(EnumScriptType.DIALOG_CLOSE, event);
        NpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onScriptBlockInteract(IScriptBlockHandler handler, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        if(handler.isClient())
            return false;
        BlockEvent.InteractEvent event = new BlockEvent.InteractEvent(handler.getBlock(), player, side, hitX, hitY, hitZ);
        handler.callScript(EnumScriptType.INTERACT, event);
        return NpcAPI.EVENT_BUS.post(event);
    }

    public static void onScriptBlockCollide(IScriptBlockHandler handler, Entity entityIn) {
        if(handler.isClient())
            return;
        BlockEvent.CollidedEvent event = new BlockEvent.CollidedEvent(handler.getBlock(), entityIn);
        handler.callScript(EnumScriptType.COLLIDE, event);
        NpcAPI.EVENT_BUS.post(event);
    }

    public static void onScriptBlockRainFill(IScriptBlockHandler handler) {
        if(handler.isClient())
            return;
        BlockEvent.RainFillEvent event = new BlockEvent.RainFillEvent(handler.getBlock());
        handler.callScript(EnumScriptType.RAIN_FILLED, event);
        NpcAPI.EVENT_BUS.post(event);
    }

    public static float onScriptBlockFallenUpon(IScriptBlockHandler handler, Entity entity, float distance) {
        if(handler.isClient())
            return distance;
        BlockEvent.EntityFallenUponEvent event = new BlockEvent.EntityFallenUponEvent(handler.getBlock(), entity, distance);
        handler.callScript(EnumScriptType.FALLEN_UPON, event);
        if(NpcAPI.EVENT_BUS.post(event))
            return 0;
        return event.distanceFallen;
    }

    public static void onScriptBlockClicked(IScriptBlockHandler handler, EntityPlayer player) {
        if(handler.isClient())
            return;
        BlockEvent.ClickedEvent event = new BlockEvent.ClickedEvent(handler.getBlock(), player);
        handler.callScript(EnumScriptType.CLICKED, event);
        NpcAPI.EVENT_BUS.post(event);
    }

    public static void onScriptBlockBreak(IScriptBlockHandler handler) {
        if(handler.isClient())
            return;
        BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(handler.getBlock());
        handler.callScript(EnumScriptType.BROKEN, event);
        NpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onScriptBlockHarvest(IScriptBlockHandler handler, EntityPlayer player) {
        if(handler.isClient())
            return false;
        BlockEvent.HarvestedEvent event = new BlockEvent.HarvestedEvent(handler.getBlock(), player);
        handler.callScript(EnumScriptType.HARVESTED, event);
        return NpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onScriptBlockExploded(IScriptBlockHandler handler) {
        if(handler.isClient())
            return false;
        BlockEvent.ExplodedEvent event = new BlockEvent.ExplodedEvent(handler.getBlock());
        handler.callScript(EnumScriptType.EXPLODED, event);
        return NpcAPI.EVENT_BUS.post(event);
    }

    public static void onScriptBlockNeighborChanged(IScriptBlockHandler handler, int x, int y, int z) {
        if(handler.isClient())
            return;
        BlockEvent.NeighborChangedEvent event = new BlockEvent.NeighborChangedEvent(handler.getBlock(), NpcAPI.Instance().getIPos(x,y,z));
        handler.callScript(EnumScriptType.NEIGHBOR_CHANGED, event);
        NpcAPI.EVENT_BUS.post(event);
    }

    public static void onScriptBlockRedstonePower(IScriptBlockHandler handler, int prevPower, int power) {
        if(handler.isClient())
            return;
        BlockEvent.RedstoneEvent event = new BlockEvent.RedstoneEvent(handler.getBlock(), prevPower, power);
        handler.callScript(EnumScriptType.REDSTONE, event);
        NpcAPI.EVENT_BUS.post(event);
    }

    public static void onScriptBlockInit(IScriptBlockHandler handler) {
        if(handler.isClient())
            return;
        BlockEvent.InitEvent event = new BlockEvent.InitEvent(handler.getBlock());
        handler.callScript(EnumScriptType.INIT, event);
        NpcAPI.EVENT_BUS.post(event);
    }

    public static void onScriptBlockUpdate(IScriptBlockHandler handler) {
        if(handler.isClient())
            return;
        BlockEvent.UpdateEvent event = new BlockEvent.UpdateEvent(handler.getBlock());
        handler.callScript(EnumScriptType.TICK, event);
        NpcAPI.EVENT_BUS.post(event);
    }

    public static void onScriptBlockTimer(IScriptBlockHandler handler, int id) {
        BlockEvent.TimerEvent event = new BlockEvent.TimerEvent(handler.getBlock(), id);
        handler.callScript(EnumScriptType.TIMER, event);
        NpcAPI.EVENT_BUS.post(event);
    }
}
