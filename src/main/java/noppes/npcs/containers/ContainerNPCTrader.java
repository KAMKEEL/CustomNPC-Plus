package noppes.npcs.containers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.RoleTrader;


public class ContainerNPCTrader extends ContainerNpcInterface{
	public RoleTrader role;

    public ContainerNPCTrader(EntityNPCInterface npc,EntityPlayer player){
    	super(player);
        role = (RoleTrader) npc.roleInterface;

        for(int i = 0; i < 18; i++){
        	int x =  53;
        	x += i%3 * 72;
        	int y = 7;
        	y += i/3 * 21;

			ItemStack item = role.inventoryCurrency.items.get(i);
			ItemStack item2 = role.inventoryCurrency.items.get(i + 18);
			if(item == null){
				item = item2;
				item2 = null;
			}
			addSlotToContainer(new Slot(role.inventorySold, i, x, y));
        }

        for(int i1 = 0; i1 < 3; i1++){
            for(int l1 = 0; l1 < 9; l1++){
            	addSlotToContainer(new Slot(player.inventory, l1 + i1 * 9 + 9, 32 + l1 * 18, 140 + i1 * 18));
            }

        }

        for(int j1 = 0; j1 < 9; j1++){
        	addSlotToContainer(new Slot(player.inventory, j1, 32 + j1 * 18, 198));
        }
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int i){
        return null;
    }
    @Override
    public ItemStack slotClick(int i, int j, int par3, EntityPlayer entityplayer){
    	if(par3 == 6)
    		par3 = 0;
    	if(i < 0 || i >= 18)
        	return super.slotClick(i, j, par3, entityplayer);
		if( j ==1 )
			return null;
        Slot slot = (Slot)inventorySlots.get(i);
        if(slot == null || slot.getStack() == null)
        	return null;
        ItemStack item = slot.getStack();
        if(!canGivePlayer(item, entityplayer))
        	return null;
        if(!canBuy(i, entityplayer))
        	return null;
        NoppesUtilPlayer.consumeItem(entityplayer, role.inventoryCurrency.getStackInSlot(i), role.ignoreDamage, role.ignoreNBT);
        NoppesUtilPlayer.consumeItem(entityplayer, role.inventoryCurrency.getStackInSlot(i + 18), role.ignoreDamage, role.ignoreNBT);
        ItemStack soldItem = item.copy();
        givePlayer(soldItem, entityplayer);
        return soldItem;
    	
    }
    public boolean canBuy(int slot, EntityPlayer player) {
		ItemStack currency = role.inventoryCurrency.getStackInSlot(slot);
		ItemStack currency2 = role.inventoryCurrency.getStackInSlot(slot + 18);
		if(currency == null && currency2 == null)
			return true;
		
		if(currency == null){
			currency = currency2;
			currency2 = null;
		}
		if(NoppesUtilPlayer.compareItems(currency, currency2, role.ignoreDamage, role.ignoreNBT)){
			currency = currency.copy();
			currency.stackSize += currency2.stackSize;
			currency2 = null;
		}
		if(currency2 == null )
			return NoppesUtilPlayer.compareItems(player, currency, role.ignoreDamage, role.ignoreNBT);
		return NoppesUtilPlayer.compareItems(player, currency, role.ignoreDamage, role.ignoreNBT) && NoppesUtilPlayer.compareItems(player, currency2, role.ignoreDamage, role.ignoreNBT);
		
    }

	private boolean canGivePlayer(ItemStack item,EntityPlayer entityplayer){//check Item being held with the mouse
        ItemStack itemstack3 = entityplayer.inventory.getItemStack();
        if(itemstack3 == null){
        	return true;
        }
        else if(NoppesUtilPlayer.compareItems(itemstack3, item, false, false)){
            int k1 = item.stackSize;
            if(k1 > 0 && k1 + itemstack3.stackSize <= itemstack3.getMaxStackSize())
            {
                return true;
            }
        }
        return false;
    }
    private void givePlayer(ItemStack item,EntityPlayer entityplayer){//set item bought to the held mouse item
        ItemStack itemstack3 = entityplayer.inventory.getItemStack();
        if(itemstack3 == null){
        	entityplayer.inventory.setItemStack(item);
        }
        else if(NoppesUtilPlayer.compareItems(itemstack3, item, false, false)){

            int k1 = item.stackSize;
            if(k1 > 0 && k1 + itemstack3.stackSize <= itemstack3.getMaxStackSize())
            {
                itemstack3.stackSize += k1;
            }
        }
    }
}
