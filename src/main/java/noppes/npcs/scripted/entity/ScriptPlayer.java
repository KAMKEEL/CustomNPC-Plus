package noppes.npcs.scripted.entity;

import java.util.*;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.Achievement;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatList;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.WorldSettings;
import noppes.npcs.*;
import noppes.npcs.api.*;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.constants.EnumQuestType;
import noppes.npcs.containers.ContainerCustomGui;
import noppes.npcs.controllers.CustomGuiController;
import noppes.npcs.controllers.data.Dialog;
import noppes.npcs.controllers.DialogController;
import noppes.npcs.controllers.PixelmonHelper;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.controllers.PlayerDataController;
import noppes.npcs.controllers.data.PlayerDialogData;
import noppes.npcs.controllers.data.PlayerQuestData;
import noppes.npcs.controllers.data.Quest;
import noppes.npcs.controllers.QuestController;
import noppes.npcs.controllers.data.QuestData;
import noppes.npcs.entity.EntityDialogNpc;
import noppes.npcs.scripted.NpcAPI;
import noppes.npcs.scripted.ScriptPixelmonPlayerData;
import noppes.npcs.scripted.ScriptSound;
import noppes.npcs.scripted.constants.EntityType;
import noppes.npcs.scripted.gui.ScriptGui;
import noppes.npcs.api.handler.data.IDialog;
import noppes.npcs.api.handler.data.IQuest;
import noppes.npcs.api.gui.ICustomGui;
import noppes.npcs.api.handler.data.ISound;
import noppes.npcs.api.overlay.ICustomOverlay;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.handler.IOverlayHandler;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.scripted.overlay.ScriptOverlay;
import noppes.npcs.util.ValueUtil;

public class ScriptPlayer<T extends EntityPlayerMP> extends ScriptLivingBase<T> implements IPlayer {
	public T player;
	private PlayerData data;

