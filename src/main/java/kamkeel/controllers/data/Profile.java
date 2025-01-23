package kamkeel.controllers.data;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

import java.util.HashMap;
import java.util.Set;

public class Profile {

    public EntityPlayer player;
    public int currentID;
    public HashMap<Integer, Slot> slots = new HashMap<>();

    public Profile(EntityPlayer player, NBTTagCompound compound){
        this.player = player;
        if(compound.hasKey("CurrentID")){
            this.currentID = compound.getInteger("CurrentID");
        } else {
            this.currentID = 1;
        }

        if (compound.hasKey("Slots")) {
            // Load Slots
            NBTTagCompound slotsCompound = compound.getCompoundTag("Slots");
            for (String key : (Set<String>) slotsCompound.func_150296_c()) {
                int slotID = Integer.parseInt(key);
                NBTTagCompound slotNBT = slotsCompound.getCompoundTag(key);
                Slot slot = Slot.fromNBT(slotID, slotNBT);
                slots.put(slotID, slot);
            }
        }
    }

    public NBTTagCompound writeToNBT() {
        NBTTagCompound fullCompound = new NBTTagCompound();

        if(this.player != null)
            fullCompound.setString("Name", player.getCommandSenderName());
        fullCompound.setInteger("CurrentID", currentID);

        if(!slots.isEmpty()){
            NBTTagCompound slotsCompound = new NBTTagCompound();
            for (HashMap.Entry<Integer, Slot> entry : slots.entrySet()) {
                int id = entry.getKey();
                Slot slot = entry.getValue();
                slot.setId(id);

                NBTTagCompound slotTag = slot.toNBT();
                slotsCompound.setTag(String.valueOf(id), slotTag);
            }

            fullCompound.setTag("Slots", slotsCompound);
        }
        return fullCompound;
    }
}
