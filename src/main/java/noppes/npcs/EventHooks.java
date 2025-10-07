package noppes.npcs;

import cpw.mods.fml.common.eventhandler.Event;
import kamkeel.npcs.network.packets.data.AchievementPacket;
import net.minecraft.command.ICommandSender;
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
import noppes.npcs.api.entity.IAnimatable;
import noppes.npcs.api.entity.ICustomNpc;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.entity.IProjectile;
import noppes.npcs.api.event.IAnimationEvent;
import noppes.npcs.api.gui.ICustomGui;
import noppes.npcs.api.gui.IItemSlot;
import noppes.npcs.api.handler.data.IAnimation;
import noppes.npcs.api.handler.data.IAnvilRecipe;
import noppes.npcs.api.handler.data.IFrame;
import noppes.npcs.api.handler.data.IPlayerEffect;
import noppes.npcs.api.handler.data.IProfile;
import noppes.npcs.api.item.IItemCustomizable;
import noppes.npcs.api.item.IItemLinked;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.constants.EnumScriptType;
import noppes.npcs.controllers.CustomGuiController;
import noppes.npcs.controllers.ScriptContainer;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.controllers.data.Dialog;
import noppes.npcs.controllers.data.EffectScript;
import noppes.npcs.controllers.data.ForgeDataScript;
import noppes.npcs.controllers.data.INpcScriptHandler;
import noppes.npcs.controllers.data.IScriptBlockHandler;
import noppes.npcs.controllers.data.IScriptHandler;
import noppes.npcs.controllers.data.Party;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.controllers.data.PlayerDataScript;
import noppes.npcs.controllers.data.Quest;
import noppes.npcs.controllers.data.RecipeScript;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.entity.EntityProjectile;
import noppes.npcs.scripted.NpcAPI;
import noppes.npcs.scripted.event.AnimationEvent;
import noppes.npcs.scripted.event.BlockEvent;
import noppes.npcs.scripted.event.CustomNPCsEvent;
import noppes.npcs.scripted.event.ForgeEvent;
import noppes.npcs.scripted.event.ItemEvent;
import noppes.npcs.scripted.event.LinkedItemEvent;
import noppes.npcs.scripted.event.NpcEvent;
import noppes.npcs.scripted.event.PartyEvent;
import noppes.npcs.scripted.event.ProjectileEvent;
import noppes.npcs.scripted.event.RecipeScriptEvent;
import noppes.npcs.scripted.event.player.CustomGuiEvent;
import noppes.npcs.scripted.event.player.DialogEvent;
import noppes.npcs.scripted.event.player.FactionEvent;
import noppes.npcs.scripted.event.player.PlayerEvent;
import noppes.npcs.scripted.event.player.PlayerEvent.ChatEvent;
import noppes.npcs.scripted.event.player.PlayerEvent.ContainerOpen;
import noppes.npcs.scripted.event.player.PlayerEvent.DamagedEntityEvent;
import noppes.npcs.scripted.event.player.PlayerEvent.DropEvent;
import noppes.npcs.scripted.event.player.PlayerEvent.EffectEvent;
import noppes.npcs.scripted.event.player.PlayerEvent.KeyPressedEvent;
import noppes.npcs.scripted.event.player.PlayerEvent.LoginEvent;
import noppes.npcs.scripted.event.player.PlayerEvent.LogoutEvent;
import noppes.npcs.scripted.event.player.PlayerEvent.PickUpEvent;
import noppes.npcs.scripted.event.player.QuestEvent;
import noppes.npcs.scripted.item.ScriptItemStack;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

public class EventHooks {
    public EventHooks() {
    }

    public static void onScriptItemInit(IItemCustomizable item) {
        INpcScriptHandler handler = (INpcScriptHandler) item.getScriptHandler();
        if (handler != null && !handler.isClient()) {
            ItemEvent.InitEvent event = new ItemEvent.InitEvent(item);
            handler.callScript(EnumScriptType.INIT, event);
            NpcAPI.EVENT_BUS.post(event);
        }
    }

    public static void onScriptItemUpdate(IItemCustomizable item, EntityLivingBase player) {
        INpcScriptHandler handler = (INpcScriptHandler) item.getScriptHandler();
        if (handler != null && !handler.isClient()) {
            ItemEvent.UpdateEvent event = new ItemEvent.UpdateEvent(item, NpcAPI.Instance().getIEntity(player));
            handler.callScript(EnumScriptType.TICK, event);
            NpcAPI.EVENT_BUS.post(event);
        }
    }

