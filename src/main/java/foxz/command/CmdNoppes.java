package foxz.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentTranslation;
import noppes.npcs.controllers.FactionController;
import noppes.npcs.controllers.PlayerData;
import noppes.npcs.entity.EntityNPCInterface;
import foxz.commandhelper.ChMcLogger;
import foxz.commandhelper.annotations.Command;
import foxz.commandhelper.annotations.SubCommand;
import foxz.commandhelper.permissions.OpOnly;
import foxz.commandhelper.permissions.ParamCheck;
import foxz.commandhelper.permissions.PlayerOnly;
import foxz.utils.Utils;

@Command(
        name = "noppes",
        desc = "noppes root command",
        sub = {CmdClone.class, CmdScript.class, CmdQuest.class, CmdDialog.class, CmdConfig.class}
)
public class CmdNoppes extends ChMcLogger {

    public CmdFaction cmdfaction = new CmdFaction(ctorParm);
    public CmdNpc cmdnpc= new CmdNpc(ctorParm);
	public static Map<String,Class<?>> SlayMap = new LinkedHashMap<String,Class<?>>();
    
    public CmdNoppes(Object sender) {
        super(sender);
        
        SlayMap.clear();
		
		SlayMap.put("all",EntityLivingBase.class);
		SlayMap.put("mobs",EntityMob.class);
		SlayMap.put("animals", EntityAnimal.class);
		SlayMap.put("items", EntityItem.class);
		SlayMap.put("xporbs", EntityXPOrb.class);
		SlayMap.put("npcs", EntityNPCInterface.class);
		
		HashMap<String,Class<?>> list = new HashMap<String,Class<?>>(EntityList.stringToClassMapping);		
		for(String name : list.keySet()){
			Class<?> cls = list.get(name);
			if(EntityNPCInterface.class.isAssignableFrom(cls))
				continue;
			if(!EntityLivingBase.class.isAssignableFrom(cls))
				continue;
			SlayMap.put(name.toLowerCase(), list.get(name));
		}

		SlayMap.remove("monster");
		SlayMap.remove("mob");
    }


    @SubCommand(
            name = "faction",
            desc = "Faction operations",
            usage = "<player> <faction> <command>",
            permissions = {OpOnly.class, ParamCheck.class}
    )
    public Boolean faction(String[] args) {
        String playername = args[0];
        String factionname = args[1];
        
        cmdfaction.data = getPlayersData(playername);
        if (cmdfaction.data.isEmpty()) {
            sendmessage(String.format("Unknow player '%s'", playername));
            return false;
        } 
        try{
            cmdfaction.selectedFaction = FactionController.getInstance().getFaction(Integer.parseInt(factionname));
        }
        catch(NumberFormatException e){
            cmdfaction.selectedFaction = FactionController.getInstance().getFactionFromName(factionname); // get faction data
        }
        if (cmdfaction.selectedFaction == null) {
            sendmessage(String.format("Unknow facion '%s", factionname));
            return false;
        }
        args = Arrays.copyOfRange(args, 2, args.length);
        cmdfaction.processCommand(this.pcParam, args);
        
        for(PlayerData playerdata : cmdfaction.data){
        	playerdata.saveNBTData(null);
        }
        return true;
    }
    
    @SubCommand(
            desc="NPC manipulations",
            usage="<npc> <command>",
            permissions={OpOnly.class,ParamCheck.class}
    )
    public boolean npc(String[] args){
        String npcname=args[0].replace("%", " ");
        args = Arrays.copyOfRange(args, 1, args.length);
    	if(args[0].equalsIgnoreCase("create")){
            cmdnpc.processCommand(this.pcParam, new String[]{args[0], npcname});
    		return true;
    	}
    	int x = pcParam.getPlayerCoordinates().posX;
    	int y = pcParam.getPlayerCoordinates().posY;
    	int z = pcParam.getPlayerCoordinates().posZ;
        List<EntityNPCInterface> list = getNearbeEntityFromPlayer(EntityNPCInterface.class, pcParam.getEntityWorld(), x, y, z, 80);
        EntityNPCInterface closest = null;
        for (EntityNPCInterface npc : list) {
            String name = npc.display.name.replace(" ", "_");
            if (name.equalsIgnoreCase(npcname)){
            	if(closest == null || closest.getDistanceSq(x, y, z) > npc.getDistanceSq(x, y, z))
            		closest = npc;
            }
        }
        if(closest != null){
            cmdnpc.selectedNpc=closest;
            cmdnpc.processCommand(this.pcParam, args);
            cmdnpc.selectedNpc = null;
        }
        else
        	sendmessage(String.format("NPC '%s' was not found",npcname));
        return true;
    }
    
