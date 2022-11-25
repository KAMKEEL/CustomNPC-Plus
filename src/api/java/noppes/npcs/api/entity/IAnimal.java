//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package noppes.npcs.api.entity;

import net.minecraft.entity.passive.EntityAnimal;
import noppes.npcs.api.item.IItemStack;

public interface IAnimal<T extends EntityAnimal> extends IEntityLiving<T> {
    /**
     * Checks if the parameter is an item which this animal can be fed to breed it (wheat, carrots or seeds depending on
     * the animal type)
     */
    boolean isBreedingItem(IItemStack itemStack);

    /**
     * Called when a player interacts with a mob. e.g. gets milk from a cow, gets into the saddle on a pig.
     */
    boolean interact(IPlayer player);

    boolean isInLove();

    void resetInLove();

    boolean canMateWith(IAnimal animal);
}
