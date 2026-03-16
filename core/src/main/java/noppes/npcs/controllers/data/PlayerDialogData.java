package noppes.npcs.controllers.data;


import java.util.HashSet;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IEntityLiving;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.handler.IPlayerDialogData;
import noppes.npcs.api.IDamageSource;
import noppes.npcs.api.INbt;
import noppes.npcs.api.INbtList;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.IWorld;
import noppes.npcs.core.NBT;

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
            INbt INbt = list.getCompound(i);
            dialogsRead.add(INbt.getInteger("Dialog"));
        }
        this.dialogsRead = dialogsRead;
    }

    public void saveNBTData(INbt compound) {
        INbtList list = NBT.list();
        for (int dia : dialogsRead) {
            INbt INbt = NBT.compound();
            INbt.setInteger("Dialog", dia);
            list.addCompound(INbt);
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
