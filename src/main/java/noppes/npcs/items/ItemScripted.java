package noppes.npcs.items;

import kamkeel.npcs.network.packets.data.ChatAlertPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import noppes.npcs.CustomItems;
import noppes.npcs.CustomNpcs;
import noppes.npcs.CustomNpcsPermissions;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.config.ConfigScript;
import noppes.npcs.constants.EnumGuiType;

public class ItemScripted extends ItemCustomizable {

    public ItemScripted() {
        maxStackSize = 1;
        setCreativeTab(CustomItems.tab);
        CustomNpcs.proxy.registerItem(this);
        setHasSubtypes(true);
    }

    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        if (!world.isRemote) {
            if (player.isSneaking() && player.capabilities.isCreativeMode) {
                if (!ConfigScript.canScript(player, CustomNpcsPermissions.TOOL_SCRIPTED_ITEM)) {
                    ChatAlertPacket.sendChatAlert((EntityPlayerMP) player, "availability.permission");
                } else {
                    NoppesUtilServer.sendOpenGui(player, EnumGuiType.ScriptItem, null, 0, 0, 0);
                }
            }
        }
        return super.onItemRightClick(stack, world, player);
    }
}
