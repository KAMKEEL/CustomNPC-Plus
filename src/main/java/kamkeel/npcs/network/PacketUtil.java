package kamkeel.npcs.network;

import kamkeel.npcs.network.enums.EnumItemPacketType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import noppes.npcs.CustomItems;
import noppes.npcs.LogWriter;

public class PacketUtil {

    public static boolean verifyItemPacket(EnumItemPacketType type, EntityPlayer player){
        if(player == null)
            return false;

        ItemStack item = player.inventory.getCurrentItem();
        if(item == null){
            LogWriter.error(String.format("%s attempted to utilize a %s Packet without an item, they could be a hacker", player.getCommandSenderName(), type.toString()));
            return false;
        }

        switch (type){
            case WAND:
                if(item.getItem() != CustomItems.wand){
                    LogWriter.error(String.format("%s attempted to utilize a %s Packet without a Wand, they could be a hacker", player.getCommandSenderName(), type.toString()));
                    return false;
                }
                break;
            case MOUNTER:
                if(item.getItem() != CustomItems.mount){
                    LogWriter.error(String.format("%s attempted to utilize a %s Packet without a Mounter, they could be a hacker", player.getCommandSenderName(), type.toString()));
                    return false;
                }
                break;
            case CLONER:
                if(item.getItem() != CustomItems.cloner){
                    LogWriter.error(String.format("%s attempted to utilize a %s Packet without a Cloner, they could be a hacker", player.getCommandSenderName(), type.toString()));
                    return false;
                }
                break;
            case TELEPORTER:
                if(item.getItem() != CustomItems.teleporter){
                    LogWriter.error(String.format("%s attempted to utilize a %s Packet without a Teleporter, they could be a hacker", player.getCommandSenderName(), type.toString()));
                    return false;
                }
                break;
            case SCRIPTER:
                if(item.getItem() != CustomItems.scripter){
                    LogWriter.error(String.format("%s attempted to utilize a %s Packet without a Scripter, they could be a hacker", player.getCommandSenderName(), type.toString()));
                    return false;
                }
                break;
            case PATHER:
                if(item.getItem() != CustomItems.moving){
                    LogWriter.error(String.format("%s attempted to utilize a %s Packet without a Pather, they could be a hacker", player.getCommandSenderName(), type.toString()));
                    return false;
                }
                break;
            case BLOCK:
                if(item.getItem() == Item.getItemFromBlock(CustomItems.waypoint) || item.getItem() == Item.getItemFromBlock(CustomItems.border) || item.getItem() == Item.getItemFromBlock(CustomItems.redstoneBlock)){
                    LogWriter.error(String.format("%s attempted to utilize a %s Packet without a valid Block, they could be a hacker", player.getCommandSenderName(), type.toString()));
                    return false;
                }
                break;
        }

        return true;
    }
}
