package noppes.npcs.blocks;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import noppes.npcs.CustomItems;
import noppes.npcs.blocks.tiles.TileColorable;
import noppes.npcs.blocks.tiles.TileLamp;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockLamp extends BlockLightable{

	public BlockLamp(boolean lit) {
        super(Blocks.planks, lit);
        setBlockBounds(0.3f, 0, 0.3f, 0.7f, 0.6f, 0.7f);
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
    		
            setBlockBounds(0.3f + xOffset, 0.2f, 0.3f + yOffset, 0.7f + xOffset, 0.7f, 0.7f + yOffset);
    	}
    	else
            setBlockBounds(0.3f, 0, 0.3f, 0.7f, 0.6f, 0.7f);
    	
    }
    
    @Override  
    public int onBlockPlaced(World world, int x, int y, int z, int side, float p_149660_6_, float p_149660_7_, float p_149660_8_, int meta){
        return side;
    }

    @Override  
    public void onPostBlockPlaced(World world, int x, int y, int z, int meta) {
    	TileLamp tile = (TileLamp) world.getTileEntity(x, y, z);
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
		return new TileLamp();
	}

	@Override
	public Block unlitBlock() {
		return CustomItems.lamp_unlit;
	}

	@Override
	public Block litBlock() {
		return CustomItems.lamp;
	}
}
