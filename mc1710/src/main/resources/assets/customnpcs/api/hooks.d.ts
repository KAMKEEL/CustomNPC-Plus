/**
 * CustomNPC+ Event Hook Declarations
 *
 * Hooks are organized by their parent event interface (e.g., INpcEvent, IPlayerEvent).
 * This allows the same hook name to have different event types per script context.
 *
 * Auto-generated from Java event interfaces - do not edit manually.
 */

import './minecraft-raw.d.ts';
import './forge-events-raw.d.ts';

declare namespace IAbilityEvent {
    function complete(event: IAbilityEvent.CompleteEvent): void;
    function execute(event: IAbilityEvent.ExecuteEvent): void;
    function hit(event: IAbilityEvent.HitEvent): void;
    function interrupt(event: IAbilityEvent.InterruptEvent): void;
    function start(event: IAbilityEvent.StartEvent): void;
    function tick(event: IAbilityEvent.TickEvent): void;
    function toggle(event: IAbilityEvent.ToggleEvent): void;
    function toggleUpdate(event: IAbilityEvent.ToggleUpdateEvent): void;
}

declare namespace IAnimationEvent {
    function animationEnd(event: IAnimationEvent.Ended): void;
    function animationStart(event: IAnimationEvent.Started): void;
    function frameEnter(event: IAnimationEvent.IFrameEvent.Entered): void;
    function frameExit(event: IAnimationEvent.IFrameEvent.Exited): void;
    function iFrame(event: IAnimationEvent.IFrameEvent): void;
}

declare namespace IAuctionEvent {
    function auctionBid(event: IAuctionEvent.BidEvent): void;
    function auctionBuyout(event: IAuctionEvent.BuyoutEvent): void;
    function auctionCancel(event: IAuctionEvent.CancelEvent): void;
    function auctionClaim(event: IAuctionEvent.ClaimEvent): void;
    function auctionCreate(event: IAuctionEvent.CreateEvent): void;
}

declare namespace IBlockEvent {
    function broken(event: IBlockEvent.BreakEvent): void;
    function clicked(event: IBlockEvent.ClickedEvent): void;
    function collided(event: IBlockEvent.CollidedEvent): void;
    function exploded(event: IBlockEvent.ExplodedEvent): void;
    function fallenUpon(event: IBlockEvent.EntityFallenUponEvent): void;
    function harvested(event: IBlockEvent.HarvestedEvent): void;
    function init(event: IBlockEvent.InitEvent): void;
    function interact(event: IBlockEvent.InteractEvent): void;
    function neighborChanged(event: IBlockEvent.NeighborChangedEvent): void;
    function rainFilled(event: IBlockEvent.RainFillEvent): void;
    function redstone(event: IBlockEvent.RedstoneEvent): void;
    function tick(event: IBlockEvent.UpdateEvent): void;
    function timer(event: IBlockEvent.TimerEvent): void;
}

declare namespace IChainEvent {
    function complete(event: IChainEvent.CompleteEvent): void;
    function interrupt(event: IChainEvent.InterruptEvent): void;
    function next(event: IChainEvent.NextEvent): void;
    function start(event: IChainEvent.StartEvent): void;
}

declare namespace ICustomGuiEvent {
    function customGuiButton(event: ICustomGuiEvent.ButtonEvent): void;
    function customGuiClosed(event: ICustomGuiEvent.CloseEvent): void;
    function customGuiScroll(event: ICustomGuiEvent.ScrollEvent): void;
    function customGuiSlot(event: ICustomGuiEvent.SlotEvent): void;
    function customGuiSlotClicked(event: ICustomGuiEvent.SlotClickEvent): void;
    function customGuiTextfield(event: ICustomGuiEvent.UnfocusedEvent): void;
}

declare namespace ICustomNPCsEvent {
    function onCNPCNaturalSpawn(event: ICustomNPCsEvent.CNPCNaturalSpawnEvent): void;
    function scriptedCommand(event: ICustomNPCsEvent.ScriptedCommandEvent): void;
}

declare namespace IDialogEvent {
    function dialogClose(event: IDialogEvent.DialogClosed): void;
    function dialogOpen(event: IDialogEvent.DialogOpen): void;
    function dialogOption(event: IDialogEvent.DialogOption): void;
}

