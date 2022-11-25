//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package noppes.npcs.api.entity;

import net.minecraft.entity.EntityCreature;
import noppes.npcs.api.IPos;
import noppes.npcs.api.ITimers;
import noppes.npcs.api.handler.IOverlayHandler;
import noppes.npcs.api.handler.data.IFaction;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.jobs.IJob;
import noppes.npcs.api.roles.IRole;

public interface ICustomNpc<T extends EntityCreature> extends IEntityLiving<T> {
    /**
     * @return Returns the current npcs size 1-30
     */
    int getSize();

    /**
     * @param size The size of the npc (1-30) default is 5
     */
    void setSize(int size);

    /**
     * @return Returns the current npcs modelType of the NPC
     */
    int getModelType();

    /**
     * @param modelType The modelType of the NPC. 0: Steve, 1: Steve64, 2: Alex
     */
    void setModelType(int modelType);

    /**
     * @return The npcs name
     */
    String getName();

    void setRotation(float rotation);

    void setRotationType(int rotationType);

    int getRotationType();

    /**
     * @param name The name of the npc
     */
    void setName(String name);

    /**
     * @since 1.7.10
     * @return The npcs title
     */
    String getTitle();

    /**
     * @since 1.7.10
     * @param title The title of the npc
     */
    void setTitle(String title);

    /**
     * @return The npcs texture
     */
    String getTexture();

    /**
     * @param texture The texture of the npc
     */
    void setTexture(String texture);

    IPos getHome();

    /**
     * @return Home position x
     */
    int getHomeX();

    /**
     * @param x The home x position
     */
    void setHomeX(int x);

    /**
     * @return Home position x
     */
    int getHomeY();

    /**
     * @param y The home y position
     */
    void setHomeY(int y);

    /**
     * @return Home position x
     */
    int getHomeZ();

    /**
     * @param z The home x position
     */
    void setHomeZ(int z);

    /**
     * @param x The home x position
     * @param y The home y position
     * @param z The home z position
     */
    void setHome(int x, int y, int z);

    void setHome(IPos pos);

    /**
     * @param health New max health
     */
    void setMaxHealth(double health);

    /**
     * @param bo Whether or not the npc will try to return to his home position
     */
    void setReturnToHome(boolean bo);

    /**
     * @return Whether or not the npc returns home
     */
    boolean getReturnToHome();

    /**
     * @return The faction of the npc
     */
    IFaction getFaction();

    /**
     * @param id The id of the new faction
     */
    void setFaction(int id);

    /**
     *
     * @param attackOtherFactions True if you want the NPC to attack other factions, false otherwise.
     */
    void setAttackFactions(boolean attackOtherFactions);

    /**
     *
     * @return Returns true if the NPC attacks other factions, false otherwise.
     */
    boolean getAttackFactions();

    /**
     *
     * @param defendFaction True if the NPC should defend faction members, false otherwise.
     */
    void setDefendFaction(boolean defendFaction);

    /**
     *
     * @return Returns true if the NPC should defend faction members, false otherwise.
     */
    boolean getDefendFaction();

    int getType();

    boolean typeOf(int type);

    /**
     * @param target The targeted npc
     * @param item The item you want to shoot
     * @param accuracy Accuracy of the shot (0-100)
     */
    void shootItem(IEntityLivingBase target, IItemStack item, int accuracy);

    void setProjectilesKeepTerrain(boolean b);

    boolean getProjectilesKeepTerrain();

    /**
     * @param message The message the npc will say
     */
    void say(String message);

    /**
     * @param message The message the npc will say
     */
    void say(IPlayer player, String message);

    /**
     * Kill the npc, doesnt't despawn it
     */
    void kill();

    /**
     * Basically completely resets the npc. This will also call the Init script
     */
    void reset();

    /**
     * @return Returns the npcs current role
     */
    IRole getRole();

    void setRole(int role);

    /**
     * @return Returns the npcs current job
     */
    IJob getJob();

    void setJob(int job);

    /**
     * @return The item held in the right hand
     */
    IItemStack getRightItem();

    /**
     * @param item Item to be held in the right hand
     */
    void setRightItem(IItemStack item);

    /**
     * @return The item held in the left hand
     */
    IItemStack getLefttItem();

    /**
     * @param item Item to be held in the left hand
     */
    void setLeftItem(IItemStack item);

    /**
     * @return Returns the projectile the npc uses
     */
    IItemStack getProjectileItem();

    /**
     * @param item Item to be used as projectile
     */
    void setProjectileItem(IItemStack item);

