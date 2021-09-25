package foxz.command;

import foxz.commandhelper.ChMcLogger;
import foxz.commandhelper.annotations.Command;
import foxz.commandhelper.annotations.SubCommand;
import foxz.commandhelper.permissions.OpOnly;
import foxz.commandhelper.permissions.ParamCheck;
import foxz.commandhelper.permissions.PlayerOnly;
import foxz.utils.Utils;
import net.minecraft.block.Block;
import net.minecraft.command.CommandBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import noppes.npcs.controllers.ServerCloneController;
import noppes.npcs.entity.EntityNPCInterface;

import java.util.List;

@Command(
        name = "clone",
        desc = "Clone operation (server side)"
)
public class CmdClone extends ChMcLogger {

    public CmdClone(Object sender) {
        super(sender);
    }

    @SubCommand(
            desc = "Add NPC(s) to clone storage",
            usage = "<npc> <tab> [clonedname]",
            permissions = {OpOnly.class ,PlayerOnly.class, ParamCheck.class}
    )
    public Boolean add(String[] args) {
        EntityPlayerMP player = (EntityPlayerMP) pcParam;
        int tab = 0;
        try{
        	tab = Integer.parseInt(args[1]);
        }
        catch(NumberFormatException ex){
        	
        }
        List<EntityNPCInterface> list = Utils.getNearbeEntityFromPlayer(EntityNPCInterface.class, player, 80);
        for (EntityNPCInterface npc : list) {
            if (npc.display.name.equalsIgnoreCase(args[0])) {
                String name = npc.display.name;
                if (args.length > 2) {
                    name = args[2];
                }
    			NBTTagCompound compound = new NBTTagCompound();
    			if(!npc.writeToNBTOptional(compound))
    				return false;
    			ServerCloneController.Instance.addClone(compound, name, tab);
    			return true;
            }
        }
        return true;
    }

    @SubCommand(desc = "List NPC from clone storage", usage = "<tab>", permissions={OpOnly.class ,ParamCheck.class})
    public Boolean list(String[] args) {
        sendmessage("--- Stored NPCs --- (server side)");
        int tab = 0;
        try{
        	tab = Integer.parseInt(args[0]);
        }
        catch(NumberFormatException ex){
        	
        }
        for (String name : ServerCloneController.Instance.getClones(tab)) {
            sendmessage(name);
        }
        sendmessage("------------------------------------");
        return true;
    }

    @SubCommand(desc = "Remove NPC from clone storage", usage = "<name> <tab>", permissions={OpOnly.class ,ParamCheck.class}) 
    public Boolean del(String[] args) {       
        String nametodel = args[0];
        int tab = 0;
        try{
        	tab = Integer.parseInt(args[1]);
        }
        catch(NumberFormatException ex){
        	
        }
        boolean deleted = false;
        for(String name : ServerCloneController.Instance.getClones(tab)){
        	if(nametodel.equalsIgnoreCase(name)){
        		ServerCloneController.Instance.removeClone(name, tab);
        		deleted = true;
        		break;
        	}
        }      
        if (!ServerCloneController.Instance.removeClone(nametodel, tab)) {
            sendmessage(String.format("Npc '%s' wasn't found", nametodel));
            return false;
        }
		;
        return true;
    }

    @SubCommand(
    		desc = "Spawn cloned NPC", 
    		usage = "<name> <tab> [[world:]x,y,z]] [newname]", 
    		permissions={OpOnly.class ,ParamCheck.class})
    public boolean spawn(String[] args) {
        String name = args[0].replaceAll("%", " "); // if name of npc separed by space, user must use % in place of space
        int tab = 0;
        try{
        	tab = Integer.parseInt(args[1]);
        }
        catch(NumberFormatException ex){
        	
        }        
        String newname=null;
        NBTTagCompound compound = ServerCloneController.Instance.getCloneData(this.pcParam, name, tab);
        if(compound == null){
        	sendmessage("Unknown npc");
        	return false;
        }
        World world = pcParam.getEntityWorld();
        double posX = pcParam.getPlayerCoordinates().posX;
        double posY = pcParam.getPlayerCoordinates().posY;
        double posZ = pcParam.getPlayerCoordinates().posZ;
        
        if(args.length > 2){
	        String location = args[2];
	        String[] par; 
	        if (location.contains(":")){
	            par = location.split(":");
	            location = par[1];
	            world = Utils.getWorld(par[0]);
	            if (world == null){
	                sendmessage (String.format("'%s' is an unknown world",par[0]));
	                return false;
	            }
	        }      

	        if (location.contains(",")){
	            par = location.split(",");
	            if (par.length != 3){
	                sendmessage("Location need be x,y,z");
	                return false;
	            }
	            try{
		            posX = CommandBase.func_110666_a(pcParam, posX, par[0]);
		            posY = CommandBase.func_110665_a(pcParam, posY, par[1].trim(), 0, 0);
		            posZ = CommandBase.func_110666_a(pcParam, posZ, par[2]);
	            }  catch(NumberFormatException ex){
	            	sendmessage("Location should be in numbers");
	            	return false;
	            }            
	            if (args.length > 3){
	                newname = args[3];
	            }
	        } else {
	            newname = location; 
	        }
        }
                
        if (posX == 0 && posY == 0 && posZ == 0){//incase it was called from the console and not pos was given
            sendmessage ("Location needed");
            return false;         
        }
        
        Entity entity = EntityList.createEntityFromNBT(compound, world);
        entity.setPosition(posX + 0.5, posY + 1, posZ + 0.5);
        if(entity instanceof EntityNPCInterface){
        	EntityNPCInterface npc = (EntityNPCInterface) entity;
        	npc.ai.startPos = new int[]{MathHelper.floor_double(posX), MathHelper.floor_double(posY), MathHelper.floor_double(posZ)};
        	if(newname != null && !newname.isEmpty())
        		npc.display.name = newname.replaceAll("%", " "); // like name, newname must use % in place of space to keep a logical way             
        }
        world.spawnEntityInWorld(entity);
        return true;
    }

