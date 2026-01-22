package noppes.npcs.janino.impl;

import noppes.npcs.api.event.IAnimationEvent;
import noppes.npcs.api.event.ICustomGuiEvent;
import noppes.npcs.api.event.ICustomNPCsEvent;
import noppes.npcs.api.event.IDialogEvent;
import noppes.npcs.api.event.IFactionEvent;
import noppes.npcs.api.event.IPartyEvent;
import noppes.npcs.api.event.IPlayerEvent;
import noppes.npcs.api.event.IQuestEvent;
import noppes.npcs.constants.EnumScriptType;
import noppes.npcs.janino.JaninoScript;
import noppes.npcs.janino.annotations.ParamName;
import noppes.npcs.janino.annotations.ScriptHook;

/**
 * Janino (Java) script implementation for player events.
 */
public class JaninoPlayerScript extends JaninoScript<JaninoPlayerScript.Functions> {

    /** Default imports available to player scripts without explicit import statements. */
    public static final String[] DEFAULT_IMPORTS = {
        "noppes.npcs.api.*",
        "noppes.npcs.api.entity.*",
        "noppes.npcs.api.event.*",
        "noppes.npcs.api.handler.*",
        "noppes.npcs.api.handler.data.*",
        "noppes.npcs.api.item.*",
        "noppes.npcs.api.gui.*"
    };

    public JaninoPlayerScript() {
        super(Functions.class, DEFAULT_IMPORTS);
    }

    /**
     * Interface defining the hook methods available for player scripts.
     * Each method corresponds to a player hook from ScriptHookController CONTEXT_PLAYER.
     */
    public interface Functions {
        // Core lifecycle
        @ScriptHook(EnumScriptType.INIT)
        default void init(@ParamName("event") IPlayerEvent.InitEvent event) {}

        @ScriptHook(EnumScriptType.TICK)
        default void tick(@ParamName("event") IPlayerEvent.UpdateEvent event) {}

        // Interaction
        @ScriptHook(EnumScriptType.INTERACT)
        default void interact(@ParamName("event") IPlayerEvent.InteractEvent event) {}

        @ScriptHook(EnumScriptType.RIGHT_CLICK)
        default void rightClick(@ParamName("event") IPlayerEvent.RightClickEvent event) {}

        // Combat
        @ScriptHook(EnumScriptType.ATTACK)
        default void attack(@ParamName("event") IPlayerEvent.AttackEvent event) {}

        @ScriptHook(EnumScriptType.ATTACKED)
        default void attacked(@ParamName("event") IPlayerEvent.AttackedEvent event) {}

        @ScriptHook(EnumScriptType.DAMAGED_ENTITY)
        default void damagedEntity(@ParamName("event") IPlayerEvent.DamagedEntityEvent event) {}

        @ScriptHook(EnumScriptType.DAMAGED)
        default void damaged(@ParamName("event") IPlayerEvent.DamagedEvent event) {}

        @ScriptHook(EnumScriptType.KILLS)
        default void kills(@ParamName("event") IPlayerEvent.KilledEntityEvent event) {}

        @ScriptHook(EnumScriptType.KILLED)
        default void killed(@ParamName("event") IPlayerEvent.DiedEvent event) {}

        // Inventory and drops
        @ScriptHook(EnumScriptType.DROP)
        default void drop(@ParamName("event") IPlayerEvent.DropEvent event) {}

        @ScriptHook(EnumScriptType.RESPAWN)
        default void respawn(@ParamName("event") IPlayerEvent.RespawnEvent event) {}

        @ScriptHook(EnumScriptType.BREAK_BLOCK)
        default void breakBlock(@ParamName("event") IPlayerEvent.BreakEvent event) {}

        // Communication
        @ScriptHook(EnumScriptType.CHAT)
        default void chat(@ParamName("event") IPlayerEvent.ChatEvent event) {}

        @ScriptHook(EnumScriptType.LOGIN)
        default void login(@ParamName("event") IPlayerEvent.LoginEvent event) {}

        @ScriptHook(EnumScriptType.LOGOUT)
        default void logout(@ParamName("event") IPlayerEvent.LogoutEvent event) {}

        @ScriptHook(EnumScriptType.KEY_PRESSED)
        default void keyPressed(@ParamName("event") IPlayerEvent.KeyPressedEvent event) {}

        @ScriptHook(EnumScriptType.MOUSE_CLICKED)
        default void mouseClicked(@ParamName("event") IPlayerEvent.MouseClickedEvent event) {}