    public static boolean onScriptItemTossed(IItemCustomizable item, EntityPlayer player, EntityItem entity) {
        INpcScriptHandler handler = (INpcScriptHandler) item.getScriptHandler();
        ItemEvent.TossedEvent event = new ItemEvent.TossedEvent(item, (IPlayer) NpcAPI.Instance().getIEntity(player), NpcAPI.Instance().getIEntity(entity));
        if (handler != null) {
            handler.callScript(EnumScriptType.TOSSED, event);
        }
        return NpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onScriptItemPickedUp(IItemCustomizable item, EntityPlayer player) {
        INpcScriptHandler handler = (INpcScriptHandler) item.getScriptHandler();
        ItemEvent.PickedUpEvent event = new ItemEvent.PickedUpEvent(item, (IPlayer) NpcAPI.Instance().getIEntity(player));
        if (handler != null) {
            handler.callScript(EnumScriptType.PICKEDUP, event);
        }
        return NpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onScriptItemSpawn(IItemCustomizable item, EntityItem entity) {
        INpcScriptHandler handler = (INpcScriptHandler) item.getScriptHandler();
        ItemEvent.SpawnEvent event = new ItemEvent.SpawnEvent(item, NpcAPI.Instance().getIEntity(entity));
        if (handler != null) {
            handler.callScript(EnumScriptType.SPAWN, event);
        }
        return NpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onScriptItemInteract(IItemCustomizable item, noppes.npcs.scripted.event.ItemEvent.InteractEvent event) {
        INpcScriptHandler handler = (INpcScriptHandler) item.getScriptHandler();
        if (handler != null) {
            handler.callScript(EnumScriptType.INTERACT, event);
        }
        return NpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onScriptItemRightClick(IItemCustomizable item, noppes.npcs.scripted.event.ItemEvent.RightClickEvent event) {
        INpcScriptHandler handler = (INpcScriptHandler) item.getScriptHandler();
        if (handler != null) {
            handler.callScript(EnumScriptType.RIGHT_CLICK, event);
        }
        return NpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onScriptItemAttack(IItemCustomizable item, noppes.npcs.scripted.event.ItemEvent.AttackEvent event) {
        INpcScriptHandler handler = (INpcScriptHandler) item.getScriptHandler();
        if (handler != null) {
            handler.callScript(EnumScriptType.ATTACK, event);
        }
        return NpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onStartUsingCustomItem(IItemCustomizable item, IPlayer player, int duration) {
        INpcScriptHandler handler = (INpcScriptHandler) item.getScriptHandler();
        ItemEvent.StartUsingItem event = new ItemEvent.StartUsingItem(item, player, duration);
        if (handler != null) {
            handler.callScript(EnumScriptType.START_USING_ITEM, event);
        }
        return NpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onUsingCustomItem(IItemCustomizable item, IPlayer player, int duration) {
        INpcScriptHandler handler = (INpcScriptHandler) item.getScriptHandler();
        ItemEvent.UsingItem event = new ItemEvent.UsingItem(item, player, duration);
        if (handler != null) {
            handler.callScript(EnumScriptType.USING_ITEM, event);
        }
        return NpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onStopUsingCustomItem(IItemCustomizable item, IPlayer player, int duration) {
        INpcScriptHandler handler = (INpcScriptHandler) item.getScriptHandler();
        ItemEvent.StopUsingItem event = new ItemEvent.StopUsingItem(item, player, duration);
        if (handler != null) {
            handler.callScript(EnumScriptType.STOP_USING_ITEM, event);
        }
        return NpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onFinishUsingCustomItem(IItemCustomizable item, IPlayer player, int duration) {
        INpcScriptHandler handler = (INpcScriptHandler) item.getScriptHandler();
        ItemEvent.FinishUsingItem event = new ItemEvent.FinishUsingItem(item, player, duration);
        if (handler != null) {
            handler.callScript(EnumScriptType.FINISH_USING_ITEM, event);
        }
        return NpcAPI.EVENT_BUS.post(event);
    }

    public static void onRepairCustomItem(IItemCustomizable item, IPlayer player, IItemStack left, IItemStack right, float anvilBreakChance) {
        INpcScriptHandler handler = (INpcScriptHandler) item.getScriptHandler();
        ItemEvent.RepairItem event = new ItemEvent.RepairItem(item, player, left, right, anvilBreakChance);
        if (handler != null) {
            handler.callScript(EnumScriptType.REPAIR_ITEM, event);
        }
        NpcAPI.EVENT_BUS.post(event);
    }

    public static void onBreakCustomItem(IItemCustomizable item, IPlayer player) {
        INpcScriptHandler handler = (INpcScriptHandler) item.getScriptHandler();
        ItemEvent.BreakItem event = new ItemEvent.BreakItem(item, player);
        if (handler != null) {
            handler.callScript(EnumScriptType.BREAK_ITEM, event);
        }
        NpcAPI.EVENT_BUS.post(event);
    }


    public static RecipeScriptEvent.Pre onRecipeScriptPre(EntityPlayer player, RecipeScript script, Object recipe, ItemStack[] items) {
        IItemStack[] iitems = new IItemStack[items.length];
        for (int i = 0; i < items.length; i++) {
            iitems[i] = items[i] == null ? null : NpcAPI.Instance().getIItemStack(items[i]);
        }
        RecipeScriptEvent.Pre event = new RecipeScriptEvent.Pre(NoppesUtilServer.getIPlayer(player), recipe, recipe instanceof IAnvilRecipe, iitems);
        if (script != null) {
            script.callScript(RecipeScript.ScriptType.PRE.function, event);
        }
        NpcAPI.EVENT_BUS.post(event);
        if (!player.worldObj.isRemote) {
            String msg = event.getMessage();
            if (msg != null && !msg.isEmpty()) {
                AchievementPacket.sendAchievement((EntityPlayerMP) player, false, "", msg);
            }
        }
        return event;
    }

    public static ItemStack onRecipeScriptPost(EntityPlayer player, RecipeScript script, Object recipe, ItemStack[] items, ItemStack result) {
        IItemStack[] iitems = new IItemStack[items.length];
        for (int i = 0; i < items.length; i++) {
            iitems[i] = items[i] == null ? null : NpcAPI.Instance().getIItemStack(items[i]);
        }
        RecipeScriptEvent.Post event = new RecipeScriptEvent.Post(NoppesUtilServer.getIPlayer(player), recipe, recipe instanceof IAnvilRecipe, iitems, NpcAPI.Instance().getIItemStack(result));
        if (script != null) {
            script.callScript(RecipeScript.ScriptType.POST.function, event);
        }
        NpcAPI.EVENT_BUS.post(event);
        return event.getResult() == null ? null : ((ScriptItemStack) event.getCraft()).getMCItemStack();
    }


    public static void onLinkedItemVersionChange(IItemLinked item, int version, int prevVersion) {
        INpcScriptHandler handler = (INpcScriptHandler) item.getScriptHandler();
        LinkedItemEvent.VersionChangeEvent event = new LinkedItemEvent.VersionChangeEvent(item, version, prevVersion);
        if (handler != null) {
            handler.callScript(EnumScriptType.LINKED_ITEM_VERSION, event);
        }
        NpcAPI.EVENT_BUS.post(event);
    }

    public static void onLinkedItemBuild(IItemLinked item) {
        INpcScriptHandler handler = (INpcScriptHandler) item.getScriptHandler();
        LinkedItemEvent.BuildEvent event = new LinkedItemEvent.BuildEvent(item);
        if (handler != null) {
            handler.callScript(EnumScriptType.LINKED_ITEM_BUILD, event);
        }
        NpcAPI.EVENT_BUS.post(event);
    }

    public static void onNPCInit(EntityNPCInterface npc) {
        if (npc == null || npc.wrappedNPC == null)
            return;

        noppes.npcs.scripted.event.NpcEvent.InitEvent event = new noppes.npcs.scripted.event.NpcEvent.InitEvent(npc.wrappedNPC);
        ScriptController.Instance.globalNpcScripts.callScript(EnumScriptType.INIT, event);
        npc.script.callScript(EnumScriptType.INIT, event);
        NpcAPI.EVENT_BUS.post(event);
        npc.advanced.soulStoneInit = false;
    }

    public static void onNPCUpdate(EntityNPCInterface npc) {
        if (npc == null || npc.wrappedNPC == null)
            return;

        noppes.npcs.scripted.event.NpcEvent.UpdateEvent event = new noppes.npcs.scripted.event.NpcEvent.UpdateEvent(npc.wrappedNPC);
        ScriptController.Instance.globalNpcScripts.callScript(EnumScriptType.TICK, event);
        npc.script.callScript(EnumScriptType.TICK, event);
        NpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onNPCDialog(EntityNPCInterface npc, EntityPlayer player, int dialogId, int optionId, Dialog dialog) {
        if (npc == null || npc.wrappedNPC == null)
            return false;

        NpcEvent.DialogEvent event = new NpcEvent.DialogEvent(npc.wrappedNPC, player, dialogId, optionId, dialog);
        ScriptController.Instance.globalNpcScripts.callScript(EnumScriptType.DIALOG, event);
        npc.script.callScript(EnumScriptType.DIALOG, event, "player", event.getPlayer(), "dialog", event.getDialogId(), "option", event.getOptionId(), "dialogObj", event.getDialog());

        return NpcAPI.EVENT_BUS.post(event);
    }

    public static void onNPCDialogClosed(EntityNPCInterface npc, EntityPlayer player, int dialogId, int optionId, Dialog dialog) {
        if (npc == null || npc.wrappedNPC == null)
            return;

        NpcEvent.DialogClosedEvent event = new NpcEvent.DialogClosedEvent(npc.wrappedNPC, player, dialogId, optionId, dialog);
        ScriptController.Instance.globalNpcScripts.callScript(EnumScriptType.DIALOG_CLOSE, event);
        npc.script.callScript(EnumScriptType.DIALOG_CLOSE, event, "player", event.getPlayer(), "dialog", event.getDialogId(), "option", event.getOptionId(), "dialogObj", event.getDialog());

        NpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onNPCInteract(EntityNPCInterface npc, EntityPlayer player) {
        if (npc == null || npc.wrappedNPC == null)
            return false;

        NpcEvent.InteractEvent event = new NpcEvent.InteractEvent(npc.wrappedNPC, player);
        ScriptController.Instance.globalNpcScripts.callScript(EnumScriptType.INTERACT, event);
        boolean result = npc.script.callScript(EnumScriptType.INTERACT, event, "player", player);
        NpcAPI.EVENT_BUS.post(event);
        return result;
    }

    public static boolean onNPCMeleeAttack(EntityNPCInterface npc, NpcEvent.MeleeAttackEvent event) {
        if (npc == null || npc.wrappedNPC == null)
            return false;

        ScriptController.Instance.globalNpcScripts.callScript(EnumScriptType.ATTACK_MELEE, event);
        npc.script.callScript(EnumScriptType.ATTACK_MELEE, event, "target", event.target);
        npc.script.callScript(EnumScriptType.ATTACK, event, "target", event.target);
        return NpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onNPCMeleeSwing(EntityNPCInterface npc, NpcEvent.SwingEvent event) {
        if (npc == null || npc.wrappedNPC == null)
            return false;

        ScriptController.Instance.globalNpcScripts.callScript(EnumScriptType.ATTACK_SWING, event);
        npc.script.callScript(EnumScriptType.ATTACK_SWING, event);
        return NpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onNPCRangedAttack(EntityNPCInterface npc, NpcEvent.RangedLaunchedEvent event) {
        if (npc == null || npc.wrappedNPC == null)
            return false;

        ScriptController.Instance.globalNpcScripts.callScript(EnumScriptType.RANGED_LAUNCHED, event);
        npc.script.callScript(EnumScriptType.RANGED_LAUNCHED, event, "target", event.target);
        npc.script.callScript(EnumScriptType.ATTACK, event, "target", event.target);
        return NpcAPI.EVENT_BUS.post(event);
    }

    public static void onNPCKilledEntity(EntityNPCInterface npc, EntityLivingBase entity) {
        if (npc == null || npc.wrappedNPC == null)
            return;

        NpcEvent.KilledEntityEvent event = new NpcEvent.KilledEntityEvent(npc.wrappedNPC, entity);
        ScriptController.Instance.globalNpcScripts.callScript(EnumScriptType.KILLS, event);
        npc.script.callScript(EnumScriptType.KILLS, event, "target", entity);
        NpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onNPCTarget(EntityNPCInterface npc, NpcEvent.TargetEvent event) {
        if (npc == null || npc.wrappedNPC == null)
            return false;

        ScriptController.Instance.globalNpcScripts.callScript(EnumScriptType.TARGET, event);
        npc.script.callScript(EnumScriptType.TARGET, event);
        return NpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onNPCTargetLost(EntityNPCInterface npc, EntityLivingBase prevtarget, EntityLivingBase newTarget) {
        if (npc.script.isClient())
            return false;
        NpcEvent.TargetLostEvent event = new NpcEvent.TargetLostEvent(npc.wrappedNPC, prevtarget, newTarget);

        ScriptController.Instance.globalNpcScripts.callScript(EnumScriptType.TARGET_LOST, event);
        npc.script.callScript(EnumScriptType.TARGET_LOST, event);
        return NpcAPI.EVENT_BUS.post(event);
    }

    public static void onNPCCollide(EntityNPCInterface npc, Entity entity) {
        if (npc == null || npc.wrappedNPC == null)
            return;

        NpcEvent.CollideEvent event = new NpcEvent.CollideEvent(npc.wrappedNPC, entity);
        ScriptController.Instance.globalNpcScripts.callScript(EnumScriptType.COLLIDE, event);
        npc.script.callScript(EnumScriptType.COLLIDE, event, "entity", entity);
        NpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onNPCDamaged(EntityNPCInterface npc, NpcEvent.DamagedEvent event) {
        if (npc == null || npc.wrappedNPC == null)
            return false;

        ScriptController.Instance.globalNpcScripts.callScript(EnumScriptType.DAMAGED, event);
        npc.script.callScript(EnumScriptType.DAMAGED, event);
        return NpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onNPCKilled(EntityNPCInterface npc, NpcEvent.DiedEvent event) {
        if (npc == null || npc.wrappedNPC == null)
            return false;

        ScriptController.Instance.globalNpcScripts.callScript(EnumScriptType.KILLED, event);
        npc.script.callScript(EnumScriptType.KILLED, event);
        return NpcAPI.EVENT_BUS.post(event);
    }

    public static void onNPCTimer(EntityNPCInterface npc, int id) {
        if (npc == null || npc.wrappedNPC == null)
            return;

        NpcEvent.TimerEvent event = new NpcEvent.TimerEvent(npc.wrappedNPC, id);
        ScriptController.Instance.globalNpcScripts.callScript(EnumScriptType.TIMER, event);
        npc.script.callScript(EnumScriptType.TIMER, event);
        NpcAPI.EVENT_BUS.post(event);
    }

    public static void onProjectileTick(EntityProjectile projectile) {
        ProjectileEvent.UpdateEvent event = new ProjectileEvent.UpdateEvent((IProjectile) NpcAPI.Instance().getIEntity(projectile));
        ScriptController.Instance.globalNpcScripts.callScript(EnumScriptType.PROJECTILE_TICK, event);
        for (ScriptContainer script : projectile.scripts) {
            if (script.isValid()) {
                script.run(EnumScriptType.PROJECTILE_TICK, event);
            }
        }

        EntityLivingBase thrower = projectile.getThrower();
        if (thrower != null) {
            if (thrower instanceof EntityNPCInterface) {
                ((EntityNPCInterface) thrower).script.callScript(EnumScriptType.PROJECTILE_TICK, event);
            }
        }

        NpcAPI.EVENT_BUS.post(event);
    }

    public static void onProjectileImpact(EntityProjectile projectile, ProjectileEvent.ImpactEvent event) {
        ScriptController.Instance.globalNpcScripts.callScript(EnumScriptType.PROJECTILE_IMPACT, event);
        for (ScriptContainer script : projectile.scripts) {
            if (script.isValid()) {
                script.run(EnumScriptType.PROJECTILE_IMPACT, event);
            }
        }

        EntityLivingBase thrower = projectile.getThrower();
        if (thrower != null) {
            if (thrower instanceof EntityNPCInterface) {
                ((EntityNPCInterface) thrower).script.callScript(EnumScriptType.PROJECTILE_IMPACT, event);
            }
        }

        NpcAPI.EVENT_BUS.post(event);
    }

    public static void onPlayerInit(PlayerDataScript handler, IPlayer player) {
        PlayerEvent.InitEvent event = new PlayerEvent.InitEvent(player);
        handler.callScript(EnumScriptType.INIT, event);
        NpcAPI.EVENT_BUS.post(event);
    }

    public static void onPlayerTick(PlayerDataScript handler, IPlayer player) {
        PlayerEvent.UpdateEvent event = new PlayerEvent.UpdateEvent(player);
        handler.callScript(EnumScriptType.TICK, event);
        NpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onPlayerInteract(PlayerDataScript handler, PlayerEvent.InteractEvent event) {
        handler.callScript(EnumScriptType.INTERACT, event);
        return NpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onPlayerRightClick(PlayerDataScript handler, PlayerEvent.RightClickEvent event) {
        handler.callScript(EnumScriptType.RIGHT_CLICK, event);
        return NpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onProfileChange(PlayerDataScript handler, IPlayer player, IProfile profile, int newSlot, int prevSlot, boolean post) {
        PlayerEvent.ProfileEvent.Changed event = new PlayerEvent.ProfileEvent.Changed(player, profile, newSlot, prevSlot, post);
        handler.callScript(EnumScriptType.PROFILE_CHANGE, event);
        return NpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onProfileRemove(PlayerDataScript handler, IPlayer player, IProfile profile, int slot, boolean post) {
        PlayerEvent.ProfileEvent.Removed event = new PlayerEvent.ProfileEvent.Removed(player, profile, slot, post);
        handler.callScript(EnumScriptType.PROFILE_REMOVE, event);
        return NpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onProfileCreate(PlayerDataScript handler, IPlayer player, IProfile profile, int slot, boolean post) {
        PlayerEvent.ProfileEvent.Create event = new PlayerEvent.ProfileEvent.Create(player, profile, slot, post);
        handler.callScript(EnumScriptType.PROFILE_CREATE, event);
        return NpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onEffectAdded(IPlayer player, IPlayerEffect effect) {
        PlayerDataScript handler = ScriptController.Instance.getPlayerScripts(player);
        PlayerEvent.EffectEvent.Added event = new PlayerEvent.EffectEvent.Added(player, effect);
        handler.callScript(EffectScript.ScriptType.OnEffectAdd.function, event);
        return NpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onEffectTick(IPlayer player, IPlayerEffect effect) {
        PlayerDataScript handler = ScriptController.Instance.getPlayerScripts(player);
        PlayerEvent.EffectEvent.Ticked event = new PlayerEvent.EffectEvent.Ticked(player, effect);
        handler.callScript(EffectScript.ScriptType.OnEffectTick.function, event);
        return NpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onEffectRemove(IPlayer player, IPlayerEffect effect, EffectEvent.ExpirationType type) {
        PlayerDataScript handler = ScriptController.Instance.getPlayerScripts(player);
        PlayerEvent.EffectEvent.Removed event = new PlayerEvent.EffectEvent.Removed(player, effect, type);
        handler.callScript(EffectScript.ScriptType.OnEffectRemove.function, event);
        return NpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onStartUsingItem(PlayerDataScript handler, IPlayer player, int duration, ItemStack item) {
        PlayerEvent.StartUsingItem event = new PlayerEvent.StartUsingItem(player, item, duration);
        handler.callScript(EnumScriptType.START_USING_ITEM, event);
        return NpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onUsingItem(PlayerDataScript handler, IPlayer player, int duration, ItemStack item) {
        PlayerEvent.UsingItem event = new PlayerEvent.UsingItem(player, item, duration);
        handler.callScript(EnumScriptType.USING_ITEM, event);
        return NpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onStopUsingItem(PlayerDataScript handler, IPlayer player, int duration, ItemStack item) {
        PlayerEvent.StopUsingItem event = new PlayerEvent.StopUsingItem(player, item, duration);
        handler.callScript(EnumScriptType.STOP_USING_ITEM, event);
        return NpcAPI.EVENT_BUS.post(event);
    }

    public static void onFinishUsingItem(PlayerDataScript handler, IPlayer player, int duration, ItemStack item) {
        PlayerEvent.FinishUsingItem event = new PlayerEvent.FinishUsingItem(player, item, duration);
        handler.callScript(EnumScriptType.FINISH_USING_ITEM, event);
        NpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onPlayerDropItems(PlayerDataScript handler, IPlayer player, ArrayList<EntityItem> entityItems) {
        IItemStack[] items = new IItemStack[entityItems.size()];
        for (int i = 0; i < entityItems.size(); i++) {
            items[i] = NpcAPI.Instance().getIItemStack(entityItems.get(i).getEntityItem());
        }

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
        PlayerEvent.UseHoe event = new PlayerEvent.UseHoe(player, hoe, x, y, z);
        handler.callScript(EnumScriptType.USE_HOE, event);
        NpcAPI.EVENT_BUS.post(event);
    }

    public static void onPlayerSleep(PlayerDataScript handler, IPlayer player, int x, int y, int z) {
        PlayerEvent.Sleep event = new PlayerEvent.Sleep(player, x, y, z);
        handler.callScript(EnumScriptType.SLEEP, event);
        NpcAPI.EVENT_BUS.post(event);
    }

    public static void onPlayerWakeUp(PlayerDataScript handler, IPlayer player, boolean setSpawn) {
        PlayerEvent.WakeUp event = new PlayerEvent.WakeUp(player, setSpawn);
        handler.callScript(EnumScriptType.WAKE_UP, event);
        NpcAPI.EVENT_BUS.post(event);
    }

    public static void onPlayerDeath(PlayerDataScript handler, IPlayer player, DamageSource source, Entity entity) {
        PlayerEvent.DiedEvent event = new PlayerEvent.DiedEvent(player, source, entity);
        handler.callScript(EnumScriptType.KILLED, event);
        NpcAPI.EVENT_BUS.post(event);
    }

    public static void onPlayerKills(PlayerDataScript handler, IPlayer player, EntityLivingBase entityLiving) {
        PlayerEvent.KilledEntityEvent event = new PlayerEvent.KilledEntityEvent(player, entityLiving);
        handler.callScript(EnumScriptType.KILLS, event);
        NpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onPlayerAttacked(PlayerDataScript handler, PlayerEvent.AttackedEvent event) {
        handler.callScript(EnumScriptType.ATTACKED, event);
        return NpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onPlayerDamaged(PlayerDataScript handler, PlayerEvent.DamagedEvent event) {
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
        PlayerEvent.FallEvent event = new PlayerEvent.FallEvent(player, distance);
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

    public static boolean onPlayerBowCharge(PlayerDataScript handler, PlayerEvent.RangedChargeEvent event) {
        handler.callScript(EnumScriptType.RANGED_CHARGE, event);
        return NpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onPlayerRanged(PlayerDataScript handler, PlayerEvent.RangedLaunchedEvent event) {
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
        PlayerDataScript handler = ScriptController.Instance.getPlayerScripts(player);
        PlayerEvent.MouseClickedEvent event = new PlayerEvent.MouseClickedEvent((IPlayer) NpcAPI.Instance().getIEntity(player), button, mouseWheel, buttonDown, isCtrlPressed, isAltPressed, isShiftPressed, isMetaPressed, heldKeys);
        handler.callScript(EnumScriptType.MOUSE_CLICKED, event);
        NpcAPI.EVENT_BUS.post(event);
    }

    public static void onPlayerKeyPressed(EntityPlayerMP player, int button, boolean isCtrlPressed, boolean isShiftPressed, boolean isAltPressed, boolean isMetaPressed, boolean buttonDown, int[] heldKeys) {
        PlayerDataScript handler = ScriptController.Instance.getPlayerScripts(player);
        KeyPressedEvent event = new KeyPressedEvent((IPlayer) NpcAPI.Instance().getIEntity(player), button, isCtrlPressed, isAltPressed, isShiftPressed, isMetaPressed, buttonDown, heldKeys);
        handler.callScript(EnumScriptType.KEY_PRESSED, event);
        NpcAPI.EVENT_BUS.post(event);
    }

    public static void onPlayerTimer(PlayerData data, int id) {
        PlayerDataScript handler = ScriptController.Instance.getPlayerScripts(data.player);
        PlayerEvent.TimerEvent event = new PlayerEvent.TimerEvent((IPlayer) NpcAPI.Instance().getIEntity(data.player), id);
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
        if (ScriptController.Instance.forgeScripts.isEnabled()) {
            IWorld e = NpcAPI.Instance().getIWorld((WorldServer) event.world);
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
        if (handler.isEnabled()) {
            String eventName = event.getClass().getName();
            int i = eventName.lastIndexOf(".");
            eventName = StringUtils.uncapitalize(eventName.substring(i + 1).replace("$", ""));
            if (event.isCancelable()) {
                ev.setCanceled(event.isCanceled());
            }

            handler.callScript(eventName, event);
            NpcAPI.EVENT_BUS.post(ev);
            if (event.isCancelable()) {
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

    public static boolean onScriptedCommand(ICommandSender sender, CustomNPCsEvent.ScriptedCommandEvent event) {
        if (sender instanceof EntityPlayer) {
            ScriptController.Instance.getPlayerScripts((EntityPlayer) sender).callScript(EnumScriptType.SCRIPT_COMMAND, event);
        } else {
            ScriptController.Instance.playerScripts.callScript(EnumScriptType.SCRIPT_COMMAND, event);
        }
        return NpcAPI.EVENT_BUS.post(event);
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
        CustomGuiEvent.CloseEvent event = new CustomGuiEvent.CloseEvent(player, gui);
        CustomGuiController.onClose(event);
    }

    public static void onQuestFinished(EntityPlayer player, Quest quest) {
        PlayerDataScript handler = ScriptController.Instance.getPlayerScripts(player);
        QuestEvent.QuestCompletedEvent event = new QuestEvent.QuestCompletedEvent((IPlayer) NpcAPI.Instance().getIEntity((EntityPlayerMP) player), quest);
        handler.callScript(EnumScriptType.QUEST_COMPLETED, event);
        NpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onQuestStarted(EntityPlayer player, Quest quest) {
        PlayerDataScript handler = ScriptController.Instance.getPlayerScripts(player);
        QuestEvent.QuestStartEvent event = new QuestEvent.QuestStartEvent((IPlayer) NpcAPI.Instance().getIEntity((EntityPlayerMP) player), quest);
        handler.callScript(EnumScriptType.QUEST_START, event);
        return NpcAPI.EVENT_BUS.post(event);
    }

    public static void onQuestTurnedIn(EntityPlayer player, QuestEvent.QuestTurnedInEvent event) {
        PlayerDataScript handler = ScriptController.Instance.getPlayerScripts(player);
        handler.callScript(EnumScriptType.QUEST_TURNIN, event);
        NpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onFactionPoints(EntityPlayer player, FactionEvent.FactionPoints event) {
        PlayerDataScript handler = ScriptController.Instance.getPlayerScripts(player);
        handler.callScript(EnumScriptType.FACTION_POINTS, event);
        return NpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onDialogOpen(EntityPlayer player, DialogEvent.DialogOpen event) {
        PlayerDataScript handler = ScriptController.Instance.getPlayerScripts(player);
        handler.callScript(EnumScriptType.DIALOG_OPEN, event);
        return NpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onDialogOption(EntityPlayer player, DialogEvent.DialogOption event) {
        PlayerDataScript handler = ScriptController.Instance.getPlayerScripts(player);
        handler.callScript(EnumScriptType.DIALOG_OPTION, event);
        return NpcAPI.EVENT_BUS.post(event);
    }

    public static void onDialogClosed(EntityPlayer player, DialogEvent.DialogClosed event) {
        PlayerDataScript handler = ScriptController.Instance.getPlayerScripts(player);
        handler.callScript(EnumScriptType.DIALOG_CLOSE, event);
        NpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onScriptBlockInteract(IScriptBlockHandler handler, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        if (handler.isClient())
            return false;
        BlockEvent.InteractEvent event = new BlockEvent.InteractEvent(handler.getBlock(), player, side, hitX, hitY, hitZ);
        handler.callScript(EnumScriptType.INTERACT, event);
        return NpcAPI.EVENT_BUS.post(event);
    }

    public static void onScriptBlockCollide(IScriptBlockHandler handler, Entity entityIn) {
        if (handler.isClient())
            return;
        BlockEvent.CollidedEvent event = new BlockEvent.CollidedEvent(handler.getBlock(), entityIn);
        handler.callScript(EnumScriptType.COLLIDE, event);
        NpcAPI.EVENT_BUS.post(event);
    }

    public static void onScriptBlockRainFill(IScriptBlockHandler handler) {
        if (handler.isClient())
            return;
        BlockEvent.RainFillEvent event = new BlockEvent.RainFillEvent(handler.getBlock());
        handler.callScript(EnumScriptType.RAIN_FILLED, event);
        NpcAPI.EVENT_BUS.post(event);
    }

    public static float onScriptBlockFallenUpon(IScriptBlockHandler handler, Entity entity, float distance) {
        if (handler.isClient())
            return distance;
        BlockEvent.EntityFallenUponEvent event = new BlockEvent.EntityFallenUponEvent(handler.getBlock(), entity, distance);
        handler.callScript(EnumScriptType.FALLEN_UPON, event);
        if (NpcAPI.EVENT_BUS.post(event))
            return 0;
        return event.distanceFallen;
    }

    public static void onScriptBlockClicked(IScriptBlockHandler handler, EntityPlayer player) {
        if (handler.isClient())
            return;
        BlockEvent.ClickedEvent event = new BlockEvent.ClickedEvent(handler.getBlock(), player);
        handler.callScript(EnumScriptType.CLICKED, event);
        NpcAPI.EVENT_BUS.post(event);
    }

    public static void onScriptBlockBreak(IScriptBlockHandler handler) {
        if (handler.isClient())
            return;
        BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(handler.getBlock());
        handler.callScript(EnumScriptType.BROKEN, event);
        NpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onScriptBlockHarvest(IScriptBlockHandler handler, EntityPlayer player) {
        if (handler.isClient())
            return false;
        BlockEvent.HarvestedEvent event = new BlockEvent.HarvestedEvent(handler.getBlock(), player);
        handler.callScript(EnumScriptType.HARVESTED, event);
        return NpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onScriptBlockExploded(IScriptBlockHandler handler) {
        if (handler.isClient())
            return false;
        BlockEvent.ExplodedEvent event = new BlockEvent.ExplodedEvent(handler.getBlock());
        handler.callScript(EnumScriptType.EXPLODED, event);
        return NpcAPI.EVENT_BUS.post(event);
    }

    public static void onScriptBlockNeighborChanged(IScriptBlockHandler handler, int x, int y, int z) {
        if (handler.isClient())
            return;
        BlockEvent.NeighborChangedEvent event = new BlockEvent.NeighborChangedEvent(handler.getBlock(), NpcAPI.Instance().getIPos(x, y, z));
        handler.callScript(EnumScriptType.NEIGHBOR_CHANGED, event);
        NpcAPI.EVENT_BUS.post(event);
    }

    public static void onScriptBlockRedstonePower(IScriptBlockHandler handler, int prevPower, int power) {
        if (handler.isClient())
            return;
        BlockEvent.RedstoneEvent event = new BlockEvent.RedstoneEvent(handler.getBlock(), prevPower, power);
        handler.callScript(EnumScriptType.REDSTONE, event);
        NpcAPI.EVENT_BUS.post(event);
    }

    public static void onScriptBlockInit(IScriptBlockHandler handler) {
        if (handler.isClient())
            return;
        BlockEvent.InitEvent event = new BlockEvent.InitEvent(handler.getBlock());
        handler.callScript(EnumScriptType.INIT, event);
        NpcAPI.EVENT_BUS.post(event);
    }

    public static void onScriptBlockUpdate(IScriptBlockHandler handler) {
        if (handler.isClient())
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

    public static void onPartyFinished(Party party, Quest quest) {
        EntityPlayer player = party.getPartyLeader();
        if (player != null) {
            PlayerDataScript handler = ScriptController.Instance.getPlayerScripts(player);
            PartyEvent.PartyQuestCompletedEvent event = new PartyEvent.PartyQuestCompletedEvent(party, quest);
            handler.callScript(EnumScriptType.PARTY_QUEST_COMPLETED, event);
            NpcAPI.EVENT_BUS.post(event);
        }
    }

    public static void onPartyQuestSet(Party party, PartyEvent.PartyQuestSetEvent event) {
        EntityPlayer player = party.getPartyLeader();
        if (player != null) {
            PlayerDataScript handler = ScriptController.Instance.getPlayerScripts(player);
            handler.callScript(EnumScriptType.PARTY_QUEST_SET, event);
            NpcAPI.EVENT_BUS.post(event);
        }
    }

    public static void onPartyTurnIn(Party party, PartyEvent.PartyQuestTurnedInEvent event) {
        EntityPlayer player = party.getPartyLeader();
        if (player != null) {
            PlayerDataScript handler = ScriptController.Instance.getPlayerScripts(player);
            handler.callScript(EnumScriptType.PARTY_QUEST_TURNED_IN, event);
            NpcAPI.EVENT_BUS.post(event);
        }
    }

    public static void onPartyInvite(Party party, PartyEvent.PartyInviteEvent event) {
        EntityPlayer player = party.getPartyLeader();
        if (player != null) {
            PlayerDataScript handler = ScriptController.Instance.getPlayerScripts(player);
            handler.callScript(EnumScriptType.PARTY_INVITE, event);
            NpcAPI.EVENT_BUS.post(event);
        }
    }

    public static void onPartyKick(Party party, PartyEvent.PartyKickEvent event) {
        EntityPlayer player = party.getPartyLeader();
        if (player != null) {
            PlayerDataScript handler = ScriptController.Instance.getPlayerScripts(player);
            handler.callScript(EnumScriptType.PARTY_KICK, event);
            NpcAPI.EVENT_BUS.post(event);
        }
    }

    public static void onPartyLeave(Party party, PartyEvent.PartyLeaveEvent event) {
        EntityPlayer player = party.getPartyLeader();
        if (player != null) {
            PlayerDataScript handler = ScriptController.Instance.getPlayerScripts(player);
            handler.callScript(EnumScriptType.PARTY_LEAVE, event);
            NpcAPI.EVENT_BUS.post(event);
        }
    }

    public static void onPartyDisband(Party party, PartyEvent.PartyDisbandEvent event) {
        EntityPlayer player = party.getPartyLeader();
        if (player != null) {
            PlayerDataScript handler = ScriptController.Instance.getPlayerScripts(player);
            handler.callScript(EnumScriptType.PARTY_DISBAND, event);
            NpcAPI.EVENT_BUS.post(event);
        }
    }

    private static boolean postAnimationEvent(IAnimationEvent event) {
        IScriptHandler handler;
        IAnimatable animatable = event.getAnimation().getParent().getEntity();
        if (animatable instanceof ICustomNpc<?>) {
            EntityNPCInterface npc = (EntityNPCInterface) ((ICustomNpc<?>) animatable).getMCEntity();
            handler = npc.script;
        } else {
            handler = ScriptController.Instance.getPlayerScripts((IPlayer) animatable);
        }

        if (handler.isClient())
            return false;

        handler.callScript(event.getHookName(), (Event) event);
        return NpcAPI.EVENT_BUS.post((Event) event);
    }

    public static boolean onAnimationStarted(IAnimation animation) {
        if (animation.getParent() == null || animation.getParent().getEntity() == null) {
            return false;
        }
        return postAnimationEvent(new AnimationEvent.Started(animation));
    }

    public static void onAnimationEnded(IAnimation animation) {
        if (animation.getParent() == null || animation.getParent().getEntity() == null) {
            return;
        }
        postAnimationEvent(new AnimationEvent.Ended(animation));
    }

    public static void onAnimationFrameEntered(IAnimation animation, IFrame frame) {
        if (frame == null || animation.getParent() == null || animation.getParent().getEntity() == null) {
            return;
        }
        postAnimationEvent(new AnimationEvent.FrameEvent.Entered(animation, frame));
    }

    public static void onAnimationFrameExited(IAnimation animation, IFrame frame) {
        if (frame == null || animation.getParent() == null || animation.getParent().getEntity() == null) {
            return;
        }
        postAnimationEvent(new AnimationEvent.FrameEvent.Exited(animation, frame));
    }
}
