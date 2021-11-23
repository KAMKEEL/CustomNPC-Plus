//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package noppes.npcs.scripted.interfaces;

import net.minecraft.entity.EntityCreature;
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
    public void setMaxHealth(int health);

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
     * @param type The AnimationType
     */
    public void setAnimation(int type);

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
    public int getMeleeStrength();

    /**
     * @param strength The melee strength
     */
    public void setMeleeStrength(int strength);

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
    public int getRangedStrength();

    /**
     * @param strength The ranged strength
     */
    public void setRangedStrength(int strength);

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
    public int getCombatRegen();

    /**
     * @param regen The combat health regen per second
     */
    public void setCombatRegen(int regen);

    /**
     * @return Returns the health regen per second when not in combat
     */
    public int getHealthRegen();

    /**
     * @param regen The health regen per second when not in combat
     */
    public void setHealthRegen(int regen);

    public long getAge();
}
