package kamkeel;

import java.util.List;

import foxz.utils.Utils;
import net.minecraft.block.Block;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import noppes.npcs.controllers.ServerCloneController;
import noppes.npcs.entity.EntityNPCInterface;

public class CmdClone extends CommandKamkeelBase {

	@Override
	public String getCommandName() {
		return "clone";
	}

	@Override
	public String getDescription() {
		return "Clone operation (server side)";
	}

    @SubCommand(
            desc = "Add NPC(s) to clone storage",
            usage = "<npc> <tab> [clonedname]",
            permission = 4
    )
    public void add(ICommandSender sender, String[] args) {
        int tab = 0;
        try{
        	tab = Integer.parseInt(args[1]);
        }
        catch(NumberFormatException ex){
        	
        }

        int x = sender.getPlayerCoordinates().posX;
        int y = sender.getPlayerCoordinates().posY;
        int z = sender.getPlayerCoordinates().posZ;
        List<EntityNPCInterface> list = getEntities(EntityNPCInterface.class, sender.getEntityWorld(), x, y, z, 80);
        for (EntityNPCInterface npc : list) {
            if (npc.display.getName().equalsIgnoreCase(args[0])) {
                String name = npc.display.getName();
                if (args.length > 2) {
                    name = args[2];
                }
    			NBTTagCompound compound = new NBTTagCompound();
    			if(!npc.writeToNBTOptional(compound))
    				return;
    			ServerCloneController.Instance.addClone(compound, name, tab);
            }
        }
    }

    @SubCommand(
    		desc = "List NPC from clone storage", 
    		usage = "<tab>", 
    		permission = 2
    )
    public void list(ICommandSender sender, String[] args) {
        sendMessage(sender, "--- Stored NPCs --- (server side)");
        int tab = 0;
        try{
        	tab = Integer.parseInt(args[0]);
        }
        catch(NumberFormatException ex){
        	
        }
        for (String name : ServerCloneController.Instance.getClones(tab)) {
            sendMessage(sender, name);
        }
        sendMessage(sender, "------------------------------------");
    }

    @SubCommand(
    		desc = "Remove NPC from clone storage", 
    		usage = "<name> <tab>", 
    		permission = 4
    ) 
    public void del(ICommandSender sender, String[] args) throws CommandException {       
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
            throw new CommandException(String.format("Npc '%s' wasn't found", nametodel));
        }
    }

    @SubCommand(
    		desc = "Spawn cloned NPC", 
    		usage = "<name> <tab> [[world:]x,y,z]] [newname]", 
    		permission = 2
    )
    public void spawn(ICommandSender sender, String[] args) throws CommandException {
        String name = args[0].replaceAll("%", " "); // if name of npc separed by space, user must use % in place of space
        int tab = 0;
        try{
            tab = Integer.parseInt(args[1]);
        }
        catch(NumberFormatException ex){

        }
        String newname=null;
        NBTTagCompound compound = ServerCloneController.Instance.getCloneData(sender, name, tab);
        if(compound == null){
            sendMessage(sender, "Unknown npc");
            return;
        }
        World world = sender.getEntityWorld();
        double posX = sender.getPlayerCoordinates().posX;
        double posY = sender.getPlayerCoordinates().posY;
        double posZ = sender.getPlayerCoordinates().posZ;

        if(args.length > 2){
            String location = args[2];
            String[] par;
            if (location.contains(":")){
                par = location.split(":");
                location = par[1];
                world = Utils.getWorld(par[0]);
                if (world == null){
                    throw new CommandException(String.format("'%s' is an unknown world", par[0]));
                }
            }

            if (location.contains(",")){
                par = location.split(",");
                if (par.length != 3){
                    sendMessage(sender, "Location need be x,y,z");
                    return;
                }
                try{
                    posX = CommandBase.func_110666_a(sender, posX, par[0]);
                    posY = CommandBase.func_110665_a(sender, posY, par[1].trim(), 0, 0);
                    posZ = CommandBase.func_110666_a(sender, posZ, par[2]);
                }  catch(NumberFormatException ex){
                    sendMessage(sender, "Location should be in numbers");
                    return;
                }
                if (args.length > 3){
                    newname = args[3];
                }
            } else {
                newname = location;
            }
        }

        if (posX == 0 && posY == 0 && posZ == 0){//incase it was called from the console and not pos was given
            sendMessage(sender, "Location needed");
            return;
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
    }

    @SubCommand(
            desc = "Spawn cloned NPC",
            usage = "<name> <tab> [[world:]x,y,z]] [newname]",
            permission = 2
    )
    public void grid(ICommandSender sender, String[] args) throws CommandException {
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
            sendMessage(sender, "length or width was not a number");
            return;
        }


        String newname=null;
        NBTTagCompound compound = ServerCloneController.Instance.getCloneData(sender, name, tab);
        if(compound == null){
            sendMessage(sender, "Unknown npc");
            return;
        }
        World world = sender.getEntityWorld();
        double posX = sender.getPlayerCoordinates().posX;
        double posY = sender.getPlayerCoordinates().posY;
        double posZ = sender.getPlayerCoordinates().posZ;

        if(args.length > 4){
            String location = args[4];
            String[] par;
            if (location.contains(":")){
                par = location.split(":");
                location = par[1];
                world = Utils.getWorld(par[0]);
                if (world == null){
                    throw new CommandException(String.format("'%s' is an unknown world", par[0]));
                }
            }

            if (location.contains(",")){
                par = location.split(",");
                if (par.length != 3){
                    sendMessage(sender, "Location need be x,y,z");
                    return;
                }
                try{
                    posX = CommandBase.func_110666_a(sender, posX, par[0]);
                    posY = CommandBase.func_110665_a(sender, posY, par[1].trim(), 0, 0);
                    posZ = CommandBase.func_110666_a(sender, posZ, par[2]);
                }  catch(NumberFormatException ex){
                    sendMessage(sender, "Location should be in numbers");
                    return;
                }
                if (args.length > 5){
                    newname = args[5];
                }
            } else {
                newname = location;
            }
        }

        if (posX == 0 && posY == 0 && posZ == 0){//incase it was called from the console and not pos was given
            sendMessage(sender, "Location needed");
            return;
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
        return;
    }

    public World getWorld(String t){
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

    public <T extends Entity> List<T> getEntities(Class<? extends T> cls, World world, int x, int y, int z, int range) {
        AxisAlignedBB bb = AxisAlignedBB.getBoundingBox(x, y, z, x + 1, y + 1, z + 1).expand(range, range, range);
        List<T> list = world.getEntitiesWithinAABB(cls, bb);
        return list;
    }
}
