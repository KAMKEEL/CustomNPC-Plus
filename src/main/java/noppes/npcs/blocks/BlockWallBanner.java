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
import net.minecraft.util.*;
import net.minecraft.world.World;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.blocks.tiles.TileColorable;
import noppes.npcs.blocks.tiles.TileWallBanner;
import noppes.npcs.items.ItemNpcTool;

import java.util.List;

import static kamkeel.npcs.util.ColorUtil.colorTableInts;
import static noppes.npcs.items.ItemNpcTool.BRUSH_COLOR_TAG;

public class BlockWallBanner extends BlockContainer {

    public int renderId = -1;

    public BlockWallBanner() {
        super(Material.rock);
    }

    @Override
    public boolean onBlockActivated(World par1World, int i, int j, int k, EntityPlayer player, int par6, float par7, float par8, float par9) {
        ItemStack item = player.inventory.getCurrentItem();
        if (item == null)
            return false;

        if (item.getItem() != null && item.getItem() instanceof ItemNpcTool && item.getItemDamage() == 0)
            return false;

        TileWallBanner tile = (TileWallBanner) par1World.getTileEntity(i, j, k);
        if (tile.canEdit())
            return true;
        else if (TileColorable.allowColorChange(item) != TileColorable.ColorChangeType.DYE)
            return false;

        int color = colorTableInts[BlockColored.func_150031_c(item.getItemDamage())];
        if (tile.color != color) {
            NoppesUtilServer.consumeItemStack(1, player);
            tile.color = color;
            par1World.markBlockForUpdate(i, j, k);
        }
        return true;
    }

    @Override
    public void onBlockPlacedBy(World par1World, int par2, int par3, int par4, EntityLivingBase par5EntityLivingBase, ItemStack par6ItemStack) {
        int l = MathHelper.floor_double((double) (par5EntityLivingBase.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
        l %= 4;
        TileWallBanner tile = (TileWallBanner) par1World.getTileEntity(par2, par3, par4);
        tile.rotation = l;
        tile.color = colorTableInts[15 - par6ItemStack.getItemDamage()];
        if (par6ItemStack.hasTagCompound() && par6ItemStack.getTagCompound().hasKey(BRUSH_COLOR_TAG)) {
            tile.color = par6ItemStack.getTagCompound().getInteger(BRUSH_COLOR_TAG);
        }

        tile.time = System.currentTimeMillis();

        par1World.setBlockMetadataWithNotify(par2, par3, par4, par6ItemStack.getItemDamage(), 2);

        if (par5EntityLivingBase instanceof EntityPlayer && par1World.isRemote)
            ((EntityPlayer) par5EntityLivingBase).addChatComponentMessage(new ChatComponentTranslation("availability.editIcon"));
    }

    public AxisAlignedBB getCollisionBoundingBoxFromPool(World p_149668_1_, int p_149668_2_, int p_149668_3_, int p_149668_4_) {
        return null;
    }

    @Override
    public void getSubBlocks(Item par1, CreativeTabs par2CreativeTabs, List par3List) {
        par3List.add(new ItemStack(par1, 1, 0));
        par3List.add(new ItemStack(par1, 1, 1));
        par3List.add(new ItemStack(par1, 1, 2));
        par3List.add(new ItemStack(par1, 1, 3));
        par3List.add(new ItemStack(par1, 1, 4));
    }


    @Override
    public int damageDropped(int par1) {
        return par1;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    public int getRenderType() {
        return renderId;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister par1IconRegister) {

    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int p_149691_1_, int meta) {
        meta %= 7;
        if (meta == 1)
            return Blocks.stone.getIcon(p_149691_1_, 0);
        else if (meta == 2)
            return Blocks.iron_block.getIcon(p_149691_1_, 0);
        else if (meta == 3)
            return Blocks.gold_block.getIcon(p_149691_1_, 0);
        else if (meta == 4)
            return Blocks.diamond_block.getIcon(p_149691_1_, 0);
        return Blocks.planks.getIcon(p_149691_1_, 0);
    }

    @Override
    public TileEntity createNewTileEntity(World var1, int var2) {
        return new TileWallBanner();
    }

    @Override
    public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z) {
        TileEntity tileentity = world.getTileEntity(x, y, z);
        ItemStack stack = new ItemStack(this, 1, world.getBlockMetadata(x, y, z));
        if (tileentity instanceof TileWallBanner) {
            NBTTagCompound compound = new NBTTagCompound();
            tileentity.writeToNBT(compound);

            NBTTagCompound brushCompound = new NBTTagCompound();
            brushCompound.setInteger(BRUSH_COLOR_TAG, compound.getInteger(BRUSH_COLOR_TAG));
            stack.setTagCompound(brushCompound);
        }
        return stack;
    }
}