    /**
     *
     * @return Returns true if the NPC can aim while shooting.
     */
    boolean canAimWhileShooting();

    /**
     *
     * @param aimWhileShooting Set to true if you want the NPC to aim while shooting, false otherwise.
     */
    void aimWhileShooting(boolean aimWhileShooting);

    void setMinProjectileDelay(int minDelay);
    int getMinProjectileDelay();

    void setMaxProjectileDelay(int maxDelay);
    int getMaxProjectileDelay();

    void setRangedRange(int rangedRange);
    int getRangedRange();

    void setFireRate(int rate);
    int getFireRate();

    void setBurstCount(int burstCount);
    int getBurstCount();

    void setShotCount(int shotCount);
    int getShotCount();

    void setAccuracy(int accuracy);
    int getAccuracy();

    /**
     *
     * @return The directory of the sound that plays when a projectile is shot
     */
    String getFireSound();

    /**
     *
     * @param fireSound The new directory of the sound that plays when a projectile is shot
     */
    void setFireSound(String fireSound);

    /**
     * @param slot The armor slot to return. 0:head, 1:body, 2:legs, 3:boots
     * @return Returns the worn armor in slot
     */
    IItemStack getArmor(int slot);

    /**
     * @param slot The armor slot to set. 0:head, 1:body, 2:legs, 3:boots
     * @param item Item to be set as armor
     */
    void setArmor(int slot, IItemStack item);

    /**
     *
     * @param slot The slot from the NPC's drop list to return (0-8)
     * @return The item in the NPC's drop list slot
     */
    IItemStack getLootItem(int slot);

    /**
     *
     * @param slot The slot from the NPC's drop list to change
     * @param item The item the drop list slot will be changed to
     */
    void setLootItem(int slot, IItemStack item);

    /**
     *
     * @param slot The slot from the NPC's drop list to return (0-8)
     * @return The chance of dropping the item in this slot. Returns 100 if the slot is not found.
     */
    double getLootChance(int slot);

    /**
     *
     * @param slot The slot from the NPC's drop list to change
     * @param chance The new chance of dropping the item in this slot
     */
    void setLootChance(int slot, double chance);

    /**
     *
     * @return The NPC's loot mode. 0 = Normal, 1 = Auto Pickup
     */
    int getLootMode();

    /**
     *
     * @param lootMode The NPC's loot mode. 0 = Normal, 1 = Auto Pickup
     */
    void setLootMode(int lootMode);

    /**
     *
     * @param lootXP The new minimum XP gained from killing the NPC. If greater than the max XP, it will be set to it.
     */
    void setMinLootXP(int lootXP);

    /**
     *
     * @param lootXP The new maximum XP gained from killing the NPC. If less than the min XP, it will be set to it.
     */
    void setMaxLootXP(int lootXP);

    /**
     *
     * @return The minimum XP gained from killing the NPC.
     */
    int getMinLootXP();

    /**
     *
     * @return The maximum XP gained from killing the NPC.
     */
    int getMaxLootXP();

    boolean getCanDrown();

    /**
     *
     * @param type 0 - Never drowns, 1 - Drowns in water, 2 - Drowns in air (without water)
     */
    void setDrowningType(int type);

    boolean canBreathe();

    /**
     * @param type The AnimationType
     */
    void setAnimation(int type);

    /**
     *
     * @param variant Changes the tactical variant of the NPC.
     *                0 - Rush
     *                1 - Dodge
     *                2 - Surround
     *                3 - Hit N Run
     *                4 - Ambush
     *                5 - Stalk
     *                6 - None
     *
     */
    void setTacticalVariant(int variant);

    /**
     *
     * @return Returns the tactical variant of the NPC.
     *                0 - Rush
     *                1 - Dodge
     *                2 - Surround
     *                3 - Hit N Run
     *                4 - Ambush
     *                5 - Stalk
     *                6 - None
     *
     */
    int getTacticalVariant();

    /**
     *
     * @param variant Sets the NPC's tactical variant by name.
     */
    void setTacticalVariant(String variant);

    /**
     *
     * @return Returns the name of the NPc's tactical variant.
     */
    String getTacticalVariantName();

    /**
     *
     * @param tacticalRadius Sets the radius in which the tactical variant is affected, if any. Effective for all tactical variants except Rush and None.
     */
    void setTacticalRadius(int tacticalRadius);

