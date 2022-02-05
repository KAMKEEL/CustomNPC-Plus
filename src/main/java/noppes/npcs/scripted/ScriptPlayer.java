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
import noppes.npcs.controllers.PixelmonHelper;
import noppes.npcs.controllers.PlayerData;
import noppes.npcs.controllers.PlayerDataController;
import noppes.npcs.controllers.PlayerDialogData;
import noppes.npcs.controllers.PlayerQuestData;
import noppes.npcs.controllers.Quest;
import noppes.npcs.controllers.QuestController;
import noppes.npcs.controllers.QuestData;
import noppes.npcs.scripted.constants.EntityType;
import noppes.npcs.util.ValueUtil;

public class ScriptPlayer extends ScriptLivingBase{
	protected EntityPlayerMP player;
	public ScriptPlayer(EntityPlayerMP player){
		super(player);
		this.player = player;
	}
	
	/**
	 * @return Returns the displayed name of the player
	 */
	public String getDisplayName(){
		return player.getDisplayName();
	}	
	
	/**
	 * @return Returns the players name
	 */
	public String getName(){
		return player.getCommandSenderName();
	}
	
	@Override
	public void setPosition(double x, double y, double z){
		NoppesUtilPlayer.teleportPlayer(player, x, y, z, player.dimension);
	}
	
	public boolean hasFinishedQuest(int id){
		PlayerQuestData data = PlayerDataController.instance.getPlayerData(player).questData;
		return data.finishedQuests.containsKey(id);
	}
	
	public boolean hasActiveQuest(int id){
		PlayerQuestData data = PlayerDataController.instance.getPlayerData(player).questData;
		return data.activeQuests.containsKey(id);
	}
	
	public boolean hasReadDialog(int id){
		PlayerDialogData data = PlayerDataController.instance.getPlayerData(player).dialogData;
		return data.dialogsRead.contains(id);
	}

	/**
	 * Add the quest from active quest list
	 * @param id The Quest ID
	 */
	public void startQuest(int id){
        Quest quest = QuestController.instance.quests.get(id);
        if (quest == null)
        	return;
		PlayerData data = PlayerDataController.instance.getPlayerData(player);
        if(data.questData.activeQuests.containsKey(id))
        	return;
        QuestData questdata = new QuestData(quest);        
        data.questData.activeQuests.put(id, questdata);
		Server.sendData((EntityPlayerMP)player, EnumPacketClient.MESSAGE, "quest.newquest", quest.title);
		Server.sendData((EntityPlayerMP)player, EnumPacketClient.CHAT, "quest.newquest", ": ", quest.title);
	}

	/**
	 * Add the quest from finished quest list
	 * @param id The Quest ID
	 */
	public void finishQuest(int id){
        Quest quest = QuestController.instance.quests.get(id);
        if (quest == null)
        	return;
		PlayerData data = PlayerDataController.instance.getPlayerData(player);
		data.questData.finishedQuests.put(id, System.currentTimeMillis());  
	}

	/**
	 * Removes the quest from active quest list
	 * @param id The Quest ID
	 */
	public void stopQuest(int id){
        Quest quest = QuestController.instance.quests.get(id);
        if (quest == null)
        	return;
		PlayerData data = PlayerDataController.instance.getPlayerData(player);
		data.questData.activeQuests.remove(id);
	}

	/**
	 * Removes the quest from active and finished quest list
	 * @param id The Quest ID
	 */
	public void removeQuest(int id){
        Quest quest = QuestController.instance.quests.get(id);
        if (quest == null)
        	return;
		PlayerData data = PlayerDataController.instance.getPlayerData(player);
		data.questData.activeQuests.remove(id);
		data.questData.finishedQuests.remove(id);
	}

	@Override
	public int getType(){
		return EntityType.PLAYER;
	}

	@Override
	public boolean typeOf(int type){
		return type == EntityType.PLAYER?true:super.typeOf(type);
	}
	/**
	 * @param faction The faction id
	 * @param points The points to increase. Use negative values to decrease
	 */
	public void addFactionPoints(int faction, int points){
		PlayerData data = PlayerDataController.instance.getPlayerData(player);
		data.factionData.increasePoints(faction, points);
	}
        
    /**         
     * @param faction The faction id
     * @return  points
     */
	public int getFactionPoints(int faction) {
		PlayerData data = PlayerDataController.instance.getPlayerData(player);
		return data.factionData.getFactionPoints(faction);
	}
	
	/**
	 * @param message The message you want to send
	 */
	public void sendMessage(String message){
		player.addChatMessage(new ChatComponentTranslation(NoppesStringUtils.formatText(message,player)));
	}
	
	/**
	 * @return Return gamemode. 0: Survival, 1: Creative, 2: Adventure
	 */
	public int getMode(){
		return player.theItemInWorldManager.getGameType().getID();
	}
	
	/**
	 * @param type The gamemode type. 0:SURVIVAL, 1:CREATIVE, 2:ADVENTURE
	 */
	public void setMode(int type){
		player.setGameType(WorldSettings.getGameTypeById(type));
	}
	
