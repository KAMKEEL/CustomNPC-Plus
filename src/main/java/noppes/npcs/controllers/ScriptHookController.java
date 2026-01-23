package noppes.npcs.controllers;

import noppes.npcs.api.event.IAbilityEvent;
import noppes.npcs.api.event.IAnimationEvent;
import noppes.npcs.api.event.IBlockEvent;
import noppes.npcs.api.event.ICustomGuiEvent;
import noppes.npcs.api.event.ICustomNPCsEvent;
import noppes.npcs.api.event.IDialogEvent;
import noppes.npcs.api.event.IFactionEvent;
import noppes.npcs.api.event.IForgeEvent;
import noppes.npcs.api.event.IItemEvent;
import noppes.npcs.api.event.ILinkedItemEvent;
import noppes.npcs.api.event.INpcEvent;
import noppes.npcs.api.event.IPartyEvent;
import noppes.npcs.api.event.IPlayerEvent;
import noppes.npcs.api.event.IProjectileEvent;
import noppes.npcs.api.event.IQuestEvent;
import noppes.npcs.api.event.IRecipeEvent;
import noppes.npcs.api.handler.IHookDefinition;
import noppes.npcs.api.handler.IScriptHookHandler;
import noppes.npcs.constants.EnumScriptType;
import noppes.npcs.constants.ScriptContext;
import noppes.npcs.controllers.data.RecipeScript;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static noppes.npcs.constants.EnumScriptType.*;
import static noppes.npcs.constants.ScriptContext.BLOCK;
import static noppes.npcs.constants.ScriptContext.EFFECT;
import static noppes.npcs.constants.ScriptContext.FORGE;
import static noppes.npcs.constants.ScriptContext.ITEM;
import static noppes.npcs.constants.ScriptContext.LINKED_ITEM;
import static noppes.npcs.constants.ScriptContext.NPC;
import static noppes.npcs.constants.ScriptContext.PLAYER;
import static noppes.npcs.constants.ScriptContext.RECIPE;

/**
 * Controller for managing script hooks.
 * Allows addon mods to register custom hooks that appear in script editor GUIs.
 *
 * <h3>Addon Hook Registration</h3>
 * <pre>{@code
 * ScriptHookController.Instance.registerHook("player",
 *     HookDefinition.of("onDBCTransform", IDBCEvent.TransformEvent.class));
 * }</pre>
 */
public class ScriptHookController implements IScriptHookHandler {

    public static ScriptHookController Instance;

    private final Map<String, Map<String, HookDefinition>> hookDefinitions = new HashMap<>();
    private int hookRevision = 0;

    public ScriptHookController() {
        Instance = this;
        initializeBuiltInHooks();
    }

    // ==================== INITIALIZATION ====================

    private void initializeBuiltInHooks() {
        for (ScriptContext context : ScriptContext.values()) {
            if (!context.hookContext.isEmpty()) {
                hookDefinitions.put(context.hookContext, new LinkedHashMap<>());
            }
        }

        initializeNpcHooks();
        initializePlayerHooks();
        initializeBlockHooks();
        initializeItemHooks();
        initializeLinkedItemHooks();
        initializeRecipeHooks();
        initializeEffectHooks();
        initializeForgeHooks();
    }

    private void initializeNpcHooks() {
        // Core lifecycle
        hook(NPC, INIT, INpcEvent.InitEvent.class);
        hook(NPC, TICK, INpcEvent.UpdateEvent.class);
        hook(NPC, TIMER, INpcEvent.TimerEvent.class);

        // Interaction
        hook(NPC, INTERACT, INpcEvent.InteractEvent.class);
        hook(NPC, DIALOG, INpcEvent.DialogEvent.class);
        hook(NPC, DIALOG_CLOSE, INpcEvent.DialogClosedEvent.class);
        hook(NPC, COLLIDE, INpcEvent.CollideEvent.class);

        // Combat
        hook(NPC, DAMAGED, INpcEvent.DamagedEvent.class);
        hook(NPC, KILLED, INpcEvent.DiedEvent.class);
        hook(NPC, KILLS, INpcEvent.KilledEntityEvent.class);
        hook(NPC, ATTACK_MELEE, INpcEvent.MeleeAttackEvent.class);
        hook(NPC, ATTACK_SWING, INpcEvent.SwingEvent.class);
        hook(NPC, RANGED_LAUNCHED, INpcEvent.RangedLaunchedEvent.class);
        hook(NPC, TARGET, INpcEvent.TargetEvent.class);
        hook(NPC, TARGET_LOST, INpcEvent.TargetLostEvent.class);

        // Projectile
        hook(NPC, PROJECTILE_TICK, IProjectileEvent.UpdateEvent.class);
        hook(NPC, PROJECTILE_IMPACT, IProjectileEvent.ImpactEvent.class);

        // Ability
        hook(NPC, ABILITY_START, IAbilityEvent.StartEvent.class);
        hook(NPC, ABILITY_EXECUTE, IAbilityEvent.ExecuteEvent.class);
        hook(NPC, ABILITY_HIT, IAbilityEvent.HitEvent.class);
        hook(NPC, ABILITY_TICK, IAbilityEvent.TickEvent.class);
        hook(NPC, ABILITY_INTERRUPT, IAbilityEvent.InterruptEvent.class);
        hook(NPC, ABILITY_COMPLETE, IAbilityEvent.CompleteEvent.class);
    }

