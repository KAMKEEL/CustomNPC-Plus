package noppes.npcs.controllers.data;

import noppes.npcs.platform.nbt.INBTCompound;
import noppes.npcs.core.NBT;

public class MagicEntry {
    public float damage;
    public float split;

    public INBTCompound writeToNBT() {
        INBTCompound compound = NBT.compound();
        compound.setFloat("Dmg", damage);
        compound.setFloat("Split", split);
        return compound;
    }

    public void readToNBT(INBTCompound compound) {
        damage = compound.getFloat("Dmg");
        split = compound.getFloat("Split");
    }
}
