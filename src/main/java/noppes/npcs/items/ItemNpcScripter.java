package noppes.npcs.items;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.World;
import noppes.npcs.CustomItems;
import noppes.npcs.CustomNpcs;
import noppes.npcs.CustomNpcsPermissions;
import noppes.npcs.config.ConfigScript;
import noppes.npcs.constants.EnumGuiType;

public class ItemNpcScripter extends Item {

    public ItemNpcScripter() {
        maxStackSize = 1;
        setCreativeTab(CustomItems.tab);
    }

    @Override
    public int getColorFromItemStack(ItemStack par1ItemStack, int par2) {
        return 0x8B4513;
    }

    @Override
    public boolean requiresMultipleRenderPasses() {
        return true;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player) {
        if (!world.isRemote) {
            if (!ConfigScript.canScript(player, CustomNpcsPermissions.TOOL_SCRIPTER)) {
                player.addChatMessage(new ChatComponentTranslation("availability.permission"));
            }
            return itemStack;
        }
        CustomNpcs.proxy.openGui(0, 0, 0, EnumGuiType.ScriptEvent, player);
        return itemStack;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerIcons(IIconRegister par1IconRegister) {
        this.itemIcon = Items.iron_shovel.getIconFromDamage(0);
    }

    @Override
    public Item setUnlocalizedName(String name) {
        GameRegistry.registerItem(this, name);
        return super.setUnlocalizedName(name);
    }
}