    private void initializePlayerHooks() {
        // Core lifecycle
        hook(PLAYER, INIT, IPlayerEvent.InitEvent.class);
        hook(PLAYER, TICK, IPlayerEvent.UpdateEvent.class);
        hook(PLAYER, TIMER, IPlayerEvent.TimerEvent.class);

        // Combat
        hook(PLAYER, ATTACK, IPlayerEvent.AttackEvent.class);
        hook(PLAYER, ATTACKED, IPlayerEvent.AttackedEvent.class);
        hook(PLAYER, DAMAGED, IPlayerEvent.DamagedEvent.class);
        hook(PLAYER, DAMAGED_ENTITY, IPlayerEvent.DamagedEntityEvent.class);
        hook(PLAYER, KILLS, IPlayerEvent.KilledEntityEvent.class);
        hook(PLAYER, KILLED, IPlayerEvent.DiedEvent.class);

        // Interaction
        hook(PLAYER, INTERACT, IPlayerEvent.InteractEvent.class);
        hook(PLAYER, RIGHT_CLICK, IPlayerEvent.RightClickEvent.class);
        hook(PLAYER, BREAK_BLOCK, IPlayerEvent.BreakEvent.class);
        hook(PLAYER, CHAT, IPlayerEvent.ChatEvent.class);

        // Connection
        hook(PLAYER, LOGIN, IPlayerEvent.LoginEvent.class);
        hook(PLAYER, LOGOUT, IPlayerEvent.LogoutEvent.class);
        hook(PLAYER, RESPAWN, IPlayerEvent.RespawnEvent.class);

        // Input
        hook(PLAYER, KEY_PRESSED, IPlayerEvent.KeyPressedEvent.class);
        hook(PLAYER, MOUSE_CLICKED, IPlayerEvent.MouseClickedEvent.class);

        // Items
        hook(PLAYER, PICKUP, IPlayerEvent.PickUpEvent.class);
        hook(PLAYER, TOSS, IPlayerEvent.TossEvent.class);
        hook(PLAYER, DROP, IPlayerEvent.DropEvent.class);
        hook(PLAYER, PICKUP_XP, IPlayerEvent.PickupXPEvent.class);
        hook(PLAYER, START_USING_ITEM, IPlayerEvent.StartUsingItem.class);
        hook(PLAYER, USING_ITEM, IPlayerEvent.UsingItem.class);
        hook(PLAYER, STOP_USING_ITEM, IPlayerEvent.StopUsingItem.class);
        hook(PLAYER, FINISH_USING_ITEM, IPlayerEvent.FinishUsingItem.class);
        hook(PLAYER, CONTAINER_OPEN, IPlayerEvent.ContainerOpen.class);

        // Movement
        hook(PLAYER, JUMP, IPlayerEvent.JumpEvent.class);
        hook(PLAYER, FALL, IPlayerEvent.FallEvent.class);
        hook(PLAYER, CHANGED_DIM, IPlayerEvent.ChangedDimension.class);

        // World
        hook(PLAYER, RANGED_CHARGE, IPlayerEvent.RangedChargeEvent.class);
        hook(PLAYER, RANGED_LAUNCHED, IPlayerEvent.RangedLaunchedEvent.class);
        hook(PLAYER, USE_HOE, IPlayerEvent.UseHoeEvent.class);
        hook(PLAYER, BONEMEAL, IPlayerEvent.BonemealEvent.class);
        hook(PLAYER, FILL_BUCKET, IPlayerEvent.FillBucketEvent.class);
        hook(PLAYER, WAKE_UP, IPlayerEvent.WakeUpEvent.class);
        hook(PLAYER, SLEEP, IPlayerEvent.SleepEvent.class);
        hook(PLAYER, PLAYSOUND, IPlayerEvent.SoundEvent.class);
        hook(PLAYER, LIGHTNING, IPlayerEvent.LightningEvent.class);
        hook(PLAYER, SCRIPT_COMMAND, ICustomNPCsEvent.ScriptedCommandEvent.class);

        // Quest
        hook(PLAYER, QUEST_START, IQuestEvent.QuestStartEvent.class);
        hook(PLAYER, QUEST_COMPLETED, IQuestEvent.QuestCompletedEvent.class);
        hook(PLAYER, QUEST_TURNIN, IQuestEvent.QuestTurnedInEvent.class);

        // Dialog
        hook(PLAYER, DIALOG_OPEN, IDialogEvent.DialogOpen.class);
        hook(PLAYER, DIALOG_OPTION, IDialogEvent.DialogOption.class);
        hook(PLAYER, DIALOG_CLOSE, IDialogEvent.DialogClosed.class);

        // Faction
        hook(PLAYER, FACTION_POINTS, IFactionEvent.FactionPoints.class);

        // Custom GUI
        hook(PLAYER, CUSTOM_GUI_CLOSED, ICustomGuiEvent.CloseEvent.class);
        hook(PLAYER, CUSTOM_GUI_BUTTON, ICustomGuiEvent.ButtonEvent.class);
        hook(PLAYER, CUSTOM_GUI_SLOT, ICustomGuiEvent.SlotEvent.class);
        hook(PLAYER, CUSTOM_GUI_SLOT_CLICKED, ICustomGuiEvent.SlotClickEvent.class);
        hook(PLAYER, CUSTOM_GUI_SCROLL, ICustomGuiEvent.ScrollEvent.class);
        hook(PLAYER, CUSTOM_GUI_TEXTFIELD, ICustomGuiEvent.UnfocusedEvent.class);

        // Party
        hook(PLAYER, PARTY_QUEST_COMPLETED, IPartyEvent.PartyQuestCompletedEvent.class);
        hook(PLAYER, PARTY_QUEST_SET, IPartyEvent.PartyQuestSetEvent.class);
        hook(PLAYER, PARTY_QUEST_TURNED_IN, IPartyEvent.PartyQuestTurnedInEvent.class);
        hook(PLAYER, PARTY_INVITE, IPartyEvent.PartyInviteEvent.class);
        hook(PLAYER, PARTY_KICK, IPartyEvent.PartyKickEvent.class);
        hook(PLAYER, PARTY_LEAVE, IPartyEvent.PartyLeaveEvent.class);
        hook(PLAYER, PARTY_DISBAND, IPartyEvent.PartyDisbandEvent.class);

        // Animation
        hook(PLAYER, ANIMATION_START, IAnimationEvent.Started.class);
        hook(PLAYER, ANIMATION_END, IAnimationEvent.Ended.class);
        hook(PLAYER, ANIMATION_FRAME_ENTER, IAnimationEvent.IFrameEvent.Entered.class);
        hook(PLAYER, ANIMATION_FRAME_EXIT, IAnimationEvent.IFrameEvent.Exited.class);

        // Profile
        hook(PLAYER, PROFILE_CHANGE, IPlayerEvent.ProfileEvent.Changed.class);
        hook(PLAYER, PROFILE_REMOVE, IPlayerEvent.ProfileEvent.Removed.class);
        hook(PLAYER, PROFILE_CREATE, IPlayerEvent.ProfileEvent.Create.class);

        // Effect (player context)
        hook(PLAYER, ON_EFFECT_ADD, IPlayerEvent.EffectEvent.Added.class);
        hook(PLAYER, ON_EFFECT_TICK, IPlayerEvent.EffectEvent.Ticked.class);
        hook(PLAYER, ON_EFFECT_REMOVE, IPlayerEvent.EffectEvent.Removed.class);
    }

