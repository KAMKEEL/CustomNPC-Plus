package noppes.npcs.controllers.data;

import kamkeel.npcs.network.PacketUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.NBTTags;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.NpcMiscInventory;
import noppes.npcs.Server;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.containers.ContainerNPCBankInterface;
import noppes.npcs.controllers.BankController;
import noppes.npcs.entity.EntityNPCInterface;

import java.util.HashMap;

public class BankData {
	public HashMap<Integer,NpcMiscInventory> itemSlots;
	public HashMap<Integer,Boolean> upgradedSlots;
	public int unlockedSlots = 0;
	public int bankId = -1;

	public BankData(){
		itemSlots = new HashMap<Integer,NpcMiscInventory>();
		upgradedSlots = new HashMap<Integer,Boolean>();

		for(int i = 0; i < 6; i++){
			itemSlots.put(i, new NpcMiscInventory(54));
			upgradedSlots.put(i, false);
		}
	}
	public void readNBT(NBTTagCompound nbttagcompound) {
		bankId = nbttagcompound.getInteger("DataBankId");
        unlockedSlots = nbttagcompound.getInteger("UnlockedSlots");
        itemSlots = getItemSlots(nbttagcompound.getTagList("BankInv", 10));
        upgradedSlots = NBTTags.getBooleanList(nbttagcompound.getTagList("UpdatedSlots", 10));
	}
	private HashMap<Integer, NpcMiscInventory> getItemSlots(NBTTagList tagList) {
		HashMap<Integer,NpcMiscInventory> list = new HashMap<Integer,NpcMiscInventory>();
        for(int i = 0; i < tagList.tagCount(); i++)
        {
            NBTTagCompound nbttagcompound = tagList.getCompoundTagAt(i);
            int slot = nbttagcompound.getInteger("Slot");
            NpcMiscInventory inv = new NpcMiscInventory(54);
            inv.setFromNBT(nbttagcompound.getCompoundTag("BankItems"));
            list.put(slot, inv);
        }
		return list;
	}
	public void writeNBT(NBTTagCompound nbttagcompound) {
		nbttagcompound.setInteger("DataBankId", bankId);
		nbttagcompound.setInteger("UnlockedSlots", unlockedSlots);
		nbttagcompound.setTag("UpdatedSlots", NBTTags.nbtBooleanList(upgradedSlots));
		nbttagcompound.setTag("BankInv", nbtItemSlots(itemSlots));
	}
	private NBTTagList nbtItemSlots(HashMap<Integer, NpcMiscInventory> items) {
		NBTTagList list = new NBTTagList();
		for(int slot: items.keySet()){
			NBTTagCompound nbttagcompound = new NBTTagCompound();
			nbttagcompound.setInteger("Slot", slot);

			nbttagcompound.setTag("BankItems",items.get(slot).getToNBT());
			list.appendTag(nbttagcompound);
		}
		return list;
	}
	public boolean isUpgraded(Bank bank, int slot) {
		if(bank.isUpgraded(slot))
			return true;
		return bank.canBeUpgraded(slot) && upgradedSlots.get(slot);
	}

	public void openBankGui(final EntityPlayer player, EntityNPCInterface npc, int bankId, int slot) {
		final Bank bank = BankController.getInstance().getBank(bankId);

		if(bank.getMaxSlots() <= slot)
			return;

    	if(bank.startSlots > unlockedSlots)
    		unlockedSlots = bank.startSlots;

    	ItemStack currency = null;
    	if(unlockedSlots <= slot){
    		currency = bank.currencyInventory.getStackInSlot(slot);
    		NoppesUtilServer.sendOpenGui(player, EnumGuiType.PlayerBankUnlock, npc, slot, bank.id, 0);
    	}
    	else if(isUpgraded(bank,slot)){
    		NoppesUtilServer.sendOpenGui(player, EnumGuiType.PlayerBankLarge, npc, slot, bank.id, 0);
    	}
    	else if(bank.canBeUpgraded(slot)){
    		currency = bank.upgradeInventory.getStackInSlot(slot);
    		NoppesUtilServer.sendOpenGui(player, EnumGuiType.PlayerBankUprade, npc, slot, bank.id, 0);
    	}
    	else{
    		NoppesUtilServer.sendOpenGui(player, EnumGuiType.PlayerBankSmall, npc, slot, bank.id, 0);
    	}
		final ItemStack item = currency;
		NBTTagCompound compound = new NBTTagCompound();
		compound.setInteger("MaxSlots", bank.getMaxSlots());
		compound.setInteger("UnlockedSlots", unlockedSlots);
		if (item != null) {
			compound.setTag("Currency", NoppesUtilServer.writeItem(item, new NBTTagCompound()));
			ContainerNPCBankInterface container = getContainer(player);
			if (container != null)
				container.setCurrency(item);
		}

        PacketUtil.sendGuiData((EntityPlayerMP) player, compound);
	}
	private ContainerNPCBankInterface getContainer(EntityPlayer player){
		Container con = player.openContainer;
		if(con == null || !(con instanceof ContainerNPCBankInterface))
			return null;

		return (ContainerNPCBankInterface) con;
	}
}
