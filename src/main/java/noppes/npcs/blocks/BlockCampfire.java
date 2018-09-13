package noppes.npcs.blocks;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import noppes.npcs.CustomItems;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.blocks.tiles.TileCampfire;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockCampfire extends BlockLightable{

	public BlockCampfire(boolean lit) {
        super(Blocks.cobblestone, lit);
        setBlockBounds(0, 0, 0, 1, 0.5f, 1);
        if(lit)
        	setLightLevel(0.9375F);
	}

	@Override
	public TileEntity createNewTileEntity(World var1, int var2) {
		return new TileCampfire();
	}
	
    @Override    
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9){
    	ItemStack item = player.inventory.getCurrentItem();
    	if(item == null)
    		return true;
        int meta = world.getBlockMetadata(x, y, z);
    	
    	if((item.getItem() == Items.flint || item.getItem() == Items.flint_and_steel) && unlitBlock() == this){
    		if(world.rand.nextInt(3) == 0 && !world.isRemote)
            	super.onBlockActivated(world, x, y, z, player, par6, par7, par8, par9);
    		CustomNpcs.proxy.spawnParticle("largesmoke", x + 0.5f, y + 0.5f, z + 0.5f, 0.0D, 0.0D, 0.0D, 2);
    		if(item.getItem() == Items.flint)
    			NoppesUtilServer.consumeItemStack(1, player);
    		else
    			item.damageItem(1, player);
    		return true;
    	}
    	
    	if(item.getItem() == Item.getItemFromBlock(Blocks.sand) && litBlock() == this){
        	super.onBlockActivated(world, x, y, z, player, par6, par7, par8, par9);
    	}
    	
    	return true;
    }

	@Override
    public int maxRotation(){
    	return 8;
    }

	@Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(World world, int x, int y, int z, Random random){
        int meta = world.getBlockMetadata(x, y, z);
        if(meta == 1)
        	return;
		
		if(random.nextInt(36) == 0){
    		world.playSound(x + 0.5F, y + 0.5F, z + 0.5F, "fire.fire", 1.0F + random.nextFloat(), 0.3F + random.nextFloat() * 0.7f , false);
		}
		
		TileCampfire tile = (TileCampfire) world.getTileEntity(x, y, z);
    	
    	float xOffset = 0.5f, yOffset = 0.7f, zOffset = 0.5f;

        double d0 = (double)((float)x + xOffset);
        double d1 = (double)((float)y + yOffset);
        double d2 = (double)((float)z + zOffset);
        
        GL11.glPushMatrix();
        
        CustomNpcs.proxy.spawnParticle("largesmoke", d0, d1, d2, 0.0D, 0.0D, 0.0D, 2);
        CustomNpcs.proxy.spawnParticle("flame", d0, d1, d2, 0.0D, 0.0D, 0.0D, 4);

        GL11.glPopMatrix();
    }

	@Override
	public Block unlitBlock() {
		return CustomItems.campfire_unlit;
	}

	@Override
	public Block litBlock() {
		return CustomItems.campfire;
	}
}
