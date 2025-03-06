package noppes.npcs.controllers.data;

import net.minecraft.nbt.NBTTagCompound;

public class MagicEntry {
    public float damage;
    public float split;

    public NBTTagCompound writeToNBT() {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setFloat("Dmg", damage);
        compound.setFloat("Split", split);
        return compound;
    }

    public void readToNBT(NBTTagCompound compound) {
        damage = compound.getFloat("Dmg");
        split = compound.getFloat("Split");
    }
}
