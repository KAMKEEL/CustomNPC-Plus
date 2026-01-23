package noppes.npcs.controllers;

import noppes.npcs.api.handler.IHookDefinition;
import noppes.npcs.api.handler.IScriptHookHandler;
import noppes.npcs.constants.EnumScriptType;
import noppes.npcs.constants.ScriptContext;
import noppes.npcs.controllers.data.RecipeScript;

import java.util.*;

/**
 * Controller for managing script hooks.
 * Allows addon mods to register custom hooks that appear in script editor GUIs.
 *
 * <h3>Rich Hook Registration</h3>
 * Supports full hook definitions with metadata for proper stub generation:
 * <pre>{@code
 * ScriptHookController.Instance.registerHookDefinition("player",
 *     HookDefinition.builder("onDBCTransform")
 *         .eventClass(IDBCEvent.TransformEvent.class)
 *         .requiredImports("com.dbc.api.event.IDBCEvent")
 *         .build());
 * }</pre>
 */
public class ScriptHookController implements IScriptHookHandler {

    public static ScriptHookController Instance;

    // Hook definitions per context - single source of truth
    private final Map<String, Map<String, HookDefinition>> hookDefinitions = new HashMap<>();

    // Revision counter for cache invalidation
    private int hookRevision = 0;

    public ScriptHookController() {
        Instance = this;
        initializeBuiltInHooks();
    }

    private void initializeBuiltInHooks() {
        // Initialize empty maps for all contexts from ScriptContext
        for (ScriptContext context : ScriptContext.values()) {
            if (!context.hookContext.isEmpty()) {
                hookDefinitions.put(context.hookContext, new LinkedHashMap<>());
            }
        }

        // NPC hooks
        initializeNpcHooks();

        // Player hooks
        initializePlayerHooks();

        // Block hooks
        initializeBlockHooks();

        // Item hooks
        initializeItemHooks();

        // Linked item hooks
        initializeLinkedItemHooks();

        // Recipe hooks
        registerHookDef(ScriptContext.RECIPE, RecipeScript.ScriptType.PRE.function, null, null);
        registerHookDef(ScriptContext.RECIPE, RecipeScript.ScriptType.POST.function, null, null);

        // Effect hooks
        registerHookDef(ScriptContext.EFFECT, EnumScriptType.ON_EFFECT_ADD.function, null, null);
        registerHookDef(ScriptContext.EFFECT, EnumScriptType.ON_EFFECT_TICK.function, null, null);
        registerHookDef(ScriptContext.EFFECT, EnumScriptType.ON_EFFECT_REMOVE.function, null, null);

        // Forge hooks
        registerHookDef(ScriptContext.FORGE, EnumScriptType.INIT.function, null, null);
        registerHookDef(ScriptContext.FORGE, EnumScriptType.CNPC_NATURAL_SPAWN.function, null, null);
    }

