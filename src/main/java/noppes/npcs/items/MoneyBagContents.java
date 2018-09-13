package noppes.npcs.items;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class MoneyBagContents {
	private EntityPlayer player;
	private int[] coinData = new int[]{0,0,0,0,0,0,0};

	public MoneyBagContents(EntityPlayer player) {
		this.player = player;
	}
	
	public void readNBT(NBTTagCompound compound){
		coinData = compound.getIntArray("coins");
	}
	
	public NBTTagCompound writeNBT(){
		NBTTagCompound  compound = new NBTTagCompound();
		compound.setIntArray("coins", coinData);
		return compound;
	}
	
	
	public enum CoinType {
		WOOD,STONE,IRON,GOLD,DIAMOND,BRONZE,EMERALD;	
	}
	
	
	//Adds stackSize to coinType
	public void AddCurrency(CoinType coinType, byte stackSize, ItemStack theBag){
		coinData[coinType.ordinal()] = coinData[coinType.ordinal()] + stackSize;
		theBag.stackTagCompound.setTag("contents", writeNBT());
	}
	
	//Creates amount number of coinType in playerInventory and subtracts from the moneyBag.
	public void WithdrawCurrencyByVal(CoinType coinType, short amount, ItemStack theBag) {
		int amtAdded = 0;
		
		coinData[coinType.ordinal()] = coinData[coinType.ordinal()]- amtAdded;
		theBag.stackTagCompound.setTag("contents", writeNBT());
	}
	
	//Creates numStacks of coinType in recievingInventory and subtracts from the moneyBag.
	public void WithdrawCurrencyByStack(CoinType coinType, byte numStacks, ItemStack theBag) {
		int amtAdded = 0;
		//check inventory space
		
		coinData[coinType.ordinal()] = coinData[coinType.ordinal()]- amtAdded;
		theBag.stackTagCompound.setTag("contents", writeNBT());
	}
}
