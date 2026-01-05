package noppes.npcs;

import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumSoundOperation;
import kamkeel.npcs.network.packets.data.DisableMouseInputPacket;
import kamkeel.npcs.network.packets.data.OverlayQuestTrackingPacket;
import kamkeel.npcs.network.packets.data.QuestCompletionPacket;
import kamkeel.npcs.network.packets.data.SoundManagementPacket;
import kamkeel.npcs.network.packets.data.SwingPlayerArmPacket;
import kamkeel.npcs.network.packets.data.gui.GuiClosePacket;
import kamkeel.npcs.network.packets.data.gui.IsGuiOpenPacket;
import kamkeel.npcs.network.packets.data.large.GuiDataPacket;
import kamkeel.npcs.network.packets.data.large.PartyDataPacket;
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.WorldServer;
import net.minecraftforge.oredict.OreDictionary;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.handler.data.IQuestObjective;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.constants.EnumOptionType;
import noppes.npcs.constants.EnumPartyExchange;
import noppes.npcs.constants.EnumPartyObjectives;
import noppes.npcs.constants.EnumQuestCompletion;
import noppes.npcs.constants.EnumQuestType;
import noppes.npcs.constants.EnumRoleType;
import noppes.npcs.containers.ContainerNPCBankInterface;
import noppes.npcs.containers.ContainerNPCFollower;
import noppes.npcs.containers.ContainerNPCFollowerHire;
import noppes.npcs.controllers.BankController;
import noppes.npcs.controllers.DialogController;
import noppes.npcs.controllers.PartyController;
import noppes.npcs.controllers.PlayerDataController;
import noppes.npcs.controllers.PlayerQuestController;
import noppes.npcs.controllers.QuestController;
import noppes.npcs.controllers.TransportController;
import noppes.npcs.controllers.data.Bank;
import noppes.npcs.controllers.data.BankData;
import noppes.npcs.controllers.data.Dialog;
import noppes.npcs.controllers.data.DialogOption;
import noppes.npcs.controllers.data.Line;
import noppes.npcs.controllers.data.Party;
import noppes.npcs.controllers.data.PartyOptions;
import noppes.npcs.controllers.data.PlayerBankData;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.controllers.data.PlayerQuestData;
import noppes.npcs.controllers.data.PlayerTransportData;
import noppes.npcs.controllers.data.Quest;
import noppes.npcs.controllers.data.QuestCategory;
import noppes.npcs.controllers.data.QuestData;
import noppes.npcs.controllers.data.TransportLocation;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.RoleFollower;
import noppes.npcs.scripted.NpcAPI;
import noppes.npcs.scripted.ScriptSound;
import noppes.npcs.scripted.event.PartyEvent;
import noppes.npcs.scripted.event.player.DialogEvent;
import noppes.npcs.scripted.event.player.QuestEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class NoppesUtilPlayer {

    public static void changeFollowerState(EntityPlayerMP player, EntityNPCInterface npc) {
        if (npc.advanced.role != EnumRoleType.Follower)
            return;

        RoleFollower role = (RoleFollower) npc.roleInterface;
        EntityPlayer owner = role.owner;
        if (owner == null || !owner.getCommandSenderName().equals(player.getCommandSenderName()))
            return;

        role.isFollowing = !role.isFollowing;
    }

    public static void hireFollower(EntityPlayerMP player, EntityNPCInterface npc) {
        if (npc.advanced.role != EnumRoleType.Follower)
            return;
        Container con = player.openContainer;
        if (con == null || !(con instanceof ContainerNPCFollowerHire))
            return;

        ContainerNPCFollowerHire container = (ContainerNPCFollowerHire) con;
        RoleFollower role = (RoleFollower) npc.roleInterface;
        followerBuy(role, container.currencyMatrix, player, npc);
    }

    public static void extendFollower(EntityPlayerMP player, EntityNPCInterface npc) {
        if (npc.advanced.role != EnumRoleType.Follower)
            return;
        Container con = player.openContainer;
        if (con == null || !(con instanceof ContainerNPCFollower))
            return;

        ContainerNPCFollower container = (ContainerNPCFollower) con;
        RoleFollower role = (RoleFollower) npc.roleInterface;
        followerBuy(role, container.currencyMatrix, player, npc);
    }

    public static void transport(EntityPlayerMP player, EntityNPCInterface npc, String location) {
        TransportLocation loc = TransportController.getInstance().getTransport(location);
        PlayerTransportData playerdata = PlayerData.get(player).transportData;

        if (loc == null || !loc.isDefault() && !playerdata.transports.contains(loc.id))
            return;

        teleportPlayer(player, loc.posX, loc.posY, loc.posZ, loc.dimension);
    }

    public static void teleportPlayer(EntityPlayerMP player, double posX, double posY, double posZ, int dimension) {
        // Dismount from NPC mount before teleportation to prevent mount desync
        if (player.ridingEntity instanceof EntityNPCInterface) {
            player.mountEntity(null);
        }

        if (player.dimension != dimension) {
            MinecraftServer server = MinecraftServer.getServer();
            WorldServer wor = server.worldServerForDimension(dimension);
            if (wor == null) {
                player.addChatMessage(new ChatComponentText("Broken transporter. Dimension does not exist"));
                return;
            }
            player.setLocationAndAngles(posX, posY, posZ, player.rotationYaw, player.rotationPitch);
            server.getConfigurationManager().transferPlayerToDimension(player, dimension, new CustomTeleporter(wor));
            player.playerNetServerHandler.setPlayerLocation(posX, posY, posZ, player.rotationYaw, player.rotationPitch);

            if (!wor.playerEntities.contains(player))
                wor.spawnEntityInWorld(player);
        } else {
            player.playerNetServerHandler.setPlayerLocation(posX, posY, posZ, player.rotationYaw, player.rotationPitch);
        }
        player.worldObj.updateEntityWithOptionalForce(player, false);
    }

    public static void teleportPlayer(EntityPlayerMP player, double posX, double posY, double posZ, float yaw, float pitch, int dimension) {
        // Dismount from NPC mount before teleportation to prevent mount desync
        if (player.ridingEntity instanceof EntityNPCInterface) {
            player.mountEntity(null);
        }

        if (player.dimension != dimension) {
            MinecraftServer server = MinecraftServer.getServer();
            WorldServer wor = server.worldServerForDimension(dimension);
            if (wor == null) {
                player.addChatMessage(new ChatComponentText("Broken transporter. Dimenion does not exist"));
                return;
            }
            player.setLocationAndAngles(posX, posY, posZ, yaw, pitch);
            server.getConfigurationManager().transferPlayerToDimension(player, dimension, new CustomTeleporter(wor));
            player.playerNetServerHandler.setPlayerLocation(posX, posY, posZ, yaw, pitch);

            if (!wor.playerEntities.contains(player))
                wor.spawnEntityInWorld(player);
        } else {
            player.playerNetServerHandler.setPlayerLocation(posX, posY, posZ, yaw, pitch);
        }
        player.worldObj.updateEntityWithOptionalForce(player, false);
    }

    public static void disableMouseInput(EntityPlayerMP player, long time, int... buttonIds) {
        StringBuilder stringedIds = new StringBuilder();
        for (int i = 0; i < buttonIds.length; i++) {
            stringedIds.append(i);
            if (i < buttonIds.length - 1) {
                stringedIds.append(";");
            }
        }

        PacketHandler.Instance.sendToPlayer(new DisableMouseInputPacket(time, stringedIds.toString()), player);
    }

    public static void swingPlayerArm(EntityPlayerMP player) {
        PacketHandler.Instance.sendToPlayer(new SwingPlayerArmPacket(), player);
    }

    private static void followerBuy(RoleFollower role, IInventory currencyInv, EntityPlayerMP player, EntityNPCInterface npc) {
        ItemStack currency = currencyInv.getStackInSlot(0);
        if (currency == null)
            return;
        HashMap<ItemStack, Integer> cd = new HashMap<ItemStack, Integer>();
        for (int i : role.inventory.items.keySet()) {
            ItemStack is = role.inventory.items.get(i);
            if (is == null || is.getItem() != currency.getItem() || is.getHasSubtypes() && is.getItemDamage() != currency.getItemDamage())
                continue;
            int days = 1;
            if (role.rates.containsKey(i))
                days = role.rates.get(i);

            cd.put(is, days);
        }
        if (cd.size() == 0)
            return;
        int stackSize = currency.stackSize;
        int days = 0;

        int possibleDays = 0;
        int possibleSize = stackSize;
        while (true) {
            for (ItemStack item : cd.keySet()) {
                int rDays = cd.get(item);
                int rValue = item.stackSize;
                if (rValue > stackSize)
                    continue;
                int newStackSize = stackSize % rValue;
                int size = stackSize - newStackSize;
                int posDays = (size / rValue) * rDays;
                if (possibleDays <= posDays) {
                    possibleDays = posDays;
                    possibleSize = newStackSize;
                }
            }
            if (stackSize == possibleSize)
                break;
            stackSize = possibleSize;
            days += possibleDays;
            possibleDays = 0;
        }
        if (days == 0)
            return;
        if (stackSize <= 0)
            currencyInv.setInventorySlotContents(0, null);
        else
            currency = currency.splitStack(stackSize);

        npc.say(player, new Line(NoppesStringUtils.formatText(role.dialogHire.replace("{days}", days + ""), player, npc)));
        role.setOwner(player);
        role.addDays(days);
    }

    public static void bankUpgrade(EntityPlayerMP player, EntityNPCInterface npc) {
        if (npc.advanced.role != EnumRoleType.Bank)
            return;
        Container con = player.openContainer;
        if (con == null || !(con instanceof ContainerNPCBankInterface))
            return;

        ContainerNPCBankInterface container = (ContainerNPCBankInterface) con;
        Bank bank = BankController.getInstance().getBank(container.bankid);
        ItemStack item = bank.upgradeInventory.getStackInSlot(container.slot);
        if (item == null)
            return;

        int price = item.stackSize;
        ItemStack currency = container.currencyMatrix.getStackInSlot(0);
        if (currency == null || price > currency.stackSize)
            return;
        if (currency.stackSize - price == 0)
            container.currencyMatrix.setInventorySlotContents(0, null);
        else
            currency = currency.splitStack(price);
        player.closeContainer();
        PlayerBankData data = PlayerDataController.Instance.getBankData(player, bank.id);
        BankData bankData = data.getBank(bank.id);
        bankData.upgradedSlots.put(container.slot, true);

        bankData.openBankGui(player, npc, bank.id, container.slot);
    }

    public static void bankUnlock(EntityPlayerMP player, EntityNPCInterface npc) {
        if (npc.advanced.role != EnumRoleType.Bank)
            return;
        Container con = player.openContainer;
        if (con == null || !(con instanceof ContainerNPCBankInterface))
            return;
        ContainerNPCBankInterface container = (ContainerNPCBankInterface) con;
        Bank bank = BankController.getInstance().getBank(container.bankid);

        ItemStack item = bank.currencyInventory.getStackInSlot(container.slot);
        if (item == null)
            return;

        int price = item.stackSize;
        ItemStack currency = container.currencyMatrix.getStackInSlot(0);
        if (currency == null || price > currency.stackSize)
            return;
        if (currency.stackSize - price == 0)
            container.currencyMatrix.setInventorySlotContents(0, null);
        else
            currency = currency.splitStack(price);
        player.closeContainer();
        PlayerBankData data = PlayerDataController.Instance.getBankData(player, bank.id);
        BankData bankData = data.getBank(bank.id);
        if (bankData.unlockedSlots + 1 <= bank.maxSlots)
            bankData.unlockedSlots++;

        bankData.openBankGui(player, npc, bank.id, container.slot);
    }

    public static void dialogSelected(int dialogId, int optionId, EntityPlayerMP player, EntityNPCInterface npc) {
        Dialog dialog = DialogController.Instance.dialogs.get(dialogId);
        if (dialog == null)
            return;
        DialogOption option = dialog.options.get(optionId);
        if (EventHooks.onDialogOption(player, new DialogEvent.DialogOption((IPlayer) NpcAPI.Instance().getIEntity(player), dialog)))
            return;

        if (!npc.isRemote()) {
            EventHooks.onNPCDialogClosed(npc, player, dialogId, optionId + 1, dialog);

            if (!dialog.hasDialogs(player) && !dialog.hasOtherOptions()) {
                EventHooks.onDialogClosed(player, new DialogEvent.DialogClosed((IPlayer) NpcAPI.Instance().getIEntity(player), dialog));
                return;
            }
            if (option == null || option.optionType == EnumOptionType.DialogOption && (!option.isAvailable(player) || !option.hasDialog()) || option.optionType == EnumOptionType.Disabled || option.optionType == EnumOptionType.QuitOption) {
                EventHooks.onDialogClosed(player, new DialogEvent.DialogClosed((IPlayer) NpcAPI.Instance().getIEntity(player), dialog));
                return;
            }
        }
        if (option.optionType == EnumOptionType.RoleOption) {
            if (npc.roleInterface != null)
                npc.roleInterface.interact(player);
            else
                GuiClosePacket.closeGUI(player, -1, new NBTTagCompound());
        } else if (option.optionType == EnumOptionType.DialogOption) {
            NoppesUtilServer.openDialog(player, npc, option.getDialog(), optionId + 1);
        } else if (option.optionType == EnumOptionType.CommandBlock) {
            GuiClosePacket.closeGUI(player, -1, new NBTTagCompound());
            NoppesUtilServer.runCommand(player, npc.getCommandSenderName(), option.command);
        } else
            GuiClosePacket.closeGUI(player, -1, new NBTTagCompound());
    }

    public static void updateQuestLogData(ByteBuf buffer, EntityPlayerMP player) throws IOException {
        PlayerData playerData = PlayerData.get(player);

        NBTTagCompound compound = ByteBufUtils.readNBT(buffer);
        HashMap<String, String> questAlerts = NBTTags.getStringStringMap(compound.getTagList("Alerts", 10));
        for (Map.Entry<String, String> entry : questAlerts.entrySet()) {
            Quest quest = getQuestFromStringKey(entry.getKey());
            if (quest != null) {
                playerData.questData.activeQuests.get(quest.id).sendAlerts = Boolean.parseBoolean(entry.getValue());
            }
        }

        String trackedQuestString = ByteBufUtils.readString(buffer);
        Quest trackedQuest = getQuestFromStringKey(trackedQuestString);
        if (trackedQuest != null) {
            playerData.questData.trackQuest(trackedQuest);
        } else {
            playerData.questData.untrackQuest();
        }
    }

    public static void clearTrackQuest(EntityPlayerMP player) throws IOException {
        PlayerData playerData = PlayerData.get(player);
        playerData.questData.untrackQuest();
    }


    public static void updatePartyQuestLogData(ByteBuf buffer, EntityPlayerMP player) throws IOException {
        PlayerData playerData = PlayerData.get(player);
        String trackedQuestString = ByteBufUtils.readString(buffer);
        Quest trackedQuest = getQuestFromStringKey(trackedQuestString);
        if (trackedQuest != null) {
            playerData.questData.trackParty(playerData.getPlayerParty());
        } else {
            playerData.questData.untrackQuest();
        }
    }

    private static Quest getQuestFromStringKey(String string) {
        if (string != null && string.contains(":")) {
            String[] splitString = string.split(":");

            String categoryName;
            String questName;
            if (splitString.length == 3 && splitString[0].equals("P")) {
                categoryName = splitString[1];
                questName = splitString[2];
            } else if (splitString.length < 2) {
                return null;
            } else {
                categoryName = splitString[0];
                questName = splitString[1];
            }

            for (QuestCategory category : QuestController.Instance.categories.values()) {
                if (category.title.equals(categoryName)) {
                    for (Quest quest : category.quests.values()) {
                        if (quest.title.equals(questName)) {
                            return quest;
                        }
                    }
                }
            }
        }
        return null;
    }

    public static void sendTrackedQuestData(EntityPlayerMP player) {
        Quest trackedQuest = (Quest) PlayerData.get(player).questData.getTrackedQuest();
        if (trackedQuest != null) {
            NBTTagCompound compound = new NBTTagCompound();
            compound.setTag("Quest", trackedQuest.writeToNBT(new NBTTagCompound()));
            compound.setString("CategoryName", trackedQuest.getCategory().getName());
            if (trackedQuest.completion == EnumQuestCompletion.Instant) {
                compound.setBoolean("Instant", true);
            } else {
                compound.setString("TurnInNPC", trackedQuest.getNpcName());
            }
            NBTTagList nbtTagList = new NBTTagList();
            for (IQuestObjective objective : trackedQuest.questInterface.getObjectives(player)) {
                nbtTagList.appendTag(new NBTTagString(objective.getText()));
            }
            compound.setTag("ObjectiveList", nbtTagList);

            PacketHandler.Instance.sendToPlayer(new OverlayQuestTrackingPacket(compound), player);
        }
    }

    public static void sendPartyTrackedQuestData(EntityPlayerMP player, Party party) {
        Quest trackedQuest = (Quest) PlayerData.get(player).questData.getTrackedQuest();
        if (trackedQuest != null) {
            NBTTagCompound compound = new NBTTagCompound();
            compound.setTag("Quest", trackedQuest.writeToNBT(new NBTTagCompound()));
            compound.setString("CategoryName", trackedQuest.getCategory().getName());
            if (trackedQuest.completion == EnumQuestCompletion.Instant) {
                compound.setBoolean("Instant", true);
            } else {
                compound.setString("TurnInNPC", trackedQuest.getNpcName());
            }
            NBTTagList nbtTagList = new NBTTagList();
            for (IQuestObjective objective : trackedQuest.questInterface.getPartyObjectives(party)) {
                nbtTagList.appendTag(new NBTTagString(objective.getText()));
                if (objective.getAdditionalText() != null)
                    nbtTagList.appendTag(new NBTTagString(objective.getAdditionalText()));
            }
            compound.setTag("ObjectiveList", nbtTagList);
            PacketHandler.Instance.sendToPlayer(new OverlayQuestTrackingPacket(compound), player);
        }
    }

    public static void sendQuestLogData(EntityPlayerMP player) {
        if (!PlayerQuestController.hasActiveQuests(player)) {
            return;
        }
        QuestLogData data = new QuestLogData();
        data.setData(player);
        GuiDataPacket.sendGuiData(player, data.writeNBT());
    }

    public static void sendTrackedQuest(EntityPlayerMP player) {
        QuestLogData data = new QuestLogData();
        data.setTrackedQuestKey(player);

        PartyDataPacket.sendPartyData(player, data.writeTrackedQuest());
    }

    public static boolean questCompletion(EntityPlayerMP player, int questId) {
        if (player == null)
            return false;

        PlayerData playerData = PlayerData.get(player);
        PlayerQuestData questData = playerData.questData;
        QuestData data = questData.activeQuests.get(questId);

        if (data == null)
            return false;

        if (!data.quest.questInterface.isCompleted(playerData))
            return false;

        if (data.quest.completion == EnumQuestCompletion.Instant) {
            EventHooks.onQuestFinished(player, data.quest);
        }

        QuestEvent.QuestTurnedInEvent event = new QuestEvent.QuestTurnedInEvent((IPlayer) NpcAPI.Instance().getIEntity(player), data.quest);

        event.expReward = data.quest.rewardExp;
        List<IItemStack> list = new ArrayList();
        Iterator var8 = data.quest.rewardItems.items.values().iterator();

        while (var8.hasNext()) {
            ItemStack item = (ItemStack) var8.next();
            if (item != null && item.stackSize > 0) {
                IItemStack iStack = NpcAPI.Instance().getIItemStack(item);
                if (iStack != null) {
                    list.add(iStack);
                }
            }
        }

        if (!data.quest.randomReward) {
            event.itemRewards = (IItemStack[]) list.toArray(new IItemStack[list.size()]);
        } else if (!list.isEmpty()) {
            event.itemRewards = new IItemStack[]{(IItemStack) list.get(player.getRNG().nextInt(list.size()))};
        }

        EventHooks.onQuestTurnedIn(player, event);
        if (event.isCancelled())
            return false;

        IItemStack[] var12 = event.itemRewards;
        int var14 = var12.length;
        for (int var10 = 0; var10 < var14; ++var10) {
            IItemStack item = var12[var10];
            if (item != null) {
                NoppesUtilServer.GivePlayerItem(player, player, item.getMCItemStack());
            }
        }

        data.quest.questInterface.handleComplete(player);
        if (data.quest.rewardExp > 0) {
            player.worldObj.playSoundAtEntity(player, "random.orb", 0.1F, 0.5F * ((player.worldObj.rand.nextFloat() - player.worldObj.rand.nextFloat()) * 0.7F + 1.8F));
            player.addExperience(data.quest.rewardExp);
        }
        data.quest.factionOptions.addPoints(player);
        if (data.quest.mail.isValid()) {
            PlayerDataController.Instance.addPlayerMessage(player.getCommandSenderName(), data.quest.mail);
        }

        if (!data.quest.command.isEmpty()) {
            NoppesUtilServer.runCommand(player, "QuestCompletion", data.quest.command);
        }

        PlayerQuestController.setQuestFinished(data.quest, player);
        if (data.quest.hasNewQuest()) {
            QuestData nextQuest = new QuestData(data.quest.getNextQuest());
            nextQuest.sendAlerts = data.quest.id != data.quest.getNextQuest().id || data.sendAlerts;
            PlayerQuestController.addActiveQuest(nextQuest, player);
            questData.trackQuest(nextQuest.quest);
        }

        playerData.save();

        return true;
    }


    public static boolean questPartyCompletion(Party party) {
        if (party == null)
            return false;

        QuestData data = party.getQuestData();
        if (data == null)
            return false;

        if (!data.quest.questInterface.isPartyCompleted(party))
            return false;


        Quest quest = data.quest;
        if (quest == null)
            return false;

        if (data.quest.completion == EnumQuestCompletion.Instant)
            EventHooks.onPartyFinished(party, data.quest);

        PartyEvent.PartyQuestTurnedInEvent partyEv = new PartyEvent.PartyQuestTurnedInEvent(party, data.quest);
        EventHooks.onPartyTurnIn(party, partyEv);
        if (partyEv.isCancelled())
            return false;

        boolean hasNextQuest = data.quest.hasNewQuest();
        PartyOptions partyOptions = quest.partyOptions;
        EnumPartyExchange complete = partyOptions.completeFor;
        EnumPartyExchange reward = partyOptions.rewardControl;
        EnumPartyExchange command = partyOptions.executeCommand;
        for (String name : party.getPlayerNames()) {
            EntityPlayer player = NoppesUtilServer.getPlayerByName(name);
            if (player == null)
                continue;

            PlayerData playerData = PlayerData.get(player);
            if (playerData == null)
                continue;

            boolean isLeader = party.getLeaderUUID().equals(player.getUniqueID());
            boolean hasActiveQuest = playerData.questData.hasActiveQuest(quest.getId());
            boolean hasFinishedQuest = playerData.questData.hasFinishedQuest(quest.getId());

            boolean meetsComplete = meetsPartyExchange(complete, hasActiveQuest, hasFinishedQuest, isLeader);
            boolean meetsReward = meetsPartyExchange(reward, hasActiveQuest, hasFinishedQuest, isLeader);
            boolean meetsCommand = meetsPartyExchange(command, hasActiveQuest, hasFinishedQuest, isLeader);

            if (meetsComplete) {
                if (data.quest.completion == EnumQuestCompletion.Instant) {
                    EventHooks.onQuestFinished(player, data.quest);
                }
            }

            if (meetsReward) {
                QuestEvent.QuestTurnedInEvent event = new QuestEvent.QuestTurnedInEvent((IPlayer) NpcAPI.Instance().getIEntity(player), data.quest);

                event.expReward = data.quest.rewardExp;
                List<IItemStack> list = new ArrayList();
                Iterator var8 = data.quest.rewardItems.items.values().iterator();

                while (var8.hasNext()) {
                    ItemStack item = (ItemStack) var8.next();
                    if (item != null && item.stackSize > 0) {
                        IItemStack iStack = NpcAPI.Instance().getIItemStack(item);
                        if (iStack != null) {
                            list.add(iStack);
                        }
                    }
                }

                if (!data.quest.randomReward) {
                    event.itemRewards = (IItemStack[]) list.toArray(new IItemStack[list.size()]);
                } else if (!list.isEmpty()) {
                    event.itemRewards = new IItemStack[]{(IItemStack) list.get(player.getRNG().nextInt(list.size()))};
                }

                EventHooks.onQuestTurnedIn(player, event);
                if (event.isCancelled())
                    continue;

                IItemStack[] var12 = event.itemRewards;
                int var14 = var12.length;
                for (int var10 = 0; var10 < var14; ++var10) {
                    IItemStack item = var12[var10];
                    if (item != null) {
                        NoppesUtilServer.GivePlayerItem(player, player, item.getMCItemStack());
                    }
                }
            }
            data.quest.questInterface.handlePartyComplete(player, party, isLeader, partyOptions.objectiveRequirement);
            if (meetsReward) {
                if (data.quest.rewardExp > 0) {
                    player.worldObj.playSoundAtEntity(player, "random.orb", 0.1F, 0.5F * ((player.worldObj.rand.nextFloat() - player.worldObj.rand.nextFloat()) * 0.7F + 1.8F));
                    player.addExperience(data.quest.rewardExp);
                }

                data.quest.factionOptions.addPoints(player);
                if (data.quest.mail.isValid()) {
                    PlayerDataController.Instance.addPlayerMessage(player.getCommandSenderName(), data.quest.mail);
                }
            }

            if (meetsCommand) {
                if (!data.quest.command.isEmpty()) {
                    NoppesUtilServer.runCommand(player, "QuestCompletion", data.quest.command);
                }
            }

            if (meetsComplete) {
                PlayerQuestController.setQuestPartyFinished(data.quest, player, data);
            }

            if (hasNextQuest && meetsComplete) {
                QuestData nextQuest = new QuestData(data.quest.getNextQuest());
                nextQuest.sendAlerts = data.quest.id != data.quest.getNextQuest().id || data.sendAlerts;
                PlayerQuestController.addActiveQuest(nextQuest, player);
            }

            playerData.save();
            QuestCompletionPacket.sendQuestComplete((EntityPlayerMP) player, data.quest.writeToNBT(new NBTTagCompound()));
        }

        if (quest.type == EnumQuestType.Item && partyOptions.objectiveRequirement == EnumPartyObjectives.Shared) {
            quest.questInterface.removePartyItems(party);
        }

        if (hasNextQuest) {
            if (party.validateQuest(data.quest.getNextQuest().getId(), true)) {
                party.setQuest(data.quest.getNextQuest());
                for (String name : party.getPlayerNames()) {
                    EntityPlayer player = NoppesUtilServer.getPlayerByName(name);
                    if (player == null)
                        continue;

                    PlayerData playerData = PlayerData.get(player);
                    if (playerData == null)
                        continue;

                    playerData.questData.trackParty(party);
                }
            } else {
                party.setQuest(null);
            }
        } else {
            party.setQuest(null);
        }

        if (data.quest.completion == EnumQuestCompletion.Npc) {
            PartyController.Instance().sendQuestChat(party, "party.completeChat");
            PartyController.Instance().pingPartyUpdate(party);
        }

        return true;
    }

    public static boolean meetsPartyExchange(EnumPartyExchange exchange, boolean hasActive, boolean hasFinished, boolean isLeader) {
        if (exchange == EnumPartyExchange.All) {
            return true;
        } else if (isLeader) {
            return true;
        } else if (exchange == EnumPartyExchange.Enrolled && hasActive) {
            return true;
        } else return exchange == EnumPartyExchange.Valid && (hasActive || hasFinished);
    }


    public static boolean compareItems(ItemStack item, ItemStack item2, boolean ignoreDamage, boolean ignoreNBT) {
        if (item2 == null || item == null) {
            return false;
        }
        boolean oreMatched = false;
        OreDictionary.itemMatches(item, item2, false);
        int[] ids = OreDictionary.getOreIDs(item);
        if (ids.length > 0) {
            for (int id : ids) {
                boolean match1 = false, match2 = false;
                for (ItemStack is : OreDictionary.getOres(id)) {
                    if (compareItemDetails(item, is, ignoreDamage, ignoreNBT)) {
                        match1 = true;
                    }
                    if (compareItemDetails(item2, is, ignoreDamage, ignoreNBT)) {
                        match2 = true;
                    }
                }
                if (match1 && match2)
                    return true;
            }
        }
        return compareItemDetails(item, item2, ignoreDamage, ignoreNBT);
    }

    private static boolean compareItemDetails(ItemStack item, ItemStack item2, boolean ignoreDamage, boolean ignoreNBT) {
        if (item.getItem() != item2.getItem()) {
            return false;
        }
        if (!ignoreDamage && item.getItemDamage() != -1 && item.getItemDamage() != item2.getItemDamage()) {
            return false;
        }
        if (!ignoreNBT && item.stackTagCompound != null && (item2.stackTagCompound == null || !item.stackTagCompound.equals(item2.stackTagCompound))) {
            return false;
        }
        if (!ignoreNBT && item2.stackTagCompound != null && item.stackTagCompound == null) {
            return false;
        }
        return true;
    }

    public static boolean compareItems(EntityPlayer player, ItemStack item, boolean ignoreDamage, boolean ignoreNBT) {
        int size = 0;
        for (ItemStack is : player.inventory.mainInventory) {
            if (is != null && compareItems(item, is, ignoreDamage, ignoreNBT))
                size += is.stackSize;
        }
        return size >= item.stackSize;
    }

    public static void consumeItem(EntityPlayer player, ItemStack item, boolean ignoreDamage, boolean ignoreNBT) {
        if (item == null)
            return;
        int size = item.stackSize;
        for (int i = 0; i < player.inventory.mainInventory.length; i++) {
            ItemStack is = player.inventory.mainInventory[i];
            if (is == null || !compareItems(item, is, ignoreDamage, ignoreNBT))
                continue;
            if (size >= is.stackSize) {
                size -= is.stackSize;
                player.inventory.mainInventory[i] = null;
            } else {
                player.inventory.mainInventory[i].splitStack(size);
                break;
            }
        }
    }

    public static void isGUIOpen(EntityPlayerMP player) {
        PacketHandler.Instance.sendToPlayer(new IsGuiOpenPacket(), player);
    }

    public static List<ItemStack> countStacks(IInventory inv, boolean ignoreDamage, boolean ignoreNBT) {
        List<ItemStack> list = new ArrayList();

        for (int i = 0; i < inv.getSizeInventory(); ++i) {
            ItemStack item = inv.getStackInSlot(i);
            if (!NoppesUtilServer.IsItemStackNull(item)) {
                boolean found = false;
                Iterator var7 = list.iterator();

                while (var7.hasNext()) {
                    ItemStack is = (ItemStack) var7.next();
                    if (compareItems(item, is, ignoreDamage, ignoreNBT)) {
                        is.stackSize = is.stackSize + item.stackSize;
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    list.add(item.copy());
                }
            }
        }

        return list;
    }

    public static void playSoundTo(EntityPlayerMP player, int id, ScriptSound sound) {
        NBTTagCompound compound = sound.writeToNBT();
        if (sound.sourceEntity == null || player.worldObj.provider.dimensionId == sound.sourceEntity.getDimension()) {
            PacketHandler.Instance.sendToPlayer(new SoundManagementPacket(EnumSoundOperation.PLAY_SOUND_TO, id, compound), player);
        }
    }

    public static void playSoundTo(EntityPlayerMP player, ScriptSound sound) {
        NBTTagCompound compound = sound.writeToNBT();
        if (sound.sourceEntity == null || player.worldObj.provider.dimensionId == sound.sourceEntity.getDimension()) {
            PacketHandler.Instance.sendToPlayer(new SoundManagementPacket(EnumSoundOperation.PLAY_SOUND_TO_NO_ID, compound), player);
        }
    }

    public static void stopSoundFor(EntityPlayerMP player, int id) {
        PacketHandler.Instance.sendToPlayer(new SoundManagementPacket(EnumSoundOperation.STOP_SOUND_FOR, id), player);
    }

    public static void pauseSoundsFor(EntityPlayerMP player) {
        PacketHandler.Instance.sendToPlayer(new SoundManagementPacket(EnumSoundOperation.PAUSE_SOUNDS), player);
    }

    public static void continueSoundsFor(EntityPlayerMP player) {
        PacketHandler.Instance.sendToPlayer(new SoundManagementPacket(EnumSoundOperation.CONTINUE_SOUNDS), player);
    }

    public static void stopSoundsFor(EntityPlayerMP player) {
        PacketHandler.Instance.sendToPlayer(new SoundManagementPacket(EnumSoundOperation.STOP_SOUNDS), player);
    }
}
