package kamkeel.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.RoleCompanion;
import noppes.npcs.roles.RoleFollower;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.List;

public class NpcCommand extends CommandKamkeelBase {

	public EntityNPCInterface selectedNpc;


	@Override
	public String getCommandName() {
		return "npc";
	}

	@Override
	public String getDescription() {
		return "NPC operation";
	}

	@Override
	public String getUsage() {
		return "<name> <command>";
	}

	@Override
	public boolean runSubCommands(){
		return false;
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) throws CommandException {
		String npcname = args[0].replace("%", " ");
		String command = args[1];
		args = Arrays.copyOfRange(args, 2, args.length);
		if(command.equalsIgnoreCase("create")){
			args = ArrayUtils.add(args, 0, npcname);
			processSubCommand(sender, command, args);
			return;
		}

		int x = sender.getPlayerCoordinates().posX;
		int y = sender.getPlayerCoordinates().posY;
		int z = sender.getPlayerCoordinates().posZ;
		List<EntityNPCInterface> list = getEntities(EntityNPCInterface.class, sender.getEntityWorld(), x, y, z, 80);
		for (EntityNPCInterface npc : list) {
			String name = npc.display.getName().replace(" ", "_");
			if (name.equalsIgnoreCase(npcname)){
				if(selectedNpc == null || selectedNpc.getDistanceSq(x, y, z) > npc.getDistanceSq(x, y, z))
					selectedNpc = npc;
			}
		}
		if(selectedNpc == null){
			sendError(sender, "Npc " + npcname + " was not found");
			return;
		}

		processSubCommand(sender, command, args);
		selectedNpc = null;
	}

	@SubCommand(
			desc = "Set Home (respawn place)",
			usage = "[x] [y] [z]"
	)
	public void home(ICommandSender sender, String[] args) {
		double posX = sender.getPlayerCoordinates().posX;
		double posY = sender.getPlayerCoordinates().posY;
		double posZ = sender.getPlayerCoordinates().posZ;

		if(args.length == 3){
			posX = CommandBase.func_110666_a(sender, selectedNpc.posX, args[0]);
			posY = CommandBase.func_110665_a(sender, selectedNpc.posY, args[1].trim(), 0, 0);
			posZ = CommandBase.func_110666_a(sender, selectedNpc.posZ, args[2]);
		}

		selectedNpc.ai.startPos = new int[]{MathHelper.floor_double(posX), MathHelper.floor_double(posY), MathHelper.floor_double(posZ)};
		sendResult(sender, String.format("Set NPC \u00A7e%s\u00A77 Home to \u00A7b[%.1f] [%.1f] [%.1f]\u00A77", selectedNpc.display.name, posX, posY, posZ));
	}

	@SubCommand(
			desc = "Set NPC visibility",
			usage = "[true/false/semi]"
	)
	public void visible(ICommandSender sender, String[] args){
		if(args.length < 1)
			return;
		boolean bo = args[0].equalsIgnoreCase("true");
		boolean semi = args[0].equalsIgnoreCase("semi");

		int current = selectedNpc.display.visible;
		if(semi)
			selectedNpc.display.visible = 2;
		else if(bo)
			selectedNpc.display.visible = 0;
		else
			selectedNpc.display.visible = 1;

		String[] list = new String[]{"True", "False", "Semi"};

		if(current != selectedNpc.display.visible)
			selectedNpc.updateClient = true;

		sendResult(sender, String.format("Set NPC \u00A7e%s\u00A77 Visibility to \u00A7b%s\u00A77", selectedNpc.display.name, list[selectedNpc.display.visible]));
	}

	@SubCommand(desc = "Delete an NPC")
	public void delete(ICommandSender sender, String[] args){
		selectedNpc.delete();
	}

