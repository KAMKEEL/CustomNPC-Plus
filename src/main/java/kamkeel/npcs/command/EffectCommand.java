package kamkeel.npcs.command;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import noppes.npcs.controllers.PlayerDataController;
import noppes.npcs.controllers.data.CustomEffect;
import noppes.npcs.controllers.data.PlayerData;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class EffectCommand extends CommandKamkeelBase {

    @Override
    public String getDescription() {
        return "Custom Effect operations";
    }

    @Override
    public String getCommandName() {
        return "effect";
    }

    // info all
    @SubCommand(desc = "Lists all effects")
    public void infoAll(ICommandSender sender, String[] args) throws CommandException {
        // TODO: FIX EFFECT COMMAND
//        Set<Map.Entry<Integer, StatusEffect>> effects = DBCEffectController.getInstance().standardEffects.entrySet();
//        Set<Map.Entry<Integer, CustomEffect>> customEffects = DBCEffectController.getInstance().customEffects.entrySet();
//
//        if (effects.isEmpty() && customEffects.isEmpty()) {
//            sendError(sender, "No effects found.");
//            return;
//        }
//
//        sendResult(sender, "--------------------");
//
//        for (Map.Entry<Integer, StatusEffect> entry : effects) {
//            sendResult(sender, String.format("\u00a7b%2$s \u00a77(ID%3$s: %1$s)", entry.getKey(), entry.getValue().getName(), entry.getValue().isCustom() ? " - CUSTOM" : " "));
//        }
//        for (Map.Entry<Integer, CustomEffect> entry : customEffects) {
//            sendResult(sender, String.format("\u00a7b%2$s \u00a77(ID%3$s: %1$s)", entry.getKey(), entry.getValue().getName(), entry.getValue().isCustom() ? " - CUSTOM" : " "));
//        }
//
//        sendResult(sender, "--------------------");
    }

    @SubCommand(desc = "Gives a effect to a player", usage = "<player> <time> <effectName>")
    public void give(ICommandSender sender, String[] args) throws CommandException {
//        String playername = args[0];
//        int time = Integer.parseInt(args[1]);
//        String name = "";
//        for (int i = 2; i < args.length; i++) {
//            name += args[i] + (i != args.length - 1 ? " " : "");
//        }
//
//        List<PlayerData> data = PlayerDataController.Instance.getPlayersData(sender, playername);
//        if (data.isEmpty()) {
//            sendError(sender, "Unknown player: " + playername);
//            return;
//        }
//
//        StatusEffect statusEffect = DBCEffectController.getInstance().get(name);
//        if (statusEffect == null) {
//            sendError(sender, "Unknown effect: " + name);
//            return;
//        }
//
//        for (PlayerData playerdata : data) {
//            DBCEffectController.getInstance().applyEffect(playerdata.player, statusEffect.getID(), time);
//            sendResult(sender, String.format("%s §agiven to §7'§b%s§7'", statusEffect.getName(), playerdata.playername));
//            if (sender != playerdata.player) {
//                sendResult(playerdata.player, String.format("§Effect §7%s §aadded.", statusEffect.getName()));
//            }
//        }
    }


    @SubCommand(desc = "Removes a effect from a player", usage = "<player> <effectName>")
    public void remove(ICommandSender sender, String[] args) throws CommandException {
//        String playername = args[0];
//        String name = "";
//        for (int i = 1; i < args.length; i++) {
//            name += args[i] + (i != args.length - 1 ? " " : "");
//        }
//
//        List<PlayerData> data = PlayerDataController.Instance.getPlayersData(sender, playername);
//        if (data.isEmpty()) {
//            sendError(sender, "Unknown player: " + playername);
//            return;
//        }
//
//        StatusEffect statusEffect = DBCEffectController.getInstance().get(name);
//        if (statusEffect == null) {
//            sendError(sender, "Unknown effect: " + name);
//            return;
//        }
//
//        for (PlayerData playerData : data) {
//            DBCEffectController.getInstance().removeEffect(playerData.player, statusEffect.getID(), DBCPlayerEvent.EffectEvent.ExpirationType.REMOVED);
//            sendResult(sender, String.format("Effect %s removed from %s", statusEffect.getName(), playerData.playername));
//        }
    }

    @SubCommand(desc = "Gives a effect to a player", usage = "<player> <time> <effectId>")
    public void giveId(ICommandSender sender, String[] args) throws CommandException {
//        String playername = args[0];
//        int time = Integer.parseInt(args[1]);
//
//        List<PlayerData> data = PlayerDataController.Instance.getPlayersData(sender, playername);
//        if (data.isEmpty()) {
//            sendError(sender, "Unknown player: " + playername);
//            return;
//        }
//
//        for (PlayerData playerdata : data) {
//            int statusEffectId = Integer.parseInt(args[2]);
//            DBCEffectController.getInstance().applyEffect(playerdata.player, statusEffectId, time);
//            sendResult(sender, String.format("Effect %d given to %s", statusEffectId, playerdata.playername));
//        }
    }

    @SubCommand(desc = "Removes a effect from a player", usage = "<player> <effectId>")
    public void removeId(ICommandSender sender, String[] args) throws CommandException {
//        String playername = args[0];
//        List<PlayerData> data = PlayerDataController.Instance.getPlayersData(sender, playername);
//        if (data.isEmpty()) {
//            sendError(sender, "Unknown player: " + playername);
//            return;
//        }
//
//        for (PlayerData playerData : data) {
//            int statusEffectId = Integer.parseInt(args[1]);
//            DBCEffectController.getInstance().removeEffect(playerData.player, statusEffectId, DBCPlayerEvent.EffectEvent.ExpirationType.REMOVED);
//            sendResult(sender, String.format("Effect %d removed from %s", statusEffectId, playerData.playername));
//        }
    }
}
