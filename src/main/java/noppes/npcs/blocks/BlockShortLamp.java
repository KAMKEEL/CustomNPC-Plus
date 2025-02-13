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
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.blocks.tiles.TileColorable;
import noppes.npcs.blocks.tiles.TileShortLamp;

import java.util.List;

import static kamkeel.npcs.util.ColorUtil.colorTableInts;

public class BlockShortLamp extends BlockContainer {

    public int renderId = -1;

    public BlockShortLamp() {
        super(Material.wood);
        setLightLevel(1);
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player,
                                    int side, float hitX, float hitY, float hitZ) {
        ItemStack item = player.inventory.getCurrentItem();
        if(TileColorable.doNotAllowModification(item))
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
        tile.color = colorTableInts[BlockColored.func_150031_c(15 - stack.getItemDamage())];
        world.setBlockMetadataWithNotify(x, y, z, stack.getItemDamage(), 2);
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
        // Always one block tall.
        setBlockBounds(0, 0, 0, 1, 1, 1);
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
}
