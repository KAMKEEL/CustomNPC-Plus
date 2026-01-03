/// <reference path="../globals.d.ts" />
/// <reference path="../hooks.d.ts" />
/**
 * Ambient declarations for `scripts_examples/npc_events.js`.
 * These map the function names used by CustomNPC+ NPC scripts to the
 * precise event interface so `event.` autocompletes correctly inside
 * each handler. Parameter names determine type inference:
 * - Use `INpcEvent` for NPC-specific events
 * - Use `IProjectileEvent` for projectile events
 */

declare function init(INpcEvent: INpcEvent.InitEvent): void;
declare function tick(INpcEvent: INpcEvent.UpdateEvent): void;
declare function interact(INpcEvent: INpcEvent.InteractEvent): void;
declare function dialog(INpcEvent: INpcEvent.DialogEvent): void;
declare function damaged(INpcEvent: INpcEvent.DamagedEvent): void;
declare function killed(INpcEvent: INpcEvent.DiedEvent): void;
declare function meleeAttack(INpcEvent: INpcEvent.MeleeAttackEvent): void;
declare function meleeSwing(INpcEvent: INpcEvent.SwingEvent): void;
declare function rangedLaunched(INpcEvent: INpcEvent.RangedLaunchedEvent): void;
declare function target(INpcEvent: INpcEvent.TargetEvent): void;
declare function collide(INpcEvent: INpcEvent.CollideEvent): void;
declare function kills(INpcEvent: INpcEvent.KilledEntityEvent): void;
declare function dialogClose(INpcEvent: INpcEvent.DialogClosedEvent): void;
declare function timer(INpcEvent: INpcEvent.TimerEvent): void;
declare function targetLost(INpcEvent: INpcEvent.TargetLostEvent): void;

// Projectile-specific
declare function projectileTick(IProjectileEvent: IProjectileEvent.UpdateEvent): void;
declare function projectileImpact(IProjectileEvent: IProjectileEvent.ImpactEvent): void;

export {};