declare namespace IEnergyBarrierEvent {
    function energyBarrierDestroyed(event: IEnergyBarrierEvent.DestroyedEvent): void;
    function energyBarrierHit(event: IEnergyBarrierEvent.HitEvent): void;
    function energyBarrierSpawned(event: IEnergyBarrierEvent.SpawnedEvent): void;
    function energyBarrierTick(event: IEnergyBarrierEvent.UpdateEvent): void;
}

declare namespace IEnergyProjectileEvent {
    function energyProjectileBlockImpact(event: IEnergyProjectileEvent.BlockImpactEvent): void;
    function energyProjectileEntityImpact(event: IEnergyProjectileEvent.EntityImpactEvent): void;
    function energyProjectileExpired(event: IEnergyProjectileEvent.ExpiredEvent): void;
    function energyProjectileFired(event: IEnergyProjectileEvent.FiredEvent): void;
    function energyProjectileTick(event: IEnergyProjectileEvent.UpdateEvent): void;
}

declare namespace IFactionEvent {
    function factionPoints(event: IFactionEvent.FactionPoints): void;
}

declare namespace IForgeEvent {
    function forgeEntity(event: IForgeEvent.EntityEvent): void;
    function forgeInit(event: IForgeEvent.InitEvent): void;
    function forgeWorld(event: IForgeEvent.WorldEvent): void;
}

declare namespace IItemEvent {
    function attack(event: IItemEvent.AttackEvent): void;
    function breakItem(event: IItemEvent.BreakItem): void;
    function finishItem(event: IItemEvent.FinishUsingItem): void;
    function init(event: IItemEvent.InitEvent): void;
    function interact(event: IItemEvent.InteractEvent): void;
    function pickedUp(event: IItemEvent.PickedUpEvent): void;
    function repairItem(event: IItemEvent.RepairItem): void;
    function rightClick(event: IItemEvent.RightClickEvent): void;
    function spawn(event: IItemEvent.SpawnEvent): void;
    function startItem(event: IItemEvent.StartUsingItem): void;
    function stopItem(event: IItemEvent.StopUsingItem): void;
    function tick(event: IItemEvent.UpdateEvent): void;
    function tossed(event: IItemEvent.TossedEvent): void;
    function usingItem(event: IItemEvent.UsingItem): void;
}

declare namespace ILinkedItemEvent {
    function buildingItem(event: ILinkedItemEvent.BuildEvent): void;
    function versionChanged(event: ILinkedItemEvent.VersionChangeEvent): void;
}

declare namespace INpcEvent {
    function collide(event: INpcEvent.CollideEvent): void;
    function damaged(event: INpcEvent.DamagedEvent): void;
    function dialog(event: INpcEvent.DialogEvent): void;
    function dialogClosed(event: INpcEvent.DialogClosedEvent): void;
    function init(event: INpcEvent.InitEvent): void;
    function interact(event: INpcEvent.InteractEvent): void;
    function killed(event: INpcEvent.DiedEvent): void;
    function kills(event: INpcEvent.KilledEntityEvent): void;
    function meleeAttack(event: INpcEvent.MeleeAttackEvent): void;
    function meleeSwing(event: INpcEvent.SwingEvent): void;
    function rangedLaunched(event: INpcEvent.RangedLaunchedEvent): void;
    function target(event: INpcEvent.TargetEvent): void;
    function targetLost(event: INpcEvent.TargetLostEvent): void;
    function tick(event: INpcEvent.UpdateEvent): void;
    function timer(event: INpcEvent.TimerEvent): void;
}

declare namespace IPartyEvent {
    function partyDisband(event: IPartyEvent.PartyDisbandEvent): void;
    function partyInvite(event: IPartyEvent.PartyInviteEvent): void;
    function partyKick(event: IPartyEvent.PartyKickEvent): void;
    function partyLeave(event: IPartyEvent.PartyLeaveEvent): void;
    function partyQuestCompleted(event: IPartyEvent.PartyQuestCompletedEvent): void;
    function partyQuestSet(event: IPartyEvent.PartyQuestSetEvent): void;
    function partyQuestTurnedIn(event: IPartyEvent.PartyQuestTurnedInEvent): void;
}