        // Item interactions
        @ScriptHook(EnumScriptType.TOSS)
        default void toss(@ParamName("event") IPlayerEvent.TossEvent event) {}

        @ScriptHook(EnumScriptType.PICKUP)
        default void pickUp(@ParamName("event") IPlayerEvent.PickUpEvent event) {}

        @ScriptHook(EnumScriptType.PICKUP_XP)
        default void pickupXP(@ParamName("event") IPlayerEvent.PickupXPEvent event) {}

        @ScriptHook(EnumScriptType.RANGED_CHARGE)
        default void rangedCharge(@ParamName("event") IPlayerEvent.RangedChargeEvent event) {}

        @ScriptHook(EnumScriptType.RANGED_LAUNCHED)
        default void rangedLaunched(@ParamName("event") IPlayerEvent.RangedLaunchedEvent event) {}

        @ScriptHook(EnumScriptType.TIMER)
        default void timer(@ParamName("event") IPlayerEvent.TimerEvent event) {}

        // Item usage
        @ScriptHook(EnumScriptType.START_USING_ITEM)
        default void startItem(@ParamName("event") IPlayerEvent.StartUsingItem event) {}

        @ScriptHook(EnumScriptType.USING_ITEM)
        default void usingItem(@ParamName("event") IPlayerEvent.UsingItem event) {}

        @ScriptHook(EnumScriptType.STOP_USING_ITEM)
        default void stopItem(@ParamName("event") IPlayerEvent.StopUsingItem event) {}

        @ScriptHook(EnumScriptType.FINISH_USING_ITEM)
        default void finishItem(@ParamName("event") IPlayerEvent.FinishUsingItem event) {}

        // Containers and world interaction
        @ScriptHook(EnumScriptType.CONTAINER_OPEN)
        default void containerOpen(@ParamName("event") IPlayerEvent.ContainerOpen event) {}

        @ScriptHook(EnumScriptType.USE_HOE)
        default void useHoe(@ParamName("event") IPlayerEvent.UseHoeEvent event) {}

        @ScriptHook(EnumScriptType.BONEMEAL)
        default void bonemeal(@ParamName("event") IPlayerEvent.BonemealEvent event) {}

        @ScriptHook(EnumScriptType.FILL_BUCKET)
        default void fillBucket(@ParamName("event") IPlayerEvent.FillBucketEvent event) {}

        // Movement and status
        @ScriptHook(EnumScriptType.JUMP)
        default void jump(@ParamName("event") IPlayerEvent.JumpEvent event) {}

        @ScriptHook(EnumScriptType.FALL)
        default void fall(@ParamName("event") IPlayerEvent.FallEvent event) {}

        @ScriptHook(EnumScriptType.WAKE_UP)
        default void wakeUp(@ParamName("event") IPlayerEvent.WakeUpEvent event) {}

        @ScriptHook(EnumScriptType.SLEEP)
        default void sleep(@ParamName("event") IPlayerEvent.SleepEvent event) {}

        @ScriptHook(EnumScriptType.PLAYSOUND)
        default void playSound(@ParamName("event") IPlayerEvent.SoundEvent event) {}

        @ScriptHook(EnumScriptType.LIGHTNING)
        default void lightning(@ParamName("event") IPlayerEvent.LightningEvent event) {}

        @ScriptHook(EnumScriptType.CHANGED_DIM)
        default void changedDim(@ParamName("event") IPlayerEvent.ChangedDimension event) {}

        // Quest
        @ScriptHook(EnumScriptType.QUEST_START)
        default void questStart(@ParamName("event") IQuestEvent.QuestStartEvent event) {}

        @ScriptHook(EnumScriptType.QUEST_COMPLETED)
        default void questCompleted(@ParamName("event") IQuestEvent.QuestCompletedEvent event) {}

        @ScriptHook(EnumScriptType.QUEST_TURNIN)
        default void questTurnIn(@ParamName("event") IQuestEvent.QuestTurnedInEvent event) {}

        // Faction
        @ScriptHook(EnumScriptType.FACTION_POINTS)
        default void factionPoints(@ParamName("event") IFactionEvent.FactionPoints event) {}

        // Dialog
        @ScriptHook(EnumScriptType.DIALOG_OPEN)
        default void dialogOpen(@ParamName("event") IDialogEvent.DialogOpen event) {}

        @ScriptHook(EnumScriptType.DIALOG_OPTION)
        default void dialogOption(@ParamName("event") IDialogEvent.DialogOption event) {}

        @ScriptHook(EnumScriptType.DIALOG_CLOSE)
        default void dialogClose(@ParamName("event") IDialogEvent.DialogClosed event) {}

