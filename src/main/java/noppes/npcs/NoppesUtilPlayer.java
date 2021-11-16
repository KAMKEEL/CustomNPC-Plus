package noppes.npcs;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.WorldServer;
import net.minecraftforge.oredict.OreDictionary;
import noppes.npcs.constants.EnumOptionType;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.constants.EnumPlayerPacket;
import noppes.npcs.constants.EnumRoleType;
import noppes.npcs.constants.EnumScriptType;
import noppes.npcs.containers.ContainerNPCBankInterface;
import noppes.npcs.containers.ContainerNPCFollower;
import noppes.npcs.containers.ContainerNPCFollowerHire;
import noppes.npcs.controllers.Bank;
import noppes.npcs.controllers.BankController;
import noppes.npcs.controllers.BankData;
import noppes.npcs.controllers.Dialog;
import noppes.npcs.controllers.DialogController;
import noppes.npcs.controllers.DialogOption;
import noppes.npcs.controllers.Line;
import noppes.npcs.controllers.PlayerBankData;
import noppes.npcs.controllers.PlayerDataController;
import noppes.npcs.controllers.PlayerQuestController;
import noppes.npcs.controllers.PlayerQuestData;
import noppes.npcs.controllers.PlayerTransportData;
import noppes.npcs.controllers.QuestData;
import noppes.npcs.controllers.TransportController;
import noppes.npcs.controllers.TransportLocation;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.RoleFollower;
import cpw.mods.fml.common.network.internal.FMLProxyPacket;

public class NoppesUtilPlayer {

	public static void changeFollowerState(EntityPlayerMP player, EntityNPCInterface npc) {
		if(npc.advanced.role != EnumRoleType.Follower)
			return;
		
		RoleFollower role = (RoleFollower) npc.roleInterface;
		EntityPlayer owner = role.owner;
		if(owner == null || !owner.getCommandSenderName().equals(player.getCommandSenderName()))
			return;
		
		role.isFollowing = !role.isFollowing;
	}
	public static void hireFollower(EntityPlayerMP player, EntityNPCInterface npc) {
		if(npc.advanced.role != EnumRoleType.Follower)
			return;
		Container con = player.openContainer;
		if(con == null || !(con instanceof ContainerNPCFollowerHire))
			return;
		
		ContainerNPCFollowerHire container = (ContainerNPCFollowerHire) con;
		RoleFollower role = (RoleFollower) npc.roleInterface;
		followerBuy(role, container.currencyMatrix, player, npc);
	}
	public static void extendFollower(EntityPlayerMP player, EntityNPCInterface npc) {
		if(npc.advanced.role != EnumRoleType.Follower)
			return;
		Container con = player.openContainer;
		if(con == null || !(con instanceof ContainerNPCFollower))
			return;
		
		ContainerNPCFollower container = (ContainerNPCFollower) con;
		RoleFollower role = (RoleFollower) npc.roleInterface;
		followerBuy(role, container.currencyMatrix, player, npc);
	}
	public static void transport(EntityPlayerMP player, EntityNPCInterface npc, String location){
		TransportLocation loc = TransportController.getInstance().getTransport(location);
        PlayerTransportData playerdata = PlayerDataController.instance.getPlayerData(player).transportData;

		if(loc == null || !loc.isDefault() && !playerdata.transports.contains(loc.id))
			return;
		
		teleportPlayer(player, loc.posX, loc.posY, loc.posZ, loc.dimension);
	}
	
	public static void teleportPlayer(EntityPlayerMP player, double posX, double posY, double posZ, int dimension){
		if(player.dimension != dimension){
			int dim = player.dimension;
			MinecraftServer server = MinecraftServer.getServer();
            WorldServer wor = server.worldServerForDimension(dimension);
            if(wor == null){
            	player.addChatMessage(new ChatComponentText("Broken transporter. Dimenion does not exist"));
            	return;
            }
            player.setLocationAndAngles(posX, posY, posZ, player.rotationYaw, player.rotationPitch);
            server.getConfigurationManager().transferPlayerToDimension(player, dimension, new CustomTeleporter(wor));
    		player.playerNetServerHandler.setPlayerLocation(posX, posY, posZ, player.rotationYaw, player.rotationPitch);

    		if(!wor.playerEntities.contains(player))
                wor.spawnEntityInWorld(player);
		}
		else{
			player.playerNetServerHandler.setPlayerLocation(posX, posY, posZ, player.rotationYaw, player.rotationPitch);
		}
        player.worldObj.updateEntityWithOptionalForce(player, false);
	}

