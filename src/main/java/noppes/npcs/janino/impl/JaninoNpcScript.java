package noppes.npcs.janino.impl;

import noppes.npcs.api.event.IAbilityEvent;
import noppes.npcs.api.event.INpcEvent;
import noppes.npcs.api.event.IProjectileEvent;
import noppes.npcs.constants.EnumScriptType;
import noppes.npcs.janino.JaninoScript;
import noppes.npcs.janino.annotations.ParamName;
import noppes.npcs.janino.annotations.ScriptHook;

/**
 * Janino (Java) script implementation for NPC events.
 * This allows NPCs to use compiled Java code instead of ECMAScript.
 */
public class JaninoNpcScript extends JaninoScript<JaninoNpcScript.Functions> {

    public JaninoNpcScript() {
        super(Functions.class, (builder) -> builder
            .setDefaultImports(
                "noppes.npcs.api.*",
                "noppes.npcs.api.entity.*",
                "noppes.npcs.api.event.*",
                "noppes.npcs.api.handler.*",
                "noppes.npcs.api.handler.data.*",
                "noppes.npcs.api.item.*",
                "noppes.npcs.api.ability.*"
            )
        );
    }

    /**
     * Interface defining the hook methods available for NPC scripts.
     * Each method corresponds to an NPC event from ScriptHookController CONTEXT_NPC.
     * Methods are annotated with @ScriptHook to map them to their EnumScriptType.
     */
    public interface Functions {
        // Core lifecycle events
        @ScriptHook(EnumScriptType.INIT)
        default void init(@ParamName("event") INpcEvent.InitEvent event) {}
        
        @ScriptHook(EnumScriptType.TICK)
        default void tick(@ParamName("event") INpcEvent.UpdateEvent event) {}
        
        
        // Interaction events
        @ScriptHook(EnumScriptType.INTERACT)
        default void interact(@ParamName("event") INpcEvent.InteractEvent event) {}
        
        @ScriptHook(EnumScriptType.DIALOG)
        default void dialog(@ParamName("event") INpcEvent.DialogEvent event) {}
        
        @ScriptHook(EnumScriptType.DIALOG_CLOSE)
        default void dialogClose(@ParamName("event") INpcEvent.DialogClosedEvent event) {}
        
        
        // Combat events - damage
        @ScriptHook(EnumScriptType.DAMAGED)
        default void damaged(@ParamName("event") INpcEvent.DamagedEvent event) {}
        
        @ScriptHook(EnumScriptType.KILLED)
        default void killed(@ParamName("event") INpcEvent.DiedEvent event) {}
        
        @ScriptHook(EnumScriptType.KILLS)
        default void kills(@ParamName("event") INpcEvent.KilledEntityEvent event) {}
        
        
        // Combat events - attacks
        @ScriptHook(EnumScriptType.ATTACK_MELEE)
        default void meleeAttack(@ParamName("event") INpcEvent.MeleeAttackEvent event) {}
        
        @ScriptHook(EnumScriptType.ATTACK_SWING)
        default void meleeSwing(@ParamName("event") INpcEvent.SwingEvent event) {}
        
        @ScriptHook(EnumScriptType.RANGED_LAUNCHED)
        default void rangedLaunched(@ParamName("event") INpcEvent.RangedLaunchedEvent event) {}
        
        
        // Targeting events
        @ScriptHook(EnumScriptType.TARGET)
        default void target(@ParamName("event") INpcEvent.TargetEvent event) {}
        
        @ScriptHook(EnumScriptType.TARGET_LOST)
        default void targetLost(@ParamName("event") INpcEvent.TargetLostEvent event) {}
        
        // Other NPC events
        @ScriptHook(EnumScriptType.COLLIDE)
        default void collide(@ParamName("event") INpcEvent.CollideEvent event) {}
        
        @ScriptHook(EnumScriptType.TIMER)
        default void timer(@ParamName("event") INpcEvent.TimerEvent event) {}
        
        
        // Projectile events
        @ScriptHook(EnumScriptType.PROJECTILE_TICK)
        default void projectileTick(@ParamName("event") IProjectileEvent.UpdateEvent event) {}
        
        @ScriptHook(EnumScriptType.PROJECTILE_IMPACT)
        default void projectileImpact(@ParamName("event") IProjectileEvent.ImpactEvent event) {}
        
        
        // Ability events
        @ScriptHook(EnumScriptType.ABILITY_START)
        default void abilityStart(@ParamName("event") IAbilityEvent.StartEvent event) {}
        
        @ScriptHook(EnumScriptType.ABILITY_EXECUTE)
        default void abilityExecute(@ParamName("event") IAbilityEvent.ExecuteEvent event) {}
        
        @ScriptHook(EnumScriptType.ABILITY_HIT)
        default void abilityHit(@ParamName("event") IAbilityEvent.HitEvent event) {}
        
        @ScriptHook(EnumScriptType.ABILITY_TICK)
        default void abilityTick(@ParamName("event") IAbilityEvent.TickEvent event) {}
        
        @ScriptHook(EnumScriptType.ABILITY_INTERRUPT)
        default void abilityInterrupt(@ParamName("event") IAbilityEvent.InterruptEvent event) {}
        
        @ScriptHook(EnumScriptType.ABILITY_COMPLETE)
        default void abilityComplete(@ParamName("event") IAbilityEvent.CompleteEvent event) {}
    }
}