    private void initializeNpcHooks() {
        String ctx = ScriptContext.NPC.hookContext;
        String npcImport = "noppes.npcs.api.event.INpcEvent";
        String projImport = "noppes.npcs.api.event.IProjectileEvent";
        String abilityImport = "noppes.npcs.api.event.IAbilityEvent";

        // Core lifecycle
        registerHookDef(ctx, EnumScriptType.INIT.function, "noppes.npcs.api.event.INpcEvent$InitEvent", npcImport);
        registerHookDef(ctx, EnumScriptType.TICK.function, "noppes.npcs.api.event.INpcEvent$UpdateEvent", npcImport);
        registerHookDef(ctx, EnumScriptType.TIMER.function, "noppes.npcs.api.event.INpcEvent$TimerEvent", npcImport);

        // Interaction
        registerHookDef(ctx, EnumScriptType.INTERACT.function, "noppes.npcs.api.event.INpcEvent$InteractEvent", npcImport);
        registerHookDef(ctx, EnumScriptType.DIALOG.function, "noppes.npcs.api.event.INpcEvent$DialogEvent", npcImport);
        registerHookDef(ctx, EnumScriptType.DIALOG_CLOSE.function, "noppes.npcs.api.event.INpcEvent$DialogClosedEvent", npcImport);
        registerHookDef(ctx, EnumScriptType.COLLIDE.function, "noppes.npcs.api.event.INpcEvent$CollideEvent", npcImport);

        // Combat
        registerHookDef(ctx, EnumScriptType.DAMAGED.function, "noppes.npcs.api.event.INpcEvent$DamagedEvent", npcImport);
        registerHookDef(ctx, EnumScriptType.KILLED.function, "noppes.npcs.api.event.INpcEvent$DiedEvent", npcImport);
        registerHookDef(ctx, EnumScriptType.KILLS.function, "noppes.npcs.api.event.INpcEvent$KilledEntityEvent", npcImport);
        registerHookDef(ctx, EnumScriptType.ATTACK_MELEE.function, "noppes.npcs.api.event.INpcEvent$MeleeAttackEvent", npcImport);
        registerHookDef(ctx, EnumScriptType.ATTACK_SWING.function, "noppes.npcs.api.event.INpcEvent$SwingEvent", npcImport);
        registerHookDef(ctx, EnumScriptType.RANGED_LAUNCHED.function, "noppes.npcs.api.event.INpcEvent$RangedLaunchedEvent", npcImport);
        registerHookDef(ctx, EnumScriptType.TARGET.function, "noppes.npcs.api.event.INpcEvent$TargetEvent", npcImport);
        registerHookDef(ctx, EnumScriptType.TARGET_LOST.function, "noppes.npcs.api.event.INpcEvent$TargetLostEvent", npcImport);

        // Projectile events
        registerHookDef(ctx, EnumScriptType.PROJECTILE_TICK.function, "noppes.npcs.api.event.IProjectileEvent$UpdateEvent", projImport);
        registerHookDef(ctx, EnumScriptType.PROJECTILE_IMPACT.function, "noppes.npcs.api.event.IProjectileEvent$ImpactEvent", projImport);

        // Ability events
        registerHookDef(ctx, EnumScriptType.ABILITY_START.function, "noppes.npcs.api.event.IAbilityEvent$StartEvent", abilityImport);
        registerHookDef(ctx, EnumScriptType.ABILITY_EXECUTE.function, "noppes.npcs.api.event.IAbilityEvent$ExecuteEvent", abilityImport);
        registerHookDef(ctx, EnumScriptType.ABILITY_HIT.function, "noppes.npcs.api.event.IAbilityEvent$HitEvent", abilityImport);
        registerHookDef(ctx, EnumScriptType.ABILITY_TICK.function, "noppes.npcs.api.event.IAbilityEvent$TickEvent", abilityImport);
        registerHookDef(ctx, EnumScriptType.ABILITY_INTERRUPT.function, "noppes.npcs.api.event.IAbilityEvent$InterruptEvent", abilityImport);
        registerHookDef(ctx, EnumScriptType.ABILITY_COMPLETE.function, "noppes.npcs.api.event.IAbilityEvent$CompleteEvent", abilityImport);
    }

