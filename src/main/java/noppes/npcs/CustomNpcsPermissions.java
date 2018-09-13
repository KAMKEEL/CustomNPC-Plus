package noppes.npcs;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

import org.apache.logging.log4j.LogManager;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import noppes.npcs.CustomNpcsPermissions.Permission;
import noppes.npcs.constants.EnumPacketServer;


public class CustomNpcsPermissions{
	public static final Permission NPC_DELETE = new Permission("customnpcs.npc.delete");
	public static final Permission NPC_CREATE = new Permission("customnpcs.npc.create");
	public static final Permission NPC_GUI = new Permission("customnpcs.npc.gui");
	public static final Permission NPC_FREEZE = new Permission("customnpcs.npc.freeze");
	public static final Permission NPC_RESET = new Permission("customnpcs.npc.reset");
	public static final Permission NPC_AI = new Permission("customnpcs.npc.ai");
	public static final Permission NPC_ADVANCED = new Permission("customnpcs.npc.advanced");
	public static final Permission NPC_DISPLAY = new Permission("customnpcs.npc.display");
	public static final Permission NPC_INVENTORY = new Permission("customnpcs.npc.inventory");
	public static final Permission NPC_STATS = new Permission("customnpcs.npc.stats");
	public static final Permission NPC_CLONE = new Permission("customnpcs.npc.clone");

	public static final Permission GLOBAL_LINKED = new Permission("customnpcs.global.linked");
	public static final Permission GLOBAL_PLAYERDATA = new Permission("customnpcs.global.playerdata");
	public static final Permission GLOBAL_BANK = new Permission("customnpcs.global.bank");
	public static final Permission GLOBAL_DIALOG = new Permission("customnpcs.global.dialog");
	public static final Permission GLOBAL_QUEST = new Permission("customnpcs.global.quest");
	public static final Permission GLOBAL_FACTION = new Permission("customnpcs.global.faction");
	public static final Permission GLOBAL_TRANSPORT = new Permission("customnpcs.global.transport");
	public static final Permission GLOBAL_RECIPE = new Permission("customnpcs.global.recipe");
	public static final Permission GLOBAL_NATURALSPAWN = new Permission("customnpcs.global.naturalspawn");

	public static final Permission SPAWNER_MOB = new Permission("customnpcs.spawner.mob");
	public static final Permission SPAWNER_CREATE = new Permission("customnpcs.spawner.create");
	
	public static final Permission TOOL_MOUNTER = new Permission("customnpcs.tool.mounter");
	public static final Permission TOOL_PATHER = new Permission("customnpcs.tool.pather");
	public static final Permission TOOL_SCRIPTER = new Permission("customnpcs.tool.scripter");

	public static final Permission EDIT_VILLAGER = new Permission("customnpcs.edit.villager");	
	public static final Permission EDIT_BLOCKS = new Permission("customnpcs.edit.blocks");
	
	public static final Permission SOULSTONE_ALL = new Permission("customnpcs.soulstone.all");
	
	public static CustomNpcsPermissions Instance;
	private Class<?> bukkit;
	private Method getPlayer;
	private Method hasPermission;
	
	public CustomNpcsPermissions(){
		Instance = this;
		try {
			bukkit = Class.forName("org.bukkit.Bukkit");
			getPlayer = bukkit.getMethod("getPlayer", String.class);
			hasPermission = Class.forName("org.bukkit.entity.Player").getMethod("hasPermission", String.class);
			LogManager.getLogger(CustomNpcs.class).info("Bukkit permissions enabled");
			LogManager.getLogger(CustomNpcs.class).info("Permissions available:");
			Collections.sort(Permission.permissions, String.CASE_INSENSITIVE_ORDER);
			for(String p : Permission.permissions){
				LogManager.getLogger(CustomNpcs.class).info(p);
			}
		} catch (ClassNotFoundException e) {
			// bukkit/mcpc+ is not loaded
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
	}
	
	public static boolean hasPermission(EntityPlayer player, Permission permission){
		if(Instance.bukkit != null){
			return Instance.bukkitPermission(player.getCommandSenderName(), permission.name);
		}
		return true;
	}

	private boolean bukkitPermission(String username, String permission) {
		try {
			Object player = getPlayer.invoke(null, username);
			return (Boolean) hasPermission.invoke(player, permission);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return false;
	}
	public static class Permission{
		private static final List<String> permissions = new ArrayList<String>();
		public String name;
		public Permission(String name){
			this.name = name;
			if(!permissions.contains(name))
				permissions.add(name);
		}
	}
	public static boolean hasPermissionString(EntityPlayerMP player, String permission) {
		if(Instance.bukkit != null){
			return Instance.bukkitPermission(player.getCommandSenderName(), permission);
		}
		return true;
	}

	public static boolean enabled() {
		return Instance.bukkit != null;
	}
}
