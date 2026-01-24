package noppes.npcs;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import kamkeel.npcs.controllers.ProfileController;
import kamkeel.npcs.controllers.SyncController;
import noppes.npcs.controllers.AuctionController;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldServer;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.controllers.data.action.ActionManager;
import noppes.npcs.entity.EntityNPCInterface;

public class ServerTickHandler {

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == Phase.END) {
            ActionManager.GLOBAL.tick();

            // Process auction system tick
            if (AuctionController.Instance != null) {
                AuctionController.Instance.onServerTick();
            }
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
        PlayerData playerData = PlayerData.get(event.player);
        if (playerData != null) {
            playerData.onLogin();
        }

        ProfileController.Instance.login(player);
        SyncController.beginLogin(player);
        SyncController.syncEffects(player);
        ScriptController.Instance.syncClientScripts(player);

        // Send auction notifications on login
        if (AuctionController.Instance != null) {
            AuctionController.Instance.onPlayerLogin(player);
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

        // Clear auction permission cache on logout
        if (AuctionController.Instance != null) {
            AuctionController.Instance.onPlayerLogout(event.player.getUniqueID());
        }

        // Save and unload the player's profile data on logout
        ProfileController.Instance.logout(event.player);
    }
}
