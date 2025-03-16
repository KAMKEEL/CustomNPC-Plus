package kamkeel.npcs.command;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import noppes.npcs.controllers.CustomEffectController;
import noppes.npcs.controllers.PlayerDataController;
import noppes.npcs.controllers.data.CustomEffect;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.scripted.event.PlayerEvent.EffectEvent.ExpirationType;

import java.util.*;

import static kamkeel.npcs.util.ColorUtil.sendError;
import static kamkeel.npcs.util.ColorUtil.sendResult;

public class EffectCommand extends CommandKamkeelBase {

    @Override
    public String getDescription() {
        return "Custom Effect operations";
    }

    @Override
    public String getCommandName() {
        return "effect";
    }

    // List command with pagination
    @SubCommand(desc = "Lists all Effects", usage = "[page]")
    public void list(ICommandSender sender, String[] args) throws CommandException {
        CustomEffectController controller = CustomEffectController.getInstance();
        if (controller.indexMapper.isEmpty()) {
            sendError(sender, "No effects found.");
            return;
        }

        // Flatten effects into a list
        List<EffectEntry> effectsList = new ArrayList<>();
        for (Map.Entry<Integer, HashMap<Integer, CustomEffect>> entry : controller.indexMapper.entrySet()) {
            int idx = entry.getKey();
            Map<Integer, CustomEffect> effects = entry.getValue();
            if (effects == null || effects.isEmpty())
                continue;
            for (Map.Entry<Integer, CustomEffect> effectEntry : effects.entrySet()) {
                int id = effectEntry.getKey();
                CustomEffect effect = effectEntry.getValue();
                effectsList.add(new EffectEntry(idx, id, effect));
            }
        }
        // Sort by index then by effect ID
        Collections.sort(effectsList, Comparator.comparingInt((EffectEntry e) -> e.index)
            .thenComparingInt(e -> e.id));

        // Determine page number
        int page = 1;
        if (args.length > 0) {
            try {
                page = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                // default to page 1
            }
        }
        int perPage = 10;
        int total = effectsList.size();
        int totalPages = (int) Math.ceil(total / (double) perPage);
        if (page < 1) page = 1;
        if (page > totalPages) page = totalPages;
        int startIndex = (page - 1) * perPage;
        int endIndex = Math.min(startIndex + perPage, total);

        sendResult(sender, "--------------------");
        sendResult(sender, "Effects (Page " + page + "/" + totalPages + "):");
        for (int i = startIndex; i < endIndex; i++) {
            EffectEntry entry = effectsList.get(i);
            sendResult(sender, String.format("\u00a7b%s \u00a77(ID: %d, INDEX: %d)",
                entry.effect.getName(),
                entry.id,
                entry.index));
        }
        sendResult(sender, "--------------------");
    }


    // Give effect by name.
    // Usage: <player> <time> <effectName>
    @SubCommand(desc = "Gives an effect to a player", usage = "<player> <time> <effectName>")
    public void give(ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 3) {
            sendError(sender, "Usage: <player> <time> <effectName>");
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

        // Join all remaining arguments as the effect name.
        StringBuilder sb = new StringBuilder();
        for (int i = 2; i < args.length; i++) {
            sb.append(args[i]).append(i < args.length - 1 ? " " : "");
        }
        String effectName = sb.toString();

        List<EffectEntry> matches = findEffectsByName(effectName);
        if (matches.isEmpty()) {
            sendError(sender, "Unknown effect: " + effectName);
            return;
        }
        if (matches.size() > 1) {
            StringBuilder ambig = new StringBuilder();
            ambig.append("Ambiguous effect name '").append(effectName).append("'. Multiple matches: ");
            for (EffectEntry match : matches) {
                ambig.append(match.effect.getName())
                    .append(" (ID: ").append(match.id)
                    .append(", INDEX: ").append(match.index)
                    .append("), ");
            }
            sendError(sender, ambig.substring(0, ambig.length() - 2));
            return;
        }
        EffectEntry match = matches.get(0);

        List<PlayerData> players = PlayerDataController.Instance.getPlayersData(sender, playername);
        if (players.isEmpty()) {
            sendError(sender, "Unknown player: " + playername);
            return;
        }

        for (PlayerData pdata : players) {
            if (pdata.player != null) {
                CustomEffectController.getInstance().applyEffect(pdata.player, match.id, time, (byte) 1, match.index);
                sendResult(sender, String.format("%s §agiven to §7'§b%s§7'", match.effect.getName(), pdata.playername));
            }
        }
    }

