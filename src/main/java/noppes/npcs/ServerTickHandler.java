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
import noppes.npcs.controllers.PlayerDataController;
import noppes.npcs.controllers.data.PlayerData;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class ServerTickHandler {
    @SubscribeEvent
    public void onServerTick(TickEvent.WorldTickEvent event) {
        if (event.phase == Phase.START) {
            NPCSpawning.findChunksForSpawning((WorldServer) event.world);
        }
    }

    private String serverName = null;

    @SubscribeEvent
    public void playerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        EntityPlayerMP player = (EntityPlayerMP) event.player;
        if (serverName == null) {
            String e = "local";
            MinecraftServer server = MinecraftServer.getServer();
            if (server.isDedicatedServer()) {
                try {
                    e = InetAddress.getByName(server.getServerHostname()).getCanonicalHostName();
                } catch (UnknownHostException e1) {
                    e = MinecraftServer.getServer().getServerHostname();
                }
                if (server.getPort() != 25565)
                    e += ":" + server.getPort();
            }
            if (e == null || e.startsWith("192.168") || e.contains("127.0.0.1") || e.startsWith("localhost"))
                e = "local";
            serverName = e;
        }
        AnalyticsTracking.sendData(event.player, "join", serverName);

        PlayerData playerData = PlayerDataController.Instance.getPlayerData(event.player);
        if (playerData != null) {
            playerData.onLogin();
        }

        SyncController.syncPlayer(player);
        SyncController.syncEffects(player);
        ProfileController.Instance.login(player);
    }

    @SubscribeEvent
    public void playerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        PlayerData playerData = PlayerDataController.Instance.getPlayerData(event.player);
        if (playerData != null) {
            playerData.onLogout();
        }
    }
}