    private void initializePlayerHooks() {
        String ctx = ScriptContext.PLAYER.hookContext;
        String playerImport = "noppes.npcs.api.event.IPlayerEvent";
        String questImport = "noppes.npcs.api.event.IQuestEvent";
        String dialogImport = "noppes.npcs.api.event.IDialogEvent";
        String factionImport = "noppes.npcs.api.event.IFactionEvent";
        String partyImport = "noppes.npcs.api.event.IPartyEvent";
        String guiImport = "noppes.npcs.api.event.ICustomGuiEvent";
        String animImport = "noppes.npcs.api.event.IAnimationEvent";

        // Core lifecycle
        registerHookDef(ctx, EnumScriptType.INIT.function, "noppes.npcs.api.event.IPlayerEvent$InitEvent", playerImport);
        registerHookDef(ctx, EnumScriptType.TICK.function, "noppes.npcs.api.event.IPlayerEvent$UpdateEvent", playerImport);
        registerHookDef(ctx, EnumScriptType.TIMER.function, "noppes.npcs.api.event.IPlayerEvent$TimerEvent", playerImport);

        // Combat
        registerHookDef(ctx, EnumScriptType.ATTACK.function, "noppes.npcs.api.event.IPlayerEvent$AttackEvent", playerImport);
        registerHookDef(ctx, EnumScriptType.ATTACKED.function, "noppes.npcs.api.event.IPlayerEvent$AttackedEvent", playerImport);
        registerHookDef(ctx, EnumScriptType.DAMAGED.function, "noppes.npcs.api.event.IPlayerEvent$DamagedEvent", playerImport);
        registerHookDef(ctx, EnumScriptType.DAMAGED_ENTITY.function, "noppes.npcs.api.event.IPlayerEvent$DamagedEntityEvent", playerImport);
        registerHookDef(ctx, EnumScriptType.KILLS.function, "noppes.npcs.api.event.IPlayerEvent$KilledEntityEvent", playerImport);
        registerHookDef(ctx, EnumScriptType.KILLED.function, "noppes.npcs.api.event.IPlayerEvent$DiedEvent", playerImport);

        // Interaction
        registerHookDef(ctx, EnumScriptType.INTERACT.function, "noppes.npcs.api.event.IPlayerEvent$InteractEvent", playerImport);
        registerHookDef(ctx, EnumScriptType.RIGHT_CLICK.function, "noppes.npcs.api.event.IPlayerEvent$RightClickEvent", playerImport);
        registerHookDef(ctx, EnumScriptType.BREAK_BLOCK.function, "noppes.npcs.api.event.IPlayerEvent$BreakEvent", playerImport);
        registerHookDef(ctx, EnumScriptType.CHAT.function, "noppes.npcs.api.event.IPlayerEvent$ChatEvent", playerImport);

        // Connection
        registerHookDef(ctx, EnumScriptType.LOGIN.function, "noppes.npcs.api.event.IPlayerEvent$LoginEvent", playerImport);
        registerHookDef(ctx, EnumScriptType.LOGOUT.function, "noppes.npcs.api.event.IPlayerEvent$LogoutEvent", playerImport);
        registerHookDef(ctx, EnumScriptType.RESPAWN.function, "noppes.npcs.api.event.IPlayerEvent$RespawnEvent", playerImport);

        // Input
        registerHookDef(ctx, EnumScriptType.KEY_PRESSED.function, "noppes.npcs.api.event.IPlayerEvent$KeyPressedEvent", playerImport);
        registerHookDef(ctx, EnumScriptType.MOUSE_CLICKED.function, "noppes.npcs.api.event.IPlayerEvent$MouseClickedEvent", playerImport);

        // Items
        registerHookDef(ctx, EnumScriptType.PICKUP.function, "noppes.npcs.api.event.IPlayerEvent$PickUpEvent", playerImport);
        registerHookDef(ctx, EnumScriptType.TOSS.function, "noppes.npcs.api.event.IPlayerEvent$TossEvent", playerImport);
        registerHookDef(ctx, EnumScriptType.DROP.function, "noppes.npcs.api.event.IPlayerEvent$DropEvent", playerImport);
        registerHookDef(ctx, EnumScriptType.PICKUP_XP.function, "noppes.npcs.api.event.IPlayerEvent$PickupXPEvent", playerImport);
        registerHookDef(ctx, EnumScriptType.START_USING_ITEM.function, "noppes.npcs.api.event.IPlayerEvent$StartUsingItem", playerImport);
        registerHookDef(ctx, EnumScriptType.USING_ITEM.function, "noppes.npcs.api.event.IPlayerEvent$UsingItem", playerImport);
        registerHookDef(ctx, EnumScriptType.STOP_USING_ITEM.function, "noppes.npcs.api.event.IPlayerEvent$StopUsingItem", playerImport);
        registerHookDef(ctx, EnumScriptType.FINISH_USING_ITEM.function, "noppes.npcs.api.event.IPlayerEvent$FinishUsingItem", playerImport);
        registerHookDef(ctx, EnumScriptType.CONTAINER_OPEN.function, "noppes.npcs.api.event.IPlayerEvent$ContainerOpen", playerImport);

        // Movement
        registerHookDef(ctx, EnumScriptType.JUMP.function, "noppes.npcs.api.event.IPlayerEvent$JumpEvent", playerImport);
        registerHookDef(ctx, EnumScriptType.FALL.function, "noppes.npcs.api.event.IPlayerEvent$FallEvent", playerImport);
        registerHookDef(ctx, EnumScriptType.CHANGED_DIM.function, "noppes.npcs.api.event.IPlayerEvent$ChangedDimension", playerImport);

        // World
        registerHookDef(ctx, EnumScriptType.RANGED_CHARGE.function, "noppes.npcs.api.event.IPlayerEvent$RangedChargeEvent", playerImport);
        registerHookDef(ctx, EnumScriptType.RANGED_LAUNCHED.function, "noppes.npcs.api.event.IPlayerEvent$RangedLaunchedEvent", playerImport);
        registerHookDef(ctx, EnumScriptType.USE_HOE.function, "noppes.npcs.api.event.IPlayerEvent$UseHoeEvent", playerImport);
        registerHookDef(ctx, EnumScriptType.BONEMEAL.function, "noppes.npcs.api.event.IPlayerEvent$BonemealEvent", playerImport);
        registerHookDef(ctx, EnumScriptType.FILL_BUCKET.function, "noppes.npcs.api.event.IPlayerEvent$FillBucketEvent", playerImport);
        registerHookDef(ctx, EnumScriptType.WAKE_UP.function, "noppes.npcs.api.event.IPlayerEvent$WakeUpEvent", playerImport);
        registerHookDef(ctx, EnumScriptType.SLEEP.function, "noppes.npcs.api.event.IPlayerEvent$SleepEvent", playerImport);
        registerHookDef(ctx, EnumScriptType.PLAYSOUND.function, "noppes.npcs.api.event.IPlayerEvent$SoundEvent", playerImport);
        registerHookDef(ctx, EnumScriptType.LIGHTNING.function, "noppes.npcs.api.event.IPlayerEvent$LightningEvent", playerImport);
        registerHookDef(ctx, EnumScriptType.SCRIPT_COMMAND.function, "noppes.npcs.api.event.ICustomNPCsEvent$ScriptedCommandEvent", "noppes.npcs.api.event.ICustomNPCsEvent");

        // Quest events
        registerHookDef(ctx, EnumScriptType.QUEST_START.function, "noppes.npcs.api.event.IQuestEvent$QuestStartEvent", questImport);
        registerHookDef(ctx, EnumScriptType.QUEST_COMPLETED.function, "noppes.npcs.api.event.IQuestEvent$QuestCompletedEvent", questImport);
        registerHookDef(ctx, EnumScriptType.QUEST_TURNIN.function, "noppes.npcs.api.event.IQuestEvent$QuestTurnedInEvent", questImport);

        // Dialog events
        registerHookDef(ctx, EnumScriptType.DIALOG_OPEN.function, "noppes.npcs.api.event.IDialogEvent$DialogOpen", dialogImport);
        registerHookDef(ctx, EnumScriptType.DIALOG_OPTION.function, "noppes.npcs.api.event.IDialogEvent$DialogOption", dialogImport);
        registerHookDef(ctx, EnumScriptType.DIALOG_CLOSE.function, "noppes.npcs.api.event.IDialogEvent$DialogClosed", dialogImport);

        // Faction events
        registerHookDef(ctx, EnumScriptType.FACTION_POINTS.function, "noppes.npcs.api.event.IFactionEvent$FactionPoints", factionImport);

        // Custom GUI events
        registerHookDef(ctx, EnumScriptType.CUSTOM_GUI_CLOSED.function, "noppes.npcs.api.event.ICustomGuiEvent$CloseEvent", guiImport);
        registerHookDef(ctx, EnumScriptType.CUSTOM_GUI_BUTTON.function, "noppes.npcs.api.event.ICustomGuiEvent$ButtonEvent", guiImport);
        registerHookDef(ctx, EnumScriptType.CUSTOM_GUI_SLOT.function, "noppes.npcs.api.event.ICustomGuiEvent$SlotEvent", guiImport);
        registerHookDef(ctx, EnumScriptType.CUSTOM_GUI_SLOT_CLICKED.function, "noppes.npcs.api.event.ICustomGuiEvent$SlotClickEvent", guiImport);
        registerHookDef(ctx, EnumScriptType.CUSTOM_GUI_SCROLL.function, "noppes.npcs.api.event.ICustomGuiEvent$ScrollEvent", guiImport);
        registerHookDef(ctx, EnumScriptType.CUSTOM_GUI_TEXTFIELD.function, "noppes.npcs.api.event.ICustomGuiEvent$UnfocusedEvent", guiImport);

        // Party events
        registerHookDef(ctx, EnumScriptType.PARTY_QUEST_COMPLETED.function, "noppes.npcs.api.event.IPartyEvent$PartyQuestCompletedEvent", partyImport);
        registerHookDef(ctx, EnumScriptType.PARTY_QUEST_SET.function, "noppes.npcs.api.event.IPartyEvent$PartyQuestSetEvent", partyImport);
        registerHookDef(ctx, EnumScriptType.PARTY_QUEST_TURNED_IN.function, "noppes.npcs.api.event.IPartyEvent$PartyQuestTurnedInEvent", partyImport);
        registerHookDef(ctx, EnumScriptType.PARTY_INVITE.function, "noppes.npcs.api.event.IPartyEvent$PartyInviteEvent", partyImport);
        registerHookDef(ctx, EnumScriptType.PARTY_KICK.function, "noppes.npcs.api.event.IPartyEvent$PartyKickEvent", partyImport);
        registerHookDef(ctx, EnumScriptType.PARTY_LEAVE.function, "noppes.npcs.api.event.IPartyEvent$PartyLeaveEvent", partyImport);
        registerHookDef(ctx, EnumScriptType.PARTY_DISBAND.function, "noppes.npcs.api.event.IPartyEvent$PartyDisbandEvent", partyImport);

        // Animation events
        registerHookDef(ctx, EnumScriptType.ANIMATION_START.function, "noppes.npcs.api.event.IAnimationEvent$Started", animImport);
        registerHookDef(ctx, EnumScriptType.ANIMATION_END.function, "noppes.npcs.api.event.IAnimationEvent$Ended", animImport);
        registerHookDef(ctx, EnumScriptType.ANIMATION_FRAME_ENTER.function, "noppes.npcs.api.event.IAnimationEvent$IFrameEvent$Entered", animImport);
        registerHookDef(ctx, EnumScriptType.ANIMATION_FRAME_EXIT.function, "noppes.npcs.api.event.IAnimationEvent$IFrameEvent$Exited", animImport);

        // Profile events
        registerHookDef(ctx, EnumScriptType.PROFILE_CHANGE.function, "noppes.npcs.api.event.IPlayerEvent$ProfileEvent$Changed", playerImport);
        registerHookDef(ctx, EnumScriptType.PROFILE_REMOVE.function, "noppes.npcs.api.event.IPlayerEvent$ProfileEvent$Removed", playerImport);
        registerHookDef(ctx, EnumScriptType.PROFILE_CREATE.function, "noppes.npcs.api.event.IPlayerEvent$ProfileEvent$Create", playerImport);

        // Effect events (for player context)
        registerHookDef(ctx, EnumScriptType.ON_EFFECT_ADD.function, "noppes.npcs.api.event.IPlayerEvent$EffectEvent$Added", playerImport);
        registerHookDef(ctx, EnumScriptType.ON_EFFECT_TICK.function, "noppes.npcs.api.event.IPlayerEvent$EffectEvent$Ticked", playerImport);
        registerHookDef(ctx, EnumScriptType.ON_EFFECT_REMOVE.function, "noppes.npcs.api.event.IPlayerEvent$EffectEvent$Removed", playerImport);
    }

