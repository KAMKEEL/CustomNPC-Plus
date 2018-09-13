package foxz.utils;

import java.io.File;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.CustomNpcs;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.RoleTrader;
import noppes.npcs.util.NBTJsonUtil;

public class Market {

    static public void save(RoleTrader r, String name) {
    	if(name.isEmpty())
    		return;
    	File file = getFile(name + "_new");
    	File file1 = getFile(name);
        
        try {
			NBTJsonUtil.SaveFile(file, r.writeNBT(new NBTTagCompound()));
	        if(file1.exists()){
	        	file1.delete();
	        }
	        file.renameTo(file1);
		} catch (Exception e) {
			
		} 
    }


    static public void load(RoleTrader role, String name){
    	if(role.npc.worldObj.isRemote)
    		return;
    	File file = getFile(name);
    	if(!file.exists())
    		return;
    	
    	try {
			role.readNBT(NBTJsonUtil.LoadFile(file));
		} catch (Exception e) {
		} 
    }
    private static File getFile(String name){
    	File dir = new File(CustomNpcs.getWorldSaveDirectory(), "markets");
    	if(!dir.exists())
    		dir.mkdir();
    	return new File(dir, name.toLowerCase() + ".json");
    }

    static public void setMarket(EntityNPCInterface npc, String marketName) {
    	if(marketName.isEmpty())
    		return;
    	if(!getFile(marketName).exists())
    		Market.save((RoleTrader) npc.roleInterface, marketName);
		
        Market.load((RoleTrader) npc.roleInterface, marketName);
    }
}
