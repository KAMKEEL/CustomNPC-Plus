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
    }
}
