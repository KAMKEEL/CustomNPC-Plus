package noppes.npcs.controllers;

import noppes.npcs.api.handler.IHookDefinition;
import noppes.npcs.api.handler.IScriptHookHandler;
import noppes.npcs.constants.EnumScriptType;
import noppes.npcs.constants.ScriptContext;
import noppes.npcs.controllers.data.RecipeScript;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    // Built-in hooks per context (legacy string list)
    private final Map<String, List<String>> builtInHooks = new HashMap<>();

    // Addon-registered hooks per context (legacy string list)
    private final Map<String, List<String>> addonHooks = new HashMap<>();

    // Rich hook definitions per context (NEW)
    private final Map<String, Map<String, HookDefinition>> hookDefinitions = new HashMap<>();

    // Revision counter for cache invalidation (NEW)
    private int hookRevision = 0;

    public ScriptHookController() {
        Instance = this;
        initializeBuiltInHooks();
    }

    private void initializeBuiltInHooks() {
        // Initialize empty lists for all contexts from ScriptContext
        for (ScriptContext context : ScriptContext.values()) {
            if (!context.hookContext.isEmpty()) {
                builtInHooks.put(context.hookContext, new ArrayList<>());
                addonHooks.put(context.hookContext, new ArrayList<>());
                hookDefinitions.put(context.hookContext, new HashMap<>());
            }
        }

        // NPC hooks with full definitions
        initializeNpcHooks();

        // Player hooks with full definitions
        initializePlayerHooks();

        // Block hooks with full definitions
        initializeBlockHooks();

        // Item hooks with full definitions
        initializeItemHooks();

        // Linked item hooks with full definitions
        initializeLinkedItemHooks();

        // Recipe hooks
        registerBuiltIn(ScriptContext.RECIPE,
            RecipeScript.ScriptType.PRE.function,
            RecipeScript.ScriptType.POST.function
        );

        // Effect hooks
        registerBuiltIn(ScriptContext.EFFECT,
            EnumScriptType.ON_EFFECT_ADD.function,
            EnumScriptType.ON_EFFECT_TICK.function,
            EnumScriptType.ON_EFFECT_REMOVE.function
        );

        // Forge hooks - init + CNPC-specific hooks, the rest are dynamically discovered from Forge event classes
        registerBuiltIn(ScriptContext.FORGE,
            EnumScriptType.INIT.function,
            EnumScriptType.CNPC_NATURAL_SPAWN.function
        );
    }

    private void initializeNpcHooks() {
        String ctx = ScriptContext.NPC.hookContext;
        String npcImport = "noppes.npcs.api.event.INpcEvent";
        String projImport = "noppes.npcs.api.event.IProjectileEvent";
        String abilityImport = "noppes.npcs.api.event.IAbilityEvent";

        // Core lifecycle
        registerBuiltInDefinition(ctx, EnumScriptType.INIT.function,
            "noppes.npcs.api.event.INpcEvent$InitEvent", npcImport);
        registerBuiltInDefinition(ctx, EnumScriptType.TICK.function,
            "noppes.npcs.api.event.INpcEvent$UpdateEvent", npcImport);
        registerBuiltInDefinition(ctx, EnumScriptType.TIMER.function,
            "noppes.npcs.api.event.INpcEvent$TimerEvent", npcImport);

        // Interaction
        registerBuiltInDefinition(ctx, EnumScriptType.INTERACT.function,
            "noppes.npcs.api.event.INpcEvent$InteractEvent", npcImport);
        registerBuiltInDefinition(ctx, EnumScriptType.DIALOG.function,
            "noppes.npcs.api.event.INpcEvent$DialogEvent", npcImport);
        registerBuiltInDefinition(ctx, EnumScriptType.DIALOG_CLOSE.function,
            "noppes.npcs.api.event.INpcEvent$DialogClosedEvent", npcImport);
        registerBuiltInDefinition(ctx, EnumScriptType.COLLIDE.function,
            "noppes.npcs.api.event.INpcEvent$CollideEvent", npcImport);

        // Combat
        registerBuiltInDefinition(ctx, EnumScriptType.DAMAGED.function,
            "noppes.npcs.api.event.INpcEvent$DamagedEvent", npcImport);
        registerBuiltInDefinition(ctx, EnumScriptType.KILLED.function,
            "noppes.npcs.api.event.INpcEvent$DiedEvent", npcImport);
        registerBuiltInDefinition(ctx, EnumScriptType.KILLS.function,
            "noppes.npcs.api.event.INpcEvent$KilledEntityEvent", npcImport);
        registerBuiltInDefinition(ctx, EnumScriptType.ATTACK_MELEE.function,
            "noppes.npcs.api.event.INpcEvent$MeleeAttackEvent", npcImport);
        registerBuiltInDefinition(ctx, EnumScriptType.ATTACK_SWING.function,
            "noppes.npcs.api.event.INpcEvent$SwingEvent", npcImport);
        registerBuiltInDefinition(ctx, EnumScriptType.RANGED_LAUNCHED.function,
            "noppes.npcs.api.event.INpcEvent$RangedLaunchedEvent", npcImport);
        registerBuiltInDefinition(ctx, EnumScriptType.TARGET.function,
            "noppes.npcs.api.event.INpcEvent$TargetEvent", npcImport);
        registerBuiltInDefinition(ctx, EnumScriptType.TARGET_LOST.function,
            "noppes.npcs.api.event.INpcEvent$TargetLostEvent", npcImport);

        // Projectile events
        registerBuiltInDefinition(ctx, EnumScriptType.PROJECTILE_TICK.function,
            "noppes.npcs.api.event.IProjectileEvent$UpdateEvent", projImport);
        registerBuiltInDefinition(ctx, EnumScriptType.PROJECTILE_IMPACT.function,
            "noppes.npcs.api.event.IProjectileEvent$ImpactEvent", projImport);

        // Ability events
        registerBuiltInDefinition(ctx, EnumScriptType.ABILITY_START.function,
            "noppes.npcs.api.event.IAbilityEvent$StartEvent", abilityImport);
        registerBuiltInDefinition(ctx, EnumScriptType.ABILITY_EXECUTE.function,
            "noppes.npcs.api.event.IAbilityEvent$ExecuteEvent", abilityImport);
        registerBuiltInDefinition(ctx, EnumScriptType.ABILITY_HIT.function,
            "noppes.npcs.api.event.IAbilityEvent$HitEvent", abilityImport);
        registerBuiltInDefinition(ctx, EnumScriptType.ABILITY_TICK.function,
            "noppes.npcs.api.event.IAbilityEvent$TickEvent", abilityImport);
        registerBuiltInDefinition(ctx, EnumScriptType.ABILITY_INTERRUPT.function,
            "noppes.npcs.api.event.IAbilityEvent$InterruptEvent", abilityImport);
        registerBuiltInDefinition(ctx, EnumScriptType.ABILITY_COMPLETE.function,
            "noppes.npcs.api.event.IAbilityEvent$CompleteEvent", abilityImport);
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
        registerBuiltInDefinition(ctx, EnumScriptType.INIT.function,
            "noppes.npcs.api.event.IPlayerEvent$InitEvent", playerImport);
        registerBuiltInDefinition(ctx, EnumScriptType.TICK.function,
            "noppes.npcs.api.event.IPlayerEvent$UpdateEvent", playerImport);
        registerBuiltInDefinition(ctx, EnumScriptType.TIMER.function,
            "noppes.npcs.api.event.IPlayerEvent$TimerEvent", playerImport);

        // Combat
        registerBuiltInDefinition(ctx, EnumScriptType.ATTACK.function,
            "noppes.npcs.api.event.IPlayerEvent$AttackEvent", playerImport);
        registerBuiltInDefinition(ctx, EnumScriptType.ATTACKED.function,
            "noppes.npcs.api.event.IPlayerEvent$AttackedEvent", playerImport);
        registerBuiltInDefinition(ctx, EnumScriptType.DAMAGED.function,
            "noppes.npcs.api.event.IPlayerEvent$DamagedEvent", playerImport);
        registerBuiltInDefinition(ctx, EnumScriptType.DAMAGED_ENTITY.function,
            "noppes.npcs.api.event.IPlayerEvent$DamagedEntityEvent", playerImport);
        registerBuiltInDefinition(ctx, EnumScriptType.KILLS.function,
            "noppes.npcs.api.event.IPlayerEvent$KilledEntityEvent", playerImport);
        registerBuiltInDefinition(ctx, EnumScriptType.KILLED.function,
            "noppes.npcs.api.event.IPlayerEvent$DiedEvent", playerImport);

        // Interaction
        registerBuiltInDefinition(ctx, EnumScriptType.INTERACT.function,
            "noppes.npcs.api.event.IPlayerEvent$InteractEvent", playerImport);
        registerBuiltInDefinition(ctx, EnumScriptType.RIGHT_CLICK.function,
            "noppes.npcs.api.event.IPlayerEvent$RightClickEvent", playerImport);
        registerBuiltInDefinition(ctx, EnumScriptType.BREAK_BLOCK.function,
            "noppes.npcs.api.event.IPlayerEvent$BreakEvent", playerImport);
        registerBuiltInDefinition(ctx, EnumScriptType.CHAT.function,
            "noppes.npcs.api.event.IPlayerEvent$ChatEvent", playerImport);

        // Connection
        registerBuiltInDefinition(ctx, EnumScriptType.LOGIN.function,
            "noppes.npcs.api.event.IPlayerEvent$LoginEvent", playerImport);
        registerBuiltInDefinition(ctx, EnumScriptType.LOGOUT.function,
            "noppes.npcs.api.event.IPlayerEvent$LogoutEvent", playerImport);
        registerBuiltInDefinition(ctx, EnumScriptType.RESPAWN.function,
            "noppes.npcs.api.event.IPlayerEvent$RespawnEvent", playerImport);

        // Input
        registerBuiltInDefinition(ctx, EnumScriptType.KEY_PRESSED.function,
            "noppes.npcs.api.event.IPlayerEvent$KeyPressedEvent", playerImport);
        registerBuiltInDefinition(ctx, EnumScriptType.MOUSE_CLICKED.function,
            "noppes.npcs.api.event.IPlayerEvent$MouseClickedEvent", playerImport);

        // Items
        registerBuiltInDefinition(ctx, EnumScriptType.PICKUP.function,
            "noppes.npcs.api.event.IPlayerEvent$PickUpEvent", playerImport);
        registerBuiltInDefinition(ctx, EnumScriptType.TOSS.function,
            "noppes.npcs.api.event.IPlayerEvent$TossEvent", playerImport);
        registerBuiltInDefinition(ctx, EnumScriptType.DROP.function,
            "noppes.npcs.api.event.IPlayerEvent$DropEvent", playerImport);
        registerBuiltInDefinition(ctx, EnumScriptType.PICKUP_XP.function,
            "noppes.npcs.api.event.IPlayerEvent$PickupXPEvent", playerImport);
        registerBuiltInDefinition(ctx, EnumScriptType.START_USING_ITEM.function,
            "noppes.npcs.api.event.IPlayerEvent$StartUsingItem", playerImport);
        registerBuiltInDefinition(ctx, EnumScriptType.USING_ITEM.function,
            "noppes.npcs.api.event.IPlayerEvent$UsingItem", playerImport);
        registerBuiltInDefinition(ctx, EnumScriptType.STOP_USING_ITEM.function,
            "noppes.npcs.api.event.IPlayerEvent$StopUsingItem", playerImport);
        registerBuiltInDefinition(ctx, EnumScriptType.FINISH_USING_ITEM.function,
            "noppes.npcs.api.event.IPlayerEvent$FinishUsingItem", playerImport);
        registerBuiltInDefinition(ctx, EnumScriptType.CONTAINER_OPEN.function,
            "noppes.npcs.api.event.IPlayerEvent$ContainerOpen", playerImport);

        // Movement
        registerBuiltInDefinition(ctx, EnumScriptType.JUMP.function,
            "noppes.npcs.api.event.IPlayerEvent$JumpEvent", playerImport);
        registerBuiltInDefinition(ctx, EnumScriptType.FALL.function,
            "noppes.npcs.api.event.IPlayerEvent$FallEvent", playerImport);
        registerBuiltInDefinition(ctx, EnumScriptType.CHANGED_DIM.function,
            "noppes.npcs.api.event.IPlayerEvent$ChangedDimension", playerImport);

        // World
        registerBuiltInDefinition(ctx, EnumScriptType.RANGED_CHARGE.function,
            "noppes.npcs.api.event.IPlayerEvent$RangedChargeEvent", playerImport);
        registerBuiltInDefinition(ctx, EnumScriptType.RANGED_LAUNCHED.function,
            "noppes.npcs.api.event.IPlayerEvent$RangedLaunchedEvent", playerImport);
        registerBuiltInDefinition(ctx, EnumScriptType.USE_HOE.function,
            "noppes.npcs.api.event.IPlayerEvent$UseHoeEvent", playerImport);
        registerBuiltInDefinition(ctx, EnumScriptType.BONEMEAL.function,
            "noppes.npcs.api.event.IPlayerEvent$BonemealEvent", playerImport);
        registerBuiltInDefinition(ctx, EnumScriptType.FILL_BUCKET.function,
            "noppes.npcs.api.event.IPlayerEvent$FillBucketEvent", playerImport);
        registerBuiltInDefinition(ctx, EnumScriptType.WAKE_UP.function,
            "noppes.npcs.api.event.IPlayerEvent$WakeUpEvent", playerImport);
        registerBuiltInDefinition(ctx, EnumScriptType.SLEEP.function,
            "noppes.npcs.api.event.IPlayerEvent$SleepEvent", playerImport);
        registerBuiltInDefinition(ctx, EnumScriptType.PLAYSOUND.function,
            "noppes.npcs.api.event.IPlayerEvent$SoundEvent", playerImport);
        registerBuiltInDefinition(ctx, EnumScriptType.LIGHTNING.function,
            "noppes.npcs.api.event.IPlayerEvent$LightningEvent", playerImport);
        registerBuiltInDefinition(ctx, EnumScriptType.SCRIPT_COMMAND.function,
            "noppes.npcs.api.event.ICustomNPCsEvent$ScriptedCommandEvent", "noppes.npcs.api.event.ICustomNPCsEvent");

        // Quest events
        registerBuiltInDefinition(ctx, EnumScriptType.QUEST_START.function,
            "noppes.npcs.api.event.IQuestEvent$QuestStartEvent", questImport);
        registerBuiltInDefinition(ctx, EnumScriptType.QUEST_COMPLETED.function,
            "noppes.npcs.api.event.IQuestEvent$QuestCompletedEvent", questImport);
        registerBuiltInDefinition(ctx, EnumScriptType.QUEST_TURNIN.function,
            "noppes.npcs.api.event.IQuestEvent$QuestTurnedInEvent", questImport);

        // Dialog events
        registerBuiltInDefinition(ctx, EnumScriptType.DIALOG_OPEN.function,
            "noppes.npcs.api.event.IDialogEvent$DialogOpen", dialogImport);
        registerBuiltInDefinition(ctx, EnumScriptType.DIALOG_OPTION.function,
            "noppes.npcs.api.event.IDialogEvent$DialogOption", dialogImport);
        registerBuiltInDefinition(ctx, EnumScriptType.DIALOG_CLOSE.function,
            "noppes.npcs.api.event.IDialogEvent$DialogClosed", dialogImport);

        // Faction events
        registerBuiltInDefinition(ctx, EnumScriptType.FACTION_POINTS.function,
            "noppes.npcs.api.event.IFactionEvent$FactionPoints", factionImport);

        // Custom GUI events
        registerBuiltInDefinition(ctx, EnumScriptType.CUSTOM_GUI_CLOSED.function,
            "noppes.npcs.api.event.ICustomGuiEvent$CloseEvent", guiImport);
        registerBuiltInDefinition(ctx, EnumScriptType.CUSTOM_GUI_BUTTON.function,
            "noppes.npcs.api.event.ICustomGuiEvent$ButtonEvent", guiImport);
        registerBuiltInDefinition(ctx, EnumScriptType.CUSTOM_GUI_SLOT.function,
            "noppes.npcs.api.event.ICustomGuiEvent$SlotEvent", guiImport);
        registerBuiltInDefinition(ctx, EnumScriptType.CUSTOM_GUI_SLOT_CLICKED.function,
            "noppes.npcs.api.event.ICustomGuiEvent$SlotClickEvent", guiImport);
        registerBuiltInDefinition(ctx, EnumScriptType.CUSTOM_GUI_SCROLL.function,
            "noppes.npcs.api.event.ICustomGuiEvent$ScrollEvent", guiImport);
        registerBuiltInDefinition(ctx, EnumScriptType.CUSTOM_GUI_TEXTFIELD.function,
            "noppes.npcs.api.event.ICustomGuiEvent$UnfocusedEvent", guiImport);

        // Party events
        registerBuiltInDefinition(ctx, EnumScriptType.PARTY_QUEST_COMPLETED.function,
            "noppes.npcs.api.event.IPartyEvent$PartyQuestCompletedEvent", partyImport);
        registerBuiltInDefinition(ctx, EnumScriptType.PARTY_QUEST_SET.function,
            "noppes.npcs.api.event.IPartyEvent$PartyQuestSetEvent", partyImport);
        registerBuiltInDefinition(ctx, EnumScriptType.PARTY_QUEST_TURNED_IN.function,
            "noppes.npcs.api.event.IPartyEvent$PartyQuestTurnedInEvent", partyImport);
        registerBuiltInDefinition(ctx, EnumScriptType.PARTY_INVITE.function,
            "noppes.npcs.api.event.IPartyEvent$PartyInviteEvent", partyImport);
        registerBuiltInDefinition(ctx, EnumScriptType.PARTY_KICK.function,
            "noppes.npcs.api.event.IPartyEvent$PartyKickEvent", partyImport);
        registerBuiltInDefinition(ctx, EnumScriptType.PARTY_LEAVE.function,
            "noppes.npcs.api.event.IPartyEvent$PartyLeaveEvent", partyImport);
        registerBuiltInDefinition(ctx, EnumScriptType.PARTY_DISBAND.function,
            "noppes.npcs.api.event.IPartyEvent$PartyDisbandEvent", partyImport);

        // Animation events
        registerBuiltInDefinition(ctx, EnumScriptType.ANIMATION_START.function,
            "noppes.npcs.api.event.IAnimationEvent$Started", animImport);
        registerBuiltInDefinition(ctx, EnumScriptType.ANIMATION_END.function,
            "noppes.npcs.api.event.IAnimationEvent$Ended", animImport);
        registerBuiltInDefinition(ctx, EnumScriptType.ANIMATION_FRAME_ENTER.function,
            "noppes.npcs.api.event.IAnimationEvent$IFrameEvent$Entered", animImport);
        registerBuiltInDefinition(ctx, EnumScriptType.ANIMATION_FRAME_EXIT.function,
            "noppes.npcs.api.event.IAnimationEvent$IFrameEvent$Exited", animImport);

        // Profile events
        registerBuiltInDefinition(ctx, EnumScriptType.PROFILE_CHANGE.function,
            "noppes.npcs.api.event.IPlayerEvent$ProfileEvent$Changed", playerImport);
        registerBuiltInDefinition(ctx, EnumScriptType.PROFILE_REMOVE.function,
            "noppes.npcs.api.event.IPlayerEvent$ProfileEvent$Removed", playerImport);
        registerBuiltInDefinition(ctx, EnumScriptType.PROFILE_CREATE.function,
            "noppes.npcs.api.event.IPlayerEvent$ProfileEvent$Create", playerImport);

        // Effect events (for player context)
        registerBuiltInDefinition(ctx, EnumScriptType.ON_EFFECT_ADD.function,
            "noppes.npcs.api.event.IPlayerEvent$EffectEvent$Added", playerImport);
        registerBuiltInDefinition(ctx, EnumScriptType.ON_EFFECT_TICK.function,
            "noppes.npcs.api.event.IPlayerEvent$EffectEvent$Ticked", playerImport);
        registerBuiltInDefinition(ctx, EnumScriptType.ON_EFFECT_REMOVE.function,
            "noppes.npcs.api.event.IPlayerEvent$EffectEvent$Removed", playerImport);
    }

    private void initializeBlockHooks() {
        String ctx = ScriptContext.BLOCK.hookContext;
        String blockImport = "noppes.npcs.api.event.IBlockEvent";

        registerBuiltInDefinition(ctx, EnumScriptType.INIT.function,
            "noppes.npcs.api.event.IBlockEvent$InitEvent", blockImport);
        registerBuiltInDefinition(ctx, EnumScriptType.TICK.function,
            "noppes.npcs.api.event.IBlockEvent$UpdateEvent", blockImport);
        registerBuiltInDefinition(ctx, EnumScriptType.INTERACT.function,
            "noppes.npcs.api.event.IBlockEvent$InteractEvent", blockImport);
        registerBuiltInDefinition(ctx, EnumScriptType.FALLEN_UPON.function,
            "noppes.npcs.api.event.IBlockEvent$EntityFallenUponEvent", blockImport);
        registerBuiltInDefinition(ctx, EnumScriptType.REDSTONE.function,
            "noppes.npcs.api.event.IBlockEvent$RedstoneEvent", blockImport);
        registerBuiltInDefinition(ctx, EnumScriptType.BROKEN.function,
            "noppes.npcs.api.event.IBlockEvent$BreakEvent", blockImport);
        registerBuiltInDefinition(ctx, EnumScriptType.EXPLODED.function,
            "noppes.npcs.api.event.IBlockEvent$ExplodedEvent", blockImport);
        registerBuiltInDefinition(ctx, EnumScriptType.RAIN_FILLED.function,
            "noppes.npcs.api.event.IBlockEvent$RainFillEvent", blockImport);
        registerBuiltInDefinition(ctx, EnumScriptType.NEIGHBOR_CHANGED.function,
            "noppes.npcs.api.event.IBlockEvent$NeighborChangedEvent", blockImport);
        registerBuiltInDefinition(ctx, EnumScriptType.CLICKED.function,
            "noppes.npcs.api.event.IBlockEvent$ClickedEvent", blockImport);
        registerBuiltInDefinition(ctx, EnumScriptType.HARVESTED.function,
            "noppes.npcs.api.event.IBlockEvent$HarvestedEvent", blockImport);
        registerBuiltInDefinition(ctx, EnumScriptType.COLLIDE.function,
            "noppes.npcs.api.event.IBlockEvent$CollidedEvent", blockImport);
        registerBuiltInDefinition(ctx, EnumScriptType.TIMER.function,
            "noppes.npcs.api.event.IBlockEvent$TimerEvent", blockImport);
    }

    private void initializeItemHooks() {
        String ctx = ScriptContext.ITEM.hookContext;
        String itemImport = "noppes.npcs.api.event.IItemEvent";

        registerBuiltInDefinition(ctx, EnumScriptType.INIT.function,
            "noppes.npcs.api.event.IItemEvent$InitEvent", itemImport);
        registerBuiltInDefinition(ctx, EnumScriptType.TICK.function,
            "noppes.npcs.api.event.IItemEvent$UpdateEvent", itemImport);
        registerBuiltInDefinition(ctx, EnumScriptType.TOSSED.function,
            "noppes.npcs.api.event.IItemEvent$TossedEvent", itemImport);
        registerBuiltInDefinition(ctx, EnumScriptType.PICKEDUP.function,
            "noppes.npcs.api.event.IItemEvent$PickedUpEvent", itemImport);
        registerBuiltInDefinition(ctx, EnumScriptType.SPAWN.function,
            "noppes.npcs.api.event.IItemEvent$SpawnEvent", itemImport);
        registerBuiltInDefinition(ctx, EnumScriptType.INTERACT.function,
            "noppes.npcs.api.event.IItemEvent$InteractEvent", itemImport);
        registerBuiltInDefinition(ctx, EnumScriptType.RIGHT_CLICK.function,
            "noppes.npcs.api.event.IItemEvent$RightClickEvent", itemImport);
        registerBuiltInDefinition(ctx, EnumScriptType.ATTACK.function,
            "noppes.npcs.api.event.IItemEvent$AttackEvent", itemImport);
        registerBuiltInDefinition(ctx, EnumScriptType.START_USING_ITEM.function,
            "noppes.npcs.api.event.IItemEvent$StartUsingItem", itemImport);
        registerBuiltInDefinition(ctx, EnumScriptType.USING_ITEM.function,
            "noppes.npcs.api.event.IItemEvent$UsingItem", itemImport);
        registerBuiltInDefinition(ctx, EnumScriptType.STOP_USING_ITEM.function,
            "noppes.npcs.api.event.IItemEvent$StopUsingItem", itemImport);
        registerBuiltInDefinition(ctx, EnumScriptType.FINISH_USING_ITEM.function,
            "noppes.npcs.api.event.IItemEvent$FinishUsingItem", itemImport);
    }

    private void initializeLinkedItemHooks() {
        String ctx = ScriptContext.LINKED_ITEM.hookContext;
        String itemImport = "noppes.npcs.api.event.IItemEvent";
        String linkedImport = "noppes.npcs.api.event.ILinkedItemEvent";

        // Linked item specific hooks
        registerBuiltInDefinition(ctx, EnumScriptType.LINKED_ITEM_BUILD.function,
            "noppes.npcs.api.event.ILinkedItemEvent$BuildEvent", linkedImport);
        registerBuiltInDefinition(ctx, EnumScriptType.LINKED_ITEM_VERSION.function,
            "noppes.npcs.api.event.ILinkedItemEvent$VersionChangeEvent", linkedImport);

        // Standard item hooks
        registerBuiltInDefinition(ctx, EnumScriptType.INIT.function,
            "noppes.npcs.api.event.IItemEvent$InitEvent", itemImport);
        registerBuiltInDefinition(ctx, EnumScriptType.TICK.function,
            "noppes.npcs.api.event.IItemEvent$UpdateEvent", itemImport);
        registerBuiltInDefinition(ctx, EnumScriptType.TOSSED.function,
            "noppes.npcs.api.event.IItemEvent$TossedEvent", itemImport);
        registerBuiltInDefinition(ctx, EnumScriptType.PICKEDUP.function,
            "noppes.npcs.api.event.IItemEvent$PickedUpEvent", itemImport);
        registerBuiltInDefinition(ctx, EnumScriptType.SPAWN.function,
            "noppes.npcs.api.event.IItemEvent$SpawnEvent", itemImport);
        registerBuiltInDefinition(ctx, EnumScriptType.INTERACT.function,
            "noppes.npcs.api.event.IItemEvent$InteractEvent", itemImport);
        registerBuiltInDefinition(ctx, EnumScriptType.RIGHT_CLICK.function,
            "noppes.npcs.api.event.IItemEvent$RightClickEvent", itemImport);
        registerBuiltInDefinition(ctx, EnumScriptType.ATTACK.function,
            "noppes.npcs.api.event.IItemEvent$AttackEvent", itemImport);
        registerBuiltInDefinition(ctx, EnumScriptType.START_USING_ITEM.function,
            "noppes.npcs.api.event.IItemEvent$StartUsingItem", itemImport);
        registerBuiltInDefinition(ctx, EnumScriptType.USING_ITEM.function,
            "noppes.npcs.api.event.IItemEvent$UsingItem", itemImport);
        registerBuiltInDefinition(ctx, EnumScriptType.STOP_USING_ITEM.function,
            "noppes.npcs.api.event.IItemEvent$StopUsingItem", itemImport);
        registerBuiltInDefinition(ctx, EnumScriptType.FINISH_USING_ITEM.function,
            "noppes.npcs.api.event.IItemEvent$FinishUsingItem", itemImport);
    }

    /**
     * Register a built-in hook with full definition metadata.
     */
    private void registerBuiltInDefinition(String context, String hookName, String eventClassName, String... imports) {
        HookDefinition def = HookDefinition.builder(hookName)
            .eventClass(eventClassName)
            .requiredImports(imports)
            .build();

        Map<String, HookDefinition> contextDefs = hookDefinitions.get(context);
        if (contextDefs == null) {
            contextDefs = new HashMap<>();
            hookDefinitions.put(context, contextDefs);
        }
        contextDefs.put(hookName, def);

        // Also register in legacy string list
        registerBuiltIn(context, hookName);
    }

    /**
     * Register built-in hooks for a ScriptContext.
     */
    private void registerBuiltIn(ScriptContext context, String... hooks) {
        registerBuiltIn(context.hookContext, hooks);
    }

    private void registerBuiltIn(String context, String... hooks) {
        List<String> list = builtInHooks.get(context);
        if (list != null) {
            for (String hook : hooks) {
                if (!list.contains(hook)) {
                    list.add(hook);
                }
            }
        }
    }

    // ==================== RICH HOOK REGISTRATION (NEW API) ====================

    @Override
    public void registerHookDefinition(String context, IHookDefinition definition) {
        if (context == null || definition == null || definition.hookName() == null) {
            return;
        }

        Map<String, HookDefinition> contextDefs = hookDefinitions.get(context);
        if (contextDefs == null) {
            contextDefs = new HashMap<>();
            hookDefinitions.put(context, contextDefs);
        }

        // Store as HookDefinition (implementation)
        if (definition instanceof HookDefinition) {
            contextDefs.put(definition.hookName(), (HookDefinition) definition);
        } else {
            // Convert foreign IHookDefinition to HookDefinition
            HookDefinition def = HookDefinition.builder(definition.hookName())
                .eventClass(definition.eventClassName())
                .paramNames(definition.paramNames())
                .requiredImports(definition.requiredImports())
                .cancelable(definition.isCancelable())
                .build();
            contextDefs.put(definition.hookName(), def);
        }

        // Also add to legacy addon hooks list for backward compatibility
        registerHook(context, definition.hookName());

        // Increment revision for cache invalidation
        hookRevision++;
    }

    @Override
    public IHookDefinition getHookDefinition(String context, String hookName) {
        if (context == null || hookName == null) {
            return null;
        }

        Map<String, HookDefinition> contextDefs = hookDefinitions.get(context);
        return contextDefs != null ? contextDefs.get(hookName) : null;
    }

    @Override
    public List<IHookDefinition> getAllHookDefinitions(String context) {
        if (context == null) {
            return Collections.emptyList();
        }

        Map<String, HookDefinition> contextDefs = hookDefinitions.get(context);
        if (contextDefs == null || contextDefs.isEmpty()) {
            return Collections.emptyList();
        }

        return new ArrayList<>(contextDefs.values());
    }

    @Override
    public int getHookRevision() {
        return hookRevision;
    }

    // ==================== CONVENIENCE METHODS ====================

    /**
     * Convenience method to register a hook using ScriptContext.
     * @param context The script context
     * @param functionName The hook function name
     */
    public void registerHook(ScriptContext context, String functionName) {
        registerHook(context.hookContext, functionName);
    }

    /**
     * Convenience method to register multiple hooks using ScriptContext.
     * @param context The script context
     * @param functionNames The hook function names
     */
    public void registerHooks(ScriptContext context, String... functionNames) {
        for (String fn : functionNames) {
            registerHook(context.hookContext, fn);
        }
    }

    /**
     * Convenience method to register a hook definition using ScriptContext.
     * @param context The script context
     * @param definition The hook definition
     */
    public void registerHookDefinition(ScriptContext context, IHookDefinition definition) {
        registerHookDefinition(context.hookContext, definition);
    }

    // ==================== LEGACY HOOK REGISTRATION ====================

    @Override
    public void registerHook(String context, String functionName) {
        if (context == null || functionName == null || functionName.isEmpty()) {
            return;
        }

        List<String> hooks = addonHooks.get(context);
        if (hooks == null) {
            // Custom context - create it
            hooks = new ArrayList<>();
            addonHooks.put(context, hooks);
        }

        if (!hooks.contains(functionName) && !isBuiltInHook(context, functionName)) {
            hooks.add(functionName);
        }
    }

    @Override
    public void registerHooks(String functionName, String... contexts) {
        if (functionName == null || functionName.isEmpty() || contexts == null) {
            return;
        }

        for (String context : contexts) {
            registerHook(context, functionName);
        }
    }

    @Override
    public void unregisterHook(String context, String functionName) {
        if (context == null || functionName == null) {
            return;
        }

        List<String> hooks = addonHooks.get(context);
        if (hooks != null) {
            hooks.remove(functionName);
        }
    }

    @Override
    public void unregisterHookFromAll(String functionName) {
        if (functionName == null) {
            return;
        }

        for (List<String> hooks : addonHooks.values()) {
            hooks.remove(functionName);
        }
    }

    @Override
    public List<String> getAddonHooks(String context) {
        List<String> hooks = addonHooks.get(context);
        return hooks != null ? Collections.unmodifiableList(hooks) : Collections.emptyList();
    }

    @Override
    public List<String> getAllHooks(String context) {
        List<String> result = new ArrayList<>();

        // Add built-in hooks
        List<String> builtIn = builtInHooks.get(context);
        if (builtIn != null) {
            result.addAll(builtIn);
        }

        // Add addon hooks
        List<String> addon = addonHooks.get(context);
        if (addon != null) {
            for (String hook : addon) {
                if (!result.contains(hook)) {
                    result.add(hook);
                }
            }
        }

        return result;
    }

    @Override
    public boolean hasHook(String context, String functionName) {
        if (context == null || functionName == null) {
            return false;
        }

        return isBuiltInHook(context, functionName) || isAddonHook(context, functionName);
    }

    @Override
    public String[] getContexts() {
        return builtInHooks.keySet().toArray(new String[0]);
    }

    private boolean isBuiltInHook(String context, String functionName) {
        List<String> hooks = builtInHooks.get(context);
        return hooks != null && hooks.contains(functionName);
    }

    private boolean isAddonHook(String context, String functionName) {
        List<String> hooks = addonHooks.get(context);
        return hooks != null && hooks.contains(functionName);
    }

    /**
     * Get built-in hooks for a context (used by GUIs for backwards compatibility).
     */
    public List<String> getBuiltInHooks(String context) {
        List<String> hooks = builtInHooks.get(context);
        return hooks != null ? Collections.unmodifiableList(hooks) : Collections.emptyList();
    }
}
