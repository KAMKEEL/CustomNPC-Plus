/**
 * Centralized global declarations for CustomNPC+ scripting.
 * Auto-generated - do not edit manually.
 */

declare global {
    // ============================================================================
    // TYPE ALIASES - Make all interfaces available globally
    // ============================================================================

    type AbstractNpcAPI = import('./noppes/npcs/api/AbstractNpcAPI').AbstractNpcAPI;
    type BlockPos = import('./net/minecraft/util/math/BlockPos').BlockPos;
    type IAbility = import('./noppes/npcs/api/ability/IAbility').IAbility;
    type IAbilityAction = import('./noppes/npcs/api/ability/IAbilityAction').IAbilityAction;
    type IAbilityCharge = import('./noppes/npcs/api/ability/type/IAbilityCharge').IAbilityCharge;
    type IAbilityCounter = import('./noppes/npcs/api/ability/type/IAbilityCounter').IAbilityCounter;
    type IAbilityCutter = import('./noppes/npcs/api/ability/type/IAbilityCutter').IAbilityCutter;
    type IAbilityDash = import('./noppes/npcs/api/ability/type/IAbilityDash').IAbilityDash;
    type IAbilityDefend = import('./noppes/npcs/api/ability/type/IAbilityDefend').IAbilityDefend;
    type IAbilityDisc = import('./noppes/npcs/api/ability/type/IAbilityDisc').IAbilityDisc;
    type IAbilityDodge = import('./noppes/npcs/api/ability/type/IAbilityDodge').IAbilityDodge;
    type IAbilityEffect = import('./noppes/npcs/api/ability/type/IAbilityEffect').IAbilityEffect;
    type IAbilityEnergyBeam = import('./noppes/npcs/api/ability/type/IAbilityEnergyBeam').IAbilityEnergyBeam;
    type IAbilityEnergyProjectile = import('./noppes/npcs/api/ability/type/IAbilityEnergyProjectile').IAbilityEnergyProjectile;
    type IAbilityEvent = import('./noppes/npcs/api/event/IAbilityEvent').IAbilityEvent;
    type IAbilityGuard = import('./noppes/npcs/api/ability/type/IAbilityGuard').IAbilityGuard;
    type IAbilityHandler = import('./noppes/npcs/api/ability/IAbilityHandler').IAbilityHandler;
    type IAbilityHandler = import('./noppes/npcs/api/handler/IAbilityHandler').IAbilityHandler;
    type IAbilityHazard = import('./noppes/npcs/api/ability/type/IAbilityHazard').IAbilityHazard;
    type IAbilityHeavyHit = import('./noppes/npcs/api/ability/type/IAbilityHeavyHit').IAbilityHeavyHit;
    type IAbilityLaser = import('./noppes/npcs/api/ability/type/IAbilityLaser').IAbilityLaser;
    type IAbilityOrb = import('./noppes/npcs/api/ability/type/IAbilityOrb').IAbilityOrb;
    type IAbilityProjectile = import('./noppes/npcs/api/ability/type/IAbilityProjectile').IAbilityProjectile;
    type IAbilityShockwave = import('./noppes/npcs/api/ability/type/IAbilityShockwave').IAbilityShockwave;
    type IAbilitySlam = import('./noppes/npcs/api/ability/type/IAbilitySlam').IAbilitySlam;
    type IAbilitySweeper = import('./noppes/npcs/api/ability/type/IAbilitySweeper').IAbilitySweeper;
    type IAbilityTeleport = import('./noppes/npcs/api/ability/type/IAbilityTeleport').IAbilityTeleport;
    type IAbilityTrap = import('./noppes/npcs/api/ability/type/IAbilityTrap').IAbilityTrap;
    type IAbilityVortex = import('./noppes/npcs/api/ability/type/IAbilityVortex').IAbilityVortex;
    type IAbilityZone = import('./noppes/npcs/api/ability/type/IAbilityZone').IAbilityZone;
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
    type IAuctionClaim = import('./noppes/npcs/api/handler/data/IAuctionClaim').IAuctionClaim;
    type IAuctionEvent = import('./noppes/npcs/api/event/IAuctionEvent').IAuctionEvent;
    type IAuctionHandler = import('./noppes/npcs/api/handler/IAuctionHandler').IAuctionHandler;
    type IAuctionListing = import('./noppes/npcs/api/handler/data/IAuctionListing').IAuctionListing;
    type IAvailability = import('./noppes/npcs/api/handler/data/IAvailability').IAvailability;
    type IBlock = import('./noppes/npcs/api/IBlock').IBlock;
    type IBlockEvent = import('./noppes/npcs/api/event/IBlockEvent').IBlockEvent;
    type IBlockScripted = import('./noppes/npcs/api/block/IBlockScripted').IBlockScripted;
    type IBlockState = import('./net/minecraft/block/state/IBlockState').IBlockState;
    type IButton = import('./noppes/npcs/api/gui/IButton').IButton;
    type IChainEvent = import('./noppes/npcs/api/event/IChainEvent').IChainEvent;
    type IChainedAbility = import('./noppes/npcs/api/ability/IChainedAbility').IChainedAbility;
    type ICloneHandler = import('./noppes/npcs/api/handler/ICloneHandler').ICloneHandler;
    type ICommand = import('./noppes/npcs/api/ICommand').ICommand;
    type IConditionalAction = import('./noppes/npcs/api/handler/data/actions/IConditionalAction').IConditionalAction;
    type IContainer = import('./noppes/npcs/api/IContainer').IContainer;
    type ICustomAbility = import('./noppes/npcs/api/ability/ICustomAbility').ICustomAbility;
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
    type IDataAbilities = import('./noppes/npcs/api/ability/IDataAbilities').IDataAbilities;
    type IDialog = import('./noppes/npcs/api/handler/data/IDialog').IDialog;
    type IDialogCategory = import('./noppes/npcs/api/handler/data/IDialogCategory').IDialogCategory;
    type IDialogEvent = import('./noppes/npcs/api/event/IDialogEvent').IDialogEvent;
    type IDialogHandler = import('./noppes/npcs/api/handler/IDialogHandler').IDialogHandler;
    type IDialogImage = import('./noppes/npcs/api/handler/data/IDialogImage').IDialogImage;
    type IDialogOption = import('./noppes/npcs/api/handler/data/IDialogOption').IDialogOption;
    type IEnergyAbility = import('./noppes/npcs/api/entity/IEnergyAbility').IEnergyAbility;
    type IEnergyAnchorData = import('./noppes/npcs/api/ability/data/IEnergyAnchorData').IEnergyAnchorData;
    type IEnergyBarrier = import('./noppes/npcs/api/entity/IEnergyBarrier').IEnergyBarrier;
    type IEnergyBarrierEvent = import('./noppes/npcs/api/event/IEnergyBarrierEvent').IEnergyBarrierEvent;
    type IEnergyBeam = import('./noppes/npcs/api/entity/IEnergyBeam').IEnergyBeam;
    type IEnergyCombatData = import('./noppes/npcs/api/ability/data/IEnergyCombatData').IEnergyCombatData;
    type IEnergyDisc = import('./noppes/npcs/api/entity/IEnergyDisc').IEnergyDisc;
    type IEnergyDisplayData = import('./noppes/npcs/api/ability/data/IEnergyDisplayData').IEnergyDisplayData;
    type IEnergyDome = import('./noppes/npcs/api/entity/IEnergyDome').IEnergyDome;
    type IEnergyExplosion = import('./noppes/npcs/api/entity/IEnergyExplosion').IEnergyExplosion;
    type IEnergyHandler = import('./noppes/npcs/api/IEnergyHandler').IEnergyHandler;
    type IEnergyHomingData = import('./noppes/npcs/api/ability/data/IEnergyHomingData').IEnergyHomingData;
    type IEnergyLaser = import('./noppes/npcs/api/entity/IEnergyLaser').IEnergyLaser;
    type IEnergyLifespanData = import('./noppes/npcs/api/ability/data/IEnergyLifespanData').IEnergyLifespanData;
    type IEnergyLightningData = import('./noppes/npcs/api/ability/data/IEnergyLightningData').IEnergyLightningData;
    type IEnergyOrb = import('./noppes/npcs/api/entity/IEnergyOrb').IEnergyOrb;
    type IEnergyPanel = import('./noppes/npcs/api/entity/IEnergyPanel').IEnergyPanel;
    type IEnergyProjectile = import('./noppes/npcs/api/entity/IEnergyProjectile').IEnergyProjectile;
    type IEnergyProjectileEvent = import('./noppes/npcs/api/event/IEnergyProjectileEvent').IEnergyProjectileEvent;
    type IEnergySlicer = import('./noppes/npcs/api/entity/IEnergySlicer').IEnergySlicer;
    type IEnergySweeper = import('./noppes/npcs/api/entity/IEnergySweeper').IEnergySweeper;
    type IEnergyZone = import('./noppes/npcs/api/entity/IEnergyZone').IEnergyZone;
    type IEntity = import('./noppes/npcs/api/entity/IEntity').IEntity;
    type IEntityItem = import('./noppes/npcs/api/entity/IEntityItem').IEntityItem;
    type IEntityLiving = import('./noppes/npcs/api/entity/IEntityLiving').IEntityLiving;
    type IEntityLivingBase = import('./noppes/npcs/api/entity/IEntityLivingBase').IEntityLivingBase;
    type IFaction = import('./noppes/npcs/api/handler/data/IFaction').IFaction;
    type IFactionEvent = import('./noppes/npcs/api/event/IFactionEvent').IFactionEvent;
    type IFactionHandler = import('./noppes/npcs/api/handler/IFactionHandler').IFactionHandler;
    type IFishHook = import('./noppes/npcs/api/entity/IFishHook').IFishHook;
    type IForgeEvent = import('./noppes/npcs/api/event/IForgeEvent').IForgeEvent;
    type IFrame = import('./noppes/npcs/api/handler/data/IFrame').IFrame;
    type IFramePart = import('./noppes/npcs/api/handler/data/IFramePart').IFramePart;
    type IHitboxData = import('./noppes/npcs/api/entity/data/IHitboxData').IHitboxData;
    type IHookDefinition = import('./noppes/npcs/api/handler/IHookDefinition').IHookDefinition;
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
    type ILabel = import('./noppes/npcs/api/gui/ILabel').ILabel;
    type ILine = import('./noppes/npcs/api/gui/ILine').ILine;
    type ILine = import('./noppes/npcs/api/handler/data/ILine').ILine;
    type ILines = import('./noppes/npcs/api/handler/data/ILines').ILines;
    type ILinkedItem = import('./noppes/npcs/api/handler/data/ILinkedItem').ILinkedItem;
    type ILinkedItemEvent = import('./noppes/npcs/api/event/ILinkedItemEvent').ILinkedItemEvent;
    type ILinkedItemHandler = import('./noppes/npcs/api/handler/ILinkedItemHandler').ILinkedItemHandler;
    type IMagic = import('./noppes/npcs/api/handler/data/IMagic').IMagic;
    type IMagicCycle = import('./noppes/npcs/api/handler/data/IMagicCycle').IMagicCycle;
    type IMagicData = import('./noppes/npcs/api/handler/data/IMagicData').IMagicData;
    type IMagicHandler = import('./noppes/npcs/api/handler/IMagicHandler').IMagicHandler;
    type IMark = import('./noppes/npcs/api/entity/data/IMark').IMark;
    type IModelData = import('./noppes/npcs/api/entity/data/IModelData').IModelData;
    type IModelRotate = import('./noppes/npcs/api/entity/data/IModelRotate').IModelRotate;
    type IModelRotatePart = import('./noppes/npcs/api/entity/data/IModelRotatePart').IModelRotatePart;
    type IModelScale = import('./noppes/npcs/api/entity/data/IModelScale').IModelScale;
    type IModelScalePart = import('./noppes/npcs/api/entity/data/IModelScalePart').IModelScalePart;
    type IMonster = import('./noppes/npcs/api/entity/IMonster').IMonster;
    type INaturalSpawn = import('./noppes/npcs/api/handler/data/INaturalSpawn').INaturalSpawn;
    type INaturalSpawnsHandler = import('./noppes/npcs/api/handler/INaturalSpawnsHandler').INaturalSpawnsHandler;
    type INbt = import('./noppes/npcs/api/INbt').INbt;
    type INbtList = import('./noppes/npcs/api/INbtList').INbtList;
    type INpcEvent = import('./noppes/npcs/api/event/INpcEvent').INpcEvent;
    type IOverlayHandler = import('./noppes/npcs/api/handler/IOverlayHandler').IOverlayHandler;
    type IOverlayLabel = import('./noppes/npcs/api/overlay/IOverlayLabel').IOverlayLabel;
    type IOverlayLine = import('./noppes/npcs/api/overlay/IOverlayLine').IOverlayLine;
    type IOverlayTexturedRect = import('./noppes/npcs/api/overlay/IOverlayTexturedRect').IOverlayTexturedRect;
    type IParticle = import('./noppes/npcs/api/IParticle').IParticle;
    type IParty = import('./noppes/npcs/api/handler/data/IParty').IParty;
    type IPartyEvent = import('./noppes/npcs/api/event/IPartyEvent').IPartyEvent;
    type IPartyHandler = import('./noppes/npcs/api/handler/IPartyHandler').IPartyHandler;
    type IPartyOptions = import('./noppes/npcs/api/handler/data/IPartyOptions').IPartyOptions;
    type IPixelmon = import('./noppes/npcs/api/entity/IPixelmon').IPixelmon;
    type IPixelmonPlayerData = import('./noppes/npcs/api/IPixelmonPlayerData').IPixelmonPlayerData;
    type IPlayer = import('./noppes/npcs/api/entity/IPlayer').IPlayer;
    type IPlayerAbilityData = import('./noppes/npcs/api/ability/IPlayerAbilityData').IPlayerAbilityData;
    type IPlayerAttributes = import('./noppes/npcs/api/handler/data/IPlayerAttributes').IPlayerAttributes;
    type IPlayerBankData = import('./noppes/npcs/api/handler/IPlayerBankData').IPlayerBankData;
    type IPlayerData = import('./noppes/npcs/api/handler/IPlayerData').IPlayerData;
    type IPlayerDialogData = import('./noppes/npcs/api/handler/IPlayerDialogData').IPlayerDialogData;
    type IPlayerEffect = import('./noppes/npcs/api/handler/data/IPlayerEffect').IPlayerEffect;
    type IPlayerEvent = import('./noppes/npcs/api/event/IPlayerEvent').IPlayerEvent;
    type IPlayerFactionData = import('./noppes/npcs/api/handler/IPlayerFactionData').IPlayerFactionData;
    type IPlayerItemGiverData = import('./noppes/npcs/api/handler/IPlayerItemGiverData').IPlayerItemGiverData;
    type IPlayerMail = import('./noppes/npcs/api/handler/data/IPlayerMail').IPlayerMail;
    type IPlayerMailData = import('./noppes/npcs/api/handler/IPlayerMailData').IPlayerMailData;
    type IPlayerQuestData = import('./noppes/npcs/api/handler/IPlayerQuestData').IPlayerQuestData;
    type IPlayerTradeData = import('./noppes/npcs/api/handler/IPlayerTradeData').IPlayerTradeData;
    type IPlayerTransportData = import('./noppes/npcs/api/handler/IPlayerTransportData').IPlayerTransportData;
    type IPos = import('./noppes/npcs/api/IPos').IPos;
    type IProfile = import('./noppes/npcs/api/handler/data/IProfile').IProfile;
    type IProfileHandler = import('./noppes/npcs/api/handler/IProfileHandler').IProfileHandler;
    type IProfileOptions = import('./noppes/npcs/api/handler/data/IProfileOptions').IProfileOptions;
    type IProjectile = import('./noppes/npcs/api/entity/IProjectile').IProjectile;
    type IProjectileEvent = import('./noppes/npcs/api/event/IProjectileEvent').IProjectileEvent;
    type IProperty = import('./net/minecraft/block/properties/IProperty').IProperty;
    type IQuest = import('./noppes/npcs/api/handler/data/IQuest').IQuest;
    type IQuestCategory = import('./noppes/npcs/api/handler/data/IQuestCategory').IQuestCategory;
    type IQuestDialog = import('./noppes/npcs/api/handler/data/IQuestDialog').IQuestDialog;
    type IQuestEvent = import('./noppes/npcs/api/event/IQuestEvent').IQuestEvent;
    type IQuestHandler = import('./noppes/npcs/api/handler/IQuestHandler').IQuestHandler;
    type IQuestInterface = import('./noppes/npcs/api/handler/data/IQuestInterface').IQuestInterface;
    type IQuestItem = import('./noppes/npcs/api/handler/data/IQuestItem').IQuestItem;
    type IQuestKill = import('./noppes/npcs/api/handler/data/IQuestKill').IQuestKill;
    type IQuestLocation = import('./noppes/npcs/api/handler/data/IQuestLocation').IQuestLocation;
    type IQuestObjective = import('./noppes/npcs/api/handler/data/IQuestObjective').IQuestObjective;
    type IRecipe = import('./noppes/npcs/api/handler/data/IRecipe').IRecipe;
    type IRecipeEvent = import('./noppes/npcs/api/event/IRecipeEvent').IRecipeEvent;
    type IRecipeHandler = import('./noppes/npcs/api/handler/IRecipeHandler').IRecipeHandler;
    type IRole = import('./noppes/npcs/api/roles/IRole').IRole;
    type IRoleBank = import('./noppes/npcs/api/roles/IRoleBank').IRoleBank;
    type IRoleFollower = import('./noppes/npcs/api/roles/IRoleFollower').IRoleFollower;
    type IRoleMailman = import('./noppes/npcs/api/roles/IRoleMailman').IRoleMailman;
    type IRoleTrader = import('./noppes/npcs/api/roles/IRoleTrader').IRoleTrader;
    type IRoleTransporter = import('./noppes/npcs/api/roles/IRoleTransporter').IRoleTransporter;
    type IScoreboard = import('./noppes/npcs/api/scoreboard/IScoreboard').IScoreboard;
    type IScoreboardObjective = import('./noppes/npcs/api/scoreboard/IScoreboardObjective').IScoreboardObjective;
    type IScoreboardTeam = import('./noppes/npcs/api/scoreboard/IScoreboardTeam').IScoreboardTeam;
    type IScreenSize = import('./noppes/npcs/api/IScreenSize').IScreenSize;
    type IScriptHookHandler = import('./noppes/npcs/api/handler/IScriptHookHandler').IScriptHookHandler;
    type IScroll = import('./noppes/npcs/api/gui/IScroll').IScroll;
    type ISkinOverlay = import('./noppes/npcs/api/ISkinOverlay').ISkinOverlay;
    type ISlot = import('./noppes/npcs/api/handler/data/ISlot').ISlot;
    type ISound = import('./noppes/npcs/api/handler/data/ISound').ISound;
    type ITag = import('./noppes/npcs/api/handler/data/ITag').ITag;
    type ITagHandler = import('./noppes/npcs/api/handler/ITagHandler').ITagHandler;
    type ITelegraph = import('./noppes/npcs/api/ITelegraph').ITelegraph;
    type ITelegraphHandler = import('./noppes/npcs/api/handler/ITelegraphHandler').ITelegraphHandler;
    type ITelegraphInstance = import('./noppes/npcs/api/ITelegraphInstance').ITelegraphInstance;
    type ITextField = import('./noppes/npcs/api/gui/ITextField').ITextField;
    type ITextPlane = import('./noppes/npcs/api/block/ITextPlane').ITextPlane;
    type ITexturedRect = import('./noppes/npcs/api/gui/ITexturedRect').ITexturedRect;
    type IThrowable = import('./noppes/npcs/api/entity/IThrowable').IThrowable;
    type ITileEntity = import('./noppes/npcs/api/ITileEntity').ITileEntity;
    type ITimers = import('./noppes/npcs/api/ITimers').ITimers;
    type ITintData = import('./noppes/npcs/api/entity/data/ITintData').ITintData;
    type ITransportCategory = import('./noppes/npcs/api/handler/data/ITransportCategory').ITransportCategory;
    type ITransportHandler = import('./noppes/npcs/api/handler/ITransportHandler').ITransportHandler;
    type ITransportLocation = import('./noppes/npcs/api/handler/data/ITransportLocation').ITransportLocation;
    type IVillager = import('./noppes/npcs/api/entity/IVillager').IVillager;
    type IWorld = import('./noppes/npcs/api/IWorld').IWorld;
    type IntHashMap = import('./net/minecraft/util/math/IntHashMap').IntHashMap;
    type MutableBlockPos = import('./net/minecraft/util/math/BlockPos').MutableBlockPos;
    type NPCEntityHelper = import('./net/minecraft/entity/NPCEntityHelper').NPCEntityHelper;
    type NPCGuiContainerHelper = import('./net/minecraft/client/gui/inventory/NPCGuiContainerHelper').NPCGuiContainerHelper;
    type NPCRendererHelper = import('./net/minecraft/client/renderer/entity/NPCRendererHelper').NPCRendererHelper;
    type NPCResourceHelper = import('./net/minecraft/client/resources/NPCResourceHelper').NPCResourceHelper;
    type PooledMutableBlockPos = import('./net/minecraft/util/math/BlockPos').PooledMutableBlockPos;
    type Vec3i = import('./net/minecraft/util/Vec3i').Vec3i;

    // ============================================================================
    // NESTED INTERFACES - Allow autocomplete like INpcEvent.InitEvent
    // ============================================================================

    namespace BlockPos {
        interface MutableBlockPos extends BlockPos {}
        interface PooledMutableBlockPos extends BlockPos {}
    }

    namespace IAbilityEvent {
        interface CompleteEvent extends IAbilityEvent {}
        interface ExecuteEvent extends IAbilityEvent {}
        interface HitEvent extends IAbilityEvent {}
        interface InterruptEvent extends IAbilityEvent {}
        interface StartEvent extends IAbilityEvent {}
        interface TickEvent extends IAbilityEvent {}
        interface ToggleEvent extends IAbilityEvent {}
        interface ToggleUpdateEvent extends IAbilityEvent {}
    }

    namespace IAnimationEvent {
        interface Ended extends IAnimationEvent {}
        interface IFrameEvent extends IAnimationEvent {}
        interface Started extends IAnimationEvent {}
    }

    namespace IAuctionEvent {
        interface BidEvent extends IAuctionEvent {}
        interface BuyoutEvent extends IAuctionEvent {}
        interface CancelEvent extends IAuctionEvent {}
        interface ClaimEvent extends IAuctionEvent {}
        interface CreateEvent extends IAuctionEvent {}
    }

    namespace IBlockEvent {
        interface BreakEvent extends IBlockEvent {}
        interface ClickedEvent extends IBlockEvent {}
        interface CollidedEvent extends IBlockEvent {}
        interface EntityFallenUponEvent extends IBlockEvent {}
        interface ExplodedEvent extends IBlockEvent {}
        interface HarvestedEvent extends IBlockEvent {}
        interface InitEvent extends IBlockEvent {}
        interface InteractEvent extends IBlockEvent {}
        interface NeighborChangedEvent extends IBlockEvent {}
        interface RainFillEvent extends IBlockEvent {}
        interface RedstoneEvent extends IBlockEvent {}
        interface TimerEvent extends IBlockEvent {}
        interface UpdateEvent extends IBlockEvent {}
    }

    namespace IChainEvent {
        interface CompleteEvent extends IChainEvent {}
        interface InterruptEvent extends IChainEvent {}
        interface NextEvent extends IChainEvent {}
        interface StartEvent extends IChainEvent {}
    }

    namespace ICustomGuiEvent {
        interface ButtonEvent extends ICustomGuiEvent {}
        interface CloseEvent extends ICustomGuiEvent {}
        interface ScrollEvent extends ICustomGuiEvent {}
        interface SlotClickEvent extends ICustomGuiEvent {}
        interface SlotEvent extends ICustomGuiEvent {}
        interface UnfocusedEvent extends ICustomGuiEvent {}
    }

    namespace ICustomNPCsEvent {
        interface CNPCNaturalSpawnEvent extends ICustomNPCsEvent {}
        interface ScriptedCommandEvent extends ICustomNPCsEvent {}
    }

    namespace IDialogEvent {
        interface DialogClosed extends IDialogEvent {}
        interface DialogOpen extends IDialogEvent {}
        interface DialogOption extends IDialogEvent {}
    }

    namespace IEnergyBarrierEvent {
        interface DestroyedEvent extends IEnergyBarrierEvent {}
        interface HitEvent extends IEnergyBarrierEvent {}
        interface SpawnedEvent extends IEnergyBarrierEvent {}
        interface UpdateEvent extends IEnergyBarrierEvent {}
    }

    namespace IEnergyProjectileEvent {
        interface BlockImpactEvent extends IEnergyProjectileEvent {}
        interface EntityImpactEvent extends IEnergyProjectileEvent {}
        interface ExpiredEvent extends IEnergyProjectileEvent {}
        interface FiredEvent extends IEnergyProjectileEvent {}
        interface UpdateEvent extends IEnergyProjectileEvent {}
    }

    namespace IFactionEvent {
        interface FactionPoints extends IFactionEvent {}
    }

    namespace IForgeEvent {
        interface EntityEvent extends IForgeEvent {}
        interface InitEvent extends IForgeEvent {}
        interface WorldEvent extends IForgeEvent {}
    }

    namespace IItemEvent {
        interface AttackEvent extends IItemEvent {}
        interface BreakItem extends IItemEvent {}
        interface FinishUsingItem extends IItemEvent {}
        interface InitEvent extends IItemEvent {}
        interface InteractEvent extends IItemEvent {}
        interface PickedUpEvent extends IItemEvent {}
        interface RepairItem extends IItemEvent {}
        interface RightClickEvent extends IItemEvent {}
        interface SpawnEvent extends IItemEvent {}
        interface StartUsingItem extends IItemEvent {}
        interface StopUsingItem extends IItemEvent {}
        interface TossedEvent extends IItemEvent {}
        interface UpdateEvent extends IItemEvent {}
        interface UsingItem extends IItemEvent {}
    }

    namespace ILinkedItemEvent {
        interface BuildEvent extends ILinkedItemEvent {}
        interface VersionChangeEvent extends ILinkedItemEvent {}
    }

    namespace INpcEvent {
        interface CollideEvent extends INpcEvent {}
        interface DamagedEvent extends INpcEvent {}
        interface DialogClosedEvent extends INpcEvent {}
        interface DialogEvent extends INpcEvent {}
        interface DiedEvent extends INpcEvent {}
        interface InitEvent extends INpcEvent {}
        interface InteractEvent extends INpcEvent {}
        interface KilledEntityEvent extends INpcEvent {}
        interface MeleeAttackEvent extends INpcEvent {}
        interface RangedLaunchedEvent extends INpcEvent {}
        interface SwingEvent extends INpcEvent {}
        interface TargetEvent extends INpcEvent {}
        interface TargetLostEvent extends INpcEvent {}
        interface TimerEvent extends INpcEvent {}
        interface UpdateEvent extends INpcEvent {}
    }

    namespace IPartyEvent {
        interface PartyDisbandEvent extends IPartyEvent {}
        interface PartyInviteEvent extends IPartyEvent {}
        interface PartyKickEvent extends IPartyEvent {}
        interface PartyLeaveEvent extends IPartyEvent {}
        interface PartyQuestCompletedEvent extends IPartyEvent {}
        interface PartyQuestSetEvent extends IPartyEvent {}
        interface PartyQuestTurnedInEvent extends IPartyEvent {}
    }

    namespace IPlayerEvent {
        interface AchievementEvent extends IPlayerEvent {}
        interface AttackEvent extends IPlayerEvent {}
        interface AttackedEvent extends IPlayerEvent {}
        interface BonemealEvent extends IPlayerEvent {}
        interface BreakEvent extends IPlayerEvent {}
        interface ChangedDimension extends IPlayerEvent {}
        interface ChatEvent extends IPlayerEvent {}
        interface ContainerClosed extends IPlayerEvent {}
        interface ContainerOpen extends IPlayerEvent {}
        interface DamagedEntityEvent extends IPlayerEvent {}
        interface DamagedEvent extends IPlayerEvent {}
        interface DiedEvent extends IPlayerEvent {}
        interface DropEvent extends IPlayerEvent {}
        interface EffectEvent extends IPlayerEvent {}
        interface FallEvent extends IPlayerEvent {}
        interface FillBucketEvent extends IPlayerEvent {}
        interface FinishUsingItem extends IPlayerEvent {}
        interface InitEvent extends IPlayerEvent {}
        interface InteractEvent extends IPlayerEvent {}
        interface JumpEvent extends IPlayerEvent {}
        interface KeyPressedEvent extends IPlayerEvent {}
        interface KilledEntityEvent extends IPlayerEvent {}
        interface LevelUpEvent extends IPlayerEvent {}
        interface LightningEvent extends IPlayerEvent {}
        interface LoginEvent extends IPlayerEvent {}
        interface LogoutEvent extends IPlayerEvent {}
        interface MouseClickedEvent extends IPlayerEvent {}
        interface PickUpEvent extends IPlayerEvent {}
        interface PickupXPEvent extends IPlayerEvent {}
        interface ProfileEvent extends IPlayerEvent {}
        interface RangedChargeEvent extends IPlayerEvent {}
        interface RangedLaunchedEvent extends IPlayerEvent {}
        interface RespawnEvent extends IPlayerEvent {}
        interface RightClickEvent extends IPlayerEvent {}
        interface SleepEvent extends IPlayerEvent {}
        interface SoundEvent extends IPlayerEvent {}
        interface StartUsingItem extends IPlayerEvent {}
        interface StopUsingItem extends IPlayerEvent {}
        interface TimerEvent extends IPlayerEvent {}
        interface TossEvent extends IPlayerEvent {}
        interface UpdateEvent extends IPlayerEvent {}
        interface UseHoeEvent extends IPlayerEvent {}
        interface UsingItem extends IPlayerEvent {}
        interface WakeUpEvent extends IPlayerEvent {}
    }

    namespace IProjectileEvent {
        interface ImpactEvent extends IProjectileEvent {}
        interface UpdateEvent extends IProjectileEvent {}
    }

    namespace IQuestEvent {
        interface QuestCompletedEvent extends IQuestEvent {}
        interface QuestStartEvent extends IQuestEvent {}
        interface QuestTurnedInEvent extends IQuestEvent {}
    }

    namespace IRecipeEvent {
        interface Post extends IRecipeEvent {}
        interface Pre extends IRecipeEvent {}
    }

    namespace IntHashMap {
        interface Entry extends IntHashMap {}
    }

}

export {};
