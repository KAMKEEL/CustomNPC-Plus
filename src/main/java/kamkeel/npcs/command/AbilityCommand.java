package kamkeel.npcs.command;

import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.AbilityController;
import kamkeel.npcs.controllers.data.ability.UserType;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.controllers.PlayerDataController;
import noppes.npcs.controllers.data.PlayerData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static kamkeel.npcs.util.ColorUtil.sendError;
import static kamkeel.npcs.util.ColorUtil.sendResult;

public class AbilityCommand extends CommandKamkeelBase {

    @Override
    public String getCommandName() {
        return "ability";
    }

    @Override
    public String getDescription() {
        return "Ability operations";
    }

    @SubCommand(
        desc = "List all custom abilities"
    )
    public void list(ICommandSender sender, String[] args) {
        Set<String> uuids = AbilityController.Instance.getCustomAbilityIds();
        if (uuids.isEmpty()) {
            sendResult(sender, "No custom abilities found.");
            return;
        }

        sendResult(sender, "Custom Abilities (" + uuids.size() + "):");
        for (String uuid : uuids) {
            Ability ability = AbilityController.Instance.getCustomAbility(uuid);
            if (ability != null) {
                String name = ability.getName() != null ? ability.getName() : uuid;
                sendResult(sender, "  - \u00A7b" + name + "\u00A77 (" + ability.getTypeId() + ") [" + uuid + "]");
            } else {
                sendResult(sender, "  - \u00A7b" + uuid);
            }
        }
    }

    @SubCommand(
        desc = "List all registered ability types"
    )
    public void types(ICommandSender sender, String[] args) {
        String[] typeIds = AbilityController.Instance.getTypes();
        if (typeIds.length == 0) {
            sendResult(sender, "No ability types registered.");
            return;
        }

        List<String> sortedTypes = new ArrayList<>();
        for (String typeId : typeIds) {
            sortedTypes.add(typeId);
        }
        Collections.sort(sortedTypes);

        sendResult(sender, "Registered Ability Types (" + sortedTypes.size() + "):");
        for (String typeId : sortedTypes) {
            sendResult(sender, "  - \u00A7d" + typeId);
        }
    }

    @SubCommand(
        desc = "Reload abilities from disk"
    )
    public void reload(ICommandSender sender, String[] args) {
        AbilityController.Instance.load();
        sendResult(sender, "Abilities reloaded from disk.");
    }

    @SubCommand(
        desc = "Delete a custom ability by UUID",
        usage = "<uuid>"
    )
    public void delete(ICommandSender sender, String[] args) {
        if (args.length < 1) {
            sendError(sender, "Usage: /kam ability delete <uuid>");
            return;
        }

        String uuid = args[0];
        if (!AbilityController.Instance.hasCustomAbility(uuid)) {
            sendError(sender, "No custom ability with UUID: " + uuid);
            return;
        }

        AbilityController.Instance.deleteCustomAbility(uuid);
        sendResult(sender, "Deleted ability: \u00A7b" + uuid);
    }

    @SubCommand(
        desc = "Get info about an ability",
        usage = "<name or uuid>"
    )
    public void info(ICommandSender sender, String[] args) {
        if (args.length < 1) {
            sendError(sender, "Usage: /kam ability info <name or uuid>");
            return;
        }

        String key = args[0];
        Ability ability = AbilityController.Instance.resolveAbility(key);
        if (ability == null) {
            sendError(sender, "No ability found for: " + key);
            return;
        }

        sendResult(sender, "Ability: \u00A7b" + ability.getName());
        sendResult(sender, "  Type: \u00A7d" + ability.getTypeId());
        sendResult(sender, "  Cooldown: \u00A7e" + ability.getCooldownTicks() + " ticks");
        sendResult(sender, "  Wind Up: \u00A7e" + ability.getWindUpTicks() + " ticks");
        if (ability.isInterruptible()) {
            sendResult(sender, "  Dazed: \u00A7e" + ability.getDazedTicks() + " ticks");
        }
        sendResult(sender, "  Range: \u00A7e" + ability.getMinRange() + " - " + ability.getMaxRange());
        sendResult(sender, "  Telegraph: \u00A7e" + (ability.isShowTelegraph() ? "Yes" : "No"));
        sendResult(sender, "  Allowed By: \u00A7e" + ability.getAllowedBy().name());
    }

    @SubCommand(
        desc = "List all built-in abilities available for players"
    )
    public void prebuilts(ICommandSender sender, String[] args) {
        Set<String> names = AbilityController.Instance.getAbilityNames();
        if (names.isEmpty()) {
            sendResult(sender, "No built-in abilities found.");
            return;
        }

        List<String> playerAbilities = new ArrayList<>();
        for (String name : names) {
            Ability ability = AbilityController.Instance.getAbility(name);
            if (ability != null && ability.getAllowedBy().allowsPlayer()) {
                playerAbilities.add(name);
            }
        }

        if (playerAbilities.isEmpty()) {
            sendResult(sender, "No player-usable built-in abilities found.");
            return;
        }

        Collections.sort(playerAbilities);
        sendResult(sender, "Built-in Player Abilities (" + playerAbilities.size() + "):");
        for (String name : playerAbilities) {
            Ability ability = AbilityController.Instance.getAbility(name);
            String userType = ability.getAllowedBy() == UserType.PLAYER_ONLY ? "\u00A7d[PLAYER]" : "\u00A7a[BOTH]";
            sendResult(sender, "  - \u00A7b" + name + " " + userType);
        }
    }