declare namespace IPlayerEvent {
    function achievement(event: IPlayerEvent.AchievementEvent): void;
    function attack(event: IPlayerEvent.AttackEvent): void;
    function attacked(event: IPlayerEvent.AttackedEvent): void;
    function bonemeal(event: IPlayerEvent.BonemealEvent): void;
    function breakBlock(event: IPlayerEvent.BreakEvent): void;
    function changedDim(event: IPlayerEvent.ChangedDimension): void;
    function chat(event: IPlayerEvent.ChatEvent): void;
    function containerClosed(event: IPlayerEvent.ContainerClosed): void;
    function containerOpen(event: IPlayerEvent.ContainerOpen): void;
    function damaged(event: IPlayerEvent.DamagedEvent): void;
    function damagedEntity(event: IPlayerEvent.DamagedEntityEvent): void;
    function drop(event: IPlayerEvent.DropEvent): void;
    function fall(event: IPlayerEvent.FallEvent): void;
    function fillBucket(event: IPlayerEvent.FillBucketEvent): void;
    function finishItem(event: IPlayerEvent.FinishUsingItem): void;
    function init(event: IPlayerEvent.InitEvent): void;
    function interact(event: IPlayerEvent.InteractEvent): void;
    function jump(event: IPlayerEvent.JumpEvent): void;
    function keyPressed(event: IPlayerEvent.KeyPressedEvent): void;
    function killed(event: IPlayerEvent.DiedEvent): void;
    function kills(event: IPlayerEvent.KilledEntityEvent): void;
    function levelUp(event: IPlayerEvent.LevelUpEvent): void;
    function lightning(event: IPlayerEvent.LightningEvent): void;
    function login(event: IPlayerEvent.LoginEvent): void;
    function logout(event: IPlayerEvent.LogoutEvent): void;
    function mouseClicked(event: IPlayerEvent.MouseClickedEvent): void;
    function onEffect(event: IPlayerEvent.EffectEvent): void;
    function onEffectAdd(event: IPlayerEvent.EffectEvent.Added): void;
    function onEffectRemove(event: IPlayerEvent.EffectEvent.Removed): void;
    function onEffectTick(event: IPlayerEvent.EffectEvent.Ticked): void;
    function pickUp(event: IPlayerEvent.PickUpEvent): void;
    function pickupXP(event: IPlayerEvent.PickupXPEvent): void;
    function playSound(event: IPlayerEvent.SoundEvent): void;
    function profile(event: IPlayerEvent.ProfileEvent): void;
    function profileChange(event: IPlayerEvent.ProfileEvent.Changed): void;
    function profileCreate(event: IPlayerEvent.ProfileEvent.Create): void;
    function profileRemove(event: IPlayerEvent.ProfileEvent.Removed): void;
    function rangedCharge(event: IPlayerEvent.RangedChargeEvent): void;
    function rangedLaunched(event: IPlayerEvent.RangedLaunchedEvent): void;
    function respawn(event: IPlayerEvent.RespawnEvent): void;
    function rightClick(event: IPlayerEvent.RightClickEvent): void;
    function sleep(event: IPlayerEvent.SleepEvent): void;
    function startItem(event: IPlayerEvent.StartUsingItem): void;
    function stopItem(event: IPlayerEvent.StopUsingItem): void;
    function tick(event: IPlayerEvent.UpdateEvent): void;
    function timer(event: IPlayerEvent.TimerEvent): void;
    function toss(event: IPlayerEvent.TossEvent): void;
    function useHoe(event: IPlayerEvent.UseHoeEvent): void;
    function usingItem(event: IPlayerEvent.UsingItem): void;
    function wakeUp(event: IPlayerEvent.WakeUpEvent): void;
}

declare namespace IProjectileEvent {
    function projectileImpact(event: IProjectileEvent.ImpactEvent): void;
    function projectileTick(event: IProjectileEvent.UpdateEvent): void;
}

declare namespace IQuestEvent {
    function questCompleted(event: IQuestEvent.QuestCompletedEvent): void;
    function questStart(event: IQuestEvent.QuestStartEvent): void;
    function questTurnIn(event: IQuestEvent.QuestTurnedInEvent): void;
}

declare namespace IRecipeEvent {
    function recipeCraftPost(event: IRecipeEvent.Post): void;
    function recipeCraftPre(event: IRecipeEvent.Pre): void;
}

export {};
