package kamkeel.npcs.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.player.EntityPlayer;
import kamkeel.npcs.controllers.AttributeController;
import kamkeel.npcs.controllers.data.attribute.AttributeDefinition;
import kamkeel.npcs.controllers.data.attribute.AttributeValueType;
import kamkeel.npcs.util.AttributeItemUtil;
import noppes.npcs.controllers.MagicController;

import static kamkeel.npcs.util.ColorUtil.sendError;
import static kamkeel.npcs.util.ColorUtil.sendResult;

public class AttributeCommand extends CommandKamkeelBase {

    @Override
    public String getCommandName() {
        return "attribute";
    }

    @Override
    public String getDescription() {
        return "Manage attributes on your held item";
    }

    @SubCommand(
        name = "list",
        usage = "[page]",
        desc = "Lists all available attributes"
    )
    public void list(ICommandSender sender, String[] args) throws CommandException {
        // Get all registered attributes.
        List<AttributeDefinition> defs = new ArrayList<AttributeDefinition>(AttributeController.getAllAttributes());
        if(defs.isEmpty()){
            sendError(sender, "No attributes found.");
            return;
        }
        // Sort attributes by display name.
        Collections.sort(defs, new Comparator<AttributeDefinition>() {
            @Override
            public int compare(AttributeDefinition a, AttributeDefinition b) {
                return a.getKey().compareToIgnoreCase(b.getKey());
            }
        });

        // Determine which page to show.
        int page = 1;
        if(args.length > 0) {
            try {
                page = Integer.parseInt(args[0]);
            } catch(NumberFormatException e) {
                // If parsing fails, default to page 1.
            }
        }
        int perPage = 10;
        int total = defs.size();
        int totalPages = (int)Math.ceil(total / (double) perPage);
        if(page < 1) page = 1;
        if(page > totalPages) page = totalPages;
        int startIndex = (page - 1) * perPage;
        int endIndex = Math.min(startIndex + perPage, total);

        sendResult(sender, "--------------------");
        sendResult(sender, "Attributes (Page " + page + "/" + totalPages + "):");
        for (int i = startIndex; i < endIndex; i++) {
            AttributeDefinition def = defs.get(i);
            // First line: numbered display name.
            sendResult(sender, (i + 1) + ". " + def.getDisplayName());
            // Second line: key and value type.
            sendResult(sender, "   - " + def.getKey() + ", " + def.getValueType());
        }
        sendResult(sender, "--------------------");
    }

    @SubCommand(
        name = "apply",
        usage = "<attribute> <value> [magicID]",
        desc = "Applies an attribute to your held item. For magic attributes, provide a magicID."
    )
    public void apply(ICommandSender sender, String[] args) throws CommandException {
        if(!(sender instanceof EntityPlayer)){
            sendError(sender, "Only players can use this command.");
            return;
        }

        EntityPlayer player = (EntityPlayer)sender;
        ItemStack held = player.getHeldItem();
        if(held == null){
            sendError(sender, "You are not holding an item.");
            return;
        }

        String attrKey = args[0];
        AttributeDefinition def = AttributeController.getAttribute(attrKey);
        if(def == null){
            sendError(sender, "Unknown attribute: " + attrKey);
            return;
        }

        // Check if this is a magic attribute.
        if(def.getValueType() == AttributeValueType.MAGIC) {
            if(args.length < 3){
                sendError(sender, "Usage for magic attribute: apply <attribute> <value> <magicID>");
                return;
            }
            int magicId;
            float value;
            try {
                value = Float.parseFloat(args[1]);
                magicId = Integer.parseInt(args[2]);
            } catch(NumberFormatException e) {
                sendError(sender, "Invalid number format for magicID or value.");
                return;
            }

            if(MagicController.getInstance().getMagic(magicId) == null){
                sendError(sender, "No Magic Found for Magic ID %d", magicId);
                return;
            }

            AttributeItemUtil.writeMagicAttribute(held, attrKey, magicId, value);
            sendResult(sender, "Applied magic attribute " + attrKey + " with magicID " + magicId + " and value " + value + " to held item.");
        } else {
            if(args.length < 2){
                sendError(sender, "Usage: <attribute> <value>");
                return;
            }
            float value;
            try {
                value = Float.parseFloat(args[1]);
            } catch(NumberFormatException e) {
                sendError(sender, "Invalid number format for value.");
                return;
            }
            AttributeItemUtil.applyAttribute(held, attrKey, value);
            sendResult(sender, "Applied attribute " + attrKey + " with value " + value + " to held item.");
        }
    }

    @SubCommand(
        name = "remove",
        usage = "<attribute> [magicID)",
        desc = "Removes an attribute from your held item. For magic attributes, provide a magicID."
    )
    public void remove(ICommandSender sender, String[] args) throws CommandException {
        if(!(sender instanceof EntityPlayer)){
            sendError(sender, "Only players can use this command.");
            return;
        }
        EntityPlayer player = (EntityPlayer)sender;
        ItemStack held = player.getHeldItem();
        if(held == null){
            sendError(sender, "You are not holding an item.");
            return;
        }

        String attrKey = args[0];
        AttributeDefinition def = AttributeController.getAttribute(attrKey);
        if(def == null){
            sendError(sender, "Unknown attribute: " + attrKey);
            return;
        }

        if(def.getValueType() == AttributeValueType.MAGIC) {
            if(args.length < 2){
                sendError(sender, "Usage for magic attribute: remove <attribute> <magicID>");
                return;
            }
            int magicId;
            try {
                magicId = Integer.parseInt(args[1]);
            } catch(NumberFormatException e) {
                sendError(sender, "Invalid magicID format.");
                return;
            }

            if(MagicController.getInstance().getMagic(magicId) == null){
                sendError(sender, "No Magic Found for Magic ID %d", magicId);
                return;
            }

            AttributeItemUtil.removeMagicAttribute(held, attrKey, magicId);
            sendResult(sender, "Removed magic attribute " + attrKey + " (magicID " + magicId + ") from held item.");
        } else {
            AttributeItemUtil.removeAttribute(held, attrKey);
            sendResult(sender, "Removed attribute " + attrKey + " from held item.");
        }
    }

    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] args) {
        // For the first parameter of the subcommand (apply or remove), list available attribute keys.
        if(args.length == 1) {
            List<String> keys = new ArrayList<>();
            for (AttributeDefinition def : AttributeController.getAllAttributes()) {
                keys.add(def.getKey());
            }
            return CommandBase.getListOfStringsMatchingLastWord(args, keys.toArray(new String[keys.size()]));
        }
        // If the attribute is magic and we are completing the magicID parameter, you could return dummy numbers.
        return super.addTabCompletionOptions(sender, args);
    }
}