    @SubCommand(
            name = "slay",
            desc = "Kills given entity within range. Also has all, mobs, animal options. Can have multiple types",
            usage = "<type>.. [range]",
            permissions = {PlayerOnly.class, OpOnly.class, ParamCheck.class}
    )
    public Boolean slay(String[] args) {
        EntityPlayerMP player = (EntityPlayerMP) pcParam;
		ArrayList<Class<?>> toDelete = new ArrayList<Class<?>>();
		boolean deleteNPCs = false;
		for(String delete : args){
			delete = delete.toLowerCase();
			Class<?> cls = SlayMap.get(delete);
			if(cls != null)
				toDelete.add(cls);
			if(delete.equals("mobs")){
				toDelete.add(EntityGhast.class);
				toDelete.add(EntityDragon.class);
			}
			if(delete.equals("npcs"))
				deleteNPCs = true;
		}
		int count = 0;
		int range = 120;
		try{
			range = Integer.parseInt(args[args.length - 1]);
		}
		catch(NumberFormatException ex){
			
		}
		AxisAlignedBB box = player.boundingBox.expand(range, range, range);
		List<Entity> list = player.worldObj.getEntitiesWithinAABB(EntityLivingBase.class, box);
		
		for(Entity entity : list){
			if(entity instanceof EntityPlayer)
				continue;
			if(entity instanceof EntityTameable && ((EntityTameable)entity).isTamed())
				continue;
			if(entity instanceof EntityNPCInterface && !deleteNPCs)
				continue;
			if(delete(entity,toDelete))
				count++;
		}
		if(toDelete.contains(EntityXPOrb.class)){
			list = player.worldObj.getEntitiesWithinAABB(EntityXPOrb.class, box);
			for(Entity entity : list){
				entity.isDead = true;
				count++;
			}
		}
		if(toDelete.contains(EntityItem.class)){
			list = player.worldObj.getEntitiesWithinAABB(EntityItem.class, box);
			for(Entity entity : list){
				entity.isDead = true;
				count++;
			}
		}
		
		player.addChatMessage(new ChatComponentTranslation(count + " entities deleted"));
        return true;
    }
    
    private boolean delete(Entity entity, ArrayList<Class<?>> toDelete) {
		for(Class<?> delete : toDelete){
			if(delete == EntityAnimal.class && (entity instanceof EntityHorse)){
				continue;
			}
			if(delete.isAssignableFrom(entity.getClass())){
				entity.isDead = true;
				return true;
			}
		}
		return false;
	}

    @Override
	public List addTabCompletion(ICommandSender par1, String[] args) {
    	if(args[0].equalsIgnoreCase("slay")){
			return CommandBase.getListOfStringsMatchingLastWord(args, SlayMap.keySet().toArray(new String[SlayMap.size()]));
    	}
		if(args[0].equalsIgnoreCase("npc") && args.length == 3){
			return cmdnpc.addTabCompletion(par1, Arrays.copyOfRange(args, 1, args.length));
		}
		if(args[0].equalsIgnoreCase("faction") && args.length == 4){
			return CommandBase.getListOfStringsMatchingLastWord(args, new String[]{"add", "subtract", "set", "reset", "drop", "create"});
		}
    	return super.addTabCompletion(par1, args);
    }
    //<editor-fold desc="--- FoxZ fork (keep it pls)">
//    @SubCommand(
//            desc = "Save AltPlayerInv",
//            usage = "{player} {inv}",
//            permissions={OpOnly.class}
//    )
//    public boolean sapi(String[] args) {
//        EntityPlayer p = getOnlinePlayer(args[0]);
//        sendmessage(String.format("noppes.sapi:%s %s", args[0], args[1]));
//        if (p == null) {
//            sendmessage("noppes.sapi:unkow " + args[0]);
//            return false;
//        }
//        savePlayerAltInv(p, args[1]);
//        sendmessage("noppes.sapi:saved");
//        return true;
//    }
//
//    @SubCommand(
//            desc = "Load AltPlayerInv",
//            usage = "{player} {inv}",
//            permissions={OpOnly.class}
//    )
//    public boolean lapi(String[] args) {
//        EntityPlayer p = getOnlinePlayer(args[0]);
//        sendmessage(String.format("noppes.lapi:%s %s", args[0], args[1]));
//        if (p == null) {
//            sendmessage("noppes.sapi:unknow " + args[0]);
//            return false;
//        }
//
//        loadPlayerAltInv(p, args[1]);
//        sendmessage("noppes.lapi:loaded");
//        return true;
//    }
    //</editor-fold>
}
