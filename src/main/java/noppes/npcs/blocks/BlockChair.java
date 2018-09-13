package noppes.npcs.blocks;

import java.util.List;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import noppes.npcs.blocks.tiles.TileChair;
import noppes.npcs.blocks.tiles.TileColorable;
import noppes.npcs.entity.EntityChairMount;
import noppes.npcs.entity.EntityCustomNpc;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockChair extends BlockRotated{
	
	public BlockChair() {
        super(Blocks.planks);
        setBlockBounds(0.1f, 0, 0.1f, 0.9f, 1, 0.9f);
	}

    @Override   
    public void onBlockPlacedBy(World par1World, int par2, int par3, int par4, EntityLivingBase par5EntityLivingBase, ItemStack par6ItemStack){
    	super.onBlockPlacedBy(par1World, par2, par3, par4, par5EntityLivingBase, par6ItemStack);
        par1World.setBlockMetadataWithNotify(par2, par3, par4, par6ItemStack.getItemDamage() , 2);
    }

    @Override   
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World p_149668_1_, int x, int y, int z){
        return AxisAlignedBB.getBoundingBox(x + 0.1f, y, z + 0.1f, x + 0.9f, y + 0.5f, z + 0.9f);
    }
    
    @Override   
    public void getSubBlocks(Item par1, CreativeTabs par2CreativeTabs, List par3List){
        par3List.add(new ItemStack(par1, 1, 0));
        par3List.add(new ItemStack(par1, 1, 1));
        par3List.add(new ItemStack(par1, 1, 2));
        par3List.add(new ItemStack(par1, 1, 3));
        par3List.add(new ItemStack(par1, 1, 4));
        par3List.add(new ItemStack(par1, 1, 5));
    }

    @Override   
    public int damageDropped(int par1){
        return par1;
    }

	@Override
	public TileEntity createNewTileEntity(World var1, int var2) {
		return new TileChair();
	}
	
	@Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int p_149727_6_, float p_149727_7_, float p_149727_8_, float p_149727_9_){
        return MountBlock(world, x, y, z, player);
    }
	
	public static boolean MountBlock(World world, int x, int y, int z, EntityPlayer player){
		if(world.isRemote)
			return true;
		List<Entity> list = world.getEntitiesWithinAABB(Entity.class, AxisAlignedBB.getBoundingBox(x, y, z, x + 1, y + 1, z + 1));
		for(Entity entity : list){
			if(entity instanceof EntityChairMount || entity instanceof EntityCustomNpc)
				return false;
		}
		EntityChairMount mount = new EntityChairMount(world);
		mount.setPosition(x + 0.5f, y, z + 0.5);
		player.mountEntity(mount);
		world.spawnEntityInWorld(mount);
		player.mountEntity(mount);
		return true;
	}
}
