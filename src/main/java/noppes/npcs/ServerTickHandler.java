package noppes.npcs;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import kamkeel.npcs.controllers.ProfileController;
import kamkeel.npcs.controllers.SyncController;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import noppes.npcs.client.AnalyticsTracking;
import noppes.npcs.controllers.AuctionNotificationController;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.controllers.data.action.ActionManager;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class ServerTickHandler {

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == Phase.END) {
            ActionManager.GLOBAL.tick();
        }
    }

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase == Phase.START) {
            NPCSpawning.findChunksForSpawning((WorldServer) event.world);
        }
    }

    private String serverName = null;

    @SubscribeEvent
    public void playerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        EntityPlayerMP player = (EntityPlayerMP) event.player;
        // Temporary Disabled
//        if (serverName == null) {
//            String e = "local";
//            MinecraftServer server = MinecraftServer.getServer();
//            if (server.isDedicatedServer()) {
//                try {
//                    e = InetAddress.getByName(server.getServerHostname()).getCanonicalHostName();
//                } catch (UnknownHostException e1) {
//                    e = MinecraftServer.getServer().getServerHostname();
//                }
//                if (server.getPort() != 25565)
//                    e += ":" + server.getPort();
//            }
//            if (e == null || e.startsWith("192.168") || e.contains("127.0.0.1") || e.startsWith("localhost"))
//                e = "local";
//            serverName = e;
//        }
//        AnalyticsTracking.sendData(event.player, "join", serverName);

        PlayerData playerData = PlayerData.get(event.player);
        if (playerData != null) {
            playerData.onLogin();
        }

        ProfileController.Instance.login(player);
        SyncController.beginLogin(player);
        SyncController.syncEffects(player);

        // Send pending auction notifications
        if (AuctionNotificationController.Instance != null) {
            AuctionNotificationController.Instance.onPlayerLogin(player);
        }
    }

    @SubscribeEvent
    public void playerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        // Dismount player from NPC mount before logout to prevent orphaned mount state
        if (event.player.ridingEntity instanceof EntityNPCInterface) {
            event.player.mountEntity(null);
        }

        PlayerData playerData = PlayerData.get(event.player);
        if (playerData != null) {
            playerData.onLogout();
        }

        // Save and unload the player's profile data on logout
        ProfileController.Instance.logout(event.player);
    }
}
