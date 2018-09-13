package noppes.npcs.blocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
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
import net.minecraft.world.World;
import noppes.npcs.CustomItems;
import noppes.npcs.CustomNpcs;
import noppes.npcs.Server;
import noppes.npcs.blocks.tiles.TileBorder;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;

public class BlockBorder extends BlockContainer{
	public int renderId = -1;

	public BlockBorder() {
		super(Material.rock);
		setBlockUnbreakable();
	}

    @Override  
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta)
    {
    	if(side == 1){
    		return this.blockIcon;
    	}
    	return Blocks.iron_block.getIcon(side, meta);
    }
    @Override    
    public boolean onBlockActivated(World par1World, int i, int j, int k, EntityPlayer player, int par6, float par7, float par8, float par9)
    {
		ItemStack currentItem = player.inventory.getCurrentItem();
		if (currentItem != null	&& currentItem.getItem() == CustomItems.wand) {
    		CustomNpcs.proxy.openGui(i, j, k, EnumGuiType.Border, player);
        	return true;
		}
		return false;
    }
    
    @Override   
    public void onBlockPlacedBy(World par1World, int x, int y, int z, EntityLivingBase par5EntityLivingBase, ItemStack par6ItemStack){
        int l = MathHelper.floor_double((double)(par5EntityLivingBase.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
        l %= 4;

        TileBorder tile = (TileBorder) par1World.getTileEntity(x, y, z);

    	TileBorder adjacent = getTile(par1World, x - 1, y, z);
    	if(adjacent == null)
    		adjacent = getTile(par1World, x, y, z - 1);
    	if(adjacent == null)
    		adjacent = getTile(par1World, x, y, z + 1);
    	if(adjacent == null)
    		adjacent = getTile(par1World, x + 1, y, z);
    	
    	if(adjacent != null){
    		NBTTagCompound compound = new NBTTagCompound();
    		adjacent.writeExtraNBT(compound);
    		tile.readExtraNBT(compound);
    	}
        
    	tile.rotation = l;    	
    	
    	if(par5EntityLivingBase instanceof EntityPlayer && par1World.isRemote){
    		CustomNpcs.proxy.openGui(x, y, z, EnumGuiType.Border, (EntityPlayer) par5EntityLivingBase);
    	}
    }
    
    private TileBorder getTile(World world, int x, int y, int z){
    	TileEntity tile = world.getTileEntity(x, y, z);
    	if(tile != null && tile instanceof TileBorder)
    		return (TileBorder) tile;
    	return null;
    }
    
    @Override   
	public int getRenderType(){
		return renderId; 	
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
	public TileEntity createNewTileEntity(World var1, int var2) {
		return new TileBorder();
	}
}