	public static void teleportPlayer(EntityPlayerMP player, double posX, double posY, double posZ, float yaw, float pitch, int dimension){
		if(player.dimension != dimension){
			int dim = player.dimension;
			MinecraftServer server = MinecraftServer.getServer();
			WorldServer wor = server.worldServerForDimension(dimension);
			if(wor == null){
				player.addChatMessage(new ChatComponentText("Broken transporter. Dimenion does not exist"));
				return;
			}
			player.setLocationAndAngles(posX, posY, posZ, yaw, pitch);
			server.getConfigurationManager().transferPlayerToDimension(player, dimension, new CustomTeleporter(wor));
			player.playerNetServerHandler.setPlayerLocation(posX, posY, posZ, yaw, pitch);

			if(!wor.playerEntities.contains(player))
				wor.spawnEntityInWorld(player);
		}
		else{
			player.playerNetServerHandler.setPlayerLocation(posX, posY, posZ, yaw, pitch);
		}
		player.worldObj.updateEntityWithOptionalForce(player, false);
	}
	
	private static void followerBuy(RoleFollower role,IInventory currencyInv,EntityPlayerMP player, EntityNPCInterface npc){
    	ItemStack currency = currencyInv.getStackInSlot(0);
		if(currency == null)
    		return;
    	HashMap<ItemStack,Integer> cd = new HashMap<ItemStack,Integer>();
    	for(int i : role.inventory.items.keySet()){
    		ItemStack is = role.inventory.items.get(i);
    		if(is == null || is.getItem() != currency.getItem() || is.getHasSubtypes() && is.getItemDamage() != currency.getItemDamage())
    			continue;
    		int days = 1;
    		if(role.rates.containsKey(i))
    			days = role.rates.get(i);
    		
    		cd.put(is,days);
    	}
    	if(cd.size() == 0)
    		return;
    	int stackSize = currency.stackSize;
    	int days = 0;
    	
    	int possibleDays = 0;
    	int possibleSize = stackSize;
    	while(true){
        	for(ItemStack item : cd.keySet()){
        		int rDays = cd.get(item);
        		int rValue = item.stackSize;
        		if(rValue > stackSize)
        			continue;
        		int newStackSize = stackSize % rValue;
        		int size = stackSize - newStackSize;
        		int posDays = (size / rValue) * rDays;
        		if(possibleDays <= posDays){
        			possibleDays = posDays;
        			possibleSize = newStackSize;
        		}
        	}
        	if(stackSize == possibleSize)
        		break;
        	stackSize = possibleSize;
        	days += possibleDays;
        	possibleDays = 0;
    	}
    	if(days == 0)
    		return;
    	if(stackSize <= 0)
    		currencyInv.setInventorySlotContents(0, null);
    	else
    		currency = currency.splitStack(stackSize);
    	
    	npc.say(player, new Line(NoppesStringUtils.formatText(role.dialogHire.replace("{days}", days+""), player, npc)));
    	role.setOwner(player);
    	role.addDays(days);
	}

