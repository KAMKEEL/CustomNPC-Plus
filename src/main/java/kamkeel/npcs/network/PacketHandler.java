package kamkeel.npcs.network;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.FMLEventChannel;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.internal.FMLProxyPacket;
import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.enums.EnumChannelType;
import kamkeel.npcs.network.packets.data.*;
import kamkeel.npcs.network.packets.data.gui.*;
import kamkeel.npcs.network.packets.data.large.*;
import kamkeel.npcs.network.packets.data.npc.*;
import kamkeel.npcs.network.packets.data.script.ScriptOverlayClosePacket;
import kamkeel.npcs.network.packets.data.script.ScriptOverlayDataPacket;
import kamkeel.npcs.network.packets.data.script.ScriptedParticlePacket;
import kamkeel.npcs.network.packets.player.*;
import kamkeel.npcs.network.packets.player.customgui.CustomGuiButtonPacket;
import kamkeel.npcs.network.packets.player.customgui.CustomGuiClosePacket;
import kamkeel.npcs.network.packets.player.customgui.CustomGuiUnfocusedPacket;
import kamkeel.npcs.network.packets.player.customgui.CustomScrollClickPacket;
import kamkeel.npcs.network.packets.player.item.GuiMagicBookPacket;
import kamkeel.npcs.network.packets.player.item.GuiPaintbrushPacket;
import kamkeel.npcs.network.packets.player.item.MagicCyclesPacket;
import kamkeel.npcs.network.packets.player.profile.*;
import kamkeel.npcs.network.packets.request.*;
import kamkeel.npcs.network.packets.request.animation.AnimationGetPacket;
import kamkeel.npcs.network.packets.request.animation.AnimationRemovePacket;
import kamkeel.npcs.network.packets.request.animation.AnimationSavePacket;
import kamkeel.npcs.network.packets.request.animation.AnimationsGetPacket;
import kamkeel.npcs.network.packets.request.bank.BankGetPacket;
import kamkeel.npcs.network.packets.request.bank.BankRemovePacket;
import kamkeel.npcs.network.packets.request.bank.BankSavePacket;
import kamkeel.npcs.network.packets.request.bank.BanksGetPacket;
import kamkeel.npcs.network.packets.request.clone.*;
import kamkeel.npcs.network.packets.request.dialog.*;
import kamkeel.npcs.network.packets.request.effects.EffectGetPacket;
import kamkeel.npcs.network.packets.request.effects.EffectRemovePacket;
import kamkeel.npcs.network.packets.request.effects.EffectSavePacket;
import kamkeel.npcs.network.packets.request.effects.EffectsGetPacket;
import kamkeel.npcs.network.packets.request.faction.*;
import kamkeel.npcs.network.packets.request.feather.DimensionTeleportPacket;
import kamkeel.npcs.network.packets.request.item.ColorBrushPacket;
import kamkeel.npcs.network.packets.request.item.ColorSetPacket;
import kamkeel.npcs.network.packets.request.item.HammerPacket;
import kamkeel.npcs.network.packets.request.jobs.JobGetPacket;
import kamkeel.npcs.network.packets.request.jobs.JobSavePacket;
import kamkeel.npcs.network.packets.request.jobs.JobSpawnerAddPacket;
import kamkeel.npcs.network.packets.request.jobs.JobSpawnerRemovePacket;
import kamkeel.npcs.network.packets.request.linked.*;
import kamkeel.npcs.network.packets.request.magic.*;
import kamkeel.npcs.network.packets.request.mainmenu.*;
import kamkeel.npcs.network.packets.request.naturalspawns.NaturalSpawnGetAllPacket;
import kamkeel.npcs.network.packets.request.naturalspawns.NaturalSpawnGetPacket;
import kamkeel.npcs.network.packets.request.naturalspawns.NaturalSpawnRemovePacket;
import kamkeel.npcs.network.packets.request.naturalspawns.NaturalSpawnSavePacket;
import kamkeel.npcs.network.packets.request.npc.*;
import kamkeel.npcs.network.packets.request.party.*;
import kamkeel.npcs.network.packets.request.pather.MovingPathGetPacket;
import kamkeel.npcs.network.packets.request.pather.MovingPathSavePacket;
import kamkeel.npcs.network.packets.request.playerdata.*;
import kamkeel.npcs.network.packets.request.quest.*;
import kamkeel.npcs.network.packets.request.recipe.RecipeGetPacket;
import kamkeel.npcs.network.packets.request.recipe.RecipeRemovePacket;
import kamkeel.npcs.network.packets.request.recipe.RecipeSavePacket;
import kamkeel.npcs.network.packets.request.recipe.RecipesGetPacket;
import kamkeel.npcs.network.packets.request.role.RoleCompanionUpdatePacket;
import kamkeel.npcs.network.packets.request.role.RoleGetPacket;
import kamkeel.npcs.network.packets.request.role.RoleSavePacket;
import kamkeel.npcs.network.packets.request.script.*;
import kamkeel.npcs.network.packets.request.script.item.ItemScriptErrorPacket;
import kamkeel.npcs.network.packets.request.script.item.ItemScriptPacket;
import kamkeel.npcs.network.packets.request.script.item.LinkedItemScriptPacket;
import kamkeel.npcs.network.packets.request.tags.*;
import kamkeel.npcs.network.packets.request.transform.TransformGetPacket;
import kamkeel.npcs.network.packets.request.transform.TransformLoadPacket;
import kamkeel.npcs.network.packets.request.transform.TransformSavePacket;
import kamkeel.npcs.network.packets.request.transport.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import noppes.npcs.CustomNpcs;
import noppes.npcs.CustomNpcsPermissions;
import noppes.npcs.LogWriter;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.config.ConfigMain;
import noppes.npcs.entity.EntityNPCInterface;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class PacketHandler {
    public static PacketHandler Instance;

    // Channels
    public Map<EnumChannelType, FMLEventChannel> channels = new Hashtable<>();

    // Client to Server
    public static final PacketChannel REQUEST_PACKET = new PacketChannel("CNPC+|Req", EnumChannelType.REQUEST);

    // Client to Server - Typically information
    public static final PacketChannel PLAYER_PACKET = new PacketChannel("CNPC+|Player", EnumChannelType.PLAYER);

    // Server to Client
    public static final PacketChannel DATA_PACKET = new PacketChannel("CNPC+|Data", EnumChannelType.DATA);

    public static final List<PacketChannel> packetChannels = new ArrayList<>();

    public PacketHandler() {
        // Register Channels
        packetChannels.add(REQUEST_PACKET);
        packetChannels.add(PLAYER_PACKET);
        packetChannels.add(DATA_PACKET);

        this.registerDataPackets();
        this.registerRequestPackets();
        this.registerPlayerPackets();
    }

    public void registerRequestPackets() {
        // NPC Packets
        REQUEST_PACKET.registerPacket(new NpcClosePacket());
        REQUEST_PACKET.registerPacket(new NpcDeletePacket());

        // Script Packets
        REQUEST_PACKET.registerPacket(new BlockScriptPacket());
        REQUEST_PACKET.registerPacket(new EventScriptPacket());
        REQUEST_PACKET.registerPacket(new ForgeScriptPacket());
        REQUEST_PACKET.registerPacket(new GlobalNPCScriptPacket());
        REQUEST_PACKET.registerPacket(new ItemScriptPacket());
        REQUEST_PACKET.registerPacket(new ItemScriptErrorPacket());
        REQUEST_PACKET.registerPacket(new LinkedItemScriptPacket());
        REQUEST_PACKET.registerPacket(new NPCScriptPacket());
        REQUEST_PACKET.registerPacket(new PlayerScriptPacket());
        REQUEST_PACKET.registerPacket(new ScriptInfoPacket());
        REQUEST_PACKET.registerPacket(new EffectScriptPacket());

        // Cloner Packets
        REQUEST_PACKET.registerPacket(new CloneListPacket());
        REQUEST_PACKET.registerPacket(new SpawnMobPacket());
        REQUEST_PACKET.registerPacket(new MobSpawnerPacket());
        REQUEST_PACKET.registerPacket(new ClonePreSavePacket());
        REQUEST_PACKET.registerPacket(new CloneSavePacket());
        REQUEST_PACKET.registerPacket(new CloneRemovePacket());
        REQUEST_PACKET.registerPacket(new CloneTagListPacket());
        REQUEST_PACKET.registerPacket(new CloneAllTagsPacket());
        REQUEST_PACKET.registerPacket(new CloneAllTagsShortPacket());


        // Linked Packets
        REQUEST_PACKET.registerPacket(new LinkedGetAllPacket());
        REQUEST_PACKET.registerPacket(new LinkedGetPacket());
        REQUEST_PACKET.registerPacket(new LinkedNPCRemovePacket());
        REQUEST_PACKET.registerPacket(new LinkedSetPacket());
        REQUEST_PACKET.registerPacket(new LinkedNPCAddPacket());
        REQUEST_PACKET.registerPacket(new LinkedItemSavePacket());
        REQUEST_PACKET.registerPacket(new LinkedItemRemovePacket());
        REQUEST_PACKET.registerPacket(new LinkedItemBuildPacket());


        // Bank Packets
        REQUEST_PACKET.registerPacket(new BanksGetPacket());
        REQUEST_PACKET.registerPacket(new BankGetPacket());
        REQUEST_PACKET.registerPacket(new BankRemovePacket());
        REQUEST_PACKET.registerPacket(new BankSavePacket());

        // Recipe Packets
        REQUEST_PACKET.registerPacket(new RecipesGetPacket());
        REQUEST_PACKET.registerPacket(new RecipeGetPacket());
        REQUEST_PACKET.registerPacket(new RecipeRemovePacket());
        REQUEST_PACKET.registerPacket(new RecipeSavePacket());

        // Natural Spawn Packets
        REQUEST_PACKET.registerPacket(new NaturalSpawnGetAllPacket());
        REQUEST_PACKET.registerPacket(new NaturalSpawnGetPacket());
        REQUEST_PACKET.registerPacket(new NaturalSpawnSavePacket());
        REQUEST_PACKET.registerPacket(new NaturalSpawnRemovePacket());

        // Dialog Category Packets
        REQUEST_PACKET.registerPacket(new DialogCategorySavePacket());
        REQUEST_PACKET.registerPacket(new DialogCategoryRemovePacket());
        REQUEST_PACKET.registerPacket(new DialogCategoryGetPacket());
        REQUEST_PACKET.registerPacket(new DialogCategoriesGetPacket());

        // Dialog Packets
        REQUEST_PACKET.registerPacket(new DialogSavePacket());
        REQUEST_PACKET.registerPacket(new DialogRemovePacket());
        REQUEST_PACKET.registerPacket(new DialogsGetPacket());
        REQUEST_PACKET.registerPacket(new DialogGetPacket());

        REQUEST_PACKET.registerPacket(new DialogNpcGetPacket());
        REQUEST_PACKET.registerPacket(new DialogNpcSetPacket());
        REQUEST_PACKET.registerPacket(new DialogNpcRemovePacket());

        // Quest Category Packets
        REQUEST_PACKET.registerPacket(new QuestCategorySavePacket());
        REQUEST_PACKET.registerPacket(new QuestCategoryRemovePacket());
        REQUEST_PACKET.registerPacket(new QuestCategoryGetPacket());
        REQUEST_PACKET.registerPacket(new QuestCategoriesGetPacket());

        // Quest Packets
        REQUEST_PACKET.registerPacket(new QuestOpenGuiPacket());
        REQUEST_PACKET.registerPacket(new QuestSavePacket());
        REQUEST_PACKET.registerPacket(new QuestRemovePacket());
        REQUEST_PACKET.registerPacket(new QuestDialogGetTitlePacket());
        REQUEST_PACKET.registerPacket(new QuestLogToServerPacket());
        REQUEST_PACKET.registerPacket(new QuestUntrackPacket());
        REQUEST_PACKET.registerPacket(new QuestGetPacket());
        REQUEST_PACKET.registerPacket(new QuestsGetPacket());

        // Faction Packets
        REQUEST_PACKET.registerPacket(new FactionSavePacket());
        REQUEST_PACKET.registerPacket(new FactionRemovePacket());
        REQUEST_PACKET.registerPacket(new FactionSetPacket());
        REQUEST_PACKET.registerPacket(new FactionGetPacket());
        REQUEST_PACKET.registerPacket(new FactionsGetPacket());

        // Tag Packets
        REQUEST_PACKET.registerPacket(new TagsGetPacket());
        REQUEST_PACKET.registerPacket(new TagGetPacket());
        REQUEST_PACKET.registerPacket(new TagsNpcGetPacket());
        REQUEST_PACKET.registerPacket(new TagSetPacket());
        REQUEST_PACKET.registerPacket(new TagSavePacket());
        REQUEST_PACKET.registerPacket(new TagRemovePacket());

        // PlayerData Packets
        REQUEST_PACKET.registerPacket(new PlayerDataGetNamesPacket());
        REQUEST_PACKET.registerPacket(new PlayerDataRemovePacket());
        REQUEST_PACKET.registerPacket(new PlayerDataGetInfoPacket());
        REQUEST_PACKET.registerPacket(new PlayerDataDeleteInfoPacket());
        REQUEST_PACKET.registerPacket(new PlayerDataSaveInfoPacket());
        REQUEST_PACKET.registerPacket(new PlayerDataMapRegenPacket());

        // Main Menu Packets
        REQUEST_PACKET.registerPacket(new MainmenuDisplayGetPacket());
        REQUEST_PACKET.registerPacket(new MainmenuDisplaySavePacket());

        REQUEST_PACKET.registerPacket(new MainmenuStatsGetPacket());
        REQUEST_PACKET.registerPacket(new MainmenuStatsSavePacket());

        REQUEST_PACKET.registerPacket(new MainmenuAIGetPacket());
        REQUEST_PACKET.registerPacket(new MainmenuAISavePacket());

        REQUEST_PACKET.registerPacket(new MainmenuAdvancedGetPacket());
        REQUEST_PACKET.registerPacket(new MainmenuAdvancedSavePacket());
        REQUEST_PACKET.registerPacket(new MainmenuAdvancedMarkDataPacket());

        REQUEST_PACKET.registerPacket(new MainmenuInvGetPacket());
        REQUEST_PACKET.registerPacket(new MainmenuInvSavePacket());

        // Transport Packets
        REQUEST_PACKET.registerPacket(new TransportCategoriesGetPacket());
        REQUEST_PACKET.registerPacket(new TransportCategorySavePacket());
        REQUEST_PACKET.registerPacket(new TransportCategoryRemovePacket());

        REQUEST_PACKET.registerPacket(new TransportRemovePacket());
        REQUEST_PACKET.registerPacket(new TransportsGetPacket());
        REQUEST_PACKET.registerPacket(new TransportSavePacket());
        REQUEST_PACKET.registerPacket(new TransportGetLocationPacket());

        // Remote Packets Packets
        REQUEST_PACKET.registerPacket(new RemoteMainMenuPacket());
        REQUEST_PACKET.registerPacket(new RemoteGlobalMenuPacket());
        REQUEST_PACKET.registerPacket(new RemoteDeletePacket());
        REQUEST_PACKET.registerPacket(new RemoteNpcsGetPacket());
        REQUEST_PACKET.registerPacket(new RemoteFreezeGetPacket());
        REQUEST_PACKET.registerPacket(new RemoteFreezePacket());
        REQUEST_PACKET.registerPacket(new RemoteResetPacket());
        REQUEST_PACKET.registerPacket(new RemoteTpToNpcPacket());

        // Job Packets
        REQUEST_PACKET.registerPacket(new JobGetPacket());
        REQUEST_PACKET.registerPacket(new JobSavePacket());
        REQUEST_PACKET.registerPacket(new JobSpawnerAddPacket());
        REQUEST_PACKET.registerPacket(new JobSpawnerRemovePacket());

        // Role Packets
        REQUEST_PACKET.registerPacket(new RoleCompanionUpdatePacket());
        REQUEST_PACKET.registerPacket(new RoleSavePacket());
        REQUEST_PACKET.registerPacket(new RoleGetPacket());

        // Transform Packets
        REQUEST_PACKET.registerPacket(new TransformSavePacket());
        REQUEST_PACKET.registerPacket(new TransformGetPacket());
        REQUEST_PACKET.registerPacket(new TransformLoadPacket());

        // Trader Packets
        REQUEST_PACKET.registerPacket(new TraderMarketSavePacket());

        // Party Packets
        REQUEST_PACKET.registerPacket(new PartySavePacket());
        REQUEST_PACKET.registerPacket(new PartyInfoPacket());
        REQUEST_PACKET.registerPacket(new PartyDisbandPacket());
        REQUEST_PACKET.registerPacket(new PartySetLeaderPacket());
        REQUEST_PACKET.registerPacket(new PartyKickPacket());
        REQUEST_PACKET.registerPacket(new PartyLeavePacket());
        REQUEST_PACKET.registerPacket(new PartySetQuestPacket());
        REQUEST_PACKET.registerPacket(new PartyInvitePacket());
        REQUEST_PACKET.registerPacket(new PartyAcceptInvitePacket());
        REQUEST_PACKET.registerPacket(new PartyIgnoreInvitePacket());
        REQUEST_PACKET.registerPacket(new PartyLogToServerPacket());

        // Animation Packets
        REQUEST_PACKET.registerPacket(new AnimationsGetPacket());
        REQUEST_PACKET.registerPacket(new AnimationGetPacket());
        REQUEST_PACKET.registerPacket(new AnimationRemovePacket());
        REQUEST_PACKET.registerPacket(new AnimationSavePacket());

        // Moving Path
        REQUEST_PACKET.registerPacket(new MovingPathSavePacket());
        REQUEST_PACKET.registerPacket(new MovingPathGetPacket());

        // Tool Packets
        REQUEST_PACKET.registerPacket(new ColorBrushPacket());
        REQUEST_PACKET.registerPacket(new ColorSetPacket());
        REQUEST_PACKET.registerPacket(new HammerPacket());

        // Custom Effect Packets
        REQUEST_PACKET.registerPacket(new EffectGetPacket());
        REQUEST_PACKET.registerPacket(new EffectsGetPacket());
        REQUEST_PACKET.registerPacket(new EffectSavePacket());
        REQUEST_PACKET.registerPacket(new EffectRemovePacket());

        // Magic Packets
        REQUEST_PACKET.registerPacket(new MagicCycleRemovePacket());
        REQUEST_PACKET.registerPacket(new MagicCycleSavePacket());
        REQUEST_PACKET.registerPacket(new MagicGetAllPacket());
        REQUEST_PACKET.registerPacket(new MagicSavePacket());
        REQUEST_PACKET.registerPacket(new MagicRemovePacket());
        REQUEST_PACKET.registerPacket(new MagicGetPacket());
        REQUEST_PACKET.registerPacket(new MagicNpcGetPacket());
        REQUEST_PACKET.registerPacket(new MagicNpcSavePacket());

        // Other Packets
        REQUEST_PACKET.registerPacket(new IsGuiOpenInform());
        REQUEST_PACKET.registerPacket(new GuiRequestPacket());
        REQUEST_PACKET.registerPacket(new DimensionsGetPacket());
        REQUEST_PACKET.registerPacket(new MerchantUpdatePacket());
        REQUEST_PACKET.registerPacket(new ModelDataSavePacket());
        REQUEST_PACKET.registerPacket(new MailOpenSetupPacket());
        REQUEST_PACKET.registerPacket(new TileEntityGetPacket());
        REQUEST_PACKET.registerPacket(new TileEntitySavePacket());
        REQUEST_PACKET.registerPacket(new MountPacket());
        REQUEST_PACKET.registerPacket(new DimensionTeleportPacket());
    }

    public void registerDataPackets() {
        // Data Packets
        DATA_PACKET.registerPacket(new LoginPacket());
        DATA_PACKET.registerPacket(new AchievementPacket());
        DATA_PACKET.registerPacket(new ChatAlertPacket());
        DATA_PACKET.registerPacket(new ChatBubblePacket());
        DATA_PACKET.registerPacket(new ConfigCommandPacket());
        DATA_PACKET.registerPacket(new DisableMouseInputPacket());
        DATA_PACKET.registerPacket(new MarkDataPacket());
        DATA_PACKET.registerPacket(new OverlayQuestTrackingPacket());
        DATA_PACKET.registerPacket(new ParticlePacket());
        DATA_PACKET.registerPacket(new PlayerUpdateSkinOverlaysPacket());
        DATA_PACKET.registerPacket(new QuestCompletionPacket());
        DATA_PACKET.registerPacket(new ScrollSelectedPacket());
        DATA_PACKET.registerPacket(new SoundManagementPacket());
        DATA_PACKET.registerPacket(new SwingPlayerArmPacket());
        DATA_PACKET.registerPacket(new UpdateAnimationsPacket());
        DATA_PACKET.registerPacket(new VillagerListPacket());
        DATA_PACKET.registerPacket(new PlayerDataInfoPacket());

        // Data | GUI Packets
        DATA_PACKET.registerPacket(new GuiClosePacket());
        DATA_PACKET.registerPacket(new GuiOpenPacket());
        DATA_PACKET.registerPacket(new GuiErrorPacket());
        DATA_PACKET.registerPacket(new GuiRedstonePacket());
        DATA_PACKET.registerPacket(new GuiTeleporterPacket());
        DATA_PACKET.registerPacket(new GuiWaypointPacket());
        DATA_PACKET.registerPacket(new IsGuiOpenPacket());
        DATA_PACKET.registerPacket(new GuiOpenBookPacket());

        // Data | NPC Packets
        DATA_PACKET.registerPacket(new DeleteNpcPacket());
        DATA_PACKET.registerPacket(new EditNpcPacket());
        DATA_PACKET.registerPacket(new UpdateNpcPacket());
        DATA_PACKET.registerPacket(new DialogPacket());
        DATA_PACKET.registerPacket(new RolePacket());
        DATA_PACKET.registerPacket(new WeaponNpcPacket());

        // Data | Script Packets
        DATA_PACKET.registerPacket(new ScriptedParticlePacket());
        DATA_PACKET.registerPacket(new ScriptOverlayClosePacket());
        DATA_PACKET.registerPacket(new ScriptOverlayDataPacket());

        // Data | Large Packets
        DATA_PACKET.registerPacket(new ClonerPacket());
        DATA_PACKET.registerPacket(new ScrollDataPacket());
        DATA_PACKET.registerPacket(new ScrollListPacket());
        DATA_PACKET.registerPacket(new SyncPacket());
        DATA_PACKET.registerPacket(new SyncEffectPacket());
        DATA_PACKET.registerPacket(new GuiDataPacket());
        DATA_PACKET.registerPacket(new PartyDataPacket());
    }

    public void registerPlayerPackets() {
        PLAYER_PACKET.registerPacket(new FollowerPacket());

        PLAYER_PACKET.registerPacket(new TransportPacket());

        PLAYER_PACKET.registerPacket(new BankActionPacket());

        PLAYER_PACKET.registerPacket(new DialogSelectPacket());

        PLAYER_PACKET.registerPacket(new CheckPlayerValue());

        PLAYER_PACKET.registerPacket(new MailActionPacket());
        PLAYER_PACKET.registerPacket(new MailActionPacket.MailSendPacket());

        PLAYER_PACKET.registerPacket(new SaveSignPacket());
        PLAYER_PACKET.registerPacket(new SaveBookPacket());

        PLAYER_PACKET.registerPacket(new CompanionActionPacket());

        PLAYER_PACKET.registerPacket(new GetNPCRole());

        PLAYER_PACKET.registerPacket(new InputDevicePacket());

        PLAYER_PACKET.registerPacket(new ScreenSizePacket());

        // CustomGUI Packets
        PLAYER_PACKET.registerPacket(new CustomGuiButtonPacket());
        PLAYER_PACKET.registerPacket(new CustomGuiClosePacket());
        PLAYER_PACKET.registerPacket(new CustomGuiUnfocusedPacket());
        PLAYER_PACKET.registerPacket(new CustomScrollClickPacket());

        // Item Gui Packets
        PLAYER_PACKET.registerPacket(new GuiPaintbrushPacket());
        PLAYER_PACKET.registerPacket(new GuiMagicBookPacket());
        PLAYER_PACKET.registerPacket(new MagicCyclesPacket());

        // Profile Packets
        PLAYER_PACKET.registerPacket(new ProfileCreatePacket());
        PLAYER_PACKET.registerPacket(new ProfileRemovePacket());
        PLAYER_PACKET.registerPacket(new ProfileRenamePacket());
        PLAYER_PACKET.registerPacket(new ProfileChangePacket());
        PLAYER_PACKET.registerPacket(new ProfileGetPacket());
        PLAYER_PACKET.registerPacket(new ProfileGetInfoPacket());
    }

    public void registerChannels() {
        for (PacketChannel channel : packetChannels) {
            FMLEventChannel eventChannel =
                NetworkRegistry.INSTANCE.newEventDrivenChannel(channel.getChannelName());
            eventChannel.register(this);
            channels.put(channel.getChannelType(), eventChannel);
        }
    }

    public PacketChannel getPacketChannel(EnumChannelType type) {
        return packetChannels.stream()
            .filter(channel -> channel.getChannelType() == type)
            .findFirst()
            .orElse(null);
    }

    public FMLEventChannel getEventChannel(AbstractPacket abstractPacket) {
        PacketChannel packetChannel = getPacketChannel(abstractPacket.getChannel().getChannelType());
        if (packetChannel == null) {
            return null;
        }
        return channels.get(packetChannel.getChannelType());
    }

    @SubscribeEvent
    public void onServerPacket(FMLNetworkEvent.ServerCustomPacketEvent event) {
        handlePacket(event.packet, ((NetHandlerPlayServer) event.handler).playerEntity, Side.SERVER);
    }

    @SubscribeEvent
    public void onClientPacket(FMLNetworkEvent.ClientCustomPacketEvent event) {
        handlePacket(event.packet, CustomNpcs.proxy.getPlayer(), Side.CLIENT);
    }

    private void handlePacket(FMLProxyPacket packet, EntityPlayer player, Side side) {
        ByteBuf buf = packet.payload();
        try {
            int packetTypeOrdinal = buf.readInt();
            EnumChannelType packetType = EnumChannelType.values()[packetTypeOrdinal];

            PacketChannel packetChannel = getPacketChannel(packetType);
            if (packetChannel == null) {
                LogWriter.error("Error: Packet channel is null for packet type: " + packetType);
                return;
            }

            int packetId = buf.readInt();
            AbstractPacket abstractPacket = packetChannel.packets.get(packetId);
            if (abstractPacket == null) {
                LogWriter.error("Error: Abstract packet is null for packet ID: " + packetId);
                return;
            }

            if (side == Side.SERVER) {
                if (abstractPacket.getChannel() == REQUEST_PACKET && ConfigMain.OpsOnly && !NoppesUtilServer.isOp(player)) {
                    LogWriter.error(String.format("%s tried to use CNPC+ without being an op", player.getCommandSenderName()));
                    return;
                }

                // Check if permission is allowed
                if (abstractPacket.getPermission() != null && !CustomNpcsPermissions.hasPermission(player, abstractPacket.getPermission())) {
                    return;
                }

                // Check for required NPC
                EntityNPCInterface npc = NoppesUtilServer.getEditingNpc(player);
                if (abstractPacket.needsNPC() && npc == null) {
                    return;
                }

                abstractPacket.setNPC(npc);
            }

            // Let the packet parse the rest
            abstractPacket.receiveData(buf, player);

        } catch (IndexOutOfBoundsException e) {
            LogWriter.error("Error: IndexOutOfBoundsException in handlePacket: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            LogWriter.error("Error: Exception in handlePacket: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Sends every FMLProxyPacket produced by packet.generatePackets()
     */
    private void sendAllPackets(AbstractPacket packet, SendAction action) {
        // get all generated FMLProxyPacket objects (could be 1 for normal, or many for large)
        List<FMLProxyPacket> proxyPackets = packet.generatePackets();
        if (proxyPackets.isEmpty()) {
            LogWriter.error("Warning: No packets generated for " + packet.getClass().getName());
        }

        for (FMLProxyPacket proxy : proxyPackets) {
            action.send(proxy);
        }
    }

    // ------------------------------------------------------------------------
    // Public API methods for sending

    public void sendToPlayer(AbstractPacket packet, EntityPlayerMP player) {
        FMLEventChannel eventChannel = getEventChannel(packet);
        if (eventChannel == null) {
            LogWriter.error("Error: Event channel is null for packet: " + packet.getClass().getName());
            return;
        }
        sendAllPackets(packet, p -> eventChannel.sendTo(p, player));
    }

    public void sendToServer(AbstractPacket packet) {
        FMLEventChannel eventChannel = getEventChannel(packet);
        if (eventChannel == null) {
            LogWriter.error("Error: Event channel is null for packet: " + packet.getClass().getName());
            return;
        }
        sendAllPackets(packet, eventChannel::sendToServer);
    }

    public void sendToAll(AbstractPacket packet) {
        FMLEventChannel eventChannel = getEventChannel(packet);
        if (eventChannel == null) {
            LogWriter.error("Error: Event channel is null for packet: " + packet.getClass().getName());
            return;
        }
        sendAllPackets(packet, eventChannel::sendToAll);
    }

    public void sendToDimension(AbstractPacket packet, final int dimensionId) {
        FMLEventChannel eventChannel = getEventChannel(packet);
        if (eventChannel == null) {
            LogWriter.error("Error: Event channel is null for packet: " + packet.getClass().getName());
            return;
        }
        sendAllPackets(packet, p -> eventChannel.sendToDimension(p, dimensionId));
    }

    public void sendTracking(AbstractPacket packet, final Entity entity) {
        FMLEventChannel eventChannel = getEventChannel(packet);
        if (eventChannel == null) {
            LogWriter.error("Error: Event channel is null for packet: " + packet.getClass().getName());
            return;
        }
        final NetworkRegistry.TargetPoint point = new NetworkRegistry.TargetPoint(entity.dimension, entity.posX, entity.posY, entity.posZ, 60);
        sendAllPackets(packet, p -> eventChannel.sendToAllAround(p, point));
    }

    // Simple functional interface to unify the "send" action
    private interface SendAction {
        void send(FMLProxyPacket proxy);
    }
}
