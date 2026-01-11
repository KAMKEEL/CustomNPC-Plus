package noppes.npcs.controllers;

import noppes.npcs.api.handler.IScriptHookHandler;
import noppes.npcs.constants.EnumScriptType;
import noppes.npcs.controllers.data.EffectScript;
import noppes.npcs.controllers.data.RecipeScript;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for managing script hooks.
 * Allows addon mods to register custom hooks that appear in script editor GUIs.
 */
public class ScriptHookController implements IScriptHookHandler {

    public static ScriptHookController Instance;

    // Built-in hooks per context
    private final Map<String, List<String>> builtInHooks = new HashMap<>();

    // Addon-registered hooks per context
    private final Map<String, List<String>> addonHooks = new HashMap<>();

    // All available contexts
    private static final String[] CONTEXTS = {
        CONTEXT_NPC,
        CONTEXT_PLAYER,
        CONTEXT_BLOCK,
        CONTEXT_ITEM,
        CONTEXT_LINKED_ITEM,
        CONTEXT_FORGE,
        CONTEXT_GLOBAL_NPC,
        CONTEXT_RECIPE,
        CONTEXT_EFFECT
    };

    public ScriptHookController() {
        Instance = this;
        initializeBuiltInHooks();
    }

    private void initializeBuiltInHooks() {
        // Initialize empty lists for all contexts
        for (String context : CONTEXTS) {
            builtInHooks.put(context, new ArrayList<>());
            addonHooks.put(context, new ArrayList<>());
        }

        // NPC hooks
        registerBuiltIn(CONTEXT_NPC,
            EnumScriptType.INIT.function,
            EnumScriptType.TICK.function,
            EnumScriptType.INTERACT.function,
            EnumScriptType.DIALOG.function,
            EnumScriptType.DAMAGED.function,
            EnumScriptType.KILLED.function,
            EnumScriptType.ATTACK_MELEE.function,
            EnumScriptType.ATTACK_SWING.function,
            EnumScriptType.RANGED_LAUNCHED.function,
            EnumScriptType.TARGET.function,
            EnumScriptType.COLLIDE.function,
            EnumScriptType.KILLS.function,
            EnumScriptType.DIALOG_CLOSE.function,
            EnumScriptType.TIMER.function,
            EnumScriptType.TARGET_LOST.function,
            EnumScriptType.PROJECTILE_TICK.function,
            EnumScriptType.PROJECTILE_IMPACT.function
        );

        // Global NPC hooks (same as NPC)
        registerBuiltIn(CONTEXT_GLOBAL_NPC,
            EnumScriptType.INIT.function,
            EnumScriptType.TICK.function,
            EnumScriptType.INTERACT.function,
            EnumScriptType.DIALOG.function,
            EnumScriptType.DAMAGED.function,
            EnumScriptType.KILLED.function,
            EnumScriptType.ATTACK_MELEE.function,
            EnumScriptType.ATTACK_SWING.function,
            EnumScriptType.RANGED_LAUNCHED.function,
            EnumScriptType.TARGET.function,
            EnumScriptType.COLLIDE.function,
            EnumScriptType.KILLS.function,
            EnumScriptType.DIALOG_CLOSE.function,
            EnumScriptType.TIMER.function,
            EnumScriptType.TARGET_LOST.function,
            EnumScriptType.PROJECTILE_TICK.function,
            EnumScriptType.PROJECTILE_IMPACT.function
        );

        // Player hooks
        registerBuiltIn(CONTEXT_PLAYER,
            EnumScriptType.INIT.function,
            EnumScriptType.TICK.function,
            EnumScriptType.INTERACT.function,
            EnumScriptType.RIGHT_CLICK.function,
            EnumScriptType.ATTACK.function,
            EnumScriptType.ATTACKED.function,
            EnumScriptType.DAMAGED_ENTITY.function,
            EnumScriptType.DAMAGED.function,
            EnumScriptType.KILLS.function,
            EnumScriptType.KILLED.function,
            EnumScriptType.DROP.function,
            EnumScriptType.RESPAWN.function,
            EnumScriptType.BREAK_BLOCK.function,
            EnumScriptType.CHAT.function,
            EnumScriptType.LOGIN.function,
            EnumScriptType.LOGOUT.function,
            EnumScriptType.KEY_PRESSED.function,
            EnumScriptType.MOUSE_CLICKED.function,
            EnumScriptType.TOSS.function,
            EnumScriptType.PICKUP.function,
            EnumScriptType.PICKUP_XP.function,
            EnumScriptType.RANGED_CHARGE.function,
            EnumScriptType.RANGED_LAUNCHED.function,
            EnumScriptType.TIMER.function,
            EnumScriptType.START_USING_ITEM.function,
            EnumScriptType.USING_ITEM.function,
            EnumScriptType.STOP_USING_ITEM.function,
            EnumScriptType.FINISH_USING_ITEM.function,
            EnumScriptType.CONTAINER_OPEN.function,
            EnumScriptType.USE_HOE.function,
            EnumScriptType.BONEMEAL.function,
            EnumScriptType.FILL_BUCKET.function,
            EnumScriptType.JUMP.function,
            EnumScriptType.FALL.function,
            EnumScriptType.WAKE_UP.function,
            EnumScriptType.SLEEP.function,
            EnumScriptType.PLAYSOUND.function,
            EnumScriptType.LIGHTNING.function,
            EnumScriptType.CHANGED_DIM.function,
            EnumScriptType.QUEST_START.function,
            EnumScriptType.QUEST_COMPLETED.function,
            EnumScriptType.QUEST_TURNIN.function,
            EnumScriptType.FACTION_POINTS.function,
            EnumScriptType.DIALOG_OPEN.function,
            EnumScriptType.DIALOG_OPTION.function,
            EnumScriptType.DIALOG_CLOSE.function,
            EnumScriptType.SCRIPT_COMMAND.function,
            EnumScriptType.CUSTOM_GUI_CLOSED.function,
            EnumScriptType.CUSTOM_GUI_BUTTON.function,
            EnumScriptType.CUSTOM_GUI_SLOT.function,
            EnumScriptType.CUSTOM_GUI_SLOT_CLICKED.function,
            EnumScriptType.CUSTOM_GUI_SCROLL.function,
            EnumScriptType.CUSTOM_GUI_TEXTFIELD.function,
            EnumScriptType.PARTY_QUEST_COMPLETED.function,
            EnumScriptType.PARTY_QUEST_SET.function,
            EnumScriptType.PARTY_QUEST_TURNED_IN.function,
            EnumScriptType.PARTY_INVITE.function,
            EnumScriptType.PARTY_KICK.function,
            EnumScriptType.PARTY_LEAVE.function,
            EnumScriptType.PARTY_DISBAND.function,
            EnumScriptType.ANIMATION_START.function,
            EnumScriptType.ANIMATION_END.function,
            EnumScriptType.ANIMATION_FRAME_ENTER.function,
            EnumScriptType.ANIMATION_FRAME_EXIT.function,
            EnumScriptType.PROFILE_CHANGE.function,
            EnumScriptType.PROFILE_REMOVE.function,
            EnumScriptType.PROFILE_CREATE.function,
            EffectScript.ScriptType.OnEffectAdd.function,
            EffectScript.ScriptType.OnEffectTick.function,
            EffectScript.ScriptType.OnEffectRemove.function
        );

        // Block hooks
        registerBuiltIn(CONTEXT_BLOCK,
            EnumScriptType.INIT.function,
            EnumScriptType.TICK.function,
            EnumScriptType.INTERACT.function,
            EnumScriptType.FALLEN_UPON.function,
            EnumScriptType.REDSTONE.function,
            EnumScriptType.BROKEN.function,
            EnumScriptType.EXPLODED.function,
            EnumScriptType.RAIN_FILLED.function,
            EnumScriptType.NEIGHBOR_CHANGED.function,
            EnumScriptType.CLICKED.function,
            EnumScriptType.HARVESTED.function,
            EnumScriptType.COLLIDE.function,
            EnumScriptType.TIMER.function
        );

        // Item hooks
        registerBuiltIn(CONTEXT_ITEM,
            EnumScriptType.INIT.function,
            EnumScriptType.TICK.function,
            EnumScriptType.TOSSED.function,
            EnumScriptType.PICKEDUP.function,
            EnumScriptType.SPAWN.function,
            EnumScriptType.INTERACT.function,
            EnumScriptType.RIGHT_CLICK.function,
            EnumScriptType.ATTACK.function,
            EnumScriptType.START_USING_ITEM.function,
            EnumScriptType.USING_ITEM.function,
            EnumScriptType.STOP_USING_ITEM.function,
            EnumScriptType.FINISH_USING_ITEM.function
        );

        // Linked item hooks
        registerBuiltIn(CONTEXT_LINKED_ITEM,
            EnumScriptType.LINKED_ITEM_BUILD.function,
            EnumScriptType.LINKED_ITEM_VERSION.function,
            EnumScriptType.INIT.function,
            EnumScriptType.TICK.function,
            EnumScriptType.TOSSED.function,
            EnumScriptType.PICKEDUP.function,
            EnumScriptType.SPAWN.function,
            EnumScriptType.INTERACT.function,
            EnumScriptType.RIGHT_CLICK.function,
            EnumScriptType.ATTACK.function,
            EnumScriptType.START_USING_ITEM.function,
            EnumScriptType.USING_ITEM.function,
            EnumScriptType.STOP_USING_ITEM.function,
            EnumScriptType.FINISH_USING_ITEM.function
        );

        // Recipe hooks
        registerBuiltIn(CONTEXT_RECIPE,
            RecipeScript.ScriptType.PRE.function,
            RecipeScript.ScriptType.POST.function
        );

        // Effect hooks
        registerBuiltIn(CONTEXT_EFFECT,
            EffectScript.ScriptType.OnEffectAdd.function,
            EffectScript.ScriptType.OnEffectTick.function,
            EffectScript.ScriptType.OnEffectRemove.function
        );

        // Forge hooks - just init, the rest are dynamically discovered
        registerBuiltIn(CONTEXT_FORGE,
            EnumScriptType.INIT.function
        );
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
        return CONTEXTS.clone();
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