    /**
     *
     * @return Gets the radius in which the tactical variant is affected, if any. Effective for all tactical variants except Rush and None.
     */
    int getTacticalRadius();

    /**
     *
     * @param ignore True if the NPC goes through cobwebs
     */
    void setIgnoreCobweb(boolean ignore);

    /**
     *
     * @return True if the NPC goes through cobwebs
     */
    boolean getIgnoreCobweb();

    /**
     *
     * @return Changes what the NPC does when it finds an enemy based on the integer given.
     *          0 - Retaliate
     *          1 - Panic
     *          2 - Retreat
     *          3 - Nothing
     */
    void setOnFoundEnemy(int onAttack);

    /**
     *
     * @return Returns an integer representing what the NPC does when it finds an enemy.
     *          0 - Retaliate
     *          1 - Panic
     *          2 - Retreat
     *          3 - Nothing
     */
    int onFoundEnemy();

    /**
     *
     * @param shelterFrom An integer representing what conditions the NPC seeks shelter under.
     *          0 - Darkenss
     *          1 - Sunlight
     *          2 - Disabled
     */
    void setShelterFrom(int shelterFrom);

    /**
     *
     * @return Returns an integer representing what conditions the NPC seeks shelter under.
     *          0 - Darkenss
     *          1 - Sunlight
     *          2 - Disabled
     */
    int getShelterFrom();

    /**
     *
     * @return Whether the NPC has a living animation
     */
    boolean hasLivingAnimation();

    /**
     *
     * @param livingAnimation True if you want the NPC to have a living animation, false otherwise
     */
    void setLivingAnimation(boolean livingAnimation);

    /**
     * @param type The visibility type of the npc, 0:visible, 1:invisible, 2:semi-visible
     */
    void setVisibleType(int type);

    /**
     * @return The visibility type of the npc, 0:visible, 1:invisible, 2:semi-visible
     */
    int getVisibleType();

    /**
     *
     * @param player The player this NPC becomes visible/invisible to
     * @param visible True if you want the NPC to be invisible to the player, false otherwise
     */
    void setVisibleTo(IPlayer player, boolean visible);

    /**
     *
     * @param player The player this NPC is visible/invisible to
     * @return False if the NPC has been toggled to be invisible to this player, true otherwise.
     * If setVisibleTo(player,visible) was not called to make this NPC invisible to the player at any point
     * in the NPC's life, this function will return true regardless of the value of isVisible().
     */
    boolean isVisibleTo(IPlayer player);

    /**
     * @param type The visibility type of the name, 0:visible, 1:invisible, 2:when-attacking
     */
    void setShowName(int type);

    /**
     * @return Returns the visibility type of the name, 0:visible, 1:invisible, 2:when-attacking
     */
    int getShowName();

    /**
     * @return Returns the visiblity of the boss bar, 0:invisible, 1:visible, 2:when-attacking
     */
    int getShowBossBar();

    /**
     * @param type The visibility type of the boss bar, 0:invisible, 1:visible, 2:when-attacking
     */
    void setShowBossBar(int type);

    /**
     * @return The melee strength
     */
    float getMeleeStrength();

    /**
     * @param strength The melee strength
     */
    void setMeleeStrength(float strength);

    /**
     * @return The melee speed
     */
    int getMeleeSpeed();

    /**
     * @param speed The melee speed
     */
    void setMeleeSpeed(int speed);

    /**
     * @return The ranged strength
     */
    float getRangedStrength();

    /**
     * @param strength The ranged strength
     */
    void setRangedStrength(float strength);

    /**
     * @return The ranged speed
     */
    int getRangedSpeed();

    /**
     * @param speed The ranged speed
     */
    void setRangedSpeed(int speed);

    /**
     * @return The ranged burst count
     */
    int getRangedBurst();

    /**
     * @param count The ranged burst count
     */
    void setRangedBurst(int count);

    /**
     *
     * @return The amount of ticks before this entity respawns
     */
    int getRespawnTime();

    /**
     *
     * @param time The new amount of ticks before this entity respawns
     */
    void setRespawnTime(int time);

    /**
     *
     * @return Respawn: 0 - Yes (Always), 1 - Day, 2 - Night, 3 - No (Dies permanently)
     */
    int getRespawnCycle();

    /**
     *
     * @param cycle Sets when the NPC respawns. Respawn: 0 - Yes (Always), 1 - Day, 2 - Night, 3 - No (Dies permanently)
     */
    void setRespawnCycle(int cycle);

    /**
     *
     * @return Whether the NPC is hidden when it dies
     */
    boolean getHideKilledBody();

