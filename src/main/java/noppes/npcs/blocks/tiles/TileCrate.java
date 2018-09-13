package noppes.npcs.blocks.tiles;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;


public class TileCrate extends TileNpcContainer{

	@Override
	public String getName() {
		return "tile.npcCrate.name";
	}
    
}
