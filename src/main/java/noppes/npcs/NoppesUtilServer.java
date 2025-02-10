package noppes.npcs;

import io.netty.buffer.ByteBuf;
import kamkeel.npcs.controllers.SyncController;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.packets.data.gui.GuiClosePacket;
import kamkeel.npcs.network.packets.data.gui.GuiErrorPacket;
import kamkeel.npcs.network.packets.data.gui.GuiOpenPacket;
import kamkeel.npcs.network.packets.data.large.GuiDataPacket;
import kamkeel.npcs.network.packets.data.large.ScrollDataPacket;
import kamkeel.npcs.network.packets.data.large.ScrollListPacket;
import kamkeel.npcs.network.packets.data.npc.DialogPacket;
import kamkeel.npcs.network.packets.data.ParticlePacket;
import kamkeel.npcs.network.packets.data.script.ScriptedParticlePacket;
import kamkeel.npcs.network.packets.data.npc.DeleteNpcPacket;
import kamkeel.npcs.network.packets.data.npc.EditNpcPacket;
import kamkeel.npcs.network.packets.data.npc.RolePacket;
import kamkeel.npcs.network.packets.data.gui.GuiTeleporterPacket;
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.command.server.CommandBlockLogic;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.MobSpawnerBaseLogic;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityCommandBlock;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.config.ConfigMain;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPlayerData;
import noppes.npcs.constants.EnumRoleType;
import noppes.npcs.containers.ContainerManageBanks;
import noppes.npcs.containers.ContainerManageRecipes;
import noppes.npcs.controllers.*;
import noppes.npcs.controllers.data.*;
import noppes.npcs.entity.EntityDialogNpc;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.RoleTransporter;
import noppes.npcs.scripted.NpcAPI;
import noppes.npcs.scripted.ScriptSound;
import noppes.npcs.scripted.event.DialogEvent;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;

import static kamkeel.npcs.network.packets.data.large.ScrollDataPacket.sendScrollData;
import static kamkeel.npcs.network.packets.data.large.ScrollGroupPacket.sendScrollGroup;

public class NoppesUtilServer {
	private static HashMap<String,Quest> editingQuests = new HashMap<String,Quest>();

    public static IPlayer getIPlayer(EntityPlayer p) {
        return (IPlayer) NpcAPI.Instance().getIEntity(p);
    }

    public static UUID getUUID(Entity entity) {
        return entity instanceof EntityPlayer ? ((EntityPlayer) entity).getGameProfile().getId() : entity.getUniqueID();
    }

    public static void setEditingNpc(EntityPlayer player, EntityNPCInterface npc){
        PlayerData data = PlayerDataController.Instance.getPlayerData(player);
        data.editingNpc = npc;
        if(npc != null)
            PacketHandler.Instance.sendToPlayer(new EditNpcPacket(npc.getEntityId()), (EntityPlayerMP)player);
    }

    public static EntityNPCInterface getEditingNpc(EntityPlayer player){
        PlayerData data = PlayerDataController.Instance.getPlayerData(player);
        return data.editingNpc;
    }

    public static void setEditingQuest(EntityPlayer player, Quest quest) {
        editingQuests.put(player.getCommandSenderName(), quest);
    }

    public static Quest getEditingQuest(EntityPlayer player){
        return editingQuests.get(player.getCommandSenderName());
    }

    public static void sendRoleData(EntityPlayer player, EntityNPCInterface npc){
        if(npc.advanced.role == EnumRoleType.None)
            return;
        NBTTagCompound comp = new NBTTagCompound();
        npc.roleInterface.writeToNBT(comp);
        comp.setInteger("EntityId", npc.getEntityId());
        comp.setInteger("Role", npc.advanced.role.ordinal());
        PacketHandler.Instance.sendToPlayer(new RolePacket(comp), (EntityPlayerMP)player);
    }

	public static void sendFactionDataAll(EntityPlayerMP player) {
		Map<String,Integer> map = new HashMap<String,Integer>();
		for(Faction faction : FactionController.getInstance().factions.values()){
			map.put(faction.name, faction.id);
		}
		sendScrollData(player, map);
	}

	public static void sendAnimationDataAll(EntityPlayerMP player) {
		Map<String,Integer> map = new HashMap<String,Integer>();
		for(Animation animation : AnimationController.getInstance().animations.values()){
			map.put(animation.name, animation.id);
		}
		sendScrollData(player, map);
	}

	public static void sendTagDataAll(EntityPlayerMP player) {
		Map<String,Integer> map = new HashMap<String,Integer>();
		for(Tag tag : TagController.getInstance().tags.values()){
			map.put(tag.name, tag.id);
		}
		sendScrollData(player, map);
	}

