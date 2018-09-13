package noppes.npcs.blocks;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import noppes.npcs.CustomItems;
import noppes.npcs.blocks.tiles.TileCandle;
import noppes.npcs.blocks.tiles.TileColorable;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockCandle extends BlockLightable{
	
	public BlockCandle(boolean lit) {
        super(Blocks.planks, lit);
        setBlockBounds(0.3f, 0, 0.3f, 0.7f, 0.5f, 0.7f);
	}    
	
    @Override
    public int maxRotation(){
    	return 8;
    }
    
	@Override 
    public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z){
    	TileEntity tileentity = world.getTileEntity(x, y, z);
    	if(!(tileentity instanceof TileColorable)){
    		super.setBlockBoundsBasedOnState(world, x, y, z);
    		return;
    	}
    	TileColorable tile = (TileColorable) tileentity;
    	if(tile.color == 2){
    		float xOffset = 0;
    		float yOffset = 0;
    		if(tile.rotation == 0)
    			yOffset = 0.2f;
    		else if(tile.rotation == 4)
    			yOffset = -0.2f;
    		else if(tile.rotation == 6)
    			xOffset = 0.2f;
    		else if(tile.rotation == 2)
    			xOffset = -0.2f;
    		
            setBlockBounds(0.2f + xOffset, 0.4f, 0.2f + yOffset, 0.8f + xOffset, 0.9f, 0.8f + yOffset);
    	}
    	else if(tile.color == 1)
            setBlockBounds(0.1f, 0.1f, 0.1f, 0.9f, 0.8f, 0.9f);
    	else
            setBlockBounds(0.3f, 0, 0.3f, 0.7f, 0.5f, 0.7f);
    	
    }


    @Override  
    public int onBlockPlaced(World world, int x, int y, int z, int side, float p_149660_6_, float p_149660_7_, float p_149660_8_, int meta){
        return side;
    }
    
    @Override  
    public void onPostBlockPlaced(World world, int x, int y, int z, int meta) {
    	TileCandle tile = (TileCandle) world.getTileEntity(x, y, z);
    	if(meta == 1)
    		tile.color = 0;
    	else if(meta == 0)
    		tile.color = 1;
    	else{
    		tile.color = 2;
    		if(meta == 2)
    			tile.rotation = 0;
    		else if(meta == 3)
    			tile.rotation = 4;
    		else if(meta == 4)
    			tile.rotation = 6;
    		else if(meta == 5)
    			tile.rotation = 2;
    	}
		world.setBlockMetadataWithNotify(x, y, z, 0, 4);
    }
    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int p_149691_1_, int meta){
        return Blocks.soul_sand.getIcon(p_149691_1_, meta);
    }

	@Override
	public TileEntity createNewTileEntity(World var1, int var2) {
		return new TileCandle();
	}

    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(World world, int x, int y, int z, Random p_149734_5_){
    	if(this == unlitBlock())
    		return;
    	TileCandle tile = (TileCandle) world.getTileEntity(x, y, z);
    	
    	if(tile.color == 1){
    		if(tile.rotation % 2 == 0){
		        world.spawnParticle("smoke", x + 0.5f, y + 0.66f, z + 0.13f, 0.0D, 0.0D, 0.0D);
		        world.spawnParticle("flame", x + 0.5f, y + 0.65f, z + 0.13f, 0.0D, 0.0D, 0.0D);
	
		        world.spawnParticle("smoke", x + 0.5f, y + 0.66f, z + 0.87f, 0.0D, 0.0D, 0.0D);
		        world.spawnParticle("flame", x + 0.5f, y + 0.65f, z + 0.87f, 0.0D, 0.0D, 0.0D);
	
		        world.spawnParticle("smoke", x + 0.13f, y + 0.66f, z + 0.5f, 0.0D, 0.0D, 0.0D);
		        world.spawnParticle("flame", x + 0.13f, y + 0.65f, z + 0.5f, 0.0D, 0.0D, 0.0D);
	
		        world.spawnParticle("smoke", x + 0.87f, y + 0.66f, z + 0.5f, 0.0D, 0.0D, 0.0D);
		        world.spawnParticle("flame", x + 0.87f, y + 0.65f, z + 0.5f, 0.0D, 0.0D, 0.0D);
    		}
    		else{
		        world.spawnParticle("smoke", x + 0.24f, y + 0.66f, z + 0.24f, 0.0D, 0.0D, 0.0D);
		        world.spawnParticle("flame", x + 0.24f, y + 0.65f, z + 0.24f, 0.0D, 0.0D, 0.0D);

		        world.spawnParticle("smoke", x + 0.76f, y + 0.66f, z + 0.76f, 0.0D, 0.0D, 0.0D);
		        world.spawnParticle("flame", x + 0.76f, y + 0.65f, z + 0.76f, 0.0D, 0.0D, 0.0D);

		        world.spawnParticle("smoke", x + 0.24f, y + 0.66f, z + 0.76f, 0.0D, 0.0D, 0.0D);
		        world.spawnParticle("flame", x + 0.24f, y + 0.65f, z + 0.76f, 0.0D, 0.0D, 0.0D);

		        world.spawnParticle("smoke", x + 0.76f, y + 0.66f, z + 0.24f, 0.0D, 0.0D, 0.0D);
		        world.spawnParticle("flame", x + 0.76f, y + 0.65f, z + 0.24f, 0.0D, 0.0D, 0.0D);
    		}
    		
    	}
    	else{
	    	float xOffset = 0.5f, yOffset = 0.45f, zOffset = 0.5f;
	    	
	    	if(tile.color == 2){
	    		yOffset = 1.05f;
	    		if(tile.rotation == 0)
	    			zOffset += 0.12f;
	    		if(tile.rotation == 4)
	    			zOffset -= 0.12f;
	    		if(tile.rotation == 6)
	    			xOffset += 0.12f;
	    		if(tile.rotation == 2)
	    			xOffset -= 0.12f;
	    	}
	        double d0 = (double)((float)x + xOffset);
	        double d1 = (double)((float)y + yOffset);
	        double d2 = (double)((float)z + zOffset);
	
	        world.spawnParticle("smoke", d0, d1, d2, 0.0D, 0.0D, 0.0D);
	        world.spawnParticle("flame", d0, d1, d2, 0.0D, 0.0D, 0.0D);
    	}
    }

	@Override
	public Block unlitBlock() {
		return CustomItems.candle_unlit;
	}

	@Override
	public Block litBlock() {
		return CustomItems.candle;
	}
}
