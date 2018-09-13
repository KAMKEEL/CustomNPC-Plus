package noppes.npcs.blocks;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import noppes.npcs.CustomItems;
import noppes.npcs.CustomNpcs;
import noppes.npcs.CustomNpcsPermissions;
import noppes.npcs.Server;
import noppes.npcs.blocks.tiles.TileRedstoneBlock;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketClient;

public class BlockNpcRedstone extends BlockContainer{
    public BlockNpcRedstone(){
        super(Material.rock);
    }

    @Override    
    public boolean onBlockActivated(World par1World, int i, int j, int k, EntityPlayer player, int par6, float par7, float par8, float par9){
    	if(par1World.isRemote)
    		return false;
		ItemStack currentItem = player.inventory.getCurrentItem();
		if (currentItem != null	&& currentItem.getItem() == CustomItems.wand && CustomNpcsPermissions.hasPermission(player, CustomNpcsPermissions.EDIT_BLOCKS)) {
			TileEntity tile = par1World.getTileEntity(i, j, k);
			NBTTagCompound compound = new NBTTagCompound();
			tile.writeToNBT(compound);
			Server.sendData((EntityPlayerMP)player, EnumPacketClient.GUI_REDSTONE, compound);
        	return true;
		}
		return false;
    }
    
    @Override
    public void onBlockAdded(World par1World, int par2, int par3, int par4){
        par1World.notifyBlocksOfNeighborChange(par2, par3, par4, this);
        par1World.notifyBlocksOfNeighborChange(par2, par3 - 1, par4, this);
        par1World.notifyBlocksOfNeighborChange(par2, par3 + 1, par4, this);
        par1World.notifyBlocksOfNeighborChange(par2 - 1, par3, par4, this);
        par1World.notifyBlocksOfNeighborChange(par2 + 1, par3, par4, this);
        par1World.notifyBlocksOfNeighborChange(par2, par3, par4 - 1, this);
        par1World.notifyBlocksOfNeighborChange(par2, par3, par4 + 1, this);
    }

    @Override
    public void onBlockPlacedBy(World world, int i, int j, int k, EntityLivingBase entityliving, ItemStack item){
    	if(entityliving instanceof EntityPlayer && world.isRemote){
    		CustomNpcs.proxy.openGui(i, j, k, EnumGuiType.RedstoneBlock, (EntityPlayer) entityliving);
    	}
    }

    @Override
    public void onBlockDestroyedByPlayer(World par1World, int par2, int par3, int par4, int par5) {
    	onBlockAdded(par1World, par2, par3, par4);
    }

    @Override
    public int colorMultiplier(IBlockAccess par1IBlockAccess, int par2, int par3, int par4){
    	if(isActivated(par1IBlockAccess, par2, par3, par4) > 0)
    		return 0xFF6B68;
    	else
    		return super.colorMultiplier(par1IBlockAccess, par2, par3, par4); 
    }
    
    @Override
    public int isProvidingWeakPower(IBlockAccess par1IBlockAccess, int par2, int par3, int par4, int par5){
        return isActivated(par1IBlockAccess, par2, par3, par4);
    }
    
    @Override
    public int isProvidingStrongPower(IBlockAccess par1World, int par2, int par3, int par4, int par5){
        return isActivated(par1World, par2, par3, par4);
    }
    
    @Override
    public boolean canProvidePower(){
        return true;
    }
    
	public int isActivated(IBlockAccess par1IBlockAccess, int par2, int par3, int par4){
		return par1IBlockAccess.getBlockMetadata(par2, par3, par4) == 1?15 : 0;
	}

	@Override
	public TileEntity createNewTileEntity(World var1, int var2) {
		return new TileRedstoneBlock();
	}
}
