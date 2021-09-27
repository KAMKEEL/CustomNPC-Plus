package noppes.npcs.scripted;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.Achievement;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatList;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.WorldSettings;
import noppes.npcs.CustomNpcsPermissions;
import noppes.npcs.NoppesStringUtils;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.Server;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.controllers.*;
import noppes.npcs.scripted.constants.EntityType;
import noppes.npcs.util.ValueUtil;

public class ScriptPlayer extends ScriptLivingBase {
    protected EntityPlayerMP player;

    public ScriptPlayer(EntityPlayerMP player) {
        super(player);
        this.player = player;
    }

    /**
     * @return Returns the displayed name of the player
     */
    public String getDisplayName() {
        return player.getDisplayName();
    }

    /**
     * @return Returns the players name
     */
    public String getName() {
        return player.getCommandSenderName();
    }

    @Override
    public void setPosition(double x, double y, double z) {
        NoppesUtilPlayer.teleportPlayer(player, x, y, z, player.dimension);
    }

    public boolean hasFinishedQuest(int id) {
        PlayerQuestData data = PlayerDataController.instance.getPlayerData(player).questData;
        return data.finishedQuests.containsKey(id);
    }

    public boolean hasActiveQuest(int id) {
        PlayerQuestData data = PlayerDataController.instance.getPlayerData(player).questData;
        return data.activeQuests.containsKey(id);
    }

    public boolean hasReadDialog(int id) {
        PlayerDialogData data = PlayerDataController.instance.getPlayerData(player).dialogData;
        return data.dialogsRead.contains(id);
    }

    /**
     * Add the quest from active quest list
     *
     * @param id The Quest ID
     */
    public void startQuest(int id) {
        Quest quest = QuestController.instance.quests.get(id);
        if (quest == null)
            return;
        PlayerData data = PlayerDataController.instance.getPlayerData(player);
        if (data.questData.activeQuests.containsKey(id))
            return;
        QuestData questdata = new QuestData(quest);
        data.questData.activeQuests.put(id, questdata);
        Server.sendData((EntityPlayerMP) player, EnumPacketClient.MESSAGE, "quest.newquest", quest.title);
        Server.sendData((EntityPlayerMP) player, EnumPacketClient.CHAT, "quest.newquest", ": ", quest.title);
    }

    /**
     * Add the quest from finished quest list
     *
     * @param id The Quest ID
     */
    public void finishQuest(int id) {
        Quest quest = QuestController.instance.quests.get(id);
        if (quest == null)
            return;
        PlayerData data = PlayerDataController.instance.getPlayerData(player);
        data.questData.finishedQuests.put(id, System.currentTimeMillis());
    }

    /**
     * Removes the quest from active quest list
     *
     * @param id The Quest ID
     */
    public void stopQuest(int id) {
        Quest quest = QuestController.instance.quests.get(id);
        if (quest == null)
            return;
        PlayerData data = PlayerDataController.instance.getPlayerData(player);
        data.questData.activeQuests.remove(id);
    }

    /**
     * Removes the quest from active and finished quest list
     *
     * @param id The Quest ID
     */
    public void removeQuest(int id) {
        Quest quest = QuestController.instance.quests.get(id);
        if (quest == null)
            return;
        PlayerData data = PlayerDataController.instance.getPlayerData(player);
        data.questData.activeQuests.remove(id);
        data.questData.finishedQuests.remove(id);
    }

    @Override
    public int getType() {
        return EntityType.PLAYER;
    }

    @Override
    public boolean typeOf(int type) {
        return type == EntityType.PLAYER || super.typeOf(type);
    }

    /**
     * @param faction The faction id
     * @param points  The points to increase. Use negative values to decrease
     */
    public void addFactionPoints(int faction, int points) {
        PlayerData data = PlayerDataController.instance.getPlayerData(player);
        data.factionData.increasePoints(faction, points);
    }

    /**
     * @param faction The faction id
     * @return points
     */
    public int getFactionPoints(int faction) {
        PlayerData data = PlayerDataController.instance.getPlayerData(player);
        return data.factionData.getFactionPoints(faction);
    }

    /**
     * @param message The message you want to send
     */
    public void sendMessage(String message) {
        player.addChatMessage(new ChatComponentTranslation(NoppesStringUtils.formatText(message, player)));
    }

    /**
     * @return Return gamemode. 0: Survival, 1: Creative, 2: Adventure
     */
    public int getMode() {
        return player.theItemInWorldManager.getGameType().getID();
    }

    /**
     * @param type The gamemode type. 0:SURVIVAL, 1:CREATIVE, 2:ADVENTURE
     */
    public void setMode(int type) {
        player.setGameType(WorldSettings.getGameTypeById(type));
    }

    /**
     * @param item The item to be checked
     * @return How many of this item the player has
     */
    public int inventoryItemCount(ScriptItemStack item) {
        int i = 0;
        for (ItemStack is : player.inventory.mainInventory) {
            if (is != null && is.isItemEqual(item.item))
                i += is.stackSize;
        }
        return i;
    }

    public int inventoryItemCount(String id, int damage) {
        Item item = (Item) Item.itemRegistry.getObject(id);
        if (item == null) {
            throw new CustomNPCsException("Unknown item id: " + id);
        } else {
            return this.inventoryItemCount(new ScriptItemStack(new ItemStack(item, 1, damage)));
        }
    }

    /**
     * @return Returns a IItemStack array size 36
     * @since 1.7.10d
     */
    public ScriptItemStack[] getInventory() {
        ScriptItemStack[] items = new ScriptItemStack[36];
        for (int i = 0; i < player.inventory.mainInventory.length; i++) {
            ItemStack item = player.inventory.mainInventory[i];
            if (item != null)
                items[i] = new ScriptItemStack(item);
        }
        return items;
    }

