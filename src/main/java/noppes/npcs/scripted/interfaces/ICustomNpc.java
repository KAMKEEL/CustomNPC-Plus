//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package noppes.npcs.scripted.interfaces;

import net.minecraft.entity.EntityCreature;
import noppes.npcs.constants.EnumNavType;
import noppes.npcs.scripted.ScriptFaction;
import noppes.npcs.scripted.ScriptItemStack;
import noppes.npcs.scripted.entity.ScriptLivingBase;
import noppes.npcs.scripted.entity.ScriptPlayer;
import noppes.npcs.scripted.roles.*;

public interface ICustomNpc<T extends EntityCreature> extends IEntityLiving<T> {
    /**
     * @return Returns the current npcs size 1-30
     */
    public int getSize();

    /**
     * @param size The size of the npc (1-30) default is 5
     */
    public void setSize(int size);

    /**
     * @return Returns the current npcs modelType of the NPC
     */
    public int getModelType();

    /**
     * @param modelType The modelType of the NPC. 0: Steve, 1: Steve64, 2: Alex
     */
    public void setModelType(int modelType);

    /**
     * @return The npcs name
     */
    public String getName();

    public void setRotation(float rotation);
    /**
     * @param name The name of the npc
     */
    public void setName(String name);

    /**
     * @since 1.7.10
     * @return The npcs title
     */
    public String getTitle();

    /**
     * @since 1.7.10
     * @param title The title of the npc
     */
    public void setTitle(String title);

    /**
     * @return The npcs texture
     */
    public String getTexture();

    /**
     * @param texture The texture of the npc
     */
    public void setTexture(String texture);

    /**
     * @return Home position x
     */
    public int getHomeX();

    /**
     * @param x The home x position
     */
    public void setHomeX(int x);

    /**
     * @return Home position x
     */
    public int getHomeY();

    /**
     * @param y The home y position
     */
    public void setHomeY(int y);

    /**
     * @return Home position x
     */
    public int getHomeZ();

    /**
     * @param z The home x position
     */
    public void setHomeZ(int z);

    /**
     * @param x The home x position
     * @param y The home y position
     * @param z The home z position
     */
    public void setHome(int x, int y, int z);
    /**
     * @param health New max health
     */
    public void setMaxHealth(double health);

    /**
     * @param bo Whether or not the npc will try to return to his home position
     */
    public void setReturnToHome(boolean bo);

    /**
     * @return Whether or not the npc returns home
     */
    public boolean getReturnToHome();

    /**
     * @return The faction of the npc
     */
    public ScriptFaction getFaction();

    /**
     * @param id The id of the new faction
     */
    public void setFaction(int id);

    /**
     *
     * @param attackOtherFactions True if you want the NPC to attack other factions, false otherwise.
     */
    public void setAttackFactions(boolean attackOtherFactions);

    /**
     *
     * @return Returns true if the NPC attacks other factions, false otherwise.
     */
    public boolean getAttackFactions();

    /**
     *
     * @param defendFaction True if the NPC should defend faction members, false otherwise.
     */
    public void setDefendFaction(boolean defendFaction);

    /**
     *
     * @return Returns true if the NPC should defend faction members, false otherwise.
     */
    public boolean getDefendFaction();

    public int getType();

    public boolean typeOf(int type);

    /**
     * @param target The targeted npc
     * @param item The item you want to shoot
     * @param accuracy Accuracy of the shot (0-100)
     */
    public void shootItem(ScriptLivingBase target, ScriptItemStack item, int accuracy);

    /**
     * @param message The message the npc will say
     */
    public void say(String message);

    /**
     * @param message The message the npc will say
     */
    public void say(ScriptPlayer player, String message);

    /**
     * Kill the npc, doesnt't despawn it
     */
    public void kill();

    /**
     * Basically completely resets the npc. This will also call the Init script
     */
    public void reset();

    /**
     * @return Returns the npcs current role
     */
    public ScriptRoleInterface getRole();

    /**
     * @return Returns the npcs current job
     */
    public ScriptJobInterface getJob();

    /**
     * @return The item held in the right hand
     */
    public ScriptItemStack getRightItem();

    /**
     * @param item Item to be held in the right hand
     */
    public void setRightItem(ScriptItemStack item);

    /**
     * @return The item held in the left hand
     */
    public ScriptItemStack getLefttItem();

    /**
     * @param item Item to be held in the left hand
     */
    public void setLeftItem(ScriptItemStack item);

    /**
     * @return Returns the projectile the npc uses
     */
    public ScriptItemStack getProjectileItem();

    /**
     * @param item Item to be used as projectile
     */
    public void setProjectileItem(ScriptItemStack item);

    /**
     *
     * @return Returns true if the NPC can aim while shooting.
     */
    public boolean canAimWhileShooting();

    /**
     *
     * @param aimWhileShooting Set to true if you want the NPC to aim while shooting, false otherwise.
     */
    public void aimWhileShooting(boolean aimWhileShooting);

    /**
     *
     * @return The directory of the sound that plays when a projectile is shot
     */
    public String getFireSound();