	public ScriptPlayer(T player){
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

	public void kick(String reason) {
		player.playerNetServerHandler.kickPlayerFromServer(reason);
	}

	@Override
	public void spawnParticle(IParticle entityParticle) {
		entityParticle.spawnOnEntity(this);
	}

	@Override
	public void setPosition(double x, double y, double z){
		NoppesUtilPlayer.teleportPlayer(player, x, y, z, player.dimension);
	}
	public void setPos(double x, double y, double z) {
		this.setPosition(x,y,z);
	}

	public void setPosition(IPos pos) {
		this.setPosition(pos.getX(),pos.getY(),pos.getZ());
	}
	public void setPos(IPos pos) {
		this.setPosition(pos);
	}

	public void setPosition(double x, double y, double z, int dimensionId) {
		if (NpcAPI.Instance().getIWorld(dimensionId) == null)
			return;

		NoppesUtilPlayer.teleportPlayer(player, x, y, z, dimensionId);
	}
	public void setPos(double x, double y, double z, int dimensionId) {
		this.setPosition(x,y,z,dimensionId);
	}
	public void setPosition(double x, double y, double z, IWorld world) {
		this.setPosition(x,y,z,world.getDimensionID());
	}
	public void setPos(double x, double y, double z, IWorld world) {
		this.setPos(x,y,z,world.getDimensionID());
	}

	public void setPosition(IPos pos, int dimensionId) {
		this.setPosition(pos.getX(),pos.getY(),pos.getZ(), dimensionId);
	}
	public void setPos(IPos pos, int dimensionId) {
		this.setPosition(pos,dimensionId);
	}
	public void setPosition(IPos pos, IWorld world) {
		this.setPosition(pos.getX(),pos.getY(),pos.getZ(),world.getDimensionID());
	}
	public void setPos(IPos pos, IWorld world) {
		this.setPosition(pos,world.getDimensionID());
	}

	public void setDimension(int dimension) {
		this.setPosition(this.getPos(),dimension);
	}
	public void setDimension(IWorld world) {
		this.setDimension(world.getDimensionID());
	}

	public int getHunger(){
		return player.getFoodStats().getFoodLevel();
	}

	public void setHunger(int hunger){
		int prevHunger = this.getHunger();
		if(hunger < 0)
			hunger = 0;

		player.getFoodStats().addStats(hunger-prevHunger,0);
	}

	public float getSaturation(){
		return player.getFoodStats().getSaturationLevel();
	}

	public void setSaturation(float saturation){
		float prevSaturation = this.getHunger();
		if(saturation < 0)
			saturation = 0;

		player.getFoodStats().addStats(0,saturation-prevSaturation);
	}

	public void showDialog(IDialog dialog) {
		if (dialog != null) {
			showDialog(dialog.getId());
		}
	}

	public boolean hasReadDialog(IDialog dialog) {
		if (dialog != null) {
			return hasReadDialog(dialog.getId());
		}
		return false;
	}

	public void readDialog(IDialog dialog) {
		if (dialog != null) {
			this.readDialog(dialog.getId());
		}
	}

	public void unreadDialog(IDialog dialog) {
		if (dialog != null) {
			this.unreadDialog(dialog.getId());
		}
	}

	public void showDialog(int id){
		Dialog dialog = (Dialog) DialogController.instance.get(id);
		if(dialog == null)
			return;

		NoppesUtilServer.openDialog(player, new EntityDialogNpc(this.player.worldObj), dialog, 0);
	}

	public boolean hasReadDialog(int id){
		PlayerDialogData data = PlayerDataController.instance.getPlayerData(player).dialogData;
		return data.dialogsRead.contains(id);
	}

	public void readDialog(int id) {
		PlayerDataController.instance.getPlayerData(player).dialogData.dialogsRead.add(id);
	}

	public void unreadDialog(int id) {
		PlayerDataController.instance.getPlayerData(player).dialogData.dialogsRead.remove(id);
	}

	public boolean hasFinishedQuest(IQuest quest) {
		if (quest == null)
			return false;
		return hasFinishedQuest(quest.getId());
	}

	public boolean hasActiveQuest(IQuest quest) {
		if (quest == null)
			return false;
		return hasActiveQuest(quest.getId());
	}

	public void startQuest(IQuest quest) {
		if (quest == null)
			return;
		startQuest(quest.getId());
	}

	public void finishQuest(IQuest quest) {
		if (quest == null)
			return;
		finishQuest(quest.getId());
	}

	public void stopQuest(IQuest quest) {
		if (quest == null)
			return;
		stopQuest(quest.getId());
	}

	public void removeQuest(IQuest quest) {
		if (quest == null)
			return;
		removeQuest(quest.getId());
	}
	
	public boolean hasFinishedQuest(int id){
		PlayerQuestData data = PlayerDataController.instance.getPlayerData(player).questData;
		return data.finishedQuests.containsKey(id);
	}
	
	public boolean hasActiveQuest(int id){
		PlayerQuestData data = PlayerDataController.instance.getPlayerData(player).questData;
		return data.activeQuests.containsKey(id);
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
		data.factionData.increasePoints(faction, points, player);
	}
	/**
	 * @param faction The faction id
	 * @param points The new point value for this faction
	 */
	public void setFactionPoints(int faction, int points) {
		PlayerData data = PlayerDataController.instance.getPlayerData(player);
		data.factionData.increasePoints(faction, points-getFactionPoints(faction), player);
	}

    /**         
     * @param faction The faction id
     * @return  points
     */
	public int getFactionPoints(int faction) {
		PlayerData data = PlayerDataController.instance.getPlayerData(player);
		return data.factionData.getFactionPoints(faction);
	}

	public void sendMessage(String message){
		player.addChatMessage(new ChatComponentTranslation(NoppesStringUtils.formatText(message,player)));
	}
	
	public void sendMessage(String message, String color, boolean bold, boolean italic, boolean underlined) {
		sendMessage(message, color, bold, italic, underlined, false, false);
	}
	
	public void sendMessage(String message, String color, boolean bold, boolean italic, boolean obfuscated, boolean strikethrough, boolean underlined) {
		EnumChatFormatting c = getColor(color);

		if (c == null)
			return;

		ChatComponentTranslation chat = new ChatComponentTranslation(NoppesStringUtils.formatText(message,player));
		ChatStyle style = new ChatStyle();
		style.setColor(c);
		style.setBold(bold);
		style.setItalic(italic);
		style.setObfuscated(obfuscated);
		style.setStrikethrough(strikethrough);
		style.setUnderlined(underlined);
		chat.setChatStyle(style);

		player.addChatMessage(chat);
	}
	
	private EnumChatFormatting getColor(String color) {
		switch (color) {
		case "black": return EnumChatFormatting.BLACK;
		case "dark_blue": return EnumChatFormatting.DARK_BLUE;
		case "dark_green": return EnumChatFormatting.DARK_GREEN;
		case "dark_aqua": return EnumChatFormatting.DARK_AQUA;
		case "dark_red": return EnumChatFormatting.DARK_RED;
		case "dark_purple": return EnumChatFormatting.DARK_PURPLE;
		case "gold": return EnumChatFormatting.GOLD;
		case "gray": return EnumChatFormatting.GRAY;
		case "dark_gray": return EnumChatFormatting.DARK_GRAY;
		case "blue": return EnumChatFormatting.BLUE;
		case "green": return EnumChatFormatting.GREEN;
		case "aqua": return EnumChatFormatting.AQUA;
		case "red": return EnumChatFormatting.RED;
		case "light_purple": return EnumChatFormatting.LIGHT_PURPLE;
		case "yellow": return EnumChatFormatting.YELLOW;
		case "white": return EnumChatFormatting.WHITE;
		default: return null;
		}
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
	 * @since 1.7.10d
	 * @return Returns a IItemStack array size 36
	 */
	public IItemStack[] getInventory(){
		IItemStack[] items = new IItemStack[36];
		for(int i = 0; i < player.inventory.mainInventory.length; i++){
			ItemStack item = player.inventory.mainInventory[i];
			if(item != null)
				items[i] = NpcAPI.Instance().getIItemStack(item);
		}
		return items;
	}

	public int inventoryItemCount(IItemStack item){
		return inventoryItemCount(item, true, true);
	}

	/**
	 * @param item The item to be checked
	 * @return How many of this item the player has
	 */
	public int inventoryItemCount(IItemStack item, boolean ignoreNBT, boolean ignoreDamage) {
		int i = 0;
		for(ItemStack is : player.inventory.mainInventory){
			if (is != null && NoppesUtilPlayer.compareItems(is, item.getMCItemStack(), ignoreDamage, ignoreNBT)) {
				i += is.stackSize;
			}
		}
		return i;
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
		return removeItem(NpcAPI.Instance().getIItemStack(new ItemStack(item, 1, damage)), amount);
	}

	public boolean removeItem(IItemStack item, int amount){
		return removeItem(item, amount, true, true);
	}

	/**
	 * @param item The Item type to be removed
	 * @param amount How many will be removed
	 * @return Returns true if the items were removed succesfully. Returns false incase a bigger amount than what the player has was given
	 */
	public boolean removeItem(IItemStack item, int amount, boolean ignoreNBT, boolean ignoreDamage) {
		int count = inventoryItemCount(item);
		if(amount  > count)
			return false;
		else if(count == amount)
			removeAllItems(item);
		else{
			for(int i = 0; i < player.inventory.mainInventory.length; i++){
				ItemStack is = player.inventory.mainInventory[i];
				if (is != null && NoppesUtilPlayer.compareItems(is, item.getMCItemStack(), ignoreDamage, ignoreNBT)) {
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
		this.updatePlayerInventory();
		return true;
	}

	public void removeAllItems(IItemStack item){
		removeAllItems(item, true, true);
	}

	/**
	 * @param item The item to be removed from the players inventory
	 */
	public int removeAllItems(IItemStack item, boolean ignoreNBT, boolean ignoreDamage){
		int removed = 0;

		for(int i = 0; i < player.inventory.mainInventory.length; i++) {
			ItemStack is = player.inventory.mainInventory[i];
			if (is != null && NoppesUtilPlayer.compareItems(is, item.getMCItemStack(), ignoreDamage, ignoreNBT)) {
				player.inventory.mainInventory[i] = null;
				removed++;
			}
		}

		return removed;
	}

	/**
	 * @since 1.7.10c
	 * @param item Item to be added
	 * @param amount The amount of the item to be added
	 * @return Returns whether or not it gave the item succesfully
	 */
	public boolean giveItem(IItemStack item, int amount){
		if(item != null && item.getMCItemStack() != null) {
			item.setStackSize(amount);
			boolean bool = this.player.inventory.addItemStackToInventory(item.getMCItemStack());
			this.updatePlayerInventory();
			return bool;
		} else {
			return false;
		}
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

	public void setSpawnpoint(IPos pos){
		this.setSpawnpoint(pos.getX(),pos.getY(),pos.getZ());
	}
	
	public void resetSpawnpoint(){
		player.setSpawnChunk(null, false);
	}

	public void clearInventory() {
		for(int i = 0; i < player.inventory.mainInventory.length; i++) player.inventory.mainInventory[i] = null;
		for(int i = 0; i < player.inventory.armorInventory.length; i++) player.inventory.armorInventory[i] = null;
	}

	public void setRotation(float rotationYaw){
		NoppesUtilPlayer.teleportPlayer(player, player.posX, player.posY, player.posZ, rotationYaw, player.rotationPitch, player.dimension);
	}

	public void setRotation(float rotationYaw, float rotationPitch){
		NoppesUtilPlayer.teleportPlayer(player, player.posX, player.posY, player.posZ, rotationYaw, rotationPitch, player.dimension);
	}

	public void swingHand(){
		NoppesUtilPlayer.swingPlayerArm(player);
	}

	public void disableMouseInput(long time, int... buttonIds) {
		NoppesUtilPlayer.disableMouseInput(player ,time, buttonIds);
	}

	public void stopUsingItem(){
		player.stopUsingItem();
	}

	public void clearItemInUse(){
		player.clearItemInUse();
	}

	public void playSound(String name, float volume, float pitch){
		player.playSound(name, volume, pitch);
	}

	public void playSound(int id, ISound sound) {
		NoppesUtilPlayer.playSoundTo(player, id, (ScriptSound) sound);
	}

	public void stopSound(int id) {
		NoppesUtilPlayer.stopSoundFor(player, id);
	}

	public void pauseSounds() {
		NoppesUtilPlayer.pauseSoundsFor(player);
	}

	public void continueSounds() {
		NoppesUtilPlayer.continueSoundsFor(player);
	}

	public void stopSounds() {
		NoppesUtilPlayer.stopSoundsFor(player);
	}

	public void mountEntity(Entity ridingEntity){
		player.mountEntity(ridingEntity);
	}

	public IEntity dropOneItem(boolean dropStack){
		return NpcAPI.Instance().getIEntity(player.dropOneItem(dropStack));
	}

	public boolean canHarvestBlock(IBlock block){
		return player.canHarvestBlock(block.getMCBlock());
	}

	public boolean interactWith(IEntity entity){
		return player.interactWith(entity.getMCEntity());
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
	public IPixelmonPlayerData getPixelmonData(){
		if(!PixelmonHelper.Enabled)
			return null;
		return new ScriptPixelmonPlayerData(player);
	}

	public ITimers getTimers() {
		return PlayerDataController.instance.getPlayerData(player).timers;
	}

	public void updatePlayerInventory() {
		((EntityPlayerMP)this.entity).inventoryContainer.detectAndSendChanges();
		PlayerData playerData = PlayerDataController.instance.getPlayerData(player);
		PlayerQuestData questData = playerData.questData;
		questData.checkQuestCompletion(playerData, EnumQuestType.Item);
	}

	@Deprecated
	public boolean checkGUIOpen() {
		NoppesUtilPlayer.isGUIOpen(player);
		PlayerData data = PlayerDataController.instance.getPlayerData(player);
		return data.getGUIOpen();
	}

	public ScriptDBCPlayer<T> getDBCPlayer() {
		Set keySet = player.getEntityData().getCompoundTag("PlayerPersisted").func_150296_c();
		Iterator iterator = keySet.iterator();

		while (iterator.hasNext())
		{
			String s = (String)iterator.next();
			if(s.contains("jrmc"))
				return new ScriptDBCPlayer<T>(this.player);
		}
		return null;
	}

	public boolean blocking() {
		return player.isBlocking();
	}

	public PlayerData getData() {
		if (this.data == null) {
			this.data = PlayerDataController.instance.getPlayerData(player);
		}

		return this.data;
	}

	public boolean isScriptingDev() {
		return CustomNpcs.isScriptDev(player);
	}

	public IQuest[] getActiveQuests() {
		PlayerQuestData data = (PlayerQuestData) this.getData().getQuestData();
		List<IQuest> quests = new ArrayList();
		Iterator var3 = data.activeQuests.keySet().iterator();

		while(var3.hasNext()) {
			int id = (Integer)var3.next();
			IQuest quest = (IQuest)QuestController.instance.quests.get(id);
			if (quest != null) {
				quests.add(quest);
			}
		}

		return (IQuest[])quests.toArray(new IQuest[quests.size()]);
	}

	public IContainer getOpenContainer() {
		return NpcAPI.Instance().getIContainer(((EntityPlayerMP)this.entity).openContainer);
	}

	public void showCustomGui(ICustomGui gui) {
		CustomGuiController.openGui(this, (ScriptGui) gui);
	}

	public ICustomGui getCustomGui() {
		return ((EntityPlayerMP)this.entity).openContainer instanceof ContainerCustomGui ? ((ContainerCustomGui)((EntityPlayerMP)this.entity).openContainer).customGui : null;
	}

	public void closeGui() {
		((EntityPlayerMP)this.entity).closeContainer();
		Server.sendData((EntityPlayerMP)this.entity, EnumPacketClient.GUI_CLOSE, -1, new NBTTagCompound());
	}

	public void showCustomOverlay(ICustomOverlay overlay) {
		CustomGuiController.openOverlay(this, (ScriptOverlay) overlay);
	}

	public void closeOverlay(int id) {
		Server.sendData((EntityPlayerMP)this.entity, EnumPacketClient.SCRIPT_OVERLAY_CLOSE, id, new NBTTagCompound());
	}

	public IOverlayHandler getOverlays() {
		return this.getData().skinOverlays;
	}

	public IQuest[] getFinishedQuests() {
		PlayerQuestData data = this.getData().questData;
		List<IQuest> quests = new ArrayList();
		Iterator var3 = data.finishedQuests.keySet().iterator();

		while(var3.hasNext()) {
			int id = (Integer)var3.next();
			IQuest quest = (IQuest)QuestController.instance.quests.get(id);
			if (quest != null) {
				quests.add(quest);
			}
		}

		return (IQuest[])quests.toArray(new IQuest[quests.size()]);
	}
}
