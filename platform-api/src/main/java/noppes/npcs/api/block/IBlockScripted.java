package noppes.npcs.api.block;

import noppes.npcs.api.IBlock;
import noppes.npcs.api.ITimers;
import noppes.npcs.api.item.IItemStack;

public interface IBlockScripted extends IBlock {

    /**
     * @param item The item to be set as model
     */
    void setModel(IItemStack item);

    void setModel(String name);

    IItemStack getModel();

    ITimers getTimers();

    /**
     * @param strength Sets the strength of the redstone signal (0-15)
     */
    void setRedstonePower(int strength);

    /**
     * @return Returns the current redstone power (0-15) this block is giving off
     */
    int getRedstonePower();

    void setIsLadder(boolean enabled);

    boolean getIsLadder();

    /**
     * @param value Sets the light value (0-15)
     */
    void setLight(int value);

    /**
     * @return Returns the light value (0-15)
     */
    int getLight();

    /**
     * @param x Scale x (0-10)
     * @param y Scale y (0-10)
     * @param z Scale z (0-10)
     */
    void setScale(float x, float y, float z);

    float getScaleX();

    float getScaleY();

    float getScaleZ();

    /**
     * @param x Rotation x (0-359)
     * @param y Rotation y (0-359)
     * @param z Rotation z (0-359)
     */
    void setRotation(int x, int y, int z);

    int getRotationX();

    int getRotationY();

    int getRotationZ();

    /**
     * On servers the enable-command-block option in the server.properties needs to be set to true <br>
     * Use /gamerule commandBlockOutput false/true to turn off/on command block feedback <br>
     * Setting NpcUseOpCommands to true in the CustomNPCs.cfg should allow the npc to run op commands, be warned this could be a major security risk, use at own risk <br>
     * For permission plugins the commands are run under uuid:c9c843f8-4cb1-4c82-aa61-e264291b7bd6 and name:[customnpcs]
     *
     * @param command The command to be executed
     */
    void executeCommand(String command);

    /**
     * TYPO VERSION
     * @return true if entities can pass through this block
     */
    boolean getIsPassible();

    /**
     * TYPO VERSION
     * @param bo whether entities can pass through this block
     */
    void setIsPassible(boolean bo);

    boolean getIsPassable();

    void setIsPassable(boolean bo);

    /**
     * @return Harvesting hardness (-1 makes it unharvestable)
     */
    float getHardness();

    void setHardness(float hardness);

    /**
     * @return Explosion resistance (-1 makes it unexplodable)
     */
    float getResistance();

    void setResistance(float resistance);

    ITextPlane getTextPlane();

    ITextPlane getTextPlane2();

    ITextPlane getTextPlane3();

    ITextPlane getTextPlane4();

    ITextPlane getTextPlane5();

    ITextPlane getTextPlane6();

    void setStoredData(String key, Object value);

    Object getStoredData(String key);

    void removeStoredData(String key);

    boolean hasStoredData(String key);

    void clearStoredData();

    String[] getStoredDataKeys();

    void removeTempData(String key);

    void setTempData(String key, Object value);

    boolean hasTempData(String key);

    Object getTempData(String key);

    void clearTempData();

    String[] getTempDataKeys();
}
