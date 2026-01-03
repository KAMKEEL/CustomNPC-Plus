/**
 * Raw Minecraft 1.7.10 Type Definitions (MCP Names)
 * These represent the internal deobfuscated Minecraft classes.
 */

declare global {
    namespace net.minecraft {
        export namespace block {
            export interface Block {
                /** @returns The unlocalized name of the block */
                getUnlocalizedName(): string;
                /** @returns The localized name of the block */
                getLocalizedName(): string;
                /** @returns Light opacity of the block */
                getLightOpacity(): number;
                /** @returns Light value of the block */
                getLightValue(): number;
            }
        }

        export namespace damage {
            export interface DamageSource {
                /** The type of damage (e.g. "player", "fall", "magic") */
                damageType: string;
                /** @returns True if damage bypasses armor */
                isUnblockable(): boolean;
                /** @returns True if damage is absolute (bypasses potions etc) */
                isDamageAbsolute(): boolean;
                /** @returns The entity that caused the damage */
                getEntity(): net.minecraft.entity.Entity;
                /** @returns The entity that directly dealt the damage (e.g. arrow) */
                getSourceOfDamage(): net.minecraft.entity.Entity;
            }
        }

        export namespace entity {
            /** Base class for all entities in Minecraft 1.7.10 */
            export interface Entity {
                /** The entity's unique ID */
                entityId: number;
                /** The world the entity is in */
                worldObj: net.minecraft.world.World;
                /** X position */
                posX: number;
                /** Y position */
                posY: number;
                /** Z position */
                posZ: number;
                /** X motion */
                motionX: number;
                /** Y motion */
                motionY: number;
                /** Z motion */
                motionZ: number;
                /** Rotation yaw */
                rotationYaw: number;
                /** Rotation pitch */
                rotationPitch: number;
                /** Entity width */
                width: number;
                /** Entity height */
                height: number;
                /** Whether the entity is on the ground */
                onGround: boolean;
                /** Whether the entity is in water */
                isInWeb: boolean;
                /** Ticks the entity has been alive */
                ticksExisted: number;
                /** Remaining air */
                air: number;
                /** Damage sustained from falling */
                fallDistance: number;

                /** Gets a unique string key for this entity type */
                getEntityString(): string;
                /** Sets the entity's position */
                setPosition(x: number, y: number, z: number): void;
                /** Sets the entity's rotation */
                setRotation(yaw: number, pitch: number): void;
                /** Marks the entity for removal */
                setDead(): void;
                /** @returns True if the entity is burning */
                isBurning(): boolean;
                /** @returns True if the entity is sneaking */
                isSneaking(): boolean;
                /** @returns True if the entity is sprinting */
                isSprinting(): boolean;
                /** @returns True if the entity is invisible */
                isInvisible(): boolean;
            }

            export interface EntityLivingBase extends Entity {
                /** @returns Current health */
                getHealth(): number;
                /** Sets the entity's health */
                setHealth(health: number): void;
                /** @returns Max health */
                getMaxHealth(): number;
                /** @returns True if entity is child */
                isChild(): boolean;
                /** Sets entity on fire */
                setFire(seconds: number): void;
            }

            export interface EntityLiving extends EntityLivingBase {
                /** Sets the attack target */
                setAttackTarget(target: net.minecraft.entity.EntityLivingBase): void;
                /** @returns The current attack target */
                getAttackTarget(): net.minecraft.entity.EntityLivingBase;
                /** @returns True if the entity can pick up loot */
                canPickUpLoot(): boolean;
                /** Sets if the entity can pick up loot */
                setCanPickUpLoot(can: boolean): void;
            }

            export interface EntityItem extends Entity {
                /** The ItemStack contained in this entity */
                getEntityItem(): net.minecraft.item.ItemStack;
                /** Sets the ItemStack contained in this entity */
                setEntityItemStack(stack: net.minecraft.item.ItemStack): void;
            }

            export interface EntityZombie extends EntityLiving {
                /** @returns True if the zombie is a child */
                isChild(): boolean;
                /** Sets if the zombie is a child */
                setChild(child: boolean): void;
            }

            export namespace player {
                /** Representation of a Player in Minecraft 1.7.10 */
                export interface EntityPlayer extends net.minecraft.entity.Entity {
                    /** The player's inventory */
                    inventory: net.minecraft.entity.player.InventoryPlayer;
                    /** The player's experience level */
                    experienceLevel: number;
                    /** Total experience points */
                    experienceTotal: number;
                    /** Progress to next level (0.0 to 1.0) */
                    experience: number;
                    /** Player capabilities (flying, creative, etc.) */
                    capabilities: net.minecraft.entity.player.PlayerCapabilities;
                    /** Food stats (hunger, saturation) */
                    foodStats: any;
                    /** The player's dimension ID */
                    dimension: number;

                    /** Sends a chat message to the player */
                    addChatMessage(component: any): void;
                    /** @returns The display name of the player */
                    getDisplayName(): string;
                    /** @returns The actual username/command sender name */
                    getCommandSenderName(): string;
                    /** Closes any open GUI */
                    closeScreen(): void;
                }

                export interface InventoryPlayer {
                    /** Current selected slot index (0-8) */
                    currentItem: number;
                    /** Main inventory array (36 slots) */
                    mainInventory: net.minecraft.item.ItemStack[];
                    /** Armor inventory array (4 slots) */
                    armorInventory: net.minecraft.item.ItemStack[];

                    /** Gets the currently held item */
                    getCurrentItem(): net.minecraft.item.ItemStack;
                    /** Consumes 1 of the item in the specified slot */
                    consumeInventoryItem(item: any): boolean;
                    /** Checks if the player has a specific item */
                    hasItem(item: any): boolean;
                }

                export interface PlayerCapabilities {
                    /** Whether the player is invulnerable */
                    disableDamage: boolean;
                    /** Whether the player is flying */
                    isFlying: boolean;
                    /** Whether the player can fly */
                    allowFlying: boolean;
                    /** Whether the player is in creative mode */
                    isCreativeMode: boolean;
                    /** Flying speed */
                    flySpeed: number;
                    /** Walking speed */
                    walkSpeed: number;
                }
            }
        }

        export namespace world {
            /** Representation of the World in Minecraft 1.7.10 */
            export interface World {
                /** World provider (dimension info) */
                provider: net.minecraft.world.WorldProvider;
                /** List of all entities in the world */
                loadedEntityList: net.minecraft.entity.Entity[];
                /** List of all players in the world */
                playerEntities: net.minecraft.entity.player.EntityPlayer[];
                /** True if this is a client-side world */
                isRemote: boolean;
                /** World time in ticks */
                worldTime: number;
                /** The world's difficulty (0-3) */
                difficultySetting: number;

                /** @returns The block at the given coordinates */
                getBlock(x: number, y: number, z: number): net.minecraft.block.Block;
                /** @returns The metadata of the block at the given coordinates */
                getBlockMetadata(x: number, y: number, z: number): number;
                /** Sets the block and metadata at the given coordinates */
                setBlock(x: number, y: number, z: number, block: net.minecraft.block.Block, metadata: number, flags: number): boolean;
                /** Spawns an entity in the world */
                spawnEntityInWorld(entity: net.minecraft.entity.Entity): boolean;
                /** Plays a sound at the given coordinates */
                playSoundEffect(x: number, y: number, z: number, name: string, volume: number, pitch: number): void;
            }

            export interface WorldProvider {
                /** Dimension ID (0 = Overworld, -1 = Nether, 1 = End) */
                dimensionId: number;
                /** Name of the dimension type */
                dimensionName: string;
                /** Whether this dimension has a sky */
                hasSkyLight: boolean;
            }
        }

        export namespace item {
            /** Representation of an Item stack in Minecraft 1.7.10 */
            export interface ItemStack {
                /** Number of items in the stack */
                stackSize: number;
                /** Damage/Metadata value of the item */
                itemDamage: number;
                /** The underlying Item object */
                item: any;
                /** NBT data of the stack */
                stackTagCompound: any;

                /** @returns The display name of the item */
                getDisplayName(): string;
                /** @returns True if the item has NBT data */
                hasTagCompound(): boolean;
                /** Copies the item stack */
                copy(): net.minecraft.item.ItemStack;
            }
        }

        export namespace util {
            /** Result of a raytrace in 1.7.10 */
            export interface MovingObjectPosition {
                /** 0 = MISS, 1 = BLOCK, 2 = ENTITY */
                typeOfHit: number;
                /** X coordinate of the hit block */
                blockX: number;
                /** Y coordinate of the hit block */
                blockY: number;
                /** Z coordinate of the hit block */
                blockZ: number;
                /** Side of the block hit (0-5) */
                sideHit: number;
                /** The entity hit, if typeOfHit is ENTITY */
                entityHit: net.minecraft.entity.Entity;
                /** The exact hit vector */
                hitVec: net.minecraft.util.Vec3;
            }

            export interface Vec3 {
                xCoord: number;
                yCoord: number;
                zCoord: number;
            }
        }
    }
}

export { };