    /**
     *
     * @param fireSound The new directory of the sound that plays when a projectile is shot
     */
    public void setFireSound(String fireSound);

    /**
     * @param slot The armor slot to return. 0:head, 1:body, 2:legs, 3:boots
     * @return Returns the worn armor in slot
     */
    public ScriptItemStack getArmor(int slot);

    /**
     * @param slot The armor slot to set. 0:head, 1:body, 2:legs, 3:boots
     * @param item Item to be set as armor
     */
    public void setArmor(int slot, ScriptItemStack item);

    /**
     *
     * @param slot The slot from the NPC's drop list to return (0-8)
     * @return The item in the NPC's drop list slot
     */
    public ScriptItemStack getLootItem(int slot);

    /**
     *
     * @param slot The slot from the NPC's drop list to change
     * @param item The item the drop list slot will be changed to
     */
    public void setLootItem(int slot, ScriptItemStack item);

    /**
     *
     * @param slot The slot from the NPC's drop list to return (0-8)
     * @return The chance of dropping the item in this slot. Returns 100 if the slot is not found.
     */
    public int getLootChance(int slot);

    /**
     *
     * @param slot The slot from the NPC's drop list to change
     * @param chance The new chance of dropping the item in this slot
     */
    public void setLootChance(int slot, int chance);

    /**
     *
     * @return The NPC's loot mode. 0 = Normal, 1 = Auto Pickup
     */
    public int getLootMode();

    /**
     *
     * @param lootMode The NPC's loot mode. 0 = Normal, 1 = Auto Pickup
     */
    public void setLootMode(int lootMode);

    /**
     *
     * @param lootXP The new minimum XP gained from killing the NPC. If greater than the max XP, it will be set to it.
     */
    public void setMinLootXP(int lootXP);

    /**
     *
     * @param lootXP The new maximum XP gained from killing the NPC. If less than the min XP, it will be set to it.
     */
    public void setMaxLootXP(int lootXP);

    /**
     *
     * @return The minimum XP gained from killing the NPC.
     */
    public int getMinLootXP();

    /**
     *
     * @return The maximum XP gained from killing the NPC.
     */
    public int getMaxLootXP();

    /**
     * @param type The AnimationType
     */
    public void setAnimation(int type);

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
    public void setTacticalVariant(int variant);

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
    public int getTacticalVariant();

    /**
     *
     * @param variant Sets the NPC's tactical variant by name.
     */
    public void setTacticalVariant(String variant);

    /**
     *
     * @return Returns the name of the NPc's tactical variant.
     */
    public String getTacticalVariantName();

    /**
     *
     * @param tacticalRadius Sets the radius in which the tactical variant is affected, if any. Effective for all tactical variants except Rush and None.
     */
    public void setTacticalRadius(int tacticalRadius);

    /**
     *
     * @return Gets the radius in which the tactical variant is affected, if any. Effective for all tactical variants except Rush and None.
     */
    public int getTacticalRadius();

    /**
     *
     * @param ignore True if the NPC goes through cobwebs
     */
    public void setIgnoreCobweb(boolean ignore);

    /**
     *
     * @return True if the NPC goes through cobwebs
     */
    public boolean getIgnoreCobweb();

    /**
     *
     * @return Changes what the NPC does when it finds an enemy based on the integer given.
     *          0 - Retaliate
     *          1 - Panic
     *          2 - Retreat
     *          3 - Nothing
     */
    public void setOnFoundEnemy(int onAttack);

    /**
     *
     * @return Returns an integer representing what the NPC does when it finds an enemy.
     *          0 - Retaliate
     *          1 - Panic
     *          2 - Retreat
     *          3 - Nothing
     */
    public int onFoundEnemy();

    /**
     *
     * @param shelterFrom An integer representing what conditions the NPC seeks shelter under.
     *          0 - Darkenss
     *          1 - Sunlight
     *          2 - Disabled
     */
    public void setShelterFrom(int shelterFrom);

    /**
     *
     * @return Returns an integer representing what conditions the NPC seeks shelter under.
     *          0 - Darkenss
     *          1 - Sunlight
     *          2 - Disabled
     */
    public int getShelterFrom();

    /**
     *
     * @return Whether the NPC has a living animation
     */
    public boolean hasLivingAnimation();

    /**
     *
     * @param livingAnimation True if you want the NPC to have a living animation, false otherwise
     */
    public void setLivingAnimation(boolean livingAnimation);

    /**
     * @param type The visibility type of the npc, 0:visible, 1:invisible, 2:semi-visible
     */
    public void setVisibleType(int type);

    /**
     * @return The visibility type of the npc, 0:visible, 1:invisible, 2:semi-visible
     */
    public int getVisibleType();

    /**
     *
     * @param player The player this NPC becomes visible/invisible to
     * @param visible True if you want the NPC to be invisible to the player, false otherwise
     */
    public void setVisibleTo(ScriptPlayer player, boolean visible);

