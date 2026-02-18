package kamkeel.npcs.command;

import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.AbilityController;
import kamkeel.npcs.controllers.data.ability.ChainedAbility;
import kamkeel.npcs.controllers.data.ability.UserType;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.controllers.PlayerDataController;
import noppes.npcs.controllers.data.PlayerAbilityData;
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
        Set<String> names = AbilityController.Instance.getCustomAbilityNames();
        if (names.isEmpty()) {
            sendResult(sender, "No custom abilities found.");
            return;
        }

        sendResult(sender, "Custom Abilities (" + names.size() + "):");
        for (String name : names) {
            Ability ability = AbilityController.Instance.getCustomAbility(name);
            if (ability != null) {
                sendResult(sender, "  - \u00A7b" + name + "\u00A77 (" + ability.getTypeId() + ")");
            } else {
                sendResult(sender, "  - \u00A7b" + name);
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
        sendResult(sender, "Abilities and chained abilities reloaded from disk.");
    }

    @SubCommand(
        desc = "Delete a custom ability by name",
        usage = "<name>"
    )
    public void delete(ICommandSender sender, String[] args) {
        if (args.length < 1) {
            sendError(sender, "Usage: /kam ability delete <name>");
            return;
        }

        String name = joinArgs(args, 0);
        if (!AbilityController.Instance.hasCustomAbilityName(name)) {
            sendError(sender, "No custom ability with name: " + name);
            return;
        }

        AbilityController.Instance.deleteCustomAbility(name);
        sendResult(sender, "Deleted ability: \u00A7b" + name);
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

        String key = joinArgs(args, 0);
        Ability ability = AbilityController.Instance.resolveAbility(key);
        if (ability == null) {
            sendError(sender, "No ability found for: " + key);
            return;
        }

        sendResult(sender, "Ability: \u00A7b" + ability.getDisplayName());
        sendResult(sender, "  Name: \u00A77" + ability.getName());
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
        desc = "Give an ability to a player",
        usage = "<player> <ability>"
    )
    public void give(ICommandSender sender, String[] args) {
        if (args.length < 2) {
            sendError(sender, "Usage: /kam ability give <player> <ability>");
            return;
        }

        String playerName = args[0];
        String abilityKey = joinArgs(args, 1);

        EntityPlayerMP player = (EntityPlayerMP) NoppesUtilServer.getPlayerByName(playerName);
        if (player == null) {
            sendError(sender, "Player not found: " + playerName);
            return;
        }

        Ability ability = AbilityController.Instance.resolveAbility(abilityKey);
        if (ability == null) {
            sendError(sender, "Ability not found: " + abilityKey);
            return;
        }

        // Use the ability's canonical ID for storage (registry key for built-in, UUID for custom)
        String canonicalKey = ability.getId();
        if (canonicalKey == null || canonicalKey.isEmpty()) {
            canonicalKey = abilityKey;
        }
        String displayName = ability.getDisplayName();

        if (!ability.getAllowedBy().allowsPlayer()) {
            sendError(sender, "Ability '\u00A7b" + displayName + "\u00A7c' is NPC-only and cannot be given to players.");
            return;
        }

        PlayerData data = PlayerDataController.Instance.getPlayerData(player);
        if (data.abilityData.hasUnlockedAbility(canonicalKey)) {
            sendError(sender, "Player already has ability: " + displayName);
            return;
        }

        data.abilityData.unlockAbility(canonicalKey);
        sendResult(sender, "Gave ability '\u00A7b" + displayName + "\u00A77' to player \u00A7a" + playerName);
    }

    @SubCommand(
        desc = "Remove an ability from a player",
        usage = "<player> <ability>"
    )
    public void remove(ICommandSender sender, String[] args) {
        if (args.length < 2) {
            sendError(sender, "Usage: /kam ability remove <player> <ability>");
            return;
        }

        String playerName = args[0];
        String abilityKey = joinArgs(args, 1);

        EntityPlayerMP player = (EntityPlayerMP) NoppesUtilServer.getPlayerByName(playerName);
        if (player == null) {
            sendError(sender, "Player not found: " + playerName);
            return;
        }

        Ability ability = AbilityController.Instance.resolveAbility(abilityKey);
        String canonicalKey = abilityKey;
        String displayName = abilityKey;
        if (ability != null) {
            canonicalKey = ability.getId() != null ? ability.getId() : abilityKey;
            displayName = ability.getDisplayName();
        }

        PlayerData data = PlayerDataController.Instance.getPlayerData(player);
        if (!data.abilityData.hasUnlockedAbility(canonicalKey)) {
            sendError(sender, "Player doesn't have ability: " + displayName);
            return;
        }

        data.abilityData.lockAbility(canonicalKey);
        sendResult(sender, "Removed ability '\u00A7b" + displayName + "\u00A77' from player \u00A7a" + playerName);
    }

    @SubCommand(
        desc = "Give a chained ability to a player",
        usage = "<player> <chain>"
    )
    public void giveChain(ICommandSender sender, String[] args) {
        if (args.length < 2) {
            sendError(sender, "Usage: /kam ability giveChain <player> <chain>");
            return;
        }

        String playerName = args[0];
        String chainKey = joinArgs(args, 1);

        EntityPlayerMP player = (EntityPlayerMP) NoppesUtilServer.getPlayerByName(playerName);
        if (player == null) {
            sendError(sender, "Player not found: " + playerName);
            return;
        }

        ChainedAbility chain = AbilityController.Instance.resolveChainedAbility(chainKey);
        if (chain == null) {
            sendError(sender, "Chained ability not found: " + chainKey);
            return;
        }

        if (!chain.getAllowedBy().allowsPlayer()) {
            sendError(sender, "Chained ability '\u00A7b" + chain.getDisplayName() + "\u00A7c' is NPC-only and cannot be given to players.");
            return;
        }

        String chainId = chain.getId();
        if (chainId == null || chainId.isEmpty()) {
            sendError(sender, "Chained ability has no ID: " + chain.getDisplayName());
            return;
        }

        // Store by UUID: chain:<uuid>
        String storageKey = PlayerAbilityData.CHAIN_PREFIX + chainId;
        PlayerData data = PlayerDataController.Instance.getPlayerData(player);
        if (data.abilityData.hasUnlockedAbility(storageKey)) {
            sendError(sender, "Player already has chained ability: " + chain.getDisplayName());
            return;
        }

        data.abilityData.unlockAbility(storageKey);
        sendResult(sender, "Gave chained ability '\u00A7b" + chain.getDisplayName() + "\u00A77' to player \u00A7a" + playerName);
    }

    @SubCommand(
        desc = "Remove a chained ability from a player",
        usage = "<player> <chain>"
    )
    public void removeChain(ICommandSender sender, String[] args) {
        if (args.length < 2) {
            sendError(sender, "Usage: /kam ability removeChain <player> <chain>");
            return;
        }

        String playerName = args[0];
        String chainKey = joinArgs(args, 1);

        EntityPlayerMP player = (EntityPlayerMP) NoppesUtilServer.getPlayerByName(playerName);
        if (player == null) {
            sendError(sender, "Player not found: " + playerName);
            return;
        }

        ChainedAbility chain = AbilityController.Instance.resolveChainedAbility(chainKey);
        if (chain == null) {
            sendError(sender, "Chained ability not found: " + chainKey);
            return;
        }

        String chainId = chain.getId();
        if (chainId == null || chainId.isEmpty()) {
            sendError(sender, "Chained ability has no ID: " + chain.getDisplayName());
            return;
        }

        // Storage uses UUID: chain:<uuid>
        String storageKey = PlayerAbilityData.CHAIN_PREFIX + chainId;
        PlayerData data = PlayerDataController.Instance.getPlayerData(player);
        if (!data.abilityData.hasUnlockedAbility(storageKey)) {
            sendError(sender, "Player doesn't have chained ability: " + chain.getDisplayName());
            return;
        }

        data.abilityData.lockAbility(storageKey);
        sendResult(sender, "Removed chained ability '\u00A7b" + chain.getDisplayName() + "\u00A77' from player \u00A7a" + playerName);
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
            String prefix = (i == selected) ? "\u00A7e> " : "  ";

            if (key.startsWith(PlayerAbilityData.CHAIN_PREFIX)) {
                // Chained ability
                String chainName = key.substring(PlayerAbilityData.CHAIN_PREFIX.length());
                ChainedAbility chain = AbilityController.Instance.resolveChainedAbility(chainName);
                String displayName = chain != null ? chain.getDisplayName() : chainName;
                sendResult(sender, prefix + "\u00A7d[Chain] \u00A7b" + displayName);
            } else {
                // Regular ability
                Ability ability = AbilityController.Instance.resolveAbility(key);
                String displayName = ability != null ? ability.getDisplayName() : key;
                if (!displayName.equals(key)) {
                    sendResult(sender, prefix + "\u00A7b" + displayName + " \u00A78[" + key + "]");
                } else {
                    sendResult(sender, prefix + "\u00A7b" + displayName);
                }
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
     * Resolves UUIDs to names so tab completion shows human-readable names.
     * Used by CommandKamkeel for {@code <ability>} usage token.
     */
    public static List<String> getPlayerAbilityNames() {
        Set<String> keys = AbilityController.Instance.getPlayerAbilityKeys();
        List<String> names = new ArrayList<>();
        for (String key : keys) {
            Ability a = AbilityController.Instance.resolveAbility(key);
            names.add(a != null ? a.getName() : key);
        }
        Collections.sort(names);
        return names;
    }

    /**
     * Get list of player-usable chained ability names (for tab completion).
     * Used by CommandKamkeel for {@code <chain>} usage token.
     */
    public static List<String> getPlayerChainNames() {
        List<String> names = new ArrayList<>();
        for (String chainName : AbilityController.Instance.getChainedAbilityNamesSet()) {
            ChainedAbility chain = AbilityController.Instance.getChainedAbility(chainName);
            if (chain != null && chain.getAllowedBy().allowsPlayer()) {
                names.add(chainName);
            }
        }
        Collections.sort(names);
        return names;
    }
}