    private void initializeBlockHooks() {
        hook(BLOCK, INIT, IBlockEvent.InitEvent.class);
        hook(BLOCK, TICK, IBlockEvent.UpdateEvent.class);
        hook(BLOCK, INTERACT, IBlockEvent.InteractEvent.class);
        hook(BLOCK, FALLEN_UPON, IBlockEvent.EntityFallenUponEvent.class);
        hook(BLOCK, REDSTONE, IBlockEvent.RedstoneEvent.class);
        hook(BLOCK, BROKEN, IBlockEvent.BreakEvent.class);
        hook(BLOCK, EXPLODED, IBlockEvent.ExplodedEvent.class);
        hook(BLOCK, RAIN_FILLED, IBlockEvent.RainFillEvent.class);
        hook(BLOCK, NEIGHBOR_CHANGED, IBlockEvent.NeighborChangedEvent.class);
        hook(BLOCK, CLICKED, IBlockEvent.ClickedEvent.class);
        hook(BLOCK, HARVESTED, IBlockEvent.HarvestedEvent.class);
        hook(BLOCK, COLLIDE, IBlockEvent.CollidedEvent.class);
        hook(BLOCK, TIMER, IBlockEvent.TimerEvent.class);
    }

    private void initializeItemHooks() {
        hook(ITEM, INIT, IItemEvent.InitEvent.class);
        hook(ITEM, TICK, IItemEvent.UpdateEvent.class);
        hook(ITEM, TOSSED, IItemEvent.TossedEvent.class);
        hook(ITEM, PICKEDUP, IItemEvent.PickedUpEvent.class);
        hook(ITEM, SPAWN, IItemEvent.SpawnEvent.class);
        hook(ITEM, INTERACT, IItemEvent.InteractEvent.class);
        hook(ITEM, RIGHT_CLICK, IItemEvent.RightClickEvent.class);
        hook(ITEM, ATTACK, IItemEvent.AttackEvent.class);
        hook(ITEM, START_USING_ITEM, IItemEvent.StartUsingItem.class);
        hook(ITEM, USING_ITEM, IItemEvent.UsingItem.class);
        hook(ITEM, STOP_USING_ITEM, IItemEvent.StopUsingItem.class);
        hook(ITEM, FINISH_USING_ITEM, IItemEvent.FinishUsingItem.class);
    }

