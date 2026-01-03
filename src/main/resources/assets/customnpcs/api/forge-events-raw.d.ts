/**
 * Specialized Raw Forge 1.7.10 Event Definitions
 * Deep package hierarchy to match Java source (Forge/MCP).
 */

declare global {
    namespace cpw.mods.fml.common.eventhandler {
        /** cpw.mods.fml.common.eventhandler.Event */
        export interface Event {
            /** @returns If this event can be canceled */
            isCancelable(): boolean;
            /** @returns If this event has been canceled */
            isCanceled(): boolean;
            /** Sets the canceled state */
            setCanceled(cancel: boolean): void;
            /** @returns The current result (ALLOW, DENY, DEFAULT) */
            getResult(): 'ALLOW' | 'DENY' | 'DEFAULT';
            /** Sets the result */
            setResult(result: 'ALLOW' | 'DENY' | 'DEFAULT'): void;
            /** @returns If this event has a result */
            hasResult(): boolean;
        }
    }

    namespace cpw.mods.fml.common.gameevent {
        /** cpw.mods.fml.common.gameevent.TickEvent */
        export interface TickEvent extends cpw.mods.fml.common.eventhandler.Event {
            type: 'PLAYER' | 'WORLD' | 'SERVER' | 'CLIENT' | 'RENDER';
            side: 'CLIENT' | 'SERVER';
            phase: 'START' | 'END';
        }

        export namespace TickEvent {
            /** cpw.mods.fml.common.gameevent.TickEvent.PlayerTickEvent */
            export interface PlayerTickEvent extends cpw.mods.fml.common.gameevent.TickEvent {
                player: net.minecraft.entity.player.EntityPlayer;
            }
            /** cpw.mods.fml.common.gameevent.TickEvent.WorldTickEvent */
            export interface WorldTickEvent extends cpw.mods.fml.common.gameevent.TickEvent {
                world: net.minecraft.world.World;
            }
            /** cpw.mods.fml.common.gameevent.TickEvent.ServerTickEvent */
            export type ServerTickEvent = cpw.mods.fml.common.gameevent.TickEvent
            /** cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent */
            export type ClientTickEvent = cpw.mods.fml.common.gameevent.TickEvent
            /** cpw.mods.fml.common.gameevent.TickEvent.RenderTickEvent */
            export interface RenderTickEvent extends cpw.mods.fml.common.gameevent.TickEvent {
                renderTickTime: number;
            }
        }

        /** cpw.mods.fml.common.gameevent.InputEvent */
        export type InputEvent = cpw.mods.fml.common.eventhandler.Event
        export namespace InputEvent {
            /** cpw.mods.fml.common.gameevent.InputEvent.KeyInputEvent */
            export type KeyInputEvent = InputEvent
            /** cpw.mods.fml.common.gameevent.InputEvent.MouseInputEvent */
            export type MouseInputEvent = InputEvent
        }

        /** cpw.mods.fml.common.gameevent.PlayerEvent */
        export interface PlayerEvent extends cpw.mods.fml.common.eventhandler.Event {
            player: net.minecraft.entity.player.EntityPlayer;
        }
        export namespace PlayerEvent {
            /** cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent */
            export type PlayerLoggedInEvent = cpw.mods.fml.common.gameevent.PlayerEvent
            /** cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent */
            export type PlayerLoggedOutEvent = cpw.mods.fml.common.gameevent.PlayerEvent
            /** cpw.mods.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent */
            export type PlayerRespawnEvent = cpw.mods.fml.common.gameevent.PlayerEvent
            /** cpw.mods.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent */
            export interface PlayerChangedDimensionEvent extends cpw.mods.fml.common.gameevent.PlayerEvent {
                fromDim: number;
                toDim: number;
            }
            /** cpw.mods.fml.common.gameevent.PlayerEvent.ItemPickupEvent */
            export interface ItemPickupEvent extends cpw.mods.fml.common.gameevent.PlayerEvent {
                pickedUp: net.minecraft.entity.EntityItem;
            }
            /** cpw.mods.fml.common.gameevent.PlayerEvent.ItemCraftedEvent */
            export interface ItemCraftedEvent extends cpw.mods.fml.common.gameevent.PlayerEvent {
                craftMatrix: any;
                crafting: net.minecraft.item.ItemStack;
            }
            /** cpw.mods.fml.common.gameevent.PlayerEvent.ItemSmeltedEvent */
            export interface ItemSmeltedEvent extends cpw.mods.fml.common.gameevent.PlayerEvent {
                smelting: net.minecraft.item.ItemStack;
            }
        }
    }

    namespace net.minecraftforge.event {
        /** net.minecraftforge.event.AnvilUpdateEvent */
        export interface AnvilUpdateEvent extends cpw.mods.fml.common.eventhandler.Event {
            left: net.minecraft.item.ItemStack;
            right: net.minecraft.item.ItemStack;
            name: string;
            output: net.minecraft.item.ItemStack;
            cost: number;
            materialCost: number;
        }

        /** net.minecraftforge.event.CommandEvent */
        export interface CommandEvent extends cpw.mods.fml.common.eventhandler.Event {
            command: any;
            sender: any;
            parameters: string[];
            exception: any;
        }

        /** net.minecraftforge.event.ServerChatEvent */
        export interface ServerChatEvent extends cpw.mods.fml.common.eventhandler.Event {
            message: string;
            username: string;
            player: net.minecraft.entity.player.EntityPlayer;
            component: any;
        }

        /** net.minecraftforge.event.FuelBurnTimeEvent */
        export interface FuelBurnTimeEvent extends cpw.mods.fml.common.eventhandler.Event {
            fuel: net.minecraft.item.ItemStack;
            burnTime: number;
        }

        export namespace brewing {
            /** net.minecraftforge.event.brewing.PotionBrewEvent */
            export interface PotionBrewEvent extends cpw.mods.fml.common.eventhandler.Event {
                getItem(index: number): net.minecraft.item.ItemStack;
                setItem(index: number, stack: net.minecraft.item.ItemStack): void;
                getLength(): number;
            }
            export namespace PotionBrewEvent {
                export type Pre = PotionBrewEvent
                export type Post = PotionBrewEvent
            }
            /** net.minecraftforge.event.brewing.PotionBrewedEvent */
            export interface PotionBrewedEvent extends cpw.mods.fml.common.eventhandler.Event {
                brewingStacks: net.minecraft.item.ItemStack[];
            }
        }

        export namespace entity {
            /** net.minecraftforge.event.entity.EntityEvent */
            export interface EntityEvent extends cpw.mods.fml.common.eventhandler.Event {
                entity: net.minecraft.entity.Entity;
            }
            export namespace EntityEvent {
                /** net.minecraftforge.event.entity.EntityEvent.EntityConstructing */
                export type EntityConstructing = net.minecraftforge.event.entity.EntityEvent
                /** net.minecraftforge.event.entity.EntityEvent.CanUpdate */
                export interface CanUpdate extends net.minecraftforge.event.entity.EntityEvent {
                    canUpdate: boolean;
                }
                /** net.minecraftforge.event.entity.EntityEvent.EnteringChunk */
                export interface EnteringChunk extends net.minecraftforge.event.entity.EntityEvent {
                    newChunkX: number;
                    newChunkZ: number;
                    oldChunkX: number;
                    oldChunkZ: number;
                }
            }

            /** net.minecraftforge.event.entity.EntityJoinWorldEvent */
            export interface EntityJoinWorldEvent extends net.minecraftforge.event.entity.EntityEvent {
                world: net.minecraft.world.World;
            }

            /** net.minecraftforge.event.entity.EntityStruckByLightningEvent */
            export interface EntityStruckByLightningEvent extends net.minecraftforge.event.entity.EntityEvent {
                lightning: net.minecraft.entity.Entity;
            }

            /** net.minecraftforge.event.entity.PlaySoundAtEntityEvent */
            export interface PlaySoundAtEntityEvent extends net.minecraftforge.event.entity.EntityEvent {
                name: string;
                volume: number;
                pitch: number;
            }

            export namespace item {
                /** net.minecraftforge.event.entity.item.ItemEvent */
                export interface ItemEvent extends net.minecraftforge.event.entity.EntityEvent {
                    entityItem: net.minecraft.entity.EntityItem;
                }
                /** net.minecraftforge.event.entity.item.ItemExpireEvent */
                export interface ItemExpireEvent extends net.minecraftforge.event.entity.item.ItemEvent {
                    extraLife: number;
                }
                /** net.minecraftforge.event.entity.item.ItemTossEvent */
                export interface ItemTossEvent extends net.minecraftforge.event.entity.item.ItemEvent {
                    player: net.minecraft.entity.player.EntityPlayer;
                }
            }

            export namespace living {
                /** net.minecraftforge.event.entity.living.LivingEvent */
                export interface LivingEvent extends net.minecraftforge.event.entity.EntityEvent {
                    entityLiving: net.minecraft.entity.EntityLivingBase;
                }
                export namespace LivingEvent {
                    /** net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent */
                    export type LivingUpdateEvent = net.minecraftforge.event.entity.living.LivingEvent
                    /** net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent */
                    export type LivingJumpEvent = net.minecraftforge.event.entity.living.LivingEvent
                }

                /** net.minecraftforge.event.entity.living.LivingAttackEvent */
                export interface LivingAttackEvent extends net.minecraftforge.event.entity.living.LivingEvent {
                    source: net.minecraft.damage.DamageSource;
                    ammount: number;
                }

                /** net.minecraftforge.event.entity.living.LivingDeathEvent */
                export interface LivingDeathEvent extends net.minecraftforge.event.entity.living.LivingEvent {
                    source: net.minecraft.damage.DamageSource;
                }

                /** net.minecraftforge.event.entity.living.LivingDropsEvent */
                export interface LivingDropsEvent extends net.minecraftforge.event.entity.living.LivingEvent {
                    source: net.minecraft.damage.DamageSource;
                    drops: any[];
                    lootingLevel: number;
                    recentlyHit: boolean;
                    specialDropValue: number;
                }

                /** net.minecraftforge.event.entity.living.LivingFallEvent */
                export interface LivingFallEvent extends net.minecraftforge.event.entity.living.LivingEvent {
                    distance: number;
                }

                /** net.minecraftforge.event.entity.living.LivingHealEvent */
                export interface LivingHealEvent extends net.minecraftforge.event.entity.living.LivingEvent {
                    amount: number;
                }

                /** net.minecraftforge.event.entity.living.LivingHurtEvent */
                export interface LivingHurtEvent extends net.minecraftforge.event.entity.living.LivingEvent {
                    source: net.minecraft.damage.DamageSource;
                    ammount: number;
                }

                /** net.minecraftforge.event.entity.living.LivingPackSizeEvent */
                export interface LivingPackSizeEvent extends net.minecraftforge.event.entity.living.LivingEvent {
                    maxPackSize: number;
                }

                /** net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent */
                export interface LivingSetAttackTargetEvent extends net.minecraftforge.event.entity.living.LivingEvent {
                    target: net.minecraft.entity.EntityLivingBase;
                }

                /** net.minecraftforge.event.entity.living.EnderTeleportEvent */
                export interface EnderTeleportEvent extends net.minecraftforge.event.entity.living.LivingEvent {
                    targetX: number;
                    targetY: number;
                    targetZ: number;
                    attackDamage: number;
                }

                export namespace LivingSpawnEvent {
                    /** net.minecraftforge.event.entity.living.LivingSpawnEvent */
                    export interface LivingSpawnEvent extends net.minecraftforge.event.entity.living.LivingEvent {
                        world: net.minecraft.world.World;
                        x: number;
                        y: number;
                        z: number;
                    }
                    /** net.minecraftforge.event.entity.living.LivingSpawnEvent.CheckSpawn */
                    export type CheckSpawn = net.minecraftforge.event.entity.living.LivingSpawnEvent.LivingSpawnEvent
                    /** net.minecraftforge.event.entity.living.LivingSpawnEvent.SpecialSpawn */
                    export type SpecialSpawn = net.minecraftforge.event.entity.living.LivingSpawnEvent.LivingSpawnEvent
                    /** net.minecraftforge.event.entity.living.LivingSpawnEvent.AllowDespawn */
                    export type AllowDespawn = net.minecraftforge.event.entity.living.LivingSpawnEvent.LivingSpawnEvent
                }

                export namespace ZombieEvent {
                    /** net.minecraftforge.event.entity.living.ZombieEvent.SummonAidEvent */
                    export interface SummonAidEvent extends net.minecraftforge.event.entity.EntityEvent {
                        customSummonedAid: net.minecraft.entity.EntityZombie;
                        world: net.minecraft.world.World;
                        x: number;
                        y: number;
                        z: number;
                        attacker: net.minecraft.entity.EntityLivingBase;
                        summonChance: number;
                    }
                }
            }

            export namespace player {
                /** net.minecraftforge.event.entity.player.PlayerEvent */
                export interface PlayerEvent extends net.minecraftforge.event.entity.living.LivingEvent {
                    /** WARNING: Forge 1.7.10 uses 'entityPlayer' as the field name in this class! */
                    entityPlayer: net.minecraft.entity.player.EntityPlayer;
                }
                export namespace PlayerEvent {
                    /** net.minecraftforge.event.entity.player.PlayerEvent.HarvestCheck */
                    export interface HarvestCheck extends net.minecraftforge.event.entity.player.PlayerEvent {
                        block: net.minecraft.block.Block;
                        success: boolean;
                    }
                    /** net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed */
                    export interface BreakSpeed extends net.minecraftforge.event.entity.player.PlayerEvent {
                        block: net.minecraft.block.Block;
                        metadata: number;
                        originalSpeed: number;
                        newSpeed: number;
                        x: number;
                        y: number;
                        z: number;
                    }
                    /** net.minecraftforge.event.entity.player.PlayerEvent.NameFormat */
                    export interface NameFormat extends net.minecraftforge.event.entity.player.PlayerEvent {
                        username: string;
                        displayname: string;
                    }
                    /** net.minecraftforge.event.entity.player.PlayerEvent.Clone */
                    export interface Clone extends net.minecraftforge.event.entity.player.PlayerEvent {
                        original: net.minecraft.entity.player.EntityPlayer;
                        wasDeath: boolean;
                    }
                    /** net.minecraftforge.event.entity.player.PlayerEvent.StartTracking */
                    export interface StartTracking extends net.minecraftforge.event.entity.player.PlayerEvent {
                        target: net.minecraft.entity.Entity;
                    }
                    /** net.minecraftforge.event.entity.player.PlayerEvent.StopTracking */
                    export interface StopTracking extends net.minecraftforge.event.entity.player.PlayerEvent {
                        target: net.minecraft.entity.Entity;
                    }
                    /** net.minecraftforge.event.entity.player.PlayerEvent.LoadFromFile */
                    export interface LoadFromFile extends net.minecraftforge.event.entity.player.PlayerEvent {
                        playerDirectory: any;
                        playerUUID: string;
                        getPlayerFile(suffix: string): any;
                    }
                    /** net.minecraftforge.event.entity.player.PlayerEvent.SaveToFile */
                    export interface SaveToFile extends net.minecraftforge.event.entity.player.PlayerEvent {
                        playerDirectory: any;
                        playerUUID: string;
                        getPlayerFile(suffix: string): any;
                    }
                }

                /** net.minecraftforge.event.entity.player.AchievementEvent */
                export interface AchievementEvent extends net.minecraftforge.event.entity.player.PlayerEvent {
                    achievement: any;
                }

                /** net.minecraftforge.event.entity.player.AnvilRepairEvent */
                export interface AnvilRepairEvent extends net.minecraftforge.event.entity.player.PlayerEvent {
                    left: net.minecraft.item.ItemStack;
                    right: net.minecraft.item.ItemStack;
                    output: net.minecraft.item.ItemStack;
                }

                /** net.minecraftforge.event.entity.player.ArrowLooseEvent */
                export interface ArrowLooseEvent extends net.minecraftforge.event.entity.player.PlayerEvent {
                    bow: net.minecraft.item.ItemStack;
                    charge: number;
                }

                /** net.minecraftforge.event.entity.player.ArrowNockEvent */
                export interface ArrowNockEvent extends net.minecraftforge.event.entity.player.PlayerEvent {
                    result: net.minecraft.item.ItemStack;
                }

                /** net.minecraftforge.event.entity.player.AttackEntityEvent */
                export interface AttackEntityEvent extends net.minecraftforge.event.entity.player.PlayerEvent {
                    target: net.minecraft.entity.Entity;
                }

                /** net.minecraftforge.event.entity.player.BonemealEvent */
                export interface BonemealEvent extends net.minecraftforge.event.entity.player.PlayerEvent {
                    world: net.minecraft.world.World;
                    block: net.minecraft.block.Block;
                    x: number;
                    y: number;
                    z: number;
                }

                /** net.minecraftforge.event.entity.player.EntityInteractEvent */
                export interface EntityInteractEvent extends net.minecraftforge.event.entity.player.PlayerEvent {
                    target: net.minecraft.entity.Entity;
                }

                /** net.minecraftforge.event.entity.player.EntityItemPickupEvent */
                export interface EntityItemPickupEvent extends net.minecraftforge.event.entity.player.PlayerEvent {
                    item: net.minecraft.entity.EntityItem;
                }

                /** net.minecraftforge.event.entity.player.FillBucketEvent */
                export interface FillBucketEvent extends net.minecraftforge.event.entity.player.PlayerEvent {
                    current: net.minecraft.item.ItemStack;
                    result: net.minecraft.item.ItemStack;
                    world: net.minecraft.world.World;
                    target: net.minecraft.util.MovingObjectPosition;
                }

                /** net.minecraftforge.event.entity.player.ItemTooltipEvent */
                export interface ItemTooltipEvent extends net.minecraftforge.event.entity.player.PlayerEvent {
                    showAdvancedItemTooltips: boolean;
                    itemStack: net.minecraft.item.ItemStack;
                    toolTip: string[];
                }

                /** net.minecraftforge.event.entity.player.PlayerDestroyItemEvent */
                export interface PlayerDestroyItemEvent extends net.minecraftforge.event.entity.player.PlayerEvent {
                    original: net.minecraft.item.ItemStack;
                }

                /** net.minecraftforge.event.entity.player.PlayerDropsEvent */
                export interface PlayerDropsEvent extends net.minecraftforge.event.entity.living.LivingDropsEvent {
                    entityPlayer: net.minecraft.entity.player.EntityPlayer;
                }

                /** net.minecraftforge.event.entity.player.PlayerFlyableFallEvent */
                export interface PlayerFlyableFallEvent extends net.minecraftforge.event.entity.player.PlayerEvent {
                    distance: number;
                }

                /** net.minecraftforge.event.entity.player.PlayerInteractEvent */
                export interface PlayerInteractEvent extends net.minecraftforge.event.entity.player.PlayerEvent {
                    action: 'RIGHT_CLICK_AIR' | 'RIGHT_CLICK_BLOCK' | 'LEFT_CLICK_BLOCK';
                    x: number;
                    y: number;
                    z: number;
                    face: number;
                    world: net.minecraft.world.World;
                    useBlock: 'ALLOW' | 'DENY' | 'DEFAULT';
                    useItem: 'ALLOW' | 'DENY' | 'DEFAULT';
                }

                /** net.minecraftforge.event.entity.player.PlayerOpenContainerEvent */
                export interface PlayerOpenContainerEvent extends net.minecraftforge.event.entity.player.PlayerEvent {
                    canInteractWith: boolean;
                }

                /** net.minecraftforge.event.entity.player.PlayerPickupXpEvent */
                export interface PlayerPickupXpEvent extends net.minecraftforge.event.entity.player.PlayerEvent {
                    orb: any;
                }

                /** net.minecraftforge.event.entity.player.PlayerSleepInBedEvent */
                export interface PlayerSleepInBedEvent extends net.minecraftforge.event.entity.player.PlayerEvent {
                    x: number;
                    y: number;
                    z: number;
                    result: 'OK' | 'NOT_POSSIBLE_HERE' | 'NOT_POSSIBLE_NOW' | 'TOO_FAR_AWAY' | 'OTHER_PROBLEM' | 'NOT_SAFE';
                }

                /** net.minecraftforge.event.entity.player.PlayerUseItemEvent */
                export interface PlayerUseItemEvent extends net.minecraftforge.event.entity.player.PlayerEvent {
                    item: net.minecraft.item.ItemStack;
                    duration: number;
                }
                export namespace PlayerUseItemEvent {
                    export type Start = net.minecraftforge.event.entity.player.PlayerUseItemEvent
                    export type Tick = net.minecraftforge.event.entity.player.PlayerUseItemEvent
                    export type Stop = net.minecraftforge.event.entity.player.PlayerUseItemEvent
                    export type Finish = net.minecraftforge.event.entity.player.PlayerUseItemEvent
                }

                /** net.minecraftforge.event.entity.player.PlayerWakeUpEvent */
                export interface PlayerWakeUpEvent extends net.minecraftforge.event.entity.player.PlayerEvent {
                    wakeImmediately: boolean;
                    updateWorld: boolean;
                    setSpawn: boolean;
                }

                /** net.minecraftforge.event.entity.player.UseHoeEvent */
                export interface UseHoeEvent extends net.minecraftforge.event.entity.player.PlayerEvent {
                    current: net.minecraft.item.ItemStack;
                    world: net.minecraft.world.World;
                    x: number;
                    y: number;
                    z: number;
                }
            }
        }

        export namespace world {
            /** net.minecraftforge.event.world.WorldEvent */
            export interface WorldEvent extends cpw.mods.fml.common.eventhandler.Event {
                world: net.minecraft.world.World;
            }
            export namespace WorldEvent {
                export type Load = net.minecraftforge.event.world.WorldEvent
                export type Unload = net.minecraftforge.event.world.WorldEvent
                export type Save = net.minecraftforge.event.world.WorldEvent
                export interface PotentialSpawns extends net.minecraftforge.event.world.WorldEvent {
                    type: any;
                    x: number;
                    y: number;
                    z: number;
                    list: any[];
                }
                export interface CreateSpawnPosition extends net.minecraftforge.event.world.WorldEvent {
                    settings: any;
                }
            }

            export namespace BlockEvent {
                /** net.minecraftforge.event.world.BlockEvent */
                export interface BlockEvent extends cpw.mods.fml.common.eventhandler.Event {
                    x: number;
                    y: number;
                    z: number;
                    world: net.minecraft.world.World;
                    block: net.minecraft.block.Block;
                    blockMetadata: number;
                }
                /** net.minecraftforge.event.world.BlockEvent.BreakEvent */
                export interface BreakEvent extends net.minecraftforge.event.world.BlockEvent.BlockEvent {
                    /** @returns The player who broke the block */
                    getPlayer(): net.minecraft.entity.player.EntityPlayer;
                    /** @returns The experience to drop */
                    getExpToDrop(): number;
                    /** Sets the experience to drop */
                    setExpToDrop(exp: number): void;
                }
                /** net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent */
                export interface HarvestDropsEvent extends net.minecraftforge.event.world.BlockEvent.BlockEvent {
                    fortuneLevel: number;
                    drops: net.minecraft.item.ItemStack[];
                    isSilkTouching: boolean;
                    dropChance: number;
                    /** May be null for non-player harvesting */
                    harvester: net.minecraft.entity.player.EntityPlayer;
                }
                /** net.minecraftforge.event.world.BlockEvent.PlaceEvent */
                export interface PlaceEvent extends net.minecraftforge.event.world.BlockEvent.BlockEvent {
                    player: net.minecraft.entity.player.EntityPlayer;
                    itemInHand: net.minecraft.item.ItemStack;
                    placedBlock: net.minecraft.block.Block;
                    placedAgainst: net.minecraft.block.Block;
                    /** Note: BlockSnapshot is a complex structure, simplifying here */
                    blockSnapshot: any;
                }
                /** net.minecraftforge.event.world.BlockEvent.MultiPlaceEvent */
                export interface MultiPlaceEvent extends net.minecraftforge.event.world.BlockEvent.PlaceEvent {
                    /** @returns A list of replaced block snapshots */
                    getReplacedBlockSnapshots(): any[];
                }
            }

            export namespace explosion {
                /** net.minecraftforge.event.world.ExplosionEvent */
                export interface ExplosionEvent extends cpw.mods.fml.common.eventhandler.Event {
                    world: net.minecraft.world.World;
                    explosion: any;
                }
                export namespace ExplosionEvent {
                    export type Start = net.minecraftforge.event.world.explosion.ExplosionEvent
                    export interface Detonate extends net.minecraftforge.event.world.explosion.ExplosionEvent {
                        affectedEntities: net.minecraft.entity.Entity[];
                    }
                }
            }

            export namespace note {
                /** net.minecraftforge.event.world.NoteBlockEvent */
                export type NoteBlockEvent = net.minecraftforge.event.world.BlockEvent.BlockEvent
                export namespace NoteBlockEvent {
                    export interface Play extends net.minecraftforge.event.world.note.NoteBlockEvent {
                        instrument: any;
                        note: number;
                    }
                    export interface Change extends net.minecraftforge.event.world.note.NoteBlockEvent {
                        oldInstrument: any;
                        oldNote: number;
                        newInstrument: any;
                        newNote: number;
                    }
                }
            }
        }
    }

    // ============================================================================
    // GLOBAL ALIASES - To match CustomNPC+ pattern (e.g., IForgeEvent.AnvilUpdateEvent)
    // Points to the new deep structure to ensure correct tooltips.
    // ============================================================================
    namespace IForgeEvent {
        export type InitEvent = cpw.mods.fml.common.eventhandler.Event;

        export type AnvilUpdateEvent = net.minecraftforge.event.AnvilUpdateEvent;
        export type CommandEvent = net.minecraftforge.event.CommandEvent;
        export type ServerChatEvent = net.minecraftforge.event.ServerChatEvent;
        export type FuelBurnTimeEvent = net.minecraftforge.event.FuelBurnTimeEvent;

        export type PotionBrewEvent = net.minecraftforge.event.brewing.PotionBrewEvent;
        export type PotionBrewedEvent = net.minecraftforge.event.brewing.PotionBrewedEvent;

        export type EntityConstructing = net.minecraftforge.event.entity.EntityEvent.EntityConstructing;
        export type CanUpdate = net.minecraftforge.event.entity.EntityEvent.CanUpdate;
        export type EnteringChunk = net.minecraftforge.event.entity.EntityEvent.EnteringChunk;
        export type EntityJoinWorldEvent = net.minecraftforge.event.entity.EntityJoinWorldEvent;
        export type EntityStruckByLightningEvent = net.minecraftforge.event.entity.EntityStruckByLightningEvent;
        export type PlaySoundAtEntityEvent = net.minecraftforge.event.entity.PlaySoundAtEntityEvent;

        export type ItemEvent = net.minecraftforge.event.entity.item.ItemEvent;
        export type ItemExpireEvent = net.minecraftforge.event.entity.item.ItemExpireEvent;
        export type ItemTossEvent = net.minecraftforge.event.entity.item.ItemTossEvent;

        export type LivingUpdateEvent = net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
        export type LivingJumpEvent = net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
        export type LivingAttackEvent = net.minecraftforge.event.entity.living.LivingAttackEvent;
        export type LivingDeathEvent = net.minecraftforge.event.entity.living.LivingDeathEvent;
        export type LivingDropsEvent = net.minecraftforge.event.entity.living.LivingDropsEvent;
        export type LivingFallEvent = net.minecraftforge.event.entity.living.LivingFallEvent;
        export type LivingHealEvent = net.minecraftforge.event.entity.living.LivingHealEvent;
        export type LivingHurtEvent = net.minecraftforge.event.entity.living.LivingHurtEvent;
        export type LivingPackSizeEvent = net.minecraftforge.event.entity.living.LivingPackSizeEvent;
        export type LivingSetAttackTargetEvent = net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
        export type EnderTeleportEvent = net.minecraftforge.event.entity.living.EnderTeleportEvent;

        export type CheckSpawn = net.minecraftforge.event.entity.living.LivingSpawnEvent.CheckSpawn;
        export type SpecialSpawn = net.minecraftforge.event.entity.living.LivingSpawnEvent.SpecialSpawn;
        export type AllowDespawn = net.minecraftforge.event.entity.living.LivingSpawnEvent.AllowDespawn;
        export type SummonAidEvent = net.minecraftforge.event.entity.living.ZombieEvent.SummonAidEvent;

        export type HarvestCheck = net.minecraftforge.event.entity.player.PlayerEvent.HarvestCheck;
        export type BreakSpeed = net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
        export type NameFormat = net.minecraftforge.event.entity.player.PlayerEvent.NameFormat;
        export type Clone = net.minecraftforge.event.entity.player.PlayerEvent.Clone;
        export type StartTracking = net.minecraftforge.event.entity.player.PlayerEvent.StartTracking;
        export type StopTracking = net.minecraftforge.event.entity.player.PlayerEvent.StopTracking;
        export type LoadFromFile = net.minecraftforge.event.entity.player.PlayerEvent.LoadFromFile;
        export type SaveToFile = net.minecraftforge.event.entity.player.PlayerEvent.SaveToFile;

        export type AchievementEvent = net.minecraftforge.event.entity.player.AchievementEvent;
        export type AnvilRepairEvent = net.minecraftforge.event.entity.player.AnvilRepairEvent;
        export type ArrowLooseEvent = net.minecraftforge.event.entity.player.ArrowLooseEvent;
        export type ArrowNockEvent = net.minecraftforge.event.entity.player.ArrowNockEvent;
        export type AttackEntityEvent = net.minecraftforge.event.entity.player.AttackEntityEvent;
        export type BonemealEvent = net.minecraftforge.event.entity.player.BonemealEvent;
        export type EntityInteractEvent = net.minecraftforge.event.entity.player.EntityInteractEvent;
        export type EntityItemPickupEvent = net.minecraftforge.event.entity.player.EntityItemPickupEvent;
        export type FillBucketEvent = net.minecraftforge.event.entity.player.FillBucketEvent;
        export type ItemTooltipEvent = net.minecraftforge.event.entity.player.ItemTooltipEvent;
        export type PlayerDestroyItemEvent = net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
        export type PlayerDropsEvent = net.minecraftforge.event.entity.player.PlayerDropsEvent;
        export type PlayerFlyableFallEvent = net.minecraftforge.event.entity.player.PlayerFlyableFallEvent;
        export type PlayerInteractEvent = net.minecraftforge.event.entity.player.PlayerInteractEvent;
        export type PlayerOpenContainerEvent = net.minecraftforge.event.entity.player.PlayerOpenContainerEvent;
        export type PlayerPickupXpEvent = net.minecraftforge.event.entity.player.PlayerPickupXpEvent;
        export type PlayerSleepInBedEvent = net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
        export type UseHoeEvent = net.minecraftforge.event.entity.player.UseHoeEvent;

        export type PlayerUseItemStart = net.minecraftforge.event.entity.player.PlayerUseItemEvent.Start;
        export type PlayerUseItemTick = net.minecraftforge.event.entity.player.PlayerUseItemEvent.Tick;
        export type PlayerUseItemStop = net.minecraftforge.event.entity.player.PlayerUseItemEvent.Stop;
        export type PlayerUseItemFinish = net.minecraftforge.event.entity.player.PlayerUseItemEvent.Finish;
        export type PlayerWakeUpEvent = net.minecraftforge.event.entity.player.PlayerWakeUpEvent;

        export type WorldLoad = net.minecraftforge.event.world.WorldEvent.Load;
        export type WorldUnload = net.minecraftforge.event.world.WorldEvent.Unload;
        export type WorldSave = net.minecraftforge.event.world.WorldEvent.Save;
        export type PotentialSpawns = net.minecraftforge.event.world.WorldEvent.PotentialSpawns;
        export type CreateSpawnPosition = net.minecraftforge.event.world.WorldEvent.CreateSpawnPosition;

        export type BreakEvent = net.minecraftforge.event.world.BlockEvent.BreakEvent;
        export type HarvestDropsEvent = net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent;
        export type PlaceEvent = net.minecraftforge.event.world.BlockEvent.PlaceEvent;
        export type MultiPlaceEvent = net.minecraftforge.event.world.BlockEvent.MultiPlaceEvent;
        export type BlockEvent = net.minecraftforge.event.world.BlockEvent.BlockEvent;

        export type ExplosionStart = net.minecraftforge.event.world.explosion.ExplosionEvent.Start;
        export type ExplosionDetonate = net.minecraftforge.event.world.explosion.ExplosionEvent.Detonate;

        export type NotePlay = net.minecraftforge.event.world.note.NoteBlockEvent.Play;
        export type NoteChange = net.minecraftforge.event.world.note.NoteBlockEvent.Change;

        // FML Events
        export type PlayerTickEvent = cpw.mods.fml.common.gameevent.TickEvent.PlayerTickEvent;
        export type WorldTickEvent = cpw.mods.fml.common.gameevent.TickEvent.WorldTickEvent;
        export type ServerTickEvent = cpw.mods.fml.common.gameevent.TickEvent.ServerTickEvent;
        export type ClientTickEvent = cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
        export type RenderTickEvent = cpw.mods.fml.common.gameevent.TickEvent.RenderTickEvent;
        export type TickEvent = cpw.mods.fml.common.gameevent.TickEvent;

        export type KeyInputEvent = cpw.mods.fml.common.gameevent.InputEvent.KeyInputEvent;
        export type MouseInputEvent = cpw.mods.fml.common.gameevent.InputEvent.MouseInputEvent;

        export type PlayerLoggedInEvent = cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
        export type PlayerLoggedOutEvent = cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
        export type PlayerRespawnEvent = cpw.mods.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;
        export type PlayerChangedDimensionEvent = cpw.mods.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent;
        export type PlayerItemPickupEvent = cpw.mods.fml.common.gameevent.PlayerEvent.ItemPickupEvent;
        export type PlayerItemCraftedEvent = cpw.mods.fml.common.gameevent.PlayerEvent.ItemCraftedEvent;
        export type PlayerItemSmeltedEvent = cpw.mods.fml.common.gameevent.PlayerEvent.ItemSmeltedEvent;
    }
}

export { };
