package noppes.npcs.blocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import java.util.Random;

public abstract class BlockLightable extends BlockRotated {

	protected BlockLightable(Block block, boolean lit) {
		super(block);

        if (lit)
            this.setLightLevel(1.0F);
	}

	public abstract Block unlitBlock();
	public abstract Block litBlock();


    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9){
        // Get the current tile entity and store its NBT
        TileEntity oldTile = world.getTileEntity(x, y, z);
        NBTTagCompound compound = new NBTTagCompound();
        oldTile.writeToNBT(compound);

        // Determine which block to set (lit/unlit) based on the current one
        Block newBlock = (litBlock() == this ? unlitBlock() : litBlock());
        int meta = world.getBlockMetadata(x, y, z);

        // Change the block; this creates a new tile entity instance
        world.setBlock(x, y, z, newBlock, meta, 2);

        // Get the new tile and load the stored NBT
        TileEntity newTile = world.getTileEntity(x, y, z);
        if(newTile != null){
            newTile.readFromNBT(compound);
            newTile.markDirty();
        }

        world.markBlockForUpdate(x, y, z);
        world.notifyBlockOfNeighborChange(x, y, z, newBlock);

        return true;
    }

    @Override
    public Item getItemDropped(int p_149650_1_, Random p_149650_2_, int p_149650_3_){
        return Item.getItemFromBlock(litBlock());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Item getItem(World p_149694_1_, int p_149694_2_, int p_149694_3_, int p_149694_4_){
        return Item.getItemFromBlock(litBlock());
    }

    @Override
    protected ItemStack createStackedBlock(int p_149644_1_){
        return new ItemStack(litBlock());
    }

}