	public static void sendBankDataAll(EntityPlayerMP player) {
		Map<String,Integer> map = new HashMap<String,Integer>();
		for(Bank bank : BankController.getInstance().banks.values()){
			map.put(bank.name, bank.id);
		}
		sendScrollData(player, map);
	}

    public static NBTTagCompound writeItem(ItemStack item, NBTTagCompound nbt){
        String resourcelocation = Item.itemRegistry.getNameForObject(item.getItem());
        nbt.setString("id", resourcelocation == null ? "minecraft:air" : resourcelocation.toString());
        nbt.setByte("Count", (byte)item.stackSize);
        nbt.setShort("Damage", (short)item.getItemDamage());

        if (item.stackTagCompound != null){
            nbt.setTag("tag", item.stackTagCompound);
        }

        return nbt;
    }

    public static ItemStack readItem(NBTTagCompound nbt){
    	Item item;
        if (nbt.hasKey("id", 8)){
        	item = getByNameOrId(nbt.getString("id"));
        }
        else{
        	item = Item.getItemById(nbt.getShort("id"));
        }
        if(item == null)
        	return null;
        ItemStack itemstack = new ItemStack(item);

        itemstack.stackSize = nbt.getByte("Count");
        itemstack.setItemDamage(nbt.getShort("Damage"));

        if (itemstack.getItemDamage() < 0){
        	itemstack.setItemDamage(0);
        }

        if (nbt.hasKey("tag", 10)){
        	itemstack.stackTagCompound = nbt.getCompoundTag("tag");
        }
        return itemstack;
    }

    public static Item getByNameOrId(String id)
    {
        Item item = (Item)Item.itemRegistry.getObject(id);

        if (item == null)
        {
            try
            {
                return Item.getItemById(Integer.parseInt(id));
            }
            catch (NumberFormatException numberformatexception)
            {
                ;
            }
        }

        return item;
    }

	public static void openDialog(EntityPlayer player, EntityNPCInterface npc, Dialog dia, int optionId){
		Dialog dialog = dia.copy(player);
        PlayerData playerdata = PlayerData.get(player);

		if (!npc.isRemote()) {
			if (EventHooks.onNPCDialog(npc, player, dialog.id, optionId, dialog)) {
				return;
			}
			if (EventHooks.onDialogOpen(new DialogEvent.DialogOpen((IPlayer) NpcAPI.Instance().getIEntity(player), dialog))) {
				return;
			}
		}

		if(npc instanceof EntityDialogNpc || dia.id < 0){
			dialog.hideNPC = true;
            PacketHandler.Instance.sendToPlayer(new DialogPacket(npc.getCommandSenderName(), -1, dialog.writeToNBT(new NBTTagCompound())), (EntityPlayerMP)player);
		}
		else
            PacketHandler.Instance.sendToPlayer(new DialogPacket("Real", npc.getEntityId(), dialog.writeToNBT(new NBTTagCompound())), (EntityPlayerMP)player);

        dia.factionOptions.addPoints(player);
        if(dialog.hasQuest())
        	PlayerQuestController.addActiveQuest(new QuestData(dialog.getQuest()),player);
        if(!dialog.command.isEmpty()){
            runCommand(player, npc.getCommandSenderName(), dialog.command);
        }
        if(dialog.mail.isValid())
        	PlayerDataController.Instance.addPlayerMessage(player.getCommandSenderName(), dialog.mail);
        PlayerDialogData data = PlayerDataController.Instance.getPlayerData(player).dialogData;
        if(!data.dialogsRead.contains(dialog.id) && dialog.id >= 0){
            data.dialogsRead.add(dialog.id);
            playerdata.updateClient = true;
        }
		setEditingNpc(player, npc);
	}
	public static void runCommand(EntityPlayer player, String name, String command){
        runCommand(player, name, command, player);
	}

    public static void runCommand(EntityLivingBase executer, String name, String command, EntityPlayer player) {
        if(player != null)
            command = command.replace("@dp", player.getCommandSenderName());
        command = command.replace("@npc", name);

        // Trim the command to remove leading/trailing spaces
        command = command.trim();

        String[] commands = command.split("@x"); // Split the command by @x

        // Create TileEntityCommandBlock instance outside the loop
        TileEntityCommandBlock tile = new TileEntityCommandBlock();
        tile.setWorldObj(executer.worldObj);
        tile.xCoord = MathHelper.floor_double(executer.posX);
        tile.yCoord = MathHelper.floor_double(executer.posY);
        tile.zCoord = MathHelper.floor_double(executer.posZ);

        // Create CommandBlockLogic instance outside the loop
        CommandBlockLogic logic = tile.func_145993_a();
        for (String cmd : commands) {
            logic.func_145752_a(cmd.trim()); // Trim the command to remove any leading/trailing spaces
            logic.func_145754_b("@" + name);
            logic.func_145755_a(executer.worldObj);
        }
    }