    /**
     *
     * @param player The player this NPC is visible/invisible to
     * @return False if the NPC has been toggled to be invisible to this player, true otherwise.
     * If setVisibleTo(player,visible) was not called to make this NPC invisible to the player at any point
     * in the NPC's life, this function will return true regardless of the value of isVisible().
     */
    public boolean isVisibleTo(ScriptPlayer player);

    /**
     * @param type The visibility type of the name, 0:visible, 1:invisible, 2:when-attacking
     */
    public void setShowName(int type);

    /**
     * @return Returns the visibility type of the name, 0:visible, 1:invisible, 2:when-attacking
     */
    public int getShowName();

    /**
     * @return Returns the visiblity of the boss bar, 0:invisible, 1:visible, 2:when-attacking
     */
    public int getShowBossBar();

    /**
     * @param type The visibility type of the boss bar, 0:invisible, 1:visible, 2:when-attacking
     */
    public void setShowBossBar(int type);

    /**
     * @return The melee strength
     */
    public float getMeleeStrength();

    /**
     * @param strength The melee strength
     */
    public void setMeleeStrength(float strength);

    /**
     * @return The melee speed
     */
    public int getMeleeSpeed();

    /**
     * @param speed The melee speed
     */
    public void setMeleeSpeed(int speed);

    /**
     * @return The ranged strength
     */
    public float getRangedStrength();

    /**
     * @param strength The ranged strength
     */
    public void setRangedStrength(float strength);

    /**
     * @return The ranged speed
     */
    public int getRangedSpeed();

    /**
     * @param speed The ranged speed
     */
    public void setRangedSpeed(int speed);

    /**
     * @return The ranged burst count
     */
    public int getRangedBurst();

    /**
     * @param count The ranged burst count
     */
    public void setRangedBurst(int count);

    /**
     *
     * @return The amount of ticks before this entity respawns
     */
    public int getRespawnTime();

    /**
     *
     * @param time The new amount of ticks before this entity respawns
     */
    public void setRespawnTime(int time);

    /**
     *
     * @return Respawn: 0 - Yes (Always), 1 - Day, 2 - Night, 3 - No (Dies permanently)
     */
    public int getRespawnCycle();

    /**
     *
     * @param cycle Sets when the NPC respawns. Respawn: 0 - Yes (Always), 1 - Day, 2 - Night, 3 - No (Dies permanently)
     */
    public void setRespawnCycle(int cycle);

    /**
     *
     * @return Whether the NPC is hidden when it dies
     */
    public boolean getHideKilledBody();

    /**
     *
     * @param hide True if the NPC is hidden when it dies. False otherwise.
     */
    public void hideKilledBody(boolean hide);

    /**
     *
     * @return Returns true if the NPC naturally despawns.
     */
    public boolean naturallyDespawns();

    /**
     *
     * @param canDespawn True if the NPC should naturally despawn. False otherwise.
     */
    public void setNaturallyDespawns(boolean canDespawn);

    /**
     * @param player The player to give the item to
     * @param item The item given to the player
     */
    public void giveItem(ScriptPlayer player, ScriptItemStack item);


    /**
     * On servers the enable-command-block option in the server.properties needs to be set to true
     * @param command The command to be executed
     */
    public void executeCommand(String command);

    public void setHeadScale(float x, float y, float z);

    public void setBodyScale(float x, float y, float z);

    public void setArmsScale(float x, float y, float z);

    public void setLegsScale(float x, float y, float z);

    /**
     * @since 1.7.10c
     * @param resistance Explosion resistance (0-2) default is 1
     */
    public void setExplosionResistance(float resistance);

    /**
     * @since 1.7.10c
     * @return Returns Explosion Resistance
     */
    public float getExplosionResistance();

    /**
     * @param resistance Melee resistance (0-2) default is 1
     */
    public void setMeleeResistance(float resistance);

    /**
     * @return Returns Melee Resistance
     */
    public float getMeleeResistance();

    /**
     * @param resistance Arrow resistance (0-2) default is 1
     */
    public void setArrowResistance(float resistance);

    /**
     * @return Returns Arrow Resistance
     */
    public float getArrowResistance();

    /**
     * @param resistance Knockback resistance (0-2) default is 1
     */
    public void setKnockbackResistance(float resistance);

    /**
     * @return Returns Knockback Resistance
     */
    public float getKnockbackResistance();

    /**
     * @param type Retaliation type. 0:normal, 1:panic, 2:retreat, 3:nothing
     */
    public void setRetaliateType(int type);

    /**
     * @return Returns the combat health regen per second
     */
    public float getCombatRegen();

    /**
     * @param regen The combat health regen per second
     */
    public void setCombatRegen(float regen);

    /**
     * @return Returns the health regen per second when not in combat
     */
    public float getHealthRegen();

    /**
     * @param regen The health regen per second when not in combat
     */
    public void setHealthRegen(float regen);

    public long getAge();

    public boolean canFly();

    public void setFly(int fly);

    public void setSkinType(byte type);

    public byte getSkinType();

    public void setSkinUrl(String url);

    public String getSkinUrl();

    public void setCloakTexture(String cloakTexture);

    public String getCloakTexture();

    public void setOverlayTexture(String overlayTexture);

    public String getOverlayTexture();
}
