package noppes.npcs.controllers.data;

import noppes.npcs.api.handler.IPlayerDialogData;
import noppes.npcs.api.INbt;
import noppes.npcs.api.INbtList;
import noppes.npcs.core.NBT;

import java.util.HashSet;

public class PlayerDialogData implements IPlayerDialogData {
    public HashSet<Integer> dialogsRead = new HashSet<Integer>();

    public PlayerDialogData() {
    }

    public void loadNBTData(INbt compound) {
        HashSet<Integer> dialogsRead = new HashSet<Integer>();
        if (compound == null)
            return;
        INbtList list = compound.getTagList("DialogData", 10);
        if (list == null) {
            return;
        }

        for (int i = 0; i < list.size(); i++) {
            INbt nbttagcompound = list.getCompound(i);
            dialogsRead.add(nbttagcompound.getInteger("Dialog"));
        }
        this.dialogsRead = dialogsRead;
    }

    public void saveNBTData(INbt compound) {
        INbtList list = NBT.list();
        for (int dia : dialogsRead) {
            INbt nbttagcompound = NBT.compound();
            nbttagcompound.setInteger("Dialog", dia);
            list.addCompound(nbttagcompound);
        }

        compound.setTagList("DialogData", list);
    }

    public boolean hasReadDialog(int id) {
        return dialogsRead.contains(id);
    }

    public void readDialog(int id) {
        dialogsRead.add(id);
    }

    public void unreadDialog(int id) {
        dialogsRead.remove(id);
    }
}
