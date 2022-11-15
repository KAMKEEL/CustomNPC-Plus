//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package noppes.npcs.api;

import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.item.IItemStack;

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
     * @return An obfuscated MC inventory object.
     */
    IInventory getMCInventory();

    /**
     *
     * @return An obfuscated MC container object.
     */
    Container getMCContainer();

    /**
     *
     * @param itemStack The item stack to be searched in the container
     * @param ignoreDamage Whether damage should be ignored when searching
     * @param ignoreNBT Whether NBT values should be ignored when searching
     * @return The amount of the item stack found, based on the flags given above.
     */
    int count(IItemStack itemStack, boolean ignoreDamage, boolean ignoreNBT);

    /**
     *
     * @return A list of all item stacks in the container as a list of IItemStack objects.
     */
    IItemStack[] getItems();

    /**
     *
     * @return True if this container belongs to a Custom GUI.
     */
    boolean isCustomGUI();

    /**
     * Sends changes to be reflected on the player's client.
     */
    void detectAndSendChanges();

    boolean isPlayerNotUsingContainer(IPlayer player);
}
