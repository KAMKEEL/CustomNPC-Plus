package kamkeel.npcs.command;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import noppes.npcs.config.ConfigMarket;
import noppes.npcs.controllers.PlayerDataController;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.controllers.data.PlayerTradeData;

import java.util.List;

import static kamkeel.npcs.util.ColorUtil.sendError;
import static kamkeel.npcs.util.ColorUtil.sendResult;

public class MoneyCommand extends CommandKamkeelBase {

    @Override
    public String getCommandName() {
        return "money";
    }

    @Override
    public String getDescription() {
        return "Currency operations";
    }

    @SubCommand(
        desc = "Show player's balance",
        usage = "<player>"
    )
    public void balance(ICommandSender sender, String[] args) throws CommandException {
        String playername = args[0];
        List<PlayerData> dataList = PlayerDataController.Instance.getPlayersData(sender, playername);

        if (dataList.isEmpty()) {
            sendError(sender, "Unknown player: " + playername);
            return;
        }

        for (PlayerData playerdata : dataList) {
            PlayerTradeData currency = playerdata.tradeData;
            long balance = currency.getBalance();
            sendResult(sender, String.format("Player \u00A7b%s\u00A77 has \u00A76%,d %s",
                playerdata.playername, balance, ConfigMarket.CurrencyName));
        }
    }

    @SubCommand(
        desc = "Give currency to player",
        usage = "<player> <amount>"
    )
    public void give(ICommandSender sender, String[] args) throws CommandException {
        String playername = args[0];
        long amount;
        try {
            amount = Long.parseLong(args[1]);
        } catch (NumberFormatException ex) {
            sendError(sender, "Invalid amount: " + args[1]);
            return;
        }

        if (amount <= 0) {
            sendError(sender, "Amount must be positive");
            return;
        }

        List<PlayerData> dataList = PlayerDataController.Instance.getPlayersData(sender, playername);

        if (dataList.isEmpty()) {
            sendError(sender, "Unknown player: " + playername);
            return;
        }

        for (PlayerData playerdata : dataList) {
            PlayerTradeData currency = playerdata.tradeData;
            if (currency.deposit(amount)) {
                playerdata.save();
                sendResult(sender, String.format("Gave \u00A76%,d %s\u00A77 to player \u00A7b%s\u00A77. New balance: \u00A76%,d",
                    amount, ConfigMarket.CurrencyName, playerdata.playername, currency.getBalance()));
            } else {
                sendError(sender, String.format("Failed to give currency to %s (would exceed max balance)",
                    playerdata.playername));
            }
        }
    }

    @SubCommand(
        desc = "Withdraw currency from player",
        usage = "<player> <amount>"
    )
    public void withdraw(ICommandSender sender, String[] args) throws CommandException {
        String playername = args[0];
        long amount;
        try {
            amount = Long.parseLong(args[1]);
        } catch (NumberFormatException ex) {
            sendError(sender, "Invalid amount: " + args[1]);
            return;
        }

        if (amount <= 0) {
            sendError(sender, "Amount must be positive");
            return;
        }

        List<PlayerData> dataList = PlayerDataController.Instance.getPlayersData(sender, playername);

        if (dataList.isEmpty()) {
            sendError(sender, "Unknown player: " + playername);
            return;
        }

        for (PlayerData playerdata : dataList) {
            PlayerTradeData currency = playerdata.tradeData;
            if (currency.withdraw(amount)) {
                playerdata.save();
                sendResult(sender, String.format("Withdrew \u00A76%,d %s\u00A77 from player \u00A7b%s\u00A77. New balance: \u00A76%,d",
                    amount, ConfigMarket.CurrencyName, playerdata.playername, currency.getBalance()));
            } else {
                sendError(sender, String.format("Failed to withdraw from %s (insufficient funds)",
                    playerdata.playername));
            }
        }
    }

    @SubCommand(
        desc = "Set player's balance",
        usage = "<player> <amount>"
    )
    public void set(ICommandSender sender, String[] args) throws CommandException {
        String playername = args[0];
        long amount;
        try {
            amount = Long.parseLong(args[1]);
        } catch (NumberFormatException ex) {
            sendError(sender, "Invalid amount: " + args[1]);
            return;
        }

        if (amount < 0) {
            sendError(sender, "Amount cannot be negative");
            return;
        }

        List<PlayerData> dataList = PlayerDataController.Instance.getPlayersData(sender, playername);

        if (dataList.isEmpty()) {
            sendError(sender, "Unknown player: " + playername);
            return;
        }

        for (PlayerData playerdata : dataList) {
            PlayerTradeData currency = playerdata.tradeData;
            currency.setBalance(amount);
            playerdata.save();
            sendResult(sender, String.format("Set balance of player \u00A7b%s\u00A77 to \u00A76%,d %s",
                playerdata.playername, currency.getBalance(), ConfigMarket.CurrencyName));
        }
    }

    @Override
    public List addTabCompletionOptions(ICommandSender par1, String[] args) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, new String[]{"balance", "give", "withdraw", "set"});
        }
        return null;
    }
}
