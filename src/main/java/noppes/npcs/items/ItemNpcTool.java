package noppes.npcs.items;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import noppes.npcs.CustomItems;
import noppes.npcs.CustomNpcs;
import noppes.npcs.CustomNpcsPermissions;
import noppes.npcs.constants.EnumGuiType;

import java.util.List;

public class ItemNpcTool extends Item {

    // Define our tool types based on metadata:
    // meta 0: Hammer, meta 1: Paintbrush, meta 2: Wrench
    public static final String[] toolTypes = new String[] {"hammer", "paintbrush", "wrench"};

    @SideOnly(Side.CLIENT)
    private IIcon[] icons;

    public ItemNpcTool() {
        super();
        maxStackSize = 1;
        setHasSubtypes(true);
        setMaxDamage(0);
        setCreativeTab(CustomItems.tab);
        CustomNpcs.proxy.registerItem(this);
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        int meta = stack.getItemDamage();
        if(meta < 0 || meta >= toolTypes.length){
            meta = 0;
        }
        return super.getUnlocalizedName() + "." + toolTypes[meta];
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerIcons(IIconRegister iconRegister) {
        icons = new IIcon[toolTypes.length];
        for (int i = 0; i < toolTypes.length; i++) {
            icons[i] = iconRegister.registerIcon("noppes:npc_tool_" + toolTypes[i]);
        }
    }

    @Override
    public IIcon getIconFromDamage(int meta) {
        if(meta < 0 || meta >= icons.length){
            meta = 0;
        }
        return icons[meta];
    }

    @Override
    public void getSubItems(Item item, CreativeTabs tab, List subItems) {
        for (int i = 0; i < toolTypes.length; i++) {
            subItems.add(new ItemStack(item, 1, i));
        }
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        if(!world.isRemote)
            return stack;

        if(player.isSneaking() && isPaintbrush(stack)){

        } else if(isPaintbrush(stack)){

        }
        if(CustomNpcsPermissions.hasPermission(player, CustomNpcsPermissions.PAINTBRUSH_GUI)){
            CustomNpcs.proxy.openGui(0, 0, 0, EnumGuiType.Paintbrush, player);
        }
        else
            player.addChatMessage(new ChatComponentTranslation("item.npc_tool." + toolTypes[stack.getItemDamage()] + ".use"));
        return stack;
    }

    public boolean isPaintbrush(ItemStack itemStack){
        return itemStack.getItemDamage() == 1;
    }

    @Override
    public Item setUnlocalizedName(String name){
        GameRegistry.registerItem(this, name);
        return super.setUnlocalizedName(name);
    }
}
