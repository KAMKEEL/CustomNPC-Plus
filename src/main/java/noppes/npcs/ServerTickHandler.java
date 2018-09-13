package noppes.npcs;

import java.net.InetAddress;
import java.net.UnknownHostException;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import noppes.npcs.client.AnalyticsTracking;
import noppes.npcs.controllers.SpawnController;
import noppes.npcs.entity.EntityNPCInterface;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;

public class ServerTickHandler {
	@SubscribeEvent
	public void onServerTick(TickEvent.WorldTickEvent event){
		if(event.phase == Phase.START){
			NPCSpawning.findChunksForSpawning((WorldServer) event.world);
		}
	}
	
	private String serverName = null;

	@SubscribeEvent
	public void playerLogin(PlayerEvent.PlayerLoggedInEvent event){
		if(serverName == null){
			String e = "local";
			MinecraftServer server = MinecraftServer.getServer();
			if(server.isDedicatedServer()){
				try {
					e = InetAddress.getByName(server.getServerHostname()).getCanonicalHostName();
				} catch (UnknownHostException e1) {
					e = MinecraftServer.getServer().getServerHostname();
				}
				if(server.getPort() != 25565)
					e += ":" + server.getPort();
			}
			if(e == null || e.startsWith("192.168") || e.contains("127.0.0.1") || e.startsWith("localhost"))
				e = "local";
			serverName = e;
		}
		AnalyticsTracking.sendData(event.player, "join", serverName);
	}
}