    private void initializeLinkedItemHooks() {
        // Linked item specific
        hook(LINKED_ITEM, LINKED_ITEM_BUILD, ILinkedItemEvent.BuildEvent.class);
        hook(LINKED_ITEM, LINKED_ITEM_VERSION, ILinkedItemEvent.VersionChangeEvent.class);

        // Standard item hooks
        hook(LINKED_ITEM, INIT, IItemEvent.InitEvent.class);
        hook(LINKED_ITEM, TICK, IItemEvent.UpdateEvent.class);
        hook(LINKED_ITEM, TOSSED, IItemEvent.TossedEvent.class);
        hook(LINKED_ITEM, PICKEDUP, IItemEvent.PickedUpEvent.class);
        hook(LINKED_ITEM, SPAWN, IItemEvent.SpawnEvent.class);
        hook(LINKED_ITEM, INTERACT, IItemEvent.InteractEvent.class);
        hook(LINKED_ITEM, RIGHT_CLICK, IItemEvent.RightClickEvent.class);
        hook(LINKED_ITEM, ATTACK, IItemEvent.AttackEvent.class);
        hook(LINKED_ITEM, START_USING_ITEM, IItemEvent.StartUsingItem.class);
        hook(LINKED_ITEM, USING_ITEM, IItemEvent.UsingItem.class);
        hook(LINKED_ITEM, STOP_USING_ITEM, IItemEvent.StopUsingItem.class);
        hook(LINKED_ITEM, FINISH_USING_ITEM, IItemEvent.FinishUsingItem.class);
    }

    private void initializeRecipeHooks() {
        hook(RECIPE, RecipeScript.ScriptType.PRE.function, IRecipeEvent.Pre.class);
        hook(RECIPE, RecipeScript.ScriptType.POST.function, IRecipeEvent.Post.class);
    }

    private void initializeEffectHooks() {
        // Effect context uses IPlayerEvent.EffectEvent (same as player context)
        hook(EFFECT, ON_EFFECT_ADD, IPlayerEvent.EffectEvent.Added.class);
        hook(EFFECT, ON_EFFECT_TICK, IPlayerEvent.EffectEvent.Ticked.class);
        hook(EFFECT, ON_EFFECT_REMOVE, IPlayerEvent.EffectEvent.Removed.class);
    }

    private void initializeForgeHooks() {
        hook(FORGE, INIT, IForgeEvent.InitEvent.class);
        hook(FORGE, FORGE_WORLD, IForgeEvent.WorldEvent.class);
        hook(FORGE, FORGE_ENTITY, IForgeEvent.EntityEvent.class);
        hook(FORGE, CNPC_NATURAL_SPAWN, ICustomNPCsEvent.CNPCNaturalSpawnEvent.class);
    }

    // ==================== INTERNAL REGISTRATION ====================

    private void hook(ScriptContext context, EnumScriptType type, Class<?> eventClass) {
        hookDefinitions.get(context.hookContext)
            .put(type.function, HookDefinition.of(type.function, eventClass));
    }