    public static void runCommand(World world, String name, String command) {
		TileEntityCommandBlock tile = new TileEntityCommandBlock();
		tile.setWorldObj(world.provider.worldObj);
		tile.xCoord = 0;
		tile.yCoord = 0;
		tile.zCoord = 0;

        // Trim the command to remove leading/trailing spaces
        command = command.trim();
        String[] commands = command.split("@x"); // Split the command by @x
        CommandBlockLogic logic = tile.func_145993_a();
        for (String cmd : commands) {
            logic.func_145752_a(cmd.trim()); // Trim the command to remove any leading/trailing spaces
            logic.func_145754_b("@" + name);
            logic.func_145755_a(world);
        }
	}

	public static void consumeItemStack(int i, EntityPlayer player){
		ItemStack item = player.inventory.getCurrentItem();
		if(player.capabilities.isCreativeMode || item == null)
			return;

        --item.stackSize;
        if (item.stackSize <= 0)
        	player.destroyCurrentEquippedItem();
	}

	public static void sendOpenGui(EntityPlayer player,
			EnumGuiType gui, EntityNPCInterface npc) {
		sendOpenGui(player, gui, npc, 0, 0, 0);
	}

	public static void sendOpenGuiNoDelay(EntityPlayer player,
								   EnumGuiType gui, EntityNPCInterface npc) {
		sendOpenGuiNoDelay(player, gui, npc, 0, 0, 0);
	}

	public static void sendOpenGui(final EntityPlayer player,
								   final EnumGuiType gui, final EntityNPCInterface npc, final int i, final int j, final int k) {
		if(!(player instanceof EntityPlayerMP))
			return;

		setEditingNpc(player, npc);
		sendExtraData(player, npc,gui, i, j, k);

		if(CustomNpcs.proxy.getServerGuiElement(gui.ordinal(), player, player.worldObj, i, j, k) != null){
			player.openGui(CustomNpcs.instance, gui.ordinal(), player.worldObj, i, j, k);
			return;
		}
		else{
            GuiOpenPacket.openGUI((EntityPlayerMP)player, gui, i, j, k);
		}
		ArrayList<String> list = getScrollData(player, gui, npc);
		if(list == null || list.isEmpty())
			return;

        ScrollListPacket.sendList((EntityPlayerMP)player, list);
    }

	public static void sendOpenGuiNoDelay(final EntityPlayer player,
								   final EnumGuiType gui, final EntityNPCInterface npc, final int i, final int j, final int k) {
		if(!(player instanceof EntityPlayerMP))
			return;

		setEditingNpc(player, npc);
		sendExtraData(player, npc,gui, i, j, k);

		if(CustomNpcs.proxy.getServerGuiElement(gui.ordinal(), player, player.worldObj, i, j, k) != null){
			player.openGui(CustomNpcs.instance, gui.ordinal(), player.worldObj, i, j, k);
			return;
		}
		else{
            GuiOpenPacket.openGUI((EntityPlayerMP)player, gui, i, j, k);
		}
		ArrayList<String> list = getScrollData(player, gui, npc);
		if(list == null || list.isEmpty())
			return;

        ScrollListPacket.sendList((EntityPlayerMP)player, list);
	}


	private static void sendExtraData(EntityPlayer player, EntityNPCInterface npc, EnumGuiType gui, int i, int j, int k) {
		if(gui == EnumGuiType.PlayerFollower || gui == EnumGuiType.PlayerFollowerHire || gui == EnumGuiType.PlayerTrader || gui == EnumGuiType.PlayerTransporter){
			sendRoleData(player, npc);
		}
	}

	private static ArrayList<String> getScrollData(EntityPlayer player, EnumGuiType gui, EntityNPCInterface npc) {
		if(gui == EnumGuiType.PlayerTransporter){
			RoleTransporter role = (RoleTransporter) npc.roleInterface;
			ArrayList<String> list = new ArrayList<String>();
	        TransportLocation location = role.getLocation();
	        String name = role.getLocation().name;
	        for(TransportLocation loc: location.category.getDefaultLocations()){
	        	if(!list.contains(loc.name)){
	        		list.add(loc.name);
	        	}
	        }
	        PlayerTransportData playerdata = PlayerDataController.Instance.getPlayerData(player).transportData;
	        for(int i : playerdata.transports){
	        	TransportLocation loc = TransportController.getInstance().getTransport(i);
	        	if(loc != null && location.category.locations.containsKey(loc.id)){
		        	if(!list.contains(loc.name)){
		        		list.add(loc.name);
		        	}
	        	}
	        }
	        list.remove(name);
	        return list;
		}
		return null;
	}
	public static void spawnParticle(Entity entity,String particle, int dimension){
        PacketHandler.Instance.sendTracking(new ParticlePacket(entity.posX, entity.posY, entity.posZ, entity.height, entity.width, entity.yOffset, particle), entity);
    }