    @SubCommand(
    		desc = "Spawn cloned NPC", 
    		usage = "<name> <tab> <lenght> <width> [[world:]x,y,z]] [newname]", 
    		permissions={OpOnly.class ,ParamCheck.class})
    public boolean grid(String[] args) {
        String name = args[0].replaceAll("%", " "); // if name of npc separed by space, user must use % in place of space
        int tab = 0;
        try{
        	tab = Integer.parseInt(args[1]);
        }
        catch(NumberFormatException ex){
        	
        }        
        
        int width, height;
        try{
        	width = Integer.parseInt(args[2]);
        	height = Integer.parseInt(args[3]);
        }
        catch(NumberFormatException ex){
        	sendmessage("lenght or width wasnt a number");
        	return false;
        }  
        
        
        String newname=null;
        NBTTagCompound compound = ServerCloneController.Instance.getCloneData(this.pcParam, name, tab);
        if(compound == null){
        	sendmessage("Unknown npc");
        	return false;
        }
        World world = pcParam.getEntityWorld();
        double posX = pcParam.getPlayerCoordinates().posX;
        double posY = pcParam.getPlayerCoordinates().posY;
        double posZ = pcParam.getPlayerCoordinates().posZ;
        
        if(args.length > 4){
	        String location = args[4];
	        String[] par; 
	        if (location.contains(":")){
	            par = location.split(":");
	            location = par[1];
	            world = Utils.getWorld(par[0]);
	            if (world == null){
	                sendmessage (String.format("'%s' is an unknown world",par[0]));
	                return false;
	            }
	        }      

	        if (location.contains(",")){
	            par = location.split(",");
	            if (par.length != 3){
	                sendmessage("Location need be x,y,z");
	                return false;
	            }
	            try{
		            posX = CommandBase.func_110666_a(pcParam, posX, par[0]);
		            posY = CommandBase.func_110665_a(pcParam, posY, par[1].trim(), 0, 0);
		            posZ = CommandBase.func_110666_a(pcParam, posZ, par[2]);
	            }  catch(NumberFormatException ex){
	            	sendmessage("Location should be in numbers");
	            	return false;
	            }            
	            if (args.length > 5){
	                newname = args[5];
	            }
	        } else {
	            newname = location; 
	        }
        }
                
        if (posX == 0 && posY == 0 && posZ == 0){//incase it was called from the console and not pos was given
            sendmessage ("Location needed");
            return false;         
        }
        
        for(int x = 0; x < width; x++){
            for(int z = 0; z < height; z++){
                Entity entity = EntityList.createEntityFromNBT(compound, world);
                int xx = MathHelper.floor_double(posX) + x;
                int yy = Math.max(MathHelper.floor_double(posY) - 2, 1);
                int zz = MathHelper.floor_double(posZ) + z;
                
                for(int y = 0; y < 10; y++){
                	Block b = world.getBlock(xx, yy + y, zz);
                	Block b2 = world.getBlock(xx, yy + y + 1, zz);
                	if(b != null && (b2 == null || b2.getCollisionBoundingBoxFromPool(world, xx, yy + y + 1, zz) == null)){
                		yy += y;
                		break;
                	}
                }
                entity.setPosition(posX + 0.5 + x, yy + 1, posZ + 0.5 + z);
                if(entity instanceof EntityNPCInterface){
                	EntityNPCInterface npc = (EntityNPCInterface) entity;
                	npc.ai.startPos = new int[]{xx, yy, zz};
                	if(newname != null && !newname.isEmpty())
                		npc.display.name = newname.replaceAll("%", " "); // like name, newname must use % in place of space to keep a logical way             
                }
                world.spawnEntityInWorld(entity);
            }
        }
        return true;
    }
}
