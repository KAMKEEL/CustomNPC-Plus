package noppes.npcs;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import kamkeel.npcs.controllers.SyncController;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumSoundOperation;
import kamkeel.npcs.network.enums.EnumSyncAction;
import kamkeel.npcs.network.enums.EnumSyncType;
import kamkeel.npcs.network.packets.data.ClonerPacket;
import kamkeel.npcs.network.packets.data.MarkDataPacket;
import kamkeel.npcs.network.packets.data.SoundManagementPacket;
import kamkeel.npcs.network.packets.data.VillagerListPacket;
import kamkeel.npcs.network.packets.data.gui.GuiOpenPacket;
import kamkeel.npcs.network.packets.data.large.SyncPacket;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.village.MerchantRecipeList;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import noppes.npcs.blocks.tiles.TileBanner;
import noppes.npcs.config.ConfigDebug;
import noppes.npcs.config.ConfigMain;
import noppes.npcs.constants.*;
import noppes.npcs.controllers.PartyController;
import noppes.npcs.controllers.PlayerDataController;
import noppes.npcs.controllers.ServerCloneController;
import noppes.npcs.controllers.data.*;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.items.ItemExcalibur;
import noppes.npcs.items.ItemShield;
import noppes.npcs.items.ItemSoulstoneEmpty;
import noppes.npcs.quests.QuestKill;
import noppes.npcs.roles.RoleFollower;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ServerEventsHandler {

	public static EntityVillager Merchant;
	public static Entity mounted;

	@SubscribeEvent
	public void invoke(EntityInteractEvent event) {
		ItemStack item = event.entityPlayer.getCurrentEquippedItem();
		if(item == null)
			return;
		boolean isRemote = event.entityPlayer.worldObj.isRemote;
		boolean npcInteracted = event.target instanceof EntityNPCInterface;

		if(!isRemote && ConfigMain.OpsOnly && !MinecraftServer.getServer().getConfigurationManager().func_152596_g(event.entityPlayer.getGameProfile())){
			return;
		}

		if(!isRemote && item.getItem() == CustomItems.soulstoneEmpty) {
			((ItemSoulstoneEmpty)item.getItem()).store((EntityLivingBase)event.target, item, event.entityPlayer);
			if(ConfigDebug.PlayerLogging && FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER){
				LogWriter.script(String.format("[%s] (Player) %s PICKED ENTITY %s", "SOULSTONE", event.entityPlayer.getCommandSenderName(), event.target));
			}
		}

		if(item.getItem() == CustomItems.wand && npcInteracted && !isRemote){
			if (!CustomNpcsPermissions.hasPermission(event.entityPlayer, CustomNpcsPermissions.NPC_GUI)){
				return;
			}
			event.setCanceled(true);
			NoppesUtilServer.sendOpenGui(event.entityPlayer, EnumGuiType.MainMenuDisplay, (EntityNPCInterface) event.target);
			if(ConfigDebug.PlayerLogging && FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER){
				LogWriter.script(String.format("[%s] (Player) %s OPEN NPC %s (%s, %s, %s) [%s]", "WAND", event.entityPlayer.getCommandSenderName(), ((EntityNPCInterface)(event.target)).display.getName(), (int)(event.target).posX, (int)(event.target).posY, (int)(event.target).posZ,  (event.target).worldObj.getWorldInfo().getWorldName()));
			}
		}
		else if(item.getItem() == CustomItems.cloner && !isRemote && !(event.target instanceof EntityPlayer)){
            if(!CustomNpcsPermissions.hasPermission(event.entityPlayer, CustomNpcsPermissions.TOOL_CLONER))
                return;
			NBTTagCompound compound = new NBTTagCompound();
			if(!event.target.writeToNBTOptional(compound))
				return;
			PlayerData data = PlayerDataController.Instance.getPlayerData(event.entityPlayer);
			ServerCloneController.Instance.cleanTags(compound);
            PacketHandler.Instance.sendToPlayer(new ClonerPacket(compound), (EntityPlayerMP)event.entityPlayer);
			data.cloned = compound;
			if (event.target instanceof EntityNPCInterface) {
				NoppesUtilServer.setEditingNpc(event.entityPlayer, (EntityNPCInterface) event.target);
			}
			event.setCanceled(true);
		}
		else if(item.getItem() == CustomItems.scripter && !isRemote && npcInteracted){
			if(!CustomNpcsPermissions.hasPermission(event.entityPlayer, CustomNpcsPermissions.TOOL_SCRIPTER))
				return;
			NoppesUtilServer.setEditingNpc(event.entityPlayer, (EntityNPCInterface)event.target);
			event.setCanceled(true);
            GuiOpenPacket.openGUI((EntityPlayerMP)event.entityPlayer, EnumGuiType.Script, 0, 0, 0);
            if(ConfigDebug.PlayerLogging && FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER){
				LogWriter.script(String.format("[%s] (Player) %s OPEN NPC %s (%s, %s, %s) [%s]", "SCRIPTER", event.entityPlayer.getCommandSenderName(), ((EntityNPCInterface)(event.target)).display.getName(), (int)(event.target).posX, (int)(event.target).posY, (int)(event.target).posZ,  (event.target).worldObj.getWorldInfo().getWorldName()));
			}
		}
		else if(item.getItem() == CustomItems.mount){
			if(!CustomNpcsPermissions.hasPermission(event.entityPlayer, CustomNpcsPermissions.TOOL_MOUNTER))
				return;
			event.setCanceled(true);
			mounted = event.target;
			if(isRemote)
				CustomNpcs.proxy.openGui(MathHelper.floor_double(mounted.posX), MathHelper.floor_double(mounted.posY), MathHelper.floor_double(mounted.posZ), EnumGuiType.MobSpawnerMounter, event.entityPlayer);
		}
		else if(item.getItem() == CustomItems.wand && !isRemote && event.target instanceof EntityVillager){
			if(!CustomNpcsPermissions.hasPermission(event.entityPlayer, CustomNpcsPermissions.EDIT_VILLAGER))
				return;
			event.setCanceled(true);
			Merchant = (EntityVillager)event.target;

			if(!isRemote){
				EntityPlayerMP player = (EntityPlayerMP) event.entityPlayer;
				player.openGui(CustomNpcs.instance, EnumGuiType.MerchantAdd.ordinal(), player.worldObj, 0, 0, 0);
				MerchantRecipeList merchantrecipelist = Merchant.getRecipes(player);

				if (merchantrecipelist != null)
				{
                    PacketHandler.Instance.sendToPlayer(new VillagerListPacket(merchantrecipelist), player);
				}
			}
		}
	}

	@SubscribeEvent
	public void partyDamagedEvent(LivingAttackEvent event) {
		if(!(event.source != null && event.source.getEntity() instanceof EntityPlayer) || !(event.entityLiving instanceof EntityPlayer) || FMLCommonHandler.instance().getEffectiveSide().isClient())
			return;

        // Check for Friendly Fire
        if(ConfigMain.PartyFriendlyFireEnabled){
            EntityPlayer sourcePlayer = (EntityPlayer) event.source.getEntity();
            PlayerData playerData = PlayerDataController.Instance.getPlayerData(sourcePlayer);
            PlayerData targetData = PlayerDataController.Instance.getPlayerData((EntityPlayer) event.entityLiving);
            if (playerData.partyUUID != null && playerData.partyUUID.equals(targetData.partyUUID)) {
                Party party = PartyController.Instance().getParty(playerData.partyUUID);
                if(party != null && !party.friendlyFire())
                    event.setCanceled(true);
            }
        }
	}

	@SubscribeEvent
	public void invoke(LivingHurtEvent event) {
		if(!(event.entityLiving instanceof EntityPlayer))
			return;

		EntityPlayer player = (EntityPlayer) event.entityLiving;
		if(event.source.isUnblockable() || event.source.isFireDamage())
			return;
		if(!player.isBlocking())
			return;
		ItemStack item = player.getCurrentEquippedItem();
		if(item == null || !(item.getItem() instanceof ItemShield))
			return;
		if(((ItemShield)item.getItem()).material.getDamageVsEntity() < player.getRNG().nextInt(9))
			return;
		float damage = item.getItemDamage() + event.ammount;

		item.damageItem((int) event.ammount, player);

		if(damage > item.getMaxDamage())
			event.ammount = damage - item.getMaxDamage();
		else{
			event.ammount = 0;
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public void invoke(PlayerInteractEvent event) {
		EntityPlayer player = event.entityPlayer;
		Block block = player.worldObj.getBlock(event.x, event.y, event.z);
		if(event.action == Action.LEFT_CLICK_BLOCK && player.getHeldItem() != null && player.getHeldItem().getItem() == CustomItems.teleporter){
			event.setCanceled(true);
		}

		if(block == Blocks.crafting_table && event.action == Action.RIGHT_CLICK_BLOCK && !player.worldObj.isRemote){
            PacketHandler.Instance.sendToPlayer(new SyncPacket(
                EnumSyncType.WORKBENCH_RECIPES,
                EnumSyncAction.RELOAD,
                -1,
                SyncController.workbenchNBT()
            ), (EntityPlayerMP) player);
		}
		if(block == CustomItems.carpentyBench && event.action == Action.RIGHT_CLICK_BLOCK && !player.worldObj.isRemote){
            PacketHandler.Instance.sendToPlayer(new SyncPacket(
                EnumSyncType.CARPENTRY_RECIPES,
                EnumSyncAction.RELOAD,
                -1,
                SyncController.carpentryNBT()
            ), (EntityPlayerMP) player);
		}
		if((block == CustomItems.banner || block == CustomItems.wallBanner || block == CustomItems.sign)  && event.action == Action.RIGHT_CLICK_BLOCK){
			ItemStack item = player.inventory.getCurrentItem();
			if(item == null || item.getItem() == null)
				return;
			int y = event.y;
			int meta = player.worldObj.getBlockMetadata(event.x, event.y, event.z);
			if(meta >= 7)
				y--;
			TileBanner tile = (TileBanner)player.worldObj.getTileEntity(event.x, y, event.z);
			if(!tile.canEdit()){
				if(item.getItem() == CustomItems.wand && CustomNpcsPermissions.hasPermission(player, CustomNpcsPermissions.EDIT_BLOCKS)){
					tile.time = System.currentTimeMillis();
					if(player.worldObj.isRemote)
						player.addChatComponentMessage(new ChatComponentTranslation("availability.editIcon"));
				}
				return;
			}

			if(!player.worldObj.isRemote){
                if(block == CustomItems.banner || block == CustomItems.wallBanner){
                    // If sneaking and using shear
                    if(player.isSneaking() && item.getItem() == Items.shears){
                        int currentVariantIndex = tile.bannerTrim.ordinal();
                        int nextVariantIndex = (currentVariantIndex + 1) % EnumBannerVariant.values().length;
                        tile.bannerTrim = EnumBannerVariant.values()[nextVariantIndex];

                        player.worldObj.markBlockForUpdate(event.x, y, event.z);
                        event.setCanceled(true);
                        return;
                    }
                }

				tile.icon = item.copy();
				player.worldObj.markBlockForUpdate(event.x, y, event.z);
				event.setCanceled(true);
			}
		}
	}

	@SubscribeEvent
	public void invoke(LivingDeathEvent event) {
		if(event.entityLiving.worldObj.isRemote)
			return;
		if(event.source.getEntity() != null){
			if(event.source.getEntity() instanceof EntityPlayer){
				doExcalibur((EntityPlayer) event.source.getEntity(),event.entityLiving);
			}

			if(event.source.getEntity() instanceof EntityNPCInterface){
				EntityNPCInterface npc = (EntityNPCInterface) event.source.getEntity();
				Line line = npc.advanced.getKillLine();
				if(line != null)
					npc.saySurrounding(line.formatTarget(event.entityLiving));

				EventHooks.onNPCKilledEntity(npc,event.entityLiving);
			}

			EntityPlayer player = null;
			if(event.source.getEntity() instanceof EntityPlayer)
				player = (EntityPlayer) event.source.getEntity();
			else if(event.source.getEntity() instanceof EntityNPCInterface && ((EntityNPCInterface)event.source.getEntity()).advanced.role == EnumRoleType.Follower)
				player = ((RoleFollower)((EntityNPCInterface)event.source.getEntity()).roleInterface).owner;
			if(player != null){
				doQuest(player, event.entityLiving, true);

				if(event.entityLiving instanceof EntityNPCInterface)
					doFactionPoints(player, (EntityNPCInterface)event.entityLiving);
			}
		}
		if(event.entityLiving instanceof EntityPlayer){
			PlayerData data = PlayerDataController.Instance.getPlayerData((EntityPlayer)event.entityLiving);
			data.save();
		}
	}

	@SubscribeEvent
	public void onPlayerClone(PlayerEvent.Clone event) {
		if (event.entity.worldObj.isRemote)
			return;

		NBTTagCompound storedData = event.original.getEntityData().getCompoundTag("CNPCStoredData");
		if (!storedData.hasNoTags()) {
			event.entityPlayer.getEntityData().setTag("CNPCStoredData", storedData);
		}
	}

	private void doExcalibur(EntityPlayer player, EntityLivingBase entity) {
		ItemStack item = player.getCurrentEquippedItem();
		if(item == null || item.getItem() != CustomItems.excalibur)
			return;
        PacketHandler.Instance.sendToPlayer(new SoundManagementPacket(EnumSoundOperation.PLAY_MUSIC, "customnpcs:songs.excalibur"), (EntityPlayerMP)player);
		player.addChatMessage(new ChatComponentTranslation("<" + StatCollector.translateToLocal(item.getItem().getUnlocalizedName() + ".name") + "> " + ItemExcalibur.quotes[player.getRNG().nextInt(ItemExcalibur.quotes.length)]));
	}

	private void doFactionPoints(EntityPlayer player, EntityNPCInterface npc) {
		npc.advanced.factions.addPoints(player);
	}

	private void doQuest(EntityPlayer player, EntityLivingBase entity, boolean all) {
        PlayerData playerData = PlayerData.get(player);
        if (playerData == null) {
            return;
        }

        PlayerQuestData questData = playerData.questData;
        if (questData == null) {
            return;
        }
        boolean checkCompletion = false;
        String entityName = EntityList.getEntityString(entity);
        if(entity instanceof EntityPlayer)
            entityName = "Player";

        Party party = playerData.getPlayerParty();
        Quest partyQuest = null;
        if(party != null){
            if(party.getQuestData() != null){
                partyQuest = party.getQuestData().quest;
                if(partyQuest != null && (partyQuest.type == EnumQuestType.Kill || partyQuest.type == EnumQuestType.AreaKill ))
                    doPartyQuest(player, party, entity);
                else
                    partyQuest = null;
            }
        }

        ArrayList<QuestData> activeQuestValues = new ArrayList<>(questData.activeQuests.values());
		for(QuestData data : activeQuestValues){
            if (data.quest == null) {
                continue;
            }

			if (data.quest.type != EnumQuestType.Kill && data.quest.type != EnumQuestType.AreaKill)
				continue;

            if(partyQuest != null && partyQuest.getId() == data.quest.getId())
                continue;

            if(data.quest.partyOptions.allowParty && data.quest.partyOptions.onlyParty)
                continue;

            if(data.quest.type == EnumQuestType.AreaKill && all){
                List<EntityPlayer> list = player.worldObj.getEntitiesWithinAABB(EntityPlayer.class, entity.boundingBox.expand(10, 10, 10));
                for(EntityPlayer pl : list)
                    if(pl != player)
                        doQuest(pl, entity, false);

            }

            String name = entityName;
			QuestKill quest = (QuestKill) data.quest.questInterface;
			Class entityType = EntityNPCInterface.class;
			if (quest.targetType == 2) {
				try {
					entityType = Class.forName(quest.customTargetType);
				} catch (ClassNotFoundException notFoundException) {
					continue;
				}
			}

			if (quest.targetType > 0 && !(entityType.isInstance(entity)))
				continue;

            if (entity.getCommandSenderName() != null && quest.targets != null && quest.targets.containsKey(entity.getCommandSenderName())) {
                name = entity.getCommandSenderName();
            } else if (entity.getCommandSenderName() == null || quest.targets == null || !quest.targets.containsKey(name)) {
                continue;
            }

			checkCompletion = true;

            HashMap<String, Integer> killed = quest.getKilled(data);
			if (!killed.containsKey(name)) {
				killed.put(name, 1);
			} else if(killed.get(name) < quest.targets.get(name)) {
				int amount = killed.get(name);
				killed.put(name, amount + 1);
			}
			quest.setKilled(data, killed);
            playerData.updateClient = true;
		}
		if(!checkCompletion)
			return;

		questData.checkQuestCompletion(playerData,EnumQuestType.Kill);
	}

    private void doPartyQuest(EntityPlayer player, Party party, EntityLivingBase entity){
        PlayerData pdata = PlayerData.get(player);
        QuestData data = party.getQuestData();
        if(data == null)
            return;

        if(pdata == null)
            return;

        if (data.quest.type != EnumQuestType.Kill && data.quest.type != EnumQuestType.AreaKill)
            return;

        String name = EntityList.getEntityString(entity);
        QuestKill quest = (QuestKill) data.quest.questInterface;
        if(data.quest.partyOptions.objectiveRequirement == EnumPartyObjectives.Leader && !party.getLeaderUUID().equals(player.getUniqueID()))
            return;

        Class entityType = EntityNPCInterface.class;
        if (quest.targetType == 2) {
            try {
                entityType = Class.forName(quest.customTargetType);
            } catch (ClassNotFoundException notFoundException) {
                return;
            }
        }

        if (quest.targetType > 0 && !(entityType.isInstance(entity)))
            return;

        if (quest.targets.containsKey(entity.getCommandSenderName()))
            name = entity.getCommandSenderName();
        else if (!quest.targets.containsKey(name))
            return;

        if(data.quest.partyOptions.objectiveRequirement == EnumPartyObjectives.All){
            HashMap<String, Integer> killed = quest.getPlayerKilled(data, player.getCommandSenderName());
            if (!killed.containsKey(name)) {
                killed.put(name, 1);
            } else if(killed.get(name) < quest.targets.get(name)) {
                int amount = killed.get(name);
                killed.put(name, amount + 1);
            }
            quest.setPlayerKilled(data, killed, player.getCommandSenderName());
        }
        else {
            HashMap<String, Integer> killed = quest.getKilled(data);
            if (!killed.containsKey(name)) {
                killed.put(name, 1);
            } else if(killed.get(name) < quest.targets.get(name)) {
                int amount = killed.get(name);
                killed.put(name, amount + 1);
            }
            quest.setKilled(data, killed);
            pdata.updateClient = true;
        }

        PartyController.Instance().pingPartyQuestObjectiveUpdate(party);
        PartyController.Instance().checkQuestCompletion(party, EnumQuestType.Kill);
    }

	@SubscribeEvent
	public void world(PlayerEvent.SaveToFile event){
		PlayerData data = PlayerDataController.Instance.getPlayerData((EntityPlayer) event.entity);
		data.save();
	}

	@SubscribeEvent
	public void world(EntityJoinWorldEvent event){
		if(event.world.isRemote || !(event.entity instanceof EntityPlayer))
			return;
		PlayerData data = PlayerDataController.Instance.getPlayerData((EntityPlayer) event.entity);
		data.updateCompanion(event.world);
	}

	@SubscribeEvent
	public void populateChunk(PopulateChunkEvent.Post event){
		NPCSpawning.performWorldGenSpawning(event.world, event.chunkX, event.chunkZ, event.rand);
	}

    @SubscribeEvent
    public void playerTracking(PlayerEvent.StartTracking event){
        if(!(event.target instanceof EntityPlayerMP || event.target instanceof EntityNPCInterface) || event.target.worldObj.isRemote)
            return;

        AnimationData animationData = AnimationData.getData(event.target);
        if (animationData != null && animationData.isClientAnimating()) {
            AnimationData playerAnimData = AnimationData.getData(event.entityPlayer);
            if (playerAnimData != null) {
                Animation currentAnimation = animationData.currentClientAnimation;
                NBTTagCompound compound = currentAnimation.writeToNBT();
                playerAnimData.viewAnimation(currentAnimation, animationData, compound,
                    animationData.isClientAnimating(), currentAnimation.currentFrame, currentAnimation.currentFrameTime);
            }
        }

        if (event.target instanceof EntityNPCInterface) {
            MarkData data = MarkData.get((EntityNPCInterface) event.target);
            if (data.marks.isEmpty())
                return;
            PacketHandler.Instance.sendToPlayer(new MarkDataPacket(event.target.getEntityId(), data.getNBT()), (EntityPlayerMP)event.entityPlayer);
        }
    }

    @SubscribeEvent
    public void entityTick(LivingEvent.LivingUpdateEvent event) {
        AnimationData data = AnimationData.getData(event.entityLiving);
        if (data != null) {
            data.increaseTime();
        }
    }
}