    // Remove effect by name.
    // Usage: <player> <effectName>
    @SubCommand(desc = "Removes an effect from a player", usage = "<player> <effectName>")
    public void remove(ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 2) {
            sendError(sender, "Usage: <player> <effectName>");
            return;
        }

        String playername = args[0];
        // Join all remaining arguments as the effect name.
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            sb.append(args[i]).append(i < args.length - 1 ? " " : "");
        }
        String effectName = sb.toString();

        List<EffectEntry> matches = findEffectsByName(effectName);
        if (matches.isEmpty()) {
            sendError(sender, "Unknown effect: " + effectName);
            return;
        }
        if (matches.size() > 1) {
            StringBuilder ambig = new StringBuilder();
            ambig.append("Ambiguous effect name '").append(effectName).append("'. Multiple matches: ");
            for (EffectEntry match : matches) {
                ambig.append(match.effect.getName())
                    .append(" (ID: ").append(match.id)
                    .append(", INDEX: ").append(match.index)
                    .append("), ");
            }
            sendError(sender, ambig.substring(0, ambig.length() - 2));
            return;
        }
        EffectEntry match = matches.get(0);

        List<PlayerData> players = PlayerDataController.Instance.getPlayersData(sender, playername);
        if (players.isEmpty()) {
            sendError(sender, "Unknown player: " + playername);
            return;
        }

        for (PlayerData pdata : players) {
            if (pdata.player != null) {
                CustomEffectController.getInstance().removeEffect(pdata.player, match.id, match.index, ExpirationType.REMOVED);
                sendResult(sender, String.format("Effect %s removed from %s", match.effect.getName(), pdata.playername));
            }
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
            if (pdata.player != null) {
                CustomEffectController.getInstance().applyEffect(pdata.player, effect.id, time, (byte) 1, effect.index);
                sendResult(sender, String.format("%s §agiven to §7'§b%s§7'", effect.getName(), pdata.playername));
            }
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
            if (pdata.player != null) {
                CustomEffectController.getInstance().removeEffect(pdata.player, effect.id, effect.index, ExpirationType.REMOVED);
                sendResult(sender, String.format("Effect %s removed from %s", effect.getName(), pdata.playername));
            }
        }
    }

    private static class EffectEntry {
        int index;
        int id;
        CustomEffect effect;

        public EffectEntry(int index, int id, CustomEffect effect) {
            this.index = index;
            this.id = id;
            this.effect = effect;
        }
    }

    // Helper method to find effects by name.
    private List<EffectEntry> findEffectsByName(String name) {
        List<EffectEntry> matches = new ArrayList<>();
        CustomEffectController controller = CustomEffectController.getInstance();
        for (Map.Entry<Integer, HashMap<Integer, CustomEffect>> entry : controller.indexMapper.entrySet()) {
            int idx = entry.getKey();
            Map<Integer, CustomEffect> effects = entry.getValue();
            if (effects == null) continue;
            for (Map.Entry<Integer, CustomEffect> effectEntry : effects.entrySet()) {
                int id = effectEntry.getKey();
                CustomEffect effect = effectEntry.getValue();
                if (effect.getName().equalsIgnoreCase(name)) {
                    List<EffectEntry> exact = new ArrayList<>();
                    exact.add(new EffectEntry(idx, id, effect));
                    return exact;
                } else if (effect.getName().toLowerCase().contains(name.toLowerCase())) {
                    matches.add(new EffectEntry(idx, id, effect));
                }
            }
        }
        return matches;
    }

    public static List<String> getSortedEffectNames() {
        List<EffectEntry> entries = new ArrayList<>();
        CustomEffectController controller = CustomEffectController.getInstance();
        for (Map.Entry<Integer, HashMap<Integer, CustomEffect>> indexEntry : controller.indexMapper.entrySet()) {
            int index = indexEntry.getKey();
            Map<Integer, CustomEffect> effects = indexEntry.getValue();
            if (effects != null) {
                for (Map.Entry<Integer, CustomEffect> effectEntry : effects.entrySet()) {
                    int id = effectEntry.getKey();
                    CustomEffect effect = effectEntry.getValue();
                    entries.add(new EffectEntry(index, id, effect));
                }
            }
        }
        // Sort entries by index then by effect ID.
        entries.sort(Comparator.comparingInt((EffectEntry e) -> e.index)
            .thenComparingInt(e -> e.id));
        List<String> effectNames = new ArrayList<>();
        for (EffectEntry entry : entries) {
            effectNames.add(entry.effect.getName());
        }
        return effectNames;
    }
}