    private void initializeBlockHooks() {
        String ctx = ScriptContext.BLOCK.hookContext;
        String blockImport = "noppes.npcs.api.event.IBlockEvent";

        registerHookDef(ctx, EnumScriptType.INIT.function, "noppes.npcs.api.event.IBlockEvent$InitEvent", blockImport);
        registerHookDef(ctx, EnumScriptType.TICK.function, "noppes.npcs.api.event.IBlockEvent$UpdateEvent", blockImport);
        registerHookDef(ctx, EnumScriptType.INTERACT.function, "noppes.npcs.api.event.IBlockEvent$InteractEvent", blockImport);
        registerHookDef(ctx, EnumScriptType.FALLEN_UPON.function, "noppes.npcs.api.event.IBlockEvent$EntityFallenUponEvent", blockImport);
        registerHookDef(ctx, EnumScriptType.REDSTONE.function, "noppes.npcs.api.event.IBlockEvent$RedstoneEvent", blockImport);
        registerHookDef(ctx, EnumScriptType.BROKEN.function, "noppes.npcs.api.event.IBlockEvent$BreakEvent", blockImport);
        registerHookDef(ctx, EnumScriptType.EXPLODED.function, "noppes.npcs.api.event.IBlockEvent$ExplodedEvent", blockImport);
        registerHookDef(ctx, EnumScriptType.RAIN_FILLED.function, "noppes.npcs.api.event.IBlockEvent$RainFillEvent", blockImport);
        registerHookDef(ctx, EnumScriptType.NEIGHBOR_CHANGED.function, "noppes.npcs.api.event.IBlockEvent$NeighborChangedEvent", blockImport);
        registerHookDef(ctx, EnumScriptType.CLICKED.function, "noppes.npcs.api.event.IBlockEvent$ClickedEvent", blockImport);
        registerHookDef(ctx, EnumScriptType.HARVESTED.function, "noppes.npcs.api.event.IBlockEvent$HarvestedEvent", blockImport);
        registerHookDef(ctx, EnumScriptType.COLLIDE.function, "noppes.npcs.api.event.IBlockEvent$CollidedEvent", blockImport);
        registerHookDef(ctx, EnumScriptType.TIMER.function, "noppes.npcs.api.event.IBlockEvent$TimerEvent", blockImport);
    }