	public static void bankUpgrade(EntityPlayerMP player, EntityNPCInterface npc) {
		if(npc.advanced.role != EnumRoleType.Bank)
			return;
		Container con = player.openContainer;
		if(con == null || !(con instanceof ContainerNPCBankInterface))
			return;
		
		ContainerNPCBankInterface container = (ContainerNPCBankInterface) con;
		Bank bank = BankController.getInstance().getBank(container.bankid);
		ItemStack item = bank.upgradeInventory.getStackInSlot(container.slot);
		if(item == null)
			return;

		int price = item.stackSize;
		ItemStack currency = container.currencyMatrix.getStackInSlot(0);
		if(currency == null || price > currency.stackSize)
			return;
		if(currency.stackSize - price == 0)
			container.currencyMatrix.setInventorySlotContents(0, null);
		else
			currency = currency.splitStack(price);
		player.closeContainer();
		PlayerBankData data = PlayerDataController.instance.getBankData(player,bank.id);
        BankData bankData = data.getBank(bank.id);
		bankData.upgradedSlots.put(container.slot, true);

		bankData.openBankGui(player, npc, bank.id, container.slot);
	}
	public static void bankUnlock(EntityPlayerMP player, EntityNPCInterface npc) {
		if(npc.advanced.role != EnumRoleType.Bank)
			return;
		Container con = player.openContainer;
		if(con == null || !(con instanceof ContainerNPCBankInterface))
			return;
		ContainerNPCBankInterface container = (ContainerNPCBankInterface) con;
		Bank bank = BankController.getInstance().getBank(container.bankid);
		
		ItemStack item = bank.currencyInventory.getStackInSlot(container.slot);
		if(item == null)
			return;
		
		int price = item.stackSize;
		ItemStack currency = container.currencyMatrix.getStackInSlot(0);
		if(currency == null || price > currency.stackSize)
			return;
		if(currency.stackSize - price == 0)
			container.currencyMatrix.setInventorySlotContents(0, null);
		else
			currency = currency.splitStack(price);
		player.closeContainer();
		PlayerBankData data = PlayerDataController.instance.getBankData(player,bank.id);
        BankData bankData = data.getBank(bank.id);
		if(bankData.unlockedSlots + 1 <= bank.maxSlots)
			bankData.unlockedSlots++;
		
		bankData.openBankGui(player, npc, bank.id, container.slot);
	}
	public static void sendData(EnumPlayerPacket enu, Object... obs) {
		ByteBuf buffer = Unpooled.buffer();
		try {
			if(!Server.fillBuffer(buffer, enu, obs))
				return;
			CustomNpcs.ChannelPlayer.sendToServer(new FMLProxyPacket(buffer,"CustomNPCsPlayer"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void dialogSelected(int dialogId, int optionId, EntityPlayerMP player, EntityNPCInterface npc) {
		Dialog dialog = DialogController.instance.dialogs.get(dialogId);
		if(dialog == null)
			return;
		npc.script.callScript(EnumScriptType.DIALOG_OPTION, "player", player, "dialog", dialogId, "option", optionId + 1);
    	
		if(!dialog.hasDialogs(player) && !dialog.hasOtherOptions())
			return;
		DialogOption option = dialog.options.get(optionId);
    	if(option == null || option.optionType == EnumOptionType.DialogOption && (!option.isAvailable(player) || !option.hasDialog()) || option.optionType == EnumOptionType.Disabled || option.optionType == EnumOptionType.QuitOption)
    		return;
    	if(option.optionType == EnumOptionType.RoleOption){
    		if(npc.roleInterface != null)
    			npc.roleInterface.interact(player);
    		else
    			Server.sendData(player, EnumPacketClient.GUI_CLOSE);
    	}
    	else if(option.optionType == EnumOptionType.DialogOption){
    		NoppesUtilServer.openDialog(player, npc, option.getDialog());
    	}
    	else if(option.optionType == EnumOptionType.CommandBlock){
			Server.sendData(player, EnumPacketClient.GUI_CLOSE);
    		NoppesUtilServer.runCommand(player, npc.getCommandSenderName(), option.command);
    	}
    	else
			Server.sendData(player, EnumPacketClient.GUI_CLOSE);
	}
	public static void sendQuestLogData(EntityPlayerMP player) {
        if(!PlayerQuestController.hasActiveQuests(player)){
        	return;
        }
        QuestLogData data = new QuestLogData();
        data.setData(player);
        Server.sendData(player, EnumPacketClient.GUI_DATA, data.writeNBT());
	}
	public static void questCompletion(EntityPlayerMP player, int questId) {
		PlayerQuestData playerdata = PlayerDataController.instance.getPlayerData(player).questData;
		QuestData data = playerdata.activeQuests.get(questId);
		if(data == null)
			return;
		
		if(!data.quest.questInterface.isCompleted(player))
			return;
		

		data.quest.questInterface.handleComplete(player);
		if(data.quest.rewardExp > 0){
			player.worldObj.playSoundAtEntity(player, "random.orb", 0.1F, 0.5F * ((player.worldObj.rand.nextFloat() - player.worldObj.rand.nextFloat()) * 0.7F + 1.8F));
            
            player.addExperience(data.quest.rewardExp);
		}
		data.quest.factionOptions.addPoints(player);
		if(data.quest.mail.isValid()){
			PlayerDataController.instance.addPlayerMessage(player.getCommandSenderName(), data.quest.mail);
		}
		if(!data.quest.randomReward){
			for(ItemStack item : data.quest.rewardItems.items.values()){
				NoppesUtilServer.GivePlayerItem(player, player, item);
			}
		}
		else{
			List<ItemStack> list = new ArrayList<ItemStack>();
			for(ItemStack item : data.quest.rewardItems.items.values()){
				if(item != null && item.getItem() != null)
					list.add(item);
			}
			if(!list.isEmpty()){
				NoppesUtilServer.GivePlayerItem(player, player, list.get(player.getRNG().nextInt(list.size())));
			}
		}
		if(!data.quest.command.isEmpty()){
			NoppesUtilServer.runCommand(player, "QuestCompletion", data.quest.command);
		}
		PlayerQuestController.setQuestFinished(data.quest, player);
		if(data.quest.hasNewQuest()) PlayerQuestController.addActiveQuest(data.quest.getNextQuest(), player);
	}
	
	public static boolean compareItems(ItemStack item, ItemStack item2, boolean ignoreDamage, boolean ignoreNBT){
        if (item2 == null || item == null){
            return false;
        }
        boolean oreMatched = false;
        OreDictionary.itemMatches(item, item2, false);
        int[] ids = OreDictionary.getOreIDs(item);
        if(ids.length > 0){
        	for(int id : ids){
            	boolean match1 = false, match2 = false;
        		for(ItemStack is : OreDictionary.getOres(id)){
        			if(compareItemDetails(item, is, ignoreDamage, ignoreNBT)){
        				match1 = true;
        			}
        			if(compareItemDetails(item2, is, ignoreDamage, ignoreNBT)){
        				match2 = true;
        			}
        		}
            	if(match1 && match2)
            		return true;
        	}
        }
		return compareItemDetails(item, item2, ignoreDamage, ignoreNBT);
	}
	private static boolean compareItemDetails(ItemStack item, ItemStack item2, boolean ignoreDamage, boolean ignoreNBT){
        if (item.getItem() != item2.getItem() ){
            return false;
        }
		if (!ignoreDamage && item.getItemDamage() != -1 && item.getItemDamage() != item2.getItemDamage()){
            return false;
        }
        if(!ignoreNBT && item.stackTagCompound != null && (item2.stackTagCompound == null || !item.stackTagCompound.equals(item2.stackTagCompound))){
            return false;
        }
        if(!ignoreNBT && item2.stackTagCompound != null && item.stackTagCompound == null){
            return false;
        }
		return true;
	}
	public static boolean compareItems(EntityPlayer player, ItemStack item, boolean ignoreDamage, boolean ignoreNBT){
		int size = 0;
		for(ItemStack is : player.inventory.mainInventory){
			if(is != null && compareItems(item, is, ignoreDamage, ignoreNBT)) 
				size += is.stackSize;
		}
		return size >= item.stackSize;
	}
	public static void consumeItem(EntityPlayer player, ItemStack item, boolean ignoreDamage, boolean ignoreNBT) {
		if(item == null)
			return;
		int size = item.stackSize;
		for(int i = 0; i < player.inventory.mainInventory.length; i++){
			ItemStack is = player.inventory.mainInventory[i];
			if(is == null || !compareItems(item, is, ignoreDamage, ignoreNBT))
				continue;
			if(size >= is.stackSize){
				size -= is.stackSize;
				player.inventory.mainInventory[i] = null;
			}
			else{
				player.inventory.mainInventory[i].splitStack(size);
				break;
			}
		}
	}
	public static void isGUIOpen(EntityPlayerMP player){
		Server.sendData(player, EnumPacketClient.ISGUIOPEN);
	}
}