    @SubCommand(
        desc = "Unlock an ability for a player",
        usage = "<player> <ability>"
    )
    public void unlock(ICommandSender sender, String[] args) {
        if (args.length < 2) {
            sendError(sender, "Usage: /kam ability unlock <player> <ability>");
            return;
        }

        String playerName = args[0];
        String abilityKey = joinArgs(args, 1);

        // Find player
        EntityPlayerMP player = (EntityPlayerMP) NoppesUtilServer.getPlayerByName(playerName);
        if (player == null) {
            sendError(sender, "Player not found: " + playerName);
            return;
        }

        // Resolve ability
        Ability ability = AbilityController.Instance.resolveAbility(abilityKey);
        if (ability == null) {
            sendError(sender, "Ability not found: " + abilityKey);
            return;
        }

        // Use the ability's canonical ID for storage (registry key for built-in, UUID for custom)
        String canonicalKey = ability.getId();
        if (canonicalKey == null || canonicalKey.isEmpty()) {
            canonicalKey = abilityKey; // Fallback to user-provided key
        }
        String displayName = ability.getName() != null ? ability.getName() : canonicalKey;

        // Check if ability allows players
        if (!ability.getAllowedBy().allowsPlayer()) {
            sendError(sender, "Ability '\u00A7b" + displayName + "\u00A7c' is NPC-only and cannot be given to players.");
            return;
        }

        // Get player data and unlock
        PlayerData data = PlayerDataController.Instance.getPlayerData(player);
        if (data.abilityData.hasUnlockedAbility(canonicalKey)) {
            sendError(sender, "Player already has ability: " + displayName);
            return;
        }

        data.abilityData.unlockAbility(canonicalKey);
        sendResult(sender, "Unlocked ability '\u00A7b" + displayName + "\u00A77' for player \u00A7a" + playerName);
    }

    @SubCommand(
        desc = "Lock (revoke) an ability from a player",
        usage = "<player> <ability>"
    )
    public void lock(ICommandSender sender, String[] args) {
        if (args.length < 2) {
            sendError(sender, "Usage: /kam ability lock <player> <ability>");
            return;
        }

        String playerName = args[0];
        String abilityKey = joinArgs(args, 1);

        // Find player
        EntityPlayerMP player = (EntityPlayerMP) NoppesUtilServer.getPlayerByName(playerName);
        if (player == null) {
            sendError(sender, "Player not found: " + playerName);
            return;
        }

        // Resolve ability to get canonical key
        Ability ability = AbilityController.Instance.resolveAbility(abilityKey);
        String canonicalKey = abilityKey;
        String displayName = abilityKey;
        if (ability != null) {
            canonicalKey = ability.getId() != null ? ability.getId() : abilityKey;
            displayName = ability.getName() != null ? ability.getName() : canonicalKey;
        }

        // Get player data and lock
        PlayerData data = PlayerDataController.Instance.getPlayerData(player);
        if (!data.abilityData.hasUnlockedAbility(canonicalKey)) {
            sendError(sender, "Player doesn't have ability: " + displayName);
            return;
        }

        data.abilityData.lockAbility(canonicalKey);
        sendResult(sender, "Locked ability '\u00A7b" + displayName + "\u00A77' from player \u00A7a" + playerName);
    }

    @SubCommand(
        desc = "List abilities unlocked for a player",
        usage = "<player>"
    )
    public void player(ICommandSender sender, String[] args) {
        if (args.length < 1) {
            sendError(sender, "Usage: /kam ability player <player>");
            return;
        }

        String playerName = args[0];

        // Find player
        EntityPlayerMP player = (EntityPlayerMP) NoppesUtilServer.getPlayerByName(playerName);
        if (player == null) {
            sendError(sender, "Player not found: " + playerName);
            return;
        }

        PlayerData data = PlayerDataController.Instance.getPlayerData(player);
        String[] abilityKeys = data.abilityData.getUnlockedAbilities();

        if (abilityKeys.length == 0) {
            sendResult(sender, "Player \u00A7a" + playerName + "\u00A77 has no unlocked abilities.");
            return;
        }

        sendResult(sender, "Abilities for \u00A7a" + playerName + "\u00A77 (" + abilityKeys.length + "):");
        int selected = data.abilityData.getSelectedIndex();
        for (int i = 0; i < abilityKeys.length; i++) {
            String key = abilityKeys[i];
            Ability ability = AbilityController.Instance.resolveAbility(key);
            String displayName = ability != null && ability.getName() != null ? ability.getName() : key;
            String prefix = (i == selected) ? "\u00A7e> " : "  ";
            // Show display name and key if different
            if (!displayName.equals(key)) {
                sendResult(sender, prefix + "\u00A7b" + displayName + " \u00A78[" + key + "]");
            } else {
                sendResult(sender, prefix + "\u00A7b" + displayName);
            }
        }
    }

    private String joinArgs(String[] args, int startIndex) {
        StringBuilder sb = new StringBuilder();
        for (int i = startIndex; i < args.length; i++) {
            if (i > startIndex) sb.append(" ");
            sb.append(args[i]);
        }
        return sb.toString();
    }

    /**
     * Get list of player-usable ability names (for tab completion).
     * Uses registry keys for built-in abilities and display names for custom abilities.
     * Used by CommandKamkeel for <ability> usage token.
     */
    public static List<String> getPlayerAbilityNames() {
        List<String> keys = new ArrayList<>(AbilityController.Instance.getPlayerAbilityKeys());
        Collections.sort(keys);
        return keys;
    }
}
