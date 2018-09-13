package noppes.npcs.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import noppes.npcs.blocks.tiles.TileColorable;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public abstract class BlockRotated extends BlockContainer{
	private Block block;
	protected BlockRotated(Block block) {
		super(block.getMaterial());
		this.block = block;
	}

	public int renderId = -1;

    @Override   
    public void onBlockPlacedBy(World par1World, int par2, int par3, int par4, EntityLivingBase par5EntityLivingBase, ItemStack par6ItemStack){
        int l = MathHelper.floor_double((double)(par5EntityLivingBase.rotationYaw * maxRotation() / 360.0F) + 0.5D) & (maxRotation() - 1);
        l %= maxRotation();

    	TileColorable tile = (TileColorable) par1World.getTileEntity(par2, par3, par4);
    	tile.rotation = l;    	
    }

    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z){
    	setBlockBoundsBasedOnState(world, x, y, z);
    	return super.getCollisionBoundingBoxFromPool(world, x, y, z);
    }
    
    public int maxRotation(){
    	return 4;
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
    public boolean isSideSolid(IBlockAccess world, int x, int y, int z, ForgeDirection side){
    	return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister par1IconRegister)
    {
    	
    }
    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int p_149691_1_, int meta){
        return block.getIcon(p_149691_1_, meta);
    }

}