	public static void spawnScriptedParticle(NBTTagCompound compound, int dimensionId){
        PacketHandler.Instance.sendToDimension(new ScriptedParticlePacket(compound), dimensionId);
	}

	public static void playSound(int id, ScriptSound sound) {
		List<EntityPlayer> list = MinecraftServer.getServer().getConfigurationManager().playerEntityList;
		for (EntityPlayer player : list) {
			NoppesUtilPlayer.playSoundTo((EntityPlayerMP) player, id, sound);
		}
	}

	public static void playSound(ScriptSound sound) {
		List<EntityPlayer> list = MinecraftServer.getServer().getConfigurationManager().playerEntityList;
		for (EntityPlayer player : list) {
			NoppesUtilPlayer.playSoundTo((EntityPlayerMP) player, sound);
		}
	}

	public static void stopSound(int id) {
		List<EntityPlayer> list = MinecraftServer.getServer().getConfigurationManager().playerEntityList;
		for (EntityPlayer player : list) {
			NoppesUtilPlayer.stopSoundFor((EntityPlayerMP) player, id);
		}
	}

	public static void pauseSounds() {
		List<EntityPlayer> list = MinecraftServer.getServer().getConfigurationManager().playerEntityList;
		for (EntityPlayer player : list) {
			NoppesUtilPlayer.pauseSoundsFor((EntityPlayerMP) player);
		}
	}

	public static void continueSounds() {
		List<EntityPlayer> list = MinecraftServer.getServer().getConfigurationManager().playerEntityList;
		for (EntityPlayer player : list) {
			NoppesUtilPlayer.continueSoundsFor((EntityPlayerMP) player);
		}
	}

	public static void stopSounds() {
		List<EntityPlayer> list = MinecraftServer.getServer().getConfigurationManager().playerEntityList;
		for (EntityPlayer player : list) {
			NoppesUtilPlayer.stopSoundsFor((EntityPlayerMP) player);
		}
	}

	public static void deleteNpc(EntityNPCInterface npc,EntityPlayer player) {
        PacketHandler.Instance.sendTracking(new DeleteNpcPacket(npc.getEntityId()), npc);
	}

	public static void createMobSpawner(int x, int y, int z, NBTTagCompound comp, EntityPlayer player) {
		comp.removeTag("Pos");
		ServerCloneController.Instance.cleanTags(comp);

		if(comp.getString("id").equalsIgnoreCase("entityhorse")){
			player.addChatMessage(new ChatComponentTranslation("Currently you cant create horse spawner, its a minecraft bug"));
			return;
		}

		player.worldObj.setBlock(x, y, z, Blocks.mob_spawner); //setBlock
		TileEntityMobSpawner tile = (TileEntityMobSpawner) player.worldObj.getTileEntity(x, y, z);
		MobSpawnerBaseLogic logic = tile.func_145881_a();

		logic.setRandomEntity(logic.new WeightedRandomMinecart(comp, comp.getString("id")));
	}

	public static void sendQuestCategoryData(EntityPlayerMP player) {
		Map<String,Integer> map = new HashMap<String,Integer>();
		for(QuestCategory category : QuestController.Instance.categories.values()){
			map.put(category.title, category.id);
		}
		sendScrollData(player, map);
	}