    /**
     *
     * @param hide True if the NPC is hidden when it dies. False otherwise.
     */
    void hideKilledBody(boolean hide);

    /**
     *
     * @return Returns true if the NPC naturally despawns.
     */
    boolean naturallyDespawns();

    /**
     *
     * @param canDespawn True if the NPC should naturally despawn. False otherwise.
     */
    void setNaturallyDespawns(boolean canDespawn);
    
    /**
     * @return true if this npc was spawned by a player using soulstone
     */
    boolean spawnedFromSoulStone();
    
    /**
     * @return the name of the player who spawned this npc using soulstone. null if not spawned by soulstone
     */
    String getSoulStonePlayerName();
    
    /**
     * @return true if npc was spawned by soul stone. becomes false after the init function is called.
     */
    boolean isSoulStoneInit();
    
    /**
     * @return does this npc refuse to be taken by soul stone 
     */
    boolean getRefuseSoulStone();
    
    /**
     * @param refuse set if this npc refuses to be taken by soul stone
     */
    void setRefuseSoulStone(boolean refuse);
    
    /**
     * requires SoulStoneFriendlyNPCs in config to be true
     * -1 by default
     * if -1, the minimum points are the faction's friendly points
     * @return the minimum faction points needed to soulstone this npc
     */
    int getMinPointsToSoulStone();
    
    /**
     * requires SoulStoneFriendlyNPCs in config to be true
     * -1 by default
     * if -1, the minimum points are the faction's friendly points
     * @param points the minimum faction points needed to soulstone this npc
     */
    void setMinPointsToSoulStone(int points);
    
    /**
     * @param player The player to give the item to
     * @param item The item given to the player
     */
    void giveItem(IPlayer player, IItemStack item);


    /**
     * On servers the enable-command-block option in the server.properties needs to be set to true
     * @param command The command to be executed
     */
    void executeCommand(String command);

    void setHeadScale(float x, float y, float z);

    void setBodyScale(float x, float y, float z);

    void setArmsScale(float x, float y, float z);

    void setLegsScale(float x, float y, float z);

    /**
     * @since 1.7.10c
     * @param resistance Explosion resistance (0-2) default is 1
     */
    void setExplosionResistance(float resistance);

    /**
     * @since 1.7.10c
     * @return Returns Explosion Resistance
     */
    float getExplosionResistance();

    /**
     * @param resistance Melee resistance (0-2) default is 1
     */
    void setMeleeResistance(float resistance);

    /**
     * @return Returns Melee Resistance
     */
    float getMeleeResistance();

    /**
     * @param resistance Arrow resistance (0-2) default is 1
     */
    void setArrowResistance(float resistance);

    /**
     * @return Returns Arrow Resistance
     */
    float getArrowResistance();

    /**
     * @param resistance Knockback resistance (0-2) default is 1
     */
    void setKnockbackResistance(float resistance);

    /**
     * @return Returns Knockback Resistance
     */
    float getKnockbackResistance();

    /**
     * @param type Retaliation type. 0:normal, 1:panic, 2:retreat, 3:nothing
     */
    void setRetaliateType(int type);

    /**
     * @return Returns the combat health regen per second
     */
    float getCombatRegen();

    /**
     * @param regen The combat health regen per second
     */
    void setCombatRegen(float regen);

    /**
     * @return Returns the health regen per second when not in combat
     */
    float getHealthRegen();

    /**
     * @param regen The health regen per second when not in combat
     */
    void setHealthRegen(float regen);

    long getAge();

    ITimers getTimers();

    void setFly(int fly);

    boolean canFly();

    void setFlySpeed(double flySpeed);

    double getFlySpeed(double flySpeed);

    void setFlyGravity(double flyGravity);

    double getFlyGravity(double flyGravity);

    void setFlyHeightLimit(int flyHeightLimit);

    int getFlyHeightLimit(int flyHeightLimit);

    void limitFlyHeight(boolean limit);

    boolean isFlyHeightLimited(boolean limit);

    void setSpeed(int speed);

    int getSpeed();

    void setSkinType(byte type);

    byte getSkinType();

    void setSkinUrl(String url);

    String getSkinUrl();

    void setCloakTexture(String cloakTexture);

    String getCloakTexture();

    void setOverlayTexture(String overlayTexture);

    String getOverlayTexture();

    IOverlayHandler getOverlays();

    void setCollisionType(int type);

    int getCollisionType();

    void updateClient();

    void updateAI();
}
