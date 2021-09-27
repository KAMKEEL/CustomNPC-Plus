package foxz.utils;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.util.List;

public class Utils {

    public static <T> List<T> getNearbeEntityFromPlayer(Class<? extends T> cls,EntityPlayerMP player, int dis) {
        AxisAlignedBB range = player.boundingBox.expand(dis, dis, dis);
        List<T> list = player.worldObj.getEntitiesWithinAABB(cls, range);
        return list;
    }

    public static EntityPlayer getOnlinePlayer(String playername){
        return MinecraftServer.getServer().getConfigurationManager().func_152612_a(playername);
    }
    
    public static World getWorld(String t){
        WorldServer[] ws=MinecraftServer.getServer().worldServers;
        for (WorldServer w:ws){
            if (w!=null){
                if ((w.provider.dimensionId + "").equalsIgnoreCase(t)){
                     return w;
                 }
            }
        }
        return null;
    }
    
    // <editor-fold desc="--- Foxz fork (pls keep it)">
//    public static void savePlayerAltInv(EntityPlayer p,String n){        
//        NBTTagList tag=p.inventory.writeToNBT(new NBTTagList());
//        p.getEntityData().setTag("AltInv_"+n, tag);
//    }
//    
//    public static void loadPlayerAltInv(EntityPlayer p,String n){
//        p.inventory.readFromNBT((NBTTagList) p.getEntityData().getTag("AltInv_"+n));
//    }
    // </editor-fold>
}