    private void initializeItemHooks() {
        String ctx = ScriptContext.ITEM.hookContext;
        String itemImport = "noppes.npcs.api.event.IItemEvent";

        registerHookDef(ctx, EnumScriptType.INIT.function, "noppes.npcs.api.event.IItemEvent$InitEvent", itemImport);
        registerHookDef(ctx, EnumScriptType.TICK.function, "noppes.npcs.api.event.IItemEvent$UpdateEvent", itemImport);
        registerHookDef(ctx, EnumScriptType.TOSSED.function, "noppes.npcs.api.event.IItemEvent$TossedEvent", itemImport);
        registerHookDef(ctx, EnumScriptType.PICKEDUP.function, "noppes.npcs.api.event.IItemEvent$PickedUpEvent", itemImport);
        registerHookDef(ctx, EnumScriptType.SPAWN.function, "noppes.npcs.api.event.IItemEvent$SpawnEvent", itemImport);
        registerHookDef(ctx, EnumScriptType.INTERACT.function, "noppes.npcs.api.event.IItemEvent$InteractEvent", itemImport);
        registerHookDef(ctx, EnumScriptType.RIGHT_CLICK.function, "noppes.npcs.api.event.IItemEvent$RightClickEvent", itemImport);
        registerHookDef(ctx, EnumScriptType.ATTACK.function, "noppes.npcs.api.event.IItemEvent$AttackEvent", itemImport);
        registerHookDef(ctx, EnumScriptType.START_USING_ITEM.function, "noppes.npcs.api.event.IItemEvent$StartUsingItem", itemImport);
        registerHookDef(ctx, EnumScriptType.USING_ITEM.function, "noppes.npcs.api.event.IItemEvent$UsingItem", itemImport);
        registerHookDef(ctx, EnumScriptType.STOP_USING_ITEM.function, "noppes.npcs.api.event.IItemEvent$StopUsingItem", itemImport);
        registerHookDef(ctx, EnumScriptType.FINISH_USING_ITEM.function, "noppes.npcs.api.event.IItemEvent$FinishUsingItem", itemImport);
    }