	@SubCommand(
			desc = "Sets the owner of an follower/companion",
			usage = "[player]"
	)
	public void owner(ICommandSender sender, String[] args){
		if(args.length < 1){
			EntityPlayer player = null;
			if(selectedNpc.roleInterface instanceof RoleFollower)
				player = ((RoleFollower)selectedNpc.roleInterface).owner;

			if(selectedNpc.roleInterface instanceof RoleCompanion)
				player = ((RoleCompanion)selectedNpc.roleInterface).owner;

			if(player == null)
				sendResult(sender, String.format("NPC \u00A7e%s\u00A77 Owner: \u00A7b%s\u00A77", selectedNpc.display.name, "NULL"));
			else
				sendResult(sender, String.format("NPC \u00A7e%s\u00A77 Owner: \u00A7b%s\u00A77", selectedNpc.display.name, player.getDisplayName()));
		}
		else{
			EntityPlayerMP player = null;
			try {
				player = CommandBase.getPlayer(sender, args[0]);
			} catch (PlayerNotFoundException e) {

			}
			if(selectedNpc.roleInterface instanceof RoleFollower)
				((RoleFollower)selectedNpc.roleInterface).setOwner(player);

			if(selectedNpc.roleInterface instanceof RoleCompanion)
				((RoleCompanion)selectedNpc.roleInterface).setOwner(player);

			if(player == null)
				sendResult(sender, String.format("NPC \u00A7e%s\u00A77 Owner: \u00A7b%s\u00A77", selectedNpc.display.name, "NULL"));
			else
				sendResult(sender, String.format("NPC \u00A7e%s\u00A77 Owner: \u00A7b%s\u00A77", selectedNpc.display.name, player.getDisplayName()));
		}
	}


	@SubCommand(
			desc = "Set NPC name",
			usage = "[name]"
	)
	public void name(ICommandSender sender, String[] args){
		if(args.length < 1)
			return;

		String name = args[0];
		for(int i = 1; i < args.length; i++){
			name += " " + args[i];
		}

		sendResult(sender, String.format("NPC \u00A7e%s\u00A77 Name set to \u00A7b%s\u00A77", selectedNpc.display.name, name));

		if(!selectedNpc.display.getName().equals(name)){
			selectedNpc.display.setName(name);
			selectedNpc.updateClient = true;
		}
	}

	@SubCommand(
			desc = "Creates an NPC",
			usage = "[name]"
	)
	public void create(ICommandSender sender, String[] args) {
		EntityPlayerMP player = (EntityPlayerMP) sender;
		World pw = player.getEntityWorld();
		EntityCustomNpc npc = new EntityCustomNpc(pw);
		if(args.length > 0)
			npc.display.name = args[0];
		npc.setPositionAndRotation(player.posX, player.posY, player.posZ, player.cameraYaw, player.cameraPitch);
		npc.ai.startPos = new int[]{MathHelper.floor_double(player.posX),MathHelper.floor_double(player.posY),MathHelper.floor_double(player.posZ)};
		pw.spawnEntityInWorld(npc);
		npc.setHealth(npc.getMaxHealth());
		NoppesUtilServer.sendOpenGui(player, EnumGuiType.MainMenuDisplay, npc);
	}

	@Override
	public List addTabCompletionOptions(ICommandSender par1, String[] args) {
		if(args.length == 2){
			return CommandBase.getListOfStringsMatchingLastWord(args, new String[]{"create", "home", "visible", "delete", "owner", "name"});
		}
		if(args.length == 3 && args[1].equalsIgnoreCase("owner")){
			return CommandBase.getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getAllUsernames());
		}

		return null;
	}

	@Override
	public int getRequiredPermissionLevel(){
		return 4;
	}

	public <T extends Entity> List<T> getEntities(Class<? extends T> cls, World world, int x, int y, int z, int range) {
		AxisAlignedBB bb = AxisAlignedBB.getBoundingBox(x, y, z, x + 1, y + 1, z + 1).expand(range, range, range);
		List<T> list = world.getEntitiesWithinAABB(cls, bb);
		return list;
	}
}
