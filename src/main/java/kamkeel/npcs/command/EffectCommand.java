package kamkeel.npcs.command;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import noppes.npcs.controllers.PlayerDataController;
import noppes.npcs.controllers.CustomEffectController;
import noppes.npcs.controllers.data.CustomEffect;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.scripted.event.PlayerEvent.EffectEvent.ExpirationType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class EffectCommand extends CommandKamkeelBase {

    @Override
    public String getDescription() {
        return "Custom Effect operations";
    }

    @Override
    public String getCommandName() {
        return "effect";
    }

    @SubCommand(desc = "Lists all Effects")
    public void infoAll(ICommandSender sender, String[] args) throws CommandException {
        CustomEffectController controller = CustomEffectController.getInstance();
        if (controller.indexMapper.isEmpty()) {
            sendError(sender, "No effects found.");
            return;
        }

        sendResult(sender, "--------------------");

        // Get all indices, sort them in ascending order.
        List<Integer> indices = new ArrayList<>(controller.indexMapper.keySet());
        Collections.sort(indices);
        for (Integer idx : indices) {
            Map<Integer, CustomEffect> effects = controller.indexMapper.get(idx);
            if (effects == null || effects.isEmpty())
                continue;
            // Sort the effect IDs in ascending order.
            List<Integer> effectIds = new ArrayList<>(effects.keySet());
            Collections.sort(effectIds);
            for (Integer id : effectIds) {
                CustomEffect effect = effects.get(id);
                sendResult(sender, String.format("\u00a7b%s \u00a77(ID: %d, INDEX: %d)",
                    effect.getName(),
                    id,
                    idx));
            }
        }

        sendResult(sender, "--------------------");
    }

    // Give effect by name.
    // Usage: <player> <time> <effectName> [index]
    @SubCommand(desc = "Gives an effect to a player", usage = "<player> <time> <effectName> [index]")
    public void give(ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 3) {
            sendError(sender, "Usage: <player> <time> <effectName> [index]");
            return;
        }

        String playername = args[0];
        int time;
        try {
            time = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            sendError(sender, "Invalid time: " + args[1]);
            return;
        }

        int index = 0;
        String effectName;
        if (args.length > 3) {
            // Try to parse the last argument as the index.
            try {
                index = Integer.parseInt(args[args.length - 1]);
                StringBuilder sb = new StringBuilder();
                for (int i = 2; i < args.length - 1; i++) {
                    sb.append(args[i]).append(i < args.length - 2 ? " " : "");
                }
                effectName = sb.toString();
            } catch (NumberFormatException e) {
                // If parsing fails, assume the entire remainder is the effect name.
                StringBuilder sb = new StringBuilder();
                for (int i = 2; i < args.length; i++) {
                    sb.append(args[i]).append(i < args.length - 1 ? " " : "");
                }
                effectName = sb.toString();
            }
        } else {
            effectName = args[2];
        }

        List<PlayerData> players = PlayerDataController.Instance.getPlayersData(sender, playername);
        if (players.isEmpty()) {
            sendError(sender, "Unknown player: " + playername);
            return;
        }

        CustomEffect effect = CustomEffectController.getInstance().get(effectName, index);
        if (effect == null) {
            sendError(sender, "Unknown effect: " + effectName + " with index " + index);
            return;
        }

        for (PlayerData pdata : players) {
            CustomEffectController.getInstance().applyEffect(pdata.player, effect.getID(), time, (byte) 1, index);
            sendResult(sender, String.format("%s §agiven to §7'§b%s§7'", effect.getName(), pdata.playername));
            if (sender != pdata.player) {
                sendResult(pdata.player, String.format("Effect §7%s §aadded.", effect.getName()));
            }
        }
    }

    // Remove effect by name.
    // Usage: <player> <effectName> [index]
    @SubCommand(desc = "Removes an effect from a player", usage = "<player> <effectName> [index]")
    public void remove(ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 2) {
            sendError(sender, "Usage: <player> <effectName> [index]");
            return;
        }

        String playername = args[0];
        int index = 0;
        String effectName;
        if (args.length > 2) {
            try {
                index = Integer.parseInt(args[args.length - 1]);
                StringBuilder sb = new StringBuilder();
                for (int i = 1; i < args.length - 1; i++) {
                    sb.append(args[i]).append(i < args.length - 2 ? " " : "");
                }
                effectName = sb.toString();
            } catch (NumberFormatException e) {
                StringBuilder sb = new StringBuilder();
                for (int i = 1; i < args.length; i++) {
                    sb.append(args[i]).append(i < args.length - 1 ? " " : "");
                }
                effectName = sb.toString();
            }
        } else {
            effectName = args[1];
        }

        List<PlayerData> players = PlayerDataController.Instance.getPlayersData(sender, playername);
        if (players.isEmpty()) {
            sendError(sender, "Unknown player: " + playername);
            return;
        }

        CustomEffect effect = CustomEffectController.getInstance().get(effectName, index);
        if (effect == null) {
            sendError(sender, "Unknown effect: " + effectName + " with index " + index);
            return;
        }

        for (PlayerData pdata : players) {
            CustomEffectController.getInstance().removeEffect(pdata.player, effect.getID(), index, ExpirationType.REMOVED);
            sendResult(sender, String.format("Effect %s removed from %s", effect.getName(), pdata.playername));
        }
    }

    // Give effect by ID.
    // Usage: <player> <time> <effectId> [index]
    @SubCommand(desc = "Gives an effect to a player by ID", usage = "<player> <time> <effectId> [index]")
    public void giveId(ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 3) {
            sendError(sender, "Usage: <player> <time> <effectId> [index]");
            return;
        }

        String playername = args[0];
        int time;
        try {
            time = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            sendError(sender, "Invalid time: " + args[1]);
            return;
        }

        int effectId;
        int index = 0;
        try {
            effectId = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            sendError(sender, "Invalid effect ID: " + args[2]);
            return;
        }
        if (args.length > 3) {
            try {
                index = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                index = 0;
            }
        }

        List<PlayerData> players = PlayerDataController.Instance.getPlayersData(sender, playername);
        if (players.isEmpty()) {
            sendError(sender, "Unknown player: " + playername);
            return;
        }

        CustomEffect effect = CustomEffectController.getInstance().get(effectId, index);
        if (effect == null) {
            sendError(sender, "Unknown effect with ID: " + effectId + " and index " + index);
            return;
        }

        for (PlayerData pdata : players) {
            CustomEffectController.getInstance().applyEffect(pdata.player, effectId, time, (byte) 1, index);
            sendResult(sender, String.format("Effect %d given to %s", effectId, pdata.playername));
        }
    }

    // Remove effect by ID.
    // Usage: <player> <effectId> [index]
    @SubCommand(desc = "Removes an effect from a player by ID", usage = "<player> <effectId> [index]")
    public void removeId(ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 2) {
            sendError(sender, "Usage: <player> <effectId> [index]");
            return;
        }

        String playername = args[0];
        int effectId;
        int index = 0;
        try {
            effectId = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            sendError(sender, "Invalid effect ID: " + args[1]);
            return;
        }
        if (args.length > 2) {
            try {
                index = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                index = 0;
            }
        }

        List<PlayerData> players = PlayerDataController.Instance.getPlayersData(sender, playername);
        if (players.isEmpty()) {
            sendError(sender, "Unknown player: " + playername);
            return;
        }

        CustomEffect effect = CustomEffectController.getInstance().get(effectId, index);
        if (effect == null) {
            sendError(sender, "Unknown effect with ID: " + effectId + " and index " + index);
            return;
        }

        for (PlayerData pdata : players) {
            CustomEffectController.getInstance().removeEffect(pdata.player, effectId, index, ExpirationType.REMOVED);
            sendResult(sender, String.format("Effect %d removed from %s", effectId, pdata.playername));
        }
    }
}
