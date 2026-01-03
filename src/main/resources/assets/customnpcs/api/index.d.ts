/**
 * Centralized global declarations for CustomNPC+ scripting in VSCode.
 * 
 * This file combines:
 * 1. Type aliases for all generated interfaces (so you can use INpcEvent, IPlayer, etc. without imports)
 * 2. Ambient function declarations for handler names (so VSCode infers the event type by function name)
 * 
 * Include this file in jsconfig.json so all .js scripts see these globals without per-file imports.
 */

declare global {
    // ============================================================================
    // TYPE ALIASES - Make all interfaces available globally in JS scripts
    // ============================================================================
    
    type AbstractNpcAPI = import('./noppes/npcs/api/AbstractNpcAPI').AbstractNpcAPI;
    type BlockPos = import('./net/minecraft/util/math/BlockPos').BlockPos;
    type IAction = import('./noppes/npcs/api/handler/data/IAction').IAction;
    type IActionChain = import('./noppes/npcs/api/handler/data/IActionChain').IActionChain;
    type IActionListener = import('./noppes/npcs/api/handler/data/IActionListener').IActionListener;
    type IActionManager = import('./noppes/npcs/api/handler/IActionManager').IActionManager;
    type IActionQueue = import('./noppes/npcs/api/handler/data/IActionQueue').IActionQueue;
    type IAnimal = import('./noppes/npcs/api/entity/IAnimal').IAnimal;
    type IAnimatable = import('./noppes/npcs/api/entity/IAnimatable').IAnimatable;
    type IAnimation = import('./noppes/npcs/api/handler/data/IAnimation').IAnimation;
    type IAnimationData = import('./noppes/npcs/api/handler/data/IAnimationData').IAnimationData;
    type IAnimationEvent = import('./noppes/npcs/api/event/IAnimationEvent').IAnimationEvent;
    type IAnimationHandler = import('./noppes/npcs/api/handler/IAnimationHandler').IAnimationHandler;
    type IAnvilRecipe = import('./noppes/npcs/api/handler/data/IAnvilRecipe').IAnvilRecipe;
    type IArrow = import('./noppes/npcs/api/entity/IArrow').IArrow;
    type IAttributeDefinition = import('./noppes/npcs/api/handler/data/IAttributeDefinition').IAttributeDefinition;
    type IAttributeHandler = import('./noppes/npcs/api/handler/IAttributeHandler').IAttributeHandler;
    type IAvailability = import('./noppes/npcs/api/handler/data/IAvailability').IAvailability;
    type IBlock = import('./noppes/npcs/api/IBlock').IBlock;
    type IBlockEvent = import('./noppes/npcs/api/event/IBlockEvent').IBlockEvent;
    type IBlockScripted = import('./noppes/npcs/api/block/IBlockScripted').IBlockScripted;
    type IBlockState = import('./net/minecraft/block/state/IBlockState').IBlockState;
    type IButton = import('./noppes/npcs/api/gui/IButton').IButton;
    type ICloneHandler = import('./noppes/npcs/api/handler/ICloneHandler').ICloneHandler;
    type ICommand = import('./noppes/npcs/api/ICommand').ICommand;
    type IConditionalAction = import('./noppes/npcs/api/handler/data/actions/IConditionalAction').IConditionalAction;
    type IContainer = import('./noppes/npcs/api/IContainer').IContainer;
    type ICustomAttribute = import('./noppes/npcs/api/handler/data/ICustomAttribute').ICustomAttribute;
    type ICustomEffect = import('./noppes/npcs/api/handler/data/ICustomEffect').ICustomEffect;
    type ICustomEffectHandler = import('./noppes/npcs/api/handler/ICustomEffectHandler').ICustomEffectHandler;
    type ICustomGui = import('./noppes/npcs/api/gui/ICustomGui').ICustomGui;
    type ICustomGuiComponent = import('./noppes/npcs/api/gui/ICustomGuiComponent').ICustomGuiComponent;
    type ICustomGuiEvent = import('./noppes/npcs/api/event/ICustomGuiEvent').ICustomGuiEvent;
    type ICustomNPCsEvent = import('./noppes/npcs/api/event/ICustomNPCsEvent').ICustomNPCsEvent;
    type ICustomNpc = import('./noppes/npcs/api/entity/ICustomNpc').ICustomNpc;
    type ICustomOverlay = import('./noppes/npcs/api/overlay/ICustomOverlay').ICustomOverlay;
    type ICustomOverlayComponent = import('./noppes/npcs/api/overlay/ICustomOverlayComponent').ICustomOverlayComponent;
    type IDBCPlayer = import('./noppes/npcs/api/entity/IDBCPlayer').IDBCPlayer;
    type IDamageSource = import('./noppes/npcs/api/IDamageSource').IDamageSource;
    type IDialog = import('./noppes/npcs/api/handler/data/IDialog').IDialog;
    type IDialogCategory = import('./noppes/npcs/api/handler/data/IDialogCategory').IDialogCategory;
    type IDialogEvent = import('./noppes/npcs/api/event/IDialogEvent').IDialogEvent;
    type IDialogHandler = import('./noppes/npcs/api/handler/IDialogHandler').IDialogHandler;
    type IDialogImage = import('./noppes/npcs/api/handler/data/IDialogImage').IDialogImage;
    type IDialogOption = import('./noppes/npcs/api/handler/data/IDialogOption').IDialogOption;
    type IEntity = import('./noppes/npcs/api/entity/IEntity').IEntity;
    type IEntityItem = import('./noppes/npcs/api/entity/IEntityItem').IEntityItem;
    type IEntityLiving = import('./noppes/npcs/api/entity/IEntityLiving').IEntityLiving;
    type IFaction = import('./noppes/npcs/api/handler/data/IFaction').IFaction;
    type IFactionEvent = import('./noppes/npcs/api/event/IFactionEvent').IFactionEvent;
    type IFactionHandler = import('./noppes/npcs/api/handler/IFactionHandler').IFactionHandler;
    type IFishHook = import('./noppes/npcs/api/entity/IFishHook').IFishHook;
    type IForgeEvent = import('./noppes/npcs/api/event/IForgeEvent').IForgeEvent;
    type IFrame = import('./noppes/npcs/api/handler/data/IFrame').IFrame;
    type IFramePart = import('./noppes/npcs/api/handler/data/IFramePart').IFramePart;
    type IItemArmor = import('./noppes/npcs/api/item/IItemArmor').IItemArmor;
    type IItemBlock = import('./noppes/npcs/api/item/IItemBlock').IItemBlock;
    type IItemBook = import('./noppes/npcs/api/item/IItemBook').IItemBook;
    type IItemCustom = import('./noppes/npcs/api/item/IItemCustom').IItemCustom;
    type IItemCustomizable = import('./noppes/npcs/api/item/IItemCustomizable').IItemCustomizable;
    type IItemEvent = import('./noppes/npcs/api/event/IItemEvent').IItemEvent;
    type IItemLinked = import('./noppes/npcs/api/item/IItemLinked').IItemLinked;
    type IItemSlot = import('./noppes/npcs/api/gui/IItemSlot').IItemSlot;
    type IItemStack = import('./noppes/npcs/api/item/IItemStack').IItemStack;
    type IJob = import('./noppes/npcs/api/jobs/IJob').IJob;
    type IJobBard = import('./noppes/npcs/api/jobs/IJobBard').IJobBard;
    type IJobConversation = import('./noppes/npcs/api/jobs/IJobConversation').IJobConversation;
    type IJobFollower = import('./noppes/npcs/api/jobs/IJobFollower').IJobFollower;
    type IJobGuard = import('./noppes/npcs/api/jobs/IJobGuard').IJobGuard;
    type IJobHealer = import('./noppes/npcs/api/jobs/IJobHealer').IJobHealer;
    type IJobItemGiver = import('./noppes/npcs/api/jobs/IJobItemGiver').IJobItemGiver;
    type IJobSpawner = import('./noppes/npcs/api/jobs/IJobSpawner').IJobSpawner;
    type ILinkedItemEvent = import('./noppes/npcs/api/event/ILinkedItemEvent').ILinkedItemEvent;
    type IMarketCategory = import('./noppes/npcs/api/handler/data/IMarketCategory').IMarketCategory;
    type IMessage = import('./noppes/npcs/api/handler/data/IMessage').IMessage;
    type IModel = import('./noppes/npcs/api/handler/data/IModel').IModel;
    type IModelData = import('./noppes/npcs/api/handler/data/IModelData').IModelData;
    type IModelPart = import('./noppes/npcs/api/handler/data/IModelPart').IModelPart;
    type IModelRotate = import('./noppes/npcs/api/handler/data/IModelRotate').IModelRotate;
    type IModelRotatePart = import('./noppes/npcs/api/handler/data/IModelRotatePart').IModelRotatePart;
    type IMonster = import('./noppes/npcs/api/entity/IMonster').IMonster;
    type INbtTagCompound = import('./noppes/npcs/api/INbt').INbtTagCompound;
    type INpcEvent = import('./noppes/npcs/api/event/INpcEvent').INpcEvent;
    type IParallel = import('./noppes/npcs/api/handler/data/IParallel').IParallel;
    type IParallelLine = import('./noppes/npcs/api/handler/data/IParallelLine').IParallelLine;
    type IPartyEvent = import('./noppes/npcs/api/event/IPartyEvent').IPartyEvent;
    type IPartyHandler = import('./noppes/npcs/api/handler/IPartyHandler').IPartyHandler;
    type IPayloadCompound = import('./noppes/npcs/api/IPayloadCompound').IPayloadCompound;
    type IPixelmon = import('./noppes/npcs/api/entity/IPixelmon').IPixelmon;
    type IPixelmonPlayerData = import('./noppes/npcs/api/IPixelmonPlayerData').IPixelmonPlayerData;
    type IPlayer = import('./noppes/npcs/api/entity/IPlayer').IPlayer;
    type IPlayerEvent = import('./noppes/npcs/api/event/IPlayerEvent').IPlayerEvent;
    type IPos = import('./noppes/npcs/api/IPos').IPos;
    type IProjectile = import('./noppes/npcs/api/entity/IProjectile').IProjectile;
    type IProjectileEvent = import('./noppes/npcs/api/event/IProjectileEvent').IProjectileEvent;
    type IQuestEvent = import('./noppes/npcs/api/event/IQuestEvent').IQuestEvent;
    type IQuestHandler = import('./noppes/npcs/api/handler/IQuestHandler').IQuestHandler;
    type IRecipeEvent = import('./noppes/npcs/api/event/IRecipeEvent').IRecipeEvent;
    type IRole = import('./noppes/npcs/api/roles/IRole').IRole;
    type IRoleArmor = import('./noppes/npcs/api/roles/IRoleArmor').IRoleArmor;
    type IRoleDye = import('./noppes/npcs/api/roles/IRoleDye').IRoleDye;
    type IRoleHair = import('./noppes/npcs/api/roles/IRoleHair').IRoleHair;
    type IRoleHat = import('./noppes/npcs/api/roles/IRoleHat').IRoleHat;
    type IRoleHeld = import('./noppes/npcs/api/roles/IRoleHeld').IRoleHeld;
    type IRoleLeft = import('./noppes/npcs/api/roles/IRoleLeft').IRoleLeft;
    type IRoleRight = import('./noppes/npcs/api/roles/IRoleRight').IRoleRight;
    type IRoleSkinPart = import('./noppes/npcs/api/roles/IRoleSkinPart').IRoleSkinPart;
    type IRoleSmile = import('./noppes/npcs/api/roles/IRoleSmile').IRoleSmile;
    type IScoreboard = import('./noppes/npcs/api/scoreboard/IScoreboard').IScoreboard;
    type IScoreboardFont = import('./noppes/npcs/api/scoreboard/IScoreboardFont').IScoreboardFont;
    type IScreenSize = import('./noppes/npcs/api/IScreenSize').IScreenSize;
    type ISerializable = import('./noppes/npcs/api/ISerializable').ISerializable;
    type IShapedRecipe = import('./noppes/npcs/api/IShapedRecipe').IShapedRecipe;
    type IShapelessRecipe = import('./noppes/npcs/api/IShapelessRecipe').IShapelessRecipe;
    type IShop = import('./noppes/npcs/api/handler/data/IShop').IShop;
    type IShopCategory = import('./noppes/npcs/api/handler/data/IShopCategory').IShopCategory;
    type IShopItem = import('./noppes/npcs/api/handler/data/IShopItem').IShopItem;
    type ISkinOverlay = import('./noppes/npcs/api/ISkinOverlay').ISkinOverlay;
    type ISlot = import('./noppes/npcs/api/handler/data/ISlot').ISlot;
    type ISound = import('./noppes/npcs/api/handler/data/ISound').ISound;
    type ITag = import('./noppes/npcs/api/handler/data/ITag').ITag;
    type ITagHandler = import('./noppes/npcs/api/handler/ITagHandler').ITagHandler;
    type ITextField = import('./noppes/npcs/api/gui/ITextField').ITextField;
    type ITextPlane = import('./noppes/npcs/api/block/ITextPlane').ITextPlane;
    type ITexturedRect = import('./noppes/npcs/api/gui/ITexturedRect').ITexturedRect;
    type IThrowable = import('./noppes/npcs/api/entity/IThrowable').IThrowable;
    type ITileEntity = import('./noppes/npcs/api/ITileEntity').ITileEntity;
    type ITimers = import('./noppes/npcs/api/ITimers').ITimers;
    type ITransportCategory = import('./noppes/npcs/api/handler/data/ITransportCategory').ITransportCategory;
    type ITransportHandler = import('./noppes/npcs/api/handler/ITransportHandler').ITransportHandler;
    type ITransportLocation = import('./noppes/npcs/api/handler/data/ITransportLocation').ITransportLocation;
    type IVillager = import('./noppes/npcs/api/entity/IVillager').IVillager;
    type IWorld = import('./noppes/npcs/api/IWorld').IWorld;
    type IntHashMap = import('./net/minecraft/util/math/IntHashMap').IntHashMap;
    type Vec3i = import('./net/minecraft/util/Vec3i').Vec3i;

    // ============================================================================
    // GLOBAL API INSTANCE
    // ============================================================================
    
    const API: import('./noppes/npcs/api/AbstractNpcAPI').AbstractNpcAPI;

    // ============================================================================
    // NESTED INTERFACES - Allow autocomplete like INpcEvent.InitEvent
    // ============================================================================
    
    namespace INpcEvent {
        interface CollideEvent extends INpcEvent {}
        interface DamagedEvent extends INpcEvent {}
        interface RangedLaunchedEvent extends INpcEvent {}
        interface MeleeAttackEvent extends INpcEvent {}
        interface SwingEvent extends INpcEvent {}
        interface KilledEntityEvent extends INpcEvent {}
        interface DiedEvent extends INpcEvent {}
        interface InteractEvent extends INpcEvent {}
        interface DialogEvent extends INpcEvent {}
        interface TimerEvent extends INpcEvent {}
        interface TargetEvent extends INpcEvent {}
        interface TargetLostEvent extends INpcEvent {}
        interface DialogClosedEvent extends INpcEvent {}
        interface UpdateEvent extends INpcEvent {}
        interface InitEvent extends INpcEvent {}
    }

    namespace IPlayerEvent {
        interface ChatEvent extends IPlayerEvent {}
        interface KeyPressedEvent extends IPlayerEvent {}
        interface MouseClickedEvent extends IPlayerEvent {}
        interface PickupXPEvent extends IPlayerEvent {}
        interface LevelUpEvent extends IPlayerEvent {}
        interface LogoutEvent extends IPlayerEvent {}
        interface LoginEvent extends IPlayerEvent {}
        interface RespawnEvent extends IPlayerEvent {}
        interface ChangedDimension extends IPlayerEvent {}
        interface TimerEvent extends IPlayerEvent {}
        interface AttackedEvent extends IPlayerEvent {}
        interface DamagedEvent extends IPlayerEvent {}
        interface LightningEvent extends IPlayerEvent {}
        interface SoundEvent extends IPlayerEvent {}
        interface FallEvent extends IPlayerEvent {}
        interface JumpEvent extends IPlayerEvent {}
        interface KilledEntityEvent extends IPlayerEvent {}
        interface DiedEvent extends IPlayerEvent {}
        interface RangedLaunchedEvent extends IPlayerEvent {}
        interface AttackEvent extends IPlayerEvent {}
        interface DamagedEntityEvent extends IPlayerEvent {}
        interface ContainerClosed extends IPlayerEvent {}
        interface ContainerOpen extends IPlayerEvent {}
        interface PickUpEvent extends IPlayerEvent {}
        interface DropEvent extends IPlayerEvent {}
        interface TossEvent extends IPlayerEvent {}
        interface InteractEvent extends IPlayerEvent {}
        interface RightClickEvent extends IPlayerEvent {}
        interface UpdateEvent extends IPlayerEvent {}
        interface InitEvent extends IPlayerEvent {}
        interface StartUsingItem extends IPlayerEvent {}
        interface UsingItem extends IPlayerEvent {}
        interface StopUsingItem extends IPlayerEvent {}
        interface FinishUsingItem extends IPlayerEvent {}
        interface BreakEvent extends IPlayerEvent {}
        interface UseHoeEvent extends IPlayerEvent {}
        interface WakeUpEvent extends IPlayerEvent {}
        interface SleepEvent extends IPlayerEvent {}
        interface AchievementEvent extends IPlayerEvent {}
        interface FillBucketEvent extends IPlayerEvent {}
        interface BonemealEvent extends IPlayerEvent {}
        interface RangedChargeEvent extends IPlayerEvent {}
        interface EffectEvent extends IPlayerEvent {}
        interface ProfileEvent extends IPlayerEvent {}
    }

    namespace IProjectileEvent {
        interface UpdateEvent extends IProjectileEvent {}
        interface ImpactEvent extends IProjectileEvent {}
    }

    namespace IQuestEvent {
        interface QuestStartEvent extends IQuestEvent {}
        interface QuestCompletedEvent extends IQuestEvent {}
        interface QuestTurnedInEvent extends IQuestEvent {}
    }

    namespace IDialogEvent {
        interface DialogClosed extends IDialogEvent {}
    }

    namespace IFactionEvent {
        interface FactionPointsEvent extends IFactionEvent {}
    }

    namespace IPartyEvent {
        interface PartyQuestCompletedEvent extends IPartyEvent {}
        interface PartyQuestSetEvent extends IPartyEvent {}
        interface PartyQuestTurnedInEvent extends IPartyEvent {}
        interface PartyInviteEvent extends IPartyEvent {}
        interface PartyKickEvent extends IPartyEvent {}
        interface PartyLeaveEvent extends IPartyEvent {}
        interface PartyDisbandEvent extends IPartyEvent {}
    }

    namespace IBlockEvent {
        interface InitEvent extends IBlockEvent {}
        interface InteractEvent extends IBlockEvent {}
        interface UpdateEvent extends IBlockEvent {}
        interface TimerEvent extends IBlockEvent {}
    }

    namespace ICustomGuiEvent {
        interface CloseEvent extends ICustomGuiEvent {}
        interface ButtonEvent extends ICustomGuiEvent {}
        interface SlotEvent extends ICustomGuiEvent {}
        interface SlotClickEvent extends ICustomGuiEvent {}
        interface ScrollEvent extends ICustomGuiEvent {}
    }

    namespace IAnimationEvent {
        interface Started extends IAnimationEvent {}
        interface Ended extends IAnimationEvent {}
        interface IFrameEvent extends IAnimationEvent {}
    }

    namespace IItemEvent {
        interface InitEvent extends IItemEvent {}
        interface UpdateEvent extends IItemEvent {}
        interface TossedEvent extends IItemEvent {}
        interface PickedUpEvent extends IItemEvent {}
        interface SpawnEvent extends IItemEvent {}
        interface InteractEvent extends IItemEvent {}
        interface RightClickEvent extends IItemEvent {}
        interface AttackEvent extends IItemEvent {}
        interface StartUsingItem extends IItemEvent {}
        interface UsingItem extends IItemEvent {}
        interface StopUsingItem extends IItemEvent {}
        interface FinishUsingItem extends IItemEvent {}
    }

    // ============================================================================
    // AMBIENT HANDLER FUNCTIONS - Defined in hooks.d.ts
    // ============================================================================
    // For type-safe handlers with overloads (parameter names determine event type),
    // see hooks.d.ts which includes declare function overloads.
    // 
    // Example:
    //   function damaged(INpcEvent: INpcEvent.DamagedEvent) { INpcEvent. }  // infers INpcEvent.DamagedEvent
    //   function damaged(IPlayerEvent: IPlayerEvent.DamagedEvent) { IPlayerEvent. }  // infers IPlayerEvent.DamagedEvent
}

export {};
