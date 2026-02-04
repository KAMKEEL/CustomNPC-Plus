package kamkeel.npcs.command;

import foxz.utils.Utils;
import net.minecraft.block.Block;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import noppes.npcs.controllers.ServerCloneController;
import noppes.npcs.controllers.data.CloneFolder;
import noppes.npcs.entity.EntityNPCInterface;

import java.util.List;

import static kamkeel.npcs.util.ColorUtil.sendError;
import static kamkeel.npcs.util.ColorUtil.sendMessage;
import static kamkeel.npcs.util.ColorUtil.sendResult;

public class CloneCommand extends CommandKamkeelBase {

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
        usage = "<npc> <tab|folder> [clonedname]",
        permission = 4
    )
    public void add(ICommandSender sender, String[] args) {
        int tab = -1;
        String folder = null;
        try {
            tab = Integer.parseInt(args[1]);
            if (tab < 1 || tab > 15) {
                sendError(sender, "Tab must be within 1-15");
                return;
            }
        } catch (NumberFormatException ex) {
            folder = args[1];
            if (ServerCloneController.Instance == null || !ServerCloneController.Instance.hasFolder(folder)) {
                sendError(sender, String.format("Unknown folder: %s", folder));
                return;
            }
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
                if (!npc.writeToNBTOptional(compound))
                    return;
                if (folder != null) {
                    ServerCloneController.Instance.addClone(compound, name, folder);
                    sendResult(sender, String.format("Added NPC \u00A7e%s\u00A77 to Folder \u00A7b%s\u00A77", name, folder));
                } else {
                    ServerCloneController.Instance.addClone(compound, name, tab);
                    sendResult(sender, String.format("Added NPC \u00A7e%s\u00A77 to Tab \u00A7b%d\u00A77", name, tab));
                }
            }
        }
    }

    @SubCommand(
        desc = "List NPC from clone storage",
        usage = "<tab|folder>",
        permission = 2
    )
    public void list(ICommandSender sender, String[] args) {
        sendMessage(sender, "--- Stored NPCs --- (server side)");
        int tab = -1;
        String folder = null;
        try {
            tab = Integer.parseInt(args[1]);
            if (tab < 1 || tab > 15) {
                sendError(sender, "Tab must be within 1-15");
                return;
            }
        } catch (NumberFormatException ex) {
            folder = args[1];
            if (ServerCloneController.Instance == null || !ServerCloneController.Instance.hasFolder(folder)) {
                sendError(sender, String.format("Unknown folder: %s", folder));
                return;
            }
        }

        List<String> clones;
        if (folder != null) {
            clones = ServerCloneController.Instance.getClones(folder);
        } else {
            clones = ServerCloneController.Instance.getClones(tab);
        }
        for (String name : clones) {
            sendMessage(sender, name);
        }
        sendMessage(sender, "------------------------------------");
    }

    @SubCommand(
        desc = "Remove NPC from clone storage",
        usage = "<name> <tab|folder>",
        permission = 4
    )
    public void del(ICommandSender sender, String[] args) throws CommandException {
        String nametodel = args[0];
        int tab = -1;
        String folder = null;
        try {
            tab = Integer.parseInt(args[1]);
            if (tab < 1 || tab > 15) {
                sendError(sender, "Tab must be within 1-15");
                return;
            }
        } catch (NumberFormatException ex) {
            folder = args[1];
            if (ServerCloneController.Instance == null || !ServerCloneController.Instance.hasFolder(folder)) {
                sendError(sender, String.format("Unknown folder: %s", folder));
                return;
            }
        }

        boolean success;
        if (folder != null) {
            success = ServerCloneController.Instance.removeClone(nametodel, folder);
        } else {
            success = ServerCloneController.Instance.removeClone(nametodel, tab);
        }

        if (!success) {
            sendError(sender, String.format("NPC '%s' was not found", nametodel));
        } else {
            if (folder != null) {
                sendResult(sender, String.format("Removed NPC \u00A7e%s\u00A77 from Folder \u00A7b%s\u00A77", nametodel, folder));
            } else {
                sendResult(sender, String.format("Removed NPC \u00A7e%s\u00A77 from Tab \u00A7b%d\u00A77", nametodel, tab));
            }
        }
    }

    @SubCommand(
        desc = "Spawn cloned NPC",
        usage = "<name> <tab|folder> [[world:]x,y,z]] [newname]",
        permission = 2
    )
    public void spawn(ICommandSender sender, String[] args) throws CommandException {
        String name = args[0].replaceAll("%", " "); // if name of npc separed by space, user must use % in place of space
        int tab = -1;
        String folder = null;
        try {
            tab = Integer.parseInt(args[1]);
            if (tab < 1 || tab > 15) {
                sendError(sender, "Tab must be within 1-15");
                return;
            }
        } catch (NumberFormatException ex) {
            folder = args[1];
            if (ServerCloneController.Instance == null || !ServerCloneController.Instance.hasFolder(folder)) {
                sendError(sender, String.format("Unknown folder: %s", folder));
                return;
            }
        }

        String newname = null;
        NBTTagCompound compound;
        if (folder != null) {
            compound = ServerCloneController.Instance.getCloneData(sender, name, folder);
        } else {
            compound = ServerCloneController.Instance.getCloneData(sender, name, tab);
        }
        if (compound == null) {
            sendError(sender, "Unknown npc");
            return;
        }
        World world = sender.getEntityWorld();
        double posX = sender.getPlayerCoordinates().posX;
        double posY = sender.getPlayerCoordinates().posY;
        double posZ = sender.getPlayerCoordinates().posZ;

        if (args.length > 2) {
            String location = args[2];
            String[] par;
            if (location.contains(":")) {
                par = location.split(":");
                location = par[1];
                world = Utils.getWorld(par[0]);
                if (world == null) {
                    sendError(sender, String.format("'%s' is an unknown world", par[0]));
                    return;
                }
            }

            if (location.contains(",")) {
                par = location.split(",");
                if (par.length != 3) {
                    sendError(sender, "Location need be x,y,z");
                    return;
                }
                try {
                    posX = func_110666_a(sender, posX, par[0]);
                    posY = func_110665_a(sender, posY, par[1].trim(), 0, 0);
                    posZ = func_110666_a(sender, posZ, par[2]);
                } catch (NumberFormatException ex) {
                    sendError(sender, "Location should be in numbers");
                    return;
                }
                if (args.length > 3) {
                    newname = args[3];
                }
            } else {
                newname = location;
            }
        }

        if (posX == 0 && posY == 0 && posZ == 0) {//incase it was called from the console and not pos was given
            sendError(sender, "Location needed");
            return;
        }

        Entity entity = EntityList.createEntityFromNBT(compound, world);
        entity.setPosition(posX + 0.5, posY + 1, posZ + 0.5);
        if (entity instanceof EntityNPCInterface) {
            EntityNPCInterface npc = (EntityNPCInterface) entity;
            npc.ais.startPos = new int[]{MathHelper.floor_double(posX), MathHelper.floor_double(posY), MathHelper.floor_double(posZ)};
            if (newname != null && !newname.isEmpty())
                npc.display.name = newname.replaceAll("%", " "); // like name, newname must use % in place of space to keep a logical way
        }
        world.spawnEntityInWorld(entity);
    }

    @SubCommand(
        desc = "Spawn cloned NPC in a grid",
        usage = "<name> <tab|folder> <width> <height> [[world:]x,y,z]] [newname]",
        permission = 2
    )
    public void grid(ICommandSender sender, String[] args) throws CommandException {
        String name = args[0].replaceAll("%", " "); // if name of npc separed by space, user must use % in place of space
        int tab = -1;
        String folder = null;
        try {
            tab = Integer.parseInt(args[1]);
            if (tab < 1 || tab > 15) {
                sendError(sender, "Tab must be within 1-15");
                return;
            }
        } catch (NumberFormatException ex) {
            folder = args[1];
            if (ServerCloneController.Instance == null || !ServerCloneController.Instance.hasFolder(folder)) {
                sendError(sender, String.format("Unknown folder: %s", folder));
                return;
            }
        }

        int width, height;
        try {
            width = Integer.parseInt(args[2]);
            height = Integer.parseInt(args[3]);
        } catch (NumberFormatException ex) {
            sendError(sender, "length or width was not a number");
            return;
        }

        String newname = null;
        NBTTagCompound compound;
        if (folder != null) {
            compound = ServerCloneController.Instance.getCloneData(sender, name, folder);
        } else {
            compound = ServerCloneController.Instance.getCloneData(sender, name, tab);
        }
        if (compound == null) {
            sendError(sender, "Unknown npc");
            return;
        }
        World world = sender.getEntityWorld();
        double posX = sender.getPlayerCoordinates().posX;
        double posY = sender.getPlayerCoordinates().posY;
        double posZ = sender.getPlayerCoordinates().posZ;

        if (args.length > 4) {
            String location = args[4];
            String[] par;
            if (location.contains(":")) {
                par = location.split(":");
                location = par[1];
                world = Utils.getWorld(par[0]);
                if (world == null) {
                    sendError(sender, String.format("'%s' is an unknown world", par[0]));
                }
            }

            if (location.contains(",")) {
                par = location.split(",");
                if (par.length != 3) {
                    sendError(sender, "Location need be x,y,z");
                    return;
                }
                try {
                    posX = func_110666_a(sender, posX, par[0]);
                    posY = func_110665_a(sender, posY, par[1].trim(), 0, 0);
                    posZ = func_110666_a(sender, posZ, par[2]);
                } catch (NumberFormatException ex) {
                    sendError(sender, "Location should be in numbers");
                    return;
                }
                if (args.length > 5) {
                    newname = args[5];
                }
            } else {
                newname = location;
            }
        }

        if (posX == 0 && posY == 0 && posZ == 0) {//incase it was called from the console and not pos was given
            sendError(sender, "Location needed");
            return;
        }

        for (int x = 0; x < width; x++) {
            for (int z = 0; z < height; z++) {
                Entity entity = EntityList.createEntityFromNBT(compound, world);
                int xx = MathHelper.floor_double(posX) + x;
                int yy = Math.max(MathHelper.floor_double(posY) - 2, 1);
                int zz = MathHelper.floor_double(posZ) + z;

                for (int y = 0; y < 10; y++) {
                    Block b = world.getBlock(xx, yy + y, zz);
                    Block b2 = world.getBlock(xx, yy + y + 1, zz);
                    if (b != null && (b2 == null || b2.getCollisionBoundingBoxFromPool(world, xx, yy + y + 1, zz) == null)) {
                        yy += y;
                        break;
                    }
                }
                entity.setPosition(posX + 0.5 + x, yy + 1, posZ + 0.5 + z);
                if (entity instanceof EntityNPCInterface) {
                    EntityNPCInterface npc = (EntityNPCInterface) entity;
                    npc.ais.startPos = new int[]{xx, yy, zz};
                    if (newname != null && !newname.isEmpty())
                        npc.display.name = newname.replaceAll("%", " "); // like name, newname must use % in place of space to keep a logical way
                }
                world.spawnEntityInWorld(entity);
            }
        }
        return;
    }

    @SubCommand(
        desc = "List all custom clone folders",
        usage = "",
        permission = 2
    )
    public void listfolders(ICommandSender sender, String[] args) {
        if (ServerCloneController.Instance == null) {
            sendError(sender, "Folder system not initialized");
            return;
        }
        sendMessage(sender, "--- Clone Folders ---");
        for (CloneFolder f : ServerCloneController.Instance.getFolderList()) {
            sendMessage(sender, f.name);
        }
        sendMessage(sender, "------------------------------------");
    }

    @SubCommand(
        desc = "Create a custom clone folder",
        usage = "<name>",
        permission = 4
    )
    public void createfolder(ICommandSender sender, String[] args) {
        if (ServerCloneController.Instance == null) {
            sendError(sender, "Folder system not initialized");
            return;
        }
        String name = args[0];
        if (!CloneFolder.isValidName(name)) {
            sendError(sender, String.format("Invalid folder name: %s", name));
            return;
        }
        if (ServerCloneController.Instance.hasFolder(name)) {
            sendError(sender, String.format("Folder '%s' already exists", name));
            return;
        }
        CloneFolder created = ServerCloneController.Instance.createFolder(name);
        if (created != null) {
            sendResult(sender, String.format("Created folder \u00A7b%s", name));
        } else {
            sendError(sender, String.format("Failed to create folder '%s'", name));
        }
    }

    @SubCommand(
        desc = "Rename a custom clone folder",
        usage = "<oldname> <newname>",
        permission = 4
    )
    public void renamefolder(ICommandSender sender, String[] args) {
        if (ServerCloneController.Instance == null) {
            sendError(sender, "Folder system not initialized");
            return;
        }
        String oldName = args[0];
        String newName = args[1];
        if (!ServerCloneController.Instance.hasFolder(oldName)) {
            sendError(sender, String.format("Unknown folder: %s", oldName));
            return;
        }
        if (!CloneFolder.isValidName(newName)) {
            sendError(sender, String.format("Invalid folder name: %s", newName));
            return;
        }
        if (ServerCloneController.Instance.renameFolder(oldName, newName)) {
            sendResult(sender, String.format("Renamed folder \u00A7b%s\u00A77 to \u00A7b%s", oldName, newName));
        } else {
            sendError(sender, String.format("Failed to rename folder '%s'", oldName));
        }
    }

    @SubCommand(
        desc = "Delete an empty custom clone folder",
        usage = "<name>",
        permission = 4
    )
    public void deletefolder(ICommandSender sender, String[] args) {
        if (ServerCloneController.Instance == null) {
            sendError(sender, "Folder system not initialized");
            return;
        }
        String name = args[0];
        if (!ServerCloneController.Instance.hasFolder(name)) {
            sendError(sender, String.format("Unknown folder: %s", name));
            return;
        }
        if (ServerCloneController.Instance.deleteFolder(name)) {
            sendResult(sender, String.format("Deleted folder \u00A7b%s", name));
        } else {
            sendError(sender, String.format("Failed to delete folder '%s' (folder must be empty)", name));
        }
    }

    @SubCommand(
        desc = "Move a clone between tabs/folders",
        usage = "<clonename> <from_tab|folder> <to_tab|folder>",
        permission = 4
    )
    public void move(ICommandSender sender, String[] args) {
        if (ServerCloneController.Instance == null) {
            sendError(sender, "Folder system not initialized");
            return;
        }
        String cloneName = args[0];

        int fromTab = -1;
        String fromFolder = null;
        try {
            fromTab = Integer.parseInt(args[1]);
            if (fromTab < 1 || fromTab > 15) {
                sendError(sender, "Source tab must be within 1-15");
                return;
            }
        } catch (NumberFormatException ex) {
            fromFolder = args[1];
            if (!ServerCloneController.Instance.hasFolder(fromFolder)) {
                sendError(sender, String.format("Unknown source folder: %s", fromFolder));
                return;
            }
        }

        int toTab = -1;
        String toFolder = null;
        try {
            toTab = Integer.parseInt(args[2]);
            if (toTab < 1 || toTab > 15) {
                sendError(sender, "Destination tab must be within 1-15");
                return;
            }
        } catch (NumberFormatException ex) {
            toFolder = args[2];
            if (!ServerCloneController.Instance.hasFolder(toFolder)) {
                sendError(sender, String.format("Unknown destination folder: %s", toFolder));
                return;
            }
        }

        boolean success;
        if (fromFolder != null && toFolder != null) {
            success = ServerCloneController.Instance.moveClone(cloneName, fromFolder, toFolder);
        } else if (fromFolder != null) {
            success = ServerCloneController.Instance.moveClone(cloneName, fromFolder, toTab);
        } else if (toFolder != null) {
            success = ServerCloneController.Instance.moveClone(cloneName, fromTab, toFolder);
        } else {
            success = ServerCloneController.Instance.moveClone(cloneName, fromTab, toTab);
        }

        if (success) {
            String from = fromFolder != null ? fromFolder : String.valueOf(fromTab);
            String to = toFolder != null ? toFolder : String.valueOf(toTab);
            sendResult(sender, String.format("Moved \u00A7e%s\u00A77 from \u00A7b%s\u00A77 to \u00A7b%s", cloneName, from, to));
        } else {
            sendError(sender, String.format("Failed to move '%s'", cloneName));
        }
    }

    public World getWorld(String t) {
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

    public <T extends Entity> List<T> getEntities(Class<? extends T> cls, World world, int x, int y, int z, int range) {
        AxisAlignedBB bb = AxisAlignedBB.getBoundingBox(x, y, z, x + 1, y + 1, z + 1).expand(range, range, range);
        List<T> list = world.getEntitiesWithinAABB(cls, bb);
        return list;
    }
}
