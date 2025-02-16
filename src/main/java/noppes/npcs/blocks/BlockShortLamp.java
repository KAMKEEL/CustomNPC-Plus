package noppes.npcs.blocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.BlockColored;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.blocks.tiles.TileColorable;
import noppes.npcs.blocks.tiles.TileShortLamp;
import noppes.npcs.blocks.tiles.TileTallLamp;

import java.util.List;

import static kamkeel.npcs.util.ColorUtil.colorTableInts;
import static noppes.npcs.items.ItemNpcTool.BRUSH_COLOR_TAG;

public class BlockShortLamp extends BlockContainer {

    public int renderId = -1;

    public BlockShortLamp() {
        super(Material.wood);
        setLightLevel(1);
        setBlockBounds(0.25f, 0, 0.25f, 0.75f, 1, 0.75f);
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player,
                                    int side, float hitX, float hitY, float hitZ) {
        ItemStack item = player.inventory.getCurrentItem();
        if(TileColorable.allowColorChange(item) != TileColorable.ColorChangeType.DYE)
            return false;
        int meta = world.getBlockMetadata(x, y, z);
        TileColorable tile = (TileColorable) world.getTileEntity(x, y, z);
        int color = colorTableInts[BlockColored.func_150031_c(item.getItemDamage())];
        if (tile.color != color) {
            NoppesUtilServer.consumeItemStack(1, player);
            tile.color = color;
            world.markBlockForUpdate(x, y, z);
        }
        return true;
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
        setBlockBoundsBasedOnState(world, x, y, z);
        return super.getCollisionBoundingBoxFromPool(world, x, y, z);
    }

    @Override
    public void getSubBlocks(Item item, CreativeTabs tab, List list) {
        list.add(new ItemStack(item, 1, 0));
        list.add(new ItemStack(item, 1, 1));
        list.add(new ItemStack(item, 1, 2));
        list.add(new ItemStack(item, 1, 3));
        list.add(new ItemStack(item, 1, 4));
    }

    @Override
    public int damageDropped(int meta) {
        return meta;
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z,
                                  EntityLivingBase entity, ItemStack stack) {
        int l = MathHelper.floor_double((double)(entity.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
        l %= 4;
        TileColorable tile = (TileColorable) world.getTileEntity(x, y, z);
        tile.rotation = l;
        tile.color = colorTableInts[15 - stack.getItemDamage()];
        if(stack.hasTagCompound() && stack.getTagCompound().hasKey(BRUSH_COLOR_TAG)){
            tile.color = stack.getTagCompound().getInteger(BRUSH_COLOR_TAG);
        }

        world.setBlockMetadataWithNotify(x, y, z, stack.getItemDamage(), 2);
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
        setBlockBounds(0.25f, 0, 0.25f, 0.75f, 1, 0.75f);
    }

    @Override
    public boolean isOpaqueCube(){
        return false;
    }

    @Override
    public boolean renderAsNormalBlock(){
        return false;
    }

    @Override
    public int getRenderType(){
        return renderId;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister reg) {
        // Optional icon registration
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        if(meta == 1)
            return Blocks.stone.getIcon(side, 0);
        else if(meta == 2)
            return Blocks.iron_block.getIcon(side, 0);
        else if(meta == 3)
            return Blocks.gold_block.getIcon(side, 0);
        else if(meta == 4)
            return Blocks.diamond_block.getIcon(side, 0);
        return Blocks.planks.getIcon(side, 0);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileShortLamp();
    }

    @Override
    public void onBlockHarvested(World world, int x, int y, int z, int meta, EntityPlayer player) {
        super.onBlockHarvested(world, x, y, z, meta, player);
    }

    @Override
    public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z) {
        TileEntity tileentity = world.getTileEntity(x, y, z);
        ItemStack stack = new ItemStack(this, 1, world.getBlockMetadata(x, y, z));
        if (tileentity instanceof TileShortLamp) {
            NBTTagCompound compound = new NBTTagCompound();
            tileentity.writeToNBT(compound);

            NBTTagCompound brushCompound = new NBTTagCompound();
            brushCompound.setInteger(BRUSH_COLOR_TAG, compound.getInteger(BRUSH_COLOR_TAG));
            stack.setTagCompound(brushCompound);
        }
        return stack;
    }
}
