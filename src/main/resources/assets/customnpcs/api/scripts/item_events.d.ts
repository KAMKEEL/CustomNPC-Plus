/// <reference path="../globals.d.ts" />
/// <reference path="../hooks.d.ts" />
/**
 * Ambient declarations for `scripts_examples/item_events.js`.
 * These map the function names used by CustomNPC+ item scripts to the
 * precise event interface so `event.` autocompletes correctly inside
 * each handler. This file is intended to be referenced from the script
 * with a triple-slash reference comment.
 */

declare function init(IItemEvent: IItemEvent.InitEvent): void;
declare function tick(IItemEvent: IItemEvent.UpdateEvent): void;
declare function tossed(IItemEvent: IItemEvent.TossedEvent): void;
declare function pickedUp(IItemEvent: IItemEvent.PickedUpEvent): void;
declare function spawn(IItemEvent: IItemEvent.SpawnEvent): void;
declare function interact(IItemEvent: IItemEvent.InteractEvent): void;
declare function rightClick(IItemEvent: IItemEvent.RightClickEvent): void;
declare function attack(IItemEvent: IItemEvent.AttackEvent): void;
declare function startItem(IItemEvent: IItemEvent.StartUsingItem): void;
declare function usingItem(IItemEvent: IItemEvent.UsingItem): void;
declare function stopItem(IItemEvent: IItemEvent.StopUsingItem): void;
declare function finishItem(IItemEvent: IItemEvent.FinishUsingItem): void;

export {};