	public static void sendPlayerData(EnumPlayerData type, EntityPlayerMP player, String name) throws IOException {
		Map<String,Integer> map = new HashMap<String,Integer>();

		if(type == EnumPlayerData.Players){
			for(String username : PlayerDataController.Instance.nameUUIDs.keySet()){
				map.put(username, 0);
			}
			for(String username : MinecraftServer.getServer().getConfigurationManager().getAllUsernames()){
				map.put(username, 0);
			}
		}
		else{
			PlayerData playerdata = PlayerDataController.Instance.getDataFromUsername(name);
			if(type == EnumPlayerData.Dialog){
				PlayerDialogData data = playerdata.dialogData;

		        for(int questId : data.dialogsRead){
		        	Dialog dialog = DialogController.Instance.dialogs.get(questId);
		        	if(dialog == null)
		        		continue;
		        	map.put(dialog.category.title + ": " + dialog.title,questId);
		        }
			}
			else if(type == EnumPlayerData.Quest){
				PlayerQuestData data = playerdata.questData;

		        for(int questId : data.activeQuests.keySet()){
		        	Quest quest = QuestController.Instance.quests.get(questId);
		        	if(quest == null)
		        		continue;
		        	map.put(quest.category.title + ": " + quest.title + " (Active)",questId);
		        }
		        for(int questId : data.finishedQuests.keySet()){
		        	Quest quest = QuestController.Instance.quests.get(questId);
		        	if(quest == null)
		        		continue;
		        	map.put(quest.category.title + ": " + quest.title + " (Finished)",questId);
		        }
			}
			else if(type == EnumPlayerData.Transport){
				PlayerTransportData data = playerdata.transportData;

		        for(int questId : data.transports){
		        	TransportLocation location = TransportController.getInstance().getTransport(questId);
		        	if(location == null)
		        		continue;
		        	map.put(location.category.title + ": " + location.name,questId);
		        }
			}
			else if(type == EnumPlayerData.Bank){
				PlayerBankData data = playerdata.bankData;

		        for(int bankId : data.banks.keySet()){
		        	Bank bank = BankController.getInstance().banks.get(bankId);
		        	if(bank == null)
		        		continue;
		        	map.put(bank.name,bankId);
		        }
			}
			else if(type == EnumPlayerData.Factions){
				PlayerFactionData data = playerdata.factionData;
		        for(int factionId : data.factionData.keySet()){
		        	Faction faction = FactionController.getInstance().factions.get(factionId);
		        	if(faction == null)
		        		continue;
		        	map.put(faction.name + "(" + data.getFactionPoints(factionId) + ")" ,factionId);
		        }
			}
		}

		sendScrollData(player, map);
	}

	public static void removePlayerData(ByteBuf buffer, EntityPlayerMP player) throws IOException {
		int id = buffer.readInt();
		if(EnumPlayerData.values().length <= id)
			return;
		String name = ByteBufUtils.readString(buffer);
		EnumPlayerData type = EnumPlayerData.values()[id];
        EntityPlayer pl = MinecraftServer.getServer().getConfigurationManager().func_152612_a(name);
		PlayerData playerdata = null;
		if(pl == null)
			playerdata = PlayerDataController.Instance.getDataFromUsername(name);
		else
			playerdata = PlayerDataController.Instance.getPlayerData(pl);

        if(type == EnumPlayerData.Players){
			String fileType = ".json";
			if(ConfigMain.DatFormat){
				fileType = ".dat";
			}
            File file = new File(PlayerDataController.Instance.getSaveDir(), playerdata.uuid + fileType);
            if(file.exists())
            	file.delete();
            if(pl != null){
				PlayerDataController.Instance.removePlayerDataCache(pl.getUniqueID().toString());

				PlayerDataController.Instance.nameUUIDs.remove(name);
				PlayerDataController.Instance.savePlayerDataMap();

            	playerdata.setNBT(new NBTTagCompound());
                sendPlayerData(type, player, name);
                playerdata.save();
                return;
            }
        }
        if(type == EnumPlayerData.Quest){
        	PlayerQuestData data = playerdata.questData;
        	int questId = buffer.readInt();
        	data.activeQuests.remove(questId);
        	data.finishedQuests.remove(questId);
            playerdata.save();
        }
        if(type == EnumPlayerData.Dialog){
        	PlayerDialogData data = playerdata.dialogData;
        	data.dialogsRead.remove(buffer.readInt());
            playerdata.save();
        }
        if(type == EnumPlayerData.Transport){
        	PlayerTransportData data = playerdata.transportData;
        	data.transports.remove(buffer.readInt());
            playerdata.save();
        }
        if(type == EnumPlayerData.Bank){
        	PlayerBankData data = playerdata.bankData;
        	data.banks.remove(buffer.readInt());
            playerdata.save();
        }
        if(type == EnumPlayerData.Factions){
        	PlayerFactionData data = playerdata.factionData;
        	data.factionData.remove(buffer.readInt());
            playerdata.save();
        }
        if(pl != null) {
            SyncController.syncPlayer((EntityPlayerMP) pl);
        }
        sendPlayerData(type, player, name);
	}

	public static void regenPlayerData(EntityPlayerMP player) throws IOException {
		PlayerDataController.Instance.generatePlayerMap(player);
	}