    private void initializeLinkedItemHooks() {
        String ctx = ScriptContext.LINKED_ITEM.hookContext;
        String itemImport = "noppes.npcs.api.event.IItemEvent";
        String linkedImport = "noppes.npcs.api.event.ILinkedItemEvent";

        // Linked item specific hooks
        registerHookDef(ctx, EnumScriptType.LINKED_ITEM_BUILD.function, "noppes.npcs.api.event.ILinkedItemEvent$BuildEvent", linkedImport);
        registerHookDef(ctx, EnumScriptType.LINKED_ITEM_VERSION.function, "noppes.npcs.api.event.ILinkedItemEvent$VersionChangeEvent", linkedImport);

        // Standard item hooks
        registerHookDef(ctx, EnumScriptType.INIT.function, "noppes.npcs.api.event.IItemEvent$InitEvent", itemImport);
        registerHookDef(ctx, EnumScriptType.TICK.function, "noppes.npcs.api.event.IItemEvent$UpdateEvent", itemImport);
        registerHookDef(ctx, EnumScriptType.TOSSED.function, "noppes.npcs.api.event.IItemEvent$TossedEvent", itemImport);
        registerHookDef(ctx, EnumScriptType.PICKEDUP.function, "noppes.npcs.api.event.IItemEvent$PickedUpEvent", itemImport);
        registerHookDef(ctx, EnumScriptType.SPAWN.function, "noppes.npcs.api.event.IItemEvent$SpawnEvent", itemImport);
        registerHookDef(ctx, EnumScriptType.INTERACT.function, "noppes.npcs.api.event.IItemEvent$InteractEvent", itemImport);
        registerHookDef(ctx, EnumScriptType.RIGHT_CLICK.function, "noppes.npcs.api.event.IItemEvent$RightClickEvent", itemImport);
        registerHookDef(ctx, EnumScriptType.ATTACK.function, "noppes.npcs.api.event.IItemEvent$AttackEvent", itemImport);
        registerHookDef(ctx, EnumScriptType.START_USING_ITEM.function, "noppes.npcs.api.event.IItemEvent$StartUsingItem", itemImport);
        registerHookDef(ctx, EnumScriptType.USING_ITEM.function, "noppes.npcs.api.event.IItemEvent$UsingItem", itemImport);
        registerHookDef(ctx, EnumScriptType.STOP_USING_ITEM.function, "noppes.npcs.api.event.IItemEvent$StopUsingItem", itemImport);
        registerHookDef(ctx, EnumScriptType.FINISH_USING_ITEM.function, "noppes.npcs.api.event.IItemEvent$FinishUsingItem", itemImport);
    }

