package noppes.npcs.api.handler.data;

import noppes.npcs.api.IWorld;
import noppes.npcs.api.entity.IEntity;

/**
 * Configures a natural spawn entry for custom NPCs, including entity templates,
 * biome restrictions, spawn conditions, and cooldowns.
 */
public interface INaturalSpawn {
    /** Despawn mode: force natural despawn behavior. */
    int DESPAWN_FORCE_NATURAL = 0;
    /** Despawn mode: preserve the entity template's despawn setting. */
    int DESPAWN_PRESERVE_TEMPLATE = 1;
    /** Despawn mode: force persistent (never despawn). */
    int DESPAWN_FORCE_PERSISTENT = 2;

    /** @param name the display name for this spawn entry. */
    void setName(String name);

    /** @return the display name of this spawn entry. */
    String getName();

    /**
     * Sets the entity template for a specific spawn slot.
     *
     * @param entity the entity template.
     * @param slot   the slot index.
     */
    void setEntity(IEntity entity, int slot);

    /**
     * Returns the entity for a specific spawn slot.
     *
     * @param world the world context for entity creation.
     * @param slot  the slot index.
     * @return the entity, or null if the slot is empty.
     */
    IEntity getEntity(IWorld world, int slot);

    /** @return the populated slot indices. */
    Integer[] getSlots();

    /** @param weight the spawn weight (higher = more frequent). */
    void setWeight(int weight);

    /** @return the spawn weight. */
    int getWeight();

    /** @param height the minimum Y-level for spawning. */
    void setMinHeight(int height);

    /** @return the minimum Y-level. */
    int getMinHeight();

    /** @param height the maximum Y-level for spawning. */
    void setMaxHeight(int height);

    /** @return the maximum Y-level. */
    int getMaxHeight();

    /** @param spawns whether entities can spawn using animal spawn rules. */
    void spawnsLikeAnimal(boolean spawns);

    /** @return true if animal spawn rules apply. */
    boolean spawnsLikeAnimal();

    /** @param spawns whether entities can spawn using monster spawn rules. */
    void spawnsLikeMonster(boolean spawns);

    /** @return true if monster spawn rules apply. */
    boolean spawnsLikeMonster();

    /** @param spawns whether entities can spawn in liquid. */
    void spawnsInLiquid(boolean spawns);

    /** @return true if spawning in liquid is allowed. */
    boolean spawnsInLiquid();

    /** @param spawns whether entities can spawn in the air. */
    void spawnsInAir(boolean spawns);

    /** @return true if spawning in air is allowed. */
    boolean spawnsInAir();

    /** @return the biome names this spawn is restricted to. */
    String[] getBiomes();

    /** @param biomes the biome names to restrict spawning to. */
    void setBiomes(String[] biomes);

    /** @param maxAlive the maximum number of alive entities from this spawn at once. */
    void setMaxAlive(int maxAlive);

    /** @return the maximum alive entity count. */
    int getMaxAlive();

    /** @param ticks the cooldown in ticks between spawn attempts. */
    void setCooldownTicks(int ticks);

    /** @return the cooldown in ticks. */
    int getCooldownTicks();

    /** @param attempts the number of spawn attempts per cycle. */
    void setAttemptsPerCycle(int attempts);

    /** @return the number of spawn attempts per cycle. */
    int getAttemptsPerCycle();

    /** @param distance the minimum distance from a player for spawning. */
    void setPlayerMinDistance(int distance);

    /** @return the minimum distance from a player. */
    int getPlayerMinDistance();

    /**
     * Sets the despawn mode.
     *
     * @param mode 0: DESPAWN_FORCE_NATURAL, 1: DESPAWN_PRESERVE_TEMPLATE, 2: DESPAWN_FORCE_PERSISTENT.
     */
    void setDespawnMode(int mode);

    /**
     * @return the despawn mode (0: force natural, 1: preserve template, 2: force persistent).
     */
    int getDespawnMode();
}
