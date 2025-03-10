package noppes.npcs.items;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.network.PacketClient;
import kamkeel.npcs.network.packets.request.item.ColorBrushPacket;
import kamkeel.npcs.network.packets.request.item.ColorSetPacket;
import kamkeel.npcs.network.packets.request.item.HammerPacket;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import noppes.npcs.CustomItems;
import noppes.npcs.CustomNpcs;
import noppes.npcs.CustomNpcsPermissions;
import noppes.npcs.blocks.BlockBanner;
import noppes.npcs.blocks.BlockChair;
import noppes.npcs.blocks.BlockTallLamp;
import noppes.npcs.blocks.tiles.TileChair;
import noppes.npcs.blocks.tiles.TileColorable;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.constants.EnumGuiType;

import java.util.List;

public class ItemNpcTool extends Item {

    // Define our tool types based on metadata:
    // meta 0: Hammer, meta 1: Paintbrush, meta 2: Wrench
    public static final String[] toolTypes = new String[] {"hammer", "paintbrush"};
    public static String BRUSH_COLOR_TAG = "BrushColor";

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

        if(isPaintbrush(stack) && player.isSneaking()){
            if(CustomNpcsPermissions.hasPermission(player, CustomNpcsPermissions.PAINTBRUSH_GUI)){
                NoppesUtil.requestOpenGUI(EnumGuiType.Paintbrush);
            }
        }
        return stack;
    }

    @Override
    public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
        if(!world.isRemote)
            return false;

        if(isPaintbrush(stack)) {
            Block block = world.getBlock(x, y, z);
            if(block instanceof BlockTallLamp || block instanceof BlockBanner){
                int meta = world.getBlockMetadata(x, y, z);
                if(meta >= 7)
                    y--;
            }

            TileEntity tile = world.getTileEntity(x, y, z);
            if(tile instanceof TileColorable) {
                PacketClient.sendClient(new ColorSetPacket(x, y, z));
                return true;
            }
        }  else if (isHammer(stack)){
            Block block = player.worldObj.getBlock(x, y, z);
            if(block instanceof BlockChair){
                TileEntity tile = player.worldObj.getTileEntity(x, y, z);
                if(tile instanceof TileChair) {
                    PacketClient.sendClient(new HammerPacket(x, y, z));
                    return true;
                }
            }
        }

        return false;
    }

    public boolean onBlockStartBreak(ItemStack itemstack, int x, int y, int z, EntityPlayer player)
    {
        if(!player.worldObj.isRemote)
            return true;

        if(isPaintbrush(itemstack)) {
            Block block = player.worldObj.getBlock(x, y, z);
            if(block instanceof BlockTallLamp || block instanceof BlockBanner){
                int meta = player.worldObj.getBlockMetadata(x, y, z);
                if(meta >= 7)
                    y--;
            }

            TileEntity tile = player.worldObj.getTileEntity(x, y, z);
            if(tile instanceof TileColorable) {
                int color = ((TileColorable) tile).color;
                PacketClient.sendClient(new ColorBrushPacket(color));
            }
        }

        return true;
    }

    public static boolean isPaintbrush(ItemStack itemStack){
        return itemStack.getItemDamage() == 1;
    }

    public static boolean isHammer(ItemStack itemStack){
        return itemStack.getItemDamage() == 0;
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