    /**
     * Internal helper to register a hook definition.
     */
    private void registerHookDef(String context, String hookName, String eventClassName, String... imports) {
        Map<String, HookDefinition> contextDefs = hookDefinitions.get(context);
        if (contextDefs == null) {
            contextDefs = new LinkedHashMap<>();
            hookDefinitions.put(context, contextDefs);
        }

        HookDefinition.Builder builder = HookDefinition.builder(hookName);
        if (eventClassName != null) {
            builder.eventClass(eventClassName);
        }
        if (imports != null && imports.length > 0 && imports[0] != null) {
            builder.requiredImports(imports);
        }
        contextDefs.put(hookName, builder.build());
    }

    private void registerHookDef(ScriptContext context, String hookName, String eventClassName, String... imports) {
        registerHookDef(context.hookContext, hookName, eventClassName, imports);
    }

    // ==================== PUBLIC API ====================

    @Override
    public void registerHookDefinition(String context, IHookDefinition definition) {
        if (context == null || definition == null || definition.hookName() == null)
            return;

        Map<String, HookDefinition> contextDefs = hookDefinitions.get(context);
        if (contextDefs == null) {
            contextDefs = new LinkedHashMap<>();
            hookDefinitions.put(context, contextDefs);
        }

        if (definition instanceof HookDefinition) {
            contextDefs.put(definition.hookName(), (HookDefinition) definition);
        } else {
            HookDefinition def = HookDefinition.builder(definition.hookName())
                .eventClass(definition.eventClassName())
                .paramNames(definition.paramNames())
                .requiredImports(definition.requiredImports())
                .cancelable(definition.isCancelable())
                .build();
            contextDefs.put(definition.hookName(), def);
        }

        hookRevision++;
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
    public int getHookRevision() {
        return hookRevision;
    }

    // ==================== CONVENIENCE METHODS ====================

    public void registerHook(ScriptContext context, String functionName) {
        registerHook(context.hookContext, functionName);
    }

    public void registerHooks(ScriptContext context, String... functionNames) {
        for (String fn : functionNames) {
            registerHook(context.hookContext, fn);
        }
    }

    public void registerHookDefinition(ScriptContext context, IHookDefinition definition) {
        registerHookDefinition(context.hookContext, definition);
    }

    // ==================== LEGACY API (IScriptHookHandler) ====================

    @Override
    public void registerHook(String context, String functionName) {
        if (context == null || functionName == null || functionName.isEmpty())
            return;

        Map<String, HookDefinition> contextDefs = hookDefinitions.get(context);
        if (contextDefs == null) {
            contextDefs = new LinkedHashMap<>();
            hookDefinitions.put(context, contextDefs);
        }

        // Only add if not already present
        if (!contextDefs.containsKey(functionName)) {
            contextDefs.put(functionName, HookDefinition.builder(functionName).build());
            hookRevision++;
        }
    }

    @Override
    public void registerHooks(String functionName, String... contexts) {
        if (functionName == null || functionName.isEmpty() || contexts == null)
            return;

        for (String context : contexts) {
            registerHook(context, functionName);
        }
    }

    @Override
    public void unregisterHook(String context, String functionName) {
        if (context == null || functionName == null)
            return;

        Map<String, HookDefinition> contextDefs = hookDefinitions.get(context);
        if (contextDefs != null && contextDefs.remove(functionName) != null) {
            hookRevision++;
        }
    }

    @Override
    public void unregisterHookFromAll(String functionName) {
        if (functionName == null)
            return;

        for (Map<String, HookDefinition> contextDefs : hookDefinitions.values()) {
            if (contextDefs.remove(functionName) != null) {
                hookRevision++;
            }
        }
    }

    @Override
    public List<String> getAddonHooks(String context) {
        // With unified storage, we can't distinguish addon vs built-in
        // Return empty for backwards compatibility
        return Collections.emptyList();
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
    public boolean hasHook(String context, String functionName) {
        if (context == null || functionName == null)
            return false;

        Map<String, HookDefinition> contextDefs = hookDefinitions.get(context);
        return contextDefs != null && contextDefs.containsKey(functionName);
    }

    @Override
    public String[] getContexts() {
        return hookDefinitions.keySet().toArray(new String[0]);
    }
}
