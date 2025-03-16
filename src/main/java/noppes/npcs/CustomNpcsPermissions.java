package noppes.npcs;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import kamkeel.npcs.developer.Developer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import org.apache.logging.log4j.LogManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class CustomNpcsPermissions {
    // Open GUI
    public static final Permission NPC_GUI = new Permission("customnpcs.npc.gui");
    public static final Permission PAINTBRUSH_GUI = new Permission("customnpcs.npc.paintbrush");

    // Builder
    public static final Permission NPC_BUILD = new Permission("customnpcs.npc.build");

    // Modify NPC
    public static final Permission NPC_DELETE = new Permission("customnpcs.npc.delete");
    public static final Permission NPC_CREATE = new Permission("customnpcs.npc.create");
    public static final Permission NPC_RESET = new Permission("customnpcs.npc.reset");

    // Extra NPCs
    public static final Permission NPC_FREEZE = new Permission("customnpcs.npc.freeze");
    public static final Permission NPC_TELEPORT = new Permission("customnpcs.npc.teleport");

    // Save NPC
    public static final Permission NPC_AI = new Permission("customnpcs.npc.ai");
    public static final Permission NPC_ADVANCED = new Permission("customnpcs.npc.advanced");
    public static final Permission NPC_DISPLAY = new Permission("customnpcs.npc.display");
    public static final Permission NPC_INVENTORY = new Permission("customnpcs.npc.inventory");
    public static final Permission NPC_STATS = new Permission("customnpcs.npc.stats");
    public static final Permission NPC_CLONE = new Permission("customnpcs.npc.clone");

    public static final Permission NPC_ADVANCED_TRADER = new Permission("customnpcs.npc.advanced.trader");
    public static final Permission NPC_ADVANCED_DIALOG = new Permission("customnpcs.npc.advanced.dialog");
    public static final Permission NPC_ADVANCED_FACTION = new Permission("customnpcs.npc.advanced.faction");
    public static final Permission NPC_ADVANCED_JOB = new Permission("customnpcs.npc.advanced.job");
    public static final Permission NPC_ADVANCED_ROLE = new Permission("customnpcs.npc.advanced.role");
    public static final Permission NPC_ADVANCED_TRANSPORT = new Permission("customnpcs.npc.advanced.transport");
    public static final Permission NPC_ADVANCED_TRANSFORM = new Permission("customnpcs.npc.advanced.transform");
    public static final Permission NPC_ADVANCED_LINKED = new Permission("customnpcs.npc.advanced.linked");
    public static final Permission NPC_ADVANCED_MAGIC = new Permission("customnpcs.npc.advanced.linked");
    public static final Permission NPC_ADVANCED_TAGS = new Permission("customnpcs.npc.advanced.tags");

    public static final Permission GLOBAL_REMOTE = new Permission("customnpcs.global.remote");

    // Save Global Preferences
    public static final Permission GLOBAL_LINKED = new Permission("customnpcs.global.linked");
    public static final Permission GLOBAL_PLAYERDATA = new Permission("customnpcs.global.playerdata");
    public static final Permission GLOBAL_BANK = new Permission("customnpcs.global.bank");
    public static final Permission GLOBAL_DIALOG = new Permission("customnpcs.global.dialog");
    public static final Permission GLOBAL_QUEST = new Permission("customnpcs.global.quest");
    public static final Permission GLOBAL_FACTION = new Permission("customnpcs.global.faction");
    public static final Permission GLOBAL_TRANSPORT = new Permission("customnpcs.global.transport");
    public static final Permission GLOBAL_RECIPE = new Permission("customnpcs.global.recipe");
    public static final Permission GLOBAL_NATURALSPAWN = new Permission("customnpcs.global.naturalspawn");
    public static final Permission GLOBAL_TAG = new Permission("customnpcs.global.tag");
    public static final Permission GLOBAL_ANIMATION = new Permission("customnpcs.global.animation");
    public static final Permission GLOBAL_MAGIC = new Permission("customnpcs.global.magic");
    public static final Permission GLOBAL_EFFECT = new Permission("customnpcs.global.effect");

    public static final Permission SPAWNER_MOB = new Permission("customnpcs.spawner.mob");
    public static final Permission SPAWNER_CREATE = new Permission("customnpcs.spawner.create");

    // Open / Use Wands
    public static final Permission TOOL_MOUNTER = new Permission("customnpcs.tool.mounter");
    public static final Permission TOOL_PATHER = new Permission("customnpcs.tool.pather");
    public static final Permission TOOL_SCRIPTER = new Permission("customnpcs.tool.scripter");
    public static final Permission TOOL_CLONER = new Permission("customnpcs.tool.cloner");
    public static final Permission TOOL_SCRIPTED_ITEM = new Permission("customnpcs.tool.scripteditem");
    public static final Permission TOOL_TELEPORTER = new Permission("customnpcs.tool.teleporter");

    // Edit Villager
    public static final Permission EDIT_VILLAGER = new Permission("customnpcs.edit.villager");

    public static final Permission EDIT_BLOCKS = new Permission("customnpcs.edit.blocks");
    public static final Permission EDIT_REDSTONE = new Permission("customnpcs.edit.redstone");
    public static final Permission EDIT_BOOK = new Permission("customnpcs.edit.book");
    public static final Permission EDIT_TOMBSTONE = new Permission("customnpcs.edit.tombsone");
    public static final Permission EDIT_WAYPOINT = new Permission("customnpcs.edit.waypoint");
    public static final Permission EDIT_BIGSIGN = new Permission("customnpcs.edit.bigsign");

    public static final Permission SOULSTONE_ALL = new Permission("customnpcs.soulstone.all");

    // Modify / Save Scripts
    public static final Permission SCRIPT = new Permission("customnpcs.script");
    public static final Permission SCRIPT_NPC = new Permission("customnpcs.script.npc");
    public static final Permission SCRIPT_FORGE = new Permission("customnpcs.script.forge");
    public static final Permission SCRIPT_PLAYER = new Permission("customnpcs.script.player");
    public static final Permission SCRIPT_GLOBAL = new Permission("customnpcs.script.global");
    public static final Permission SCRIPT_ITEM = new Permission("customnpcs.script.item");
    public static final Permission SCRIPT_BLOCK = new Permission("customnpcs.script.block");

    // Profiles
    public static final Permission PROFILE_ADMIN = new Permission("customnpcs.profile.admin");
    public static final Permission PROFILE_CREATE = new Permission("customnpcs.profile.create");
    public static final Permission PROFILE_DELETE = new Permission("customnpcs.profile.delete");
    public static final Permission PROFILE_RENAME = new Permission("customnpcs.profile.rename");
    public static final Permission PROFILE_CHANGE = new Permission("customnpcs.profile.change");
    public static final Permission PROFILE_REGION_BYPASS = new Permission("customnpcs.profile.region-bypass");
    public static final Permission PROFILE_MAX = new Permission("customnpcs.profile.max.*");

    public static CustomNpcsPermissions Instance;
    private Class<?> bukkit;
    private Method getPlayer;
    private Method hasPermission;

    public CustomNpcsPermissions() {
        Instance = this;
        try {
            bukkit = Class.forName("org.bukkit.Bukkit");
            getPlayer = bukkit.getMethod("getPlayer", String.class);
            hasPermission = Class.forName("org.bukkit.entity.Player").getMethod("hasPermission", String.class);
            LogManager.getLogger(CustomNpcs.class).info("Bukkit permissions enabled");
            LogManager.getLogger(CustomNpcs.class).info("Permissions available:");
            Collections.sort(Permission.permissions, String.CASE_INSENSITIVE_ORDER);
            for (String p : Permission.permissions) {
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

    public static boolean hasPermission(EntityPlayer player, Permission permission) {
        if (player != null) {
            if (FMLCommonHandler.instance().getSide() == Side.SERVER && NoppesUtilServer.isOp(player))
                return true;
            if (Developer.Instance.hasUniversal(player.getUniqueID()))
                return true;
            if (permission != null) {
                if (Instance.bukkit != null) {
                    return Instance.bukkitPermission(player.getCommandSenderName(), permission.name);
                }
            }
        }
        return true;
    }

    public static boolean hasCustomPermission(EntityPlayer player, String permission) {
        if (Developer.Instance.hasUniversal(player.getUniqueID())) {
            return true;
        }
        if (Instance.bukkit != null) {
            return Instance.bukkitPermission(player.getCommandSenderName(), permission);
        }
        return false;
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

    public static class Permission {
        private static final List<String> permissions = new ArrayList<String>();
        public String name;

        public Permission(String name) {
            this.name = name;
            if (!permissions.contains(name))
                permissions.add(name);
        }
    }

    public static boolean hasPermissionString(EntityPlayerMP player, String permission) {
        if (Instance.bukkit != null) {
            return Instance.bukkitPermission(player.getCommandSenderName(), permission);
        }
        return true;
    }

    public static boolean enabled() {
        return Instance.bukkit != null;
    }
}