	public static void sendRecipeData(EntityPlayerMP player, int size) {
		HashMap<String,Integer> map = new HashMap<String,Integer>();
		if(size == 3){
			for(RecipeCarpentry recipe : RecipeController.Instance.globalRecipes.values()){
				map.put(recipe.name, recipe.id);
			}
		} else if (size == 4){
            for(RecipeCarpentry recipe : RecipeController.Instance.carpentryRecipes.values()){
                map.put(recipe.name, recipe.id);
            }
        }
		else{
            for(RecipeAnvil recipe : RecipeController.Instance.anvilRecipes.values()){
                map.put(recipe.name, recipe.id);
            }
		}
		sendScrollData(player, map);
	}

	public static void sendDialogData(EntityPlayerMP player, DialogCategory category) {
		if(category == null)
			return;
		HashMap<String,Integer> map = new HashMap<String,Integer>();
		for(Dialog dialog : category.dialogs.values()){
			map.put(dialog.title, dialog.id);
		}
		sendScrollData(player, map);
	}

	public static void sendDialogGroup(EntityPlayerMP player, DialogCategory category) {
		if(category == null)
			return;
		HashMap<String,Integer> map = new HashMap<String,Integer>();
		for(Dialog dialog : category.dialogs.values()){
			map.put(dialog.title, dialog.id);
		}

		sendScrollGroup(player, map);
	}

	public static void sendQuestData(EntityPlayerMP player, QuestCategory category) {
		if(category == null)
			return;
		HashMap<String,Integer> map = new HashMap<String,Integer>();
		for(Quest quest : category.quests.values()){
			map.put(quest.title, quest.id);
		}
		sendScrollData(player, map);
	}

	public static void sendQuestGroup(EntityPlayerMP player, QuestCategory category) {
		if(category == null)
			return;
		HashMap<String,Integer> map = new HashMap<String,Integer>();
		for(Quest quest : category.quests.values()){
			map.put(quest.title, quest.id);
		}

		sendScrollGroup(player, map);
	}

    public static void sendCustomEffectDataAll(EntityPlayerMP player) {
        Map<String, Integer> map = new HashMap<String, Integer>();
        for (CustomEffect effect : StatusEffectController.getInstance().customEffects.values()) {
            map.put(effect.name, effect.id);
        }
        ScrollDataPacket.sendScrollData(player, map);
    }

	public static void sendTransportCategoryData(EntityPlayerMP player) {
		HashMap<String,Integer> map = new HashMap<String,Integer>();
		for(TransportCategory category : TransportController.getInstance().categories.values()){
			map.put(category.title, category.id);
		}
		sendScrollData(player, map);
	}

	public static void sendTransportData(EntityPlayerMP player, int categoryid) {
		TransportCategory category = TransportController.getInstance().categories.get(categoryid);
		if(category == null)
			return;
		HashMap<String,Integer> map = new HashMap<String,Integer>();
		for(TransportLocation transport : category.locations.values()){
			map.put(transport.name, transport.id);
		}
		sendScrollData(player, map);
	}


	public static void sendNpcDialogs(EntityPlayer player) {
		EntityNPCInterface npc = getEditingNpc(player);
		if(npc == null)
			return;
		for(int pos : npc.dialogs.keySet()){
			DialogOption option = npc.dialogs.get(pos);
			if(option == null || !option.hasDialog())
				continue;

			NBTTagCompound compound = option.writeNBT();
			compound.setInteger("Position", pos);
            GuiDataPacket.sendGuiData((EntityPlayerMP)player, compound);
		}
	}

	public static DialogOption setNpcDialog(int slot, int dialogId, EntityPlayer player) throws IOException {
		return setNpcDialog(slot, dialogId, getEditingNpc(player));
	}

	public static DialogOption setNpcDialog(int slot, int dialogId, EntityNPCInterface npc) {
		if(npc == null)
			return null;
		if(!npc.dialogs.containsKey(slot))
			npc.dialogs.put(slot, new DialogOption());

		DialogOption option = npc.dialogs.get(slot);
		option.dialogId = dialogId;
		if(option.hasDialog())
			option.title = option.getDialog().title;

		return option;
	}

	public static void saveTileEntity(EntityPlayerMP player, NBTTagCompound compound){
		int x = compound.getInteger("x");
		int y = compound.getInteger("y");
		int z = compound.getInteger("z");

		TileEntity tile = player.worldObj.getTileEntity(x, y, z);
		if(tile != null)
			tile.readFromNBT(compound);

	}

    public static void setClonerGui(EntityPlayerMP player, int x, int y, int z){
        if(player == null)
            return;
        GuiOpenPacket.openGUI(player, EnumGuiType.Cloner, x, y, z);
    }

    public static void setTeleporterGUI(EntityPlayerMP player){
        if(player == null)
            return;
        PacketHandler.Instance.sendToPlayer(new GuiTeleporterPacket(), player);
    }

