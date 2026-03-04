package kamkeel.npcs.network;

import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.enums.EnumItemPacketType;
import kamkeel.npcs.network.packets.data.large.GuiDataPacket;
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.CustomItems;
import noppes.npcs.LogWriter;
import noppes.npcs.NBTTags;
import noppes.npcs.controllers.ScriptContainer;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.controllers.data.IScriptHandler;
import noppes.npcs.controllers.data.IScriptUnit;
import noppes.npcs.items.ItemNpcTool;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class PacketUtil {

    // ==================== SCRIPT SESSION TOKEN SYSTEM ====================
    // Prevents saving script data before the client has received it from the server.
    // Each GET generates a token sent to the client; each SAVE must echo it back.
    // Multiple tokens per player are allowed so concurrent script editors don't invalidate each other.
    // Tokens are consumed on successful verify to prevent unbounded growth.

    private static final int MAX_TOKENS_PER_PLAYER = 10;
    private static final Map<UUID, Set<String>> scriptSessionTokens = new ConcurrentHashMap<>();

    public static String createScriptSession(EntityPlayerMP player) {
        String token = Long.toHexString(ThreadLocalRandom.current().nextLong());
        Set<String> tokens = scriptSessionTokens.computeIfAbsent(player.getUniqueID(), k -> new LinkedHashSet<>());
        synchronized (tokens) {
            // Evict oldest tokens if at capacity
            while (tokens.size() >= MAX_TOKENS_PER_PLAYER) {
                Iterator<String> it = tokens.iterator();
                it.next();
                it.remove();
            }
            tokens.add(token);
        }
        return token;
    }

    public static boolean verifyScriptSession(EntityPlayer player, String token) {
        if (token == null || token.isEmpty())
            return false;
        Set<String> tokens = scriptSessionTokens.get(player.getUniqueID());
        if (tokens == null)
            return false;
        synchronized (tokens) {
            return tokens.remove(token);
        }
    }

    public static void clearScriptSession(EntityPlayer player) {
        scriptSessionTokens.remove(player.getUniqueID());
    }

    public static boolean verifyItemPacket(String name, EnumItemPacketType type, EntityPlayer player) {
        if (player == null)
            return false;

        ItemStack item = player.inventory.getCurrentItem();
        if (item == null) {
            LogWriter.error(String.format("%s attempted to utilize a %s %s Packet without an item, they could be a hacker",
                player.getCommandSenderName(), type, name));
            return false;
        }

        switch (type) {
            case WAND:
                if (item.getItem() != CustomItems.wand) {
                    LogWriter.error(String.format("%s attempted to utilize a %s Packet without a Wand, they could be a hacker",
                        player.getCommandSenderName(), type, name));
                    return false;
                }
                break;
            case MOUNTER:
                if (item.getItem() != CustomItems.mount) {
                    LogWriter.error(String.format("%s attempted to utilize a %s Packet without a Mounter, they could be a hacker",
                        player.getCommandSenderName(), type, name));
                    return false;
                }
                break;
            case CLONER:
                if (item.getItem() != CustomItems.cloner) {
                    LogWriter.error(String.format("%s attempted to utilize a %s %s Packet without a Cloner, they could be a hacker",
                        player.getCommandSenderName(), type, name));
                    return false;
                }
                break;
            case TELEPORTER:
                if (item.getItem() != CustomItems.teleporter) {
                    LogWriter.error(String.format("%s attempted to utilize a %s %s Packet without a Teleporter, they could be a hacker",
                        player.getCommandSenderName(), type, name));
                    return false;
                }
                break;
            case SCRIPTER:
                if (item.getItem() != CustomItems.scripter) {
                    LogWriter.error(String.format("%s attempted to utilize a %s %s Packet without a Scripter, they could be a hacker",
                        player.getCommandSenderName(), type, name));
                    return false;
                }
                break;
            case PATHER:
                if (item.getItem() != CustomItems.moving) {
                    LogWriter.error(String.format("%s attempted to utilize a %s %s Packet without a Pather, they could be a hacker",
                        player.getCommandSenderName(), type, name));
                    return false;
                }
                break;
            case BLOCK:
                if (item.getItem() == Item.getItemFromBlock(CustomItems.waypoint)
                    || item.getItem() == Item.getItemFromBlock(CustomItems.border)
                    || item.getItem() == Item.getItemFromBlock(CustomItems.redstoneBlock)) {
                    LogWriter.error(String.format("%s attempted to utilize a %s %s Packet without a valid Block, they could be a hacker",
                        player.getCommandSenderName(), type, name));
                    return false;
                }
                break;
            case BRUSH:
                if (item.getItem() != CustomItems.tool || item.getItemDamage() != 1) {
                    LogWriter.error(String.format("%s attempted to utilize a %s %s Packet without a Pather, they could be a hacker",
                        player.getCommandSenderName(), type, name));
                    return false;
                }
                break;
            case HAMMER:
                if (item.getItem() != CustomItems.tool || item.getItemDamage() != 0) {
                    LogWriter.error(String.format("%s attempted to utilize a %s %s Packet without a Hammer, they could be a hacker",
                        player.getCommandSenderName(), type, name));
                    return false;
                }
                break;
            case MAGIC_BOOK:
                if (item.getItem() != CustomItems.tool || item.getItemDamage() != 2) {
                    LogWriter.error(String.format("%s attempted to utilize a %s %s Packet without a Magic Book, they could be a hacker",
                        player.getCommandSenderName(), type, name));
                    return false;
                }
                break;
        }
        return true;
    }

    /**
     * New method that accepts multiple EnumItemPacketTypes.
     * It returns true if any one of the provided types is valid.
     * If none are valid, a merged error message is logged.
     */
    public static boolean verifyItemPacket(String name, EntityPlayer player, EnumItemPacketType... types) {
        if (player == null)
            return false;

        ItemStack item = player.inventory.getCurrentItem();
        if (item == null) {
            LogWriter.error(String.format("%s attempted to utilize a %s without an item. Expected one of: %s, they could be a hacker",
                player.getCommandSenderName(), name, getExpectedItemNames(types)));
            return false;
        }

        for (EnumItemPacketType type : types) {
            if (isValidItemForType(item, type)) {
                return true;
            }
        }

        // None passed; log one merged error message listing all expected items.
        LogWriter.error(String.format("%s attempted to utilize a %s without a valid item. Expected one of: %s, they could be a hacker",
            player.getCommandSenderName(), name, getExpectedItemNames(types)));
        return false;
    }

    private static boolean isValidItemForType(ItemStack item, EnumItemPacketType type) {
        if (item == null || item.getItem() == null)
            return false;

        switch (type) {
            case WAND:
                return item.getItem() == CustomItems.wand;
            case MOUNTER:
                return item.getItem() == CustomItems.mount;
            case CLONER:
                return item.getItem() == CustomItems.cloner;
            case TELEPORTER:
                return item.getItem() == CustomItems.teleporter;
            case SCRIPTER:
                return item.getItem() == CustomItems.scripter;
            case PATHER:
                return item.getItem() == CustomItems.moving;
            case BLOCK:
                // For BLOCK, the item is valid as long as it is NOT one of these blocks.
                return !(item.getItem() == Item.getItemFromBlock(CustomItems.waypoint)
                    || item.getItem() == Item.getItemFromBlock(CustomItems.border)
                    || item.getItem() == Item.getItemFromBlock(CustomItems.redstoneBlock));
            case BRUSH:
                return item.getItem() instanceof ItemNpcTool && item.getItemDamage() == 1;
            case HAMMER:
                return item.getItem() instanceof ItemNpcTool && item.getItemDamage() == 0;
            case MAGIC_BOOK:
                return item.getItem() instanceof ItemNpcTool && item.getItemDamage() == 2;
            default:
                return false;
        }
    }

    /**
     * Builds a comma‐separated list of expected item names for the given types.
     */
    private static String getExpectedItemNames(EnumItemPacketType... types) {
        Set<String> expectedNames = new LinkedHashSet<>();
        for (EnumItemPacketType type : types) {
            expectedNames.add(getExpectedItemName(type));
        }
        return String.join(", ", expectedNames);
    }

    /**
     * Returns the expected item name for the given EnumItemPacketType.
     */
    private static String getExpectedItemName(EnumItemPacketType type) {
        switch (type) {
            case WAND:
                return "Wand";
            case MOUNTER:
                return "Mounter";
            case CLONER:
                return "Cloner";
            case TELEPORTER:
                return "Teleporter";
            case SCRIPTER:
                return "Scripter";
            case PATHER:
                return "Pather";
            case BLOCK:
                return "valid Block";
            case BRUSH:
                return "Paintbrush";
            case HAMMER:
                return "Hammer";
            case MAGIC_BOOK:
                return "Magic Book";
            default:
                return type.toString();
        }
    }

    public static void getScripts(IScriptHandler data, EntityPlayerMP player) {
        String token = createScriptSession(player);

        NBTTagCompound compound = new NBTTagCompound();
        compound.setBoolean("ScriptEnabled", data.getEnabled());
        compound.setString("ScriptLanguage", data.getLanguage());
        compound.setTag("Languages", ScriptController.Instance.nbtLanguages());
        compound.setTag("ScriptConsole", NBTTags.NBTLongStringMap(data.getConsoleText()));
        compound.setString("ScriptSessionToken", token);
        GuiDataPacket.sendGuiData(player, compound);
        List<IScriptUnit> containers = data.getScripts();
        for (int i = 0; i < containers.size(); i++) {
            IScriptUnit container = containers.get(i);
            NBTTagCompound tabCompound = new NBTTagCompound();
            tabCompound.setInteger("Tab", i);
            tabCompound.setTag("Script", container.writeToNBT(new NBTTagCompound()));
            tabCompound.setInteger("TotalScripts", containers.size());
            GuiDataPacket.sendGuiData(player, tabCompound);
        }

        NBTTagCompound loadComplete = new NBTTagCompound();
        loadComplete.setInteger("LoadComplete", 1);
        GuiDataPacket.sendGuiData(player, loadComplete);
    }

    public static boolean saveScripts(IScriptHandler data, ByteBuf buffer, EntityPlayer player) throws IOException {
        int tab = buffer.readInt();
        int totalScripts = buffer.readInt();
        NBTTagCompound compound = ByteBufUtils.readNBT(buffer);

        // Verify script session token to prevent saving unloaded defaults
        String token = compound.getString("ScriptSessionToken");
        if (!verifyScriptSession(player, token)) {
            LogWriter.error(String.format("Rejected script save from %s: session not verified (data not loaded)", player.getCommandSenderName()));
            return false;
        }

        if (totalScripts == 0) {
            data.getScripts().clear();
        }

        if (tab >= 0) {
            if (data.getScripts().size() > totalScripts) {
                data.setScripts(data.getScripts().subList(0, totalScripts));
            } else while (data.getScripts().size() < totalScripts) {
                data.getScripts().add(new ScriptContainer(data));
            }
            // Use factory method to create correct script unit type based on NBT
            IScriptUnit script = IScriptUnit.createFromNBT(compound, data);
            data.getScripts().set(tab, script);
        } else {
            data.setLanguage(compound.getString("ScriptLanguage"));
            if (!ScriptController.Instance.languages.containsKey(data.getLanguage())) {
                if (!ScriptController.Instance.languages.isEmpty()) {
                    data.setLanguage((String) ScriptController.Instance.languages.keySet().toArray()[0]);
                } else {
                    data.setLanguage("ECMAScript");
                }
            }
            data.setEnabled(compound.getBoolean("ScriptEnabled"));
        }
        return true;
    }
}
