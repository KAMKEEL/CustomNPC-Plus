import './minecraft-raw.d.ts';
import './forge-events-raw.d.ts';

/**
 * CustomNPC+ Event Hook Overloads
 * 
 * Functions are grouped by name to enable precise type inference.
 * For common hooks (init, tick, damaged), parameter names (INpcEvent, IPlayerEvent, etc.)
 * are used to help the LSP distinguish between multiple valid overloads.
 */

declare global {
    // ============ Global Lifecycle & Common Hooks ============
    function init(INpcEvent: INpcEvent.InitEvent): void;
    function init(IPlayerEvent: IPlayerEvent.InitEvent): void;
    function init(IItemEvent: IItemEvent.InitEvent): void;
    function init(IBlockEvent: IBlockEvent.InitEvent): void;
    function init(ForgeEvent: cpw.mods.fml.common.eventhandler.Event): void;

    function tick(INpcEvent: INpcEvent.UpdateEvent): void;
    function tick(IPlayerEvent: IPlayerEvent.UpdateEvent): void;
    function tick(IItemEvent: IItemEvent.UpdateEvent): void;
    function tick(IBlockEvent: IBlockEvent.UpdateEvent): void;

    function interact(INpcEvent: INpcEvent.InteractEvent): void;
    function interact(IPlayerEvent: IPlayerEvent.InteractEvent): void;
    function interact(IItemEvent: IItemEvent.InteractEvent): void;
    function interact(IBlockEvent: IBlockEvent.InteractEvent): void;

    function rightClick(IPlayerEvent: IPlayerEvent.RightClickEvent): void;
    function rightClick(IItemEvent: IItemEvent.RightClickEvent): void;

    function attack(IPlayerEvent: IPlayerEvent.AttackEvent): void;
    function attack(IItemEvent: IItemEvent.AttackEvent): void;

    function damaged(INpcEvent: INpcEvent.DamagedEvent): void;
    function damaged(IPlayerEvent: IPlayerEvent.DamagedEvent): void;
    function damaged(ForgeEvent: net.minecraftforge.event.entity.living.LivingHurtEvent): void;

    function killed(INpcEvent: INpcEvent.DiedEvent): void;
    function killed(IPlayerEvent: IPlayerEvent.DiedEvent): void;

    function kills(INpcEvent: INpcEvent.KilledEntityEvent): void;
    function kills(IPlayerEvent: IPlayerEvent.KilledEntityEvent): void;

    function timer(INpcEvent: INpcEvent.TimerEvent): void;
    function timer(IPlayerEvent: IPlayerEvent.TimerEvent): void;
    function timer(IBlockEvent: IBlockEvent.TimerEvent): void;

    function dialogClose(INpcEvent: INpcEvent.DialogClosedEvent): void;
    function dialogClose(IDialogEvent: IDialogEvent.DialogClosed): void;

    function rangedLaunched(INpcEvent: INpcEvent.RangedLaunchedEvent): void;
    function rangedLaunched(IPlayerEvent: IPlayerEvent.RangedLaunchedEvent): void;

    function startItem(IPlayerEvent: IPlayerEvent.StartUsingItem): void;
    function startItem(IItemEvent: IItemEvent.StartUsingItem): void;

    function usingItem(IPlayerEvent: IPlayerEvent.UsingItem): void;
    function usingItem(IItemEvent: IItemEvent.UsingItem): void;

    function stopItem(IPlayerEvent: IPlayerEvent.StopUsingItem): void;
    function stopItem(IItemEvent: IItemEvent.StopUsingItem): void;

    function finishItem(IPlayerEvent: IPlayerEvent.FinishUsingItem): void;
    function finishItem(IItemEvent: IItemEvent.FinishUsingItem): void;

    // ============ Player Specific Hooks ============
    function attacked(IPlayerEvent: IPlayerEvent.AttackedEvent): void;
    function damagedEntity(IPlayerEvent: IPlayerEvent.DamagedEntityEvent): void;
    function drop(IPlayerEvent: IPlayerEvent.DropEvent): void;
    function respawn(IPlayerEvent: IPlayerEvent.RespawnEvent): void;
    function breakBlock(IPlayerEvent: IPlayerEvent.BreakEvent): void;
    function chat(IPlayerEvent: IPlayerEvent.ChatEvent): void;
    function login(IPlayerEvent: IPlayerEvent.LoginEvent): void;
    function logout(IPlayerEvent: IPlayerEvent.LogoutEvent): void;
    function keyPressed(IPlayerEvent: IPlayerEvent.KeyPressedEvent): void;
    function mouseClicked(IPlayerEvent: IPlayerEvent.MouseClickedEvent): void;
    function toss(IPlayerEvent: IPlayerEvent.TossEvent): void;
    function pickUp(IPlayerEvent: IPlayerEvent.PickUpEvent): void;
    function pickupXP(IPlayerEvent: IPlayerEvent.PickupXPEvent): void;
    function rangedCharge(IPlayerEvent: IPlayerEvent.RangedChargeEvent): void;
    function containerOpen(IPlayerEvent: IPlayerEvent.ContainerOpen): void;
    function useHoe(IPlayerEvent: IPlayerEvent.UseHoeEvent): void;
    function bonemeal(IPlayerEvent: IPlayerEvent.BonemealEvent): void;
    function fillBucket(IPlayerEvent: IPlayerEvent.FillBucketEvent): void;
    function jump(IPlayerEvent: IPlayerEvent.JumpEvent): void;
    function fall(IPlayerEvent: IPlayerEvent.FallEvent): void;
    function wakeUp(IPlayerEvent: IPlayerEvent.WakeUpEvent): void;
    function sleep(IPlayerEvent: IPlayerEvent.SleepEvent): void;
    function playSound(IPlayerEvent: IPlayerEvent.SoundEvent): void;
    function lightning(IPlayerEvent: IPlayerEvent.LightningEvent): void;
    function changedDim(IPlayerEvent: IPlayerEvent.ChangedDimension): void;

    // ============ NPC Specific Hooks ============
    function dialog(INpcEvent: INpcEvent.DialogEvent): void;
    function meleeAttack(INpcEvent: INpcEvent.MeleeAttackEvent): void;
    function meleeSwing(INpcEvent: INpcEvent.SwingEvent): void;
    function target(INpcEvent: INpcEvent.TargetEvent): void;
    function collide(INpcEvent: INpcEvent.CollideEvent): void;
    function targetLost(INpcEvent: INpcEvent.TargetLostEvent): void;

    // ============ Item Specific Hooks ============
    function tossed(IItemEvent: IItemEvent.TossedEvent): void;
    function pickedUp(IItemEvent: IItemEvent.PickedUpEvent): void;
    function spawn(IItemEvent: IItemEvent.SpawnEvent): void;

    // ============ Projectile Specific Hooks ============
    function projectileTick(IProjectileEvent: IProjectileEvent.UpdateEvent): void;
    function projectileImpact(IProjectileEvent: IProjectileEvent.ImpactEvent): void;

    // ============ Block Specific Hooks ============
    function redstone(IBlockEvent: IBlockEvent.RedstoneEvent): void;
    function broken(IBlockEvent: IBlockEvent.BreakEvent): void;
    function exploded(IBlockEvent: IBlockEvent.ExplodedEvent): void;
    function rainFilled(IBlockEvent: IBlockEvent.RainFillEvent): void;
    function neighborChanged(IBlockEvent: IBlockEvent.NeighborChangedEvent): void;
    function clicked(IBlockEvent: IBlockEvent.ClickedEvent): void;
    function harvested(IBlockEvent: IBlockEvent.HarvestedEvent): void;
    function collide(IBlockEvent: IBlockEvent.CollidedEvent): void;
    function fallenUpon(IBlockEvent: IBlockEvent.EntityFallenUponEvent): void;

    // ============ Specialized Systems (Quest, Dialog, GUI, etc.) ============
    function questStart(IQuestEvent: IQuestEvent.QuestStartEvent): void;
    function questCompleted(IQuestEvent: IQuestEvent.QuestCompletedEvent): void;
    function questTurnIn(IQuestEvent: IQuestEvent.QuestTurnedInEvent): void;
    function factionPoints(IFactionEvent: IFactionEvent.FactionPointsEvent): void;
    function dialogOpen(IDialogEvent: IDialogEvent.DialogClosed): void;
    function dialogOption(IDialogEvent: IDialogEvent.DialogOption): void;
    function customGuiClosed(ICustomGuiEvent: ICustomGuiEvent.CloseEvent): void;
    function customGuiButton(ICustomGuiEvent: ICustomGuiEvent.ButtonEvent): void;
    function customGuiSlot(ICustomGuiEvent: ICustomGuiEvent.SlotEvent): void;
    function customGuiSlotClicked(ICustomGuiEvent: ICustomGuiEvent.SlotClickEvent): void;
    function customGuiScroll(ICustomGuiEvent: ICustomGuiEvent.ScrollEvent): void;

    // ============ DBC Addon Hooks ============
    function dbcFormChange(IDBCEvent: import('./kamkeel/npcdbc/api/event/IDBCEvent').IDBCEvent.FormChangeEvent): void;
    function dbcDamaged(IDBCEvent: import('./kamkeel/npcdbc/api/event/IDBCEvent').IDBCEvent.DamagedEvent): void;
    function dbcCapsuleUsed(IDBCEvent: import('./kamkeel/npcdbc/api/event/IDBCEvent').IDBCEvent.CapsuleUsedEvent): void;
    function dbcSenzuUsed(IDBCEvent: import('./kamkeel/npcdbc/api/event/IDBCEvent').IDBCEvent.SenzuUsedEvent): void;
    function dbcRevived(IDBCEvent: import('./kamkeel/npcdbc/api/event/IDBCEvent').IDBCEvent.DBCReviveEvent): void;
    function dbcKnockout(IDBCEvent: import('./kamkeel/npcdbc/api/event/IDBCEvent').IDBCEvent.DBCKnockout): void;

    // ============ Forge & Minecraft RAW Hooks ============
    function onCNPCNaturalSpawn(ICustomNPCsEvent: ICustomNPCsEvent.CNPCNaturalSpawnEvent): void;

    function anvilUpdateEvent(ForgeEvent: net.minecraftforge.event.AnvilUpdateEvent): void;
    function tickEventPlayerTickEvent(ForgeEvent: cpw.mods.fml.common.gameevent.TickEvent.PlayerTickEvent): void;
    function tickEventWorldTickEvent(ForgeEvent: cpw.mods.fml.common.gameevent.TickEvent.WorldTickEvent): void;
    function tickEventServerTickEvent(ForgeEvent: cpw.mods.fml.common.gameevent.TickEvent.ServerTickEvent): void;
    function commandEvent(ForgeEvent: net.minecraftforge.event.CommandEvent): void;
    function serverChatEvent(ForgeEvent: net.minecraftforge.event.ServerChatEvent): void;
    function itemTossEvent(ForgeEvent: net.minecraftforge.event.entity.item.ItemTossEvent): void;
    function livingHurtEvent(ForgeEvent: net.minecraftforge.event.entity.living.LivingHurtEvent): void;
    function fillBucketEvent(ForgeEvent: net.minecraftforge.event.entity.player.FillBucketEvent): void;

    function inputEventKeyInputEvent(ForgeEvent: cpw.mods.fml.common.gameevent.InputEvent.KeyInputEvent): void;
    function inputEventMouseInputEvent(ForgeEvent: cpw.mods.fml.common.gameevent.InputEvent.MouseInputEvent): void;
    function playerEventPlayerChangedDimensionEvent(ForgeEvent: cpw.mods.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent): void;
    function playerEventPlayerRespawnEvent(ForgeEvent: cpw.mods.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent): void;
    function playerEventPlayerLoggedOutEvent(ForgeEvent: cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent): void;
    function playerEventPlayerLoggedInEvent(ForgeEvent: cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent): void;
    function playerEventItemSmeltedEvent(ForgeEvent: cpw.mods.fml.common.gameevent.PlayerEvent.ItemSmeltedEvent): void;
    function playerEventItemCraftedEvent(ForgeEvent: cpw.mods.fml.common.gameevent.PlayerEvent.ItemCraftedEvent): void;
    function playerEventItemPickupEvent(ForgeEvent: cpw.mods.fml.common.gameevent.PlayerEvent.ItemPickupEvent): void;
    function fuelBurnTimeEvent(ForgeEvent: net.minecraftforge.event.FuelBurnTimeEvent): void;
    function potionBrewEventPost(ForgeEvent: net.minecraftforge.event.brewing.PotionBrewEvent.Post): void;
    function potionBrewEventPre(ForgeEvent: net.minecraftforge.event.brewing.PotionBrewEvent.Pre): void;
    function potionBrewedEvent(ForgeEvent: net.minecraftforge.event.brewing.PotionBrewedEvent): void;
    function entityEventEnteringChunk(ForgeEvent: net.minecraftforge.event.entity.EntityEvent.EnteringChunk): void;
    function entityEventCanUpdate(ForgeEvent: net.minecraftforge.event.entity.EntityEvent.CanUpdate): void;
    function entityJoinWorldEvent(ForgeEvent: net.minecraftforge.event.entity.EntityJoinWorldEvent): void;
    function entityStruckByLightningEvent(ForgeEvent: net.minecraftforge.event.entity.EntityStruckByLightningEvent): void;
    function playSoundAtEntityEvent(ForgeEvent: net.minecraftforge.event.entity.PlaySoundAtEntityEvent): void;
    function itemEvent(ForgeEvent: net.minecraftforge.event.entity.item.ItemEvent): void;
    function itemExpireEvent(ForgeEvent: net.minecraftforge.event.entity.item.ItemExpireEvent): void;
    function enderTeleportEvent(ForgeEvent: net.minecraftforge.event.entity.living.EnderTeleportEvent): void;
    function livingAttackEvent(ForgeEvent: net.minecraftforge.event.entity.living.LivingAttackEvent): void;
    function livingDeathEvent(ForgeEvent: net.minecraftforge.event.entity.living.LivingDeathEvent): void;
    function livingDropsEvent(ForgeEvent: net.minecraftforge.event.entity.living.LivingDropsEvent): void;
    function livingEventLivingJumpEvent(ForgeEvent: net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent): void;
    function livingFallEvent(ForgeEvent: net.minecraftforge.event.entity.living.LivingFallEvent): void;
    function livingHealEvent(ForgeEvent: net.minecraftforge.event.entity.living.LivingHealEvent): void;
    function livingPackSizeEvent(ForgeEvent: net.minecraftforge.event.entity.living.LivingPackSizeEvent): void;
    function livingSetAttackTargetEvent(ForgeEvent: net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent): void;
    function livingSpawnEventAllowDespawn(ForgeEvent: net.minecraftforge.event.entity.living.LivingSpawnEvent.AllowDespawn): void;
    function livingSpawnEventSpecialSpawn(ForgeEvent: net.minecraftforge.event.entity.living.LivingSpawnEvent.SpecialSpawn): void;
    function livingSpawnEventCheckSpawn(ForgeEvent: net.minecraftforge.event.entity.living.LivingSpawnEvent.CheckSpawn): void;
    function zombieEventSummonAidEvent(ForgeEvent: net.minecraftforge.event.entity.living.ZombieEvent.SummonAidEvent): void;
    function minecartCollisionEvent(ForgeEvent: cpw.mods.fml.common.eventhandler.Event): void;
    function minecartEvent(ForgeEvent: cpw.mods.fml.common.eventhandler.Event): void;
    function minecartInteractEvent(ForgeEvent: cpw.mods.fml.common.eventhandler.Event): void;
    function minecartUpdateEvent(ForgeEvent: cpw.mods.fml.common.eventhandler.Event): void;
    function achievementEvent(ForgeEvent: net.minecraftforge.event.entity.player.AchievementEvent): void;
    function anvilRepairEvent(ForgeEvent: net.minecraftforge.event.entity.player.AnvilRepairEvent): void;
    function arrowLooseEvent(ForgeEvent: net.minecraftforge.event.entity.player.ArrowLooseEvent): void;
    function arrowNockEvent(ForgeEvent: net.minecraftforge.event.entity.player.ArrowNockEvent): void;
    function attackEntityEvent(ForgeEvent: net.minecraftforge.event.entity.player.AttackEntityEvent): void;
    function bonemealEvent(ForgeEvent: net.minecraftforge.event.entity.player.BonemealEvent): void;
    function entityInteractEvent(ForgeEvent: net.minecraftforge.event.entity.player.EntityInteractEvent): void;
    function entityItemPickupEvent(ForgeEvent: net.minecraftforge.event.entity.player.EntityItemPickupEvent): void;
    function playerDestroyItemEvent(ForgeEvent: net.minecraftforge.event.entity.player.PlayerDestroyItemEvent): void;
    function playerDropsEvent(ForgeEvent: net.minecraftforge.event.entity.player.PlayerDropsEvent): void;
    function playerEventSaveToFile(ForgeEvent: net.minecraftforge.event.entity.player.PlayerEvent.SaveToFile): void;
    function playerEventLoadFromFile(ForgeEvent: net.minecraftforge.event.entity.player.PlayerEvent.LoadFromFile): void;
    function playerEventStopTracking(ForgeEvent: net.minecraftforge.event.entity.player.PlayerEvent.StopTracking): void;
    function playerEventStartTracking(ForgeEvent: net.minecraftforge.event.entity.player.PlayerEvent.StartTracking): void;
    function playerEventClone(ForgeEvent: net.minecraftforge.event.entity.player.PlayerEvent.Clone): void;
    function playerEventNameFormat(ForgeEvent: net.minecraftforge.event.entity.player.PlayerEvent.NameFormat): void;
    function playerEventBreakSpeed(ForgeEvent: net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed): void;
    function playerEventHarvestCheck(ForgeEvent: net.minecraftforge.event.entity.player.PlayerEvent.HarvestCheck): void;
    function playerFlyableFallEvent(ForgeEvent: net.minecraftforge.event.entity.player.PlayerFlyableFallEvent): void;
    function playerOpenContainerEvent(ForgeEvent: net.minecraftforge.event.entity.player.PlayerOpenContainerEvent): void;
    function playerPickupXpEvent(ForgeEvent: net.minecraftforge.event.entity.player.PlayerPickupXpEvent): void;
    function playerSleepInBedEvent(ForgeEvent: net.minecraftforge.event.entity.player.PlayerSleepInBedEvent): void;
    function playerUseItemEventFinish(ForgeEvent: net.minecraftforge.event.entity.player.PlayerUseItemEvent.Finish): void;
    function playerUseItemEventStop(ForgeEvent: net.minecraftforge.event.entity.player.PlayerUseItemEvent.Stop): void;
    function playerUseItemEventTick(ForgeEvent: net.minecraftforge.event.entity.player.PlayerUseItemEvent.Tick): void;
    function playerUseItemEventStart(ForgeEvent: net.minecraftforge.event.entity.player.PlayerUseItemEvent.Start): void;
    function playerWakeUpEvent(ForgeEvent: net.minecraftforge.event.entity.player.PlayerWakeUpEvent): void;
    function useHoeEvent(ForgeEvent: net.minecraftforge.event.entity.player.UseHoeEvent): void;
    function blockEventMultiPlaceEvent(ForgeEvent: net.minecraftforge.event.world.BlockEvent.MultiPlaceEvent): void;
    function blockEventPlaceEvent(ForgeEvent: net.minecraftforge.event.world.BlockEvent.PlaceEvent): void;
    function blockEventBreakEvent(ForgeEvent: net.minecraftforge.event.world.BlockEvent.BreakEvent): void;
    function blockEventHarvestDropsEvent(ForgeEvent: net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent): void;
    function explosionEventDetonate(ForgeEvent: net.minecraftforge.event.world.explosion.ExplosionEvent.Detonate): void;
    function explosionEventStart(ForgeEvent: net.minecraftforge.event.world.explosion.ExplosionEvent.Start): void;
    function noteBlockEventChange(ForgeEvent: net.minecraftforge.event.world.note.NoteBlockEvent.Change): void;
    function noteBlockEventPlay(ForgeEvent: net.minecraftforge.event.world.note.NoteBlockEvent.Play): void;
    function worldEventCreateSpawnPosition(ForgeEvent: net.minecraftforge.event.world.WorldEvent.CreateSpawnPosition): void;
    function worldEventSave(ForgeEvent: net.minecraftforge.event.world.WorldEvent.Save): void;
    function worldEventUnload(ForgeEvent: net.minecraftforge.event.world.WorldEvent.Unload): void;
    function worldEventLoad(ForgeEvent: net.minecraftforge.event.world.WorldEvent.Load): void;

    // ============ Other Systems ============
    function scriptCommand(ICustomNPCsEvent: ICustomNPCsEvent.ScriptedCommandEvent): void;

    function partyQuestCompleted(IPartyEvent: IPartyEvent.PartyQuestCompletedEvent): void;
    function partyQuestSet(IPartyEvent: IPartyEvent.PartyQuestSetEvent): void;
    function partyQuestTurnedIn(IPartyEvent: IPartyEvent.PartyQuestTurnedInEvent): void;
    function partyInvite(IPartyEvent: IPartyEvent.PartyInviteEvent): void;
    function partyKick(IPartyEvent: IPartyEvent.PartyKickEvent): void;
    function partyLeave(IPartyEvent: IPartyEvent.PartyLeaveEvent): void;
    function partyDisband(IPartyEvent: IPartyEvent.PartyDisbandEvent): void;

    function animationStart(IAnimationEvent: IAnimationEvent.Started): void;
    function animationEnd(IAnimationEvent: IAnimationEvent.Ended): void;
    function frameEnter(IAnimationEvent: IAnimationEvent.IFrameEvent): void;
    function frameExit(IAnimationEvent: IAnimationEvent.IFrameEvent): void;

    function profileChange(IPlayerEvent: IPlayerEvent.ProfileEvent.Changed): void;
    function profileRemove(IPlayerEvent: IPlayerEvent.ProfileEvent.Removed): void;
    function profileCreate(IPlayerEvent: IPlayerEvent.ProfileEvent.Create): void;
    function onEffectAdd(IPlayerEvent: IPlayerEvent.EffectEvent.Added): void;
    function onEffectTick(IPlayerEvent: IPlayerEvent.EffectEvent.Ticked): void;
    function onEffectRemove(IPlayerEvent: IPlayerEvent.EffectEvent.Removed): void;
}

export { };
