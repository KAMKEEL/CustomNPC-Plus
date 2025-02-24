package noppes.npcs.attribute;

import net.minecraft.server.MinecraftServer;
import net.minecraft.entity.player.EntityPlayer;

/**
 * Tick handler that runs every tick and, every 10 ticks, checks all players for equipment changes.
 */
public class PlayerAttributeTickHandler {
    private int tickCount = 0;

    public void onTick() {
        tickCount++;
        if (tickCount % 10 == 0) {
            for (Object o : MinecraftServer.getServer().getConfigurationManager().playerEntityList) {
                EntityPlayer player = (EntityPlayer) o;
                PlayerAttributeManager.getTracker(player.getUniqueID()).updateIfChanged(player);
            }
        }
    }
}
