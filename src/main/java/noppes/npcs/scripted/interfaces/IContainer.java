//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package noppes.npcs.scripted.interfaces;

import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import noppes.npcs.scripted.interfaces.entity.IPlayer;
import noppes.npcs.scripted.interfaces.item.IItemStack;

public interface IContainer {
    int getSize();

    IItemStack getSlot(int slot);

    /**
     *
     * @param slot The slot to be replaced
     * @param item The item replacing the previous item in that slot, as an IItemStack object
     */
    void setSlot(int slot, IItemStack item);

    /**
     *
     * @return Returns a non-script, obfuscated MC inventory object.
     */
    IInventory getMCInventory();

    /**
     *
     * @return Returns a non-script, obfuscated MC container object.
     */
    Container getMCContainer();

    /**
     *
     * @param itemStack The item stack to be searched in the container
     * @param ignoreDamage Whether damage should be ignored when searching
     * @param ignoreNBT Whether NBT values should be ignored when searching
     * @return Returns the amount of the item stack found, based on the flags given above.
     */
    int count(IItemStack itemStack, boolean ignoreDamage, boolean ignoreNBT);

    /**
     *
     * @return Returns a list of all item stacks in the container as a list of IItemStack objects.
     */
    IItemStack[] getItems();

    /**
     *
     * @return Returns true if this container belongs to a Custom GUI.
     */
    boolean isCustomGUI();

    void detectAndSendChanges();

    void putStackInSlot(int slot, IItemStack itemStack);

    boolean isPlayerNotUsingContainer(IPlayer player);
}
