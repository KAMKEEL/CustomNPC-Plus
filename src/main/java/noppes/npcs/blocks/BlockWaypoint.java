package noppes.npcs.blocks;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import noppes.npcs.CustomItems;
import noppes.npcs.CustomNpcs;
import noppes.npcs.CustomNpcsPermissions;
import noppes.npcs.Server;
import noppes.npcs.blocks.tiles.TileWaypoint;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketClient;

public class BlockWaypoint extends BlockContainer{

	public BlockWaypoint() {
        super(Material.iron);
        this.setCreativeTab(CustomItems.tab);
	}

    @Override    
    public boolean onBlockActivated(World par1World, int i, int j, int k, EntityPlayer player, int par6, float par7, float par8, float par9)
    {
    	if(par1World.isRemote)
    		return false;
		ItemStack currentItem = player.inventory.getCurrentItem();
		if (currentItem != null	&& currentItem.getItem() == CustomItems.wand && CustomNpcsPermissions.hasPermission(player, CustomNpcsPermissions.EDIT_BLOCKS)) {
			TileEntity tile = par1World.getTileEntity(i, j, k);
			NBTTagCompound compound = new NBTTagCompound();
			tile.writeToNBT(compound);
			Server.sendData((EntityPlayerMP)player, EnumPacketClient.GUI_WAYPOINT, compound);
        	return true;
		}
		return false;
    }
    
    @Override
    public void onBlockPlacedBy(World world, int i, int j, int k, EntityLivingBase entityliving, ItemStack item){
    	if(entityliving instanceof EntityPlayer && world.isRemote){
    		CustomNpcs.proxy.openGui(i, j, k, EnumGuiType.Waypoint, (EntityPlayer) entityliving);
    	}
    }
    
	@Override
	public TileEntity createNewTileEntity(World var1, int var2) {
		return new TileWaypoint();
	}

}