    /**
     * @param item   The Item type to be removed
     * @param amount How many will be removed
     * @return Returns true if the items were removed succesfully. Returns false incase a bigger amount than what the player has was given
     */
    public boolean removeItem(ScriptItemStack item, int amount) {
        int count = inventoryItemCount(item);
        if (amount > count) {
            return false;
        } else {
            if (count == amount) {
                this.removeAllItems(item);
            } else {
                for (int i = 0; i < ((EntityPlayerMP) this.entity).inventory.getSizeInventory(); ++i) {
                    ItemStack is = ((EntityPlayerMP) this.entity).inventory.getStackInSlot(i);
                    if (is != null && this.isItemEqual(item.getMCItemStack(), is)) {
                        if (amount < is.stackSize) {
                            is.splitStack(amount);
                            break;
                        }

                        player.inventory.setInventorySlotContents(i, null);
                        amount -= is.stackSize;
                    }
                }
            }
            this.updatePlayerInventory();
            return true;
        }
    }

    private boolean isItemEqual(ItemStack stack, ItemStack other) {
        return other.stackSize >= 1 && (stack.getItem() == other.getItem() && (stack.getItemDamageForDisplay() < 0 || stack.getItemDamageForDisplay() == other.getItemDamageForDisplay()));
    }

    /**
     * @param id     The items name
     * @param damage The damage value
     * @param amount How many will be removed
     * @return Returns true if the items were removed succesfully. Returns false incase a bigger amount than what the player has was given or item doesnt exist
     * @since 1.7.10c
     */
    public boolean removeItem(String id, int damage, int amount) {
        Item item = (Item) Item.itemRegistry.getObject(id);
        if (item == null) {
            throw new CustomNPCsException("Unknown item id: " + id, new Object[0]);
        }
        return removeItem(new ScriptItemStack(new ItemStack(item, 1, damage)), amount);
    }

    /**
     * @param item   Item to be added
     * @param amount The amount of the item to be added
     * @return Returns whether or not it gave the item succesfully
     * @since 1.7.10c
     */
    public boolean giveItem(ScriptItemStack item, int amount) {
        if (item != null && item.getMCItemStack() != null && amount > 0) {
            item.setStackSize(amount);
            ItemStack mcItemStack = item.getMCItemStack();
            boolean bool = player.inventory.addItemStackToInventory(mcItemStack);
            if (bool) {
                updatePlayerInventory();
            }
            return bool;
        } else {
            return false;
        }
    }

    /**
     * @param id     The items name
     * @param damage The damage value
     * @param amount The amount of the item to be added
     * @return Returns whether or not it gave the item succesfully
     * @since 1.7.10c
     */
    public boolean giveItem(String id, int damage, int amount) {
        Item item = (Item) Item.itemRegistry.getObject(id);
        if (item == null) {
            throw new CustomNPCsException("Unknown item id: " + id, new Object[0]);
        }

        ItemStack itemStack = new ItemStack(item, amount, damage);
        boolean bool = player.inventory.addItemStackToInventory(itemStack);
        if (bool) {
            updatePlayerInventory();
        }

        return bool;
    }

    /**
     * Same as the /spawnpoint command
     *
     * @param x The x position
     * @param y The y position
     * @param z The z position
     */
    public void setSpawnpoint(int x, int y, int z) {
        x = ValueUtil.CorrectInt(x, -30000000, 30000000);
        z = ValueUtil.CorrectInt(z, -30000000, 30000000);
        y = ValueUtil.CorrectInt(y, 0, 256);
        player.setSpawnChunk(new ChunkCoordinates(x, y, z), true);
    }

    public void resetSpawnpoint() {
        player.setSpawnChunk(null, false);
    }

    /**
     * @param item The item to be removed from the players inventory
     */
    public void removeAllItems(ScriptItemStack item) {
        for (int i = 0; i < player.inventory.mainInventory.length; i++) {
            ItemStack is = player.inventory.mainInventory[i];
            if (is != null && is.isItemEqual(item.item))
                player.inventory.mainInventory[i] = null;
        }
        updatePlayerInventory();
    }


    /**
     * @param achievement The achievement id. For a complete list see http://minecraft.gamepedia.com/Achievements
     * @return Returns whether or not the player has this achievement
     */
    public boolean hasAchievement(String achievement) {
        StatBase statbase = StatList.func_151177_a(achievement);
        if (!(statbase instanceof Achievement)) {
            return false;
        }
        return player.func_147099_x().hasAchievementUnlocked((Achievement) statbase);
    }

    /**
     * @param permission Bukkit/Cauldron permission
     * @return Returns whether or not the player has the permission
     */
    public boolean hasBukkitPermission(String permission) {
        return CustomNpcsPermissions.hasPermissionString(player, permission);
    }

    /**
     * @return Returns the exp level
     * @since 1.7.10c
     */
    public int getExpLevel() {
        return player.experienceLevel;
    }

    /**
     * @param level The new exp level you want to set
     * @since 1.7.10c
     */
    public void setExpLevel(int level) {
        player.experienceLevel = level;
        player.addExperienceLevel(0);
    }

    /**
     * Requires pixelmon to be installed
     *
     * @since 1.7.10d
     */
    public ScriptPixelmonPlayerData getPixelmonData() {
        if (!PixelmonHelper.Enabled)
            return null;
        return new ScriptPixelmonPlayerData(player);
    }

    public void updatePlayerInventory() {
        ((EntityPlayerMP) this.entity).inventoryContainer.detectAndSendChanges();
    }
}
