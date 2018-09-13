package noppes.npcs.blocks;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import noppes.npcs.CustomItems;
import noppes.npcs.CustomNpcs;
import noppes.npcs.CustomNpcsPermissions;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.Server;
import noppes.npcs.blocks.tiles.TileBigSign;
import noppes.npcs.blocks.tiles.TileColorable;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketClient;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockBigSign extends BlockContainer{
	
	public int renderId = -1;

	public BlockBigSign() {
        super(Material.wood);
	}

    @Override   
    public int damageDropped(int par1){
        return par1;
    }
    
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World p_149668_1_, int p_149668_2_, int p_149668_3_, int p_149668_4_){
    	return null;
    }
    @Override    
    public boolean onBlockActivated(World par1World, int i, int j, int k, EntityPlayer player, int par6, float par7, float par8, float par9){
    	if(par1World.isRemote)
    		return false;
		ItemStack currentItem = player.inventory.getCurrentItem();
		if (currentItem != null	&& currentItem.getItem() == CustomItems.wand && CustomNpcsPermissions.hasPermission(player, CustomNpcsPermissions.EDIT_BLOCKS)) {
			TileBigSign tile = (TileBigSign) par1World.getTileEntity(i, j, k);
			tile.canEdit = true;
			NoppesUtilServer.sendOpenGui(player, EnumGuiType.BigSign, null, i, j, k);
        	return true;
		}
		return false;
    }
    @Override   
    public void onBlockPlacedBy(World par1World, int par2, int par3, int par4, EntityLivingBase par5EntityLivingBase, ItemStack par6ItemStack){
    	int l = MathHelper.floor_double((double)(par5EntityLivingBase.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
        l %= 4;

        TileBigSign tile = (TileBigSign) par1World.getTileEntity(par2, par3, par4);
    	tile.rotation = l;
    	
        par1World.setBlockMetadataWithNotify(par2, par3, par4, par6ItemStack.getItemDamage() , 2);

    	if(par5EntityLivingBase instanceof EntityPlayer && par1World.isRemote){
    		CustomNpcs.proxy.openGui(par2, par3, par4, EnumGuiType.BigSign, (EntityPlayer) par5EntityLivingBase);
    	}
    }

    @Override 
    public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z){
    	TileEntity tileentity = world.getTileEntity(x, y, z);
    	if(!(tileentity instanceof TileColorable)){
    		super.setBlockBoundsBasedOnState(world, x, y, z);
    		return;
    	}
    	TileColorable tile = (TileColorable) tileentity;
        int meta = tile.getBlockMetadata();
    	float xStart = 0;
    	float zStart = 0;
    	float xEnd = 1;
    	float zEnd = 1;
    	if(tile.rotation == 0)
    		zStart = 0.87f;
    	else if(tile.rotation == 2)
    		zEnd = 0.13f;
    	else if(tile.rotation == 3)
    		xStart = 0.87f;
    	else if(tile.rotation == 1)
    		xEnd = 0.13f;
        setBlockBounds(xStart, 0, zStart, xEnd, 1, zEnd);
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
    public void registerBlockIcons(IIconRegister par1IconRegister)
    {
    	
    }
    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int p_149691_1_, int meta)
    {
        return Blocks.planks.getIcon(p_149691_1_, meta);
    }

	@Override
	public TileEntity createNewTileEntity(World var1, int var2) {
		return new TileBigSign();
	}
}

