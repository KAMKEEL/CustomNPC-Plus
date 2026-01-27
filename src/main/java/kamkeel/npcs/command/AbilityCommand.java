package kamkeel.npcs.command;

import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.AbilityController;
import net.minecraft.command.ICommandSender;

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
        desc = "List all saved abilities"
    )
    public void list(ICommandSender sender, String[] args) {
        Set<String> names = AbilityController.Instance.getSavedAbilityNames();
        if (names.isEmpty()) {
            sendResult(sender, "No saved abilities found.");
            return;
        }

        List<String> sortedNames = new ArrayList<>(names);
        Collections.sort(sortedNames);

        sendResult(sender, "Saved Abilities (" + sortedNames.size() + "):");
        for (String name : sortedNames) {
            Ability ability = AbilityController.Instance.getSavedAbility(name);
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
        sendResult(sender, "Abilities reloaded from disk.");
    }

    @SubCommand(
        desc = "Delete a saved ability",
        usage = "<name>"
    )
    public void delete(ICommandSender sender, String[] args) {
        if (args.length < 1) {
            sendError(sender, "Usage: /kam ability delete <name>");
            return;
        }

        String name = args[0];
        if (!AbilityController.Instance.getSavedAbilityNames().contains(name)) {
            sendError(sender, "No saved ability with name: " + name);
            return;
        }

        AbilityController.Instance.deleteAbility(name);
        sendResult(sender, "Deleted ability: \u00A7b" + name);
    }

    @SubCommand(
        desc = "Get info about a saved ability",
        usage = "<name>"
    )
    public void info(ICommandSender sender, String[] args) {
        if (args.length < 1) {
            sendError(sender, "Usage: /kam ability info <name>");
            return;
        }

        String name = args[0];
        Ability ability = AbilityController.Instance.getSavedAbility(name);
        if (ability == null) {
            sendError(sender, "No saved ability with name: " + name);
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
    }
}
