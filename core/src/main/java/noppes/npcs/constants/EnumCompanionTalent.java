package noppes.npcs.constants;


import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IEntityLiving;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.IDamageSource;
import noppes.npcs.api.INbt;
import noppes.npcs.api.INbtList;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.IWorld;
import noppes.npcs.CustomItems;

public enum EnumCompanionTalent {
    INVENTORY(CustomItems.satchel), ARMOR(Items.iron_chestplate),
    SWORD(Items.diamond_sword), RANGED(Items.bow),
    ACROBATS(Items.leather_boots), INTEL(CustomItems.letter);

    public IItemStack item;

    private EnumCompanionTalent(Item item) {
        this.item = new IItemStack(item);
    }
}