        // Commands
        @ScriptHook(EnumScriptType.SCRIPT_COMMAND)
        default void scriptCommand(@ParamName("event") ICustomNPCsEvent.ScriptedCommandEvent event) {}

        // Custom GUI
        @ScriptHook(EnumScriptType.CUSTOM_GUI_CLOSED)
        default void customGuiClosed(@ParamName("event") ICustomGuiEvent.CloseEvent event) {}

        @ScriptHook(EnumScriptType.CUSTOM_GUI_BUTTON)
        default void customGuiButton(@ParamName("event") ICustomGuiEvent.ButtonEvent event) {}

        @ScriptHook(EnumScriptType.CUSTOM_GUI_SLOT)
        default void customGuiSlot(@ParamName("event") ICustomGuiEvent.SlotEvent event) {}

        @ScriptHook(EnumScriptType.CUSTOM_GUI_SLOT_CLICKED)
        default void customGuiSlotClicked(@ParamName("event") ICustomGuiEvent.SlotClickEvent event) {}

        @ScriptHook(EnumScriptType.CUSTOM_GUI_SCROLL)
        default void customGuiScroll(@ParamName("event") ICustomGuiEvent.ScrollEvent event) {}

        @ScriptHook(EnumScriptType.CUSTOM_GUI_TEXTFIELD)
        default void customGuiTextfield(@ParamName("event") ICustomGuiEvent.UnfocusedEvent event) {}

        // Party
        @ScriptHook(EnumScriptType.PARTY_QUEST_COMPLETED)
        default void partyQuestCompleted(@ParamName("event") IPartyEvent.PartyQuestCompletedEvent event) {}

        @ScriptHook(EnumScriptType.PARTY_QUEST_SET)
        default void partyQuestSet(@ParamName("event") IPartyEvent.PartyQuestSetEvent event) {}

        @ScriptHook(EnumScriptType.PARTY_QUEST_TURNED_IN)
        default void partyQuestTurnedIn(@ParamName("event") IPartyEvent.PartyQuestTurnedInEvent event) {}

        @ScriptHook(EnumScriptType.PARTY_INVITE)
        default void partyInvite(@ParamName("event") IPartyEvent.PartyInviteEvent event) {}

        @ScriptHook(EnumScriptType.PARTY_KICK)
        default void partyKick(@ParamName("event") IPartyEvent.PartyKickEvent event) {}

        @ScriptHook(EnumScriptType.PARTY_LEAVE)
        default void partyLeave(@ParamName("event") IPartyEvent.PartyLeaveEvent event) {}

        @ScriptHook(EnumScriptType.PARTY_DISBAND)
        default void partyDisband(@ParamName("event") IPartyEvent.PartyDisbandEvent event) {}

        // Animation
        @ScriptHook(EnumScriptType.ANIMATION_START)
        default void animationStart(@ParamName("event") IAnimationEvent.Started event) {}

        @ScriptHook(EnumScriptType.ANIMATION_END)
        default void animationEnd(@ParamName("event") IAnimationEvent.Ended event) {}

        @ScriptHook(EnumScriptType.ANIMATION_FRAME_ENTER)
        default void frameEnter(@ParamName("event") IAnimationEvent.IFrameEvent.Entered event) {}

        @ScriptHook(EnumScriptType.ANIMATION_FRAME_EXIT)
        default void frameExit(@ParamName("event") IAnimationEvent.IFrameEvent.Exited event) {}

        // Profile
        @ScriptHook(EnumScriptType.PROFILE_CHANGE)
        default void profileChange(@ParamName("event") IPlayerEvent.ProfileEvent.Changed event) {}

        @ScriptHook(EnumScriptType.PROFILE_REMOVE)
        default void profileRemove(@ParamName("event") IPlayerEvent.ProfileEvent.Removed event) {}

        @ScriptHook(EnumScriptType.PROFILE_CREATE)
        default void profileCreate(@ParamName("event") IPlayerEvent.ProfileEvent.Create event) {}

        // Effects
        @ScriptHook(EnumScriptType.ON_EFFECT_ADD)
        default void onEffectAdd(@ParamName("event") IPlayerEvent.EffectEvent.Added event) {}

        @ScriptHook(EnumScriptType.ON_EFFECT_TICK)
        default void onEffectTick(@ParamName("event") IPlayerEvent.EffectEvent.Ticked event) {}

        @ScriptHook(EnumScriptType.ON_EFFECT_REMOVE)
        default void onEffectRemove(@ParamName("event") IPlayerEvent.EffectEvent.Removed event) {}
    }
}
