package noppes.npcs.janino.impl;

import noppes.npcs.api.event.IPlayerEvent;
import noppes.npcs.constants.EnumScriptType;
import noppes.npcs.janino.JaninoScript;
import noppes.npcs.janino.annotations.ParamName;
import noppes.npcs.janino.annotations.ScriptHook;

/**
 * Janino (Java) script implementation for player events.
 *
 * <h3>Dynamic Hook Support</h3>
 * With the new dynamic hook resolution, users can define methods for ANY hook -
 * not just those defined in the Functions interface. Simply write a public method
 * with the hook name and matching parameter type:
 *
 * <pre>{@code
 * // Standard hook (defined in interface)
 * public void init(IPlayerEvent.InitEvent event) {
 *     // ...
 * }
 *
 * // Custom addon hook (NOT in interface - still works!)
 * public void myAddonHook(MyAddonEvent event) {
 *     // ...
 * }
 *
 * // Any hook by name
 * public void someCustomHook(Object event) {
 *     // ...
 * }
 * }</pre>
 *
 * The interface below defines core hooks for:
 * <ul>
 *   <li>EnumScriptType mapping (so "init" maps to init() method)</li>
 *   <li>GUI hook list display</li>
 *   <li>Stub generation with correct parameter types</li>
 * </ul>
 */
public class JaninoPlayerScript extends JaninoScript<JaninoPlayerScript.Functions> {

    /**
     * Default imports for player scripts.
     * These are automatically available in user code without explicit import statements.
     */
    private static final String[] DEFAULT_IMPORTS = {
        "noppes.npcs.api.event.IPlayerEvent",
        "noppes.npcs.api.event.IQuestEvent",
        "noppes.npcs.api.event.IDialogEvent",
        "noppes.npcs.api.event.IFactionEvent",
        "noppes.npcs.api.event.IPartyEvent",
        "noppes.npcs.api.event.ICustomGuiEvent",
        "noppes.npcs.api.event.IAnimationEvent",
        "noppes.npcs.api.event.ICustomNPCsEvent",
        "noppes.npcs.api.entity.IPlayer",
        "noppes.npcs.api.NpcAPI"
    };

    public JaninoPlayerScript() {
        super(Functions.class, DEFAULT_IMPORTS, false);
    }

    /**
     * Core hook methods for player scripts.
     *
     * Note: Users can define additional methods beyond this interface!
     * Any public method matching a hook name will be found via reflection.
     */
    public interface Functions {
        // ==================== CORE LIFECYCLE ====================
        @ScriptHook(EnumScriptType.INIT)
        default void init(@ParamName("event") IPlayerEvent.InitEvent event) {}

        @ScriptHook(EnumScriptType.TICK)
        default void tick(@ParamName("event") IPlayerEvent.UpdateEvent event) {}

        @ScriptHook(EnumScriptType.TIMER)
        default void timer(@ParamName("event") IPlayerEvent.TimerEvent event) {}

        // ==================== COMBAT ====================
        @ScriptHook(EnumScriptType.ATTACK)
        default void attack(@ParamName("event") IPlayerEvent.AttackEvent event) {}

        @ScriptHook(EnumScriptType.ATTACKED)
        default void attacked(@ParamName("event") IPlayerEvent.AttackedEvent event) {}

        @ScriptHook(EnumScriptType.DAMAGED)
        default void damaged(@ParamName("event") IPlayerEvent.DamagedEvent event) {}

        @ScriptHook(EnumScriptType.DAMAGED_ENTITY)
        default void damagedEntity(@ParamName("event") IPlayerEvent.DamagedEntityEvent event) {}

        @ScriptHook(EnumScriptType.KILLS)
        default void kills(@ParamName("event") IPlayerEvent.KilledEntityEvent event) {}

        @ScriptHook(EnumScriptType.KILLED)
        default void killed(@ParamName("event") IPlayerEvent.DiedEvent event) {}

        // ==================== INTERACTION ====================
        @ScriptHook(EnumScriptType.INTERACT)
        default void interact(@ParamName("event") IPlayerEvent.InteractEvent event) {}

        @ScriptHook(EnumScriptType.RIGHT_CLICK)
        default void rightClick(@ParamName("event") IPlayerEvent.RightClickEvent event) {}

        @ScriptHook(EnumScriptType.BREAK_BLOCK)
        default void breakBlock(@ParamName("event") IPlayerEvent.BreakEvent event) {}

        // ==================== CONNECTION ====================
        @ScriptHook(EnumScriptType.LOGIN)
        default void login(@ParamName("event") IPlayerEvent.LoginEvent event) {}

        @ScriptHook(EnumScriptType.LOGOUT)
        default void logout(@ParamName("event") IPlayerEvent.LogoutEvent event) {}

        @ScriptHook(EnumScriptType.RESPAWN)
        default void respawn(@ParamName("event") IPlayerEvent.RespawnEvent event) {}

        // ==================== INPUT ====================
        @ScriptHook(EnumScriptType.KEY_PRESSED)
        default void keyPressed(@ParamName("event") IPlayerEvent.KeyPressedEvent event) {}

        @ScriptHook(EnumScriptType.MOUSE_CLICKED)
        default void mouseClicked(@ParamName("event") IPlayerEvent.MouseClickedEvent event) {}

        @ScriptHook(EnumScriptType.CHAT)
        default void chat(@ParamName("event") IPlayerEvent.ChatEvent event) {}

        // ==================== ITEMS ====================
        @ScriptHook(EnumScriptType.PICKUP)
        default void pickUp(@ParamName("event") IPlayerEvent.PickUpEvent event) {}

        @ScriptHook(EnumScriptType.TOSS)
        default void toss(@ParamName("event") IPlayerEvent.TossEvent event) {}

        @ScriptHook(EnumScriptType.DROP)
        default void drop(@ParamName("event") IPlayerEvent.DropEvent event) {}

        // ==================== MOVEMENT ====================
        @ScriptHook(EnumScriptType.JUMP)
        default void jump(@ParamName("event") IPlayerEvent.JumpEvent event) {}

        @ScriptHook(EnumScriptType.FALL)
        default void fall(@ParamName("event") IPlayerEvent.FallEvent event) {}

        @ScriptHook(EnumScriptType.CHANGED_DIM)
        default void changedDim(@ParamName("event") IPlayerEvent.ChangedDimension event) {}

        // ====================================================================================
        // ALL OTHER HOOKS WORK VIA DYNAMIC RESOLUTION!
        // ====================================================================================
        // Users can define methods like:
        //   public void questStart(IQuestEvent.QuestStartEvent event) {}
        //   public void dialogOpen(IDialogEvent.DialogOpen event) {}
        //   public void customGuiButton(ICustomGuiEvent.ButtonEvent event) {}
        //   public void partyInvite(IPartyEvent.PartyInviteEvent event) {}
        //   public void myAddonHook(AddonEvent event) {}
        //
        // These will be found automatically by the JaninoHookResolver!
        // ====================================================================================
    }
}
