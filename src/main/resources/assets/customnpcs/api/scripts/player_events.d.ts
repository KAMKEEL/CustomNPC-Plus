/// <reference path="../globals.d.ts" />
/// <reference path="../hooks.d.ts" />
/**
 * Ambient declarations for `scripts_examples/player_events.js`.
 * These map the function names used by CustomNPC+ player scripts to the
 * precise event interface so `event.` autocompletes correctly inside
 * each handler. Parameter names determine type inference:
 * - Use `IPlayerEvent` for player-specific events
 * - Use `IQuestEvent`, `IFactionEvent`, `IDialogEvent`, etc. for specialized event types
 */

declare function init(IPlayerEvent: IPlayerEvent.InitEvent): void;
declare function tick(IPlayerEvent: IPlayerEvent.UpdateEvent): void;
declare function interact(IPlayerEvent: IPlayerEvent.InteractEvent): void;
declare function rightClick(IPlayerEvent: IPlayerEvent.RightClickEvent): void;
declare function attack(IPlayerEvent: IPlayerEvent.AttackEvent): void;
declare function attacked(IPlayerEvent: IPlayerEvent.AttackedEvent): void;
declare function damagedEntity(IPlayerEvent: IPlayerEvent.DamagedEntityEvent): void;
declare function damaged(IPlayerEvent: IPlayerEvent.DamagedEvent): void;
declare function kills(IPlayerEvent: IPlayerEvent.KilledEntityEvent): void;
declare function killed(IPlayerEvent: IPlayerEvent.DiedEvent): void;
declare function drop(IPlayerEvent: IPlayerEvent.DropEvent): void;
declare function respawn(IPlayerEvent: IPlayerEvent.RespawnEvent): void;
declare function breakBlock(IPlayerEvent: IPlayerEvent.BreakEvent): void;
declare function chat(IPlayerEvent: IPlayerEvent.ChatEvent): void;
declare function login(IPlayerEvent: IPlayerEvent.LoginEvent): void;
declare function logout(IPlayerEvent: IPlayerEvent.LogoutEvent): void;
declare function keyPressed(IPlayerEvent: IPlayerEvent.KeyPressedEvent): void;
declare function mouseClicked(IPlayerEvent: IPlayerEvent.MouseClickedEvent): void;
declare function toss(IPlayerEvent: IPlayerEvent.TossEvent): void;
declare function pickUp(IPlayerEvent: IPlayerEvent.PickUpEvent): void;
declare function pickupXP(IPlayerEvent: IPlayerEvent.PickupXPEvent): void;
declare function rangedCharge(IPlayerEvent: IPlayerEvent.RangedChargeEvent): void;
declare function rangedLaunched(IPlayerEvent: IPlayerEvent.RangedLaunchedEvent): void;
declare function timer(IPlayerEvent: IPlayerEvent.TimerEvent): void;
declare function startItem(IPlayerEvent: IPlayerEvent.StartUsingItem): void;
declare function usingItem(IPlayerEvent: IPlayerEvent.UsingItem): void;
declare function stopItem(IPlayerEvent: IPlayerEvent.StopUsingItem): void;
declare function finishItem(IPlayerEvent: IPlayerEvent.FinishUsingItem): void;
declare function containerOpen(IPlayerEvent: IPlayerEvent.ContainerOpen): void;
declare function useHoe(IPlayerEvent: IPlayerEvent.UseHoeEvent): void;
declare function bonemeal(IPlayerEvent: IPlayerEvent.BonemealEvent): void;
declare function fillBucket(IPlayerEvent: IPlayerEvent.FillBucketEvent): void;
declare function jump(IPlayerEvent: IPlayerEvent.JumpEvent): void;
declare function fall(IPlayerEvent: IPlayerEvent.FallEvent): void;
declare function wakeUp(IPlayerEvent: IPlayerEvent.WakeUpEvent): void;
declare function sleep(IPlayerEvent: IPlayerEvent.SleepEvent): void;
declare function playSound(IPlayerEvent: IPlayerEvent.SoundEvent): void;
declare function lightning(IPlayerEvent: IPlayerEvent.LightningEvent): void;
declare function changedDim(IPlayerEvent: IPlayerEvent.ChangedDimension): void;

// Quest / faction / dialog / custom events
declare function questStart(IQuestEvent: IQuestEvent.QuestStartEvent): void;
declare function questCompleted(IQuestEvent: IQuestEvent.QuestCompletedEvent): void;
declare function questTurnIn(IQuestEvent: IQuestEvent.QuestTurnedInEvent): void;
declare function factionPoints(IFactionEvent: IFactionEvent.FactionPoints): void;

declare function dialogOpen(IDialogEvent: IDialogEvent.DialogClosed): void;
declare function dialogOption(IDialogEvent: IDialogEvent.DialogOption): void;
declare function dialogClose(IDialogEvent: IDialogEvent.DialogClosed): void;

declare function scriptCommand(ICustomNPCsEvent: ICustomNPCsEvent.ScriptedCommandEvent): void;
declare function customGuiClosed(ICustomGuiEvent: ICustomGuiEvent.CloseEvent): void;
declare function customGuiButton(ICustomGuiEvent: ICustomGuiEvent.ButtonEvent): void;
declare function customGuiSlot(ICustomGuiEvent: ICustomGuiEvent.SlotEvent): void;
declare function customGuiSlotClicked(ICustomGuiEvent: ICustomGuiEvent.SlotClickEvent): void;
declare function customGuiScroll(ICustomGuiEvent: ICustomGuiEvent.ScrollEvent): void;

// Party events
declare function partyQuestCompleted(IPartyEvent: IPartyEvent.PartyQuestCompletedEvent): void;
declare function partyQuestSet(IPartyEvent: IPartyEvent.PartyQuestSetEvent): void;
declare function partyQuestTurnedIn(IPartyEvent: IPartyEvent.PartyQuestTurnedInEvent): void;
declare function partyInvite(IPartyEvent: IPartyEvent.PartyInviteEvent): void;
declare function partyKick(IPartyEvent: IPartyEvent.PartyKickEvent): void;
declare function partyLeave(IPartyEvent: IPartyEvent.PartyLeaveEvent): void;
declare function partyDisband(IPartyEvent: IPartyEvent.PartyDisbandEvent): void;

// Animation events
declare function animationStart(IAnimationEvent: IAnimationEvent.Started): void;
declare function animationEnd(IAnimationEvent: IAnimationEvent.Ended): void;
declare function frameEnter(IAnimationEvent: IAnimationEvent.IFrameEvent): void;
declare function frameExit(IAnimationEvent: IAnimationEvent.IFrameEvent): void;

// Profile / effect events
declare function profileChange(IPlayerEvent: IPlayerEvent.ProfileEvent): void;
declare function profileRemove(IPlayerEvent: IPlayerEvent.ProfileEvent): void;
declare function profileCreate(IPlayerEvent: IPlayerEvent.ProfileEvent): void;
declare function onEffectAdd(IPlayerEvent: IPlayerEvent.EffectEvent): void;
declare function onEffectTick(IPlayerEvent: IPlayerEvent.EffectEvent): void;
declare function onEffectRemove(IPlayerEvent: IPlayerEvent.EffectEvent): void;

export {};
