package noppes.npcs.items;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.network.PacketClient;
import kamkeel.npcs.network.packets.request.item.ColorSetPacket;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import noppes.npcs.CustomItems;
import noppes.npcs.CustomNpcs;
import noppes.npcs.CustomNpcsPermissions;
import noppes.npcs.blocks.tiles.TileColorable;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.constants.EnumGuiType;

import java.util.List;

public class ItemNpcTool extends Item {

    // Define our tool types based on metadata:
    // meta 0: Hammer, meta 1: Paintbrush, meta 2: Wrench
    public static final String[] toolTypes = new String[] {"hammer", "paintbrush", "wrench"};
    public static String BRUSH_COLOR_TAG = "BrushColor";

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
        if(world.isRemote)
            return stack;

        if(isPaintbrush(stack) && !player.isSneaking()){
            if(CustomNpcsPermissions.hasPermission(player, CustomNpcsPermissions.PAINTBRUSH_GUI)){
                NoppesUtil.requestOpenGUI(EnumGuiType.Paintbrush);
            }
        }
        return stack;
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
        if(isPaintbrush(stack)) {
            TileEntity tile = world.getTileEntity(x, y, z);
            if(tile instanceof TileColorable) {
                if(player.isSneaking() && world.isRemote){
                    int color = ((TileColorable) tile).color;
                    PacketClient.sendClient(new ColorSetPacket(color));
                }
                else if (!world.isRemote && CustomNpcsPermissions.hasPermission(player, CustomNpcsPermissions.NPC_BUILD)){
                    int color = getColor(stack.getTagCompound());
                    TileColorable colorable = (TileColorable) tile;
                    colorable.setColor(color);
                }
                return true;
            }
        }

        return false;
    }


    public static boolean isPaintbrush(ItemStack itemStack){
        return itemStack.getItemDamage() == 1;
    }

    @Override
    public Item setUnlocalizedName(String name){
        GameRegistry.registerItem(this, name);
        return super.setUnlocalizedName(name);
    }

    public static int getColor(NBTTagCompound tagCompound){
        if(tagCompound == null || !tagCompound.hasKey(BRUSH_COLOR_TAG))
            return 0xFFFFFF;

        return tagCompound.getInteger(BRUSH_COLOR_TAG);
    }

    public static int setColor(NBTTagCompound tagCompound, int color){
        if(tagCompound == null || !tagCompound.hasKey(BRUSH_COLOR_TAG))
            return 0xFFFFFF;

        return tagCompound.getInteger(BRUSH_COLOR_TAG);
    }
}
