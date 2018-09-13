package noppes.npcs.blocks;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import noppes.npcs.Server;
import noppes.npcs.blocks.tiles.TileMailbox;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketClient;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockMailbox extends BlockContainer{
	
	public int renderId = -1;

	public BlockMailbox() {
        super(Material.iron);
	}

    @Override   
    public void getSubBlocks(Item par1, CreativeTabs par2CreativeTabs, List par3List)
    {
        par3List.add(new ItemStack(par1, 1, 0));
        par3List.add(new ItemStack(par1, 1, 1));
    }
    
    @Override    
    public boolean onBlockActivated(World par1World, int i, int j, int k, EntityPlayer player, int par6, float par7, float par8, float par9){
    	if(!par1World.isRemote){
    		Server.sendData((EntityPlayerMP)player, EnumPacketClient.GUI, EnumGuiType.PlayerMailbox, i, j, k);
    	}
		return true;
    }

    @Override   
    public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune)
    {
        ArrayList<ItemStack> ret = new ArrayList<ItemStack>();
        int damage = this.getDamageValue(world, x, y, z);
        ret.add(new ItemStack(this, 1, damage));
        return ret;
    }

    @Override   
    public int damageDropped(int par1)
    {
        return par1 >> 2;
    }

    @Override   
    public void onBlockPlacedBy(World par1World, int par2, int par3, int par4, EntityLivingBase par5EntityLivingBase, ItemStack par6ItemStack)
    {
        int l = MathHelper.floor_double((double)(par5EntityLivingBase.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
        l %= 4;
        
        par1World.setBlockMetadataWithNotify(par2, par3, par4, l | par6ItemStack.getItemDamage() << 2, 2);
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
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int p_149691_1_, int p_149691_2_)
    {
        return Blocks.soul_sand.getBlockTextureFromSide(p_149691_1_);
    }

	@Override
	public TileEntity createNewTileEntity(World var1, int var2) {
		return new TileMailbox();
	}
}