	public static void setRecipeGui(EntityPlayerMP player, RecipeCarpentry recipe){
		if(recipe == null)
			return;
		if(!(player.openContainer instanceof ContainerManageRecipes))
			return;

		ContainerManageRecipes container = (ContainerManageRecipes) player.openContainer;
		container.setRecipe(recipe);
        GuiDataPacket.sendGuiData((EntityPlayerMP)player, recipe.writeNBT());
	}

    public static void setRecipeAnvilGui(EntityPlayerMP player, RecipeAnvil recipe){
        if(recipe == null)
            return;
        if(!(player.openContainer instanceof ContainerManageRecipes))
            return;

        ContainerManageRecipes container = (ContainerManageRecipes) player.openContainer;
        container.setRecipe(recipe);
        GuiDataPacket.sendGuiData((EntityPlayerMP)player, recipe.writeNBT());
    }

	public static void sendBank(EntityPlayerMP player,Bank bank) {
		NBTTagCompound compound = new NBTTagCompound();
		bank.writeEntityToNBT(compound);
        GuiDataPacket.sendGuiData((EntityPlayerMP)player, compound);

		if(player.openContainer instanceof ContainerManageBanks){
			((ContainerManageBanks)player.openContainer).setBank(bank);
		}
		player.sendContainerAndContentsToPlayer(player.openContainer, player.openContainer.getInventory());
	}

	public static void sendNearbyNpcs(EntityPlayerMP player) {
		List<EntityNPCInterface> npcs = player.worldObj.getEntitiesWithinAABB(EntityNPCInterface.class, player.boundingBox.expand(120, 120, 120));
		HashMap<String,Integer> map = new HashMap<String,Integer>();
		for(EntityNPCInterface npc : npcs){
			if(npc.isDead)
				continue;
			float distance = player.getDistanceToEntity(npc);
	        DecimalFormat df = new DecimalFormat("#.#");
	        String s = df.format(distance);
	        if(distance < 10)
	        	s = "0" + s;
			map.put(s + " : " + npc.display.name, npc.getEntityId());
		}

		sendScrollData(player, map);
	}

	public static void sendGuiError(EntityPlayer player, int i) {
        GuiErrorPacket.errorGUI((EntityPlayerMP)player, i, new NBTTagCompound());
	}

	public static void sendGuiClose(EntityPlayerMP player, int i, NBTTagCompound comp) {
		if(player.openContainer != player.inventoryContainer){
			player.openContainer = player.inventoryContainer;
		}
        GuiClosePacket.closeGUI(player, i, comp);
	}

	public static Entity spawnCloneWithProtection(NBTTagCompound compound, int x, int y,
			int z, World worldObj) {
		ServerCloneController.Instance.cleanTags(compound);
		compound.setTag("Pos", NBTTags.nbtDoubleList(x + 0.5, y, z + 0.5));
		Entity entity = EntityList.createEntityFromNBT(compound, worldObj);
		if(entity == null){
			return null;
		}
		entity.setPosition((double) x + 0.5, (double) y, (double) z + 0.5);
		if(entity instanceof EntityNPCInterface){
			EntityNPCInterface npc = (EntityNPCInterface) entity;
			npc.ai.startPos = new int[]{MathHelper.floor_double(npc.posX),MathHelper.floor_double(npc.posY),MathHelper.floor_double(npc.posZ)};
			npc.ticksExisted = 0;
			npc.totalTicksAlive = 0;
		}
		worldObj.spawnEntityInWorld(entity);
		entity.prevPosY = entity.lastTickPosY = entity.posY = y + 1;
		return entity;
	}

	public static Entity getEntityFromNBT(NBTTagCompound compound, int x, int y,
										  int z, World worldObj) {
		ServerCloneController.Instance.cleanTags(compound);
		compound.setTag("Pos", NBTTags.nbtDoubleList(x + 0.5, y + 1, z + 0.5));
		Entity entity = EntityList.createEntityFromNBT(compound, worldObj);
		if(entity == null){
			return null;
		}
		if(entity instanceof EntityNPCInterface){
			EntityNPCInterface npc = (EntityNPCInterface) entity;
			npc.ai.startPos = new int[]{MathHelper.floor_double(npc.posX),MathHelper.floor_double(npc.posY),MathHelper.floor_double(npc.posZ)};
			npc.ticksExisted = 0;
			npc.totalTicksAlive = 0;
		}
		return entity;
	}