    private void hook(ScriptContext context, EnumScriptType type) {
        hookDefinitions.get(context.hookContext)
            .put(type.function, HookDefinition.simple(type.function));
    }

    private void hook(ScriptContext context, String hookName) {
        hookDefinitions.get(context.hookContext)
            .put(hookName, HookDefinition.simple(hookName));
    }

    private void hook(ScriptContext context, String hookName, Class<?> eventClass) {
        hookDefinitions.get(context.hookContext)
            .put(hookName, HookDefinition.of(hookName, eventClass));
    }

    // ==================== PUBLIC API ====================

    /**
     * Register a hook with event class (auto-derives imports).
     * For addon mods to register custom hooks.
     *
     * @param context    Hook context (e.g., "npc", "player")
     * @param hookName   Function name in scripts
     * @param eventClass The event class (imports auto-derived from enclosing class)
     */
    public void registerHook(String context, String hookName, Class<?> eventClass) {
        if (context == null || hookName == null)
            return;

        Map<String, HookDefinition> contextDefs = hookDefinitions.get(context);
        if (contextDefs == null) {
            contextDefs = new LinkedHashMap<>();
            hookDefinitions.put(context, contextDefs);
        }

        contextDefs.put(hookName, HookDefinition.of(hookName, eventClass));
        hookRevision++;
    }

    public void registerHook(ScriptContext context, String hookName, Class<?> eventClass) {
        registerHook(context.hookContext, hookName, eventClass);
    }

    /**
     * Register a simple hook with no event class metadata.
     */
    public void registerHook(String context, String hookName) {
        if (context == null || hookName == null)
            return;

        Map<String, HookDefinition> contextDefs = hookDefinitions.get(context);
        if (contextDefs == null) {
            contextDefs = new LinkedHashMap<>();
            hookDefinitions.put(context, contextDefs);
        }

        if (!contextDefs.containsKey(hookName)) {
            contextDefs.put(hookName, HookDefinition.simple(hookName));
            hookRevision++;
        }
    }

    public void registerHook(ScriptContext context, String hookName) {
        registerHook(context.hookContext, hookName);
    }

    /**
     * Register a hook definition for addon mods.
     */
    public void registerHook(String context, HookDefinition definition) {
        if (context == null || definition == null)
            return;

        Map<String, HookDefinition> contextDefs = hookDefinitions.get(context);
        if (contextDefs == null) {
            contextDefs = new LinkedHashMap<>();
            hookDefinitions.put(context, contextDefs);
        }

        contextDefs.put(definition.hookName(), definition);
        hookRevision++;
    }

    public void registerHook(ScriptContext context, HookDefinition definition) {
        registerHook(context.hookContext, definition);
    }

    @Override
    public void registerHookDefinition(String context, IHookDefinition definition) {
        if (definition instanceof HookDefinition) {
            registerHook(context, (HookDefinition) definition);
        } else if (definition != null) {
            registerHook(context, HookDefinition.builder(definition.hookName())
                .eventClass(definition.eventClassName())
                .paramNames(definition.paramNames())
                .requiredImports(definition.requiredImports())
                .cancelable(definition.isCancelable())
                .build());
        }
    }

    @Override
    public IHookDefinition getHookDefinition(String context, String hookName) {
        if (context == null || hookName == null)
            return null;

        Map<String, HookDefinition> contextDefs = hookDefinitions.get(context);
        return contextDefs != null ? contextDefs.get(hookName) : null;
    }

    @Override
    public List<IHookDefinition> getAllHookDefinitions(String context) {
        if (context == null)
            return Collections.emptyList();

        Map<String, HookDefinition> contextDefs = hookDefinitions.get(context);
        if (contextDefs == null || contextDefs.isEmpty())
            return Collections.emptyList();

        return new ArrayList<>(contextDefs.values());
    }

    @Override
    public List<String> getAllHooks(String context) {
        if (context == null)
            return Collections.emptyList();

        Map<String, HookDefinition> contextDefs = hookDefinitions.get(context);
        if (contextDefs == null || contextDefs.isEmpty())
            return Collections.emptyList();

        return new ArrayList<>(contextDefs.keySet());
    }

    @Override
    public boolean hasHook(String context, String hookName) {
        if (context == null || hookName == null)
            return false;

        Map<String, HookDefinition> contextDefs = hookDefinitions.get(context);
        return contextDefs != null && contextDefs.containsKey(hookName);
    }

    @Override
    public int getHookRevision() {
        return hookRevision;
    }

    @Override
    public String[] getContexts() {
        return hookDefinitions.keySet().toArray(new String[0]);
    }
}
