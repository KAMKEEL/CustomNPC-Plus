package noppes.npcs.controllers.data;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.api.handler.IPlayerBankData;
import noppes.npcs.controllers.BankController;

import java.util.HashMap;

public class PlayerBankData implements IPlayerBankData {
    private final PlayerData parent;
    public HashMap<Integer, BankData> banks;

    public PlayerBankData(PlayerData parent) {
        banks = new HashMap<Integer, BankData>();
        this.parent = parent;
    }

    public void loadNBTData(NBTTagCompound compound) {
        HashMap<Integer, BankData> banks = new HashMap<Integer, BankData>();
        NBTTagList list = compound.getTagList("BankData", 10);
        if (list == null) {
            return;
        }
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound nbttagcompound = list.getCompoundTagAt(i);
            BankData data = new BankData();
            data.readNBT(nbttagcompound);
            banks.put(data.bankId, data);
        }
        this.banks = banks;
    }

    public void saveNBTData(NBTTagCompound playerData) {
        NBTTagList list = new NBTTagList();
        for (BankData data : banks.values()) {
            NBTTagCompound nbttagcompound = new NBTTagCompound();
            data.writeNBT(nbttagcompound);
            list.appendTag(nbttagcompound);
        }
        playerData.setTag("BankData", list);
    }


    public BankData getBank(int bankId) {
        return banks.get(bankId);
    }

    public BankData getBankOrDefault(int bankId) {
        BankData data = banks.get(bankId);
        if (data != null)
            return data;
        Bank bank = BankController.getInstance().getBank(bankId);
        return banks.get(bank.id);
    }

    public boolean hasBank(int bank) {
        return banks.containsKey(bank);
    }

    public void loadNew(int bank) {
        BankData data = new BankData();
        data.bankId = bank;
        banks.put(bank, data);

    }
}
