package noppes.npcs.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEnchantmentTable;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockFurnace;
import net.minecraft.block.BlockGlowstone;
import net.minecraft.block.BlockSapling;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import noppes.npcs.CustomItems;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockBlood extends Block{
	
    @SideOnly(Side.CLIENT)
    private IIcon blockIcon2;
    @SideOnly(Side.CLIENT)
    private IIcon blockIcon3;
    
    private final int renderId = RenderingRegistry.getNextAvailableRenderId();
    
    public BlockBlood(){
        super(Material.rock);
		this.setBlockUnbreakable();
		setCreativeTab(CustomItems.tabMisc);	
		setBlockBounds(0.01f, 0.01f, 0.01f, 0.99f, 0.99f, 0.99f);
		this.setLightLevel(0.08f);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int metadata){
    	metadata += side;
    	if(metadata % 3 == 1)
    		return blockIcon2;
    	if(metadata % 3 == 2)
    		return blockIcon3;
        return this.blockIcon; 
    }
    
    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int i, int j, int k){
    	return null;
    }
    
    @Override
    public AxisAlignedBB getSelectedBoundingBoxFromPool(World par1World, int par2, int par3, int par4){
        return AxisAlignedBB.getBoundingBox(par2, par3, par4 , par2, par3, par4 );
    }
    
    @Override
    public boolean renderAsNormalBlock(){
        return false;
    }    
    
    @SideOnly(Side.CLIENT)
    @Override
    public void registerBlockIcons(IIconRegister par1IconRegister){
        this.blockIcon = par1IconRegister.registerIcon(this.getTextureName());
        this.blockIcon2 = par1IconRegister.registerIcon(getTextureName() + "2");
        this.blockIcon3 = par1IconRegister.registerIcon(getTextureName() + "3");
    }

    @Override
    public boolean shouldSideBeRendered(IBlockAccess world, int par2, int par3, int par4, int par5){
        Block block = world.getBlock(par2, par3, par4);
        return block != Blocks.air && block.renderAsNormalBlock(); //only render the side if a block is adjacent
    }
    
    @Override
    public boolean isOpaqueCube(){
        return false;
    }

    @Override   
    public int getRenderBlockPass(){
        return 1;
    }
    
    @Override
    public void onBlockPlacedBy(World par1World, int par2, int par3, int par4, EntityLivingBase par5EntityLiving, ItemStack item){
        int var6 = MathHelper.floor_double((double)(par5EntityLiving.rotationYaw / 90.0F) + 0.5D) & 3;
        par1World.setBlockMetadataWithNotify(par2, par3, par4, var6, 2); //add metadata based on player rotation
    }
    
    public int getRenderType(){
		return renderId;
    }
}