	public static Entity spawnClone(NBTTagCompound compound, int x, int y,
									int z, World worldObj) {
		Entity entity = getEntityFromNBT(compound,x,y,z,worldObj);
		if(entity == null){
			return null;
		}

		entity.dimension = worldObj.provider.dimensionId;
		int i = MathHelper.floor_double(entity.posX / 16.0D);
		int j = MathHelper.floor_double(entity.posZ / 16.0D);
		if (!entity.forceSpawn && !worldObj.checkChunksExist(
				(int)entity.posX,(int)entity.posY,(int)entity.posZ,(int)entity.posX,(int)entity.posY,(int)entity.posZ)) {
			return null;
		}
		else {
			worldObj.getChunkFromChunkCoords(i, j).addEntity(entity);
			worldObj.loadedEntityList.add(entity);
			worldObj.onEntityAdded(entity);
			return entity;
		}
	}

    public static boolean isOp(EntityPlayer player) {
		return MinecraftServer.getServer().getConfigurationManager().func_152596_g(player.getGameProfile());
	}


    public static void GivePlayerItem(Entity entity, EntityPlayer player, ItemStack item) {
        if (entity.worldObj.isRemote || item == null) {
            return;
        }
        item = item.copy();
        float f = 0.7F;
        double d = (double) (entity.worldObj.rand.nextFloat() * f) + (double) (1.0F - f);
        double d1 = (double) (entity.worldObj.rand.nextFloat() * f) + (double) (1.0F - f);
        double d2 = (double) (entity.worldObj.rand.nextFloat() * f) + (double) (1.0F - f);
        EntityItem entityitem = new EntityItem(entity.worldObj, entity.posX + d, entity.posY + d1, entity.posZ + d2,
                item);
        entityitem.delayBeforeCanPickup = 2;
        entity.worldObj.spawnEntityInWorld(entityitem);

        int i = item.stackSize;

        if (player.inventory.addItemStackToInventory(item)) {
            entity.worldObj.playSoundAtEntity(entityitem, "random.pop", 0.2F,
                    ((entity.worldObj.rand.nextFloat() - entity.worldObj.rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
            player.onItemPickup(entityitem, i);

            if (item.stackSize <= 0) {
                entityitem.setDead();
            }
        }
    }
	public static EntityPlayer getPlayer(UUID id) {
		List<EntityPlayer> list = MinecraftServer.getServer().getConfigurationManager().playerEntityList;
		for(EntityPlayer player : list){
			if(id.equals(player.getUniqueID()))
				return player;
		}
        return null;
	}

    public static Entity getEntityFromUUID(World world, UUID uuid) {
        for (Object obj : world.loadedEntityList) {
            if (obj instanceof Entity) {
                Entity entity = (Entity) obj;
                if (entity.getUniqueID().equals(uuid)) {
                    return entity;
                }
            }
        }
        return null;
    }

	static public EntityPlayer getPlayerByName(String playername){
		return MinecraftServer.getServer().getConfigurationManager().func_152612_a(playername);
	}

	public static Entity GetDamageSource(DamageSource damagesource) {
		Entity entity = damagesource.getEntity();
		if(entity == null) {
			entity = damagesource.getSourceOfDamage();
		}

		if(entity instanceof EntityArrow && ((EntityArrow)entity).shootingEntity instanceof EntityLivingBase) {
			entity = ((EntityArrow)entity).shootingEntity;
		} else if(entity instanceof EntityThrowable) {
			entity = ((EntityThrowable)entity).getThrower();
		}

		return entity;
	}

	public static void isGUIOpen(ByteBuf buffer, EntityPlayer player) throws IOException {
		PlayerData playerdata = PlayerDataController.Instance.getPlayerData(player);
		boolean isGUIOpen = buffer.readBoolean();
		playerdata.setGUIOpen(isGUIOpen);
	}

	public static boolean IsItemStackNull(ItemStack is) {
		return is == null || is.stackSize == 0 || is.getItem() == null;
	}

	public static String millisToTime(long millis) {
		long seconds = millis / 1000;
		long minutes = seconds / 60;
		long hours = minutes / 60;
		long days = hours / 24;
		seconds %= 60;
		minutes %= 60;
		hours %= 24;

		StringBuilder sb = new StringBuilder();
		if (days > 0) {
			sb.append(days).append(" day").append(days == 1 ? "" : "s").append(", ");
		}
		if (hours > 0) {
			sb.append(hours).append(" hour").append(hours == 1 ? "" : "s").append(", ");
		}
		if (minutes > 0) {
			sb.append(minutes).append(" minute").append(minutes == 1 ? "" : "s").append(", ");
		}
		sb.append(seconds).append(" second").append(seconds == 1 ? "" : "s");
		return sb.toString();
	}
}
