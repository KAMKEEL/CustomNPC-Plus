package noppes.npcs.items;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.World;
import noppes.npcs.CustomItems;
import noppes.npcs.CustomNpcs;
import noppes.npcs.CustomNpcsPermissions;
import noppes.npcs.config.ConfigScript;
import noppes.npcs.constants.EnumGuiType;

public class ItemScripted extends ItemCustomizable {

    public ItemScripted() {
        maxStackSize = 1;
        setCreativeTab(CustomItems.tab);
        CustomNpcs.proxy.registerItem(this);
        setHasSubtypes(true);
    }

    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player)
    {
        if(player.isSneaking() && player.capabilities.isCreativeMode) {
            if(!ConfigScript.canScript(player, CustomNpcsPermissions.TOOL_SCRIPTED_ITEM)){
                player.addChatMessage(new ChatComponentTranslation("availability.permission"));
            } else {
                CustomNpcs.proxy.openGui(0, 0, 0, EnumGuiType.ScriptItem, player);
            }
        }
        return super.onItemRightClick(stack, world, player);
    }
}