	/**
	 * @param item The item to be checked
	 * @return How many of this item the player has
	 */
	public int inventoryItemCount(ScriptItemStack item){
		int i = 0;
		for(ItemStack is : player.inventory.mainInventory){
            if (is != null && is.isItemEqual(item.item))
            	i += is.stackSize;
		}
		return i;
	}
	
	/**
	 * @since 1.7.10d
	 * @return Returns a IItemStack array size 36
	 */
	public ScriptItemStack[] getInventory(){
		ScriptItemStack[] items = new ScriptItemStack[36];
		for(int i = 0; i < player.inventory.mainInventory.length; i++){
			ItemStack item = player.inventory.mainInventory[i];
			if(item != null)
				items[i] = new ScriptItemStack(item);
		}
		return items;
	}
	
	/**
	 * @param item The Item type to be removed
	 * @param amount How many will be removed
	 * @return Returns true if the items were removed succesfully. Returns false incase a bigger amount than what the player has was given
	 */
	public boolean removeItem(ScriptItemStack item, int amount){
		int count = inventoryItemCount(item);
		if(amount  > count)
			return false;
		else if(count == amount)
			removeAllItems(item);
		else{
			for(int i = 0; i < player.inventory.mainInventory.length; i++){
				ItemStack is = player.inventory.mainInventory[i];
	            if (is != null && is.isItemEqual(item.item)){
	            	if(amount > is.stackSize){
	                	player.inventory.mainInventory[i] = null;
	                	amount -= is.stackSize;
	            	}
	            	else{
	            		is.splitStack(amount);
	            		break;
	            	}
	            }
			}
		}
		return true;
	}

	/**
	 * @since 1.7.10c
	 * @param id The items name
	 * @param damage The damage value 
	 * @param amount How many will be removed
	 * @return Returns true if the items were removed succesfully. Returns false incase a bigger amount than what the player has was given or item doesnt exist
	 */
	public boolean removeItem(String id, int damage, int amount){
		Item item = (Item)Item.itemRegistry.getObject(id);
		if(item == null)
			return false;		
		return removeItem(new ScriptItemStack(new ItemStack(item, 1, damage)), amount);
	}

	/**
	 * @since 1.7.10c
	 * @param item Item to be added
	 * @param amount The amount of the item to be added
	 * @return Returns whether or not it gave the item succesfully
	 */
	public boolean giveItem(ScriptItemStack item, int amount){
		String itemname = Item.itemRegistry.getNameForObject(item.getMCItemStack().getItem());
		return giveItem(itemname, item.getItemDamage(), amount);
	}
	
	/**
	 * @since 1.7.10c
	 * @param id The items name
	 * @param damage The damage value
	 * @param amount The amount of the item to be added
	 * @return Returns whether or not it gave the item succesfully
	 */
	public boolean giveItem(String id, int damage, int amount){
		Item item = (Item)Item.itemRegistry.getObject(id);
		if(item == null)
			return false;		
		return player.inventory.addItemStackToInventory(new ItemStack(item, amount, damage));
	}
	
	/**
	 * Same as the /spawnpoint command
	 * @param x The x position
	 * @param y The y position
	 * @param z The z position
	 */
	public void setSpawnpoint(int x, int y, int z){
		x = ValueUtil.CorrectInt(x, -30000000, 30000000);
		z = ValueUtil.CorrectInt(z, -30000000, 30000000);
		y = ValueUtil.CorrectInt(y, 0, 256);
        player.setSpawnChunk(new ChunkCoordinates(x, y, z), true);
	}
	
	public void resetSpawnpoint(){
		player.setSpawnChunk(null, false);
	}
		
	/**
	 * @param item The item to be removed from the players inventory
	 */
	public void removeAllItems(ScriptItemStack item){
		for(int i = 0; i < player.inventory.mainInventory.length; i++){
			ItemStack is = player.inventory.mainInventory[i];
            if (is != null && is.isItemEqual(item.item))
            	player.inventory.mainInventory[i] = null;
		}
	}
	
	
	/**
	 * @param achievement The achievement id. For a complete list see http://minecraft.gamepedia.com/Achievements
	 * @return Returns whether or not the player has this achievement
	 */
	public boolean hasAchievement(String achievement){
        StatBase statbase = StatList.func_151177_a(achievement);
        if(statbase == null || !(statbase instanceof Achievement)){
        	return false;
        }
		return player.func_147099_x().hasAchievementUnlocked((Achievement) statbase);
	}
	
	/**
	 * @param permission Bukkit/Cauldron permission
	 * @return Returns whether or not the player has the permission
	 */
	public boolean hasBukkitPermission(String permission){
		return CustomNpcsPermissions.hasPermissionString(player, permission);
	}

	/**
	 * @since 1.7.10c
	 * @return Returns the exp level
	 */
	public int getExpLevel(){
		return player.experienceLevel;
	}
	
	/**
	 * @since 1.7.10c
	 * @param level The new exp level you want to set
	 */
	public void setExpLevel(int level){
		player.experienceLevel = level;
		player.addExperienceLevel(0);
	}
	
	/**
	 * Requires pixelmon to be installed
	 * @since 1.7.10d
	 */
	public ScriptPixelmonPlayerData getPixelmonData(){
		if(!PixelmonHelper.Enabled)
			return null;
		return new ScriptPixelmonPlayerData(player);
	}
}
