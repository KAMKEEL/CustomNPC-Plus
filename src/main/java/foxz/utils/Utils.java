package foxz.utils;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.util.List;

public class Utils {

    static public <T> List<T> getNearbeEntityFromPlayer(Class<? extends T> cls, EntityPlayerMP player, int dis) {
        AxisAlignedBB range = player.boundingBox.expand(dis, dis, dis);
        List<T> list = player.worldObj.getEntitiesWithinAABB(cls, range);
        return list;
    }

    static public EntityPlayer getOnlinePlayer(String playername) {
        return MinecraftServer.getServer().getConfigurationManager().func_152612_a(playername);
    }

    static public World getWorld(String t) {
        WorldServer[] ws = MinecraftServer.getServer().worldServers;
        for (WorldServer w : ws) {
            if (w != null) {
                if ((w.provider.dimensionId + "").equalsIgnoreCase(t)) {
                    return w;
                }
            }
        }
        return null;
    }
}
