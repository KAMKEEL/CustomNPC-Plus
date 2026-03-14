package noppes.npcs.controllers.data;

import noppes.npcs.api.INbt;
import noppes.npcs.core.NBT;

public class MagicEntry {
    public float damage;
    public float split;

    public INbt writeToNBT() {
        INbt compound = NBT.compound();
        compound.setFloat("Dmg", damage);
        compound.setFloat("Split", split);
        return compound;
    }

    public void readToNBT(INbt compound) {
        damage = compound.getFloat("Dmg");
        split = compound.getFloat("Split");
    }
}
