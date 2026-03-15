/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler.data
 */

/**
 * Configures a natural spawn entry for custom NPCs, including entity templates,
 * biome restrictions, spawn conditions, and cooldowns.
  * @javaFqn noppes.npcs.api.handler.data.INaturalSpawn
*/
export interface INaturalSpawn {
    /** Despawn mode: force natural despawn behavior. */
    int DESPAWN_FORCE_NATURAL = 0;
    /** Despawn mode: preserve the entity template's despawn setting. */
    int DESPAWN_PRESERVE_TEMPLATE = 1;
    /** Despawn mode: force persistent (never despawn). */
    int DESPAWN_FORCE_PERSISTENT = 2;
    
    /** @param name the display name for this spawn entry. */
    setName(name: String): import('./void').void;
    /** @return the display name of this spawn entry. */
    getName(): String;
    /**
     * Sets the entity template for a specific spawn slot.
     *
     * @param entity the entity template.
     * @param slot   the slot index.
     */
    setEntity(entity: import('../../entity/IEntity').IEntity, slot: import('./int').int): import('./void').void;
    /**
     * Returns the entity for a specific spawn slot.
     *
     * @param world the world context for entity creation.
     * @param slot  the slot index.
     * @return the entity, or null if the slot is empty.
     */
    getEntity(world: import('../../IWorld').IWorld, slot: import('./int').int): import('../../entity/IEntity').IEntity;
    /** @return the populated slot indices. */
    getSlots(): Integer[];
    /** @param weight the spawn weight (higher = more frequent). */
    setWeight(weight: import('./int').int): import('./void').void;
    /** @return the spawn weight. */
    getWeight(): import('./int').int;
    /** @param height the minimum Y-level for spawning. */
    setMinHeight(height: import('./int').int): import('./void').void;
    /** @return the minimum Y-level. */
    getMinHeight(): import('./int').int;
    /** @param height the maximum Y-level for spawning. */
    setMaxHeight(height: import('./int').int): import('./void').void;
    /** @return the maximum Y-level. */
    getMaxHeight(): import('./int').int;
    /** @param spawns whether entities can spawn using animal spawn rules. */
    spawnsLikeAnimal(spawns: import('./boolean').boolean): import('./void').void;
    /** @return true if animal spawn rules apply. */
    spawnsLikeAnimal(): import('./boolean').boolean;
    /** @param spawns whether entities can spawn using monster spawn rules. */
    spawnsLikeMonster(spawns: import('./boolean').boolean): import('./void').void;
    /** @return true if monster spawn rules apply. */
    spawnsLikeMonster(): import('./boolean').boolean;
    /** @param spawns whether entities can spawn in liquid. */
    spawnsInLiquid(spawns: import('./boolean').boolean): import('./void').void;
    /** @return true if spawning in liquid is allowed. */
    spawnsInLiquid(): import('./boolean').boolean;
    /** @param spawns whether entities can spawn in the air. */
    spawnsInAir(spawns: import('./boolean').boolean): import('./void').void;
    /** @return true if spawning in air is allowed. */
    spawnsInAir(): import('./boolean').boolean;
    /** @return the biome names this spawn is restricted to. */
    getBiomes(): String[];
    /** @param biomes the biome names to restrict spawning to. */
    setBiomes(biomes: String[]): import('./void').void;
    /** @param maxAlive the maximum number of alive entities from this spawn at once. */
    setMaxAlive(maxAlive: import('./int').int): import('./void').void;
    /** @return the maximum alive entity count. */
    getMaxAlive(): import('./int').int;
    /** @param ticks the cooldown in ticks between spawn attempts. */
    setCooldownTicks(ticks: import('./int').int): import('./void').void;
    /** @return the cooldown in ticks. */
    getCooldownTicks(): import('./int').int;
    /** @param attempts the number of spawn attempts per cycle. */
    setAttemptsPerCycle(attempts: import('./int').int): import('./void').void;
    /** @return the number of spawn attempts per cycle. */
    getAttemptsPerCycle(): import('./int').int;
    /** @param distance the minimum distance from a player for spawning. */
    setPlayerMinDistance(distance: import('./int').int): import('./void').void;
    /** @return the minimum distance from a player. */
    getPlayerMinDistance(): import('./int').int;
    /**
     * Sets the despawn mode.
     *
     * @param mode 0: DESPAWN_FORCE_NATURAL, 1: DESPAWN_PRESERVE_TEMPLATE, 2: DESPAWN_FORCE_PERSISTENT.
     */
    setDespawnMode(mode: import('./int').int): import('./void').void;
    /**
     * @return the despawn mode (0: force natural, 1: preserve template, 2: force persistent).
     */
    getDespawnMode(): import('./int').int;
    biomes: String[];
    dimensions: Integer[];
    id: import('./int').int;
    name: String;
    spawnCompounds: Java.java.util.HashMap<Integer, import('./NBTTagCompound').NBTTagCompound>;
    animalSpawning: import('./boolean').boolean;
    monsterSpawning: import('./boolean').boolean;
    liquidSpawning: import('./boolean').boolean;
    airSpawning: import('./boolean').boolean;
    spawnHeightMin: import('./int').int;
    spawnHeightMax: import('./int').int;
    maxAlive: import('./int').int;
    cooldownTicks: import('./int').int;
    attemptsPerCycle: import('./int').int;
    playerMinDistance: import('./int').int;
    despawnMode: import('./int').int;
}
